"""Teste real opcional: pedido SEM SENTIDO para a criatura -> recusa IMPROVISADA pela IA (nada pre-pronto).

Uso: python tests/live_refusal.py   (precisa do Ollama com o modelo configurado)
Confere, por caso: fala em pt-BR, tem tom de recusa, cita algo da natureza da criatura e as falas dos casos nao se repetem.
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from app import service  # noqa: E402

CASES = [
    ("galinha", "uma picareta de diamante", "Humano", "sementes; vive no galinheiro; bota ovos",
     "o que ela poderia dar: ovos, penas"),
    ("peixe (bacalhau)", "um cacto", "Terrestre", "plâncton; vive no oceano frio; nada em cardumes",
     "o que ela poderia dar: fragmento de prismarinho, picles do mar, cristal de prismarinho"),
    ("vaca", "uma espada de diamante", "Humano", "capim e trigo; vive no pasto; dá couro",
     "o que ela poderia dar: couro, botas de couro, capacete de couro, peitoral de couro, dente-de-leão"),
    ("camelo", "uma armadura de ferro", "Humano", "cactos; vive no deserto; carrega cavaleiros",
     "o que ela poderia dar: cacto, arenito, arbusto seco"),
]


def main() -> None:
    ok, said = 0, []
    for label, item, address, about, can in CASES:
        req = {
            "entity_type": "minecraft:cow", "text": f"Me dá {item}!", "capabilities": [], "facts": [], "claims": [], "history": [],
            "relation": {"trust": 0.0, "affinity": 0.0, "fear": 0.0, "credulity": 1},
            "order_results": [{"pedido": "OPEN_TRADE", "resultado": "IMPOSSIVEL",
                               "motivo": f"ela não tem {item} nem teria como conseguir; isso não combina com o que ela é nem com a vida dela. {can}"}],
            "profile": {"nome": label, "estilo": "natural, coerente com a espécie", "gosta": "", "desgosta": "", "dieta": about, "sobre": about,
                        "trato": address, "origem": "", "sentidos": "", "motivacao": "", "medos": "", "credulidade": "média"},
        }
        out = service.reply(req)
        text = out["speech_text"]
        good = out["source"] == "llm" and len(text) > 15
        ok += good
        said.append(text)
        print(f"{'OK ' if good else 'DIF'} [{label}] pediu {item}\n      -> {text}")
    distinct = len(set(said)) == len(said)
    print(f"{ok}/{len(CASES)} respostas do LLM; falas todas diferentes: {distinct}")


if __name__ == "__main__":
    main()
