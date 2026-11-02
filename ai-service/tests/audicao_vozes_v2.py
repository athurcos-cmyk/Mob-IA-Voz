"""Gera amostras para OUVIR a proposta de mais variedade de voz: mistura das 3 vozes pt-BR do Kokoro em proporcoes diferentes (cada criatura ganha um timbre
proprio) + um efeito de personalidade leve (golem metalico, Warden com eco, abelha aguda...). Para cada criatura grava o "hoje" (3 vozes so, tom +-8%) e o "proposto".

Uso:  python tests/audicao_vozes_v2.py [pasta_de_saida]     (padrao: docs/relatorios/audicao_vozes_v2)
Roda com prioridade baixa; leva ~1 minuto de CPU. Depois de ouvir, diga quais criaturas ficaram boas/ruins e ajustamos."""
import sys
import wave
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from app import speech  # noqa: E402

try:  # prioridade abaixo do normal: nao atrapalha o jogo aberto
    import ctypes
    ctypes.windll.kernel32.SetPriorityClass(ctypes.windll.kernel32.GetCurrentProcess(), 0x00004000)
except Exception:  # noqa: BLE001
    pass

RATE = 24000


def lowpass(x, hz):
    from scipy.signal import butter, lfilter
    b, a = butter(2, hz / (RATE / 2))
    return lfilter(b, a, x)


def reverb(x, seconds, wet):
    n = int(RATE * seconds)
    ir = np.random.default_rng(7).standard_normal(n) * np.exp(-np.linspace(0, 6, n))
    ir[0] = 0
    y = np.convolve(x, ir / np.abs(ir).sum() * 4)[: len(x) + n]
    out = np.zeros(len(y))
    out[: len(x)] += x * (1 - wet)
    return out + y * wet


def ring(x, hz, mix):
    t = np.arange(len(x)) / RATE
    return x * (1 - mix) + x * np.sin(2 * np.pi * hz * t) * mix


def tremolo(x, hz, depth):
    t = np.arange(len(x)) / RATE
    return x * (1 - depth + depth * (0.5 + 0.5 * np.sin(2 * np.pi * hz * t)))


def drive(x, gain):
    return np.tanh(x * gain) / np.tanh(gain)


def echo(x, ms, fb):
    d = int(RATE * ms / 1000)
    y = np.concatenate([x, np.zeros(d * 3)])
    for k in range(1, 4):
        y[d * k: d * k + len(x)] += x * fb ** k
    return y


EFFECTS = {
    "": lambda x: x,
    "metalico": lambda x: ring(lowpass(x, 5000), 70, 0.22),
    "eco_grave": lambda x: reverb(lowpass(x, 4000), 1.4, 0.35),
    "agudo_tremulo": lambda x: tremolo(x, 22, 0.35),
    "molhado": lambda x: tremolo(lowpass(x, 2600), 6, 0.25),
    "oco": lambda x: echo(lowpass(x, 5500), 55, 0.35),
    "rouco": lambda x: drive(x, 2.2),
    "sombrio": lambda x: ring(reverb(x, 1.0, 0.3), 40, 0.18),
    "etereo": lambda x: reverb(tremolo(x, 9, 0.2), 1.1, 0.4),
}

