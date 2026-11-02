"""Juiz de IA POR FAMILIA de assunto das conversas ambiente (ambient.json): serve para decidir o que entra e achar as familias fracas.
Renderiza N cenas de cada familia (mesma logica do motor Java, resumida) e pede a nota 1-5 para ESSAS criaturas nessa situacao.

Uso:  python tests/battery_families.py relatorio.md [--only=id1,id2] [--n=8] [--min=4.0]
Sem --only avalia todas. O relatorio lista a media por familia e os problemas citados; familias abaixo de --min saem marcadas com "REVER".
"""
import concurrent.futures as cf
import json
import os
import random
import re
import statistics
import sys
import time
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from app import config, llm  # noqa: E402

AMB = Path(__file__).resolve().parents[2] / "mod-fabric" / "src" / "main" / "resources" / "mobvoz" / "ambient.json"
JUDGE_MODEL = "deepseek-v4-pro"
LOCAL = os.environ.get("MOBVOZ_JUDGE") == "local"  # juiz local (Ollama, de graca): mais fraco e mais brando que o da API
LOCAL_MODEL = os.environ.get("MOBVOZ_JUDGE_MODEL", "qwen3:4b")
SCHEMA = {"type": "object", "required": ["nota", "problemas"],
          "properties": {"nota": {"type": "integer"}, "problemas": {"type": "array", "items": {"type": "string"}}}}
