# 19 — Mundo vivo e o nome do jogador

## O nome do jogador (cada criatura tem a sua consciência)

- Para uma criatura que não sabe o nome do jogador, ele é só **"Humano"** (o tratamento do perfil: "Mortal", "Terrestre"… em algumas espécies). Ela o chama assim quando soa natural, sem repetir em toda fala, e pode **perguntar o nome** (uma vez) se combinar com a personalidade.
- Quando o jogador **diz** o nome ("meu nome é Bia", "me chamo Bia", "pode me chamar de Bia", "eu sou a Bia") ou **responde a uma pergunta de nome** da criatura ("Bia."), **só aquela criatura** guarda o nome, na memória dela (`MobMemory.names`, persiste no save). A detecção é por regra no servidor (`PlayerNames`), não pelo LLM: só letras, até 20 caracteres, 2 palavras, e palavras comuns ("humano", "fazendeiro", "sim"…) nunca viram nome.
- Daí em diante o prompt dessa criatura diz o nome e ela passa a usá-lo no lugar de "Humano". As outras continuam sem saber.
- O nome também **se espalha por fofoca**: se uma criatura que sabe o nome o diz em voz alta numa conversa ambiente, a outra o aprende (ver abaixo).

## Mundo vivo: conversas ambiente entre criaturas

Perto do jogador (até 16 blocos, no Overworld), duas criaturas calmas (nada hostil, sem alvo, fora de conversa com o jogador) a até 6 blocos uma da outra conversam sozinhas: 2 ou 3 falas, voz do TTS local e legenda. **Sem IA**: as falas são prontas e combinam (abertura × resposta × fecho) com espaços variáveis; custo zero de LLM.

- **Só conversa quem faz sentido.** Aldeão com aldeão (e o golem da vila, só nos assuntos feitos para ele); animais de fazenda e cavalos entre si; lobo e gato, e os demais animais, só com a **mesma espécie**; monstros (zumbi, esqueleto, aranha, creeper, enderman, bruxa, Warden) entre si, **de qualquer espécie** (zumbi com esqueleto, creeper com zumbi...), e só quando não têm alvo. Nunca aldeão com vaca, nem monstro com aldeão ou animal. Cada assunto fala só o que cabe a quem o diz: vaca não fala de trocar, aldeão não muge, zumbi não é simpático (`AmbientCoherenceTest` falha se uma fala usar palavras de outro mundo).
- **Repara no que você segura e veste.** Se quem abre a conversa GOSTA do que você segura (perfil da espécie), quer: "O humano está com trigos. Queria um pouco também." Aldeões comentam a sua armadura; monstros também ("Duro de morder"). O que você veste também entra nos fatos da cena para a IA (a vaca vê o trigo e pergunta se é para ela).
- **Só o que é verdade agora.** Cada assunto tem condições: hora (dia/entardecer/noite/amanhecer), clima, jogador ferido/com fome/segurando algo, e o que **cada criatura realmente sabe** do jogador (já o conhece? sabe o nome? tem fato verificado de troca, presente, devolução honesta, agressão, traição, ameaça, consolo, domesticação?). Nunca afirma o que não é verdade: sem fato de troca, ninguém comenta uma troca.
- **Sobre você.** Comentam o que você segura ("Olha só, o humano anda com uma espada de diamante"), seu estado, a presença à noite ou na chuva, quem já conversou com você e quem não, se sabem o seu nome e como o contaram uns aos outros. Quem não sabe diz "o humano".
- **Sobre o mundo.** Chuva, trovoada, sol, entardecer, noite, amanhecer; a vila (colheita, trocas, camas, zumbis, golem, ofícios de fazendeiro, bibliotecário, ferreiro, clérigo, pastor), bichos (pasto, cercado, fome, medo de lobo, galinha, ovelha, porco, vaca, lobo, gato, cavalo) e curiosidades (o mundo quadrado, o fundo das cavernas, portais, creepers, endermen, abelhas).
- **Variedade.** 55 famílias de assunto, ~1.400 cenas base; os espaços ({produto}, {lugar}…) multiplicam. O servidor evita repetir qualquer das últimas 80 cenas. O ritmo padrão é uma cena por região a cada 60 s no máximo (cada criatura espera o triplo) e no máximo 3 por minuto no total.
- **Não muda o jogo.** Só efeito colateral: ouvir o nome do jogador em voz alta faz a outra criatura aprendê-lo.
- **Boatos são ficção.** Algumas famílias de vila contam boatos genéricos ("soube que apareceu gente nova perto do rio"); são cor local, não fatos do mundo.

### Configuração (`config/mobvoz/server.json`)

| Chave | Padrão | Efeito |
|---|---|---|
| `ambientChatter` | `true` | liga/desliga as conversas ambiente (também exige `chatterEnabled`, que o preset `low` desliga) |
| `ambientCooldownSeconds` | `60` | intervalo mínimo entre conversas na mesma região (mín. 10) |

### Como acrescentar falas

Edite `tools/gen/genambient.py` (famílias: `fam(id, [quem abre, quem responde], [condições], aberturas, respostas, fechos)`) e rode `python genambient.py`; ele valida os espaços e as condições e escreve `ambient.json`. Depois `gradlew test` (`AmbientScenesTest` renderiza todas as combinações). Regras: falas curtas, sem travessão, **sem adjetivo com gênero** para a própria criatura nem para o jogador (use "essa pessoa", "o humano"), e todo espaço `{item}` exige a condição `holds`, `{nome}` exige `n0` ou `n1`.

### Bateria de coerência (com juiz de IA)

`AmbientCoherenceTest` exporta 200 cenas de 20 tipos de par (`build/ambient-samples.json`) e `ai-service/tests/battery_ambient.py` pede a um juiz de IA exigente que as avalie (faz sentido para ESSAS criaturas?). Relatório: `docs/relatorios/bateria_ambiente.md`. Primeira rodada: 3,83/5; depois de separar assuntos "de gente" dos de animais, de só usar resposta genérica ("Pois é.") após afirmação (nunca após pergunta) e de impedir que o golem caia em assuntos de aldeão: **3,98/5**; depois de liberar a conversa entre monstros de espécies diferentes (mais difícil de acertar), a rodada de 250 cenas em 25 tipos de par ficou em **3,85/5** (zumbi+esqueleto ~4,2, creeper+zumbi 3,6, aranha+zumbi 3,5, enderman+bruxa 3,2; animais de fazenda 3 a 4; lobo ~2,7 é o pior par). O juiz é antropocêntrico ("animais não falam do humano") e a nota serve para achar os piores casos, não como prova.

### Testes

`AmbientScenesTest` (unidade: conteúdo limpo, condições, nome, variedade) e `AmbientChatterGameTest` (em jogo: dois aldeões conversam sozinhos; a fofoca do nome chega ao outro e não à vaca distante; dizer o nome a uma vaca só a ela o ensina).

### O que falta

Ninguém jogou isso ainda: a frequência (40% de chance a cada 5 s por jogador, sujeita ao cooldown), os assuntos e o tom são palpites a ajustar jogando. As falas ambiente são do mesmo TTS das conversas; a **qualidade e a identidade da voz dos mobs** é um ponto à parte, anotado em `STATUS.md` para tratar depois.
