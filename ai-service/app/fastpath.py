"""Atalho de papo simples: pergunta ou cumprimento SEM pedido, troca, item, afirmacao nem tom dispensa a analise da IA
(uma chamada a menos por fala). Conservador de proposito: na menor duvida devolve False e a analise completa roda.
O que passa aqui teria, de qualquer jeito, uma analise vazia (sem afirmacao, ordens, troca, ameaca, consolo, desconto ou apelo).
Medido em tests/battery_fastpath.py (a IA real confere cada fala que o atalho deixa passar)."""
import json
import re
import unicodedata
from pathlib import Path

MAX_CHARS = 90
MAX_WORDS = 14

_ITEMS = frozenset(json.loads(Path(__file__).with_name("item_words.json").read_text(encoding="utf-8")))

# Qualquer uma destas pistas (texto sem acento) manda a fala para a analise completa.
_CUES = re.compile(r"\b(?:" + "|".join([
    # eu/meu: afirmacao do jogador sobre si (parentesco, posse, feito)
    r"eu", r"sou", r"era", r"fui", r"somos", r"sejam?\w*", r"nos", r"nosso\w*", r"nossa\w*", r"meu\w*", r"minha\w*", r"mim", r"comigo", r"conosco",
    r"dono", r"rei", r"rainha", r"chefe", r"pai", r"mae", r"filh\w*", r"irma\w*", r"tio", r"tia", r"primo", r"prima", r"avo",
    r"casad\w*", r"espos\w*", r"marido", r"namorad\w*", r"lembr\w*", r"sabia\w*", r"prometo", r"prometi", r"juro", r"garanto", r"ontem",
    # me/te: pedidos e atos dirigidos a criatura
    r"me", r"te", r"lhe", r"tenha", r"tenhas",
    # ordens e movimento
    r"segue", r"segui", r"seguir", r"seguindo", r"seguimos", r"seguem", r"sigo", r"siga\w*", r"vem", r"venh\w*", r"vamo\w*", r"vou", r"vao",
    r"vir", r"ir", r"anda", r"ande", r"andem", r"fic[ae]\w*", r"fiqu\w*", r"espera", r"esperar", r"espere\w*", r"esperando", r"aguard\w*",
    r"volt\w*", r"entr\w*", r"sai", r"saia", r"sair", r"portal", r"nether", r"pare", r"parar", r"corr\w*", r"pul\w*",
    # pedidos
    r"pode", r"podes", r"posso", r"podemos", r"poderi\w*", r"contar", r"conto", r"quer\w*", r"queria", r"precis\w*", r"necessit\w*", r"gostaria",
    r"desej\w*", r"aceit\w*", r"topa\w*", r"deix\w*", r"faca\w*", r"faria", r"fazemos", r"ajud\w*", r"proteg\w*", r"defend\w*", r"salv\w*",
    r"guard\w*", r"vigi\w*", r"luta\w*", r"lute", r"ataq\w*", r"brig\w*", r"obedec\w*", r"ordem", r"ordens", r"mand\w*", r"cuide", r"cuidem",
    # troca, preco, presente
    r"troc\w*", r"negoci\w*", r"compr\w*", r"vend\w*", r"pag\w*", r"preco\w*", r"valor\w*", r"valem", r"vale", r"cust\w*", r"barat\w*", r"caro", r"cara",
    r"caros", r"caras", r"descont\w*", r"oferec\w*", r"oferta\w*", r"presente\w*", r"ganh\w*", r"emprest\w*", r"dar", r"dou", r"dando", r"dado",
    r"deu", r"daria", r"tem", r"tens", r"teria", r"tinha", r"trouxe", r"trag\w*", r"leva\w*", r"pega\w*", r"peg[ao]", r"toma\w*",
    r"beb\w*", r"cozinh\w*", r"assar", r"assad\w*", r"churrasc\w*",
    # tom: ameaca ou consolo
    r"mat\w*", r"morr\w*", r"mort\w*", r"machuc\w*", r"feri\w*", r"dor", r"doer", r"sangr\w*", r"abat\w*", r"explod\w*", r"queim\w*", r"destru\w*",
    r"odei\w*", r"idiota", r"burr\w*", r"besta", r"feio\w*", r"chato", r"calm[ao]", r"tranquil\w*", r"assust\w*", r"desculp\w*",
    r"perdo\w*", r"carinh\w*", r"abrac\w*", r"paz", r"pazes", r"consol\w*", r"bat[aeo]\w*", r"bater",
    # domar
    r"dom[ae]\w*", r"mont\w*", r"cavalg\w*", r"adot\w*", r"criar",
    # pedir, ir embora, nomear
    r"sem", r"ped\w*", r"peco", r"embora", r"foss\w*", r"cham(?:ar|arei|ado|ada|o)", r"batiz\w*", r"apelid\w*", r"rebatiz\w*",
]) + r")\b|^(?:cuid\w*|vai|va|faz|fazer)\b|\b(?:vai|va) (?:embora|pra|para|la|ate|sair|ficar)\b|\bfaz (?:um|uma|isso|aquilo|negocio|favor)\b|"
    r"\bfazer (?:um|uma|isso|aquilo|negocio|favor)\b|\b(?:virar|vira|virou|em|ser|seria|sera|e) (?:comida|comido|comida)\b|\bser comid\w*|\bvou comer\b|\bnome (?:sera|agora|passa|vai|e|eh) \w+|\bcuid[ae]\b")

