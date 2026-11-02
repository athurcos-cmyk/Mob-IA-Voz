# -*- coding: utf-8 -*-
"""Reescreve as familias de conversa de ambiente reprovadas pelo juiz como CENAS INTEIRAS: cada cena e uma troca de tres falas (abertura, resposta, fecho)
que combinam entre si, com as condicoes dela (hora, clima, bioma). O motor passa a sortear so entre cenas inteiras, nunca falas soltas.
Saida: tools/gen/ambient_cenas.json  {id: [{"a","b","c","need":[...]}]}  (aplicado por genambient.py).

Uso (precisa da chave da API; o sidecar NAO precisa estar ligado):
    set MOBVOZ_PROVIDER=api
    python tools/gen/reescrever_cenas.py dados_do_juiz.json [--min=4.0] [--so=id1,id2]
  dados_do_juiz.json vem de:  python ai-service/tests/battery_families.py rel.md --json=dados_do_juiz.json
Depois: python tools/gen/genambient.py, os testes de unidade e o juiz de novo so nessas familias; o que nao melhorou volta (ver aplicar_melhores.py)."""
import concurrent.futures as cf
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "ai-service"))
sys.path.insert(0, str(Path(__file__).resolve().parent))
from app import llm  # noqa: E402
from reescrever_familias import GENDERED, HOSTILE, VAGUE, forbidden_for  # noqa: E402

AMB = ROOT / "mod-fabric" / "src" / "main" / "resources" / "mobvoz" / "ambient.json"
OUT = Path(__file__).with_name("ambient_cenas.json")
MODEL = "deepseek-v4-pro"

# condicoes que o jogo SABE verificar neste momento; a cena so pode afirmar o que a condicao garante
GROUPS = [("day", "night", "dusk", "dawn"), ("clear", "rain", "thunder")]
BIOMES = ("snow", "desert", "jungle", "swamp", "forest", "mountain", "beach", "savanna")
EXTRA = ("fullmoon", "newmoon", "deep", "high")
NETHER_END = {"nether", "blaze", "ghast", "piglin", "piglin_brute", "hoglin", "zombified_piglin", "strider", "magma_cube", "wither_skeleton", "shulker", "endermite"}
# palavra na fala -> condicao que a torna verdadeira (a cena SO pode citar o clima/hora se declarar a condicao)
WORDS = [(r"chuv|chover|chovendo|garoa|molhad", "rain"), (r"trov|raio|tempestade|relampag", "thunder"), (r"\bnoite|noturn|escuro lá fora|lua\b", "night"),
         (r"amanhec|madrugada|nascer do sol", "dawn"), (r"entardec|pôr do sol|poente|fim de tarde", "dusk"), (r"\bneve|nevand|gelad|\bfrio\b", "snow"),
         (r"deserto|areia", "desert"), (r"selva", "jungle"), (r"pântano|brejo", "swamp"), (r"\bsol\b|ensolarad|céu limpo|céu aberto", "clear")]

SYSTEM = (
    "Você escreve CENAS de conversa AMBIENTE de um mod de Minecraft: duas criaturas conversam entre si, em português do Brasil falado, e um jogador as ouve. "
    "Cada CENA é uma troca COMPLETA de três falas que combinam entre si: 'a' = quem abre fala algo concreto; 'b' = o OUTRO responde DIRETAMENTE ao que 'a' disse "
    "(se 'a' é pergunta, 'b' responde a pergunta; se é observação, 'b' reage à MESMA coisa citando-a); 'c' = quem abriu fecha a conversa de forma natural, "
    "ligada ao que foi dito (sem repetir a resposta, sem fecho genérico solto).\n"
    "Regras:\n"
    "1) Escreva 8 cenas DIFERENTES entre si, todas dentro do tema da família, cada uma com uma micro-situação concreta e específica (cite a coisa pelo nome). "
    "Nada vago, nada filosófico, nada de provérbio humano. Nunca respostas vazias ('Também acho', 'Verdade', 'Concordo').\n"
    "2) Cada fala tem de 3 a 14 palavras, no máximo 85 caracteres, SEM travessão, SEM emoji, SEM adjetivo com gênero para a própria criatura "
    "(diga 'cheio de orgulho', não 'orgulhosa').\n"
    "3) Respeite o mundo de quem fala: bichos e criaturas do mar/ar/mata NÃO falam de vila, troca, livro, cama, pão, poção, aldeão, golem, horta; "
    "monstros e criaturas do Nether NÃO são simpáticos (nada de amigo, obrigado, bonito, lindo, por favor, bom dia, gentil, prazer) e do Nether não se fala de sol, "
    "chuva nem hora do mundo de cima. Fatos de Minecraft CORRETOS, sem inventar mecânicas.\n"
    "4) CONDIÇÕES: o jogo só deixa a cena rolar se o campo 'need' dela for verdadeiro agora. Se uma fala cita chuva, noite, dia, sol, neve, deserto etc., a cena "
    "TEM de declarar a condição correspondente em 'need'; se não cita nada disso, deixe 'need' vazio. Condições permitidas: __ALLOWED__. "
    "Use no máximo 2 condições por cena e nunca duas do mesmo grupo (day/night/dusk/dawn; clear/rain/thunder). No MÁXIMO 4 das 8 cenas têm condição; pelo menos 4 ficam "
    "com 'need' VAZIO (falas que servem em qualquer momento, sem citar clima, hora nem bioma).\n"
    "5) Slots opcionais (só se a família já os usa nas falas atuais): {jogador} (o humano, só como sujeito) e {Jogador} no início de frase.\n"
    "6) Corrija TODOS os problemas apontados pelo juiz nas cenas atuais.\n"
    "Responda SOMENTE JSON {\"cenas\": [{\"a\": \"...\", \"b\": \"...\", \"c\": \"...\", \"need\": [\"...\"]}, ...]}."
)
SCHEMA = {"type": "object", "required": ["cenas"], "properties": {"cenas": {"type": "array", "items": {
    "type": "object", "required": ["a", "b", "c", "need"],
    "properties": {"a": {"type": "string"}, "b": {"type": "string"}, "c": {"type": "string"}, "need": {"type": "array", "items": {"type": "string"}}}}}}}


