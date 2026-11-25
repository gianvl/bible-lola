#!/usr/bin/env python3
"""Build the Bible text assets bundled in the Android app.

Sources (both public domain):
  * KJV  - eBible.org "eng-kjv2006" verse-per-line file
           https://ebible.org/Scriptures/eng-kjv2006_vpl.zip
  * ADB  - CrossWire SWORD module "TagAngBiblia" (Ang Dating Biblia, 1905)
           https://crosswire.org/ftpmirror/pub/sword/packages/rawzip/TagAngBiblia.zip

Output: app/src/main/assets/bible/<translation>/<NN>.json, where NN is the
1-based book number (01 = Genesis ... 66 = Revelation) and each file holds a
JSON array of chapters, each chapter an array of verse strings.

Usage:
  python3 -I tools/build_bible_assets.py <kjv_vpl.txt> <TagAngBiblia dir> <assets/bible dir>
"""
from __future__ import annotations

import json
import re
import struct
import sys
import zlib
from pathlib import Path

# Canonical Protestant order, as used by the eBible VPL book codes.
BOOK_CODES = [
    "GEN", "EXO", "LEV", "NUM", "DEU", "JOS", "JDG", "RUT", "1SA", "2SA",
    "1KI", "2KI", "1CH", "2CH", "EZR", "NEH", "EST", "JOB", "PSA", "PRO",
    "ECC", "SOL", "ISA", "JER", "LAM", "EZE", "DAN", "HOS", "JOE", "AMO",
    "OBA", "JON", "MIC", "NAH", "HAB", "ZEP", "HAG", "ZEC", "MAL",
    "MAT", "MAR", "LUK", "JOH", "ACT", "ROM", "1CO", "2CO", "GAL", "EPH",
    "PHI", "COL", "1TH", "2TH", "1TI", "2TI", "TIT", "PHM", "HEB", "JAM",
    "1PE", "2PE", "1JO", "2JO", "3JO", "JUD", "REV",
]
OT_BOOK_COUNT = 39

VPL_LINE = re.compile(r"^(\w{3}) (\d+):(\d+) (.*)$")
WHITESPACE = re.compile(r"\s+")


def clean(text: str) -> str:
    # [word] marks italic "supplied" words in the KJV; keep the word, drop the brackets.
    text = text.replace("[", "").replace("]", "").replace("¶", "")
    return WHITESPACE.sub(" ", text).strip()


def read_kjv(path: Path) -> list[list[list[str]]]:
    books: dict[str, dict[int, dict[int, str]]] = {code: {} for code in BOOK_CODES}
    for line_no, raw in enumerate(path.read_text(encoding="utf-8-sig").splitlines(), 1):
        if not raw.strip():
            continue
        match = VPL_LINE.match(raw)
        if not match:
            raise ValueError(f"{path}:{line_no}: unrecognised line: {raw[:60]!r}")
        code, chapter, verse, text = match.groups()
        books[code].setdefault(int(chapter), {})[int(verse)] = clean(text)

    result = []
    for code in BOOK_CODES:
        chapters = books[code]
        if sorted(chapters) != list(range(1, len(chapters) + 1)):
            raise ValueError(f"{code}: chapters are not contiguous")
        book = []
        for number in range(1, len(chapters) + 1):
            verses = chapters[number]
            if sorted(verses) != list(range(1, len(verses) + 1)):
                raise ValueError(f"{code} {number}: verses are not contiguous")
            book.append([verses[v] for v in range(1, len(verses) + 1)])
        result.append(book)
    return result


class ZText:
    """Minimal reader for a SWORD zText (BlockType=BOOK, CompressType=ZIP) testament."""

    def __init__(self, directory: Path, testament: str):
        self.blocks = (directory / f"{testament}.bzs").read_bytes()
        self.index = (directory / f"{testament}.bzv").read_bytes()
        self.data = (directory / f"{testament}.bzz").read_bytes()
        self._cache: dict[int, bytes] = {}

    def _block(self, number: int) -> bytes:
        if number not in self._cache:
            offset, size, _ = struct.unpack_from("<III", self.blocks, number * 12)
            self._cache[number] = zlib.decompress(self.data[offset:offset + size])
        return self._cache[number]

    def entry(self, index: int) -> str:
        block, offset, size = struct.unpack_from("<IIH", self.index, index * 10)
        if size == 0:
            return ""
        return self._block(block)[offset:offset + size].decode("utf-8")


def read_sword(directory: Path, shape: list[list[int]]) -> list[list[list[str]]]:
    """Read a KJV-versified zText module, using `shape` (verse counts per chapter)."""
    result = []
    for testament, books in (("ot", shape[:OT_BOOK_COUNT]), ("nt", shape[OT_BOOK_COUNT:])):
        reader = ZText(directory, testament)
        # SWORD index layout: 0 = module heading, 1 = testament heading, then for each
        # book a book heading followed, per chapter, by a chapter heading and its verses.
        # `index` always points at the last entry consumed.
        index = 1
        for verse_counts in books:
            index += 1  # book heading
            book = []
            for count in verse_counts:
                index += 1  # chapter heading
                book.append([clean(reader.entry(index + v)) for v in range(1, count + 1)])
                index += count
            result.append(book)
        expected_entries = len(reader.index) // 10
        if index + 1 != expected_entries:
            raise ValueError(f"{testament}: consumed {index + 1} of {expected_entries} index entries")
    return result


def validate(name: str, bible: list[list[list[str]]]) -> None:
    empty = [
        f"{BOOK_CODES[b]} {c + 1}:{v + 1}"
        for b, book in enumerate(bible)
        for c, chapter in enumerate(book)
        for v, verse in enumerate(chapter)
        if not verse
    ]
    total = sum(len(ch) for book in bible for ch in book)
    print(f"{name}: {len(bible)} books, {sum(map(len, bible))} chapters, {total} verses, {len(empty)} empty")
    if empty:
        print(f"  empty verses: {', '.join(empty[:20])}{' ...' if len(empty) > 20 else ''}")


def write(bible: list[list[list[str]]], out_dir: Path) -> None:
    out_dir.mkdir(parents=True, exist_ok=True)
    for number, book in enumerate(bible, 1):
        (out_dir / f"{number:02d}.json").write_text(
            json.dumps(book, ensure_ascii=False, separators=(",", ":")), encoding="utf-8"
        )


def main() -> None:
    if len(sys.argv) != 4:
        sys.exit(__doc__)
    kjv_path, sword_dir, out_root = (Path(arg) for arg in sys.argv[1:])

    kjv = read_kjv(kjv_path)
    shape = [[len(chapter) for chapter in book] for book in kjv]
    adb = read_sword(sword_dir, shape)

    validate("kjv", kjv)
    validate("adb", adb)
    write(kjv, out_root / "kjv")
    write(adb, out_root / "adb")


if __name__ == "__main__":
    main()
