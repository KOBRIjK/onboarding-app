# Backend

FastAPI backend for authentication.

## Structure

- `app/main.py` creates the FastAPI application.
- `app/api` contains route declarations and dependencies.
- `app/core` contains settings and security helpers.
- `app/db` contains SQLAlchemy session setup.
- `app/models` contains database models.
- `app/schemas` contains request and response schemas.
- `app/services` contains business logic.

## Run locally

```powershell
docker compose up -d backend-postgres
cd backend
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
Copy-Item .env.example .env
uvicorn main:app --reload
```

Set a real `JWT_SECRET` in `.env` before using the API. The backend database
is Postgres by default:

```env
DATABASE_URL=postgresql+psycopg://onboarding:onboarding_password@127.0.0.1:5433/onboarding
```

To copy an existing legacy SQLite database into Postgres, start the Postgres
service and run:

```powershell
cd backend
python scripts\migrate_sqlite_to_postgres.py --source app.db
```

If the target Postgres database already contains seeded rows and you want it to
match SQLite exactly, rerun with `--replace`.

## Run with Docker

Create `backend/.env` first:

```powershell
Copy-Item backend\.env.example backend\.env
```

Then start the backend and its Postgres database:

```powershell
docker compose up -d --build backend
```

The API will be available on `http://127.0.0.1:8000`. In Docker Compose,
`DATABASE_URL` is overridden to use `backend-postgres:5432` inside the container
network, while the `.env.example` value keeps working for local host runs.

## Onboarding endpoints

The backend seeds a default `backend-developer` onboarding flow on startup.
Newly registered users receive this flow automatically.

```text
GET /api/v1/onboarding/flow
GET /api/v1/onboarding/tasks
POST /api/v1/onboarding/tasks/{step_id}/status
GET /api/v1/onboarding/progress
GET /api/v1/onboarding/report
```

Status update body:

```json
{
  "status": "done",
  "notes": "Configured local environment"
}
```

Allowed task statuses: `not_started`, `in_progress`, `done`.

## RAG endpoint

The notebook code is available through:

```text
POST /api/v1/rag/ask
```

Request body:

```json
{
  "query": "О чём говорится в документе?"
}
```

RAG reads pages from the local Confluence declared in `docker-compose.yaml`.

```powershell
docker compose up -d
```

Relevant `.env` settings:

```env
CONFLUENCE_BASE_URL=http://127.0.0.1:8090
CONFLUENCE_USERNAME=your-username
CONFLUENCE_PASSWORD=your-password
CONFLUENCE_SPACE_KEY=
```

The model and vector index are loaded lazily on the first request. The FAISS
index is cached in `backend/storage/faiss` by default and rebuilt when the
Confluence content, embedding model, chunk size, or chunk overlap changes.

To force reloading pages from Confluence without restarting backend:

```text
POST /api/v1/rag/reload
```
