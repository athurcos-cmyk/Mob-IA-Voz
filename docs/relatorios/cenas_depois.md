# Juiz de IA por família de assunto (conversas ambiente)

2026-11-02 12:28; 68 famílias, 505 cenas, juiz `deepseek-v4-pro`, 132 s. Média geral **4.15**; famílias abaixo de 4.0: **17**.

| Família | cenas | nota |
|---|---|---|
| endermite_perola | 8 | 2.75  REVER |
| ravager_investida | 7 | 3.29  REVER |
| peixe_esconde | 6 | 3.33  REVER |
| vingador_machado | 6 | 3.50  REVER |
| allay_musica | 7 | 3.57  REVER |
| guardiao_anciao | 8 | 3.62  REVER |
| magma_pulo | 8 | 3.62  REVER |
| ghast_bolas | 7 | 3.71  REVER |
| ghast_choro | 7 | 3.71  REVER |
| axolote_morto | 8 | 3.75  REVER |
| nether_lava | 8 | 3.75  REVER |
| saqueador_ataque | 8 | 3.75  REVER |
| allay_amizade | 7 | 3.86  REVER |
| aq_noite | 7 | 3.86  REVER |
| cruz_ovelha_lobo | 8 | 3.88  REVER |
| cruz_vaca_lobo | 8 | 3.88  REVER |
| papagaio_imitar | 8 | 3.88  REVER |
| aq_algas | 8 | 4.00 |
| bicho_jogador_ferido | 8 | 4.00 |
| blaze_fortaleza | 8 | 4.00 |
| cruz_raposa_coelho | 7 | 4.00 |
| evocador_vex | 5 | 4.00 |
| jogador_nadando | 7 | 4.00 |
| shulker_casca | 8 | 4.00 |
| vaca_balde | 7 | 4.00 |
| warden_vibracao | 16 | 4.00 |
| wskel_fortaleza | 7 | 4.00 |
| zpiglin_bando | 8 | 4.00 |
| hoglin_floresta | 8 | 4.12 |
| jaguatirica_creeper | 8 | 4.12 |
| silverfish_pedra | 8 | 4.12 |
| vex_voo | 8 | 4.12 |
| afogado_agua | 7 | 4.14 |
| aq_fundo | 7 | 4.14 |
| aranha_zumbi | 7 | 4.14 |
| bicho_lembranca_geral | 7 | 4.14 |
| cave_spider_mina | 7 | 4.14 |
| peixe_cardume | 6 | 4.17 |
| cruz_lobo_gato | 5 | 4.20 |
| aq_humano_nada | 8 | 4.25 |
| coelho_pulo | 8 | 4.25 |
| enderman_olhar | 16 | 4.25 |
| slime_pulo | 8 | 4.25 |
| zumbi_horda | 8 | 4.25 |
| brutamontes_guarda | 7 | 4.29 |
| papagaio_musica | 7 | 4.29 |
| husk_deserto | 6 | 4.33 |
| curiosidade_portal | 8 | 4.38 |
| bicho_comida_geral | 7 | 4.43 |
| lobo_osso | 7 | 4.43 |
| lobo_alcateia | 6 | 4.50 |
| strider_lava | 6 | 4.50 |
| voo_altura | 6 | 4.50 |
| camelo_deserto | 7 | 4.57 |
| galinha_ovo | 7 | 4.57 |
| blaze_agua | 5 | 4.60 |
| mooshroom_ilha | 8 | 4.62 |
| raposa_astucia | 8 | 4.62 |
| aq_perigo | 6 | 4.67 |
| golfinho_peixe | 6 | 4.67 |
| cabra_pulo | 7 | 4.71 |
| raposa_frutinha | 7 | 4.71 |
| coelho_medo | 8 | 4.75 |
| abelha_colmeia | 7 | 4.86 |
| curiosidade_fundo | 7 | 4.86 |
| papagaio_selva | 7 | 4.86 |
| sniffer_cheiro | 7 | 4.86 |
| urso_gelo | 7 | 5.00 |

## Problemas apontados (famílias abaixo do mínimo primeiro)

### endermite_perola (2.75)  _um endermite e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 2: 0: Enderman teleportou, deixou pérola. / 1: A pérola caiu quando ele sumiu. / 0: Pérola no chão, eu saio dela.
  - ⚠ Fala 0: 'Enderman teleportou, deixou pérola.' soa como observação neutra, mas endermites são hostis e não fariam comentários casuais sobre endermen.
  - ⚠ Fala 1: 'A pérola caiu quando ele sumiu.' é redundante e não adiciona informação nova.
- nota 2: 0: Pérola quebrada me chama. / 1: A pérola quebrada faz barulho de rachar. / 0: Vou até a pérola quebrada.
  - ⚠ Fala 0: 'Pérola quebrada me chama' soa filosófica demais para um endermite, que é uma criatura simples.
  - ⚠ Fala 1: 'A pérola quebrada faz barulho de rachar' é uma observação óbvia e não responde diretamente ao chamado.
- nota 3: 0: Pérola no chão, eu saio dela. / 1: A pérola racha e eu apareço. / 0: Vou procurar outra pérola.
  - ⚠ Fala 0: 'Pérola no chão, eu saio dela.' soa estranha, pois endermites não nascem de pérolas no chão, mas sim de pérolas de ender arremessadas.
  - ⚠ Fala 1: 'A pérola racha e eu apareço.' é redundante com a fala anterior e não avança a conversa.
- nota 3: 0: O brilho roxo me guia. / 1: Brilho roxo vem de pérola no chão. / 0: Sigo o brilho até a pérola.
  - ⚠ Fala 0: 'O brilho roxo me guia.' soa vaga e filosófica demais para um endermite, que é uma criatura simples.
  - ⚠ Fala 1: 'Brilho roxo vem de pérola no chão.' é uma constatação óbvia, mas aceitável.
- nota 3: 0: Sigo o cheiro da pérola. / 1: Cheiro de pérola é forte perto do End. / 0: Fico perto das pérolas.
  - ⚠ Fala 0 'Sigo o cheiro da pérola.' soa estranha para um endermite, que não tem olfato apurado nem comportamento de seguir pérolas.
  - ⚠ Fala 1 'Cheiro de pérola é forte perto do End.' atribui olfato ao endermite, o que não combina com a criatura.

### ravager_investida (3.29)  _um devastador e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 3: 0: Quando eu corro, tudo cai. / 1: Eu sempre vou na frente. / 0: A investida continua.
  - ⚠ A fala 'Eu sempre vou na frente.' não responde diretamente à fala anterior 'Quando eu corro, tudo cai.'
  - ⚠ A fala 'A investida continua.' não fecha a conversa de forma natural, soa como uma frase solta.
- nota 4: 0: Nada resiste à minha cabeçada. / 1: Eu derrubo muros. / 0: Nada nos para.
  - ⚠ A fala 'Eu derrubo muros' é uma resposta plausível, mas não se conecta diretamente à fala anterior 'Nada resiste à minha cabeçada', soando mais como uma afirmação paralela do que uma continuação.
  - ⚠ O fecho 'Nada nos para' é genérico e não amarra a conversa de forma específica, embora mantenha o tom.
- nota 3: 0: Os saqueadores confiam em mim. / 1: A aldeia vai cair. / 0: Vamos derrubar.
  - ⚠ A fala 'Os saqueadores confiam em mim' soa estranha para um devastador, pois devastadores são bestas de carga e não costumam expressar confiança em saqueadores.
  - ⚠ A resposta 'A aldeia vai cair' não responde diretamente à fala anterior, que era sobre confiança, e sim muda o foco para a aldeia.
- nota 4: 0: Eu derrubo muros. / 1: Nada resiste à minha cabeçada. / 0: Vamos derrubar.
  - ⚠ A fala 'Vamos derrubar.' é um fecho um pouco vago, mas aceitável.
- nota 3: 0: Minha investida não tem freio. / 1: Minha investida derruba tudo. / 0: Mais força.
  - ⚠ As falas são muito repetitivas e não desenvolvem uma conversa natural.
  - ⚠ A resposta 'Minha investida derruba tudo.' não acrescenta informação nova, apenas ecoa a primeira fala.

### peixe_esconde (3.33)  _um peixe e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Achei um buraco entre as pedras. / 1: Esse buraco tem espaço pra gente. / 0: Vou entrar nele agora.
  - ⚠ A fala 'Vou entrar nele agora.' soa um pouco abrupta como fecho, mas é aceitável para peixes.
- nota 4: 0: Tem um canto escuro atrás do coral. / 1: Esse canto escuro esconde bem. / 0: Vou ficar atrás do coral.
  - ⚠ A fala 'Vou ficar atrás do coral.' não fecha claramente a conversa, soa como uma continuação.
- nota 3: 0: Me escondi no meio do cardume. / 1: O cardume se move junto com você. / 0: Assim ninguém me acha.
  - ⚠ A fala 'O cardume se move junto com você.' soa como uma observação genérica, não como uma resposta natural de um peixe.
  - ⚠ O fecho 'Assim ninguém me acha.' não conecta claramente com a resposta anterior, deixando a conversa sem um encerramento coeso.
- nota 3: 0: Achei uma fenda na pedra grande. / 1: Essa fenda é estreita pra passar. / 0: Vou me espremer na fenda.
  - ⚠ Fala 0: 'Achei uma fenda na pedra grande.' soa natural, mas não especifica o que é a pedra grande no contexto do peixe.
  - ⚠ Fala 1: 'Essa fenda é estreita pra passar.' é uma resposta coerente, mas não acrescenta muito à conversa.
- nota 2: 0: A sombra da rocha esconde bem. / 1: A sombra da rocha é grande. / 0: Vou ficar na sombra da rocha.
  - ⚠ A fala 'A sombra da rocha esconde bem.' é vaga e não combina com o mundo de um peixe, que não se esconde em sombras de rochas.
  - ⚠ A resposta 'A sombra da rocha é grande.' não responde diretamente à fala anterior, apenas repete informação.

### vingador_machado (3.50)  _um vingador e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 3: 0: O aço canta quando acerta o tronco. / 1: Prefiro o som da madeira rachando. / 0: Cada um com seu jeito de cortar.
  - ⚠ Fala 0: 'O aço canta quando acerta o tronco.' soa poética demais para uma conversa casual de vingadores.
  - ⚠ Fala 1: 'Prefiro o som da madeira rachando.' é uma resposta plausível, mas não esclarece o contexto.
- nota 4: 0: Vou abrir clareira na mata fechada. / 1: Leve o machado grande, tem tronco grosso. / 0: Vou pegar o mais afiado da forja.
  - ⚠ Fala 0: 'Vou abrir clareira na mata fechada.' soa um pouco formal para conversa cotidiana, mas aceitável.
  - ⚠ Fala 1: 'Leve o machado grande, tem tronco grosso.' é natural, mas 'tem tronco grosso' poderia ser mais específico.
- nota 1: 0: A lâmina está sedenta por madeira. / 1: Vamos saciá-la naquele carvalho velho. / 0: Depois a gente afia de novo.
  - ⚠ Fala 0: 'A lâmina está sedenta por madeira' não combina com o mundo de um vingador, que é um mob hostil focado em vingança, não em cortar madeira.
  - ⚠ Fala 1: 'Vamos saciá-la naquele carvalho velho' sugere uma atividade pacífica de coleta de madeira, incompatível com a natureza agressiva de um vingador.
- nota 4: 0: Esse machado já derrubou três árvores hoje. / 1: Só três? O meu derrubou cinco. / 0: Então me empresta o seu para eu testar.
  - ⚠ A fala 'Só três? O meu derrubou cinco.' soa competitiva demais para uma conversa casual entre vingadores, mas ainda é plausível.
  - ⚠ A fala 'Então me empresta o seu para eu testar.' é um fecho razoável, mas poderia ser mais natural se houvesse uma reação à resposta anterior.
- nota 4: 0: Meu machado corta mais fundo hoje. / 1: Vou usar o meu para rachar aquela tora grossa. / 0: Então vamos ver quem derruba mais árvores.
  - ⚠ A fala 'Meu machado corta mais fundo hoje' soa um pouco vaga e não se conecta claramente com a situação 'uma situação qualquer do dia a dia'.
  - ⚠ A resposta 'Vou usar o meu para rachar aquela tora grossa' é coerente, mas o fecho 'Então vamos ver quem derruba mais árvores' transforma a conversa em uma competição sem motivo aparente.

### allay_musica (3.57)  _um allay e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 3: 0: O disco girou e eu dancei ao lado. / 1: O disco me fez girar sem parar. / 0: Vou girar até o disco parar.
  - ⚠ As falas são muito repetitivas em torno de 'girar' e 'disco', soando artificiais.
  - ⚠ A segunda fala 'O disco me fez girar sem parar' não responde diretamente à primeira, apenas repete a ideia.
- nota 4: 0: O toque de nota me fez brilhar no escuro. / 1: Eu brilhei junto quando a nota tocou. / 0: Vou brilhar mais ao som da próxima nota.
  - ⚠ Fala 0: 'O toque de nota me fez brilhar no escuro.' soa um pouco formal para uma conversa casual entre allays.
  - ⚠ Fala 1: 'Eu brilhei junto quando a nota tocou.' é redundante com a fala anterior, não acrescenta informação nova.
- nota 4: 0: A música da jukebox me deixou leve. / 1: A jukebox tocou e eu flutuei alto. / 0: Vou flutuar até a música acabar.
  - ⚠ A fala 'A jukebox tocou e eu flutuei alto.' soa um pouco redundante em relação à primeira, mas ainda é coerente.
  - ⚠ A última fala 'Vou flutuar até a música acabar.' é um fecho aceitável, mas poderia ser mais natural se fosse uma reação direta à resposta anterior.
- nota 3: 0: A nota da flauta ecoou na floresta. / 1: A flauta na floresta me fez girar. / 0: Vou girar entre as árvores da floresta.
  - ⚠ Fala 0: 'A nota da flauta ecoou na floresta.' soa mais como narração do que fala natural de um allay.
  - ⚠ Fala 1: 'A flauta na floresta me fez girar.' é estranha, pois allays giram naturalmente, não por causa de flauta.
- nota 3: 0: A flauta soou e eu voei até a fonte. / 1: A flauta me fez dançar perto da fonte. / 0: Agora vou seguir o som da flauta.
  - ⚠ As falas são muito repetitivas, todas mencionam a flauta e a fonte, sem variação.
  - ⚠ A resposta não desenvolve a conversa, apenas repete a ideia da primeira fala.

