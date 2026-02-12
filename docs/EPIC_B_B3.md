# Epic B - Task B3 Report

## Task
Add exam-only completion endpoint.

## Changes made
- Extended `src/main/java/com/example/prog1learnapp/controller/ExamController.java` with:
  - `POST /exam/{exerciseId}/complete`
- Endpoint behavior:
  - returns `401` JSON when principal missing,
  - returns `400` JSON if no active exam session exists,
  - validates exercise is part of active exam via `examSelectionService.markCompleted(...)`,
  - updates only session state (`EXAM_SESSION_STATE`),
  - returns `{ "status": "success" }` on success,
  - returns `400` JSON for invalid exercise IDs not in selected set.

## Alignment with Epic A docs
- Uses `ExamSessionState.completedExerciseIds` from A2.
- Uses `markCompleted(...)` validation logic from A3.
- Does not write to `User.completedExercises` (exam-only tracking).

## Validation
- Compile check passed:
  - `bash ./mvnw -q -DskipTests compile`

## Result
- Exam completion endpoint is now available and isolated from normal lesson progress tracking.
