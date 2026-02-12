# Epic C - Task C4 Report

## Task
Add exam JS behavior.

## Changes made
- Added script file: `src/main/resources/static/js/exam.js`.
- Wired script into `src/main/resources/templates/exam.html`.
- Implemented JS behaviors:
  - Global solution reveal:
    - button `#show-solutions-btn` reveals `#exam-solutions-container`
    - button is disabled after reveal
  - Exam completion AJAX:
    - binds to `.exam-complete-btn`
    - sends `POST /exam/{exerciseId}/complete`
    - updates button + status text on success
    - handles and displays error states on failure
- Included CSRF header/token lookup hooks compatible with existing app patterns.

## Alignment with Epic B docs
- Calls endpoint contract from B3: `POST /exam/{exerciseId}/complete` with JSON status response.
- Operates on `exam.html` structure introduced in B2/C1.

## Validation
- Compile check passed:
  - `bash ./mvnw -q -DskipTests compile`

## Result
- Exam page now supports interactive exam-only completion updates and one-click reveal of all solutions at the end.
