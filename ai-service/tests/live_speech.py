"""Teste real opcional (sidecar rodando): TTS pt-BR -> STT pt -> compara. Salva a pergunta de teste do gametest.

Uso: python tests/live_speech.py [--save caminho.pcm]
"""
import json
import sys
import time
import unicodedata
import urllib.request

BASE = "http://127.0.0.1:8765"
PHRASES = ["O que você gosta de comer?", "Eu sou seu pai, sabia?", "Fica aqui e não entra no portal.", "Você quer me seguir até a minha casa?"]


def post(path, data, ctype):
    req = urllib.request.Request(BASE + path, data=data, headers={"Content-Type": ctype})
    with urllib.request.urlopen(req, timeout=120) as r:
        return r.read()


def norm(s):
    s = unicodedata.normalize("NFD", s.lower())
    return "".join(c for c in s if c.isalnum() or c == " ").split()


def main():
    save = sys.argv[sys.argv.index("--save") + 1] if "--save" in sys.argv else None
    ok = 0
    for i, ph in enumerate(PHRASES):
        t = time.perf_counter()
        pcm = post("/v1/synthesize", json.dumps({"text": ph, "voice": "pt_BR-faber-medium"}).encode(), "application/json")
        t_tts = int((time.perf_counter() - t) * 1000)
        t = time.perf_counter()
        out = json.loads(post("/v1/transcribe", pcm, "application/octet-stream"))
        t_stt = int((time.perf_counter() - t) * 1000)
        same = norm(out["text"]) == norm(ph)
        ok += same
        print(f"{'OK ' if same else 'DIF'} tts {t_tts} ms ({len(pcm) / 96000:.1f} s de audio) stt {t_stt} ms: '{out['text']}' (esperado '{ph}')")
        if save and i == 0:
            with open(save, "wb") as f:
                f.write(pcm)
    print(f"{ok}/{len(PHRASES)} transcricoes identicas")


if __name__ == "__main__":
    main()