### guardiao_anciao (3.62)  _um guardião ancião e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 3: 0: O monumento guarda segredos antigos. / 1: Os blocos de esponja escondem o ouro. / 0: Vou proteger cada câmara.
  - ⚠ A fala 'Os blocos de esponja escondem o ouro' não combina com o mundo do guardião ancião, pois esponjas não escondem ouro no jogo.
  - ⚠ A resposta não se conecta diretamente com a fala anterior sobre segredos antigos, parecendo aleatória.
- nota 3: 0: O oceano me obedece há eras. / 1: As marés trazem os afogados até mim. / 0: Vou manter o domínio das águas.
  - ⚠ Fala 0: 'O oceano me obedece há eras.' soa grandiosa demais para uma conversa casual do dia a dia.
  - ⚠ Fala 1: 'As marés trazem os afogados até mim.' não responde diretamente à fala anterior, apenas adiciona outra afirmação.
- nota 4: 0: Os invasores não entendem o perigo. / 1: Eles acordam os guardiões adormecidos. / 0: O monumento vai punir todos.
  - ⚠ Fala 0: 'Os invasores não entendem o perigo.' soa um pouco genérica, mas aceitável para guardiões anciões.
  - ⚠ Fala 1: 'Eles acordam os guardiões adormecidos.' é coerente, mas poderia ser mais específica ao mundo aquático.
- nota 2: 0: Sinto cada corrente que me cerca. / 1: As correntes não me deixam sair. / 0: Vou continuar preso aqui.
  - ⚠ Fala 0: 'Sinto cada corrente que me cerca' soa filosófica demais para um guardião ancião, que normalmente emite sons agressivos e não reflete sobre sua condição.
  - ⚠ Fala 1: 'As correntes não me deixam sair' é redundante e não avança a conversa.
- nota 4: 0: Quem nada perto sente meu cansaço. / 1: A fadiga de mineração os afunda. / 0: Nenhum peixe escapa do meu olho.
  - ⚠ A fala 'A fadiga de mineração os afunda' soa um pouco técnica para um guardião ancião, mas ainda aceitável.
  - ⚠ A última fala 'Nenhum peixe escapa do meu olho' não fecha claramente a conversa sobre cansaço e fadiga.

### magma_pulo (3.62)  _um cubo de magma e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Pulei e a lava espirrou no meu lado. / 1: A lava sempre espirra quando caímos perto dela. / 0: Vou pular de novo pra espirrar mais.
  - ⚠ A fala 'Vou pular de novo pra espirrar mais.' soa um pouco repetitiva e poderia ser mais natural, mas ainda faz sentido para cubos de magma.
- nota 4: 0: Meu pulo rachou o chão de basalto. / 1: Rachou mesmo, dá pra ver a fenda. / 0: Então vou pular até abrir mais fendas.
  - ⚠ A fala 'Meu pulo rachou o chão de basalto' é plausível para um cubo de magma, mas a resposta 'Rachou mesmo, dá pra ver a fenda' soa um pouco redundante e sem reação emocional.
  - ⚠ O fecho 'Então vou pular até abrir mais fendas' é coerente, mas a conversa como um todo carece de um toque mais natural ou expressivo.
- nota 3: 0: Pulei alto e escutei o barulho do impacto. / 1: O impacto faz um estrondo que ecoa na caverna. / 0: Ecoa porque a caverna é grande.
  - ⚠ A fala 'Pulei alto e escutei o barulho do impacto.' soa mais como uma descrição do que uma fala natural.
  - ⚠ A resposta 'O impacto faz um estrondo que ecoa na caverna.' é redundante e não avança a conversa.
- nota 4: 0: Quando pulo, o chão treme embaixo de mim. / 1: Sinto o tremor até aqui, o chão todo balança. / 0: Balança porque meu pulo é forte.
  - ⚠ A fala 'Balança porque meu pulo é forte.' soa um pouco repetitiva e não acrescenta muito à conversa, mas ainda é aceitável.
- nota 3: 0: Meu pulo espalhou cinzas pelo ar. / 1: As cinzas sobem quando você cai com força. / 0: Sobem e depois caem de volta.
  - ⚠ Fala 0: 'Meu pulo espalhou cinzas pelo ar.' - Cubos de magma não produzem cinzas; eles são criaturas de lava, então seria mais natural mencionar fogo ou magma.
  - ⚠ Fala 1: 'As cinzas sobem quando você cai com força.' - A resposta não se conecta claramente com a fala anterior, pois não explica por que as cinzas sobem, apenas repete a ideia.

### ghast_bolas (3.71)  _um ghast e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Vi um humano com uma espada de ferro. / 1: Ele tentou acertar minha bola de fogo. / 0: A bola explodiu perto dele.
  - ⚠ A fala 'Ele tentou acertar minha bola de fogo' soa um pouco estranha, pois ghasts não costumam se referir às suas bolas de fogo como 'minha bola de fogo' de forma possessiva; seria mais natural algo c
- nota 4: 0: Minha bola de fogo caiu no lago de lava. / 1: A lava espirrou para todos os lados. / 0: Quase queimou meus tentáculos.
  - ⚠ A fala 'Quase queimou meus tentáculos' soa um pouco estranha, pois ghasts não têm tentáculos; eles têm tentáculos? Na verdade, ghasts têm tentáculos, então está correto, mas a expressão 'quase queimou
- nota 3: 0: Acertei uma bola de fogo no teto do Nether. / 1: O teto de netherrack desabou um pouco. / 0: Caiu poeira e pedras em mim.
  - ⚠ A fala 'O teto de netherrack desabou um pouco.' soa como uma observação neutra, mas ghasts são criaturas hostis e emotivas; espera-se uma reação mais agressiva ou alarmada.
  - ⚠ A fala 'Caiu poeira e pedras em mim.' é muito passiva para um ghast, que normalmente reagiria com raiva ou dor, não apenas constatação.
- nota 4: 0: Minha bola de fogo acertou o chão de basalto. / 1: O basalto rachou com a explosão. / 0: Quase cai no lago de lava.
  - ⚠ A fala 'Quase cai no lago de lava.' não fecha claramente a conversa, soa mais como um comentário adicional do que um fecho.
- nota 4: 0: Soltei uma bola de fogo e acertei longe. / 1: A bola explodiu na pedra do Nether. / 0: A explosão abriu um buraco no terreno.
  - ⚠ A fala 'A explosão abriu um buraco no terreno' soa um pouco técnica para um ghast, que normalmente tem falas mais simples e emotivas.

### ghast_choro (3.71)  _um ghast e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 3: 0: O jogador construiu uma ponte sobre o lago. / 1: Ela é feita de pedra do Nether. / 0: Vou queimar a ponte dele.
  - ⚠ Fala 0: 'O jogador construiu uma ponte sobre o lago.' - Ghasts vivem no Nether, onde não há lagos de água; seria mais coerente um lago de lava.
  - ⚠ Fala 1: 'Ela é feita de pedra do Nether.' - Ghasts não constroem nem se importam com materiais de construção; essa observação soa mais como de um aldeão ou jogador.
- nota 4: 0: Aquele bloco de pedra negra quebrou com meu fogo. / 1: Sim, caiu um pedaço de basalto. / 0: Vou atirar de novo naquela parte.
  - ⚠ A fala 'Aquele bloco de pedra negra quebrou com meu fogo.' soa um pouco formal para um ghast, que normalmente emite sons agudos e curtos.
  - ⚠ A resposta 'Sim, caiu um pedaço de basalto.' é plausível, mas poderia ser mais natural se refletisse a natureza flutuante e distante dos ghasts.
- nota 4: 0: Vi um jogador perto do lago de lava. / 1: Ele tentou se esconder atrás das pedras. / 0: Vou soltar uma bola de fogo nele.
  - ⚠ A fala 'Vou soltar uma bola de fogo nele.' soa um pouco abrupta como fecho, mas é aceitável para um ghast.
- nota 4: 0: Meu grito assustou aquele esqueleto. / 1: Ele correu para trás da fortaleza. / 0: Vou perseguir ele com fogo.
  - ⚠ A fala 'Vou perseguir ele com fogo' soa um pouco estranha, pois ghasts atacam com bolas de fogo, mas 'com fogo' é aceitável.
  - ⚠ A expressão 'perseguir ele' é coloquial, mas poderia ser 'persegui-lo' para soar mais natural em português.
- nota 4: 0: Minha bola de fogo atingiu o teto. / 1: Caiu um pedaço de netherrack. / 0: Vou atirar de novo no teto.
  - ⚠ A fala 'Vou atirar de novo no teto' soa repetitiva e pouco natural como fecho, pois não acrescenta reação ou consequência à queda do netherrack.

### axolote_morto (3.75)  _um axolote e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 3: 0: Teve um que boiou e nem se mexeu. / 1: Boiar assim espanta os curiosos, ninguém chega perto. / 0: Se eu boiar, os outros fogem de mim.
  - ⚠ A fala 'Boiar assim espanta os curiosos, ninguém chega perto.' soa estranha para axolotes, que não têm comportamento de espantar curiosos.
  - ⚠ A fala 'Se eu boiar, os outros fogem de mim.' não se conecta claramente com a fala anterior, deixando a conversa sem fechamento natural.
- nota 4: 0: O peixe ficou imóvel quando a água mexeu. / 1: Peixe parado parece sem vida, ninguém ataca. / 0: Fico imóvel assim quando tem perigo.
  - ⚠ A fala 'Peixe parado parece sem vida, ninguém ataca.' soa um pouco estranha para axolotes, que são predadores aquáticos e não costumam falar sobre peixes como presas de forma tão genérica.
  - ⚠ A última fala 'Fico imóvel assim quando tem perigo.' é um fecho aceitável, mas poderia ser mais natural se conectasse melhor com a resposta anterior.
- nota 3: 0: Um peixe boiou de noite, na lua cheia. / 1: De noite, boiar assim chama predador noturno. / 0: A lua cheia ilumina o peixe boiando.
  - ⚠ Fala 0: 'Um peixe boiou de noite, na lua cheia.' soa como uma observação aleatória, sem conexão clara com a situação ou com a conversa.
  - ⚠ Fala 1: 'De noite, boiar assim chama predador noturno.' é uma resposta lógica, mas a transição para a fala 0 final não fecha a conversa de forma natural.
- nota 4: 0: O peixe se fingiu de morto na correnteza. / 1: É defesa quando a água agita, ele esperou passar. / 0: Depois da correnteza, ele nadou embora.
  - ⚠ A fala 'O peixe se fingiu de morto na correnteza' é um pouco longa para uma conversa casual, mas aceitável.
- nota 4: 0: Um peixe fingiu de morto perto das algas. / 1: Fingir de morto engana quem caça, ele escapou. / 0: O caçador passou reto e o peixe fugiu.
  - ⚠ A fala 'Um peixe fingiu de morto perto das algas.' soa como uma observação factual, mas axolotes não costumam narrar eventos de forma tão direta; poderia ser mais natural se expressasse surpresa ou re
  - ⚠ A resposta 'Fingir de morto engana quem caça, ele escapou.' é um pouco didática para uma conversa casual entre axolotes; parece mais uma explicação do que uma reação espontânea.

### nether_lava (3.75)  _uma criatura do Nether e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 3: 0: Vi uma bolha de fogo estourar na margem. / 1: A lava cospe fogo quando quer queimar. / 0: Não pisa perto do lago.
  - ⚠ A fala 'A lava cospe fogo quando quer queimar.' soa filosófica demais para criaturas do Nether, que normalmente têm falas mais diretas e instintivas.
  - ⚠ A resposta 'A lava cospe fogo quando quer queimar.' não responde diretamente à observação sobre a bolha de fogo, quebrando a naturalidade da conversa.
- nota 3: 0: A pedra perto da lava rachou. / 1: A lava vai engolir a pedra rachada. / 0: Cuidado com a lava.
  - ⚠ Fala 0: 'A pedra perto da lava rachou.' soa como uma observação genérica, sem personalidade da criatura do Nether.
  - ⚠ Fala 1: 'A lava vai engolir a pedra rachada.' é redundante e não avança a conversa.
- nota 4: 0: O calor está mais forte perto do lago. / 1: A lava está mais quente que o normal. / 0: Melhor ficar longe da margem.
  - ⚠ Fala 0: 'O calor está mais forte perto do lago.' - 'lago' no Nether geralmente é de lava, mas a fala não especifica; pode causar ambiguidade.
  - ⚠ Fala 1: 'A lava está mais quente que o normal.' - 'mais quente que o normal' é vago, mas aceitável para criaturas do Nether.
- nota 4: 0: A lava engoliu um pedaço da margem. / 1: A margem está cada vez mais fina. / 0: Não pisa perto do lago.
  - ⚠ Falta contexto sobre a espécie das criaturas, mas as falas são plausíveis para mobs do Nether.
  - ⚠ A segunda fala é um pouco redundante com a primeira, mas aceitável.
- nota 4: 0: A lava está mais alta que ontem. / 1: Ela vai cobrir a pedra negra toda. / 0: Vamos para o outro lado da caverna.
  - ⚠ Falta especificidade da criatura do Nether; a conversa poderia ser de qualquer mob, sem elementos únicos do Nether além da lava e pedra negra.

### saqueador_ataque (3.75)  _um saqueador e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Os sinos vão tocar em vão. / 1: O capitão manda atacar. / 0: Sem piedade com os aldeões.
  - ⚠ A fala 'Os sinos vão tocar em vão' é um pouco poética para um saqueador, mas ainda aceitável.
  - ⚠ A resposta 'O capitão manda atacar' não responde diretamente à primeira fala, mas mantém o contexto de ataque.
- nota 4: 0: A aldeia nem sabe o que vai acontecer. / 1: Vamos derrubar tudo. / 0: Vamos atacar.
  - ⚠ A fala 'Vamos derrubar tudo.' é um pouco genérica e não responde diretamente à fala anterior, mas ainda é plausível.
  - ⚠ A fala 'Vamos atacar.' é redundante com 'Vamos derrubar tudo.' e não fecha a conversa de forma natural.
- nota 4: 0: O capitão ergueu a bandeira. / 1: A besta vai disparar primeiro. / 0: Sem piedade com os aldeões.
  - ⚠ A fala 'A besta vai disparar primeiro.' soa um pouco desconexa da anterior, pois não fica claro se é uma resposta direta ou uma continuação do pensamento.
  - ⚠ A fala 'Sem piedade com os aldeões.' é um fecho aceitável, mas poderia ser mais natural se conectasse melhor com a fala anterior.
- nota 4: 0: Os machados estão afiados. / 1: Eu avanço pela esquerda. / 0: Vamos atacar.
  - ⚠ A fala 'Eu avanço pela esquerda.' parece uma resposta a um plano de ataque, mas a fala inicial 'Os machados estão afiados.' não estabelece claramente um contexto de ataque iminente, tornando a respost
  - ⚠ A fala final 'Vamos atacar.' fecha a conversa, mas não responde diretamente à fala anterior, deixando a sequência um pouco abrupta.
