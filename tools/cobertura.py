# -*- coding: utf-8 -*-
"""Mapa de cobertura dos mobs: o que cada grupo tem (historia, perfil proprio, receitas, papo entre eles, reacoes, aberturas).
Le os arquivos do mod (resources/mobvoz). Uso: python tools/cobertura.py [saida.md]   (sem argumento imprime)"""
import collections
import json
import sys
from pathlib import Path

RES = Path(__file__).resolve().parents[1] / "mod-fabric" / "src" / "main" / "resources" / "mobvoz"

# grupo -> especies (sem o prefixo minecraft:)
GROUPS = {
    "Fazenda": "cow pig sheep chicken horse donkey mule mooshroom".split(),
    "Estimacao": "wolf cat parrot".split(),
    "Mata e outros terrestres": "fox rabbit panda ocelot goat llama camel frog armadillo polar_bear sniffer".split(),
    "Aquaticos": "cod salmon tropical_fish pufferfish squid glow_squid dolphin turtle axolotl".split(),
    "Voadores": "bee bat allay parrot".split(),
    "Aldeia": "villager wandering_trader iron_golem snow_golem".split(),
    "Monstros comuns": "zombie skeleton creeper spider enderman witch".split(),
    "Outros hostis": "slime cave_spider silverfish husk stray drowned guardian elder_guardian vex phantom pillager vindicator evoker ravager".split(),
    "Nether": "piglin piglin_brute hoglin zombified_piglin strider blaze ghast magma_cube wither_skeleton".split(),
    "End e chefes": "shulker endermite warden wither ender_dragon".split(),
}


def load():
    lore = json.loads((RES / "lore.json").read_text(encoding="utf-8"))["lore"]
    prof = json.loads((RES / "profiles.json").read_text(encoding="utf-8"))["profiles"]
    rec = json.loads((RES / "trades.json").read_text(encoding="utf-8"))["recipes"]
    amb = json.loads((RES / "ambient.json").read_text(encoding="utf-8"))["families"]
    ids = lambda x: set(x.keys()) if isinstance(x, dict) else {e.get("id") for e in x}
    return ids(lore), ids(prof), collections.Counter(r["profile"] for r in rec), amb


# classes de assunto (campo "who" de ambient.json) que valem para cada especie, alem das genericas (calm, animals, monsters, villagers)
ALIAS = {"iron_golem": ["golem"], "villager": ["villager"], "horse": ["horse", "herd"], "donkey": ["horse", "herd"], "mule": ["horse", "herd"],
         "cow": ["cow", "farm", "herd"], "pig": ["pig", "farm", "herd"], "sheep": ["sheep", "farm", "herd"], "chicken": ["chicken", "farm", "herd"],
         "mooshroom": ["cow", "farm", "herd"], "wolf": ["wolf", "pet"], "cat": ["cat", "pet"]}
GENERIC = {"calm", "animals", "monsters", "villagers"}


def classes_of(m, extra):
    return set(ALIAS.get(m, [m])) | set(extra.get(m, []))


def families_for(amb, members, extra, kind):
    """Familias do tipo (papo / reacao / abertura) cuja classe de quem abre vale para alguma das especies (sem as genericas)."""
    cls = set()
    for m in members:
        cls |= classes_of(m, extra)
    cls -= GENERIC
    out = []
    for f in amb:
        k = "puxa" if f["id"].startswith("ev_puxa_") else "ev" if f["id"].startswith("ev_") else "papo"
        if k == kind and (f["who"][0] in cls or (f["who"][0].startswith("villager:") and "villager" in members)):
            out.append(f)
    return out


def n_lines(fams, kind):
    return sum(len(f["a"]) if kind == "puxa" else len(f["a"]) + len(f["b"]) + len(f["c"]) for f in fams)


def species_with(amb, members, extra, kind):
    return [m for m in members if families_for(amb, [m], extra, kind)]


def main():
    lore, prof, rec, amb = load()
    extra = {}
    out = []
    out.append("| Grupo | Especies | Com historia | Com perfil proprio | Com receita de troca | Com papo entre si (falas proprias) | Falas de papo | Falas de reacao | Aberturas |")
    out.append("|---|---|---|---|---|---|---|---|---|")
    tot = collections.Counter()
    for g, ms in GROUPS.items():
        has_lore = sum(1 for m in ms if f"minecraft:{m}" in lore)
        has_prof = sum(1 for m in ms if f"minecraft:{m}" in prof)
        has_rec = sum(1 for m in ms if rec.get(f"minecraft:{m}"))
        fp, fe, fa = (families_for(amb, ms, extra, k) for k in ("papo", "ev", "puxa"))
        p, e, a = n_lines(fp, "papo"), n_lines(fe, "ev"), n_lines(fa, "puxa")
        with_chat = len(species_with(amb, ms, extra, "papo"))
        out.append(f"| {g} | {len(ms)} | {has_lore} | {has_prof} | {has_rec} | {with_chat} | {p} | {e} | {a} |")
        tot.update(especies=len(ms), lore=has_lore, perfil=has_prof, receita=has_rec, papo=with_chat, p=p, e=e, a=a)
    out.append(f"| **Total** | {tot['especies']} | {tot['lore']} | {tot['perfil']} | {tot['receita']} | {tot['papo']} | {tot['p']} | {tot['e']} | {tot['a']} |")
    fams = [f for f in amb if not f["id"].startswith("ev_")]
    out.append("")
    out.append(f"Familias de papo (nao-evento): {len(fams)}; falas de papo no total: {sum(len(f['a']) + len(f['b']) + len(f['c']) for f in fams)}; "
               f"familias de reacao: {sum(1 for f in amb if f['id'].startswith('ev_') and not f['id'].startswith('ev_puxa_'))}; "
               f"familias de abertura: {sum(1 for f in amb if f['id'].startswith('ev_puxa_'))}.")
    text = "\n".join(out)
    if len(sys.argv) > 1:
        Path(sys.argv[1]).write_text(text + "\n", encoding="utf-8")
    print(text)


if __name__ == "__main__":
    main()