SYSTEM = (
    "Você avalia pequenas conversas AMBIENTE de um jogo de Minecraft: duas criaturas conversam entre si perto do jogador e ele as ouve. "
    "Recebe: 'quem' (as duas criaturas), 'situacao' e 'falas' (0 = quem abre, 1 = quem responde, 0 de novo = fecho). "
    "Se houver UMA fala só, é uma criatura reagindo sozinha, em voz alta, a algo que acabou de acontecer: julgue se essa reação soa natural e variada. "
    "Quando a situação é um ACONTECIMENTO (explosão, raio, presente, pancada...), a fala tem de combinar com ele e com o mundo de quem a diz. "
    "Dê 'nota' de 1 a 5: faz sentido para ESSAS criaturas nessa situação? Critérios: (a) cada fala combina com o mundo de quem a diz "
    "(vaca não fala de trocas ou casas; aldeão não muge; zumbi não é simpático; bicho não filosofa); (b) a resposta combina com o que foi dito "
    "e o fecho fecha a conversa; (c) bate com a situação; (d) soa natural em português falado, curto. "
    "Lembre: é um jogo em que os mobs falam; não penalize só porque animais falam ou citam o 'humano'. "
    "5 = faz total sentido e é agradável de ouvir; 3 = estranho mas passável; 1 = sem sentido ou fora do mundo da criatura. "
    "Seja EXIGENTE e específico. Liste em 'problemas' cada problema real (frase curta, citando a fala); lista vazia se não há. "
    "Responda SOMENTE JSON: {\"nota\": 1-5, \"problemas\": [\"...\"]}."
)
WHO = {
    "calm": "duas criaturas calmas (aldeões ou animais da fazenda)", "villagers": "dois aldeões", "villager": "dois aldeões",
    "animals": "dois animais da fazenda", "farm": "dois animais da fazenda", "herd": "dois animais da fazenda", "monsters": "dois monstros hostis",
    "cow": "uma vaca", "pig": "um porco", "sheep": "uma ovelha", "chicken": "uma galinha", "wolf": "um lobo", "cat": "um gato", "horse": "um cavalo",
    "golem": "um golem de ferro", "zombie": "um zumbi", "skeleton": "um esqueleto", "spider": "uma aranha", "creeper": "um creeper",
    "enderman": "um enderman", "witch": "uma bruxa", "warden": "um Warden", "pet": "um animal de estimação (lobo ou gato)",
    "aquatic": "um bicho aquático", "fish": "um peixe", "squid": "uma lula", "glow_squid": "uma lula brilhante", "dolphin": "um golfinho", "turtle": "uma tartaruga",
    "axolotl": "um axolote", "flying": "um bicho voador", "bee": "uma abelha", "parrot": "um papagaio", "bat": "um morcego", "allay": "um allay", "fox": "uma raposa",
    "rabbit": "um coelho", "panda": "um panda", "ocelot": "uma jaguatirica", "goat": "uma cabra", "llama": "uma lhama", "camel": "um camelo", "frog": "um sapo",
    "armadillo": "um tatu", "polar_bear": "um urso-polar", "sniffer": "um sniffer", "mooshroom": "uma vaca-cogumelo", "nether": "uma criatura do Nether",
    "piglin": "um piglin", "piglin_brute": "um piglin brutamontes", "hoglin": "um hoglin", "zombified_piglin": "um piglin zumbificado", "strider": "um strider",
    "blaze": "um blaze", "ghast": "um ghast", "magma_cube": "um cubo de magma", "wither_skeleton": "um esqueleto wither", "shulker": "um shulker",
    "endermite": "um endermite", "slime": "um slime", "cave_spider": "uma aranha da caverna", "silverfish": "um traça", "husk": "um husk", "stray": "um stray",
    "drowned": "um afogado", "guardian": "um guardião", "elder_guardian": "um guardião ancião", "vex": "um vex", "phantom": "um phantom", "pillager": "um saqueador",
    "vindicator": "um vingador", "evoker": "um evocador", "ravager": "um devastador",
}
TAGS = {"day": "é de dia", "night": "é noite", "dusk": "é fim de tarde", "dawn": "está amanhecendo", "clear": "o céu está limpo", "rain": "está chovendo",
        "thunder": "há tempestade", "hurt": "o jogador está ferido", "hungry": "o jogador está com fome", "holds": "o jogador segura um item",
        "armor": "o jogador veste armadura", "k0": "quem abre já conhece o jogador", "k1": "quem responde já conhece o jogador", "n0": "quem abre sabe o nome do jogador",
        "want0": "quem abre GOSTA do que o jogador segura",
        "f0:TRADE": "quem abre já fez uma troca justa com o jogador (fato verificado)", "f0:GIFT": "o jogador já deu um presente a quem abre (fato verificado)",
        "f0:HONEST_RETURN": "o jogador já devolveu a quem abre algo que ele tinha entregado, sem ser pedido (fato verificado)",
        "f0:DAMAGED_BY_PLAYER": "o jogador já machucou quem abre (fato verificado)", "f0:PACT_BROKEN": "o jogador já quebrou um acordo com quem abre (fato verificado)",
        "f0:THREAT_SPOKEN": "o jogador já ameaçou quem abre com palavras (fato verificado)", "f0:COMFORTED": "o jogador já acalmou quem abre quando estava com medo (fato verificado)",
        "f0:TAMED": "quem abre foi domado e agora pertence ao jogador (fato verificado)",
        "ev:boom": "ACABOU DE EXPLODIR um creeper bem perto", "ev:thunder": "ACABOU DE CAIR UM RAIO bem perto", "ev:wake": "o jogador ACABOU DE ACORDAR de manhã",
        "ev:revive": "o jogador ACABOU DE VOLTAR de uma morte (a criatura já o conhece)", "ev:thanks": "o jogador ACABOU DE MATAR um monstro perto da vila",
        "ev:raiders": "SAQUEADORES hostis estão chegando perto da vila", "ev:fall": "o jogador ACABOU DE CAIR de muito alto e se machucou",
        "ev:hit": "o jogador ACABOU DE BATER nessa criatura (ela reage sozinha)", "ev:gift_like": "o jogador ACABOU DE DAR um presente que a criatura ADORA",
        "ev:gift_meh": "o jogador ACABOU DE DAR um presente que não diz nada à criatura", "ev:gift_dislike": "o jogador ACABOU DE DAR um presente que a criatura DETESTA",
        "ev:danger": "um inimigo se aproxima do jogador e a criatura, que o acompanha, o avisa", "ev:greet": "a criatura reencontra o jogador, que ela já conhece",
        "ev:egg": "o jogador ACABOU DE PEGAR o ovo que a galinha tinha botado", "ev:honest": "o jogador ACABOU DE DEVOLVER, sem ser pedido, algo que a criatura lhe entregara",
        "snow": "o bioma é de neve e frio", "desert": "o bioma é um deserto", "jungle": "o bioma é uma selva", "swamp": "o bioma é um pântano",
        "forest": "o bioma é uma floresta", "mountain": "o bioma é de montanha, alto e ventoso", "beach": "estão numa praia", "savanna": "o bioma é uma savana",
        "fullmoon": "é noite de lua cheia", "newmoon": "é noite de lua nova, sem lua", "deep": "estão bem fundo, em cavernas", "high": "estão num lugar muito alto",
        "sneak": "o jogador anda agachado, na pontinha dos pés", "run": "o jogador está correndo", "ride": "o jogador está montado em algo (cavalo, barco...)",
        "swim": "o jogador está nadando na água", "burning": "o jogador está pegando fogo", "fly": "o jogador está voando (asas de elytra)",
        "emeralds": "o jogador carrega esmeraldas", "diamonds": "o jogador carrega diamantes", "followers": "vários seres seguem o jogador como companheiros",
        "ally": "um ser segue o jogador como companheiro"}


