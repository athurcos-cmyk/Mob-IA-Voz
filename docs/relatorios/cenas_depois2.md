# Juiz de IA por família de assunto (conversas ambiente)

2026-11-02 12:29; 17 famílias, 126 cenas, juiz `deepseek-v4-pro`, 39 s. Média geral **3.73**; famílias abaixo de 4.0: **12**.

| Família | cenas | nota |
|---|---|---|
| endermite_perola | 8 | 2.62  REVER |
| vingador_machado | 8 | 3.12  REVER |
| peixe_esconde | 6 | 3.17  REVER |
| ghast_choro | 7 | 3.43  REVER |
| ghast_bolas | 8 | 3.50  REVER |
| magma_pulo | 8 | 3.50  REVER |
| nether_lava | 7 | 3.71  REVER |
| ravager_investida | 7 | 3.71  REVER |
| axolote_morto | 8 | 3.75  REVER |
| aq_noite | 7 | 3.86  REVER |
| allay_musica | 8 | 3.88  REVER |
| papagaio_imitar | 8 | 3.88  REVER |
| guardiao_anciao | 7 | 4.00 |
| cruz_vaca_lobo | 8 | 4.12 |
| allay_amizade | 7 | 4.29 |
| cruz_ovelha_lobo | 7 | 4.43 |
| saqueador_ataque | 7 | 4.57 |

## Problemas apontados (famílias abaixo do mínimo primeiro)

### endermite_perola (2.62)  _um endermite e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 3: 0: Pérola rachada, nasci dela. / 1: A rachadura abriu e eu saí. / 0: Pérola nova, vida nova.
  - ⚠ Fala 0 'Pérola rachada, nasci dela.' soa poética demais para um endermite, que é uma criatura simples e não costuma refletir sobre origem.
  - ⚠ Fala 1 'A rachadura abriu e eu saí.' é redundante e não acrescenta informação nova, tornando a conversa pouco natural.
- nota 3: 0: Enderman passou, pérola caiu. / 1: A pérola bateu no chão e rachou. / 0: Vou atrás da próxima pérola.
  - ⚠ Fala 0: 'Enderman passou, pérola caiu.' soa como uma observação factual, mas endermites não costumam se interessar por pérolas de enderman.
  - ⚠ Fala 1: 'A pérola bateu no chão e rachou.' é uma descrição passiva, mas endermites são agressivas e não ficariam comentando detalhes.
- nota 2: 0: Enderman derrubou pérola no End. / 1: A pérola quicou e parou na pedra. / 0: Pérola no chão, eu saio dela.
  - ⚠ Fala 0: 'Enderman derrubou pérola no End.' soa como narração, não como fala natural de um endermite.
  - ⚠ Fala 1: 'A pérola quicou e parou na pedra.' também é descritiva demais para uma conversa entre criaturas.
- nota 3: 0: Enderman teleportou, deixou pérola. / 1: A pérola caiu quando ele sumiu. / 0: Pérola no chão, eu saio dela.
  - ⚠ Fala 0: 'Enderman teleportou, deixou pérola.' soa como uma observação factual, mas endermites não costumam se referir a endermen dessa forma; seria mais natural algo como 'O alto sumiu, deixou a bolin
  - ⚠ Fala 1: 'A pérola caiu quando ele sumiu.' é redundante com a fala anterior, não acrescenta informação nova.
- nota 2: 0: Pérola quebrada me chama. / 1: A pérola quebrada faz barulho de rachar. / 0: Vou até a pérola quebrada.
  - ⚠ Fala 0: 'Pérola quebrada me chama' soa filosófica demais para um endermite, que é uma criatura simples.
  - ⚠ Fala 1: 'A pérola quebrada faz barulho de rachar' é uma observação óbvia e não responde diretamente ao chamado.

### vingador_machado (3.12)  _um vingador e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Vou abrir clareira na mata fechada. / 1: Leve o machado grande, tem tronco grosso. / 0: Vou pegar o mais afiado da forja.
  - ⚠ Fala 0: 'Vou abrir clareira na mata fechada.' soa um pouco formal para conversa cotidiana, mas aceitável.
  - ⚠ Fala 1: 'Leve o machado grande, tem tronco grosso.' é natural, mas 'tem tronco grosso' poderia ser mais específico.
- nota 1: 0: Aquele carvalho velho vai cair hoje. / 1: Eu prefiro derrubar o pinheiro da colina. / 0: Vamos derrubar os dois antes do anoitecer.
  - ⚠ A fala 'Aquele carvalho velho vai cair hoje.' não combina com o mundo de um vingador, que não derruba árvores.
  - ⚠ A fala 'Eu prefiro derrubar o pinheiro da colina.' sugere uma conversa sobre derrubar árvores, o que não é típico de vingadores.
