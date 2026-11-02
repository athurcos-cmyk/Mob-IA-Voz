# Audição: voz de hoje × proposta (mistura das vozes + efeito)

Para cada criatura há dois arquivos: `NN_nome_hoje.wav` (como está no jogo agora: só 3 vozes, tom ±8%) e `NN_nome_proposta.wav` (mistura própria + efeito leve).

| # | criatura | mistura (dora = aguda, alex = média, santa = grave) | tom | efeito |
|---|---|---|---|---|
| 1 | vaca | pf_dora 70%, pm_alex 30% | 0.92 | - |
| 2 | porco | pm_alex 60%, pf_dora 40% | 1.06 | - |
| 3 | galinha | pf_dora 100% | 1.28 | - |
| 4 | ovelha | pf_dora 80%, pm_santa 20% | 1.06 | - |
| 5 | cavalo | pm_alex 50%, pm_santa 50% | 0.97 | - |
| 6 | lobo | pm_santa 65%, pm_alex 35% | 0.95 | - |
| 7 | aldeao fazendeiro | pm_alex 85%, pm_santa 15% | 1.0 | - |
| 8 | aldea bibliotecaria | pf_dora 60%, pm_alex 40% | 1.0 | - |
| 9 | golem de ferro | pm_santa 100% | 0.78 | metalico |
| 10 | zumbi | pm_santa 80%, pm_alex 20% | 0.88 | molhado |
| 11 | esqueleto | pm_alex 55%, pm_santa 45% | 1.0 | oco |
| 12 | piglin | pm_alex 50%, pm_santa 50% | 0.96 | rouco |
| 13 | warden | pm_santa 100% | 0.62 | eco_grave |
| 14 | wither | pm_santa 100% | 0.66 | sombrio |
| 15 | abelha | pf_dora 100% | 1.5 | agudo_tremulo |
| 16 | vex | pf_dora 90%, pm_alex 10% | 1.35 | etereo |

Texto falado por cada uma está em `tests/audicao_vozes_v2.py`. Diga quais soaram bem, quais ficaram exagerados (efeito demais) e quais ainda parecem iguais.
