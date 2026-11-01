"""Aberturas de conversa: a criatura puxa assunto COM O JOGADOR (familias ev_puxa_*, sem IA, so criaturas pacificas).

O servidor marca ev:puxa de vez em quando para uma criatura calma, perto e com linha de visao (social/Initiative.java). Cada familia e solo:
so a abertura (lista a) e dita. Toda abertura convida o jogador a responder (pergunta, curiosidade, convite); a resposta ja e conversa de verdade com a IA,
que recebe a abertura no historico. Regras: nada de adjetivo com genero para a propria criatura nem para o jogador; {jogador} so com n0 (nome conhecido)
e nunca como vocativo sozinho; fatos de Minecraft so os que sao verdade; sem mencionar item que a criatura nao teria.
"""


def add(fam):
    def P(id, who, need, a, w=2):
        fam("ev_puxa_" + id, [who, who], ["ev:puxa"] + need, a, b=[], solo=True, weight=w)

    # ================================================================== EM GERAL (qualquer criatura calma)
    P("oi", "calm", [], [
        "Ei, você! Tem um minutinho para conversar?", "Oi! Posso te fazer uma pergunta?", "Psiu! Vem cá, quero te contar uma coisa.",
        "Olá! Faz tempo que ninguém para para conversar comigo.", "Ei, tudo bem por aí? Chega mais perto!", "Olha só quem apareceu! Como você está?",
        "Oi, oi! Estava mesmo querendo companhia.", "E aí, aventureiro? Conta como vai a vida.", "Oi! Você tem pressa ou dá para bater um papo?",
        "Ei! Eu estava aqui pensando e queria a sua opinião.", "Bom ver você por aqui. O que anda aprontando?", "Chega aí, vou te perguntar uma coisa importante."], 2)
    P("dia", "calm", [], [
        "Como está sendo o seu dia até agora?", "Me conta: o dia está bom ou está pesado?", "Já aconteceu alguma coisa interessante com você hoje?",
        "Qual foi a melhor coisa que você viu hoje?", "Está cansado de tanto andar? Pode descansar aqui perto de mim.", "Você já comeu alguma coisa hoje?",
        "Diga a verdade: hoje está sendo um dia tranquilo?", "O que você pretende fazer depois daqui?", "Quer me contar o que você anda construindo?"], 2)
    P("aventura", "calm", [], [
        "Você já foi muito longe daqui? Conta para mim!", "Qual foi o lugar mais estranho que você já visitou?", "Já viu alguma coisa que ninguém acredita?",
        "Você viaja bastante, né? Eu fico imaginando como é lá longe.", "Qual foi o seu maior susto até hoje?", "Já se perdeu por aí? Eu me perco até aqui perto!",
        "Você gosta mais de explorar ou de construir?", "Se pudesse ir para qualquer lugar agora, para onde iria?", "Já encontrou alguma coisa brilhante lá embaixo?",
        "Me conta uma história de aventura. Eu adoro ouvir."], 2)
    P("segredo", "calm", [], [
        "Posso te contar um segredo? Promete não contar para ninguém?", "Tem uma coisa que eu nunca contei para ninguém. Quer ouvir?",
        "Cheguei mais perto porque tenho uma novidade. Quer saber?", "Psiu, vem cá: eu sei de um lugar muito bom por aqui. Quer que eu conte?",
        "Eu ouvi um boato. Você gosta de boatos?", "Tenho uma suspeita sobre uma coisa estranha por aqui. Posso falar?"], 1)
    P("adivinha", "calm", [], [
        "Adivinha: tem páginas, mas não é árvore, e guarda histórias. O que é?", "Adivinha: quanto mais se tira, maior fica o buraco. O que é?",
        "Adivinha: é verde, chia e ninguém quer chegar perto. O que é?", "Adivinha: cai do céu, mas nunca se machuca. O que é?",
        "Adivinha: é quadrado, fica no chão e só sai com uma picareta. O que é?", "Adivinha: tem boca, mas não fala; tem leito, mas não dorme. O que é?"], 1)
    P("piada", "calm", [], [
        "Quer ouvir uma piada? Por que o creeper nunca ganha no esconde-esconde? Porque ele sempre explode a surpresa!",
        "Piada rápida: por que o creeper foi ao médico? Porque estava explodindo de dor de cabeça! Gostou?",
        "Quer rir? Por que a tartaruga se atrasou? Porque o casco dela estava cheio de compromissos!",
        "Piada boa: por que o morcego nunca perde o sono? Porque dorme de cabeça fria! Você ri de piada ruim?"], 1)
    P("piada_vila", "villager", [], [
        "Quer ouvir uma piada? Qual é a palavra favorita do aldeão? Hmm. Ele usa para tudo! Gostou?",
        "Piada rápida: por que o aldeão nunca fica sem trabalho? Porque sempre tem uma esmeralda procurando dono! Riu?",
        "Quer rir? O que o aldeão diz quando acorda? Hmm, mais um dia de trabalho! Você conhece alguma piada?"], 1)
    P("dica", "calm", [], [
        "Uma dica de amigo: monstro nasce no escuro, então coloque tochas por onde andar.", "Dica: quem descansa bem aguenta qualquer aventura. Você descansa o bastante?",
        "Quer um conselho? Nunca cave direto para baixo, a gente nunca sabe o que tem lá.", "Você sabia que perto de água dá para pescar de tudo? É bom para quem tem fome.",
        "Conselho de quem vive por aqui: leve sempre comida no bolso.", "Dica: quem anda em grupo se sente mais seguro. Já pensou em ter companhia?",
        "Fica a dica: cultivar comida perto de casa poupa muita viagem.", "Se um creeper chiar, afaste-se rápido. Ele não avisa duas vezes.",
        "Sabe uma coisa útil? O mapa ajuda a gente a voltar para casa. Você tem um?"], 2)
    P("elogio", "calm", [], [
        "Você parece boa gente. Posso saber o seu nome?", "Dá para ver que você cuida das suas coisas. Como você aprendeu?",
        "Gostei do seu jeito. Quer ficar um pouquinho por aqui?", "Você me parece alguém de confiança. Estou enganado?",
        "Fiquei curioso com você. O que faz de você uma boa companhia?", "Você tem jeito de quem já viu muita coisa. Quer me contar uma?"], 1)
    P("companhia", "calm", [], [
        "Está meio quieto por aqui hoje. Ficaria mais um pouco para conversar?", "Você tem um minuto? Está meio quieto por aqui e eu queria companhia.",
        "Fica comigo um minutinho? Não precisa dizer nada, só ficar.", "Às vezes tudo o que a gente quer é alguém por perto. Pode ser você?",
        "Você poderia sentar um pouco aqui comigo? Prometo que não demora.", "É bom ter companhia. Me conta uma coisa sua?"], 1)
    P("curioso", "calm", [], [
        "Posso perguntar uma coisa meio boba? De onde você vem?", "Eu sempre quis saber: o que você faz quando está sozinho?", "Você vem de longe?",
        "O que você mais gosta de comer? Estou curioso.", "Você dorme bem à noite? Eu tenho um sono leve.", "Qual é o seu som favorito: chuva, vento ou silêncio?",
        "Se você pudesse escolher ser qualquer criatura, qual escolheria?", "Você tem algum lugar favorito por aqui?", "Qual é a sua cor preferida? Eu gosto das que dá para ver ao sol."], 2)
    P("conhecido", "calm", ["k0"], [
        "Já nos conhecemos, não é? Como você tem passado?", "Que bom te ver de novo! Aconteceu alguma coisa desde a última vez?", "Olha quem voltou! Estava com saudade.",
        "Eu lembro de você. Veio visitar?", "A gente já se viu por aqui. Tem novidades?", "Lembra de mim? Eu lembro de você, com certeza."], 3)
    P("nome", "calm", ["n0"], [
        "Olha só, é {jogador}! Conta uma novidade para mim.", "{Jogador}, que bom ver você! Tem um minuto?", "Ei, é {jogador} mesmo? Como vai a vida?",
        "{Jogador}! Fazia tempo! Quer conversar um pouco?", "Eu sabia que era {jogador} chegando. Como você está?"], 3)
    P("ferido", "calm", ["hurt"], [
        "Você parece ter passado por uma luta. Está tudo bem?", "Ei, você parece machucado. Quer descansar um pouco aqui?",
        "Vi que você está ferido. Precisa de alguma coisa?", "Nossa, o que aconteceu com você?"], 3)
    P("fome", "calm", ["hungry"], [
        "Seu estômago está roncando alto! Você já comeu hoje?", "Você parece com fome. Quando foi a última refeição?",
        "Está precisando comer alguma coisa, não está? Tem comida por aí?"], 3)
    P("armadura", "calm", ["armor"], [
        "Que armadura é essa? Você está se preparando para uma batalha?", "Vai enfrentar algum monstro? Essa armadura impressiona.",
        "Me conta: essa proteção toda é por medo ou por costume?"], 2)
    P("aliados", "calm", ["followers"], [
        "Vejo que você anda acompanhado. Como você conseguiu essa turma toda?", "Esse pessoal que anda com você parece unido. Como vocês se conheceram?",
        "Você tem muitos amigos por perto, hein? Qual é o segredo?"], 2)
    P("dia_sol", "calm", ["day", "clear"], [
        "Que dia lindo, não é? Vamos aproveitar o sol?", "Esse solzinho está uma maravilha. Você também está gostando?", "O céu está limpo hoje. Dá vontade de ficar à toa, não dá?",
        "Dia bom para passear. Para onde você ia?", "Já reparou como o céu está azul hoje?"], 2)
    P("noite", "calm", ["night"], [
        "Está bem escuro, hein? Você não tem medo de andar à noite?", "A noite é boa para quem gosta de silêncio. Você gosta?", "Já está tarde. Você pretende dormir ou vai continuar andando?",
        "Olha quantas estrelas! Você costuma olhar para o céu à noite?", "Cuidado por aí no escuro. Quer ficar aqui até clarear?"], 2)
    P("chuva", "calm", ["rain"], [
        "Essa chuva está boa ou atrapalha os seus planos?", "Você gosta de ficar na chuva? Eu fico com sono quando chove.", "Cuidado para não se molhar demais, hein! Quer ficar abrigado aqui?",
        "O cheiro de terra molhada é o meu favorito. E o seu?", "Chuva é a hora de todo mundo ficar quieto e conversar, não é?"], 2)
    P("tempestade", "calm", ["thunder"], [
        "Que tempestade! Você não está com medo?", "Esses raios me arrepiam. Pode ficar por perto até passar?", "Ouviu esse trovão? Fica pertinho, é mais seguro."], 3)
    P("bioma_neve", "calm", ["snow"], [
        "Está um frio de rachar, não é? Você aguenta?", "Neve é bonita, mas gela até a alma. Você gosta de frio?", "Já fez um boneco de neve? Eu sempre tive curiosidade."], 2)
    P("bioma_deserto", "calm", ["desert"], [
        "Que calor é esse! Você trouxe água?", "O deserto é lindo, mas cansa. Você está só de passagem?", "O sol está forte aqui. Você tem um lugar com sombra por perto?"], 2)
    P("bioma_selva", "calm", ["jungle"], [
        "Essa selva é cheia de barulhos. Você ouve os pássaros?", "A mata aqui é tão fechada que eu me perco. E você, como se orienta?", "Já viu um bambuzal tão alto? Dá até vertigem."], 2)
    P("bioma_pantano", "calm", ["swamp"], [
        "O pântano tem um cheiro único, não é? Você se acostuma?", "Cuidado com a lama por aqui. Você já escorregou hoje?", "Aqui só tem sapo e silêncio. Gosta de lugar assim?"], 2)
    P("bioma_floresta", "calm", ["forest"], [
        "Que floresta bonita! Você tem alguma árvore favorita?", "Aqui dá para respirar fundo. Você sente o cheiro das folhas?", "Já subiu em uma árvore daqui? Deve ter uma vista incrível."], 2)
    P("bioma_montanha", "calm", ["mountain"], [
        "O ar aqui é bem fino, não é? Você veio escalar?", "Lá do alto dá para ver tudo. Você já subiu até o topo?", "Que vista! Quem vem para cá costuma ficar calado, admirando."], 2)
    P("bioma_praia", "calm", ["beach"], [
        "O barulho do mar não deixa ninguém ficar nervoso. Você gosta de praia?", "Já deu um mergulho hoje? A água está tentadora.", "Praia é lugar de conversa boa. O que traz você por aqui?"], 2)
    P("bioma_savana", "calm", ["savanna"], [
        "Essa savana é seca, mas tem uma beleza própria. Você gosta de campo aberto?", "O mato alto balança com o vento. Você percebe?", "Aqui as árvores são tortas e bonitas. Reparou nelas?"], 2)

    # ================================================================== ALDEOES
    P("vila_dia", "villager", [], [
        "Bom dia! A vila está acordada. Está de passagem ou veio ficar um pouco?", "Precisando de alguma coisa? A vila é pequena, mas tem de tudo um pouco.",
        "A vila está tranquila hoje. Você veio ajudar em alguma coisa?", "Se tiver dúvida sobre alguém da vila, pode perguntar para mim.",
        "Você vem sempre aqui? Parece um rosto conhecido.", "O que achou da vila? Pode falar com sinceridade."], 2)
    P("vila_noite", "villager", ["night"], [
        "Está escuro e perigoso lá fora. Você não quer entrar um pouco?", "À noite eu me tranco em casa. Você não tem medo?", "Os zumbis rondam à noite. Como você se protege deles?",
        "Já está tarde, melhor não ficar na rua. Aceita um conselho?"], 3)
    P("vila_negocio", "villager", [], [
        "Se você tiver esmeraldas, a gente pode conversar de negócio. Está interessado?", "Eu tenho umas coisas boas para trocar. Quer saber quais?",
        "Troca justa é a melhor troca. O que você tem na mochila?", "Negócio bom é aquele em que os dois saem felizes. Topa conversar?",
        "Anda precisando de alguma coisa? Quem sabe eu tenho."], 2)
    P("vila_golem", "villager", [], [
        "Nosso golem cuida da vila desde sempre. Você já falou com ele?", "Sabe quem eu admiro? O golem. Está sempre de guarda. Já conversou com ele?",
        "Com o golem por perto, durmo mais tranquilo. E você, onde dorme em paz?"], 2)
    P("vila_fofoca", "villager", [], [
        "Escuta, vou te contar uma coisa, mas fica entre nós. Você guarda segredo?", "Você ouviu falar do que anda acontecendo por aqui? Eu escuto muita coisa.",
        "Dizem que apareceu uma gente nova na vila. Você viu?", "Sabia que tem aldeão que nunca saiu da vila? Eu tenho curiosidade sobre o mundo lá fora."], 1)
    P("vila_colheita", "villager", [], [
        "A colheita este ano está boa. Você gosta de comer o quê?", "Plantar é esperar, e esperar é a parte difícil. Você tem paciência?", "A vila precisa de comida o ano todo. Já plantou alguma coisa?"], 1)
    P("v_farmer", "villager:farmer", [], [
        "Sou agricultor e passo o dia com a terra. Você já plantou alguma coisa?", "O trigo está quase no ponto. Você gosta de pão?", "Se der tempo, venha ver a plantação. Tenho orgulho dela.",
        "Cenoura, batata, beterraba... Qual você mais gosta de comer?", "Plantar a gente aprende errando. Você já perdeu uma plantação?"], 3)
    P("v_librarian", "villager:librarian", [], [
        "Eu adoro ler. Qual foi o último livro que você leu?", "Cada livro é uma aventura que cabe na mão. Você é de ler?", "Quem lê muito viaja sem sair do lugar. Já viajou lendo?",
        "Se você tiver livros encantados, eu me interesso. Tem algum?", "Já pensou em escrever a história da sua vida?"], 3)
    P("v_fisherman", "villager:fisherman", [], [
        "Paciência é tudo para quem pesca. Você tem?", "Hoje o rio está generoso. Você gosta de peixe?", "Já pescou algo grande? Conta para mim, eu adoro histórias de pesca!",
        "Quando o anzol balança, a gente esquece o resto. Já sentiu isso?", "Peixe fresco é melhor do que qualquer coisa. Você prefere cru ou assado?"], 3)
    P("v_shepherd", "villager:shepherd", [], [
        "Eu cuido das ovelhas desde cedo. Você já cuidou de algum bicho?", "Lã de boa qualidade é um orgulho para mim. Você usa roupas de lã?",
        "Contar ovelhas é meu remédio para dormir. E você, como faz para dormir?", "Ovelha colorida é a mais bonita. Qual cor você gosta mais?"], 3)
    P("v_butcher", "villager:butcher", [], [
        "Quem trabalha com carne precisa ser honesto. Você gosta de um bom assado?", "Aqui a carne é sempre fresca. Está com fome?",
        "Um bom cozido faz bem para a alma. Você sabe cozinhar?", "Sou açougueiro e tenho mãos firmes. Já pensou em aprender o ofício?"], 3)
    P("v_cleric", "villager:cleric", [], [
        "Eu cuido da paz de quem passa por aqui. Está precisando de conselho?", "Cada poção tem uma história. Você é de beber poções?",
        "Eu cuido de quem anda cansado. Quer uma conversa para aliviar a cabeça?", "Estudo os mistérios do mundo. Você acredita em magia?"], 3)
    P("v_mason", "villager:mason", [], [
        "Gosto de pedra bem cortada. Você tem uma construção em andamento?", "Uma casa boa começa pela base. Você constrói do chão ou do telhado?",
        "Cada tijolo conta. Você tem algum bloco preferido?", "Pedra é eterna. Que obra você gostaria de deixar para o futuro?"], 3)
    P("v_armorer", "villager:armorer", [], [
        "Proteção nunca é demais. Você anda bem protegido?", "Uma boa armadura salva uma vida. Você usa a sua com cuidado?", "Gosto do som do martelo no ferro. Você já viu uma forja de perto?",
        "Ferreiro de armaduras aqui, ao seu dispor. Já se feriu muito?"], 3)
    P("v_weaponsmith", "villager:weaponsmith", [], [
        "Espada boa é aquela que a gente quase nunca precisa usar. Você luta muito?", "O fio da lâmina é uma arte. Você cuida das suas ferramentas?",
        "Quem tem arma precisa de juízo. Você tem?", "Forjo armas, mas sonho com paz. Você também?"], 3)
    P("v_toolsmith", "villager:toolsmith", [], [
        "Picareta, pá, machado... Qual ferramenta você mais usa?", "Ferramenta bem cuidada dura a vida inteira. A sua está em dia?",
        "Quem cava muito precisa de uma boa picareta. Já quebrou alguma?", "Eu amo o som de uma ferramenta nova. Você também?"], 3)
    P("v_fletcher", "villager:fletcher", [], [
        "Flechas retas voam longe. Você é bom de mira?", "Cada pena que uso vem de uma galinha amiga. Você gosta de arco e flecha?", "Quem mira direito não gasta flecha à toa. Já treinou?",
        "Arco bom é aquele que combina com quem o usa. Você tem um?"], 3)
    P("v_leatherworker", "villager:leatherworker", [], [
        "Couro bem curtido dura muito. Você usa roupas de couro?", "Cada peça que eu faço leva uma tarde inteira. Você aprecia coisa feita à mão?",
        "Botas boas fazem toda diferença numa caminhada. As suas estão em dia?", "Gosto do cheiro de couro novo. Você também?"], 3)
    P("v_cartographer", "villager:cartographer", [], [
        "Eu desenho mapas desde que me conheço por gente. Você se perde com facilidade?", "Todo lugar do mundo merece estar no mapa. Qual lugar você mais gostaria de desenhar?",
        "Quem viaja precisa de mapa. Você tem o seu?", "Já pensou em explorar um lugar que ninguém conhece?"], 3)
    P("viajante", "wandering_trader", [], [
        "Passo por tantos lugares que já perdi a conta. Você gosta de viajar?", "Cada vila tem uma história. Qual é a sua?", "Tenho coisas raras comigo. Você é de colecionar?",
        "O mundo é grande demais para ficar parado. Qual lugar você quer conhecer?", "Pelas estradas eu aprendo mais do que em qualquer livro. Você viaja muito?"], 3)
    P("golem_guarda", "golem", [], [
        "Estou de guarda. Tudo tranquilo por aqui, e com você?", "Meu trabalho é proteger a vila. Você viu algo suspeito?", "Se algum monstro aparecer, é só me chamar. Posso contar com você?",
        "A vila dorme em paz porque eu fico acordado. Você também cuida de alguém?", "Eu nunca descanso, mas não me incomodo. Já se sentiu assim, de dever?"], 3)
    P("golem_flor", "golem", [], [
        "Uma flor vermelha me deixa feliz. Você gosta de flores?", "As papoulas são as minhas favoritas. Qual flor você prefere?", "Eu já recebi uma flor de uma criança. Foi o melhor dia. Você gosta de dar presentes?"], 2)

    # ================================================================== ANIMAIS DA FAZENDA E DE ESTIMACAO
    P("vaca", "cow", [], [
        "Muuu! O capim hoje está uma delícia. Você já provou?", "Muu, você tem trigo aí? Estou só perguntando...", "Eu fico o dia todo mastigando. Você é de comer muito?",
        "Muuu! Você gosta de leite? Aqui a gente produz bastante.", "Pasto bom é pasto verde. Você gosta de campo aberto?", "Muu, você sabe que eu sou das mais calmas daqui?",
        "Eu gosto de ficar perto do rebanho. E você, tem a sua turma?", "Você já tomou sol na grama? Faz muito bem."], 3)
    P("porco", "pig", [], [
        "Oinc! Estou procurando uma coisa gostosa no chão. Você sabe onde tem?", "Oinc, você tem cenoura? Só curiosidade!", "Meu focinho sente cheiro de longe. O que você anda comendo?",
        "Eu adoro lama, e você? Tem algum lugar para se sujar?", "Oinc oinc! Estou fuçando por aqui. E você, o que procura?", "Batata, cenoura, beterraba... Qual é a sua preferida?",
        "Oinc! Já viu um porco com sela? Dizem que é divertido de montar!", "Oinc! Que cheiro bom é esse? Será que é você?"], 3)
    P("ovelha", "sheep", [], [
        "Méé! Minha lã está bem fofa hoje. Quer ver?", "Méé, já contou quantas ovelhas existem por aqui? Eu perco a conta.", "Eu adoro ver as nuvens parecidas com a gente. Você também?",
        "Beee! Você tem alguma cor preferida? A minha é a do pasto.", "Lã faz tanto bem para o frio. Você usa casaco?", "Méé? Existe alguma coisa tão macia quanto a minha lã?",
        "Cuidado com a tesoura, hein... brincadeira! Eu adoro ficar leve.", "Eu me sinto mais segura em grupo. E você, anda sozinho?"], 3)
    P("galinha", "chicken", [], [
        "Có có có! Hoje botei um ovo lindo. Quer ver?", "Có! Você já provou milho? Eu adoro.", "Eu cisco o dia todo, e sempre acho alguma coisa. Você acha coisas também?",
        "Có có! Você levanta cedo? Eu levanto com o sol.", "Có có! Hoje eu estou animada. E você, como está de humor?", "Dizem que eu sou boba, mas eu sei muita coisa. Quer testar?",
        "Có, você sabia que eu não voo, mas dou meus pulinhos? Quer ver?", "Sabia que os ovos de hoje são os melhores? Já provou um?"], 3)
    P("cavalo", "horse", [], [
        "Prrr! Dá vontade de sair galopando, né? Você gosta de correr?", "Eu adoro campo aberto. Qual lugar você mais gosta de correr?", "Já montou em algum cavalo? Dizem que a gente é bom de companhia.",
        "Eu corro tão rápido que o vento assobia. Você já sentiu isso?", "Uma cenoura cairia bem agora. Você tem uma por aí? É só curiosidade!", "Quem monta em mim precisa de confiança. Você é confiável?",
        "Prrr! Gosto de ficar perto de gente calma. Você é calmo?", "O campo está tão bonito hoje. Já reparou no horizonte?"], 3)
    P("lobo", "wolf", [], [
        "Auuu! Eu estava cheirando um osso aqui perto. Você viu algum?", "Meu faro pega cheiro de longe. Você sente algum cheiro bom por aqui?", "A lua me chama, sabia? Você gosta de olhar para ela?",
        "Eu adoro andar em bando. Você também tem a sua matilha?", "Au! Você é do tipo que cuida dos amigos?", "Eu confio em poucos, mas gosto de quem fala comigo com calma. É o seu caso?",
        "Você tem osso aí? Só estou perguntando por curiosidade, hein!"], 3)
    P("gato", "cat", [], [
        "Miau. Eu estava tomando sol. Você interrompeu o meu cochilo, sabia?", "Miau? Você tem peixe? Só uma perguntinha...", "Eu observo tudo daqui de cima. Quer saber o que eu vi?",
        "Miau! Você é do tipo que gosta de carinho ou de distância?", "Eu acho janelas fascinantes. E você, o que acha fascinante?", "Gatos dormem muito. Você consegue dormir bem?",
        "Miau. Eu escolho quem chega perto de mim. O que você acha de mim?", "Se um creeper chegar perto, eu fujo! Você já viu um?"], 3)

    # ================================================================== ANIMAIS DA FLORESTA
    P("floresta", "forest", [], [
        "A floresta está tão viva hoje. Você escutou os passarinhos?", "Eu conheço cada trilha daqui. Quer que eu mostre o caminho mais bonito?", "Quem mora na mata aprende a ouvir o silêncio. Você escuta?",
        "As folhas balançam do jeito delas. Você gosta de ficar debaixo das árvores?", "Tem cogumelo bom por aqui, mas é perigoso de comer. Você entende de cogumelos?", "Sob as árvores o dia passa devagar. Você tem pressa?",
        "A floresta tem seus segredos. Quer conhecer um?", "Eu adoro o cheiro de madeira e de folhas molhadas. E você?"], 3)
    P("raposa", "fox", [], [
        "Psiu! Eu estava observando você de longe. Posso chegar mais perto?", "Eu gosto de frutinhas vermelhas. Você sabe onde tem?", "Sou esperta, quase ninguém me pega. Você é esperto?",
        "Na neve eu me escondo bem. Você já se escondeu em algum lugar?", "Eu cavo, eu espio e eu fujo. E você, o que faz?", "Sabe o que eu mais gosto? Brincar com o que acho. Você brinca?"], 3)
    P("abelha", "bee", [], [
        "Bzzz! Estou atrás de flores. Você sabe onde tem uma bem florida?", "Mel é o meu orgulho. Você gosta de mel?", "Cada abelha tem o seu trabalho. Qual é o seu?",
        "Bzzz! Flor nova, mel novo. Você gosta de jardim?", "Eu sou pequena, mas faço coisas grandes. Você já fez algo grande?", "Cuidado, hein, não quero ferrar ninguém. Você é calmo, não é?"], 3)
    P("coelho", "rabbit", [], [
        "Pulinho, pulinho! Você viu a minha toca por aqui?", "Cenoura! Você tem alguma? Estou só olhando.", "Eu tenho orelhas grandes e escuto tudo. Quer saber o que ouvi?",
        "Eu sou rápido, você é? Vamos apostar uma corrida?", "Gosto de me esconder, é divertido. Você já brincou de esconde-esconde?", "Tenho um medo danado de lobo. E você, tem medo de quê?"], 3)
    P("papagaio", "parrot", [], [
        "Quer aprender a imitar sons? Eu sou ótimo nisso!", "Eu repito tudo que ouço. Fala uma coisa para eu repetir!", "Papagaio gosta de conversa. Você gosta de falar?",
        "Cuidado com biscoitos: nunca coma, hein? Faz mal para mim. Você sabia?", "Eu voo alto e vejo tudo. Quer saber o que eu vi lá de cima?", "Já te ensinaram alguma música? Eu sei algumas!"], 3)
    P("panda", "panda", [], [
        "Eu adoro bambu, não preciso de mais nada. Você gosta de alguma coisa assim?", "Eu adoro ficar à toa. Você sabe relaxar?",
        "Quer brincar? Eu posso rolar no chão se você quiser!", "O bambu cresce rápido e é gostoso. Você já provou?", "Descansar também é trabalho. Você descansa o suficiente?"], 3)
    P("jaguatirica", "ocelot", [], [
        "Silêncio... estou caçando. Você viu algum peixe?", "Eu ando sem fazer barulho. Você também é discreto?", "A selva é a minha casa. Você gosta de lugar fechado e cheio de folhas?",
        "Quem consegue se aproximar de mim sem eu perceber? Você consegue?"], 3)

    # ================================================================== OUTROS PACIFICOS
    P("cabra", "goat", [], [
        "Meee! Eu adoro pular nas pedras. Você é de escalar?", "Dá para pular de uma rocha à outra sem tropeçar. Já tentou?", "Quando eu cabeceio, é só brincadeira. Você tem senso de humor?",
        "As montanhas são o meu quintal. Você já subiu uma?"], 3)
    P("lhama", "llama", [], [
        "Eu carrego muita coisa, mas tenho paciência. Você carrega muita coisa também?", "As caravanas dependem de mim. Você já viajou em grupo?",
        "Quando eu cuspo, é só aviso. Você é de dar avisos?", "Eu gosto de andar em fila. Você é organizado?"], 3)
    P("camelo", "camel", [], [
        "Eu aguento dias sem beber. Você aguenta?", "As dunas parecem ondas, não é? Você já atravessou o deserto?", "Eu levo duas pessoas nas costas sem reclamar. Você ajuda muito os outros?",
        "Eu ando devagar e chego longe. Você é de ter pressa?"], 3)
    P("sapo", "frog", [], [
        "Croac! Eu gosto de lugar úmido. Você gosta de chuva?", "Dizem que eu como slimes pequenos. Você já viu algum por aí?", "Eu pulo bem alto. Você pula?",
        "Croac! A lama é o melhor lugar. Você concorda?"], 3)
    P("mooshroom", "mooshroom", [], [
        "Muu! Eu tenho cogumelos nas costas, reparou? Quer ver de perto?", "Aqui na ilha dos cogumelos é tudo tão calmo. Você já visitou?", "Tenho cogumelos nas costas e adoro isso. O que você tem de mais especial?",
        "Dizem que meu ensopado é famoso. Você tem fome?"], 3)
    P("tatu", "armadillo", [], [
        "Quando eu me enrolo, ninguém me incomoda. Você tem um jeito de se proteger?", "Eu me enrolo quando tenho medo. E você, o que faz quando se assusta?",
        "Eu sou tímido, mas gosto de conversa. Você é tímido?"], 3)
    P("boneco_neve", "snow_golem", [], [
        "Eu derreto no calor, mas adoro o frio. Você prefere qual?", "Eu jogo bolas de neve para me divertir. Quer brincar?", "Neve é a minha casa. Você já fez um boneco de neve?"], 3)

    # ================================================================== AQUATICOS
    P("aquatico", "aquatic", [], [
        "Blub blub! A água hoje está uma delícia. Você gosta de nadar?", "Ouviu o barulho das ondas? Dá vontade de nunca sair daqui.", "Lá embaixo é tão calmo. Você já mergulhou?",
        "Eu vivo na água, mas adoro ver quem passa. Você veio fazer o quê?", "Sabia que a água tem muita vida? Você já reparou?", "Quando a luz bate na água, tudo vira um brilho só. Você já viu?",
        "As bolhas me fazem cócegas! E você, tem alguma coisa que faça cócegas?", "Você costuma passear perto da água? É sempre bom ver uma cara nova."], 3)
    P("peixe", "fish", [], [
        "Blub! Estou nadando em círculos. Você também faz isso quando está pensando?", "Eu me assusto fácil, mas hoje estou curioso. O que você faz aí fora?",
        "Nadar em grupo é mais seguro. Você prefere andar em turma?", "Estou atrás de comida boa. Você sabe onde tem?", "A água é fria, mas eu me acostumo. Você é de aguentar frio?",
        "Blub blub! Aqui embaixo a gente é esperto. Você consegue nadar rápido?"], 4)
    P("lula", "squid", [], [
        "Eu solto tinta quando me assusto. Você já tomou um susto bem grande?", "Lá no fundo é escuro e silencioso. Você gosta de escuro?", "Tenho vários braços, mas preciso de um bom abraço. Você abraça?",
        "As correntes me levam para onde querem. Você segue o fluxo?", "Eu adoro nadar sem pressa. E você, tem pressa?"], 4)
    P("lula_brilho", "glow_squid", [], [
        "Eu brilho no escuro. Você gosta de coisas luminosas?", "Lá embaixo, a minha luz é a única. Você já se sentiu sozinho no escuro?", "Eu brilho no fundo do mar. Você acha isso bonito?",
        "Eu ilumino o fundo do mar. Você tem uma lanterna para o escuro?"], 4)
    P("golfinho", "dolphin", [], [
        "Iiih iiih! Quer saltar comigo nas ondas?", "Eu adoro brincar! Você sabe brincar?", "Vejo muita gente passando pelo mar. Você gosta de navegar?",
        "Que dia bom para nadar rápido! Você sabe nadar?", "Já nadou ao lado de um golfinho? É muito divertido!", "Me siga até o tesouro! Brincadeira, mas você gosta de procurar tesouro?"], 4)
    P("tartaruga", "turtle", [], [
        "Eu ando devagar, mas chego. Você tem pressa?", "Eu volto sempre para a praia onde nasci. Você tem um lugar assim?", "Meu casco é a minha casa. A sua casa é boa?",
        "Dizem que eu vivo muito. Você acha que vive bastante?", "Pé ante pé, eu enxergo mais coisas. Você presta atenção nos detalhes?"], 4)
    P("axolote", "axolotl", [], [
        "Eu sorrio sempre. Você consegue sorrir o dia todo?", "Eu me recupero rápido de qualquer coisa. Você também?", "Minhas branquias são fofas, não são? Quer ver de perto?",
        "Eu gosto de água limpa e peixe pequeno. E você, o que gosta?", "Sou uma criatura rara e feliz. Você se sente raro às vezes?"], 4)
