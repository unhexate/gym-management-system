# Frontend Logical Improvements Audit

Date: 2026-03-24
Scope reviewed: all files in `gym-frontend/`

## Executive Summary

The frontend is generally structured well, but there are multiple **role-flow and ownership mismatches** where users can access flows that are not logically theirs (or where the UI encourages staff-only actions for members).  
Most issues are not visual; they are **behavior and permission consistency** issues.

---

## 1) Critical Role & Ownership Issues

### 1.1 Payments page mixes staff and member workflows
- **Where:** `payments.html`, `auth.js`
- **Current behavior:**
  - `MEMBER` can access the page.
  - “Process a Payment” is hidden for members, but the page is still designed around staff processing and ID entry.
  - Members can load history by member ID field (auto-filled to own ID, read-only).
- **Problem:**
  - Payment submission ownership is unclear. The page implies staff-side payment recording, not member-initiated payment requests.
  - This creates conceptual mismatch (exactly the issue you noticed).
- **Improvement:**
  - Split payment logic into explicit flows:
    - **Staff flow (ADMIN/RECEPTIONIST):** record payment for a member.
    - **Member flow (MEMBER):** submit payment intent/proof for own membership only, or initiate checkout (depending on backend capability).
  - If backend only supports staff recording, then remove MEMBER access from `ROLE_PAGES` for `payments.html` and provide a member-only “My Billing” read-only page.

### 1.2 Membership lookup allows unrestricted ID targeting for non-members
- **Where:** `membership.html`
- **Current behavior:** Any allowed non-member role can look up any `memberId` by typing an ID.
- **Problem:** No ownership boundary at UI level; weak guard against accidental data exposure.
- **Improvement:**
  - Keep full lookup for `ADMIN` and `RECEPTIONIST`.
  - For `TRAINER`, limit lookup to assigned members only (if assignment data exists).
  - For `MEMBER`, keep strict self-only mode (already partially done).

### 1.3 Workout lookup allows unrestricted member targeting
- **Where:** `workouts.html`
- **Current behavior:** Trainer/Admin can view any member plan by entering arbitrary `memberId`.
- **Problem:** Missing assignment-aware boundaries for trainers.
- **Improvement:**
  - Restrict trainer lookups and plan creation to members assigned to that trainer.
  - Hide free-form member ID inputs for trainers; replace with assignment-driven selector list.

### 1.4 User management allows receptionist to create ADMIN users
- **Where:** `users.html`
- **Current behavior:** `RECEPTIONIST` can open User Management and role dropdown includes `ADMIN`.
- **Problem:** Privilege escalation risk through UI policy.
- **Improvement:**
  - In UI: if role is `RECEPTIONIST`, only allow creating `MEMBER` (and maybe `TRAINER`, based on policy).
  - Reserve creating `ADMIN` and `RECEPTIONIST` accounts for `ADMIN` only.

---

## 2) High-Impact Flow Improvements

### 2.1 Remove cross-entity manual ID dependency where context exists
- **Where:** `membership.html`, `payments.html`, `workouts.html`, `attendance.html`
- **Current behavior:** many forms require manual numeric IDs (`memberId`, `membershipId`, `trainerId`, `planId`).
- **Problem:** easy input errors, inconsistent ownership, and poor usability.
- **Improvement:**
  - Replace manual ID fields with contextual selectors/search:
    - staff selects member by name/email -> hidden resolved ID
    - trainer ID auto-derived from session (already partially present)
    - membership ID derived from active membership lookup, not manual typing

### 2.2 Attendance is write-only and session-local
- **Where:** `attendance.html`
- **Current behavior:** check-in log exists only in browser memory for the current page session.
- **Problem:** operators cannot verify real persisted attendance history from backend.
- **Improvement:**
  - Add backend-backed attendance history section (today + date range filters).
  - Keep transient local log only as a “just submitted” queue.

