"""Validacao estrita da resposta do LLM. O LLM so PROPOE; nada aqui executa coisa alguma."""
import json
import re

MAX_SPEECH = 360
EMOTIONS = ["neutro", "curioso", "alegre", "cauteloso", "irritado", "triste", "desconfiado", "hostil", "calculista", "medroso"]
INTENTS = ["conversar", "negociar", "recusar", "aceitar", "ameaçar", "ajudar", "pedir", "avisar", "fofocar", "planejar"]
# Catalogo global conhecido. As capacidades REAIS de cada pedido vem do servidor (request.capabilities).
KNOWN_ACTIONS = ["NONE", "EMOTE", "FOLLOW_PLAYER", "STAY_AT", "PORTAL_HOLD", "DISMISS_ALLY", "DEFEND_PLAYER",
                 "CEASE_HOSTILITY", "OFFER_TRADE", "ACCEPT_OFFER", "REGISTER_CLAIM", "UPDATE_BELIEF"]

STANCES = ["UNKNOWN", "SUSPECTS", "BELIEVES", "DOUBTS", "DISPROVED"]
MAX_CLAIM_TEXT = 140
MAX_CLAIMS = 8
FULL_REQUIRED = ["speech_text", "language", "emotion", "intent", "proposals"]
REQUIRED = ["speech_text", "emotion"]  # o resto tem padrao (resposta enxuta = menos tokens = mais rapido)
# Campos de alegacao/crenca (fase 5). Opcionais na validacao; o schema do Ollama os exige para forcar o preenchimento.
OPTIONAL = ["player_claim", "belief", "claim_update_index", "claim_update_stance", "language", "intent", "proposals"]

# Resposta enxuta: so a fala e a emocao (usada quando nao ha alegacao do jogador para julgar). Cerca de 35 tokens a menos por resposta.
REPLY_SCHEMA_LITE = {
    "type": "object",
    "required": ["speech_text", "emotion"],
    "properties": {
        "speech_text": {"type": "string"},
        "emotion": {"type": "string", "enum": EMOTIONS},
    },
}

# JSON Schema enviado ao Ollama (saida estruturada). A validacao de verdade e validate_reply.
REPLY_SCHEMA = {
    "type": "object",
    "required": FULL_REQUIRED + ["player_claim", "belief", "claim_update_index", "claim_update_stance"],
    "properties": {
        "belief": {"type": "string", "enum": STANCES},
        "claim_update_index": {"type": "integer"},
        "claim_update_stance": {"type": "string", "enum": STANCES},
        "speech_text": {"type": "string"},
        "language": {"type": "string", "enum": ["pt-BR"]},
        "emotion": {"type": "string", "enum": EMOTIONS},
        "intent": {"type": "string", "enum": INTENTS},
        "proposals": {
            "type": "array",
            "maxItems": 3,
            "items": {
                "type": "object",
                "required": ["type"],
                "properties": {
                    "type": {"type": "string", "enum": KNOWN_ACTIONS},
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "text": {"type": "string"},
                            "stance": {"type": "string", "enum": STANCES},
                            "claim_index": {"type": "integer"},
                        },
                    },
                },
            },
        },
    },
}

_EN = set("the you and are is what that this with have for your not but can how why where who want like yes hello hi i'm it's don't do does".split())
_PT = set("o a os as um uma de do da dos das em no na que não nao eu você voce vc sim olá ola para por com mas como muito bem é e se meu minha seu sua gosto quero está esta tenho foi são".split())


_EMOJI = re.compile("[🀀-🫿☀-➿️‍]")


class ValidationError(ValueError):
    pass


# O modelo pequeno as vezes erra uma palavra para um palavrao ("fucar" -> "foder"). Jogo para todas as idades: rejeita e pede outra fala.
_PROFANITY = re.compile(r"\b(foder|fode|foda|fodido|porra|caralho|merda|puta|putaria|buceta|cu|viado|cacete|bosta|desgraca|desgraça)\b", re.IGNORECASE)


def looks_english(text: str) -> bool:
    words = re.findall(r"[a-zà-ÿ']+", text.lower())
    en = sum(w in _EN for w in words)
    pt = sum(w in _PT for w in words)
    return en >= 2 and en > pt


