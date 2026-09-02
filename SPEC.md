# Հաշվի՛ր — Armenian counting game for toddlers

Android app teaching numbers 1–10 in Armenian to a 3-year-old. Solo project, no backend, no network.

The whole design rests on one idea: **the child is counting real things, and the app gets out of the way.** Every decision below either serves the count or is cut.

---

## Non-negotiable constraints

- **Zero permissions.** The manifest declares none. No INTERNET. If a change requires a permission, stop and ask.
- **No third-party dependencies** beyond AndroidX/Compose. No analytics, no crash reporting, no ad SDK.
- **No instructional or UI text on child-facing screens.** No button labels, no prompts, no menus. Prompting is by audio and visual cue only. Does not apply behind the parent gate.
- **Number words are the exception and are required.** The Armenian word appears at round end beneath the numeral, as one visual unit. This triples the mapping — quantity, numeral, written word, spoken word — which is how numeral recognition is built at this age.
- **No failure states.** No score, no timer, no lives, no "wrong" screen.
- **Nothing on screen that isn't part of the task.** Colourful, decorated surroundings measurably disrupt structured play in 3–4-year-olds. Decoration is not neutral here; it is a cost.
- Portrait locked, single Activity, no navigation library.
- Responsive to phone and tablet from one composable tree driven by `WindowSizeClass`.

---

## Design system

### The core principle: saturated figure, pastel ground

Young children's retinal cones respond far more strongly to saturated, high-chroma colour than to pastels — pastels are genuinely harder for them to perceive and discriminate. But saturated, busy *environments* disrupt focused play in exactly this age group.

Therefore:

| Layer | Treatment |
|---|---|
| Countable objects | Fully saturated, high chroma. The most vivid thing on screen, always. |
| Numerals and words | Near-black, maximum contrast. Second most prominent. |
| Background, surfaces, chrome | Soft pastel, low chroma. |
| Պույ-պույ the mouse | Soft mid-tone. Present but never out-competing the objects. |

This is not a style choice, it is the information hierarchy: **whatever is most saturated is what she should be looking at and touching.** Never violate it. If a decorative element ends up more vivid than the objects, it is a bug.

### Palette

```
Backgrounds (pastel, rotate per round to keep novelty without clutter)
  bg-paper     #FBF6EE   warm off-white, default
  bg-sage      #E6EFE4
  bg-sky       #E4EDF6
  bg-peach     #FBEBE1
  bg-lilac     #F0E8F4

Objects (saturated — the figure layer)
  obj-apricot     #F5901E
  obj-pomegranate #D42B3A
  obj-grape       #7B3FA0
  obj-balloon     #E4356E
  obj-star        #F5C518
  obj-sheep       #FFFFFF  with #3A4454 outline at 3dp — white needs an outline to hold its edge on pastel

Ink
  ink-primary  #2E2A28   numerals, number words
  ink-soft     #6B6560   parent screen secondary text only

Character
  mouse-body   #B9B3C7   soft dove grey
  mouse-ear    #F2C4C9   pale pink, inner ear and nose
  mouse-detail #3A4454

Feedback
  fb-positive  #3BA55C   confetti, correct card border
  fb-neutral   #C9A227   used for the wobble on a wrong tap — amber, never red
```

**No red for errors, anywhere.** There are no errors in this app.

### Contrast requirements

Every object colour against every background must clear 4.5:1. Verify all 6 × 5 combinations in a unit test; if any pair fails, that pairing is excluded from the rotation rather than the colour being adjusted.

Ink on any background must clear 7:1.

### Typography

Number words are the only child-facing text, so the font is load-bearing. Requirements: full Armenian coverage, heavy weight, rounded open letterforms. Noto Sans Armenian Bold or Black is the safe default. **Bundle it — never rely on system fallback**, which on many Armenian-locale-absent devices renders tofu.

Check ու, և, and ը render correctly before committing to any alternative.

### Motion

- Every animation ≤ 400ms. A toddler taps faster than adults expect; queued animations feel broken.
- Response to a tap must begin within one frame. Contingency is the mechanic — when each touch is met with an immediate response, children feel in control and stay focused. Latency destroys this.
- Taps on other objects must register while an animation is in flight.
- No looping ambient animation anywhere. Nothing moves unless it is responding to her or marking a round boundary.
- Easing: `FastOutSlowIn` for entrances, spring with low stiffness for the tap bounce.

### Sizing

Nielsen Norman recommends **20mm × 20mm minimum** touch targets for young children — four times the adult 10mm minimum — because large targets exploit gross motor skills and forgive undeveloped fine motor control.

- **Absolute floor: 126dp** (≈20mm) for any child-facing tap target.
- **Phone exception:** at counts 9–10 a phone play area cannot fit 20mm targets in the required columns. There the floor drops to 113dp (≈18mm), and no lower. Log a warning in debug builds if computed size falls below 113dp.
- Spacing between adjacent objects: minimum 25% of object diameter. Crowded targets defeat large targets.
- Tablets are the preferred device for this app precisely because high counts fit properly. Note this in the store listing.

