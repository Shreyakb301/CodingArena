# CodingArena

A single-player coding-interview practice app, built with Kotlin Multiplatform
and Compose Multiplatform. iOS first, Android supported by the same codebase.

The product rule, taken from the brief:

> Do not optimize for the number of solved problems. Optimize for measurable
> improvement.

---

## Build status — read this first

| Part | Status |
|---|---|
| `:core` — domain, engines, content, curriculum, persistence, sync | **Compiles and passes 304 tests** on the JVM target |
| `:composeApp` — Compose UI, Android + iOS targets | **Written but never compiled** |
| `iosApp` — Xcode host | Generated from `project.yml`, never opened |

The environment this was built in cannot compile Compose Multiplatform at all.
Compose transitively requires `androidx.annotation` and `androidx.lifecycle`,
which are published **only** to Google's Maven repository (`dl.google.com`),
and that host is blocked by the sandbox network policy. There is also no
Android SDK, and Kotlin/Native cannot cross-compile Apple targets from Linux.

So the split above is not a design preference — it is the line between what
could be verified and what could not. Everything below that line is
conventional Compose written against the documented APIs, but expect to fix a
few version or API mismatches on first compile. Nothing in `:composeApp` has
ever been run.

`:core` is deliberately free of Compose so it stays buildable and testable on
any machine with just a JDK, including CI. As much logic as possible has been
pushed down into it for exactly this reason — the challenge screen's rules, for
instance, live in `ChallengeSession` in `:core` and are unit tested, leaving the
Compose `ChallengeViewModel` as a thin wrapper.

### Verify the part that works

```bash
cd CodingArena
./gradlew checkCore          # == :core:jvmTest, 304 tests, needs only a JDK
```

### See the UI, fastest route

```bash
./gradlew :composeApp:run
```

Opens the real app in a desktop window, constrained to phone width (390dp), in
seconds - no Xcode, no code signing, no device. Desktop is **not** a shipping
target; it exists so layout and navigation bugs can be found cheaply, because
the alternative is a full iOS build and a cable for every change.

It runs the same Compose code as iOS against the same SQLDelight database, so
almost anything broken there is broken on the phone too.

### Build the whole app

On a Mac with Xcode and (optionally) the Android SDK:

```bash
./gradlew build                              # targets are auto-detected
brew install xcodegen
cd iosApp && xcodegen generate && open iosApp.xcodeproj
```

Targets are detected in `settings.gradle.kts` and can be forced:

| Property | Default | Effect |
|---|---|---|
| `codingarena.android` | Android SDK present? | Adds the Android target |
| `codingarena.apple` | running on macOS? | Adds the iOS targets |
| `codingarena.ui` | either of the above | Includes `:composeApp` |

The desktop target is always present once `:composeApp` is included, so
`./gradlew :composeApp:run` works on any machine that can resolve Compose.

```bash
./gradlew build -Pcodingarena.android=false
```

The Android Gradle Plugin is only added to the buildscript classpath when it is
actually needed (see the root `build.gradle.kts`), which is what lets a
JDK-only machine configure the build at all.

---

## What is implemented

### The learning loop

`SubmitAnswerUseCase` is the spine of the app. One answer produces, in a single
pass: an attempt record, an Elo update across overall/topic/mode ratings, rating
history rows, a spaced-repetition reschedule, a streak update, a Solution
Review, and any newly unlocked achievements. It is one use case rather than
several on purpose — these effects must not drift apart.

### Rating engine (`RatingEngine`)

Elo as specified, with three deliberate departures:

- **Provisional K.** A larger K factor for the first five attempts on a rating,
  so a new user converges quickly instead of crawling.
- **Anti-farming damping.** Correct answers on problems more than 300 points
  below the user are worth ~25% of the normal gain. Losses are never damped.
  Replaying introductory content therefore cannot inflate a rating.
- **Per-attempt caps.** No single answer moves a rating more than 32 points.

Secondary topics move at 40% of the primary topic's delta, and Code Rush
answers at 50% weight overall.

### Solution Review (`SolutionReviewEngine`)

