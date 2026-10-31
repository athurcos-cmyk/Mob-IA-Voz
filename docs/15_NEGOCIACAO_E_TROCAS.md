# 15 — Negociação, trocas por profissão e coerência de espécie

Implementado e testado em jogo real (ver `STATUS.md`). Tudo é **servidor autoritativo**: o LLM só entende a fala e fala depois; quem decide interesse, preço, convencimento e entrega é código determinístico.

## Como uma troca acontece

1. Você fala/escreve com a criatura na mira: *"Quer trocar?"* ou direto *"Eu preciso de 3 cabeças Wither e te dou 3 ferros e 1 picareta de diamante."*
2. O analisador extrai **o que você pede**, **o que oferece** e **se deu um motivo** para ela querer algo. O servidor traduz os nomes para itens reais pelo dicionário `values.json` (nome desconhecido não vira item).
3. O servidor abre a oferta da receita **do ofício dela** (`trades.json`) e a criatura **fala só o que o servidor verificou**: o que aceita, o preço, o que dá, quais itens oferecidos interessam e quais não.
4. Você joga os itens **no chão** perto dela. Ela anda até eles (ou alcança, se não puder andar) e toma **o mínimo necessário** para fechar o valor. A sobra volta para você, com uma fala (*"Humano, eu só pedi 3 fenos. Peguei o que precisava e devolvi o que sobrou."*).
5. Só então entrega a recompensa **física** (e, se a receita tem, uma fala de origem: *"Achei isso nas profundezas do oceano."*).

Quando o combinado não é cumprido:
- item que não interessa → fica no chão, ela comenta (*"ferros não me interessam. A não ser que você me dê um bom motivo."*);
- valor insuficiente → nada é tomado, *"isso ainda não paga o que combinamos… o negócio não está fechado"*;
- prazo vencido → *"você não cumpriu o combinado, então o negócio está desfeito."*

## Preço variável e barganha

O preço do catálogo é o **preço-base**. Ao abrir a oferta o servidor calcula o preço real (`Pricing`), **trava-o na oferta** (sobrevive a salvar/sair; reabrir a mesma oferta não muda) e a criatura o diz em itens, explicando o motivo com as próprias palavras:
- **Confiança:** quem confia em você paga até 15% menos; quem desconfia, até 15% mais.
- **Humor:** ±10%, fixo por criatura + receita + dia de jogo (insistir no mesmo dia não muda; outra criatura ou outro dia, sim).
- **Barganha:** se você dá um motivo para um preço menor ("já te trouxe vários presentes"), a IA só avalia a força (0 sem motivo, 1 plausível, 2 forte) e o servidor aplica até ~12% por tentativa, escalado pela credulidade e confiança. Divide as 3 tentativas de convencimento da oferta.
- **Limites que nunca se quebram:** nunca abaixo do valor do que a criatura entrega (sem criar valor), nunca abaixo de 70% do preço-base nem acima de 125%. Receitas sem folga (a caveira, margem 1,0) não têm desconto.
- `server.json`: `"dynamicPrices": false` volta ao preço-base fixo.

## Falas da troca: geradas pela IA

Tudo o que a criatura diz durante a troca (sobra devolvida, valor insuficiente, item sem interesse, prazo vencido, entrega) é **gerado pela IA** a partir de um fato verificado pelo servidor ("você só pegou 3 fenos e devolveu 1", "os itens pagam só 67%; falta 4 barras de ouro"), usando os números e itens exatos. O texto fixo antigo só toca se a IA estiver fora do ar. O campo `flavor` das receitas virou **dica de origem** para a IA ("achei nas profundezas do oceano"), não uma frase.

## Quantidade e preço em itens (sem cooldown, sem receita rara)

- **Uma troca = um negócio**, e o negócio pode ter **várias unidades**: "3 caveiras" é um negócio de 3 unidades, com preço proporcional (3 × o de uma). Cada receita tem só um teto **por negócio** (`units` em `trades.json`, nunca mais que uma pilha). **Não há cooldown, janela de tempo, limite por dia nem receita "rara" com opt-in**: pode fazer outro negócio logo em seguida.
- O preço é calculado internamente em "pontos" (uma régua para comparar itens diferentes), mas **o jogador só ouve itens e porcentagens**: *"1 caveira: 12 barras de ouro, ou 6 esmeraldas, ou 3 diamantes"*, *"essa picareta paga uns 67% do preço; falta o equivalente a 4 barras de ouro"*. Você pode combinar itens.
- **Regra de preço (testada para o catálogo inteiro):** preço por unidade = valor do que a criatura entrega × margem (1,5 padrão; 1,1–1,3 em blocos, armaduras e esmeraldas). O teste `noRecipeCreatesValueOutOfThinAir` impede qualquer receita de entregar mais valor do que recebe, e `catalogIsClosedAndSane` impede preços absurdos (mais de 32 itens do aceito mais valioso por unidade, ou mais de uma pilha por negócio).
- O que não for entregue é devolvido; a sobra de um negócio também.

