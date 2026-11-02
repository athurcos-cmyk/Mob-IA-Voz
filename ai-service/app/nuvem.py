"""Voz na nuvem (Azure Speech), OPCIONAL: so liga se o dono do PC guardou a propria chave (configurar-voz-nuvem.ps1) e escolheu `voiceEngine: nuvem`.
Qualquer falha (sem chave, sem internet, limite do dia) devolve None e quem chamou usa o Kokoro: uma fala nunca fica muda.

Gasto: Azure cobra por caractere do texto. O limite diario (padrao 15.000 = cabe nos 500 mil/mes gratis do tier F0) fica em ~/.mobvoz/azure_uso.json.
Trava dura: MOBVOZ_NO_CLOUD=1 desliga a nuvem por completo (testes e baterias automaticas SEMPRE rodam assim)."""
import json
import logging
import math
import os
import threading
import time
import urllib.error
import urllib.request
import zlib
from datetime import date
from xml.sax.saxutils import escape

log = logging.getLogger("mobvoz.nuvem")
RATE = 24000  # raw-24khz-16bit-mono-pcm
DAILY_CHARS = int(os.environ.get("MOBVOZ_NUVEM_MAX_CHARS", "15000"))
# vozes neurais pt-BR comuns (preco padrao); a lista real da conta, se vier, filtra esta
FEMALE = ["Francisca", "Brenda", "Elza", "Giovanna", "Leila", "Leticia", "Manuela", "Thalita", "Yara"]
MALE = ["Antonio", "Donato", "Fabio", "Humberto", "Julio", "Nicolau", "Valerio"]
REF_PITCH = {"F": 1.2, "M": 0.95}  # tom "normal" de cada genero na escala de vozes.py (Kokoro): o desvio disso vira semitons

_lock = threading.Lock()
_available: dict = {}
_down_until = 0.0
_fails = 0
_warned: set = set()


def _home() -> str:
    return os.environ.get("MOBVOZ_HOME") or os.path.join(os.path.expanduser("~"), ".mobvoz")


def credentials() -> tuple[str, str]:
    """(chave, regiao): variaveis MOBVOZ_AZURE_KEY/REGION ou ~/.mobvoz/azure.json. Nunca no repositorio, nunca em log."""
    key, region = os.environ.get("MOBVOZ_AZURE_KEY", "").strip(), os.environ.get("MOBVOZ_AZURE_REGION", "").strip()
    if not key:
        try:
            with open(os.path.join(_home(), "azure.json"), encoding="utf-8-sig") as f:
                d = json.load(f)
            key, region = str(d.get("key", "")).strip(), str(d.get("region", "")).strip()
        except (OSError, ValueError):
            pass
    return key, region.lower().replace(" ", "")


def enabled() -> bool:
    return os.environ.get("MOBVOZ_NO_CLOUD", "") not in ("1", "true", "yes") and all(credentials()) and time.time() >= _down_until


def _warn_once(tag: str, msg: str) -> None:
    if tag not in _warned:
        _warned.add(tag)
        log.warning(msg)


def _budget_file() -> str:
    return os.path.join(_home(), "azure_uso.json")


def _spend(chars: int) -> bool:
    """Reserva `chars` do limite de hoje; False (sem gastar) se estourar."""
    today = date.today().isoformat()
    with _lock:
        used = 0
        try:
            with open(_budget_file(), encoding="utf-8") as f:
                d = json.load(f)
            used = int(d["chars"]) if d.get("date") == today else 0
        except (OSError, ValueError, KeyError, TypeError):
            pass
        if used + chars > DAILY_CHARS:
            _warn_once("limite" + today, f"limite diario da voz na nuvem atingido ({DAILY_CHARS} caracteres); o resto do dia usa a voz local")
            return False
        try:
            os.makedirs(_home(), exist_ok=True)
            with open(_budget_file(), "w", encoding="utf-8") as f:
                json.dump({"date": today, "chars": used + chars}, f)
        except OSError:
            pass
        return True


