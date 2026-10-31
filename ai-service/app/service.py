"""Orquestra: LLM -> validacao -> (1 retry) -> fallback em pt-BR. Nunca levanta para o chamador."""
import json
import logging
import re
import time
import zlib

from . import config, llm, prompts
from .validation import _EMOJI, REPLY_SCHEMA_LITE, ValidationError, looks_english, validate_reply

log = logging.getLogger("mobvoz.ai")


STANCE_BY_LEVEL = {0: "DOUBTS", 1: "SUSPECTS", 2: "BELIEVES"}


def default_belief(rel: dict) -> str:
    """Crenca inicial deterministica: credulidade (0 baixa..2 alta, definida pelo servidor) +- confianca no jogador."""
    level = int(rel.get("credulity", 1))
    trust = rel.get("trust", 0)
    if trust >= 0.5:
        level += 1
    elif trust <= -0.3:
        level -= 1
    return STANCE_BY_LEVEL[max(0, min(2, level))]


def analyze(req: dict, chat=llm.chat) -> dict:
    """Passo 1 (antes do servidor decidir): afirmacao nao verificavel + ordens em linguagem livre. Falha = nada detectado."""
    text = str(req.get("text", ""))[: config.MAX_PLAYER_TEXT].strip()
    allowed = set(c for c in req.get("capabilities", []) if isinstance(c, str))
    empty = {"claim": "", "orders": [], "ask": {"item": "", "count": 0}, "offers": [], "persuasion": {"item": "", "strength": 0}, "social": "NONE", "haggle": 0, "appeal": 0}
    if not text:
        return empty
    try:
        raw = chat(prompts.analyze_messages(text), model=req.get("model") if isinstance(req.get("model"), str) else None,
                   schema=prompts.ANALYZE_SCHEMA, options={"temperature": 0, "num_predict": 260})
        data = json.loads(raw)
        claim = data.get("claim", "")
        claim = " ".join(claim.split())[:140] if isinstance(claim, str) else ""
        orders = [o for o in data.get("orders", []) if isinstance(o, str) and o in prompts.ORDERS and o in allowed][:2]
        out = dict(empty, claim=claim if "REGISTER_CLAIM" in allowed else "", orders=list(dict.fromkeys(orders)))
        ap = data.get("appeal", 0)
        out["appeal"] = ap if isinstance(ap, int) and not isinstance(ap, bool) and 0 <= ap <= 2 else 0
        if "SOCIAL" in allowed and data.get("social") in ("THREAT", "COMFORT"):
            out["social"] = data["social"]
        if "OPEN_TRADE" in allowed:  # negociacao so se o servidor a permite; tudo saneado (o servidor ainda resolve nomes no catalogo)
            out["ask"] = _item(data.get("ask"), "count", 0, 64, zero_ok=True)
            out["offers"] = [o for o in (_item(x, "count", 1, 64) for x in data.get("offers", [])[:4] if isinstance(x, dict)) if o["item"]]
            pers = data.get("persuasion") if isinstance(data.get("persuasion"), dict) else {}
            s = pers.get("strength", 0)
            h = data.get("haggle", 0)
            out["haggle"] = h if isinstance(h, int) and not isinstance(h, bool) and 0 <= h <= 2 else 0
            out["persuasion"] = {"item": str(pers.get("item", ""))[:60].strip(),
                                 "strength": s if isinstance(s, int) and not isinstance(s, bool) and 0 <= s <= 2 else 0}
        return out
    except (llm.LlmError, ValueError, AttributeError, TypeError):
        return empty


def _thin_address(speech: str, history: list[dict], terms: list[str], spread: bool = False) -> str:
    """O tratamento ("Humano") ou o nome do jogador nao aparece em falas seguidas: se uma das 2 ultimas falas da criatura ja o usou,
    o vocativo ("Humano, ..." / "..., Bia.") sai desta (o modelo ignora pedidos de "de vez em quando"). Frase que usa o nome de outro jeito fica.
    spread: alem disso o tratamento generico (1o termo; o NOME aprendido fica) so sobrevive em ~1 de cada 3 falas (sorteio fixo pela propria fala)."""
    recent = " ".join(h.get("voce", "").lower() for h in history[-2:])
    out = speech
    drop_generic = spread and zlib.crc32(speech.encode()) % 3 != 0
    for i, t in enumerate(terms):
        if not t:
            continue
        e = re.escape(t)
        if not (i == 0 and drop_generic) and not re.search(rf"\b{e}\b", recent, re.I):
            continue
        out = re.sub(rf"^{e}[,!:]\s+", "", out, flags=re.I)
        out = re.sub(rf",\s*{e}(?=\s*[.!?]|\s*$)", "", out, flags=re.I)
        out = re.sub(rf",\s*{e}\s*,", ",", out, flags=re.I)
    out = out.strip()
    return out[:1].upper() + out[1:] if len(out) >= 3 else speech


def _repeats(speech: str, history: list[dict]) -> bool:
    """Quase igual (>= 85%) a algo que a propria criatura ja disse? O modelo pequeno as vezes copia a ultima resposta."""
    import difflib
    norm = " ".join(speech.lower().split())
    return any(difflib.SequenceMatcher(None, norm, " ".join(h.get("voce", "").lower().split())).ratio() >= 0.85
               for h in history if h.get("voce"))


