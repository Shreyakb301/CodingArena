export interface Env {
  DB: D1Database;
  JWT_SECRET: string;
  GOOGLE_CLIENT_ID?: string;
  GOOGLE_CLIENT_SECRET?: string;
  APP_URL?: string;
}

export interface UserRow {
  id: string;
  display_name: string;
  email: string | null;
  role: string;
  password_hash: string | null;
  google_id: string | null;
  created_at: number;
}

export interface AuthResponse {
  token: string;
  userId: string;
  role: string;
  displayName: string;
}

const uuid = (): string => crypto.randomUUID();

export const userByEmail = (db: D1Database, email: string) =>
  db.prepare("SELECT * FROM arena_user WHERE email = ?").bind(email.trim().toLowerCase()).first<UserRow>();

const userByGoogleId = (db: D1Database, googleId: string) =>
  db.prepare("SELECT * FROM arena_user WHERE google_id = ?").bind(googleId).first<UserRow>();

export async function createUser(
  db: D1Database,
  opts: { displayName: string; email: string | null; passwordHash: string | null; role?: string },
): Promise<UserRow> {
  const row: UserRow = {
    id: uuid(),
    display_name: opts.displayName.trim(),
    email: opts.email?.trim().toLowerCase() ?? null,
    role: opts.role ?? "STUDENT",
    password_hash: opts.passwordHash,
    google_id: null,
    created_at: Date.now(),
  };
  await db
    .prepare(
      "INSERT INTO arena_user(id, display_name, email, role, password_hash, google_id, created_at) VALUES (?,?,?,?,?,?,?)",
    )
    .bind(row.id, row.display_name, row.email, row.role, row.password_hash, null, row.created_at)
    .run();
  return row;
}

/** Returns the account linked to googleId, else links one with the same email, else creates a student. */
export async function linkOrCreateGoogleUser(
  db: D1Database,
  googleId: string,
  email: string,
  name: string,
): Promise<UserRow> {
  const byGoogle = await userByGoogleId(db, googleId);
  if (byGoogle) return byGoogle;

  const normalisedEmail = email.trim().toLowerCase();
  const byEmail = await userByEmail(db, normalisedEmail);
  if (byEmail) {
    await db.prepare("UPDATE arena_user SET google_id = ? WHERE id = ?").bind(googleId, byEmail.id).run();
    return { ...byEmail, google_id: googleId };
  }

  const displayName = name.trim().slice(0, 80) || normalisedEmail.split("@")[0];
  const row: UserRow = {
    id: uuid(),
    display_name: displayName,
    email: normalisedEmail,
    role: "STUDENT",
    password_hash: null,
    google_id: googleId,
    created_at: Date.now(),
  };
  await db
    .prepare(
      "INSERT INTO arena_user(id, display_name, email, role, password_hash, google_id, created_at) VALUES (?,?,?,?,?,?,?)",
    )
    .bind(row.id, row.display_name, row.email, row.role, null, googleId, row.created_at)
    .run();
  return row;
}
