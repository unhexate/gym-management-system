/**
 * auth.js – client-side RBAC + session management
 *
 * Session is stored in localStorage as:
 *   { email, password, role, name }
 *
 * Authentication uses HTTP Basic (credentials sent with every API call).
 * Role gating controls which nav links are visible and which pages can
 * be accessed directly.
 */

// ── Role → allowed pages ──────────────────────────────────────────────────
const ROLE_PAGES = {
  ADMIN:        ['index.html', 'users.html', 'membership.html', 'payments.html',
                 'workouts.html', 'attendance.html', 'reports.html'],
  RECEPTIONIST: ['index.html', 'users.html', 'membership.html', 'payments.html',
                 'attendance.html'],
  TRAINER:      ['index.html', 'workouts.html', 'attendance.html'],
  MEMBER:       ['index.html', 'membership.html', 'payments.html', 'workouts.html'],
};

// ── Nav link definitions (order determines display order) ─────────────────
const NAV_LINKS = [
  { href: 'index.html',      label: 'Home' },
  { href: 'users.html',      label: 'Users' },
  { href: 'membership.html', label: 'Membership' },
  { href: 'payments.html',   label: 'Payments' },
  { href: 'workouts.html',   label: 'Workouts' },
  { href: 'attendance.html', label: 'Attendance' },
  { href: 'reports.html',    label: 'Reports' },
];

// ── Session helpers ───────────────────────────────────────────────────────
const SESSION_KEY = 'gym_session';
const WINDOW_NAME_PREFIX = 'gym_session:';

function getWindowNameSession() {
  try {
    if (!window.name || !window.name.startsWith(WINDOW_NAME_PREFIX)) return null;
    return JSON.parse(window.name.slice(WINDOW_NAME_PREFIX.length));
  } catch {
    return null;
  }
}

function getSession() {
  try {
    const localSession = JSON.parse(localStorage.getItem(SESSION_KEY));
    if (localSession) return localSession;
  } catch { /* ignore localStorage issues */ }

  return getWindowNameSession();
}

function setSession(data) {
  try {
    localStorage.setItem(SESSION_KEY, JSON.stringify(data));
  } catch { /* ignore localStorage issues */ }

  try {
    window.name = WINDOW_NAME_PREFIX + JSON.stringify(data);
  } catch { /* ignore window.name issues */ }
}

function clearSession() {
  try {
    localStorage.removeItem(SESSION_KEY);
  } catch { /* ignore localStorage issues */ }

  try {
    if (window.name && window.name.startsWith(WINDOW_NAME_PREFIX)) {
      window.name = '';
    }
  } catch { /* ignore window.name issues */ }
}

function buildAuthHeaderFromSession(session) {
  if (!session?.email || !session?.password) return null;
  return 'Basic ' + btoa(session.email + ':' + session.password);
}

/** Build a Base64-encoded Basic auth header value. */
function basicAuthHeader(email, password) {
  return 'Basic ' + btoa(email + ':' + password);
}

async function validateSession() {
  const session = getSession();
  const authHeader = buildAuthHeaderFromSession(session);
  if (!authHeader) return false;

  try {
    const res = await fetch('http://localhost:8080/api/users/me', {
      headers: { 'Authorization': authHeader },
    });

    if (!res.ok) {
      clearSession();
      return false;
    }

    const json = await res.json();
    const user = json.data;
    setSession({
      ...session,
      role: user.role,
      name: user.name,
      id: user.id,
    });
    return true;
  } catch {
    return false;
  }
}

// ── Page guard ────────────────────────────────────────────────────────────
/**
 * Call at the top of every protected page.
 * @param {string[]} allowedRoles - roles that may visit this page.
 *   Pass null / [] to allow any authenticated user.
 * @returns {object|null} session object, or null if redirecting.
 */
function requireRole(allowedRoles) {
  const session = getSession();
  if (!session) {
    location.replace('login.html');
    return null;
  }
  if (allowedRoles && allowedRoles.length > 0 && !allowedRoles.includes(session.role)) {
    location.replace('index.html');
    return null;
  }
  return session;
}

function hasRole(role) {
  const session = getSession();
  return !!session && session.role === role;
}

function hasAnyRole(roles) {
  const session = getSession();
  return !!session && Array.isArray(roles) && roles.includes(session.role);
}

function visiblePagesForCurrentRole() {
  const session = getSession();
  return ROLE_PAGES[session?.role] || [];
}

// ── Nav renderer ──────────────────────────────────────────────────────────
function renderNav() {
  const nav = document.querySelector('nav');
  if (!nav) return;

  const session    = getSession();
  const role       = session?.role;
  const allowed    = ROLE_PAGES[role] || [];
  const currentPage = location.pathname.split('/').pop() || 'index.html';

  const links = NAV_LINKS
    .filter(l => !role || allowed.includes(l.href))
    .map(l => {
      const cls = l.href === currentPage ? 'active' : '';
      return `<a href="${l.href}" class="${cls}">${l.label}</a>`;
    }).join('');

  const userBlock = session
    ? `<span class="nav-user">${session.name} &bull; <span class="nav-role">${session.role}</span></span>
       <button class="btn btn-secondary btn-sm" onclick="logout()">Logout</button>`
    : '';

  nav.innerHTML = `<span class="brand">🏋️ GymMS</span>${links}${userBlock}`;
}

// ── Logout ────────────────────────────────────────────────────────────────
function logout() {
  clearSession();
  location.replace('login.html');
}

// Auto-render nav on every page load
document.addEventListener('DOMContentLoaded', renderNav);