### 2.3 Registration in login page bypasses operational onboarding controls
- **Where:** `login.html`
- **Current behavior:** public register creates a MEMBER directly.
- **Problem:** may conflict with gym operations where reception/admin should verify member onboarding details before account activation.
- **Improvement:**
  - Decide one model clearly:
    - **Self-signup allowed:** keep, but make it explicit as “Create Member Account”.
    - **Staff onboarding required:** remove public register tab and direct users to reception.

### 2.4 API/auth model stores plaintext password in client session
- **Where:** `auth.js`, `api.js`
- **Current behavior:** email/password stored in `localStorage` and reused via Basic Auth.
- **Problem:** security and session integrity risk; also affects logic (hard to enforce session invalidation semantics).
- **Improvement:**
  - Move to token/session-based auth (short-lived access token + refresh flow).
  - Keep role and identity claims in token/session payload, not raw password.

---

## 3) Medium Logical Consistency Improvements

### 3.1 Home page descriptions do not adapt strongly by role
- **Where:** `index.html`
- **Current behavior:** links are filtered by role, but wording remains generic and sometimes staff-centric.
- **Improvement:** role-specific descriptions (e.g., MEMBER sees “View my payments” instead of “Process payment”).

### 3.2 Error handling can misclassify failures as empty data
- **Where:** `payments.html`, `workouts.html`, `membership.html`
- **Current behavior:** in some catch blocks, UI shows empty-state and also toasts error.
- **Problem:** user may infer “no data” when real issue is network/permission error.
- **Improvement:** separate explicit states:
  - empty result
  - unauthorized/forbidden
  - network/server failure

### 3.3 Reports page stale KPI rendering edge case
- **Where:** `reports.html`
- **Current behavior:** KPI row is shown when keys exist, but no explicit clearing path if later response has no KPI keys but non-empty payload.
- **Improvement:** always reset KPI container before rendering; explicitly hide when no KPI keys.

### 3.4 Update profile flow is ID-based for staff only
- **Where:** `users.html`
- **Current behavior:** update requires manual `userId` entry.
- **Improvement:** add user search/selection to reduce wrong-user edits; optionally add “My Profile” self-edit page for all roles.

---

## 4) Suggested Target Role Matrix (Frontend UX)

- **ADMIN**
  - Full access: users, memberships, payments, workouts, attendance, reports.
- **RECEPTIONIST**
  - Users (member creation only), memberships, staff-side payment recording, attendance.
  - No reports unless required by policy.
- **TRAINER**
  - Workouts for assigned members only.
  - Attendance only if trainers are expected to check in members.
  - No payments/users by default.
- **MEMBER**
  - My Membership (read/self service), My Workouts (read), My Payments (read or submit request).
  - No cross-member ID lookup, no staff process actions.

---

## 5) Recommended Implementation Order

1. **Fix role matrix + page access** (`auth.js`) to remove contradictory access first.  
2. **Split member vs staff payment UX** (`payments.html`) to align ownership model.  
3. **Eliminate free-form ID inputs where possible** with selector/search patterns.  
4. **Constrain receptionist user creation roles** (`users.html`).  
5. **Introduce backend-backed attendance history view** (`attendance.html`).

---

## 6) Minimum Viable Corrections (Quick Wins)

If you want only minimal high-value fixes first:
- Remove `MEMBER` from `payments.html` in `ROLE_PAGES` until member-payment flow exists.
- In `users.html`, hide `ADMIN` and `RECEPTIONIST` role options for receptionist sessions.
- In `workouts.html`, auto-lock trainer context and replace raw `memberId` with allowed members list.
- In `membership.html` and `payments.html`, keep member views self-only and remove editable member ID controls.

---

## 7) Backend Work Required for Each Task

This maps each frontend improvement to concrete backend work in the current Spring Boot codebase.

### 7.1 Task 1.1 (Split member vs staff payment workflows)
- **Controllers (`PaymentController`)**
  - Keep current staff endpoint for recording payments.
  - Add member-facing endpoint(s):
    - Option A: `POST /api/payments/member/self/request` (payment request/proof flow)
    - Option B: `POST /api/payments/member/self/checkout` (gateway-init flow)
  - Add `GET /api/payments/me` for self-history without passing `memberId`.
