#!/usr/bin/env python3
"""
Seed initial users directly into the Gym Management System SQLite database.

- Inserts into `users` plus role-specific joined tables (`admins`, `members`,
  `trainers`, `receptionists`).
- Inserts default rows into `membership_plans`.
- Safe to run multiple times: existing emails are skipped.

Usage:
  python scripts/seed_users.py
  python scripts/seed_users.py --db /path/to/gymdb.db
"""

from __future__ import annotations

import argparse
import sqlite3
from dataclasses import dataclass
from datetime import date
from pathlib import Path
from typing import Optional


@dataclass(frozen=True)
class SeedUser:
    name: str
    email: str
    phone: str
    password: str
    role: str
    specialization: Optional[str] = None
    experience_years: Optional[int] = None


@dataclass(frozen=True)
class SeedPlan:
    plan_name: str
    duration_months: int
    price: float
    description: str


SEED_USERS: list[SeedUser] = [
    SeedUser(
        name="System Admin",
        email="admin@gym.com",
        phone="9000000001",
        password="admin123",
        role="ADMIN",
    ),
    SeedUser(
        name="Front Desk",
        email="reception@gym.com",
        phone="9000000002",
        password="recep123",
        role="RECEPTIONIST",
    ),
    SeedUser(
        name="Head Trainer",
        email="trainer@gym.com",
        phone="9000000003",
        password="trainer123",
        role="TRAINER",
        specialization="Strength",
        experience_years=5,
    ),
    SeedUser(
        name="Demo Member",
        email="member@gym.com",
        phone="9000000004",
        password="member123",
        role="MEMBER",
    ),
]


SEED_PLANS: list[SeedPlan] = [
    SeedPlan(
        plan_name="BASIC",
        duration_months=1,
        price=1000.0,
        description="Basic monthly membership",
    ),
    SeedPlan(
        plan_name="PREMIUM",
        duration_months=3,
        price=3000.0,
        description="Premium quarterly membership (pricing strategy applies discount)",
    ),
]


def default_db_path() -> Path:
    # .../gym-management-system/scripts/seed_users.py -> .../gym-management-system/gymdb.db
    return Path(__file__).resolve().parents[1] / "gymdb.db"


def ensure_schema_exists(conn: sqlite3.Connection) -> None:
    required = {
        "users",
        "admins",
        "members",
        "trainers",
        "receptionists",
        "membership_plans",
    }
    rows = conn.execute("SELECT name FROM sqlite_master WHERE type='table'").fetchall()
    existing = {r[0] for r in rows}
    missing = sorted(required - existing)
    if missing:
        raise RuntimeError(
            "Database schema is missing tables: "
            + ", ".join(missing)
            + ". Start the Spring Boot app once to initialize schema."
        )


def user_exists(conn: sqlite3.Connection, email: str) -> bool:
    row = conn.execute("SELECT id FROM users WHERE email = ?", (email,)).fetchone()
    return row is not None


def insert_user(conn: sqlite3.Connection, user: SeedUser) -> int:
    cursor = conn.execute(
        """
        INSERT INTO users (name, email, phone, password, role)
        VALUES (?, ?, ?, ?, ?)
        """,
        (user.name, user.email, user.phone, user.password, user.role),
    )
    return int(cursor.lastrowid)


def insert_role_row(conn: sqlite3.Connection, user_id: int, user: SeedUser) -> None:
    if user.role == "ADMIN":
        conn.execute("INSERT INTO admins (id) VALUES (?)", (user_id,))
        return

    if user.role == "RECEPTIONIST":
        conn.execute("INSERT INTO receptionists (id) VALUES (?)", (user_id,))
        return

    if user.role == "MEMBER":
        conn.execute(
            "INSERT INTO members (id, join_date, status) VALUES (?, ?, ?)",
            (user_id, date.today().isoformat(), "ACTIVE"),
        )
        return

    if user.role == "TRAINER":
        conn.execute(
            """
            INSERT INTO trainers (id, specialization, experience_years)
            VALUES (?, ?, ?)
            """,
            (user_id, user.specialization, user.experience_years),
        )
        return

    raise ValueError(f"Unsupported role: {user.role}")


def plan_exists(conn: sqlite3.Connection, plan_name: str) -> Optional[int]:
    row = conn.execute(
        "SELECT id FROM membership_plans WHERE lower(plan_name) = lower(?)",
        (plan_name,),
    ).fetchone()
    return int(row[0]) if row else None


def insert_plan(conn: sqlite3.Connection, plan: SeedPlan) -> int:
    cursor = conn.execute(
        """
        INSERT INTO membership_plans (plan_name, duration_months, price, description)
        VALUES (?, ?, ?, ?)
        """,
        (plan.plan_name, plan.duration_months, plan.price, plan.description),
    )
    return int(cursor.lastrowid)


def seed(db_path: Path) -> None:
    created = 0
    skipped = 0
    plans_created = 0
    plans_skipped = 0
    plan_id_map: dict[str, int] = {}

    with sqlite3.connect(db_path) as conn:
        conn.execute("PRAGMA foreign_keys = ON")
        ensure_schema_exists(conn)

        for plan in SEED_PLANS:
            existing_id = plan_exists(conn, plan.plan_name)
            if existing_id is not None:
                plans_skipped += 1
                plan_id_map[plan.plan_name] = existing_id
                print(f"[skip] plan {plan.plan_name} already exists (id={existing_id})")
                continue

            plan_id = insert_plan(conn, plan)
            plans_created += 1
            plan_id_map[plan.plan_name] = plan_id
            print(f"[ok]   created plan {plan.plan_name:<8} (id={plan_id})")

        for user in SEED_USERS:
            if user_exists(conn, user.email):
                skipped += 1
                print(f"[skip] {user.email} already exists")
                continue

            user_id = insert_user(conn, user)
            insert_role_row(conn, user_id, user)
            created += 1
            print(f"[ok]   created {user.role:<12} {user.email} (id={user_id})")

        conn.commit()

    print("\nDone.")
    print(f"Plans created: {plans_created}")
    print(f"Plans skipped: {plans_skipped}")
    print(f"Created: {created}")
    print(f"Skipped: {skipped}")

    if plan_id_map:
        print("\nUse these plan IDs in the enrollment form:")
        for plan_name in sorted(plan_id_map.keys()):
            print(f"- {plan_name}: {plan_id_map[plan_name]}")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Seed demo users into gymdb.db")
    parser.add_argument(
        "--db",
        type=Path,
        default=default_db_path(),
        help="Path to SQLite DB file (default: gym-management-system/gymdb.db)",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    db_path = args.db.resolve()

    if not db_path.exists():
        raise SystemExit(
            f"Database file not found: {db_path}\n"
            "Run the Spring Boot app once so it creates gymdb.db, then run this script again."
        )

    seed(db_path)


if __name__ == "__main__":
    main()
