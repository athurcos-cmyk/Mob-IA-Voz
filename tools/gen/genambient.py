"""Gera mod-fabric/src/main/resources/mobvoz/ambient.json: conversas AMBIENTE entre criaturas (sem IA, sem custo).

Cada FAMILIA e um assunto: lista de falas de abertura (a, quem fala primeiro), respostas (b) e, opcionalmente, fecho (c).
Uma cena = uma fala de cada lista, sorteada, entao uma familia com 6 aberturas x 6 respostas x 4 fechos rende 144 cenas.
O servidor so escolhe familias cujas condicoes (need) sao VERDADEIRAS agora (hora, clima, estado do jogador, o que cada
criatura realmente sabe sobre ele): a fala ambiente nunca inventa fato. Nada aqui muda o estado do jogo.

Campos: who = [classe de quem abre, classe de quem responde]; need = condicoes (prefixo ! nega); weight = peso do sorteio.
Classes (quem pode abrir/responder): calm (aldeao ou animal), villagers (aldeoes e golem), animals, monsters (so da MESMA especie: zombie
           skeleton spider creeper enderman witch warden), e as especies: farm cow pig sheep chicken pet wolf cat horse golem villager villager:<profissao>.
           Duas criaturas so conversam se compartilham o tipo (aldeao com aldeao, animal com animal, monstro com a mesma especie).
Condicoes: ev:... (acontecimento do WorldReactions; familias ev_*, ver genambient_ev.py) snow desert jungle swamp forest mountain beach savanna fullmoon newmoon deep high sneak run ride swim burning fly emeralds diamonds followers ally unseen (nenhuma das duas ve o jogador: familias sobre o que ele segura/veste/faz levam "!unseen" sozinhas) day dusk night dawn clear rain thunder hurt hungry holds want0 (quem abre GOSTA do que o jogador segura) armor k0 k1 (criatura 0/1 ja conhece o jogador) n0 n1 (sabe o NOME)
           f0:TIPO (a criatura 0 tem este fato verificado sobre o jogador: TRADE GIFT HONEST_RETURN DAMAGED_BY_PLAYER PACT_BROKEN
           THREAT_SPOKEN COMFORTED TAMED).
Espacos: {jogador} ("o humano" ou o nome que a criatura sabe), {Jogador} (inicio de frase), {nome} (so com n0 ou n1), {item} (so com holds), {armadura} (so com armor: "uma armadura de ferro"),
         {@pool} (sorteia do pool, igual dentro da cena). Falas curtas, sem travessao, sem adjetivo com genero para a propria criatura.

Rode: python genambient.py
"""
import json
from pathlib import Path

OUT = Path(__file__).resolve().parents[2] / "mod-fabric" / "src" / "main" / "resources" / "mobvoz" / "ambient.json"

POOLS = {
    "produto": ["trigo", "cenoura", "batata", "beterraba", "melancia", "abóbora", "pão"],
    "perigo": ["zumbi", "esqueleto", "aranha", "creeper"],
    "afazer": ["arrumar a casa", "cuidar da horta", "contar o estoque", "varrer a porta", "regar as plantas", "consertar uma cerca"],
    "lugar": ["lá na colina", "perto do rio", "atrás da vila", "perto do poço", "do outro lado da cerca", "na beira do caminho"],
}

FAMILIES = []


# Assuntos que so fazem sentido se uma das duas criaturas ENXERGA o jogador agora (o servidor poe a tag "unseen" quando nenhuma o ve).
SEES_IDS = {"jogador_passando", "jogador_voltou", "jogador_apresenta", "jogador_nome_ambos", "jogador_noite", "jogador_chuva", "vila_passando_troca",
            "bicho_jogador_noite", "bicho_jogador_chuva", "jogador_monstros_visto", "esqueleto_mira", "bruxa_pocao", "enderman_olhar"}


SEES_TAGS = {"holds", "armor", "hurt", "hungry", "sneak", "run", "ride", "swim", "burning", "fly", "emeralds", "diamonds"}


def fam(id, who, need, a, b, c=None, weight=1, solo=False):
    about = id.startswith("jogador_") or "holds" in need or "armor" in need
    if id in SEES_IDS or SEES_TAGS & set(need):
        need = need + ["!unseen"]
    FAMILIES.append({"id": id, "who": who, "need": need, "weight": weight, "player": about, "a": a, "b": b, "bg": [], "c": c or [], "solo": solo})


# ----------------------------------------------------------------------------------------------- tempo
fam("chuva", ["calm", "calm"], ["rain"],
    ["Olha só, começou a chover.", "A chuva voltou, hein?", "Esse cheirinho de terra molhada é bom, né?", "Está chovendo de novo. Será que não para?",
     "Chuva boa essa, vai regar tudo.", "Quando chove assim, dá vontade de ficar sem se mexer."],
    ["É, o céu está bem carregado.", "Pelo menos as plantas agradecem.", "Eu gosto, o barulho da chuva acalma.", "Daqui a pouco passa, sempre passa.",
     "Só não gosto de lama.", "Cuidado para não escorregar por aí."],
    ["Verdade. Vamos ver quanto tempo dura.", "Bom, fazer o quê.", "Se trovejar eu me escondo.", "Que dure só o suficiente."])
fam("trovoada", ["calm", "calm"], ["thunder"],
    ["Ouviu esse trovão? Estremeceu tudo.", "Que tempestade feia.", "Raios de novo! Isso me arrepia.", "Esse temporal está de dar medo."],
    ["Fica longe de árvore alta, dizem que atrai raio.", "Tomara que não caia nada perto de nós.", "Eu só espero que passe logo.", "Raio assim faz a gente querer ficar junto."],
    ["Chega mais perto, é mais seguro.", "Se cair um raio aqui perto, é um susto danado.", "Vamos aguentar juntos."])
fam("sol", ["calm", "calm"], ["day", "clear"],
    ["Que dia bonito hoje.", "O sol está gostoso, né?", "Dia perfeito para ficar à toa.", "O céu está limpo, nem uma nuvem.", "Acordei de bom humor com esse sol."],
    ["Também percebi, dá até ânimo.", "Aproveita, que o tempo muda rápido.", "Dias assim deviam durar mais.", "É, hoje nem dá vontade de reclamar."],
    ["Então vamos aproveitar.", "Pena que a noite chega logo.", "Hoje está tudo no lugar."])
fam("entardecer", ["calm", "calm"], ["dusk"],
    ["O sol está se pondo.", "Já vai anoitecer, olha o céu.", "Que pôr do sol bonito.", "A tarde acabou rápido hoje."],
    ["Hora de ficar de olho, a noite traz coisa ruim.", "Logo os monstros começam a aparecer.", "Eu gosto dessa cor no céu.", "Melhor se recolher antes de escurecer."],
    ["É verdade, vamos com cuidado.", "Pelo menos o céu está lindo.", "Cada dia termina assim."])
fam("noite", ["calm", "calm"], ["night"],
    ["Está escuro demais por aqui.", "Já ouvi uns rosnados lá longe.", "Noite fria, essa.", "Quem está de pé a essa hora?", "A lua está bonita, mas não confio nessa escuridão."],
    ["Fica por perto, é mais seguro.", "Eu só queria que amanhecesse logo.", "Se ouvir um chiado, corre.", "De noite o mundo fica estranho."],
    ["Aguenta firme, o dia já vem.", "Vamos ficar juntos, então.", "Qualquer barulho eu aviso."])
fam("amanhecer", ["calm", "calm"], ["dawn"],
    ["O dia está nascendo, olha ali.", "Finalmente amanheceu.", "Sobrevivemos a mais uma noite."],
    ["Que alívio, nem dormi direito.", "O sol sempre chega, é só esperar.", "Agora dá para respirar."],
    ["Vamos ver o que o dia traz.", "Bom dia, então."])

# ----------------------------------------------------------------------------------------------- vila
fam("colheita", ["villager", "villager"], ["day"],
    ["A plantação de {@produto} está linda este ano.", "Colhi {@produto} a manhã inteira.", "Hoje só pensei em {@produto}.", "A horta rendeu bem, vou separar {@produto} para trocar.",
     "Como anda a sua plantação de {@produto}?"],
    ["Com cuidado assim, nunca falta comida.", "Troquei um pouco com um viajante outro dia.", "Quem planta direito colhe sempre.", "A minha anda mais devagar, mas vai."],
    ["Se sobrar, a gente troca.", "Boa colheita para nós dois.", "A terra daqui é generosa."])
fam("trocas_vila", ["villager", "villager"], ["day"],
    ["Meu estoque de esmeraldas está curto.", "Hoje o movimento de trocas foi fraco.", "Quase ninguém passou para negociar.", "Ainda bem que ontem vendi quase tudo."],
    ["Paciência, sempre aparece alguém.", "Se um viajante chegar, eu aviso.", "Um bom negócio vale a espera."],
    ["Tomara. Preciso arrumar a banca.", "É assim mesmo, tem dia de sobra e dia de falta."])
fam("camas", ["villager", "villager"], ["night"],
    ["Já arrumei minha cama, vou dormir cedo.", "Estou com sono, esse dia foi longo.", "Aquela cama perto da porta é minha, hein."],
    ["Descansa, amanhã tem trabalho.", "Eu também, mal me aguento em pé.", "Boa noite, então."],
    ["Boa noite. Tranca a porta.", "Até amanhã."])
fam("zumbis_na_vila", ["villager", "villager"], ["night"],
    ["Ouviu aquele gemido lá fora?", "Tranquei a porta duas vezes hoje.", "Dizem que tem {@perigo} rondando a vila."],
    ["Ainda bem que o golem fica de guarda.", "Não abre a porta por nada.", "Eles odeiam a luz do sol, só precisamos esperar."],
    ["Vou ficar em silêncio até o dia chegar.", "Que a noite passe logo."])
fam("afazeres", ["villager", "villager"], ["day"],
    ["Hoje ainda preciso {@afazer}.", "Passei a manhã sem parar, e ainda falta {@afazer}.", "Depois de {@afazer}, vou descansar."],
    ["Trabalho de vila nunca acaba.", "Eu também tenho a minha lista.", "Se eu terminar antes, ajudo você."],
    ["Valeu, ajuda é sempre bem-vinda.", "Assim a gente termina logo."])
fam("fofoca_vila", ["villager", "villager"], ["day"],
    ["Soube que apareceu gente nova {@lugar}.", "Alguém viu o que se passa {@lugar}?", "Ouvi uns barulhos estranhos {@lugar}."],
    ["Eu não vi nada, mas fico de ouvido aberto.", "Aqui na vila as notícias correm rápido.", "Melhor não sair sozinho por lá."],
    ["Se souber de algo, me conta.", "Vou ficar de olho."])
