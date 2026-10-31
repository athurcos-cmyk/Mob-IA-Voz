# Juiz de IA por família de assunto (conversas ambiente)

2026-10-31 19:03; 85 famílias, 680 cenas, juiz `deepseek-v4-pro`, 211 s. Média geral **4.15**; famílias abaixo de 4.0: **30**.

| Família | cenas | nota |
|---|---|---|
| jogador_dono | 8 | 2.25  REVER |
| jogador_traiu | 8 | 2.50  REVER |
| galinha | 8 | 3.00  REVER |
| jogador_ameacou | 8 | 3.00  REVER |
| jogador_bateu | 8 | 3.00  REVER |
| jogador_presente | 8 | 3.00  REVER |
| lobo | 8 | 3.00  REVER |
| jogador_passando | 8 | 3.12  REVER |
| jogador_acalmou | 8 | 3.25  REVER |
| jogador_honesto | 8 | 3.25  REVER |
| jogador_troca | 8 | 3.25  REVER |
| monstros_chuva | 8 | 3.25  REVER |
| bicho_cheiro | 8 | 3.38  REVER |
| cercado | 8 | 3.38  REVER |
| gato | 8 | 3.38  REVER |
| jogador_apresenta | 8 | 3.38  REVER |
| afazeres | 8 | 3.50  REVER |
| fome_bicho | 8 | 3.50  REVER |
| porco | 8 | 3.50  REVER |
| jogador_fome | 8 | 3.62  REVER |
| jogador_nome_ambos | 8 | 3.62  REVER |
| jogador_nome_conta | 8 | 3.62  REVER |
| jogador_sem_nome | 8 | 3.62  REVER |
| clerigo | 8 | 3.75  REVER |
| jogador_ferido | 8 | 3.75  REVER |
| creeper_zumbi | 8 | 3.88  REVER |
| curiosidade_quadrado | 8 | 3.88  REVER |
| golem_flor | 8 | 3.88  REVER |
| jogador_conhecido | 8 | 3.88  REVER |
| ovelha | 8 | 3.88  REVER |
| aranha_zumbi | 8 | 4.00 |
| bicho_armadura | 8 | 4.00 |
| bicho_jogador_chuva | 8 | 4.00 |
| bicho_jogador_noite | 8 | 4.00 |
| cavalo | 8 | 4.00 |
| jogador_monstros_visto | 8 | 4.00 |
| bicho_item | 8 | 4.12 |
| medo_lobo | 8 | 4.12 |
| zumbi_esqueleto | 8 | 4.12 |
| bicho_barulho | 8 | 4.25 |
| curiosidade_portal | 8 | 4.25 |
| zumbi_jogador | 8 | 4.25 |
| chuva | 8 | 4.38 |
| curiosidade_creeper | 8 | 4.38 |
| curiosidade_enderman | 8 | 4.38 |
| enderman_olhar | 8 | 4.38 |
| noite | 8 | 4.38 |
| pastor | 8 | 4.38 |
| sol | 8 | 4.38 |
| vaca | 8 | 4.38 |
| aranha_teia | 8 | 4.50 |
| bicho_sombra | 8 | 4.50 |
| bruxa_pocao | 8 | 4.50 |
| colheita | 8 | 4.50 |
| curiosidade_fundo | 8 | 4.50 |
| esqueleto_creeper | 8 | 4.50 |
| fofoca_vila | 8 | 4.50 |
| pasto | 8 | 4.50 |
| vila_armadura | 8 | 4.50 |
| golem_guarda | 8 | 4.62 |
| jogador_chuva | 8 | 4.62 |
| trovoada | 8 | 4.62 |
| bicho_quer_comida | 8 | 4.75 |
| curiosidade_abelha | 8 | 4.75 |
| entardecer | 8 | 4.75 |
| esqueleto_mira | 8 | 4.75 |
| golem_quer_flor | 8 | 4.75 |
| warden_vibracao | 8 | 4.75 |
| zumbi_armadura | 8 | 4.75 |
| amanhecer | 8 | 4.88 |
| bibliotecario | 8 | 4.88 |
| camas | 8 | 4.88 |
| esqueleto_armadura | 8 | 4.88 |
| vila_fome | 8 | 4.88 |
| creeper_chiado | 8 | 5.00 |
| fazendeiro | 8 | 5.00 |
| ferreiro | 8 | 5.00 |
| jogador_item | 8 | 5.00 |
| jogador_noite | 8 | 5.00 |
| monstros_dia | 8 | 5.00 |
| monstros_noite | 8 | 5.00 |
| trocas_vila | 8 | 5.00 |
| vila_passando_troca | 8 | 5.00 |
| zumbi_carne | 8 | 5.00 |
| zumbis_na_vila | 8 | 5.00 |

## Problemas apontados (famílias abaixo do mínimo primeiro)

### jogador_dono (2.25)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 3: 0: O humano cuida de mim agora, sabia? / 1: Que sorte a sua. / 0: Cuida sim.
  - ⚠ Fala 0: 'O humano cuida de mim agora, sabia?' soa vaga e não especifica a criatura, mas é plausível para animais de fazenda.
  - ⚠ Fala 1: 'Que sorte a sua.' é uma resposta curta e natural, mas não desenvolve a conversa.
- nota 4: 0: Tenho dono agora: o humano. / 1: Cuida bem de você? / 0: Cuida sim.
  - ⚠ A fala 'Tenho dono agora: o humano.' soa mais como um animal domesticado (lobo/gato) do que como aldeão ou animal de fazenda, que normalmente não têm 'dono'.
  - ⚠ A resposta 'Cuida sim.' é muito curta e não desenvolve a conversa, mas ainda é aceitável.
- nota 3: 0: Tenho dono agora: o humano. / 1: Que sorte a sua. / 0: Estou bem melhor.
  - ⚠ Fala 0 'Tenho dono agora: o humano.' soa estranha para criaturas calmas como vacas ou aldeões, pois 'dono' implica relação de posse típica de animais domésticos, mas aldeões não têm donos.
  - ⚠ Fala 1 'Que sorte a sua.' é genérica e não aprofunda a conversa, mas é aceitável.
- nota 1: 0: Agora eu sou de o humano, e isso me faz bem. / 1: Cuida bem de você? / 0: Estou bem melhor.
  - ⚠ Fala 0: 'Agora eu sou de o humano' é gramaticalmente incorreta e não soa natural em português.
  - ⚠ Fala 0: 'sou de o humano' não combina com o mundo de criaturas calmas como aldeões ou animais de fazenda, que não se expressariam assim.
- nota 1: 0: Agora eu sou de o humano, e isso me faz bem. / 1: Que sorte a sua. / 0: Cuida sim.
  - ⚠ Fala 0 'Agora eu sou de o humano, e isso me faz bem.' é gramaticalmente incorreta e ambígua; 'de o humano' não faz sentido.
  - ⚠ Fala 1 'Que sorte a sua.' não responde adequadamente à declaração anterior, pois não há contexto de sorte.

### jogador_traiu (2.50)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 3: 0: O humano quebrou a nossa palavra. Eu me lembro. / 1: Que pena, parecia uma pessoa correta. / 0: Pois é, dói.
  - ⚠ Fala 0: 'O humano quebrou a nossa palavra' soa filosófica demais para criaturas calmas como vacas ou ovelhas.
  - ⚠ Fala 1: 'Que pena, parecia uma pessoa correta' é muito elaborada para um animal de fazenda.
- nota 3: 0: Fizemos um acordo, e o humano acabou com ele. / 1: Palavra quebrada é difícil de perdoar. / 0: Pois é, dói.
  - ⚠ Fala 0: 'Fizemos um acordo, e o humano acabou com ele.' é vaga e não combina com criaturas calmas como aldeões ou animais de fazenda, que não fariam acordos complexos.
  - ⚠ Fala 1: 'Palavra quebrada é difícil de perdoar.' soa filosófica demais para animais de fazenda, que não teriam esse nível de reflexão.
- nota 1: 0: O humano quebrou a nossa palavra. Eu me lembro. / 1: Palavra quebrada é difícil de perdoar. / 0: Pois é, dói.
  - ⚠ Fala 0: 'O humano quebrou a nossa palavra. Eu me lembro.' - Criaturas calmas como aldeões ou animais de fazenda não fariam referência a 'quebrar palavra' de forma tão abstrata; aldeões não têm esse ti
  - ⚠ Fala 1: 'Palavra quebrada é difícil de perdoar.' - Soa filosófico demais para criaturas simples; não combina com o mundo de aldeões ou animais.
- nota 3: 0: O humano quebrou a nossa palavra. Eu me lembro. / 1: Palavra quebrada é difícil de perdoar. / 0: Quem sabe um dia melhore.
  - ⚠ Fala 0: 'O humano quebrou a nossa palavra' é vaga e não combina com criaturas calmas como vacas ou aldeões, que não fariam acordos verbais complexos.
  - ⚠ Fala 1: 'Palavra quebrada é difícil de perdoar' soa filosófica demais para animais de fazenda.
- nota 3: 0: Fizemos um acordo, e o humano acabou com ele. / 1: Então não dá para confiar tanto. / 0: Pois é, dói.
  - ⚠ Fala 0: 'Fizemos um acordo, e o humano acabou com ele.' é vaga e não combina com criaturas calmas como aldeões ou animais de fazenda, que não fariam acordos complexos.
  - ⚠ Fala 1: 'Então não dá para confiar tanto.' soa filosófica demais para animais de fazenda, que não expressariam desconfiança abstrata.

### galinha (3.00)  _uma galinha e dois animais da fazenda; uma situação qualquer do dia a dia_
- nota 4: 0: Có có! Achei umas sementes no chão. / 1: Eu fico de olho no céu, por via das dúvidas. / 0: Então vou ciscar mais um pouco.
  - ⚠ A fala 1 não identifica qual animal responde, mas é plausível para um animal de fazenda vigilante.
- nota 2: 0: Có có! Achei umas sementes no chão. / 1: Ovo novo é sempre uma alegria. / 0: Então vou ciscar mais um pouco.
  - ⚠ A fala 'Ovo novo é sempre uma alegria.' não responde diretamente à fala sobre sementes, quebrando a coerência da conversa.
  - ⚠ A resposta 'Ovo novo é sempre uma alegria.' parece deslocada, pois não há menção a ovos na fala anterior.
- nota 2: 0: Có, tem raposa por perto? / 1: Cuidado com a raposa. / 0: Có có, a vida é boa.
  - ⚠ A fala 'Có có, a vida é boa.' não fecha a conversa de forma coerente com o alerta anterior sobre a raposa.
  - ⚠ A resposta 'Cuidado com a raposa.' não responde diretamente à pergunta 'tem raposa por perto?'.
- nota 4: 0: Có có! Achei umas sementes no chão. / 1: Que sorte a sua. / 0: Có có, a vida é boa.
  - ⚠ A fala 'Que sorte a sua.' é genérica e não identifica qual animal da fazenda respondeu, mas é aceitável.
  - ⚠ A fala 'Có có, a vida é boa.' é um pouco filosófica para uma galinha, mas ainda dentro do possível.
- nota 4: 0: Hoje o dia foi só ciscar. / 1: Eu fico de olho no céu, por via das dúvidas. / 0: Có có, a vida é boa.
  - ⚠ A fala 'Eu fico de olho no céu, por via das dúvidas.' é um pouco vaga e não se conecta claramente com a fala anterior sobre ciscar, mas pode ser interpretada como um animal de fazenda atento a predado
  - ⚠ A fala 'Có có, a vida é boa.' é um fecho aceitável, mas poderia ser mais natural se respondesse diretamente à fala anterior.

### jogador_ameacou (3.00)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 3: 0: Eu me assustei com as palavras de o humano outro dia. / 1: Espero que tenha sido um mal-entendido. / 0: Pode ser.
  - ⚠ Fala 0: 'as palavras de o humano' soa estranho; seria mais natural 'as palavras do humano'.
  - ⚠ Fala 0: 'Eu me assustei com as palavras de o humano outro dia' é vaga e não combina com criaturas calmas de fazenda, que normalmente não discutem palavras humanas.
- nota 3: 0: Eu me assustei com as palavras de o humano outro dia. / 1: Tem quem fale sem pensar. / 0: Vou dar uma chance.
  - ⚠ Fala 0: 'Eu me assustei com as palavras de o humano outro dia.' soa estranho: 'de o humano' deveria ser 'do humano'.
  - ⚠ Fala 1: 'Tem quem fale sem pensar.' é vaga e não responde diretamente ao susto mencionado.
- nota 3: 0: O humano me ameaçou uma vez, só com palavras. / 1: Ameaça de boca pesa, né? / 0: Vou dar uma chance.
  - ⚠ Fala 0: 'O humano me ameaçou uma vez, só com palavras.' - 'só com palavras' soa estranho para criaturas calmas como aldeões ou animais de fazenda, que normalmente não discutem nuances de ameaça.
  - ⚠ Fala 1: 'Ameaça de boca pesa, né?' - 'de boca' é uma expressão idiomática que pode não soar natural para criaturas do jogo, especialmente animais.
- nota 3: 0: O humano me ameaçou uma vez, só com palavras. / 1: Espero que tenha sido um mal-entendido. / 0: Pode ser.
  - ⚠ Fala 0: 'O humano me ameaçou uma vez, só com palavras.' soa muito abstrata para criaturas calmas de fazenda; ameaças verbais são complexas demais para o mundo delas.
  - ⚠ Fala 1: 'Espero que tenha sido um mal-entendido.' é uma resposta racional e diplomática, incomum para animais ou aldeões simples.
- nota 3: 0: O humano me ameaçou uma vez, só com palavras. / 1: Espero que tenha sido um mal-entendido. / 0: Vou dar uma chance.
  - ⚠ Fala 0: 'O humano me ameaçou uma vez, só com palavras.' - 'só com palavras' soa estranho para criaturas calmas como aldeões ou animais de fazenda, que normalmente não discutem ameaças verbais.
  - ⚠ Fala 1: 'Espero que tenha sido um mal-entendido.' - Resposta genérica que não aprofunda a situação, mas é aceitável.

### jogador_bateu (3.00)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 3: 0: O humano me machucou uma vez. Não esqueci. / 1: Quem bate uma vez pode bater de novo. / 0: Já aprendi a lição.
  - ⚠ Fala 0: 'O humano me machucou uma vez. Não esqueci.' soa vingativa demais para criaturas calmas como aldeões ou animais de fazenda.
  - ⚠ Fala 1: 'Quem bate uma vez pode bater de novo.' é um provérbio humano, não combina com a simplicidade de mobs.
- nota 3: 0: Prefiro manter distância de o humano. / 1: Então fica de olho. / 0: Já aprendi a lição.
  - ⚠ Fala 0 'Prefiro manter distância de o humano.' soa estranha: 'de o humano' deveria ser 'do humano'.
  - ⚠ Fala 1 'Então fica de olho.' é vaga e não responde diretamente à preferência expressa.
- nota 3: 0: Prefiro manter distância de o humano. / 1: Melhor manter distância dessa pessoa mesmo. / 0: Já aprendi a lição.
  - ⚠ Fala 0: 'Prefiro manter distância de o humano.' soa estranho; deveria ser 'do humano'.
  - ⚠ Fala 1: 'Melhor manter distância dessa pessoa mesmo.' usa 'pessoa' em vez de 'humano', quebrando a consistência.
- nota 3: 0: Eu me lembro bem de quando o humano me bateu. / 1: Melhor manter distância dessa pessoa mesmo. / 0: É o que eu penso.
  - ⚠ Fala 0: 'Eu me lembro bem de quando o humano me bateu.' soa muito específica e dramática para uma conversa casual entre criaturas calmas; não combina com 'situação qualquer do dia a dia'.
  - ⚠ Fala 1: 'Melhor manter distância dessa pessoa mesmo.' é uma resposta razoável, mas a palavra 'pessoa' é estranha para criaturas que normalmente se referem ao jogador como 'humano' ou 'jogador'.