- nota 4: 0: A muralha da aldeia é fraca. / 1: Vamos derrubar tudo. / 0: Em frente, sem recuar.
  - ⚠ A fala 'A muralha da aldeia é fraca.' soa um pouco genérica, mas aceitável para saqueadores.
  - ⚠ A resposta 'Vamos derrubar tudo.' é coerente, mas poderia ser mais específica.

### allay_amizade (3.86)  _um allay e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Eu consegui um item novo do humano. / 1: Que item novo você conseguiu? / 0: Um cristal de ametista, vou cantar para ele.
  - ⚠ A fala 'vou cantar para ele' é um pouco estranha, pois allays não cantam para itens, apenas coletam e entregam.
- nota 3: 0: Um humano me entregou um item, eu guardei. / 1: O que era o item que ele entregou? / 0: Era uma flor, vou guardar meu item.
  - ⚠ A fala 'Era uma flor, vou guardar meu item.' não fecha a conversa de forma natural, pois repete a ação de guardar sem responder diretamente à pergunta sobre o que era o item.
  - ⚠ A resposta 'O que era o item que ele entregou?' soa redundante, já que a primeira fala já menciona que um item foi entregue e guardado.
- nota 4: 0: Eu tenho um humano que me dá itens. / 1: O que ele te deu recentemente? / 0: Uma pena, vou procurar mais itens.
  - ⚠ A fala 'Eu tenho um humano que me dá itens' soa possessiva demais para um allay, que é uma criatura livre e não tem dono.
  - ⚠ A resposta 'Uma pena, vou procurar mais itens' não fecha a conversa de forma natural, pois não há uma despedida ou conclusão.
- nota 4: 0: Eu ganhei um item do humano e não soltei mais. / 1: Qual item você está carregando agora? / 0: Um graveto, vou procurar mais itens.
  - ⚠ A fala 'Eu ganhei um item do humano e não soltei mais.' soa um pouco estranha, pois allays normalmente coletam itens e os entregam, não 'ganham' e retêm; mas é aceitável no contexto do jogo.
- nota 4: 0: O humano me deu um item e eu cantei. / 1: Você cantou por causa do item? / 0: Sim, cantei de alegria, vou seguir o humano.
  - ⚠ A fala 'O humano me deu um item e eu cantei.' soa um pouco artificial, pois allays não falam de forma tão direta sobre suas ações.
  - ⚠ A resposta 'Você cantou por causa do item?' é redundante, já que a primeira fala já estabeleceu a relação entre o item e o canto.

### aq_noite (3.86)  _um bicho aquático e outra criatura da mesma espécie; é noite; é noite de lua cheia_
- nota 4: 0: A lua aparece toda tremida na superfície. / 1: A luz dela não chega até o fundo. / 0: Prefiro ficar aqui embaixo mesmo.
  - ⚠ A fala 'A lua aparece toda tremida na superfície' soa um pouco poética demais para um bicho aquático, mas ainda é aceitável.
  - ⚠ A resposta 'A luz dela não chega até o fundo' é coerente, mas poderia ser mais natural se fosse mais curta, como 'Aqui embaixo não dá pra ver'.
- nota 3: 0: A água fria da noite me deixa mais lento. / 1: Por isso os peixes pequenos se juntam em cardumes. / 0: Vou procurar um cardume para me aquecer.
  - ⚠ A fala 'Vou procurar um cardume para me aquecer' não faz sentido, pois cardumes não aquecem; a motivação de se juntar a um cardume é proteção, não calor.
  - ⚠ A resposta 'Por isso os peixes pequenos se juntam em cardumes' não conecta claramente com a lentidão causada pela água fria; a relação entre lentidão e cardume é forçada.
- nota 3: 0: Tem uma tartaruga subindo para respirar. / 1: Ela volta logo para o fundo. / 0: Vou atrás dela, pode me levar a um lugar seguro.
  - ⚠ A fala 'Tem uma tartaruga subindo para respirar.' soa como uma observação neutra, mas não fica claro por que isso seria relevante para as criaturas da mesma espécie.
  - ⚠ A resposta 'Ela volta logo para o fundo.' não responde diretamente à primeira fala, apenas adiciona informação.
- nota 4: 0: A noite deixa a água bem escura. / 1: Mal consigo ver as algas perto de mim. / 0: Vou usar o cheiro para achar comida.
  - ⚠ A fala 'Vou usar o cheiro para achar comida' pode soar estranha para criaturas aquáticas, pois o olfato não é o sentido mais comum em ambientes subaquáticos; seria mais natural usar tato ou detecção d
- nota 4: 0: As sombras no fundo ficam maiores de noite. / 1: Elas escondem os predadores que caçam agora. / 0: Vou ficar perto das plantas para me esconder.
  - ⚠ A fala 'Vou ficar perto das plantas para me esconder.' é um pouco genérica; poderia ser mais específica do ambiente aquático (ex: 'Vou ficar entre as algas').

### cruz_ovelha_lobo (3.88)  _uma ovelha e um lobo; uma situação qualquer do dia a dia_
- nota 4: 0: Lobo, estou de olho em você. / 1: Pode vigiar, não vou fazer nada. / 0: Assim espero, senão chamo o rebanho.
  - ⚠ A fala da ovelha 'Lobo, estou de olho em você.' é um pouco genérica para 'uma situação qualquer do dia a dia', mas ainda plausível.
  - ⚠ A resposta do lobo 'Pode vigiar, não vou fazer nada.' soa levemente defensiva demais para um lobo, que normalmente não se justificaria, mas é aceitável.
- nota 3: 0: A neve cobre a grama, não acho comida. / 1: Vou descer a montanha para caçar. / 0: Vou tentar achar um pouco de feno.
  - ⚠ A fala do lobo 'Vou descer a montanha para caçar' não responde diretamente à ovelha, que fala sobre falta de comida.
  - ⚠ A fala final da ovelha 'Vou tentar achar um pouco de feno' não fecha a conversa de forma natural, pois o lobo já indicou que vai caçar.
- nota 4: 0: A chuva vai atrapalhar meu pasto. / 1: Vou me abrigar na caverna. / 0: Boa ideia, até a chuva passar.
  - ⚠ A fala do lobo 'Vou me abrigar na caverna' não combina com a situação de chuva, pois lobos não se abrigam em cavernas por causa de chuva; seria mais natural para uma ovelha.
  - ⚠ A resposta da ovelha 'Boa ideia, até a chuva passar' não reflete a relação natural de predador e presa entre lobo e ovelha.
- nota 4: 0: Lobo, não se aproxima, hein. / 1: Só estou caçando coelhos por aqui. / 0: Então fica longe do meu pasto.
  - ⚠ A fala da ovelha 'Lobo, não se aproxima, hein.' soa um pouco formal para uma ovelha, mas é aceitável.
  - ⚠ A resposta do lobo 'Só estou caçando coelhos por aqui.' é plausível, mas um lobo normalmente não se justificaria para uma ovelha; ele poderia ser mais ameaçador ou indiferente.
- nota 4: 0: Eu só quero pastar em paz. / 1: Vou para o outro lado do rio. / 0: Melhor assim, cada um no seu canto.
  - ⚠ A fala do lobo 'Vou para o outro lado do rio' não combina com a situação de 'uma situação qualquer do dia a dia', pois lobos não costumam atravessar rios sem motivo aparente.
  - ⚠ A resposta da ovelha 'Melhor assim, cada um no seu canto' soa um pouco forçada, mas é aceitável.

### cruz_vaca_lobo (3.88)  _uma vaca e um lobo; uma situação qualquer do dia a dia_
- nota 4: 0: Lobo, por que você está perto do pasto? / 1: Estou caçando coelhos, não vacas. / 0: Então continue longe do rebanho.
  - ⚠ A fala da vaca 'Lobo, por que você está perto do pasto?' soa um pouco formal para uma vaca, mas é aceitável.
  - ⚠ A resposta do lobo 'Estou caçando coelhos, não vacas.' é direta e combina com o lobo, mas poderia ser mais natural se fosse mais curta, como 'Caçando coelhos.'
- nota 4: 0: Lobo, afaste-se do pasto agora. / 1: Não estou ameaçando ninguém, só procurando ossos. / 0: Procure ossos longe daqui.
  - ⚠ A fala da vaca 'Lobo, afaste-se do pasto agora.' soa um pouco autoritária para uma vaca, que normalmente seria mais passiva ou assustada.
- nota 4: 0: Seu cheiro está assustando as vacas, lobo. / 1: Não posso controlar meu cheiro, mas não vou caçar aqui. / 0: Então saia do nosso território.
  - ⚠ A fala 'Seu cheiro está assustando as vacas, lobo.' soa um pouco formal para uma vaca, mas é aceitável.
  - ⚠ A resposta do lobo 'Não posso controlar meu cheiro, mas não vou caçar aqui.' é coerente, mas poderia ser mais curta.
- nota 4: 0: O que você está fazendo aqui, lobo? / 1: Vim beber água do rio, nada mais. / 0: Beba e vá embora logo.
  - ⚠ A fala da vaca 'Beba e vá embora logo' soa um pouco ríspida, mas é plausível para uma vaca desconfiada de um lobo.
- nota 4: 0: Seus olhos estão no bezerro, lobo. / 1: Só estou observando, não vou atacar. / 0: Melhor você ir para outro lugar.
  - ⚠ A fala da vaca 'Seus olhos estão no bezerro, lobo.' soa um pouco formal para uma vaca, mas é aceitável.
  - ⚠ A resposta do lobo 'Só estou observando, não vou atacar.' é um pouco defensiva demais para um lobo, que normalmente não se justificaria.

### papagaio_imitar (3.88)  _um papagaio e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Imitei o zumbi e assustei um morcego. / 1: O morcego voou para longe rapidinho. / 0: Vou imitar de novo para ver se volta.
  - ⚠ A fala 'Vou imitar de novo para ver se volta.' soa um pouco repetitiva, mas é aceitável para um papagaio.
- nota 4: 0: Ouvi um afogado e imitei o gorgolejo. / 1: Aquele som parece bolhas na água. / 0: Vou imitar perto do rio para ver se aparece.
  - ⚠ A fala 'Vou imitar perto do rio para ver se aparece' é um pouco longa para uma conversa curta, mas ainda aceitável.
- nota 4: 0: Hoje imitei um esqueleto. Ficou perfeito. / 1: O barulho dos ossos batendo é difícil. / 0: Vou repetir para você comparar.
  - ⚠ A fala 'O barulho dos ossos batendo é difícil.' soa um pouco estranha para um papagaio, pois imitar sons é natural para a espécie, mas pode ser aceitável como um desafio específico.
  - ⚠ A fala 'Vou repetir para você comparar.' é um fecho razoável, mas poderia ser mais natural se incluísse uma reação ou expectativa.
- nota 1: 0: O som do Enderman é difícil, mas consegui. / 1: Aquele som distorcido é complicado mesmo. / 0: Vou treinar até ficar igual ao original.
  - ⚠ Papagaios não falam sobre imitar sons de Enderman de forma tão articulada; soa como conversa humana.
  - ⚠ A fala 'O som do Enderman é difícil, mas consegui.' é longa e complexa para um papagaio.
- nota 4: 0: Ouvi um creeper chiando e aprendi o som. / 1: Faz de novo, quero ouvir o chiado. / 0: Vou fazer e você tenta acompanhar.
  - ⚠ A fala 'Ouvi um creeper chiando e aprendi o som.' soa um pouco artificial para um papagaio, que normalmente imita sons de forma mais direta, mas é aceitável.
  - ⚠ A resposta 'Faz de novo, quero ouvir o chiado.' é natural, mas poderia ser mais curta, como 'Faz de novo!'.

### aq_algas (4.00)  _um bicho aquático e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Prefiro o recife, é mais colorido que as algas. / 1: Só não gosto quando enroscam. / 0: Vou me esconder ali um pouco.
  - ⚠ A fala 'Só não gosto quando enroscam.' é um pouco vaga, mas aceitável para criaturas aquáticas.
- nota 4: 0: Tem uma tartaruga comendo algas ali. / 1: Ela arranca pedaços grandes com a boca. / 0: Vou esperar ela ir embora para comer também.
  - ⚠ A fala 'Vou esperar ela ir embora para comer também.' soa um pouco estranha, pois bichos aquáticos geralmente não esperam outros irem embora para comer; competição por comida é comum, mas a fala é pas
- nota 4: 0: Vi um ouriço escondido nas algas. / 1: Ele espeta se chegar perto demais. / 0: Vou nadar longe dele então.
  - ⚠ A fala 'Vi um ouriço escondido nas algas.' pode não combinar com o mundo de um bicho aquático, pois ouriços não são tipicamente aquáticos; talvez se refira a um ouriço-do-mar, mas a fala não especific
- nota 4: 0: As algas estão cheias de peixes pequenos. / 1: Eles vêm comer os restos que ficam presos. / 0: Vou procurar um cardume para seguir.
  - ⚠ A fala 'Eles vêm comer os restos que ficam presos.' é um pouco vaga, mas aceitável.
  - ⚠ A fala 'Vou procurar um cardume para seguir.' não fecha claramente a conversa, mas é plausível.
- nota 4: 0: Me escondo nas algas quando algo se aproxima. / 1: Eu gosto de passar por entre elas. / 0: Cuidado para não se enroscar.
  - ⚠ A fala 'Eu gosto de passar por entre elas.' não responde diretamente à fala anterior sobre se esconder, mas ainda é plausível como um comentário relacionado às algas.
  - ⚠ A fala 'Cuidado para não se enroscar.' fecha a conversa de forma um pouco abrupta, mas é aceitável.

### bicho_jogador_ferido (4.00)  _dois animais da fazenda; o jogador está ferido_
- nota 4: 0: O humano caiu no chão. / 1: Ele está sangrando na perna. / 0: Vou ficar por perto para vigiar.
  - ⚠ Fala 0 'Vou ficar por perto para vigiar.' soa um pouco protetora demais para um animal de fazenda, mas é aceitável.
- nota 4: 0: O humano está machucado. / 1: Dá pena ver ele mancar. / 0: Fico perto caso precise de ajuda.
  - ⚠ A fala 'Fico perto caso precise de ajuda.' soa um pouco altruísta demais para animais de fazenda, que normalmente não oferecem ajuda ativa.
- nota 4: 0: O humano está respirando com dificuldade. / 1: Ele está ferido no peito. / 0: Não vou fazer barulho.
  - ⚠ A fala 'Não vou fazer barulho' não fecha claramente a conversa, pois não responde diretamente à fala anterior sobre o ferimento.
