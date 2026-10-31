"""Teste real opcional: a IA julga o que a criatura aceitaria como pagamento e o que teria para dar (sem listas fixas).

Uso: python tests/live_judge.py   (precisa do Ollama). Mostra o veredito e se bate com a expectativa de bom senso.
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from app import service  # noqa: E402

P = {
    "golem": {"nome": "golem de ferro", "estilo": "grave, protetor e simples", "dieta": "não come", "sobre": "vive em aldeias; protege os aldeões; conserta-se com ferro; dá papoulas"},
    "enderman": {"nome": "enderman", "estilo": "enigmático e melancólico", "dieta": "não come; coleciona blocos", "sobre": "vive no Fim; pega e carrega blocos; teme água"},
    "aranha": {"nome": "aranha", "estilo": "silenciosa e paciente", "dieta": "carne crua e podre; insetos", "sobre": "vive em cavernas; tece teias"},
    "esqueleto": {"nome": "esqueleto", "estilo": "seco e irônico", "dieta": "não come", "sobre": "atira flechas; vive em cavernas; é feito de ossos"},
    "galinha": {"nome": "galinha", "estilo": "ingênua e agitada", "dieta": "sementes", "sobre": "vive no galinheiro e nos campos; bota ovos"},
}
# (criatura, aceitar, dar, esperado: {item: conjunto de vereditos aceitaveis}, give aceitavel)
# So itens FORA do catalogo/gosto fixo chegam ao julgamento (os do catalogo ja sao decididos pelo servidor).
CASES = [
    ("golem", ["ferro bruto", "picareta de pedra", "bife assado"], "picareta de ferro",
     {"ferro bruto": {"YES", "MAYBE"}, "picareta de pedra": {"YES", "MAYBE"}, "bife assado": {"NO", "MAYBE"}}, {"YES", "MAYBE"}),
    ("enderman", ["diamante", "esmeralda", "terra", "tronco de carvalho"], "",
     {"diamante": {"NO", "MAYBE"}, "esmeralda": {"NO", "MAYBE"}, "terra": {"YES", "MAYBE"}, "tronco de carvalho": {"YES", "MAYBE"}}, None),
    ("aranha", ["bife assado", "diamante", "picareta de ferro"], "",
     {"bife assado": {"NO", "MAYBE"}, "diamante": {"NO"}, "picareta de ferro": {"NO"}}, None),
    ("esqueleto", [], "espada de ferro", {}, {"YES", "MAYBE"}),
    ("galinha", [], "picareta de ferro", {}, {"NO", "MAYBE"}),
    ("galinha", [], "espada de diamante", {}, {"NO", "MAYBE"}),
]


def main() -> None:
    ok = total = 0
    for who, accept, give, expect, give_ok in CASES:
        out = service.judge({"profile": P[who], "accept": accept, "give": give})
        line = []
        for item, allowed in expect.items():
            total += 1
            v = out.get("accept", {}).get(item)
            ok += v in allowed
            line.append(f"{item}={v}{'' if v in allowed else ' (!)'}")
        if give_ok is not None:
            total += 1
            g = out.get("give")
            ok += g in give_ok
            line.append(f"dar {give}={g}{'' if g in give_ok else ' (!)'}")
        print(f"[{who}] {out.get('accept')} dar={out.get('give')} -> {', '.join(line)}")
    print(f"{ok}/{total} vereditos dentro do esperado")


if __name__ == "__main__":
    main()
