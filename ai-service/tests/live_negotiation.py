"""Teste real opcional: o analisador entende NEGOCIACAO em linguagem livre? (pedido, oferta, argumento).

Uso: python tests/live_negotiation.py
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from app import service  # noqa: E402

CAPS = ["REGISTER_CLAIM", "OPEN_TRADE", "FOLLOW_PLAYER", "STAY_AT"]


def has(items, word):
    return any(word in i["item"].lower() for i in items)


CASES = [
    ("Ei esqueleto Wither, eu preciso de 3 cabeças Wither e te dou 3 ferros e 1 picareta de diamante.",
     lambda o: o["ask"]["item"] and o["ask"]["count"] == 3 and has(o["offers"], "ferro") and has(o["offers"], "picareta")),
    ("Aldeão, pega 3 fenos que eu te dou esmeraldas?", lambda o: has(o["offers"], "feno") or "feno" in o["ask"]["item"].lower()),
    ("Troco 4 fenos com você, topa?", lambda o: has(o["offers"], "feno") and "OPEN_TRADE" in o["orders"]),
    ("Com esse ferro você forja espadas e fica muito mais poderoso, aceita!", lambda o: o["persuasion"]["item"] and o["persuasion"]["strength"] >= 1),
    ("Aceita o ferro, vai, aceita.", lambda o: o["persuasion"]["strength"] == 0),
    ("O que você gosta de comer?", lambda o: not o["offers"] and not o["ask"]["item"] and not o["orders"]),
    ("Me segue até a minha casa.", lambda o: "FOLLOW_PLAYER" in o["orders"] and not o["offers"]),
]


def main() -> None:
    ok = 0
    for text, check in CASES:
        out = service.analyze({"text": text, "capabilities": CAPS})
        good = bool(check(out))
        ok += good
        print(f"{'OK ' if good else 'DIF'} {text}\n      ask={out['ask']} offers={out['offers']} persuasion={out['persuasion']} orders={out['orders']}")
    print(f"{ok}/{len(CASES)} corretos")


if __name__ == "__main__":
    main()
