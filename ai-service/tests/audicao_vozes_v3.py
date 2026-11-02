"""Segunda rodada de amostras de voz, depois do feedback do dono: so as criaturas que ele NAO aprovou na rodada 1, com as misturas mais distintas e menos efeito.
Quem tem efeito sai em duas versoes: A = sem efeito nenhum, B = efeito bem leve. Mais algumas criaturas novas (aranha, creeper, enderman, bruxa, gato, raposa).

Aprovados na rodada 1 (ficam como estao na proposta): ovelha, cavalo, lobo, aldeao fazendeiro, aldea bibliotecaria, vex.
Uso:  python tests/audicao_vozes_v3.py [pasta]     (padrao: docs/relatorios/audicao_vozes_v3). Prioridade baixa; ~1 minuto de CPU."""
import importlib.util
import sys
from pathlib import Path

import numpy as np

HERE = Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location("v2", HERE / "audicao_vozes_v2.py")
v2 = importlib.util.module_from_spec(spec)
sys.path.insert(0, str(HERE.parent))
spec.loader.exec_module(v2)

# efeitos MUITO leves (a rodada 1 pesou a mao e o dono nao gostou)
LEVES = {
    "": lambda x: x,
    "reverb_leve": lambda x: v2.reverb(x, 0.7, 0.12),
    "filtro_leve": lambda x: v2.lowpass(x, 4200),
    "oco_leve": lambda x: v2.echo(v2.lowpass(x, 6000), 40, 0.18),
    "tremolo_leve": lambda x: v2.tremolo(x, 16, 0.10),
    "rouco_leve": lambda x: v2.drive(x, 1.4),
    "sombrio_leve": lambda x: v2.ring(v2.reverb(x, 0.8, 0.15), 40, 0.07),
}

# nome, linha, [ (rotulo, mistura, tom, velocidade, efeito) ... ]
CREATURES = [
    ("vaca", "Muuu... eu só quero pastar em paz, humano.", [("nova", {"pf_dora": 1.0}, 0.84, 0.88, "")]),
    ("porco", "Eu topo trocar, mas só se for cenoura das boas.", [("nova", {"pm_alex": 1.0}, 1.12, 1.10, "")]),
    ("galinha", "Có! Tem semente? Eu troco pena por semente.", [("nova", {"pf_dora": 0.5, "pm_alex": 0.5}, 1.12, 1.15, "")]),
    ("piglin", "Ouro! Ouro! Onde tem ouro?", [("A_sem_efeito", {"pm_santa": 0.55, "pm_alex": 0.45}, 0.88, 1.12, ""),
                                           ("B_rouco_leve", {"pm_santa": 0.55, "pm_alex": 0.45}, 0.88, 1.12, "rouco_leve")]),
    ("golem_de_ferro", "Eu protejo esta vila. Foi feito de ferro, para durar.", [("A_sem_efeito", {"pm_santa": 1.0}, 0.74, 0.88, ""),
                                                                              ("B_reverb_leve", {"pm_santa": 1.0}, 0.74, 0.88, "reverb_leve")]),
    ("zumbi", "Carne... eu só sinto fome, humano.", [("A_sem_efeito", {"pm_santa": 0.85, "pm_alex": 0.15}, 0.84, 0.80, ""),
                                                    ("B_filtro_leve", {"pm_santa": 0.85, "pm_alex": 0.15}, 0.84, 0.80, "filtro_leve")]),
    ("esqueleto", "Minha mira nunca falha. Quer testar?", [("A_sem_efeito", {"pm_alex": 0.6, "pm_santa": 0.4}, 0.98, 1.0, ""),
                                                         ("B_oco_leve", {"pm_alex": 0.6, "pm_santa": 0.4}, 0.98, 1.0, "oco_leve")]),
    ("warden", "Eu escuto o seu coração daqui.", [("A_sem_efeito", {"pm_santa": 1.0}, 0.60, 0.78, ""), ("B_reverb_leve", {"pm_santa": 1.0}, 0.60, 0.78, "reverb_leve")]),
    ("wither", "Tudo vai virar cinzas ao meu redor.", [("A_sem_efeito", {"pm_santa": 1.0}, 0.66, 0.85, ""), ("B_sombrio_leve", {"pm_santa": 1.0}, 0.66, 0.85, "sombrio_leve")]),
    ("abelha", "Que dia lindo para voar entre as flores!", [("A_sem_efeito", {"pf_dora": 0.8, "pm_alex": 0.2}, 1.32, 1.2, ""),
                                                          ("B_tremolo_leve", {"pf_dora": 0.8, "pm_alex": 0.2}, 1.32, 1.2, "tremolo_leve")]),
    # novos
    ("aranha", "Minha teia pega tudo o que passa por aqui.", [("nova", {"pf_dora": 0.55, "pm_santa": 0.45}, 0.98, 0.95, "")]),
    ("creeper", "Cuidado onde você pisa, humano.", [("nova", {"pm_alex": 0.7, "pf_dora": 0.3}, 1.0, 0.92, "")]),
    ("enderman", "Não me olhe nos olhos. Eu não gosto.", [("nova", {"pm_santa": 0.7, "pf_dora": 0.3}, 0.78, 0.85, "")]),
    ("bruxa", "Uma poção para você? Custa uma esmeralda.", [("nova", {"pf_dora": 0.85, "pm_santa": 0.15}, 0.90, 1.0, "")]),
    ("gato", "Se tiver peixe, eu penso no seu pedido.", [("nova", {"pf_dora": 0.6, "pm_alex": 0.4}, 1.10, 1.05, "")]),
    ("raposa", "Rápida como o vento, esperta como poucos.", [("nova", {"pf_dora": 0.45, "pm_alex": 0.55}, 1.18, 1.12, "")]),
]


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    out = Path(sys.argv[1]) if len(sys.argv) > 1 else HERE.parents[1] / "docs" / "relatorios" / "audicao_vozes_v3"
    out.mkdir(parents=True, exist_ok=True)
    linhas = ["# Audição, rodada 2: só o que não foi aprovado", "",
              "Aprovados na rodada 1 (ficam): ovelha, cavalo, lobo, aldeão fazendeiro, aldeã bibliotecária, vex.", "",
              "Quem tinha efeito sai em **A (sem efeito nenhum)** e **B (efeito bem leve)**. Os novos e os de mistura mais distinta saem em uma versão só (`nova`).", "",
              "| # | criatura | versão | mistura (dora = aguda, alex = média, santa = grave) | tom | velocidade | efeito |", "|---|---|---|---|---|---|---|"]
    n = 0
    for nome, texto, versoes in CREATURES:
        n += 1
        for rot, mix, pitch, speed, fx in versoes:
            try:
                a, rate = v2.kokoro_blend(texto, mix, pitch, speed)
                a = LEVES[fx](a)
                v2.save(out / f"{n:02d}_{nome}_{rot}.wav", a, rate)
                linhas.append(f"| {n} | {nome.replace('_', ' ')} | {rot} | " + ", ".join(f"{v} {int(w * 100)}%" for v, w in mix.items()) + f" | {pitch} | {speed} | {fx or '-'} |")
                print("ok", nome, rot)
            except Exception as e:  # noqa: BLE001
                print("falhou", nome, rot, e)
    (out / "LEIAME.md").write_text("\n".join(linhas) + "\n", encoding="utf-8")
    print("pronto em", out)


if __name__ == "__main__":
    main()
