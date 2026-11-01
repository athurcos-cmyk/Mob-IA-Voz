# Juiz de IA por família de assunto (conversas ambiente)

2026-11-01 03:23; 35 famílias, 209 cenas, juiz `deepseek-v4-pro`, 48 s. Média geral **4.01**; famílias abaixo de 4.0: **13**.

| Família | cenas | nota |
|---|---|---|
| ev_devolveu_bicho | 4 | 3.00  REVER |
| ev_presente_indiferente_golem | 3 | 3.33  REVER |
| ev_presente_odeia_golem | 3 | 3.33  REVER |
| ev_devolveu_golem | 4 | 3.50  REVER |
| ev_volta_ameacou | 4 | 3.50  REVER |
| ev_presente_indiferente_bicho | 5 | 3.60  REVER |
| jogador_companheiro | 8 | 3.62  REVER |
| jogador_nadando | 8 | 3.62  REVER |
| ev_presente_gosta_golem | 4 | 3.75  REVER |
| ev_boom_golem | 5 | 3.80  REVER |
| ev_presente_indiferente_vila | 5 | 3.80  REVER |
| ev_presente_odeia_bicho | 5 | 3.80  REVER |
| ev_volta | 7 | 3.86  REVER |
| bioma_selva | 8 | 4.00 |
| ev_obrigado_golem | 4 | 4.00 |
| ev_ovo | 7 | 4.00 |
| ev_perigo | 7 | 4.00 |
| ev_queda_bicho | 8 | 4.00 |
| ev_raio_golem | 4 | 4.00 |
| ev_saque_golem | 4 | 4.00 |
| ev_volta_traiu | 4 | 4.00 |
| jogador_montado | 8 | 4.00 |
| ev_queda_vila | 8 | 4.12 |
| jogador_pegando_fogo | 8 | 4.12 |
| ev_presente_gosta_vila | 7 | 4.14 |
| ev_presente_gosta_bicho | 6 | 4.17 |
| ev_presente_odeia_vila | 5 | 4.20 |
| ev_boom_bicho | 8 | 4.25 |
| ev_devolveu | 4 | 4.25 |
| ev_volta_acalmou | 4 | 4.25 |
| jogador_seguidores | 8 | 4.25 |
| bioma_savana | 8 | 4.38 |
| bioma_montanha | 8 | 4.50 |
| jogador_diamantes | 8 | 4.50 |
| jogador_voando | 8 | 4.62 |

## Problemas apontados (famílias abaixo do mínimo primeiro)

### ev_devolveu_bicho (3.00)  _dois animais da fazenda; o jogador ACABOU DE DEVOLVER, sem ser pedido, algo que a criatura lhe entregara_
- nota 3: 0: Devolveu sem eu pedir. Agora eu confio mais em você.
  - ⚠ A fala é genérica e não especifica qual animal da fazenda está falando, o que pode soar estranho dependendo da criatura (ex: uma galinha falando isso).
  - ⚠ A situação é específica, mas a fala não menciona o objeto devolvido, o que a torna menos natural.
- nota 3: 0: Você devolveu 3 esmeraldas? Eu vou lembrar disso!
  - ⚠ A fala menciona 'esmeraldas', que não é um item típico de animais de fazenda, soando deslocada para o mundo deles.
  - ⚠ A reação parece mais adequada a um aldeão do que a um animal de fazenda, quebrando a imersão.
- nota 3: 0: Gostei disso, humano. Honestidade é rara.
  - ⚠ A fala 'Gostei disso, humano. Honestidade é rara.' soa filosófica demais para um animal de fazenda, que normalmente teria uma reação mais simples e instintiva.
  - ⚠ A criatura não especifica o que foi devolvido, o que torna a fala um pouco vaga para a situação.
- nota 3: 0: Poucos devolveriam 3 esmeraldas. Você é de confiança.
  - ⚠ A fala 'Poucos devolveriam 3 esmeraldas. Você é de confiança.' soa mais como um aldeão do que como um animal de fazenda, pois menciona esmeraldas, que são moeda de troca típica de aldeões, não de anim

### ev_presente_indiferente_golem (3.33)  _um golem de ferro e outra criatura da mesma espécie; o jogador ACABOU DE DAR um presente que não diz nada à criatura_
- nota 4: 0: Pedra? Não é bem o que eu preciso, mas agradeço.
  - ⚠ A fala soa um pouco formal demais para um golem de ferro, que costuma ser mais direto e bruto.
- nota 3: 0: Você pensou em mim, e isso conta.
  - ⚠ A fala 'Você pensou em mim, e isso conta.' soa filosófica demais para um golem de ferro, que normalmente tem falas simples e protetoras.
- nota 3: 0: Hm. Não sei para que serve, mas aceito.
  - ⚠ Golem de ferro não costuma expressar incerteza ou aceitar presentes de forma tão passiva; sua fala soa mais humana do que a de um golem.

### ev_presente_odeia_golem (3.33)  _um golem de ferro e outra criatura da mesma espécie; o jogador ACABOU DE DAR um presente que a criatura DETESTA_
- nota 3: 0: Não, não! Isso me incomoda. Guarde com você.
  - ⚠ A fala 'Não, não! Isso me incomoda. Guarde com você.' é genérica e não reflete a natureza de um golem de ferro, que normalmente é protetor e não expressa desgosto por presentes de forma tão direta.
  - ⚠ A situação indica que o presente foi detestado, mas a fala não especifica o que foi dado, tornando a reação vaga e menos imersiva.
- nota 4: 0: Carne podre? Eu não gosto disso. Leve embora.
  - ⚠ A fala 'Carne podre? Eu não gosto disso. Leve embora.' é um pouco longa para uma reação espontânea, mas ainda aceitável.
- nota 3: 0: Humano, isso me desagrada de verdade.
  - ⚠ A fala 'Humano, isso me desagrada de verdade.' é muito formal e filosófica para um golem de ferro, que costuma ter falas curtas e diretas.
  - ⚠ A reação não expressa claramente a aversão ao presente, apenas um desagrado genérico.

