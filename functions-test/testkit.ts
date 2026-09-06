// In-memory fakes for the Workers runtime bits the functions use, so handlers
// can be tested with `node --test` and no Cloudflare account.
//
// Kept out of functions/ so wrangler never sees it.

import type { Env, UserRow } from "../functions/_lib/db.ts";

interface Stmt {
  sql: string;
  args: unknown[];
}

/** Understands only the handful of queries the functions actually issue. */
export class FakeD1 {
  rows: UserRow[] = [];
  throwOnInsert = false;

  prepare(sql: string) {
    return new FakeStatement(this, sql.replace(/\s+/g, " ").trim());
  }

  _run({ sql, args }: Stmt) {
    if (/^INSERT INTO arena_user/i.test(sql)) {
      if (this.throwOnInsert) throw new Error("UNIQUE constraint failed: arena_user.email");
      const [id, display_name, email, role, password_hash, google_id, created_at] = args as [
        string, string, string | null, string, string | null, string | null, number,
      ];
      if (email && this.rows.some((r) => r.email === email)) {
        throw new Error("UNIQUE constraint failed: arena_user.email");
      }
      this.rows.push({ id, display_name, email, role, password_hash, google_id, created_at });
      return;
    }
    if (/^UPDATE arena_user SET google_id/i.test(sql)) {
      const [googleId, id] = args as string[];
      const row = this.rows.find((r) => r.id === id);
      if (row) row.google_id = googleId;
      return;
    }
    throw new Error(`FakeD1: unhandled run: ${sql}`);
  }

  _first({ sql, args }: Stmt): unknown {
    if (/WHERE email = \?/i.test(sql)) return this.rows.find((r) => r.email === args[0]) ?? null;
    if (/WHERE google_id = \?/i.test(sql)) return this.rows.find((r) => r.google_id === args[0]) ?? null;
    throw new Error(`FakeD1: unhandled first: ${sql}`);
  }
}

class FakeStatement {
  private db: FakeD1;
  private sql: string;
  private args: unknown[] = [];
  constructor(db: FakeD1, sql: string) {
    this.db = db;
    this.sql = sql;
  }
  bind(...values: unknown[]) {
    this.args = values;
    return this;
  }
  async first<T = unknown>(): Promise<T | null> {
    return this.db._first({ sql: this.sql, args: this.args }) as T | null;
  }
  async run() {
    this.db._run({ sql: this.sql, args: this.args });
    return { results: [], success: true };
  }
}

export class FakeKV {
  store = new Map<string, { value: ArrayBuffer; metadata: unknown }>();

  async getWithMetadata<M = unknown>(key: string, _type: "arrayBuffer") {
    const hit = this.store.get(key);
    return { value: hit?.value ?? null, metadata: (hit?.metadata as M) ?? null };
  }
  async get(key: string, _type: "arrayBuffer") {
    return this.store.get(key)?.value ?? null;
  }
  async put(key: string, value: ArrayBuffer | ArrayBufferView | string, options?: { metadata?: unknown }) {
    const buf =
      typeof value === "string"
        ? (new TextEncoder().encode(value).buffer as ArrayBuffer)
        : ArrayBuffer.isView(value)
          ? (value.buffer as ArrayBuffer)
          : value;
    this.store.set(key, { value: buf, metadata: options?.metadata ?? null });
  }
  async delete(key: string) {
    this.store.delete(key);
  }
}

export function fakeEnv(overrides: Partial<Env> = {}): Env {
  return {
    DB: new FakeD1() as unknown as D1Database,
    SNAPSHOTS: new FakeKV() as unknown as KVNamespace,
    JWT_SECRET: "test-secret-0123456789",
    ...overrides,
  } as Env;
}

export const jsonRequest = (body: unknown, headers: Record<string, string> = {}) =>
  new Request("https://app.test/v1/x", {
    method: "POST",
    headers: { "content-type": "application/json", ...headers },
    body: JSON.stringify(body),
  });

export const rawRequest = (method: string, body: BodyInit | null, headers: Record<string, string> = {}) =>
  new Request("https://app.test/v1/x", { method, headers, body });

export const bearer = (token: string) => ({ authorization: `Bearer ${token}` });

/** Minimal EventContext for a handler under test. */
export const ctx = (request: Request, env: Env) =>
  ({ request, env, params: {}, next: async () => new Response() }) as never;