- nota 3: 0: O humano me machucou uma vez. Não esqueci. / 1: Quem bate uma vez pode bater de novo. / 0: É o que eu penso.
  - ⚠ Fala 0: 'O humano me machucou uma vez. Não esqueci.' soa vingativa demais para criaturas calmas como aldeões ou animais de fazenda.
  - ⚠ Fala 1: 'Quem bate uma vez pode bater de novo.' é um provérbio abstrato, incomum para animais ou aldeões simples.

### jogador_presente (3.00)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 3: 0: Ninguém tinha me dado um presente antes de o humano. / 1: Pelo visto é gente boa. / 0: Foi um dia especial.
  - ⚠ Fala 0: 'Ninguém tinha me dado um presente antes de o humano.' soa ambígua e truncada; falta 'chegar' ou 'aparecer' para ficar natural.
  - ⚠ Fala 1: 'Pelo visto é gente boa.' é uma resposta plausível, mas não se conecta diretamente ao presente mencionado.
- nota 3: 0: O humano me deu um presente outro dia. Fiquei com o coração quentinho. / 1: Que gesto bonito. / 0: Nunca esqueci.
  - ⚠ Fala 0: 'Fiquei com o coração quentinho' soa sentimental demais para criaturas calmas como aldeões ou animais de fazenda, que tendem a expressões mais simples.
  - ⚠ Fala 1: 'Que gesto bonito' é uma resposta genérica e pouco natural para a situação, sem personalidade da criatura.
- nota 3: 0: Sabia que o humano já me presenteou? / 1: Que gesto bonito. / 0: Nunca esqueci.
  - ⚠ Fala 0 'Sabia que o humano já me presenteou?' soa formal demais para uma conversa casual entre criaturas calmas.
  - ⚠ Fala 1 'Que gesto bonito.' é vaga e não desenvolve a conversa, parecendo resposta automática.
- nota 3: 0: O humano me deu um presente outro dia. Fiquei com o coração quentinho. / 1: Pelo visto é gente boa. / 0: Nunca esqueci.
  - ⚠ Fala 0: 'Fiquei com o coração quentinho' soa sentimental demais para criaturas como vacas ou aldeões, que não expressam emoções assim.
  - ⚠ Fala 1: 'Pelo visto é gente boa' é uma expressão muito humana e coloquial, não combina com o vocabulário típico de mobs.
- nota 3: 0: O humano me deu um presente outro dia. Fiquei com o coração quentinho. / 1: Poucos fazem isso. / 0: Nunca esqueci.
  - ⚠ Fala 0: 'Fiquei com o coração quentinho' soa sentimental demais para criaturas calmas como aldeões ou animais de fazenda, que tendem a expressões mais simples.
  - ⚠ Fala 1: 'Poucos fazem isso' é vaga e não desenvolve a conversa, deixando a resposta sem conexão clara com o presente recebido.

### lobo (3.00)  _um lobo e dois animais da fazenda; uma situação qualquer do dia a dia_
- nota 3: 0: Cheirei um osso enterrado ali perto. / 1: Você parece sempre alerta. / 0: Quem é da matilha eu protejo.
  - ⚠ A fala 'Cheirei um osso enterrado ali perto.' é plausível para um lobo, mas não se conecta claramente com a resposta 'Você parece sempre alerta.'
  - ⚠ A resposta 'Você parece sempre alerta.' não responde diretamente à fala anterior, soando deslocada.
- nota 3: 0: Cheirei um osso enterrado ali perto. / 1: Você parece sempre alerta. / 0: Meu nariz nunca erra.
  - ⚠ A fala 'Você parece sempre alerta.' não responde diretamente à fala do lobo sobre cheirar um osso.
  - ⚠ O fecho 'Meu nariz nunca erra.' não encerra a conversa de forma natural, pois não há uma conclusão clara.
- nota 3: 0: Estou de olho em tudo por aqui. / 1: Você parece sempre alerta. / 0: Meu nariz nunca erra.
  - ⚠ Fala 0 'Estou de olho em tudo por aqui.' é genérica e não reflete especificamente um lobo (poderia ser dita por qualquer criatura).
  - ⚠ Fala 1 'Você parece sempre alerta.' é uma observação vaga que não avança a conversa.
- nota 3: 0: Um dia ainda pego aquele esqueleto. / 1: Você parece sempre alerta. / 0: Quem é da matilha eu protejo.
  - ⚠ A fala 'Um dia ainda pego aquele esqueleto.' soa vingativa demais para um lobo, que normalmente age por instinto e não guarda rancor.
  - ⚠ A resposta 'Você parece sempre alerta.' é genérica e não se conecta diretamente com a fala anterior sobre o esqueleto.
- nota 3: 0: Um dia ainda pego aquele esqueleto. / 1: É bom ter alguém que vigia. / 0: Quem é da matilha eu protejo.
  - ⚠ Fala 0 'Um dia ainda pego aquele esqueleto.' não combina com a situação 'uma situação qualquer do dia a dia', pois esqueletos não são comuns no dia a dia da fazenda.
  - ⚠ Fala 1 'É bom ter alguém que vigia.' é vaga e não responde diretamente à fala do lobo sobre pegar o esqueleto.

### jogador_passando (3.12)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; é de dia_
- nota 3: 0: Vi o humano passando agora há pouco. / 1: Eu fico de olho. / 0: Vamos ver o que acontece.
  - ⚠ Fala 1 'Eu fico de olho.' é vaga e não responde diretamente ao comentário sobre o humano.
  - ⚠ Fala 0 final 'Vamos ver o que acontece.' não fecha a conversa de forma conclusiva, deixando-a em aberto.
- nota 4: 0: Vi o humano passando agora há pouco. / 1: Eu fico de olho. / 0: Eu também.
  - ⚠ A fala 'Eu fico de olho.' é um pouco vaga e não responde diretamente ao comentário sobre o humano, mas ainda é plausível.
- nota 1: 0: Vi o humano passando agora há pouco. / 1: Será que vem conversar? / 0: Eu também.
  - ⚠ A fala 'Eu também.' não fecha a conversa de forma coerente com a pergunta 'Será que vem conversar?'.
  - ⚠ A resposta 'Será que vem conversar?' não combina com a fala inicial 'Vi o humano passando agora há pouco.' de forma natural.
- nota 4: 0: Olha, é o humano outra vez. / 1: Será que vem conversar? / 0: Vamos ver o que vai fazer.
  - ⚠ Fala 0 'Olha, é o humano outra vez.' soa um pouco genérica, mas aceitável para criaturas calmas.
  - ⚠ Fala 1 'Será que vem conversar?' sugere expectativa de interação, o que é plausível para aldeões, mas menos para animais de fazenda.
- nota 1: 0: Olha, é o humano outra vez. / 1: Será que vem conversar? / 0: Eu também.
  - ⚠ Fala 0: 'Olha, é o humano outra vez.' - Criaturas calmas como vacas ou ovelhas não falariam de forma tão articulada sobre o humano; soa mais como aldeão.
  - ⚠ Fala 1: 'Será que vem conversar?' - Animais de fazenda não demonstrariam curiosidade sobre conversar com o humano; comportamento não condiz com a natureza passiva deles.

### jogador_acalmou (3.25)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Quando eu estava com medo, o humano apareceu e me ajudou a respirar. / 1: Que bom que existe quem faz isso. / 0: Não esqueci essa gentileza.
  - ⚠ A fala 'Quando eu estava com medo, o humano apareceu e me ajudou a respirar' é um pouco abstrata para criaturas calmas como aldeões ou animais de fazenda, que normalmente não expressam medo ou técnica
- nota 3: 0: O humano me acalmou quando eu estava com medo. / 1: Gente assim aquece o coração. / 0: Não esqueci essa gentileza.
  - ⚠ Fala 0: 'O humano me acalmou quando eu estava com medo.' - Animais de fazenda não expressam medo de forma tão articulada; soa humano demais.
  - ⚠ Fala 1: 'Gente assim aquece o coração.' - 'Gente' é termo humano; animais não usariam essa palavra.
- nota 3: 0: Quando eu estava com medo, o humano apareceu e me ajudou a respirar. / 1: Gente assim aquece o coração. / 0: Não esqueci essa gentileza.
  - ⚠ Fala 0: 'me ajudou a respirar' é muito específico e humano para criaturas calmas como vacas ou aldeões; não combina com o mundo delas.
  - ⚠ Fala 1: 'Gente assim aquece o coração' soa filosófico demais para uma criatura simples.
- nota 3: 0: Quando eu estava com medo, o humano apareceu e me ajudou a respirar. / 1: Então essa pessoa é de confiança. / 0: Não esqueci essa gentileza.
  - ⚠ Fala 0: 'me ajudou a respirar' é muito específico e humano para criaturas calmas como vacas ou aldeões; não combina com o mundo delas.
  - ⚠ Fala 1: 'essa pessoa é de confiança' soa como julgamento humano, não natural para animais ou aldeões simples.
- nota 3: 0: Quando eu estava com medo, o humano apareceu e me ajudou a respirar. / 1: Que bom que existe quem faz isso. / 0: Fez bem, de verdade.
  - ⚠ Fala 0: 'Quando eu estava com medo, o humano apareceu e me ajudou a respirar.' - 'Ajudou a respirar' é uma ação muito específica e humana, não combina com criaturas calmas como vacas ou aldeões em uma
  - ⚠ Fala 1: 'Que bom que existe quem faz isso.' - Resposta genérica que não aprofunda a conversa, mas é aceitável.

### jogador_honesto (3.25)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 3: 0: O humano me devolveu o que eu tinha entregado, sem eu pedir. / 1: Gente assim é difícil de achar. / 0: Fez toda a diferença para mim.
  - ⚠ Fala 0: 'O humano me devolveu o que eu tinha entregado, sem eu pedir.' soa vaga e pouco natural para criaturas calmas; não especifica o que foi entregue, o que enfraquece a coerência.
  - ⚠ Fala 1: 'Gente assim é difícil de achar.' usa 'gente' de forma genérica, o que pode não combinar com a perspectiva de criaturas não humanas.
- nota 3: 0: Teve uma vez que o humano me devolveu algo, sem ninguém pedir. / 1: Isso é raro, essa pessoa é honesta. / 0: Pois é, eu confio.
  - ⚠ Fala 0: 'Teve uma vez que o humano me devolveu algo, sem ninguém pedir.' - 'devolveu algo' é vago e não se conecta claramente com o mundo de criaturas calmas como aldeões ou animais de fazenda; animai
  - ⚠ Fala 1: 'Isso é raro, essa pessoa é honesta.' - 'essa pessoa' soa estranho para criaturas que normalmente se referem ao jogador como 'humano' ou 'jogador', não 'pessoa'.
- nota 3: 0: O humano me devolveu o que eu tinha entregado, sem eu pedir. / 1: Então vale a confiança. / 0: Fez toda a diferença para mim.
  - ⚠ Fala 0: 'O humano me devolveu o que eu tinha entregado, sem eu pedir.' - 'entregado' é vago e não combina com o mundo de criaturas calmas como vacas ou aldeões, que não entregam itens de forma abstrat
  - ⚠ Fala 1: 'Então vale a confiança.' - soa como conclusão filosófica, estranha para criaturas simples.
- nota 3: 0: Teve uma vez que o humano me devolveu algo, sem ninguém pedir. / 1: Então vale a confiança. / 0: Pois é, eu confio.
  - ⚠ Fala 0: 'Teve uma vez que o humano me devolveu algo, sem ninguém pedir.' soa vaga e pouco natural para criaturas calmas; falta contexto do que foi devolvido.
  - ⚠ Fala 1: 'Então vale a confiança.' é uma resposta que não conecta claramente com a fala anterior; a lógica de 'devolver algo' implicar 'valer a confiança' é forçada.
- nota 3: 0: O humano me devolveu o que eu tinha entregado, sem eu pedir. / 1: Gente assim é difícil de achar. / 0: Pois é, eu confio.
  - ⚠ Fala 0: 'O humano me devolveu o que eu tinha entregado, sem eu pedir.' - 'entregado' é vago e não combina com o mundo de criaturas calmas como vacas ou aldeões, que não fazem trocas complexas.
  - ⚠ Fala 1: 'Gente assim é difícil de achar.' - 'gente' refere-se a humanos, mas criaturas não humanas não usariam esse termo para humanos; soa estranho.

### jogador_troca (3.25)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 3: 0: Negociei com o humano outro dia, correu tudo bem. / 1: Paga direito? / 0: Dá para confiar.
  - ⚠ Fala 0: 'Negociei com o humano outro dia, correu tudo bem.' - Aldeões negociam, mas animais de fazenda não; a fala é genérica demais para 'duas criaturas calmas' sem especificar.
  - ⚠ Fala 1: 'Paga direito?' - Soa como gíria humana moderna, não combina com o tom simples de aldeões ou animais.
- nota 3: 0: Negociei com o humano outro dia, correu tudo bem. / 1: Bom saber, eu também troco com quem é honesto. / 0: Paga, sim.
  - ⚠ Fala 0: 'Negociei com o humano outro dia, correu tudo bem.' - Aldeões negociam, mas animais de fazenda não; a fala é genérica demais para 'duas criaturas calmas' sem especificar.
  - ⚠ Fala 1: 'Bom saber, eu também troco com quem é honesto.' - 'Troco' é vago; para aldeões faz sentido, mas para animais não.
- nota 3: 0: O humano fez uma troca justa comigo. / 1: Paga direito? / 0: Paga, sim.
  - ⚠ A fala 'Paga direito?' soa como gíria humana moderna, não combina com o tom simples de aldeões ou animais.
  - ⚠ A resposta 'Paga, sim.' é muito curta e não fecha a conversa de forma natural, deixando a interação truncada.
- nota 3: 0: O humano fez uma troca justa comigo. / 1: Isso é raro hoje em dia. / 0: Paga, sim.
  - ⚠ A fala 'Paga, sim.' não fecha a conversa de forma natural; parece uma resposta curta demais e desconexa.
  - ⚠ A fala 'Isso é raro hoje em dia.' soa mais como um comentário de aldeão do que de um animal de fazenda, que não teria noção de trocas justas.
- nota 1: 0: A última troca com o humano foi tranquila. / 1: Paga direito? / 0: Paga, sim.
  - ⚠ Fala 0: 'A última troca com o humano foi tranquila.' - Animais da fazenda não fazem trocas; isso é típico de aldeões, mas a descrição inclui animais.
  - ⚠ Fala 1: 'Paga direito?' - Pergunta sobre pagamento não combina com animais da fazenda.

### monstros_chuva (3.25)  _dois monstros hostis; está chovendo_
- nota 3: 0: Essa chuva estraga tudo. / 1: Pelo menos os humanos ficam em casa. / 0: Eu espero aqui.
  - ⚠ Fala 1: 'Pelo menos os humanos ficam em casa.' soa estranho para monstros hostis, que normalmente não se importam com humanos a não ser para atacar.
  - ⚠ Fala 0 (fecho): 'Eu espero aqui.' é vago e não fecha a conversa de forma natural.
- nota 3: 0: Chuva de novo, que droga. / 1: Procura um abrigo. / 0: Vai passar.
  - ⚠ Fala 1 'Procura um abrigo' soa como conselho amigável, incomum para monstros hostis.
  - ⚠ Fala 0 'Vai passar' é muito otimista para um monstro hostil.
- nota 3: 0: Essa chuva estraga tudo. / 1: Eu só quero que passe. / 0: Vai passar.
  - ⚠ Fala 0 'Essa chuva estraga tudo.' é genérica demais para monstros hostis, que normalmente não se importam com chuva.
  - ⚠ Fala 1 'Eu só quero que passe.' soa passiva demais para criaturas hostis.
- nota 3: 0: Chuva de novo, que droga. / 1: Eu só quero que passe. / 0: Vai passar.
  - ⚠ Fala 0 'Chuva de novo, que droga.' soa muito humano e coloquial para monstros hostis, que normalmente não expressam frustração com o clima.
  - ⚠ Fala 1 'Eu só quero que passe.' é vaga e não combina com a natureza agressiva de monstros hostis.
