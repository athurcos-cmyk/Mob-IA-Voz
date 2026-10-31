"""Catalogo por criatura: o que ela ACEITA e o que faz SENTIDO ela dar. Fonte: minecraft.wiki (drops/comportamento) + bom senso.
Gera values.json (itens novos), trades.json e wants/drops dos perfis. Preco por unidade = ceil_meio(count * valor * margem)."""
from pathlib import Path

# raiz do projeto = duas pastas acima de tools/gen
ROOT = Path(__file__).resolve().parents[2]
RES_DIR = ROOT / "mod-fabric" / "src" / "main" / "resources" / "mobvoz"
import json, math

M = "minecraft:"
V = M + "villager#"
VALUES_PATH = str(RES_DIR / "values.json")
TRADES_PATH = str(RES_DIR / "trades.json")
# id -> (valor em pontos; 1 esmeralda = 10, nomes pt-BR: singular, plural, ...). Nome repetido = substantivo de massa.
NEW_VALUES = {
    "dandelion": (0.5, ["dente-de-leão", "dentes-de-leão"]),
    "blue_orchid": (1.5, ["orquídea azul", "orquídeas azuis", "orquídea-azul"]),
    "allium": (1.5, ["alium", "aliuns"]),
    "azure_bluet": (1.0, ["azulzinha", "azulzinhas"]),
    "red_tulip": (1.0, ["tulipa vermelha", "tulipas vermelhas", "tulipa"]),
    "oxeye_daisy": (1.0, ["margarida", "margaridas"]),
    "cornflower": (1.0, ["centáurea", "centáureas"]),
    "lily_of_the_valley": (1.5, ["lírio-do-vale", "lírios-do-vale"]),
    "sunflower": (1.5, ["girassol", "girassóis"]),
    "cactus": (0.3, ["cacto", "cactos"]),
    "dead_bush": (0.1, ["arbusto seco", "arbustos secos"]),
    "sandstone": (0.5, ["arenito", "arenitos"]),
    "vine": (0.3, ["vinha", "vinhas", "trepadeira", "trepadeiras"]),
    "leather_helmet": (7.5, ["capacete de couro", "capacetes de couro"]),
    "leather_chestplate": (12.0, ["peitoral de couro", "peitorais de couro"]),
    "leather_leggings": (10.5, ["calça de couro", "calças de couro"]),
    "leather_boots": (6.0, ["botas de couro", "botas de couro"]),
    "saddle": (16.0, ["sela", "selas"]),
    "lead": (3.0, ["guia", "guias", "rédea", "rédeas"]),
    "iron_helmet": (15.0, ["capacete de ferro", "capacetes de ferro"]),
    "iron_chestplate": (24.0, ["peitoral de ferro", "peitorais de ferro"]),
    "iron_leggings": (21.0, ["calça de ferro", "calças de ferro"]),
    "iron_boots": (12.0, ["botas de ferro", "botas de ferro"]),
    "iron_axe": (9.2, ["machado de ferro", "machados de ferro"]),
    "iron_shovel": (3.2, ["pá de ferro", "pás de ferro"]),
    "shield": (4.0, ["escudo", "escudos"]),
    "stone_pickaxe": (0.8, ["picareta de pedra", "picaretas de pedra"]),
    "bow": (2.0, ["arco", "arcos"]),
    "iron_nugget": (0.35, ["pepita de ferro", "pepitas de ferro"]),
    "gold_nugget": (0.55, ["pepita de ouro", "pepitas de ouro"]),
    "cobblestone": (0.2, ["pedregulho", "pedregulhos", "pedra"]),
    "stone_bricks": (0.4, ["tijolo de pedra", "tijolos de pedra"]),
    "terracotta": (0.5, ["terracota", "terracotas"]),
    "mud": (0.2, ["lama", "lama"]),
    "brown_mushroom": (0.5, ["cogumelo marrom", "cogumelos marrons"]),
    "red_mushroom": (0.5, ["cogumelo vermelho", "cogumelos vermelhos"]),
    "mushroom_stew": (2.0, ["ensopado de cogumelo", "ensopados de cogumelo"]),
    "beetroot_seeds": (0.25, ["semente de beterraba", "sementes de beterraba"]),
    "pumpkin_seeds": (0.25, ["semente de abóbora", "sementes de abóbora"]),
    "melon_seeds": (0.25, ["semente de melancia", "sementes de melancia"]),
    "goat_horn": (12.0, ["chifre de cabra", "chifres de cabra"]),
    "snowball": (0.1, ["bola de neve", "bolas de neve"]),
    "rabbit_hide": (0.7, ["pele de coelho", "peles de coelho"]),
    "phantom_membrane": (4.0, ["membrana de phantom", "membranas de phantom"]),
    "sweet_berries": (0.3, ["frutinha silvestre", "frutinhas silvestres"]),
    "honeycomb": (2.0, ["favo de mel", "favos de mel"]),
    "bamboo": (0.3, ["bambu", "bambus"]),
    "slime_ball": (2.0, ["gosma", "gosmas", "bola de slime", "bolas de slime"]),
    "turtle_scute": (3.0, ["escudo de tartaruga", "escudos de tartaruga"]),
    "spider_eye": (1.0, ["olho de aranha", "olhos de aranha"]),
    "lily_pad": (0.5, ["vitória-régia", "vitórias-régias"]),
    "ochre_froglight": (8.0, ["luz de sapo", "luzes de sapo"]),
    "warped_fungus": (1.0, ["fungo distorcido", "fungos distorcidos"]),
    "seagrass": (0.2, ["grama marinha", "gramas marinhas"]),
    "sea_pickle": (1.0, ["picles do mar", "picles do mar"]),
    "prismarine_crystals": (3.0, ["cristal de prismarinho", "cristais de prismarinho"]),
    "glass_bottle": (0.3, ["garrafa de vidro", "garrafas de vidro"]),
    "glowstone_dust": (1.0, ["pó de glowstone", "pó de glowstone"]),
    "nether_wart": (1.0, ["verruga do nether", "verrugas do nether"]),
    "obsidian": (4.0, ["obsidiana", "obsidiana"]),
    "quartz": (1.5, ["quartzo", "quartzos"]),
    "fire_charge": (1.5, ["bola de fogo", "bolas de fogo", "carga de fogo", "cargas de fogo"]),
    "cooked_beef": (1.5, ["bife assado", "bifes assados"]),
    "cooked_porkchop": (1.5, ["costeleta assada", "costeletas assadas"]),
    "pumpkin_pie": (2.5, ["torta de abóbora", "tortas de abóbora"]),
    "fishing_rod": (1.5, ["vara de pescar", "varas de pescar"]),
    "oak_boat": (2.5, ["barco", "barcos"]),
    "white_bed": (4.0, ["cama", "camas"]),
    "shears": (6.0, ["tesoura", "tesouras"]),
    "blaze_powder": (4.0, ["pó de blaze", "pó de blaze"]),
    "item_frame": (2.5, ["moldura", "molduras"]),
    "nether_star": (150.0, ["estrela do nether", "estrelas do nether", "estrela do inferno", "estrela do wither", "estrelas do wither"]),
    # itens "livres": o jogador pode oferece-los/pedi-los; a IA julga se a criatura os aceitaria ou teria
    "raw_iron": (2.0, ["ferro bruto", "ferros brutos"]),
    "raw_gold": (3.5, ["ouro bruto", "ouro bruto"]),
    "raw_copper": (0.8, ["cobre bruto", "cobre bruto"]),
    "copper_ingot": (1.5, ["barra de cobre", "barras de cobre", "cobre"]),
    "stone": (0.2, ["pedra lisa", "pedras lisas"]),
    "oak_log": (0.3, ["tronco de carvalho", "troncos de carvalho", "tronco", "troncos", "madeira"]),
    "oak_planks": (0.1, ["tábua de carvalho", "tábuas de carvalho", "tábua", "tábuas"]),
    "dirt": (0.05, ["terra", "terra"]),
    "torch": (0.1, ["tocha", "tochas"]),
    "bucket": (9.0, ["balde", "baldes"]),
    "water_bucket": (9.5, ["balde de água", "baldes de água"]),
    "wooden_pickaxe": (0.3, ["picareta de madeira", "picaretas de madeira"]),
    "wooden_sword": (0.3, ["espada de madeira", "espadas de madeira"]),
    "stone_sword": (0.7, ["espada de pedra", "espadas de pedra"]),
    "stone_axe": (0.8, ["machado de pedra", "machados de pedra"]),
    "iron_hoe": (6.2, ["enxada de ferro", "enxadas de ferro"]),
    "golden_pickaxe": (15.0, ["picareta de ouro", "picaretas de ouro"]),
    "cooked_chicken": (1.5, ["frango assado", "frangos assados", "carne de galinha assada"]),
    "cooked_salmon": (1.5, ["salmão assado", "salmões assados"]),
    "cooked_cod": (1.5, ["bacalhau assado", "bacalhaus assados", "peixe assado", "peixes assados"]),
    "baked_potato": (0.7, ["batata assada", "batatas assadas"]),
    "golden_carrot": (3.5, ["cenoura dourada", "cenouras douradas"]),
    "cookie": (0.3, ["biscoito", "biscoitos", "cookie", "cookies"]),
    "cake": (7.0, ["bolo", "bolos"]),
    "amethyst_shard": (2.0, ["fragmento de ametista", "fragmentos de ametista", "ametista", "ametistas"]),
    "coal_block": (9.0, ["bloco de carvão", "blocos de carvão"]),
    "emerald_block": (90.0, ["bloco de esmeralda", "blocos de esmeralda"]),
    "diamond_block": (180.0, ["bloco de diamante", "blocos de diamante"]),
    "lantern": (4.0, ["lanterna", "lanternas"]),
}

