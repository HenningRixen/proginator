# Epic B - Task B2 Report

## Task
Add exam page endpoint.

## Changes made
- Extended `src/main/java/com/example/prog1learnapp/controller/ExamController.java` with `GET /exam`.
- Endpoint behavior:
  - redirects to `/login` if principal is missing,
  - redirects to `/exam/start` if no active `EXAM_SESSION_STATE` exists,
  - resolves selected exercises using `examSelectionService.resolveSelectedExercises(...)`,
  - adds model attributes:
    - `examExercises`
    - `examCompletedIds`
  - returns view `exam`.
- Added controlled fallback: if selection resolution fails (`ExamSelectionException`), redirect to `/exam/start`.
- Added minimal view template `src/main/resources/templates/exam.html` to render selected exercises and completion status.

## Alignment with Epic A docs
- Uses session object `ExamSessionState` (A2).
- Uses ordered exercise resolution from service (A3).

## Validation
- Compile check passed:
  - `bash ./mvnw -q -DskipTests compile`

## Result
- `/exam` now renders the current session's fixed 3-exercise selection and is stable across reloads for the same attempt.