- nota 4: 0: Chuva de novo, que droga. / 1: Pelo menos os humanos ficam em casa. / 0: Vai passar.
  - ⚠ A fala 'Pelo menos os humanos ficam em casa.' soa mais como uma observação de um mob passivo ou neutro, não de um monstro hostil que normalmente quer atacar humanos.

### bicho_cheiro (3.38)  _dois animais da fazenda; é de dia_
- nota 4: 0: Sentiu o cheiro do vento hoje? / 1: Eu só sinto o que tem para comer. / 0: Gosto quando o dia cheira assim.
  - ⚠ A fala 'Eu só sinto o que tem para comer.' é um pouco vaga e não responde diretamente à pergunta sobre o cheiro do vento, mas ainda é plausível para um animal de fazenda.
- nota 4: 0: Tem um cheiro bom por aqui. / 1: Pois é. / 0: Gosto quando o dia cheira assim.
  - ⚠ Fala 1 'Pois é.' é muito vaga e não acrescenta à conversa, mas é aceitável para animais.
- nota 3: 0: O ar está diferente hoje. / 1: Nem me fale. / 0: Gosto quando o dia cheira assim.
  - ⚠ Fala 0 'O ar está diferente hoje.' é vaga e não específica para animais de fazenda; poderia ser dita por qualquer criatura.
  - ⚠ Fala 1 'Nem me fale.' é uma resposta genérica que não desenvolve a conversa nem reflete características de animais de fazenda.
- nota 2: 0: O ar está diferente hoje. / 1: Pois é. / 0: Hmm, também.
  - ⚠ Fala 0: 'O ar está diferente hoje.' é vaga e não combina com animais de fazenda, que normalmente não comentam sobre o ar de forma abstrata.
  - ⚠ Fala 1: 'Pois é.' é uma resposta genérica que não desenvolve a conversa.
- nota 1: 0: O ar está diferente hoje. / 1: É verdade. / 0: Hmm, também.
  - ⚠ Fala 0: 'O ar está diferente hoje.' é vaga e não combina com animais de fazenda, que normalmente falam de coisas concretas como comida ou perigo.
  - ⚠ Fala 1: 'É verdade.' é uma resposta genérica que não acrescenta nada e não parece natural para um animal.

### cercado (3.38)  _dois animais da fazenda; uma situação qualquer do dia a dia_
- nota 3: 0: O cercado está apertado hoje. / 1: É verdade. / 0: Verdade, perto de vocês eu me sinto bem.
  - ⚠ A fala 'Verdade, perto de vocês eu me sinto bem.' não fecha a conversa de forma natural, parece um desvio do assunto.
  - ⚠ A resposta 'É verdade.' é muito vaga e não acrescenta nada, tornando o diálogo pouco interessante.
- nota 3: 0: Será que um dia essa cerca abre? / 1: Gosto de saber onde fica o meu cantinho. / 0: Aqui pelo menos tem comida.
  - ⚠ A fala 'Será que um dia essa cerca abre?' sugere desejo de fuga, mas a resposta 'Gosto de saber onde fica o meu cantinho.' não responde diretamente à pergunta.
  - ⚠ O fecho 'Aqui pelo menos tem comida.' muda o assunto sem concluir a conversa sobre a cerca.
- nota 3: 0: Tem espaço de sobra para todo mundo aqui. / 1: Também acho. / 0: Aqui pelo menos tem comida.
  - ⚠ Fala 0 'Tem espaço de sobra para todo mundo aqui.' é genérica demais para animais de fazenda, não reflete comportamento típico.
  - ⚠ Fala 1 'Também acho.' é muito curta e não acrescenta nada, soa artificial.
- nota 4: 0: O cercado está apertado hoje. / 1: Gosto de saber onde fica o meu cantinho. / 0: Verdade, perto de vocês eu me sinto bem.
  - ⚠ A fala 'Gosto de saber onde fica o meu cantinho.' soa um pouco filosófica para um animal de fazenda, mas ainda é aceitável.
  - ⚠ A fala 'Verdade, perto de vocês eu me sinto bem.' é um pouco sentimental demais para um animal, mas não quebra a imersão.
- nota 4: 0: O cercado está apertado hoje. / 1: Nem me fale. / 0: Aqui pelo menos tem comida.
  - ⚠ Fala 0 'O cercado está apertado hoje.' é um pouco vaga, mas plausível para animais de fazenda.
  - ⚠ Fala 1 'Nem me fale.' é uma resposta natural, mas não especifica o incômodo.

### gato (3.38)  _um gato e dois animais da fazenda; uma situação qualquer do dia a dia_
- nota 4: 0: Dormi no sol a tarde toda. / 1: Que vida boa a sua. / 0: Dizem que é sorte, mas é esperteza.
  - ⚠ A fala 'Dizem que é sorte, mas é esperteza.' soa um pouco filosófica demais para um gato, mas ainda é aceitável.
- nota 3: 0: Hoje eu não faço nada, e está ótimo. / 1: Você realmente não tem pressa. / 0: Dizem que é sorte, mas é esperteza.
  - ⚠ Fala 0: 'Hoje eu não faço nada, e está ótimo.' soa mais como um humano preguiçoso do que um gato; gatos não costumam verbalizar preguiça dessa forma.
  - ⚠ Fala 1: 'Você realmente não tem pressa.' é genérica e não reflete a natureza de um animal de fazenda (ex: vaca, ovelha, porco).
- nota 2: 0: Dormi no sol a tarde toda. / 1: Que vida boa a sua. / 0: É verdade. Eles têm medo de mim.
  - ⚠ A fala 'É verdade. Eles têm medo de mim.' não fecha a conversa de forma coerente com o contexto de dormir no sol.
  - ⚠ A resposta 'Que vida boa a sua.' é genérica e não combina especificamente com animais de fazenda.
- nota 4: 0: Hoje eu não faço nada, e está ótimo. / 1: Dizem que gato afasta creeper, é verdade? / 0: Dizem que é sorte, mas é esperteza.
  - ⚠ A fala 'Dizem que gato afasta creeper, é verdade?' é plausível para animais da fazenda, mas a resposta 'Dizem que é sorte, mas é esperteza.' é um pouco vaga e não fecha totalmente a conversa.
- nota 4: 0: Dormi no sol a tarde toda. / 1: Dizem que gato afasta creeper, é verdade? / 0: É verdade. Eles têm medo de mim.
  - ⚠ A fala 'Dizem que gato afasta creeper, é verdade?' é uma pergunta sobre mecânica de jogo, mas não está claro qual animal da fazenda a diz; poderia soar estranho vindo de uma vaca ou ovelha, pois não é
  - ⚠ A resposta 'É verdade. Eles têm medo de mim.' é plausível para um gato, mas a conversa não se conecta bem com a fala inicial sobre dormir no sol, tornando a troca um pouco desconexa.

### jogador_apresenta (3.38)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; quem abre já conhece o jogador; NÃO: quem responde já conhece o jogador_
- nota 3: 0: Está vendo aquela pessoa? É o humano. Já conversei com ela. / 1: Não conhecia, nunca falei com essa pessoa. / 0: Boa sorte.
  - ⚠ A fala 'Boa sorte.' não fecha a conversa de forma natural; parece desconectada da resposta anterior.
  - ⚠ A resposta 'Não conhecia, nunca falei com essa pessoa.' é redundante e pouco natural para uma conversa curta.
- nota 3: 0: Está vendo quem passou? É o humano, eu já conheço. / 1: Parece alguém interessante. / 0: Boa sorte.
  - ⚠ A fala 'Parece alguém interessante.' soa vaga e não combina com a resposta esperada de uma criatura da mesma espécie que não conhece o jogador.
  - ⚠ O fecho 'Boa sorte.' não fecha a conversa de forma natural, pois não há contexto para desejar sorte.
- nota 4: 0: Está vendo quem passou? É o humano, eu já conheço. / 1: Parece alguém interessante. / 0: Vale a pena puxar assunto.
  - ⚠ A fala 'Parece alguém interessante.' soa genérica demais para uma criatura calma como um aldeão ou animal de fazenda, que tenderia a comentar algo mais concreto sobre o humano.
  - ⚠ A fala 'Vale a pena puxar assunto.' é um pouco vaga e não fecha a conversa de forma natural, pois não há uma reação clara da outra criatura.
- nota 3: 0: Olha quem está aí: o humano. Passa sempre por aqui. / 1: Parece alguém interessante. / 0: Vale a pena puxar assunto.
  - ⚠ Fala 1 'Parece alguém interessante' soa estranha para criaturas calmas como vacas ou aldeões, que normalmente não avaliam o jogador dessa forma.
  - ⚠ Fala 0 'Vale a pena puxar assunto' é vaga e não fecha a conversa de forma natural, deixando a interação sem conclusão clara.
- nota 4: 0: Está vendo aquela pessoa? É o humano. Já conversei com ela. / 1: Não conhecia, nunca falei com essa pessoa. / 0: Vai lá falar, não morde.
  - ⚠ A fala 'Vai lá falar, não morde.' soa um pouco agressiva para criaturas calmas, mas ainda aceitável.

### afazeres (3.50)  _dois aldeões; é de dia_
- nota 3: 0: Passei a manhã sem parar, e ainda falta consertar uma cerca. / 1: Pois é. / 0: Valeu, ajuda é sempre bem-vinda.
  - ⚠ A fala 'Valeu, ajuda é sempre bem-vinda.' não se conecta com a resposta anterior 'Pois é.', pois não houve oferta de ajuda.
- nota 3: 0: Hoje ainda preciso arrumar a casa. / 1: Eu também tenho a minha lista. / 0: Valeu, ajuda é sempre bem-vinda.
  - ⚠ Fala 0: 'Valeu, ajuda é sempre bem-vinda.' não responde diretamente à fala 1, que apenas menciona ter uma lista, sem oferecer ajuda explicitamente.
  - ⚠ Fala 0: 'Valeu, ajuda é sempre bem-vinda.' soa deslocada, pois não houve oferta de ajuda na conversa.
- nota 3: 0: Hoje ainda preciso consertar uma cerca. / 1: É verdade. / 0: Assim a gente termina logo.
  - ⚠ A resposta 'É verdade.' é vaga e não acrescenta nada à conversa.
  - ⚠ O fecho 'Assim a gente termina logo.' não se conecta claramente com a fala anterior, pois não há menção de trabalho conjunto.
- nota 4: 0: Passei a manhã sem parar, e ainda falta arrumar a casa. / 1: Eu também tenho a minha lista. / 0: Valeu, ajuda é sempre bem-vinda.
  - ⚠ A fala 'Valeu, ajuda é sempre bem-vinda.' soa como resposta a uma oferta de ajuda, mas o aldeão 1 apenas mencionou ter sua própria lista, sem oferecer ajuda explicitamente.
- nota 3: 0: Hoje ainda preciso regar as plantas. / 1: Nem me fale. / 0: Valeu, ajuda é sempre bem-vinda.
  - ⚠ A fala 'Valeu, ajuda é sempre bem-vinda.' não responde diretamente a 'Nem me fale.' e parece deslocada, como se agradecesse por uma ajuda que não foi oferecida.
  - ⚠ A resposta 'Nem me fale.' é muito vaga e não desenvolve a conversa, deixando o fecho sem conexão clara.

### fome_bicho (3.50)  _dois animais da fazenda; é de dia_
- nota 3: 0: Será que hoje alguém traz comida? / 1: Quem espera, come. / 0: Alguma hora vem.
  - ⚠ A fala 'Quem espera, come.' soa como um provérbio humano, o que é estranho para animais da fazenda.
  - ⚠ A resposta 'Alguma hora vem.' é vaga e não fecha a conversa de forma natural.
- nota 3: 0: Estou com uma fome danada. / 1: Paciência, sempre aparece alguém. / 0: Vamos esperar, então.
  - ⚠ Fala 1 'Paciência, sempre aparece alguém' é vaga e não combina claramente com a fome mencionada; parece mais sobre esperar uma pessoa do que comida.
  - ⚠ Fala 0 'Vamos esperar, então' fecha de forma fraca, sem resolver a fome ou adicionar contexto.
- nota 3: 0: Estou com uma fome danada. / 1: Paciência, sempre aparece alguém. / 0: Alguma hora vem.
  - ⚠ A fala 'Paciência, sempre aparece alguém.' é vaga e não deixa claro se refere a comida ou a outro animal, o que pode confundir.
  - ⚠ O fecho 'Alguma hora vem.' é redundante e não acrescenta informação, soando como repetição da fala anterior.
- nota 4: 0: Fico de olho para ver se aparece alguém com trigo. / 1: Paciência, sempre aparece alguém. / 0: Alguma hora vem.
  - ⚠ A fala 'Paciência, sempre aparece alguém.' é um pouco genérica e não especifica que tipo de criatura ou pessoa aparece, mas ainda é plausível para animais de fazenda.
- nota 3: 0: Estou com uma fome danada. / 1: Quem espera, come. / 0: Alguma hora vem.
  - ⚠ A fala 'Quem espera, come.' soa como um provérbio humano, não algo que um animal de fazenda diria naturalmente.
  - ⚠ A resposta 'Alguma hora vem.' é vaga e não fecha a conversa de forma clara.

### porco (3.50)  _um porco e dois animais da fazenda; uma situação qualquer do dia a dia_
- nota 3: 0: Barro é a melhor parte da vida. / 1: Não conta para ninguém onde achou. / 0: Sujeira é parte do charme.
  - ⚠ Fala 0 'Barro é a melhor parte da vida.' é plausível para um porco, mas um pouco filosófica demais para um animal de fazenda.
  - ⚠ Fala 1 'Não conta para ninguém onde achou.' parece uma resposta de um animal que esconde comida, mas não fica claro qual animal é e por que esconderia barro.
- nota 3: 0: Fuço o dia inteiro e nunca me canso. / 1: Só cuidado para não se sujar demais. / 0: Depois eu mostro o lugar.
  - ⚠ A fala 'Fuço o dia inteiro e nunca me canso.' é plausível para um porco, mas a resposta 'Só cuidado para não se sujar demais.' é genérica e não especifica qual animal da fazenda está falando, o que po
  - ⚠ O fecho 'Depois eu mostro o lugar.' não conecta claramente com a resposta anterior, deixando a conversa sem um encerramento natural.
- nota 3: 0: Achei algo gostoso enterrado. / 1: Só cuidado para não se sujar demais. / 0: Depois eu mostro o lugar.
  - ⚠ Fala 0 'Achei algo gostoso enterrado.' é vaga e não combina com porco, que normalmente procura comida na superfície ou em comedouros, não enterrada.
  - ⚠ Fala 1 'Só cuidado para não se sujar demais.' soa mais como conselho humano do que algo que um animal de fazenda diria naturalmente.
- nota 4: 0: Achei algo gostoso enterrado. / 1: Só cuidado para não se sujar demais. / 0: Sujeira é parte do charme.
  - ⚠ A fala 'Só cuidado para não se sujar demais.' não especifica qual animal responde, mas é plausível para animais de fazenda.
  - ⚠ A fala 'Sujeira é parte do charme.' é um pouco filosófica para um porco, mas aceitável dado o contexto.
- nota 4: 0: Barro é a melhor parte da vida. / 1: Só cuidado para não se sujar demais. / 0: Depois eu mostro o lugar.
  - ⚠ A fala 'Barro é a melhor parte da vida.' é um pouco filosófica para um porco, mas aceitável no contexto do jogo.
  - ⚠ A resposta 'Só cuidado para não se sujar demais.' não especifica qual animal fala, mas é plausível para animais de fazenda.

### jogador_fome (3.62)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; o jogador está com fome_
- nota 3: 0: Acho que o humano não come há um tempo. / 1: Quem tem comida devia dividir. / 0: Se eu tivesse, dividia.
  - ⚠ Fala 1 'Quem tem comida devia dividir.' soa genérica demais para criaturas calmas como aldeões ou animais de fazenda, sem especificar quem ou o quê.
  - ⚠ Fala 0 final 'Se eu tivesse, dividia.' é vaga e não fecha a conversa de forma natural, deixando a interação sem conclusão clara.
