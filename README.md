# Hoja (葉 / 論) — Debate & Rhetoric Studio

**Hoja** (*Swahili for "a compelling argument"*) is an Android debate sparring and adjudication app crafted in a **Japanese hand-drawn Washi & Sumi storybook aesthetic**. Guided by **Mwalimu Kori**—an evolving Secretary Bird debate coach—users receive daily East African and global debate motions, choose to **Propose** or **Oppose**, spar in a live 3-round chat debate against an AI opponent, and receive a comprehensive **Chief Adjudicator Analysis** that pinpoints where their reasoning went wrong and how to rewrite their weakest argument.

---

## ✨ Key Features

### 1. Japanese Hand-Drawn Washi & Sumi Aesthetic
- **Dual-Mode Palette (WCAG AA/AAA Verified)**:
  - **Daylight Washi (Light Mode)**: Warm cream rice paper (`#F9F4E8`), sumi ink (`#221D1A`), and 5 pastel gouache washes (Sage, Vermilion Coral, Sky, Golden Ochre, and Lavender Mist).
  - **Sumi Night (Dark Mode)**: Deep charcoal slate (`#141210`) with warm parchment ink (`#F5EFE4`) and rich muted jewel-tone washes.
- **Custom Vector Illustrations & Seamless Stage**:
  - Hand-drawn `Canvas` illustrations for **Mwalimu Kori** with 6-quill occipital crests, anime-expressive eyes with specular catchlights, and rank-specific outfits.
  - Continuous Duolingo-style scenic panorama backdrop with acacia silhouettes, distant Rift Valley hills, and washi paper grain.

### 2. Beginner-to-Champion Progression System
Users begin their journey at **Beginner** and automatically advance by earning qualifying Adjudicator scores:
1. **Beginner (*Fledgling Kori*)**: Mint-sage collar & brass whistle — *Score 7.0+ in 2 debates to advance*.
2. **Intermediate (*Scholar Kori*)**: Tortoiseshell half-rim spectacles & olive tie — *Score 7.5+ in 2 debates to advance*.
3. **Advanced (*Master Kori*)**: Crimson-gold savannah cape & adjudicator lapel pin — *Score 8.0+ in 3 debates to advance*.
4. **Champion (*Grand Sensei Kori*)**: Golden olive laurels, ceremonial haori & floating ink scroll — *Highest rank achieved*.

### 3. Consecutive Daily Usage Streak System
- Tracks consecutive days of app usage automatically in both **Room** (`daily_app_usage`) and **SharedPreferences**.
- Displays a custom hand-drawn **Current Streak** badge (`CurrentStreakDashboardBadge`) on the main dashboard with an animated **Sumi & Vermilion Flame Medallion**, 7-day momentum dots, active status pill, and personal best counter.

### 4. Live 3-Round Debate Chat & AI Chief Adjudicator
- **Motion Rerolls**: Instant Gemini-powered motion rerolling with an avoid-list of previously seen motions so every roll is fresh.
- **3-Round Debate Flow**: Opening (`Round 1/3`), Rebuttal (`Round 2/3`), and Closing (`Round 3/3`) with round-aware sentence starters and expandable model rebuttals.
- **Post-Debate AI Adjudicator**:
  - **Where You Went Wrong**: Specific logical blindspots and missed rebuttals.
  - **How to Fix Your Weakest Line**: Side-by-side comparison of what the user wrote vs. a stronger tournament-grade rewrite.
  - **Rubric Breakdown**: Scores out of 10 for *Argument & Logic*, *Direct Rebuttal*, *Evidence & Examples*, and overall performance.

### 5. Room Database History & Transcript Review
- Saves every completed debate session (`DebateSessionEntity`) locally in **Room** (`hoja_debate.db`), including both the full chat transcript (`transcriptJson`) and the AI Adjudicator evaluation.
- Dedicated **Debate History** screen lets users browse past debates, inspect the saved **AI Analysis** or toggle to the full **Saved Transcript** turn-by-turn, and delete old sessions.

---

## 🏗️ Architecture & Tech Stack

Built following **Clean MVVM Architecture** and the **Ponytail Minimalism Philosophy** (YAGNI, zero dead code, component reuse):

- **UI Layer**: 100% Jetpack Compose + Material Design 3 + Custom `Canvas` Vector Art (`OnboardingAndHomeScreens.kt`, `PrepAndDebateScreens.kt`, `ResultsLessonsSettingsScreens.kt`, `SecretaryBirdMascot.kt`).
- **State Management**: `HojaViewModel` with `StateFlow` and `collectAsStateWithLifecycle`.
- **Local Persistence (Room + SharedPreferences)**:
  - `HojaDatabase` (`version = 2`):
    - `cached_daily_motions` (`CachedDailyMotionEntity`)
    - `debate_sessions` (`DebateSessionEntity`)
    - `daily_app_usage` (`DailyUsageEntity`)
  - `HojaRepository`: Single source of truth for streak calculation (`calculateUpdatedStreak`), rank progression, motion caching, and transcript serialization.
- **AI Layer (Firebase AI Logic / Gemini)**:
  - `gemini-3.5-flash-lite` for low-latency motion generation, motion rerolls, and live opponent round replies.
  - `gemini-3.8-flash` for deep end-of-debate Chief Adjudicator analysis and structured JSON scoring.

---

## 🚀 Building & Running Locally

### Prerequisites
- **JDK 17+**
- **Android SDK (API 36)**
- Firebase configuration (`app/google-services.json` is pre-configured)

### Build Debug APK
```bash
gradle assembleDebug
```
The compiled debug APK is generated at:
`app/build/outputs/apk/debug/app-debug.apk` (and mirrored to `.build-outputs/app-debug.apk`).

### Run Unit Tests
```bash
gradle :app:testDebugUnitTest --tests "com.example.ExampleUnitTest"
```
Verifies WCAG AA/AAA contrast ratios across both Light and Dark storybook palettes, consecutive-day streak calculations, and rank progression rules.
