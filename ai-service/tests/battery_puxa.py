"""Bateria das ABERTURAS DE CONVERSA (familias ev_puxa_* de ambient.json), com a IA real como juiz. Uso (precisa da chave da API):

    set MOBVOZ_PROVIDER=api
    python tests/battery_puxa.py relatorio.md [--a] [--b] [--n=60]

--a  QUALIDADE: o juiz nota cada abertura (1-5): combina com a criatura, convida o jogador a responder, fatos de Minecraft corretos, sem erro de genero.
--b  CONTEXTO: o jogador responde a abertura (resposta direta, so "sim/nao", pergunta de volta, resposta sem pe nem cabeca) e a IA real responde COM o
     historico e o aviso que o servidor da (ConversationService.withOpening) E SEM eles (controle). O juiz diz se a resposta CONTINUA o assunto.
Sem --a/--b roda as duas."""
import concurrent.futures as cf
import json
import random
import re
import statistics
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from app import llm, service  # noqa: E402

AMB = Path(__file__).resolve().parents[2] / "mod-fabric" / "src" / "main" / "resources" / "mobvoz" / "ambient.json"
JUDGE = "deepseek-v4-pro"
MARK = "(a criatura puxou assunto com o jogador)"
WHO = {"calm": "uma criatura calma (aldeao ou animal)", "villager": "um aldeao", "golem": "um golem de ferro", "cow": "uma vaca", "pig": "um porco", "sheep": "uma ovelha",
       "chicken": "uma galinha", "horse": "um cavalo", "wolf": "um lobo", "cat": "um gato", "forest": "um bicho da floresta", "fox": "uma raposa", "bee": "uma abelha",
       "rabbit": "um coelho", "parrot": "um papagaio", "panda": "um panda", "ocelot": "uma jaguatirica", "goat": "uma cabra", "llama": "uma lhama", "camel": "um camelo",
       "frog": "um sapo", "mooshroom": "uma vaca-cogumelo", "armadillo": "um tatu", "snow_golem": "um golem de neve", "aquatic": "um bicho aquatico",
       "fish": "um peixe", "squid": "uma lula", "glow_squid": "uma lula brilhante", "dolphin": "um golfinho", "turtle": "uma tartaruga", "axolotl": "um axolote",
       "wandering_trader": "um mercador viajante"}
ENTITY = {"calm": ["minecraft:cow", "minecraft:villager", "minecraft:sheep", "minecraft:pig"], "villager": ["minecraft:villager"], "golem": ["minecraft:iron_golem"],
          "forest": ["minecraft:fox", "minecraft:rabbit"], "aquatic": ["minecraft:dolphin", "minecraft:cod"], "fish": ["minecraft:cod"]}
TAGS = {"day": "e de dia", "night": "e noite", "rain": "esta chovendo", "thunder": "ha tempestade", "clear": "o ceu esta limpo", "hurt": "o jogador esta ferido",
        "hungry": "o jogador esta com fome", "armor": "o jogador veste armadura", "followers": "varios seres seguem o jogador", "k0": "a criatura ja conhece o jogador",
        "n0": "a criatura sabe o nome do jogador", "snow": "bioma de neve", "desert": "bioma deserto", "jungle": "bioma selva", "swamp": "bioma pantano",
        "forest": "bioma floresta", "mountain": "bioma montanha", "beach": "praia", "savanna": "bioma savana"}


def chat_json(system, user, schema, model=JUDGE, tokens=900, temperature=0):
    msgs = [{"role": "system", "content": system}, {"role": "user", "content": user if isinstance(user, str) else json.dumps(user, ensure_ascii=False)}]
    return json.loads(llm.chat_api(msgs, model, 90, schema, {"temperature": temperature, "num_predict": tokens}))


def fill(t):
    return t.replace("{Jogador}", "Bia").replace("{jogador}", "Bia").replace("{nome}", "Bia").replace("{Nome}", "Bia")


def families():
    d = json.loads(AMB.read_text(encoding="utf-8"))
    return [f for f in d["families"] if f["id"].startswith("ev_puxa_")]


# ----------------------------------------------------------------------------------------------- A: qualidade
SYS_A = ("Você avalia FALAS DE ABERTURA de um mod de Minecraft: uma criatura pacífica PUXA ASSUNTO com o jogador, em voz alta, sem ele ter dito nada. "
         "Recebe 'quem' (a criatura), 'condicoes' e 'falas' (uma lista numerada). Dê 'nota' 1-5 a CADA fala. Critérios: (a) combina com o mundo e a personalidade de quem fala "
         "(vaca não fala de trocas ou casas; golem é sério; peixe fala de água; aldeão de ofício); (b) convida o jogador a responder (pergunta, curiosidade ou convite), "
         "sem soar como monólogo; (c) fatos de Minecraft CORRETOS (sem inventar mecânicas); (d) soa natural em português falado, curto, sem adjetivo de gênero errado "
         "para a criatura ou para o jogador; (e) não é chata nem invasiva. 5 = ótima; 3 = passável; 1 = sem sentido ou errada. Seja EXIGENTE. "
         "Responda SOMENTE JSON {\"linhas\": [{\"i\": 0, \"nota\": 1-5, \"problema\": \"\"}]} com 'problema' vazio se não há.")