- nota 4: 0: O humano está com cara de fome. / 1: Quem está de barriga vazia não pensa direito. / 0: Se eu tivesse, dividia.
  - ⚠ A fala 'Se eu tivesse, dividia.' é um pouco vaga, pois não especifica o que seria dividido, mas ainda faz sentido no contexto.
- nota 4: 0: Acho que o humano não come há um tempo. / 1: Quem está de barriga vazia não pensa direito. / 0: Se eu tivesse, dividia.
  - ⚠ A fala 'Se eu tivesse, dividia.' é um pouco vaga, pois não especifica o que seria dividido, mas ainda faz sentido no contexto.
- nota 3: 0: O humano está com cara de fome. / 1: Quem tem comida devia dividir. / 0: Se eu tivesse, dividia.
  - ⚠ Fala 1 'Quem tem comida devia dividir.' soa como moral humana, não como algo que um animal ou aldeão diria naturalmente.
  - ⚠ Fala 0 final 'Se eu tivesse, dividia.' é vaga e não fecha a conversa de forma conclusiva.
- nota 4: 0: O humano está com cara de fome. / 1: Comida resolve metade dos problemas. / 0: Se eu tivesse, dividia.
  - ⚠ A fala 'Comida resolve metade dos problemas.' soa filosófica demais para criaturas calmas como aldeões ou animais de fazenda, que tendem a falas simples e práticas.

### jogador_nome_ambos (3.62)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; quem abre sabe o nome do jogador_
- nota 4: 0: Olha quem está aí, Bia. / 1: Será que veio conversar? / 0: Bom ter gente conhecida.
  - ⚠ Fala 0 'Bom ter gente conhecida.' soa um pouco formal para uma conversa curta entre criaturas calmas, mas ainda aceitável.
- nota 4: 0: Bia anda ocupado hoje. / 1: É sempre bom ver Bia por aqui. / 0: Vou dar um oi.
  - ⚠ Fala 0 'Bia anda ocupado hoje.' usa 'ocupado' no masculino, mas 'Bia' é tipicamente um nome feminino; deveria ser 'ocupada'.
  - ⚠ Fala 0 'Vou dar um oi.' é um fecho um pouco abrupto, mas aceitável.
- nota 3: 0: Bia anda ocupado hoje. / 1: Será que veio conversar? / 0: Bom ter gente conhecida.
  - ⚠ Fala 0 'Bia anda ocupado hoje.' usa 'ocupado' no masculino, mas 'Bia' é tipicamente nome feminino; inconsistência de gênero.
  - ⚠ Fala 1 'Será que veio conversar?' é vaga e não responde diretamente à observação sobre Bia estar ocupado.
- nota 3: 0: Bia voltou! Que bom ver essa pessoa. / 1: Será que veio conversar? / 0: Vou dar um oi.
  - ⚠ Fala 0 'Bia voltou! Que bom ver essa pessoa.' soa estranho para criaturas calmas como vacas ou ovelhas, que não demonstrariam alegria explícita.
  - ⚠ Fala 1 'Será que veio conversar?' é muito reflexiva para animais de fazenda, que não filosofam.
- nota 2: 0: Bia anda ocupado hoje. / 1: Eu também fico contente. / 0: Bom ter gente conhecida.
  - ⚠ A fala 'Eu também fico contente.' não responde à observação 'Bia anda ocupado hoje.'
  - ⚠ O fecho 'Bom ter gente conhecida.' não conclui a conversa de forma coerente com as falas anteriores.

### jogador_nome_conta (3.62)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; quem abre sabe o nome do jogador_
- nota 3: 0: Sabia que essa pessoa se chama Bia? / 1: Que nome bonito, Bia. / 0: Nome é a primeira coisa que a gente deveria saber.
  - ⚠ Fala 0: 'Sabia que essa pessoa se chama Bia?' soa estranho para criaturas calmas como aldeões ou animais, pois não é comum que saibam o nome do jogador.
  - ⚠ Fala 1: 'Que nome bonito, Bia.' é uma resposta vaga e não combina com o mundo de criaturas simples, que não costumam elogiar nomes.
- nota 3: 0: O nome daquele humano é Bia, ele me disse. / 1: Que nome bonito, Bia. / 0: Aqui a gente se trata pelo nome.
  - ⚠ Fala 0: 'Aqui a gente se trata pelo nome' soa como uma regra social humana, não típica de criaturas calmas como animais de fazenda ou aldeões.
  - ⚠ Fala 1: 'Que nome bonito, Bia' é uma reação muito humana e afetiva para criaturas que normalmente não expressam opiniões estéticas sobre nomes.
- nota 4: 0: O nome daquele humano é Bia, ele me disse. / 1: Boa, agora posso chamar pelo nome. / 0: É bom saber com quem se fala.
  - ⚠ Fala 0: 'O nome daquele humano é Bia, ele me disse.' - 'ele' é ambíguo, pois o humano é Bia, mas 'ele' pode se referir ao próprio humano ou a outro; melhor seria 'ela me disse' ou 'o humano me disse'.
  - ⚠ Fala 1: 'Boa, agora posso chamar pelo nome.' - 'Boa' é uma gíria que pode soar estranha para criaturas calmas como aldeões ou animais de fazenda; talvez 'Que bom' seja mais natural.
- nota 3: 0: Sabia que essa pessoa se chama Bia? / 1: Boa, agora posso chamar pelo nome. / 0: É bom saber com quem se fala.
  - ⚠ Fala 1 'Boa, agora posso chamar pelo nome.' soa como se a criatura tivesse acabado de aprender o nome, mas a fala 0 já o menciona, criando redundância.
  - ⚠ Fala 0 'É bom saber com quem se fala.' é genérica e não fecha a conversa de forma natural para criaturas calmas.
- nota 4: 0: Me contou pessoalmente: é Bia. / 1: Boa, agora posso chamar pelo nome. / 0: Aqui a gente se trata pelo nome.
  - ⚠ A fala 'Aqui a gente se trata pelo nome.' soa um pouco formal demais para criaturas calmas como animais de fazenda ou aldeões, que tenderiam a frases mais simples.

### jogador_sem_nome (3.62)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; quem abre já conhece o jogador; NÃO: quem abre sabe o nome do jogador_
- nota 3: 0: Nunca perguntei como se chama o humano. / 1: Nunca perguntei. Para mim é só um humano. / 0: Quem sabe conta.
  - ⚠ A fala 'Quem sabe conta.' é vaga e não fecha a conversa de forma natural.
  - ⚠ A resposta 'Nunca perguntei. Para mim é só um humano.' repete a ideia da fala anterior sem acrescentar informação.
- nota 4: 0: Será que aquele humano tem nome? / 1: Alguém devia perguntar. / 0: Da próxima vez eu pergunto.
  - ⚠ A fala 'Alguém devia perguntar.' soa um pouco vaga, mas é aceitável para criaturas calmas.
- nota 4: 0: Será que aquele humano tem nome? / 1: Alguém devia perguntar. / 0: Quem sabe conta.
  - ⚠ A fala 'Quem sabe conta.' é um pouco vaga e não fecha claramente a conversa, podendo soar como uma frase solta.
- nota 3: 0: Nunca perguntei como se chama o humano. / 1: Nem me fale. / 0: Quem sabe conta.
  - ⚠ A fala 'Nem me fale.' soa como desinteresse, mas não responde diretamente à pergunta sobre o nome do humano.
  - ⚠ O fecho 'Quem sabe conta.' é vago e não conclui a conversa de forma natural.
- nota 4: 0: Será que aquele humano tem nome? / 1: Deve ter, todo mundo tem. / 0: Quem sabe conta.
  - ⚠ A fala 'Quem sabe conta.' é um pouco vaga e não fecha claramente a conversa, mas é aceitável.

### clerigo (3.75)  _villager:cleric e dois aldeões; uma situação qualquer do dia a dia_
- nota 3: 0: Dizem que meu laboratório cheira esquisito. / 1: Se alguém ficar doente, você dá um jeito. / 0: Poção boa tem de tudo um pouco.
  - ⚠ A fala 'Poção boa tem de tudo um pouco' não fecha claramente a conversa iniciada sobre o cheiro do laboratório.
  - ⚠ A resposta 'Se alguém ficar doente, você dá um jeito' não se conecta diretamente com a fala inicial sobre o cheiro.
- nota 4: 0: Tem coisa estranha misturada no meu caldeirão, e funciona. / 1: Cheira mesmo, mas a gente se acostuma. / 0: É tudo ciência, juro.
  - ⚠ Fala 1: 'Cheira mesmo, mas a gente se acostuma.' soa um pouco vaga e não responde diretamente à estranheza mencionada na fala 0.
  - ⚠ Fala 0: 'É tudo ciência, juro.' pode ser considerada levemente anacrônica para um aldeão clérigo, que normalmente fala de poções e magia, não de ciência.
- nota 4: 0: Dizem que meu laboratório cheira esquisito. / 1: Se alguém ficar doente, você dá um jeito. / 0: É tudo ciência, juro.
  - ⚠ A fala 'É tudo ciência, juro.' soa um pouco moderna para um aldeão clérigo, mas é aceitável.
- nota 4: 0: As poções de hoje ficaram bonitas. / 1: Se alguém ficar doente, você dá um jeito. / 0: Poção boa tem de tudo um pouco.
  - ⚠ A fala 'Poção boa tem de tudo um pouco' é um pouco vaga e não fecha claramente a conversa, mas é aceitável para um aldeão clérigo.
- nota 4: 0: Dizem que meu laboratório cheira esquisito. / 1: Prefiro não provar nada daí. / 0: Poção boa tem de tudo um pouco.
  - ⚠ A fala 'Poção boa tem de tudo um pouco' soa um pouco vaga e não fecha claramente a conversa sobre o cheiro do laboratório.

### jogador_ferido (3.75)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; o jogador está ferido_
- nota 4: 0: O humano parece ferido. / 1: Uma boa refeição ajuda a se curar. / 0: Que se recupere logo.
  - ⚠ A fala 'Uma boa refeição ajuda a se curar.' soa mais como conselho de aldeão do que de animal de fazenda, mas ainda é plausível para criaturas calmas.
- nota 4: 0: O humano parece ferido. / 1: Alguém devia oferecer ajuda. / 0: Ao menos está vivo.
  - ⚠ A fala 'Alguém devia oferecer ajuda.' soa um pouco vaga e não combina totalmente com a passividade típica de criaturas calmas como aldeões ou animais de fazenda, que normalmente não sugerem ações altr
- nota 4: 0: O humano parece ferido. / 1: Alguém devia oferecer ajuda. / 0: Que se recupere logo.
  - ⚠ A fala 'Alguém devia oferecer ajuda.' soa mais como um pensamento humano do que algo que um aldeão ou animal diria naturalmente.
- nota 3: 0: O humano parece ferido. / 1: Uma boa refeição ajuda a se curar. / 0: Ao menos está vivo.
  - ⚠ A fala 'Uma boa refeição ajuda a se curar.' soa como conselho de aldeão, mas se for dita por um animal de fazenda, não combina com seu mundo.
  - ⚠ A fala 'Ao menos está vivo.' é um fecho fraco, pois não responde diretamente à sugestão anterior.
- nota 4: 0: O humano parece ferido. / 1: Bem que podia descansar um pouco. / 0: Que se recupere logo.
  - ⚠ A fala 'Bem que podia descansar um pouco.' soa um pouco estranha para criaturas calmas, pois 'bem que podia' é uma expressão mais humana e coloquial, mas ainda aceitável.
  - ⚠ A última fala 'Que se recupere logo.' é um fecho adequado, mas poderia ser mais natural se fosse algo como 'Tomara que melhore.'

### creeper_zumbi (3.88)  _um creeper e um zumbi; uma situação qualquer do dia a dia_
- nota 4: 0: Ssss... hoje eu exploro. / 1: Grrr... explode longe de mim. / 0: Ssss...
  - ⚠ A fala do creeper 'Ssss... hoje eu exploro.' é ambígua: 'exploro' pode ser entendido como explorar o ambiente, mas para um creeper o verbo 'explodir' seria mais característico; ainda assim, é aceitáve
  - ⚠ A resposta do zumbi 'Grrr... explode longe de mim.' é coerente, mas o fecho 'Ssss...' do creeper é muito vago e não encerra claramente a conversa.
- nota 4: 0: Ssss... hoje eu exploro. / 1: Grrr... explode longe de mim. / 0: Ssss... silêncio.
  - ⚠ A fala 'Ssss... hoje eu exploro.' é um pouco estranha para um creeper, pois 'explorar' não é uma ação típica associada a creepers, que geralmente se aproximam e explodem.
  - ⚠ A resposta 'Grrr... explode longe de mim.' do zumbi é coerente, mas o fecho 'Ssss... silêncio.' do creeper parece abrupto e não encerra a conversa de forma natural.
- nota 3: 0: Ssss... hoje eu exploro. / 1: Grrr... gato é bicho ruim. / 0: Ssss... silêncio.
  - ⚠ Fala 0 'Ssss... hoje eu exploro.' não combina com creeper, que não explora, apenas vagueia e explode.
  - ⚠ Fala 1 'Grrr... gato é bicho ruim.' é uma resposta desconexa da fala anterior.
- nota 4: 0: Ssss... não chegue perto de gato. / 1: Grrr... gato é bicho ruim. / 0: Ssss... silêncio.
  - ⚠ A fala 'Ssss... silêncio.' é um fecho um pouco abrupto, mas aceitável para um creeper.
- nota 2: 0: Ssss... hoje eu exploro. / 1: Grrr... gato é bicho ruim. / 0: Ssss...
  - ⚠ Fala 0 'Ssss... hoje eu exploro.' não combina com creeper, que não explora, apenas vagueia e explode.
  - ⚠ Fala 1 'Grrr... gato é bicho ruim.' não responde à fala anterior, quebra a conversa.

### curiosidade_quadrado (3.88)  _dois aldeões; uma situação qualquer do dia a dia_
- nota 3: 0: Você já parou para pensar por que tudo aqui é quadrado? / 1: Que ideia estranha. / 0: Pois é, o mundo é cheio de mistérios.
  - ⚠ Aldeões não costumam filosofar sobre o mundo ser quadrado; fala 0 soa mais como jogador do que aldeão.
  - ⚠ A resposta 'Que ideia estranha.' é vaga e não desenvolve a conversa, deixando o fecho fraco.
- nota 3: 0: Sonhei que o mundo era redondo. / 1: Nem me fale. / 0: Pois é, o mundo é cheio de mistérios.
  - ⚠ A fala 'Sonhei que o mundo era redondo' é filosófica demais para um aldeão comum, que normalmente fala de trocas, colheitas ou fofocas.
  - ⚠ A resposta 'Nem me fale' não se conecta claramente com o sonho mencionado, soando vaga.
- nota 3: 0: Sonhei que o mundo era redondo. / 1: Nem me fale. / 0: Melhor nem pensar muito nisso.
  - ⚠ A fala 'Sonhei que o mundo era redondo.' é estranha para um aldeão, pois eles não demonstram conhecimento de astronomia ou filosofia.
  - ⚠ A resposta 'Nem me fale.' não se conecta claramente com o sonho mencionado, parecendo uma reação genérica.
- nota 3: 0: Sonhei que o mundo era redondo. / 1: Pois é. / 0: Pelo menos é um mundo bonito.
  - ⚠ A fala 'Sonhei que o mundo era redondo.' é filosófica demais para um aldeão, que normalmente fala de trocas, colheitas ou perigos.
  - ⚠ A resposta 'Pois é.' é vaga e não desenvolve a conversa, parecendo desinteressada.
- nota 4: 0: Você já parou para pensar por que tudo aqui é quadrado? / 1: Nunca tinha reparado, agora vou pensar nisso o dia todo. / 0: Pois é, o mundo é cheio de mistérios.
  - ⚠ A fala 'Você já parou para pensar por que tudo aqui é quadrado?' soa filosófica demais para um aldeão, que normalmente fala de coisas práticas como profissões e trocas.

