# Հաշվի՛ր

An Armenian counting game for toddlers. Numbers 1–10, spoken in Armenian, on an island.

Built for one three-year-old, and shared in case it is useful to anyone else teaching a small
child to count in Armenian. No ads, no tracking, no accounts, no network. The manifest declares
no permissions at all.

## The idea

Պույ-պույ Ճստունի is a mouse from an Armenian children's tale: she finds a coconut, climbs
inside, eats until she is too round to get back out, and cries herself thin enough to escape.
The whole app is set on her beach, and every word the child hears is hers.

There are three modes.

| Mode | What happens |
|---|---|
| **Հաշվել** | Count the fruit. Tap each one and hear it counted. Then the fruit gather into one group, Պույ-պույ asks how many there were, and the child answers out loud before the numeral appears. |
| **Գուշակել** | «Ո՞րն է յոթը» — four cards, one right. Wrong answers stay marked so the child can see what is already ruled out. Nothing is scored. |
| **Սովորել** | A number appears and Պույ-պույ asks what it is. The child says it aloud and the grown-up sitting next to her marks it with the green or red button. |

## What it is trying to teach

Not counting out loud — most three-year-olds can already recite to ten. What is being built is
the mapping between **quantity, numeral, written word and spoken word**, and above all
**cardinality**: knowing that the last number you say is the answer to how many there are.

A child who counts four apples perfectly and then recounts from one when asked "how many?" has
the sequence but not the concept. That gap is what Հաշվել's question is for.

## Design rules

- **No failure states.** No score, no timer, no lives, no losing. A wrong answer gets a soft
  "not yet" and another go.
- **No text on the child's screens** except the Armenian number words, which are part of the
  lesson.
- **Saturated subject, muted ground.** The fruit and the numerals are always the most vivid
  things on screen; the island behind them never competes.
- **Large targets.** 126dp minimum, four times the usual adult minimum, because three-year-olds
  have gross motor control and not much fine motor control.
- **Tablets are better than phones.** At counts of seven and above a phone cannot fit
  comfortably tappable targets in the required columns.

## Build

Android Studio, or:

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew installDebug
```

Kotlin, Jetpack Compose, minSdk 24. No dependencies beyond AndroidX and Compose — no analytics,
no crash reporting, no ad SDK, no DI framework. Every drawing in the app, the fruit and the
mouse included, is a Compose vector path rather than an image.

Releases are built by GitHub Actions and attached to the
[Releases](../../releases) page as an APK.

## Audio

43 clips in `app/src/main/res/raw`, all spoken by one voice. [AUDIO.md](AUDIO.md) lists every
one with the Armenian text, for anyone re-recording them in another voice or another language.

## Credits

Noto Sans Armenian is licensed under the SIL Open Font License; see
[licenses/](licenses/NotoSansArmenian-OFL.txt).