- nota 4: 0: O humano gemeu de dor. / 1: A ferida deve estar doendo. / 0: Melhor não chegar perto agora.
  - ⚠ A fala 'O humano gemeu de dor.' é muito descritiva e soa como narração, não como fala natural de um animal.
  - ⚠ A resposta 'A ferida deve estar doendo.' é um pouco óbvia e não acrescenta muito à conversa.
- nota 4: 0: O humano está mancando forte. / 1: A pata dele está machucada. / 0: Espero que sare logo.
  - ⚠ A fala 'A pata dele está machucada.' usa 'pata' para humano, o que é estranho, pois 'pata' é mais comum para animais; seria mais natural 'perna' ou 'pé'.

### blaze_fortaleza (4.00)  _um blaze e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: As chamas do gerador mudaram de cor. / 1: Ficaram azuis por um momento. / 0: Sinal de que o núcleo está instável.
  - ⚠ Fala 0: 'As chamas do gerador mudaram de cor.' - 'gerador' não é um termo comum no mundo de um blaze; eles vivem em fortalezas do Nether, não em geradores.
  - ⚠ Fala 0: 'Sinal de que o núcleo está instável.' - 'núcleo' soa técnico demais para um blaze, que é uma criatura elemental de fogo.
- nota 3: 0: O som de passos ecoou na entrada. / 1: Era um esqueleto, não um humano. / 0: Esqueleto queima rápido.
  - ⚠ A fala 'O som de passos ecoou na entrada.' soa mais como narração do que como fala natural de um blaze.
  - ⚠ A resposta 'Era um esqueleto, não um humano.' não combina diretamente com a fala anterior, que não menciona humano.
- nota 4: 0: O gerador de fogo da fortaleza está mais forte. / 1: O calor aumentou depois que o núcleo foi exposto. / 0: Vou ficar longe do núcleo por enquanto.
  - ⚠ A fala 'O gerador de fogo da fortaleza está mais forte.' soa um pouco técnica para um blaze, mas ainda plausível.
  - ⚠ A resposta 'O calor aumentou depois que o núcleo foi exposto.' é coerente, mas 'núcleo' pode ser um termo muito específico para o contexto.
- nota 4: 0: A lava do fosso subiu até a ponte. / 1: As pedras da ponte estão rachando com o calor. / 0: Vou vigiar a ponte de longe.
  - ⚠ A fala 'Vou vigiar a ponte de longe' não fecha claramente a conversa, pois não responde diretamente à preocupação sobre as pedras rachando.
- nota 4: 0: Mais um humano tentou invadir a fortaleza. / 1: Ele caiu no fosso de lava. / 0: A lava resolveu o problema.
  - ⚠ A fala 'A lava resolveu o problema.' soa um pouco redundante após a resposta anterior, mas ainda é aceitável como fecho.

### cruz_raposa_coelho (4.00)  _uma raposa e um coelho; uma situação qualquer do dia a dia_
- nota 4: 0: Eu só vim passear. / 1: Vou me esconder na toca mesmo assim. / 0: Vou indo por aí, então.
  - ⚠ A fala do coelho 'Vou me esconder na toca mesmo assim' soa um pouco abrupta, pois não há indicação clara de ameaça na fala da raposa, mas é plausível pela relação natural predador-presa.
- nota 4: 0: Coelho, não precisa fugir, hoje estou sem fome. / 1: Eu não confio em você. / 0: Entendo sua desconfiança, mas falo sério.
  - ⚠ A fala 'Entendo sua desconfiança, mas falo sério.' soa um pouco formal para uma conversa casual entre animais, mas ainda é aceitável.
- nota 4: 0: Suas orelhas aparecem por trás do arbusto. / 1: Droga, preciso de um esconderijo melhor. / 0: Tente atrás das pedras grandes.
  - ⚠ A fala 'Suas orelhas aparecem por trás do arbusto.' soa mais como uma observação do que uma fala natural de diálogo, mas é aceitável como provocação da raposa.
- nota 4: 0: O cheiro de cenoura está forte por aqui. / 1: Deve ser da minha toca, tenho muitas guardadas. / 0: Cuidado para não atrair outros predadores.
  - ⚠ A fala da raposa 'Cuidado para não atrair outros predadores' soa um pouco estranha, pois a raposa é um predador do coelho, mas pode ser interpretada como um aviso irônico ou ameaçador, o que é aceitáv
- nota 4: 0: Que cenoura é essa aí, coelho? / 1: Cenoura é minha, fica longe. / 0: Não quero sua cenoura, só estou curioso.
  - ⚠ A fala 'Cenoura é minha, fica longe.' soa um pouco abrupta e poderia ser mais natural, como 'É minha, fica longe.'

### evocador_vex (4.00)  _um evocador e outra criatura da mesma espécie; está chovendo_
- nota 4: 0: Os morcegos invadiram o sótão. / 1: Fizeram ninho entre as vigas. / 0: Vou espantá-los com fumaça.
  - ⚠ A fala 'Vou espantá-los com fumaça' soa um pouco prática demais para um evocador, que normalmente usaria magia ou invocaria vexes para lidar com morcegos.
- nota 3: 0: O caldeirão de água benta secou. / 1: A umidade do pântano evaporou. / 0: Vou buscar mais no porão.
  - ⚠ Evocadores não têm porões nem caldeirões de água benta; isso é mais típico de clérigos ou bruxas.
  - ⚠ A resposta 'A umidade do pântano evaporou' não responde diretamente à fala anterior sobre o caldeirão.
- nota 4: 0: O totem da imortalidade está rachado. / 1: A rachadura atravessa o olho esquerdo. / 0: Vou escondê-lo no cofre da torre.
  - ⚠ A fala 'Vou escondê-lo no cofre da torre.' sugere uma construção específica que não é típica de evocadores, que vivem em mansões e não em torres.
- nota 4: 0: O machado encravou na porta da mansão. / 1: A madeira rangeu, mas a porta aguentou. / 0: Vou reforçar as dobradiças.
  - ⚠ A fala 'O machado encravou na porta da mansão.' soa mais como uma narração do que uma fala natural entre criaturas.
  - ⚠ A resposta 'A madeira rangeu, mas a porta aguentou.' também é descritiva, não parece uma reação conversacional típica.

### jogador_nadando (4.00)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; o jogador está nadando na água_
- nota 4: 0: O humano está boiando de costas. / 1: Boiar cansa menos, mas afunda se dormir. / 0: Alguém tem que vigiar.
  - ⚠ A fala 'Boiar cansa menos, mas afunda se dormir.' soa um pouco filosófica demais para criaturas simples como animais de fazenda ou aldeões, que normalmente não fariam tal observação.
- nota 4: 0: A noite chegou e o humano ainda nada. / 1: No escuro não vê os troncos na água. / 0: Vai se machucar.
  - ⚠ A fala 'No escuro não vê os troncos na água' é um pouco específica demais para criaturas calmas como aldeões ou animais de fazenda, que normalmente não comentariam sobre troncos na água.
- nota 3: 0: O humano está nadando no rio! / 1: Nadar no rio é perigoso, tem correnteza. / 0: Que saia logo da água.
  - ⚠ A fala 'Que saia logo da água.' soa incompleta e pouco natural; falta um sujeito ou uma construção mais coloquial.
  - ⚠ A resposta 'Nadar no rio é perigoso, tem correnteza.' é plausível, mas a preocupação com correnteza pode não se aplicar a todos os rios do jogo, tornando-a genérica.
- nota 4: 0: Olha, o humano entrou na água. / 1: Será que vai mergulhar fundo? / 0: Melhor não, pode bater nas pedras.
  - ⚠ A fala 'Melhor não, pode bater nas pedras.' soa um pouco cautelosa demais para criaturas calmas como vacas ou ovelhas, que normalmente não se preocupam com a segurança do jogador.
- nota 4: 0: O humano mergulhou e não voltou ainda. / 1: Ficou muito tempo sem respirar. / 0: Precisa subir logo.
  - ⚠ A fala 'Precisa subir logo.' soa um pouco abrupta como fecho, mas é aceitável.

### shulker_casca (4.00)  _um shulker e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Minha casca tem um arranhão novo do esqueleto. / 1: A flecha raspou de lado e deixou marca. / 0: Vou lixar esse arranhão com a pedra da ponta.
  - ⚠ Shulkers não usam ferramentas como pedra para lixar; comportamento não condiz com a criatura.
- nota 4: 0: Um creeper explodiu perto da minha casca. / 1: A explosão não abriu nem uma rachadura. / 0: Minha casca aguentou bem dessa vez.
  - ⚠ A fala 'A explosão não abriu nem uma rachadura.' soa um pouco formal para uma conversa casual entre shulkers, mas ainda é aceitável.
- nota 4: 0: Minha casca rachou quando o esqueleto atirou. / 1: Aquela flecha bateu forte, mas não atravessou. / 0: Vou lixar a borda rachada com a pedra da ponta.
  - ⚠ A fala 'Vou lixar a borda rachada com a pedra da ponta' soa estranha para um shulker, pois não é típico dessa criatura usar ferramentas ou lixar a própria casca.
- nota 4: 0: Vi um zumbi batendo na minha casca. / 1: Ele não passou da camada de fora. / 0: Ainda bem que a casca é grossa na frente.
  - ⚠ A fala 'Ainda bem que a casca é grossa na frente.' soa um pouco estranha, pois shulkers têm casca em todo o corpo, não apenas na frente.
- nota 4: 0: A casca protegeu do ataque do ghast. / 1: A bola de fogo bateu e desviou para o lado. / 0: Vou checar se a casca ainda está firme.
  - ⚠ A fala 'A casca protegeu do ataque do ghast.' soa um pouco formal para uma conversa casual entre shulkers.
  - ⚠ A resposta 'A bola de fogo bateu e desviou para o lado.' é um pouco redundante, pois repete a informação do ataque.

### vaca_balde (4.00)  _uma vaca e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: O humano me dá capim depois da ordenha. / 1: As folhas verdes são crocantes e doces. / 0: Mastigar devagar é um bom descanso.
  - ⚠ A fala 'As folhas verdes são crocantes e doces' soa um pouco poética demais para uma vaca, mas ainda é aceitável.
  - ⚠ A fala 'Mastigar devagar é um bom descanso' é um pouco reflexiva, mas não chega a ser filosófica.
- nota 2: 0: O leite escorre pro balde, espumando. / 1: O som do jato no metal é constante. / 0: Quando afina, o balde já está quase cheio.
  - ⚠ Fala 0: 'O leite escorre pro balde, espumando.' soa como narração, não como fala natural de uma vaca.
  - ⚠ Fala 1: 'O som do jato no metal é constante.' é uma observação técnica incomum para uma vaca.
- nota 4: 0: O humano esqueceu o balde no pasto. / 1: O metal brilha perto da cerca. / 0: Alguém vai tropeçar nele mais tarde.
  - ⚠ A fala 'O metal brilha perto da cerca.' soa um pouco observacional demais para uma vaca, mas ainda é plausível como reação ao balde.
- nota 3: 0: O balde fica pesado e o humano sorri. / 1: Leite fresco enche a vasilha até a borda. / 0: Vou aproveitar o alívio no úbere.
  - ⚠ A fala 'O balde fica pesado e o humano sorri.' soa mais como narração do que fala natural de uma vaca.
  - ⚠ A resposta 'Leite fresco enche a vasilha até a borda.' também é descritiva, não uma reação conversacional.

### warden_vibracao (4.00)  _um Warden e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Tem algo se movendo na água. / 1: Ondas batem na pedra. Alguém caiu no lago. / 0: Vou esmagar quem fez esse barulho.
  - ⚠ A fala 'Vou esmagar quem fez esse barulho' é agressiva, mas Wardens são cegos e reagem a vibrações, não a barulho; 'barulho' pode ser aceitável, mas 'vibração' seria mais preciso.
  - ⚠ A resposta 'Ondas batem na pedra. Alguém caiu no lago.' é um pouco poética para um Warden, que é uma criatura instintiva e não filosófica.
- nota 3: 0: Alguém corre na areia. / 1: Passos abafados, mas o som chega. / 0: Vou seguir a trilha das vibrações.
  - ⚠ Warden não fala em frases completas; sua comunicação é baseada em grunhidos e vibrações.
  - ⚠ A fala 'Vou seguir a trilha das vibrações' soa muito articulada para um Warden.
- nota 4: 0: Tem minério caindo no chão. / 1: Pedra quebrada faz barulho alto. / 0: Quem quebrou vai pagar.
  - ⚠ A fala 'Quem quebrou vai pagar.' soa um pouco vingativa demais para um Warden, que normalmente age por instinto e não por vingança.
- nota 4: 0: Sinto cheiro de criatura morta. / 1: Carne podre vibra com moscas. / 0: Nada de passos. Sigo em frente.
  - ⚠ A fala 'Carne podre vibra com moscas' é um pouco poética demais para um Warden, que é uma criatura cega e instintiva, mas ainda assim aceitável.
- nota 4: 0: Sinto uma vibração forte. / 1: É uma explosão de creeper. / 0: Vou atrás da criatura que sobrou.
  - ⚠ A fala 'Vou atrás da criatura que sobrou.' é um pouco vaga, mas aceitável para um Warden.

### wskel_fortaleza (4.00)  _um esqueleto wither e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Meu machado cortou três hoje. / 1: Três a menos para vigiar o portão. / 0: O portão fica mais seguro agora.
  - ⚠ A fala 'Meu machado cortou três hoje' é vaga e não especifica o que foi cortado, o que pode confundir o ouvinte.
  - ⚠ A resposta 'Três a menos para vigiar o portão' assume que os três cortados eram vigias, mas isso não fica claro na fala inicial.
- nota 4: 0: O cheiro de enxofre está forte. / 1: Enxofre forte sinaliza lava próxima. / 0: Vou checar a fonte do cheiro.
  - ⚠ A fala 'Enxofre forte sinaliza lava próxima.' soa um pouco formal e explicativa para uma conversa casual entre criaturas.
- nota 4: 0: As escadas rangem sob os pés. / 1: Rangido atrai presa para a armadilha. / 0: Deixo as escadas como estão.
  - ⚠ A fala 'Rangido atrai presa para a armadilha.' soa um pouco formal para uma conversa casual entre esqueletos wither, mas ainda é aceitável.
- nota 4: 0: A lava ilumina pouco os corredores. / 1: Pouca luz esconde os invasores. / 0: Ando com o machado pronto.
  - ⚠ A fala 'Ando com o machado pronto' pode soar um pouco estranha para esqueletos wither, que normalmente usam espadas de pedra, mas é aceitável como variação.