### golem_flor (3.88)  _um golem de ferro e dois aldeões; é de dia_
- nota 3: 0: Já tem muito tempo que não vejo ninguém sorrir por aqui. / 1: Que gesto bonito. / 0: É o meu jeito de agradecer.
  - ⚠ Fala 0: 'Já tem muito tempo que não vejo ninguém sorrir por aqui' soa filosófica demais para um golem de ferro, que é uma criatura protetora e não costuma refletir sobre emoções.
  - ⚠ Fala 1: 'Que gesto bonito' é vaga e não responde diretamente à fala anterior, que não menciona nenhum gesto.
- nota 4: 0: Hoje encontrei uma flor perto do caminho. / 1: Isso alegra a vila toda. / 0: Cuidar também é isso.
  - ⚠ A fala 'Cuidar também é isso' é um pouco vaga, mas aceitável para um golem.
- nota 3: 0: Hoje encontrei uma flor perto do caminho. / 1: Que gesto bonito. / 0: É o meu jeito de agradecer.
  - ⚠ Golem de ferro não fala sobre flores ou gestos bonitos; sua fala é mais protetora e direta.
  - ⚠ Aldeão respondendo 'Que gesto bonito' não combina com o comportamento típico de aldeões, que são mais focados em trocas e trabalho.
- nota 3: 0: Já tem muito tempo que não vejo ninguém sorrir por aqui. / 1: Flor combina com você, mesmo sendo de ferro. / 0: É o meu jeito de agradecer.
  - ⚠ Golem de ferro não costuma falar de forma sentimental ou poética; sua fala 'Já tem muito tempo que não vejo ninguém sorrir por aqui' soa filosófica demais para a criatura.
  - ⚠ A resposta do aldeão 'Flor combina com você, mesmo sendo de ferro' é estranha, pois aldeões não têm o hábito de elogiar golems dessa forma e a menção a flor não se conecta claramente com a fala anteri
- nota 3: 0: Já tem muito tempo que não vejo ninguém sorrir por aqui. / 1: Que gesto bonito. / 0: Cuidar também é isso.
  - ⚠ Fala 0 'Já tem muito tempo que não vejo ninguém sorrir por aqui.' soa filosófica demais para um golem de ferro, que costuma ser protetor e direto.
  - ⚠ Fala 1 'Que gesto bonito.' não responde diretamente à observação do golem, deixando a conversa vaga.

### jogador_conhecido (3.88)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; quem abre já conhece o jogador_
- nota 3: 0: Já cruzei com o humano por aqui antes. / 1: Que interessante. / 0: Foi tranquilo, achei simpático.
  - ⚠ A fala 'Que interessante.' é muito vaga e não combina com a resposta esperada de uma criatura da mesma espécie em uma conversa casual.
  - ⚠ O fecho 'Foi tranquilo, achei simpático.' não responde diretamente à fala anterior e soa deslocado.
- nota 4: 0: O humano e eu já conversamos um pouco. / 1: Me conta, o que foi que disse? / 0: Foi tranquilo, achei simpático.
  - ⚠ Fala 1 'Me conta, o que foi que disse?' soa um pouco formal para uma conversa casual entre criaturas da mesma espécie, mas é aceitável.
- nota 3: 0: Eu já conversei com o humano, sabia? / 1: Me conta, o que foi que disse? / 0: Cada conversa é diferente.
  - ⚠ Fala 0 'Cada conversa é diferente.' não fecha a conversa de forma clara, soa vaga e não responde diretamente à pergunta 'o que foi que disse?'
  - ⚠ A resposta 'Me conta, o que foi que disse?' é natural, mas a fala final deixa a conversa sem conclusão, parecendo incompleta.
- nota 4: 0: Já cruzei com o humano por aqui antes. / 1: E como foi? / 0: Dá para conversar numa boa.
  - ⚠ A fala 'Dá para conversar numa boa.' soa um pouco vaga e não fecha a conversa de forma conclusiva.
- nota 4: 0: Eu já conversei com o humano, sabia? / 1: Me conta, o que foi que disse? / 0: Foi tranquilo, achei simpático.
  - ⚠ A fala 'Foi tranquilo, achei simpático.' soa um pouco vaga e não detalha o que o humano disse, mas ainda é aceitável.

### ovelha (3.88)  _uma ovelha e dois animais da fazenda; uma situação qualquer do dia a dia_
- nota 4: 0: Minha lã está crescendo de novo. / 1: Tem que tosar logo, hein? / 0: Depois eu cresço de novo.
  - ⚠ A fala 'Depois eu cresço de novo.' é ambígua: parece que a ovelha cresce, não a lã.
- nota 4: 0: Mééé, passei o dia mordiscando grama. / 1: Tem que tosar logo, hein? / 0: Eu gosto do vento na lã.
  - ⚠ A fala 'Tem que tosar logo, hein?' não especifica qual dos dois animais da fazenda está falando, mas é plausível para um animal de fazenda.
  - ⚠ A resposta 'Eu gosto do vento na lã.' é um fecho um pouco desconexo, pois não responde diretamente à sugestão de tosar, mas ainda é aceitável como uma divagação da ovelha.
- nota 4: 0: Mééé, passei o dia mordiscando grama. / 1: Tem que tosar logo, hein? / 0: Depois eu cresço de novo.
  - ⚠ A fala 'Tem que tosar logo, hein?' não especifica qual dos dois animais da fazenda está falando, mas é plausível para um animal de fazenda.
  - ⚠ A resposta 'Depois eu cresço de novo.' é um fecho aceitável, mas poderia ser mais natural se referisse diretamente à tosquia.
- nota 4: 0: Mééé, passei o dia mordiscando grama. / 1: A lã fica bonita assim. / 0: Depois eu cresço de novo.
  - ⚠ A fala 'A lã fica bonita assim.' não especifica qual animal responde, mas é plausível para animais da fazenda.
  - ⚠ O fecho 'Depois eu cresço de novo.' é um pouco vago, mas aceitável.
- nota 4: 0: Mééé, passei o dia mordiscando grama. / 1: A lã fica bonita assim. / 0: Eu gosto do vento na lã.
  - ⚠ A fala 'A lã fica bonita assim.' não especifica qual animal responde, mas é plausível para animais da fazenda.
  - ⚠ A resposta 'Eu gosto do vento na lã.' é um fecho aceitável, mas poderia ser mais conectada à fala anterior.

### aranha_zumbi (4.00)  _uma aranha e um zumbi; uma situação qualquer do dia a dia_
- nota 4: 0: Sss... eu fico no teto, você no chão. / 1: Grrr... teia gruda. / 0: Sss... uma corrida, então.
  - ⚠ Fala 1 'Grrr... teia gruda.' é um pouco vaga, mas aceitável para um zumbi.
  - ⚠ Fala 0 final 'Sss... uma corrida, então.' não deixa claro o que seria a corrida, mas fecha a conversa de forma razoável.
- nota 4: 0: Sss... eu fico no teto, você no chão. / 1: Grrr... teia gruda. / 0: Sss... paciência.
  - ⚠ A fala 'Grrr... teia gruda.' é um pouco vaga, mas aceitável para um zumbi.
  - ⚠ A resposta 'Sss... paciência.' fecha a conversa de forma um tanto abrupta, mas ainda faz sentido.
- nota 4: 0: Sss... a sua fome é barulhenta. / 1: Grrr... teia gruda. / 0: Sss... uma corrida, então.
  - ⚠ A fala 'Grrr... teia gruda.' é um pouco vaga e não responde diretamente à provocação da aranha, mas ainda é plausível para um zumbi.
  - ⚠ A fala 'Sss... uma corrida, então.' fecha a conversa de forma um pouco abrupta, mas aceitável.
- nota 4: 0: Sss... eu fico no teto, você no chão. / 1: Grrr... quem pegar o humano primeiro. / 0: Sss... uma corrida, então.
  - ⚠ A fala 'Grrr... quem pegar o humano primeiro.' soa como um desafio competitivo, o que é plausível para um zumbi, mas a resposta da aranha 'Sss... uma corrida, então.' não deixa claro se ela aceita ou 
- nota 4: 0: Sss... a sua fome é barulhenta. / 1: Grrr... quem pegar o humano primeiro. / 0: Sss... uma corrida, então.
  - ⚠ A fala do zumbi 'Grrr... quem pegar o humano primeiro.' soa como uma pergunta incompleta, mas pode ser interpretada como um desafio.

### bicho_armadura (4.00)  _dois animais da fazenda; o jogador veste armadura_
- nota 4: 0: O que será aquilo que o humano está vestindo? / 1: Parece pesado. / 0: Pelo visto ele sabe se cuidar.
  - ⚠ A fala 'Pelo visto ele sabe se cuidar.' é um pouco abstrata para animais de fazenda, mas ainda aceitável.
- nota 4: 0: O humano está todo coberto de metal. / 1: Será que protege de lobo? / 0: Pelo visto ele sabe se cuidar.
  - ⚠ A fala 'Será que protege de lobo?' é um pouco vaga, mas plausível para animais de fazenda que temem lobos.
  - ⚠ A fala 'Pelo visto ele sabe se cuidar.' soa levemente reflexiva demais para um animal de fazenda, mas ainda aceitável.
- nota 4: 0: O humano veste uma armadura de ferro. Brilha até de longe. / 1: Parece pesado. / 0: Pelo visto ele sabe se cuidar.
  - ⚠ A fala 'Pelo visto ele sabe se cuidar.' é um pouco abstrata para animais de fazenda, mas ainda aceitável.
- nota 4: 0: O humano veste uma armadura de ferro. Brilha até de longe. / 1: Eu não vestiria isso. / 0: Pelo visto ele sabe se cuidar.
  - ⚠ A fala 'Eu não vestiria isso.' é um pouco vaga e não deixa claro o motivo, mas ainda é plausível para um animal da fazenda.
- nota 4: 0: O humano está todo coberto de metal. / 1: Eu não vestiria isso. / 0: Pelo visto ele sabe se cuidar.
  - ⚠ A fala 'Eu não vestiria isso.' é um pouco vaga, mas aceitável para um animal de fazenda.
  - ⚠ A fala 'Pelo visto ele sabe se cuidar.' soa levemente filosófica para um animal de fazenda, mas ainda passável.

### bicho_jogador_chuva (4.00)  _dois animais da fazenda; está chovendo_
- nota 3: 0: O humano está na chuva. / 1: Ele não se importa? / 0: Eu só queria sombra.
  - ⚠ Fala 0 'Eu só queria sombra' não fecha a conversa de forma coerente com a chuva mencionada anteriormente.
  - ⚠ Fala 1 'Ele não se importa?' é uma pergunta que não é respondida diretamente, deixando a conversa sem conclusão.
- nota 4: 0: Olha o humano debaixo da chuva. / 1: Ele não se importa? / 0: Eu só queria sombra.
  - ⚠ A fala 'Eu só queria sombra' não fecha a conversa de forma clara, pois não responde diretamente à pergunta 'Ele não se importa?' e introduz um desejo não relacionado à chuva.
- nota 4: 0: O humano está na chuva. / 1: Eu prefiro um canto seco. / 0: Eu só queria sombra.
  - ⚠ A fala 'Eu só queria sombra' não fecha a conversa de forma clara, pois não responde diretamente à preferência por um canto seco.
- nota 4: 0: O humano está na chuva. / 1: Eu prefiro um canto seco. / 0: Cada um com o seu gosto.
  - ⚠ Fala 0 'O humano está na chuva.' é um pouco observacional demais para um animal de fazenda, mas aceitável.
  - ⚠ Fala 1 'Eu prefiro um canto seco.' soa mais como um animal doméstico do que de fazenda, mas ainda plausível.
- nota 4: 0: Olha o humano debaixo da chuva. / 1: Eu prefiro um canto seco. / 0: Cada um com o seu gosto.
  - ⚠ A fala 'Eu prefiro um canto seco' é um pouco vaga, mas aceitável para animais da fazenda.

### bicho_jogador_noite (4.00)  _dois animais da fazenda; é noite_
- nota 4: 0: Olha lá, o humano no escuro. / 1: Eu fico quieto. / 0: É um bicho corajoso, esse aí.
  - ⚠ Fala 1 'Eu fico quieto.' é vaga e não responde diretamente à observação sobre o humano no escuro.
  - ⚠ Fala 0 final 'É um bicho corajoso, esse aí.' usa 'bicho' para se referir ao humano, o que pode soar estranho vindo de animais da fazenda.
- nota 4: 0: Olha lá, o humano no escuro. / 1: Cada um com o seu jeito. / 0: Eu não sairia.
  - ⚠ Fala 1 'Cada um com o seu jeito.' é um pouco vaga e não responde diretamente à observação sobre o humano no escuro.
- nota 4: 0: O humano caminha de noite. / 1: Eu fico quieto. / 0: É um bicho corajoso, esse aí.
  - ⚠ Fala 0: 'O humano caminha de noite.' soa como observação neutra, mas poderia ser mais natural para animais da fazenda, que normalmente expressariam surpresa ou medo.
  - ⚠ Fala 1: 'Eu fico quieto.' é uma resposta plausível, mas não conecta diretamente com a fala anterior; poderia indicar por que fica quieto (ex: medo).
- nota 3: 0: Tem alguém andando no escuro. / 1: Cada um com o seu jeito. / 0: É um bicho corajoso, esse aí.
  - ⚠ Fala 1 'Cada um com o seu jeito.' não responde diretamente à observação sobre alguém andando no escuro.
  - ⚠ Fala 0 final 'É um bicho corajoso, esse aí.' não fecha a conversa de forma conclusiva, apenas adiciona um comentário.
- nota 4: 0: Tem alguém andando no escuro. / 1: Eu fico quieto. / 0: Eu não sairia.
  - ⚠ Fala 1 'Eu fico quieto.' é um pouco vaga, mas aceitável para um animal assustado.
  - ⚠ Fala 0 'Eu não sairia.' soa como conselho humano, mas ainda plausível para animais cautelosos.

### cavalo (4.00)  _um cavalo e dois animais da fazenda; é de dia_
- nota 4: 0: Dá vontade de correr até o vento passar por mim. / 1: Corre mesmo, que eu fico olhando. / 0: Mais tarde eu corro.
  - ⚠ A fala 'Corre mesmo, que eu fico olhando.' soa um pouco passiva para um animal de fazenda, mas é aceitável.
  - ⚠ A resposta 'Mais tarde eu corro.' é um fecho fraco, pois não encerra a conversa de forma natural.
- nota 3: 0: Hoje o campo está livre, só falta espaço. / 1: Corre mesmo, que eu fico olhando. / 0: Mais tarde eu corro.
  - ⚠ Fala 0 'Hoje o campo está livre, só falta espaço.' é vaga e não combina claramente com um cavalo; poderia ser dita por qualquer animal.
  - ⚠ Fala 1 'Corre mesmo, que eu fico olhando.' soa mais como um comentário humano do que de um animal de fazenda.
- nota 4: 0: Adoro um bom galope. / 1: Eu prefiro trotar devagar e olhar a paisagem. / 0: Mais tarde eu corro.
  - ⚠ A fala 'Eu prefiro trotar devagar e olhar a paisagem.' pode ser dita por qualquer animal da fazenda, mas trotar é específico de cavalos; se for uma vaca ou ovelha, soa estranho.
  - ⚠ A fala 'Mais tarde eu corro.' é um fecho fraco, pois não responde diretamente à preferência do outro animal, apenas adiciona uma ação futura sem conexão clara.
- nota 4: 0: Adoro um bom galope. / 1: Corre mesmo, que eu fico olhando. / 0: Correr é o que eu faço de melhor.
  - ⚠ A fala 'Corre mesmo, que eu fico olhando.' soa um pouco vaga e não especifica qual animal da fazenda está falando, mas é aceitável.
- nota 3: 0: Hoje o campo está livre, só falta espaço. / 1: Só cuide para não cair em buraco. / 0: Mais tarde eu corro.
  - ⚠ Fala 0 'Hoje o campo está livre, só falta espaço.' é vaga e não combina claramente com um cavalo ou animais de fazenda.
  - ⚠ Fala 1 'Só cuide para não cair em buraco.' parece um aviso humano, não algo que um animal diria naturalmente.

