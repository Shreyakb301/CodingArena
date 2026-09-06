import assert from "node:assert/strict";
import { test } from "node:test";
import { error, getCookie, json, redirectToApp } from "../functions/_lib/http.ts";

test("json sets status and content-type", async () => {
  const res = json({ ok: true }, 201);
  assert.equal(res.status, 201);
  assert.equal(res.headers.get("content-type"), "application/json");
  assert.deepEqual(await res.json(), { ok: true });
});

test("error is a 400 by default with a message body", async () => {
  const res = error("nope");
  assert.equal(res.status, 400);
  assert.deepEqual(await res.json(), { message: "nope" });
  assert.equal(error("x", 401).status, 401);
});

test("getCookie extracts one cookie from the header", () => {
  const req = new Request("https://x/", { headers: { cookie: "a=1; arena_oauth_state=abc-123; b=2" } });
  assert.equal(getCookie(req, "arena_oauth_state"), "abc-123");
  assert.equal(getCookie(req, "missing"), null);
});

test("getCookie returns null when there is no cookie header", () => {
  assert.equal(getCookie(new Request("https://x/"), "anything"), null);
});

test("getCookie url-decodes the value", () => {
  const req = new Request("https://x/", { headers: { cookie: "t=a%20b%3Dc" } });
  assert.equal(getCookie(req, "t"), "a b=c");
});

test("redirectToApp builds a 302 to the app fragment", () => {
  const res = redirectToApp("https://codingarena.pages.dev", "token=xyz");
  assert.equal(res.status, 302);
  assert.equal(res.headers.get("location"), "https://codingarena.pages.dev/#token=xyz");
});
