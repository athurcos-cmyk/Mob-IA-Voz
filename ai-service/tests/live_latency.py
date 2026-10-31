"""Mede onde o tempo vai numa fala: analise (LLM) + resposta (LLM) + voz (TTS), pelo sidecar real.

Uso: python tests/live_latency.py   (sidecar em 127.0.0.1:8765 e Ollama ligados)
Mostra tambem, direto do Ollama, quanto do tempo e leitura do prompt e quanto e geracao.
"""
import json
import statistics
import time
import urllib.request

BASE = "http://127.0.0.1:8765"
OLLAMA = "http://127.0.0.1:11434"
LINES = [
    "Oi porco, tudo bem?",
    "O que você acha das vacas?",
    "Quer trocar? Tenho cenouras.",
    "Me segue até a minha casa.",
    "Eu sou o rei dessa vila.",
]
PROFILE = {"nome": "porco", "estilo": "teimoso e guloso", "gosta": "cenouras", "desgosta": "raios", "dieta": "cenoura, batata, beterraba",
           "sobre": "vive em fazendas", "trato": "Humano", "origem": "Nasceu nas fazendas.", "sentidos": "Cheira comida de longe.",
           "motivacao": "Procura comida.", "medos": "Raios e quem fala em virar comida.", "credulidade": "média"}


def post(path, body, raw=False):
    req = urllib.request.Request(BASE + path, data=json.dumps(body).encode(), headers={"Content-Type": "application/json"})
    t = time.perf_counter()
    with urllib.request.urlopen(req, timeout=120) as r:
        data = r.read()
    ms = (time.perf_counter() - t) * 1000
    return (data if raw else json.loads(data)), ms


def ollama_split(system_len_hint=""):
    """Uma chamada crua ao Ollama so para separar leitura-do-prompt x geracao (tokens/s)."""
    body = {"model": "qwen3:4b", "stream": False, "think": False, "keep_alive": "10m",
            "messages": [{"role": "user", "content": "Diga uma frase curta sobre porcos, em português."}], "options": {"num_predict": 60}}
    req = urllib.request.Request(OLLAMA + "/api/chat", data=json.dumps(body).encode(), headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(req, timeout=120) as r:
        d = json.load(r)
    return (f"prompt {d.get('prompt_eval_count')} tok em {d.get('prompt_eval_duration', 0) / 1e6:.0f} ms | "
            f"geração {d.get('eval_count')} tok em {d.get('eval_duration', 0) / 1e6:.0f} ms "
            f"({d.get('eval_count', 0) / max(d.get('eval_duration', 1) / 1e9, 1e-9):.0f} tok/s)")


def main() -> None:
    post("/v1/analyze", {"text": "oi", "capabilities": ["REGISTER_CLAIM"]})  # aquece o modelo
    rows = []
    for text in LINES:
        an, t_an = post("/v1/analyze", {"text": text, "capabilities": ["REGISTER_CLAIM", "FOLLOW_PLAYER", "OPEN_TRADE", "SOCIAL"]})
        rp, t_rp = post("/v1/reply", {"entity_type": "minecraft:pig", "text": text, "capabilities": [], "facts": [], "profile": PROFILE,
                                      "history": [], "detected_claim": an.get("claim", ""), "order_results": []})
        pcm, t_tts = post("/v1/synthesize", {"text": rp["speech_text"], "voice": "pt_BR-cadu-medium", "pitch": 1.0, "speed": 1.0}, raw=True)
        rows.append((t_an, t_rp, t_tts))
        print(f"{text[:34]:34} analise {t_an:6.0f} ms | resposta {t_rp:6.0f} ms | voz {t_tts:5.0f} ms | total {t_an + t_rp + t_tts:6.0f} ms "
              f"| {len(rp['speech_text'])} chars")
    m = [statistics.median(c) for c in zip(*rows)]
    print(f"MEDIANA: analise {m[0]:.0f} + resposta {m[1]:.0f} + voz {m[2]:.0f} = {sum(m):.0f} ms (antes de a criatura comecar a falar)")
    print("Ollama puro:", ollama_split())


if __name__ == "__main__":
    main()