- nota 4: 0: A ponte sobre o abismo balançou. / 1: Balanço pode derrubar quem cruza. / 0: Patrulho longe da ponte.
  - ⚠ A fala 'Balanço pode derrubar quem cruza.' soa um pouco formal para uma conversa casual entre esqueletos wither.
  - ⚠ A fala 'Patrulho longe da ponte.' é um fecho abrupto, mas aceitável.

### zpiglin_bando (4.00)  _um piglin zumbificado e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Ninguém encara o bando e sai vivo. / 1: A gente rasga o humano em pedaços. / 0: O bando não deixa sobrar nada.
  - ⚠ Fala 0: 'Ninguém encara o bando e sai vivo.' soa um pouco genérica, mas aceitável para piglins zumbificados.
  - ⚠ Fala 1: 'A gente rasga o humano em pedaços.' é agressiva e condizente, mas poderia ser mais específica do mundo dos piglins.
- nota 4: 0: Se o humano bater, o bando revida. / 1: A gente arranca a carne dele com os dentes. / 0: Ele não aguenta o bando inteiro.
  - ⚠ A fala 'A gente arranca a carne dele com os dentes' é um pouco longa para uma conversa casual, mas ainda aceitável.
  - ⚠ A resposta 'A gente arranca a carne dele com os dentes' não responde diretamente à fala anterior, mas mantém o tema de agressão ao humano.
- nota 4: 0: Se um de nós apanha, o bando inteiro avança. / 1: A gente mordo humano junto, sem esperar. / 0: Nenhum foge, todos atacam.
  - ⚠ A fala 'A gente mordo humano junto, sem esperar.' tem erro de concordância verbal ('mordo' em vez de 'morde'), soando artificial.
- nota 4: 0: Um de nós cai, todos avançam. / 1: A gente pisa no humano até ele parar. / 0: O bando não deixa ele escapar.
  - ⚠ A fala 'A gente pisa no humano até ele parar' soa um pouco estranha para piglins zumbificados, que normalmente grunhem e não falam de forma tão articulada, mas é aceitável no contexto do jogo.
- nota 4: 0: A raiva de um vira raiva de todos. / 1: A gente grunhe junto e parte pra cima. / 0: O humano cai e a gente continua.
  - ⚠ A fala 'A raiva de um vira raiva de todos.' soa um pouco filosófica para um piglin zumbificado, que normalmente age por instinto.
  - ⚠ A fala 'O humano cai e a gente continua.' é um fecho aceitável, mas poderia ser mais direto e agressivo, como 'Humano cai, a gente continua.'

### hoglin_floresta (4.12)  _um hoglin e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Tem fungo novo crescendo perto da lava. / 1: Fungo novo é macio e fácil de mastigar. / 0: Vou comer aquele antes que queime.
  - ⚠ A fala 'Vou comer aquele antes que queime' soa um pouco estranha, pois fungos próximos à lava já estariam queimados ou não seriam comestíveis; poderia ser mais natural como 'Vou comer aquele antes que
- nota 4: 0: A floresta carmesim está cheia de fungos hoje. / 1: Cheio de fungo bom, mas os outros hoglins vêm brigar. / 0: Se aparecerem, eu enfrento.
  - ⚠ A fala 'Cheio de fungo bom, mas os outros hoglins vêm brigar.' soa um pouco estranha: 'fungo bom' é vago e a menção a brigas parece abrupta, mas ainda é plausível para hoglins.
- nota 4: 0: O chão está coberto de fungos bons de comer. / 1: Tem fungo demais, mas outros vão querer. / 0: Vou comer rápido antes que cheguem.
  - ⚠ A fala 'Tem fungo demais, mas outros vão querer.' soa um pouco estranha para um hoglin, pois eles são agressivos e não costumam se preocupar com outros, mas ainda é aceitável.
- nota 4: 0: Fungo vermelho brotando perto da lava. / 1: Aquele ali é meu, não chega perto. / 0: Vou pegar antes que alguém roube.
  - ⚠ A fala 'Vou pegar antes que alguém roube' soa um pouco genérica, mas ainda plausível para hoglins.
- nota 4: 0: Vejo um cacho de fungos além das árvores. / 1: Aquele cacho é meu, fique longe. / 0: Vou lá pegar todos.
  - ⚠ A fala 'Vou lá pegar todos' soa um pouco abrupta como fecho, mas é aceitável para hoglins.

### jaguatirica_creeper (4.12)  _uma jaguatirica e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Os creepers fogem de mim, sabia? / 1: Sei, já vi um correndo ao ouvir seu miado. / 0: Meu miado é aviso, não ameaça.
  - ⚠ A fala 'Os creepers fogem de mim, sabia?' soa um pouco estranha para uma jaguatirica, pois creepers são criaturas explosivas e não costumam fugir de mobs passivos; mas é aceitável no contexto do jogo.
  - ⚠ A resposta 'Sei, já vi um correndo ao ouvir seu miado.' é natural, mas o fecho 'Meu miado é aviso, não ameaça.' poderia ser mais curto e direto.
- nota 4: 0: O creeper me viu e saiu em disparada mata adentro. / 1: Fugiu antes de você chegar perto do ninho? / 0: Sim, e nem precisei mostrar os dentes.
  - ⚠ A fala 'O creeper me viu e saiu em disparada mata adentro' soa um pouco formal para uma jaguatirica, mas é aceitável.
  - ⚠ A resposta 'Fugiu antes de você chegar perto do ninho?' introduz um ninho sem contexto prévio, mas é plausível.
- nota 4: 0: Dizem que sou o terror dos creepers. / 1: Ouvi isso de um papagaio, ele repete tudo. / 0: Papagaio fala demais, mas dessa vez acertou.
  - ⚠ A fala 'Dizem que sou o terror dos creepers' soa um pouco estranha para uma jaguatirica, pois não é comum associá-la a caçar creepers, mas é aceitável no contexto do jogo.
  - ⚠ A resposta 'Ouvi isso de um papagaio, ele repete tudo' é natural, mas o fecho 'Papagaio fala demais, mas dessa vez acertou' poderia ser mais conciso.
- nota 4: 0: Os creepers mudam de direção quando me sentem. / 1: Sentem seu cheiro e viram para longe do rio. / 0: Vão para as colinas, onde eu não vou.
  - ⚠ A fala 'Os creepers mudam de direção quando me sentem' é um pouco estranha, pois creepers não têm comportamento de evitar jaguatiricas no jogo, mas é aceitável como conversa entre animais.
  - ⚠ A resposta 'Sentem seu cheiro e viram para longe do rio' é coerente, mas 'viram para longe do rio' é específico demais sem contexto.
- nota 4: 0: Vi um creeper hoje, ele correu assim que me sentiu. / 1: Eles sabem que jaguatirica é rápida e perigosa. / 0: Rápida sim, mas prefiro roedores a pólvora.
  - ⚠ A fala 'Rápida sim, mas prefiro roedores a pólvora.' soa um pouco filosófica demais para uma jaguatirica, mas ainda é aceitável.

### silverfish_pedra (4.12)  _um traça e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 3: 0: A pedra quebrada é o nosso sinal. / 1: O barulho da picareta avisa que tem humano. / 0: Aí a gente sai tudo de uma vez.
  - ⚠ Fala 0: 'A pedra quebrada é o nosso sinal.' soa vaga e não combina com o mundo de traças, que não usam sinais complexos.
  - ⚠ Fala 1: 'O barulho da picareta avisa que tem humano.' é plausível, mas a resposta não se conecta diretamente com a fala anterior.
- nota 4: 0: Ninguém vê a gente dentro da pedra. / 1: Só quando quebram, aí já é tarde para eles. / 0: Por isso a pedra é o nosso esconderijo.
  - ⚠ A fala 'Só quando quebram, aí já é tarde para eles.' é um pouco vaga sobre o que acontece quando quebram a pedra, mas ainda faz sentido no contexto.
- nota 4: 0: A fortaleza tem pedra infestada no corredor do portal. / 1: Ali ninguém quebra pedra, então a gente fica seguro. / 0: Vamos ficar nesse corredor até aparecer intruso.
  - ⚠ A fala 'A fortaleza tem pedra infestada no corredor do portal.' soa mais como uma constatação do que uma fala natural de traça, mas é aceitável.
  - ⚠ A resposta 'Ali ninguém quebra pedra, então a gente fica seguro.' é coerente, mas 'a gente' é uma expressão humana; traças poderiam usar 'nós' ou 'as traças'.
- nota 4: 0: A fortaleza tem pedra infestada por todo lado. / 1: No corredor do portal tem um monte. / 0: Então vamos ficar por lá.
  - ⚠ A fala 'No corredor do portal tem um monte.' é um pouco vaga, mas aceitável no contexto.
- nota 4: 0: Dentro da pedra a gente espera o barulho. / 1: Se chover, o barulho da chuva atrapalha ouvir picareta. / 0: Então melhor ficar em pedra funda longe da superfície.
  - ⚠ A fala 'Dentro da pedra a gente espera o barulho.' é um pouco vaga, mas aceitável.
  - ⚠ A resposta 'Se chover, o barulho da chuva atrapalha ouvir picareta.' é coerente, mas poderia ser mais natural.

### vex_voo (4.12)  _um vex e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Passei por um fio de teia sem me prender. / 1: Teia não pega quem voa rápido assim. / 0: Vou manter a velocidade para não ficar preso.
  - ⚠ A fala 'Passei por um fio de teia sem me prender.' soa um pouco artificial, pois vexes não costumam se preocupar com teias, já que voam e raramente são afetados por elas.
  - ⚠ A resposta 'Teia não pega quem voa rápido assim.' é redundante, pois vexes já sabem que são imunes a teias enquanto voam.
- nota 4: 0: Meu mestre me chamou de volta. / 1: Mestre chamando é ordem para parar de voar. / 0: Vou voltar antes que ele invoque outro vex.
  - ⚠ A fala 'Mestre chamando é ordem para parar de voar.' soa um pouco formal e explicativa para uma conversa casual entre vexes.
- nota 4: 0: Atravessei uma parede e quase bati num creeper. / 1: Creeper perto de parede é perigo dobrado. / 0: Vou evitar paredes com cheiro de pólvora.
  - ⚠ A fala 'Vou evitar paredes com cheiro de pólvora' é um pouco estranha, pois vexes não têm olfato apurado para detectar pólvora; seria mais natural evitar paredes em geral ou usar visão.
- nota 4: 0: Quase fui atingido por uma flecha. / 1: Flecha passa direto se você virar na hora certa. / 0: Vou virar mais vezes para não dar alvo fácil.
  - ⚠ A fala 'Flecha passa direto se você virar na hora certa.' soa um pouco técnica para um vex, que é um espírito agressivo e caótico, mas ainda é plausível.
  - ⚠ A última fala 'Vou virar mais vezes para não dar alvo fácil.' é um pouco longa e reflexiva para o estilo de fala curto e direto esperado de um vex.
- nota 4: 0: Minha espada cortou um morcego no voo. / 1: Morcego cortado cai sem fazer barulho. / 0: Prefiro cortar morcegos do que ser atingido por eles.
  - ⚠ A fala 'Morcego cortado cai sem fazer barulho.' soa mais como uma observação do que uma resposta direta à fala anterior.
  - ⚠ A fala 'Prefiro cortar morcegos do que ser atingido por eles.' é um fecho aceitável, mas poderia ser mais natural.

### afogado_agua (4.14)  _um afogado e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: O tridente rachou depois da caçada. / 1: A rachadura foi na base, perto do cabo. / 0: Vou amarrar alga para segurar a base.
  - ⚠ A fala 'Vou amarrar alga para segurar a base' soa um pouco técnica demais para um afogado, mas ainda é plausível.
- nota 4: 0: Meu tridente está todo rachado. / 1: Rachado assim não fura a barriga de um bacalhau. / 0: Vou arrancar outro de um afogado morto.
  - ⚠ A fala 'Vou arrancar outro de um afogado morto' soa um pouco estranha, pois afogados não costumam se referir a outros da mesma espécie como 'afogado morto' de forma tão casual.
- nota 4: 0: Aquele tridente está caindo aos pedaços. / 1: As farpas soltas machucam a mão na hora de arremessar. / 0: Melhor usar pedra afiada para caçar.
  - ⚠ A fala 'Melhor usar pedra afiada para caçar' soa um pouco estranha para um afogado, que normalmente usa tridentes, mas é aceitável como improviso.
- nota 4: 0: Meu tridente não perfura mais nada. / 1: A ponta quebrou, só resta o cabo. / 0: Vou usar o cabo para cutucar peixes.
  - ⚠ A fala 'Vou usar o cabo para cutucar peixes.' soa um pouco estranha, pois afogados normalmente não usam ferramentas para pescar, mas é aceitável como improviso.
- nota 4: 0: Achei um tridente rachado no fundo. / 1: Deve ter batido numa pedra quando o dono morreu. / 0: Vou jogar fora, não serve para caçar.
  - ⚠ A fala 'Vou jogar fora, não serve para caçar.' soa um pouco estranha para um afogado, pois eles não costumam 'jogar fora' itens, mas é aceitável.

### aq_fundo (4.14)  _um bicho aquático e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Lá embaixo a água é bem mais fria, já reparou? / 1: Reparei sim, dá até um arrepio. / 0: Por isso prefiro ficar mais em cima.
  - ⚠ Fala 0: 'Lá embaixo a água é bem mais fria, já reparou?' soa um pouco genérica, mas aceitável para criaturas aquáticas.
  - ⚠ Fala 1: 'Reparei sim, dá até um arrepio.' é natural, mas 'arrepio' pode ser estranho para criaturas aquáticas, dependendo da espécie.
- nota 4: 0: O fundo tem cada barulho esquisito. / 1: Tem coisa lá que é melhor nem ver. / 0: Melhor evitar aquele canto.
  - ⚠ Fala 1: 'Tem coisa lá que é melhor nem ver' soa um pouco vaga, mas aceitável para bichos aquáticos.
  - ⚠ Fala 0 (fecho): 'Melhor evitar aquele canto' é coerente, porém poderia ser mais específica para fechar melhor.
- nota 4: 0: Tem bicho que nunca sobe à superfície. / 1: Só vou até onde ainda enxergo alguma coisa. / 0: Fica perto da luz, então.
  - ⚠ A fala 'Só vou até onde ainda enxergo alguma coisa.' soa um pouco vaga, mas ainda plausível para criaturas aquáticas.
