# Running CodingArena on your iPhone

Everything here assumes a MacBook. Xcode is macOS-only, so there is no iOS
path without one.

---

## One-time setup

```bash
# Xcode itself, from the Mac App Store (large - start this first)
xcode-select --install

# A JDK for Gradle, and XcodeGen for the project file
brew install openjdk@21 xcodegen
```

## Look at it on the Mac first

```bash
cd CodingArena
./gradlew :composeApp:run
```

The same UI in a desktop window, phone-width, in seconds. Fix what you can see
there before dealing with signing and cables - the iOS loop is much slower.

## Build

```bash
cd CodingArena

# 1. Shared code first. This catches the cheap errors without Xcode involved.
./gradlew build

# 2. Generate the Xcode project and open it
cd iosApp
xcodegen generate
open iosApp.xcodeproj
```

In Xcode:

1. Select the **iosApp** target → **Signing & Capabilities**
2. Tick **Automatically manage signing**
3. Set **Team** to your Apple ID (add it under Xcode → Settings → Accounts)
4. Plug in your iPhone and pick it as the run destination
5. Press **Run**

First launch on the device: iOS will refuse to open an app signed by an
untrusted developer. Go to **Settings → General → VPN & Device Management**,
tap your Apple ID, and trust it. This is once per signing certificate.

---

## Free vs paid signing

| | Free Apple ID | Developer Program ($99/yr) |
|---|---|---|
| App lifetime | **7 days**, then re-sign | 1 year |
| Install method | Cable + Xcode | TestFlight, over the air |
| Devices | 3 | 100 |

The free path works fine for trying it out. For daily use the weekly re-signing
gets old quickly - that is the real reason to pay, not any feature difference.

---

## When it goes wrong

**`JAVA_HOME is not set`** — Xcode build phases run without your shell
environment. The build script tries `/usr/libexec/java_home` and the usual
Homebrew locations; if none exist, install a JDK with `brew install openjdk@21`.

**A permission or sandbox error in the framework build phase** — Xcode 15+
sandboxes build scripts. `ENABLE_USER_SCRIPT_SANDBOXING: NO` in `project.yml`
covers this; if you regenerate the project by hand, keep that setting.

**`ld: framework not found ComposeApp`** — the Gradle build phase did not
produce the framework. Run it directly to see the real error:

```bash
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

**`Undefined symbol` for a Kotlin function you know exists** — check whether
Kotlin/Native renamed it on export. Functions whose names begin with `init`
become `doInit...` in Swift. This is why the Koin entry point is called
`setupKoin` and not `initKoin`.

**Bundle identifier is already taken** — change `PRODUCT_BUNDLE_IDENTIFIER` in
`project.yml` to something unique, then `xcodegen generate` again.

**Changed `project.yml` and nothing happened** — the `.xcodeproj` is generated,
not live. Re-run `xcodegen generate`.

---

## What has not been verified

None of this has run. It was written on Linux, where Compose Multiplatform
cannot be compiled at all and Kotlin/Native cannot target Apple. The
configuration follows the standard Compose Multiplatform layout and the known
sharp edges are handled above, but expect to fix something on the first build.

The shared logic in `:core` is a different story - that is tested, and
`./gradlew checkCore` will prove it on your machine in about a minute.
