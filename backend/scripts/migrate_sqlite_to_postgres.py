from __future__ import annotations

import argparse
import os
import sys
from pathlib import Path

from sqlalchemy import create_engine, func, inspect, select
from sqlalchemy.engine import make_url

BACKEND_DIR = Path(__file__).resolve().parents[1]
if str(BACKEND_DIR) not in sys.path:
    sys.path.insert(0, str(BACKEND_DIR))

from app import models  # noqa: E402,F401
from app.db.base import Base  # noqa: E402
from app.models import (  # noqa: E402
    OnboardingFlow,
    OnboardingStep,
    RefreshSession,
    User,
    UserFlowAssignment,
    UserStepProgress,
)

DEFAULT_DATABASE_URL = (
    "postgresql+psycopg://onboarding:onboarding_password@127.0.0.1:5433/onboarding"
)

TABLES = (
    User.__table__,
    OnboardingFlow.__table__,
    OnboardingStep.__table__,
    UserFlowAssignment.__table__,
    UserStepProgress.__table__,
    RefreshSession.__table__,
)


def read_env_file(path: Path) -> dict[str, str]:
    values: dict[str, str] = {}
    if not path.exists():
        return values

    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue

        key, value = line.split("=", 1)
        values[key.strip()] = value.strip().strip('"').strip("'")

    return values


def resolve_sqlite_path(raw_path: str) -> Path:
    path = Path(raw_path)
    if path.is_absolute():
        return path

    cwd_path = Path.cwd() / path
    if cwd_path.exists():
        return cwd_path

    return BACKEND_DIR / path


def default_source_path(env_values: dict[str, str]) -> Path:
    raw_path = os.environ.get("SQLITE_PATH") or env_values.get("SQLITE_PATH") or "app.db"
    return resolve_sqlite_path(raw_path)


def default_target_url(env_values: dict[str, str]) -> str:
    return (
        os.environ.get("DATABASE_URL")
        or env_values.get("DATABASE_URL")
        or DEFAULT_DATABASE_URL
    )


def table_count(connection, table) -> int:
    return connection.execute(select(func.count()).select_from(table)).scalar_one()


def redact_url(url: str) -> str:
    parsed = make_url(url)
    if parsed.password:
        parsed = parsed.set(password="***")
    return str(parsed)


def parse_args() -> argparse.Namespace:
    env_values = read_env_file(BACKEND_DIR / ".env")

    parser = argparse.ArgumentParser(
        description="Copy the legacy SQLite onboarding database into Postgres."
    )
    parser.add_argument(
        "--source",
        type=Path,
        default=default_source_path(env_values),
        help="Path to the source SQLite database. Defaults to SQLITE_PATH or app.db.",
    )
    parser.add_argument(
        "--target-url",
        default=default_target_url(env_values),
        help="Target Postgres SQLAlchemy URL. Defaults to DATABASE_URL.",
    )
    parser.add_argument(
        "--replace",
        action="store_true",
        help="Delete existing target rows before importing.",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Validate source and target counts without copying rows.",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    source_path = resolve_sqlite_path(str(args.source))

    if not source_path.exists():
        raise SystemExit(f"SQLite source database does not exist: {source_path}")

    source_engine = create_engine(f"sqlite:///{source_path.as_posix()}")
    target_engine = create_engine(args.target_url, pool_pre_ping=True)

    source_table_names = set(inspect(source_engine).get_table_names())
    missing_tables = [
        table.name for table in TABLES if table.name not in source_table_names
    ]
    if missing_tables:
        raise SystemExit(
            "SQLite source is missing tables: " + ", ".join(missing_tables)
        )

    Base.metadata.create_all(target_engine)

    with source_engine.connect() as source_conn, target_engine.begin() as target_conn:
        source_counts = {
            table.name: table_count(source_conn, table)
            for table in TABLES
        }
        target_counts = {
            table.name: table_count(target_conn, table)
            for table in TABLES
        }
        non_empty_targets = {
            name: count
            for name, count in target_counts.items()
            if count > 0
        }

        print(f"Source SQLite: {source_path}")
        print(f"Target Postgres: {redact_url(args.target_url)}")
        print("Source rows:")
        for table_name, count in source_counts.items():
            print(f"  {table_name}: {count}")

        if args.dry_run:
            print("Dry run complete. No rows were copied.")
            return

        if non_empty_targets and not args.replace:
            tables = ", ".join(
                f"{name}={count}" for name, count in non_empty_targets.items()
            )
            raise SystemExit(
                "Target Postgres already contains rows: "
                f"{tables}. Re-run with --replace to overwrite them."
            )

        if args.replace:
            for table in reversed(TABLES):
                target_conn.execute(table.delete())

        copied_counts: dict[str, int] = {}
        for table in TABLES:
            rows = [
                dict(row._mapping)
                for row in source_conn.execute(select(table))
            ]
            if rows:
                target_conn.execute(table.insert(), rows)
            copied_counts[table.name] = len(rows)

    print("Copied rows:")
    for table_name, count in copied_counts.items():
        print(f"  {table_name}: {count}")


if __name__ == "__main__":
    main()