- nota 3: 0: Nenhuma porta resiste a esse machado. / 1: Quero ver você derrubar a daquela cabana. / 0: Vamos lá agora, antes que escureça.
  - ⚠ Fala 0: 'Nenhuma porta resiste a esse machado.' soa como vanglória, mas não especifica o contexto; pode ser natural para um vingador.
  - ⚠ Fala 1: 'Quero ver você derrubar a daquela cabana.' é um desafio plausível, mas a referência 'daquela cabana' é vaga sem contexto visual.
- nota 4: 0: Esse machado já derrubou três árvores hoje. / 1: Só três? O meu derrubou cinco. / 0: Então me empresta o seu para eu testar.
  - ⚠ A fala 'Só três? O meu derrubou cinco.' soa competitiva demais para uma conversa casual entre vingadores, mas ainda é plausível.
  - ⚠ A fala 'Então me empresta o seu para eu testar.' é um fecho razoável, mas poderia ser mais natural se houvesse uma reação à resposta.
- nota 1: 0: A lâmina está sedenta por madeira. / 1: Vamos saciá-la naquele carvalho velho. / 0: Depois a gente afia de novo.
  - ⚠ A fala 'A lâmina está sedenta por madeira' não combina com o mundo de um vingador, que é um mob hostil focado em vingança, não em cortar madeira.
  - ⚠ A resposta 'Vamos saciá-la naquele carvalho velho' continua o tema de corte de madeira, o que é incoerente com a natureza de um vingador.

### peixe_esconde (3.17)  _um peixe e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 3: 0: Achei uma fenda na pedra grande. / 1: Essa fenda é estreita pra passar. / 0: Vou me espremer na fenda.
  - ⚠ Fala 0: 'Achei uma fenda na pedra grande.' soa genérica e não reflete o mundo aquático de um peixe.
  - ⚠ Fala 1: 'Essa fenda é estreita pra passar.' é uma resposta plausível, mas não acrescenta contexto específico de peixe.
- nota 2: 0: A sombra da rocha esconde bem. / 1: A sombra da rocha é grande. / 0: Vou ficar na sombra da rocha.
  - ⚠ A fala 'A sombra da rocha esconde bem.' é vaga e não combina com o mundo de um peixe, que não se esconde em sombras de rochas.
  - ⚠ A resposta 'A sombra da rocha é grande.' não acrescenta nada e soa repetitiva.
- nota 4: 0: Achei um buraco entre as pedras. / 1: Esse buraco tem espaço pra gente. / 0: Vou entrar nele agora.
  - ⚠ A fala 'Vou entrar nele agora.' soa um pouco abrupta como fecho, mas é aceitável para peixes.
- nota 4: 0: Tem um canto escuro atrás do coral. / 1: Esse canto escuro esconde bem. / 0: Vou ficar atrás do coral.
  - ⚠ A fala 'Vou ficar atrás do coral.' não fecha claramente a conversa, soa mais como uma continuação.
- nota 3: 0: Me escondi no meio do cardume. / 1: O cardume se move junto com você. / 0: Assim ninguém me acha.
  - ⚠ A fala 'O cardume se move junto com você.' soa como uma observação externa, não como uma resposta natural de um peixe.
  - ⚠ O fecho 'Assim ninguém me acha.' não conecta claramente com a resposta anterior.

### ghast_choro (3.43)  _um ghast e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 3: 0: O jogador construiu uma ponte sobre o lago de lava. / 1: Ela é feita de pedra do Nether. / 0: Vou queimar a ponte dele.
  - ⚠ A fala 'Vou queimar a ponte dele' não combina com o mundo de um ghast, pois ghasts não queimam pontes de pedra do Nether, que são resistentes ao fogo.
  - ⚠ A resposta 'Ela é feita de pedra do Nether' não responde diretamente à fala anterior, que apenas constata a construção da ponte, sem perguntar sobre o material.
- nota 4: 0: Meu grito assustou aquele esqueleto. / 1: Ele correu para trás da fortaleza. / 0: Vou persegui-lo com fogo.
  - ⚠ A fala 'Vou persegui-lo com fogo' soa um pouco vingativa demais para um ghast, que normalmente apenas atira bolas de fogo sem intenção pessoal.
- nota 4: 0: O jogador está minerando quartzo. / 1: Ele não viu a gente ainda. / 0: Vou gritar para assustar ele.
  - ⚠ A fala 'O jogador está minerando quartzo.' soa mais como uma observação neutra do que algo que um ghast diria naturalmente; ghasts são hostis e dramáticos, então espera-se algo mais ameaçador ou emoci
  - ⚠ A fala 'Vou gritar para assustar ele.' é um pouco redundante, pois ghasts já gritam naturalmente; poderia ser mais espontânea, como 'Vou gritar!' sem explicar o motivo.
