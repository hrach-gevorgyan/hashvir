# Changelog

All notable changes to this project are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this
project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

Nothing yet.

## [0.3.0] — 2026-09-08

### Changed

- compileSdk and targetSdk to 36. Google Play requires it for new and updated apps as of
  31 August 2026. API 36 enforces edge-to-edge with no opt-out; every screen already insets
  its own content with `WindowInsets.safeDrawing` and the activity already draws behind the
  system bars, so nothing else changed — but this still wants checking on a real device.
- Lint now runs in CI, after the unit tests.
- **All 43 spoken clips regenerated** with the Piper Armenian voice and the mouse pitch shift,
  replacing the recorded ones. Timings retimed throughout: the synthetic clips are shorter,
  the greeting by nearly a second.
- Voice clips can now be generated rather than only recorded: `tools/words.csv` plus
  `tools/gen_voices.py`, taken from the puy-puy app so Պույ-պույ is one character across both.
  Piper offline by default, Azure `hy-AM-AnahitNeural` optionally. Generated clips are
  committed as build inputs; nothing is generated at runtime, which matters because Android
  has no Armenian voice at all.
- The loudness stage was rebuilt twice over to make the generated set usable. ffmpeg's
  single-pass `loudnorm` only estimates and landed clips 3–7 LUFS apart; `normalize_audio.py`
  then held peaks by scaling whole clips down, undoing the match again. Now: measure, flat
  gain, and a limiter that touches only the moments over the ceiling. 46 of 47 clips sit
  within 1dB of −16 LUFS, against a 7dB spread before.
- Պույ-պույ is male. The English prose in the README and the code comments said otherwise.
  No audio is affected: Armenian has no grammatical gender.

### Removed

- `res/font/noto_sans_armenian.xml`. Nothing referenced it: `Type.kt` loads the two TTFs
  directly. It also carried six lint warnings for using API 26 attributes on minSdk 24.

## [0.2.4] — 2026-09-03

### Fixed

- Հերթով only ever went up to five, and the numbers were laid out in reading order — 1 top
  left and the rest ascending across the screen — so the round could be finished left to right
  without knowing a single number. Placement is now shuffled, and the range runs 3 to 10.

## [0.2.3] — 2026-09-03

### Added

- **Հերթով, a fourth mode.** Three to ten numbers appear scattered and she taps them in
  order: 1, then 2, then 3. Each correct tap says the number aloud and ticks it off; a wrong one
  wobbles amber and costs nothing. After the first mistake the number she is looking for starts
  pulsing, so she is never stuck. This is ordinality — that numbers come in a fixed sequence and
  each has a place in it — where Հաշվել teaches cardinality. Uses no new audio.

### Changed

- Four more praise clips: Ճի՛շտ է, Դու կարողացա՛ր, Ի՜նչ լավ ես անում, Այո՛, ճի՛շտ է. Eight now.
- The pause for answering «Քանի՞ հատ միրգ հաշվեցիր դու» goes from three seconds to four.
- Praise clips are now dealt from a shuffled deck: every other clip is heard before any one
  repeats, and a fresh deck never opens with the clip that closed the last. Independent random
  draws repeat far more than people expect. The same now applies to the two oops clips.
- Praise is drawn from however many clips exist, up to twelve. Dropping `praise_9.ogg` into
  `res/raw` is all it takes to add another: no list to update and no code to change.

### Fixed

- Fruit could sit behind Պույ-պույ on a tablet. She is drawn over the play area, but the
  layout treated the whole screen as available, so on a wide screen the rows reached into her
  corner. `Layout.playHeight` now reserves the bottom strip she stands in, and rounds are both
  generated and drawn against that smaller area. A tall phone hid it because the same rows sit
  higher up.
- The four cards in Գուշակել were sized against the full screen height for the same reason and
  could reach her corner too.

## [0.2.2] — 2026-09-03

### Fixed

- The intro greeting was silent on slower devices. It is the longest clip and is asked for
  moments after launch, so SoundPool was often still decoding it and the play call no-opped —
  Պույ-պույ waved through her introduction in silence. The intro screen now waits for the clip
  to be ready, up to four seconds, before starting it and its timer.
- The set of decoded clip ids was a plain `mutableSetOf` written from SoundPool's callback
  thread and read from the main thread. It is now concurrent.

### Changed

- Documentation brought in line with the app: CLAUDE.md now records the three rules that were
  deliberately reversed (menu labels, red for ruled-out cards, a mouse that is always moving)
  rather than describing a version that no longer exists.
- README lists what is deliberately not built yet.