- **Security (`SecurityConfig`)**
  - `ADMIN`/`RECEPTIONIST`: can record payments for any member.
  - `MEMBER`: can only access self endpoints.
- **Services (`PaymentService`, `GymManagementFacade`)**
  - Separate method paths for `recordPaymentByStaff(...)` vs `createMemberPaymentRequest(...)`.
  - Enforce ownership in service layer (defense in depth, not UI-only).
- **Model/DB**
  - If using request flow, add `payment_status` (`PENDING`, `APPROVED`, `REJECTED`) and optional `proof_reference`.
- **Tests**
  - Add authorization tests for cross-member access rejection (`403`).
  - Add positive tests for self-payment history (`/api/payments/me`).

### 7.2 Task 1.2 (Membership ownership boundaries)
- **Controllers (`MembershipController`)**
  - Add `GET /api/memberships/me` for member self lookup.
  - Keep `GET /api/memberships/member/{id}` for staff/admin only.
- **Services (`MembershipService`)**
  - Add owner-aware retrieval method: resolve current principal -> member -> membership.
  - Optional trainer-aware lookup: `getMembershipForTrainerMember(trainerId, memberId)`.
- **Model/DB**
  - If trainer restrictions are needed, introduce/confirm trainer-member assignment relation.
- **Tests**
  - Member should not access another member’s data by ID.
  - Trainer should only access assigned members if policy is enabled.

### 7.3 Task 1.3 (Workout restrictions by assignment)
- **Controllers (`WorkoutController`)**
  - Add `GET /api/workouts/me` for members.
  - Add trainer-scoped list endpoint: `GET /api/trainers/me/members` (or equivalent).
- **Services (`WorkoutService`)**
  - On create/read, validate trainer-member assignment.
  - Reject unassigned member actions with `AccessDeniedException` or domain exception -> `403`.
- **Model/DB**
  - Add `trainer_member_assignment` table/entity if absent.
  - Add repository methods for `existsByTrainerIdAndMemberId(...)`.
- **Tests**
  - Trainer can create/read for assigned member.
  - Trainer blocked for unassigned member.

### 7.4 Task 1.4 (Prevent receptionist privilege escalation)
- **Controllers (`UserController`)**
  - Keep create endpoint but enforce creator-role policy server-side.
- **Services (`UserService` + `UserFactory`)**
  - Add creator-aware validation:
    - `RECEPTIONIST` can create only allowed roles (typically `MEMBER`).
    - Only `ADMIN` can create `ADMIN`/`RECEPTIONIST`.
- **Security (`SecurityConfig`)**
  - Ensure endpoint permission does not imply unrestricted target role creation.
- **Tests**
  - Receptionist creating admin should return `403` or validation error.

### 7.5 Task 2.1 (Replace manual ID flows with contextual lookups)
- **Controllers**
  - Add search/select endpoints to resolve IDs by business identity:
    - `GET /api/users/search?query=`
    - `GET /api/plans` (if plan list is not already exposed)
    - `GET /api/memberships/member/{id}/active` or `GET /api/memberships/me/active`
- **Services/Repositories**
  - Implement efficient indexed search on user name/email/phone.
  - Add active-membership resolver by member.
- **Model/DB**
  - Add indexes on `users.email`, `users.phone`, possibly `users.name` for quick lookup.
- **Tests**
  - Search returns role-appropriate users only.
  - Active membership resolution is deterministic for one member.

### 7.6 Task 2.2 (Backend-backed attendance history)
- **Controllers (`AttendanceController`)**
  - Add `GET /api/attendance/member/{id}` and/or `GET /api/attendance?from=&to=&memberId=`.
  - Add self endpoint for members if required: `GET /api/attendance/me`.
- **Services (`AttendanceService`)**
  - Add date-range query logic and optional pagination.
