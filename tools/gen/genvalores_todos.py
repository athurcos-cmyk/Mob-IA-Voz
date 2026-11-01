# -*- coding: utf-8 -*-
"""Valor de troca e nome pt-BR de TODOS os itens do jogo (values.json), para qualquer item poder ser pedido/oferecido.

Fontes (tudo do proprio jogo, sem inventar): receitas oficiais (data/minecraft/recipe), tags de itens, nomes pt_br.json.
Valor = o que ja estava cadastrado (preservado) > valor-base de materia-prima (regras abaixo) > soma dos ingredientes da receita mais barata
(1 esmeralda = 10 pontos) > classificacao de raridade por IA para o que nao tem receita (drops de mobs, achados raros), guardada em
tools/gen/dados/raridade_ia.json (so chama a IA para o que ainda falta). Itens de operador/inalcancaveis ficam de fora.

Ordem: 1) python tools/gen/genprofiles.py  2) python tools/gen/genvalores_todos.py [--ia]  (o 2o amplia values.json e mantem os apelidos; o 1o refaz perfis/receitas)
Uso:  python tools/gen/genvalores_todos.py [--ia]   (--ia chama a API para classificar o que falta; precisa de MOBVOZ_PROVIDER=api e da chave)
Depois rode:  python tools/gen/genprofiles.py   (une com os perfis/receitas) e confira com os testes."""
import glob
import json
import re
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
RES = ROOT / "mod-fabric" / "src" / "main" / "resources" / "mobvoz"
DADOS = Path(__file__).with_name("dados")
M = "minecraft:"

JAR = [x for x in glob.glob(str(ROOT / "mod-fabric/.gradle-user-home/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged/1.21.8*/*.jar")) if "sources" not in x][0]
ASSETS = ROOT / "mod-fabric/.gradle-user-home/caches/fabric-loom/assets"

# itens que nao se pedem nem se dao (operador, inalcancaveis em jogo normal, tecnicos)
EXCLUDE = re.compile(r"(spawn_egg$|^command_block|^chain_command_block|^repeating_command_block|^structure_(block|void)$|^jigsaw$|^barrier$|^light$|^debug_stick$"
                     r"|^bedrock$|^end_portal(_frame)?$|^nether_portal$|^reinforced_deepslate$|^spawner$|^trial_spawner$|^vault$|^knowledge_book$|^petrified_oak_slab$"
                     r"|^farmland$|^budding_amethyst$|^dragon_egg$|^air$|^test_|^player_head$|^written_book$|^filled_map$|^potion$|^splash_potion$|^lingering_potion$"
                     r"|^tipped_arrow$|^enchanted_book$|^suspicious_|^frogspawn$|^infested_|^chorus_plant$|^command_block_minecart$|^end_gateway$|^moving_piston$"
                     r"|^bundle$|^firework_star$|^firework_rocket$|^crossbow$|^trial_key$|^ominous_trial_key$|^ominous_bottle$|^wind_charge$|^mace$|^heavy_core$|^lodestone$"
                     r"|^creaking_heart$|^resin_clump$|^open_eyeblossom$|^closed_eyeblossom$|^dried_ghast$|^happy_ghast_spawn$)")