- **All 43 spoken clips regenerated** with the Piper Armenian voice and the mouse pitch shift,
  replacing the recorded ones. Timings retimed throughout: the synthetic clips are shorter,
  the greeting by nearly a second.
- Voice clips can now be generated rather than only recorded: `tools/words.csv` plus
  `tools/gen_voices.py`, taken from the puy-puy app so Պույ-պույ is one character across both.
  Piper offline by default, Azure `hy-AM-AnahitNeural` optionally. Generated clips are
  committed as build inputs; nothing is generated at runtime, which matters because Android
  has no Armenian voice at all.
- The loudness stage was rebuilt twice over to make the generated set usable. ffmpeg's
  single-pass `loudnorm` only estimates and landed clips 3–7 LUFS apart; `normalize_audio.py`
  then held peaks by scaling whole clips down, undoing the match again. Now: measure, flat
  gain, and a limiter that touches only the moments over the ceiling. 46 of 47 clips sit
  within 1dB of −16 LUFS, against a 7dB spread before.
- Պույ-պույ is male. The English prose in the README and the code comments said otherwise.
  No audio is affected: Armenian has no grammatical gender.

### Removed

- Dead constant `Island.CoconutFlesh`, an unused import, and the last inline fully-qualified
  references. The build is now free of compiler warnings.

## [0.2.1] — 2026-09-03

### Fixed

- Release APKs are now signed. 0.2.0 was built before a signing key existed, so its APK could
  not be installed at all — Android refuses an unsigned package outright.

## [0.2.0] — 2026-09-03

First public release. The APK attached to this tag is unsigned and will not install; use
0.2.1 or later.

### Added

- **Three modes, chosen from a menu.** Հաշվել (count the fruit), Գուշակել (pick the number you
  hear), Սովորել (say the number out loud while a grown-up marks it).
- **Cardinality step in Հաշվել.** After the last tap the fruit gather into a single group,
  Պույ-պույ asks «Քանի՞ հատ միրգ հաշվեցիր դու», and the child has three seconds of silence to
  answer before the numeral confirms it. This is the difference between reciting a sequence and
  understanding that the last number said is the answer.
- **Պույ-պույ Ճստունի**, the mouse from the Armenian coconut tale, as the single voice and face
  of the app. She breathes, blinks, walks, waves, thinks, hops when the answer is right, shakes
  her head when it is not, gestures with either paw, and moves her jaw in time with every clip
  she speaks.
- **Tropical island setting** drawn from the tale: sun, clouds, a sea band, palms, shells, a
  beach she chases a rolling coconut along on the menu.
- **Six fruits** — apple, orange, banana, pear, strawberry, grapes — all Compose vector paths.
- **43 Armenian audio clips**, one voice, normalised to −16 LUFS.
- **Bundled Noto Sans Armenian** in Bold and Black, cut as static instances from the variable
  font so numerals never fall back to a system font.
- Confetti, three tappable stars with ascending notes, and four spoken praises on completion.
- GitHub Actions workflow: tests on every push, a signed APK attached to each `v*` tag.

### Design

- **No failure states anywhere.** No score, no timer, no lives, no losing.
- **No text on the child's screens** except the Armenian number words.
- **Zero permissions** in the manifest, no network code, no analytics, no ad SDK, and no
  dependencies beyond AndroidX and Compose.
- **126dp minimum touch targets**, four times the usual adult minimum, with the computed size
  logged in debug builds when a screen cannot hold it.
- Colour contrast enforced by unit test: every fruit clears 4.5:1 against every background,
  every ink clears 7:1.

### Known limitations

- On phones, counts of seven and above fall to roughly 13–14mm targets, below what is
  comfortable for a three-year-old. A tablet fits them properly.
- No parent controls yet: no volume, no mute, no way to restrict the range to 1–5, and no gate
  in front of the back button.
- Zero, subitising and comparing quantities are not covered.

[Unreleased]: https://github.com/hrach-gevorgyan/hashvir/compare/v0.3.0...HEAD
[0.3.0]: https://github.com/hrach-gevorgyan/hashvir/releases/tag/v0.3.0
[0.2.4]: https://github.com/hrach-gevorgyan/hashvir/releases/tag/v0.2.4
[0.2.3]: https://github.com/hrach-gevorgyan/hashvir/releases/tag/v0.2.3
[0.2.2]: https://github.com/hrach-gevorgyan/hashvir/releases/tag/v0.2.2
[0.2.1]: https://github.com/hrach-gevorgyan/hashvir/releases/tag/v0.2.1
[0.2.0]: https://github.com/hrach-gevorgyan/hashvir/releases/tag/v0.2.0
