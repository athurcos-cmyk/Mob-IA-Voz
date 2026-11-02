"""STT (faster-whisper) e TTS: Kokoro (principal, mais natural) ou Piper (modo leve). Carregamento preguicoso; tudo local e na CPU.

Formato de audio do jogo (Simple Voice Chat): PCM 16-bit little-endian, mono, 48 kHz.
Audio de entrada nunca e gravado em disco nem logado.
"""
import logging
import re
import threading
from pathlib import Path

import numpy as np

from . import config

SVC_RATE = 48000
WHISPER_RATE = 16000

_lock = threading.Lock()
_whisper = None
_voices: dict = {}
_kokoro = None
_warned = False
log = logging.getLogger("mobvoz.tts")

KOKORO_FILES = ("kokoro-v1.0.int8.onnx", "voices-v1.0.bin")
# tom natural de cada voz do Kokoro; o que sobra do tom do perfil vira um deslocamento leve (individualidade), sem estragar a voz
KOKORO_CENTER = {"pm_santa": 0.7, "pm_alex": 0.95, "pf_dora": 1.2}


def pcm48_to_16k(pcm: bytes) -> np.ndarray:
    """int16 48 kHz -> float32 16 kHz com filtro anti-aliasing (polifasico)."""
    from scipy.signal import resample_poly
    a = np.frombuffer(pcm[: len(pcm) // 2 * 2], dtype="<i2").astype(np.float32) / 32768.0
    return resample_poly(a, 1, 3).astype(np.float32)


_whispers: dict = {}


def _whisper_model(name: str, local_only: bool = False):
    """Carrega (uma vez) um modelo do Whisper. Poucos nucleos (STT_THREADS) para nao disputar CPU com o jogo."""
    if name not in _whispers:
        from faster_whisper import WhisperModel
        _whispers[name] = WhisperModel(name, device=config.STT_DEVICE, compute_type=config.STT_COMPUTE, cpu_threads=config.STT_THREADS,
                                       local_files_only=local_only)
    return _whispers[name]


def _run_whisper(model, audio) -> tuple[str, float, str]:
    """(texto, confianca media em log-prob, idioma). Confianca alta = proxima de 0; abaixo de STT_MIN_CONF o modelo rapido ficou em duvida."""
    segments, info = model.transcribe(audio, language="pt", beam_size=1, vad_filter=True, condition_on_previous_text=False, initial_prompt=config.STT_HINT or None)
    segs = list(segments)
    text = " ".join(s.text.strip() for s in segs).strip()
    conf = sum(s.avg_logprob for s in segs) / len(segs) if segs else -9.0
    return text, conf, info.language


def transcribe(pcm48: bytes) -> dict:
    """Modelo RAPIDO primeiro (base: ~0,8 s); so se ele ficar em duvida (vazio ou pouco confiante) tenta o MAIOR (small), se estiver instalado."""
    with _lock:  # um modelo, uma transcricao por vez (CPU)
        audio = pcm48_to_16k(pcm48)
        text, conf, lang = _run_whisper(_whisper_model(config.STT_MODEL), audio)
        seconds = len(audio) / WHISPER_RATE
        if config.STT_FALLBACK and config.STT_FALLBACK != config.STT_MODEL and seconds >= 0.6 and (not text or conf < config.STT_MIN_CONF):
            try:
                t2, c2, lang2 = _run_whisper(_whisper_model(config.STT_FALLBACK, local_only=True), audio)
                if t2 and (not text or c2 > conf):
                    text, lang = t2, lang2
            except Exception as e:  # modelo grande nao instalado: segue com o rapido
                log.info("reserva de fala (%s) indisponivel: %s", config.STT_FALLBACK, e)
    return {"text": text, "duration_s": round(seconds, 2), "language": lang}


def _voice(name: str):
    if name not in _voices:
        from piper import PiperVoice
        path = Path(config.MODELS_DIR) / f"{name}.onnx"
        if not path.exists():
            raise FileNotFoundError(f"voz nao instalada: {name}")
        _voices[name] = PiperVoice.load(path)
    return _voices[name]


def resample(a: np.ndarray, src: int, dst: int) -> np.ndarray:
    if src == dst:
        return a
    from scipy.signal import resample_poly
    from math import gcd
    g = gcd(src, dst)
    return resample_poly(a, dst // g, src // g)


def kokoro_voice(piper_voice: str, pitch: float) -> tuple:
    """Voz do Kokoro + deslocamento de tom (0,92 a 1,08) para o perfil: grave -> santa, agudo -> dora, o resto por homem/mulher da voz Piper."""
    if pitch <= 0.8:
        v = "pm_santa"
    elif pitch >= 1.12:
        v = "pf_dora"
    else:
        v = "pf_dora" if "faber" in piper_voice else "pm_alex"
    return v, max(0.92, min(pitch / KOKORO_CENTER[v], 1.08))


def kokoro_available() -> bool:
    d = Path(config.MODELS_DIR)
    if not all((d / f).exists() for f in KOKORO_FILES):
        return False
    try:
        import kokoro_onnx  # noqa: F401
    except ImportError:
        return False
    return True


def active_engine(requested: str | None = None) -> str:
    """'nuvem' (so se pedida e configurada), 'kokoro' ou 'piper'. Kokoro e o padrao; sem os modelos/pacote instalados, cai para o Piper (modo leve) e avisa uma vez."""
    global _warned
    want = (requested or config.TTS_ENGINE or "kokoro").lower()
    if want == "nuvem":
        from . import nuvem
        if nuvem.enabled():
            return "nuvem"
        want = "kokoro"  # sem chave / limite / MOBVOZ_NO_CLOUD: voz local
    if want == "piper":
        return "piper"
    if kokoro_available():
        return "kokoro"
    if not _warned:
        _warned = True
        log.warning("Kokoro indisponivel (rode baixar_kokoro.py e instale kokoro-onnx); usando o Piper")
    return "piper"


def _kokoro_model():
    global _kokoro
    if _kokoro is None:
        from kokoro_onnx import Kokoro
        d = Path(config.MODELS_DIR)
        _kokoro = Kokoro(str(d / KOKORO_FILES[0]), str(d / KOKORO_FILES[1]))
    return _kokoro


def _to_pcm48(audio: np.ndarray, src_rate: int) -> bytes:
    out = resample(audio, src_rate, SVC_RATE)
    return np.clip(out, -32768, 32767).astype("<i2").tobytes()


_WORD = re.compile(r"[A-Za-zÀ-ÿ]+")


def _sound_word(w: str) -> str | None:
    """Sons esticados que o leitor de voz SOLETRA ('Zzzz' vira 'ex-ex-ex-e'): devolve a palavra falada que os substitui, '' para sumir, ou None se nao e um deles."""
    low = w.lower()
    if re.fullmatch(r"b?z{2,}", low):
        return "Zum"  # zumbido de abelha
    if re.fullmatch(r"(?:s{2,}h*|sh{2,}|ps{2,})", low):
        return ""  # sibilo (aranha, creeper): na fala escrita fica bom, falado vira letra repetida
    if re.fullmatch(r"(?:g?r{3,}|b?r{3,}|gr{2,}|br{2,})", low):
        return ""  # rosnado / tremor
    if re.fullmatch(r"h?m{3,}", low):
        return "Hmm"
    return None


def clean_for_speech(text: str) -> str:
    """Texto -> o que e FALADO (a legenda continua o texto original). Tira sons esticados que viram soletracao (zzz, sss, grrr, shhh) e reduz letras esticadas dentro de palavras."""
    def fix(m: re.Match) -> str:
        w = m.group(0)
        s = _sound_word(w)
        if s is not None:
            return s if not w[0].isupper() else s[:1].upper() + s[1:]
        return re.sub(r"([bcdfghjklmnpqrstvwxzBCDFGHJKLMNPQRSTVWXZ])\1{2,}", r"\1", w)  # 'sssó' -> 'só'

    out = _WORD.sub(fix, text)
    out = re.sub(r"\s+([,.!?…;:])", r"\1", out)
    out = re.sub(r"([,;:])(?:\s*[,;:])+", r"\1", out)
    out = re.sub(r"^[\s,.;:…!?-]+", "", out)
    out = re.sub(r"\s{2,}", " ", out).strip()
    if out and text[:1].isupper():
        out = out[:1].upper() + out[1:]  # o 1o som saiu: a frase que sobrou comeca com maiuscula
    return out or "Hmm."  # so havia o som: fala um 'Hmm' curto em vez de silencio


def _fx_etereo(x: np.ndarray, rate: int) -> np.ndarray:
    """Efeito do Vex (o unico que o dono aprovou): tremolo suave + eco curto."""
    t = np.arange(len(x)) / rate
    x = x * (0.8 + 0.2 * (0.5 + 0.5 * np.sin(2 * np.pi * 9 * t)))
    n = int(rate * 1.1)
    ir = np.random.default_rng(7).standard_normal(n) * np.exp(-np.linspace(0, 6, n))
    ir[0] = 0
    y = np.convolve(x, ir / np.abs(ir).sum() * 4)[: len(x) + n]
    out = np.zeros(len(y), dtype=np.float32)
    out[: len(x)] += x * 0.6
    return (out + y * 0.4).astype(np.float32)


def _synthesize_kokoro_plan(text: str, pl) -> bytes:
    """Voz da especie (mistura das 3 vozes) com a variacao do individuo e a emocao: ver vozes.plan."""
    k = _kokoro_model()
    style = sum(k.get_voice_style(v) * w for v, w in pl.mix)
    with _lock:
        samples, rate = k.create(text, voice=style.astype(np.float32), speed=pl.speed / pl.pitch, lang="pt-br")
    audio = samples.astype(np.float32)
    shifted = int(rate * pl.pitch)  # o tom vem de reinterpretar a taxa; a velocidade ja foi dividida por ele, entao a duracao final nao muda
    if pl.fx == "etereo":
        audio = _fx_etereo(audio, shifted)
    return _to_pcm48(audio * 32767.0, shifted)


def synthesize(text: str, voice: str = "pt_BR-faber-medium", pitch: float = 1.0, speed: float = 1.0, engine: str | None = None,
               species: str = "", seed: int = 0, emotion: str = "") -> bytes:
    """Texto pt-BR -> PCM int16 mono 48 kHz. `voice`/`pitch`/`speed` vem do perfil (vozes Piper); o Kokoro as traduz.
    Com `species` (e `seed` do individuo, `emotion` da fala) o Kokoro usa a mistura de vozes da especie (ver vozes.py)."""
    text = clean_for_speech(text)
    eng = active_engine(engine)
    if eng == "nuvem":
        from . import nuvem, vozes
        raw = nuvem.synthesize(text, vozes.plan(species or "minecraft:villager", seed, emotion, pitch, voice), species, seed)
        if raw:
            return _to_pcm48(np.frombuffer(raw[: len(raw) // 2 * 2], dtype="<i2").astype(np.float32), nuvem.RATE)
        eng = active_engine("kokoro")  # a nuvem nao respondeu (ou estourou o limite do dia): esta fala sai na voz local
    if eng == "kokoro":
        try:
            if species:
                from . import vozes
                return _synthesize_kokoro_plan(text, vozes.plan(species, seed, emotion, pitch, voice))
            return _synthesize_kokoro(text, voice, pitch, speed)
        except Exception as e:  # noqa: BLE001 - uma fala nunca fica muda por falha do motor principal
            log.warning("Kokoro falhou (%s); esta fala usa o Piper", e)
    return _synthesize_piper(text, voice, pitch, speed)


def _synthesize_kokoro(text: str, voice: str, pitch: float, speed: float) -> bytes:
    v, shift = kokoro_voice(voice, pitch)
    with _lock:
        samples, rate = _kokoro_model().create(text, voice=v, speed=max(0.7, min(speed, 1.3)) / shift, lang="pt-br")
    # o deslocamento de tom reinterpreta a taxa (muda tom e duracao); a velocidade ja foi dividida por ele, entao a duracao final nao muda
    return _to_pcm48(samples.astype(np.float32) * 32767.0, int(rate * shift))


def _synthesize_piper(text: str, voice: str, pitch: float, speed: float) -> bytes:
    from piper import SynthesisConfig
    p = max(0.6, min(pitch, 1.6))
    with _lock:
        v = _voice(voice)
        # reinterpretar a taxa muda tom e duracao juntos: o length_scale compensa, e a duracao passa a depender so de `speed`
        chunks = list(v.synthesize(text, SynthesisConfig(length_scale=p / max(speed, 0.5))))
    audio = np.concatenate([c.audio_int16_array for c in chunks]).astype(np.float32)
    return _to_pcm48(audio, int(v.config.sample_rate * p))