### ev_devolveu_golem (3.50)  _um golem de ferro e outra criatura da mesma espécie; o jogador ACABOU DE DEVOLVER, sem ser pedido, algo que a criatura lhe entregara_
- nota 3: 0: Eu vi o que fez. A vila precisa de gente assim.
  - ⚠ A fala soa genérica e não específica para um golem de ferro; poderia ser dita por qualquer criatura.
  - ⚠ Não há resposta ou fecho, pois é uma fala única, mas a reação não parece natural para um golem, que normalmente é silencioso e protetor, não elogioso.
- nota 4: 0: Honesto, hein? Gostei disso, humano.
  - ⚠ Golem de ferro não costuma expressar aprovação com 'Gostei disso, humano' de forma tão casual; soa mais humano que golem.
- nota 3: 0: Você devolveu o que não era seu. Isso é raro.
  - ⚠ A fala soa filosófica demais para um golem de ferro, que normalmente tem falas simples e protetoras.
  - ⚠ A situação é específica, mas a fala não deixa claro o que foi devolvido ou por que isso é raro, parecendo genérica.

### ev_volta_ameacou (3.50)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; a criatura reencontra o jogador, que ela já conhece; o jogador já ameaçou quem abre com palavras (fato verificado)_
- nota 3: 0: Lembro das palavras duras que ouvi do humano.
  - ⚠ A fala 'Lembro das palavras duras que ouvi do humano.' soa um pouco formal e filosófica para uma criatura calma como um animal de fazenda ou aldeão, que tenderia a expressões mais simples e diretas.
- nota 3: 0: Suas ameaças ainda ecoam na minha cabeça.
  - ⚠ A fala 'Suas ameaças ainda ecoam na minha cabeça' soa filosófica demais para uma criatura calma como um animal de fazenda ou aldeão, que tenderia a expressar medo ou mágoa de forma mais simples.

### ev_presente_indiferente_bicho (3.60)  _dois animais da fazenda; o jogador ACABOU DE DAR um presente que não diz nada à criatura_
- nota 3: 0: Isso eu não como, mas gostei do carinho.
  - ⚠ A fala 'Isso eu não como, mas gostei do carinho.' é genérica e não especifica qual animal da fazenda está falando, o que pode soar estranho dependendo da criatura (ex: uma galinha ou vaca).
  - ⚠ A situação de 'presente que não diz nada à criatura' é vaga; a fala não reflete claramente o que foi dado ou por que não é comestível.
- nota 3: 0: Não é ruim, pedra. Só não é o meu tipo de comida.
  - ⚠ A fala 'Não é ruim, pedra. Só não é o meu tipo de comida.' soa estranha para um animal de fazenda, pois animais não avaliam presentes como 'não é ruim' ou 'não é o meu tipo de comida' de forma tão hum
  - ⚠ A situação é um presente dado pelo jogador, mas a fala não especifica qual animal está falando, o que pode gerar ambiguidade sobre o mundo da criatura.

### jogador_companheiro (3.62)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; um ser segue o jogador como companheiro_
- nota 3: 0: O humano tem um amigo fiel que anda atrás dele. / 1: Eu também queria alguém assim por perto. / 0: Que continuem assim.
  - ⚠ Fala 0: 'O humano tem um amigo fiel que anda atrás dele.' soa observacional demais para uma criatura calma, mas é aceitável.
  - ⚠ Fala 1: 'Eu também queria alguém assim por perto.' expressa desejo de companhia, o que é plausível, mas pode ser estranho para animais de fazenda.
- nota 3: 0: O humano tem um amigo fiel que anda atrás dele. / 1: Deve ser uma boa companhia. / 0: Que continuem assim.
  - ⚠ Fala 0: 'O humano tem um amigo fiel que anda atrás dele.' soa observacional demais para criaturas calmas como vacas ou aldeões; falta naturalidade.
  - ⚠ Fala 1: 'Deve ser uma boa companhia.' é genérica e não aprofunda a conversa.
- nota 4: 0: Olha, aquele que segue o humano parece contente. / 1: Deve ser uma boa companhia. / 0: Que continuem assim.
  - ⚠ Fala 0 'Olha, aquele que segue o humano parece contente.' soa um pouco formal para uma conversa casual entre criaturas.
  - ⚠ Fala 0 'Que continuem assim.' é um fecho um pouco vago, mas aceitável.
- nota 3: 0: O humano tem um amigo fiel que anda atrás dele. / 1: Deve ser uma boa companhia. / 0: É bonito ver amizade.
  - ⚠ Fala 0: 'O humano tem um amigo fiel que anda atrás dele.' soa observadora demais para criaturas calmas, mas é aceitável.
  - ⚠ Fala 1: 'Deve ser uma boa companhia.' é genérica e não aprofunda a conversa.
- nota 4: 0: Olha, aquele que segue o humano parece contente. / 1: Parecem bem felizes. / 0: É bonito ver amizade.
  - ⚠ Fala 0: 'Olha, aquele que segue o humano parece contente.' soa um pouco formal para criaturas calmas, mas aceitável.
  - ⚠ Fala 1: 'Parecem bem felizes.' usa plural 'parecem' mas se refere a um único ser, o que pode confundir.

### jogador_nadando (3.62)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; o jogador está nadando na água_
- nota 4: 0: O humano está na água! / 1: Se afogar não é brincadeira. / 0: Vamos ficar de olho.
  - ⚠ A fala 'Se afogar não é brincadeira.' soa um pouco formal para criaturas calmas, mas ainda é aceitável.
- nota 4: 0: Olha só, o humano dando um mergulho. / 1: Eu nem chego perto de rio. / 0: Tomara que não se afogue.
  - ⚠ A fala 'Eu nem chego perto de rio.' soa um pouco genérica, sem especificar por que a criatura evita água, o que poderia enriquecer a conversa.
- nota 3: 0: A água deve estar gelada hoje. / 1: Espero que sim, a água não perdoa. / 0: Vamos ficar de olho.
  - ⚠ A fala 'Espero que sim, a água não perdoa.' soa estranha para criaturas calmas como aldeões ou animais de fazenda, pois sugere um tom ameaçador incomum.
  - ⚠ A fala 'Vamos ficar de olho.' é vaga e não fecha claramente a conversa sobre a água gelada.