# valor-base de materia-prima (sem receita). Padroes primeiro; o valor exato cadastrado em values.json sempre vence.
BASE_EXACT = {
    "dirt": 0.05, "coarse_dirt": 0.05, "rooted_dirt": 0.1, "podzol": 0.1, "mycelium": 0.3, "grass_block": 0.1, "sand": 0.1, "red_sand": 0.1, "gravel": 0.1,
    "clay_ball": 0.3, "stone": 0.2, "cobblestone": 0.2, "deepslate": 0.25, "cobbled_deepslate": 0.25, "netherrack": 0.1, "end_stone": 0.3, "basalt": 0.3,
    "blackstone": 0.3, "tuff": 0.2, "calcite": 0.3, "dripstone_block": 0.3, "andesite": 0.2, "diorite": 0.2, "granite": 0.2, "obsidian": 4.0, "crying_obsidian": 8.0,
    "ice": 0.4, "snow_block": 0.2, "snowball": 0.1, "packed_ice": 1.0, "blue_ice": 4.0, "moss_block": 0.5, "mud": 0.2, "sandstone": 0.5, "red_sandstone": 0.5,
    "coal": 1.0, "raw_iron": 2.0, "raw_gold": 3.5, "raw_copper": 0.8, "iron_ingot": 3.0, "gold_ingot": 5.0, "copper_ingot": 1.5, "diamond": 20.0, "emerald": 10.0,
    "lapis_lazuli": 1.0, "redstone": 0.8, "quartz": 1.5, "amethyst_shard": 2.0, "netherite_scrap": 40.0, "netherite_ingot": 200.0, "glowstone_dust": 1.0,
    "oak_log": 0.3, "ancient_debris": 45.0, "echo_shard": 25.0, "disc_fragment_5": 8.0, "sculk": 1.0, "sculk_catalyst": 8.0, "sculk_sensor": 6.0, "sculk_shrieker": 10.0,
    "nether_wart": 1.0, "magma_block": 1.5, "soul_sand": 0.5, "soul_soil": 0.5, "crimson_nylium": 0.3, "warped_nylium": 0.3, "shroomlight": 2.0, "glowstone": 4.0,
    "slime_ball": 2.0, "string": 0.5, "feather": 0.2, "leather": 1.0, "bone": 0.5, "gunpowder": 2.0, "spider_eye": 1.0, "rotten_flesh": 0.4, "ender_pearl": 6.0,
    "blaze_rod": 5.0, "ghast_tear": 12.0, "phantom_membrane": 4.0, "prismarine_shard": 1.5, "prismarine_crystals": 3.0, "nautilus_shell": 15.0, "heart_of_the_sea": 90.0,
    "scute": 3.0, "turtle_scute": 3.0, "armadillo_scute": 3.0, "ink_sac": 1.0, "glow_ink_sac": 2.0, "rabbit_hide": 0.7, "rabbit_foot": 6.0, "shulker_shell": 12.0,
    "nether_star": 150.0, "totem_of_undying": 100.0, "elytra": 130.0, "trident": 60.0, "enchanted_golden_apple": 90.0, "golden_apple": 12.0, "sponge": 4.0, "wet_sponge": 4.0,
    "wither_skeleton_skull": 60.0, "dragon_breath": 12.0, "experience_bottle": 8.0, "name_tag": 6.0, "saddle": 16.0, "lead": 3.0, "trial_key": 30.0,
    "sniffer_egg": 20.0, "turtle_egg": 6.0, "egg": 0.4, "honeycomb": 2.0, "honey_bottle": 3.0, "pointed_dripstone": 0.3, "glow_berries": 0.6, "sweet_berries": 0.3,
    "kelp": 0.2, "seagrass": 0.2, "bamboo": 0.3, "sugar_cane": 0.3, "cactus": 0.3, "vine": 0.3, "lily_pad": 0.5, "wheat": 0.5, "wheat_seeds": 0.25, "carrot": 0.5, "potato": 0.5,
    "beetroot": 0.5, "pumpkin": 1.0, "melon_slice": 0.6, "apple": 1.0, "cocoa_beans": 0.4, "poisonous_potato": 0.2, "brown_mushroom": 0.5, "red_mushroom": 0.5,
    "fern": 0.1, "short_grass": 0.05, "tall_grass": 0.05, "large_fern": 0.1, "dead_bush": 0.1, "glow_lichen": 0.3, "hanging_roots": 0.2, "spore_blossom": 4.0,
    "pitcher_plant": 6.0, "torchflower": 6.0, "wither_rose": 14.0, "pink_petals": 0.5, "cherry_leaves": 0.2, "chorus_fruit": 1.0, "popped_chorus_fruit": 1.0,
    "porkchop": 1.0, "beef": 1.0, "chicken": 1.0, "mutton": 1.0, "rabbit": 1.0, "cod": 0.8, "salmon": 0.8, "tropical_fish": 1.0, "pufferfish": 1.5,
    "water_bucket": 9.5, "lava_bucket": 12.0, "milk_bucket": 9.5, "powder_snow_bucket": 9.5, "axolotl_bucket": 20.0, "cod_bucket": 10.0, "salmon_bucket": 10.0,
    "pufferfish_bucket": 11.0, "tropical_fish_bucket": 11.0, "tadpole_bucket": 12.0, "bucket": 9.0, "glass_bottle": 0.3, "paper": 0.35, "book": 2.0,
}
BASE_PATTERNS = [
    (r"_log$|_stem$|_hyphae$|_wood$|^stripped_", 0.3), (r"_sapling$|_propagule$", 0.3), (r"_leaves$", 0.1), (r"_fungus$|_roots$", 1.0),
    (r"^(poppy|dandelion|blue_orchid|allium|azure_bluet|.*_tulip|oxeye_daisy|cornflower|lily_of_the_valley|sunflower|lilac|rose_bush|peony)$", 1.0),
    (r"_ore$", 3.0), (r"_concrete$", 0.5), (r"_coral$|_coral_fan$|_coral_block$", 1.5), (r"_dye$", 0.4), (r"_pottery_sherd$", 4.0), (r"^music_disc_", 40.0), (r"_banner_pattern$", 8.0),
    (r"_smithing_template$", 25.0), (r"^(.*)_horn$|^goat_horn$", 12.0), (r"_head$", 12.0),
]


