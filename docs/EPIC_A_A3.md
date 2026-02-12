# Epic A - Task A3 Report

## Task
Implement selection service for Exam Mode.

## Changes made
- Added `src/main/java/com/example/prog1learnapp/service/ExamSelectionService.java`.
- Added `src/main/java/com/example/prog1learnapp/service/ExamSelectionException.java`.
- Service implements:
  - `startNewExam()`
  - `resolveSelectedExercises(ExamSessionState state)`
  - `markCompleted(ExamSessionState state, Long exerciseId)`
- Selection logic:
  - 1 random exercise from lessons `3,4,5`
  - 1 random exercise from lessons `6,7,8`
  - 1 random exercise from lessons `9,10,11`
  - output order is fixed by these three bands.
- Uses repository method from A1: `findByLessonIdIn(Collection<Long>)`.
- Added controlled error handling via `ExamSelectionException` when a band has no candidates.

## Validation
- Ran compile check successfully:
  - `bash ./mvnw -q -DskipTests compile`

## Result
- Exam selection backend logic is now implemented and ready for controller/session integration in Epic B.