fam("golem_guarda", ["villager", "golem"], [],
    ["Valeu por vigiar a vila.", "Você nunca dorme, né?", "Dá uma sensação boa ter você por perto.", "A vila fica mais tranquila com você aqui."],
    ["Proteger é o que eu sei fazer.", "Dormir não é comigo, eu fico de pé.", "Enquanto eu estiver aqui, ninguém passa.", "Eu só faço o que precisa ser feito."],
    ["Por isso a vila ainda existe.", "Qualquer coisa, grito por você."])
fam("golem_flor", ["golem", "villager"], ["day"],
    ["Hoje encontrei uma flor perto do caminho.", "Gosto de levar uma flor a quem trata bem os moradores.", "Já tem muito tempo que não vejo ninguém sorrir por aqui."],
    ["Flor combina com você, mesmo sendo de ferro.", "Que gesto bonito.", "Isso alegra a vila toda."],
    ["É o meu jeito de agradecer.", "Cuidar também é isso."])
fam("fazendeiro", ["villager:farmer", "villager"], ["day"],
    ["Meu ofício é fazer a terra dar fruto.", "Quando a semente brota, esqueço o cansaço.", "Pão na mesa começa na minha horta."],
    ["E a vila inteira come por causa disso.", "Sem você passaríamos fome.", "Trabalho honesto, esse."],
    ["Fico feliz em ajudar.", "A terra devolve o que a gente dá."])
fam("bibliotecario", ["villager:librarian", "villager"], [],
    ["Cada livro guarda um segredo.", "Passei a manhã organizando as estantes.", "Quem lê enxerga mais longe."],
    ["Eu não entendo as letras, mas gosto de ouvir você.", "Um dia você me ensina?", "Deve ser bom saber tanta coisa."],
    ["Ler é uma forma de viajar.", "Quando quiser, eu leio uma página para você."])
fam("ferreiro", ["villager:toolsmith", "villager"], [],
    ["O fogo da forja nunca me deixa com frio.", "Hoje afiei ferramentas o dia todo.", "Uma boa picareta muda tudo."],
    ["Dá para ouvir o martelo daqui.", "A vila agradece o seu trabalho.", "Isso é que é ofício de verdade."],
    ["Metal bom pede paciência.", "Passa lá depois, tenho coisa nova."])
fam("clerigo", ["villager:cleric", "villager"], [],
    ["As poções de hoje ficaram bonitas.", "Tem coisa estranha misturada no meu caldeirão, e funciona.", "Dizem que meu laboratório cheira esquisito."],
    ["Cheira mesmo, mas a gente se acostuma.", "Se alguém ficar doente, você dá um jeito.", "Prefiro não provar nada daí."],
    ["Poção boa tem de tudo um pouco.", "É tudo ciência, juro."])
fam("pastor", ["villager:shepherd", "villager"], ["day"],
    ["As ovelhas hoje estão agitadas.", "Lã não falta nesta estação.", "Passei o dia atrás de uma ovelha fujona."],
    ["Elas são teimosas mesmo.", "Elas dão trabalho, mas eu gosto delas.", "Cuidar de rebanho dá trabalho."],
    ["No fim, elas voltam sempre.", "Eu gosto delas."])

# ----------------------------------------------------------------------------------------------- bichos
fam("pasto", ["farm", "farm"], ["day"],
    ["Esse capim está uma delícia.", "Passei a manhã pastando, que paz.", "Viu aquela touceira de grama ali?", "O pasto hoje está macio."],
    ["Só não gosto quando o vento levanta poeira.", "Já comi tanto que nem me mexo.", "Dá para viver só disso."],
    ["Hoje o dia está bom para pastar.", "Aqui a gente está seguro."])
fam("cercado", ["farm", "farm"], [],
    ["O cercado está apertado hoje.", "Tem espaço de sobra para todo mundo aqui.", "Será que um dia essa cerca abre?"],
    ["Antes aqui do que solto à noite.", "Cerca é sinal de que alguém cuida da gente.", "Gosto de saber onde fica o meu cantinho."],
    ["Verdade, perto de vocês eu me sinto bem.", "Aqui pelo menos tem comida."])
fam("fome_bicho", ["farm", "farm"], ["day"],
    ["Será que hoje alguém traz comida?", "Fico de olho para ver se aparece alguém com trigo.", "Estou com uma fome danada."],
    ["Paciência, sempre aparece alguém.", "Quem espera, come."],
    ["Vamos esperar, então.", "Alguma hora vem."])
fam("medo_lobo", ["farm", "farm"], ["night"],
    ["Já ouviu um uivo de lobo por perto?", "Escutei um rosnado lá longe.", "À noite o campo me assusta."],
    ["Me arrepio só de lembrar.", "Chega pertinho de mim, que é mais seguro.", "Lobo não gosta de rebanho junto."],
    ["Vamos ficar de olho.", "Se vier, a gente corre."])
fam("galinha", ["chicken", "farm"], [],
    ["Có có! Achei umas sementes no chão.", "Botei um ovo e já cacarejei para todo mundo.", "Hoje o dia foi só ciscar.", "Có, tem raposa por perto?"],
    ["Que sorte a sua.", "Cuidado com a raposa.", "Eu fico de olho no céu, por via das dúvidas.", "Ovo novo é sempre uma alegria."],
    ["Có có, a vida é boa.", "Então vou ciscar mais um pouco."])
fam("ovelha", ["sheep", "farm"], [],
    ["Minha lã está crescendo de novo.", "Mééé, passei o dia mordiscando grama.", "Sinto que estou ganhando lã a cada dia."],
    ["A lã fica bonita assim.", "Tem que tosar logo, hein?", "Tem quem inveje essa lã."],
    ["Eu gosto do vento na lã.", "Depois eu cresço de novo."])
fam("porco", ["pig", "farm"], [],
    ["Achei algo gostoso enterrado.", "Fuço o dia inteiro e nunca me canso.", "Barro é a melhor parte da vida."],
    ["Não conta para ninguém onde achou.", "Só cuidado para não se sujar demais."],
    ["Sujeira é parte do charme.", "Depois eu mostro o lugar."])
fam("vaca", ["cow", "farm"], ["day"],
    ["Muuu, o dia está calmo.", "Hoje o pasto está especialmente macio.", "Dá vontade de ficar mastigando devagar o dia todo."],
    ["Calma é o que a gente tem de melhor.", "Sem pressa, a vida rende mais.", "Aqui a gente só precisa de grama e sombra."],
    ["Muuu, concordo.", "Quando escurecer a gente se junta."])
fam("lobo", ["wolf", "animals"], [],
    ["Estou de olho em tudo por aqui.", "Cheirei um osso enterrado ali perto.", "Um dia ainda pego aquele esqueleto."],
    ["Cuidado para não assustar ninguém.", "Você parece sempre alerta.", "É bom ter alguém que vigia."],
    ["Quem é da matilha eu protejo.", "Meu nariz nunca erra."])
fam("gato", ["cat", "animals"], [],
    ["Dormi no sol a tarde toda.", "Creeper nenhum chega perto de mim.", "Hoje eu não faço nada, e está ótimo."],
    ["Que vida boa a sua.", "Você realmente não tem pressa.", "Dizem que gato afasta creeper, é verdade?"],
    ["É verdade. Eles têm medo de mim.", "Dizem que é sorte, mas é esperteza."])
fam("cavalo", ["horse", "animals"], ["day"],
    ["Dá vontade de correr até o vento passar por mim.", "Hoje o campo está livre, só falta espaço.", "Adoro um bom galope."],
    ["Eu prefiro trotar devagar e olhar a paisagem.", "Corre mesmo, que eu fico olhando.", "Só cuide para não cair em buraco."],
    ["Mais tarde eu corro.", "Correr é o que eu faço de melhor."])

# ----------------------------------------------------------------------------------------------- curiosidades do mundo
fam("curiosidade_quadrado", ["villager", "villager"], [],
    ["Você já parou para pensar por que tudo aqui é quadrado?", "Sonhei que o mundo era redondo.", "Já notou que o sol nasce sempre do mesmo lado?"],
    ["Já! E ninguém sabe responder.", "Que ideia estranha.", "Nunca tinha reparado, agora vou pensar nisso o dia todo."],
    ["Pois é, o mundo é cheio de mistérios.", "Melhor nem pensar muito nisso.", "Pelo menos é um mundo bonito."], weight=1)
fam("curiosidade_fundo", ["villager", "villager"], [],
    ["O que será que tem lá embaixo, nas cavernas?", "Dizem que tem brilho de diamante bem fundo.", "Já ouviu os sons que vêm do chão à noite?"],
    ["Escuridão e coisa ruim, dizem.", "Prefiro o lado de cima.", "Quem desce muito nunca mais volta o mesmo."],
    ["Melhor deixar o fundo em paz.", "Que ninguém desça sem tocha."])
fam("curiosidade_portal", ["villager", "villager"], [],
    ["Já viu um portal roxo?", "Dizem que existe um mundo de fogo do outro lado.", "Falaram de uma ilha lá no Fim."],
    ["Prefiro nem chegar perto.", "Ouvi histórias, mas não sei se são verdade.", "Deve ser um lugar bem esquisito."],
    ["Vai saber o que é verdade.", "Eu fico por aqui mesmo."])
fam("curiosidade_creeper", ["villager", "villager"], [],
    ["Dizem que creeper tem medo de gato.", "Já viu um creeper de perto? Silencioso demais.", "Tenho pavor daquele chiado do creeper."],
    ["Eu ouvi isso também, deve ser verdade.", "Nem me fale, só de pensar eu me arrepio.", "O pior é que ele chega sem avisar."],
    ["Por isso fico de olho em tudo.", "Que ele passe longe daqui."])
fam("curiosidade_enderman", ["villager", "villager"], ["night"],
    ["Dizem que enderman odeia água.", "Já viu aqueles olhos roxos no escuro?", "Tem uns bichos altos que levam blocos embora."],
    ["Melhor não encarar, dizem que ficam bravos.", "Nunca falei com um, mas tenho vontade.", "Eles somem num piscar de olhos."],
    ["Deve ser uma vida estranha.", "Cada um com seu jeito, né?"])
fam("curiosidade_abelha", ["calm", "calm"], ["day", "clear"],
    ["Você viu as abelhas hoje? Trabalham sem parar.", "Aquelas abelhas são bem mais ocupadas que a gente."],
    ["Sem elas as flores nem teriam graça.", "Só não mexa na colmeia, elas se zangam.", "Dizem que o mel é uma maravilha."],
    ["Elas sabem o que fazem.", "Respeito muito esse esforço."])

