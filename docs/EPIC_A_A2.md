# Epic A - Task A2 Report

## Task
Add exam session state model.

## Changes made
- Added `src/main/java/com/example/prog1learnapp/model/ExamSessionState.java`.
- Implemented a serializable session model with:
  - `selectedExerciseIds` (`List<Long>`)
  - `completedExerciseIds` (`Set<Long>`)
  - `createdAt` (`Instant`)
- Added helper methods:
  - `isExerciseSelected(Long exerciseId)`
  - `markCompleted(Long exerciseId)`

## Why
Exam Mode needs stable per-attempt state in the HTTP session:
- fixed selected exercises,
- exam-only completion tracking,
- optional metadata timestamp.

## Result
- A dedicated session object exists and is ready to be stored/retrieved via session attributes in exam endpoints.
