# 17 — Interações naturais (domar por conversa, honestidade, cena, avisos, cumprimentos)

Princípio (AGENTS.md #4 e #18): **a IA entende livremente e propõe; o servidor valida e executa só ações fechadas e testadas.** Cada interação abaixo é uma ação nova e pequena, com limites e testes. Todas as falas são **geradas pela IA** a partir de fatos verificados pelo servidor.

Chave geral: `config/mobvoz/server.json` → `"naturalEvents": false` desliga as falas **espontâneas** (aviso de perigo, cumprimento, reação ao ovo e à honestidade). Conversar, domar e a memória continuam funcionando.

## 1. Domar por conversa (`TAME_ME`)

- Frases como *"aceita ser meu cavalo?"*, *"deixa eu te montar"*, *"vem ser meu lobinho de verdade"* viram o pedido `TAME_ME`.
- Criaturas domesticáveis: cavalo, burro, mula, lhama (não o camelo), lobo, gato, papagaio. Outras recebem uma resposta de "não é de se domar" (`IMPOSSIVEL`).
- O servidor decide pelo "calor" da criatura (confiança + afinidade − medo) contra o limiar da espécie **+ 0,10** (cavalo e lobo: 0,20; gato e papagaio: 0,50). Estranhos recusam.
- **Argumento:** o analisador dá a força do motivo (`appeal` 0 sem motivo, 1 plausível, 2 forte: *"prometo cuidar de você, um estábulo e comida"*). O servidor soma até **+0,20** ao calor (escalado pela credulidade) e **zero** se a criatura tem medo de quem pede. Um bom argumento ajuda quem já tem um mínimo de confiança; não faz milagre com um desconhecido.
- Aceitando, o jogo domestica de verdade (`tameWithName` / `tame`): o jogador vira o dono e monta **sem precisar de maçã nem de sela de domesticação**. Fica registrado como fato `TAMED` na memória dela (confiança sobe).
- O `appeal` vale também para *seguir*, *proteger* e *trégua*: um bom argumento ajuda do mesmo jeito, com o mesmo teto.
- Já tem dono → *"já é sua"* (ou *"já tem dono"* se for de outro jogador).

## 2. Honestidade lembrada (e o Dragão cita)

- Quando uma criatura **entrega** algo numa troca, o servidor guarda isso por 10 minutos de jogo. Se o jogador **devolve** esse tipo de item perto dela, sem oferta aberta e sem ela pedir, é uma **devolução honesta**.
- Efeitos verificados: fato `HONEST_RETURN` na memória dela (confiança +0,15), fato no **grupo** dela (todos os Endermen / todos da aldeia sabem) e uma linha no **registro do mundo**, que o Ender Dragon recebe (*"Registro do mundo: o jogador devolveu honestamente… a um(a) enderman"*). Ela reage em voz alta, gerada pela IA, no tom de *"raro um humano honesto, vou me lembrar disso"*.
- **Sem limite diário:** negociar e devolver quantas vezes quiser. Cada devolução consome a entrega que a originou, então só se repete negociando de novo (não há como farmar confiança sem pagar a troca).
- O Dragão só cita o que está no registro; não inventa.

## 3. Cena real nas conversas

A cada fala a criatura recebe até 5 fatos da cena: hora (dia, pôr do sol, noite, amanhecer), clima (chuva, tempestade), dimensão (Nether/Fim), vida e fome do jogador, o que ele segura (se for um item conhecido) e quem está por perto (por espécie), além de se **ela** está ferida. A IA usa isso **só quando combina** com o assunto e o temperamento (um medroso comenta a tempestade, um guerreiro nota a espada); nunca lista tudo.

## 4. Aviso de perigo do aliado

Um aliado (que segue ou protege) avisa o dono, em voz alta, quando um inimigo está a menos de 10 blocos e o mira (ou está a menos de 6 blocos). No máximo um aviso a cada 30 s por jogador, e a criatura tem seu próprio intervalo.

## 5. Cumprimento de quem a criatura conhece

Ao reencontrar o jogador a menos de 6 blocos, a criatura cumprimenta **só se o conhece** (presente, troca, ameaça, consolo, devolução honesta, domesticação, dano). Desconhecidos e hostis ficam quietos. No máximo 1 cumprimento por criatura por dia de jogo e 1 por minuto por jogador.

## 6. Seguir e proteger andam juntos; aliados

Seguir inclui proteger (e proteger sem estar de guarda também passa a seguir). O limite padrão é **15** aliados por jogador (máximo 32; presets não o reduzem).

## Testes

- Unidade (`NaturalInteractionsTest`): domar (limiar, argumento e teto, medo), honestidade (memória, grupo, Dragão), cena, regras de aviso/cumprimento.
- Em jogo (`NaturalInteractionsGameTest`): cavalo e lobo domados; desconhecido recusa; vaca não doma; "já é sua"; honestidade do Enderman com registro do Dragão e sem farm; cena real com chuva/noite/vida; aliado avisa do esqueleto; aldeão que conhece cumprimenta e o desconhecido não.
- Com a IA (`DialogueGameTest`, DeepSeek): conversa de ponta a ponta com o cavalo; estado do mundo conferido.
