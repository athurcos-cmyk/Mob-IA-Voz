"""Bateria do atalho de papo simples (app/fastpath.py), com a IA REAL como juiz. Uso (API propria; precisa da chave em ~/.mobvoz/api.json):

    set MOBVOZ_PROVIDER=api
    python tests/battery_fastpath.py --gen      # a IA escreve frases novas por categoria (nao enviesadas pelo atalho) e salva
    python tests/battery_fastpath.py            # confere: para cada frase, a analise REAL da IA vs a decisao do atalho

Seguranca: "falso atalho" = o atalho dispensou a analise mas a IA acharia algo (afirmacao, ordem, troca, tom, desconto, apelo). Tem de ser 0.
Cobertura: das frases cuja analise e vazia, quantas o atalho de fato dispensa (quanto maior, mais rapido o jogo)."""
import json
import sys
import time
from collections import defaultdict
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from app import fastpath, llm, service  # noqa: E402
from tests.fastpath_corpus import ACTION, CHAT  # noqa: E402

GEN_FILE = Path(__file__).with_name("fastpath_generated.json")
JUDGED_FILE = Path(__file__).with_name("fastpath_judged.json")  # cache: texto -> analise REAL (a IA com temperatura 0 e estavel; apague para refazer)
CAPS = ["REGISTER_CLAIM", "FOLLOW_PLAYER", "STAY_AT", "PORTAL_HOLD", "PORTAL_RELEASE", "DISMISS_ALLY", "CEASE_HOSTILITY", "DEFEND_PLAYER", "TAME_ME",
        "OPEN_TRADE", "SOCIAL"]

CATEGORIES = {
    "papo": "perguntas de conversa comum para a criatura (gostos, rotina, opinião, sentimentos, curiosidade sobre a vida dela)",
    "cumprimento": "cumprimentos, despedidas, agradecimentos e elogios curtos",
    "mundo": "perguntas ou comentários sobre o clima, a hora, o lugar, a noite, os perigos do mundo",
    "afirmacao": "afirmações do jogador sobre si mesmo ou sobre a criatura (parentesco, título, posse, algo que fez, mentiras divertidas)",
    "pedido_educado": "pedidos e ordens educados, ESCRITOS COMO PERGUNTA ou com rodeios (seguir, ficar, ajudar, proteger, esperar, ir embora)",
    "troca": "falas de troca, compra, venda, preço, desconto, oferta ou pedido de item, em linguagem natural",
    "ameaca_consolo": "ameaças (matar, comer, cozinhar, machucar) e consolos (acalmar, prometer não machucar, carinho)",
    "domar_apelido": "tentar domar, montar ou adotar a criatura, e dar nome ou apelido a ela",
    "misto": "frases que começam como papo ou cumprimento mas escondem um pedido, uma troca, uma ameaça ou uma afirmação no meio ou no fim",
    "item_papo": "perguntas sobre comida, ferramentas, itens e materiais que PARECEM papo mas citam um item (gosta de, já comeu, sabe fazer)",
    "sem_pontuacao": "frases como saem de um reconhecimento de voz: tudo minúsculo, sem pontuação, mistura de papo, pedidos e trocas",
    "emocao": "frases emotivas, desabafos, piadas, perguntas existenciais e curiosidades que NÃO pedem nada à criatura",
}


def empty_analysis(a: dict) -> bool:
    return (not a.get("claim") and not a.get("orders") and not a.get("ask", {}).get("item") and not a.get("offers")
            and not a.get("persuasion", {}).get("item") and not a.get("persuasion", {}).get("strength")
            and a.get("social", "NONE") == "NONE" and not a.get("haggle") and not a.get("appeal"))


def harmful(a: dict) -> bool:
    """O que a analise acharia e MUDA o jogo (afirmacao, ordem, tom, desconto, apelo, pedido/pagamento, convencimento). So citar um item em pergunta de gosto (offers) e benigno."""
    return bool(a.get("claim") or a.get("orders") or a.get("ask", {}).get("item") or a.get("persuasion", {}).get("item")
                or a.get("persuasion", {}).get("strength") or a.get("social", "NONE") != "NONE" or a.get("haggle") or a.get("appeal"))


