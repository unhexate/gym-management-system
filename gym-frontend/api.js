const BASE = 'http://localhost:8080';

function authHeaders() {
  try {
    const s = typeof getSession === 'function'
      ? getSession()
      : JSON.parse(localStorage.getItem('gym_session'));
    if (s) return { 'Authorization': 'Basic ' + btoa(s.email + ':' + s.password) };
  } catch { /* no session */ }
  return {};
}

async function api(method, path, body) {
  const opts = {
    method,
    headers: { 'Content-Type': 'application/json', ...authHeaders() },
  };
  if (body) opts.body = JSON.stringify(body);
  const res = await fetch(BASE + path, opts);
  if (res.status === 401) {
    if (typeof clearSession === 'function') clearSession();
    location.replace('login.html');
    return;
  }
  if (res.status === 403) { throw new Error('You do not have permission to perform this action.'); }
  const json = await res.json();
  if (!res.ok) throw new Error(json.message || `HTTP ${res.status}`);
  return json.data;
}

const get  = (path)       => api('GET',  path);
const post = (path, body) => api('POST', path, body);
const put  = (path, body) => api('PUT',  path, body);

// ── Users ──────────────────────────────────────────────
function registerUser(data)             { return post('/api/users', data); }
function updateProfile(userId, data)    { return put(`/api/users/${userId}/profile`, data); }

// ── Memberships ────────────────────────────────────────
function enrollMembership(data)         { return post('/api/memberships', data); }
function getMembership(memberId)        { return get(`/api/memberships/member/${memberId}`); }
function getMyMembership()              { return get('/api/memberships/me'); }

// ── Payments ───────────────────────────────────────────
function processPayment(data)           { return post('/api/payments', data); }
function getPayments(memberId)          { return get(`/api/payments/member/${memberId}`); }
function getMyPayments()                { return get('/api/payments/me'); }

// ── Workouts ───────────────────────────────────────────
function createWorkout(data)            { return post('/api/workouts', data); }
function getWorkout(memberId)           { return get(`/api/workouts/member/${memberId}`); }

// ── Attendance ─────────────────────────────────────────
function markAttendance(data)           { return post('/api/attendance', data); }
function getAttendance(memberId)        { return get(`/api/attendance/member/${memberId}`); }
function getMyAttendance()              { return get('/api/attendance/me'); }

// ── Reports ────────────────────────────────────────────
function getReports()                   { return get('/api/reports'); }

// ── Toast ──────────────────────────────────────────────
function toast(msg, type = 'ok') {
  const el = document.getElementById('toast');
  el.textContent = msg;
  el.className = `show ${type}`;
  clearTimeout(el._t);
  el._t = setTimeout(() => el.className = '', 3000);
}


