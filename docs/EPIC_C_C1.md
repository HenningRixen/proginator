# Epic C - Task C1 Report

## Task
Create `exam.html` page structure.

## Changes made
- Replaced `src/main/resources/templates/exam.html` with a full page structure aligned to Epic B model attributes.
- Added page layout sections:
  - navbar with back link and "Neue Pruefung starten"
  - header for Exam Mode context
  - stacked exercise list (`th:each`) using `examExercises`
  - empty-state section
  - footer
- For each exercise card, added:
  - lesson/week badge
  - exercise title
  - description block
  - starter-code block
  - completion button (`exam-complete-btn`, `data-exercise-id`)
  - completion status text based on `examCompletedIds`

## Alignment with Epic B docs
- Uses `/exam` view model fields from B2:
  - `examExercises`
  - `examCompletedIds`
- Uses completion endpoint contract from B3 via button data (`exercise.id`) for upcoming JS wiring.

## Validation
- Compile check passed:
  - `bash ./mvnw -q -DskipTests compile`

## Result
- Exam page now renders 3 exercises as stacked cards with the required core exercise content blocks and completion controls.
