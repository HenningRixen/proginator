# Epic A - Task A1 Report

## Task
Add repository query for lesson ranges.

## Changes made
- Updated `src/main/java/com/example/prog1learnapp/repository/ExerciseRepository.java`.
- Added import: `java.util.Collection`.
- Added method: `List<Exercise> findByLessonIdIn(Collection<Long> lessonIds);`.

## Why
Exam Mode selection needs to fetch candidate exercises across lesson bands (`3-5`, `6-8`, `9-11`) efficiently.

## Result
- Repository now supports querying exercises by a set of lesson IDs.
- This method is ready to be consumed by the Exam selection service (Task A3).
