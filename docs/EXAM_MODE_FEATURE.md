# Exam Mode Feature - Complete Technical Documentation

## 1. Purpose and Scope
This document is the single-source technical reference for the Exam Mode

Exam Mode provides a session-based 3-exercise exam flow with deterministic lesson-band distribution, exam-only completion tracking, resume behavior, and gated solution reveal.

This file is intended for:
- Developers who need to maintain or extend the feature.
- AI agents that need complete implementation context before making changes.

---

## 2. Final Functional Behavior

### 2.1 Exam composition
Each exam attempt contains exactly 3 exercises:
1. one random exercise from lessons 3-5
2. one random exercise from lessons 6-8
3. one random exercise from lessons 9-11

Order is fixed by band (3-5 first, then 6-8, then 9-11).

### 2.2 Attempt lifecycle
- `GET /exam/start` always creates a new attempt.
- Attempt state is stored in HTTP session.
- `GET /exam` renders the current attempt without reshuffling.
- Attempt remains stable across reloads and when navigating exam -> dashboard -> exam (resume path).

### 2.3 Completion semantics
- Completing an exercise in Exam Mode updates only exam session state.
- Normal user progress (`User.completedExercises`) is not modified by exam completion.
- Normal lesson completion endpoint (`/exercise/{id}/complete`) remains unchanged and still updates user progress.

### 2.4 Solution reveal gating
- Solutions are hidden by default.
- UI shows progress text: `x von 3 Aufgaben erledigt`.
- `Loesungen anzeigen` is disabled until all 3 exercises are marked done.
- At 3/3, button unlocks and solutions can be revealed.

---

## 3. Architecture and Data Model

### 3.1 Session state object
`src/main/java/com/example/prog1learnapp/model/ExamSessionState.java`

Fields:
- `attemptId` (`String`, UUID): stable identity for one attempt.
- `selectedExerciseIds` (`List<Long>`): exactly 3 exercise IDs.
- `completedExerciseIds` (`Set<Long>`): exam-only completion set.
- `createdAt` (`Instant`): attempt timestamp.

Helper methods:
- `isExerciseSelected(Long exerciseId)`
- `markCompleted(Long exerciseId)`

### 3.2 Selection service
`src/main/java/com/example/prog1learnapp/service/ExamSelectionService.java`

Responsibilities:
- build new exam attempt with one random pick per lesson band
- preserve deterministic output order by band
- resolve selected IDs into ordered `Exercise` list
- validate completion calls against selected IDs

Error type:
- `ExamSelectionException`

Repository dependency:
- `ExerciseRepository.findByLessonIdIn(Collection<Long> lessonIds)`

---

## 4. HTTP Endpoints and Contracts

## 4.1 `GET /exam/start`
Controller: `ExamController.startExam(...)`

Behavior:
- requires authenticated principal
- creates a new `ExamSessionState`
- stores state under session key `EXAM_SESSION_STATE`
- redirects to `/exam`

## 4.2 `GET /exam`
Controller: `ExamController.exam(...)`

Behavior:
- requires authenticated principal
- if no session state: redirects to `/exam/start`
- resolves selected exercises and returns `exam` view
- does not reshuffle existing state on normal render/reload
- if resolve fails, renders `exam` with error model (no implicit restart)

Model attributes (normal path):
- `examAttemptId`
- `examExercises`
- `examCompletedIds`
- `examCompletedCount`
- `examTotalCount`
- `canRevealSolutions`

Model attributes (error path):
- `examAttemptId`
- `examError`
- empty-safe values for exercise/completion counters

## 4.3 `POST /exam/{exerciseId}/complete`
Controller: `ExamController.completeExamExercise(...)`

Behavior:
- requires authenticated principal
- validates session exam state exists
- validates exercise belongs to active exam
- marks exercise completed in session only

JSON success response:
- `status: "success"`
- `completedCount`
- `totalCount`
- `canRevealSolutions`

JSON error response:
- `status: "error"`
- `message`

## 4.4 Existing endpoint compatibility
`POST /exercise/{id}/complete` remains normal progress endpoint and still updates user progress in DB.

---

## 5. Dashboard Integration (Resume vs Start)

Controller:
- `LearnController.dashboard(...)`

Reads session exam state and sets:
- `hasActiveExamAttempt`
- `examCompletedCount`
- `examTotalCount`

Template behavior (`dashboard.html`):
- if active attempt exists:
  - CTA routes to `/exam`
  - label: `Exam fortsetzen`
  - progress hint shown
- if no active attempt:
  - CTA routes to `/exam/start`
  - label: `Exam Modus starten`

