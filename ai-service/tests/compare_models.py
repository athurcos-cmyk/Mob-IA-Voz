"""Compara o modelo local (Ollama) com a IA propria (API) nas MESMAS conversas, com os mesmos dados que o jogo manda.

Uso: MOBVOZ_API_KEY=... python tests/compare_models.py [saida.md]
Para cada fala mede o tempo da analise e da resposta e guarda o texto, para leitura humana. Nao decide qualidade sozinho.
"""
import json
import statistics
import sys
import time
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from app import llm, service  # noqa: E402

RES = Path(__file__).resolve().parents[2] / "mod-fabric" / "src" / "main" / "resources" / "mobvoz"
LORE = json.loads((RES / "lore.json").read_text(encoding="utf-8"))["lore"]
PROF = {p["id"]: p for p in json.loads((RES / "profiles.json").read_text(encoding="utf-8"))["profiles"]}
CRED = {0: "baixa: desconfia de histórias e exageros", 1: "média: acredita no razoável, desconfia de exageros", 2: "alta: tende a acreditar em quem é gentil"}
CAPS = ["REGISTER_CLAIM", "OPEN_TRADE", "FOLLOW_PLAYER", "STAY_AT", "SOCIAL", "DEFEND_PLAYER"]


def profile(tid: str) -> dict:
    lo = LORE[tid.split("#")[0]]
    p = PROF.get(tid, {"label": tid.split(":")[1].replace("_", " "), "style": "natural e curioso", "likes": [], "dislikes": [], "credulity": 1,
                       "diet": "o que combina com a espécie", "about": "", "address": "Humano"})
    return {"nome": p["label"], "estilo": p["style"], "gosta": "", "desgosta": "", "credulidade": CRED[p["credulity"]], "dieta": p["diet"],
            "sobre": p["about"], "trato": p.get("address", "Humano"), "origem": lo["origem"], "sentidos": lo["sentidos"],
            "motivacao": lo["motivacao"], "medos": lo["medos"]}


def cited(tid: str, nome: str) -> str:
    lo = LORE[tid]
    return (f"O jogador citou {nome}. O que você sabe dela(e): {lo['origem']} {lo['motivacao']} Teme: {lo['medos']} "
            "Dê a SUA opinião pessoal sobre ela, pela sua natureza.")


def result(pedido, res, motivo):
    return {"pedido": pedido, "resultado": res, "motivo": motivo}


# (criatura, rotulo, [ (fala_do_jogador | None se evento, fatos_extras, order_results) ])
STAR = ("por 1 estrela do nether ela quer, jogado no chão perto dela, o equivalente a: 4 blocos de ouro, ou 9 diamantes, ou 6 fragmentos de netherite "
        "(pode combinar itens; ela costuma aceitar: fragmentos de netherite, esmeraldas, diamantes, blocos de ouro; qualquer outra coisa ela avalia na hora). "
        "Limite: no máximo 1 unidades por negócio")