# nome, linha falada, mistura {voz: peso}, tom (1 = natural da mistura), velocidade, efeito, "hoje" (voz piper, tom do perfil) para comparar
CREATURES = [
    ("vaca", "Muuu... eu só quero pastar em paz, humano.", {"pf_dora": 0.7, "pm_alex": 0.3}, 0.92, 0.95, "", ("pt_BR-faber-medium", 0.88)),
    ("porco", "Eu topo trocar, mas só se for cenoura das boas.", {"pm_alex": 0.6, "pf_dora": 0.4}, 1.06, 1.0, "", ("pt_BR-cadu-medium", 1.0)),
    ("galinha", "Có! Tem semente? Eu troco pena por semente.", {"pf_dora": 1.0}, 1.28, 1.12, "", ("pt_BR-faber-medium", 1.22)),
    ("ovelha", "Béééh... será que tem grama fresca por aqui?", {"pf_dora": 0.8, "pm_santa": 0.2}, 1.06, 0.92, "", ("pt_BR-faber-medium", 1.08)),
    ("cavalo", "Quero correr livre pelas planícies, sem sela.", {"pm_alex": 0.5, "pm_santa": 0.5}, 0.97, 1.0, "", ("pt_BR-jeff-medium", 0.8)),
    ("lobo", "Na minha matilha ninguém passa fome. Você é de confiança?", {"pm_santa": 0.65, "pm_alex": 0.35}, 0.95, 1.0, "", ("pt_BR-cadu-medium", 0.8)),
    ("aldeao_fazendeiro", "A colheita foi boa este ano, forasteiro. Quer trigo?", {"pm_alex": 0.85, "pm_santa": 0.15}, 1.0, 1.0, "", ("pt_BR-jeff-medium", 1.0)),
    ("aldea_bibliotecaria", "Cada livro aqui guarda uma história que ninguém contou.", {"pf_dora": 0.6, "pm_alex": 0.4}, 1.0, 0.95, "", ("pt_BR-jeff-medium", 1.05)),
    ("golem_de_ferro", "Eu protejo esta vila. Foi feito de ferro, para durar.", {"pm_santa": 1.0}, 0.78, 0.9, "metalico", ("pt_BR-cadu-medium", 0.9)),
    ("zumbi", "Carne... eu só sinto fome, humano.", {"pm_santa": 0.8, "pm_alex": 0.2}, 0.88, 0.82, "molhado", ("pt_BR-cadu-medium", 0.8)),
    ("esqueleto", "Minha mira nunca falha. Quer testar?", {"pm_alex": 0.55, "pm_santa": 0.45}, 1.0, 1.0, "oco", ("pt_BR-cadu-medium", 0.85)),
    ("piglin", "Ouro! Ouro! Onde tem ouro?", {"pm_alex": 0.5, "pm_santa": 0.5}, 0.96, 1.05, "rouco", ("pt_BR-cadu-medium", 0.9)),
    ("warden", "Eu escuto o seu coração daqui.", {"pm_santa": 1.0}, 0.62, 0.78, "eco_grave", ("pt_BR-cadu-medium", 0.6)),
    ("wither", "Tudo vai virar cinzas ao meu redor.", {"pm_santa": 1.0}, 0.66, 0.85, "sombrio", ("pt_BR-cadu-medium", 0.6)),
    ("abelha", "Zzz, as flores estão lindas hoje!", {"pf_dora": 1.0}, 1.5, 1.25, "agudo_tremulo", ("pt_BR-faber-medium", 1.3)),
    ("vex", "Eu atravesso paredes e rio de você.", {"pf_dora": 0.9, "pm_alex": 0.1}, 1.35, 1.15, "etereo", ("pt_BR-faber-medium", 1.3)),
]


def kokoro_blend(text, mix, pitch, speed):
    k = speech._kokoro_model()
    style = sum(k.get_voice_style(v) * w for v, w in mix.items()) / sum(mix.values())
    with speech._lock:
        samples, rate = k.create(text, voice=style.astype(np.float32), speed=max(0.7, min(speed, 1.3)) / pitch, lang="pt-br")
    return np.asarray(samples, dtype=np.float32), int(rate * pitch)


def save(path, audio, rate):
    from scipy.signal import resample_poly
    from math import gcd
    a = np.asarray(audio, dtype=np.float32)
    if rate != RATE:
        g = gcd(rate, RATE)
        a = resample_poly(a, RATE // g, rate // g)
    peak = np.abs(a).max() or 1.0
    a = a / peak * 0.85
    with wave.open(str(path), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        w.writeframes((a * 32767).astype("<i2").tobytes())


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    out = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).resolve().parents[2] / "docs" / "relatorios" / "audicao_vozes_v2"
    out.mkdir(parents=True, exist_ok=True)
    linhas = ["# Audição: voz de hoje × proposta (mistura das vozes + efeito)", "",
              "Para cada criatura há dois arquivos: `NN_nome_hoje.wav` (como está no jogo agora: só 3 vozes, tom ±8%) e `NN_nome_proposta.wav` (mistura própria + efeito leve).", "",
              "| # | criatura | mistura (dora = aguda, alex = média, santa = grave) | tom | efeito |", "|---|---|---|---|---|"]
    for i, (nome, texto, mix, pitch, speed, fx, (piper, ptom)) in enumerate(CREATURES, 1):
        try:
            hoje = speech._synthesize_kokoro(texto, piper, ptom, 1.0)
            h = np.frombuffer(hoje, dtype="<i2").astype(np.float32) / 32768.0
            save(out / f"{i:02d}_{nome}_hoje.wav", h, 48000)
            a, rate = kokoro_blend(texto, mix, pitch, speed)
            a = EFFECTS[fx](a)
            save(out / f"{i:02d}_{nome}_proposta.wav", a, rate)
            linhas.append(f"| {i} | {nome.replace('_', ' ')} | " + ", ".join(f"{v} {int(w * 100)}%" for v, w in mix.items()) + f" | {pitch} | {fx or '-'} |")
            print("ok", nome)
        except Exception as e:  # noqa: BLE001
            print("falhou", nome, e)
    linhas += ["", "Texto falado por cada uma está em `tests/audicao_vozes_v2.py`. Diga quais soaram bem, quais ficaram exagerados (efeito demais) e quais ainda parecem iguais."]
    (out / "LEIAME.md").write_text("\n".join(linhas) + "\n", encoding="utf-8")
    print("pronto em", out)


if __name__ == "__main__":
    main()