## Valor, interesse e convencimento

- Cada item tem um **valor** (pontos; 1 esmeralda = 10). Cada criatura tem **interesse** nele: *aceita* (1), *indiferente* (0), *detesta* (sempre 0).
- Itens indiferentes podem passar a interessar se você **convencer** com um motivo concreto. O analisador dá força 0/1/2 ao argumento, mas o **efeito é limitado** pelo servidor: credulidade da criatura + confiança nela, teto de 0,8 de interesse, no máximo 3 tentativas por oferta, e **aversão nunca é convencida** por conversa.
- Convencer não é gratuito: um item convencido vale só uma fração do valor, então o preço continua exigindo muito mais.

## Aldeões por profissão

`profiles.json` + `trades.json`: fazendeiro (feno, trigo, cenoura, batata, beterraba → esmeralda), ferreiros/armeiro (carvão, ferro, diamante), bibliotecário (papel, livro, tinta), clérigo (ouro, carne podre, lápis-lazúli), pescador, pastor (lã), açougueiro (carnes), flecheiro (graveto, sílex, pena, linha), curtidor (couro), cartógrafo (papel, linha), pedreiro (argila, tijolo). A profissão vem do aldeão real do jogo (`VillagerData`), então mudar de profissão muda o que ele pede e dá.

## Quem negocia hoje (o motor é o mesmo para todos)

Nada na negociação é específico de uma criatura: quem define tudo é `trades.json` (receitas: preço, quantidade, fala de entrega), `profiles.json` (o que aceita e `drops` = exatamente as saídas das receitas) e `values.json` (valor e nomes). Cada criatura tem **várias receitas**; a primeira é a padrão ("quer trocar?") e o item pedido escolhe a certa. O teste `AllTradersGameTest` joga o pagamento de cada criatura com o mob real e confere entrega e devolução.

Pesquisado no wiki (drops e comportamento) e limitado ao que faz sentido para cada um:

| Criatura | Aceita | Dá |
|---|---|---|
| Golem de ferro | flores, trepadeira, abóbora | barras e bloco de ferro, armadura de ferro, pedregulho, tijolos de pedra |
| Vaca | trigo, feno | couro e peças de couro, dente-de-leão |
| Vaca-cogumelo | trigo, feno | cogumelos, ensopado, couro |
| Ovelha | trigo, feno | lã, azulzinha, margarida, centáurea |
| Cavalo | maçã, cenoura, açúcar, trigo, feno | guia (rédea), margarida, papoula |
| Cabra | trigo, feno | chifre de cabra, bola de neve |
| Camelo | trigo, feno, melancia | cacto, arenito, arbusto seco |
| Lhama | trigo, feno | couro |
| Porco | cenoura, batata, beterraba | maçã, cogumelo marrom, lama |
| Galinha, papagaio | sementes | ovos e penas / penas |
| Coelho, gato, lobo, raposa | comida de cada um | peles, penas, linha, ossos, frutinhas, membrana de phantom (gato) |
| Abelha | flores, açúcar | favo de mel |
| Panda, tartaruga | bambu / grama marinha | gosma / escudo de tartaruga |
| Sapo, strider | gosma e olho de aranha / fungo distorcido | vitória-régia e luz de sapo / linha |
| Bacalhau, salmão, lula | algas, grama marinha (e peixe, na lula) | só coisas da água: prismarinho, picles do mar, argila, tinta |
| Zumbi, esqueleto, aranha | carne / material de flecha / carne | cenoura, batata, ferro / flechas, ossos, arco / teia, linha, olho de aranha |
| Bruxa | verruga do Nether, olho de aranha, lápis-lazúli | redstone, pó de glowstone, pólvora, garrafa |
| Blaze | carvão | vara e pó de blaze |
| Piglin | ouro (barra, pepita, bloco) | pepitas de ferro, linha, obsidiana, quartzo, couro, bola de fogo |
| Enderman | blocos (grama, areia, cascalho, argila, abóbora, melancia) | pérola do Fim |
| Esqueleto Wither | ouro, diamantes, esmeraldas, netherite | caveira, **estrela do Nether** (165 de valor: ex. 4 blocos de ouro, ou 9 diamantes), carvão, ossos |
| 13 aldeões por profissão | o material do ofício | esmeralda **e** os produtos do ofício (armeiro: armadura de ferro; fazendeiro: pão e torta…) |

Criaturas sem nada que faça sentido dar (creeper, slime, ghast, golem de neve…) ainda não têm receitas e não negociam: a resposta delas é uma recusa natural.