# ----------------------------------------------------------------------------------------------- sobre o JOGADOR (a parte principal)
fam("jogador_passando", ["calm", "calm"], ["day"],
    ["{Jogador} anda por aí de novo.", "Olha, é {jogador} outra vez.", "Vi {jogador} passando agora há pouco.", "{Jogador} parece apressado, né?"],
    ["Gosto quando tem movimento por aqui.", "Será que vem conversar?", "Eu fico de olho.", "Pois é."],
    ["Vamos ver o que vai fazer.", "Eu também.", "Vamos ver o que acontece."], weight=2)
fam("jogador_noite", ["villager", "villager"], ["night"],
    ["{Jogador} anda por aí em plena noite.", "Olha lá, {jogador} caminhando no escuro.", "Quem sai a essa hora? Ah, é {jogador}."],
    ["Corajoso ou distraído, ainda não sei.", "Eu não sairia de casa agora.", "Espero que tenha uma tocha."],
    ["A noite não perdoa ninguém.", "Que a noite seja gentil com essa pessoa."], weight=2)
fam("jogador_chuva", ["villager", "villager"], ["rain"],
    ["{Jogador} está na chuva, nem parece se importar.", "Olha {jogador} debaixo dessa chuva toda."],
    ["Alguém devia oferecer um abrigo.", "Deve estar com frio.", "Gente teimosa, essa."],
    ["Ao menos a chuva lava a poeira.", "Se vier até aqui, eu deixo ficar."], weight=2)
fam("jogador_item", ["villager", "villager"], ["holds"],
    ["{Jogador} está segurando {item}, reparou?", "Olha só, {jogador} anda com {item} na mão.", "Aquela pessoa carrega {item}. Deve valer algo."],
    ["Será que quer trocar?", "Se for troca, eu topo.", "Parece útil, isso.", "Eu também gostaria de ter {item}."],
    ["Vou perguntar, quem sabe.", "Cada um carrega o que precisa."], weight=3)
fam("jogador_ferido", ["calm", "calm"], ["hurt"],
    ["{Jogador} parece ferido.", "Cuidado, {jogador} não está nada bem.", "Olha o estado de {jogador}, que pena."],
    ["Precisa se cuidar, isso sim.", "Uma boa refeição ajuda a se curar.", "Alguém devia oferecer ajuda.", "Bem que podia descansar um pouco."],
    ["Ao menos está vivo.", "Que se recupere logo."], weight=3)
fam("jogador_fome", ["calm", "calm"], ["hungry"],
    ["{Jogador} está com cara de fome.", "Acho que {jogador} não come há um tempo."],
    ["Quem tem comida devia dividir.", "Quem está de barriga vazia não pensa direito.", "Comida resolve metade dos problemas."],
    ["Se eu tivesse, dividia.", "Que ache o que comer."], weight=3)
fam("jogador_conhecido", ["calm", "calm"], ["k0"],
    ["Eu já conversei com {jogador}, sabia?", "Já cruzei com {jogador} por aqui antes.", "{Jogador} e eu já conversamos um pouco."],
    ["E como foi?", "Me conta, o que foi que disse?", "Que interessante."],
    ["Dá para conversar numa boa.", "Foi tranquilo, achei simpático.", "Cada conversa é diferente."], weight=2)
fam("jogador_apresenta", ["calm", "calm"], ["k0", "!k1"],
    ["Está vendo aquela pessoa? É {jogador}. Já conversei com ela.", "Está vendo quem passou? É {jogador}, eu já conheço.", "Olha quem está aí: {jogador}. Passa sempre por aqui."],
    ["Não conhecia, nunca falei com essa pessoa.", "Ah, então é ela? Vou prestar atenção.", "Parece alguém interessante."],
    ["Vale a pena puxar assunto.", "Vai lá falar, não morde.", "Boa sorte."], weight=3)
fam("jogador_nome_conta", ["calm", "calm"], ["n0", "!n1"],
    ["Sabia que essa pessoa se chama {nome}?", "O nome daquele humano é {nome}, ele me disse.", "Me contou pessoalmente: é {nome}."],
    ["{Nome}? Não sabia, vou guardar.", "Que nome bonito, {nome}.", "Boa, agora posso chamar pelo nome."],
    ["Aqui a gente se trata pelo nome.", "É bom saber com quem se fala.", "Nome é a primeira coisa que a gente deveria saber."], weight=4)
fam("jogador_sem_nome", ["calm", "calm"], ["k0", "!n0", "!n1"],
    ["Você sabe o nome dessa pessoa?", "Será que aquele humano tem nome?", "Nunca perguntei como se chama {jogador}."],
    ["Nunca perguntei. Para mim é só um humano.", "Deve ter, todo mundo tem.", "Alguém devia perguntar."],
    ["Da próxima vez eu pergunto.", "Quem sabe conta."], weight=3)
fam("jogador_nome_ambos", ["calm", "calm"], ["n0", "n1"],
    ["{Nome} voltou! Que bom ver essa pessoa.", "Olha quem está aí, {nome}.", "{Nome} anda ocupado hoje."],
    ["É sempre bom ver {nome} por aqui.", "Eu também fico contente.", "Será que veio conversar?"],
    ["Vou dar um oi.", "Bom ter gente conhecida."], weight=4)
fam("jogador_troca", ["calm", "calm"], ["f0:TRADE"],
    ["{Jogador} fez uma troca justa comigo.", "A última troca com {jogador} foi tranquila.", "Negociei com {jogador} outro dia, correu tudo bem."],
    ["Paga direito?", "Bom saber, eu também troco com quem é honesto.", "Isso é raro hoje em dia."],
    ["Paga, sim.", "Dá para confiar."], weight=4)
fam("jogador_presente", ["calm", "calm"], ["f0:GIFT"],
    ["{Jogador} me deu um presente outro dia. Fiquei com o coração quentinho.", "Sabia que {jogador} já me presenteou?", "Ninguém tinha me dado um presente antes de {jogador}."],
    ["Que gesto bonito.", "Poucos fazem isso.", "Pelo visto é gente boa."],
    ["Nunca esqueci.", "Foi um dia especial."], weight=4)
fam("jogador_honesto", ["calm", "calm"], ["f0:HONEST_RETURN"],
    ["{Jogador} me devolveu o que eu tinha entregado, sem eu pedir.", "Teve uma vez que {jogador} me devolveu algo, sem ninguém pedir."],
    ["Isso é raro, essa pessoa é honesta.", "Então vale a confiança.", "Gente assim é difícil de achar."],
    ["Pois é, eu confio.", "Fez toda a diferença para mim."], weight=4)
fam("jogador_bateu", ["calm", "calm"], ["f0:DAMAGED_BY_PLAYER"],
    ["{Jogador} me machucou uma vez. Não esqueci.", "Eu me lembro bem de quando {jogador} me bateu.", "Prefiro manter distância de {jogador}."],
    ["Então fica de olho.", "Quem bate uma vez pode bater de novo.", "Melhor manter distância dessa pessoa mesmo."],
    ["É o que eu penso.", "Já aprendi a lição."], weight=4)
fam("jogador_traiu", ["calm", "calm"], ["f0:PACT_BROKEN"],
    ["{Jogador} quebrou a nossa palavra. Eu me lembro.", "Fizemos um acordo, e {jogador} acabou com ele."],
    ["Palavra quebrada é difícil de perdoar.", "Então não dá para confiar tanto.", "Que pena, parecia uma pessoa correta."],
    ["Pois é, dói.", "Quem sabe um dia melhore."], weight=4)
fam("jogador_ameacou", ["calm", "calm"], ["f0:THREAT_SPOKEN"],
    ["{Jogador} me ameaçou uma vez, só com palavras.", "Eu me assustei com as palavras de {jogador} outro dia."],
    ["Tem quem fale sem pensar.", "Ameaça de boca pesa, né?", "Espero que tenha sido um mal-entendido."],
    ["Pode ser.", "Vou dar uma chance."], weight=4)
fam("jogador_acalmou", ["calm", "calm"], ["f0:COMFORTED"],
    ["{Jogador} me acalmou quando eu estava com medo.", "Quando eu estava com medo, {jogador} apareceu e me ajudou a respirar."],
    ["Que bom que existe quem faz isso.", "Gente assim aquece o coração.", "Então essa pessoa é de confiança."],
    ["Não esqueci essa gentileza.", "Fez bem, de verdade."], weight=4)
fam("jogador_dono", ["calm", "calm"], ["f0:TAMED"],
    ["Agora eu sou de {jogador}, e isso me faz bem.", "{Jogador} cuida de mim agora, sabia?", "Tenho dono agora: {jogador}."],
    ["Cuida bem de você?", "Que sorte a sua.", "Sempre quis ter alguém assim."],
    ["Cuida sim.", "Estou bem melhor."], weight=4)

# ----------------------------------------------------------------------------------------------- o que o jogador SEGURA e VESTE, por tipo de mob
fam("vila_passando_troca", ["villager", "villager"], ["day"],
    ["{Jogador} passou por aqui. Será que quer trocar alguma coisa?", "Olha {jogador} na rua. Bom cliente, talvez."],
    ["Se quiser trocar, eu atendo.", "Hoje o meu estoque está bom.", "Quem sabe compra alguma coisa."],
    ["Vou arrumar a banca, por via das dúvidas.", "Que venha negociar."], weight=2)
fam("vila_fome", ["villager", "villager"], ["hungry"],
    ["{Jogador} está com cara de fome.", "Acho que {jogador} não come há um tempo."],
    ["Dava para oferecer um pouco de pão.", "Tenho um trigo sobrando, se ele quiser trocar.", "A vila sempre tem o que comer."],
    ["Se ele pedir, eu dou um jeito.", "Comida é o que não falta por aqui."], weight=3)
fam("vila_armadura", ["villager", "villager"], ["armor"],
    ["{Jogador} veste {armadura}. Que peça boa.", "Reparou na roupa de {jogador}? É {armadura}.", "Olha só, {jogador} está de {armadura}."],
    ["Um bom armeiro faria igual.", "Deve ter custado caro.", "Dá até medo de chegar perto.", "Isso é equipamento de quem anda por lugares perigosos."],
    ["Pelo menos protege a vila também.", "Eu só queria saber onde fez."], weight=3)
fam("golem_quer_flor", ["golem", "villager"], ["holds", "want0"],
    ["{Jogador} está com {item}. Eu adoro isso.", "Olha, {item} na mão de {jogador}. Gosto muito."],
    ["Pede com educação, quem sabe ele dá.", "Você é todo de ferro, mas tem coração mole.", "Ele nem percebeu que você está olhando."],
    ["Uma flor assim alegra qualquer dia.", "Eu cuido da vila, e ele cuida das flores."], weight=4)
