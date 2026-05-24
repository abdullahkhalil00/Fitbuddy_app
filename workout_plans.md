# Workout Plans & Firestore Structure

This document outlines the workout plan structure and the Firestore configuration for the FitFuel app.

## 1. Firestore Setup (Actual Structure)

Create a `workout` collection (singular) in your Firebase Console. Each document name should match a User Goal (e.g., "Shoulders", "Fat Loss", "Chest"). Exercises are stored as **fields** within the document.

### Collection Hierarchy:
`workout (collection)`
  - `Shoulders (document)`
    - `Shoulder press`: "10 min" (String)
    - `Lateral raises`: "9 min" (String)
    - `Front raises`: "8 min" (String)
  - `Chest (document)`
    - `Push ups`: "10 min"
    - `Bench press`: "12 min"

**Note:** The app automatically removes the " min" suffix and converts the number to an integer for the timer and progress calculation.

---

## 2. Exercise Database (Reference)

Use these entities when populating your Firestore fields:

| Category | Exercises | Default Duration String |
| :--- | :--- | :--- |
| **Chest** | Push ups, Bench press | "10 min" |
| **Back** | Pull ups, Lat pulldown | "10 min" |
| **Shoulders** | Shoulder press, Lateral raises | "10 min" |
| **Biceps** | Dumbbell curls, Hammer curls | "8 min" |
| **Triceps** | Tricep pushdown, Dips | "8 min" |
| **Legs** | Squats, Lunges | "10 min" |
| **Abs** | Crunches, Plank | "5 min" |
| **Full Body** | Burpees, Deadlift | "10 min" |
| **Cardio** | Running, Jump rope | "15 min" |

---

## 3. Progress Tracking (Local Cache)

Progress is tracked locally in the mobile's cache (`SharedPreferences`) and synced to the Home Screen:
- **Storage:** `WorkoutCache` file.
- **Keys:** 
    - `{Date}_completed`: Total minutes finished today.
    - `{Date}_{ExerciseName}`: Boolean status of specific exercise.
- **Formula:** `(Completed Minutes / Total Minutes in Today's List) * 100`
- **Home Screen:** Displays total percentage and the list of exercises fetched from the current Goal document in Firestore.
