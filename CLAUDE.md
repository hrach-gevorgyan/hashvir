# CLAUDE.md — Հաշվի՛ր

Armenian counting game for a 3-year-old. Android, Kotlin, Compose. Solo project, no backend, no network.

Three modes, chosen from a menu: **Հաշվել** (count the fruit), **Գուշակել** (pick the number you
hear), **Սովորել** (say the number out loud, a grown-up marks it). The whole app is set on the
beach from the Armenian tale where Պույ-պույ finds a coconut, climbs in, eats until she is too
round to get out, and cries herself thin again.

---

## Part 1 — Behavioural guidelines

Behavioral guidelines to reduce common LLM coding mistakes.

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

### 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

### 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

### 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

### 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

**These guidelines are working if:** fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.

---

## Part 2 — Project hard rules

These override "use judgment". Violating one is a bug, not a style difference.

**Privacy and dependencies**

- **Zero permissions** in the manifest. No INTERNET. If a change needs a permission, stop and ask.
- **No third-party dependencies** beyond AndroidX/Compose. No analytics, crash reporting, ads,
  Room, DataStore, DI framework.
- Nothing leaves the device. There is no network code and no data collection of any kind.

**How it treats the child**

- **No failure states.** No score, no timer, no lives, no losing. A wrong answer gets a soft
  "not yet" and another go, and nothing is ever taken away.
- **Stars are punctuation, not currency.** Always three, non-accumulating, no total anywhere.
  Anything that makes them countable or bankable is out of scope — ask first.
- **One voice, one face.** Every spoken word is Պույ-պույ's, and her mouth moves while it plays.
- **Nothing on screen that is not part of the task.** The island behind the play area is muted
  and static; if any decoration out-competes the fruit or the numerals, it is a bug.

**Look**

- **Saturated subject, muted ground.** The fruit and the numerals are always the most saturated
  things on screen. Enforced for colour by `ContrastTest`: every fruit clears 4.5:1 against
  every background, every ink clears 7:1.
- Every fruit carries the shared 3dp outline, and contrast is measured on that outline —
  saturated fills cannot clear 4.5:1 against pastel on their own.
- **Touch targets ≥ 126dp.** `Layout` treats the percentage sizes as a floor, not a target, and
  logs a debug warning whenever a screen cannot hold 113dp. Phones cannot at high counts; that
  is a known limitation, not something to silently design around.
- Portrait locked, single Activity, no navigation library. One composable tree for phone and
  tablet, split only by `WindowSizeClass`.
- Bundle the Armenian font. Never rely on system fallback, which renders tofu on devices with
  no Armenian font. Armenian ascenders overflow the default line box, so any numeral or word
  needs an explicit `lineHeight`.

**Sound**

- SoundPool for everything. A missing clip logs once and no-ops; it never crashes.
- **Clips never overlap.** Lengths are recorded in `SoundBank.speechLengthMs` and every sequence
  is spaced against them. Re-run `tools/normalize_audio.py` after changing any clip and update
  those numbers.

**Deliberate reversals of the original design**

The first version of this app was text-free, animation-free and had no red anywhere. All three
were changed on purpose, and should not be "fixed" back:

- The **menu carries Armenian labels**, because an adult has to be able to pick a mode.
- **Red marks a ruled-out card** in Գուշակել and the grown-up's ✗ button in Սովորել. Red still
  never means failure — a wrong tap is amber and a soft sound.
- **Պույ-պույ is always moving** — breathing, blinking, walking. The original rule banned ambient
  animation to protect focus; the character being alive was judged worth more.

## Part 3 — Commands

```bash
./gradlew assembleDebug     # build
./gradlew testDebugUnitTest # layout, contrast, round generation, choices, clips
./gradlew installDebug      # to attached device
```

## Part 4 — Release

`.github/workflows/release.yml` builds on every push to `main` and attaches a signed APK to a
GitHub Release when a `v*` tag is pushed. Signing keys come from repository secrets; without
them the workflow still builds and uploads an unsigned APK.

```
git tag v0.2.0 && git push origin v0.2.0
```

This app is not going to Google Play. Distribution is the APK on the Releases page.