FLOWERS = [M + x for x in ("dandelion", "poppy", "blue_orchid", "allium", "azure_bluet", "red_tulip", "oxeye_daisy", "cornflower",
                           "lily_of_the_valley", "sunflower")]
SEEDS = [M + x for x in ("wheat_seeds", "beetroot_seeds", "pumpkin_seeds", "melon_seeds")]


def w(*names):
    return [M + n for n in names]


def r(item, count=1, units=4, flavor="", margin=1.5, price=None):
    return dict(item=M + item, count=count, units=units, flavor=flavor, margin=margin, price=price)


# perfil -> (aceita, [receitas]); a 1a receita e a PADRAO ("quer trocar?" sem pedir nada).
CATALOG = {
    M + "pig": (w("carrot", "potato", "beetroot"), [
        r("apple"), r("brown_mushroom", 2, flavor="Fucei isso debaixo das árvores."), r("mud", 4, flavor="Do melhor lamaçal.")]),
    M + "cow": (w("wheat", "hay_block"), [
        r("leather", 2), r("leather_boots", 1, 2), r("leather_helmet", 1, 2), r("leather_leggings", 1, 2), r("leather_chestplate", 1, 2),
        r("dandelion", 2, flavor="Colhi no pasto, para você.")]),
    M + "mooshroom": (w("wheat", "hay_block"), [
        r("red_mushroom", 3), r("brown_mushroom", 3), r("mushroom_stew", 1, 2, "Ainda quentinho."), r("leather", 1)]),
    M + "chicken": (SEEDS, [r("egg", 2), r("feather", 3, flavor="Troquei de penas ontem.")]),
    M + "sheep": (w("wheat", "hay_block"), [
        r("white_wool", 2), r("azure_bluet", 1), r("oxeye_daisy", 1), r("cornflower", 1)]),
    M + "goat": (w("wheat", "hay_block"), [
        r("goat_horn", 1, 2, "Ganhei numa cabeçada numa pedra boa."), r("snowball", 8)]),
    M + "horse": (w("apple", "carrot", "sugar", "golden_apple", "wheat", "hay_block"), [
        r("lead", 1, 2), r("oxeye_daisy", 1), r("poppy", 1)]),
    M + "camel": (w("wheat", "hay_block", "melon_slice"), [
        r("cactus", 3, flavor="Espinhoso, mas faz bem no deserto."), r("sandstone", 4), r("dead_bush", 2)]),
    M + "llama": (w("wheat", "hay_block"), [r("leather", 2)]),
    M + "rabbit": (w("carrot", "dandelion"), [r("rabbit_hide", 2)]),
    M + "cat": (w("cod", "salmon"), [
        r("string", 2, flavor="Brinquei com isso a manhã inteira."), r("feather", 2), r("rabbit_hide", 1),
        r("phantom_membrane", 1, 2, "Achei no telhado de manhã.")]),
    M + "wolf": (w("beef", "porkchop", "chicken"), [r("bone", 2), r("rabbit_hide", 1), r("feather", 2)]),
    M + "fox": (w("chicken", "salmon", "cod"), [r("sweet_berries", 4, flavor="Colhi hoje cedo."), r("feather", 2), r("rabbit_hide", 1)]),
    M + "bee": (FLOWERS + w("sugar"), [r("honeycomb", 1, flavor="Direto da colmeia.")]),
    M + "panda": (w("bamboo"), [r("slime_ball", 1, flavor="Espirrei isso sem querer.")]),
    M + "turtle": (w("seagrass", "kelp"), [r("turtle_scute", 1, 3, "Larguei quando cresci.")]),
    M + "parrot": (SEEDS, [r("feather", 3, flavor="Troquei de penas ontem.")]),
    M + "frog": (w("slime_ball", "spider_eye"), [r("lily_pad", 2), r("ochre_froglight", 1, 2, "De um cubo de magma que engoli.")]),
    M + "strider": (w("warped_fungus"), [r("string", 4, flavor="Tecido na lava, de tanto andar.")]),
    M + "squid": (w("cod", "salmon", "kelp"), [r("ink_sac", 2, flavor="Soltei isso no susto.")]),
    M + "cod": (w("kelp", "seagrass"), [
        r("prismarine_shard", 1, flavor="Achei isso nas profundezas do oceano."), r("sea_pickle", 2), r("prismarine_crystals", 1, 3)]),
    M + "salmon": (w("kelp", "seagrass"), [r("clay_ball", 4, flavor="Do fundo do rio, rio acima."), r("gravel", 2)]),
    M + "zombie": (w("beef", "porkchop", "chicken"), [
        r("carrot", 2), r("potato", 2), r("iron_ingot", 1, 3, "Achei no bolso de alguém.")]),
    M + "skeleton": (w("feather", "flint", "stick"), [r("arrow", 4), r("bone", 2), r("bow", 1, 2)]),
    M + "spider": (w("porkchop", "beef", "chicken", "rotten_flesh"), [
        r("cobweb", 1, 8, "Teci isso na noite passada."), r("string", 3), r("spider_eye", 2)]),
    M + "witch": (w("nether_wart", "spider_eye", "lapis_lazuli"), [
        r("redstone", 3), r("glowstone_dust", 2), r("gunpowder", 2), r("glass_bottle", 3)]),
    M + "blaze": (w("coal", "charcoal"), [
        r("blaze_rod", 1, 2, "Arrancado do meu próprio fogo."), r("blaze_powder", 2)]),
    M + "piglin": (w("gold_ingot", "gold_nugget", "gold_block"), [
        r("iron_nugget", 9), r("string", 6), r("obsidian", 1, 2), r("quartz", 4), r("leather", 2), r("fire_charge", 2)]),
    M + "enderman": (w("grass_block", "sand", "gravel", "clay", "pumpkin", "melon"), [
        r("ender_pearl", 1, 2, "Guardo isso desde o Fim.")]),
    M + "iron_golem": (FLOWERS + w("vine", "pumpkin"), [
        r("iron_ingot", 3, flavor="Esse ferro saiu do meu próprio corpo; já fiz o conserto, pode levar."),
        r("iron_block", 1, 2, margin=1.1), r("iron_helmet", 1, 2, margin=1.2), r("iron_chestplate", 1, 2, margin=1.2),
        r("iron_leggings", 1, 2, margin=1.2), r("iron_boots", 1, 2, margin=1.2),
        r("cobblestone", 8), r("stone_bricks", 4)]),
    M + "wither_skeleton": (w("gold_ingot", "gold_block", "diamond", "diamond_pickaxe", "diamond_sword", "netherite_scrap", "emerald"), [
        r("wither_skeleton_skull", 1, 3, margin=1.0, price=60.0), r("nether_star", 1, 1, "Ela estava guardada com as caveiras; só os nossos sabem onde.", margin=1.1), r("coal", 3), r("bone", 3)]),
    V + "farmer": (w("hay_block", "wheat", "carrot", "potato", "beetroot"), [
        r("emerald", margin=1.3), r("bread", 3), r("pumpkin_pie", 1, 3)]),
    V + "toolsmith": (w("coal", "iron_ingot", "charcoal"), [
        r("emerald", margin=1.3), r("iron_pickaxe", 1, 2), r("iron_axe", 1, 2), r("iron_shovel", 1, 2), r("stone_pickaxe", 1)]),
    V + "armorer": (w("coal", "iron_ingot", "diamond"), [
        r("emerald", margin=1.3), r("iron_helmet", 1, 2), r("iron_chestplate", 1, 2), r("iron_leggings", 1, 2), r("iron_boots", 1, 2),
        r("shield", 1, 2)]),
    V + "weaponsmith": (w("coal", "iron_ingot", "diamond"), [r("emerald", margin=1.3), r("iron_sword", 1, 2), r("iron_axe", 1, 2)]),
    V + "librarian": (w("paper", "ink_sac", "feather"), [r("emerald", margin=1.3), r("book", 1)]),
    V + "cleric": (w("rotten_flesh", "gold_ingot", "nether_wart"), [r("emerald", margin=1.3), r("lapis_lazuli", 3), r("redstone", 4)]),
    V + "fisherman": (w("string", "coal", "cod", "salmon"), [r("emerald", margin=1.3), r("fishing_rod", 1, 2), r("oak_boat", 1, 2)]),
    V + "shepherd": (w("white_wool", "wheat"), [r("emerald", margin=1.3), r("shears", 1, 2), r("white_bed", 1, 2)]),
    V + "butcher": (w("porkchop", "beef", "chicken", "coal"), [
        r("emerald", margin=1.3), r("cooked_beef", 2, margin=1.2), r("cooked_porkchop", 2, margin=1.2)]),
    V + "fletcher": (w("stick", "flint", "feather", "string"), [r("emerald", margin=1.3), r("arrow", 8), r("bow", 1, 2)]),
    V + "leatherworker": (w("leather"), [
        r("emerald", margin=1.3), r("leather_helmet", 1, 2), r("leather_chestplate", 1, 2), r("leather_leggings", 1, 2),
        r("leather_boots", 1, 2), r("saddle", 1, 1)]),
    V + "cartographer": (w("paper", "string"), [r("emerald", margin=1.3), r("item_frame", 1, 3)]),
    V + "mason": (w("clay_ball", "brick"), [r("emerald", margin=1.3), r("stone_bricks", 8), r("terracotta", 4)]),
}