- nota 3: 0: Minha bola de fogo atingiu o teto. / 1: Caiu um pedaço de netherrack. / 0: Vou atirar de novo no teto.
  - ⚠ A fala 'Vou atirar de novo no teto' soa repetitiva e não fecha a conversa de forma natural.
  - ⚠ A resposta 'Caiu um pedaço de netherrack' é muito literal e não adiciona contexto ou reação à fala anterior.
- nota 3: 0: Aquele bloco de pedra negra quebrou com meu fogo. / 1: Caiu um pedaço de basalto. / 0: Vou atirar de novo naquela parte.
  - ⚠ A fala 'Caiu um pedaço de basalto.' não responde diretamente à fala anterior sobre o bloco quebrado.
  - ⚠ A fala 'Vou atirar de novo naquela parte.' não fecha a conversa de forma conclusiva.

### ghast_bolas (3.50)  _um ghast e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Minha bola de fogo acertou o chão de basalto. / 1: O basalto rachou com a explosão. / 0: Quase cai no lago de lava.
  - ⚠ A fala 'Quase cai no lago de lava.' não fecha claramente a conversa, soa mais como um comentário adicional do que um fecho.
- nota 2: 0: Soltei uma bola de fogo e acertei longe. / 1: A explosão abriu um buraco no terreno. / 0: Agora tem um buraco na caverna.
  - ⚠ Fala 0: 'Soltei uma bola de fogo e acertei longe.' soa como um relato técnico, não como fala natural de um ghast.
  - ⚠ Fala 1: 'A explosão abriu um buraco no terreno.' é redundante e não responde diretamente à primeira fala.
- nota 3: 0: O humano rebateu minha bola de fogo. / 1: A bola voltou voando para mim. / 0: Me acertou em cheio e doeu.
  - ⚠ A fala 'A bola voltou voando para mim' soa redundante e não avança a conversa, pois já foi dito que o humano rebateu a bola.
  - ⚠ O fecho 'Me acertou em cheio e doeu' é previsível e não adiciona humor ou surpresa, tornando a conversa pouco interessante.
- nota 4: 0: Vi um humano com uma espada de ferro. / 1: Ele tentou acertar minha bola de fogo. / 0: A bola acertou o bloco e destruiu.
  - ⚠ A fala 'Ele tentou acertar minha bola de fogo' soa um pouco estranha, pois ghasts não costumam se referir às suas bolas de fogo como 'minha bola de fogo' de forma possessiva; seria mais natural algo c
  - ⚠ A última fala 'A bola acertou o bloco e destruiu' é um pouco vaga e não fecha a conversa de forma natural; falta um elemento de reação ou conclusão.
- nota 4: 0: Errei a mira e a bola explodiu na parede. / 1: A parede de netherrack desmoronou. / 0: Caiu poeira e pedras em mim.
  - ⚠ A fala 'Caiu poeira e pedras em mim.' soa um pouco redundante após a resposta anterior, mas ainda é plausível como fecho.

### magma_pulo (3.50)  _um cubo de magma e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 3: 0: Pulei e o chão rangeu embaixo de mim. / 1: O chão range porque está rachado. / 0: Então vou pular até ele rachar mais.
  - ⚠ A fala 'Pulei e o chão rangeu embaixo de mim' soa mais como uma constatação humana do que algo que um cubo de magma diria naturalmente.
  - ⚠ A resposta 'O chão range porque está rachado' é muito analítica para uma criatura simples como um cubo de magma.
- nota 4: 0: Quando pulo, o chão treme embaixo de mim. / 1: Sinto o tremor até aqui, o chão todo balança. / 0: Balança porque meu pulo é forte.
  - ⚠ A fala 'Balança porque meu pulo é forte.' soa um pouco repetitiva e não acrescenta muito à conversa, mas ainda é aceitável.
- nota 4: 0: Pulei e a lava espirrou no meu lado. / 1: A lava sempre espirra quando caímos perto dela. / 0: Vou pular de novo pra espirrar mais.
  - ⚠ A fala 'Vou pular de novo pra espirrar mais' soa um pouco infantil, mas é aceitável para cubos de magma.
- nota 3: 0: Meu pulo espalhou cinzas pelo ar. / 1: As cinzas sobem quando você cai com força. / 0: Vou pular de novo pra espalhar mais.
  - ⚠ Fala 0: 'Meu pulo espalhou cinzas pelo ar.' - Cubos de magma não produzem cinzas; eles são criaturas de lava, então seria mais plausível espalhar fogo ou partículas de magma.
  - ⚠ Fala 1: 'As cinzas sobem quando você cai com força.' - Reforça a inconsistência das cinzas, que não fazem parte do ambiente natural de cubos de magma.