- nota 4: 0: Dizem que tem tesouro lá embaixo. / 1: Ouvi falar de um baú afundado. / 0: Quem sabe um dia a gente desce.
  - ⚠ Fala 0: 'Dizem que tem tesouro lá embaixo.' - 'lá embaixo' é vago para criaturas aquáticas, que vivem na água; poderia ser mais específico como 'no fundo'.
  - ⚠ Fala 1: 'Ouvi falar de um baú afundado.' - 'afundado' é redundante para criaturas aquáticas, pois tudo está submerso para elas.

### aranha_zumbi (4.14)  _uma aranha e um zumbi; o bioma é uma selva_
- nota 3: 0: Sss... na selva tem muitas teias. / 1: Grrr... cipós atrapalham meu caminho. / 0: Sss... use o facão para abrir.
  - ⚠ Zumbi menciona 'facão', item que não existe no Minecraft.
  - ⚠ A fala da aranha 'use o facão para abrir' soa como conselho prático, incomum para uma aranha.
- nota 4: 0: Sss... eu fico no teto, você no chão. / 1: Grrr... eu pego o humano embaixo. / 0: Sss... eu desço e enrosco.
  - ⚠ A fala 'eu desço e enrosco' é um pouco vaga, mas aceitável para uma aranha.
- nota 4: 0: Sss... areia quente no deserto. / 1: Grrr... meus pés afundam nela. / 0: Sss... rasteje sobre as dunas.
  - ⚠ A fala do zumbi 'Grrr... meus pés afundam nela.' é um pouco estranha, pois zumbis normalmente não reclamam de desconforto físico, mas é aceitável.
  - ⚠ A fala final da aranha 'Sss... rasteje sobre as dunas.' soa como um conselho, o que é um pouco incomum para uma aranha, mas ainda faz sentido no contexto.
- nota 4: 0: Sss... a sua fome é barulhenta. / 1: Grrr... barriga roncou alto. / 0: Sss... silêncio na caçada.
  - ⚠ A fala 'Sss... silêncio na caçada.' soa um pouco artificial para uma aranha, que normalmente não dá ordens de silêncio a um zumbi.
- nota 4: 0: Sss... a chuva abafa os passos. / 1: Grrr... humano não ouve nada. / 0: Sss... ataque na poça d'água.
  - ⚠ A fala 'ataque na poça d'água' é um pouco vaga, mas aceitável como plano de emboscada.

### bicho_lembranca_geral (4.14)  _dois animais da fazenda; uma situação qualquer do dia a dia_
- nota 4: 0: A lama cobriu minhas patas até o joelho. / 1: Ficou tão pesado que mal conseguia andar. / 0: Ainda sinto a lama entre os cascos.
  - ⚠ A fala 'A lama cobriu minhas patas até o joelho' é um pouco longa para uma conversa casual entre animais, mas ainda aceitável.
  - ⚠ A resposta 'Ficou tão pesado que mal conseguia andar' não especifica quem ficou pesado, mas o contexto permite inferir que é a lama nas patas.
- nota 4: 0: O rio encheu e cobriu a trilha do pasto. / 1: Ficamos presos no curral por dois dias. / 0: Depois a água baixou e voltamos a pastar.
  - ⚠ A fala 'Ficamos presos no curral por dois dias.' soa mais como uma constatação do que uma resposta direta à fala anterior, mas ainda é plausível.
  - ⚠ A fala 'Depois a água baixou e voltamos a pastar.' fecha a conversa de forma um pouco abrupta, sem uma reação emocional ou comentário adicional.
- nota 4: 0: A chuva trouxe minhocas para perto da cerca. / 1: Elas ficaram se mexendo na lama. / 0: Comi umas três de uma vez.
  - ⚠ A fala 'Elas ficaram se mexendo na lama.' soa um pouco estranha, pois minhocas não costumam ser descritas como 'se mexendo na lama' de forma tão vaga; seria mais natural dizer 'Elas estavam se contorc
- nota 4: 0: Eu nunca esqueci o susto com a cerca quebrada. / 1: O barulho da madeira estalando me acordou. / 0: Até hoje fico longe daquela cerca.
  - ⚠ Falta contexto da situação: as falas não deixam claro o que aconteceu no momento, apenas relembram um evento passado.
- nota 4: 0: O vento forte derrubou o galho seco no chão. / 1: O barulho seco me fez pular de susto. / 0: Depois vi que era só um galho.
  - ⚠ A fala 0 inicial soa mais como narração do que como fala natural de um animal.
  - ⚠ A fala 1 'O barulho seco me fez pular de susto' é um pouco formal para fala espontânea.

### cave_spider_mina (4.14)  _uma aranha da caverna e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: A mina tem muitos cantos para espreitar. / 1: O canto atrás do carrinho é bom. / 0: Fico lá esperando presa.
  - ⚠ A fala 'O canto atrás do carrinho é bom.' pode ser ambígua, pois 'carrinho' pode se referir a um carrinho de mina, mas não fica claro se a aranha conhece esse objeto.
  - ⚠ A fala 'Fico lá esperando presa.' é um pouco genérica e poderia ser mais específica para aranhas, como 'esperando um inseto' ou 'esperando um minerador'.
- nota 4: 0: As teias cobriram a entrada da mina. / 1: Nada entra sem ficar preso. / 0: A mina fica só nossa.
  - ⚠ A fala 'A mina fica só nossa.' soa possessiva demais para aranhas, que não demonstram territorialidade em relação a minas.
- nota 4: 0: A picada parou aquele minerador. / 1: Ele caiu com a picareta na mão. / 0: A picareta fica para a gente.
  - ⚠ A fala 'A picareta fica para a gente' sugere posse de ferramenta, o que não combina com o comportamento natural de aranhas da caverna.
- nota 3: 0: Um zumbi ficou preso na teia. / 1: Zumbi preso apodrece devagar. / 0: Carne podre atrai moscas.
  - ⚠ A fala 'Zumbi preso apodrece devagar.' soa mais como uma constatação filosófica do que uma resposta natural de uma aranha da caverna.
  - ⚠ A fala 'Carne podre atrai moscas.' não fecha a conversa de forma natural, parece um comentário solto.
- nota 4: 0: As teias balançam com o vento da mina. / 1: O vento traz cheiro de morcego. / 0: Morcego vem direto para a teia.
  - ⚠ A fala 'Morcego vem direto para a teia.' soa um pouco abrupta como fecho, mas é aceitável.

### peixe_cardume (4.17)  _um peixe e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Tem sempre um que se perde do cardume. / 1: Aquele filhote entrou na caverna de coral. / 0: Ele vai achar a saída, os corais têm buracos.
  - ⚠ A fala 'Ele vai achar a saída, os corais têm buracos.' soa um pouco otimista demais para um peixe, mas é aceitável.
- nota 3: 0: Quem virou para o lado errado de novo? / 1: Eu vi uma sombra grande e me assustei. / 0: Então nada mais rápido e volta para o grupo.
  - ⚠ A fala 'Quem virou para o lado errado de novo?' não combina com o mundo de um peixe, pois peixes não têm conceito de 'lado errado' em grupo.
  - ⚠ A resposta 'Eu vi uma sombra grande e me assustei.' é plausível, mas a fala final 'Então nada mais rápido e volta para o grupo.' soa como ordem de líder, o que não é típico de peixes.
- nota 4: 0: Fica no cardume, não saia da turma. / 1: Estou nadando junto, olha as minhas barbatanas. / 0: Então continua assim, perto de mim.
  - ⚠ A fala 'Estou nadando junto, olha as minhas barbatanas.' soa um pouco artificial para um peixe, pois peixes não costumam chamar atenção para as próprias barbatanas em conversas cotidianas.
- nota 4: 0: Nadar em grupo é bem mais seguro. / 1: Quando o tubarão apareceu, eu me escondi no meio. / 0: Por isso nunca saia de perto dos outros.
  - ⚠ A fala 'Quando o tubarão apareceu, eu me escondi no meio.' introduz um evento específico (tubarão) que não é mencionado na situação, que é genérica ('uma situação qualquer do dia a dia'), tornando a r

### cruz_lobo_gato (4.20)  _um lobo e um gato; uma situação qualquer do dia a dia_
- nota 4: 0: Gato, você come peixe cru. / 1: Peixe cru é o que eu pesco no rio. / 0: Eu prefiro carne de ovelha, mas peixe serve.
  - ⚠ A fala 'Gato, você come peixe cru.' soa como uma afirmação, não como uma pergunta ou comentário natural para iniciar conversa.
  - ⚠ A resposta 'Peixe cru é o que eu pesco no rio.' é redundante, pois já se sabe que gatos comem peixe cru.
- nota 4: 0: Gato, você não late pra avisar. / 1: Eu ronrono, e o ronrono acalma a cria. / 0: Eu uivo, e o uivo chama a alcateia.
  - ⚠ A fala 'Eu ronrono, e o ronrono acalma a cria.' usa 'cria' de forma genérica, o que pode soar estranho para um gato, que normalmente se referiria a 'filhotes' ou 'gatinhos'.
  - ⚠ A fala 'Eu uivo, e o uivo chama a alcateia.' é adequada para um lobo, mas a conversa parece mais uma troca de informações do que uma conversa natural do dia a dia.
- nota 4: 0: Gato, você se acha melhor que eu? / 1: Subo em árvore, você não alcança. / 0: Mas no chão eu te alcanço, gato.
  - ⚠ A fala 'Subo em árvore, você não alcança.' soa um pouco seca, mas é plausível para um gato.
  - ⚠ A última fala 'Mas no chão eu te alcanço, gato.' é uma ameaça leve, mas aceitável para um lobo.
- nota 4: 0: Gato, você mia baixo demais. / 1: Miar alto espanta o peixe na beira do rio. / 0: Por isso que eu uivo, para a alcateia ouvir.
  - ⚠ A fala do lobo 'Por isso que eu uivo, para a alcateia ouvir' não responde diretamente à explicação do gato sobre miar baixo para não espantar peixes, mas ainda fecha a conversa de forma aceitável.

### aq_humano_nada (4.25)  _um bicho aquático e outra criatura da mesma espécie; o jogador está nadando na água_
- nota 4: 0: O humano nada devagar, mas nada. / 1: Comparado com a gente, parece parado. / 0: Mas ele não afunda, então tudo bem.
  - ⚠ A fala 'Comparado com a gente, parece parado.' soa um pouco estranha para criaturas aquáticas, pois elas não costumam fazer comparações de velocidade com humanos, mas é aceitável.
- nota 4: 0: Olha o humano nadando à noite. / 1: Ele mal enxerga no escuro da água. / 0: Nós enxergamos bem, então ficamos de olho.
  - ⚠ A fala 'Nós enxergamos bem, então ficamos de olho.' soa um pouco artificial, como se estivesse explicando demais para o jogador.
- nota 4: 0: Veja só quem veio para a nossa água. / 1: Ele mergulha e volta para respirar. / 0: Vamos observar de longe, sem assustar.
  - ⚠ A fala 'Ele mergulha e volta para respirar.' soa mais como uma observação neutra do que uma resposta natural à fala anterior, mas ainda é plausível.
- nota 4: 0: O humano está nadando! Olha só. / 1: Ele chuta a água com força, fazendo espuma. / 0: Vamos dar espaço para não levar chute.
  - ⚠ A fala 'Ele chuta a água com força, fazendo espuma.' soa mais como uma descrição do que como uma fala natural entre criaturas.
- nota 4: 0: O humano está nadando perto das algas. / 1: Ele pode se enroscar nas algas. / 0: Vamos torcer para ele não se enroscar.
  - ⚠ A fala 'Vamos torcer para ele não se enroscar' soa um pouco artificial para criaturas aquáticas, pois 'torcer' é uma expressão humana.

### coelho_pulo (4.25)  _um coelho e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Vi uma poça funda perto do carvalho. / 1: Eu pulei por cima dela e respinguei água. / 0: Vou evitar essa poça e pular mais adiante.
  - ⚠ A fala 'Eu pulei por cima dela e respinguei água' soa um pouco estranha, pois coelhos normalmente não descrevem suas ações em detalhes; seria mais natural algo como 'Eu pulei e me molhei'.
  - ⚠ A fala 'Vou evitar essa poça e pular mais adiante' é um pouco formal para uma conversa casual entre coelhos; poderia ser mais curta, como 'Vou pular mais longe'.
- nota 4: 0: A grama perto do rio está macia. / 1: É, pulei ali e minhas patas afundaram um pouco. / 0: Então vou dar mais um pulo naquele trecho.
  - ⚠ A fala 'Então vou dar mais um pulo naquele trecho.' soa um pouco artificial para um coelho, que normalmente não anuncia suas ações dessa forma.
- nota 4: 0: A terra solta me fez pular mais alto. / 1: Na terra solta o impulso é melhor, senti isso também. / 0: Vou procurar mais terra solta para treinar.
  - ⚠ A fala 'Vou procurar mais terra solta para treinar' soa um pouco artificial para um coelho, pois coelhos não 'treinam' conscientemente; seria mais natural algo como 'Vou procurar mais terra solta para
- nota 4: 0: Aquele tronco caído é um bom alvo. / 1: Eu também pulo até ele, mas às vezes erro o cálculo. / 0: Vou treinar o pulo até acertar o tronco sempre.
  - ⚠ A fala 'Aquele tronco caído é um bom alvo' soa um pouco artificial para um coelho, pois coelhos não costumam treinar pulos em alvos específicos, mas é aceitável no contexto do jogo.
  - ⚠ A resposta 'Eu também pulo até ele, mas às vezes erro o cálculo' é plausível, mas 'erro o cálculo' parece uma expressão humana demais para um coelho.
- nota 4: 0: Pulei três vezes sem parar. / 1: Eu contei seus pulos, o terceiro foi o mais alto. / 0: Vou tentar pular quatro vezes agora.
  - ⚠ A fala 'Eu contei seus pulos, o terceiro foi o mais alto.' soa um pouco analítica para um coelho, mas ainda é plausível.

### enderman_olhar (4.25)  _um enderman e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: O mundo tem blocos demais espalhados. / 1: Eu pego alguns e levo para outro lugar. / 0: Assim organizo meu canto sem ser incomodado.
  - ⚠ A fala 'O mundo tem blocos demais espalhados.' soa um pouco filosófica para um enderman, que normalmente emite sons curtos e não reflete sobre o mundo.
  - ⚠ A resposta 'Eu pego alguns e levo para outro lugar.' é plausível, mas poderia ser mais direta, como 'Pego bloco. Levo.'
- nota 4: 0: Quem me encara, eu persigo. / 1: Eu fico furioso e grito quando isso acontece. / 0: Melhor evitar esse humano curioso.
  - ⚠ A fala 'Melhor evitar esse humano curioso.' soa um pouco genérica e não fecha a conversa de forma natural, pois não responde diretamente à fala anterior.
