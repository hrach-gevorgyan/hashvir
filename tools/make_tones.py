"""Generate the four non-verbal clips: chime and the three ascending star notes.

Pure stdlib, no encoder needed. Writes 16-bit mono WAV into app/src/main/res/raw.
Resource names ignore the extension, so `chime.wav` is still R.raw.chime and
SoundPool plays WAV as happily as OGG.

Run: python tools/make_tones.py
"""
import math
import struct
import wave
from pathlib import Path

RATE = 44100
OUT = Path(__file__).resolve().parent.parent / "app/src/main/res/raw"

# Marimba-like: a fundamental plus stretched inharmonic partials, each decaying
# faster than the one below it. Warm rather than glassy.
PARTIALS = [(1.0, 1.00, 1.00), (4.0, 0.28, 1.70), (9.2, 0.10, 2.60)]


def tone(freq, seconds, peak=0.72):
    n = int(RATE * seconds)
    samples = []
    for i in range(n):
        t = i / RATE
        value = 0.0
        for ratio, amp, decay in PARTIALS:
            value += amp * math.sin(2 * math.pi * freq * ratio * t) * math.exp(-decay * t / seconds * 5)
        # 8ms fade in, so the attack has no click, and a fade out to silence.
        attack = min(1.0, t / 0.008)
        release = min(1.0, (seconds - t) / 0.04)
        samples.append(value * attack * release)

    loudest = max(abs(s) for s in samples) or 1.0
    scale = peak / loudest
    return [int(max(-1.0, min(1.0, s * scale)) * 32767) for s in samples]


def write(name, samples):
    path = OUT / f"{name}.wav"
    with wave.open(str(path), "w") as f:
        f.setnchannels(1)
        f.setsampwidth(2)
        f.setframerate(RATE)
        f.writeframes(b"".join(struct.pack("<h", s) for s in samples))
    print(f"{path.name}  {path.stat().st_size // 1024}kB")


if __name__ == "__main__":
    # Round complete: one warm note, low and unhurried.
    write("chime", tone(392.00, 1.10))          # G4

    # The three stars, a major triad so tapping left to right rises.
    for index, freq in enumerate([523.25, 659.25, 783.99], start=1):   # C5 E5 G5
        write(f"star_{index}", tone(freq, 0.45))
