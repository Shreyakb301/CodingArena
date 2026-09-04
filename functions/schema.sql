-- D1 schema for the Cloudflare Pages backend.
-- Apply:  npx wrangler d1 execute codingarena --remote --file=functions/schema.sql

CREATE TABLE IF NOT EXISTS arena_user (
  id            TEXT PRIMARY KEY,
  display_name  TEXT NOT NULL,
  email         TEXT UNIQUE,
  role          TEXT NOT NULL DEFAULT 'STUDENT',
  password_hash TEXT,                 -- null for identity-provider accounts
  google_id     TEXT UNIQUE,
  created_at    INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS student_progress (
  user_id      TEXT PRIMARY KEY,
  payload_json TEXT NOT NULL,
  updated_at   INTEGER NOT NULL
);