fam("bicho_quer_comida", ["animals", "animals"], ["holds", "want0"],
    ["{Jogador} está com {item}. Queria um pouco também.", "Olha só, {item} na mão de {jogador}. Será que é para mim?", "{Item}! É o meu favorito. Será que ele divide?", "Só de ver {item} já fico com fome."],
    ["Eu também queria.", "Chega mais perto, quem sabe ele dá.", "Ele nem olhou para nós, que pena.", "Se ele der, divide comigo?"],
    ["Eu espero aqui, bem quieto.", "Vou chegar de mansinho.", "Quem sabe ele se compadece."], weight=5)
fam("bicho_item", ["animals", "animals"], ["holds", "!want0"],
    ["{Jogador} está com {item}. Não é de comer, né?", "Olha só, {item} na mão de {jogador}. O que será isso?", "Aquela pessoa carrega {item}. Cheiro estranho."],
    ["Não parece comida.", "Isso não me interessa.", "Gente carrega cada coisa esquisita.", "Eu prefiro capim."],
    ["Cada um com o seu gosto.", "Por mim, pode ficar com ele."], weight=3)
fam("bicho_armadura", ["animals", "animals"], ["armor"],
    ["{Jogador} veste {armadura}. Brilha até de longe.", "O que será aquilo que {jogador} está vestindo?", "{Jogador} está todo coberto de metal."],
    ["Parece pesado.", "Deve fazer barulho para andar.", "Será que protege de lobo?", "Eu não vestiria isso."],
    ["Pelo visto ele sabe se cuidar."], weight=3)

# ----------------------------------------------------------------------------------------------- monstros: so com a MESMA especie
fam("zumbi_carne", ["zombie", "zombie"], [],
    ["Humano... cheiro de carne.", "Fome... sempre fome.", "Grrr... tem gente por perto.", "A noite é boa. A fome também."],
    ["Carne... carne...", "Eu também sinto o cheiro.", "Vamos... devagar...", "Grrrr..."],
    ["Hmmm...", "Quase... quase..."], weight=2)
fam("zumbi_jogador", ["zombie", "zombie"], ["holds"],
    ["Aquele humano tem {item}. Grrr.", "{Item}... o humano segura {item}."],
    ["Tira dele... tira...", "Não importa. Carne.", "Cuidado... pode doer."],
    ["Grrr...", "Fome..."], weight=3)
fam("zumbi_armadura", ["zombie", "zombie"], ["armor"],
    ["O humano veste {armadura}. Duro de morder.", "Metal... {armadura}... Grrr."],
    ["Duro... mas a fome é maior.", "Grrr... duro de morder.", "Eu mordo de qualquer jeito."],
    ["Grrr...", "Hmmm..."], weight=3)
fam("esqueleto_mira", ["skeleton", "skeleton"], [],
    ["Aquele humano está ao alcance de uma flecha.", "Silêncio. Estou mirando.", "Ossos quietos, flechas prontas."],
    ["Não erra desta vez.", "Eu cubro pela esquerda."],
    ["Já estou com a flecha encaixada.", "Vamos esperar o momento certo."], weight=2)
fam("esqueleto_armadura", ["skeleton", "skeleton"], ["armor"],
    ["{Jogador} veste {armadura}. Vai ser difícil acertar.", "Aquele humano está de {armadura}."],
    ["Mire na cabeça.", "Flecha encontra brecha.", "Armadura nenhuma é perfeita."],
    ["Vamos tentar mesmo assim.", "Paciência é a nossa arma."], weight=3)
fam("aranha_teia", ["spider", "spider"], [],
    ["Sss... tem gente quente por perto.", "Sss... a teia está pronta.", "Sss... as paredes são minhas."],
    ["Sss... eu sinto as vibrações.", "Sss... deixa chegar mais perto.", "Sss... a noite é nossa."],
    ["Sss... paciência.", "Sss..."], weight=2)
fam("creeper_chiado", ["creeper", "creeper"], [],
    ["Ssss... tem gente.", "Ssss... gato por perto não.", "Ssss... silêncio."],
    ["Ssss... eu também ouvi.", "Ssss... quieto, quieto.", "Ssss... cuidado com os gatos."],
    ["Ssss...", "Ssss... paciência."], weight=2)
fam("enderman_olhar", ["enderman", "enderman"], [],
    ["Não olhe nos olhos. Ele nos vê.", "Mais um humano. Que fique longe dos meus blocos.", "A água. Mantenha-se longe da água."],
    ["Eu sinto o olhar dele.", "Eu também odeio a chuva."],
    ["Melhor sumir antes que ele olhe.", "Vamos para outro lugar."], weight=2)
fam("bruxa_pocao", ["witch", "witch"], [],
    ["Hehe, tenho uma poção nova para testar.", "O caldeirão está borbulhando.", "Esse humano parece um bom voluntário."],
    ["Hehe, a minha é ainda melhor.", "Mistura com cogumelo, dá um efeito curioso."],
    ["Hehe, a noite vai ser divertida.", "Já sei qual receita usar."], weight=2)
fam("warden_vibracao", ["warden", "warden"], [],
    ["Eu sinto passos.", "O silêncio é melhor.", "Há vibração por perto."],
    ["Eu também sinto.", "Fica quieto.", "Cada passo ecoa no meu peito."],
    ["Escuta.", "Quieto."], weight=2)

# ----------------------------------------------------------------------------------------------- animais: so o que um bicho percebe e quer
fam("bicho_cheiro", ["farm", "farm"], ["day"],
    ["Sentiu o cheiro do vento hoje?", "O ar está diferente hoje.", "Tem um cheiro bom por aqui."],
    ["Cheiro de grama, só isso.", "Eu só sinto o que tem para comer.", "É bom mesmo."],
    ["Gosto quando o dia cheira assim.", "Hmm, também."], weight=2)
fam("bicho_barulho", ["animals", "animals"], ["night"],
    ["Ouviu aquele barulho lá longe?", "Escutei uns passos no escuro.", "Tem alguma coisa se mexendo ali."],
    ["Ouvi. Melhor ficar quieto.", "Fica perto de mim.", "Deve ser o vento."],
    ["Se vier, a gente foge junto.", "Vou ficar de olho."], weight=2)
fam("bicho_sombra", ["animals", "animals"], ["day", "clear"],
    ["Achei uma sombra boa.", "Esse cantinho está fresco.", "Tem sombra de sobra aqui."],
    ["Divide comigo?", "Cabe todo mundo.", "Que bom, o sol está forte."],
    ["Fica à vontade.", "Aqui a gente descansa."], weight=2)
fam("bicho_jogador_noite", ["animals", "animals"], ["night"],
    ["Tem alguém andando no escuro.", "O humano caminha de noite.", "Olha lá, o humano no escuro."],
    ["Eu fico quieto.", "Ele não tem medo?", "Cada um com o seu jeito."],
    ["É um bicho corajoso, esse aí.", "Eu não sairia."], weight=2)
fam("bicho_jogador_chuva", ["animals", "animals"], ["rain"],
    ["O humano está na chuva.", "Olha o humano debaixo da chuva."],
    ["Ele não se importa?", "Eu prefiro um canto seco.", "Fica molhado, coitado."],
    ["Cada um com o seu gosto.", "Eu só queria sombra."], weight=2)

# ----------------------------------------------------------------------------------------------- monstros de especies DIFERENTES (entre si, nunca com aldeao ou animal)
fam("monstros_noite", ["monsters", "monsters"], ["night"],
    ["A noite é nossa.", "Hoje a caça vai ser boa.", "Sinto cheiro de humano por perto.", "Escuridão boa, essa."],
    ["Eu também senti.", "Vamos juntos, então.", "Cada um por um lado.", "Que ninguém atrapalhe."],
    ["Quieto, vem vindo alguém.", "Que a noite comece."], weight=2)
fam("monstros_dia", ["monsters", "monsters"], ["day", "clear"],
    ["Esse sol é um castigo.", "Odeio o dia.", "Dia é hora de se esconder."],
    ["Procura uma sombra.", "Esperar a noite é o jeito.", "Também não gosto."],
    ["Falta pouco para escurecer.", "Paciência."], weight=2)
fam("monstros_chuva", ["monsters", "monsters"], ["rain"],
    ["Essa chuva estraga tudo.", "Chuva de novo, que droga."],
    ["Pelo menos os humanos ficam em casa.", "Eu só quero que passe.", "Procura um abrigo."],
    ["Vai passar.", "Eu espero aqui."], weight=2)
fam("jogador_monstros_visto", ["monsters", "monsters"], [],
    ["Tem um humano ali.", "Aquele humano não está nos vendo.", "Humano à vista."],
    ["Cuidado com o que ele carrega.", "Eu chego pela esquerda.", "Silêncio, chega mais perto."],
    ["Agora não. Espera o momento certo.", "Eu avisei."], weight=2)
fam("zumbi_esqueleto", ["zombie", "skeleton"], [],
    ["Seus ossos fazem barulho demais.", "Você só sabe atirar de longe.", "Dá para parar de me acertar com flecha?"],
    ["Seus gemidos avisam a presa.", "De perto você só arrasta o pé.", "Foi sem querer. Quase."],
    ["Grrr... vamos caçar juntos.", "Hmm... tá bom."], weight=3)
fam("esqueleto_creeper", ["skeleton", "creeper"], [],
    ["Fica longe da minha flecha, hein?", "Você explode e eu atiro. Dá um bom time."],
    ["Ssss... só não me acerta.", "Ssss... eu chego perto, você mira de longe."],
    ["Combinado.", "Então vamos."], weight=3)
fam("aranha_zumbi", ["spider", "zombie"], [],
    ["Sss... a sua fome é barulhenta.", "Sss... eu fico no teto, você no chão."],
    ["Grrr... teia gruda.", "Grrr... quem pegar o humano primeiro."],
    ["Sss... uma corrida, então.", "Sss... paciência."], weight=3)
fam("creeper_zumbi", ["creeper", "zombie"], [],
    ["Ssss... não chegue perto de gato.", "Ssss... hoje eu exploro."],
    ["Grrr... explode longe de mim.", "Grrr... gato é bicho ruim."],
    ["Ssss...", "Ssss... silêncio."], weight=3)


# =====================================================================================================================================
# REVISAO + AMPLIACAO (2026-10-31), guiada pelo juiz de IA por familia (ai-service/tests/battery_families.py). Regras que o juiz cobrou:
#  - fala de resposta e fecho em AFIRMACAO sobre o MESMO assunto (nada de pergunta sem resposta, nada de "Pois e." solto);
#  - especie especifica conversa com a MESMA especie (a galinha so responde a galinha); bicho nao da conselho de gente;
#  - falas sobre o mundo falam de habitos e sensacoes, nunca de um lugar ou fato que pode nao existir na cena.
# =====================================================================================================================================
def drop(*ids):
    FAMILIES[:] = [f for f in FAMILIES if f["id"] not in ids]


