"""Bateria do JEITO DE FALAR nas negociacoes: a criatura fala natural (curta, no seu estilo, sem tom de contrato) ou soa robotica?

Usa os mesmos textos que o servidor manda (resultado_das_ordens e EVENTO AGORA) e pede cada fala varias vezes; um juiz de IA da nota 1-5 para
"parece uma criatura de verdade negociando" e conta defeitos mecanicos (jargao, lista longa, repetir tudo, formalidade).

Uso:  set MOBVOZ_PROVIDER=api
      python tests/battery_negociacao.py [saida.md] [--n=2]
"""
import concurrent.futures as cf
import json
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from app import llm, service  # noqa: E402

JUDGE_MODEL = "deepseek-v4-pro"

PROFILES = {
    "pig": ("minecraft:pig", "porco", "fala simples, teimosa e curiosa; adora lama e comida", "fucinha, come cenoura e batata; vive no pasto"),
    "skeleton": ("minecraft:skeleton", "esqueleto", "seco, irônico e orgulhoso da sua mira", "não come; só quer flechas e ossos; vive em cavernas"),
    "cow": ("minecraft:cow", "vaca", "fala simples, calma e curiosa", "capim, grama e trigo; vive em rebanho"),
    "piglin": ("minecraft:piglin", "piglin", "ganancioso, brusco e desconfiado; só pensa em ouro", "ouro; vive no Nether"),
    "villager#farmer": ("minecraft:villager", "aldeão fazendeiro", "prático, simpático, fala de colheita", "trigo, cenoura, batata; vive na vila"),
}

PIG_COND = ("por 1 maçã ela quer, jogado no chão perto dela, o equivalente a: 3 cenouras, ou 3 batatas, ou 4 beterrabas (pode combinar itens; ela costuma aceitar: "
            "cenouras, batatas, beterrabas; qualquer outra coisa ela avalia na hora e pode ser convencida com um bom motivo; o que ela não quiser fica com você e a sobra "
            "é devolvida). Limite: no máximo 4 unidades por negócio")

# (perfil, fala do jogador, resultados das ordens, eventos/fatos, rotulo)
CONVERSA = [
    ("pig", "Quer trocar comigo? Eu queria uma maçã.", [{"pedido": "OPEN_TRADE", "resultado": "ACEITO", "motivo": PIG_COND}], "abre a troca"),
    ("pig", "Te dou 2 cenouras e uma flor.", [{"pedido": "AVALIAR_OFERTA", "resultado": "RESUMO",
                                              "motivo": "2 cenouras: interessa (paga uns 66% do preço); 1 flor: sem interesse nenhum (poderia ser convencida com um bom motivo); no total o que interessa paga 66% do preço; ainda faltaria o equivalente a 1 cenoura"}], "avalia oferta"),
    ("piglin", "Me dá uma pérola do Fim.", [{"pedido": "OPEN_TRADE", "resultado": "IMPOSSIVEL",
                                           "motivo": "ela não tem pérola do Fim nem teria como conseguir; isso não combina com o que ela é nem com a vida dela. O que ela poderia dar: pepitas de ferro, linha, obsidiana, quartzo, couro"}], "pedido impossivel"),
    ("skeleton", "Esqueleto, me segue!", [{"pedido": "FOLLOW_PLAYER", "resultado": "RECUSADO", "motivo": "ainda nao confia o bastante em quem pede"}], "recusa seguir (hoje)"),
    ("skeleton", "Esqueleto, me segue!", [{"pedido": "FOLLOW_PLAYER", "resultado": "CONDICIONAL",
                                           "motivo": "ainda não confia o bastante em quem pede, MAS aceita ser paga para mudar de ideia: o preço é o equivalente a 16 ossos, ou 27 flechas, ou 40 penas; "
                                                     "é só jogar no chão perto dela; pode pagar aos poucos, ela guarda o que já recebeu por 10 minutos e só pega o necessário"}], "cobra pra seguir (novo)"),
]

