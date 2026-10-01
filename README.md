# FitTrack — Fitness Tracker App

FitTrack is a native Android application that allows users to record their daily fitness activities and monitor their progress against personal goals. It was developed as **Task 3 (Fitness Tracker App)** of the **CodeAlpha App Development Internship**.

---

## Table of Contents

1. [Features](#features)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [Prerequisites](#prerequisites)
5. [How to Use the App](#how-to-use-the-app)
6. [Data Storage](#data-storage)
7. [Design and Reliability Notes](#design-and-reliability-notes)
8. [Future Enhancements](#future-enhancements)
9. [Author](#author)

---

## Features

- **Manual activity logging** — Record the exercise type, date, workout duration (minutes), calories burned and steps for each session.
- **Daily dashboard** — Progress bars show today's totals for steps, calories and active minutes relative to the user's daily goals.
- **Weekly summary** — A 7-day bar chart visualises steps, calories or active minutes (selectable), accompanied by weekly totals and a workout count.
- **Editable daily goals** — Goals for steps, calories and active minutes can be customised at any time.
- **Activity history** — A list of recent entries, with the option to delete any individual entry after confirmation.
- **Offline, local storage** — All data is stored on the device using SQLite; no account or internet connection is required.
- **Responsive layout** — The interface uses density-independent units, weighted layouts and scrollable containers so that it adapts to different screen sizes.


## Technology Stack

| Component | Technology |
|---|---|
| Language | Kotlin |
| UI | Android Views (XML) with Material Components (Material 3 theme) |
| Local database | SQLite via `SQLiteOpenHelper` |
| Preferences | `SharedPreferences` (daily goals) |
| Charting | Custom `BarChartView` drawn on `Canvas` (no third-party libraries) |
| Build system | Gradle (Kotlin DSL), Android Gradle Plugin 8.5.2 |
| Minimum SDK | API 21 (Android 5.0) |

**Dependencies:** `androidx.core:core-ktx`, `androidx.appcompat:appcompat`, `com.google.android.material:material`.

## Project Structure

```
CodeAlpha_FitnessTracker/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/codealpha/fitnesstracker/
│       │   ├── MainActivity.kt     # Dashboard, dialogs and UI logic
│       │   ├── DbHelper.kt         # SQLite database access (insert, delete, totals, history)
│       │   └── BarChartView.kt     # Custom 7-day bar chart view
│       └── res/
│           ├── layout/             # Screen and dialog layouts
│           ├── menu/               # Options menu (goals, clear data)
│           ├── values/             # Strings, colours, themes
│           └── mipmap-*/           # Launcher icons
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

## Prerequisites

- [Android Studio](https://developer.android.com/studio) Hedgehog (2023.1.1) or newer
- JDK 17 (bundled with recent versions of Android Studio)
- Android SDK Platform 34
- An Android device or emulator running Android 5.0 (API 21) or higher
- An internet connection for the first Gradle sync



## How to Use the App

1. **Log an activity** — Tap the **+** button, choose the exercise type and date, enter the duration, calories and/or steps, then tap **Save**. At least one value must be entered.
2. **Track daily progress** — The *Today* card displays progress bars for steps, calories and active minutes.
3. **Review the week** — The *Last 7 days* card shows a bar chart. Use the *Steps / Calories / Minutes* selector to change the metric displayed.
4. **Set goals** — Open the options menu (⋮) and select **Set daily goals**. The default goals are 10,000 steps, 500 kcal and 30 minutes.
5. **Delete entries** — Tap the bin icon beside an entry in *Recent activity* and confirm. The menu option **Clear all data** removes every entry.

## Data Storage

Fitness entries are stored in a local SQLite database (`fittrack.db`) in a single table:

| Column | Type | Description |
|---|---|---|
| `id` | INTEGER (PK, autoincrement) | Unique entry identifier |
| `date` | TEXT | Entry date (`yyyy-MM-dd`) |
| `type` | TEXT | Exercise type |
| `minutes` | INTEGER | Workout duration in minutes |
| `calories` | INTEGER | Calories burned (kcal) |
| `steps` | INTEGER | Step count |

Daily goals are stored separately in `SharedPreferences`. Data remains on the device and is not transmitted anywhere.

## Design and Reliability Notes

- User input is validated before saving; empty or invalid values are rejected with a message.
- Database operations are wrapped in error handling so that a failure displays a message rather than closing the application.
- Dialogs are scrollable, ensuring that input fields remain accessible when the on-screen keyboard is open or on small displays.
- The activity handles orientation changes without restarting, preserving the on-screen state.

## Future Enhancements

- Automatic step counting using the device's step-counter sensor
- Integration with Health Connect or a wearable device
- Cloud backup and synchronisation (e.g. Firebase)
- Editing of existing entries
- Dark theme and data export (CSV)

## Author

**\<Maria Nazish\>**
CodeAlpha App Development Intern


*Developed as part of the CodeAlpha App Development Internship — Task 3: Fitness Tracker App.*
