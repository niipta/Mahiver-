# MahirVerse

> Master your study, master your life.

A clean, minimal, and **premium** study management app for Android, built with Kotlin + Jetpack Compose.

![MahirVerse Banner](https://ai.google.dev/static/site-assets/images/share-ais-513315318.png)

## Features

- **Focus Timer (Pomodoro)** — Foreground service with DND auto-enable, ambient sound support, deep-focus mode, and rich notifications.
- **Syllabus Tracker** — Subjects → Topics → Subtopics with priority/weak markers, custom colors, and completion tracking.
- **Spaced Repetition** — SM-2-inspired scheduler (1 → 3 → 7 → 15 → 30 → 60 → 120 days) that doesn't terminate at level 4.
- **AI Study Planner** — Gemini-powered daily plan generator that surfaces suggested topics, weak areas, and priorities.
- **Analytics** — 7/30/90-day charts, subject-wise breakdown, productivity score, radar balance chart, PNG export.
- **Streaks & Achievements** — Daily goal tracker (configurable minutes/topics), streak freezes, 14+ achievements with confetti unlock overlays.
- **Backup / Restore** — AES-256-GCM encrypted local backups via Android Keystore.
- **Cloud Sync** — Firestore sync queue for syllabus/exam/revision/planner changes.
- **Home-Screen Widget** — Glance-based widget showing today's goal + streak.
- **Premium UI** — Fraunces + Inter typography, refined gold-on-ink palette, AMOLED mode, dynamic color (Material You), spring animations, haptic feedback throughout.

## Tech Stack

| Layer        | Choice                                                |
|--------------|-------------------------------------------------------|
| Language     | Kotlin 2.0.21                                         |
| UI           | Jetpack Compose + Material 3                          |
| DI           | Hilt 2.51.1                                           |
| Database     | Room 2.7 (with migrations, schema export)             |
| Sync         | Firebase (Auth, Firestore, Messaging)                 |
| AI           | Gemini API via Retrofit + Moshi                       |
| Background   | WorkManager + Foreground Service                      |
| Widget       | Glance 1.1                                            |

## Run Locally

**Prerequisites:** Android Studio (Hedgehog or newer), JDK 17.

1. Clone / extract this project.
2. Open in Android Studio → **Open** the project root.
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project root and set:
   ```bash
   GEMINI_API_KEY=your_gemini_api_key_here
   SUPABASE_URL=your_supabase_url
   SUPABASE_KEY=your_supabase_anon_key
   ```
5. Run on an emulator or physical device (min SDK 24, target SDK 35).

## Premium Look & Feel

The UI has been redesigned with the following principles:

- **Refined palette**: warm off-white background (#FAF9F7) with deep ink-black cards (#0A0B10) and a less-saturated gold accent (#B08433 / #D4A853).
- **Typography**: Fraunces serif for headlines (premium editorial feel), Inter for body text.
- **Spacing**: 4-pt grid system enforced via `Dimens` tokens for consistency across all screens.
- **Motion**: Staggered `AnimatedEntry` on every screen, spring-animated bottom-nav indicator, animated counters on stat cards, confetti on achievement unlock.
- **Haptics**: Differentiated `tap` / `confirm` / `reject` / `success` patterns, all gated by a user setting.
- **Dark mode**: True black AMOLED option + dynamic Material You color support on Android 12+.

## Architecture

```
com.example/
├── MainActivity.kt           # NavHost + work scheduling
├── MahirVerseApplication.kt  # Hilt entry
├── data/                     # Room, DAOs, repositories, AI engine, sync
│   ├── ai/                   # Gemini API client
│   ├── sync/                 # Firestore sync queue
│   └── ...
├── domain/                   # Pure logic: Streak, Achievements, Completion
├── service/                  # FocusService, StreakWorker, etc.
├── ui/
│   ├── theme/                # Color, Type, Theme, Dimens, StatColors
│   ├── components/           # Shared composables (MahirCard, EmptyState, ...)
│   ├── home/                 # Daily goal ring + stats + quick actions
│   ├── focus/                # Pomodoro timer
│   ├── syllabus/             # Subject tree
│   ├── revision/             # Spaced repetition queue
│   ├── planner/              # Today/Tomorrow/Month tabs
│   ├── analytics/            # Charts + export
│   ├── history/              # Session log
│   ├── achievements/         # Badge grid
│   ├── backup/               # Export/import
│   ├── more/                 # Settings
│   └── onboarding/           # 4-page intro
├── util/                     # Haptics, SecurityUtil
└── widget/                   # Glance widget
```

## Privacy

All study data is stored **locally** in Room. Sync to Firestore is opt-in via Firebase Auth. Backups are AES-256-GCM encrypted with keys held in Android Keystore.

---

Built with care. Master your study, master your life.