- nota 4: 0: Meu pulo rachou o chão de basalto. / 1: Rachou mesmo, dá pra ver a fenda. / 0: Então vou pular até abrir mais fendas.
  - ⚠ A fala 'Meu pulo rachou o chão de basalto' soa um pouco formal para um cubo de magma, que normalmente tem fala simples e repetitiva.
  - ⚠ A resposta 'Rachou mesmo, dá pra ver a fenda' é natural, mas o fecho 'Então vou pular até abrir mais fendas' parece mais uma decisão lógica do que uma reação instintiva de um cubo de magma.

### nether_lava (3.71)  _uma criatura do Nether e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: O calor está mais forte perto do lago de lava. / 1: A lava está mais quente que o normal. / 0: Vamos para o outro lado da caverna.
  - ⚠ A resposta 'A lava está mais quente que o normal.' é redundante com a fala inicial, não acrescenta informação nova.
  - ⚠ O fecho 'Vamos para o outro lado da caverna.' é uma reação plausível, mas poderia ser mais natural se expressasse urgência ou motivo.
- nota 4: 0: O lago de lava borbulha sem parar. / 1: A lava quer engolir o Nether inteiro. / 0: Não pisa perto do lago.
  - ⚠ A fala 'A lava quer engolir o Nether inteiro.' soa um pouco filosófica para criaturas do Nether, que normalmente têm falas mais diretas e instintivas.
- nota 3: 0: A lava cospe fogo na pedra negra. / 1: A pedra vai rachar e cair no lago. / 0: Melhor ficar longe da beirada.
  - ⚠ Fala 0: 'A lava cospe fogo na pedra negra.' soa mais como uma observação do que uma fala natural entre criaturas do Nether.
  - ⚠ Fala 1: 'A pedra vai rachar e cair no lago.' é uma previsão plausível, mas falta um tom de conversa.
- nota 4: 0: O brilho da lava está mais intenso hoje. / 1: Ela vai cobrir a pedra negra toda. / 0: Fica longe da margem.
  - ⚠ A fala 'Ela vai cobrir a pedra negra toda.' é um pouco vaga, mas aceitável no contexto do Nether.
- nota 4: 0: O lago de lava subiu de novo. / 1: Vai engolir a pedra negra da margem. / 0: Fica longe da beirada.
  - ⚠ Falta contexto sobre a espécie das criaturas, mas as falas são plausíveis para criaturas do Nether.
  - ⚠ A segunda fala ('Vai engolir a pedra negra da margem.') é uma observação que não responde diretamente à primeira, mas ainda assim é coerente.

### ravager_investida (3.71)  _um devastador e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: Quando eu corro, tudo cai. / 1: Até os postes de cerca da aldeia. / 0: A investida continua.
  - ⚠ A fala 'A investida continua.' soa um pouco vaga e não fecha claramente a conversa, podendo ser interpretada como uma continuação em vez de um fecho.
- nota 3: 0: Os saqueadores confiam em mim. / 1: Eles me soltam na frente da muralha. / 0: Vamos derrubar.
  - ⚠ Fala 0 'Os saqueadores confiam em mim.' soa estranha para um devastador, que é uma criatura bestial e não fala sobre confiança.
  - ⚠ Fala 1 'Eles me soltam na frente da muralha.' é uma descrição factual, mas não parece uma resposta natural à fala anterior.
- nota 4: 0: Eu sempre vou na frente. / 1: Na frente da horda, abrindo caminho. / 0: Nada nos para.
  - ⚠ A fala 'Nada nos para.' soa um pouco genérica, mas ainda combina com a natureza agressiva do devastador.
- nota 3: 0: Minha investida não tem freio. / 1: Nem quando bato em pedra do Nether. / 0: Mais força.
  - ⚠ A fala 'Minha investida não tem freio.' soa um pouco filosófica para um devastador, que é uma criatura bruta.
  - ⚠ A resposta 'Nem quando bato em pedra do Nether.' é coerente, mas a menção a 'pedra do Nether' pode ser específica demais para uma conversa casual.
- nota 4: 0: Nada resiste à minha cabeçada. / 1: Só o escudo do o humano me atrasa. / 0: Eu derrubo muros.
  - ⚠ A fala 'Só o escudo do o humano me atrasa' contém um erro gramatical ('do o humano'), soando pouco natural.
  - ⚠ A resposta 'Só o escudo do o humano me atrasa' não se conecta diretamente com a fala anterior 'Nada resiste à minha cabeçada', pois introduz uma exceção sem transição clara.

### axolote_morto (3.75)  _um axolote e outra criatura da mesma espécie; o bioma é um pântano_
- nota 4: 0: Vi um peixe morto na água rasa do pântano. / 1: No pântano, peixe morto apodrece rápido. / 0: Vou sair daqui antes que fique ruim.
  - ⚠ A fala 'Vou sair daqui antes que fique ruim' é um pouco vaga, mas aceitável.
