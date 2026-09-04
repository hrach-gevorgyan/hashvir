# Changelog

All notable changes to this project are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this
project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

Nothing yet.

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

[Unreleased]: https://github.com/hrach-gevorgyan/hashvir/compare/v0.2.2...HEAD
[0.2.2]: https://github.com/hrach-gevorgyan/hashvir/releases/tag/v0.2.2
[0.2.1]: https://github.com/hrach-gevorgyan/hashvir/releases/tag/v0.2.1
[0.2.0]: https://github.com/hrach-gevorgyan/hashvir/releases/tag/v0.2.0