- nota 4: 0: O humano olhou para mim de novo. / 1: Então ele quer ser perseguido até o fim. / 0: Vou atrás dele sem parar.
  - ⚠ A fala 'Então ele quer ser perseguido até o fim' soa um pouco formal para um diálogo curto entre endermans, mas ainda é aceitável.
- nota 4: 0: Carrego este bloco de grama para longe. / 1: Cuidado para não ser visto pelo humano. / 0: Se ele olhar, eu solto o bloco e ataco.
  - ⚠ A fala 'Se ele olhar, eu solto o bloco e ataco' é um pouco longa para uma conversa natural, mas ainda aceitável.
- nota 4: 0: Meus olhos ardem quando o humano me encara. / 1: Sinta a raiva crescer e teleporte para perto dele. / 0: Vou fazer ele parar de olhar.
  - ⚠ A fala 'Sinta a raiva crescer e teleporte para perto dele.' soa como um conselho genérico, não uma resposta natural entre endermen.
  - ⚠ A fala 'Vou fazer ele parar de olhar.' é um fecho aceitável, mas poderia ser mais específica ao comportamento do enderman.

### slime_pulo (4.25)  _um slime e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Vi outro slime se dividir hoje. / 1: Ele se partiu em dois slimes pequenos. / 0: Depois os dois saíram pulando juntos.
  - ⚠ A fala 'Depois os dois saíram pulando juntos.' é um pouco redundante, pois slimes naturalmente pulam, mas ainda é aceitável.
- nota 4: 0: Eu pulo e faço blob, blob, blob. / 1: Esse barulho de esguicho ecoa no pântano. / 0: Vamos pular juntos e fazer mais barulho.
  - ⚠ A fala 'Esse barulho de esguicho ecoa no pântano.' soa um pouco descritiva demais para uma resposta natural entre slimes.
- nota 3: 0: Meu corpo quica no chão duro. / 1: No chão duro o quique faz tum, tum. / 0: Então quica aqui para eu ouvir o tum.
  - ⚠ A fala 'No chão duro o quique faz tum, tum.' soa artificial e repetitiva, não natural para uma conversa entre slimes.
  - ⚠ O fecho 'Então quica aqui para eu ouvir o tum.' é estranho, pois slimes não têm ouvidos e a conversa não se encerra de forma natural.
- nota 3: 0: O pulo faz barulho de esguicho. / 1: Esguicho que lembra água batendo na lama. / 0: Vou pular de novo para ouvir o esguicho.
  - ⚠ A fala 'O pulo faz barulho de esguicho.' é muito descritiva e não soa como fala natural de slime.
  - ⚠ A resposta 'Esguicho que lembra água batendo na lama.' é uma observação poética incomum para um slime.

### zumbi_horda (4.25)  _um zumbi e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Ouvi passos perto do rio. / 1: Talvez seja um pescador distraído. / 0: Vamos rastejar até a margem.
  - ⚠ Zumbis não costumam rastejar, mas sim andar lentamente; 'rastejar' soa estranho para a locomoção típica de um zumbi.
- nota 4: 0: A selva esconde presas fáceis. / 1: Macacos e viajantes perdidos. / 0: Vamos cercar a trilha da selva.
  - ⚠ A fala 'Macacos e viajantes perdidos.' é uma resposta curta, mas não desenvolve a ideia de 'presas fáceis' de forma natural; parece uma lista, não uma conversa.
  - ⚠ O fecho 'Vamos cercar a trilha da selva.' é coerente, mas poderia ser mais específico sobre a ação conjunta.
- nota 4: 0: O pântano atola nossos pés. / 1: Mas os humanos também ficam presos. / 0: Então vamos afundar até a carne.
  - ⚠ A fala 'Então vamos afundar até a carne' é um pouco estranha, pois zumbis não costumam se preocupar com afundar, mas pode ser interpretada como uma aceitação sombria.
- nota 4: 0: Tem uma fogueira acesa na clareira. / 1: Humanos aquecem as mãos e cochilam. / 0: Aproveitamos o calor para atacar.
  - ⚠ A fala 'Aproveitamos o calor para atacar' soa um pouco formal para um zumbi, que normalmente teria uma fala mais simples e direta.
- nota 4: 0: A neve congela nossos membros. / 1: Mordida gelada ainda arranca sangue. / 0: Vamos atrás de rastros na neve.
  - ⚠ A fala 'Mordida gelada ainda arranca sangue' soa um pouco estranha para zumbis, pois eles não costumam falar de sangue de forma tão direta, mas é aceitável no contexto de caça.

### brutamontes_guarda (4.29)  _um piglin brutamontes e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: O bastião tem paredes grossas. / 1: Nenhum invasor derruba parede assim. / 0: Então fique de olho na entrada.
  - ⚠ A fala 'O bastião tem paredes grossas.' soa um pouco genérica e não reflete o mundo específico dos piglins brutamontes, que são mais focados em força e ouro.
- nota 4: 0: Eu não durmo, eu vigio. / 1: Vigiar é o que mantém o bastião de pé. / 0: Então continue acordado.
  - ⚠ A fala 'Vigiar é o que mantém o bastião de pé.' soa um pouco formal para um piglin brutamontes, que tende a ser mais direto e agressivo.
- nota 4: 0: Meu machado está afiado para quem tentar. / 1: Quem tentar vai virar pedaço. / 0: Então corte quem vier.
  - ⚠ A fala 'Quem tentar vai virar pedaço.' é um pouco genérica e não acrescenta muito à conversa, mas ainda é plausível para piglins brutamontes.
  - ⚠ A fala 'Então corte quem vier.' soa um pouco redundante após a resposta, mas fecha a conversa de forma aceitável.
- nota 4: 0: O eco dos meus passos avisa que estou de vigia. / 1: Passo pesado espanta invasor. / 0: Então ande pesado pelo bastião.
  - ⚠ A fala 'O eco dos meus passos avisa que estou de vigia.' soa um pouco filosófica para um piglin brutamontes, que tende a ser mais direto e agressivo.
  - ⚠ A resposta 'Passo pesado espanta invasor.' é curta e combina, mas poderia ser mais natural com um verbo conjugado, como 'Passo pesado espanta invasores.'
- nota 4: 0: Aqui não entra nem um piglin zumbi. / 1: Zumbi que tenta leva machadada. / 0: Então mate qualquer zumbi que aparecer.
  - ⚠ A fala 'Zumbi que tenta leva machadada' soa truncada e pouco natural em português falado; seria mais natural 'Zumbi que tentar levar machadada' ou 'Zumbi que tentar, leva machadada'.

### papagaio_musica (4.29)  _um papagaio e outra criatura da mesma espécie; o bioma é uma floresta_
- nota 4: 0: Música boa, eu danço e grito bem alto. / 1: Eu grito junto, mas só se for na mata. / 0: Na mata o som ecoa e a dança fica melhor.
  - ⚠ A fala 'Música boa, eu danço e grito bem alto.' não especifica a origem da música, o que pode soar vago para um papagaio na floresta.
  - ⚠ A resposta 'Eu grito junto, mas só se for na mata.' é coerente, mas a condição 'só se for na mata' é redundante, pois já estão na floresta.
- nota 4: 0: Se tem música, eu estou balançando a cabeça. / 1: Eu também, mas só se for disco de verdade. / 0: Disco de verdade é o que faz a gente dançar.
  - ⚠ A fala 'Disco de verdade é o que faz a gente dançar.' soa um pouco filosófica para um papagaio, que tende a repetir sons e frases curtas.
- nota 4: 0: Quando toca música, eu me mexo todo. / 1: Eu me mexo e bato as asas sem parar. / 0: Então vamos achar um disco para tocar.
  - ⚠ A fala 'Quando toca música, eu me mexo todo' é um pouco genérica, mas aceitável para um papagaio.
  - ⚠ A resposta 'Eu me mexo e bato as asas sem parar' é redundante com a primeira fala, mas ainda natural.
- nota 4: 0: Ouviu o som do disco ontem à noite? / 1: Ouvi, estava perto da minha árvore. / 0: Pois é, eu fiquei dançando no galho.
  - ⚠ A fala 'Ouviu o som do disco ontem à noite?' soa um pouco formal para um papagaio, mas é aceitável.
  - ⚠ A resposta 'Ouvi, estava perto da minha árvore.' é natural, mas poderia ser mais curta.
- nota 4: 0: Já ouviu um disco tocando perto da selva? / 1: Sim, ouvi um som vindo da caverna. / 0: Então vamos procurar o toca-discos.
  - ⚠ A fala 'Então vamos procurar o toca-discos.' soa um pouco abrupta, pois não há indicação clara de que o disco está na caverna, apenas o som.

### husk_deserto (4.33)  _um husk e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Pegadas frescas levam para o templo. / 1: Humanos entram lá atrás de tesouro. / 0: Vamos segui-los até a armadilha.
  - ⚠ A fala 'Vamos segui-los até a armadilha' sugere cooperação e planejamento, o que é incomum para husks, que são mortos-vivos hostis e não costumam trabalhar em equipe ou usar armadilhas.
- nota 4: 0: A carne de coelho crua mata a fome. / 1: Mas sangue quente é melhor que carne fria. / 0: Então vamos caçar coelhos nas dunas.
  - ⚠ A fala 'Mas sangue quente é melhor que carne fria.' soa um pouco estranha para um husk, pois eles são mortos-vivos e não têm preferência por sangue quente.
- nota 4: 0: O vento forte apaga rastros na areia. / 1: Assim os humanos não fogem pelo caminho. / 0: Vamos usar o vento a nosso favor.
  - ⚠ A fala 'Assim os humanos não fogem pelo caminho' soa um pouco estratégica demais para um husk, que normalmente é uma criatura sem inteligência complexa, mas ainda é aceitável no contexto do jogo.
- nota 4: 0: Os poços secam rápido no calor. / 1: Humanos sedentos voltam para beber. / 0: Ficamos perto do poço seco.
  - ⚠ A fala 'Ficamos perto do poço seco.' soa como um plano, mas não fecha claramente a conversa; falta uma reação ou conclusão mais natural.

### curiosidade_portal (4.38)  _dois aldeões; uma situação qualquer do dia a dia_
- nota 4: 0: Ouvi falar de um mundo todo em chamas. / 1: Dizem que tem um dragão lá no fim. / 0: Só de pensar já fico arrepiado.
  - ⚠ A fala 'Dizem que tem um dragão lá no fim.' parece desconectada da fala anterior sobre um mundo em chamas, pois não estabelece relação clara entre o dragão e o fogo.
- nota 4: 0: Ouvi dizer que do outro lado tem uma terra escura. / 1: E ainda tem uns vultos estranhos. / 0: Melhor nem imaginar.
  - ⚠ A fala 'Ouvi dizer que do outro lado tem uma terra escura.' é um pouco vaga, mas aceitável para aldeões.
  - ⚠ A resposta 'E ainda tem uns vultos estranhos.' combina, mas 'vultos' pode ser um termo um pouco sofisticado para aldeões.
- nota 4: 0: Um vendedor contou que o portal leva a um mar de fogo. / 1: Deve ser quente demais lá. / 0: Ainda bem que a gente tem água por aqui.
  - ⚠ A fala 'Um vendedor contou que o portal leva a um mar de fogo.' é um pouco longa para uma conversa casual, mas ainda aceitável.
  - ⚠ A resposta 'Deve ser quente demais lá.' é natural, mas poderia ser mais específica ao contexto do portal.
- nota 4: 0: Você já passou por um portal roxo? / 1: Nunca, e nem quero. / 0: Pois é, dizem que é perigoso.
  - ⚠ A fala 'Pois é, dizem que é perigoso.' não fecha a conversa de forma conclusiva, deixando-a em aberto.

### bicho_comida_geral (4.43)  _dois animais da fazenda; o céu está limpo_
- nota 4: 0: A alfafa perto do riacho está viçosa. / 1: Alfafa viçosa é um achado raro. / 0: Vou comer antes que outros cheguem.
  - ⚠ A fala 'Alfafa viçosa é um achado raro.' soa um pouco formal para animais de fazenda em conversa casual.
- nota 4: 0: O milho seco sumiu rápido de novo. / 1: Milho seco é gostoso demais. / 0: Vou ver se acho mais perto do celeiro.
  - ⚠ A fala 'Vou ver se acho mais perto do celeiro' soa um pouco genérica e não fecha a conversa de forma natural, pois não responde diretamente à fala anterior.
- nota 4: 0: Aquele feno no cocho cheira bem forte. / 1: Feno forte enche a barriga rápido. / 0: Vou comer mais um pouco antes de deitar.
  - ⚠ A fala 'Feno forte enche a barriga rápido' soa um pouco estranha, pois 'forte' não é um atributo típico de feno que afete a saciedade.
- nota 4: 0: O cocho está cheio de grãos hoje. / 1: Grãos são ótimos para dar energia. / 0: Vou comer agora e descansar depois.
  - ⚠ A fala 'Grãos são ótimos para dar energia.' soa um pouco genérica e não específica de um animal de fazenda, mas ainda é plausível.

### lobo_osso (4.43)  _um lobo e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Vi um esqueleto deixando cair um osso. / 1: Sorte sua, eu só acho osso velho. / 0: Ossos frescos são melhores.
  - ⚠ A fala 'Sorte sua, eu só acho osso velho.' soa um pouco estranha, pois lobos não costumam 'achar' ossos, mas sim caçar ou encontrar restos; 'osso velho' é aceitável, mas poderia ser mais natural.
  - ⚠ A fala 'Ossos frescos são melhores.' é um fecho fraco, pois não acrescenta muito à conversa e poderia ser mais específica ou reativa.
- nota 4: 0: Se um humano me der osso, eu sigo ele para sempre. / 1: Osso é coisa séria. / 0: Sério mesmo, vale a lealdade.
  - ⚠ A fala 'Sério mesmo, vale a lealdade' soa um pouco formal para um lobo, mas ainda aceitável.
- nota 4: 0: Esse osso tem gosto de carne queimada. / 1: Deve ser de uma presa do fogo. / 0: Prefiro osso de caça normal.
  - ⚠ A fala 'Deve ser de uma presa do fogo' é um pouco vaga, mas aceitável para um lobo.

### lobo_alcateia (4.50)  _um lobo e outra criatura da mesma espécie; estão num lugar muito alto_
- nota 4: 0: O uivo do velho lobo veio do alto. / 1: Ele avisou que tem carniça perto da pedra. / 0: Corre antes que os corvos cheguem.
  - ⚠ A fala 'O uivo do velho lobo veio do alto.' soa mais como narração do que como fala direta de um lobo.
  - ⚠ A resposta 'Ele avisou que tem carniça perto da pedra.' é coerente, mas 'carniça' é um termo um pouco formal para um lobo em fala curta.