drop("vaca", "porco", "galinha", "ovelha", "lobo", "gato", "cavalo", "bicho_cheiro", "monstros_chuva", "jogador_passando", "jogador_apresenta",
     "jogador_sem_nome", "cercado", "fome_bicho", "jogador_ferido", "jogador_fome")

# ---- especies: a mesma especie conversa entre si, com falas so dela
fam("vaca", ["cow", "cow"], [],
    ["Muuu, o dia está calmo.", "Hoje o pasto está especialmente macio.", "Dá vontade de ficar mastigando devagar o dia todo.", "Muuu, o rebanho está todo junto hoje."],
    ["Calma é o que a gente tem de melhor.", "Sem pressa, a vida rende mais.", "Aqui só precisamos de grama e sombra.", "Muuu, concordo."],
    ["Quando escurecer, a gente se junta.", "Fica por perto, que é mais seguro."])
fam("porco", ["pig", "pig"], [],
    ["Fuço o dia inteiro e nunca me canso.", "Achei uma raiz gostosa debaixo da terra.", "Barro é a melhor parte do dia."],
    ["Oink, cada um com o seu cantinho de lama.", "Achar comida enterrada é uma arte.", "Eu fuço do outro lado, então."],
    ["Depois a gente se encontra no barro.", "Oink, a vida é boa."])
fam("galinha", ["chicken", "chicken"], [],
    ["Có có! Achei umas sementes no chão.", "Botei um ovo e já cacarejei para todo mundo.", "Có, hoje o dia foi só ciscar."],
    ["Có có, ciscar é a melhor parte.", "Ovo novo é sempre uma alegria.", "Có, eu fico de olho no céu, por via das dúvidas."],
    ["Có có, a vida é boa.", "Então vou ciscar mais um pouco."])
fam("ovelha", ["sheep", "sheep"], [],
    ["Mééé, minha lã está crescendo de novo.", "Passei o dia mordiscando grama.", "Hoje o vento deixa a lã toda arrepiada."],
    ["Mééé, lã crescida aquece no frio.", "Depois da tosa a gente sente um friozinho.", "Grama boa faz lã boa."],
    ["Mééé, que dia bom.", "A gente se junta quando escurecer."])
fam("lobo", ["wolf", "wolf"], [],
    ["Cheirei um osso enterrado ali perto.", "Fiquei de guarda a manhã toda.", "Ouvi um uivo lá longe."],
    ["A matilha inteira escutou.", "Meu nariz nunca erra.", "Quem é da matilha, eu protejo."],
    ["Auuu, fico de olho nos meus.", "Vamos patrulhar juntos."])
fam("gato", ["cat", "cat"], [],
    ["Dormi no sol a tarde toda.", "Creeper nenhum chega perto de mim.", "Hoje eu quase não saí do lugar."],
    ["Sol é o melhor travesseiro.", "Eles têm medo de gato, é verdade.", "Cochilo é uma arte."],
    ["Miau, agora vou dormir de novo.", "Ninguém me acorda hoje."])
fam("cavalo", ["horse", "horse"], ["day"],
    ["Dá vontade de correr até o vento passar por mim.", "Hoje o campo está livre, só falta espaço.", "Adoro um bom galope."],
    ["Um galope longo limpa a cabeça.", "Eu prefiro trotar e olhar a paisagem.", "Correr é o que fazemos de melhor."],
    ["Mais tarde a gente corre junto.", "Relincho de alegria."])

# ---- calmos, revistos
fam("bicho_cheiro", ["farm", "farm"], ["day"],
    ["Tem um cheiro bom de grama no ar.", "O vento trouxe cheiro de chuva.", "Sinto cheiro de feno novo por perto."],
    ["O nariz da gente não engana.", "Esse cheiro me dá fome.", "Eu também senti, vem de longe."],
    ["Vou seguir o cheiro depois.", "Gosto quando o dia cheira assim."], weight=2)
fam("cercado", ["farm", "farm"], [],
    ["O cercado está cheio hoje.", "Tem espaço de sobra para todo mundo aqui.", "A cerca é alta, mas aqui dentro é seguro."],
    ["Aqui dentro nada nos incomoda.", "Eu gosto de saber onde fica o meu cantinho.", "À noite a cerca faz companhia."],
    ["Perto de vocês eu me sinto bem.", "Aqui pelo menos tem comida."])
fam("fome_bicho", ["farm", "farm"], ["day"],
    ["Estou com uma fome danada.", "Já passou da hora de comer.", "Fico de olho para ver se aparece comida."],
    ["Eu também estou com o estômago vazio.", "Vou procurar uma touceira de grama.", "O cheiro de feno me deixa com mais fome."],
    ["Quem chegar primeiro na comida, come.", "Vamos esperar mais um pouco."])
fam("jogador_passando", ["calm", "calm"], ["day"],
    ["O humano anda por aí de novo.", "Olha, é o humano outra vez.", "Vi o humano passando agora há pouco.", "O humano parece apressado."],
    ["Ele passa sempre por aqui.", "Gosto quando tem movimento por aqui.", "Eu fico de olho nele."],
    ["Vamos ver o que ele faz.", "Que siga o caminho dele."], weight=2)
fam("jogador_apresenta", ["calm", "calm"], ["k0", "!k1"],
    ["Está vendo quem passou? É {jogador}, eu já conheço.", "{Jogador} ali já esteve comigo antes.", "{Jogador} eu já conheço de outros dias."],
    ["Eu ainda não conhecia.", "Nunca falei com ele.", "Então é bom saber quem é."],
    ["Vale a pena conhecer.", "Ele é de boa conversa."], weight=3)
fam("jogador_sem_nome", ["calm", "calm"], ["k0", "!n0", "!n1"],
    ["Ninguém sabe como {jogador} se chama.", "Nunca perguntei o nome de {jogador}.", "{Jogador} deve ter um nome, mas eu não sei."],
    ["Para mim é só o humano.", "Eu também só o chamo de humano.", "Alguém precisa perguntar."],
    ["Quem sabe um dia ele conta.", "Eu pergunto da próxima vez."], weight=3)
fam("jogador_ferido", ["villager", "villager"], ["hurt"],
    ["{Jogador} parece ferido.", "Cuidado, {jogador} não está nada bem.", "Olha o estado de {jogador}, que pena."],
    ["Uma boa refeição ajuda a se curar.", "Alguém devia oferecer ajuda a ele.", "Ele precisa descansar um pouco."],
    ["Que se recupere logo.", "Tomara que passe depressa."], weight=3)
fam("jogador_fome", ["villager", "villager"], ["hungry"],
    ["{Jogador} está com cara de fome.", "Acho que {jogador} não come há um tempo."],
    ["Dava para oferecer um pouco de pão.", "Quem está de barriga vazia não pensa direito.", "Comida resolve metade dos problemas."],
    ["Se ele pedir, eu dou um jeito.", "Que ache o que comer."], weight=3)
fam("bicho_jogador_ferido", ["animals", "animals"], ["hurt"],
    ["O humano está machucado.", "Olha como o humano anda devagar.", "O humano parece sentir dor."],
    ["Dá pena ver.", "Ele devia descansar à sombra.", "Eu fico quieto para não incomodar."],
    ["Que melhore logo.", "Fico de olho nele."], weight=3)
fam("bicho_jogador_fome", ["animals", "animals"], ["hungry"],
    ["O humano está com cara de fome.", "O humano ficou olhando para a comida.", "O humano deve estar faminto."],
    ["Aqui só tem capim, mas é bom.", "Ele devia achar algo para comer.", "A fome deixa todo mundo de mau humor."],
    ["Que ache comida logo.", "Eu entendo bem essa fome."], weight=3)
fam("bicho_jogador_conhecido", ["animals", "animals"], ["k0"],
    ["O humano já esteve aqui antes.", "Esse humano eu já conheço.", "O cheiro do humano me é familiar."],
    ["Eu também me lembro dele.", "Ele parece de boa índole.", "Aqui ele é sempre bem-vindo."],
    ["Boa gente.", "Vamos ver o que ele quer hoje."], weight=2)
fam("monstros_chuva", ["monsters", "monsters"], ["rain"],
    ["Essa chuva apaga tudo que a gente rastreia.", "Chuva de novo, que droga.", "A água me deixa de mau humor."],
    ["Pelo menos a escuridão fica melhor.", "Os humanos ficam trancados, mas é irritante.", "Eu só quero que passe."],
    ["Depois a caça recomeça.", "Eu espero aqui no escuro."], weight=2)

# ---- animais: habitos e sensacoes
fam("bicho_lama", ["farm", "farm"], [],
    ["A lama fresca é a melhor coisa do calor.", "Depois da chuva a lama fica perfeita.", "Lama macia faz bem para os cascos."],
    ["Eu me deito nela e fico à vontade.", "Sujar o casco é um prazer.", "Barro seco coça, barro molhado é paz."],
    ["Amanhã a lama ainda estará lá.", "Que dia bom para quem gosta de lama."])
fam("bicho_agua", ["animals", "animals"], ["day"],
    ["Água fresquinha é o que eu mais quero.", "A sede aperta quando o sol esquenta.", "Beber água limpa é um prazer."],
    ["Água boa é metade do dia bom.", "Eu vou beber depois.", "Sede passa rápido com água fresca."],
    ["Depois a gente vai juntos.", "Que a água nunca falte."])
fam("bicho_abelha", ["farm", "farm"], ["day", "clear"],
    ["Tem uma abelha zumbindo perto de mim.", "As abelhas hoje estão agitadas.", "O zumbido das abelhas me faz companhia."],
    ["Elas só querem as flores.", "Se ninguém mexer, elas não incomodam.", "O zumbido até acalma."],
    ["Melhor deixar elas em paz.", "Que continuem trabalhando."])
fam("bicho_cochilo", ["animals", "animals"], ["day"],
    ["Vou cochilar um pouco aqui.", "O sol me deixou com sono.", "Esse canto está ótimo para dormir."],
    ["Eu também estou com sono.", "Cochilo depois de comer é o melhor.", "Fecha os olhos que eu vigio."],
    ["Me acorda quando o sol baixar.", "Boa soneca."])
fam("bicho_vento", ["farm", "farm"], ["day"],
    ["Hoje venta bastante.", "O vento está fazendo o capim dançar.", "O vento hoje traz cheiros de longe."],
    ["Dá para ouvir o capim farfalhando.", "Eu gosto do vento no pelo.", "O vento esfria o calor."],
    ["Vento bom é vento fraco.", "Que continue assim."])
