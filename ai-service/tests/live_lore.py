"""Teste real opcional (LLM): cada criatura fala da sua origem/sentidos/motivos coerentes com a lore.json do mod, e reage ao tom.

Uso: python tests/live_lore.py
"""
import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from app import service  # noqa: E402

RES = Path(__file__).resolve().parents[2] / "mod-fabric" / "src" / "main" / "resources" / "mobvoz"
LORE = json.loads((RES / "lore.json").read_text(encoding="utf-8"))["lore"]
PROF = {p["id"]: p for p in json.loads((RES / "profiles.json").read_text(encoding="utf-8"))["profiles"]}
CRED = {0: "baixa: desconfia de histórias e exageros", 1: "média: acredita no razoável", 2: "alta: tende a acreditar em quem é gentil"}

# (tipo, pergunta, palavras aceitaveis)
CASES = [
    ("minecraft:warden", "Você consegue me ver?", ["cego", "vibra", "ouv", "barulho", "ruído", "cheiro", "não vejo", "não enxergo"]),
    ("minecraft:warden", "Por que você fica tão furioso com barulho?", ["vibra", "barulho", "ruído", "silêncio", "ouv"]),
    ("minecraft:creeper", "Por que você explode?", ["não sei", "lenda", "dizem", "porco", "corpo", "perto"]),
    ("minecraft:enderman", "Por que você pega blocos?", ["bloco", "bonit", "teleport", "pego"]),
    ("minecraft:enderman", "O que acontece se eu te olhar nos olhos?", ["furios", "olhar", "encar", "olhos", "enfurec"]),
    ("minecraft:phantom", "Por que você me persegue?", ["dorm", "sono", "insôn", "descans", "noite"]),
    ("minecraft:iron_golem", "Quem te criou?", ["aldeõ", "vila", "criad", "protej"]),
    ("minecraft:zombie_villager", "Você lembra de quando era aldeão?", ["aldeão", "vila", "cura", "lembr", "fome"]),
]


def prof_for(tid: str) -> dict:
    lo = LORE[tid]
    p = PROF.get(tid, {"label": tid.split(":")[1].replace("_", " "), "style": "natural e curioso", "likes": [], "dislikes": [], "credulity": 1,
                       "diet": "o que combina com a espécie", "about": "", "address": "Humano"})
    return {"nome": p["label"], "estilo": p["style"], "gosta": "", "desgosta": "", "credulidade": CRED[p["credulity"]], "dieta": p["diet"],
            "sobre": p["about"], "trato": p.get("address", "Humano"), "origem": lo["origem"], "sentidos": lo["sentidos"],
            "motivacao": lo["motivacao"], "medos": lo["medos"]}


def main() -> None:
    ok = 0
    for tid, q, words in CASES:
        out = service.reply({"entity_type": tid, "text": q, "capabilities": [], "profile": prof_for(tid)})
        said = out["speech_text"]
        good = any(w in said.lower() for w in words)
        ok += good
        print(f"{'OK ' if good else 'DIF'} [{tid.split(':')[1]}] {q}\n      -> {said}")
    # reacoes: o servidor decide; aqui so conferimos que a fala acompanha o resultado real
    for tid, res, why, must in [
        ("minecraft:pig", "FOGE", LORE["minecraft:pig"]["reacao"], ["não", "socorro", "pelo amor", "foge", "corr", "por favor", "me"]),
        ("minecraft:warden", "IMPASSIVEL", LORE["minecraft:warden"]["reacao"], ["vibra", "barulho", "ruído", "cego", "ouv", "não"]),
    ]:
        out = service.reply({"entity_type": tid, "text": "Vou te transformar em comida!", "capabilities": [], "profile": prof_for(tid),
                             "order_results": [{"pedido": "REACAO", "resultado": res, "motivo": why}]})
        good = any(w in out["speech_text"].lower() for w in must)
        ok += good
        print(f"{'OK ' if good else 'DIF'} [{tid.split(':')[1]} / {res}] -> {out['speech_text']}")
    print(f"{ok}/{len(CASES) + 2} coerentes")


if __name__ == "__main__":
    main()
