# RBAC Policy Manager — Frontend

Minimal React (Vite) frontend. No CSS framework, no design system — this exists
so the implemented backend features (especially the authorization
check / explainability engine) are easy to see and click through, not to
demonstrate frontend craft.

## Setup

1. Drop this `frontend/` folder into your project (or copy its contents into
   wherever your React app should live).
2. `cd frontend`
3. `npm install`
4. `cp .env.example .env` and set `VITE_API_BASE_URL` to your backend's URL
   (defaults to `http://localhost:8080` if you skip this).
5. `npm run dev` and open the printed local URL.
6. Log in with the seeded admin user (`admin` / `ChangeMe123!`, unless you've
   changed it).

## What's included

- **Login** — `POST /auth/login`, JWT stored in `sessionStorage`.
- **Dashboard** — pings `/api/ping`, links to every screen.
- **Actions / Resources / Permissions / Roles / Groups / Subjects** — one
  generic table+form component (`EntityListPage.jsx`) driven by
  `config/entityConfigs.js`. Create, list, disable. (No edit/enable UI —
  the backend doesn't expose a way to list disabled records, so there's
  nothing to enable from the frontend.)
- **Role / Group detail pages** — direct children + direct permissions,
  read-only (no hierarchy/association mutation API exists).
- **Subject detail page** — assigned roles + a role-assignment form
  (`POST /api/subjects/{id}/roles/{roleId}`). No remove-role control, since
  that endpoint returns `501`.
- **Authorization Check (the explainability viewer)** — the main feature.
  Pick a subject/resource/action, hit `POST /api/authorization/check`, and
  see the decision plus every evidence path rendered as a
  Subject → Role → (Group) → Permission timeline.

## Notes

- All data fetching is plain `fetch` via `src/api/client.js` — no query
  library, no state management beyond React state.
- Backend lists are active-only and unpaginated, so list screens do
  client-side search only, per the API's actual behavior.
- If you'd rather wire this into an existing frontend instead of running it
  standalone, everything under `src/` is self-contained and framework-plain
  (React + react-router-dom only) — copy folders in as needed.