SCHEMA_A = {"type": "object", "required": ["linhas"], "properties": {"linhas": {"type": "array", "items": {"type": "object", "required": ["i", "nota", "problema"],
            "properties": {"i": {"type": "integer"}, "nota": {"type": "integer"}, "problema": {"type": "string"}}}}}}


def quality(fam):
    who = WHO.get(fam["who"][0], fam["who"][0])
    cond = [TAGS[n] for n in fam["need"] if n in TAGS]
    lines = [fill(t) for t in fam["a"]]
    try:
        d = chat_json(SYS_A, {"quem": who, "condicoes": cond, "falas": {str(i): t for i, t in enumerate(lines)}}, SCHEMA_A, tokens=1500)
        rows = {int(x["i"]): (max(1, min(5, int(x["nota"]))), str(x.get("problema", ""))[:160]) for x in d["linhas"] if isinstance(x, dict)}
    except (llm.LlmError, ValueError, TypeError, KeyError):
        rows = {}
    return fam["id"], who, lines, rows


def run_a(out):
    with cf.ThreadPoolExecutor(6) as ex:
        res = list(ex.map(quality, families()))
    notas = [n for _, _, _, r in res for n, _ in r.values()]
    out.append(f"## A. Qualidade das aberturas\n\n{len(res)} familias, {sum(len(l) for _, _, l, _ in res)} aberturas julgadas. Media geral: **{statistics.mean(notas):.2f}** "
               f"(minimo {min(notas)}); abaixo de 4: {sum(1 for n in notas if n < 4)}; abaixo de 3: {sum(1 for n in notas if n < 3)}.\n")
    out.append("| familia | criatura | media | menor |\n|---|---|---|---|")
    weak = []
    for fid, who, lines, rows in sorted(res, key=lambda r: statistics.mean([n for n, _ in r[3].values()] or [0])):
        ns = [n for n, _ in rows.values()] or [0]
        out.append(f"| {fid} | {who} | {statistics.mean(ns):.2f} | {min(ns)} |")
        for i, (n, p) in rows.items():
            if n <= 3:
                weak.append((fid, lines[i], n, p))
    out.append("\n### Aberturas com nota 3 ou menos\n")
    for fid, t, n, p in weak:
        out.append(f"- `{fid}` ({n}): {t}  ->  {p}")
    print("A:", f"media {statistics.mean(notas):.2f}", "| notas<=3:", len(weak))
    return weak


# ----------------------------------------------------------------------------------------------- B: contexto
SYS_PLAYER = ("Você escreve a RESPOSTA de um jogador de Minecraft a uma criatura que acabou de puxar assunto. Responda SOMENTE JSON {\"resposta\": \"...\"}. "
              "Português do Brasil, falado, curto (3 a 14 palavras).")
SCHEMA_P = {"type": "object", "required": ["resposta"], "properties": {"resposta": {"type": "string"}}}
KINDS = {
    "direta": "Responda de verdade à pergunta ou ao convite da criatura, com uma informação sua.",
    "seca": "Responda de forma seca, só 'sim', 'não', 'talvez' ou 'depende', no máximo 3 palavras.",
    "volta": "Responda devolvendo a pergunta à criatura (ex.: 'E você?', 'Mas por que você pergunta?').",
    "estranha": "Responda com algo SEM NENHUMA relação com o assunto (ex.: pizza, o preço de uma coisa, outro lugar), como quem não ouviu.",
}
SYS_C = ("Você avalia CONTINUIDADE de conversa num jogo de Minecraft. Recebe 'criatura', 'abertura' (o que a criatura disse ao jogador para começar), "
         "'tipo_de_resposta' e 'jogador' (o que o jogador respondeu) e 'resposta' (o que a criatura falou em seguida). Dê 'nota' 1-5: a resposta CONTINUA o assunto "
         "que a criatura abriu (reage ao que o jogador disse, no mesmo tema)? 5 = continua naturalmente; 3 = liga ao assunto só de leve; 1 = ignora a abertura e muda de assunto "
         "ou recomeça do zero. Se o jogador respondeu algo SEM RELAÇÃO, vale reconhecer a estranheza e voltar ao tema. 'mudou_de_assunto' = true se a resposta nem cita o assunto aberto. "
         "Responda SOMENTE JSON {\"nota\": 1-5, \"mudou_de_assunto\": true|false, \"motivo\": \"\"}.")
SCHEMA_C = {"type": "object", "required": ["nota", "mudou_de_assunto", "motivo"], "properties": {"nota": {"type": "integer"}, "mudou_de_assunto": {"type": "boolean"}, "motivo": {"type": "string"}}}
OPEN_FACT = ("VOCÊ acabou de puxar assunto com o jogador, dizendo: \"{l}\". O que o jogador falou agora responde a isso: continue ESSE mesmo assunto, "
             "sem mudar de tema nem recomeçar a conversa.")