### jogador_monstros_visto (4.00)  _dois monstros hostis; uma situação qualquer do dia a dia_
- nota 4: 0: Humano à vista. / 1: Eu chego pela esquerda. / 0: Eu avisei.
  - ⚠ Fala 1 'Eu chego pela esquerda.' é um pouco específica demais para um monstro hostil, mas ainda plausível em um contexto de emboscada.
  - ⚠ Fala 0 'Eu avisei.' soa levemente vingativa, o que pode ser estranho para monstros, mas aceitável.
- nota 4: 0: Aquele humano não está nos vendo. / 1: Cuidado com o que ele carrega. / 0: Agora não. Espera o momento certo.
  - ⚠ Fala 1: 'Cuidado com o que ele carrega.' é um pouco vaga, mas plausível para monstros hostis.
  - ⚠ Fala 0 (fecho): 'Agora não. Espera o momento certo.' soa natural, mas poderia ser mais específica sobre o que estão esperando.
- nota 4: 0: Humano à vista. / 1: Silêncio, chega mais perto. / 0: Eu avisei.
  - ⚠ A fala 'Silêncio, chega mais perto.' é um pouco ambígua: pode ser interpretada como ordem para o outro monstro se aproximar, mas o contexto sugere que é sobre o humano se aproximar; falta clareza.
- nota 4: 0: Humano à vista. / 1: Eu chego pela esquerda. / 0: Agora não. Espera o momento certo.
  - ⚠ Fala 1 'Eu chego pela esquerda.' soa tática demais para monstros hostis genéricos, mas é aceitável se forem criaturas como esqueletos ou zumbis.
- nota 4: 0: Aquele humano não está nos vendo. / 1: Silêncio, chega mais perto. / 0: Eu avisei.
  - ⚠ A fala 'Eu avisei.' não fecha claramente a conversa, pois não responde diretamente à ordem 'Silêncio, chega mais perto.'

### bicho_item (4.12)  _dois animais da fazenda; o jogador segura um item; NÃO: quem abre GOSTA do que o jogador segura_
- nota 4: 0: Aquela pessoa carrega trigo. Cheiro estranho. / 1: Isso não me interessa. / 0: Cada um com o seu gosto.
  - ⚠ Fala 1 'Isso não me interessa.' soa um pouco seco e não desenvolve a conversa, mas é plausível para um animal desinteressado.
- nota 4: 0: Aquela pessoa carrega trigo. Cheiro estranho. / 1: Não parece comida. / 0: Por mim, pode ficar com ele.
  - ⚠ Fala 0: 'Aquela pessoa carrega trigo. Cheiro estranho.' - Trigo é comida típica de fazenda, então 'cheiro estranho' e 'não parece comida' são contraditórios para animais de fazenda.
  - ⚠ Fala 1: 'Não parece comida.' - Se é trigo, deveria parecer comida para animais de fazenda.
- nota 3: 0: Olha só, trigo na mão de o humano. O que será isso? / 1: Isso não me interessa. / 0: Cada um com o seu gosto.
  - ⚠ A fala 'Isso não me interessa.' soa seca e não combina com a curiosidade inicial de 'Olha só, trigo na mão de o humano. O que será isso?'
  - ⚠ O fecho 'Cada um com o seu gosto.' não responde diretamente à indiferença da segunda fala, deixando a conversa sem conclusão natural.
- nota 4: 0: Olha só, trigo na mão de o humano. O que será isso? / 1: Eu prefiro capim. / 0: Cada um com o seu gosto.
  - ⚠ A fala 'Olha só, trigo na mão de o humano. O que será isso?' soa estranha: 'de o humano' deveria ser 'do humano'.
  - ⚠ A pergunta 'O que será isso?' não combina com a situação, pois o animal reconhece o trigo, então não deveria questionar o que é.
- nota 4: 0: Aquela pessoa carrega trigo. Cheiro estranho. / 1: Eu prefiro capim. / 0: Por mim, pode ficar com ele.
  - ⚠ Fala 0: 'Aquela pessoa carrega trigo. Cheiro estranho.' - 'Cheiro estranho' é vago e não combina com a reação típica de animais de fazenda ao trigo, que normalmente é atrativo.
  - ⚠ Fala 1: 'Eu prefiro capim.' - Resposta plausível, mas não desenvolve a estranheza mencionada, deixando a conversa um pouco desconexa.

### medo_lobo (4.12)  _dois animais da fazenda; é noite_
- nota 4: 0: À noite o campo me assusta. / 1: Lobo não gosta de rebanho junto. / 0: Vamos ficar de olho.
  - ⚠ A fala 'Lobo não gosta de rebanho junto.' soa um pouco desconexa da fala anterior, pois não responde diretamente ao medo expresso, embora faça sentido no contexto de ameaça noturna.
- nota 4: 0: Já ouviu um uivo de lobo por perto? / 1: Lobo não gosta de rebanho junto. / 0: Vamos ficar de olho.
  - ⚠ A fala 'Lobo não gosta de rebanho junto.' é um pouco vaga e não responde diretamente à pergunta sobre ter ouvido um uivo.
- nota 4: 0: Escutei um rosnado lá longe. / 1: Chega pertinho de mim, que é mais seguro. / 0: Se vier, a gente corre.
  - ⚠ A fala 'Chega pertinho de mim, que é mais seguro.' soa um pouco humana demais para animais de fazenda, mas é aceitável no contexto do jogo.
- nota 4: 0: Já ouviu um uivo de lobo por perto? / 1: Chega pertinho de mim, que é mais seguro. / 0: Vamos ficar de olho.
  - ⚠ A fala 'Chega pertinho de mim, que é mais seguro.' soa um pouco protetora demais para animais de fazenda, mas é aceitável.
- nota 4: 0: Escutei um rosnado lá longe. / 1: Chega pertinho de mim, que é mais seguro. / 0: Vamos ficar de olho.
  - ⚠ A fala 'Chega pertinho de mim, que é mais seguro' soa um pouco protetora demais para animais de fazenda, mas é aceitável.

### zumbi_esqueleto (4.12)  _um zumbi e um esqueleto; uma situação qualquer do dia a dia_
- nota 4: 0: Dá para parar de me acertar com flecha? / 1: De perto você só arrasta o pé. / 0: Hmm... tá bom.
  - ⚠ A fala do esqueleto 'De perto você só arrasta o pé.' é um pouco vaga e não responde diretamente ao pedido do zumbi, mas ainda faz sentido como provocação.
- nota 4: 0: Dá para parar de me acertar com flecha? / 1: Seus gemidos avisam a presa. / 0: Hmm... tá bom.
  - ⚠ A fala do esqueleto 'Seus gemidos avisam a presa' é um pouco formal para uma conversa casual, mas aceitável.
  - ⚠ O fecho 'Hmm... tá bom.' é um pouco vago, mas fecha a conversa de forma natural.
- nota 4: 0: Você só sabe atirar de longe. / 1: De perto você só arrasta o pé. / 0: Grrr... vamos caçar juntos.
  - ⚠ Fala 0 'Você só sabe atirar de longe.' é uma provocação típica de zumbi, mas poderia ser mais agressiva.
  - ⚠ Fala 1 'De perto você só arrasta o pé.' é uma resposta adequada, mas 'arrasta o pé' pode soar estranho para esqueleto.
- nota 4: 0: Dá para parar de me acertar com flecha? / 1: Seus gemidos avisam a presa. / 0: Grrr... vamos caçar juntos.
  - ⚠ A fala 'Seus gemidos avisam a presa.' é um pouco longa e formal para um esqueleto, que tende a ser mais direto.
  - ⚠ O fecho 'Grrr... vamos caçar juntos.' é aceitável, mas poderia ser mais natural para um zumbi, que normalmente não propõe cooperação.
- nota 4: 0: Seus ossos fazem barulho demais. / 1: Foi sem querer. Quase. / 0: Grrr... vamos caçar juntos.
  - ⚠ A fala 'Foi sem querer. Quase.' é um pouco vaga e não responde diretamente à reclamação sobre o barulho dos ossos, mas ainda é plausível para um esqueleto.
  - ⚠ O fecho 'Grrr... vamos caçar juntos.' muda o tom de reclamação para cooperação de forma abrupta, mas é aceitável para zumbis.

### bicho_barulho (4.25)  _dois animais da fazenda; é noite_
- nota 4: 0: Escutei uns passos no escuro. / 1: Fica perto de mim. / 0: Vou ficar de olho.
  - ⚠ Falta especificidade: 'dois animais da fazenda' é genérico; sem saber quais animais, não dá para avaliar se as falas combinam com o mundo de cada um.
  - ⚠ A fala 'Vou ficar de olho' pode soar estranha para alguns animais (ex.: vaca, ovelha), pois remete a vigilância ativa, algo mais típico de lobos ou gatos.
- nota 4: 0: Tem alguma coisa se mexendo ali. / 1: Deve ser o vento. / 0: Se vier, a gente foge junto.
  - ⚠ Fala 0: 'Tem alguma coisa se mexendo ali.' é vaga, mas aceitável para animais noturnos.
  - ⚠ Fala 1: 'Deve ser o vento.' é plausível, mas animais de fazenda podem não racionalizar assim.
- nota 4: 0: Tem alguma coisa se mexendo ali. / 1: Fica perto de mim. / 0: Vou ficar de olho.
  - ⚠ Fala 0 'Tem alguma coisa se mexendo ali.' é genérica e não específica de animais de fazenda, mas aceitável.
  - ⚠ Fala 1 'Fica perto de mim.' soa um pouco humano, mas plausível para animais assustados.
- nota 4: 0: Ouviu aquele barulho lá longe? / 1: Fica perto de mim. / 0: Se vier, a gente foge junto.
  - ⚠ Fala 1 'Fica perto de mim.' é um pouco vaga para a resposta, mas ainda faz sentido como conforto.
  - ⚠ Fala 0 final 'Se vier, a gente foge junto.' é um fecho aceitável, mas poderia ser mais natural como 'Se vier, a gente corre junto.'
- nota 4: 0: Ouviu aquele barulho lá longe? / 1: Deve ser o vento. / 0: Se vier, a gente foge junto.
  - ⚠ A fala 'Se vier, a gente foge junto.' é um pouco vaga, mas aceitável para animais da fazenda.

### curiosidade_portal (4.25)  _dois aldeões; uma situação qualquer do dia a dia_
- nota 4: 0: Dizem que existe um mundo de fogo do outro lado. / 1: Pois é. / 0: Vai saber o que é verdade.
  - ⚠ A fala 'Pois é.' é muito vaga e não desenvolve a conversa, mas é aceitável para aldeões.
  - ⚠ A fala 'Vai saber o que é verdade.' soa um pouco filosófica demais para aldeões, que costumam ser mais simples.
- nota 4: 0: Falaram de uma ilha lá no Fim. / 1: Nem me fale. / 0: Vai saber o que é verdade.
  - ⚠ A fala 'Nem me fale.' é um pouco vaga, mas aceitável como resposta de desinteresse.
  - ⚠ A fala 'Vai saber o que é verdade.' fecha a conversa, mas poderia ser mais natural como 'Vai saber se é verdade.'
- nota 3: 0: Falaram de uma ilha lá no Fim. / 1: Pois é. / 0: Vai saber o que é verdade.
  - ⚠ A fala 'Pois é.' é muito vaga e não desenvolve a conversa, soando como uma resposta sem engajamento.
  - ⚠ O fecho 'Vai saber o que é verdade.' não conclui claramente o assunto, deixando a conversa sem um encerramento natural.
- nota 4: 0: Falaram de uma ilha lá no Fim. / 1: Nem me fale. / 0: Eu fico por aqui mesmo.
  - ⚠ A fala 'Falaram de uma ilha lá no Fim.' é um pouco vaga, mas plausível para aldeões que conhecem o End.
  - ⚠ A resposta 'Nem me fale.' é curta e natural, mas não desenvolve a conversa.

### zumbi_jogador (4.25)  _um zumbi e outra criatura da mesma espécie; o jogador segura um item_
- nota 4: 0: Uma espada de ferro... o humano segura uma espada de ferro. / 1: Cuidado... pode doer. / 0: Fome...
  - ⚠ A fala 'Fome...' não fecha claramente a conversa, soando como um pensamento solto.
- nota 4: 0: Uma espada de ferro... o humano segura uma espada de ferro. / 1: Tira dele... tira... / 0: Fome...
  - ⚠ A fala 'Tira dele... tira...' é um pouco vaga, mas aceitável para zumbis.
  - ⚠ A fala 'Fome...' fecha a conversa de forma abrupta, mas condiz com zumbis.
- nota 4: 0: Uma espada de ferro... o humano segura uma espada de ferro. / 1: Cuidado... pode doer. / 0: Grrr...
  - ⚠ A fala 'Cuidado... pode doer.' é um pouco vaga, mas aceitável para um zumbi.
  - ⚠ O fecho 'Grrr...' é curto e fecha a conversa, mas poderia ser mais reativo à fala anterior.
- nota 4: 0: Uma espada de ferro... o humano segura uma espada de ferro. / 1: Tira dele... tira... / 0: Grrr...
  - ⚠ A fala 'Tira dele... tira...' é um pouco vaga, mas aceitável para zumbis.
  - ⚠ O fecho 'Grrr...' é curto e fecha a conversa, mas poderia ser mais conectado à ação de tirar a espada.
- nota 4: 0: Uma espada de ferro... o humano segura uma espada de ferro. / 1: Não importa. Carne. / 0: Grrr...
  - ⚠ A fala 'Não importa. Carne.' é um pouco vaga, mas aceitável para zumbis.
  - ⚠ O fecho 'Grrr...' é curto e fecha a conversa, mas poderia ser mais conectado.

### chuva (4.38)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; está chovendo_
- nota 4: 0: Chuva boa essa, vai regar tudo. / 1: Pelo menos as plantas agradecem. / 0: Se trovejar eu me escondo.
  - ⚠ A fala 'Se trovejar eu me escondo' é um pouco genérica, mas aceitável para criaturas calmas.
- nota 4: 0: Chuva boa essa, vai regar tudo. / 1: É, o céu está bem carregado. / 0: Bom, fazer o quê.
  - ⚠ A fala 'Bom, fazer o quê.' soa um pouco vaga e não fecha a conversa de forma natural, podendo ser mais específica para a situação de chuva.
- nota 3: 0: Olha só, começou a chover. / 1: Também acho. / 0: Que dure só o suficiente.
  - ⚠ A resposta 'Também acho.' não combina com a fala anterior 'Olha só, começou a chover.', pois não expressa concordância com uma opinião.
  - ⚠ A fala 'Que dure só o suficiente.' é vaga e não fecha claramente a conversa.
- nota 4: 0: Olha só, começou a chover. / 1: Nem me fale. / 0: Verdade. Vamos ver quanto tempo dura.
  - ⚠ A fala 'Nem me fale.' soa um pouco vaga e não acrescenta muito à conversa, mas é aceitável.
  - ⚠ A última fala 'Verdade. Vamos ver quanto tempo dura.' é um pouco redundante, pois já foi dito que começou a chover.

### curiosidade_creeper (4.38)  _dois aldeões; uma situação qualquer do dia a dia_
- nota 3: 0: Dizem que creeper tem medo de gato. / 1: Pois é. / 0: Por isso fico de olho em tudo.
  - ⚠ A fala 'Por isso fico de olho em tudo.' não conecta claramente com a fala anterior sobre creepers e gatos.
  - ⚠ A conversa parece truncada e sem um fecho natural.
- nota 4: 0: Já viu um creeper de perto? Silencioso demais. / 1: Eu ouvi isso também, deve ser verdade. / 0: Por isso fico de olho em tudo.
  - ⚠ A fala 'Eu ouvi isso também, deve ser verdade' não responde diretamente à pergunta 'Já viu um creeper de perto?', mas é aceitável como comentário.
- nota 3: 0: Dizem que creeper tem medo de gato. / 1: Nem me fale. / 0: Por isso fico de olho em tudo.
  - ⚠ A fala 'Nem me fale.' não responde diretamente ao medo de gatos dos creepers, soa vaga.
  - ⚠ A fala 'Por isso fico de olho em tudo.' não conecta claramente com o medo de gatos, parece genérica.