fam("bicho_barulho_estranho", ["animals", "animals"], ["night"],
    ["Ouvi um gemido estranho lá longe.", "Escutei alguma coisa arrastando os pés.", "Tem algo gemendo no escuro."],
    ["Parece coisa de zumbi.", "Melhor ficar longe da cerca.", "Eu me encolho quando ouço isso."],
    ["Se chegar perto, a gente foge junto.", "Eu fico atento."], weight=2)
fam("bicho_noite_lua", ["animals", "animals"], ["night", "clear"],
    ["A lua está grande hoje.", "Dá para ver tudo com essa luz.", "A noite está clara."],
    ["Fica mais fácil de enxergar o perigo.", "A luz da lua acalma.", "Eu gosto do silêncio da noite."],
    ["Fica por perto.", "Que a noite passe tranquila."])
fam("bicho_manha", ["farm", "farm"], ["day"],
    ["O sol da manhã é o melhor.", "De manhã o capim está cheio de orvalho.", "Gosto do começo do dia."],
    ["O orvalho deixa tudo fresco.", "Tudo parece mais calmo cedo.", "É a melhor hora para comer."],
    ["Que o dia seja bom.", "Vamos aproveitar a luz."])
fam("bicho_hora_comer", ["farm", "farm"], ["day"],
    ["Acho que já está na hora de comer.", "O estômago avisa a hora certa.", "Sinto cheiro de comida chegando."],
    ["O meu também está reclamando.", "Quem chega primeiro come melhor.", "Hora de comer é a melhor hora."],
    ["Vamos juntos.", "Guarda um lugar para mim."])
fam("bicho_filhotes_cuidado", ["animals", "animals"], [],
    ["Cuidar dos pequenos dá trabalho.", "Os mais novos aprendem rápido.", "Os pequenos sempre querem brincar."],
    ["Mas vale a pena cada dia.", "Eu também fico de olho neles.", "Daqui a pouco já estão grandes."],
    ["Cada um cresce no seu tempo.", "Que cresçam fortes."])

# ---- aldeoes: assuntos da vila
fam("vila_sino", ["villager", "villager"], [],
    ["O sino da vila é o som que mais me assusta.", "Quando o sino toca, ninguém fica fora de casa.", "O sino chama todo mundo quando há perigo."],
    ["Eu corro para dentro quando escuto.", "Tomara que seja sempre só um aviso.", "A vila inteira ouve."],
    ["O golem fica de olho em tudo.", "A vila sempre se junta."])
fam("vila_pilhadores", ["villager", "villager"], [],
    ["Pilhador é a pior coisa que existe.", "Dizem que pilhadores atacam vilas inteiras.", "A ideia de pilhadores me tira o sono."],
    ["O golem não deixa ninguém passar.", "Eu fecho as portas mais cedo por via das dúvidas.", "Prefiro nem pensar nisso."],
    ["Melhor estar sempre preparado.", "A vila é forte quando está unida."], weight=2)
fam("vila_comerciante", ["villager", "villager"], ["day"],
    ["Comerciante viajante é sempre uma surpresa.", "O comerciante de lhamas vende coisas raras.", "Comerciante que aparece do nada traz boas histórias."],
    ["Ele nunca fica mais de um dia.", "Eu sempre olho o que ele traz.", "As lhamas dele cospem em quem se aproxima."],
    ["Cada visita rende uma boa história.", "Quem sabe ele volte."])
fam("vila_gato", ["villager", "villager"], [],
    ["Gato na vila é bom para afastar creeper.", "Dizem que os gatos protegem a vila dos creepers.", "Um gato dormindo na porta dá sorte."],
    ["Creeper nem chega perto de um gato.", "Eu deixo leite para o gato da vila.", "Gato quieto é gato em guarda."],
    ["Cada um com o seu trabalho.", "Gato é bem-vindo em qualquer casa."])
fam("vila_esmeralda", ["villager", "villager"], ["day"],
    ["Esmeralda é a moeda que todo mundo quer.", "O preço das esmeraldas muda todo dia.", "Guardar esmeraldas é guardar sossego."],
    ["Quem negocia bem nunca passa aperto.", "Eu troco o que sobra por esmeraldas.", "Um bom estoque vale ouro, ou melhor, esmeralda."],
    ["Negócio honesto é o melhor negócio.", "Cada esmeralda conta."])
fam("vila_tempestade", ["villager", "villager"], ["thunder"],
    ["Fechei as janelas por causa da tempestade.", "Esse temporal sacode a casa inteira.", "A tempestade assusta até o golem."],
    ["Dentro de casa a gente fica seguro.", "Raio que cai perto faz tudo tremer.", "Eu acendo uma tocha e espero passar."],
    ["Amanhã o céu limpa.", "A vila aguenta qualquer tempo."], weight=2)
fam("vila_pao", ["villager", "villager"], ["day"],
    ["O pão fresco cheira a casa boa.", "Falta pão quando a colheita é fraca.", "Pão repartido rende mais."],
    ["Trigo bom faz pão bom.", "Eu guardo um pouco para o dia seguinte.", "Uma casa sem pão fica triste."],
    ["O melhor perfume é o do forno.", "Pão e conversa fazem a vila."])
fam("vila_porta_noite", ["villager", "villager"], ["night"],
    ["Eu confiro a porta antes de dormir.", "A porta bem trancada é a minha paz.", "À noite todo cuidado é pouco."],
    ["Zumbi quebra porta de madeira, sabia?", "Uma luz na entrada ajuda a afastar os monstros.", "Eu prendo tudo duas vezes."],
    ["Dorme bem.", "Amanhã tudo estará no lugar."], weight=2)
fam("vila_manha", ["villager", "villager"], ["day"],
    ["A manhã na vila é a melhor hora.", "Gosto de abrir a porta quando o sol nasce.", "Um dia novo sempre traz boas trocas."],
    ["Todo mundo acorda de bom humor.", "O trabalho rende mais cedo.", "O ar da manhã faz bem."],
    ["Que o dia seja bom.", "Vamos aproveitar a luz."])
fam("golem_orgulho", ["villager", "golem"], [],
    ["Quando o golem passa, a vila respira aliviada.", "Com o golem por perto, durmo tranquilo.", "Já vi o golem proteger uma casa inteira."],
    ["Eu só faço o que preciso.", "Ninguém passa enquanto eu estiver de pé.", "Proteger a vila é o meu trabalho."],
    ["Por isso a vila ainda existe.", "A vila agradece."], weight=2)
fam("golem_ronda", ["golem", "villager"], [],
    ["Dei a volta na vila e está tudo calmo.", "Fiz a ronda e nada se mexeu.", "A noite foi tranquila na vila."],
    ["Valeu, a gente dorme melhor assim.", "Sem você, eu não teria coragem de sair.", "A vila é segura por sua causa."],
    ["Fico de pé até amanhecer.", "É o que sei fazer."], weight=2)

# ---- aldeoes: ofícios
fam("acougueiro", ["villager:butcher", "villager"], [],
    ["A carne de hoje está bem fresca.", "Cortar carne é um ofício que exige respeito.", "Meu cutelo está afiado como nunca."],
    ["Sem você, a vila passaria fome.", "Cheiro de carne assando chama todo mundo.", "Cada ofício tem a sua importância."],
    ["Passa aqui quando quiser levar um pedaço.", "O fogo do defumador nunca apaga."])
fam("pescador", ["villager:fisherman", "villager"], [],
    ["O rio hoje está generoso.", "Peixe fresco é meu orgulho.", "A isca certa muda tudo."],
    ["Peixe assado é um banquete.", "Eu nunca tive paciência para pescar.", "A vila come melhor por sua causa."],
    ["Amanhã volto cedo ao rio.", "Quem pesca aprende a esperar."])
fam("flecheiro", ["villager:fletcher", "villager"], [],
    ["Fiz flechas novas esta manhã.", "Pena de galinha faz flecha voar reto.", "Uma boa flecha precisa de equilíbrio."],
    ["Precisão é uma virtude.", "A vila fica mais segura com boas flechas.", "Isso é trabalho de mestre."],
    ["Cada flecha tem seu destino.", "Mira firme e coração calmo."])
fam("pedreiro", ["villager:mason", "villager"], [],
    ["A pedra de hoje está ótima.", "Pedra lisa dá casa firme.", "Um muro bem feito dura gerações."],
    ["As casas daqui duram mesmo.", "Sem pedra, a vila seria de lama.", "O seu trabalho segura tudo de pé."],
    ["Pedra sobre pedra, a vila cresce.", "Tem muito o que construir ainda."])
fam("armeiro", ["villager:armorer", "villager"], [],
    ["A armadura sai melhor com o fogo forte.", "Ferro bem forjado é meio caminho andado.", "Cada peça de armadura conta uma história."],
    ["Quem usa uma peça sua fica mais seguro.", "O barulho da forja é música.", "Deve ser pesado trabalhar com tanto metal."],
    ["Metal bom vira proteção boa.", "O fogo da forja não pode apagar."])
fam("ferreiro_armas", ["villager:weaponsmith", "villager"], [],
    ["Afiar uma espada pede paciência.", "Hoje o machado ficou perfeito.", "Uma lâmina boa nunca precisa de pressa."],
    ["Espero que nunca precisem usar.", "A vila dorme melhor com ferro bom por perto.", "Isso é arte, não só ofício."],
    ["Arma boa é a que não precisa sair da bainha.", "Mais um dia, mais uma lâmina."])
fam("curtidor", ["villager:leatherworker", "villager"], [],
    ["O couro de hoje está macio.", "Bota de couro dura uma vida.", "Cortar couro pede mão firme."],
    ["Ninguém anda bem sem bota boa.", "O cheiro de couro é inconfundível.", "Esse ofício é mais difícil do que parece."],
    ["Couro bem curtido não rasga.", "Cada peça sai única."])
fam("cartografo", ["villager:cartographer", "villager"], [],
    ["Desenhei um mapa novo hoje.", "Mapa bom mostra mais do que estradas.", "Todo caminho começa num bom desenho."],
    ["Eu nunca saio da vila, mas gosto de olhar os mapas.", "Deve ser bonito ver o mundo no papel.", "O mundo além das colinas deve ser enorme."],
    ["Um dia alguém percorre o que eu desenhei.", "O mapa guarda o que ninguém viu."])

# ---- calmos gerais
fam("ceu_nuvens", ["calm", "calm"], ["day", "clear"],
    ["Tem nuvem bonita no céu hoje.", "O céu hoje está bem azul.", "Olha como o céu está limpo."],
    ["Dá gosto de olhar para cima.", "Dia assim alegra qualquer um.", "Eu podia passar o dia olhando para o céu."],
    ["Que ele fique assim o dia todo.", "Hoje está tudo calmo."])
