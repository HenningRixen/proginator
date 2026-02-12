# Exam Mode Implementation Plan (Checkable Task List)

## Summary
Implement a session-based Exam Mode with exactly 3 random exercises (bands 3-5, 6-8, 9-11), stacked on one page, with solutions revealed together at page end, and exam-only completion tracking.

## Public Interface Changes
- `GET /exam/start`
- `GET /exam`
- `POST /exam/{exerciseId}/complete`
- New template: `templates/exam.html`
- New style: `static/css/exam.css` (optional but recommended)
- Dashboard CTA linking to `/exam/start`

---

## Checkable Tasks

### Epic A: Backend Exam Session + Selection
- [x] **A1. Add repository query for lesson ranges**
  - Change: `ExerciseRepository` gets `findByLessonIdIn(Collection<Long> lessonIds)`.
  - Done when: app compiles and method is used by exam selection logic.

- [x] **A2. Add exam session state model**
  - Change: create `ExamSessionState` with:
    - `selectedExerciseIds` (3 IDs, fixed per attempt)
    - `completedExerciseIds` (exam-only)
    - optional metadata (`createdAt`)
  - Done when: state can be stored/retrieved from HTTP session.

- [x] **A3. Implement selection service**
  - Change: create `ExamSelectionService` that:
    - selects exactly 1 random exercise from 3-5
    - selects exactly 1 random exercise from 6-8
    - selects exactly 1 random exercise from 9-11
    - preserves display order by band
  - Done when: service returns exactly 3 valid exercises and throws controlled error if any pool is empty.

### Epic B: Exam Endpoints
- [x] **B1. Add exam start endpoint**
  - Change: `GET /exam/start` creates a new `ExamSessionState`, stores in session, redirects to `/exam`.
  - Done when: calling endpoint always resets exam with a fresh random set.

- [x] **B2. Add exam page endpoint**
  - Change: `GET /exam` loads exercises from session IDs and renders `exam.html`.
  - Done when: reload keeps same 3 exercises for same attempt.

- [x] **B3. Add exam-only completion endpoint**
  - Change: `POST /exam/{exerciseId}/complete` marks completion in exam session only.
  - Validation: reject IDs not in selected exam set.
  - Done when: returns JSON success/error and does not touch `User.completedExercises`.

### Epic C: UI (Single Page, 3 Stacked Exercises)
- [x] **C1. Create `exam.html` page structure**
  - Change: render 3 exercises vertically with title, lesson badge, description, starter code, complete button.
  - Done when: all three appear on one page in fixed band order.

- [x] **C2. Add bottom solution section**
  - Change: one global “Lösungen anzeigen” control at page bottom; reveals all 3 solutions together.
  - Done when: solutions are hidden initially and visible only after reveal action.

- [x] **C3. Add exam-specific styling**
  - Change: create/update `exam.css` aligned with existing design tokens (`base.css`).
  - Done when: layout is readable and consistent on desktop + mobile.

- [x] **C4. Add exam JS behavior**
  - Change: AJAX completion calls to `/exam/{id}/complete`; button/label state updates.
  - Done when: each exercise can be marked completed without page reload.

### Epic D: Navigation + Security
- [x] **D1. Add dashboard entry point**
  - Change: add “Exam Modus starten” CTA in `dashboard.html` linking to `/exam/start`.
  - Done when: user can reach exam flow directly from dashboard.

- [x] **D2. CSRF handling for exam completion**
  - Change: either include CSRF token in fetch headers or explicitly ignore `/exam/*/complete` (if matching existing approach).
  - Done when: completion endpoint works under current security config.

### Epic E: Tests (Must Pass)
- [ ] **E1. Service unit tests for selection**
  - Cases:
    - picks 1 per band
    - total = 3
    - stable order by band
    - empty pool failure path
  - Done when: tests pass consistently.

- [ ] **E2. Controller integration tests (`MockMvc`)**
  - Cases:
    - `/exam/start` creates session + redirects
    - `/exam` renders 3 exercises
    - `/exam` reload preserves same IDs
    - `/exam/{id}/complete` success for selected ID
    - `/exam/{id}/complete` rejects non-selected ID
  - Done when: all endpoint behaviors are covered and passing.

- [ ] **E3. Regression tests**
  - Case: `/exercise/{id}/complete` still updates `User.completedExercises`.
  - Case: exam completion does not affect dashboard progress.
  - Done when: existing lesson progress behavior remains unchanged.

---

## Acceptance Criteria Checklist
- [x] Exactly 3 exercises per exam attempt.
- [x] Distribution is always: one from 3-5, one from 6-8, one from 9-11.
- [ ] Same attempt is stable across reloads.
- [x] Exercises are shown stacked on one page.
- [ ] Solutions are revealed together only at page end.
- [x] Exam completion is isolated from normal completion/progress.
- [x] “Neue Prüfung starten” creates a fresh set.

---

## Assumptions / Defaults
- Lesson IDs map to weeks and remain stable (`3..11` used for bands).
- One active exam attempt per user session.
- No DB persistence for exam attempts in this version.
- German UI text, consistent with existing templates.

run codex resume 019c538a-12b0-74c1-93d1-23cf82d399a4