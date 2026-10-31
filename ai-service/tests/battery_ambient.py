"""Bateria de COERENCIA das conversas ambiente entre criaturas, com juiz de IA (API). As cenas vem do MESMO motor do jogo:
rode antes `gradlew test --offline` (AmbientCoherenceTest grava mod-fabric/build/ambient-samples.json).

Uso:  python tests/battery_ambient.py [relatorio.md]

Para cada tipo de par (vaca+vaca, aldeao+golem, zumbi+zumbi...) o juiz diz se a troca faz sentido para ESSAS criaturas naquela situacao.
Como na bateria de conversa, a nota do juiz serve para achar os piores casos, nao como prova de qualidade: leia o relatorio.
"""
import concurrent.futures as cf
import json
import statistics
import sys
import time
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from app import config, llm  # noqa: E402

SAMPLES = Path(__file__).resolve().parents[2] / "mod-fabric" / "build" / "ambient-samples.json"
JUDGE_MODEL = "deepseek-v4-pro"
SCHEMA = {"type": "object", "required": ["nota", "problemas"],
          "properties": {"nota": {"type": "integer"}, "problemas": {"type": "array", "items": {"type": "string"}}}}
SYSTEM = (
    "Você avalia pequenas conversas AMBIENTE de um jogo de Minecraft: duas criaturas conversam entre si perto do jogador e o jogador as ouve. "
    "Recebe: 'par' (quem conversa e a situação) e 'falas' (0 = quem abre, 1 = quem responde). "
    "Dê 'nota' de 1 a 5 para: faz sentido para ESSAS criaturas e ESSA situação? Critérios: (a) a fala combina com o mundo de quem a diz "
    "(uma vaca não fala de trocas, esmeraldas ou casas; um aldeão não muge; um zumbi não é simpático); (b) a resposta responde ao que foi dito; "
    "(c) bate com a situação do par (hora, clima, o que o jogador segura ou veste, quem conhece quem); (d) soa natural em português falado. "
    "5 = faz total sentido; 3 = estranho mas passável; 1 = sem sentido ou fora do mundo da criatura. "
    "Seja EXIGENTE e específico. Liste em 'problemas' cada problema real (frase curta, citando a fala); lista vazia se não há. "
    "Responda SOMENTE JSON: {\"nota\": 1-5, \"problemas\": [\"...\"]}."
)


def judge(item):
    payload = {"par": item["par"], "falas": item["falas"]}
    msgs = [{"role": "system", "content": SYSTEM}, {"role": "user", "content": json.dumps(payload, ensure_ascii=False)}]
    for model in (JUDGE_MODEL, config.API_MODEL):
        try:
            d = json.loads(llm.chat_api(msgs, model, 60, SCHEMA, {"temperature": 0, "num_predict": 300}))
            return {"nota": max(1, min(5, int(d.get("nota", 1)))), "problemas": [str(p)[:200] for p in d.get("problemas", [])][:4]}
        except (llm.LlmError, ValueError, TypeError):
            continue
    return {"nota": 0, "problemas": ["avaliador falhou"]}


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    out_path = Path(sys.argv[1]) if len(sys.argv) > 1 and not sys.argv[1].startswith("--") else None
    items = json.loads(SAMPLES.read_text(encoding="utf-8"))
    t0 = time.time()
    with cf.ThreadPoolExecutor(max_workers=6) as ex:
        results = list(ex.map(judge, items))
    for it, r in zip(items, results):
        it["juiz"] = r
    pares = {}
    for it in items:
        pares.setdefault(it["par"], []).append(it)
    lines = ["# Bateria de coerência das conversas ambiente (juiz de IA)", "",
             f"Rodada em {time.strftime('%Y-%m-%d %H:%M')}; {len(items)} cenas em {len(pares)} tipos de par; juiz `{JUDGE_MODEL}`; {time.time() - t0:.0f} s.", "",
             "| Par | cenas | nota média | cenas com nota ≤ 3 |", "|---|---|---|---|"]
    allnotes = []
    for par, its in pares.items():
        notas = [i["juiz"]["nota"] for i in its if i["juiz"]["nota"] > 0]
        allnotes += notas
        low = sum(1 for n in notas if n <= 3)
        lines.append(f"| {par} | {len(its)} | {statistics.mean(notas):.2f} | {low} |")
    lines += ["", f"**Geral: {statistics.mean(allnotes):.2f}** em {len(allnotes)} cenas; nota ≤ 3: {sum(1 for n in allnotes if n <= 3)}.", "",
              "## Cenas com nota ≤ 3 (ou com problemas apontados)", ""]
    for it in items:
        r = it["juiz"]
        if r["nota"] <= 3 or r["problemas"]:
            lines.append(f"- **{it['par']}** (`{it['familia']}`) nota {r['nota']}")
            lines += [f"  - {f}" for f in it["falas"]]
            lines += [f"  - ⚠ {p}" for p in r["problemas"]]
    lines += ["", "## Todas as cenas", ""]
    for par, its in pares.items():
        lines.append(f"### {par}")
        for it in its:
            lines.append(f"- ({it['juiz']['nota']}) " + " / ".join(it["falas"]))
        lines.append("")
    text = "\n".join(lines)
    if out_path:
        out_path.write_text(text, encoding="utf-8")
    print(text[:2500])


if __name__ == "__main__":
    main()