- nota 4: 0: Olha só, o humano dando um mergulho. / 1: Se afogar não é brincadeira. / 0: Que saia logo da água.
  - ⚠ A fala 'Se afogar não é brincadeira.' soa um pouco formal para uma conversa casual entre criaturas da fazenda.
  - ⚠ A fala 'Que saia logo da água.' usa 'que' de forma um pouco incomum para o português falado; seria mais natural 'Tomara que saia logo da água.'
- nota 4: 0: Olha só, o humano dando um mergulho. / 1: Espero que sim, a água não perdoa. / 0: Vamos ficar de olho.
  - ⚠ A fala 'Espero que sim, a água não perdoa' soa um pouco vaga e não responde diretamente ao comentário anterior, mas ainda é plausível.

### ev_presente_gosta_golem (3.75)  _um golem de ferro e outra criatura da mesma espécie; o jogador ACABOU DE DAR um presente que a criatura ADORA_
- nota 3: 0: Para mim? Que gentileza, humano.
  - ⚠ Golem de ferro não costuma expressar gratidão de forma tão calorosa; sua fala é mais mecânica e protetora.
  - ⚠ A fala 'Que gentileza, humano' soa mais como um aldeão do que como um golem de ferro.
- nota 4: 0: Gostei do presente. Vou proteger você ainda melhor.
  - ⚠ A fala é um pouco longa para uma reação espontânea, mas aceitável.
  - ⚠ Golem de ferro não costuma expressar emoções como 'gostei', mas é passável.
- nota 4: 0: Não esperava ganhar papoula. Fiquei feliz.
  - ⚠ A fala 'Não esperava ganhar papoula. Fiquei feliz.' é um pouco formal para uma reação espontânea; soa mais natural algo como 'Uma papoula! Obrigado!'
- nota 4: 0: Hm! Papoula! Eu gosto muito disso.
  - ⚠ A fala 'Hm! Papoula! Eu gosto muito disso.' soa um pouco formal para uma reação espontânea; 'Hm' é uma interjeição comum, mas a frase completa poderia ser mais curta e natural, como 'Hm, papoula! Ador

### ev_boom_golem (3.80)  _um golem de ferro e outra criatura da mesma espécie; ACABOU DE EXPLODIR um creeper bem perto_
- nota 3: 0: Mantenham a calma, eu estou aqui.
  - ⚠ A fala 'Mantenham a calma, eu estou aqui.' soa mais como um humano tentando acalmar outros, não como um golem de ferro, que normalmente é silencioso e protetor, mas não fala dessa forma.
  - ⚠ A situação é uma explosão de creeper, mas a fala não menciona a explosão ou o perigo, apenas tenta acalmar, o que pode não combinar com a urgência do momento.
- nota 4: 0: Alerta! Algo explodiu perto da vila.
  - ⚠ A fala 'Alerta! Algo explodiu perto da vila.' é um pouco formal para uma reação imediata a uma explosão; espera-se algo mais curto e instintivo, como 'Explosão!' ou 'Creeper!'.
- nota 4: 0: Explosão! Fiquem atrás de mim!
  - ⚠ A fala 'Explosão! Fiquem atrás de mim!' é um pouco longa para uma reação imediata a uma explosão; golems de ferro tendem a ser mais lacônicos.
- nota 4: 0: Foi um creeper. Eu cuido disso.
  - ⚠ A fala 'Eu cuido disso' soa um pouco vaga, pois o creeper já explodiu; o golem poderia reagir de forma mais direta ao ocorrido.

### ev_presente_indiferente_vila (3.80)  _dois aldeões; o jogador ACABOU DE DAR um presente que não diz nada à criatura_
- nota 3: 0: Pedra? Pensei que fosse outra coisa, mas tudo bem.
  - ⚠ A fala 'Pedra? Pensei que fosse outra coisa, mas tudo bem.' é vaga e não deixa claro o que o aldeão esperava, soando um pouco artificial.
  - ⚠ A reação não expressa claramente a relação do aldeão com o presente, como gratidão ou desapontamento, o que a torna menos natural.
- nota 3: 0: Aceito, mas não sei o que fazer com pedra.
  - ⚠ A fala 'Aceito, mas não sei o que fazer com pedra.' soa estranha para um aldeão, pois eles normalmente trocam itens e não recebem presentes sem contexto.
  - ⚠ A reação não demonstra surpresa ou gratidão típica de um aldeão ao receber um item inesperado.
- nota 3: 0: Você lembrou de mim, e isso já é bom.
  - ⚠ A fala 'Você lembrou de mim, e isso já é bom.' soa filosófica demais para um aldeão, que normalmente fala de forma simples e prática.

### ev_presente_odeia_bicho (3.80)  _dois animais da fazenda; o jogador ACABOU DE DAR um presente que a criatura DETESTA_
- nota 3: 0: Nem de graça eu aceito carne podre.
  - ⚠ A fala 'Nem de graça eu aceito carne podre' é genérica e não especifica qual animal da fazenda está falando, o que pode soar estranho dependendo da criatura (ex: uma galinha ou vaca).
  - ⚠ A reação não demonstra claramente a emoção de detestar o presente, apenas recusa de forma direta, sem variação ou expressão mais natural para a situação.
- nota 4: 0: Blergh! Tira carne podre daqui, o cheiro é horrível!
  - ⚠ A fala 'Blergh! Tira carne podre daqui, o cheiro é horrível!' é uma reação plausível de um animal de fazenda ao receber carne podre, mas não especifica qual animal, o que pode gerar ambiguidade.
- nota 3: 0: Que mal eu te fiz para ganhar carne podre?
  - ⚠ A fala 'Que mal eu te fiz para ganhar carne podre?' soa mais como uma reclamação direta ao jogador do que uma reação natural em voz alta, mas é aceitável para um animal de fazenda.