---

## Stack

- Kotlin, Jetpack Compose, minSdk 24, targetSdk current
- SoundPool for all audio (not MediaPlayer — clips are short and overlap)
- State in `remember`/`ViewModel`; persistence via SharedPreferences
- No Room, no DataStore, no DI framework

## Project layout

```
app/src/main/
  java/.../
    MainActivity.kt
    game/
      GameViewModel.kt       round generation, mode alternation
      Round.kt               data classes
      Layout.kt              scattered + structured placement
    audio/
      SoundBank.kt           SoundPool wrapper, preload on init
    ui/
      CountingScreen.kt
      RecognitionScreen.kt
      ObjectSprite.kt
      NumberGlyph.kt         numeral + Armenian word as one unit
      Helper.kt              Պույ-պույ
      Stars.kt
      Confetti.kt
      ParentGate.kt
      ParentSheet.kt
    theme/
      Palette.kt             the tokens above, as named constants
      Type.kt
  res/raw/                   audio
  res/font/                  bundled Armenian font
  res/drawable/              object + mouse vectors
```

---

## Audio assets

All in `res/raw`, OGG, loudness-normalized to -16 LUFS.

| Pattern | Count | Content |
|---|---|---|
| `num_1` … `num_10` | 10 | counting voice, played per tap |
| `total_1` … `total_10` | 10 | the total announced at round end |
| `ask_1` … `ask_10` | 10 | «Ո՞րն է X-ը» for recognition mode |
| `praise_1` … `praise_4` | 4 | Ապրե՛ս, Շատ լավ, Կեցցե՛ս, Հրաշալի է |
| `oops_1`, `oops_2` | 2 | soft, warm, never buzzer-like |
| `chime` | 1 | round complete |
| `star_1`, `star_2`, `star_3` | 3 | ascending notes for tappable stars |

`SoundBank` preloads everything at startup, exposes `play(name: String)`. Missing file → log and no-op, never crash.

**Voice treatment.** All speech is Պույ-պույ's. Praise and asking clips may be pitch-shifted slightly upward. The `num_*` and `total_*` clips stay unprocessed — they are the learning content and clarity beats character.

---

## Counting mode

```kotlin
data class Round(
    val count: Int,               // 1..10
    val objectType: ObjectType,
    val background: BackgroundTint,
    val positions: List<Offset>,  // normalized 0f..1f within play area
    val tapped: Set<Int>
)
```

**Range:** all counts 1–10 from first launch. No ramp, no unlock. The count list to ten is already familiar at three; what is being taught is quantity→numeral→word, which is no harder at 8 than at 3.

Draw `count` uniformly from 1..10, never repeating the previous round's count. Object type never repeats consecutively. Background tint rotates.

**Placement (`Layout.kt`) — two modes:**

- **Counts 1–5, scattered.** Grid with ≥ `2 * count` cells, choose `count` at random, jitter ±15% of cell size. Object diameter 30% of the shorter screen dimension for 1–3, 24% for 4–5.
- **Counts 6–10, structured rows.** Even spacing, no jitter, reads left-to-right top-to-bottom. 6→3+3, 7→4+3, 8→4+4, 9→3+3+3, 10→5+5 on tablet, 4+3+3 on phone. Diameter sized to hit the dp floor above.

Scattered is play; structured is countable. Do not scatter at high counts — she loses track.

**Interaction:**

1. Tap untapped object → add index to `tapped`, spring bounce 1.0 → 1.25 → 1.0 over 300ms, play `num_{tapped.size}`.
2. Tap already-tapped object → nothing. No sound, no animation, no penalty.
3. **Tapped objects stay at 85% scale and 45% opacity for the rest of the round.** At high counts this is what lets her see what's left without recounting. Do not remove or fully fade them.
4. `tapped.size == count` → 400ms pause, `NumberGlyph` scales in centred at 40% of the shorter dimension, Armenian word directly beneath at ~25% of glyph height, same colour, same animation, one unit. Play `total_{count}`, `chime` 600ms later.
5. 1.5s after the chime, next round.

`roundsCompleted` is persisted, used only to unlock recognition mode.

---

## Recognition mode

Unlocks at `roundsCompleted >= 30`. Thereafter alternate three counting rounds, three recognition rounds.

Պույ-պույ enters `asking`, plays `ask_{answer}`. Three cards side by side, each a large numeral with the Armenian word beneath, on a pastel card, each card ≥ 126dp tall. Distractors from `answer ± 1..3`, clamped to 1..10, distinct. Correct position randomized.

- Wrong tap → card shakes horizontally 200ms (±8dp, three cycles), `oops_1|2`, amber tint pulse, cards stay, she retries.
- Right tap → confetti from the card, `praise_n`, advance after 1.5s.

---

## Helper character — Պույ-պույ

