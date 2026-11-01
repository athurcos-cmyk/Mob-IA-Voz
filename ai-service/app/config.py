"""Configuracao do sidecar. Tudo local (127.0.0.1), sem servicos pagos."""
import os

HOST = "127.0.0.1"  # nunca expor fora do localhost (AGENTS.md #11)
PORT = int(os.environ.get("MOBVOZ_PORT", "8765"))
OLLAMA_URL = os.environ.get("MOBVOZ_OLLAMA_URL", "http://127.0.0.1:11434")
MODEL = os.environ.get("MOBVOZ_MODEL", "qwen3:4b")
LLM_TIMEOUT_S = float(os.environ.get("MOBVOZ_LLM_TIMEOUT", "30"))
TOKEN = os.environ.get("MOBVOZ_TOKEN", "")  # se definido, exige o header X-MobVoz-Token
MAX_BODY = 64 * 1024
MAX_PLAYER_TEXT = 500
STT_MODEL = os.environ.get("MOBVOZ_STT_MODEL", "small")  # faster-whisper: tiny|base|small|medium
STT_DEVICE = os.environ.get("MOBVOZ_STT_DEVICE", "cpu")  # GPU fica livre para o LLM
STT_COMPUTE = os.environ.get("MOBVOZ_STT_COMPUTE", "int8")
MODELS_DIR = os.environ.get("MOBVOZ_MODELS", os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "models"))
MAX_AUDIO = 48000 * 2 * 20  # 20 s de PCM 48 kHz 16-bit
MAX_TTS_CHARS = 400
TTS_ENGINE = os.environ.get("MOBVOZ_TTS", "kokoro").lower()  # "kokoro" (mais natural, mais pesado) | "piper" (modo leve)

# --- IA propria (opcional): so vale se o usuario escolher. Padrao = Ollama local, nada sai da maquina. ---
PROVIDER = os.environ.get("MOBVOZ_PROVIDER", "ollama").lower()  # "ollama" | "api"
API_URL = os.environ.get("MOBVOZ_API_URL", "https://api.deepseek.com")
API_MODEL = os.environ.get("MOBVOZ_API_MODEL", "deepseek-flash")


def api_key() -> str:
    """Chave da API: variavel MOBVOZ_API_KEY ou ~/.mobvoz/api.json ({"key": "..."}). Nunca no repositorio, nunca em log."""
    k = os.environ.get("MOBVOZ_API_KEY", "").strip()
    if k:
        return k
    try:
        import json
        with open(os.path.join(os.path.expanduser("~"), ".mobvoz", "api.json"), encoding="utf-8-sig") as f:  # -sig: o PowerShell 5 grava o arquivo com BOM
            return str(json.load(f).get("key", "")).strip()
    except (OSError, ValueError):
        return ""

