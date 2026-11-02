"""HTTP local: GET /health, POST /v1/reply. Liga so em 127.0.0.1."""
import json
import os
import sys
import time
import threading
import logging
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

from . import config, llm, service, speech


class Handler(BaseHTTPRequestHandler):
    def _send(self, code: int, obj: dict) -> None:
        data = json.dumps(obj, ensure_ascii=False).encode()
        self.send_response(code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def _authorized(self) -> bool:
        return not config.TOKEN or self.headers.get("X-MobVoz-Token") == config.TOKEN

    def do_GET(self):
        if self.path == "/health":
            models = llm.list_models()
            self._send(200, {"status": "ok", "model": config.MODEL, "ollama": bool(models), "models": models, "tts": speech.active_engine()})
        else:
            self._send(404, {"error": "not_found"})

    def _send_raw(self, data: bytes, ctype: str, extra: dict | None = None) -> None:
        self.send_response(200)
        self.send_header("Content-Type", ctype)
        self.send_header("Content-Length", str(len(data)))
        for k, v in (extra or {}).items():
            self.send_header(k, v)
        self.end_headers()
        self.wfile.write(data)

    def do_POST(self):
        if self.path not in ("/v1/reply", "/v1/analyze", "/v1/scene", "/v1/judge", "/v1/transcribe", "/v1/synthesize"):
            return self._send(404, {"error": "not_found"})
        if not self._authorized():
            return self._send(401, {"error": "unauthorized"})
        if self.path == "/v1/transcribe":
            return self._transcribe()
        if self.path == "/v1/synthesize":
            return self._synthesize()
        try:
            n = int(self.headers.get("Content-Length", "0"))
            if n <= 0 or n > config.MAX_BODY:
                return self._send(413, {"error": "body_size"})
            req = json.loads(self.rfile.read(n))
            if not isinstance(req, dict) or (self.path not in ("/v1/scene", "/v1/judge") and not isinstance(req.get("text"), str)):
                return self._send(400, {"error": "bad_request"})
        except (ValueError, json.JSONDecodeError):
            return self._send(400, {"error": "bad_json"})
        handler = {"/v1/analyze": service.analyze, "/v1/scene": service.scene, "/v1/judge": service.judge}.get(self.path, service.reply)
        self._send(200, handler(req))

    def _transcribe(self):
        """Corpo = PCM int16 LE mono 48 kHz cru. Devolve {text}. Audio descartado apos o uso."""
        n = int(self.headers.get("Content-Length", "0"))
        if n <= 0 or n > config.MAX_AUDIO:
            return self._send(413, {"error": "audio_size"})
        try:
            self._send(200, speech.transcribe(self.rfile.read(n)))
        except Exception as e:  # modelo ausente, etc.: o jogo segue sem travar
            logging.getLogger("mobvoz.stt").warning("falha no STT: %s", e)
            self._send(503, {"error": "stt_unavailable"})

    def _synthesize(self):
        """JSON {text, voice, pitch, speed, engine?} -> PCM int16 LE mono 48 kHz."""
        try:
            n = int(self.headers.get("Content-Length", "0"))
            if n <= 0 or n > config.MAX_BODY:
                return self._send(413, {"error": "body_size"})
            req = json.loads(self.rfile.read(n))
            text = str(req["text"]).strip()[: config.MAX_TTS_CHARS]
            if not text:
                return self._send(400, {"error": "empty_text"})
            voice = str(req.get("voice", "pt_BR-faber-medium"))
            if not voice.replace("-", "").replace("_", "").isalnum():  # sem path traversal
                return self._send(400, {"error": "bad_voice"})
            engine = req.get("engine") if req.get("engine") in ("kokoro", "piper") else None
            species = str(req.get("species", ""))[:60]
            if species and not species.replace(":", "").replace("_", "").replace("#", "").replace("-", "").replace(".", "").isalnum():
                species = ""  # so letras, numeros e : _ # - .
            emotion = str(req.get("emotion", ""))[:20] if str(req.get("emotion", "")).isalpha() else ""
            pcm = speech.synthesize(text, voice, float(req.get("pitch", 1.0)), float(req.get("speed", 1.0)), engine, species, int(req.get("seed", 0)), emotion)
        except (KeyError, ValueError, TypeError, json.JSONDecodeError):
            return self._send(400, {"error": "bad_request"})
        except Exception as e:
            logging.getLogger("mobvoz.tts").warning("falha no TTS: %s", e)
            return self._send(503, {"error": "tts_unavailable"})
        self._send_raw(pcm, "application/octet-stream", {"X-Sample-Rate": str(speech.SVC_RATE)})

    def log_message(self, fmt, *args):  # sem texto de jogador nos logs (AGENTS.md #7)
        logging.getLogger("mobvoz.http").info("%s %s", self.command, self.path)


def explain_llm_failure(log) -> None:
    """A IA que conversa nao respondeu: diz na janela preta POR QUE e o que fazer (em vez de so aparecer "nao consegui pensar" no jogo)."""
    try:
        llm.chat([{"role": "user", "content": "oi"}], timeout=25)
        log.error("A IA respondeu, mas fora do formato esperado. Tente falar de novo; se continuar, avise o autor.")
        return
    except llm.LlmError as e:
        msg = str(e)
    if config.PROVIDER == "api":
        if "401" in msg or "403" in msg:
            dica = "a chave da API esta errada ou sem permissao. De dois cliques em 2-CONFIGURAR-API.bat e cole a chave de novo."
        elif "402" in msg:
            dica = "a conta da API esta sem saldo. Coloque credito no site da API."
        elif "429" in msg:
            dica = "o limite de uso da API foi atingido. Espere um pouco e tente de novo."
        else:
            dica = f"nao consegui falar com a API ({msg}). Confira a internet e a chave."
    else:
        dica = (f"nao consegui falar com o Ollama ({msg}). Instale o Ollama (ollama.com/download), deixe-o aberto e rode no PowerShell:  "
                f"ollama pull {config.MODEL}   Ou use a sua propria API: 2-CONFIGURAR-API.bat e depois feche e abra o 3-LIGAR-SERVICO.bat.")
    log.error("==== ATENCAO: a IA que conversa NAO respondeu: %s", dica)


def warm_up() -> None:
    """Aquece modelo, vozes e STT em segundo plano: a 1a fala do jogo nao paga o carregamento (antes ~17 s, depois ~3 s)."""
    log = logging.getLogger("mobvoz.warm")
    t0 = time.perf_counter()
    for name, job in (
        ("LLM", lambda: (service.analyze({"text": "oi", "capabilities": ["REGISTER_CLAIM", "OPEN_TRADE", "SOCIAL"]}),
                         service.reply({"entity_type": "minecraft:cow", "text": "oi", "capabilities": []}))[1]),
        ("vozes", lambda: [speech.synthesize("Oi.", v, 1.0, 1.0) for v in (("pt_BR-faber-medium",) if speech.active_engine() == "kokoro"
                                                                          else ("pt_BR-faber-medium", "pt_BR-cadu-medium", "pt_BR-jeff-medium"))]),
        ("STT", lambda: speech.transcribe(bytes(48000 * 2))),  # 1 s de silencio
    ):
        try:
            r = job()
            if name == "LLM" and isinstance(r, dict) and r.get("source") != "llm":
                explain_llm_failure(log)
        except Exception as e:  # modelo/voz ausente: o sidecar segue (a falha reaparece no uso)
            log.warning("aquecimento de %s falhou: %s", name, e)
    log.info("aquecimento concluido em %.1f s", time.perf_counter() - t0)


def lower_priority() -> None:
    """O servico de IA roda com prioridade ABAIXO do normal no Windows: o Minecraft sempre tem preferencia na CPU (nada de travar o jogo). MOBVOZ_PRIORITY=normal desliga."""
    if sys.platform != "win32" or os.environ.get("MOBVOZ_PRIORITY", "low").lower() == "normal":
        return
    try:
        import ctypes
        ctypes.windll.kernel32.SetPriorityClass(ctypes.windll.kernel32.GetCurrentProcess(), 0x00004000)  # BELOW_NORMAL_PRIORITY_CLASS
    except Exception as e:  # nao e essencial
        logging.getLogger("mobvoz").warning("nao consegui baixar a prioridade do servico: %s", e)


def main() -> None:
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(name)s %(message)s")
    lower_priority()
    srv = ThreadingHTTPServer((config.HOST, config.PORT), Handler)
    logging.info("MobVoz sidecar em http://%s:%d (IA: %s)", config.HOST, config.PORT, "API " + config.API_MODEL if config.PROVIDER == "api" else "local " + config.MODEL)
    threading.Thread(target=warm_up, daemon=True, name="warm-up").start()
    srv.serve_forever()


if __name__ == "__main__":
    main()