The flagship screen, and entirely rule-based — no model calls, so it works
offline and costs nothing to run. Specificity comes from the content: every
distractor carries a `rationale` (why *this* choice fails) and, where the
instinct behind it is sound, an `insight` (what was right about it). A wrong
answer therefore still opens with a genuine "good move" line.

Labels are weighted by how surprising the result was, using the Elo expected
score: missing a problem you had a 90% chance at is a **Blunder**; missing one
far above your rating is only an **Inaccuracy**.

### Interview readiness (`ReadinessEngine`)

Readiness is the **floor** of your core topics, not the average — an interview
asks whichever question it likes, so a 1600 Arrays rating does not offset a 700
Graphs rating. Breadth is a hard ceiling on top of that: three strong topics out
of twelve caps the score in the fifties no matter how strong they are.

*(This one was wrong first time round: breadth was named in the rationale text
but never actually constrained the number, so a narrow profile scored 79. A
test caught it and the engine was fixed.)*

### Placement test

Optional, skippable, and it samples all five areas the brief names. Scoring runs
an Elo pass with a deliberately large K — eight questions have to be able to move
the estimate across the whole range, which is precisely what a normal K factor
prevents. The result feeds three things: the starting rating, the topics seeded
as already-known, and the topic the first learning path targets.

### Roadmap and Blitz

The **NeetCode 150** (with the **Blind 75** flagged as a subset) is modelled as
a curriculum: 150 entries across 18 pattern groups. **Blitz** drills it as
flashcards — *"Longest Substring Without Repeating Characters — which pattern?"*
— with four options, a 450ms flash on a right answer and 1.4s on a wrong one so
the correct pattern registers before moving on.

Three decisions worth knowing:

- **Distractors are drawn from `confusableWith`, not at random.** Offering
  "Trie" against a Sliding Window problem teaches nothing; offering "Two
  Pointers" drills the mistake you would actually make.
- **A card locks in after three consecutive correct recalls**, and a single
  miss resets the streak to zero. Memorisation means getting it right
  repeatedly, not on average.
- **Roadmap progress is tracked separately from rating.** Rating measures
  skill and is deliberately hard to farm; roadmap progress measures coverage of
  a specific list. Drilling a card you already know barely moves your rating but
  should still visibly advance the map — so they are two scoreboards, not one.

**A wrong answer teaches.** Every miss shows the distinction between the two
patterns, authored per *pair* rather than per problem - 38 explanations cover
every wrong answer possible across all 150, and a test enforces that no
reachable mistake is left unexplained. A correct answer flashes past in 450ms;
a wrong one waits for a tap, because auto-advancing past an explanation defeats
the point of writing it.

> **Sliding Window vs Two Pointers**
> Both walk two indices, but for different reasons. A window keeps a contiguous
> run and its width changes to satisfy a constraint. Two pointers converge from
> the ends of ordered data, discarding a whole side each step.

**Cards come back on a schedule.** Recall records ride the same
1/3/7/14/30-day ladder as the rest of the app, so the roadmap works over weeks
rather than within a session. "Locked in" is a status, not a graduation - a
mastered card still returns to prove it stuck.

**Solved is tracked separately from recalled.** Knowing a problem is Sliding
Window and having written the solution are different milestones, so the roadmap
carries a checkbox for the second.

The most useful output is the confusion report: *"You called Sliding Window
problems Two Pointers 5 times"* is far more actionable than an accuracy figure.

**On content:** problem titles, slugs, difficulties and pattern groupings are
factual metadata about a published list. The original problem *statements* are
copyrighted and are **not** reproduced — each entry carries a one-line
paraphrase written for this app, plus a link out to solve the real thing.

**Needs a spot-check:** the Blind 75 flags are a hand transcription and
currently mark 76 problems, one more than the canonical list. `NeetCode150Test`
pins the count so it cannot drift further, but one flag is likely wrong and
should be checked against the source.

### Everything else

- **Spaced repetition** — the spec's 1/3/7/14/30-day ladder, with *slow* solves
  capped at a week and any miss dropping straight back to day one.
- **Streaks** — epoch-day based, idempotent within a day, and reachable only
  through a qualifying activity. Opening the app has no code path into it.
- **Code Rush** — three lives, difficulty ramping with each correct answer, four
  modes. Counts toward the streak only past five questions.
