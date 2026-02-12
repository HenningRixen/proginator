# Epic C - Task C2 Report

## Task
Add bottom solution section.

## Changes made
- Updated `src/main/resources/templates/exam.html`.
- Added one global solution section at the end of the exercise list:
  - heading `Loesungen`
  - single reveal trigger button: `#show-solutions-btn`
  - hidden container: `#exam-solutions-container` (initially hidden)
- Added rendering of all exercise solutions inside the hidden container:
  - one solution card per selected exam exercise
  - ordered by the same `examExercises` list

## Alignment with Epic B docs
- Reuses the same fixed-order `examExercises` from `GET /exam` (B2), so solution ordering matches exercise ordering.

## Validation
- Compile check passed:
  - `bash ./mvnw -q -DskipTests compile`

## Result
- A single bottom-of-page solution block exists and is prepared to reveal all 3 solutions together.
