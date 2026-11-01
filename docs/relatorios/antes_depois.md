# O que melhorou: antes × depois (passada longa de 2026-11-01)

Números tirados de `docs/relatorios/cobertura_antes.md` e `cobertura_depois.md` (gerados por `tools/cobertura.py`), do juiz de IA (`ai-service/tests/battery_families.py`) e dos testes em jogo.

## Resumo

| Item | Antes | Depois |
|---|---|---|
| Famílias de conversa de ambiente (papo entre criaturas) | 162 (300 com as de evento e abertura) | 282 (420 no total) |
| Falas de papo no total | 1.348 | 3.101 |
| Espécies com papo próprio | 19 de 73 | 65 de 73 |
| Quem conversa com quem | aldeão×aldeão, animais de fazenda e estimação, monstros comuns da mesma espécie; só no mundo comum | aldeão×aldeão, **qualquer bicho × qualquer bicho**, aquáticos entre si, voadores entre si, Nether entre si, monstros entre si; **em qualquer dimensão** |
| Espécies com perfil próprio (gostos, voz, medos) | 34 | 39 (+ hoglin, piglin zumbi, piglin bruto, ghast, cubo de magma) |
| Espécies com receita de troca | 32 | 37 (+ 9 receitas novas e 3 itens novos) |
| Mobs do Nether seguem, defendem e fazem aliança | não (ficavam de fora) | sim: piglin, piglin zumbi, hoglin, zoglin, esqueleto wither, blaze, strider (testado em jogo; o ghast recusa porque voa) |
| Recusa de seguir (aquático, voador, aldeão...) | uma frase genérica | motivo bom por grupo: aquáticos, voadores (inclui o ghast), vila, outros, Warden, ficar de guarda, lutar contra pacífico |
| Golfinho, lula e lula-brilhante | contavam como "calmos" de terra (falas de pasto e sol) | contam como aquáticos (descoberto pelo teste em jogo) |

## Por grupo (espécies com papo próprio e falas de papo)

| Grupo | Espécies | Papo próprio antes → depois | Falas de papo antes → depois |
|---|---|---|---|
| Fazenda | 8 | 8 → 8 | 121 → 210 |
| Estimação | 3 | 2 → 3 | 16 → 178 |
| Mata e outros terrestres | 11 | 0 → 11 | 0 → 318 |
| Aquáticos | 9 | 0 → 5 | 0 → 142 |
| Voadores | 4 | 0 → 4 | 0 → 166 |
| Aldeia | 4 | 2 → 2 | 382 → 382 |
| Monstros comuns | 6 | 6 → 6 | 196 → 298 |
| Outros hostis | 14 | 0 → 14 | 0 → 261 |
| Nether | 9 | 0 → 9 | 0 → 230 |
| End e chefes | 5 | 1 → 3 | 16 → 62 |

## Qualidade (juiz de IA, nota de 1 a 5)

- Famílias antigas: cerca de 4,3.
- Famílias novas: nota média **3,77** nas 113 que ficaram (43 com 4,0 ou mais, 89 com 3,5 ou mais). Abaixo das antigas.
- Três rodadas de reescrita assistida por IA não melhoraram (3,61 → 3,60 → 3,60), então foram tiradas 27 famílias abaixo de 3,3 (`tools/gen/ambient_removidas.json`).
- Causa medida: o motor sorteia abertura, resposta e fecho sem olhar se combinam. Famílias **sem condição** (126 das 140 novas) tiram 3,55; as com 1 condição, 3,93; com 2, 4,88. As reclamações mais comuns do juiz: fecho que não fecha (618), resposta que não responde à abertura (480), tema vago (387).
- Próximo passo para chegar a 4,3: escrever a mão os temas fracos ou fazer o motor casar resposta e fecho por etiqueta de tema.

## Testes

- Testes de unidade (Java e Python): passando.
- `AmbientMundoGameTest` (novo, em jogo): 12 duplas conversam sozinhas perto do jogador com 2 falas ou mais (abelha, papagaio, cabra, peixe, golfinho+peixe, piglin, hoglin, blaze, strider, zumbi, lobo+gato, vaca+lobo na ordem trocada). A cabra falhou 1 vez em 4 rodadas e passou nas outras (oscilação não explicada).
- `NetherAlliesGameTest` (novo): piglin, piglin zumbi, hoglin, esqueleto wither e blaze aceitam seguir e defender e vão até o jogador; o ghast recusa com um bom motivo.
- Não testado: microfone real, o botão de instalar dentro do exe, o aviso do SmartScreen, as recusas com a IA de verdade em jogo.
