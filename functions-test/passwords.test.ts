import assert from "node:assert/strict";
import { test } from "node:test";
import { hashPassword, verifyPassword } from "../functions/_lib/passwords.ts";

test("hash then verify accepts the right password", async () => {
  const stored = await hashPassword("correct horse battery staple");
  assert.equal(await verifyPassword("correct horse battery staple", stored), true);
});

test("verify rejects the wrong password", async () => {
  const stored = await hashPassword("correct horse battery staple");
  assert.equal(await verifyPassword("Correct Horse Battery Staple", stored), false);
  assert.equal(await verifyPassword("", stored), false);
});

test("each hash uses a fresh salt", async () => {
  const a = await hashPassword("same-password");
  const b = await hashPassword("same-password");
  assert.notEqual(a, b);
  assert.equal(await verifyPassword("same-password", a), true);
  assert.equal(await verifyPassword("same-password", b), true);
});

test("verify handles missing or malformed stored hashes", async () => {
  assert.equal(await verifyPassword("x", null), false);
  assert.equal(await verifyPassword("x", ""), false);
  assert.equal(await verifyPassword("x", "no-colon"), false);
  assert.equal(await verifyPassword("x", "onlysalt:"), false);
});

test("stored format is base64(salt):base64(hash)", async () => {
  const stored = await hashPassword("pw");
  const [salt, hash] = stored.split(":");
  assert.match(salt, /^[A-Za-z0-9+/=]+$/);
  assert.match(hash, /^[A-Za-z0-9+/=]+$/);
  assert.equal(atob(salt).length, 16);
});