- **Repositories**
  - Add methods for by-member and by-time-window fetch.
- **Model/DB**
  - Ensure attendance timestamp is indexed for date filtering.
- **Tests**
  - Date range filters, role restrictions, and empty-result behavior.

### 7.7 Task 2.3 (Registration policy clarity)
- **Option A: Self-signup allowed**
  - Keep `POST /api/users` public only for role `MEMBER`.
  - Validate that public callers cannot set privileged roles.
- **Option B: Staff onboarding required**
  - Remove public access; require auth and role checks for all user creation.
  - Optionally add `status` field (`PENDING_APPROVAL`, `ACTIVE`) if approval workflow is needed.
- **Tests**
  - Public role escalation attempts must fail.
  - If approval model exists, inactive users cannot authenticate.

### 7.8 Task 2.4 (Move away from Basic Auth + plaintext session)
- **Security (`SecurityConfig`)**
  - Introduce token-based auth (JWT or opaque session tokens).
  - Configure access token TTL and refresh token strategy.
- **Controllers**
  - Add auth endpoints:
    - `POST /api/auth/login`
    - `POST /api/auth/refresh`
    - `POST /api/auth/logout`
    - `GET /api/users/me` remains as identity endpoint.
- **Services**
  - Add token issue/validation/revocation logic.
- **Model/DB**
  - If using refresh token persistence, add token table with revocation/expiry metadata.
- **Tests**
  - Token expiry, refresh, logout revocation, and role claim enforcement tests.

### 7.9 Task 3.1 (Role-adapted home descriptions)
- **Backend impact:** none required.
- **Optional enhancement:** add capability endpoint `GET /api/users/me/capabilities` so frontend can render behavior by server-defined permissions instead of hardcoded role maps.

### 7.10 Task 3.2 (Clear error state semantics)
- **Controllers / Exception Handling (`GlobalExceptionHandler`)**
  - Standardize error schema (`code`, `message`, `details`, `timestamp`, `path`).
  - Use consistent status mapping: `404` for not found, `403` for forbidden, `409` for state conflict, `500` for unexpected.
- **Tests**
  - Contract tests asserting error envelope and status codes by scenario.

### 7.11 Task 3.3 (Reports KPI rendering edge case)
- **Backend impact:** none mandatory.
- **Optional enhancement:** expose KPI-only endpoint `GET /api/reports/kpis` with stable contract to reduce frontend branching.

### 7.12 Task 3.4 (Profile update without raw user ID entry)
- **Controllers (`UserController`)**
  - Add self-update endpoint: `PUT /api/users/me/profile`.
  - Keep admin/staff update-by-id endpoint for operational use.
- **Services (`UserService`)**
  - Add field-level policy checks (e.g., who can edit role/email/password).
- **Tests**
  - Self-update cannot change restricted fields.
  - Staff/admin update policy remains enforced.

---

## 8) Cross-Cutting Backend Deliverables

To support all tasks cleanly, implement these shared backend foundations:

1. **Centralized authorization policy layer**
   - Keep ownership/role checks in service methods (not only in controllers).
2. **Current-user resolver utility**
   - Reusable helper to map authenticated principal -> domain `User`.
3. **Consistent API contracts**
   - Distinguish collection empty (`200 []`) vs missing resource (`404`) vs forbidden (`403`).
4. **DB migration scripts**
   - Track any new columns/tables (assignment, payment status, token store) through versioned migrations.
5. **Test matrix expansion**
   - Add role x endpoint x ownership tests for every new/changed endpoint.

---

## 9) Step-by-Step Implementation Plan (Highest Priority)

This plan focuses on the most important risk areas first: **authorization correctness**, **payment ownership**, and **privilege escalation prevention**.

### Phase 1 — Lock down role and ownership boundaries (Day 1–2)

1. **Freeze target authorization matrix**
  - Finalize who can do what for `ADMIN`, `RECEPTIONIST`, `TRAINER`, `MEMBER`.
  - Output: one source-of-truth matrix used by both backend and frontend.