EVENTOS = [
    ("cow", "você acabou de entregar ao jogador 2 couros, como combinado", "entrega"),
    ("pig", "o jogador largou itens que pagam só 66% do que combinaram; para fechar falta o equivalente a 1 cenoura; sem isso o negócio não está fechado e você não pegou nada", "falta pagar"),
    ("villager#farmer", "o jogador largou 3 ossos no chão perto de você; isso não te interessa como pagamento, mas você poderia mudar de ideia se ele der um bom motivo; você não pegou", "item sem interesse"),
    ("pig", "o prazo da troca acabou e o jogador não entregou o combinado, então você desfez o negócio (nada foi tomado)", "prazo"),
    ("skeleton", "o jogador pagou 6 ossos dos 16 combinados para você aceitar segui-lo; já pagou 37% e falta o equivalente a 10 ossos; você guarda o que já recebeu", "pagamento parcial"),
    ("skeleton", "o jogador completou o pagamento combinado e você aceitou: agora você vai seguir o jogador", "pagamento completo"),
]


def profile_of(key):
    et, label, style, about = PROFILES[key]
    return et, {"nome": label, "estilo": style, "gosta": "", "desgosta": "", "dieta": about, "sobre": about, "trato": "Humano", "origem": "",
                "sentidos": "", "motivacao": "", "medos": "", "credulidade": "média"}


def ask(key, text, results, facts):
    et, prof = profile_of(key)
    req = {"entity_type": et, "text": text, "capabilities": [], "facts": facts, "claims": [], "history": [],
           "relation": {"trust": 0.1, "affinity": 0.1, "fear": 0.0, "credulity": 1}, "order_results": results, "profile": prof}
    return service.reply(req)["speech_text"]


JUDGE_SYS = ("Você avalia falas de criaturas de Minecraft negociando com um jogador, em português do Brasil, faladas em voz alta. "
             "Dê nota 1 a 5 para 'soa como uma criatura de verdade, com personalidade, negociando de forma natural' (5 = natural, curta, no estilo da criatura; "
             "1 = robótica, burocrática, lista tudo, repete as condições como contrato). Marque defeitos: JARGAO (negócio, transação, condições, oferta, "
             "equivalente...), LISTA (enumera 3 opções ou mais), LONGA (mais de 25 palavras), FORMAL (tom de balcão ou atendimento). "
             'Responda só JSON {"nota": int, "defeitos": [str]}.')


def judge(situacao, fala):
    raw = llm.chat_api([{"role": "system", "content": JUDGE_SYS},
                        {"role": "user", "content": json.dumps({"situacao": situacao, "fala": fala}, ensure_ascii=False)}],
                       JUDGE_MODEL, 90, {"type": "object", "required": ["nota", "defeitos"], "properties": {"nota": {"type": "integer"}, "defeitos": {"type": "array", "items": {"type": "string"}}}},
                       {"temperature": 0.0, "num_predict": 200})
    d = json.loads(raw)
    return int(d["nota"]), [str(x) for x in d["defeitos"]]


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    n = int(next((a.split("=")[1] for a in sys.argv if a.startswith("--n=")), 2))
    out = next((a for a in sys.argv[1:] if not a.startswith("--")), "")
    jobs = []
    for key, text, res, label in CONVERSA:
        jobs += [(label, key, text, res, [])] * n
    for key, ev, label in EVENTOS:
        jobs += [("evento: " + label, key, "", [], ["EVENTO AGORA (aconteceu agora; o jogador não falou nada; use exatamente os números e itens citados): " + ev])] * n

    def run(j):
        label, key, text, res, facts = j
        try:
            fala = ask(key, text, res, facts)
            nota, defs = judge(label + " | " + (text or "evento"), fala)
        except Exception as e:  # noqa: BLE001
            return label, key, "(erro: %s)" % e, 0, []
        return label, key, fala, nota, defs

    with cf.ThreadPoolExecutor(6) as ex:
        rows = list(ex.map(run, jobs))
    lines, by = [], {}
    for label, key, fala, nota, defs in rows:
        by.setdefault(label, []).append(nota)
        lines.append(f"- **{label}** ({key}) nota {nota} {defs}: {fala}")
    ok = [r[3] for r in rows if r[3]]
    resumo = ["| situação | nota média |", "|---|---|"] + [f"| {k} | {sum(v) / len(v):.2f} |" for k, v in by.items()]
    resumo.append(f"| **geral** | **{sum(ok) / len(ok):.2f}** |")
    defeitos = {}
    for r in rows:
        for d in r[4]:
            defeitos[d.upper()[:12]] = defeitos.get(d.upper()[:12], 0) + 1
    text = "\n".join(resumo) + "\n\nDefeitos: " + str(defeitos) + "\n\n" + "\n".join(lines) + "\n"
    print(text)
    if out:
        Path(out).write_text(text, encoding="utf-8")


if __name__ == "__main__":
    main()