def recipe_id(profile, item):
    short = profile.split(":")[1].replace("#", "_")
    return f"{short}_{item.split(':')[1]}_v2"


def merged_values():
    data = json.load(open(VALUES_PATH, encoding="utf-8"))
    items = data["items"]
    for k, (v, names) in NEW_VALUES.items():
        items[M + k] = {"value": v, "pt": names}
    return data


def write_values(data):
    lines = [f'    {json.dumps(k)}: {json.dumps(v, ensure_ascii=False)}' for k, v in data["items"].items()]
    text = '{\n  "_comment": ' + json.dumps(data["_comment"], ensure_ascii=False) + ',\n  "items": {\n' + ",\n".join(lines) + "\n  }\n}\n"
    open(VALUES_PATH, "w", encoding="utf-8").write(text)


def price_of(rec, values):
    if rec["price"] is not None:
        return rec["price"]
    return math.ceil(rec["count"] * values[rec["item"]]["value"] * rec["margin"] * 2 - 1e-9) / 2


def write_trades(values):
    out = []
    for profile, (_, recs) in CATALOG.items():
        for rec in recs:
            o = {"id": recipe_id(profile, rec["item"]), "profile": profile, "price": price_of(rec, values), "item": rec["item"],
                 "count": rec["count"], "units": rec["units"], "offer": 4800 if "wither" in profile else 2400}
            if rec["flavor"]:
                o["flavor"] = rec["flavor"]
            out.append(o)
    lines = [json.dumps(o, ensure_ascii=False) for o in out]
    text = ('{\n  "_comment": "Catalogo FECHADO de trocas por criatura (gerado a partir do catalogo por criatura). price = valor POR UNIDADE (pontos de '
            'values.json) em itens que a criatura aceita; item tem de estar em profile.drops. A 1a receita de cada perfil e a padrao. '
            'units = maximo de unidades por negocio.",\n  "recipes": [\n    ' + ",\n    ".join(lines) + "\n  ]\n}\n")
    open(TRADES_PATH, "w", encoding="utf-8").write(text)
    return len(out)


def apply_to_profiles(profiles):
    """Sobrescreve wants/drops dos perfis com o catalogo (uma unica fonte)."""
    by_id = {p["id"]: p for p in profiles}
    for profile, (wants, recs) in CATALOG.items():
        if profile not in by_id:
            raise SystemExit("sem perfil para " + profile)
        by_id[profile]["wants"] = wants
        by_id[profile]["drops"] = list(dict.fromkeys(rec["item"] for rec in recs))