def who_text(who):
    a = WHO.get(who[0], who[0])
    b = WHO.get(who[1], who[1])
    if who[0] == who[1] or (a.startswith("dois ") and a == b):
        return a if a.startswith("dois ") else a + " e outra criatura da mesma espécie"
    return f"{a} e {b}"


def item_for(f):
    w = set(f["who"])
    if w & {"golem"}:
        return "uma papoula"
    if w & {"villager", "villagers"} or any(x.startswith("villager:") for x in w):
        return "esmeraldas"
    if w & {"animals", "farm", "herd", "cow", "pig", "sheep", "chicken", "horse", "wolf", "cat", "pet"}:
        return "trigo" if not w & {"wolf", "cat"} else ("osso" if "wolf" in w else "peixe cru")
    return "uma espada de ferro"


def thing_for(f):
    ev = next((n for n in f["need"] if n.startswith("ev:")), "")
    return {"ev:danger": "zumbi", "ev:honest": "trigo" if "animals" in f["who"] else "3 esmeraldas", "ev:gift_like": "papoula" if "golem" in f["who"] else "cenoura", "ev:gift_meh": "pedra", "ev:gift_dislike": "carne podre"}.get(ev, "")


def render(f, rnd, pools, scene=None):
    if scene is not None:  # cena escrita inteira (abertura, resposta e fecho que combinam)
        texts = [scene["a"], scene["b"]] + ([scene["c"]] if scene.get("c") else [])
    else:
        i = rnd.randrange(len(f["a"]))
        a = f["a"][i]
        bpool = f["b"] if (not f.get("bg") or a.strip().endswith("?")) else f["b"] + f["bg"]
        texts = [a] if f.get("solo") else [a, rnd.choice(bpool)] + ([rnd.choice(f["c"])] if f["c"] else [])
    chosen = {}
    item = item_for(f)
    out = []
    for t in texts:
        def sub(m):
            s = m.group(1)
            if s == "jogador":
                return "o humano"
            if s == "Jogador":
                return "O humano"
            if s in ("nome", "Nome"):
                return "Bia"
            if s == "coisa":
                return thing_for(f)
            if s == "Coisa":
                return thing_for(f).capitalize()
            if s == "item":
                return item
            if s == "Item":
                return item[:1].upper() + item[1:]
            if s == "armadura":
                return "uma armadura de ferro"
            if s.startswith("@"):
                return chosen.setdefault(s, rnd.choice(pools[s[1:]]))
            return s
        out.append(re.sub(r"\{([^}]*)\}", sub, t).replace("de o humano", "do humano").replace("em o humano", "no humano").replace(" a o humano", " ao humano"))
    return [f"{k}: {t}" for k, t in zip([0, 1, 0], out)]