### curiosidade_enderman (4.38)  _dois aldeões; é noite_
- nota 4: 0: Dizem que enderman odeia água. / 1: Nunca falei com um, mas tenho vontade. / 0: Cada um com seu jeito, né?
  - ⚠ A fala 'Nunca falei com um, mas tenho vontade.' soa um pouco estranha para um aldeão, pois eles geralmente não demonstram curiosidade sobre mobs hostis como endermen.
- nota 3: 0: Já viu aqueles olhos roxos no escuro? / 1: Nunca falei com um, mas tenho vontade. / 0: Cada um com seu jeito, né?
  - ⚠ A fala 'Nunca falei com um, mas tenho vontade.' é estranha para um aldeão, pois aldeões não demonstram curiosidade em conversar com criaturas hostis como Endermen.
  - ⚠ A fala 'Cada um com seu jeito, né?' soa muito casual e filosófica para um aldeão, que tende a ser mais direto e focado em trocas.
- nota 4: 0: Tem uns bichos altos que levam blocos embora. / 1: Melhor não encarar, dizem que ficam bravos. / 0: Deve ser uma vida estranha.
  - ⚠ A fala 'Deve ser uma vida estranha.' soa um pouco filosófica demais para um aldeão, que tende a ser mais simples e prático.
- nota 4: 0: Tem uns bichos altos que levam blocos embora. / 1: Melhor não encarar, dizem que ficam bravos. / 0: Cada um com seu jeito, né?
  - ⚠ A fala 'Cada um com seu jeito, né?' soa um pouco vaga e não fecha claramente a conversa sobre os bichos altos.

### enderman_olhar (4.38)  _um enderman e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: A água. Mantenha-se longe da água. / 1: Eu também odeio a chuva. / 0: Melhor sumir antes que ele olhe.
  - ⚠ A fala 'Melhor sumir antes que ele olhe.' é ambígua: 'ele' pode se referir ao jogador, mas não fica claro no contexto.
- nota 3: 0: Mais um humano. Que fique longe dos meus blocos. / 1: Eu também odeio a chuva. / 0: Vamos para outro lugar.
  - ⚠ A fala 'Eu também odeio a chuva' não responde diretamente à fala anterior sobre humanos e blocos, quebrando a coerência da conversa.
  - ⚠ A fala 'Vamos para outro lugar' não fecha claramente a conversa, pois não se conecta com a resposta sobre chuva.
- nota 4: 0: Não olhe nos olhos. Ele nos vê. / 1: Eu também odeio a chuva. / 0: Melhor sumir antes que ele olhe.
  - ⚠ A fala 'Eu também odeio a chuva.' não responde diretamente à fala anterior sobre olhar nos olhos, mas é plausível como associação de perigos para endermen.
  - ⚠ A fala 'Melhor sumir antes que ele olhe.' fecha a conversa, mas a transição da chuva para o olhar é um pouco abrupta.
- nota 4: 0: Não olhe nos olhos. Ele nos vê. / 1: Eu também odeio a chuva. / 0: Vamos para outro lugar.
  - ⚠ A fala 'Eu também odeio a chuva.' não responde diretamente à fala anterior 'Não olhe nos olhos. Ele nos vê.', mas é plausível como uma associação de desconfortos.
  - ⚠ A fala 'Vamos para outro lugar.' fecha a conversa de forma um pouco abrupta, mas aceitável.

### noite (4.38)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; é noite_
- nota 4: 0: Já ouvi uns rosnados lá longe. / 1: Se ouvir um chiado, corre. / 0: Aguenta firme, o dia já vem.
  - ⚠ Fala 1: 'Se ouvir um chiado, corre.' - 'chiado' é vago; poderia ser mais específico (ex.: 'chiado de creeper') para clareza.
  - ⚠ Fala 0 (fecho): 'Aguenta firme, o dia já vem.' - 'Aguenta firme' soa um pouco formal para criaturas calmas; 'Aguenta aí' seria mais natural.
- nota 4: 0: Quem está de pé a essa hora? / 1: Fica por perto, é mais seguro. / 0: Aguenta firme, o dia já vem.
  - ⚠ A fala 'Aguenta firme, o dia já vem.' soa um pouco filosófica demais para criaturas simples como animais de fazenda ou aldeões, mas ainda é aceitável.
- nota 4: 0: Quem está de pé a essa hora? / 1: De noite o mundo fica estranho. / 0: Qualquer barulho eu aviso.
  - ⚠ A fala 'Qualquer barulho eu aviso.' não fecha claramente a conversa, soa como uma continuação.
- nota 4: 0: A lua está bonita, mas não confio nessa escuridão. / 1: Se ouvir um chiado, corre. / 0: Qualquer barulho eu aviso.
  - ⚠ A fala 'Se ouvir um chiado, corre.' é genérica e não especifica a criatura, mas pode ser aceitável para aldeões ou animais.
  - ⚠ A fala 'Qualquer barulho eu aviso.' é um pouco vaga, mas fecha a conversa de forma coerente.
- nota 4: 0: Quem está de pé a essa hora? / 1: Se ouvir um chiado, corre. / 0: Qualquer barulho eu aviso.
  - ⚠ A fala 'Se ouvir um chiado, corre.' é um aviso sobre Creeper, mas não responde diretamente à pergunta 'Quem está de pé a essa hora?'.
  - ⚠ A fala 'Qualquer barulho eu aviso.' não fecha claramente a conversa, pois não responde ao aviso anterior.

### pastor (4.38)  _villager:shepherd e dois aldeões; é de dia_
- nota 4: 0: Lã não falta nesta estação. / 1: Elas são teimosas mesmo. / 0: No fim, elas voltam sempre.
  - ⚠ A fala 'Elas são teimosas mesmo.' é um pouco vaga, mas pode se referir às ovelhas, o que faz sentido para um pastor.
- nota 3: 0: As ovelhas hoje estão agitadas. / 1: Quando precisar de ajuda, chama. / 0: No fim, elas voltam sempre.
  - ⚠ A fala 'Quando precisar de ajuda, chama.' não responde diretamente à observação sobre as ovelhas agitadas.
  - ⚠ O fecho 'No fim, elas voltam sempre.' não conecta claramente com a resposta anterior, deixando a conversa sem continuidade lógica.
- nota 3: 0: As ovelhas hoje estão agitadas. / 1: Quando precisar de ajuda, chama. / 0: Eu gosto delas.
  - ⚠ A fala 'Quando precisar de ajuda, chama.' não responde diretamente à observação sobre as ovelhas agitadas.
  - ⚠ O fecho 'Eu gosto delas.' não conclui a conversa de forma natural, pois retoma o assunto inicial sem conexão com a fala anterior.

### sol (4.38)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; é de dia; o céu está limpo_
- nota 3: 0: Acordei de bom humor com esse sol. / 1: Também acho. / 0: Pena que a noite chega logo.
  - ⚠ A resposta 'Também acho.' não combina com a fala anterior, que expressa um estado de humor, não uma opinião.
  - ⚠ O fecho 'Pena que a noite chega logo.' muda o tom da conversa de forma abrupta, sem transição.
- nota 3: 0: O céu está limpo, nem uma nuvem. / 1: Nem me fale. / 0: Pena que a noite chega logo.
  - ⚠ A fala 'Nem me fale.' soa como reclamação, mas não há contexto negativo anterior; a primeira fala é neutra/positiva.
  - ⚠ A fala 'Pena que a noite chega logo.' introduz um tom melancólico que não combina com a situação de dia claro e céu limpo, que geralmente é agradável.
- nota 4: 0: Que dia bonito hoje. / 1: Aproveita, que o tempo muda rápido. / 0: Hoje está tudo no lugar.
  - ⚠ A fala 'Hoje está tudo no lugar.' soa um pouco vaga e não fecha a conversa de forma natural, podendo ser mais específica ou conectada ao clima.

### vaca (4.38)  _uma vaca e dois animais da fazenda; é de dia_
- nota 4: 0: Muuu, o dia está calmo. / 1: Calma é o que a gente tem de melhor. / 0: Quando escurecer a gente se junta.
  - ⚠ A fala 'Calma é o que a gente tem de melhor.' soa um pouco filosófica para animais de fazenda, mas ainda é aceitável.
  - ⚠ A fala 'Quando escurecer a gente se junta.' é um pouco vaga, mas plausível para animais que se agrupam à noite.
- nota 4: 0: Dá vontade de ficar mastigando devagar o dia todo. / 1: Calma é o que a gente tem de melhor. / 0: Quando escurecer a gente se junta.
  - ⚠ A fala 'Quando escurecer a gente se junta.' não fecha claramente a conversa, pois não responde diretamente à fala anterior sobre calma.
- nota 4: 0: Hoje o pasto está especialmente macio. / 1: Sem pressa, a vida rende mais. / 0: Quando escurecer a gente se junta.
  - ⚠ A fala 'Sem pressa, a vida rende mais.' soa filosófica demais para animais de fazenda, que tendem a falas simples e concretas.
  - ⚠ A fala 'Quando escurecer a gente se junta.' é um pouco vaga; não fica claro o que farão juntos, mas ainda é plausível.
- nota 4: 0: Hoje o pasto está especialmente macio. / 1: Calma é o que a gente tem de melhor. / 0: Quando escurecer a gente se junta.
  - ⚠ A fala 'Calma é o que a gente tem de melhor.' soa um pouco filosófica demais para animais de fazenda, mas ainda é aceitável.
  - ⚠ A fala 'Quando escurecer a gente se junta.' é um pouco vaga, mas fecha a conversa de forma natural.
- nota 4: 0: Muuu, o dia está calmo. / 1: Aqui a gente só precisa de grama e sombra. / 0: Quando escurecer a gente se junta.
  - ⚠ A fala 'Aqui a gente só precisa de grama e sombra.' é genérica demais para 'dois animais da fazenda' sem especificar quais, mas ainda se encaixa no mundo da fazenda.
  - ⚠ A fala 'Quando escurecer a gente se junta.' soa um pouco vaga, mas é plausível para animais de fazenda.

### aranha_teia (4.50)  _uma aranha e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Sss... as paredes são minhas. / 1: Sss... eu sinto as vibrações. / 0: Sss...
  - ⚠ A fala 'Sss... as paredes são minhas.' é um pouco vaga e não se conecta claramente com a resposta 'Sss... eu sinto as vibrações.'
  - ⚠ O fecho 'Sss...' é muito curto e não fecha a conversa de forma satisfatória.
- nota 4: 0: Sss... as paredes são minhas. / 1: Sss... deixa chegar mais perto. / 0: Sss...
  - ⚠ A fala 'Sss... as paredes são minhas.' é um pouco vaga para uma situação qualquer do dia a dia, mas ainda faz sentido para aranhas territoriais.
  - ⚠ A resposta 'Sss... deixa chegar mais perto.' sugere uma emboscada, o que é plausível, mas o fecho 'Sss...' é muito curto e não fecha claramente a conversa.
- nota 4: 0: Sss... as paredes são minhas. / 1: Sss... deixa chegar mais perto. / 0: Sss... paciência.
  - ⚠ Fala 0 'Sss... as paredes são minhas.' é um pouco vaga para uma situação qualquer do dia a dia, mas aceitável para aranhas.
  - ⚠ Fala 1 'Sss... deixa chegar mais perto.' sugere caça, o que pode não bater com 'situação qualquer do dia a dia'.
- nota 4: 0: Sss... as paredes são minhas. / 1: Sss... a noite é nossa. / 0: Sss... paciência.
  - ⚠ A fala 'Sss... as paredes são minhas.' é um pouco vaga para uma situação qualquer do dia a dia, mas ainda faz sentido para aranhas.
  - ⚠ A resposta 'Sss... a noite é nossa.' combina com a fala anterior, mas poderia ser mais específica.

### bicho_sombra (4.50)  _dois animais da fazenda; é de dia; o céu está limpo_
- nota 3: 0: Achei uma sombra boa. / 1: Pois é. / 0: Fica à vontade.
  - ⚠ A fala 'Pois é.' é muito vaga e não combina com a resposta esperada de um animal da fazenda.
  - ⚠ A fala 'Fica à vontade.' soa mais como um convite humano do que algo que um animal diria naturalmente.
- nota 3: 0: Tem sombra de sobra aqui. / 1: Nem me fale. / 0: Fica à vontade.
  - ⚠ Fala 0 'Tem sombra de sobra aqui.' é vaga e não especifica o contexto da sombra, mas ainda plausível para animais de fazenda.
  - ⚠ Fala 1 'Nem me fale.' é uma resposta genérica que não desenvolve a conversa, mas aceitável.

### bruxa_pocao (4.50)  _uma bruxa e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: O caldeirão está borbulhando. / 1: Hehe, a minha é ainda melhor. / 0: Já sei qual receita usar.
  - ⚠ A fala 'Hehe, a minha é ainda melhor.' é um pouco vaga, mas aceitável para bruxas competitivas.
- nota 4: 0: Hehe, tenho uma poção nova para testar. / 1: Hehe, a minha é ainda melhor. / 0: Hehe, a noite vai ser divertida.
  - ⚠ A fala 'Hehe, a minha é ainda melhor' é competitiva, mas bruxas geralmente não competem entre si; seria mais natural uma troca de segredos ou ameaças.
  - ⚠ A fala 'Hehe, a noite vai ser divertida' é vaga e não fecha claramente a conversa sobre poções.
- nota 4: 0: Hehe, tenho uma poção nova para testar. / 1: Mistura com cogumelo, dá um efeito curioso. / 0: Já sei qual receita usar.
  - ⚠ A fala 'Mistura com cogumelo, dá um efeito curioso.' soa um pouco vaga, mas é plausível para bruxas.
- nota 4: 0: Hehe, tenho uma poção nova para testar. / 1: Hehe, a minha é ainda melhor. / 0: Já sei qual receita usar.
  - ⚠ A fala 'Já sei qual receita usar' não fecha claramente a conversa, deixando a interação sem conclusão.

### colheita (4.50)  _dois aldeões; é de dia_
- nota 4: 0: Como anda a sua plantação de trigo? / 1: Troquei um pouco com um viajante outro dia. / 0: A terra daqui é generosa.
  - ⚠ A resposta 'Troquei um pouco com um viajante outro dia' não responde diretamente à pergunta sobre a plantação, mas é plausível como desvio de conversa.
  - ⚠ O fecho 'A terra daqui é generosa' não conecta claramente com a troca mencionada, mas ainda é aceitável como comentário geral.
- nota 3: 0: Colhi abóbora a manhã inteira. / 1: Também acho. / 0: Se sobrar, a gente troca.
  - ⚠ A resposta 'Também acho.' não combina com a fala anterior, que é uma afirmação sobre colher abóbora, não uma opinião.
  - ⚠ O fecho 'Se sobrar, a gente troca.' não fecha a conversa de forma natural, pois não há contexto de troca estabelecido.
- nota 4: 0: Hoje só pensei em batata. / 1: É verdade. / 0: A terra daqui é generosa.
  - ⚠ A resposta 'É verdade.' é muito vaga e não desenvolve a conversa, mas ainda é plausível para aldeões.

### curiosidade_fundo (4.50)  _dois aldeões; uma situação qualquer do dia a dia_
- nota 4: 0: O que será que tem lá embaixo, nas cavernas? / 1: Prefiro o lado de cima. / 0: Que ninguém desça sem tocha.
  - ⚠ A fala 'Prefiro o lado de cima.' é um pouco vaga, mas aceitável para um aldeão.
  - ⚠ A última fala 'Que ninguém desça sem tocha.' soa mais como um aviso do que um fecho natural, mas ainda faz sentido.
- nota 3: 0: Já ouviu os sons que vêm do chão à noite? / 1: Prefiro o lado de cima. / 0: Melhor deixar o fundo em paz.
  - ⚠ A fala 'Prefiro o lado de cima.' não responde diretamente à pergunta sobre sons à noite, soando desconexa.
  - ⚠ O fecho 'Melhor deixar o fundo em paz.' é vago e não conclui claramente a conversa.