def validate_reply(raw: str, capabilities: list[str], detected_claim: str = "", default_belief: str = "UNKNOWN") -> dict:
    """Converte o texto cru do LLM em resposta valida, ou levanta ValidationError."""
    try:
        data = json.loads(raw)
    except (TypeError, json.JSONDecodeError) as e:
        raise ValidationError(f"nao e JSON: {e}")
    if not isinstance(data, dict):
        raise ValidationError("raiz nao e objeto")
    extra = set(data) - set(REQUIRED) - set(OPTIONAL)
    if extra:
        raise ValidationError(f"campos desconhecidos: {sorted(extra)}")
    for k in REQUIRED:
        if k not in data:
            raise ValidationError(f"falta campo {k}")
    speech = data["speech_text"]
    if not isinstance(speech, str) or not speech.strip():
        raise ValidationError("speech_text vazio")
    speech = " ".join(_EMOJI.sub("", speech).replace("*", "").split())  # o TTS nao deve falar emoji/markdown
    speech = re.sub(r"\s*[—–]\s*", ", ", speech).strip(" ,")  # travessao vira pausa (o TTS o le mal)
    if not speech:
        raise ValidationError("speech_text vazio")
    if len(speech) > MAX_SPEECH:
        raise ValidationError("speech_text longo demais")
    if data.get("language", "pt-BR") != "pt-BR":
        raise ValidationError("language precisa ser pt-BR")
    if looks_english(speech):
        raise ValidationError("fala parece estar em ingles")
    if _PROFANITY.search(speech):
        raise ValidationError("a fala tem palavrao; troque por uma palavra adequada para todas as idades")
    if data["emotion"] not in EMOTIONS:
        raise ValidationError("emotion invalida")
    if data.get("intent", "conversar") not in INTENTS:
        raise ValidationError("intent invalida")
    proposals = data.get("proposals", [])
    if not isinstance(proposals, list) or len(proposals) > 3:
        raise ValidationError("proposals invalido")
    allowed = set(capabilities)
    clean = []
    for p in proposals:
        if not isinstance(p, dict) or not isinstance(p.get("type"), str):
            raise ValidationError("proposal malformada")
        if p["type"] not in KNOWN_ACTIONS:
            raise ValidationError(f"acao desconhecida: {p['type'][:30]}")
        # Acao conhecida mas sem capacidade neste pedido: descartada, nunca repassada.
        if p["type"] in allowed:
            params = _clean_params(p["type"], p.get("parameters"))
            if params is not None:  # parametros invalidos: descarta so esta proposta, nao a fala
                clean.append({"type": p["type"], "parameters": params})
    clean += _claim_proposals(data, allowed, detected_claim, default_belief)
    return {"speech_text": speech, "language": "pt-BR", "emotion": data["emotion"],
            "intent": data.get("intent", "conversar"), "proposals": clean[:3]}


def _clean_params(action: str, raw) -> dict | None:
    """Parametros estritos por acao; so as chaves conhecidas passam. None = proposta invalida."""
    raw = raw if isinstance(raw, dict) else {}
    if action == "REGISTER_CLAIM":
        text = raw.get("text")
        if not isinstance(text, str) or len(text.strip()) < 3:
            return None
        stance = raw.get("stance", "UNKNOWN")
        return {"text": " ".join(text.split())[:MAX_CLAIM_TEXT], "stance": stance if stance in STANCES else "UNKNOWN"}
    if action == "UPDATE_BELIEF":
        idx, stance = raw.get("claim_index"), raw.get("stance")
        if isinstance(idx, bool) or not isinstance(idx, int) or not 0 <= idx < MAX_CLAIMS or stance not in STANCES:
            return None
        return {"claim_index": idx, "stance": stance}
    return {}


def _claim_proposals(data: dict, allowed: set, detected_claim: str = "", default_belief: str = "UNKNOWN") -> list[dict]:
    """Campos de alegacao do LLM -> propostas REGISTER_CLAIM/UPDATE_BELIEF (so se o servidor permitiu)."""
    out = []
    claim = detected_claim or ""  # a alegacao vem do classificador; o LLM principal so decide a crenca
    if "REGISTER_CLAIM" in allowed and isinstance(claim, str) and claim.strip():
        stance = data.get("belief")  # o LLM pode refinar; UNKNOWN/ausente = usa a crenca inicial do servidor
        stance = stance if stance in STANCES and stance != "UNKNOWN" else default_belief
        p = _clean_params("REGISTER_CLAIM", {"text": claim, "stance": stance})
        if p is not None:
            out.append({"type": "REGISTER_CLAIM", "parameters": p})
    idx = data.get("claim_update_index")
    if "UPDATE_BELIEF" in allowed and isinstance(idx, int) and not isinstance(idx, bool) and idx >= 0:
        p = _clean_params("UPDATE_BELIEF", {"claim_index": idx, "stance": data.get("claim_update_stance")})
        if p is not None:
            out.append({"type": "UPDATE_BELIEF", "parameters": p})
    return out