def judge(item):
    msgs = [{"role": "system", "content": SYSTEM}, {"role": "user", "content": json.dumps(item["payload"], ensure_ascii=False)}]
    for model in ((LOCAL_MODEL,) if LOCAL else (JUDGE_MODEL, config.API_MODEL)):
        try:
            opts = {"temperature": 0, "num_predict": 300}
            d = json.loads(llm.chat_ollama(msgs, model, 120, SCHEMA, opts) if LOCAL else llm.chat_api(msgs, model, 60, SCHEMA, opts))
            return {"nota": max(1, min(5, int(d.get("nota", 1)))), "problemas": [str(p)[:200] for p in d.get("problemas", [])][:4]}
        except (llm.LlmError, ValueError, TypeError):
            continue
    return {"nota": 0, "problemas": ["avaliador falhou"]}


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    args = [a for a in sys.argv[1:] if a.startswith("--")]
    out_path = next((Path(a) for a in sys.argv[1:] if not a.startswith("--")), None)
    only = next((set(a.split("=", 1)[1].split(",")) for a in args if a.startswith("--only=")), None)
    n = int(next((a.split("=", 1)[1] for a in args if a.startswith("--n=")), 8))
    minimo = float(next((a.split("=", 1)[1] for a in args if a.startswith("--min=")), 4.0))
    d = json.loads(AMB.read_text(encoding="utf-8"))
    rnd = random.Random(7)
    items = []
    for f in d["families"]:
        if only and f["id"] not in only:
            continue
        seen = set()
        for _ in range(n * 4):
            scene = rnd.choice(f["scenes"]) if f.get("scenes") else None
            lines = render(f, rnd, d["pools"], scene)
            key = " | ".join(lines)
            if key in seen:
                continue
            seen.add(key)
            need = f["need"] + (scene.get("need", []) if scene else [])
            tags = [TAGS[t] for t in need if t in TAGS] + [("NÃO: " + TAGS[t[1:]]) for t in need if t.startswith("!") and t[1:] in TAGS]
            items.append({"family": f["id"], "payload": {"quem": who_text(f["who"]), "situacao": "; ".join(tags) or "uma situação qualquer do dia a dia", "falas": lines}})
            if len(seen) >= n:
                break
    t0 = time.time()
    with cf.ThreadPoolExecutor(max_workers=2 if LOCAL else 8) as ex:
        res = list(ex.map(judge, items))
    by = {}
    for it, r in zip(items, res):
        it["juiz"] = r
        by.setdefault(it["family"], []).append(it)
    rows = []
    for fid, its in by.items():
        notas = [i["juiz"]["nota"] for i in its if i["juiz"]["nota"] > 0]
        rows.append((statistics.mean(notas) if notas else 0, fid, its))
    rows.sort()
    allnotas = [i["juiz"]["nota"] for it in by.values() for i in it if i["juiz"]["nota"] > 0]
    lines = ["# Juiz de IA por família de assunto (conversas ambiente)", "",
             f"{time.strftime('%Y-%m-%d %H:%M')}; {len(by)} famílias, {len(items)} cenas, juiz `{LOCAL_MODEL if LOCAL else JUDGE_MODEL}`, {time.time() - t0:.0f} s. Média geral **{statistics.mean(allnotas):.2f}**; "
             f"famílias abaixo de {minimo}: **{sum(1 for r in rows if r[0] < minimo)}**.", "", "| Família | cenas | nota |", "|---|---|---|"]
    json_path = next((a.split("=", 1)[1] for a in args if a.startswith("--json=")), None)
    if json_path:  # dados por familia (nota, problemas, cenas) para o reescritor assistido por IA (tools/gen/reescrever_familias.py)
        data = {fid: {"nota": round(m, 2), "cenas": [{"nota": i["juiz"]["nota"], "falas": i["payload"]["falas"], "problemas": i["juiz"]["problemas"]} for i in its]}
                for m, fid, its in rows}
        Path(json_path).write_text(json.dumps(data, ensure_ascii=False, indent=1), encoding="utf-8")
    for m, fid, its in rows:
        lines.append(f"| {fid} | {len(its)} | {m:.2f}{'  REVER' if m < minimo else ''} |")
    lines += ["", "## Problemas apontados (famílias abaixo do mínimo primeiro)", ""]
    for m, fid, its in rows:
        probs = [(i["juiz"]["nota"], i["payload"]["falas"], i["juiz"]["problemas"]) for i in its if i["juiz"]["nota"] <= 3 or i["juiz"]["problemas"]]
        if not probs:
            continue
        lines.append(f"### {fid} ({m:.2f})  _{its[0]['payload']['quem']}; {its[0]['payload']['situacao']}_")
        for nota, falas, pr in probs[:5]:
            lines.append(f"- nota {nota}: " + " / ".join(falas))
            lines += [f"  - ⚠ {p}" for p in pr[:2]]
        lines.append("")
    text = "\n".join(lines)
    if out_path:
        out_path.write_text(text, encoding="utf-8")
    print("\n".join(lines[:5]))
    print("\n".join(f"{m:.2f} {fid}" for m, fid, _ in rows[:25]))


if __name__ == "__main__":
    main()