def load():
    z = zipfile.ZipFile(JAR)
    names = z.namelist()
    items = sorted(n.split("/")[-1][:-5] for n in names if n.startswith("assets/minecraft/items/") and n.endswith(".json"))
    recipes = [json.loads(z.read(n)) for n in names if n.startswith("data/minecraft/recipe/") and n.endswith(".json")]
    tags = {}
    for n in names:
        if n.startswith("data/minecraft/tags/item/") and n.endswith(".json"):
            tags["#" + M + n[len("data/minecraft/tags/item/"):-5]] = json.loads(z.read(n)).get("values", [])
    idx = json.loads((ASSETS / "indexes" / "1.21.8-26.json").read_text(encoding="utf-8"))["objects"]["minecraft/lang/pt_br.json"]["hash"]
    pt = json.loads((ASSETS / "objects" / idx[:2] / idx).read_bytes().decode("utf-8"))
    en = json.loads(z.read("assets/minecraft/lang/en_us.json"))
    return items, recipes, tags, pt, en


def resolve_tag(tag, tags, seen=None):
    seen = seen or set()
    out = []
    for v in tags.get(tag, []):
        v = v["id"] if isinstance(v, dict) else v
        if v.startswith("#"):
            if v not in seen:
                seen.add(v)
                out += resolve_tag(v, tags, seen)
        else:
            out.append(v)
    return out


def ingredient_options(ing, tags):
    """Ingrediente da receita -> lista de ids possiveis."""
    if isinstance(ing, list):
        out = []
        for i in ing:
            out += ingredient_options(i, tags)
        return out
    if isinstance(ing, dict):
        ing = ing.get("item") or ing.get("tag") or ""
    if ing.startswith("#"):
        return resolve_tag(ing, tags)
    return [ing]


def base_value(item):
    if item in BASE_EXACT:
        return BASE_EXACT[item]
    for rx, v in BASE_PATTERNS:
        if re.search(rx, item):
            return v
    return None


def recipe_inputs(r, tags):
    """(lista de listas-de-opcoes, saida, quantidade) de uma receita, ou None se nao se aplica."""
    t = r.get("type", "").replace(M, "")
    res = r.get("result")
    if isinstance(res, str):
        res = {"id": res}
    if not res or "id" not in res:
        return None
    out, n = res["id"].replace(M, ""), max(1, res.get("count", 1))
    if t == "crafting_shaped":
        keys = {k: ingredient_options(v, tags) for k, v in r["key"].items()}
        ing = [keys[c] for row in r["pattern"] for c in row if c != " " and c in keys]
        return ing, out, n
    if t == "crafting_shapeless":
        return [ingredient_options(i, tags) for i in r["ingredients"]], out, n
    if t in ("smelting", "blasting", "smoking", "campfire_cooking", "stonecutting"):
        return [ingredient_options(r["ingredient"], tags)], out, n
    if t == "crafting_transmute":
        return [ingredient_options(r["input"], tags), ingredient_options(r["material"], tags)], out, n
    if t == "smithing_transform":
        return [ingredient_options(r[k], tags) for k in ("template", "base", "addition")], out, n
    return None


def compute(items, recipes, tags, existing):
    value = {k.replace(M, ""): v["value"] for k, v in existing.items()}
    fixed = set(value)
    for it in items:
        if it not in value:
            b = base_value(it)
            if b is not None:
                value[it] = b
                fixed.add(it)
    parsed = [x for x in (recipe_inputs(r, tags) for r in recipes) if x]
    for _ in range(40):  # ponto fixo: cada rodada resolve os itens cujos ingredientes ja tem valor
        changed = False
        for it in items:  # cobre oxidado/desgastado: vale o mesmo que o cobre novo
            m = re.match(r"^(exposed|weathered|oxidized)_(.+)$", it)
            base = m and next((c for c in (m.group(2), m.group(2) + "_block") if c in value), None)
            if m and it not in value and base:
                value[it] = value[base]
                changed = True
        for ings, out, n in parsed:
            if out in fixed or out not in items:
                continue
            total = 0.0
            ok = True
            for opts in ings:
                known = [value[o.replace(M, "")] for o in opts if o.replace(M, "") in value]
                if not known:
                    ok = False
                    break
                total += min(known)
            if not ok:
                continue
            v = round(total / n, 2)
            if out not in value or v < value[out] - 1e-9:
                value[out] = v
                changed = True
        if not changed:
            break
    for part in ("helmet", "chestplate", "leggings", "boots"):  # cota de malha so existe como achado: vale um pouco menos que o ferro
        if "iron_" + part in value and "chainmail_" + part not in fixed:
            value["chainmail_" + part] = round(value["iron_" + part] * 0.85, 2)
    return value, fixed


