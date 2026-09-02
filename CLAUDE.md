# CLAUDE.md — Հաշվի՛ր

Armenian counting game for a 3-year-old. Android, Kotlin, Compose. Solo project, no backend, no network.
The full product spec lives in [SPEC.md](SPEC.md) and is the source of truth for behaviour.

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

## Part 3 — Build order

Follow SPEC.md "Build order" strictly. **Do not start a step before the previous one runs on a device.**
Step 9 is a hard stop: ship and test with the child for a week before building recognition mode.

## Part 4 — Commands

```bash
./gradlew assembleDebug     # build
./gradlew testDebugUnitTest # unit tests (layout, contrast, distractors)
./gradlew installDebug      # to attached device
```

## Part 5 — Recorded deviations from SPEC.md

- **Every object carries the 3dp `#3A4454` outline, not just the sheep, and contrast is
  measured on the outline.** Saturated fills cannot clear 4.5:1 on pastel grounds — apricot
  reaches 2.2:1, star 1.5:1. SPEC's own fallback (exclude the pairing) would have deleted
  three of six object types. Outlining preserves full fill saturation and puts every pair at
  8.2–9.1:1. Decided with the owner, 2026-09-02.
- **`com.hrach.hashvir.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` appears in the merged
  manifest.** Injected by `androidx.core` for targetSdk 33+. Signature-level, self-scoped,
  not shown on the install screen. Left in place by decision; the app's own manifest still
  declares no permission.
- **Font is a static instance of the Google Fonts variable `NotoSansArmenian[wdth,wght]`,
  cut at wght 700 and 900.** The `notofonts` per-script builds carry no Latin digits, which
  the numerals need. Variable fonts also degrade to Regular below API 26. License in
  `licenses/NotoSansArmenian-OFL.txt`.
- **Object diameter is `max(percentage, dpFloor)`, then capped by what fits.** SPEC gives
  30%/24% of the shorter dimension, which lands under the 126dp floor on every phone
  (108dp and 86dp on a 360dp-wide screen). Decided with the owner, 2026-09-03.
- **Count 10 falls back to the phone split 4+3+3 when 5+5 cannot hold the floor.** 5+5 needs
  756dp of width at 126dp; a 7" tablet has 600dp and would otherwise render 100dp targets.
  SPEC assigns 5+5 to "tablet", which a 7" tablet is.
- **On a small phone the scatter grid drops below 2*count cells rather than the target below
  the floor.** SPEC asks for both; they conflict at count 5 on a 360dp screen. Target size
  wins — it is the one tied to motor control.
- **Object size scales with the screen; physical mm is not held constant.** SPEC step 7 asks
  for roughly constant mm, but "30%% of the shorter dimension" scales by definition. Objects
  run 20mm on a phone to 38mm on an 11" tablet. The real invariant is the 126dp floor: 20mm
  is a minimum for motor control, not a target, and bigger is easier to hit. Decided with the
  owner, 2026-09-03. The mm table is printed by `LayoutTest` on every run.
- **Պույ-պույ has no animated idle state.** SPEC's helper table gives idle a slow tail curl,
  blink and nose twitch, but the same document forbids looping ambient animation and requires
  her to be "completely still" while objects are tappable — the only time idle would show. The
  state is implemented as `Still`: static, minimised, in a corner. `Asking` arrives with
  recognition mode at step 10.
- **The mouse is drawn as Compose paths, not a res/drawable vector.** Ears and tail have to
  animate independently of the body, which is one layer per path either way; drawing them
  directly avoids a vector plus a matching set of animated wrappers.