def allowed_for(fam):
    """Condicoes extras que esta familia pode ter por cena (sem contradizer as da familia; nada de ceu no Nether/End)."""
    if {fam["who"][0], fam["who"][1]} & NETHER_END:
        return []
    out = []
    own = {n.lstrip("!") for n in fam["need"]}
    for g in GROUPS:
        if not own & set(g):
            out += list(g)
    if not own & set(BIOMES):
        out += list(BIOMES)
    out += [e for e in EXTRA if e not in own]
    return out


def clean_scene(sc, fam, allowed, forbid):
    a, b, c = (" ".join(str(sc.get(k, "")).split()).replace("—", ",").replace("–", ",") for k in "abc")
    need = [n for n in dict.fromkeys(sc.get("need") or []) if n in allowed]
    for g in GROUPS:  # nunca duas do mesmo grupo
        mine = [n for n in need if n in g]
        if len(mine) > 1:
            return None
    if len(need) > 2 or len(need) != len(set(sc.get("need") or []) & set(allowed)):
        return None
    for t in (a, b, c):
        low = " " + t.lower() + " "
        if not 8 <= len(t) <= 95 or any(w in low for w in forbid) or any(g in low for g in GENDERED):
            return None
        for pat, tag in WORDS:  # cita clima/hora/bioma sem declarar a condicao => a cena mentiria quando a condicao nao vale
            if re.search(pat, low) and tag not in need and tag not in {n.lstrip("!") for n in fam["need"]}:
                if not (tag in ("night", "dusk", "dawn") and ("deep" in need or "deep" in fam["need"])):
                    return None
    if any(b.lower().strip(" .!").startswith(v) and len(b.split()) <= 4 for v in VAGUE) or a == b or b == c:
        return None
    slots = set(re.findall(r"\{([^}]*)\}", a + b + c))
    if slots - {"jogador", "Jogador"} and not (slots <= {"jogador", "Jogador", "item", "Item", "armadura", "coisa", "Coisa", "nome", "Nome"}):
        return None
    for s in slots:  # so os espacos que a familia ja usa nas falas atuais
        if "{" + s + "}" not in json.dumps(fam["a"] + fam["b"] + fam["c"], ensure_ascii=False) and s not in ("jogador", "Jogador"):
            return None
    return {"a": a, "b": b, "c": c, "need": need}


def rewrite(item):
    fam, data = item
    probs = []
    for c in data["cenas"]:
        if c["nota"] <= 3 or c["problemas"]:
            probs += c["problemas"][:2]
    probs = list(dict.fromkeys(probs))[:10]
    allowed = allowed_for(fam)
    forbid = forbidden_for(fam)
    user = {"quem": fam["who"], "tema_id": fam["id"], "condicoes_da_familia": fam["need"], "condicoes_extras_permitidas": allowed, "nota_do_juiz": data["nota"],
            "aberturas_atuais": fam["a"][:8], "respostas_atuais": fam["b"][:8], "fechos_atuais": fam["c"][:6], "problemas_apontados_pelo_juiz": probs}
    system = SYSTEM.replace("__ALLOWED__", ", ".join(allowed) if allowed else "nenhuma (deixe 'need' sempre vazio)")
    msgs = [{"role": "system", "content": system}, {"role": "user", "content": json.dumps(user, ensure_ascii=False)}]
    best = []
    for _ in range(4):
        try:
            d = json.loads(llm.chat_api(msgs, MODEL, 150, SCHEMA, {"temperature": 0.7, "num_predict": 2500}))
            ok, seen = [], set()
            for sc in d["cenas"]:
                r = clean_scene(sc, fam, allowed, forbid)
                if r and r["a"] not in seen:
                    seen.add(r["a"])
                    ok.append(r)
            if sum(1 for r in ok if not r["need"]) < 3:  # tem de haver cenas SEM condicao: a familia precisa poder acontecer em qualquer momento
                continue
            if len(ok) > len(best):
                best = ok
            if len(best) >= 6:
                break
        except (llm.LlmError, ValueError, KeyError, TypeError):
            continue
    return fam["id"], (best if len(best) >= 5 else None)


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    src = Path(sys.argv[1])
    minimo = float(next((a.split("=")[1] for a in sys.argv if a.startswith("--min=")), 4.0))
    so = next((set(a.split("=")[1].split(",")) for a in sys.argv if a.startswith("--so=")), None)
    judged = json.loads(src.read_text(encoding="utf-8"))
    fams = {f["id"]: f for f in json.loads(AMB.read_text(encoding="utf-8"))["families"]}
    old = json.loads(OUT.read_text(encoding="utf-8")) if OUT.exists() else {}
    todo = [(fams[i], d) for i, d in judged.items() if i in fams and d["nota"] < minimo and not fams[i].get("solo") and (not so or i in so)]
    print(len(todo), "familias a reescrever como cenas")
    with cf.ThreadPoolExecutor(6) as ex:
        for fid, res in ex.map(rewrite, todo):
            if res:
                old[fid] = res
            else:
                print("falhou:", fid)
    OUT.write_text(json.dumps(old, ensure_ascii=False, indent=1), encoding="utf-8")
    print(len(old), "familias com cenas inteiras em", OUT)


if __name__ == "__main__":
    main()
