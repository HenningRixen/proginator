# Epic C - Task C3 Report

## Task
Add exam-specific styling.

## Changes made
- Added stylesheet `src/main/resources/static/css/exam.css`.
- Linked stylesheet from `src/main/resources/templates/exam.html`.
- Implemented exam page styles for:
  - stacked exercise cards
  - exercise header/badges
  - task content block
  - code blocks (starter code + solutions)
  - completion action row
  - bottom solution section
  - responsive behavior for mobile (`@media (max-width: 768px)`)

## Alignment with Epic B docs
- Styling targets the existing B2 `exam` view structure and B3 completion-status display fields.

## Validation
- Compile check passed:
  - `bash ./mvnw -q -DskipTests compile`

## Result
- Exam page now has dedicated visual layout and responsive behavior consistent with the app's shared design tokens (`base.css`).