- nota 4: 0: Um peixe boiou de noite, na lua cheia. / 1: De noite, boiar assim chama predador noturno. / 0: Vou evitar boiar quando a lua estiver cheia.
  - ⚠ A fala 'Um peixe boiou de noite, na lua cheia.' soa como uma observação genérica, não como uma reação natural de um axolote a um acontecimento específico.
  - ⚠ A resposta 'De noite, boiar assim chama predador noturno.' é um pouco formal para uma conversa entre axolotes, mas ainda aceitável.
- nota 4: 0: O peixe se fingiu de morto na correnteza. / 1: É defesa quando a água agita, ele esperou passar. / 0: Depois da correnteza, ele nadou embora.
  - ⚠ A fala 'O peixe se fingiu de morto na correnteza.' soa um pouco observacional demais para um axolote, que normalmente não comenta sobre peixes em tom de relato.
  - ⚠ A resposta 'É defesa quando a água agita, ele esperou passar.' é um pouco explicativa e filosófica para um axolote, que tende a ser mais simples.
- nota 3: 0: A água agitou e o peixe virou de lado. / 1: Na correnteza, o truque funciona, ele sumiu depois. / 0: Vou tentar isso se a água agitar.
  - ⚠ A fala 'A água agitou e o peixe virou de lado.' soa como uma observação factual, mas não fica claro se é uma reação natural de um axolote.
  - ⚠ A resposta 'Na correnteza, o truque funciona, ele sumiu depois.' é vaga e não se conecta claramente com a fala anterior.
- nota 3: 0: Teve um que boiou e nem se mexeu. / 1: Boiar assim espanta os curiosos, ninguém chega perto. / 0: Se eu boiar, os outros fogem de mim.
  - ⚠ A fala 'Boiar assim espanta os curiosos, ninguém chega perto.' soa estranha para axolotes, que não têm comportamento de espantar curiosos.
  - ⚠ A fala 'Se eu boiar, os outros fogem de mim.' não se conecta claramente com a resposta anterior, deixando a conversa sem fechamento natural.

### aq_noite (3.86)  _um bicho aquático e outra criatura da mesma espécie; é noite_
- nota 3: 0: Tem uma tartaruga subindo para respirar. / 1: Ela volta logo para o fundo. / 0: Vou ficar perto das plantas para me esconder.
  - ⚠ A fala 'Tem uma tartaruga subindo para respirar.' soa como uma observação neutra, mas não combina com o contexto de ser noite e com a necessidade de se esconder.
  - ⚠ A resposta 'Ela volta logo para o fundo.' não se conecta diretamente com a fala anterior, pois não há relação clara entre a tartaruga e a ação de se esconder.
- nota 4: 0: Dá para ouvir os peixes se mexendo no escuro. / 1: Eles raspam nas pedras procurando resto de comida. / 0: Também vou procurar algo para comer.
  - ⚠ A fala 'Dá para ouvir os peixes se mexendo no escuro.' é um pouco genérica e não especifica o tipo de bicho aquático, mas ainda é plausível.
  - ⚠ A resposta 'Eles raspam nas pedras procurando resto de comida.' é natural, mas poderia ser mais específica para a espécie.
- nota 3: 0: A água fria da noite me deixa mais lento. / 1: Por isso os peixes pequenos se juntam em cardumes. / 0: Vou procurar um cardume para me aquecer.
  - ⚠ A fala 'Vou procurar um cardume para me aquecer' não faz sentido, pois cardumes não aquecem; a motivação de se juntar a um cardume é proteção, não calor.
  - ⚠ A resposta 'Por isso os peixes pequenos se juntam em cardumes' não conecta claramente com a lentidão causada pela água fria; a relação entre lentidão e cardume é forçada.
- nota 4: 0: As sombras no fundo ficam maiores de noite. / 1: Elas escondem os predadores que caçam agora. / 0: Vou ficar perto das plantas para me esconder.
  - ⚠ A fala 'Vou ficar perto das plantas para me esconder' é um pouco genérica; poderia ser mais específica do ambiente aquático (ex: 'Vou ficar entre as algas').
- nota 4: 0: A noite deixa a água bem escura. / 1: Mal consigo ver as algas perto de mim. / 0: Vou usar o cheiro para achar comida.
  - ⚠ A fala 'Vou usar o cheiro para achar comida' pode soar estranha para um bicho aquático, pois muitos não usam olfato na água, mas é plausível para alguns.

### allay_musica (3.88)  _um allay e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 4: 0: A jukebox tocou uma melodia suave. / 1: Eu flutuei alto seguindo a melodia. / 0: Vou flutuar até a música acabar.
  - ⚠ A fala 'Eu flutuei alto seguindo a melodia.' soa um pouco estranha, pois allays normalmente flutuam o tempo todo, então 'flutuei alto' não é uma ação notável.
  - ⚠ A fala 'Vou flutuar até a música acabar.' é redundante, pois allays já são atraídos por música e flutuam naturalmente.
