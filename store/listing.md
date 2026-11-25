# Google Play store listing

Paste these into **Grow users → Store presence → Main store listing**.

## App name (max 30)

```
Bible for Lola
```

## Short description (max 80)

```
Simple, ad-free Bible in English (KJV) and Tagalog. Big text and read aloud.
```

## Full description (max 4000)

```
A simple, peaceful Bible app made for grandparents and anyone who wants to just read, with nothing in the way.

Bible for Lola has the complete Old and New Testament, all 66 books, in two languages:
• English – King James Version (KJV)
• Tagalog – Ang Dating Biblia (1905)

Switch between English and Tagalog with one tap. The menus change language too.

MADE TO BE EASY
• Big, clear letters you can make even bigger (A+ / A−)
• Large buttons and a simple layout: pick a book, pick a chapter, read
• Opens right where you stopped reading last time
• Big "Previous" and "Next" buttons to turn the chapter
• Read aloud: the phone reads the chapter to you
• Dark mode for reading at night (follows your phone setting)

NOTHING ELSE
• No ads, ever
• No account or sign-in
• Works completely offline, no internet needed
• Collects no data at all

Made with love for our Lola, and shared for every lola and lolo who loves reading God's Word.

Both Bible texts are in the public domain.
```

## Graphics

| Asset | File | Requirement |
|---|---|---|
| App icon | `icon-512.png` | 512 × 512 PNG |
| Feature graphic | `feature-graphic-1024x500.jpg` | 1024 × 500 JPG/PNG, no transparency |
| Phone screenshots | `screenshots/*.png` | 2–8 images, 1080 × 1920 |

Upload screenshots in this order:
1. `01_home_en.png`: home, English
2. `02_reader_psalm23_en.png`: Psalm 23, English
3. `03_chapters_juan_tl.png`: chapter picker, Tagalog
4. `04_reader_juan3_tl.png`: Juan 3, Tagalog
5. `05_reader_juan3_tl_dark.png`: Juan 3:16, dark mode
6. `06_home_tl_dark.png`: home, Tagalog, dark mode

`src/` holds the HTML the icon and feature graphic are rendered from (headless Chrome).
