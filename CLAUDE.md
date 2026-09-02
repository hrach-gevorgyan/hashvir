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

- **Zero permissions** in the manifest. No INTERNET. If a change needs a permission, stop and ask.
- **No third-party dependencies** beyond AndroidX/Compose. No analytics, crash reporting, ads, Room, DataStore, DI.
- **No text on child-facing screens** except the Armenian number words. No labels, prompts, menus.
- **No failure states.** No score, timer, lives, "wrong" screen. **No red anywhere for errors** — amber only.
- **Saturated figure, pastel ground.** Countable objects are always the most saturated thing on screen. If any
  decoration out-competes them, it is a bug.
- **Stars are punctuation, not currency.** Always three, non-accumulating. Anything that makes them countable
  or bankable is out of scope — ask first.
- **Nothing on screen that isn't part of the task.** Decoration is a cost, not a neutral.
- Portrait locked, single Activity, no navigation library. Responsive from one composable tree via `WindowSizeClass`.
- Touch targets ≥ 126dp; 113dp floor only at counts 9–10 on phone. Never lower.
- Every animation ≤ 400ms; tap response begins within one frame; no looping ambient animation.
- SoundPool for all audio. Missing clip → log and no-op, never crash.
- Bundle the Armenian font. Never rely on system fallback.

## Part 3 — Commands

```bash
./gradlew assembleDebug     # build
./gradlew testDebugUnitTest # unit tests (layout, contrast, distractors)
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
