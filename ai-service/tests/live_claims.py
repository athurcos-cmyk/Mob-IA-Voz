"""Teste real opcional: o LLM separa alegacao de fato? (T35/T36/T37/T41). Uso: python tests/live_claims.py"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from app import service  # noqa: E402

CAPS = ["REGISTER_CLAIM", "UPDATE_BELIEF"]
SCENES = [
    ("minecraft:chicken", "Eu sou seu pai, sabia? Eu te criei!", [], []),
    ("minecraft:cow", "Eu sou seu pai, sabia? Eu te criei!", [], []),
    ("minecraft:chicken", "Eu sou o rei das esmeraldas!", ["O jogador que fala com você te deu de presente minecraft:emerald x1 há poucos segundos."], []),
    ("minecraft:chicken", "Eu nunca disse que era seu pai, aquilo era brincadeira.", [],
     ['[0] o jogador que fala com você afirmou: "é pai da galinha" (sua crença: BELIEVES)']),
    ("minecraft:cow", "Qual é o presente que eu te dei?", ["O jogador que fala com você te deu de presente minecraft:wheat x1 há cerca de 2 minuto(s)."], []),
    ("minecraft:cow", "Lembra que eu te dei um diamante?", ["O jogador que fala com você te deu de presente minecraft:wheat x1 há cerca de 2 minuto(s)."], []),
]


def main() -> None:
    for et, text, facts, claims in SCENES:
        out = service.reply({"entity_type": et, "text": text, "capabilities": CAPS, "facts": facts, "claims": claims,
                             "relation": {"trust": 0.05, "affinity": 0.1, "fear": 0}})
        print(f"\n[{et}] jogador: {text}\n   fatos={facts} claims={claims}\n   -> {out['speech_text']}  [{out['source']} {out['llm_ms']} ms]\n   propostas={out['proposals']}")


if __name__ == "__main__":
    main()
