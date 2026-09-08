import assert from "node:assert/strict";
import { test } from "node:test";
import { onRequestPost as register } from "../functions/v1/auth/register.ts";
import { onRequestGet as me } from "../functions/v1/auth/me.ts";
import { signJwt } from "../functions/_lib/jwt.ts";
import { ctx, fakeEnv, jsonRequest, rawRequest, bearer } from "./testkit.ts";

const run = (handler: unknown, req: Request, env = fakeEnv()) =>
  (handler as (c: unknown) => Promise<Response>)(ctx(req, env));

test("me returns the identity for a registered user's token", async () => {
  const env = fakeEnv();
  const reg = await run(register, jsonRequest({ displayName: "Ada", email: "a@b.com", password: "longenough1" }), env);
  const { token, userId } = (await reg.json()) as { token: string; userId: string };

  const res = await run(me, rawRequest("GET", null, bearer(token)), env);
  assert.equal(res.status, 200);
  assert.deepEqual(await res.json(), { userId, role: "STUDENT", displayName: "Ada" });
});

test("me is 401 without a token", async () => {
  assert.equal((await run(me, rawRequest("GET", null))).status, 401);
});

test("me is 401 for a token signed with the wrong secret", async () => {
  const token = await signJwt({ sub: "someone", role: "STUDENT" }, "not-the-real-secret");
  assert.equal((await run(me, rawRequest("GET", null, bearer(token)))).status, 401);
});

test("me is 404 when the account no longer exists", async () => {
  const env = fakeEnv();
  const token = await signJwt({ sub: "ghost", role: "STUDENT" }, env.JWT_SECRET);
  assert.equal((await run(me, rawRequest("GET", null, bearer(token)), env)).status, 404);
});
