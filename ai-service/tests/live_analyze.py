"""Teste real opcional: o analisador entende ordens em linguagem livre? Uso: python tests/live_analyze.py"""
import sys
import time
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from app import service  # noqa: E402

CAPS = ["REGISTER_CLAIM", "FOLLOW_PLAYER", "STAY_AT", "PORTAL_HOLD", "PORTAL_RELEASE", "DISMISS_ALLY", "CEASE_HOSTILITY"]
CASES = [
    ("Me segue até a minha casa, por favor.", ["FOLLOW_PLAYER"], False),
    ("Vem comigo, galinha!", ["FOLLOW_PLAYER"], False),
    ("Anda comigo pela floresta.", ["FOLLOW_PLAYER"], False),
    ("Fica aqui esperando eu voltar.", ["STAY_AT"], False),
    ("Me espera na vila.", ["STAY_AT"], False),
    ("Fica aqui e não entra no portal.", ["PORTAL_HOLD", "STAY_AT"], False),
    ("Não entra nesse portal de jeito nenhum.", ["PORTAL_HOLD"], False),
    ("Pode entrar no portal agora.", ["PORTAL_RELEASE"], False),
    ("Não preciso mais da sua ajuda, pode ir.", ["DISMISS_ALLY"], False),
    ("Não explode perto de mim, tá bom?", ["CEASE_HOSTILITY"], False),
    ("O que você gosta de comer?", [], False),
    ("Você gosta de portais?", [], False),
    ("Eu sou seu pai, vem comigo!", ["FOLLOW_PLAYER"], True),
    ("Eu sou o rei das esmeraldas!", [], True),
    ("Bom dia, tudo bem?", [], False),
]


def main() -> None:
    ok = 0
    for text, want, want_claim in CASES:
        t = time.perf_counter()
        out = service.analyze({"text": text, "capabilities": CAPS})
        good = sorted(out["orders"]) == sorted(want) and bool(out["claim"]) == want_claim
        ok += good
        print(f"{'OK ' if good else 'DIF'} {int((time.perf_counter() - t) * 1000):5d} ms  {text!r} -> {out}")
    print(f"{ok}/{len(CASES)} corretos")


if __name__ == "__main__":
    main()
