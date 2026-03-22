# Implementation Progress — 2026-03-24

## Current Status

Work has started and multiple high-priority slices are already implemented with incremental conventional commits.

## Completed (Committed)

1. `a278b8c` — **feat(auth): enforce role-based user creation and payment access**
   - Added backend security config and user-details service.
   - Enforced server-side role policy in user creation:
     - Public registration -> `MEMBER` only.
     - `RECEPTIONIST` cannot create `ADMIN`/`RECEPTIONIST`.
   - Added payment access boundaries.
   - Added/updated tests (`UserServiceTest`, integration tests).

2. `9a8de48` — **feat(frontend): align payment and user flows by role**
   - Frontend payment flow split by role behavior.
   - Member payment history now uses self endpoint logic.
   - Receptionist UI role options restricted to member creation.

3. `7b99c14` — **feat(membership): add member self-service membership endpoint**
   - Added `/api/memberships/me`.
   - Restricted `/api/memberships/member/{id}` to staff roles.
   - Updated membership frontend to use self endpoint for members.
   - Added integration coverage for self access and forbidden cross-access.

4. `7d5d7a2` — **feat(attendance): add persisted history endpoints and UI**
   - Added `/api/attendance/member/{memberId}` and `/api/attendance/me`.
   - Updated attendance frontend to load backend history.
   - Fixed check-in time format mismatch (`time` field now maps to backend expected `HH:mm`).
   - Added integration test for attendance history retrieval.

## Completed But Not Yet Committed (as of this note)

The following files are present in working tree and need committing:
- `FRONTEND_LOGICAL_IMPROVEMENTS.md`
- `GYM_MANAGEMENT_SYSTEM_PLAN.md`
- `gym-frontend/index.html`
- `gym-frontend/login.html`
- `gym-frontend/reports.html`
- `gym-frontend/style.css`
- `gym-frontend/workouts.html`
- `gym-management-system/scripts/seed_users.py`

(Will be committed immediately after writing this note.)

## Remaining Work (Priority)

### P1 — Security/Ownership Completion
- Enforce trainer/member ownership constraints for workouts (assigned-member checks).
- Add equivalent ownership constraints for membership lookup where policy requires.

### P1 — Payment Domain Completion
- Implement explicit member payment submission flow (request/checkout) instead of only staff-recorded flow.
- Add payment status lifecycle (`PENDING/APPROVED/REJECTED`) if request model is adopted.

### P2 — Remove Manual ID Dependence
- Replace raw ID entry UX with search/select endpoints (`users/search`, active membership resolver, assignment-based lists).
- Wire frontend forms to selector-based resolution.

### P2 — Error Contract Consistency
- Standardize backend error envelope fields and status semantics across controllers.
- Add contract tests for `403`, `404`, validation, and conflict scenarios.

### P3 — Auth Hardening
- Migrate from Basic Auth + plaintext browser session to token-based auth.
- Add auth lifecycle endpoints (`login/refresh/logout`) and revocation/expiry tests.

## Note To Self (Next Session)

- Start from **Workout ownership enforcement** first (service-level assignment checks + integration tests), then move to **payment member submission flow**.
- Keep incremental commit discipline:
  - one feature slice per commit,
  - conventional commit format,
  - run focused tests before each commit.
- Avoid committing generated artifacts (`__pycache__`, build output) and keep commits scoped.