def context_case(case):
    fam, line, kind, entity = case
    rnd = random.Random(hash((fam["id"], line, kind)) & 0xFFFF)
    try:
        player = chat_json(SYS_PLAYER + " " + KINDS[kind], {"criatura": WHO.get(fam["who"][0], ""), "fala_da_criatura": line}, SCHEMA_P, tokens=120, temperature=0.9)["resposta"].strip()
    except (llm.LlmError, ValueError, KeyError, TypeError):
        return None
    base = {"entity_type": entity, "text": player, "capabilities": [], "claims": [], "relation": {"trust": 0.0, "affinity": 0.0, "fear": 0.0, "credulity": 1}}
    results = {}
    for label, facts, history in (("com_contexto", [OPEN_FACT.format(l=line)], [{"jogador": MARK, "voce": line}]), ("sem_contexto", [], [])):
        r = service.reply(dict(base, facts=facts, history=history))
        try:
            j = chat_json(SYS_C, {"criatura": entity.split(":")[1], "abertura": line, "tipo_de_resposta": kind, "jogador": player, "resposta": r["speech_text"]}, SCHEMA_C, tokens=300)
            results[label] = (max(1, min(5, int(j["nota"]))), bool(j["mudou_de_assunto"]), str(j.get("motivo", ""))[:200], r["speech_text"], r.get("source"))
        except (llm.LlmError, ValueError, KeyError, TypeError):
            results[label] = (0, True, "juiz falhou", r["speech_text"], r.get("source"))
    return fam["id"], line, kind, player, results


def run_b(out, n):
    rnd = random.Random(42)
    cases = []
    fams = families()
    rnd.shuffle(fams)
    for fam in fams:
        if len(cases) >= n * 4:
            break
        who = fam["who"][0]
        ents = ENTITY.get(who) or [f"minecraft:{who.replace('villager:', 'villager')}" if not who.startswith("villager:") else "minecraft:villager"]
        line = fill(rnd.choice(fam["a"]))
        for kind in KINDS:
            cases.append((fam, line, kind, rnd.choice(ents)))
    with cf.ThreadPoolExecutor(6) as ex:
        res = [r for r in ex.map(context_case, cases) if r]
    by = {"com_contexto": [], "sem_contexto": []}
    bykind = {k: {"com_contexto": [], "sem_contexto": []} for k in KINDS}
    for fid, line, kind, player, rr in res:
        for lab in by:
            by[lab].append(rr[lab])
            bykind[kind][lab].append(rr[lab])
    def stat(rows):
        ns = [r[0] for r in rows if r[0]]
        return (statistics.mean(ns) if ns else 0, sum(1 for r in rows if r[1]), len(rows))
    out.append(f"## B. Contexto: a resposta continua o assunto que a criatura abriu?\n\n{len(res)} conversas (abertura + resposta do jogador + resposta da IA real), cada uma COM e SEM o contexto.\n")
    out.append("| situacao | com contexto: nota | mudou de assunto | sem contexto: nota | mudou de assunto |\n|---|---|---|---|---|")
    for lab_k, rows in [("TODAS", None)] + [(k, bykind[k]) for k in KINDS]:
        a = stat(by["com_contexto"] if rows is None else rows["com_contexto"])
        b = stat(by["sem_contexto"] if rows is None else rows["sem_contexto"])
        out.append(f"| {lab_k} | **{a[0]:.2f}** | {a[1]}/{a[2]} | {b[0]:.2f} | {b[1]}/{b[2]} |")
    out.append("\n### Conversas em que, COM contexto, a IA mudou de assunto ou deu nota 3 ou menos\n")
    bad = [(f, l, k, p, rr) for f, l, k, p, rr in res if rr["com_contexto"][1] or rr["com_contexto"][0] <= 3]
    for f, l, k, p, rr in bad:
        c = rr["com_contexto"]
        out.append(f"- `{f}` [{k}] criatura: \"{l}\" | jogador: \"{p}\" | IA: \"{c[3]}\" -> nota {c[0]}: {c[2]}")
    a = stat(by["com_contexto"])
    print("B:", f"com contexto {a[0]:.2f} (mudou de assunto {a[1]}/{a[2]})", "| sem contexto", f"{stat(by['sem_contexto'])[0]:.2f}", "| ruins:", len(bad))
    return a


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    path = next((Path(a) for a in sys.argv[1:] if not a.startswith("--")), None)
    n = next((int(a.split("=")[1]) for a in sys.argv if a.startswith("--n=")), 60)
    do_a = "--a" in sys.argv or "--b" not in sys.argv
    do_b = "--b" in sys.argv or "--a" not in sys.argv
    out = ["# Bateria das aberturas de conversa (mob puxa assunto)\n"]
    if do_a:
        run_a(out)
    if do_b:
        run_b(out, n)
    if path:
        path.write_text("\n".join(out) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
