import assert from "node:assert/strict";
import { test } from "node:test";
import { onRequestGet as callback } from "../functions/v1/auth/google/callback.ts";
import { onRequestGet as start } from "../functions/v1/auth/google/start.ts";
import { linkOrCreateGoogleUser } from "../functions/_lib/db.ts";
import { verifyJwt } from "../functions/_lib/jwt.ts";
import { FakeD1, ctx, fakeEnv } from "./testkit.ts";

const GOOGLE = { GOOGLE_CLIENT_ID: "client.apps.googleusercontent.com", GOOGLE_CLIENT_SECRET: "gsecret" };

const run = (handler: unknown, req: Request, env = fakeEnv()) =>
  (handler as (c: unknown) => Promise<Response>)(ctx(req, env));

const cbRequest = (params: Record<string, string>, cookie?: string) => {
  const url = new URL("https://codingarena.pages.dev/v1/auth/google/callback");
  for (const [k, v] of Object.entries(params)) url.searchParams.set(k, v);
  return new Request(url, { headers: cookie ? { cookie } : {} });
};

/** Fakes an unsigned Google id_token (the callback decodes, does not verify). */
function idToken(claims: Record<string, unknown>): string {
  const b64 = (o: object) =>
    btoa(JSON.stringify(o)).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
  return `${b64({ alg: "RS256" })}.${b64(claims)}.sig`;
}

test("start is 501 until Google is configured", async () => {
  const res = await run(start, new Request("https://codingarena.pages.dev/v1/auth/google/start"));
  assert.equal(res.status, 501);
});

test("start redirects to the consent screen and sets a state cookie", async () => {
  const res = await run(start, new Request("https://codingarena.pages.dev/v1/auth/google/start"), fakeEnv(GOOGLE));
  assert.equal(res.status, 302);
  const loc = res.headers.get("location")!;
  assert.ok(loc.startsWith("https://accounts.google.com/o/oauth2/v2/auth?"));
  assert.ok(loc.includes(`client_id=${encodeURIComponent(GOOGLE.GOOGLE_CLIENT_ID)}`));
  assert.match(res.headers.get("set-cookie") ?? "", /arena_oauth_state=/);
});

test("callback rejects a missing or mismatched state", async () => {
  const env = fakeEnv(GOOGLE);
  const noState = await run(callback, cbRequest({ code: "c" }), env);
  assert.match(noState.headers.get("location")!, /auth_error=bad_state/);

  const mismatch = await run(callback, cbRequest({ code: "c", state: "x" }, "arena_oauth_state=y"), env);
  assert.match(mismatch.headers.get("location")!, /auth_error=bad_state/);
});

test("callback rejects a token with the wrong audience", async () => {
  const env = fakeEnv(GOOGLE);
  const originalFetch = globalThis.fetch;
  globalThis.fetch = async () =>
    new Response(JSON.stringify({ id_token: idToken({ aud: "someone-else", sub: "1", email: "a@b.com", email_verified: true }) }));
  try {
    const res = await run(callback, cbRequest({ code: "c", state: "s" }, "arena_oauth_state=s"), env);
    assert.match(res.headers.get("location")!, /auth_error=bad_audience/);
  } finally {
    globalThis.fetch = originalFetch;
  }
});

test("callback rejects an unverified email", async () => {
  const env = fakeEnv(GOOGLE);
  const originalFetch = globalThis.fetch;
  globalThis.fetch = async () =>
    new Response(JSON.stringify({
      id_token: idToken({
        aud: GOOGLE.GOOGLE_CLIENT_ID, iss: "https://accounts.google.com",
        sub: "g-1", email: "a@b.com", email_verified: false,
      }),
    }));
  try {
    const res = await run(callback, cbRequest({ code: "c", state: "s" }, "arena_oauth_state=s"), env);
    assert.match(res.headers.get("location")!, /auth_error=email_unverified/);
  } finally {
    globalThis.fetch = originalFetch;
  }
});

test("callback links the account and redirects to the app with a session token", async () => {
  const env = fakeEnv(GOOGLE);
  const originalFetch = globalThis.fetch;
  globalThis.fetch = async () =>
    new Response(JSON.stringify({
      id_token: idToken({
        aud: GOOGLE.GOOGLE_CLIENT_ID, iss: "https://accounts.google.com",
        sub: "g-42", email: "grace@hopper.dev", email_verified: true, name: "Grace",
      }),
    }));
  try {
    const res = await run(callback, cbRequest({ code: "c", state: "s" }, "arena_oauth_state=s"), env);
    assert.equal(res.status, 302);
    const loc = res.headers.get("location")!;
    assert.ok(loc.startsWith("https://codingarena.pages.dev/#token="));
    const token = decodeURIComponent(loc.split("#token=")[1]);
    const claims = await verifyJwt(token, env.JWT_SECRET);
    assert.ok(claims?.sub);
    assert.equal((env.DB as unknown as FakeD1).rows[0].email, "grace@hopper.dev");
  } finally {
    globalThis.fetch = originalFetch;
  }
});

test("linkOrCreateGoogleUser: links an existing email, then reuses the account", async () => {
  const db = new FakeD1();
  db.rows.push({
    id: "u1", display_name: "Grace", email: "grace@hopper.dev", role: "STUDENT",
    password_hash: "x", google_id: null, created_at: 1,
  });
  const linked = await linkOrCreateGoogleUser(db as never, "g-1", "grace@hopper.dev", "Grace H");
  assert.equal(linked.id, "u1");
  assert.equal(linked.google_id, "g-1");
  const again = await linkOrCreateGoogleUser(db as never, "g-1", "grace@hopper.dev", "Grace H");
  assert.equal(again.id, "u1");
  assert.equal(db.rows.length, 1);
});