A mouse named Պույ-պույ, the source of all speech, so the child hears one voice from one face.

**Art direction.** Sitting pose, oversized round ears, flat shapes, soft dove grey with pale pink inner ears and nose. The ears are the character — make them large enough to read at 15% of screen height, since a mouse silhouette without big ears reads as an indistinct blob at small size. Body is one static vector; ears and tail are separate layers and drive all animation. Thin curled tail, 2dp stroke minimum or it disappears on a pastel background. She must never be more saturated than the countable objects.

**Strict appearance rules.** Visible and animating only at round start and round end. While objects are tappable she is either off-screen or completely still at reduced size in a corner. She never animates during counting.

| State | Animation | Used |
|---|---|---|
| idle | slow tail curl, occasional blink and nose twitch | minimised |
| happy | both ears perk, whole-body bounce | round completion |
| asking | ears rotate forward, slight lean | start of recognition round |

Tapping her does nothing. She is the voice's face, not a companion.

---

## Stars

Punctuation, not currency. Three stars appear alongside the numeral on every completed round — always three, never fewer, regardless of speed, mistakes, or count.

- **Non-accumulating.** No total, no bank, no persistence, no counter anywhere.
- **Tappable.** Each plays an ascending note and spins. A toy attached to finishing, not a grade.
- Tapping does not advance or delay the round.

Do not add star totals, collection screens, unlocks gated on stars, or performance-based variation. Reward loops are the single most common way educational apps for young children overpower their own instruction. If a change would make stars something she can have *more or fewer* of, it is out of scope — ask first.

---

## Parent gate

A small circle in a screen corner, low contrast, requiring a continuous 3-second press with a visible progress ring. Releasing early resets to zero. Nothing else leads out of the game.

## Parent screen

An adult surface: full Armenian text, standard Material controls, normal font sizes, no playful styling. Bottom sheet over a dimmed play screen.

| Row | Control |
|---|---|
| Volume | slider plus mute toggle |
| Range | segmented: 1–10 (default) / 1–5 only |
| Reset progress | button with confirm dialog |
| About | version, author, no outbound links |

The range control is a fallback if ten objects prove overwhelming in practice. Default is the full range.

## First run

On first launch only, open the parent screen immediately with a short line of Armenian text stating that holding the corner button for three seconds returns here. Parent reads once, sets volume, dismisses into the game. Never shown automatically again.

This is the only onboarding. Everything else a parent needs lives in the Play Store description.

---

## Build order

Do not start a step before the previous one runs on a device.

1. **Skeleton.** Builds, blank themed screen, portrait locked, zero permissions.
2. **Palette + type.** `Palette.kt` and `Type.kt` with bundled font. A debug screen rendering every object colour on every background, with the computed contrast ratio printed. Fix failures now, not later.
3. **SoundBank.** Preloads `res/raw`, debug button plays `num_1`. Verify latency is imperceptible.
4. **One hardcoded round.** Four apricots at fixed positions, tap counting with audio, glyph plus word on completion.
5. **Layout.kt.** Both placement modes, dp floor enforcement, debug overlay drawing target bounds and printing computed mm.
6. **Round loop.** Auto-advance, full 1–10, no-repeat rules, background rotation.
7. **Responsive pass.** Phone, 7" tablet, 11" tablet. Physical object size in mm should stay roughly constant across all three.
8. **Պույ-պույ + stars** on the completion screen.
9. **STOP. Ship to device. Test with the actual child for a week.** Do not build further until this has happened.
10. **Recognition mode.**
11. **Parent gate, parent screen, first-run sheet, confetti polish, screen-pinning documentation.**

---

## Testing

Unit tests worth writing:

- `Layout.kt` scattered: no overlaps, all in bounds, counts 1–5
- `Layout.kt` structured: correct row splits, even spacing, counts 6–10
- `Layout.kt` sizing: computed target ≥ 113dp at every count on every reference screen size
- Contrast: every object/background pair ≥ 4.5:1, every ink/background pair ≥ 7:1
- Distractor generator: always distinct, always 1..10

Everything else is verified by hand on a device.

**What to watch during the week of real testing**, in priority order:

1. Does she tap the same object twice at counts 9–10? If yes, tapped-state dimming is too weak — go to 35% opacity.
2. Does she look at the numeral at round end, or has she already looked away? If she looks away, the 400ms pause is too long.
3. Does she repeat the Armenian words aloud? That's the actual success metric, not time-in-app.
4. Does she try to tap Պույ-պույ? If she does repeatedly, either give the mouse one harmless response or move her further out of the way.

---

## Deliberately not built

Score, medals, timers, lives, level select, in-game tutorial for the child, localization framework, dark mode, cloud sync, achievements, daily streaks, star totals, any accumulating reward, ambient background animation, background music, character voice-acting beyond the fixed clip set.

The parent screen and first-run sheet are the only exceptions to the no-chrome rule — do not grow them beyond the rows specified.
