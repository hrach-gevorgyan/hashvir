# Audio recording list — 43 clips

Drop finished files in `app/src/main/res/raw/`.

**File requirements**
- Format **OGG Vorbis**, filename exactly as in the `File` column plus `.ogg` (e.g. `num_1.ogg`).
- Lowercase, digits and underscore only — anything else and the resource will not compile.
- Mono, 44.1 kHz is fine. Keep each clip tight: trim leading silence to under ~30 ms, or the
  tap will feel laggy no matter how fast the code is.
- Loudness-normalize the whole set to **-16 LUFS** so nothing jumps in volume.
- All speech is Պույ-պույ's — one voice, one character, throughout.

---

## `num_1` … `num_10` — counting voice, played on each tap

The workhorse clips. She hears these more than anything else in the app.
**Keep these unprocessed** — no pitch shift, no character voice. Clarity beats character;
this is the learning content. Neutral, clear, even pace, rising or level intonation as when
counting objects one by one.

| File | Says | Latin |
|---|---|---|
| `num_1` | մեկ | mek |
| `num_2` | երկու | yerku |
| `num_3` | երեք | yerek' |
| `num_4` | չորս | ch'ors |
| `num_5` | հինգ | hing |
| `num_6` | վեց | vets' |
| `num_7` | յոթ | yot' |
| `num_8` | ութ | ut' |
| `num_9` | ինը | ine |
| `num_10` | տասը | tase |

## `total_1` … `total_10` — the total, announced at round end

Recorded as «Ընդամենը՝ X» — conclusive, slightly slower, a small pause before the number, so it
reads as a summing-up rather than another count. Unprocessed.

| File | Says |
|---|---|
| `total_1` | Ընդամենը՝ մեկ |
| `total_2` | Ընդամենը՝ երկու |
| `total_3` | Ընդամենը՝ երեք |
| `total_4` | Ընդամենը՝ չորս |
| `total_5` | Ընդամենը՝ հինգ |
| `total_6` | Ընդամենը՝ վեց |
| `total_7` | Ընդամենը՝ յոթ |
| `total_8` | Ընդամենը՝ ութ |
| `total_9` | Ընդամենը՝ ինը |
| `total_10` | Ընդամենը՝ տասը |

## `ask_1` … `ask_10` — recognition mode prompt

May be pitch-shifted slightly upward for character. Warm, curious, inviting — a question,
never a test.

| File | Says |
|---|---|
| `ask_1` | Ո՞րն է մեկը |
| `ask_2` | Ո՞րն է երկուսը |
| `ask_3` | Ո՞րն է երեքը |
| `ask_4` | Ո՞րն է չորսը |
| `ask_5` | Ո՞րն է հինգը |
| `ask_6` | Ո՞րն է վեցը |
| `ask_7` | Ո՞րն է յոթը |
| `ask_8` | Ո՞րն է ութը |
| `ask_9` | Ո՞րն է ինը |
| `ask_10` | Ո՞րն է տասը |

> **Check these two before recording the set.** `ask_9` and `ask_10` — «ինը» and «տասը» already
> end in ը, so the definite form is awkward written down. Say whichever sounds right out loud
> and record that; you have the native ear here, I don't.

## `praise_1` … `praise_4` — after a correct recognition tap

Delighted, brief. May be pitch-shifted upward.

| File | Says |
|---|---|
| `praise_1` | Ապրե՛ս |
| `praise_2` | Շատ լավ |
| `praise_3` | Կեցցե՛ս |
| `praise_4` | Հրաշալի է |

## `oops_1`, `oops_2` — after a wrong recognition tap

**Soft and warm, never buzzer-like.** There are no errors in this app; this is a gentle
"hmm, look again", closer to a hum than a word. Nothing falling or disappointed — she will
retry immediately and should not feel it as a failure. Short, under ~600 ms.

| File | Suggestion |
|---|---|
| `oops_1` | Հը՞մ |
| `oops_2` | Օ՜յ |

## Spoken, supplied as MP3

| File | Says |
|---|---|
| `intro` | Բարև։ Ես Պույ-պույն եմ։ Ազգանունս Ճստունի։ Ես քեզ կօգնեմ սովորել թվերը։ |
| `what_number` | Սա ո՞ր թիվն է — the prompt in Սովորել |
| `how_many` | **Still needed.** Քանի՞ հատ էր — asked at the end of every Հաշվել round, once the fruit have gathered into one group. Warm and curious, with a real pause after it: this is the question she answers out loud, and it is what turns counting into quantity. |

## Non-verbal — 4 clips

| File | Content |
|---|---|
| `chime` | Round complete. One warm, soft marimba note (G4). Plays 600 ms after the total. |
| `star_1` | Ascending note 1 — lowest |
| `star_2` | Ascending note 2 — middle |
| `star_3` | Ascending note 3 — highest |

The three star notes are the same instrument a major triad apart (C5, E5, G5), so tapping all
three left to right sounds like a little rising phrase.

**These four are generated, not recorded** — `python tools/make_tones.py` writes them as WAV
into `res/raw`. Resource names ignore the extension, so `chime.wav` is still `R.raw.chime`.
Replace them with recorded OGGs any time; just delete the WAVs so there is no name clash.
