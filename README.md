# MahirVerse

Android study management app built with Kotlin + Jetpack Compose.

## Features
- **Syllabus Tracker** — subjects, topics, subtopics with progress tracking
- **Study Planner** — daily/weekly/monthly plans with inline editor
- **Focus Timer** — deep focus sessions with ambient sounds
- **Mock Tests** — Analysis Planner with error pattern tracking, smart recommendations, topic weightage
- **Analytics** — study history, streaks, achievements
- **Backup & Sync** — Supabase + Firebase integration

## Building the APK

### Option 1: GitHub Actions (recommended)

The repo includes a GitHub Actions workflow at `.github/workflows/build-apk.yml`
that builds a debug APK on every push to `main`/`master`. The APK is uploaded
as a build artifact you can download from the Actions tab.

> **Note:** The workflow file needs to be created via the GitHub web UI if your
> PAT doesn't have `workflow` scope. The workflow content is saved as
> `workflow-build-apk.yml.txt` in the repo root — copy its contents into
> `.github/workflows/build-apk.yml` via the web UI (Add file → Create new file).

### Option 2: Local build

Requirements: JDK 17, Android SDK 35, Gradle 9.3.1

```bash
# Set up local.properties with your SDK path
echo "sdk.dir=/path/to/Android/Sdk" > local.properties

# Create .env for the Secrets Gradle Plugin
cp .env.example .env

# Build
./gradlew assembleDebug --no-daemon --max-workers=1
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

## Database version 11

The app uses Room with migrations. Current schema version is 11 (upgraded from
10 in this release). The v10→v11 migration:
- Drops the old `mock_attempts` table
- Recreates `mock_tests` with a flat schema (category, marksObtained, correctCount,
  wrongCount, unattemptedCount, actualDurationSeconds, percentile, rank,
  totalCandidates, attemptedAt, description, tags)
- Adds the new `mock_questions` table for per-question error tracking
- Creates indices on mockTestId, subjectName, topicName, errorCategory, category,
  attemptedAt

This is a destructive migration for mock data only — all other tables
(syllabus, revisions, focus sessions, plans) are preserved.

## Installation

```bash
# Uninstall any previous version first (the DB migration is destructive for mocks)
adb uninstall com.aistudio.mahirverse.xjklqa

# Install the new APK
adb install app-debug.apk
```
