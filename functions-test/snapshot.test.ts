import assert from "node:assert/strict";
import { test } from "node:test";
import { onRequestGet as getSnapshot, onRequestPut as putSnapshot } from "../functions/v1/progress/snapshot.ts";
import { signJwt } from "../functions/_lib/jwt.ts";
import { FakeKV, bearer, ctx, fakeEnv, rawRequest } from "./testkit.ts";

const run = (handler: unknown, req: Request, env = fakeEnv()) =>
  (handler as (c: unknown) => Promise<Response>)(ctx(req, env));

async function token(env = fakeEnv()) {
  return signJwt({ sub: "user-42", role: "STUDENT" }, env.JWT_SECRET);
}

test("GET without a bearer token is 401", async () => {
  assert.equal((await run(getSnapshot, rawRequest("GET", null))).status, 401);
});

test("GET with a bad token is 401", async () => {
  assert.equal((await run(getSnapshot, rawRequest("GET", null, bearer("garbage")))).status, 401);
});

test("GET before anything is stored is 404", async () => {
  const env = fakeEnv();
  const res = await run(getSnapshot, rawRequest("GET", null, bearer(await token(env))), env);
  assert.equal(res.status, 404);
});

test("PUT then GET round-trips the exact bytes and the updatedAt header", async () => {
  const env = fakeEnv();
  const t = await token(env);
  const payload = crypto.getRandomValues(new Uint8Array(2048));

  const put = await run(
    putSnapshot,
    rawRequest("PUT", payload, { ...bearer(t), "x-snapshot-updated-at": "1700000000000" }),
    env,
  );
  assert.equal(put.status, 204);

  const get = await run(getSnapshot, rawRequest("GET", null, bearer(t)), env);
  assert.equal(get.status, 200);
  assert.equal(get.headers.get("x-snapshot-updated-at"), "1700000000000");
  assert.deepEqual(new Uint8Array(await get.arrayBuffer()), payload);
});

test("one user cannot read another user's snapshot", async () => {
  const env = fakeEnv();
  const alice = await signJwt({ sub: "alice" }, env.JWT_SECRET);
  const bob = await signJwt({ sub: "bob" }, env.JWT_SECRET);
  await run(putSnapshot, rawRequest("PUT", new Uint8Array([1, 2, 3]), bearer(alice)), env);

  assert.equal((await run(getSnapshot, rawRequest("GET", null, bearer(bob)), env)).status, 404);
  assert.equal((env.SNAPSHOTS as unknown as FakeKV).store.has("snapshot:alice"), true);
});

test("PUT rejects an empty body", async () => {
  const env = fakeEnv();
  const res = await run(putSnapshot, rawRequest("PUT", new Uint8Array(0), bearer(await token(env))), env);
  assert.equal(res.status, 400);
});

test("PUT rejects an oversized body", async () => {
  const env = fakeEnv();
  const big = new Uint8Array(13 * 1024 * 1024);
  const res = await run(putSnapshot, rawRequest("PUT", big, bearer(await token(env))), env);
  assert.equal(res.status, 413);
});