**Para ensinar uma criatura nova a negociar:** adicione uma entrada em `profiles.json` (se não tiver) e receitas em `trades.json`, com os itens em `values.json`. Nenhum código novo. Se a receita violar a coerência (aceitar e dar o mesmo item, saída fora de `drops`, criar valor ou preço absurdo), `gradlew test` falha.

## Negociação aberta: a IA julga, o servidor segura as regras

O catálogo acima é só o **ponto de partida** (o que cada criatura costuma aceitar e dar). Nada impede o jogador de tentar outra coisa:

- **Pagamento fora da lista.** Se você oferece algo que não está no gosto fixo da criatura (ferro bruto ao golem, picareta de pedra, esmeralda ao Enderman), o servidor pergunta à **IA** (`/v1/judge`) se *aquela criatura aceitaria*: `YES` = interessa (70% do valor), `MAYBE` = talvez (35%), `NO` = não quer. Ela **pode recusar** (o Enderman recusa diamante e esmeralda; a aranha não aceita carne cozida). Um `NO` pode ser **convencido** com um bom motivo (mesma regra de antes: limitado pela credulidade, no máximo 3 tentativas, teto de 80%), como "uma picareta de pedra ajuda você a minerar mais ferro". Só o que o jogador **disse** é julgado; o que vem do gosto fixo continua valendo. *Aversão* (itens que a criatura detesta no perfil) continua sem convencimento.
- **Item pedido fora do catálogo.** A IA diz se a criatura **teria** aquilo: `YES` → o servidor improvisa uma receita (margem 1,5, até 3 unidades); `MAYBE`/`NO` → ainda há **sorte**: 35% / 15% de ela ter *achado jogado por aí*, sorteio fixo por (criatura, item, dia de jogo), então insistir não adianta, mas outra galinha ou outro dia pode dar certo. Achado por sorte sai mais caro (margem 2,5, 1 unidade) e a criatura **conta onde achou** (fala gerada pela IA). Nunca é improvisado item de valor acima de 30 (sorte: 20); diamante, netherite e caveira só existem no catálogo.
- **O que não muda** (regras do servidor, a IA só opina): o item precisa existir em `values.json` (a lista de itens conhecidos); o preço nunca fica abaixo do valor do que a criatura entrega (o id da receita improvisada carrega o preço e é revalidado ao carregar o save); a reserva, devolução da sobra e idempotência são as mesmas. **Sem sidecar/IA**, vale só o catálogo e a sorte de 15% para itens fora dele.

Com o modelo local `qwen3:4b` o julgamento é conservador (tende a `NO`): nos testes reais (`ai-service/tests/live_judge.py`) 11 de 14 vereditos bateram com o bom senso, e a maioria dos erros foi recusar demais, o que o convencimento cobre.

## Pedido sem sentido (resposta gerada pela IA)

Se o jogador pede o que a criatura não tem como dar e a sorte não ajuda (diamante à galinha, cacto ao peixe), o servidor responde `IMPOSSIVEL` com o que ela **poderia** dar, e a **IA improvisa a recusa na voz da criatura** (o prompt manda inventar uma frase nova, no tom de "nem por todas as sementes do mundo", sem texto pronto; `ai-service/tests/live_refusal.py`). Não existe frase de recusa pré-escrita no mod. "Uma flor" sem dizer qual dá a flor que aquela criatura tem; cada pasto dá a sua (ovelha: azulzinha, margarida, centáurea; cavalo: margarida, papoula; vaca: dente-de-leão).

## Conversar não é provocar

Enquanto o jogador **fala ou negocia** com uma criatura (e por 30 s depois da última fala), ela **não o mira nem o fere**, mesmo hostil e sem ser aliada (`Parley`). A conversa acaba se o jogador **a ferir** ou **a ameaçar** e ela decidir lutar (reação ENFURECE). Vale só para quem está conversando com você, não para os outros mobs.

## Só dá o que faz sentido

Toda receita **do catálogo** é validada no teste: o item entregue **precisa estar em `drops` do perfil**, e `drops` são exatamente as saídas das receitas. Peixe só dá coisas do mar; galinha só ovo e pena. Fora do catálogo valem a IA e a sorte descritas acima, sempre com teto de valor.

## Coerência de espécie nas falas

Cada perfil carrega **dieta, habitat, estilo, o que gosta/detesta e como chama o jogador** (`Humano`, `Mortal`…). Criaturas sem perfil próprio recebem um perfil derivado da **categoria** (monstro, animal, aquático). Teste com o modelo real: galinha → sementes, vaca → capim/pasto, gato → peixe, lobo → carne, coelho → cenoura (11/11).

## Como estender

- Nova criatura: um objeto em `profiles.json` (+ receita em `trades.json` se negocia).
- Novo item: `values.json` (valor + nomes em pt-BR; o primeiro nome é o falado).
- `gradlew test` falha se uma receita der um item que não está nos `drops` do perfil ou aceitar um item sem valor.