def pluralize(name):
    parts = name.split(" ")
    w = parts[0]
    if w.endswith("s") or w.endswith("x"):
        p = w
    elif w.endswith("ão"):
        p = w[:-2] + ("ães" if w == "pão" else "ões")
    elif w.endswith(("r", "z")):
        p = w + "es"
    elif w.endswith("m"):
        p = w[:-1] + "ns"
    elif w.endswith("l"):
        p = w[:-1] + "is"
    else:
        p = w + "s"
    rest = []
    for x in parts[1:]:  # adjetivo depois do substantivo concorda ("maçã dourada" -> "maçãs douradas"); "de/do/da..." e o resto ficam
        if x in ("de", "do", "da", "dos", "das", "com", "em", "para", "e") or rest and rest[-1] in ("de", "do", "da", "dos", "das"):
            rest.append(x)
        elif len(parts) == 2 and x.endswith(("o", "a")) and x not in ("de", "do", "da"):
            rest.append(x + "s")
        else:
            rest.append(x)
    return " ".join([p] + rest)


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    use_ai = "--ia" in sys.argv
    items, recipes, tags, pt, en = load()
    existing = json.loads((DADOS / "values_base.json").read_text(encoding="utf-8"))  # os valores/nomes escritos a mao (nunca sao sobrescritos)
    comment = existing["_comment"]
    existing = existing["items"]
    sys.path.insert(0, str(Path(__file__).parent))
    import catalog  # noqa: E402  os itens escritos a mao em catalog.py/mais_perfis.py tambem sao "ja cadastrados" (nomes e valores preservados)
    for k, (v, names) in catalog.NEW_VALUES.items():
        existing[M + k] = {"value": v, "pt": list(names)}
    value, fixed = compute(items, recipes, tags, existing)
    ia_path = DADOS / "raridade_ia.json"
    ia = json.loads(ia_path.read_text(encoding="utf-8")) if ia_path.exists() else {}
    for k, v in ia.items():
        if k in items and k not in value and v.get("value", 0) > 0:
            value[k] = float(v["value"])
    for part in ("helmet", "chestplate", "leggings", "boots"):
        if "iron_" + part in value:
            value["chainmail_" + part] = round(value["iron_" + part] * 0.85, 2)
    todo = [i for i in items if i not in value and not EXCLUDE.search(i)]
    print(len(items), "itens;", len(value), "com valor;", len(todo), "sem valor e fora da lista de exclusao")
    if todo and use_ai:
        ask_ia(todo, pt, en, ia)
        ia_path.parent.mkdir(exist_ok=True)
        ia_path.write_text(json.dumps(ia, ensure_ascii=False, indent=0), encoding="utf-8")
        for k, v in ia.items():
            if k in items and k not in value and v.get("value", 0) > 0:
                value[k] = float(v["value"])
        todo = [i for i in items if i not in value and not EXCLUDE.search(i)]
        print("depois da IA:", len(todo), "ainda sem valor")
    if todo and not use_ai:
        print("sem valor (amostra):", todo[:80])
    write(items, value, existing, comment, pt, en)


