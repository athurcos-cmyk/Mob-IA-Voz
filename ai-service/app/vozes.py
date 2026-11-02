"""Vozes das criaturas: cada ESPECIE tem uma mistura propria das 3 vozes pt-BR do Kokoro (aguda, media, grave), e cada INDIVIDUO da especie uma variacao
pequena e estavel (misturas, tom e velocidade levemente diferentes), entao duas vacas ou dois aldeoes nao soam iguais. A EMOCAO da fala muda tom e velocidade um pouco.

Tudo e deterministico: a mesma criatura sempre tem a mesma voz. As misturas abaixo sao as que o dono aprovou de ouvido (docs/relatorios/audicao_vozes*):
sem efeito de estudio, so mistura de vozes; o unico efeito e o do Vex (etereo), que ele gostou."""
import random
import zlib
from dataclasses import dataclass

DORA, ALEX, SANTA = "pf_dora", "pm_alex", "pm_santa"
BASES = (DORA, ALEX, SANTA)
BUCKETS = 64  # variantes por especie; duas criaturas da mesma especie so soam iguais com chance de 1/64

# especie (sem 'minecraft:'; aldeao 'villager#profissao') -> ({voz: peso}, tom, velocidade, efeito)
TABLE: dict[str, tuple[dict, float, float, str]] = {
    "cow": ({DORA: 1.0}, 0.84, 0.88, ""), "mooshroom": ({DORA: 1.0}, 0.86, 0.9, ""),
    "pig": ({ALEX: 1.0}, 1.12, 1.10, ""), "chicken": ({DORA: 0.5, ALEX: 0.5}, 1.12, 1.15, ""),
    "sheep": ({DORA: 0.8, SANTA: 0.2}, 1.06, 0.92, ""), "horse": ({ALEX: 0.5, SANTA: 0.5}, 0.97, 1.0, ""), "donkey": ({ALEX: 0.4, SANTA: 0.6}, 0.98, 0.92, ""),
    "mule": ({ALEX: 0.3, SANTA: 0.7}, 0.96, 0.92, ""), "wolf": ({SANTA: 0.65, ALEX: 0.35}, 0.95, 1.0, ""), "cat": ({DORA: 0.6, ALEX: 0.4}, 1.10, 1.05, ""),
    "fox": ({DORA: 0.45, ALEX: 0.55}, 1.18, 1.12, ""), "bee": ({DORA: 0.8, ALEX: 0.2}, 1.32, 1.2, ""), "vex": ({DORA: 0.9, ALEX: 0.1}, 1.35, 1.15, "etereo"),
    "iron_golem": ({SANTA: 1.0}, 0.74, 0.88, ""), "zombie": ({SANTA: 0.85, ALEX: 0.15}, 0.84, 0.80, ""), "husk": ({SANTA: 0.9, ALEX: 0.1}, 0.82, 0.78, ""),
    "skeleton": ({ALEX: 0.6, SANTA: 0.4}, 0.98, 1.0, ""), "stray": ({ALEX: 0.7, SANTA: 0.3}, 1.0, 0.95, ""), "piglin": ({SANTA: 0.55, ALEX: 0.45}, 0.88, 1.12, ""),
    "warden": ({SANTA: 1.0}, 0.60, 0.78, ""), "wither": ({SANTA: 1.0}, 0.66, 0.85, ""), "spider": ({DORA: 0.55, SANTA: 0.45}, 0.98, 0.95, ""),
    "cave_spider": ({DORA: 0.7, SANTA: 0.3}, 1.05, 1.0, ""), "creeper": ({ALEX: 0.7, DORA: 0.3}, 1.0, 0.92, ""), "enderman": ({SANTA: 0.7, DORA: 0.3}, 0.78, 0.85, ""),
    "witch": ({DORA: 0.85, SANTA: 0.15}, 0.90, 1.0, ""), "villager#farmer": ({ALEX: 0.85, SANTA: 0.15}, 1.0, 1.0, ""),
    "villager#librarian": ({DORA: 0.6, ALEX: 0.4}, 1.0, 0.95, ""),
}

# emocao da fala (campo `emotion` da resposta da IA) -> (fator de tom, fator de velocidade)
EMOTION = {
    "neutro": (1.0, 1.0), "curioso": (1.04, 1.02), "alegre": (1.06, 1.08), "cauteloso": (0.97, 0.92), "irritado": (1.0, 1.10), "triste": (0.93, 0.88),
    "desconfiado": (0.97, 0.95), "hostil": (0.94, 1.05), "calculista": (0.98, 0.95), "medroso": (1.08, 1.12),
}


@dataclass(frozen=True)
class Plan:
    mix: tuple  # ((voz, peso), ...) pesos somam 1
    pitch: float
    speed: float
    fx: str


def _rng(*parts) -> random.Random:
    return random.Random(zlib.crc32("|".join(str(p) for p in parts).encode("utf-8")))


def _normalize(w: dict) -> dict:
    total = sum(w.values()) or 1.0
    return {k: v / total for k, v in w.items()}


def _derived(species: str, profile_pitch: float, piper_voice: str) -> tuple[dict, float, float, str]:
    """Especie sem mistura propria: parte do tom do perfil (grave = santa, agudo = dora) e desloca um pouco, de um jeito fixo por especie, para nao soar igual as vizinhas."""
    t = max(0.0, min(1.0, (profile_pitch - 0.6) / 0.8))  # 0 = grave .. 1 = agudo
    w = {SANTA: max(0.0, 1 - 2 * t), DORA: max(0.0, 2 * t - 1)}
    w[ALEX] = max(0.0, 1 - w[SANTA] - w[DORA])
    r = _rng("especie", species)
    w = _normalize({k: v + r.uniform(0, 0.22) for k, v in w.items()})
    return w, max(0.7, min(profile_pitch, 1.3)) * r.uniform(0.96, 1.04), 1.0, ""


def plan(species: str, seed: int, emotion: str = "", profile_pitch: float = 1.0, piper_voice: str = "") -> Plan:
    """Voz desta criatura: mistura da especie + variacao estavel do individuo (seed) + leve ajuste pela emocao da fala."""
    key = species.replace("minecraft:", "")
    base = TABLE.get(key) or TABLE.get(key.split("#")[0]) or _derived(key, profile_pitch, piper_voice)
    mix, pitch, speed, fx = base
    r = _rng("individuo", key, int(seed) % BUCKETS)
    w = _normalize({v: mix.get(v, 0.0) + r.uniform(0, 0.18) for v in BASES})  # cada indivíduo tem um pouco de cada voz: timbre proprio
    pitch *= r.uniform(0.93, 1.07)
    speed *= r.uniform(0.95, 1.05)
    pf, sf = EMOTION.get(emotion, (1.0, 1.0))
    return Plan(tuple(sorted(w.items())), max(0.5, min(pitch * pf, 1.6)), max(0.7, min(speed * sf, 1.3)), fx)