def generate(per_category: int = 40) -> None:
    schema = {"type": "object", "required": ["frases"], "properties": {"frases": {"type": "array", "items": {"type": "string"}}}}
    out: dict[str, list[str]] = {}

    def one(item):
        cat, desc = item
        msgs = [{"role": "system", "content": "Você cria frases de teste em português do Brasil para um mod de Minecraft em que o jogador fala com criaturas "
                                              "(vaca, porco, ovelha, galinha, lobo, gato, cavalo, aldeão, golem de ferro, zumbi, esqueleto, creeper, Warden...). "
                                              "Responda só JSON {\"frases\": [..]}. Frases curtas (3 a 16 palavras), variadas, informais, SEM repetir estrutura."},
                {"role": "user", "content": f"Escreva {per_category} frases diferentes da categoria: {desc}. Varie bastante o vocabulário e a criatura com quem se fala."}]
        raw = llm.chat(msgs, schema=schema, options={"temperature": 1.0, "num_predict": 3000})
        return cat, [" ".join(str(f).split()) for f in json.loads(raw)["frases"] if isinstance(f, str) and f.strip()]

    with ThreadPoolExecutor(6) as ex:
        for cat, frases in ex.map(one, CATEGORIES.items()):
            out[cat] = list(dict.fromkeys(frases))
            print(f"{cat}: {len(out[cat])} frases")
    GEN_FILE.write_text(json.dumps(out, ensure_ascii=False, indent=1), encoding="utf-8")


def judge_all(corpus: dict[str, list[str]]) -> int:
    real = fastpath.is_simple
    fastpath.is_simple = lambda t: False  # a analise de VERDADE (sem atalho) e o gabarito
    rows = []
    t0 = time.perf_counter()
    items = [(c, t) for c, lst in corpus.items() for t in lst]

    cache = json.loads(JUDGED_FILE.read_text(encoding="utf-8")) if JUDGED_FILE.exists() else {}

    def one(ct):
        c, t = ct
        a = cache.get(t) or service.analyze({"text": t, "capabilities": CAPS})
        cache[t] = a
        return c, t, empty_analysis(a), a

    with ThreadPoolExecutor(8) as ex:
        rows = list(ex.map(one, items))
    fastpath.is_simple = real
    JUDGED_FILE.write_text(json.dumps(cache, ensure_ascii=False, indent=0), encoding="utf-8")
    print(f"{len(rows)} analises reais em {time.perf_counter() - t0:.0f} s")
    stats = defaultdict(lambda: [0, 0, 0, 0])  # n, atalho, vazias, falsos
    bad = []
    benign = []
    for c, t, empty, a in rows:
        fast = real(t)
        s = stats[c]
        s[0] += 1
        s[1] += fast
        s[2] += empty
        if fast and harmful(a):
            s[3] += 1
            bad.append((c, t, a))
        elif fast and not empty:
            benign.append((c, t, a))
    print(f"\n{'categoria':16s} {'frases':>6s} {'atalho':>7s} {'ia_vazia':>8s} {'FALSOS':>7s}")
    tot = [0, 0, 0, 0]
    for c, s in stats.items():
        print(f"{c:16s} {s[0]:6d} {s[1]:7d} {s[2]:8d} {s[3]:7d}")
        tot = [x + y for x, y in zip(tot, s)]
    print(f"{'TOTAL':16s} {tot[0]:6d} {tot[1]:7d} {tot[2]:8d} {tot[3]:7d}")
    empties = tot[2]
    caught = sum(1 for c, t, empty, a in rows if empty and real(t))
    print(f"\ncobertura: {caught} de {empties} frases de analise vazia usam o atalho ({100 * caught / max(1, empties):.0f}%)")
    print(f"falsos atalhos (o atalho dispensou, a IA acharia algo): {len(bad)}")
    for c, t, a in bad:
        print(f"  [{c}] {t!r} -> {json.dumps({k: v for k, v in a.items() if v and v != 'NONE' and v != 0 and v != {'item': '', 'count': 0}}, ensure_ascii=False)}")
    print(f"\nbenignos (so citou item numa pergunta de gosto): {len(benign)}")
    for c, t, a in benign:
        print(f"  [{c}] {t!r} -> {a.get('offers')}")
    return len(bad)


def main() -> None:
    if "--gen" in sys.argv:
        generate()
        return
    corpus = {"chat(nosso)": CHAT, "acao(nosso)": ACTION}
    only = [a.split("=", 1)[1] for a in sys.argv if a.startswith("--so=")]  # ex.: --so=fastpath_generated_v2.json
    for f in sorted(GEN_FILE.parent.glob("fastpath_generated*.json")):
        if only and f.name not in only:
            continue
        for cat, lst in json.loads(f.read_text(encoding="utf-8")).items():
            corpus[cat + "|" + f.stem.replace("fastpath_generated", "") if f.name != GEN_FILE.name else cat] = lst
    if only:
        corpus = {k: v for k, v in corpus.items() if "|" in k}
    sys.exit(1 if judge_all(corpus) else 0)


if __name__ == "__main__":
    main()