fam("ceu_estrelas", ["calm", "calm"], ["night", "clear"],
    ["Dá para ver as estrelas hoje.", "O céu está cheio de estrelas.", "A lua está bonita hoje."],
    ["Nunca vi tantas juntas.", "A noite fica menos assustadora assim.", "Cada uma parece uma luzinha."],
    ["Boa noite para olhar para cima.", "Mas fica por perto."], weight=2)
fam("madrugada", ["calm", "calm"], ["dawn"],
    ["Acordei cedo hoje.", "A manhã está chegando devagar.", "O primeiro sol é o mais bonito."],
    ["Gosto desse silêncio antes de todos acordarem.", "O dia que começa assim promete.", "O frio da madrugada passa logo."],
    ["Vai ser um bom dia.", "Que ele seja tranquilo."], weight=2)
fam("noite_frio", ["calm", "calm"], ["night"],
    ["A noite está fria.", "O vento da noite gela até os ossos.", "Hoje esfriou de verdade."],
    ["Fica pertinho de mim que esquenta.", "Escondido do vento é melhor.", "A gente se acostuma com o frio."],
    ["Amanhã o sol esquenta de novo.", "Ainda bem que o dia volta."])
fam("jogador_voltou", ["calm", "calm"], ["k0"],
    ["O humano voltou por aqui.", "Olha só, o humano de novo.", "Já conheço esse humano."],
    ["Gosto quando ele volta.", "Ele sempre traz movimento.", "Ele já é de casa."],
    ["Que fique à vontade.", "Vamos ver o que ele quer."], weight=2)

# ---- monstros (entre si)
fam("zumbi_sol", ["zombie", "zombie"], ["day"],
    ["Grrr... o sol queima a minha pele.", "Grrr... odeio o dia.", "Grrr... preciso de sombra."],
    ["Grrr... a sombra é a única amiga.", "Grrr... espera a noite chegar.", "Grrr... quem tem a pele podre sofre mais."],
    ["Grrr... paciência.", "Grrr... falta pouco."])
fam("zumbi_porta", ["zombie", "zombie"], [],
    ["Grrr... aquela porta de madeira é dura.", "Grrr... humano atrás da porta é um prêmio.", "Grrr... bater na porta cansa."],
    ["Grrr... um dia ela cede.", "Grrr... paciência abre qualquer madeira.", "Grrr... o cheiro de carne vem de dentro."],
    ["Grrr... insiste.", "Grrr..."])
fam("esqueleto_sombra", ["skeleton", "skeleton"], ["day"],
    ["O sol arruína os meus ossos.", "Fico sempre perto de uma sombra.", "De dia eu me escondo."],
    ["Dia é bom para ficar parado.", "À noite a mira é melhor.", "Quem se queima não acerta nada."],
    ["Quando escurecer, eu atiro.", "Falta pouco para a minha hora."])
fam("esqueleto_flechas", ["skeleton", "skeleton"], [],
    ["Minhas flechas acabam rápido.", "Uma boa flecha precisa de pena e osso.", "Cada flecha que erra é uma vergonha."],
    ["Pega algumas do chão.", "A pontaria vem com a prática.", "Eu nunca erro duas vezes."],
    ["Mais uma tentativa.", "Vou acertar da próxima."])
fam("aranha_caverna", ["spider", "spider"], [],
    ["Sss... a caverna é melhor que a superfície.", "Sss... gosto do escuro e das paredes.", "Sss... a teia é a minha casa."],
    ["Sss... na sombra ninguém nos vê.", "Sss... a luz do sol incomoda os olhos.", "Sss... subir é mais fácil que andar."],
    ["Sss... a noite é nossa.", "Sss..."])
fam("aranha_presa", ["spider", "spider"], [],
    ["Sss... o humano é muito barulhento.", "Sss... hoje vou tecer uma teia grande.", "Sss... o que se mexe vira jantar."],
    ["Sss... paciência é tudo.", "Sss... a teia faz o serviço sozinha.", "Sss... espere ele chegar mais perto."],
    ["Sss... silêncio agora.", "Sss... quase."])
fam("creeper_gato", ["creeper", "creeper"], [],
    ["Ssss... gato por perto não dá.", "Ssss... aquele miado me arrepia.", "Ssss... gato é inimigo declarado."],
    ["Ssss... eu corro assim que ouço.", "Ssss... eles me olham e eu me esvazio.", "Ssss... melhor evitar."],
    ["Ssss... fica longe deles.", "Ssss..."])
fam("creeper_silencio", ["creeper", "creeper"], [],
    ["Ssss... o segredo é chegar sem fazer barulho.", "Ssss... paciência antes da explosão.", "Ssss... eu sou só um sussurro."],
    ["Ssss... a surpresa é metade da graça.", "Ssss... quem escuta tarde, escuta demais.", "Ssss... o chiado é o único aviso."],
    ["Ssss... silêncio.", "Ssss..."])
fam("enderman_bloco", ["enderman", "enderman"], [],
    ["Achei um bloco interessante hoje.", "Gosto de levar os blocos que me agradam.", "Cada bloco que carrego é um tesouro."],
    ["Eu também guardo os meus em lugares escondidos.", "Ninguém entende o valor de um bloco.", "É só um capricho nosso."],
    ["Ninguém precisa saber.", "Cada um com o seu tesouro."])
fam("enderman_agua", ["enderman", "enderman"], [],
    ["A chuva é a minha pior inimiga.", "A água me queima como fogo.", "Eu fico longe de qualquer poça."],
    ["Eu me teletransporto quando ela cai.", "Água em excesso é um castigo.", "Por isso eu prefiro lugares secos."],
    ["Que a chuva fique longe.", "Eu sumo antes que me molhe."])
fam("bruxa_cogumelo", ["witch", "witch"], [],
    ["O cogumelo de hoje deu uma poção ótima.", "Pântano bom é o que tem cogumelo à vontade.", "Cada ingrediente tem a sua hora."],
    ["Meu caldeirão está sempre fervendo.", "Eu misturo de tudo um pouco.", "Poção ruim vira lição."],
    ["Hehe, a receita é segredo.", "Já sei qual testar primeiro."])
fam("bruxa_pocao2", ["witch", "witch"], [],
    ["Poção de lentidão ficou perfeita hoje.", "Hehe, acertei a mistura certa.", "Uma poção nova sempre me anima."],
    ["Eu prefiro as venenosas.", "A minha acerta de longe.", "Quero ver o efeito nos humanos."],
    ["Hehe, a prática leva à perfeição.", "Hehe, depois a gente compara."])
fam("warden_sculk", ["warden", "warden"], [],
    ["O sculk sente tudo ao meu redor.", "Cada passo faz o chão vibrar.", "Eu escuto o coração dos intrusos."],
    ["O silêncio é o meu aliado.", "Quem corre faz mais barulho.", "Eu não preciso de olhos."],
    ["Escuta.", "Quieto."])
fam("monstros_lua", ["monsters", "monsters"], ["night"],
    ["A lua está cheia hoje.", "Noite clara é noite de caça.", "Quanto mais escuro, melhor."],
    ["Os humanos dormem e a gente trabalha.", "Hoje ninguém escapa.", "A escuridão é nossa casa."],
    ["Que a noite seja longa.", "Vamos começar."])
fam("monstros_armadura", ["monsters", "monsters"], ["armor"],
    ["O humano veste {armadura}. Vai dar trabalho.", "Aquele humano está com {armadura}.", "Olha só, o humano trouxe {armadura}."],
    ["Todo metal cansa no fim.", "Eu espero ele baixar a guarda.", "A gente cansa ele primeiro."],
    ["Paciência vence qualquer armadura.", "Cada brecha serve."], weight=3)
fam("monstros_item", ["monsters", "monsters"], ["holds"],
    ["O humano carrega {item}. Cuidado.", "Aquele humano tem {item} na mão.", "O humano segura {item}."],
    ["Se ele usar, vai doer.", "Eu vou pela esquerda.", "Ele ainda não nos viu."],
    ["Espera ele abaixar a guarda.", "Agora não."], weight=3)
fam("monstros_ferido", ["monsters", "monsters"], ["hurt"],
    ["O humano está ferido.", "Cheiro de sangue por perto.", "O humano mal consegue andar."],
    ["É a nossa chance.", "Ele não vai longe.", "Eu fico por perto."],
    ["Sem pressa.", "Deixa ele vir."], weight=3)


# ---- rodada 2 do juiz: reescritas das familias que ficaram abaixo de 4.0 (respostas sobre o MESMO assunto, sem perguntas soltas, sem adjetivos de genero)
drop("cercado", "jogador_nome_conta", "jogador_dono", "gato", "golem_flor", "bicho_jogador_noite", "jogador_apresenta", "bicho_filhotes_cuidado",
     "clerigo", "curiosidade_quadrado", "jogador_nome_ambos", "warden_sculk")

fam("cercado", ["farm", "farm"], [],
    ["O cercado está cheio hoje.", "Tem espaço de sobra aqui dentro.", "A cerca deixa a gente todo no mesmo canto."],
    ["Gosto de ter companhia por perto.", "Aqui dentro a grama chega para todos.", "Perto da cerca é onde eu fico."],
    ["Assim ninguém se perde.", "Hoje está bom aqui."])
fam("jogador_nome_conta", ["calm", "calm"], ["n0", "!n1"],
    ["O nome desse humano é {nome}, ele me contou.", "Sabe o nome daquele humano? É {nome}.", "Aquele humano me disse que se chama {nome}."],
    ["{Nome}? Não sabia, vou guardar.", "Então é {nome}. Obrigado por contar.", "Agora posso chamar pelo nome."],
    ["Quando ele voltar, eu chamo pelo nome.", "Nome ajuda a lembrar de quem é."], weight=4)
fam("jogador_nome_ambos", ["calm", "calm"], ["n0", "n1"],
    ["{Nome} voltou! Que bom ver.", "Olha quem está aí, {nome}.", "{Nome} está por aqui hoje."],
    ["É sempre bom ver {nome} por aqui.", "Fico contente quando {nome} aparece.", "Também gosto de ver {nome}."],
    ["Vou dar um oi.", "Bom ter gente conhecida."], weight=4)
fam("jogador_dono", ["calm", "calm"], ["f0:TAMED"],
    ["Agora eu sou de {jogador}, e isso me faz bem.", "{Jogador} cuida de mim agora, sabia?", "{Jogador} é o meu dono agora."],
    ["Ele te trata bem?", "Está feliz assim?", "Está sendo bem cuidado?"],
    ["Trata, sim, e estou bem.", "Estou, sim, de verdade."], weight=4)
