# Epic B - Task B1 Report

## Task
Add exam start endpoint.

## Changes made
- Added `src/main/java/com/example/prog1learnapp/controller/ExamController.java`.
- Added `GET /exam/start` endpoint:
  - verifies authenticated principal,
  - calls `examSelectionService.startNewExam()`,
  - stores result in HTTP session under key `EXAM_SESSION_STATE`,
  - redirects to `/exam`.

## Alignment with Epic A docs
- Uses `ExamSessionState` from A2.
- Uses `ExamSelectionService.startNewExam()` from A3.

## Validation
- Compile check passed:
  - `bash ./mvnw -q -DskipTests compile`

## Result
- Starting exam now always initializes a fresh attempt in session and forwards user to exam page route.
