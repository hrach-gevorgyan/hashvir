"""Loudness-normalize every clip in res/raw to -16 LUFS and store them all as OGG.

The recorded voice, the generated marimba tones and the two MP3s were all at different
levels, so the chime jumped out over Պույ-պույ. This puts the whole set on one scale.

Run: python tools/normalize_audio.py
Requires: pip install soundfile numpy pyloudnorm
"""
import glob
import os

import numpy as np
import pyloudnorm as pyln
import soundfile as sf

TARGET_LUFS = -16.0
# -1.5 dBFS. Lossy encoders overshoot the input peak by a few tenths of a dB, so a ceiling
# any closer to full scale decodes above 1.0 and clips.
PEAK_CEILING = 0.84
RAW = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'app', 'src', 'main', 'res', 'raw')


def _limit(audio, rate):
    """Hold the peak under the ceiling without dragging the whole clip down.

    Scaling the entire clip by ceiling/peak was the obvious thing and it undoes the loudness
    match: synthetic speech has sharp transients, so one consonant sets the scale factor and
    the whole word ends up several LU quiet. Instead only the moments that exceed the ceiling
    are attenuated, on a smoothed envelope so the gain never steps.
    """
    peak = np.abs(audio).max()
    if peak <= PEAK_CEILING:
        return audio

    # Required attenuation per sample, 1.0 wherever the signal is already under the ceiling.
    need = np.minimum(1.0, PEAK_CEILING / np.maximum(np.abs(audio), 1e-9))

    # Smooth it over ~4ms, and take a running minimum first so the gain is already down
    # before the peak arrives rather than clamping on it.
    window = max(3, int(rate * 0.004))
    pad = np.pad(need, (window, window), mode="edge")
    envelope = np.minimum.reduce([pad[i:i + len(need)] for i in range(2 * window)])
    kernel = np.hanning(window * 2 + 1)
    kernel /= kernel.sum()
    envelope = np.convolve(envelope, kernel, mode="same")

    return audio * envelope


def normalize(path):
    audio, rate = sf.read(path, always_2d=True)
    # Mono: the clips are speech and short tones, and stereo doubles the APK for nothing.
    mono = audio.mean(axis=1)

    # Trim leading and trailing near-silence so a tap sound never waits on dead air.
    envelope = np.abs(mono)
    loud = np.where(envelope > envelope.max() * 0.02)[0]
    if len(loud):
        start = max(0, loud[0] - int(rate * 0.01))
        end = min(len(mono), loud[-1] + int(rate * 0.05))
        mono = mono[start:end]

    meter = pyln.Meter(rate)
    # A clip shorter than the 400ms measurement block cannot be metered; those get peak
    # normalized instead, which is close enough for a note that short.
    if len(mono) > rate * 0.45:
        loudness = meter.integrated_loudness(mono)
        if np.isfinite(loudness):
            mono = pyln.normalize.loudness(mono, loudness, TARGET_LUFS)

    mono = _limit(mono, rate)

    out = os.path.splitext(path)[0] + '.ogg'
    sf.write(out, mono, rate, format='OGG', subtype='VORBIS')
    if out != path:
        os.remove(path)
    return out, len(mono) / rate


if __name__ == '__main__':
    files = sorted(glob.glob(os.path.join(RAW, '*.ogg')) +
                   glob.glob(os.path.join(RAW, '*.mp3')) +
                   glob.glob(os.path.join(RAW, '*.wav')))
    for path in files:
        out, seconds = normalize(path)
        print('%-16s %5.2fs' % (os.path.basename(out), seconds))
    print('%d clips at %.1f LUFS' % (len(files), TARGET_LUFS))
