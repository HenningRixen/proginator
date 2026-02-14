# Feedback Feature

## Overview

The feedback feature allows authenticated users to submit:

- A star rating (`1` to `5`)
- A study program (`WINF` or `AINF`, shown as `Winf`/`Ainf` in UI)
- A free-text feedback message (max 2000 chars)

All three are required for new submissions.

## User Flow

1. User opens `/feedback`.
2. User selects stars (`rating` radio group).
3. User selects study program (`studyProgram` radio group).
4. User enters feedback text.
5. User clicks `Feedback absenden`.

Client-side validation blocks submit if:

- no star rating selected
- no study program selected
- feedback text empty

## Backend Flow

Endpoint: `POST /feedback` in `src/main/java/com/example/prog1learnapp/controller/FeedbackController.java`

Server-side validation enforces:

- `text` not empty and max 2000 chars
- `rating` exists and is between `1` and `5`
- `studyProgram` exists and is one of `WINF` or `AINF`

If validation fails: redirect to `/feedback` with flash error.
If validation passes: persist `Feedback` row and redirect to `/feedback` with flash success.

## Persistence Model

Entity: `src/main/java/com/example/prog1learnapp/model/Feedback.java`

Stored fields:

- `id`
- `text`
- `rating`
- `study_program` (enum string: `WINF`/`AINF`)
- `created_at`

Enum parser:

- `src/main/java/com/example/prog1learnapp/model/StudyProgram.java`
- Handles trim + case-insensitive mapping
- Invalid values resolve to `null` and are rejected by controller validation

## Database Behavior (Prod Profile)

Config: `src/main/resources/application-prod.yaml`

- `spring.jpa.hibernate.ddl-auto: update`

Meaning:

- On startup with `prod` profile, Hibernate updates schema automatically.
- Existing feedback rows without `study_program` remain valid (`NULL` allowed for old rows).
- New rows should always contain `WINF` or `AINF`.

## Local Mock-Prod Verification (`lernapp-postgres`)

1. Ensure DB container is running:

```bash
docker start lernapp-postgres
```

2. Run app with prod profile:

```bash
bash mvnw -Dspring-boot.run.profiles=prod spring-boot:run
```

3. Check table structure:

```bash
docker exec lernapp-postgres psql -U lernapp -d lernapp -c "\d+ feedback"
```

4. Check latest feedback rows (SQL helper):

```bash
docker exec lernapp-postgres psql -U lernapp -d lernapp -f /dev/stdin < docs/sql/feedback_latest.sql
```

## Tests

Integration tests:

- `src/test/java/com/example/prog1learnapp/controller/FeedbackControllerIntegrationTest.java`

Covered scenarios:

- valid submission persists
- missing study program rejected
- invalid study program rejected
- missing star rating rejected