- nota 4: 0: O toque de nota me fez brilhar no escuro. / 1: Meu brilho iluminou o caminho da caverna. / 0: Vou brilhar mais ao som da próxima nota.
  - ⚠ A fala 'O toque de nota me fez brilhar no escuro.' é um pouco vaga, pois não especifica o contexto do toque de nota, mas ainda faz sentido para um allay.
  - ⚠ A resposta 'Meu brilho iluminou o caminho da caverna.' é coerente, mas poderia ser mais natural se mencionasse a caverna antes, como 'Na caverna, meu brilho iluminou o caminho.'
- nota 4: 0: O disco girou e eu dancei ao lado. / 1: O ritmo do disco acelerou meus passos. / 0: Vou girar até o disco parar.
  - ⚠ Fala 1: 'O ritmo do disco acelerou meus passos.' soa um pouco formal para uma conversa casual entre allays, que costumam ser brincalhões e simples.
- nota 3: 0: O sino tocou e eu fiquei leve. / 1: A leveza me levou até o topo da torre. / 0: Vou descer girando quando o sino tocar de novo.
  - ⚠ A fala 'O sino tocou e eu fiquei leve.' não deixa claro o contexto do sino, mas é aceitável.
  - ⚠ A resposta 'A leveza me levou até o topo da torre.' é coerente, mas a conexão com o sino poderia ser mais explícita.
- nota 3: 0: A flauta soou e eu voei até a fonte. / 1: A água da fonte espirrou quando cheguei. / 0: Agora vou seguir o som da flauta.
  - ⚠ A fala 'A água da fonte espirrou quando cheguei.' não responde diretamente à fala anterior, apenas descreve um evento paralelo.
  - ⚠ A fala 'Agora vou seguir o som da flauta.' não fecha a conversa de forma natural, pois não há conexão clara com a fala anterior.

### papagaio_imitar (3.88)  _um papagaio e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 3: 0: O som do Enderman é difícil, mas consegui. / 1: Aquele som distorcido é complicado mesmo. / 0: Vou treinar até ficar igual ao original.
  - ⚠ Papagaios imitam sons, mas falar sobre 'treinar até ficar igual ao original' soa mais como um humano do que como um papagaio.
  - ⚠ A conversa é genérica e não aproveita características específicas de papagaios, como imitar outros mobs ou sons do ambiente.
- nota 4: 0: Ouvi um afogado e imitei o gorgolejo. / 1: Aquele som parece bolhas na água. / 0: Vou imitar perto do rio para ver se aparece.
  - ⚠ A fala 'Ouvi um afogado e imitei o gorgolejo' é um pouco ambígua: 'afogado' pode ser entendido como um zumbi afogado, mas não fica claro se o papagaio imitou o som do afogado ou o gorgolejo da água.
  - ⚠ A resposta 'Aquele som parece bolhas na água' não conecta diretamente com a imitação do gorgolejo, pois bolhas são diferentes de gorgolejo.
- nota 4: 0: Imitei o zumbi e assustei um morcego. / 1: O morcego voou para longe rapidinho. / 0: Vou imitar de novo para ver se volta.
  - ⚠ A fala 'Vou imitar de novo para ver se volta.' soa um pouco repetitiva, mas é aceitável para um papagaio.
- nota 4: 0: Ouvi um creeper chiando e aprendi o som. / 1: Faz de novo, quero ouvir o chiado. / 0: Vou fazer e você tenta acompanhar.
  - ⚠ A fala 'Ouvi um creeper chiando e aprendi o som.' soa um pouco artificial para um papagaio, que normalmente imita sons de forma mais direta, mas é aceitável.
  - ⚠ A resposta 'Faz de novo, quero ouvir o chiado.' é natural, mas o fecho 'Vou fazer e você tenta acompanhar.' poderia ser mais curto e direto.
- nota 3: 0: Ouvi um lobo uivando e imitei na hora. / 1: O uivo do lobo ecoa pela mata. / 0: Vou uivar de novo para atrair a matilha.
  - ⚠ A fala 'O uivo do lobo ecoa pela mata.' soa mais como narração do que como resposta natural de um papagaio.
  - ⚠ A fala 'Vou uivar de novo para atrair a matilha.' sugere intenção complexa, incomum para um papagaio.

### guardiao_anciao (4.00)  _um guardião ancião e outra criatura da mesma espécie; uma situação qualquer do dia a dia_
- nota 3: 0: O laser do meu olho atravessa qualquer armadura. / 1: Até a armadura de netherite derrete com o calor. / 0: Nenhum escudo protege contra isso.
  - ⚠ A fala 'Até a armadura de netherite derrete com o calor' não combina com o mundo de um guardião ancião, pois netherite é um item do Nether e guardiões anciões vivem em monumentos oceânicos, sem relaçã
  - ⚠ A conversa parece mais uma discussão técnica sobre mecânicas de jogo do que uma interação natural entre criaturas do mesmo tipo.