### ev_volta (3.86)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; a criatura reencontra o jogador, que ela já conhece_
- nota 3: 0: Ainda lembro do humano.
  - ⚠ A fala 'Ainda lembro do humano.' é muito genérica e não especifica a criatura, podendo soar estranha para animais como vacas ou ovelhas, que normalmente não expressam memória dessa forma.
- nota 4: 0: Bom ver o humano outra vez.
  - ⚠ A fala 'Bom ver o humano outra vez.' é um pouco genérica e não especifica a criatura, mas é aceitável para aldeões ou animais domesticados.

### bioma_selva (4.00)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; o bioma é uma selva_
- nota 4: 0: Essa selva não acaba nunca. / 1: Eu sempre me perco por aqui. / 0: Fica perto para não se perder.
  - ⚠ A fala 'Eu sempre me perco por aqui' soa um pouco genérica, mas ainda plausível para criaturas calmas em uma selva.
  - ⚠ A última fala 'Fica perto para não se perder' é um conselho natural, mas poderia ser mais específica ao contexto da selva.
- nota 4: 0: Tanto bicho barulhento por aqui! / 1: Tem cheiro de chuva em todo canto. / 0: A selva cuida de quem a respeita.
  - ⚠ A fala 'Tem cheiro de chuva em todo canto.' não responde diretamente à primeira fala sobre barulho, mas ainda é plausível como comentário sobre o ambiente.
  - ⚠ A fala 'A selva cuida de quem a respeita.' soa um pouco filosófica para criaturas calmas como animais de fazenda, mas pode ser aceitável para aldeões.
- nota 4: 0: Cada folha aqui é do tamanho de uma porta. / 1: Tem cheiro de chuva em todo canto. / 0: A selva cuida de quem a respeita.
  - ⚠ A fala 'A selva cuida de quem a respeita' soa filosófica demais para criaturas calmas como animais de fazenda ou aldeões simples.
- nota 4: 0: Essa selva não acaba nunca. / 1: Cuidado com o que se esconde nos cipós. / 0: A selva cuida de quem a respeita.
  - ⚠ A fala 'A selva cuida de quem a respeita' soa um pouco filosófica para criaturas calmas como aldeões ou animais de fazenda, que normalmente não expressam conceitos abstratos.
- nota 4: 0: Essa selva não acaba nunca. / 1: Os papagaios não calam a boca. / 0: Fica perto para não se perder.
  - ⚠ A fala 'Os papagaios não calam a boca' soa um pouco abrupta, mas é plausível como resposta.
  - ⚠ A fala 'Fica perto para não se perder' é um fecho adequado, mas poderia ser mais natural se conectasse com a reclamação anterior.

### ev_obrigado_golem (4.00)  _um golem de ferro e outra criatura da mesma espécie; o jogador ACABOU DE MATAR um monstro perto da vila_
- nota 4: 0: Um a menos para eu cuidar. Valeu.
  - ⚠ A fala 'Um a menos para eu cuidar. Valeu.' soa um pouco casual para um golem de ferro, que costuma ser mais protetor e direto, mas ainda é aceitável.
- nota 4: 0: Ameaça neutralizada. Respeito.
  - ⚠ Fala única de golem de ferro soa um pouco formal demais para uma reação espontânea, mas ainda aceitável.
- nota 3: 0: Você luta bem. Fico menos sozinho nessa guarda.
  - ⚠ A fala 'Você luta bem. Fico menos sozinho nessa guarda.' soa mais como um elogio direto ao jogador, mas a situação indica que o jogador acabou de matar um monstro; o golem poderia reconhecer isso de f
  - ⚠ A expressão 'Fico menos sozinho nessa guarda' é um pouco filosófica para um golem de ferro, que tende a ser mais direto e protetor.

### ev_ovo (4.00)  _uma galinha e outra criatura da mesma espécie; o jogador ACABOU DE PEGAR o ovo que a galinha tinha botado_
- nota 3: 0: Eu choco, você pega? Assim não vale!
  - ⚠ A fala 'Eu choco, você pega? Assim não vale!' soa um pouco artificial para uma galinha, pois mistura uma reclamação com uma pergunta retórica de forma pouco natural.
  - ⚠ A situação é o jogador pegando o ovo, mas a fala não deixa claro se a galinha está se dirigindo ao jogador ou a outra galinha, o que pode confundir.
- nota 4: 0: Có, có, có! Isso é abuso!
  - ⚠ A fala 'Có, có, có! Isso é abuso!' é um pouco longa para uma reação espontânea de galinha, mas ainda aceitável.

### ev_perigo (4.00)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; um inimigo se aproxima do jogador e a criatura, que o acompanha, o avisa_
- nota 4: 0: Cuidado! Perigo à vista: zumbi!
  - ⚠ A fala 'Cuidado! Perigo à vista: zumbi!' é um pouco formal para uma criatura calma, mas ainda aceitável.
- nota 3: 0: Psiu! Zumbi se aproximando, muito cuidado!
  - ⚠ A fala 'Psiu! Zumbi se aproximando, muito cuidado!' soa mais como um aviso de um companheiro humano do que de uma criatura calma como um aldeão ou animal de fazenda, que normalmente não usaria 'Psiu!'
  - ⚠ A situação menciona 'duas criaturas calmas' e 'outra criatura da mesma espécie', mas há apenas uma fala, o que pode indicar que a criatura está falando sozinha; isso não fica claro e pode parecer arti
- nota 4: 0: Perigo! Zumbi está muito perto!
  - ⚠ A fala 'Perigo! Zumbi está muito perto!' é um aviso genérico, mas não especifica quem fala; para criaturas calmas como vacas ou ovelhas, seria mais natural um som de alerta ou uma frase mais simples, 
- nota 4: 0: Atenção! Zumbi se aproximando!
  - ⚠ A fala 'Atenção! Zumbi se aproximando!' é um aviso genérico que não especifica a criatura que fala; se for um animal de fazenda, soa artificial, pois animais não costumam usar linguagem tão estruturad
- nota 4: 0: Quieto aí... zumbi bem na nossa frente!
  - ⚠ A fala 'Quieto aí... zumbi bem na nossa frente!' é um aviso, mas não especifica se a criatura que fala é um aldeão ou um animal; para um animal, a menção a 'zumbi' pode ser aceitável, mas a expressão 