def reply(req: dict, chat=llm.chat, classify=None) -> dict:
    entity_type = str(req.get("entity_type", "minecraft:cow"))[:80]
    text = str(req.get("text", ""))[: config.MAX_PLAYER_TEXT].strip()
    caps = [c for c in req.get("capabilities", []) if isinstance(c, str)][:12]
    facts = [f[:200] for f in req.get("facts", []) if isinstance(f, str)][:14]
    claims = [c[:260] for c in req.get("claims", []) if isinstance(c, str)][:8]
    rel = req.get("relation") if isinstance(req.get("relation"), dict) else {}
    rel = {k: round(float(v), 2) for k, v in rel.items() if k in ("trust", "affinity", "fear", "credulity") and isinstance(v, (int, float))}
    history = [{k: " ".join(str(v).split())[:220] for k, v in h.items() if k in ("jogador", "voce")}
               for h in req.get("history", []) if isinstance(h, dict)][-6:]
    model = req.get("model") if isinstance(req.get("model"), str) else None
    t0 = time.perf_counter()
    claim = ""
    belief0 = default_belief(rel)
    results = [r for r in req.get("order_results", []) if isinstance(r, dict)][:4]
    if "detected_claim" in req:  # o servidor ja rodou analyze(): nao repete
        claim = str(req.get("detected_claim") or "")[:140] if "REGISTER_CLAIM" in caps else ""
    elif "REGISTER_CLAIM" in caps and text:
        claim = (classify or (lambda t: analyze({"text": t, "capabilities": ["REGISTER_CLAIM"], "model": model}, chat)["claim"]))(text)
    correction = None
    for attempt in range(2):  # 1 tentativa + 1 correcao
        try:
            raw = chat(prompts.build_messages(entity_type, caps, text, facts, history, correction, claims, rel, claim, belief0, results, req.get("profile") if isinstance(req.get("profile"), dict) else None), model=model, schema=None if (claim or claims) else REPLY_SCHEMA_LITE)
            out = validate_reply(raw, caps, claim, belief0)
            prof = req.get("profile") if isinstance(req.get("profile"), dict) else {}
            out["speech_text"] = _thin_address(out["speech_text"], history, [str(prof.get("trato", "Humano")), str(prof.get("nome_jogador", ""))], spread=True)
            if _repeats(out["speech_text"], history):
                raise ValidationError("repetiu uma fala que você já disse nesta conversa; responda de outro jeito, avançando o assunto")
            out.update(source="llm", llm_ms=int((time.perf_counter() - t0) * 1000))
            return out
        except ValidationError as e:
            correction = str(e)
            log.warning("resposta invalida (tentativa %d): %s", attempt + 1, e)
        except llm.LlmError as e:
            log.warning("LLM indisponivel: %s", e)
            break
    return {"speech_text": prompts.fallback_speech(entity_type), "language": "pt-BR", "emotion": "neutro",
            "intent": "conversar", "proposals": [], "source": "fallback",
            "llm_ms": int((time.perf_counter() - t0) * 1000)}


def scene(req: dict, chat=llm.chat) -> dict:
    """Cena mob-mob curta. Linhas invalidas (ingles, longas, emoji) sao descartadas; falha = lista vazia (servidor usa fallback)."""
    label = str(req.get("label", "criatura"))[:60]
    style = str(req.get("style", ""))[:200]
    turns = max(2, min(int(req.get("max_turns", 3)), 3))
    try:
        raw = chat(prompts.scene_messages(label, style, turns), model=req.get("model") if isinstance(req.get("model"), str) else None,
                   schema=prompts.SCENE_SCHEMA, options={"temperature": 0.8, "num_predict": 200})
        lines = []
        for item in json.loads(raw).get("lines", [])[:turns]:
            text = " ".join(_clean_line(str(item.get("text", ""))).split())
            if not text or len(text) > 140 or looks_english(text):
                continue
            lines.append({"speaker": 1 if item.get("speaker") == 1 else 0, "text": text})
        return {"lines": lines if len(lines) >= 2 else []}
    except (llm.LlmError, ValueError, AttributeError, TypeError):
        return {"lines": []}


def _clean_line(t: str) -> str:
    return _EMOJI.sub("", t).replace("*", "")


def _item(raw, count_key: str, lo: int, hi: int, zero_ok: bool = False) -> dict:
    """{item, count} saneado: nome curto sem controle, quantidade limitada."""
    raw = raw if isinstance(raw, dict) else {}
    name = " ".join(str(raw.get("item", "")).split())[:60]
    c = raw.get(count_key, 0)
    c = c if isinstance(c, int) and not isinstance(c, bool) else 0
    return {"item": name, "count": 0 if (zero_ok and not name) else max(lo, min(c, hi))}


def judge(req: dict, chat=llm.chat) -> dict:
    """A IA julga (so propoe): o que a criatura aceitaria como pagamento e se teria o item pedido. Falha = {} (servidor usa as listas)."""
    profile = req.get("profile") if isinstance(req.get("profile"), dict) else {}
    accept = [" ".join(str(a).split())[:60] for a in req.get("accept", []) if isinstance(a, str) and a.strip()][:6]
    give = " ".join(str(req.get("give", "")).split())[:60]
    if not accept and not give:
        return {"accept": {}, "give": "NO"}
    try:
        raw = chat(prompts.judge_messages(profile, accept, give), model=req.get("model") if isinstance(req.get("model"), str) else None,
                   schema=prompts.JUDGE_SCHEMA, options={"temperature": 0.2, "num_predict": 200})
        data = json.loads(raw)
        said = {str(x.get("item", "")): x.get("verdict") for x in data.get("accept", []) if isinstance(x, dict)}
        out = {a: (said.get(a) if said.get(a) in prompts.VERDICTS else "NO") for a in accept}
        g = data.get("give")
        return {"accept": out, "give": g if g in prompts.VERDICTS and give else "NO"}
    except (llm.LlmError, ValueError, AttributeError, TypeError):
        return {}
