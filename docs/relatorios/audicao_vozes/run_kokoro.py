"""Roda com o python do venv de teste (kokoro-onnx, modelo int8 = o mais leve)."""
import sys
from pathlib import Path

import soundfile as sf

sys.path.insert(0, str(Path(__file__).resolve().parent))
from common import KOKORO, LINES, OUT, ram_mb, timed  # noqa: E402
from kokoro_onnx import Kokoro  # noqa: E402

here = Path(__file__).resolve().parent
base = ram_mb()
k, t_load = timed(lambda: Kokoro(str(here / "kokoro-v1.0.int8.onnx"), str(here / "voices-v1.0.bin")))
print(f"  carregou o modelo int8: {t_load:.2f}s")
print("\nKOKORO (int8)")
for lid, label, key, text in LINES:
    voice, speed = KOKORO[key]
    (samples, rate), dt = timed(lambda: k.create(text, voice=voice, speed=speed, lang="pt-br"))
    sf.write(OUT / f"{lid}_kokoro.wav", samples, rate, subtype="PCM_16")
    dur = len(samples) / rate
    print(f"  {label:28s} Kokoro {voice} vel {speed:<4} fala={dur:5.2f}s  gerar={dt:4.2f}s  (x{dt / dur:.2f} do tempo da fala)")
print(f"  memoria do processo com o modelo carregado: {ram_mb():.0f} MB (antes: {base:.0f} MB)")
