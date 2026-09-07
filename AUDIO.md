# Audio recording list — 47 clips

Drop finished files in `app/src/main/res/raw/`, then run `python tools/normalize_audio.py`,
which trims the silence, converts to mono OGG and puts the whole set at -16 LUFS.

All 43 spoken clips are **generated** by `tools/gen_voices.py` from `tools/words.csv`, plus
four non-verbal ones. They are build inputs: generated once, committed, no network at runtime
and no on-device TTS — Android has no Armenian voice, so runtime synthesis is not an option.

The voice is Piper `hy_AM-gor-medium` (male, the only free keyless Armenian voice there is)
put through a mouse shift: pitch and formants up 24.9%, tempo stretched back 2.15%, a highpass
at 150Hz and a 2.5dB lift at 3kHz to put back the consonants the shift costs.

`tools/words.csv` is the machine-readable version of this list, and
`tools/gen_voices.py` turns it into OGG files — the same generator the puy-puy app uses, so
Պույ-պույ sounds like one character across both. Clips are build *inputs*: they are generated
once and committed, and the app builds and runs with no network.

```bash
tools/get_piper_voice.sh          # 63MB Armenian voice, not committed
python tools/gen_voices.py        # piper, offline, no account needed
python tools/gen_voices.py --engine azure   # hy-AM-AnahitNeural, warmer, needs a key
```

Existing files are skipped, so this fills gaps rather than overwriting. `WordsCsvTest` fails if a clip the app plays has no line in the CSV.

Run `normalize_audio.py` after generating: `gen_voices.py` gets each clip close, and the
normalizer is what actually lands them all on -16 LUFS.
 This list exists so the set can be re-recorded in
another voice, or another language, without guessing at what each one says.

**File requirements**
- Format **OGG Vorbis**, mono, filename exactly as in the `File` column plus `.ogg`
  (e.g. `num_1.ogg`). `normalize_audio.py` converts MP3 and WAV for you.
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

## `praise_*` — after anything she gets right

Delighted, brief, and **varied** — she hears these more than anything except the numbers
themselves, so repetition is what wears them out first. The app plays a random one and never
the same twice running.

**Up to twelve are supported, and only the ones that exist are ever used.** Recording another
is a matter of dropping `praise_9.ogg` into `res/raw` and re-running the normalizer. No code
change, nothing to register.

Recorded:

| File | Says |
|---|---|
| `praise_1` | Ապրե՛ս |
| `praise_2` | Շատ լավ |
| `praise_3` | Կեցցե՛ս |
| `praise_4` | Հրաշալի է |

Also recorded:

| File | Says |
|---|---|
| `praise_5` | Ճի՛շտ է |
| `praise_6` | Դու կարողացա՛ր |
| `praise_7` | Ի՜նչ լավ ես անում |
| `praise_8` | Այո՛, ճի՛շտ է |

Worth more than another phrase: record a few of these **with different delivery** — one
laughing, one quieter, one more surprised. Eight phrases delivered identically will start to
sound automatic to her faster than four phrases delivered eight different ways.

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
| `how_many` | Քանի՞ հատ միրգ հաշվեցիր դու — asked at the end of every Հաշվել round, once the fruit have gathered into one group. Followed by 3s of silence, which is hers: this is the question she answers out loud, and it is what turns counting into quantity. |

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