### ev_queda_bicho (4.00)  _dois animais da fazenda; o jogador ACABOU DE CAIR de muito alto e se machucou_
- nota 4: 0: Ui! O humano despencou de lá! / 1: Aposto que doeu. / 0: Melhor ficar com os pés no chão.
  - ⚠ A fala 'Melhor ficar com os pés no chão.' soa como um conselho genérico, não específico de animais da fazenda.
- nota 4: 0: Que tombo feio! / 1: Pelo menos ele levantou. / 0: Que tome cuidado com os ossos.
  - ⚠ A fala 'Que tome cuidado com os ossos.' soa um pouco estranha; talvez 'Que ele tome cuidado com os ossos.' ou 'Tomara que não tenha quebrado os ossos.' fosse mais natural.
- nota 4: 0: Que tombo feio! / 1: Pelo menos ele levantou. / 0: Aqui o chão é firme, ninguém precisa voar.
  - ⚠ A fala 'Aqui o chão é firme, ninguém precisa voar.' soa um pouco filosófica para animais de fazenda, mas ainda é aceitável como comentário simples.
- nota 4: 0: Nossa, o humano caiu do alto! / 1: Aposto que doeu. / 0: Aqui o chão é firme, ninguém precisa voar.
  - ⚠ A fala 'Aqui o chão é firme, ninguém precisa voar.' soa um pouco filosófica para animais de fazenda, mas ainda é aceitável.
- nota 4: 0: Nossa, o humano caiu do alto! / 1: Humanos não sabem cair. / 0: Aqui o chão é firme, ninguém precisa voar.
  - ⚠ A fala 'Aqui o chão é firme, ninguém precisa voar.' soa um pouco filosófica para animais de fazenda, mas ainda é aceitável.

### ev_raio_golem (4.00)  _um golem de ferro e outra criatura da mesma espécie; ACABOU DE CAIR UM RAIO bem perto_
- nota 4: 0: Trovão forte. Fiquem juntos.
  - ⚠ A fala 'Trovão forte. Fiquem juntos.' soa um pouco formal para uma reação imediata a um raio; poderia ser mais curta e instintiva, como 'Cuidado!' ou 'Protejam-se!'.
- nota 4: 0: O ferro atrai raio, dizem. Vou ficar longe dos topos.
  - ⚠ A fala soa mais como um pensamento do que uma reação imediata a um raio que acabou de cair; falta urgência ou surpresa.
- nota 4: 0: Um raio e tanto. Ferro aguenta, mas não vou ficar debaixo de árvore.
  - ⚠ A fala 'não vou ficar debaixo de árvore' soa um pouco humana demais para um golem de ferro, que normalmente não se preocupa com segurança pessoal.
- nota 4: 0: Raio registrado. A vila está bem.
  - ⚠ A fala 'Raio registrado. A vila está bem.' soa um pouco robótica para um golem de ferro, que normalmente tem falas mais simples e protetoras.

### ev_saque_golem (4.00)  _um golem de ferro e outra criatura da mesma espécie; SAQUEADORES hostis estão chegando perto da vila_
- nota 4: 0: Alerta: invasores. Eu cuido deles.
  - ⚠ A fala 'Alerta: invasores. Eu cuido deles.' soa um pouco robótica para um golem de ferro, que normalmente tem fala mais simples e direta.
- nota 4: 0: Hoje ninguém passa por mim.
  - ⚠ A fala 'Hoje ninguém passa por mim' é um pouco genérica e não menciona diretamente os saqueadores, mas ainda faz sentido para um golem de ferro protetor.
- nota 4: 0: Saqueadores à vista. Todos atrás de mim.
  - ⚠ A fala 'Todos atrás de mim' soa como ordem de líder, mas golems de ferro não têm hierarquia ou liderança entre si; seria mais natural algo como 'Proteger a vila' ou 'Defender os aldeões'.
- nota 4: 0: Eu seguro a linha, ninguém corre perigo.
  - ⚠ A fala 'Eu seguro a linha, ninguém corre perigo' é um pouco vaga; poderia ser mais específica sobre a defesa da vila contra os saqueadores.

### ev_volta_traiu (4.00)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; a criatura reencontra o jogador, que ela já conhece; o jogador já quebrou um acordo com quem abre (fato verificado)_
- nota 4: 0: Você de novo. Não esqueci o acordo que quebrou.
  - ⚠ A fala é um pouco longa para uma reação espontânea, mas ainda aceitável.
  - ⚠ Falta contexto sobre qual criatura está falando; se for um animal, a menção a 'acordo' pode ser estranha.
- nota 4: 0: Cuidado comigo: palavra quebrada não se esquece.
  - ⚠ A fala 'Cuidado comigo: palavra quebrada não se esquece.' soa um pouco formal e filosófica para uma criatura calma como um animal de fazenda, que tenderia a expressões mais simples e diretas.

### jogador_montado (4.00)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; o jogador está montado em algo (cavalo, barco...)_
- nota 4: 0: Olha só, o humano vai de carona por aí. / 1: Eu preferia andar com meus próprios pés. / 0: Quem anda assim chega rápido.
  - ⚠ A fala 'Quem anda assim chega rápido.' não fecha claramente a conversa, soando mais como uma continuação do que um fecho.
- nota 4: 0: Olha só, o humano vai de carona por aí. / 1: Eu preferia andar com meus próprios pés. / 0: Boa viagem.
  - ⚠ A fala 'Eu preferia andar com meus próprios pés' soa um pouco estranha para criaturas como vacas ou porcos, que normalmente não expressam preferências de locomoção dessa forma.
- nota 4: 0: Quem dera eu andar assim sem esforço. / 1: Eu preferia andar com meus próprios pés. / 0: Quem anda assim chega rápido.
  - ⚠ A fala 'Quem dera eu andar assim sem esforço.' soa um pouco formal para uma criatura calma, mas ainda aceitável.
  - ⚠ A resposta 'Eu preferia andar com meus próprios pés.' é natural, mas o fecho 'Quem anda assim chega rápido.' poderia ser mais conectado à resposta anterior.
