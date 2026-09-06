import assert from "node:assert/strict";
import { test } from "node:test";
import { onRequestPost as register } from "../functions/v1/auth/register.ts";
import { onRequestPost as login } from "../functions/v1/auth/login.ts";
import { verifyJwt } from "../functions/_lib/jwt.ts";
import { FakeD1, ctx, fakeEnv, jsonRequest } from "./testkit.ts";

const run = (handler: unknown, req: Request, env = fakeEnv()) =>
  (handler as (c: unknown) => Promise<Response>)(ctx(req, env));

test("register creates a user and returns a valid session", async () => {
  const env = fakeEnv();
  const res = await run(register, jsonRequest({ displayName: "Ada", email: "ADA@Example.com", password: "longenough1" }), env);
  assert.equal(res.status, 201);
  const body = (await res.json()) as { token: string; userId: string; role: string; displayName: string };
  assert.equal(body.displayName, "Ada");
  assert.equal(body.role, "STUDENT");
  const claims = await verifyJwt(body.token, env.JWT_SECRET);
  assert.equal(claims?.sub, body.userId);
  // stored email is normalised
  assert.equal((env.DB as unknown as FakeD1).rows[0].email, "ada@example.com");
});

test("register rejects a short display name and short password", async () => {
  assert.equal((await run(register, jsonRequest({ displayName: "A", email: "a@b.com", password: "longenough1" }))).status, 400);
  assert.equal((await run(register, jsonRequest({ displayName: "Ada", email: "a@b.com", password: "short" }))).status, 400);
});

test("register rejects a garbage body", async () => {
  const bad = new Request("https://app.test/x", { method: "POST", headers: { "content-type": "application/json" }, body: "{" });
  assert.equal((await run(register, bad)).status, 400);
});

test("register rejects a duplicate email cleanly (not a 500)", async () => {
  const env = fakeEnv();
  await run(register, jsonRequest({ displayName: "Ada", email: "a@b.com", password: "longenough1" }), env);
  const dupe = await run(register, jsonRequest({ displayName: "Grace", email: "a@b.com", password: "longenough2" }), env);
  assert.equal(dupe.status, 400);
  assert.match((await dupe.json() as { message: string }).message, /registered/i);
});

test("register does not 500 when the unique constraint fires under a race", async () => {
  const env = fakeEnv();
  (env.DB as unknown as FakeD1).throwOnInsert = true;
  const res = await run(register, jsonRequest({ displayName: "Ada", email: "a@b.com", password: "longenough1" }), env);
  assert.equal(res.status, 400, `expected a clean 400, got ${res.status}`);
});

test("register requires an email for teachers", async () => {
  const res = await run(register, jsonRequest({ displayName: "Prof", password: "longenough1", role: "TEACHER" }));
  assert.equal(res.status, 400);
});

test("login succeeds with the right password and fails with the wrong one", async () => {
  const env = fakeEnv();
  await run(register, jsonRequest({ displayName: "Ada", email: "a@b.com", password: "longenough1" }), env);

  const ok = await run(login, jsonRequest({ email: "A@B.com", password: "longenough1" }), env);
  assert.equal(ok.status, 200);
  assert.ok((await ok.json() as { token: string }).token);

  const bad = await run(login, jsonRequest({ email: "a@b.com", password: "wrongpassword" }), env);
  assert.equal(bad.status, 401);
  assert.match((await bad.json() as { message: string }).message, /invalid credentials/i);
});

test("login on an unknown email is 401, not a leak", async () => {
  const res = await run(login, jsonRequest({ email: "nobody@nowhere.com", password: "whatever12" }));
  assert.equal(res.status, 401);
});

test("login rejects an empty body", async () => {
  assert.equal((await run(login, jsonRequest({}))).status, 400);
});