SCENES = [
    ("minecraft:pig", "Porco (conversa com memória)", [
        ("Oi porco, tudo bem?", [], []),
        ("O que você acha das vacas?", [cited("minecraft:cow", "vaca")], []),
        ("E das galinhas?", [cited("minecraft:chicken", "galinha")], []),
        ("Você tem medo de alguma coisa?", [], []),
        ("E por que você tem medo disso?", [], []),
    ]),
    ("minecraft:cow", "Vaca", [
        ("O que você gosta de fazer o dia todo?", [], []),
        ("O que você acha dos esqueletos?", [cited("minecraft:skeleton", "esqueleto")], []),
        ("Já teve medo de alguém?", [], []),
    ]),
    ("minecraft:wither", "Wither (chefe)", [
        ("Ei Wither, você pode me dar uma estrela do Nether?", [], [result("OPEN_TRADE", "IMPOSSIVEL",
            "ela não tem estrela do nether nem teria como conseguir; isso não combina com o que ela é nem com a vida dela. ")]),
        ("Então me ajuda a derrotar o Dragão do Fim?", [], [result("DEFEND_PLAYER", "RECUSADO", "contratos com chefes estao desativados neste mundo")]),
        ("Por que você é tão furioso?", [], []),
    ]),
    ("minecraft:wither_skeleton", "Esqueleto Wither (estrela, com oferta aberta)", [
        ("Esqueleto Wither, você tem uma estrela do Nether para me dar?", [], [result("OPEN_TRADE", "ACEITO", STAR)]),
        ("Quanto custa?", [], [result("OFERTA_ABERTA", "JA_ATIVO", "a troca combinada continua de pé: " + STAR)]),
    ]),
    ("minecraft:chicken", "Galinha (pedido absurdo)", [
        ("Me dá uma picareta de diamante!", [], [result("OPEN_TRADE", "IMPOSSIVEL",
            "ela não tem picareta de diamante nem teria como conseguir; isso não combina com o que ela é nem com a vida dela. O que ela poderia dar: ovos, penas")]),
    ]),
    ("minecraft:cod", "Peixe (pedido absurdo)", [
        ("Você tem um cacto pra mim?", [], [result("OPEN_TRADE", "IMPOSSIVEL",
            "ela não tem cacto nem teria como conseguir; isso não combina com o que ela é nem com a vida dela. O que ela poderia dar: fragmento de prismarinho, picles do mar")]),
        ("Você já saiu da água alguma vez?", [], []),
    ]),
    ("minecraft:pig", "Porco ameaçado", [
        ("Vou te transformar em comida!", [], [result("REACAO", "FOGE", LORE["minecraft:pig"]["reacao"])]),
    ]),
    ("minecraft:iron_golem", "Golem de ferro", [
        ("Qual é o seu dever?", [], []),
        ("O que você acha dos zumbis?", [cited("minecraft:zombie", "zumbi")], []),
    ]),
    ("minecraft:villager#farmer", "Aldeão (eventos da troca)", [
        (None, ["EVENTO AGORA (aconteceu agora; o jogador não falou nada; use exatamente os números e itens citados): o jogador largou itens que pagam só 35% do que combinaram; para fechar falta o equivalente a 2 fenos; sem isso o negócio não está fechado e você não pegou nada"], []),
        (None, ["EVENTO AGORA (aconteceu agora; o jogador não falou nada; use exatamente os números e itens citados): você só pegou 3 fenos do chão (era o que bastava) e devolveu o resto ao jogador, que deu mais que o combinado. você acabou de entregar ao jogador 1 esmeralda, como combinado"], []),
    ]),
    ("minecraft:chicken", "Galinha (evento do ovo)", [
        (None, ["EVENTO AGORA (aconteceu agora; o jogador não falou nada; use exatamente os números e itens citados): o jogador acabou de pegar o ovo que você tinha acabado de botar"], []),
    ]),
]


def run(name, chat):
    rows, out = [], []
    for tid, label, lines in SCENES:
        prof = profile(tid)
        history = []
        out.append(f"\n### {label}")
        for text, extra, results in lines:
            t0 = time.perf_counter()
            an = service.analyze({"text": text, "capabilities": CAPS}, chat=chat) if text else None
            t_an = (time.perf_counter() - t0) * 1000
            t1 = time.perf_counter()
            rp = service.reply({"entity_type": tid.split("#")[0], "text": text or "", "capabilities": [], "facts": extra, "history": history[-6:],
                                "profile": prof, "order_results": results, "detected_claim": (an or {}).get("claim", ""),
                                "relation": {"trust": 0.0, "affinity": 0.0, "fear": 0.0, "credulity": 1}}, chat=chat)
            t_rp = (time.perf_counter() - t1) * 1000
            history.append({"jogador": text or "(aconteceu algo)", "voce": rp["speech_text"]})
            rows.append((t_an, t_rp))
            out.append(f"- **Jogador:** {text or '(evento)'}\n  **{name}:** {rp['speech_text']}  _(análise {t_an:.0f} ms, resposta {t_rp:.0f} ms, origem {rp['source']})_")
    an_ms = [r[0] for r in rows if r[0] > 1]
    out.append(f"\n**Mediana {name}:** análise {statistics.median(an_ms):.0f} ms | resposta {statistics.median(r[1] for r in rows):.0f} ms")
    return out


def main() -> None:
    which = sys.argv[2:] or ["local", "api"]
    report = []
    for name in which:
        chat = llm.chat_api if name == "api" else llm.chat_ollama
        service.analyze({"text": "oi", "capabilities": CAPS}, chat=chat)  # aquecimento (nao conta)
        report.append(f"\n## {'IA própria (API)' if name == 'api' else 'Modelo local (Ollama qwen3:4b)'}")
        report += run(name, chat)
    text = "\n".join(report)
    print(text)
    if len(sys.argv) > 1:
        Path(sys.argv[1]).write_text(text, encoding="utf-8")


if __name__ == "__main__":
    main()