def ask_ia(todo, pt, en, ia):
    sys.path.insert(0, str(ROOT / "ai-service"))
    from app import llm  # noqa: E402
    import concurrent.futures as cf
    system = ("Voce classifica a RARIDADE de itens do Minecraft Java 1.21 para um sistema de trocas com criaturas. Da a cada item um VALOR em pontos numa escala "
              "em que: graveto 0,1; carvao 1; barra de ferro 3; barra de ouro 5; esmeralda 10; diamante 20; fragmento de netherite 40; pergaminho/livro 2; sela 16; "
              "perola do Fim 6; haste de blaze 5; lagrima de ghast 12; membrana de phantom 4; tridente 60; totem da imortalidade 100; elytra 130; estrela do Nether 150. "
              "Itens comuns e baratos 0,1 a 2; uteis 2 a 12; raros 12 a 60; muito raros 60 a 200. O valor reflete quao dificil e conseguir o item em jogo normal. "
              "Se o item for tecnico, de operador, criativo, ou impossivel de obter, use value 0. Responda SOMENTE JSON {\"items\": [{\"id\": str, \"value\": number}]}.")
    schema = {"type": "object", "required": ["items"], "properties": {"items": {"type": "array", "items": {"type": "object", "required": ["id", "value"],
              "properties": {"id": {"type": "string"}, "value": {"type": "number"}}}}}}
    chunks = [todo[i:i + 40] for i in range(0, len(todo), 40)]

    def run(chunk):
        lst = [{"id": i, "nome": pt.get("item.minecraft." + i) or pt.get("block.minecraft." + i) or en.get("item.minecraft." + i) or i} for i in chunk]
        for _ in range(3):
            try:
                raw = llm.chat_api([{"role": "system", "content": system}, {"role": "user", "content": json.dumps(lst, ensure_ascii=False)}],
                                   "deepseek-v4-pro", 150, schema, {"temperature": 0.0, "num_predict": 3000})
                return {x["id"]: {"value": round(float(x["value"]), 2)} for x in json.loads(raw)["items"] if x["id"] in chunk}
            except Exception as e:  # noqa: BLE001
                print("falha", e)
        return {}

    with cf.ThreadPoolExecutor(5) as ex:
        for res in ex.map(run, chunks):
            ia.update(res)


def write(items, value, existing, comment, pt, en):
    out = {}
    used = {}
    # os ja cadastrados primeiro (nomes e valores preservados); depois os novos, sem tomar nome de quem ja existe
    def norm(s):
        import unicodedata
        n = unicodedata.normalize("NFD", s.lower())
        return re.sub(r"\s+", " ", re.sub(r"[^a-z0-9 ]", " ", "".join(c for c in n if not unicodedata.combining(c)))).strip()

    for k, v in existing.items():
        out[k] = v
        for n in v["pt"]:
            used.setdefault(norm(n), k)
    novos = 0
    for it in items:
        key = M + it
        if key in out or it not in value:
            continue
        nome = (pt.get("item.minecraft." + it) or pt.get("block.minecraft." + it) or "").strip()
        if not nome:
            continue
        nome = nome.lower()
        names = []
        for cand in (nome, pluralize(nome)):
            if norm(cand) not in used or used[norm(cand)] == key:
                names.append(cand)
        if not names or norm(nome) in used and used[norm(nome)] != key:
            continue  # nome ja pertence a outro item: este so entra pelo id (nao ha como pedi-lo por esse nome)
        for n in names:
            used[norm(n)] = key
        out[key] = {"value": value[it], "pt": names if len(names) > 1 else names * 2}
        novos += 1
    for alias, target in ALIASES.items():
        t = M + target
        if t in out and norm(alias) not in used:
            out[t]["pt"].append(alias)
            used[norm(alias)] = t
    lines = [f'    {json.dumps(k)}: {json.dumps(v, ensure_ascii=False)}' for k, v in out.items()]
    text = '{\n  "_comment": ' + json.dumps(comment, ensure_ascii=False) + ',\n  "items": {\n' + ",\n".join(lines) + "\n  }\n}\n"
    (RES / "values.json").write_text(text, encoding="utf-8")
    print(len(out), "itens em values.json (", novos, "novos )")


# jeitos comuns de falar que o nome oficial nao cobre
ALIASES = {
    "composteira": "composter", "compostagem": "composter", "bonemeal": "bone_meal", "bone meal": "bone_meal", "bonimil": "bone_meal", "farinha de osso": "bone_meal",
    "armadura de ferro": "iron_chestplate", "armadura de diamante": "diamond_chestplate", "armadura de ouro": "golden_chestplate", "armadura de couro": "leather_chestplate",
    "armadura de netherite": "netherite_chestplate", "armadura de malha": "chainmail_chestplate", "armadura": "iron_chestplate",
    "olho de aranha": "spider_eye", "oleo de aranha": "spider_eye", "tnt": "tnt", "dinamite": "tnt", "bau": "chest", "fornalha": "furnace", "forno": "furnace",
    "mesa de trabalho": "crafting_table", "bancada": "crafting_table", "cama": "white_bed", "elitro": "elytra", "totem": "totem_of_undying", "tridente": "trident",
}


if __name__ == "__main__":
    main()
