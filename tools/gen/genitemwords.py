# -*- coding: utf-8 -*-
"""Gera ai-service/app/item_words.json: as palavras de ITEM do catalogo (values.json), sem acento e em minusculas.
O atalho de papo simples (ai-service/app/fastpath.py) usa a lista: fala que cita um item sempre passa pela analise da IA.
Rode: python tools/gen/genitemwords.py"""
import json
import re
import unicodedata
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
VALUES = ROOT / "mod-fabric" / "src" / "main" / "resources" / "mobvoz" / "values.json"
OUT = ROOT / "ai-service" / "app" / "item_words.json"

STOP = {"de", "do", "da", "dos", "das", "com", "para", "uma", "uns", "umas", "bem", "mal", "sem", "nos"}
# nomes de criaturas e cores aparecem em nomes de item ("ovo de aranha", "tinta azul"), mas falar DELAS e papo, nao pedido
NOT_ITEMS = {"vaca", "porco", "galinha", "aranha", "esqueleto", "sapo", "leao", "coelho", "cabra", "tartaruga", "slime", "blaze", "wither", "whiter",
             "phantom", "ender", "bovina", "azul", "azuis", "azulzinha", "azulzinhas", "branca", "brancas", "marrom", "marrons", "vermelha", "vermelhas",
             "vermelho", "vermelhos", "lisa", "lisas", "regia", "regias", "marinha", "marinhas", "silvestre", "silvestres", "fim", "inferno", "mar", "luz", "luzes",
             "vitoria", "vitorias", "guia", "guias", "linha", "linhas", "pena", "vale", "podre", "podres", "seco", "secos", "cru", "bruto", "brutos"}
EXTRA = ["item", "itens", "objeto", "objetos", "ferramenta", "ferramentas", "arma", "armas", "bloco", "blocos", "moeda", "moedas"]


def norm(s: str) -> str:
    return "".join(c for c in unicodedata.normalize("NFD", s.lower()) if unicodedata.category(c) != "Mn")


def main() -> None:
    items = json.loads(VALUES.read_text(encoding="utf-8"))["items"]
    words = set(EXTRA)
    for entry in items.values():
        for name in entry.get("pt", []):
            for w in re.findall(r"[a-z]+", norm(name)):
                if len(w) >= 3 and w not in STOP and w not in NOT_ITEMS:
                    words.add(w)
    OUT.write_text(json.dumps(sorted(words), ensure_ascii=False, indent=0), encoding="utf-8")
    print(len(words), "palavras ->", OUT)


if __name__ == "__main__":
    main()