- nota 4: 0: O que será que tem lá embaixo, nas cavernas? / 1: Prefiro o lado de cima. / 0: Melhor deixar o fundo em paz.
  - ⚠ A fala 'Prefiro o lado de cima.' é um pouco vaga, mas aceitável para um aldeão.
  - ⚠ A conversa é curta e um pouco genérica, mas faz sentido para aldeões curiosos sobre cavernas.

### esqueleto_creeper (4.50)  _um esqueleto e um creeper; uma situação qualquer do dia a dia_
- nota 4: 0: Você explode e eu atiro. Dá um bom time. / 1: Ssss... só não me acerta. / 0: Então vamos.
  - ⚠ A fala do creeper 'Ssss... só não me acerta.' é um pouco longa para o padrão de creepers, que costumam ser mais curtos e sibilantes.
- nota 4: 0: Você explode e eu atiro. Dá um bom time. / 1: Ssss... eu chego perto, você mira de longe. / 0: Então vamos.
  - ⚠ A fala do creeper 'Ssss... eu chego perto, você mira de longe.' é um pouco longa para o padrão de creepers, que costumam ser mais lacônicos, mas ainda aceitável.
- nota 4: 0: Fica longe da minha flecha, hein? / 1: Ssss... eu chego perto, você mira de longe. / 0: Então vamos.
  - ⚠ A fala 'Então vamos.' é um fecho um pouco vago, mas aceitável.
- nota 4: 0: Fica longe da minha flecha, hein? / 1: Ssss... só não me acerta. / 0: Então vamos.
  - ⚠ A fala 'Então vamos.' é vaga e não fecha claramente a conversa, deixando a interação sem um desfecho natural.

### fofoca_vila (4.50)  _dois aldeões; é de dia_
- nota 4: 0: Alguém viu o que se passa perto do rio? / 1: Aqui na vila as notícias correm rápido. / 0: Vou ficar de olho.
  - ⚠ A resposta 'Aqui na vila as notícias correm rápido.' não responde diretamente à pergunta sobre o rio, deixando a conversa um pouco vaga.
- nota 3: 0: Soube que apareceu gente nova perto do poço. / 1: Também acho. / 0: Vou ficar de olho.
  - ⚠ A resposta 'Também acho' não combina com a fala anterior, que é uma afirmação sobre gente nova, não uma opinião.
  - ⚠ O fecho 'Vou ficar de olho' não fecha a conversa de forma natural, pois não responde à fala anterior.
- nota 4: 0: Ouvi uns barulhos estranhos do outro lado da cerca. / 1: É verdade. / 0: Vou ficar de olho.
  - ⚠ A resposta 'É verdade.' é muito vaga e não acrescenta nada à conversa, soando pouco natural para um diálogo entre aldeões.

### pasto (4.50)  _dois animais da fazenda; é de dia_
- nota 4: 0: Esse capim está uma delícia. / 1: Pois é. / 0: Aqui a gente está seguro.
  - ⚠ A fala 'Aqui a gente está seguro' soa um pouco filosófica para animais de fazenda, mas é aceitável.
- nota 4: 0: Esse capim está uma delícia. / 1: Dá para viver só disso. / 0: A vida é simples, e isso me basta.
  - ⚠ A fala 'A vida é simples, e isso me basta.' soa filosófica demais para um animal de fazenda, que normalmente não reflete sobre a vida.
- nota 4: 0: Passei a manhã pastando, que paz. / 1: Pois é. / 0: A vida é simples, e isso me basta.
  - ⚠ A fala 'A vida é simples, e isso me basta.' soa filosófica demais para um animal de fazenda, que normalmente não reflete sobre a vida dessa forma.
- nota 4: 0: Viu aquela touceira de grama ali? / 1: Dá para viver só disso. / 0: A vida é simples, e isso me basta.
  - ⚠ A fala 'Dá para viver só disso.' é um pouco vaga, mas aceitável para animais de fazenda.
  - ⚠ A fala 'A vida é simples, e isso me basta.' soa levemente filosófica para um animal, mas ainda dentro do tom simples.

### vila_armadura (4.50)  _dois aldeões; o jogador veste armadura_
- nota 4: 0: Olha só, o humano está de uma armadura de ferro. / 1: Um bom armeiro faria igual. / 0: Eu só queria saber onde fez.
  - ⚠ A fala 'Um bom armeiro faria igual.' soa um pouco deslocada, pois não responde diretamente à observação sobre a armadura do humano, mas ainda é plausível para um aldeão.
- nota 4: 0: Reparou na roupa de o humano? É uma armadura de ferro. / 1: Isso é equipamento de quem anda por lugares perigosos. / 0: Eu só queria saber onde fez.
  - ⚠ A fala 'Eu só queria saber onde fez.' soa um pouco deslocada, pois aldeões não costumam se interessar por fabricação de armaduras, mas é aceitável como curiosidade.
- nota 4: 0: O humano veste uma armadura de ferro. Que peça boa. / 1: Dá até medo de chegar perto. / 0: Eu só queria saber onde fez.
  - ⚠ A fala 'Eu só queria saber onde fez.' soa um pouco abrupta e não fecha totalmente a conversa, pois não responde diretamente ao medo expresso pelo outro aldeão.
- nota 4: 0: Olha só, o humano está de uma armadura de ferro. / 1: Deve ter custado caro. / 0: Eu só queria saber onde fez.
  - ⚠ A fala 'Eu só queria saber onde fez.' soa um pouco vaga e não fecha a conversa de forma natural, pois não responde diretamente à fala anterior sobre o custo.

### golem_guarda (4.62)  _dois aldeões e um golem de ferro; uma situação qualquer do dia a dia_
- nota 4: 0: Dá uma sensação boa ter você por perto. / 1: Eu só faço o que precisa ser feito. / 0: Qualquer coisa, grito por você.
  - ⚠ A fala 'Qualquer coisa, grito por você' soa um pouco estranha para um aldeão, pois gritar não é uma ação típica deles; talvez 'chamo' fosse mais natural.
- nota 4: 0: Você nunca dorme, né? / 1: Enquanto eu estiver aqui, ninguém passa. / 0: Por isso a vila ainda existe.
  - ⚠ A fala 'Enquanto eu estiver aqui, ninguém passa.' soa mais como uma ameaça do que uma resposta natural de um golem de ferro, que é protetor mas não costuma falar de forma tão direta.
- nota 4: 0: A vila fica mais tranquila com você aqui. / 1: Dormir não é comigo, eu fico de pé. / 0: Qualquer coisa, grito por você.
  - ⚠ A fala 'Dormir não é comigo, eu fico de pé.' soa como um trocadilho, o que é estranho para um golem de ferro, que normalmente não faz jogos de palavras.
  - ⚠ A fala 'Qualquer coisa, grito por você.' é um pouco vaga; um aldeão provavelmente diria algo mais específico, como 'Se vir perigo, me avise.'

### jogador_chuva (4.62)  _dois aldeões; está chovendo_
- nota 4: 0: O humano está na chuva, nem parece se importar. / 1: Deve estar com frio. / 0: Ao menos a chuva lava a poeira.
  - ⚠ A fala 'Ao menos a chuva lava a poeira' soa um pouco filosófica demais para um aldeão, que tende a ser mais prático e simples.
- nota 4: 0: O humano está na chuva, nem parece se importar. / 1: Alguém devia oferecer um abrigo. / 0: Se vier até aqui, eu deixo ficar.
  - ⚠ A fala 'Alguém devia oferecer um abrigo.' soa um pouco formal para uma conversa casual entre aldeões, mas ainda é aceitável.
- nota 4: 0: O humano está na chuva, nem parece se importar. / 1: Alguém devia oferecer um abrigo. / 0: Ao menos a chuva lava a poeira.
  - ⚠ A fala 'Alguém devia oferecer um abrigo.' soa um pouco vaga e não combina totalmente com a atitude prática dos aldeões, que normalmente fariam uma oferta direta ou comentariam sobre suas casas.

### trovoada (4.62)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; há tempestade_
- nota 4: 0: Que tempestade feia. / 1: Fica longe de árvore alta, dizem que atrai raio. / 0: Vamos aguentar juntos.
  - ⚠ A fala 'Fica longe de árvore alta, dizem que atrai raio.' soa mais como conselho de aldeão do que de animal de fazenda, que normalmente não falaria sobre raios e árvores.
- nota 4: 0: Esse temporal está de dar medo. / 1: Fica longe de árvore alta, dizem que atrai raio. / 0: Chega mais perto, é mais seguro.
  - ⚠ A fala 'Chega mais perto, é mais seguro.' pode ser ambígua: se estão ao ar livre durante tempestade, aproximar-se de outra criatura não necessariamente aumenta segurança, mas é aceitável como conselho
- nota 4: 0: Que tempestade feia. / 1: Raio assim faz a gente querer ficar junto. / 0: Vamos aguentar juntos.
  - ⚠ A fala 'Raio assim faz a gente querer ficar junto' soa um pouco romântica demais para criaturas calmas como aldeões ou animais de fazenda, que normalmente não expressam esse tipo de sentimento.

### bicho_quer_comida (4.75)  _dois animais da fazenda; o jogador segura um item; quem abre GOSTA do que o jogador segura_
- nota 4: 0: Trigo! É o meu favorito. Será que ele divide? / 1: Ele nem olhou para nós, que pena. / 0: Quem sabe ele se compadece.
  - ⚠ A fala 'Ele nem olhou para nós, que pena.' soa mais como um lamento humano do que como algo que um animal de fazenda diria, mas é aceitável.
- nota 4: 0: Olha só, trigo na mão de o humano. Será que é para mim? / 1: Eu também queria. / 0: Quem sabe ele se compadece.
  - ⚠ Fala 1: 'Eu também queria.' é um pouco vaga, mas aceitável para um animal.
  - ⚠ Fala 0 (fecho): 'Quem sabe ele se compadece.' soa um pouco formal para um animal de fazenda, mas ainda passável.

### curiosidade_abelha (4.75)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; é de dia; o céu está limpo_
- nota 4: 0: Você viu as abelhas hoje? Trabalham sem parar. / 1: Pois é. / 0: Elas sabem o que fazem.
  - ⚠ A fala 'Elas sabem o que fazem.' soa um pouco filosófica para criaturas simples como animais de fazenda ou aldeões, mas ainda é aceitável.
- nota 4: 0: Você viu as abelhas hoje? Trabalham sem parar. / 1: Dizem que o mel é uma maravilha. / 0: Elas sabem o que fazem.
  - ⚠ A fala 'Dizem que o mel é uma maravilha.' soa um pouco vaga e não responde diretamente à pergunta sobre as abelhas trabalharem sem parar, mas ainda é plausível.

### entardecer (4.75)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; é fim de tarde_
- nota 4: 0: O sol está se pondo. / 1: Hora de ficar de olho, a noite traz coisa ruim. / 0: Cada dia termina assim.
  - ⚠ A fala 'Cada dia termina assim.' soa um pouco filosófica demais para criaturas simples como aldeões ou animais de fazenda.
- nota 4: 0: A tarde acabou rápido hoje. / 1: Eu gosto dessa cor no céu. / 0: É verdade, vamos com cuidado.
  - ⚠ A fala 'vamos com cuidado' não se conecta claramente com o contexto de fim de tarde ou com a fala anterior sobre a cor do céu.

### esqueleto_mira (4.75)  _um esqueleto e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Ossos quietos, flechas prontas. / 1: Não erra desta vez. / 0: Vamos esperar o momento certo.
  - ⚠ A fala 'Não erra desta vez.' soa um pouco vaga sem contexto, mas é plausível para esqueletos.
- nota 4: 0: Ossos quietos, flechas prontas. / 1: Não erra desta vez. / 0: Já estou com a flecha encaixada.
  - ⚠ A fala 'Não erra desta vez.' soa como uma ordem ou provocação, mas não fica claro se é ameaça ou incentivo, o que pode confundir o tom da conversa.

### golem_quer_flor (4.75)  _um golem de ferro e dois aldeões; o jogador segura um item; quem abre GOSTA do que o jogador segura_
- nota 4: 0: Olha, uma papoula na mão de o humano. Gosto muito. / 1: Ele nem percebeu que você está olhando. / 0: Eu cuido da vila, e ele cuida das flores.
  - ⚠ A fala 'Eu cuido da vila, e ele cuida das flores.' soa um pouco filosófica demais para um golem de ferro, que normalmente tem falas simples e diretas.
- nota 4: 0: Olha, uma papoula na mão de o humano. Gosto muito. / 1: Pede com educação, quem sabe ele dá. / 0: Eu cuido da vila, e ele cuida das flores.
  - ⚠ A fala 'Eu cuido da vila, e ele cuida das flores.' soa um pouco filosófica demais para um golem de ferro, que tende a ser mais direto e protetor.

### warden_vibracao (4.75)  _um Warden e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 3: 0: O silêncio é melhor. / 1: Cada passo ecoa no meu peito. / 0: Escuta.
  - ⚠ Warden não fala em frases curtas e poéticas; sua comunicação é baseada em vibrações e grunhidos, não em diálogos filosóficos.
  - ⚠ A fala 'Cada passo ecoa no meu peito' é muito introspectiva para um mob cego e agressivo.

### zumbi_armadura (4.75)  _um zumbi e outra criatura da mesma espécie; o jogador veste armadura_
- nota 4: 0: Metal... uma armadura de ferro... Grrr. / 1: Eu mordo de qualquer jeito. / 0: Grrr...
  - ⚠ A fala 'Eu mordo de qualquer jeito.' soa um pouco casual demais para um zumbi, que normalmente emite grunhidos e frases curtas.
- nota 4: 0: Metal... uma armadura de ferro... Grrr. / 1: Eu mordo de qualquer jeito. / 0: Hmmm...
  - ⚠ A fala 'Eu mordo de qualquer jeito' é um pouco genérica, mas aceitável para zumbis.
  - ⚠ O fecho 'Hmmm...' é vago e não encerra claramente a conversa.

### amanhecer (4.88)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; está amanhecendo_
- nota 4: 0: O dia está nascendo, olha ali. / 1: Que alívio, nem dormi direito. / 0: Vamos ver o que o dia traz.
  - ⚠ A fala 'Que alívio, nem dormi direito' sugere ansiedade ou medo noturno, o que não é típico de criaturas calmas como aldeões ou animais de fazenda, mas é plausível se considerarmos ameaças noturnas no

### bibliotecario (4.88)  _villager:librarian e dois aldeões; uma situação qualquer do dia a dia_
- nota 4: 0: Cada livro guarda um segredo. / 1: Um dia você me ensina? / 0: Ler é uma forma de viajar.
  - ⚠ A fala 'Cada livro guarda um segredo.' é um pouco filosófica para um aldeão bibliotecário, mas aceitável.
  - ⚠ A resposta 'Um dia você me ensina?' é vaga, pois não especifica o que o outro aldeão quer aprender, mas ainda faz sentido.

### camas (4.88)  _dois aldeões; é noite_
- nota 4: 0: Aquela cama perto da porta é minha, hein. / 1: Eu também, mal me aguento em pé. / 0: Até amanhã.
  - ⚠ A fala 'Eu também, mal me aguento em pé.' não responde diretamente à primeira fala sobre a cama, mas pode ser interpretada como concordância em ir dormir.

### esqueleto_armadura (4.88)  _um esqueleto e outra criatura da mesma espécie; o jogador veste armadura_
- nota 4: 0: Aquele humano está de uma armadura de ferro. / 1: Armadura nenhuma é perfeita. / 0: Paciência é a nossa arma.
  - ⚠ A fala 'Paciência é a nossa arma' soa filosófica demais para um esqueleto, que normalmente é agressivo e direto.

### vila_fome (4.88)  _dois aldeões; o jogador está com fome_
- nota 4: 0: Acho que o humano não come há um tempo. / 1: Tenho um trigo sobrando, se ele quiser trocar. / 0: Comida é o que não falta por aqui.
  - ⚠ A fala 'Comida é o que não falta por aqui.' soa um pouco deslocada, pois o aldeão anterior ofereceu trigo para troca, e essa resposta não fecha claramente a conversa.