- nota 4: 0: O peixe prateado esconde-se nas fendas do monumento. / 1: Ele morde os pés dos invasores desavisados. / 0: Vou soltar mais peixes prateados nas fendas.
  - ⚠ A fala 'Vou soltar mais peixes prateados nas fendas' soa um pouco artificial, pois guardiões anciões não controlam peixes prateados diretamente; eles são mobs hostis independentes.
- nota 4: 0: As esponjas absorvem a água das câmaras. / 1: Elas secam as salas para revelar os invasores. / 0: Vou verificar as esponjas antes da próxima maré.
  - ⚠ A fala 'Elas secam as salas para revelar os invasores' soa um pouco formal para uma conversa casual entre criaturas, mas ainda é aceitável.
- nota 4: 0: Os afogados nadam ao redor do monumento. / 1: Eles atacam qualquer um que se aproxima. / 0: Vou usar os afogados como primeira defesa.
  - ⚠ A fala 'Vou usar os afogados como primeira defesa' soa um pouco estratégica demais para um guardião ancião, que normalmente age por instinto, mas ainda é plausível.
- nota 3: 0: Os invasores acordam os guardiões adormecidos. / 1: Eles não sabem que os guardiões atacam em grupo. / 0: Vou liderar o ataque quando eles chegarem.
  - ⚠ Fala 0: 'Os invasores acordam os guardiões adormecidos.' soa como narração, não como fala natural entre criaturas.
  - ⚠ Fala 1: 'Eles não sabem que os guardiões atacam em grupo.' é uma observação tática, mas não responde diretamente à fala anterior.

### cruz_vaca_lobo (4.12)  _uma vaca e um lobo; uma situação qualquer do dia a dia_
- nota 4: 0: Seu cheiro está assustando as vacas, lobo. / 1: Não posso controlar meu cheiro, mas não vou caçar aqui. / 0: Então fique longe do rebanho.
  - ⚠ A fala 'Seu cheiro está assustando as vacas, lobo.' soa um pouco formal para uma vaca, mas é aceitável.
  - ⚠ A resposta do lobo 'Não posso controlar meu cheiro, mas não vou caçar aqui.' é coerente, mas poderia ser mais curta.
- nota 4: 0: Seus olhos estão no bezerro, lobo. / 1: Só observando, não vou atacar. / 0: Melhor você ir para outro lugar.
  - ⚠ A fala da vaca 'Seus olhos estão no bezerro, lobo.' soa um pouco formal para uma vaca, mas ainda aceitável.
  - ⚠ A resposta do lobo 'Só observando, não vou atacar.' é direta, mas poderia ser mais natural para um lobo em português falado.
- nota 4: 0: Lobo, você está olhando para o rebanho faminto? / 1: Acabei de comer, não estou com fome. / 0: Então não fique encarando as vacas.
  - ⚠ A fala 'Lobo, você está olhando para o rebanho faminto?' soa um pouco estranha, pois 'rebanho faminto' não é uma expressão natural para uma vaca se referir ao próprio grupo.
  - ⚠ A resposta do lobo 'Acabei de comer, não estou com fome.' é plausível, mas não responde diretamente à pergunta sobre estar olhando para o rebanho.
- nota 4: 0: Lobo, por que você está perto do pasto? / 1: Caçando coelhos, não vacas. / 0: Então continue longe do rebanho.
  - ⚠ A fala da vaca 'Lobo, por que você está perto do pasto?' soa um pouco formal para uma conversa casual entre animais, mas ainda aceitável.
  - ⚠ A resposta do lobo 'Caçando coelhos, não vacas.' é direta e combina com o mundo do lobo, mas poderia ser mais natural se fosse mais curta, como 'Só caçando coelhos.'
- nota 4: 0: Tem lobo por perto, fica de olho. / 1: Não se preocupe, vaca, não como carne de vaca. / 0: Mesmo assim, mantenha distância.
  - ⚠ A fala do lobo 'não como carne de vaca' é estranha, pois lobos no Minecraft atacam vacas, então a resposta não condiz com o comportamento real do mob.

### allay_amizade (4.29)  _um allay e outra criatura da mesma espécie; o bioma é uma floresta_
- nota 4: 0: Encontrei um graveto no chão da floresta. / 1: Graveto é útil, o humano sempre pega. / 0: Vou procurar mais gravetos.
  - ⚠ A fala 'Graveto é útil, o humano sempre pega.' soa um pouco estranha para um allay, pois allays são atraídos por itens específicos e não costumam comentar sobre o comportamento humano de pegar graveto