- nota 4: 0: O humano está bem acomodado nesse transporte. / 1: Dá para ir bem mais longe assim. / 0: Boa viagem.
  - ⚠ A fala 'O humano está bem acomodado nesse transporte.' soa um pouco formal para criaturas calmas, mas ainda aceitável.
  - ⚠ A fala 'Dá para ir bem mais longe assim.' é um pouco vaga, mas faz sentido no contexto.
- nota 4: 0: Quem dera eu andar assim sem esforço. / 1: Isso é que é viajar bem. / 0: Boa viagem.
  - ⚠ A fala 'Isso é que é viajar bem.' soa um pouco genérica e não especifica o que torna a viagem boa, mas ainda é aceitável.

### ev_queda_vila (4.12)  _dois aldeões; o jogador ACABOU DE CAIR de muito alto e se machucou_
- nota 4: 0: Que tombo! Ouvi o barulho daqui. / 1: Eu teria chorado. / 0: Se cuide.
  - ⚠ A fala 'Eu teria chorado.' soa um pouco estranha para um aldeão, pois não é uma reação típica de aldeões em Minecraft, que costumam ser mais práticos ou assustados.
- nota 4: 0: O humano despencou lá de cima! / 1: Parece que sobreviveu. / 0: Se cuide.
  - ⚠ A fala 'Se cuide.' soa um pouco vaga e não fecha a conversa de forma natural, pois não se refere diretamente ao acontecimento ou à resposta anterior.
- nota 4: 0: Caiu feio, hein? Está bem? / 1: Alguém dê um pouco de comida a ele. / 0: Se cuide.
  - ⚠ A fala 'Alguém dê um pouco de comida a ele.' soa um pouco formal para uma conversa casual entre aldeões, mas ainda é aceitável.
- nota 4: 0: O humano despencou lá de cima! / 1: Não pula de novo, por favor. / 0: Chão é duro, melhor lembrar.
  - ⚠ A fala 'Não pula de novo, por favor.' soa como um pedido direto ao jogador, mas os aldeões estão conversando entre si, não com o jogador.
  - ⚠ A fala 'Chão é duro, melhor lembrar.' é um pouco vaga e não fecha claramente a conversa.
- nota 4: 0: Que tombo! Ouvi o barulho daqui. / 1: Parece que sobreviveu. / 0: Se cuide.
  - ⚠ A fala 'Se cuide.' soa um pouco abrupta e não fecha a conversa de forma natural, pois não responde diretamente à fala anterior.

### jogador_pegando_fogo (4.12)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; o jogador está pegando fogo_
- nota 4: 0: Alguém ajuda! Tem fogo no humano! / 1: Corre para a água! / 0: Tomara que apague a tempo.
  - ⚠ A fala 'Corre para a água!' soa como uma ordem direta ao humano, mas é dita entre criaturas; seria mais natural se fosse um comentário como 'Ele precisa de água!'
- nota 4: 0: Alguém ajuda! Tem fogo no humano! / 1: Tem balde de água por aqui? / 0: Tomara que apague a tempo.
  - ⚠ A fala 'Tem balde de água por aqui?' soa mais como uma pergunta prática de um aldeão, mas se for um animal, não combina com o mundo de uma vaca ou ovelha, que não usam baldes.
- nota 4: 0: Alguém ajuda! Tem fogo no humano! / 1: Rolar no chão não adianta nada! / 0: Água, água, rápido!
  - ⚠ A fala 'Rolar no chão não adianta nada!' soa como um comentário crítico, mas não é uma resposta direta ao pedido de ajuda, o que enfraquece a coesão da conversa.
  - ⚠ A fala 'Água, água, rápido!' é um fecho adequado, mas poderia ser mais natural se a segunda criatura tivesse respondido de forma mais cooperativa antes.
- nota 4: 0: O humano está pegando fogo! / 1: Tem balde de água por aqui? / 0: Tomara que apague a tempo.
  - ⚠ A fala 'Tem balde de água por aqui?' soa mais como uma pergunta prática de um aldeão, mas se for um animal, não combina com seu mundo.
  - ⚠ A fala 'Tomara que apague a tempo.' é um pouco filosófica para um animal, mas aceitável para um aldeão.
- nota 3: 0: O humano está pegando fogo! / 1: Rolar no chão não adianta nada! / 0: Tomara que apague a tempo.
  - ⚠ A fala 'Rolar no chão não adianta nada!' soa estranha para criaturas calmas, pois demonstra conhecimento específico sobre combate a incêndio.
  - ⚠ A fala 'Tomara que apague a tempo.' é passiva e não reflete urgência diante do fogo.

### ev_presente_gosta_vila (4.14)  _dois aldeões; o jogador ACABOU DE DAR um presente que a criatura ADORA_
- nota 3: 0: Você acertou em cheio: cenoura!
  - ⚠ Fala única de aldeão soa como reação a presente, mas 'Você acertou em cheio: cenoura!' é um pouco formal para fala espontânea; espera-se algo mais curto como 'Cenoura! Obrigado!'
- nota 4: 0: Nossa, cenoura! Que gesto bonito.
  - ⚠ A fala 'Nossa, cenoura! Que gesto bonito.' soa um pouco formal para uma reação espontânea; 'Nossa, cenoura! Obrigado!' seria mais natural.
- nota 3: 0: Receber cenoura alegrou o meu dia.
  - ⚠ A fala 'Receber cenoura alegrou o meu dia.' soa um pouco formal para uma reação espontânea; algo como 'Cenoura! Adoro!' seria mais natural.
  - ⚠ A situação indica que o presente foi dado a uma criatura, mas a fala não especifica quem recebeu; como são dois aldeões, poderia haver uma interação entre eles.

### ev_presente_gosta_bicho (4.17)  _dois animais da fazenda; o jogador ACABOU DE DAR um presente que a criatura ADORA_
- nota 4: 0: Nhac, nhac... que bom!
  - ⚠ A fala 'Nhac, nhac... que bom!' é genérica e não especifica qual animal da fazenda está falando, o que pode soar estranho dependendo da criatura (ex: uma galinha não faria 'nhac').

