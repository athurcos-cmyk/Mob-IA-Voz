"""Teste real opcional: a fala e coerente com a ESPECIE? Pergunta "O que voce gosta de comer?" a varias criaturas usando o MESMO
profiles.json do mod (como o servidor monta o perfil) e confere palavras que fazem sentido para a especie.

Uso: python tests/live_species.py
"""
import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from app import service  # noqa: E402

PROFILES = Path(__file__).resolve().parents[2] / "mod-fabric" / "src" / "main" / "resources" / "mobvoz" / "profiles.json"
CRED = {0: "baixa: desconfia de histórias e exageros", 1: "média: acredita no razoável, desconfia de exageros",
        2: "alta: tende a acreditar em quem é gentil"}

# especie -> palavras aceitaveis na resposta (qualquer uma serve)
EXPECT = {
    "minecraft:chicken": ["semente", "trigo", "grão", "minhoca"],
    "minecraft:cow": ["capim", "grama", "trigo", "pasto", "past"],
    "minecraft:pig": ["cenoura", "batata", "beterraba", "maçã", "lama"],
    "minecraft:sheep": ["grama", "capim", "pasto", "past"],
    "minecraft:wolf": ["carne", "osso"],
    "minecraft:cat": ["peixe", "salmão", "bacalhau"],
    "minecraft:rabbit": ["cenoura", "batata", "dente"],
    "minecraft:horse": ["grama", "maçã", "cenoura", "trigo", "açúcar"],
    "minecraft:cod": ["plâncton", "peixinho", "alga", "mar", "água"],
    "minecraft:zombie": ["carne", "cérebro", "aldeão"],
    "minecraft:villager#farmer": ["pão", "trigo", "cenoura", "batata", "beterraba", "colheita"],
}


def build(pid: str, prof: dict) -> dict:
    names = lambda ids: ", ".join(i.split(":")[-1].replace("_", " ") for i in ids)  # noqa: E731
    return {"nome": prof["label"], "estilo": prof["style"], "gosta": names(prof["likes"]), "desgosta": names(prof["dislikes"]),
            "credulidade": CRED[prof["credulity"]], "dieta": prof["diet"], "sobre": prof["about"], "trato": prof.get("address", "Humano")}


def main() -> None:
    profs = {p["id"]: p for p in json.loads(PROFILES.read_text(encoding="utf-8"))["profiles"]}
    ok = 0
    for pid, words in EXPECT.items():
        out = service.reply({"entity_type": pid.split("#")[0], "text": "O que você gosta de comer?", "capabilities": [],
                             "profile": build(pid, profs[pid])})
        said = out["speech_text"]
        good = any(w in said.lower() for w in words)
        ok += good
        print(f"{'OK ' if good else 'DIF'} [{pid}] {said}")
    print(f"{ok}/{len(EXPECT)} coerentes com a especie")


if __name__ == "__main__":
    main()