- nota 4: 0: Achei uma concha na praia. / 1: Concha é bonita, o humano gosta. / 0: Vou dar para ele quando voltar.
  - ⚠ A fala 'Concha é bonita, o humano gosta.' soa um pouco genérica, mas ainda é plausível para um allay, que é uma criatura amigável e colecionadora.
- nota 4: 0: Achei uma pena perto do rio. / 1: Pena é leve, boa para carregar. / 0: Vou entregar ao humano depois.
  - ⚠ A fala 'Vou entregar ao humano depois.' soa um pouco abrupta como fecho, mas é aceitável para um allay.
- nota 4: 0: O humano me entregou uma flor vermelha. / 1: Flor vermelha, eu também já coletei uma. / 0: Vou deixar perto do baú.
  - ⚠ A fala 'Vou deixar perto do baú' sugere que o allay entende o conceito de baú, o que é plausível, mas não é uma ação típica de allays, que normalmente entregam itens ao jogador ou a blocos de nota, nã
- nota 4: 0: O humano me deu um pedaço de ferro. / 1: Ferro é pesado, mas vale a pena. / 0: Vou continuar seguindo o humano.
  - ⚠ A fala 'Ferro é pesado, mas vale a pena.' soa um pouco filosófica para um allay, que normalmente tem falas simples e ligadas a coletar itens.

### cruz_ovelha_lobo (4.43)  _uma ovelha e um lobo; é noite_
- nota 4: 0: A neve cobre a grama, não acho comida. / 1: Vou descer a montanha para caçar. / 0: Então não vou ter que me preocupar com você.
  - ⚠ A fala da ovelha 'A neve cobre a grama, não acho comida.' é plausível, mas a resposta do lobo 'Vou descer a montanha para caçar.' não responde diretamente à reclamação da ovelha, apenas muda de assunt
  - ⚠ O fecho 'Então não vou ter que me preocupar com você.' é um alívio para a ovelha, mas não fecha a conversa de forma natural, pois o lobo não reage a isso.
- nota 4: 0: Méé, tem lobo perto do rebanho! / 1: Não se preocupe, não vou atacar hoje. / 0: Hoje? E o que garante amanhã?
  - ⚠ A fala do lobo 'Não se preocupe, não vou atacar hoje' soa estranha para um lobo, que normalmente não tranquilizaria uma ovelha, mas é aceitável em um contexto de jogo onde mobs falam.
- nota 4: 0: O mato está seco, cuidado com fogo. / 1: Não vou acender nada, só procuro sombra. / 0: A sombra daquela árvore serve para nós dois.
  - ⚠ A fala do lobo 'Não vou acender nada, só procuro sombra' soa um pouco estranha, pois lobos não costumam acender fogo, mas é aceitável como resposta à preocupação da ovelha.
  - ⚠ A fala final 'A sombra daquela árvore serve para nós dois' é amigável demais para um lobo, que normalmente seria hostil a uma ovelha, mas pode ser interpretada como uma trégua momentânea.
- nota 4: 0: Lobo, estou de olho em você. / 1: Pode vigiar, não vou fazer nada. / 0: Melhor assim, mas não confio.
  - ⚠ A fala da ovelha 'Lobo, estou de olho em você.' soa um pouco direta demais para uma ovelha, que normalmente fugiria em vez de confrontar, mas é aceitável no contexto do jogo.

### saqueador_ataque (4.57)  _um saqueador e outra criatura da mesma espécie; o bioma é de neve e frio_
- nota 4: 0: O capitão ergueu a bandeira no alto da colina. / 1: A bandeira mostra que vamos atacar juntos. / 0: Então sigam a bandeira sem hesitar.
  - ⚠ A fala 'A bandeira mostra que vamos atacar juntos.' soa um pouco formal para um saqueador, mas ainda é aceitável.
  - ⚠ A fala 'Então sigam a bandeira sem hesitar.' parece uma ordem de líder, mas não fica claro se o falante é o capitão ou um subordinado.
- nota 4: 0: A muralha da aldeia tem uma brecha perto do portão. / 1: Vou mirar na brecha com minha besta. / 0: Então avance quando eu der o sinal.
  - ⚠ A fala 'Vou mirar na brecha com minha besta.' soa um pouco estranha, pois 'mirar na brecha' não é uma expressão natural; seria mais natural 'mirar na abertura' ou 'atirar pela brecha'.
- nota 4: 0: Os machados estão afiados para a madeira das casas. / 1: Cada machado vai rachar uma porta. / 0: Então que comece a rachadura.
  - ⚠ A fala 'Os machados estão afiados para a madeira das casas.' soa um pouco formal para um saqueador, mas ainda aceitável.
  - ⚠ A resposta 'Cada machado vai rachar uma porta.' é coerente, mas 'rachar' pode ser interpretado como gíria regional, o que não é um problema grave.