fam("gato", ["cat", "cat"], [],
    ["Dormi no sol a tarde toda.", "Hoje eu quase não saí do lugar.", "Sol quente e silêncio é tudo que eu peço."],
    ["Sol é o melhor travesseiro.", "Cochilo bom não precisa de mais nada.", "Eu também prefiro ficar parado."],
    ["Miau, agora vou dormir de novo.", "Ninguém me acorda hoje."])
fam("golem_flor", ["golem", "villager"], ["day"],
    ["Achei uma papoula perto do caminho e guardei.", "Gosto de levar uma flor a quem trata bem os moradores."],
    ["Flor combina com você, mesmo sendo de ferro.", "Isso alegra a vila toda.", "A vila gosta quando você faz isso."],
    ["É o meu jeito de agradecer.", "Cuidar também é isso."])
fam("bicho_jogador_noite", ["animals", "animals"], ["night"],
    ["Tem alguém andando no escuro.", "O humano caminha de noite.", "Olha lá, o humano no escuro."],
    ["Eu fico quieto até ele passar.", "Melhor ele passar longe de mim.", "Ele devia ter medo do escuro."],
    ["Eu fico por aqui.", "Que passe sem nos incomodar."], weight=2)
fam("jogador_apresenta", ["calm", "calm"], ["k0", "!k1"],
    ["{Jogador} ali já esteve comigo antes.", "Aquele humano eu já conheço de outros dias.", "Reconheci {jogador}, já nos vimos antes."],
    ["Eu ainda não o conhecia.", "Para mim é a primeira vez que o vejo.", "Nunca tinha cruzado com ele."],
    ["Vale a pena conhecer.", "Ele é tranquilo, pode confiar."], weight=3)
fam("bicho_filhotes_cuidado", ["animals", "animals"], [],
    ["Os filhotes não param quietos.", "Os pequenos vivem atrás da gente."],
    ["Eu fico de olho para não se perderem.", "Daqui a pouco já estão do nosso tamanho.", "Os meus dormem em cima de mim."],
    ["Tomara que cresçam fortes.", "Daqui a pouco eles se viram sozinhos."])
fam("clerigo", ["villager:cleric", "villager"], [],
    ["As poções de hoje ficaram boas.", "Tem muita coisa misturada no meu caldeirão.", "Dizem que meu laboratório cheira estranho."],
    ["Cheira mesmo, mas a gente se acostuma.", "Se alguém adoecer, você dá um jeito.", "Eu prefiro não provar nada daí."],
    ["Poção boa não precisa de cheiro bom.", "No fim, sempre funciona."])
fam("curiosidade_quadrado", ["villager", "villager"], [],
    ["Tudo aqui é quadrado, já reparou?", "Eu sonhei que o mundo era redondo.", "O sol nasce sempre do mesmo lado, já reparou?"],
    ["Já reparei, e nunca entendi por quê.", "Que ideia estranha, mas me deixou pensando.", "Esse mundo é cheio de mistérios."],
    ["Pelo menos é um mundo bonito.", "Melhor não pensar muito nisso."])
fam("warden_sculk", ["warden", "warden"], [],
    ["O sculk sente tudo ao meu redor.", "Cada passo faz o chão vibrar.", "Eu escuto o coração dos intrusos."],
    ["O silêncio é o meu aliado.", "Quem corre faz mais barulho.", "Eu não preciso de olhos."],
    ["O sculk nunca se engana.", "Quieto, alguém se aproxima."])


# ---- rodada 3 do juiz: falas mais coladas no mesmo assunto
drop("curiosidade_quadrado", "cercado", "creeper_zumbi", "bicho_jogador_chuva", "bicho_jogador_fome", "bicho_jogador_noite", "curiosidade_enderman",
     "jogador_ameacou", "jogador_apresenta", "jogador_conhecido", "jogador_dono")

fam("curiosidade_quadrado", ["villager", "villager"], [],
    ["Tudo aqui é quadrado, já reparou?", "O sol nasce sempre do mesmo lado, já reparou?"],
    ["Já reparei, e nunca entendi por quê.", "Reparei, mas nunca perguntei a ninguém."],
    ["Quem sabe um dia alguém explica.", "Deixa para lá, a vila é boa assim."])
fam("cercado", ["farm", "farm"], [],
    ["O cercado está movimentado hoje.", "Tem bastante gente aqui no cercado hoje."],
    ["Gosto de ter companhia por perto.", "Assim ninguém fica sozinho."],
    ["Aqui dentro a gente se sente em casa.", "Que o dia seja calmo."])
fam("creeper_zumbi", ["creeper", "zombie"], [],
    ["Ssss... gato me dá arrepio.", "Ssss... odeio quando um gato aparece."],
    ["Grrr... gato assusta até quem não tem medo.", "Grrr... eu mantenho distância."],
    ["Ssss... é melhor ficar longe deles.", "Ssss... assunto encerrado."], weight=3)
fam("creeper_zumbi_explode", ["creeper", "zombie"], [],
    ["Ssss... hoje eu vou explodir alguma coisa.", "Ssss... estou com vontade de explodir."],
    ["Grrr... explode longe de mim.", "Grrr... só não me leve junto."],
    ["Ssss... fico de longe, prometo.", "Ssss... sem promessas."], weight=3)
fam("bicho_jogador_chuva", ["animals", "animals"], ["rain"],
    ["O humano está na chuva.", "Olha o humano debaixo da chuva."],
    ["Ele deve estar todo molhado.", "Eu prefiro um canto seco."],
    ["Eu fico aqui, abrigado.", "Cada um com o seu jeito de passar a chuva."], weight=2)
fam("bicho_jogador_fome", ["animals", "animals"], ["hungry"],
    ["O humano está com cara de fome.", "O humano ficou olhando para a comida.", "O humano deve estar faminto."],
    ["Ele devia achar algo para comer.", "Eu também fico de mau humor quando passo fome."],
    ["Tomara que ele ache comida logo.", "Eu entendo bem essa fome."], weight=3)
fam("bicho_jogador_noite", ["animals", "animals"], ["night"],
    ["Tem alguém andando no escuro.", "O humano caminha de noite.", "Olha lá, o humano no escuro."],
    ["Eu me encolho até ele passar.", "Melhor ele passar longe de mim."],
    ["Tomara que passe sem nos incomodar.", "Daqui a pouco o silêncio volta."], weight=2)
fam("curiosidade_enderman", ["villager", "villager"], ["night"],
    ["Dizem que enderman odeia água.", "Já viu aqueles olhos roxos no escuro?", "Tem uns bichos altos que levam blocos embora."],
    ["Melhor não encarar, dizem que ficam bravos.", "Eles somem num piscar de olhos.", "Eu mantenho distância deles."],
    ["Deve ser uma vida estranha.", "Melhor ficar longe deles."])
fam("jogador_ameacou", ["calm", "calm"], ["f0:THREAT_SPOKEN"],
    ["{Jogador} me ameaçou uma vez, só com palavras.", "Eu me assustei com as palavras de {jogador} outro dia."],
    ["Tem quem fale sem pensar.", "Espero que tenha sido um mal-entendido.", "Palavras assustam, eu entendo."],
    ["Vou dar uma chance a ele.", "Vou ficar de olho, por via das dúvidas."], weight=4)
fam("jogador_apresenta", ["calm", "calm"], ["k0", "!k1"],
    ["{Jogador} ali já esteve comigo antes.", "Aquele humano eu já conheço de outros dias."],
    ["Eu ainda não o conhecia.", "Para mim é a primeira vez."],
    ["Ele é tranquilo, pode confiar.", "Quem sabe vocês se dão bem."], weight=3)
fam("jogador_conhecido", ["calm", "calm"], ["k0"],
    ["Eu já conversei com {jogador}, sabia?", "Já cruzei com {jogador} por aqui antes.", "{Jogador} e eu já conversamos um pouco."],
    ["E como foi?", "Foi tranquilo?"],
    ["Foi tranquilo, achei simpático.", "Dá para conversar numa boa."], weight=2)
fam("jogador_dono", ["calm", "calm"], ["f0:TAMED"],
    ["Agora eu sou de {jogador}, e isso me faz bem.", "{Jogador} cuida de mim agora, sabia?", "{Jogador} é o meu dono agora."],
    ["Ele te trata bem?", "Está feliz assim?"],
    ["Ele me trata bem, estou feliz.", "Estou feliz, sim, de verdade."], weight=4)


from genambient_ev import add as _add_ev  # noqa: E402

_add_ev(fam)  # reacoes ao mundo (ev_*) e assuntos novos de ambiente

from genambient_puxa import add as _add_puxa  # noqa: E402

_add_puxa(fam)  # a criatura puxa assunto com o jogador (ev_puxa_*)
drop("curiosidade_quadrado")  # o juiz a achou filosofica demais para aldeoes, nas tres reescritas

GENERIC_B = ["Pois é.", "É verdade.", "Também acho.", "Nem me fale."]
for _f in FAMILIES:
    if _f["id"] in {"pasto", "colheita", "curiosidade_fundo", "curiosidade_portal", "curiosidade_creeper", "sol", "chuva", "bicho_sombra"}:
        _f["bg"] = list(GENERIC_B)  # so depois de afirmacao (o motor nunca as usa para responder a uma pergunta)


def check():
    import re
    slot = re.compile(r"\{([^}]*)\}")
    combos = 0
    for f in FAMILIES:
        lists = [f["a"]] if f.get("solo") else [f["a"], f["b"]] + ([f["c"]] if f["c"] else [])
        assert not f.get("solo") or not (f["b"] or f["c"]), f["id"]
        assert f["id"].startswith("ev_") == any(n.startswith("ev:") for n in f["need"]), f["id"]
        n = 1
        for lst in lists:
            n *= len(lst)
            for t in lst:
                assert "—" not in t and "–" not in t, t
                assert len(t) <= 110, t
                for s in slot.findall(t):
                    assert s in ("jogador", "Jogador", "nome", "Nome", "item", "Item", "armadura", "coisa", "Coisa") or (s.startswith("@") and s[1:] in POOLS), (f["id"], s)
                    if s in ("item", "Item"):
                        assert "holds" in f["need"], f["id"]
                    if s in ("coisa", "Coisa"):
                        assert any(n.startswith("ev:") for n in f["need"]), f["id"]
                    if s == "armadura":
                        assert "armor" in f["need"], f["id"]
                    if s in ("nome", "Nome"):
                        assert "n0" in f["need"] or "n1" in f["need"], f["id"]
        combos += n
    return combos


if __name__ == "__main__":
    total = check()
    OUT.write_text(json.dumps({"pools": POOLS, "families": FAMILIES}, ensure_ascii=False, indent=1), encoding="utf-8")
    print(f"{len(FAMILIES)} familias, {total} combinacoes de cena (antes dos espacos {{@pool}}) -> {OUT}")
