import assert from "node:assert/strict";
import { test } from "node:test";
import { decodeJwt, signJwt, verifyJwt } from "../functions/_lib/jwt.ts";

const SECRET = "s3cr3t-value-for-tests";

test("sign then verify round-trips the claims", async () => {
  const token = await signJwt({ sub: "user-1", role: "STUDENT" }, SECRET);
  const claims = await verifyJwt(token, SECRET);
  assert.equal(claims?.sub, "user-1");
  assert.equal(claims?.role, "STUDENT");
  assert.equal(claims?.iss, "codingarena");
  assert.equal(typeof claims?.exp, "number");
});

test("verify rejects a token signed with a different secret", async () => {
  const token = await signJwt({ sub: "user-1" }, SECRET);
  assert.equal(await verifyJwt(token, "wrong-secret"), null);
});

test("verify rejects a tampered payload", async () => {
  const token = await signJwt({ sub: "user-1", role: "STUDENT" }, SECRET);
  const [h, , s] = token.split(".");
  const forged = btoa(JSON.stringify({ sub: "admin", role: "TEACHER" }))
    .replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
  assert.equal(await verifyJwt(`${h}.${forged}.${s}`, SECRET), null);
});

test("verify rejects malformed tokens", async () => {
  assert.equal(await verifyJwt("not-a-jwt", SECRET), null);
  assert.equal(await verifyJwt("a.b", SECRET), null);
  assert.equal(await verifyJwt("", SECRET), null);
});

test("verify rejects an expired token", async () => {
  const now = Math.floor(Date.now() / 1000);
  // Hand-roll a token that is already expired but correctly signed.
  const b64url = (o: object) =>
    btoa(JSON.stringify(o)).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
  const enc = new TextEncoder();
  const data = `${b64url({ alg: "HS256", typ: "JWT" })}.${b64url({ sub: "u", exp: now - 60 })}`;
  const key = await crypto.subtle.importKey("raw", enc.encode(SECRET), { name: "HMAC", hash: "SHA-256" }, false, ["sign"]);
  const sig = new Uint8Array(await crypto.subtle.sign("HMAC", key, enc.encode(data)));
  let s = "";
  for (const b of sig) s += String.fromCharCode(b);
  const token = `${data}.${btoa(s).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "")}`;
  assert.equal(await verifyJwt(token, SECRET), null);
});

test("decodeJwt reads claims without verifying (Google id_token path)", async () => {
  const token = await signJwt({ sub: "g-123", email: "a@b.com", aud: "client" }, SECRET);
  const claims = decodeJwt(token);
  assert.equal(claims.sub, "g-123");
  assert.equal(claims.email, "a@b.com");
});

test("tokens with unicode claims survive the round trip", async () => {
  const token = await signJwt({ sub: "u", name: "Ada Lovelace ✨" }, SECRET);
  assert.equal((await verifyJwt(token, SECRET))?.name, "Ada Lovelace ✨");
});