- **Learning paths** — lesson → easy → medium → review → mastery, aimed at one
  weakness, with a rationale written from the actual numbers.
- **Achievements** — 14 of them, all keyed to rating movement, streaks,
  hint-free solves or mastery. There is deliberately no "solve 100 problems".

### Content

46 curated problems covering all 20 categories from the brief and every one of
the 11 challenge types; a 20-entry pattern library; 14 achievements; and the
150-entry NeetCode roadmap.

Content integrity is enforced by tests, not by review. `StarterContentTest`
checks cross-references, per-type coverage, and that every distractor carries a
rationale. Four of those assertions exist because the content *was* broken:
five patterns shipped with a full lesson and no practice problems, and four
topics had no content at all — meaning the recommender could never target them
and a user weak in Backtracking, Greedy, Tries or Bit Manipulation was invisible
to the learning path engine. The tests now make that un-regressable.

### Offline-first

SQLDelight throughout, with the starter content seeded on first launch, so the
very first session works with no network and no backend. Attempts and
achievements carry `synced` flags for the upload pass in Phase 2.

---

## Layout

```
CodingArena/
├── core/                       # KMP, no Compose — builds anywhere with a JDK
│   ├── commonMain/kotlin/com/codingarena/
│   │   ├── content/            # bundled problems, patterns, achievements
│   │   ├── core/{common,database}/
│   │   ├── data/{local,remote,repository}/
│   │   ├── di/                 # Koin core module
│   │   └── domain/{model,engine,session,repository,sync,usecase}/
│   ├── commonMain/sqldelight/  # Arena.sq
│   ├── commonTest/             # engine + content tests
│   └── jvmTest/                # integration tests against real SQLite
├── composeApp/                 # Compose UI, Android + iOS targets
│   └── commonMain/kotlin/com/codingarena/
│       ├── app/                # App, navigation, routes
│       ├── core/design/        # theme + shared components
│       └── features/           # one package per screen, MVVM
└── iosApp/                     # SwiftUI host, generated by XcodeGen
```

The brief's tree put everything in `composeApp/src/commonMain`. Splitting the
non-UI half into `:core` was the one structural change made, for the reason in
the build-status section: it keeps the entire learning engine testable without
Compose, an Android SDK, or a Mac. Package names still follow the brief.

---

## Deviations from the brief

1. **Two Gradle modules** instead of one, as above.
2. **`ChallengeType.TIME_COMPLEXITY` / `SPACE_COMPLEXITY`** rather than the
   brief's single `COMPLEXITY`, since §5.4 asks for both as separate formats.
   Two further types were added — `DATA_STRUCTURE_CHOICE` and
   `PATTERN_RECOGNITION` — to cover the rest of §5.4.
3. **The Xcode project is generated**, not committed. A `.pbxproj` is thousands
   of lines of opaque UUIDs that cannot be reviewed and conflicts badly; a
   hand-written one that has never been opened in Xcode is a liability. The
   `project.yml` spec is short enough to read and includes the Gradle build
   phase.

---

## Not built

Everything the brief excludes from the MVP, plus these Phase 2 items:

- **A real backend.** `ArenaRemoteDataSource` defines the contract and
  `SyncUseCase` implements the pass — upload-first ordering, per-step failure
  isolation, and timestamp-based rating conflict resolution, all tested against
  a fake. What is missing is the Supabase client behind it; today the binding is
  `OfflineOnlyRemoteDataSource`, which honestly reports "no connection" rather
  than pretending to succeed. Swapping one Koin binding turns sync on.
- **Sign in with Apple / auth.** Profiles are local and guest-only.
- **Push notifications.** The settings toggle persists but nothing schedules.

---

## Testing

```
core/src/commonTest/   engines (rating, review, SRS, streaks, achievements,
                       Code Rush, learning path, readiness, placement,
                       recommender), the challenge session, model behaviour,
                       content integrity
core/src/jvmTest/      repositories, use cases, sync and a full end-to-end
                       journey, against in-memory SQLite
```

304 tests, all passing. `LearningLoopJourneyTest` walks the whole flagship loop
— install, onboard, solve, review, streak, rating movement — so it fails loudest
if the pieces stop fitting together.

There are **no UI tests** — see the build-status note.