### ev_presente_odeia_vila (4.20)  _dois aldeões; o jogador ACABOU DE DAR um presente que a criatura DETESTA_
- nota 3: 0: Isso eu não aceito, me dá nojo.
  - ⚠ A fala 'Isso eu não aceito, me dá nojo' é um pouco longa para uma reação espontânea; poderia ser mais curta e direta.
  - ⚠ A expressão 'me dá nojo' é um pouco forte para um aldeão, que normalmente tem reações mais contidas.
- nota 3: 0: Prefiro que fique com carne podre.
  - ⚠ A fala 'Prefiro que fique com carne podre.' soa ambígua: não fica claro se o aldeão está recusando o presente ou se refere a algo que ele já tem, e não expressa claramente desgosto pelo presente receb

### ev_boom_bicho (4.25)  _dois animais da fazenda; ACABOU DE EXPLODIR um creeper bem perto_
- nota 4: 0: Que barulhão foi esse? / 1: Cheiro de pólvora no ar. / 0: Melhor esperar tudo se acalmar.
  - ⚠ A fala 'Cheiro de pólvora no ar.' é um pouco formal para animais de fazenda, mas ainda aceitável.
- nota 4: 0: Que susto! O que foi isso? / 1: Foi lá perto, senti o chão tremer. / 0: Passou o susto, passou.
  - ⚠ A fala 'Passou o susto, passou.' soa um pouco artificial como fecho, mas é aceitável.
- nota 4: 0: Aquele estrondo me deu um arrepio! / 1: Fica junto de mim, vai que tem mais. / 0: Passou o susto, passou.
  - ⚠ A fala 'Passou o susto, passou.' soa um pouco artificial como fecho, mas é aceitável.
- nota 4: 0: Que susto! O que foi isso? / 1: Será que o humano viu? / 0: Vamos ficar longe daquele lado.
  - ⚠ A fala 'Será que o humano viu?' soa um pouco deslocada para animais da fazenda, que normalmente não se preocupariam com a percepção do humano sobre a explosão.
- nota 4: 0: Aquele estrondo me deu um arrepio! / 1: Será que o humano viu? / 0: Melhor esperar tudo se acalmar.
  - ⚠ A fala 'Será que o humano viu?' soa um pouco genérica para animais da fazenda, mas ainda é plausível.

### ev_devolveu (4.25)  _dois aldeões; o jogador ACABOU DE DEVOLVER, sem ser pedido, algo que a criatura lhe entregara_
- nota 3: 0: Você devolveu 3 esmeraldas? Isso é raro!
  - ⚠ A fala soa como uma reação a uma troca, mas a situação é uma devolução espontânea; 'Isso é raro!' é genérico e não reflete a surpresa específica de um aldeão ao receber algo de volta sem pedir.
  - ⚠ A fala é única, então não há resposta ou fecho, mas como reação isolada, falta um tom mais natural de espanto ou gratidão típico de um aldeão.
- nota 4: 0: Eu nem ia notar, mas você devolveu 3 esmeraldas. Valeu!
  - ⚠ A fala 'Eu nem ia notar, mas você devolveu 3 esmeraldas. Valeu!' soa um pouco artificial para um aldeão, que normalmente fala de forma mais simples e direta, mas ainda é aceitável.

### ev_volta_acalmou (4.25)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; a criatura reencontra o jogador, que ela já conhece; o jogador já acalmou quem abre quando estava com medo (fato verificado)_
- nota 4: 0: O humano! Foi quem me acalmou naquele dia.
  - ⚠ A fala é um pouco longa para uma reação espontânea, mas ainda aceitável.
- nota 4: 0: Gente que acalma os outros é rara, e você é uma delas.
  - ⚠ A fala soa um pouco formal para uma criatura de fazenda, mas é aceitável.
- nota 4: 0: Fico em paz só de ver o humano.
  - ⚠ A fala 'Fico em paz só de ver o humano' é um pouco filosófica para uma criatura simples como um animal de fazenda, mas ainda aceitável.

### jogador_seguidores (4.25)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; vários seres seguem o jogador como companheiros_
- nota 4: 0: Quanta gente anda atrás do humano! / 1: Será que tem lugar para mais um? / 0: Quem sabe eu me junto também.
  - ⚠ A fala 'Quanta gente anda atrás do humano!' soa um pouco genérica, mas aceitável para aldeões ou animais.
  - ⚠ A resposta 'Será que tem lugar para mais um?' é natural, mas o fecho 'Quem sabe eu me junto também.' poderia ser mais direto.
- nota 4: 0: Olha só o bando que segue o humano! / 1: Será que tem lugar para mais um? / 0: Boa companhia vale muito.
  - ⚠ A fala 'Boa companhia vale muito.' soa um pouco filosófica para criaturas calmas como animais de fazenda, mas é aceitável para aldeões.
- nota 4: 0: Quanta gente anda atrás do humano! / 1: Deve ser gente de confiança. / 0: Quem sabe eu me junto também.
  - ⚠ A fala 'Quanta gente anda atrás do humano!' soa um pouco genérica, mas aceitável para criaturas calmas.
  - ⚠ A resposta 'Deve ser gente de confiança.' é plausível, mas poderia ser mais específica ao mundo da criatura.
- nota 3: 0: O humano nunca anda sozinho, reparou? / 1: Eu queria entrar nesse grupo. / 0: Quem sabe eu me junto também.
  - ⚠ A fala 'Eu queria entrar nesse grupo.' soa deslocada para criaturas calmas como aldeões ou animais de fazenda, que normalmente não expressam desejo de seguir o jogador em grupo.
  - ⚠ A fala 'Quem sabe eu me junto também.' é vaga e não fecha a conversa de forma natural, deixando a interação sem conclusão clara.
