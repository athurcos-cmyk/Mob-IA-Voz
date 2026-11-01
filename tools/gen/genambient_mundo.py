"""Conversa de ambiente (mob com mob, sem IA) dos bichos pacificos que antes ficavam de fora: aquaticos, voadores, bichos da mata e outros,
bichos de especies diferentes entre si e mais assuntos para a fazenda e a estimacao. Usado por genambient.py.

Cada familia: quem abre (a), quem responde (b) e quem fecha (c). TODA abertura (a) tem de funcionar sozinha e as respostas (b) e fechos (c) servem
para qualquer abertura da mesma familia. Regras: nada de adjetivo com genero para a propria criatura; {jogador} so como sujeito; sem palavras de
aldeao (vila, troca, livro, cama...) nem de monstro; so fatos de Minecraft que sao verdade (ver AmbientCoherenceTest).
Classes (campo who): aquatic fish squid glow_squid dolphin turtle axolotl, flying bee parrot bat allay, forest wild + a especie (fox, goat...), animals (qualquer bicho).
"""


def add(fam):
    def F(id, who, need, a, b, c, w=2):
        fam(id, list(who), need, a, b, c, weight=w)

    AQ = ("aquatic", "aquatic")

    # ================================================================== AQUATICOS (entre si)
    F("aq_correnteza", AQ, [],
      ["A correnteza está forte hoje.", "Sentiu a água puxando para o lado?", "Hoje a corrente me levou para bem longe.", "Essa correnteza não perdoa ninguém.",
       "A água está agitada, você notou?", "Quando a corrente vira, eu só deixo levar."],
      ["Notei, melhor ficar perto do fundo.", "É só nadar junto, ela passa.", "Eu gosto, dá menos trabalho.", "Cuidado para não se perder nela.", "Pior é quando ela gira."],
      ["Vamos devagar, então.", "Fica pertinho de mim.", "Quando acalmar, a gente volta."])
    F("aq_fundo", AQ, [],
      ["Lá no fundo está tudo escuro.", "Tem bicho que nunca sobe à superfície.", "Lá embaixo a água é bem mais fria, já reparou?", "O fundo tem cada barulho esquisito.",
       "Já foi até o fundão? Eu dei meia-volta.", "A luz daqui de cima já me basta."],
      ["Eu gosto do escuro, é tranquilo.", "Só vou até onde ainda enxergo alguma coisa.", "Tem coisa lá que é melhor nem ver.", "Eu também prefiro a luz.", "Dizem que tem tesouro lá embaixo."],
      ["Cada um no seu cantinho.", "Pelo menos aqui dá para ver o sol.", "Fica perto da luz, então."])
    F("aq_luz", AQ, ["day"],
      ["A luz do sol entrando na água está linda.", "Olha os raios de luz dançando lá embaixo.", "Quando o sol bate assim, tudo brilha.", "Adoro ficar onde a luz chega."],
      ["Dá vontade de ficar parado só olhando.", "É a melhor hora para nadar.", "Fica tudo mais quentinho.", "Parece que a água fica dourada."],
      ["Vamos aproveitar enquanto dura.", "Daqui a pouco o sol muda de lugar.", "Que dia bom."])
    F("aq_chuva", AQ, ["rain"],
      ["Ouviu as gotas batendo lá em cima?", "A superfície está toda picotada de chuva.", "Chove lá fora e aqui embaixo nem se sente.", "A chuva faz um barulhinho bom daqui de baixo."],
      ["Aqui embaixo é o melhor lugar para a chuva.", "Eu fico olhando os círculos na superfície.", "A água fica mais agitada quando chove.", "Nem percebo, só pelo som."],
      ["Fica quietinho até passar.", "Pelo menos ninguém se molha mais do que já está.", "Chuva boa."])
    F("aq_noite", AQ, ["night"],
      ["Lá em cima já escureceu.", "À noite o mar fica mais quieto.", "Dá para ver a lua daqui, toda tremida.", "De noite eu nado mais devagar."],
      ["A água fica bem escura.", "Prefiro ficar junto de alguém.", "A lua deixa tudo prateado.", "Silêncio bom."],
      ["Boa hora para descansar.", "Fica por perto, então.", "Amanhã tem sol de novo."])
    F("aq_bolhas", AQ, [],
      ["Quem soltou essas bolhas?", "As bolhas me fazem cócegas.", "Tem bolha subindo de todo lado hoje.", "Sempre que passo por uma bolha, dou risada."],
      ["Foi sem querer, juro.", "Eu adoro nadar no meio delas.", "Elas sobem tão rápido!", "Fazem um barulhinho gostoso."],
      ["Vou soltar mais algumas.", "Quem estourar mais, ganha.", "Hoje o dia é de bolhas."])
    F("aq_algas", AQ, [],
      ["As algas estão enormes hoje.", "Me escondo nas algas quando algo se aproxima.", "As algas balançam, dá até sono.", "Prefiro o recife, é mais colorido que as algas."],
      ["Elas escondem bem a gente.", "Eu gosto de passar por entre elas.", "Só não gosto quando enroscam.", "É um bom lugar para descansar."],
      ["Vou me esconder ali um pouco.", "Nos vemos depois das algas.", "Cuidado para não se enroscar."])
    F("aq_perigo", AQ, [],
      ["Ouviu aquele gemido? Parece um afogado.", "Cuidado com o templo do fundo, lá tem guardiões.", "Tem uns mortos-vivos rondando a costa à noite.", "Algo grande passou por aqui agora há pouco."],
      ["Vamos nadar para longe.", "Eu fico de olho.", "Melhor ficar em grupo.", "Eu não chego perto de nada que gême."],
      ["Fica atento, então.", "Vamos sair daqui.", "Se algo vier, a gente se separa."])
    F("aq_naufragio", AQ, [],
      ["Tem um naufrágio perto daqui, já viu?", "Dizem que tem baú no fundo daquele barco.", "As ruínas do fundo guardam segredos.", "Nunca entrei no naufrágio, é muito escuro."],
      ["Eu já espiei de longe.", "Tem muita tábua velha lá dentro.", "Deve ter tesouro, mas ninguém tem coragem.", "Só chego até a porta."],
      ["Um dia a gente entra.", "Quem sabe o humano não resolve explorar.", "Melhor deixar quieto."])
    F("aq_humano_nada", AQ, ["swim"],
      ["{Jogador} está nadando! Olha só.", "Tem alguém nadando por aqui, sem barbatanas.", "{Jogador} nada devagar, mas nada.", "Veja só quem veio para a nossa água."],
      ["Pelo menos tenta.", "Será que precisa de ajuda?", "Ele faz um barulhão na água.", "Eu desvio, por via das dúvidas."],
      ["Vamos dar espaço a ele.", "Se pedir ajuda, eu levo até a beirada.", "Nadar é para quem nasceu para isso."], 3)
    F("aq_humano_vendo", AQ, [],
      ["{Jogador} está na beira da água.", "Será que {jogador} vai pescar?", "O humano está olhando para cá.", "{Jogador} ficou parado perto da água."],
      ["Fica de olho no anzol.", "Ele não parece perigoso.", "Vamos nos afastar só um pouquinho.", "Talvez só esteja admirando o mar."],
      ["Melhor ficar longe da vara.", "Se jogar o anzol, a gente some.", "Humanos são imprevisíveis."])

    # peixes
    F("peixe_cardume", ("fish", "fish"), [],
      ["Fica no cardume, não saia da turma.", "Hoje o cardume está animado.", "Quem virou para o lado errado de novo?", "Nadar em grupo é bem mais seguro.", "Tem sempre um que se perde do cardume."],
      ["Fui eu, desculpa.", "Eu sigo quem está na frente.", "Em grupo ninguém me estranha.", "Eu só vou onde a turma vai.", "Se um vira, todos viram."],
      ["Vamos juntos, então.", "Ninguém fica para trás.", "Em fila, ninguém se perde."], 3)
    F("peixe_pesca", ("fish", "fish"), [],
      ["Cuidado com o anzol.", "Tem humano na beirada com uma vara.", "Ouvi dizer que o isco é bem tentador, mas é cilada.", "Quem foi pescado ontem não voltou mais."],
      ["Eu desvio de qualquer coisa que balança.", "Nem chego perto.", "Quem inventou essa coisa de anzol?", "Só como o que já está solto."],
      ["Fica esperto.", "Vamos para o outro lado.", "Melhor perder a fome do que a vida."], 3)
    F("peixe_comida", ("fish", "fish"), [],
      ["Estou com uma fome danada.", "Achei umas coisinhas gostosas ali na pedra.", "Será que hoje tem comida nova?", "Quando eu como, esqueço de tudo."],
      ["Onde foi isso?", "Eu também estou de barriga vazia.", "Me leva até lá.", "Eu como o que aparecer na frente."],
      ["Vamos lá juntos.", "Se tiver pouco, a gente divide.", "Depois me conta se valeu."])
    F("peixe_esconde", ("fish", "fish"), [],
      ["Vamos brincar de esconde-esconde nas algas?", "Eu me escondo melhor que você.", "Contei até dez e ninguém me achou.", "Achei o melhor esconderijo do mar."],
      ["Aposto que eu te acho.", "Sem fazer bolhas, hein.", "Eu espio por entre as pedras.", "Onde é? Prometo não contar."],
      ["Achei você!", "Agora é a minha vez de esconder.", "Foi divertido."])

    # lulas
    F("lula_tinta", ("squid", "squid"), [],
      ["Quando me assusto, solto tinta e sumo.", "Hoje usei toda a minha tinta de uma vez.", "A tinta é o meu melhor truque.", "Quem chega perto leva um jato de tinta."],
      ["Eu também, é automático.", "Só não pode soltar na hora errada.", "Deixa tudo escuro por um tempinho.", "Aprendi a usar só quando preciso."],
      ["Cada um com o seu truque.", "Boa estratégia.", "Vamos sair daqui antes de ter de usar."])
    F("lula_deriva", ("squid", "squid"), [],
      ["Eu só deixo a água me levar.", "Nadar sem rumo é o meu passatempo.", "Hoje fui parar onde nunca estive.", "Quem tem pressa não aproveita o mar."],
      ["Eu também me perco por aí.", "Isso é viver bem.", "Só preciso estar de volta antes de escurecer.", "A correnteza escolhe o caminho."],
      ["Boa viagem, então.", "A gente se esbarra mais tarde.", "Sem pressa."])
    F("lula_brilho_dueto", ("glow_squid", "glow_squid"), [],
      ["A gente brilha no escuro, sabia?", "Aqui na caverna a nossa luz é a única.", "Quando eu pulso, tudo ao redor acende.", "O escuro daqui precisa de nós."],
      ["Sem a gente, ninguém enxerga nada.", "Eu gosto de iluminar o caminho.", "Parece que o escuro respeita a nossa luz.", "É bonito, mas dá cansaço."],
      ["Vamos brilhar mais um pouco.", "Boa luz.", "Fica por perto que eu ilumino."], 3)

    # golfinhos
    F("golfinho_salto", ("dolphin", "dolphin"), [],
      ["Vamos ver quem salta mais alto?", "Hoje saltei três vezes seguidas.", "Eu adoro sair da água e cair com barulho.", "O segredo é pegar impulso lá do fundo."],
      ["Eu salto mais alto que você.", "Só não vale tocar no barco.", "Conta quantos saltos eu faço.", "Vamos juntos, no três."],
      ["Bora de novo.", "Esse foi o melhor.", "Quem cansa primeiro perde."], 3)
    F("golfinho_barco", ("dolphin", "dolphin"), [],
      ["Passou um barco, vamos seguir?", "Eu adoro nadar ao lado dos barcos.", "O barulho do barco é bom de seguir.", "Tem barco por perto, sinto a água tremer."],
      ["Vamos lá, mas sem encostar.", "Eles ficam felizes quando a gente aparece.", "A gente sempre chega na frente.", "Se ele parar, eu fico esperando."],
      ["Foi bom enquanto durou.", "Já voltamos.", "Corrida ganha."], 3)
    F("golfinho_tesouro", ("dolphin", "dolphin"), [],
      ["Sabia que eu sei onde tem tesouro?", "Se o humano me der um peixe, eu mostro o caminho.", "As ruínas do fundo guardam coisas boas.", "Sempre que alguém me alimenta, eu levo até um baú."],
      ["Eu sei, já levei gente até lá.", "Só mostro para quem merece.", "Quem sabe hoje aparece alguém.", "Nunca falha."],
      ["Vamos esperar um humano passar.", "Segredo nosso.", "Que seja um tesouro grande."])
    F("golfinho_peixe", ("dolphin", "fish"), [],
      ["Quer apostar uma corrida de nado?", "Você é rápido, hein! Quase me alcançou.", "Eu nado mais rápido do que qualquer um aqui.", "Fica de olho que eu passo voando."],
      ["Eu me escondo antes de você chegar.", "Só ganho se me deixar sair na frente.", "Você sempre passa sem ver ninguém.", "Eu só quero que não me confunda com almoço."],
      ["Foi só brincadeira.", "Fica à vontade, hoje estou sem fome.", "Corrida ganha por mim."], 3)

    # tartarugas
    F("tartaruga_praia", ("turtle", "turtle"), [],
      ["Eu sempre volto para a praia onde nasci.", "A areia hoje está quentinha.", "Cada passo meu leva uma eternidade, mas eu chego.", "Nada como o sol na areia."],
      ["Eu também, é um chamado.", "Quem tem pressa não chega melhor.", "A areia daqui é a melhor.", "É devagar, mas é nosso."],
      ["Chegamos quando chegamos.", "Vamos juntos.", "Sem pressa."], 3)
    F("tartaruga_ovos", ("turtle", "turtle"), [],
      ["Tem ovo para cuidar na areia, não esquece.", "Os ovos precisam de sol e de sossego.", "Dizem que os filhotes sempre acham o mar sozinhos.", "Ninguém pode pisar nos ovos."],
      ["Eu fico de guarda.", "Se alguém chegar, eu aviso.", "Tomara que vingue todos.", "Cada um que nasce é uma festa."],
      ["Vamos ficar de olho.", "Eles crescem rápido.", "Que o mar os receba bem."], 3)
    F("tartaruga_casco", ("turtle", "turtle"), [],
      ["Meu casco é a minha casa.", "Quem tem casco nunca fica sem abrigo.", "O casco pesa, mas dá segurança.", "Gosto de me encolher quando o dia está difícil."],
      ["O meu é meio arranhado, mas é meu.", "Eu me encolho e fica tudo bem.", "É melhor que qualquer escudo.", "Eu sempre carrego o meu."],
      ["Cada um com o seu abrigo.", "Boa proteção.", "Dá para descansar em qualquer lugar."])

    # axolotes
    F("axolote_sorriso", ("axolotl", "axolotl"), [],
      ["Eu sorrio o tempo todo, sabia?", "Meu sorriso é de nascença.", "Quem olha para mim fica de bom humor.", "Hoje estou mais sorridente que nunca."],
      ["O meu também não sai do rosto.", "É um sorriso que não cansa.", "Dá para sorrir mesmo bravo.", "Todo mundo gosta da gente por isso."],
      ["Continua sorrindo.", "Vamos espalhar sorrisos.", "Que dia bom."], 3)
    F("axolote_morto", ("axolotl", "axolotl"), [],
      ["Quando tenho medo, me finjo de morto.", "Já fingi de morto para escapar de um problema.", "O truque é ficar bem paradinho.", "Fingir de morto funciona sempre."],
      ["Eu me recupero rápido depois.", "Só tomo cuidado para não fingir demais.", "Funciona até com quem é esperto.", "E depois levanto como se nada tivesse acontecido."],
      ["Bom truque.", "Se precisar, a gente finge junto.", "Que ninguém descubra."], 3)
    F("axolote_caverna", ("axolotl", "axolotl"), [],
      ["Gosto das cavernas com água e plantas.", "A caverna úmida é o melhor lugar do mundo.", "Aqui tem peixe pequeno para comer.", "Quem sai da caverna sente falta dela."],
      ["Tudo verde e molhadinho.", "Aqui a água é limpinha.", "Eu fico nadando de um lado para o outro.", "É o meu cantinho favorito."],
      ["Vamos ficar por aqui.", "A caverna é a nossa casa.", "Fica à vontade."])

    # ================================================================== VOADORES (entre si)
    FL = ("flying", "flying")
    F("voo_vento", FL, [],
      ["O vento está forte hoje, difícil de voar.", "Aproveitei o vento e cheguei bem longe.", "Voar contra o vento cansa.", "Hoje o vento está do nosso lado."],
      ["Eu fico no meio do caminho e deixo ele me levar.", "Melhor esperar ele diminuir.", "Eu adoro quando ele ajuda.", "Cuidado para não ser levado para longe."],
      ["Vamos juntos, então.", "Fica por perto.", "O vento passa."])
    F("voo_altura", FL, [],
      ["Daqui de cima dá para ver tudo.", "Sobrevoei aquela montanha hoje cedo.", "Lá em cima o ar é mais frio.", "Gosto de ficar bem alto, longe de tudo."],
      ["Eu fico mais perto do chão.", "O que você viu de lá?", "Dá até vertigem.", "Só desço quando tenho fome."],
      ["Vamos voar mais alto.", "Quando eu subir, te chamo.", "A vista compensa."])
    F("voo_chuva", FL, ["rain"],
      ["Chuva é a pior hora para voar.", "As asas ficam pesadas na chuva.", "Vou esperar a chuva passar aqui debaixo.", "Quem voa na chuva se arrepende."],
      ["Eu me escondo na primeira folha.", "Não dá nem para enxergar direito.", "Eu espero até acalmar.", "Pelo menos hoje descanso."],
      ["Fica comigo até passar.", "Logo o sol volta.", "Espera um pouco."])
    F("voo_noite", FL, ["night"],
      ["Está escuro demais para voar longe.", "À noite é bom ficar quieto num galho.", "Quem voa de noite precisa de coragem.", "Eu só voo de noite quando é preciso."],
      ["Eu prefiro o dia.", "A lua ajuda um pouco.", "Cuidado com o que ronda no escuro.", "Melhor ficar por perto."],
      ["Amanhã voamos de novo.", "Boa noite de descanso.", "Fica atento."])

    F("abelha_flores", ("bee", "bee"), [],
      ["Achei um campo cheio de flores hoje.", "As flores daquele lado estão lindas.", "Nada melhor do que flor fresca de manhã.", "Cada flor tem um sabor diferente."],
      ["Me leva até lá.", "Eu já passei por lá e peguei bastante.", "Hoje só achei flor murcha.", "Eu gosto das flores amarelas."],
      ["Vamos juntas, então.", "Chego primeiro!", "Quem pegar mais, ganha."], 3)
    F("abelha_colmeia", ("bee", "bee"), [],
      ["Voltei para a colmeia com o pólen cheio.", "A colmeia está lotada hoje.", "Nossa colmeia é a mais bem cuidada de todas.", "Quando chego em casa, o dia acaba bem."],
      ["Eu também, estou sem energia.", "Tem sempre mais espaço para quem trabalha.", "Cuidado para ninguém mexer nela.", "O mel já está quase pronto."],
      ["Amanhã tem mais.", "Descansa um pouco.", "Bom trabalho de novo."], 3)
    F("abelha_trabalho", ("bee", "bee"), [],
      ["Trabalhar o dia inteiro dá um cansaço danado.", "Quantas flores você já visitou hoje?", "Hoje bati meu recorde de pólen.", "Sem a gente, nada floresce por aqui."],
      ["Perdi a conta depois de vinte.", "É um trabalho que vale a pena.", "Eu faço isso com gosto.", "A gente cuida de tudo sem ninguém ver."],
      ["Vamos continuar.", "Mais uma voltinha e acabou.", "Bom trabalho."])
    F("abelha_ferrao", ("bee", "bee"), [],
      ["Não gosto quando alguém mexe na colmeia.", "Eu só ataco quando me ameaçam.", "Dá pena usar o ferrão, só fazemos isso uma vez.", "Quem respeita a colmeia, nunca sai picado."],
      ["Eu também sou calma, mas protejo a casa.", "Só em último caso.", "É sempre o último recurso.", "Quem vem em paz, passa em paz."],
      ["Que ninguém nos incomode.", "Vamos ficar atentas.", "Tomara que não precise."])

    F("papagaio_imitar", ("parrot", "parrot"), [],
      ["Ouvi um creeper chiando e aprendi o som.", "Sei imitar quase tudo que escuto.", "Hoje imitei um esqueleto. Ficou perfeito.", "Aprendi um som novo hoje e não vou contar qual."],
      ["Faz de novo, quero aprender.", "Eu imito até o zumbi.", "Isso assusta qualquer um.", "Eu só imito o que é engraçado."],
      ["Que tal um dueto?", "Vou treinar mais.", "Ninguém imita como a gente."], 3)
    F("papagaio_musica", ("parrot", "parrot"), [],
      ["Quando toca disco, eu danço sem parar.", "Já ouviu uma música tocando por aqui?", "Tem coisa melhor que dançar com música?", "Se tem música, eu estou balançando a cabeça."],
      ["Eu também, não resisto.", "Dança comigo!", "Eu só paro quando a música acaba.", "O ritmo me pega todo."],
      ["Vamos dançar mais.", "Que a música não pare.", "Foi ótimo."], 3)
    F("papagaio_selva", ("parrot", "parrot"), [],
      ["Eu adoro a selva, é tudo tão alto e verde.", "Aqui no alto das árvores o mundo é outro.", "Só voo onde tem copa de árvore.", "A selva tem cada som!"],
      ["O melhor é pousar num galho alto.", "Daqui dá para ver o mundo todo.", "Eu moro aqui e não saio.", "Esse barulho todo é uma festa."],
      ["Vamos para as copas.", "A selva é a nossa casa.", "Fica à vontade."])
    F("papagaio_biscoito", ("parrot", "parrot"), [],
      ["Se um humano oferecer biscoito, nem pense em comer.", "Biscoito faz mal para a gente, sabia?", "Já recusei biscoito três vezes hoje.", "Quem me ensinou a não comer biscoito foi uma amiga."],
      ["Eu sei, é veneno para nós.", "Prefiro sementes.", "Aviso todo mundo que passa.", "Os humanos nem sabem disso."],
      ["Fica de olho.", "Sementes é que são boas.", "Passa o recado adiante."])

    F("morcego_caverna", ("bat", "bat"), [],
      ["Nada melhor que uma caverna escura.", "Gosto de ficar onde o teto é baixo.", "A caverna é o meu lugar.", "Quando entra luz, eu me escondo."],
      ["Eu também, é bem aconchegante.", "Escuridão é o melhor remédio.", "Aqui ninguém nos incomoda.", "Fico de cabeça para baixo e durmo bem."],
      ["Vamos para o fundo.", "Que fique sempre escuro.", "Boa soneca."], 3)
    F("morcego_noite", ("bat", "bat"), ["night"],
      ["A noite é a nossa hora.", "Adoro voar quando não tem sol.", "De noite o céu é todo nosso.", "Hoje vou voar até ficar cansado."],
      ["Eu desvio de tudo com o escuro.", "Vamos juntos, então.", "A noite é o melhor momento.", "Eu só enxergo bem quando escurece."],
      ["Boa caçada.", "Fica por perto.", "Voa devagar."])
    F("morcego_dia", ("bat", "bat"), ["day"],
      ["De dia só quero dormir.", "Quem inventou o sol?", "Com tanta luz eu não enxergo nada.", "Hoje o dia foi longo demais."],
      ["De cabeça para baixo, tudo é melhor.", "Dorme que a noite já vem.", "Eu fecho os olhos e pronto.", "Sol é sinônimo de soneca."],
      ["Boa soneca.", "Quando escurecer, a gente sai.", "Bons sonhos."])

    F("allay_itens", ("allay", "allay"), [],
      ["Eu adoro juntar coisas que os humanos deixam cair.", "Quando alguém me dá um item, fico seguindo a pessoa.", "Hoje coletei um monte de coisas diferentes.", "Cada item que eu pego é um pequeno tesouro."],
      ["Eu levo tudo de volta para quem me deu.", "Os humanos nem sempre percebem.", "Eu gosto de ajudar.", "É minha missão favorita."],
      ["Vamos buscar mais.", "Boa coleta.", "Ninguém recolhe como a gente."], 3)
    F("allay_musica", ("allay", "allay"), [],
      ["Quando toca música, eu danço.", "Essas notinhas de música me deixam leve.", "Eu adoro um bloco de notas.", "O som me faz brilhar mais."],
      ["Eu também fico mais feliz.", "Eu fico girando no ar.", "A música deixa tudo mais bonito.", "Tem som que eu repito o dia todo."],
      ["Vamos dançar.", "Que a música nunca pare.", "Foi lindo."], 3)
    F("allay_amizade", ("allay", "allay"), [],
      ["Eu fiquei amigo de um humano.", "Quando alguém é gentil comigo, eu não saio mais de perto.", "Sempre sigo quem me dá um item.", "Tenho um humano favorito."],
      ["Eu também, é o melhor jeito de viver.", "A gente se entende só de olhar.", "Eu fico por perto dele o dia todo.", "Eu levo coisas para ele."],
      ["Que bom que temos alguém.", "Vamos cuidar deles.", "Amizade é tudo."])

    # ================================================================== BICHOS DA MATA E OUTROS (entre os da mesma especie)
    def S(id, sp, a, b, c, need=None, w=3):
        F(id, (sp, sp), need or [], a, b, c, w)

    S("raposa_frutinha", "fox", ["Achei uma moita cheia de frutinhas vermelhas.", "Frutinha é o meu lanche favorito.", "Quem me dera ter frutinha todo dia.", "Hoje comi tanta frutinha que mal consigo andar."],
      ["Onde foi isso?", "Eu também adoro, mas ando atrás de galinha.", "Me leva lá.", "Eu como o que encontro."], ["Vamos lá juntos.", "Guarda um pouco para mim.", "Quem chegar primeiro, come mais."])
    S("raposa_astucia", "fox", ["Eu sempre dou um jeito de escapar.", "Sem fazer barulho, ninguém me percebe.", "Esperto mesmo é quem sabe a hora de sumir.", "Hoje enganei um lobo, juro."],
      ["Você é boa nisso.", "O segredo é não fazer barulho.", "Eu pulo e sumo.", "Eu ensinei você, lembra?"], ["Vamos treinar mais.", "Agora é a minha vez.", "Boa fuga."])
    S("raposa_neve", "fox", ["Na neve eu me escondo bem, ninguém me acha.", "O frio não me incomoda, o pelo é grosso.", "A neve é a minha casa.", "Gosto de brincar na neve fresquinha."],
      ["Branquinha nem se vê.", "Eu me enrolo no rabo e durmo.", "É frio, mas é bonito.", "Pelo menos o lobo não me acha."], ["Vamos nos esconder juntos.", "Fica quentinho.", "Que a neve não pare."], ["snow"], 4)
    S("lobo_alcateia", "wolf", ["A alcateia está inteira hoje.", "Sem alcateia, lobo nenhum é nada.", "Eu confio em quem corre ao meu lado.", "Ninguém fica para trás."],
      ["Eu fico na retaguarda.", "Onde vai um, vão todos.", "Cada um sabe o seu lugar.", "Quem mexe com um, mexe com todos."], ["Vamos em frente.", "A alcateia segue unida.", "Boa caçada."])
    S("lobo_lua", "wolf", ["A lua está cheia, dá vontade de uivar.", "Quando vejo a lua, não me seguro.", "Uivar para a lua é a minha parte favorita da noite.", "Hoje a lua está brilhando demais."],
      ["Vamos uivar juntos?", "Eu uivo mais alto que você.", "Eu só uivo quando tem lua.", "Acorda a floresta toda."], ["Auuuu!", "Foi um bom uivo.", "Mais um e vamos dormir."], ["night", "clear"], 4)
    S("coelho_cenoura", "rabbit", ["Achei uma cenoura enorme!", "Hoje sonhei com cenoura.", "Nada melhor que cenoura fresquinha.", "Se tiver cenoura, eu fico por perto."],
      ["Onde? Me leva!", "Eu só como a de cima.", "Guarda um pedaço para mim.", "É a melhor coisa do mundo."], ["Vamos comer juntos.", "Corre antes que alguém pegue.", "Que delícia."])
    S("coelho_medo", "rabbit", ["Ouvi um lobo uivando, estou tremendo.", "Qualquer barulho me assusta.", "Fico sempre perto da toca.", "A raposa passou por aqui, vi o rabo dela."],
      ["Corre para a toca!", "Eu também fico com medo.", "É melhor se esconder.", "Eu sempre tenho um plano de fuga."], ["Vamos nos esconder.", "Quando passar, a gente sai.", "Fica quietinho."])
    S("coelho_pulo", "rabbit", ["Vamos ver quem pula mais alto?", "Pulo uma, pulo duas, pulo três.", "Hoje pulei mais longe que nunca.", "Pular é a melhor forma de andar."],
      ["Aposto que eu pulo mais.", "Esse foi bom!", "Eu pulo de lado.", "Só não vale cair na toca."], ["Bora de novo.", "Boa pulada.", "Quem cansa primeiro perde."])
    S("panda_bambu", "panda", ["Bambu, bambu, bambu. O que mais eu quero?", "Esse bambu está uma delícia.", "Passo o dia mastigando bambu.", "Bambu fresquinho de manhã não tem igual."],
      ["Eu também só quero bambu.", "Me passa um pedaço.", "Só bambu me acalma.", "Come devagar, que dura mais."], ["Mais um pedaço e descansamos.", "Que delícia.", "Hora do cochilo."])
    S("panda_preguica", "panda", ["Hoje estou sem vontade de mexer nada.", "Ficar deitado é um talento.", "Preguiça é um modo de vida.", "Eu poderia dormir o dia inteiro."],
      ["Eu também, vem dormir.", "Rola comigo?", "Descansar é importante.", "Quem não descansa, não aproveita."], ["Hora do soninho.", "Fica quietinho.", "Boa soneca."])
    S("panda_espirro", "panda", ["Acabei de espirrar e me assustei sozinho.", "Dizem que meus espirros são engraçados.", "Atchim! Foi forte, né?", "Não sei por que espirro tanto."],
      ["Foi o maior que já ouvi!", "Eu dei risada.", "Saúde!", "Dá até susto."], ["Atchim!", "Saúde de novo.", "Vamos rir mais."])
    S("jaguatirica_selva", "ocelot", ["A selva é perfeita para se esconder.", "Ando sem fazer barulho nenhum.", "Gosto quando as folhas cobrem tudo.", "Ninguém me vê chegando."],
      ["Eu me escondo nas árvores.", "Eu também, silêncio é tudo.", "Quem te vê, nem percebe.", "É um bom lugar para caçar."], ["Fica quieto.", "Vamos caçar.", "Boa espreita."])
    S("jaguatirica_creeper", "ocelot", ["Os creepers fogem de mim, sabia?", "Quando chego, o creeper some.", "Dizem que sou o terror deles.", "Eu nem preciso fazer nada."],
      ["Eu adoro isso.", "Eles têm medo de tudo.", "É engraçado ver eles correndo.", "Fico de guarda na entrada."], ["Que fiquem longe.", "Já vão tarde.", "Boa defesa."])
    S("cabra_pulo", "goat", ["Dá para pular de uma pedra para outra sem cair.", "Gosto das montanhas altas.", "Quanto mais alto, melhor o pulo.", "As pedras são o meu quintal."],
      ["Eu pulo mais longe que você.", "Quero ver você tentar.", "As alturas me chamam.", "Cuidado para não escorregar."], ["Bora pular.", "Boa pulada.", "Vamos subir mais."])
    S("cabra_cabecada", "goat", ["Se alguém me irrita, dou uma cabeçada.", "Eu bato de leve, só para avisar.", "A cabeçada é o meu jeito de brincar.", "Não gosto de quem me atrapalha."],
      ["Eu também, é só um aviso.", "Quem mexe comigo leva.", "Só bato quem merece.", "É um bom método."], ["Que ninguém nos incomode.", "Vamos manter distância.", "Boa defesa."])
    S("lhama_caravana", "llama", ["Andar em fila é a coisa mais organizada do mundo.", "Quando a caravana passa, todo mundo olha.", "Eu sempre fico no meio da fila.", "Quem lidera a fila hoje?"],
      ["Eu levo a carga sem reclamar.", "Cada um com o seu lugar.", "Vamos manter o ritmo.", "A caravana nunca para."], ["Vamos em frente.", "Boa viagem.", "A fila é a nossa força."])
    S("lhama_cuspe", "llama", ["Eu cuspo em quem me incomoda.", "O cuspe é o meu aviso final.", "Hoje cuspi num lobo, foi ótimo.", "Cuidado, porque eu cuspo longe."],
      ["Eu também, é eficiente.", "O lobo saiu correndo.", "Eu uso só em último caso.", "Ninguém mexe com a gente."], ["Que fiquem longe.", "Foi merecido.", "Boa mira."])
    S("camelo_deserto", "camel", ["O deserto é imenso, mas a gente conhece cada duna.", "Eu aguento dias sem beber.", "A areia quente não me incomoda.", "Quem anda no deserto precisa de paciência."],
      ["Eu levo gente e carga sem reclamar.", "O segredo é andar devagar.", "A sede passa quando a gente se acostuma.", "O sol é nosso amigo e inimigo."], ["Vamos em frente.", "Boa travessia.", "Devagar chegamos."])
    S("camelo_corrida", "camel", ["Quando me animo, saio correndo na areia.", "Corro mais do que parece.", "Já fiz uma arrancada que levantou areia para todo lado.", "Todo mundo me subestima."],
      ["Eu também, é divertido.", "Quero ver essa arrancada.", "Eu corro com gente nas costas.", "Eu fico só olhando."], ["Bora correr.", "Foi uma bela arrancada.", "Poeira para todo lado."])
    S("sapo_charco", "frog", ["Nada melhor que um charco bem lamacento.", "Gosto de pular de folha em folha.", "O pântano é o meu cantinho favorito.", "A água parada é perfeita para descansar."],
      ["Eu fico em cima das folhas boiando.", "Croac, concordo.", "Prefiro a margem, mas gosto igual.", "Aqui é fresquinho."], ["Croac!", "Vamos pular.", "Boa soneca."])
    S("sapo_slime", "frog", ["Acabei de comer um slime pequeno, que delícia.", "Slimes são a minha comida favorita.", "Quando como um cubo de magma, a luz acende.", "Fico de olho nos slimes que pulam por perto."],
      ["Eu também, mas só os pequenos.", "Me conta onde achou.", "Eu gosto dos verdes.", "Eu só como quando tenho fome."], ["Croac, hora do lanche.", "Boa caça.", "Vamos procurar mais."])
    S("tatu_rolar", "armadillo", ["Quando tenho medo, eu me enrolo todo.", "Me enrolo e ninguém me atinge.", "Minha casca é uma proteção perfeita.", "Fico enrolado até o perigo passar."],
      ["Eu também, é o melhor truque.", "Só desenrolo quando não ouço nada.", "Funciona com lobo e tudo.", "É o meu esconderijo portátil."], ["Boa defesa.", "Vamos ficar atentos.", "Só saio quando tudo passar."])
    S("urso_gelo", "polar_bear", ["O gelo daqui é o melhor do mundo.", "Gosto de pescar nas fendas do gelo.", "Quando o filhote está por perto, ninguém se aproxima.", "A neve cobre tudo de branco."],
      ["Eu fico de olho nos filhotes.", "O frio não me incomoda.", "Peixe fresco é o melhor lanche.", "Esse gelo é bem grosso."], ["Vamos pescar.", "Cuida do filhote.", "Boa pescaria."], ["snow"], 4)
    S("sniffer_cheiro", "sniffer", ["Sinto cheiro de semente antiga por aqui.", "Meu nariz é a coisa mais importante que eu tenho.", "Cavo o chão só para achar cheiro.", "Nada como o cheiro de terra úmida."],
      ["Eu só cheiro o que vale a pena.", "O que você achou hoje?", "Eu sou o melhor farejador.", "Cada cheiro conta uma história."], ["Vamos cheirar mais.", "Boa busca.", "Quem acha, avisa."])
    S("mooshroom_ilha", "mooshroom", ["Aqui na ilha dos cogumelos tudo é tranquilo.", "Gosto de pastar entre os cogumelos.", "Meus cogumelos nas costas são o meu orgulho.", "Nenhum monstro aparece por aqui."],
      ["É o melhor lugar do mundo.", "Eu também me sinto em paz aqui.", "Os cogumelos crescem sem parar.", "Aqui só tem paz."], ["Vamos ficar por aqui.", "Fica à vontade.", "Nada de perigo."])

    # ================================================================== BICHOS DE ESPECIES DIFERENTES (cruzados)
    F("cruz_lobo_raposa", ("wolf", "fox"), [],
      ["Raposa, você anda sempre sozinha, né?", "Cuidado com a minha alcateia, raposa.", "Cada um na sua toca, combinado?", "Você é esperta, mas eu sou mais rápido."],
      ["Eu me viro bem sem ninguém.", "Eu sempre dou um jeito de escapar.", "Combinado, longe de mim.", "Eu só quero as frutinhas."],
      ["Então tudo certo.", "Cada um por si.", "Fica em paz."], 3)
    F("cruz_raposa_coelho", ("fox", "rabbit"), [],
      ["Coelho, não precisa fugir, hoje estou sem fome.", "Que cenoura é essa aí, coelho?", "Eu só vim passear.", "Pode ficar tranquilo, hoje eu como frutinhas."],
      ["Eu não confio em você.", "Hoje não, por favor.", "Eu me escondo na toca por via das dúvidas.", "Não chega muito perto."],
      ["Fica tranquilo.", "Boa sorte com a toca.", "Hoje é seu dia de sorte."], 3)
    F("cruz_gato_papagaio", ("cat", "parrot"), [],
      ["Papagaio, você fala demais.", "Que passarinho barulhento.", "Me imita de novo e vai ver.", "Eu só estou olhando você, papagaio."],
      ["Eu imito até você, gato.", "Prefiro voar longe de você.", "Miau, miau, miau!", "Você é muito ranzinza."],
      ["Hmpf.", "Que passarinho abusado.", "Eu fico só olhando."], 3)
    F("cruz_lobo_gato", ("wolf", "cat"), [],
      ["Gato, você é meio convencido, não é?", "Eu protejo todo mundo, e você?", "Você dorme o dia inteiro.", "Acorda, gato, tem lobo por aqui."],
      ["Eu durmo porque posso.", "Você faz barulho demais.", "Eu sou independente.", "Cada um com o seu jeito."],
      ["Cada um com seu estilo.", "Prefiro minha alcateia.", "Fica bem aí."], 3)
    F("cruz_vaca_lobo", ("cow", "wolf"), [],
      ["Lobo, por favor, não olha para o rebanho assim.", "Tem lobo por perto, fica de olho.", "Eu só estou pastando, não precisa se preocupar.", "Lobo, você não está com fome, né?"],
      ["Estou só passeando, vaca.", "Eu prefiro osso, fica tranquila.", "Não gosto de atrapalhar quem pasta.", "Hoje estou sem fome."],
      ["Então tá bom.", "Muuu, que susto.", "Fica longe do rebanho."], 3)
    F("cruz_ovelha_lobo", ("sheep", "wolf"), [],
      ["Lobo, não se aproxima, hein.", "Méé, tem lobo por perto!", "Eu só quero pastar em paz.", "Lobo, estou de olho em você."],
      ["Eu nem olho para a ovelha.", "Fica tranquila, sou só um guarda.", "Estou só de passagem.", "Eu cuido de quem é amigo."],
      ["Méé, que susto.", "Fica longe, então.", "Valeu pela calma."], 3)
    F("cruz_galinha_raposa", ("chicken", "fox"), [],
      ["Raposa! Tem raposa por perto, fujam!", "Raposa, não chega perto dos meus ovos.", "Có có, cuidado com a raposa!", "Eu só quero ciscar em paz."],
      ["Eu só estava passeando.", "Prometo que não encosto em nada.", "Hoje só quero frutinha.", "Quem disse que eu estou com fome?"],
      ["Có, acho bom.", "Fica longe do galinheiro.", "Estou de olho em você."], 3)
    F("cruz_lhama_camelo", ("llama", "camel"), [],
      ["Camelo, você conhece o deserto, né?", "Quem carrega mais, você ou eu?", "Eu levo carga nas montanhas, e você nas dunas.", "Nós dois somos bons de caravana."],
      ["Cada um com seu terreno.", "A areia é bem mais difícil.", "A gente se entende pela carga.", "Eu levo gente, você leva mais coisa."],
      ["Boa travessia.", "Que sejam bons caminhos.", "Cada um no seu ritmo."], 3)
    F("cruz_cavalo_lhama", ("horse", "llama"), [],
      ["Lhama, você anda devagar demais.", "Eu corro, você carrega. Combina.", "Já viu um cavalo galopar? Eu sou o melhor.", "Você tem um jeito de andar engraçado."],
      ["Cada um com seu ritmo.", "Eu aguento mais peso que você.", "Quem corre demais, cansa.", "Eu não preciso de pressa."],
      ["Combina bem.", "Boa viagem, então.", "A pé ou a galope, cada um chega."], 3)

    # generico para qualquer par de bichos
    A2 = ("animals", "animals")
    F("bicho_comida_geral", A2, [],
      ["O que você mais gosta de comer?", "Estou com fome, o que tem por aqui?", "Qual é o seu lanche favorito?", "Você já comeu hoje?"],
      ["Depende do dia, mas gosto de coisas frescas.", "Eu como o que aparece na frente.", "Hoje ainda não achei nada bom.", "Não posso reclamar."],
      ["Que delícia, então.", "Vamos procurar juntos.", "Bom apetite."], 3)
    F("bicho_sono_geral", A2, [],
      ["Onde você costuma dormir?", "Eu sempre procuro um cantinho quentinho.", "Hoje estou com um sono danado.", "Qual é o melhor lugar para descansar?"],
      ["Eu durmo onde der.", "Gosto de lugar escondido.", "Eu me encolho e pronto.", "Perto de alguém é melhor."],
      ["Boa soneca.", "Dorme bem.", "Que o sono seja bom."], 3)
    F("bicho_humano_geral", A2, [],
      ["Os humanos são esquisitos, não são?", "Já reparou como os humanos andam em pé?", "Os humanos têm cada ideia!", "Acho que eles não entendem a gente."],
      ["Eles falam demais.", "Mas alguns são legais.", "Eles nunca sabem para onde ir.", "Pelo menos trazem comida às vezes."],
      ["Cada um com seu jeito.", "A gente se entende.", "Fica de olho neles."], 3)
    F("bicho_medo_geral", A2, [],
      ["O que te dá mais medo?", "Eu tenho medo de barulho alto.", "Qualquer coisa estranha me deixa tenso.", "Nunca saio sozinho no escuro."],
      ["Eu fico nervoso com creeper.", "Gosto de ter alguém por perto.", "Tenho medo de ficar sozinho.", "Eu me escondo quando escuta algo."],
      ["Vamos ficar juntos.", "Ninguém precisa ter medo sozinho.", "Eu fico por perto."], 3)
    F("bicho_brincar_geral", A2, [],
      ["Vamos brincar um pouco?", "Hoje estou com vontade de correr.", "Tem alguma brincadeira nova?", "Quero me divertir antes de escurecer."],
      ["Eu topo.", "Qual brincadeira?", "Só se for rápido.", "Vamos ver quem chega primeiro."],
      ["Foi divertido.", "Amanhã tem mais.", "Que dia bom."], 3)
    F("bicho_amizade_geral", A2, [],
      ["A gente é de espécies diferentes, mas se entende.", "Gosto de conversar com gente de outro jeito.", "Cada bicho tem uma história.", "É bom ter vizinho de outro tipo."],
      ["Também acho.", "Aprendo muito com você.", "As diferenças deixam tudo mais interessante.", "Podemos aprender uns com os outros."],
      ["Fica por aqui.", "Boa conversa.", "A gente se vê."], 3)
    F("bicho_vento_geral", A2, [],
      ["O vento hoje está bom.", "Que dia bonito para ficar aqui.", "Dá para sentir o cheiro de longe.", "O tempo está calmo."],
      ["Gosto quando é assim.", "Tomara que dure.", "Dá vontade de ficar parado.", "O cheiro da relva está ótimo."],
      ["Vamos aproveitar.", "Bom dia para todos.", "Que seja sempre assim."], 3)
    F("bicho_lembranca_geral", A2, [],
      ["Lembra de quando éramos filhotes?", "Faz tempo que eu não corria assim.", "Já passei por cada coisa por aqui.", "Cada dia traz uma novidade."],
      ["Eu me lembro de tudo.", "Os tempos eram simples.", "Hoje é bem diferente.", "Foi bom enquanto durou."],
      ["Vamos fazer novas memórias.", "Fica comigo.", "Bom ter você."], 3)

    # ================================================================== MAIS ASSUNTOS PARA A FAZENDA E A ESTIMACAO
    S("vaca_balde", "cow", ["Às vezes um humano chega com um balde.", "O humano pega leite e vai embora sem dizer nada.", "Dar leite é o meu trabalho favorito.", "Não me importo de dar leite, se for com carinho."],
      ["Quem trata bem, eu dou.", "Eu só peço um pouco de trigo.", "Depois do balde, fico leve.", "É o que a gente faz de melhor."], ["Muuu, bom dia.", "Cada um com seu trabalho.", "Dar leite é uma honra."])
    S("porco_lama", "pig", ["Nada melhor que uma poça de lama.", "Hoje me sujei todo e adorei.", "Quem não gosta de lama não sabe viver.", "A lama é a melhor parte do dia."],
      ["Eu me rolo até ficar marrom.", "Quanto mais lama, melhor.", "Fica fresquinho.", "Minha lama é a mais gostosa."], ["Oinc, que delícia.", "Vamos nos sujar mais.", "A lama é vida."])
    S("ovelha_la", "sheep", ["Minha lã está enorme, preciso de uma tosa.", "A lã faz tanto calor no verão.", "Já reparou nas cores diferentes de lã?", "Ficar sem lã é uma sensação estranha."],
      ["Depois de tosada, fico leve.", "A minha é a mais macia.", "Eu me sinto bem com ela.", "Eu adoro quando alguém cuida."], ["Méé, boa lã.", "Fica quentinha.", "Que cor linda."])
    S("galinha_ovo", "chicken", ["Botei um ovo agora há pouco.", "Todo dia é dia de ovo.", "Ninguém bota ovo como eu.", "Quem pegar meu ovo tem que me avisar."],
      ["Eu também, hoje foi cedo.", "Os meus são sempre enormes.", "Eu fico cheia de orgulho de cada um.", "Dá trabalho, mas vale."], ["Có, parabéns.", "Que dia produtivo.", "Vamos ciscar mais."])
    S("cavalo_galope", "horse", ["Nada como um galope em campo aberto.", "Quando corro, o mundo desaparece.", "Eu fico de olho nos campos vazios.", "O vento no rosto é a melhor parte."],
      ["Eu também, é a melhor sensação.", "Adoro correr até me cansar.", "Quem monta em mim precisa de confiança.", "Eu corro mais que o vento."], ["Bora galopar.", "Prrr, foi demais.", "Mais uma volta."])
    S("lobo_osso", "wolf", ["Achei um osso enorme hoje.", "Nada como roer um osso no fim do dia.", "Guardo meus ossos num lugar secreto.", "Se um humano me der osso, eu sigo ele para sempre."],
      ["Onde foi isso?", "Eu também, é difícil resistir.", "Eu não conto onde guardo.", "Osso é coisa séria."], ["Que delícia.", "Guarda um pouco.", "Boa roída."])
    S("gato_sol", "cat", ["Achei o cantinho mais quente do mundo.", "O sol bate bem aqui, venha ver.", "Quando tem sol, eu não saio mais.", "Gosto de ficar parado olhando o nada."],
      ["Eu também, é um luxo.", "Me dá um pouquinho de espaço.", "Dá sono só de olhar.", "A vida é boa assim."], ["Miau, que paz.", "Boa soneca.", "Eu não saio daqui."], ["day", "clear"], 4)
