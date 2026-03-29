# Implementation Progress — 2026-03-24

## Current Status

Work is actively progressing with incremental conventional commits. P1 ownership and P2 selector migration are now substantially complete.

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

5. `aab8ae3` — **feat(workouts): enforce trainer ownership on member plans**
   - Added service-level ownership guard in workout creation/update flow.
   - Prevented cross-trainer takeover of existing member workout plans.
   - Added integration coverage for forbidden overwrite scenario.

6. `cca4e07` — **feat(selectors): add user search and membership plan lookup**
   - Added `GET /api/users/search` with role/query/limit filters.
   - Added lightweight lookup DTO for safe selector payloads.
   - Added `GET /api/memberships/plans` for plan selectors.
   - Added integration tests for both selector endpoints.

7. `2847f02` — **feat(attendance-ui): replace member IDs with selector**
   - Replaced typed member IDs with member dropdown selectors.
   - Reused backend `users/search` endpoint for attendance mark/history flows.

8. `7568750` — **feat(payments-ui): resolve active membership from member selector**
   - Replaced typed member and membership IDs with selector-driven flow.
   - Auto-resolves active membership from selected member before processing payment.

9. `562955b` — **feat(workouts-ui): use trainer/member selectors**
   - Replaced typed trainer/member IDs with selector controls.
   - Preserved trainer self-binding behavior for trainer role.

10. `ce4bcab` — **feat(users-ui): select user instead of typing ID**
    - Replaced update-profile user ID input with user selector.
    - Refreshes selector options after user registration.

11. `d4d5e4f` — **chore(progress): record selector migration and ownership slices**
   - Updated implementation status with the latest completed slices.

12. `e9936f8` — **feat(assignments): add trainer-manageable member selectors**
   - Added trainer-manageable member resolution in workout service.
   - Added `GET /api/workouts/manageable-members` for trainer-scoped selectors.
   - Wired trainer selectors in workouts and attendance to assignment-scoped member data.
   - Added integration coverage for manageable-members filtering behavior.

13. `dfd6156` — **feat(membership): enforce trainer ownership on member lookup**
   - Added trainer ownership enforcement on `GET /api/memberships/member/{memberId}`.
   - Prevented trainers from accessing memberships of members assigned to other trainers.
   - Added integration test for forbidden cross-trainer membership lookup.

## Remaining Work (Priority)

### P1 — Security/Ownership Completion
- Enforce trainer/member ownership constraints for workouts (assigned-member checks). ✅
- Add equivalent ownership constraints for membership lookup where policy requires. ✅

### P1 — Payment Domain Completion
- Implement explicit member payment submission flow (request/checkout) instead of only staff-recorded flow.
- Add payment status lifecycle (`PENDING/APPROVED/REJECTED`) if request model is adopted.

### P2 — Remove Manual ID Dependence
- Replace raw ID entry UX with search/select endpoints (`users/search`, active membership resolver, assignment-based lists). ✅
- Wire frontend forms to selector-based resolution. (done for users/membership/payments/workouts/attendance)

### P2 — Error Contract Consistency
- Standardize backend error envelope fields and status semantics across controllers.
- Add contract tests for `403`, `404`, validation, and conflict scenarios.

### P3 — Auth Hardening
- Migrate from Basic Auth + plaintext browser session to token-based auth.
- Add auth lifecycle endpoints (`login/refresh/logout`) and revocation/expiry tests.

## Note To Self (Next Session)

- Next focus: **error contract consistency** (`403/404/validation/conflict` envelope and tests).
- Keep incremental commit discipline:
  - one feature slice per commit,
  - conventional commit format,
  - run focused tests before each commit.
- Avoid committing generated artifacts (`__pycache__`, build output) and keep commits scoped.
