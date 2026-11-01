# -*- coding: utf-8 -*-
"""Reescreve as familias de conversa de ambiente que o juiz de IA reprovou, usando o que o proprio juiz apontou como feedback.
Saida: tools/gen/ambient_reescritas.json  {id: {"a": [...], "b": [...], "c": [...]}}  (aplicado por genambient.py no fim).

Uso (precisa da chave da API; o sidecar NAO precisa estar ligado):
    set MOBVOZ_PROVIDER=api
    python tools/gen/reescrever_familias.py dados_do_juiz.json [--min=4.0] [--so=id1,id2]
  dados_do_juiz.json vem de:  python ai-service/tests/battery_families.py rel.md --json=dados_do_juiz.json
Depois: python tools/gen/genambient.py, os testes de unidade e o juiz de novo (a reescrita pode piorar; so fica o que o juiz aprova).

Licao medida: pedir "respostas genericas que servem para qualquer abertura" NAO melhorou a nota (3,61 -> 3,60). O que funciona (as familias nota 4,3+) e girar
TODAS as falas em torno de UMA micro-situacao concreta, com respostas que citam a coisa."""
import concurrent.futures as cf
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "ai-service"))
from app import llm  # noqa: E402

AMB = ROOT / "mod-fabric" / "src" / "main" / "resources" / "mobvoz" / "ambient.json"
OUT = Path(__file__).with_name("ambient_reescritas.json")
MODEL = "deepseek-v4-pro"

SYSTEM = (
    "Você reescreve FALAS DE CONVERSA AMBIENTE de um mod de Minecraft: duas criaturas conversam entre si em português do Brasil falado, e um jogador as ouve. "
    "Uma FAMÍLIA tem três listas: 'a' = aberturas (quem fala primeiro), 'b' = respostas (quem responde), 'c' = fechos (quem fecha a conversa). "
    "O motor sorteia UMA abertura, UMA resposta e UM fecho, SEM olhar quais combinam. Por isso o segredo é: TODAS as falas da família giram em torno de UMA ÚNICA "
    "MICRO-SITUAÇÃO CONCRETA (ex.: 'o tridente rachado do afogado', 'o lago de lava que subiu', 'o bambu que acabou'). Escolha essa micro-situação (a mesma do tema atual, "
    "mas mais específica) e escreva:\n"
    "1) 'a': 6 a 8 jeitos DIFERENTES de abrir a MESMA micro-situação (o que se viu, o que aconteceu, o que se sente sobre ela). Todas afirmações ou observações concretas, "
    "com a voz da criatura, citando a coisa pelo nome (o tridente, a lava, o bambu). Nada vago, nada filosófico, nada de provérbio humano.\n"
    "2) 'b': 5 a 6 respostas CURTAS que reagem à MESMA micro-situação, CITANDO a coisa ou a ideia ('Esse tridente já viu dias melhores.', 'A lava sobe sem avisar.'). "
    "Cada resposta tem de fazer sentido depois de QUALQUER abertura. NUNCA respostas vazias do tipo 'Também acho', 'Verdade', 'Concordo'.\n"
    "3) 'c': 3 a 4 fechos curtos que encerram em tom da criatura, sem repetir a resposta ('Vou treinar mais, então.', 'Fica de olho nessa lava.').\n"
    "4) Cada fala de 3 a 12 palavras, no máximo 80 caracteres, SEM travessão, SEM emoji, SEM adjetivo com gênero para a própria criatura (diga 'cheio de orgulho', não 'orgulhosa').\n"
    "5) Respeite o mundo de quem fala: bichos e criaturas do mar/ar/mata NÃO falam de vila, troca, livro, cama, pão, poção, aldeão, golem, horta; monstros e criaturas do Nether "
    "NÃO são simpáticos (nada de amigo, obrigado, bonito, lindo, por favor, bom dia, gentil, prazer) e do Nether não se fala de sol, chuva nem hora do mundo de cima. "
    "Fatos de Minecraft CORRETOS e sem inventar mecânicas.\n"
    "6) Corrija TODOS os problemas apontados pelo juiz. Slots opcionais: {jogador} (o humano, só como sujeito) e {Jogador} no início de frase.\n"
    "Responda SOMENTE JSON {\"a\": [...], \"b\": [...], \"c\": [...]}."
)
SCHEMA = {"type": "object", "required": ["a", "b", "c"], "properties": {k: {"type": "array", "items": {"type": "string"}} for k in "abc"}}