2. **Enforce role limits in backend first (`SecurityConfig`)**
  - Restrict endpoint access by role before UI changes.
  - Add/confirm method-level guards where URL-level security is too broad.

3. **Add ownership checks in service layer**
  - Prevent cross-member access even if a user manipulates request IDs.
  - Priority services: `PaymentService`, `MembershipService`, `WorkoutService`, `UserService`.

4. **Block receptionist privilege escalation**
  - In `UserService`, reject receptionist attempts to create `ADMIN`/`RECEPTIONIST`.
  - Add explicit error response and tests.

**Exit criteria:** No endpoint allows unauthorized role access or cross-member access by changing IDs.

### Phase 2 — Fix payment workflow ownership (Day 2–4)

5. **Split payment API paths by actor**
  - Staff path: existing record-payment flow for `ADMIN/RECEPTIONIST`.
  - Member path: self-only flow (`/api/payments/me` + request/checkout endpoint).

6. **Implement self-history endpoint**
  - Add `GET /api/payments/me` resolved from authenticated principal.
  - Remove dependency on passing `memberId` for members.

7. **Adjust frontend payment page to role-specific UX**
  - Staff sees “record payment”.
  - Member sees self-billing/self-payment actions only.
  - If member payment submission is not ready, temporarily make member view read-only.

8. **Add payment authorization test suite**
  - Positive and negative tests for staff vs member permissions and self-only access.

**Exit criteria:** Member cannot perform staff payment actions; staff can manage operational payment flow; payment history is self-safe.

### Phase 3 — Remove dangerous manual-ID workflows (Day 4–6)

9. **Add backend lookup endpoints for selector-driven UI**
  - `GET /api/users/search?query=`
  - `GET /api/memberships/me` and/or active-membership endpoint
  - trainer-assigned member list endpoint

10. **Replace frontend raw ID inputs in priority pages**
   - `payments.html`, `membership.html`, `workouts.html` first.
   - Use selector/search controls that resolve IDs internally.

11. **Trainer assignment enforcement**
   - Ensure workouts can only be created/viewed for assigned members.
   - Add assignment checks at service layer and tests.

**Exit criteria:** Critical actions do not depend on user typing raw numeric IDs.

### Phase 4 — Attendance reliability and error-contract clarity (Day 6–7)

12. **Add backend attendance history endpoints**
   - Date-range and member-scoped queries.
   - Optional member self history endpoint.

13. **Update frontend attendance page to persisted history**
   - Keep transient “just submitted” list secondary; show real backend log as source of truth.

14. **Standardize API error contract**
   - Use consistent status and payload schema (`403`, `404`, `409`, etc.) via `GlobalExceptionHandler`.

**Exit criteria:** Attendance data is verifiable from backend; frontend can distinguish empty vs unauthorized vs server error.

### Phase 5 — Authentication hardening (Day 8–10)

15. **Introduce token-based auth endpoints**
   - Add login/refresh/logout flow.
   - Maintain `GET /api/users/me` for identity and role hydration.

16. **Migrate frontend session handling**
   - Stop storing plaintext password in browser storage.
   - Store only token/session metadata.

17. **Add auth lifecycle tests**
   - Token expiry, refresh validity, logout revocation, and role-claim enforcement.

**Exit criteria:** Browser no longer stores plaintext credentials; session lifecycle is revocable and test-covered.

---

## 10) Practical Delivery Order (If You Want MVP Fast)

If you need fastest risk reduction with minimal scope, implement in this exact order:

1. Role + ownership backend enforcement (Phase 1)
2. Payment flow split + member self-history (Phase 2)
3. Receptionist role-creation restriction (`users` policy) (from Phase 1, can be shipped immediately)
4. Manual-ID removal on payments/workouts/membership (Phase 3)
5. Attendance history endpoint + UI (Phase 4)
6. Token auth migration (Phase 5)

This sequence gives you security correctness first, then business-flow correctness, then UX reliability, and finally auth hardening.
