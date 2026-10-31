"""Clientes de LLM: Ollama local (padrao) ou uma API compativel com OpenAI (ex.: DeepSeek) escolhida EXPLICITAMENTE pelo usuario.
Chamado apenas de threads do sidecar, nunca no tick do jogo. A chave de API nunca e logada nem gravada por este codigo."""
import json
import urllib.error
import urllib.request

from . import config
from .validation import REPLY_SCHEMA


class LlmError(RuntimeError):
    pass


def chat_ollama(messages: list[dict], model: str | None = None, timeout: float | None = None, schema: dict | None = None,
                options: dict | None = None) -> str:
    body = {
        "model": model or config.MODEL,
        "messages": messages,
        "stream": False,
        "think": False,
        "format": schema or REPLY_SCHEMA,
        "keep_alive": "10m",
        "options": options or {"temperature": 0.7, "num_predict": 260},
    }
    req = urllib.request.Request(config.OLLAMA_URL + "/api/chat", data=json.dumps(body).encode(),
                                 headers={"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(req, timeout=timeout or config.LLM_TIMEOUT_S) as r:
            return json.load(r)["message"]["content"]
    except Exception as e:  # rede, timeout, JSON, modelo ausente...
        raise LlmError(str(e)) from e


def chat_api(messages: list[dict], model: str | None = None, timeout: float | None = None, schema: dict | None = None,
             options: dict | None = None) -> str:
    """API compativel com OpenAI (DeepSeek etc.). Modo JSON simples: o formato vai descrito no prompt e a validacao continua no servidor.
    O raciocinio interno e desligado (senao a fala demora e e cortada)."""
    key = config.api_key()
    if not key:
        raise LlmError("provedor 'api' sem chave (defina MOBVOZ_API_KEY ou ~/.mobvoz/api.json)")
    opts = options or {"temperature": 0.7, "num_predict": 260}
    msgs = [dict(m) for m in messages]
    hint = "\n\nResponda com UM objeto JSON (somente JSON) que siga este JSON Schema: " + json.dumps(schema or REPLY_SCHEMA, ensure_ascii=False)
    if msgs and msgs[0].get("role") == "system":
        msgs[0]["content"] += hint
    else:
        msgs.insert(0, {"role": "system", "content": hint.strip()})
    body = {
        "model": model or config.API_MODEL,
        "messages": msgs,
        "response_format": {"type": "json_object"},
        "temperature": opts.get("temperature", 0.7),
        "max_tokens": max(opts.get("num_predict", 260), 200),
        "thinking": {"type": "disabled"},
        "stream": False,
    }
    req = urllib.request.Request(config.API_URL.rstrip("/") + "/chat/completions", data=json.dumps(body).encode(),
                                 headers={"Content-Type": "application/json", "Authorization": "Bearer " + key})
    try:
        with urllib.request.urlopen(req, timeout=timeout or config.LLM_TIMEOUT_S) as r:
            return json.load(r)["choices"][0]["message"]["content"]
    except urllib.error.HTTPError as e:
        raise LlmError(f"API respondeu HTTP {e.code}") from e  # sem corpo/headers: nada de chave em log
    except Exception as e:
        raise LlmError(type(e).__name__) from e


def chat(messages: list[dict], model: str | None = None, timeout: float | None = None, schema: dict | None = None,
         options: dict | None = None) -> str:
    """Provedor escolhido em MOBVOZ_PROVIDER ('ollama' = local, padrao; 'api' = API propria do usuario)."""
    fn = chat_api if config.PROVIDER == "api" else chat_ollama
    # 'model' vindo do jogo e do Ollama; na API vale sempre o modelo configurado
    return fn(messages, None if config.PROVIDER == "api" else model, timeout, schema, options)


def list_models() -> list[str]:
    try:
        with urllib.request.urlopen(config.OLLAMA_URL + "/api/tags", timeout=3) as r:
            return [m["name"] for m in json.load(r)["models"]]
    except Exception:
        return []
