<div align="center">

<img src="docs/banner.png" alt="Հաշվի՛ր — Armenian counting game for toddlers" width="100%">

# Հաշվի՛ր

**An Armenian counting game for toddlers.** Numbers 1–10, spoken in Armenian, on an island.

[![Build](https://github.com/hrach-gevorgyan/hashvir/actions/workflows/release.yml/badge.svg)](https://github.com/hrach-gevorgyan/hashvir/actions/workflows/release.yml)
[![Download APK](https://img.shields.io/github/v/release/hrach-gevorgyan/hashvir?label=download%20apk&color=3BA55C)](https://github.com/hrach-gevorgyan/hashvir/releases/latest)
[![Android](https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&logoColor=white)](#-build)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1-7F52FF?logo=kotlin&logoColor=white)](#-build)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](#-build)

[![Permissions](https://img.shields.io/badge/permissions-none-3BA55C)](#-what-it-does-not-do)
[![Ads](https://img.shields.io/badge/ads-none-3BA55C)](#-what-it-does-not-do)
[![Tracking](https://img.shields.io/badge/tracking-none-3BA55C)](#-what-it-does-not-do)
[![Offline](https://img.shields.io/badge/works-offline-3BA55C)](#-what-it-does-not-do)
[![APK](https://img.shields.io/badge/apk-1.8%20MB-3BA55C)](#-build)

</div>

---

## 🥥 The story it lives in

**Պույ-պույ Ճստունի** is a mouse from an Armenian children's tale. She finds a coconut, climbs
inside, eats until she is too round to get back out, and cries herself thin enough to escape.

The whole app is set on her beach. Every word the child hears is hers — one voice, one face,
from the greeting to the last number.

---

## 🎮 Three ways to play

<table>
<tr>
<td width="33%" valign="top">

### 🍎 Հաշվել
**Count the fruit**

Tap each one and hear it counted — «մեկ, երկու, երեք…»

Then the fruit gather into one group, Պույ-պույ asks **«Քանի՞ հատ միրգ հաշվեցիր դու»**, and
the child answers out loud *before* the numeral appears.

</td>
<td width="33%" valign="top">

### 🔢 Գուշակել
**Pick the number**

«Ո՞րն է յոթը» — four cards, one right.

Wrong answers stay marked in red so she can see what is already ruled out. Nothing is scored,
nothing is taken away, and she can keep trying.

</td>
<td width="33%" valign="top">

### 🗣️ Սովորել
**Say it out loud**

A number fills the screen and Պույ-պույ asks what it is.

The child says it aloud; the grown-up next to her taps ✓ or ✗. Wrong just means the same
number comes round again.

</td>
</tr>
</table>

---

## 🧠 What it is actually teaching

Not counting out loud — most three-year-olds can already recite to ten.

What is being built is the mapping between **quantity → numeral → written word → spoken word**,
and above all **cardinality**: knowing that *the last number you say is the answer to how many
there are.*

> A child who counts four apples perfectly and then starts again from one when you ask "how
> many?" has the sequence but not the concept. That gap is the single biggest leap at this age,
> and it is what Հաշվել's question exists for.

---

## 🎨 Design rules

| | |
|---|---|
| 🚫 **No failure states** | No score, no timer, no lives, no losing. A wrong answer gets a soft "not yet" and another go. |
| 🔇 **No text for the child** | Except the Armenian number words, which are part of the lesson. Everything else is voice and picture. |
| 🎯 **Saturated subject, muted ground** | The fruit and numerals are always the most vivid things on screen. The island never competes. |
| 👆 **Large targets** | 126dp minimum — four times the usual adult minimum, because toddlers have gross motor control and little fine motor control. |
| 📱 **Tablets beat phones** | Above six items, a phone cannot fit comfortably tappable targets in the required columns. |

---

## 🔒 What it does **not** do

No internet. No analytics. No crash reporting. No ad SDK. No accounts. No data collection of
any kind. **The manifest declares zero permissions** — there is nothing to grant and nothing to
revoke.

Dependencies are AndroidX and Compose, and nothing else.

---

## 📦 Install

Grab the APK from [**Releases**](https://github.com/hrach-gevorgyan/hashvir/releases/latest) and
install it on any device running **Android 7.0 or newer**.

A tablet is recommended.

---

## 🔨 Build

```bash
./gradlew assembleDebug        # build
./gradlew testDebugUnitTest    # unit tests
./gradlew installDebug         # to a connected device
```

Kotlin · Jetpack Compose · minSdk 24 · single Activity · no navigation library.

Every drawing in the app — the six fruits, Պույ-պույ, the palms, the coconut — is a Compose
vector path. There is not one bitmap in the UI.

<details>
<summary><b>Repository layout</b></summary>

```
app/src/main/java/com/hrach/hashvir/
  game/       round generation, layout maths, view model
  audio/      SoundPool wrapper
  ui/         screens, the mouse, the fruit, the island
  theme/      palette, typography, contrast
tools/        audio normalisation and banner generation
```

Unit tests cover the layout rules, colour contrast, round generation and the choice generator.

</details>

<details>
<summary><b>Audio</b></summary>

43 clips in `app/src/main/res/raw`, one voice throughout, all normalised to −16 LUFS.
[AUDIO.md](AUDIO.md) lists every one with its Armenian text, for anyone re-recording them in
another voice or another language.

```bash
python tools/normalize_audio.py
```

</details>

<details>
<summary><b>Releases</b></summary>

GitHub Actions runs the tests on every push and attaches a signed APK to a Release when a tag
is pushed:

```bash
git tag v0.2 && git push origin v0.2
```

Signing keys come from repository secrets. Without them the build still succeeds and produces
an unsigned APK.

</details>

---

<div align="center">

Built for one three-year-old, and shared in case it helps anyone else teaching a small child to
count in Armenian.

<sub>Noto Sans Armenian is used under the SIL Open Font License — see
<a href="licenses/NotoSansArmenian-OFL.txt">licenses/</a>.</sub>

</div>