- nota 4: 0: A cadela do vale está no cio. / 1: O cheiro dela chega até a mata. / 0: O macho alfa vai decidir.
  - ⚠ A fala 'A cadela do vale está no cio.' soa um pouco formal para um lobo, mas é aceitável.
  - ⚠ A resposta 'O cheiro dela chega até a mata.' é natural, mas poderia ser mais curta.
- nota 4: 0: O filhote do Pardo comeu demais. / 1: Ele vai vomitar atrás do toco. / 0: Depois a gente lambe o focinho dele.
  - ⚠ A fala 'Depois a gente lambe o focinho dele' soa um pouco estranha, pois lamber o focinho de outro lobo não é uma ação típica de cuidado entre lobos adultos, mas pode ser interpretada como um gesto de

### strider_lava (4.50)  _um strider e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Meu casco esquenta bem no lago. / 1: O calor da lava deixa a gente rápido. / 0: Vou aproveitar para caçar fungo.
  - ⚠ A fala 'Vou aproveitar para caçar fungo' soa estranha, pois striders não caçam fungos; eles os comem, mas 'caçar' implica perseguir presas, o que não combina com o comportamento passivo do strider.
- nota 4: 0: Senti a lava mexer embaixo de mim. / 1: Pode ser um cubo de magma vindo. / 0: Vou atravessar para o outro lado.
  - ⚠ A fala 'Vou atravessar para o outro lado' não fecha claramente a conversa, deixando a resposta do segundo strider sem reação.
- nota 4: 0: Tem pedra nova caindo na lava. / 1: Essa pedra afunda e vira obstáculo. / 0: Vou desviar dessas pedras.
  - ⚠ A fala 'Essa pedra afunda e vira obstáculo.' soa um pouco técnica para um strider, que é uma criatura simples do Nether.

### voo_altura (4.50)  _um bicho voador e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: A altura me faz sentir livre de tudo. / 1: Livre até demais, às vezes me perco. / 0: Mas sempre acho um jeito de voltar.
  - ⚠ Fala 0: 'A altura me faz sentir livre de tudo.' soa um pouco filosófica para um bicho voador, mas ainda aceitável.
  - ⚠ Fala 1: 'Livre até demais, às vezes me perco.' é coerente, mas 'me perco' pode ser vago sem contexto.
- nota 4: 0: Vi um bando de passarinhos voando em formação. / 1: Eles voam juntos para não se perderem. / 0: Faz sentido, juntos é mais seguro.
  - ⚠ A fala 'Faz sentido, juntos é mais seguro.' soa um pouco filosófica para um bicho voador, mas ainda é aceitável.
- nota 4: 0: As nuvens ficam logo abaixo das minhas asas. / 1: Quando chove, elas ficam escuras e pesadas. / 0: Então é melhor voar abaixo delas.
  - ⚠ A fala 'As nuvens ficam logo abaixo das minhas asas.' soa um pouco poética para um bicho voador, mas ainda aceitável.

### camelo_deserto (4.57)  _um camelo e outra criatura da mesma espécie; o bioma é um deserto; o céu está limpo_
- nota 3: 0: O vento levanta a areia do deserto. / 1: Fecho os olhos e abaixo a cabeça. / 0: Depois que passa, continuo a viagem.
  - ⚠ Fala 0: 'O vento levanta a areia do deserto.' soa como narração, não como fala natural de um camelo.
  - ⚠ Fala 1: 'Fecho os olhos e abaixo a cabeça.' descreve uma ação em vez de expressar reação ou diálogo.
- nota 4: 0: A carga pesa, mas eu sigo em frente. / 1: O peso nas costas não me faz parar. / 0: Entrego a carga no acampamento à noite.
  - ⚠ A fala 'Entrego a carga no acampamento à noite.' soa um pouco artificial como fecho, pois não responde diretamente à fala anterior e parece mais uma narração do que uma fala natural.

### galinha_ovo (4.57)  _uma galinha e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Ninguém bota ovo como eu. / 1: Eu boto um ovo todo dia sem falta. / 0: Pois eu já botei dois hoje.
  - ⚠ A fala 'Pois eu já botei dois hoje.' soa um pouco competitiva demais para uma conversa casual entre galinhas, mas ainda é plausível.
- nota 4: 0: Botei um ovo agora há pouco. / 1: O meu saiu bem branquinho hoje. / 0: O meu saiu com a casca lisa.
  - ⚠ A fala 'O meu saiu bem branquinho hoje' soa um pouco artificial para uma conversa entre galinhas, pois elas não costumam comentar a cor dos ovos como se fosse uma novidade.
- nota 4: 0: Quem pegar meu ovo tem que me avisar. / 1: O jogador pegou um ovo ali perto do galinheiro. / 0: Então vou ciscar atrás dele.
  - ⚠ A fala 'Quem pegar meu ovo tem que me avisar.' soa um pouco possessiva e planejada demais para uma galinha, mas ainda é aceitável.
  - ⚠ A resposta 'O jogador pegou um ovo ali perto do galinheiro.' é informativa, mas a galinha não costuma se referir ao jogador como 'jogador' no mundo do jogo; poderia ser 'humano' ou 'aquele bicho'.

### blaze_agua (4.60)  _um blaze e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Uma poça de água me apaga na hora. / 1: Então fica longe das fontes. / 0: Sempre procuro lugar sem umidade.
  - ⚠ A fala 'Uma poça de água me apaga na hora.' soa um pouco informativa demais para uma conversa casual entre blazes, mas ainda é plausível.
  - ⚠ A resposta 'Então fica longe das fontes.' é um conselho óbvio, mas aceitável.
- nota 4: 0: Se eu tocar na água, dói. / 1: Nem tenta apagar fogo com isso. / 0: Melhor evitar qualquer poça.
  - ⚠ A fala 'Nem tenta apagar fogo com isso.' soa um pouco desconexa, pois a primeira fala não menciona apagar fogo, apenas que água dói.

### mooshroom_ilha (4.62)  _uma vaca-cogumelo e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: A relva da ilha tem gosto de cogumelo fresco. / 1: Porque o micélio se espalha por todo o solo. / 0: Vou pastar perto do lago.
  - ⚠ A fala 'Porque o micélio se espalha por todo o solo.' soa mais técnica do que o esperado para uma vaca-cogumelo, mas ainda é plausível no contexto do jogo.
- nota 4: 0: As vacas comem cogumelos o dia todo por aqui. / 1: E ainda sobra para nós, mooshrooms. / 0: Deixa eu pegar aquele cogumelo vermelho.
  - ⚠ A fala 'E ainda sobra para nós, mooshrooms.' soa um pouco estranha, pois a criatura se refere a si mesma na terceira pessoa, o que não é natural em conversas cotidianas.
- nota 4: 0: Os cogumelos vermelhos são os mais saborosos da ilha. / 1: Os marrons são mais comuns, mas os vermelhos têm mais gosto. / 0: Vou atrás de um vermelho agora.
  - ⚠ A fala 'Os cogumelos vermelhos são os mais saborosos da ilha' soa um pouco formal para uma conversa casual entre animais, mas ainda é aceitável.

### raposa_astucia (4.62)  _uma raposa e outra criatura da mesma espécie; o bioma é uma floresta_
- nota 4: 0: Sem fazer barulho, ninguém me percebe. / 1: Eu prendo a respiração ao passar na folhagem seca. / 0: O silêncio é a melhor toca.
  - ⚠ A fala 'O silêncio é a melhor toca.' soa um pouco filosófica para uma raposa, mas ainda aceitável.
- nota 4: 0: Eu sempre dou um jeito de escapar. / 1: Na praia, corro para as pedras e o lobo escorrega. / 0: As ondas apagam meu cheiro.
  - ⚠ A fala 'Na praia, corro para as pedras e o lobo escorrega.' menciona lobo, mas a situação não indica presença de lobo; pode soar descontextualizada.
- nota 4: 0: Hoje enganei um lobo, juro. / 1: Eu o fiz correr em círculos atrás do próprio rabo. / 0: Ele cansou e desistiu de me caçar.
  - ⚠ A fala 'Eu o fiz correr em círculos atrás do próprio rabo' soa um pouco estranha para uma raposa, pois 'próprio rabo' é uma expressão mais humana; talvez 'rabo dele' fosse mais natural.
  - ⚠ A situação é vaga ('uma situação qualquer do dia a dia'), mas as falas se encaixam em um contexto de caça, o que é plausível para raposas.

### aq_perigo (4.67)  _um bicho aquático e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Aquele tridente rachado no chão, é de afogado. / 1: Deve ter sido deixado por um afogado que saiu da água. / 0: Então tem afogados por perto agora.
  - ⚠ A fala 'Aquele tridente rachado no chão, é de afogado.' soa um pouco formal para uma conversa casual entre criaturas aquáticas.
  - ⚠ A resposta 'Deve ter sido deixado por um afogado que saiu da água.' é redundante, pois já se sabe que o tridente é de afogado.
- nota 4: 0: Aquele coral brilhante está atraindo predadores. / 1: Os peixes pequenos se escondem nele, mas os grandes vêm caçar. / 0: Vamos evitar aquele coral brilhante.
  - ⚠ A fala 'Aquele coral brilhante está atraindo predadores.' é um pouco genérica para um bicho aquático, mas aceitável.
  - ⚠ A resposta 'Os peixes pequenos se escondem nele, mas os grandes vêm caçar.' é coerente, mas poderia ser mais natural na fala curta de um mob.

### golfinho_peixe (4.67)  _um golfinho e um peixe; uma situação qualquer do dia a dia_
- nota 4: 0: Fica de olho que eu passo nadando. / 1: Pode vir, eu me escondo atrás do coral. / 0: Do coral você vê meu rastro de bolhas.
  - ⚠ A fala 'Do coral você vê meu rastro de bolhas' é um pouco estranha, pois o peixe escondido atrás do coral teria visão limitada do rastro de bolhas do golfinho.
- nota 4: 0: Eu nado mais rápido do que qualquer um aqui. / 1: Duvido, já vi o atum passar por você. / 0: Aquele atum teve sorte, hoje eu mostro.
  - ⚠ A fala 'Aquele atum teve sorte, hoje eu mostro.' soa um pouco vaga e não fecha a conversa de forma clara, deixando a interação sem um desfecho natural.

### cabra_pulo (4.71)  _uma cabra e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Tem uma pedra nova caída perto do penhasco. / 1: Ela pode estar solta, melhor testar antes. / 0: Vou empurrar com a pata primeiro.
  - ⚠ A fala 'Ela pode estar solta, melhor testar antes.' soa um pouco cautelosa demais para uma cabra, que normalmente age por impulso, mas ainda é plausível.
- nota 4: 0: A pedra grande perto do riacho é meu alvo hoje. / 1: Aquela pedra tem um lado com musgo, evite isso. / 0: Então pulo no lado seco, sem musgo.
  - ⚠ A fala 'A pedra grande perto do riacho é meu alvo hoje.' soa um pouco artificial para uma cabra, pois cabras não costumam planejar alvos de salto em voz alta.

### raposa_frutinha (4.71)  _uma raposa e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Vi um coelho comendo frutinhas na clareira. / 1: Coelho não divide, mas a moita é grande. / 0: Espero ele sair e pego o que sobrar.
  - ⚠ A fala 'Coelho não divide, mas a moita é grande.' soa um pouco estranha, pois raposas não costumam comentar sobre compartilhamento de comida dessa forma.
- nota 4: 0: Frutinha é o meu lanche favorito. / 1: Prefiro frutinha doce à galinha correndo. / 0: Então divide a próxima moita comigo.
  - ⚠ A fala 'Prefiro frutinha doce à galinha correndo.' soa um pouco estranha, pois raposas normalmente preferem galinhas a frutas, mas é aceitável como preferência individual.

### coelho_medo (4.75)  _um coelho e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: O falcão circulou o prado duas vezes. / 1: Falcão? Então não saia da sombra. / 0: Esperamos ele ir embora.
  - ⚠ A fala 'Falcão? Então não saia da sombra.' soa um pouco formal para um coelho, mas é aceitável.
  - ⚠ A fala 'Esperamos ele ir embora.' usa 'ele' em vez de 'ele ir embora' (coloquial), mas é natural.
- nota 4: 0: Cascos de cavalo perto do rio. / 1: Cavalo não caça coelho, mas assusta. / 0: Mesmo assim, vamos para a toca.
  - ⚠ A fala 'Cascos de cavalo perto do rio.' soa mais como uma observação do que uma fala natural de coelho, mas é aceitável.
  - ⚠ A resposta 'Cavalo não caça coelho, mas assusta.' é um pouco analítica demais para um coelho, mas ainda plausível.

### abelha_colmeia (4.86)  _uma abelha e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: As larvas estão com fome. / 1: Vou levar geleia real para elas. / 0: Depois verifico se precisam de mais.
  - ⚠ A fala 'Vou levar geleia real para elas' pode soar estranha, pois geleia real é produzida pelas abelhas operárias, mas não é tipicamente 'levada' como alimento para larvas; normalmente as larvas são a

### curiosidade_fundo (4.86)  _dois aldeões; uma situação qualquer do dia a dia_
- nota 4: 0: O que você acha que tem no fundo da ravina? / 1: Acho que tem minério de ferro e talvez carvão. / 0: Ferro e carvão, mas só se descer com corda.
  - ⚠ A fala 'Ferro e carvão, mas só se descer com corda.' soa um pouco artificial, pois aldeões não costumam falar de descer com corda, mas é aceitável.

### papagaio_selva (4.86)  _um papagaio e outra criatura da mesma espécie; é noite; o bioma é uma selva_
- nota 4: 0: Tem um ninho de arara no alto. / 1: Vi uma arara azul levando gravetos. / 0: Deixa o ninho em paz, é dela.
  - ⚠ A fala 'Deixa o ninho em paz, é dela.' soa um pouco abrupta, como se houvesse uma ameaça implícita não desenvolvida, mas ainda é plausível.

### sniffer_cheiro (4.86)  _um sniffer e outra criatura da mesma espécie; o bioma é um deserto_
- nota 4: 0: Meu nariz está pegando cheiro de semente antiga. / 1: Essa semente é rara, vamos achar. / 0: Sigo seu nariz, você acha primeiro.
  - ⚠ A fala 'Sigo seu nariz, você acha primeiro' soa um pouco estranha, pois o sniffer que falou primeiro é o dono do nariz, então 'seu nariz' deveria ser 'meu nariz' ou 'seu faro'.