_WH = re.compile(r"^(?:o que|oque|que|como|por que|porque|pq|quem|qual|quais|quando|onde|aonde|quanto|quantos|quantas|cade|sera que|existe|ja)\b")
_GREETING = re.compile(r"^(?:oi+|oie|ola|opa|e ai|eae|fala|salve|bom dia|boa tarde|boa noite|tudo bem|tudo bom|tudo certo|como vai|como esta|beleza|"
                       r"obrigad[oa]|brigad[oa]|valeu|tchau|ate (?:mais|logo|breve|a proxima)|adeus)\b")
_THAT_HAS = re.compile(r"^(?:o que|que) tem\b")  # "O que tem de bom por aqui?" e papo; "Voce tem X?" e pedido
# Construcoes de papo que contem uma pista, mas nao pedem nada (a pista some so nelas): "voce me acha legal?", "bom te ver", "tem medo de...?"
_SAFE = [
    (re.compile(r"\bme (?:acha|conhece|ouve|escuta|entende|ve|enxerga|reconhece|ama|odeia|estranha|nota|percebe|sente|cheira)\b"), " "),
    (re.compile(r"\bte (?:deixa|faz|incomoda|alegra|irrita|agrada|chateia|preocupa|ver|vejo|conheco|acha)\b"), " "),
    (re.compile(r"\btem (?:algum |alguma |um |uma |muito |muita |mais )?(?:medos?|saudades?|amig\w+|famil\w+|sonh\w+|nome|irma\w*|casa|filhote\w*|"
                r"favorit\w+|vontade|sono|fome|sede|frio|calor|raiva|ciume\w*|ideia|segredo\w*|bicho\w*|habito\w*|mania\w*|jeito|historia\w*|"
                r"vida|tempo|hobby|hobbies|preferid\w+|orgulho|inimig\w+)\b"), " "),
    (re.compile(r"\bquem cuida\b"), " "),
    (re.compile(r"\bgolem de ferro\b"), "golem"),
    (re.compile(r"\besqueleto wither\b"), "esqueleto"),
]
# Pergunta de GOSTO ou de saber ("gosta de maca?", "ja comeu bolo?"): citar o item ali nao e pedido nem troca.
_TASTE = re.compile(r"\b(?:gosta|gostou|prefere|come|comeu|provou|conhece|sabe|acha|adora|detesta|ama|odeia|curte|enjoa)\b")


def _norm(text: str) -> str:
    return "".join(c for c in unicodedata.normalize("NFD", text.lower()) if unicodedata.category(c) != "Mn")


def is_simple(text: str) -> bool:
    """True so quando a fala e uma pergunta ou um cumprimento curto sem nenhuma pista de pedido, troca, item, afirmacao ou tom."""
    raw = (text or "").strip()
    if not raw or len(raw) > MAX_CHARS or re.search(r"\d", raw):
        return False
    n = _norm(raw)
    n = _THAT_HAS.sub("o que", n)
    for pat, rep in _SAFE:
        n = pat.sub(rep, n)
    words = re.findall(r"[a-z]+", n)
    if not words or len(words) > MAX_WORDS or _CUES.search(n):
        return False
    has_item = any(w in _ITEMS or (w.endswith("s") and w[:-1] in _ITEMS) for w in words)
    if has_item and not (raw.endswith("?") and _TASTE.search(n)):
        return False
    body = " ".join(words)
    if _GREETING.match(body) and len(words) <= 6:
        return True
    return raw.endswith("?") or bool(_WH.match(body))
