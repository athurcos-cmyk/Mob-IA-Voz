"""Roda com o python do .venv do ai-service. Gera os WAVs do Piper como o jogo faz hoje e com o tempo corrigido."""
import sys
import wave

import numpy as np

sys.path.insert(0, r"C:\Users\Thurcos\Desktop\IA Mobs Voice\MobVozIA_2.1\ai-service")
sys.path.insert(0, str(__import__("pathlib").Path(__file__).resolve().parent))
from app import speech  # noqa: E402
from piper import SynthesisConfig  # noqa: E402
from common import LINES, OUT, profile, ram_mb, timed  # noqa: E402


def save(path, pcm_bytes, rate=48000):
    with wave.open(str(path), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(rate)
        w.writeframes(pcm_bytes)


def synth_fixed(text, voice, pitch, speed):
    """Igual a speech.synthesize, mas compensa o tempo: o tom nao altera mais a duracao."""
    v = speech._voice(voice)
    chunks = list(v.synthesize(text, SynthesisConfig(length_scale=max(0.6, min(pitch, 1.6)) / max(speed, 0.5))))
    audio = np.concatenate([c.audio_float_array for c in chunks])
    rate = chunks[0].sample_rate
    out = speech.resample(audio, int(rate * max(0.6, min(pitch, 1.6))), speech.SVC_RATE)
    return (np.clip(out, -1, 1) * 32767).astype(np.int16).tobytes()


base = ram_mb()
rows = []
loaded = set()
for lid, label, key, text in LINES:
    p = profile(key)
    voice, pitch, speed = p["voice"], p["pitch"], p["speed"]
    if voice not in loaded:
        _, t_load = timed(lambda: speech._voice(voice))
        loaded.add(voice)
        print(f"  carregou {voice}: {t_load:.2f}s")
    pcm, dt = timed(lambda: speech.synthesize(text, voice, pitch, speed))
    save(OUT / f"{lid}_piper_hoje.wav", pcm)
    dur = len(pcm) / 2 / 48000
    rows.append((label, f"Piper hoje ({voice.split('-')[1]}, tom {pitch}, vel {speed})", dur, dt))
    pcm2, dt2 = timed(lambda: synth_fixed(text, voice, pitch, speed))
    save(OUT / f"{lid}_piper_corrigido.wav", pcm2)
    rows.append((label, "Piper tempo corrigido", len(pcm2) / 2 / 48000, dt2))

print("\nPIPER")
for r in rows:
    print(f"  {r[0]:28s} {r[1]:42s} fala={r[2]:5.2f}s  gerar={r[3]:4.2f}s  (x{r[3] / r[2]:.2f} do tempo da fala)")
print(f"  memoria do processo com {len(loaded)} vozes carregadas: {ram_mb():.0f} MB (antes: {base:.0f} MB)")