This prevents unintentional reshuffle when returning from exam via dashboard.

---

## 6. Frontend and UI Behavior

### 6.1 Main exam template
`src/main/resources/templates/exam.html`

Key sections:
- page header with attempt ID
- stacked 3 exercise cards
- exam-only completion buttons
- solution section with progress and locked reveal button
- hidden solutions container (`hidden` + `aria-hidden`)

### 6.2 Styling
Files:
- `src/main/resources/static/css/exam.css`
- `src/main/resources/static/css/exercise.css` (reused for visual parity)

Important style rules:
- exam heading/subtitle readable in white
- hidden-state enforcement:
  - `#exam-solutions-container[hidden] { display: none !important; }`
- visible state:
  - `.is-visible` class for revealed solutions

### 6.3 Client behavior script
`src/main/resources/static/js/exam.js`

Responsibilities:
- syntax highlighting initialization
- ensure solutions hidden on load
- reveal handler for solutions button
- completion AJAX calls
- updates per-exercise status text
- updates `x von y` progress text
- unlocks reveal button when backend returns `canRevealSolutions=true`

---

## 7. Security and CSRF

`src/main/java/com/example/prog1learnapp/config/SecurityConfig.java`

CSRF ignore matchers include:
- `/exercise/*/complete`
- `/exam/*/complete`
- `/h2-console/**`

Exam JS still reads CSRF meta values when present, but endpoint works under current ignore policy.

---

## 8. Testing Strategy and Coverage

### 8.1 Service tests
`src/test/java/com/example/prog1learnapp/service/ExamSelectionServiceTest.java`

Covers:
- one exercise per band
- total of 3 exercises
- ordered band output
- failure when a band has no candidates
- attempt ID presence

### 8.2 Integration tests
`src/test/java/com/example/prog1learnapp/controller/ExamControllerIntegrationTest.java`

Covers:
- `/exam/start` creates session state
- `/exam` renders 3 exercises and attempt data
- reload stability for IDs/attemptId
- explicit restart creates new attemptId
- invalid selected IDs do not silently reshuffle
- exam completion success/failure
- dashboard resume behavior with active attempt
- solution gate state model behavior and unlock at 3/3
- normal completion endpoint regression
- exam completion does not alter user progress regression

### 8.3 Test environment fix
`src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`
- set to `mock-maker-subclass` to avoid inline mock-maker attach issues on current JDK.

### 8.4 Useful test commands
- `bash ./mvnw -q -DskipTests compile`
- `bash ./mvnw -q -Dtest=ExamSelectionServiceTest,ExamControllerIntegrationTest test`

---

## 9. File Map (Feature-Relevant)

Backend:
- `src/main/java/com/example/prog1learnapp/controller/ExamController.java`
- `src/main/java/com/example/prog1learnapp/controller/LearnController.java`
- `src/main/java/com/example/prog1learnapp/model/ExamSessionState.java`
- `src/main/java/com/example/prog1learnapp/service/ExamSelectionService.java`
- `src/main/java/com/example/prog1learnapp/service/ExamSelectionException.java`
- `src/main/java/com/example/prog1learnapp/repository/ExerciseRepository.java`
- `src/main/java/com/example/prog1learnapp/config/SecurityConfig.java`

Frontend:
- `src/main/resources/templates/exam.html`
- `src/main/resources/templates/dashboard.html`
- `src/main/resources/static/js/exam.js`
- `src/main/resources/static/css/exam.css`
- `src/main/resources/static/css/dashboard.css`

Tests:
- `src/test/java/com/example/prog1learnapp/service/ExamSelectionServiceTest.java`
- `src/test/java/com/example/prog1learnapp/controller/ExamControllerIntegrationTest.java`
- `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`

Planning/Traceability:
- this file is the sole long-term reference document for Exam Mode.

---

## 10. Operational Rules for Future Changes

1. Only `/exam/start` may intentionally reshuffle.
2. `/exam` must render current session state and never silently reset valid attempts.
3. Keep exam completion isolated from `User.completedExercises` unless product requirements explicitly change.
4. Do not bypass solution gate: reveal must remain locked until all 3 are completed.
5. If changing exercise count from 3, update:
   - selection logic,
   - UI progress text,
   - unlock condition,
   - tests.
6. Maintain integration tests whenever routing/session logic changes.

---

## 11. Known Constraints / Assumptions

- Exam attempts are session-scoped, not DB-persisted.
- Lesson IDs for band logic are stable (`3..11`).
- UI language is currently German-oriented.
- Dashboard resume behavior depends on active session exam state.