- nota 4: 0: Olha só o bando que segue o humano! / 1: Eu queria entrar nesse grupo. / 0: Quem sabe eu me junto também.
  - ⚠ A fala 'Eu queria entrar nesse grupo.' soa um pouco formal para uma criatura de fazenda, mas é aceitável.
  - ⚠ A fala 'Quem sabe eu me junto também.' é um fecho razoável, mas poderia ser mais natural, como 'Vamos junto!'

### bioma_savana (4.38)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; o bioma é uma savana_
- nota 4: 0: Aqui a terra parece não ter fim. / 1: Pelo menos o vento refresca. / 0: Vamos aproveitar a sombra.
  - ⚠ A fala 'Aqui a terra parece não ter fim' soa um pouco filosófica para criaturas calmas como animais de fazenda, que tendem a expressões mais simples.
- nota 4: 0: Aqui a terra parece não ter fim. / 1: Pelo menos o vento refresca. / 0: Fim de tarde aqui é lindo.
  - ⚠ Fala 0: 'Aqui a terra parece não ter fim' soa um pouco filosófica para criaturas calmas como animais de fazenda, mas é aceitável para aldeões.
  - ⚠ Fala 1: 'Pelo menos o vento refresca' é uma resposta natural, mas não se conecta diretamente com a primeira fala sobre a vastidão da terra.
- nota 4: 0: Aqui a terra parece não ter fim. / 1: Aqui o céu parece enorme. / 0: Vamos aproveitar a sombra.
  - ⚠ A fala 'Aqui a terra parece não ter fim' é um pouco filosófica para criaturas calmas como animais de fazenda, mas aceitável para aldeões.
  - ⚠ A resposta 'Aqui o céu parece enorme' é paralela e combina, mas soa um pouco poética demais para uma conversa casual.
- nota 3: 0: Faz tempo que não chove, a terra está seca. / 1: É mesmo, dá gosto de olhar. / 0: Vamos aproveitar a sombra.
  - ⚠ A fala 'É mesmo, dá gosto de olhar.' não combina com a reclamação sobre a seca; parece uma resposta desconexa.
  - ⚠ A fala 'Vamos aproveitar a sombra.' não fecha a conversa de forma natural, pois não se relaciona com o tema da seca.

### bioma_montanha (4.50)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; o bioma é de montanha, alto e ventoso_
- nota 4: 0: As montanhas parecem tocar o céu. / 1: Cabras gostam disso; eu, nem tanto. / 0: Cada passo aqui conta.
  - ⚠ A fala 'Cabras gostam disso; eu, nem tanto.' pode soar estranha se as criaturas não forem cabras, pois menciona outra espécie sem contexto.
- nota 4: 0: Esse ar é fino, até cansa de respirar. / 1: Aqui o frio vem sem aviso. / 0: Vamos andar devagar.
  - ⚠ A fala 'Esse ar é fino, até cansa de respirar' soa um pouco filosófica para criaturas calmas como animais de fazenda, que tendem a expressões mais simples.
- nota 4: 0: O vento aqui no alto é de arrancar. / 1: Aqui o frio vem sem aviso. / 0: Vamos andar devagar.
  - ⚠ A fala 'Aqui o frio vem sem aviso' não responde diretamente à fala anterior sobre o vento, mas ainda é coerente com o contexto.
- nota 4: 0: As montanhas parecem tocar o céu. / 1: Cuidado para não escorregar. / 0: Cada passo aqui conta.
  - ⚠ A fala 'Cuidado para não escorregar.' é genérica e não responde diretamente à observação sobre as montanhas tocarem o céu, quebrando um pouco a fluidez da conversa.

### jogador_diamantes (4.50)  _dois aldeões; o jogador carrega diamantes_
- nota 4: 0: Aqueles diamantes do humano brilham de longe! / 1: Cuidado para ninguém roubar. / 0: Boa sorte para ele.
  - ⚠ A fala 'Cuidado para ninguém roubar.' soa um pouco vaga e não responde diretamente ao comentário sobre o brilho dos diamantes.
  - ⚠ O fecho 'Boa sorte para ele.' é um pouco desconexo, pois não retoma o assunto do roubo ou do brilho.
- nota 4: 0: O humano carrega diamantes, que sorte! / 1: Cuidado para ninguém roubar. / 0: Boa sorte para ele.
  - ⚠ A fala 'Boa sorte para ele.' soa um pouco desconexa como fecho, pois não responde diretamente ao aviso anterior.
- nota 4: 0: Aqueles diamantes do humano brilham de longe! / 1: Eu só queria ver um de perto. / 0: Boa sorte para ele.
  - ⚠ A fala 'Boa sorte para ele.' não fecha a conversa de forma natural, pois não responde diretamente ao desejo do segundo aldeão de ver um diamante de perto.
- nota 4: 0: O humano carrega diamantes, que sorte! / 1: Eu só queria ver um de perto. / 0: Que guarde bem.
  - ⚠ A fala 'Eu só queria ver um de perto.' soa um pouco deslocada, pois aldeões não demonstram interesse em itens como diamantes, apenas em esmeraldas.

### jogador_voando (4.62)  _duas criaturas calmas (aldeões ou animais da fazenda) e outra criatura da mesma espécie; o jogador está voando (asas de elytra)_
- nota 4: 0: Olha! O humano está voando! / 1: Como será que ele faz isso? / 0: Quem dera voar assim.
  - ⚠ A fala 'Quem dera voar assim.' soa um pouco filosófica para criaturas simples como animais de fazenda, mas é aceitável para aldeões.
- nota 4: 0: O humano cruzou o céu como um pássaro! / 1: Cuidado com a aterrissagem! / 0: Eu ficaria no chão mesmo.
  - ⚠ A fala 'Eu ficaria no chão mesmo.' soa um pouco vaga e não fecha a conversa de forma clara, pois não responde diretamente ao aviso 'Cuidado com a aterrissagem!'.
- nota 4: 0: O humano cruzou o céu como um pássaro! / 1: Como será que ele faz isso? / 0: Quem dera voar assim.
  - ⚠ A fala 'Quem dera voar assim.' não fecha a conversa de forma natural, pois não responde à pergunta 'Como será que ele faz isso?' e deixa a conversa sem conclusão.