# mesmas palavras que o AmbientCoherenceTest barra (mundo de quem fala)
NOT_FOR_ANIMALS = ["troca", "trocar", "negóci", "esmeralda", "estoque", "banca", "vila", "aldeão", "aldeões", "golem", " cama ", " porta ", "plantação", "horta", "colheita",
                   "armeiro", "cliente", "livro", "forja", "poção", "caldeirão", "pão", "fofoca"]
NOT_FOR_MONSTERS = ["prazer", "simpátic", "amigo", "obrigad", "bom dia", "boa noite", "por favor", "gente boa", "bonito", "lindo", "gentil", "honest", "troca", "vila",
                    "horta", "colheita", "muuu", "có "]
HOSTILE = {"monsters", "nether", "zombie", "skeleton", "spider", "creeper", "enderman", "witch", "warden", "slime", "cave_spider", "silverfish", "husk", "stray", "drowned",
           "guardian", "elder_guardian", "vex", "phantom", "pillager", "vindicator", "evoker", "ravager", "shulker", "endermite", "piglin", "piglin_brute", "hoglin",
           "zombified_piglin", "blaze", "ghast", "magma_cube", "wither_skeleton"}
# adjetivos com genero para a propria criatura que o modelo costuma escrever ("satisfeito", "orgulhosa")
GENDERED = ("satisfeit", "orgulhos", "cansad", "assustad", "sozinh", "animad", "ocupad")
VAGUE = ("também acho", "concordo", "verdade", "pois é", "com certeza")  # respostas vazias: nao contam


def forbidden_for(fam):
    if fam["who"][0] in HOSTILE or fam["who"][1] in HOSTILE:
        return NOT_FOR_MONSTERS
    if str(fam["who"][0]).startswith("villager") or fam["who"][0] in ("golem", "wandering_trader", "villagers"):
        return []
    return NOT_FOR_ANIMALS


def clean(lst, lo, hi, forbid=(), no_vague=False):
    out = []
    for t in lst:
        t = " ".join(str(t).split()).replace("—", ",").replace("–", ",")
        low = " " + t.lower() + " "
        if not t or not 8 <= len(t) <= 90 or t in out or any(w in low for w in forbid) or any(g in low for g in GENDERED):
            continue
        if no_vague and any(low.strip().startswith(v) and len(low.split()) <= 4 for v in VAGUE):
            continue
        out.append(t)
    return out if len(out) >= lo else None


def rewrite(item):
    fam, data = item
    probs = []
    for c in data["cenas"]:
        if c["nota"] <= 3 or c["problemas"]:
            probs += c["problemas"][:2]
    probs = list(dict.fromkeys(probs))[:10]
    user = {"quem": fam["who"], "tema_id": fam["id"], "condicoes": fam["need"], "nota_do_juiz": data["nota"],
            "aberturas_atuais": fam["a"], "respostas_atuais": fam["b"], "fechos_atuais": fam["c"], "problemas_apontados_pelo_juiz": probs}
    msgs = [{"role": "system", "content": SYSTEM}, {"role": "user", "content": json.dumps(user, ensure_ascii=False)}]
    for _ in range(3):
        try:
            d = json.loads(llm.chat_api(msgs, MODEL, 120, SCHEMA, {"temperature": 0.7, "num_predict": 1500}))
            forbid = forbidden_for(fam)
            a, b, c = clean(d["a"], 6, 10, forbid), clean(d["b"], 5, 8, forbid, True), clean(d["c"], 3, 6, forbid)
            if a and b and c:
                return fam["id"], {"a": a, "b": b, "c": c}
        except (llm.LlmError, ValueError, KeyError, TypeError):
            continue
    return fam["id"], None


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    src = Path(sys.argv[1])
    minimo = float(next((a.split("=")[1] for a in sys.argv if a.startswith("--min=")), 4.0))
    so = next((set(a.split("=")[1].split(",")) for a in sys.argv if a.startswith("--so=")), None)
    judged = json.loads(src.read_text(encoding="utf-8"))
    fams = {f["id"]: f for f in json.loads(AMB.read_text(encoding="utf-8"))["families"]}
    old = json.loads(OUT.read_text(encoding="utf-8")) if OUT.exists() else {}
    todo = [(fams[i], d) for i, d in judged.items() if i in fams and d["nota"] < minimo and not fams[i].get("solo") and (not so or i in so)]
    print(len(todo), "familias a reescrever")
    with cf.ThreadPoolExecutor(6) as ex:
        for fid, res in ex.map(rewrite, todo):
            if res:
                old[fid] = res
            else:
                print("falhou:", fid)
    OUT.write_text(json.dumps(old, ensure_ascii=False, indent=1), encoding="utf-8")
    print(len(old), "reescritas salvas em", OUT)


if __name__ == "__main__":
    main()
