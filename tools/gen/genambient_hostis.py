"""Conversa de ambiente (mob com mob, sem IA) dos HOSTIS que ficavam de fora: Nether, End, os "outros" (slime, afogado, guardiao, saqueadores...) e mais
assuntos para os monstros comuns. Usado por genambient.py.

Tom: rude, grunhido, cada um no seu mundo; nunca simpatico (nada de amigo, prazer, obrigado, bonito, por favor: o AmbientCoherenceTest barra).
Do Nether nao se fala de sol, chuva nem hora do mundo de cima. Fatos de Minecraft so os que sao verdade.
Classes (campo who): nether + a especie (piglin, hoglin, strider, blaze, ghast, magma_cube, wither_skeleton, zombified_piglin, piglin_brute); monsters + a especie.
"""


def add(fam):
    def F(id, who, need, a, b, c, w=3):
        fam(id, list(who), need, a, b, c, weight=w)

    def S(id, sp, a, b, c, need=None, w=3):
        F(id, (sp, sp), need or [], a, b, c, w)

    NN = ("nether", "nether")

    # ================================================================== NETHER (entre si)
    F("nether_lava", NN, [],
      ["Esse lago de lava hoje está pior que ontem.", "Cuidado onde pisa, a lava não perdoa.", "O calor aqui dá até sede.", "A lava sobe quando ninguém olha."],
      ["Eu sempre ando longe da beirada.", "Já perdi gente nesse lago.", "O calor é o de sempre.", "Eu nem sinto mais."],
      ["Fica de olho no chão.", "Vamos para longe da lava.", "Cada passo conta."])
    F("nether_teto", NN, [],
      ["Já olhou para o teto? É só pedra preta até onde dá para ver.", "Dizem que acima do teto tem outro mundo.", "O teto daqui é feito de rocha que nada quebra.", "Aqui embaixo ninguém vê o céu."],
      ["Quem se importa com o céu?", "Prefiro o teto, mais seguro.", "Só os humanos olham para cima.", "Nunca vi o céu mesmo."],
      ["Melhor não pensar nisso.", "Cada um com o seu teto.", "Segue andando."])
    F("nether_humano", NN, [],
      ["{Jogador} atravessou o portal. Fiquem atentos.", "Cheiro de humano no nosso mundo.", "Tem alguém de fora andando por aqui.", "Os humanos nunca sabem o que estão fazendo."],
      ["Não é bem-vindo.", "Eu o vi chegando.", "Que fique longe do bastião.", "Vamos ver quanto tempo dura."],
      ["Fica de olho nele.", "Se atrapalhar, resolvemos.", "Ele que se cuide."], 4)
    F("nether_alma", NN, [],
      ["Já passou pelo vale das almas? Dá arrepio.", "A areia de lá faz tudo ficar mais lento.", "Escuta o vento no vale das almas.", "O azul daquele fogo me inquieta."],
      ["Prefiro ficar longe.", "Lá os ghasts choram sem parar.", "Fogo azul é coisa séria.", "Não gosto de lugar que sussurra."],
      ["Longe dali é melhor.", "Que fiquem as almas.", "Cada um com o seu medo."])
    F("nether_portal", NN, [],
      ["O portal está aceso de novo.", "Cada vez que o portal liga, alguém aparece.", "Tem algo roxo brilhando lá no fundo.", "Se o portal fechar, ninguém volta para casa."],
      ["Fica esperto.", "Eu fico longe dele.", "Quem passa por ele nunca é o mesmo.", "Esse roxo me incomoda."],
      ["Vamos deixar quieto.", "Melhor não chegar perto.", "Que se apague logo."])
    F("nether_basalto", NN, [],
      ["As colunas de basalto daqui são afiadas.", "O deserto de basalto não tem vida nenhuma.", "Gosto do silêncio das pedras pretas.", "Tem cinza caindo para todo lado."],
      ["Por isso ninguém vem para cá.", "Eu me escondo entre as colunas.", "É um bom lugar para quem gosta de paz.", "A cinza nem me incomoda."],
      ["Fica por aqui, então.", "Longe de tudo.", "Aqui ninguém nos acha."])
    F("nether_fome", NN, [],
      ["Estou com uma fome do tamanho do Nether.", "Será que sobrou algo para comer?", "Quem comeu minha carne?", "Quando eu como, esqueço da lava."],
      ["Procura nos cantos.", "Aqui nada sobra.", "Eu também estou com fome.", "Come o que achar."],
      ["Depois a gente procura mais.", "Quem chegar primeiro, come.", "Sem pressa."])

    # piglins
    S("piglin_ouro", "piglin", ["Ouro! Ouro! Onde tem ouro?", "Achei um lingote brilhante agora há pouco.", "Não existe nada mais brilhante que ouro.", "Um dia ainda vou ter um baú só de ouro."],
      ["Mostra aí, vai.", "Eu também só penso em ouro.", "Quem achar primeiro, fica.", "Eu guardo o meu debaixo da armadura."], ["Mais ouro, sempre.", "Vamos achar mais.", "Quem tem ouro, manda."])
    S("piglin_bastiao", "piglin", ["O bastião está bem guardado hoje.", "Ninguém passa pelo bastião sem ouro.", "Tem baú demais para um bastião só.", "Quem chegar perto, vai se arrepender."],
      ["Eu fico no portão.", "Os brutos estão de olho.", "Ninguém passa por mim.", "Cuidado com os humanos que chegam."], ["Vamos manter a guarda.", "O bastião é nosso.", "Que ninguém entre."])
    S("piglin_hoglin", "piglin", ["Hoje a caça é de hoglin.", "Estou de olho em um hoglin gordo.", "Uma boa caçada rende carne para todos.", "O hoglin não tem chance contra nós."],
      ["Eu pego o maior.", "Vamos juntos, então.", "Cuidado com as presas dele.", "A carne dele é a melhor."], ["Boa caçada.", "Quem trouxer mais, manda.", "Vamos atrás."])
    S("piglin_armadura", "piglin", ["Se o humano veste ouro, não ataco.", "Armadura de ouro deixa a gente em paz.", "Quem usa ouro é respeitado por aqui.", "Eu reconheço ouro de longe."],
      ["É a regra do bastião.", "Mas sem ouro, ele é presa.", "Eu só respeito quem vale.", "Se for dourado, passa."], ["Respeito é respeito.", "Ouro abre portas.", "Quem brilha, passa."])
    S("brutamontes_guarda", "piglin_brute", ["Eu não ligo para ouro. Eu guardo o bastião.", "Ninguém passa por mim.", "Meu machado nunca descansa.", "O bastião não tem folga."],
      ["Eu também fico de pé a noite inteira.", "Quem tenta entrar se arrepende.", "Os piglins pequenos só querem ouro.", "Eu faço o trabalho de verdade."], ["Segue de guarda.", "Cada um no seu posto.", "Ninguém entra."])
    S("zpiglin_passado", "zombified_piglin", ["Eu já fui um piglin, sabia?", "Lembro vagamente de ouro e de um bastião.", "Depois do portal, tudo mudou.", "Não sinto mais fome de ouro."],
      ["Eu também, mas não lembro de nada.", "Ficou só a raiva.", "O portal nos deixou assim.", "Tudo ficou lento."], ["Seguimos andando.", "O que passou, passou.", "Vamos em frente."])
    S("zpiglin_bando", "zombified_piglin", ["Se um de nós apanha, todos atacam.", "Ninguém mexe com um sem enfrentar o grupo.", "Estamos juntos até o fim.", "Se o humano bater em um, o bando inteiro vai."],
      ["Isso mesmo, a raiva é de todos.", "Eu fico atento.", "Ele que tente.", "É assim que sempre foi."], ["O bando é nossa força.", "Vamos ficar juntos.", "Fica por perto."])

    # hoglins
    S("hoglin_floresta", "hoglin", ["A floresta carmesim está cheia de fungos hoje.", "Adoro andar entre os fungos vermelhos.", "O cheiro da floresta é o melhor do Nether.", "Aqui tem comida para todo mundo."],
      ["Eu como bastante.", "Vamos explorar mais.", "É o único lugar que gosto.", "Tem sempre um fungo novo."], ["Vamos pastar mais.", "Fica por perto.", "O melhor lugar do mundo."])
    S("hoglin_fungo", "hoglin", ["Tem um fungo roxo ali, e eu não passo perto.", "Esse fungo roxo me dá nojo.", "Fico longe de tudo que é roxo.", "O cheiro daquele fungo me afasta."],
      ["Eu também fujo dele.", "Ele me deixa nervoso.", "Ninguém quer chegar perto.", "Quem planta isso quer nos afastar."], ["Vamos para outro lado.", "Fica longe.", "Que ele suma logo."])
    S("blaze_fogo", "blaze", ["Hoje estou pegando fogo de verdade.", "Quanto mais calor, mais eu brilho.", "Fogo é o meu elemento.", "Aqui o calor é perfeito."],
      ["Eu também estou em chamas.", "Brilha mais que ontem.", "Nada me apaga.", "Eu giro e solto fogo."], ["Vamos queimar tudo.", "Mais calor.", "Que não apague."])
    S("blaze_fortaleza", "blaze", ["A fortaleza está agitada hoje.", "Nossa fortaleza nunca fica vazia.", "Quem chegou à fortaleza se arrependeu.", "O gerador de fogo está a mil."],
      ["Eu protejo o corredor.", "Ninguém passa pelo meu andar.", "Os humanos só trazem problema.", "Eu atiro primeiro."], ["Fica de guarda.", "Ninguém entra.", "A fortaleza é nossa."])
    S("blaze_agua", "blaze", ["Odeio água. Qualquer gota me machuca.", "Uma bola de neve é o fim de qualquer um.", "Quem joga neve em mim vai se arrepender.", "Se eu tocar na água, dói."],
      ["Eu também, é o meu pesadelo.", "Neve é traição.", "Fique longe de qualquer coisa fria.", "Por isso eu fico aqui."], ["Que ninguém traga neve.", "Fica no calor.", "Longe da água."])
    S("ghast_choro", "ghast", ["Uuuh, estou chorando de novo.", "Hoje o dia foi triste demais.", "Eu choro quando ninguém está olhando.", "Quem me ouve achando assustador não sabe como dói."],
      ["Eu também choro.", "É o nosso jeito.", "As lágrimas valem muito.", "Chorar faz bem."], ["Uuuh...", "Vamos chorar juntos.", "Passa."])
    S("ghast_bolas", "ghast", ["Soltei uma bola de fogo e acertei longe.", "Tem humano rebatendo minhas bolas.", "Já viu o que acontece quando devolvem a bola?", "Quando devolvem a bola de fogo, eu me estrago."],
      ["É assustador.", "Eu desvio, por via das dúvidas.", "Eles aprenderam o truque.", "Só atiro de longe."], ["Mira melhor.", "Cuidado com o rebote.", "Fica atento."])
    S("magma_pulo", "magma_cube", ["Eu pulo, pulo e pulo, e não canso.", "Quando me parto, viro vários.", "Hoje fiz um pulo enorme.", "Ninguém pula como a gente."],
      ["Eu me divido em dois.", "Eu pulo mais alto.", "Cuidado com a lava.", "Não me divida, hein."], ["Pula mais.", "Que pulo!", "Vamos de novo."])
    S("wskel_fortaleza", "wither_skeleton", ["A fortaleza é fria, mas é nossa.", "Sou mais alto que qualquer esqueleto.", "Meu machado nunca perde o fio.", "Quem leva minha cabeça sabe o que faz."],
      ["Eu também tenho orgulho.", "Fique atento nos corredores.", "Os humanos nunca saem inteiros.", "Eu golpeio e já esqueço."], ["Fica de guarda.", "Ninguém passa.", "Vamos vigiar."])
    S("strider_lava", "strider", ["Nada melhor que andar sobre a lava.", "O lago de lava é a minha estrada.", "Quando saio da lava, começo a tremer.", "A lava quente me aquece por dentro."],
      ["Eu fico tremendo de frio fora dela.", "Vamos voltar para a lava.", "Eu nunca saio daqui.", "É o melhor lugar do Nether."], ["Vamos andar pela lava.", "Fica quentinho.", "Boa caminhada."])

    # ================================================================== END
    S("shulker_casca", "shulker", ["Fecho a casca e ninguém me alcança.", "Quem chega perto, leva um tiro.", "Fico parado a vida toda.", "A casca é a minha casa."],
      ["Eu também, é bem seguro.", "Disparo e me escondo.", "Quem se aproxima, flutua.", "É um bom jeito de viver."], ["Fica fechado.", "Que ninguém se aproxime.", "Boa guarda."])
    S("endermite_perola", "endermite", ["Vim de uma pérola, sabia?", "Cada pérola que cai, nasce um de nós.", "Odeio os endermans, eles me caçam.", "Fico escondido e pronto."],
      ["Também fujo deles.", "É um mundo difícil.", "Eu me escondo nas pedras.", "Todo mundo nos persegue."], ["Fica escondido.", "Cuidado com eles.", "Fica quieto."])

    # ================================================================== OUTROS HOSTIS
    S("slime_pulo", "slime", ["Eu pulo e faço blob, blob, blob.", "Quando me partem, viro vários.", "O pântano à noite é o melhor lugar para mim.", "Não existe nada mais divertido que pular."],
      ["Eu me divido em dois.", "Eu também pulo mais alto.", "Blob, blob!", "Cuidado para não se perder."], ["Vamos pular mais.", "Blob.", "Cada um do seu tamanho."])
    S("cave_spider_mina", "cave_spider", ["As minas abandonadas são nossas.", "Gosto de teia por todo canto.", "Quem entra na mina, leva uma picada.", "Aqui o escuro é perfeito."],
      ["Eu me escondo nas teias.", "O veneno é o nosso orgulho.", "Eles nem percebem a gente.", "Pequenos, mas perigosos."], ["Fica de tocaia.", "Ninguém passa.", "Mais teia."])
    S("silverfish_pedra", "silverfish", ["Me escondo dentro da pedra e ninguém percebe.", "Quando alguém quebra a pedra, saio aos montes.", "A fortaleza é cheia de nós.", "Pedra infestada é a nossa casa."],
      ["Eu me escondo bem.", "Todo mundo cai no truque.", "Quando um sai, vêm todos.", "Eles nem desconfiam."], ["Fica escondido.", "Vamos aparecer juntos.", "Boa tocaia."])
    S("husk_deserto", "husk", ["O sol do deserto nem me queima.", "Gosto de areia quente.", "Aqui os humanos passam com sede.", "Zumbis comuns queimam no sol, eu não."],
      ["Eu também, é uma vantagem.", "Vou rondar as dunas.", "A fome me deixa pior.", "O deserto é nosso."], ["Fica no calor.", "Quem passa, passa mal.", "Vamos rondar."])
    S("stray_neve", "stray", ["Minhas flechas deixam o inimigo lento.", "Na neve eu me escondo bem.", "O frio daqui é meu aliado.", "Gosto da neve estalando sob os pés."],
      ["As minhas também, vamos juntos.", "Quem é atingido, para.", "É a nossa especialidade.", "Aqui ninguém corre."], ["Fica de arco pronto.", "Boa mira.", "Que nevasque mais."], ["snow"], 4)
    S("afogado_tridente", "drowned", ["Achei um tridente hoje, vou acertar tudo.", "Quem tem tridente manda.", "Quando jogo, ninguém escapa.", "O tridente é a minha marca."],
      ["Eu só tenho garras.", "Me mostra!", "Eu também quero um.", "Jogo melhor que você."], ["Mira bem.", "Boa pontaria.", "Vamos afogar tudo."])
    S("afogado_agua", "drowned", ["Já faz tempo que estou debaixo d'água.", "Saímos à noite, na beira da água.", "O fundo é o meu lar.", "Gosto de ouvir as ondas quebrando."],
      ["Eu também, é o melhor lugar.", "Quando escurece, a gente sai.", "É frio, mas é nosso.", "A concha de nautilo é um tesouro."], ["Vamos subir à noite.", "A água é nossa.", "Fica no fundo."])
    S("guardiao_monumento", "guardian", ["O monumento está bem guardado.", "Quem chega perto do monumento, leva meu raio.", "Cuidado com os espinhos quando eu miro.", "Ninguém passa pelo meu olho."],
      ["Eu também fico de olho.", "Meu raio nunca falha.", "Aqui ninguém passa.", "O monumento é sagrado."], ["Fica de guarda.", "Ninguém entra.", "Mira firme."])
    S("guardiao_anciao", "elder_guardian", ["Eu sou o mais velho do monumento.", "Quem me enfrenta fica sem força para cavar.", "Já vi muita gente tentar e desistir.", "O monumento tem séculos."],
      ["Ninguém desafia o ancião.", "Tem respeito por aqui.", "Eu vigio por você.", "O fundo é nosso."], ["Fica de guarda.", "Que ninguém passe.", "O fundo é nosso."], None, 2)
    S("vex_voo", "vex", ["Eu atravesso paredes, é a melhor parte.", "Quem me invocou? Foi um evocador.", "Voo onde ninguém consegue me alcançar.", "Minha espada é pequena, mas dói."],
      ["Eu também sumo e apareço.", "Vamos voar juntos.", "O mestre manda e eu vou.", "Somos rápidos."], ["Vamos atacar.", "Voa mais rápido.", "Fica por perto."])
    S("fantasma_noite", "phantom", ["Quem não dorme vira alvo, e eu estou de olho.", "Gosto de voar em círculos por cima do humano.", "Quanto mais noites sem dormir, melhor.", "Quando o sol nasce, eu pego fogo."],
      ["Eu também, é um problema.", "Vamos mergulhar juntos.", "A insônia é nossa aliada.", "Fica no escuro."], ["Voa alto.", "Quando amanhecer, a gente some.", "Boa caçada."], ["night"], 4)
    S("saqueador_ataque", "pillager", ["A aldeia nem sabe o que vai acontecer.", "Hoje o ataque vai ser grande.", "A besta carrega tudo que precisamos.", "Quem tem bandeira, lidera."],
      ["Eu avanço pela esquerda.", "Vamos derrubar tudo.", "O capitão manda.", "Eles não têm chance."], ["Vamos atacar.", "Sem piedade.", "Em frente."])
    S("vingador_machado", "vindicator", ["Meu machado não tem dó de ninguém.", "Quem se coloca na frente, cai.", "Vou entrar pela porta da frente.", "Hoje é dia de invadir."],
      ["Eu fico no meu lado.", "Eu cuido dos guardas.", "Vamos juntos.", "Ninguém nos segura."], ["Em frente.", "Quebra tudo.", "Sem medo."])
    S("evocador_vex", "evoker", ["Eu invoco os vex, e eles obedecem.", "Quando levanto os braços, o chão se abre.", "O totem não adianta contra mim.", "Quem se aproxima vê presas sob os pés."],
      ["Eu fico atrás, é mais seguro.", "Seus vex são rápidos.", "Ninguém escapa das presas.", "É um truque e tanto."], ["Invoca mais.", "Ninguém passa.", "Mira bem."])
    S("ravager_investida", "ravager", ["Eu derrubo qualquer porta.", "Minha investida não tem freio.", "Os saqueadores confiam em mim.", "Quando eu corro, tudo cai."],
      ["Eu derrubo muros.", "Nada resiste à minha cabeçada.", "A aldeia vai cair.", "Eu sempre vou na frente."], ["Vamos derrubar.", "Mais força.", "Nada nos para."])
    F("raiders_totem", ("pillager", "vindicator"), [],
      ["Já viu o totem do evocador? Cuidado.", "O totem dele salva até quem não merece.", "Se o evocador vem junto, é mais fácil.", "A bandeira do capitão é o nosso símbolo."],
      ["Eu fico de longe.", "Sem ele, a gente tem problemas.", "Vamos seguir a bandeira.", "O capitão manda."], ["Em frente.", "Todos juntos.", "Sem medo."], 3)

    # ================================================================== MAIS ASSUNTOS PARA OS MONSTROS COMUNS
    S("zumbi_sol", "zombie", ["O sol me incomoda, vou procurar sombra.", "Quando amanhece, tenho que me esconder.", "A noite é muito melhor que o dia.", "Um dia ainda vou sair no sol e não me queimar."],
      ["Eu pego fogo rapidinho.", "Eu fico em baixo da árvore.", "Prefiro a caverna.", "A noite é nossa."], ["Fica na sombra.", "Esperar anoitecer.", "Noite boa."])
    S("zumbi_horda", "zombie", ["Hoje a horda está grande.", "Quando andamos juntos, ninguém nos para.", "O cheiro de humano me atrai.", "Tem uma aldeia perto e muita gente."],
      ["Eu sigo o grupo.", "Vamos em fila.", "Ninguém fica para trás.", "Eu sinto o cheiro também."], ["Em frente.", "Mais um passo.", "Cérebros."])
    S("esqueleto_arco", "skeleton", ["Meu arco nunca erra, só quando o vento atrapalha.", "Gosto de atirar de longe.", "Quem se aproxima de mim leva flecha.", "Cada flecha é um recado."],
      ["Eu também treino todo dia.", "Mira firme.", "O vento atrapalha demais.", "Só acerto quando fico parado."], ["Mira melhor.", "Boa pontaria.", "Fica de arco armado."])
    S("esqueleto_sol", "skeleton", ["O sol nos derrete, é um castigo.", "Quando amanhece, eu me escondo.", "A sombra é a nossa amiga.", "Quem inventou o dia?"],
      ["Eu fico dentro da caverna.", "O sol é nosso pior inimigo.", "Eu só saio à noite.", "Só as árvores nos salvam."], ["Fica na sombra.", "Esperar a noite.", "Noite boa."])
    S("creeper_chiado", "creeper", ["Ssss... já fiquei com vontade de explodir.", "Quando eu chio, todo mundo corre.", "Tem humano perto, eu sinto.", "Só chego perto quando ninguém olha."],
      ["Ssss, eu também.", "Tenho que me controlar.", "O melhor é a surpresa.", "O barulho é o aviso."], ["Ssss...", "Fica pronto.", "Quase."])
    S("creeper_gato", "creeper", ["Os gatos me dão medo.", "Quando um gato aparece, eu fujo.", "Eu não sei por quê, mas não aguento.", "Tem um gato por perto e já estou nervoso."],
      ["Eu também fujo deles.", "São terríveis.", "Eu nem chego perto.", "Melhor ficar longe."], ["Fica longe de gatos.", "Cuidado.", "Ssss."])
    S("aranha_teia", "spider", ["Subi até o teto e fiz uma teia enorme.", "Quem cai na minha teia nunca sai.", "Eu consigo andar na parede.", "Gosto da penumbra."],
      ["Eu também, é uma beleza.", "Minhas teias são mais fortes.", "A parede é o meu chão.", "De dia fico calma, de noite não."], ["Mais teia.", "Fica de tocaia.", "Cuidado."])
    S("enderman_olhar", "enderman", ["Nunca me olhe nos olhos.", "Quem me encara, eu persigo.", "Só queria carregar meus blocos em paz.", "O mundo tem blocos demais espalhados."],
      ["Eu também, é irritante.", "Cuidado com a água, ela me machuca.", "Eu me teleporto quando quero.", "Eu pego o que me interessa."], ["Fica atento.", "Sem encarar.", "Silêncio."])
    S("bruxa_pocao", "witch", ["Minha poção nova está quase pronta.", "Misturei tudo e deu um cheiro horrível.", "Uma boa poção resolve qualquer problema.", "Cuidado com o que eu levo no cinto."],
      ["Eu também gosto de misturar.", "Quero uma amostra.", "Eu fico de longe, por precaução.", "Qual é o efeito?"], ["Mais uma gota.", "Fica longe.", "Hehehe."])
    S("warden_vibracao", "warden", ["Sinto cada passo que dão.", "Escuto tudo, mesmo sem enxergar.", "Quem faz barulho é encontrado.", "O escuro é o meu lar."],
      ["Eu também.", "Cada vibração me chama.", "Fico quieto até ouvir algo.", "Ninguém me engana."], ["Silêncio.", "Mais um passo.", "Escuta."], None, 2)