def _request(url: str, key: str, body: bytes | None = None, headers: dict | None = None, timeout: float = 10.0) -> bytes:
    req = urllib.request.Request(url, data=body, headers={"Ocp-Apim-Subscription-Key": key, **(headers or {})})
    with urllib.request.urlopen(req, timeout=timeout) as r:
        return r.read()


def voices(key: str, region: str) -> dict:
    """{'F': [...], 'M': [...]} com os nomes completos (pt-BR-XNeural) que a conta realmente tem; a lista fixa se a consulta falhar (a consulta e gratis)."""
    ck = (key[:6], region)
    if ck not in _available:
        found = {"F": [f"pt-BR-{n}Neural" for n in FEMALE], "M": [f"pt-BR-{n}Neural" for n in MALE]}
        try:
            data = json.loads(_request(f"https://{region}.tts.speech.microsoft.com/cognitiveservices/voices/list", key))
            real = {v["ShortName"]: v.get("Gender", "")[:1] for v in data if v.get("Locale") == "pt-BR"}
            for g in ("F", "M"):
                ok = [n for n in found[g] if real.get(n) == g]
                found[g] = ok or [n for n, gg in real.items() if gg == g and n.endswith("Neural") and "Multilingual" not in n]
        except Exception as e:  # noqa: BLE001 - fica com a lista fixa
            log.info("lista de vozes da nuvem indisponivel (%s); usando a lista padrao", e)
        _available[ck] = found
    return _available[ck]


def choose(plan, species: str, seed: int, names: dict) -> tuple[str, float, float]:
    """(voz, semitons, velocidade) da criatura: genero pela mistura da especie, voz entre as do genero pelo individuo, tom/velocidade do plano (vozes.py)."""
    w = dict(plan.mix)
    g = "F" if w.get("pf_dora", 0) > 0.5 else "M"
    pool = names.get(g) or names.get("F" if g == "M" else "M") or []
    if not pool:
        return "", 0.0, 1.0
    sp = species.replace("minecraft:", "").split("#")[0]
    pick = pool[zlib.crc32(f"{sp}|{int(seed) % 64}".encode()) % len(pool)]
    semitones = max(-8.0, min(8.0, 12 * math.log2(plan.pitch / REF_PITCH[g])))
    return pick, semitones, plan.speed


def ssml(text: str, voice: str, semitones: float, speed: float) -> str:
    rate = max(-30, min(40, round((speed - 1) * 100)))
    return (f"<speak version='1.0' xml:lang='pt-BR' xmlns='http://www.w3.org/2001/10/synthesis'><voice name='{voice}'>"
            f"<prosody pitch='{semitones:+.1f}st' rate='{rate:+d}%'>{escape(text)}</prosody></voice></speak>")


def synthesize(text: str, plan, species: str = "", seed: int = 0) -> bytes | None:
    """PCM int16 mono 24 kHz, ou None (o chamador usa a voz local). Nunca levanta."""
    global _down_until, _fails
    if not enabled() or not text.strip():
        return None
    key, region = credentials()
    voice, st, speed = choose(plan, species, seed, voices(key, region))
    if not voice or not _spend(len(text)):
        return None
    try:
        pcm = _request(f"https://{region}.tts.speech.microsoft.com/cognitiveservices/v1", key, ssml(text, voice, st, speed).encode("utf-8"),
                       {"Content-Type": "application/ssml+xml", "X-Microsoft-OutputFormat": "raw-24khz-16bit-mono-pcm", "User-Agent": "MobVozIA"})
        _fails = 0
        return pcm if len(pcm) > 200 else None
    except urllib.error.HTTPError as e:
        _warn_once(f"http{e.code}", f"a voz na nuvem recusou o pedido (HTTP {e.code}): "
                   + ("chave ou regiao erradas; rode de novo o configurar-voz-nuvem" if e.code in (401, 403) else "usando a voz local por enquanto"))
    except Exception as e:  # noqa: BLE001 - sem internet etc.
        _warn_once("rede", f"sem resposta da voz na nuvem ({type(e).__name__}); usando a voz local por enquanto")
    _fails += 1
    if _fails >= 3:  # nao fica esperando o tempo limite a cada fala
        _down_until, _fails = time.time() + 300, 0
    return None
