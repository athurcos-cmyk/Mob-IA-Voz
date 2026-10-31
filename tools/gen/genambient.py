"""Gera mod-fabric/src/main/resources/mobvoz/ambient.json: conversas AMBIENTE entre criaturas (sem IA, sem custo).

Cada FAMILIA e um assunto: lista de falas de abertura (a, quem fala primeiro), respostas (b) e, opcionalmente, fecho (c).
Uma cena = uma fala de cada lista, sorteada, entao uma familia com 6 aberturas x 6 respostas x 4 fechos rende 144 cenas.
O servidor so escolhe familias cujas condicoes (need) sao VERDADEIRAS agora (hora, clima, estado do jogador, o que cada
criatura realmente sabe sobre ele): a fala ambiente nunca inventa fato. Nada aqui muda o estado do jogo.

Campos: who = [classe de quem abre, classe de quem responde]; need = condicoes (prefixo ! nega); weight = peso do sorteio.
Classes (quem pode abrir/responder): calm (aldeao ou animal), villagers (aldeoes e golem), animals, monsters (so da MESMA especie: zombie
           skeleton spider creeper enderman witch warden), e as especies: farm cow pig sheep chicken pet wolf cat horse golem villager villager:<profissao>.
           Duas criaturas so conversam se compartilham o tipo (aldeao com aldeao, animal com animal, monstro com a mesma especie).
Condicoes: day dusk night dawn clear rain thunder hurt hungry holds want0 (quem abre GOSTA do que o jogador segura) armor k0 k1 (criatura 0/1 ja conhece o jogador) n0 n1 (sabe o NOME)
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


def fam(id, who, need, a, b, c=None, weight=1):
    about = id.startswith("jogador_") or "holds" in need or "armor" in need
    FAMILIES.append({"id": id, "who": who, "need": need, "weight": weight, "player": about, "a": a, "b": b, "bg": [], "c": c or []})


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
    ["Elas são teimosas mesmo.", "Quando precisar de ajuda, chama.", "Cuidar de rebanho dá trabalho."],
    ["No fim, elas voltam sempre.", "Eu gosto delas."])

# ----------------------------------------------------------------------------------------------- bichos
fam("pasto", ["farm", "farm"], ["day"],
    ["Esse capim está uma delícia.", "Passei a manhã pastando, que paz.", "Viu aquela touceira de grama ali?", "O pasto hoje está macio."],
    ["Só não gosto quando o vento levanta poeira.", "Já comi tanto que nem me mexo.", "Dá para viver só disso."],
    ["A vida é simples, e isso me basta.", "Aqui a gente está seguro."])
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
fam("bicho_cheiro", ["animals", "animals"], ["day"],
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


GENERIC_B = ["Pois é.", "É verdade.", "Também acho.", "Nem me fale."]
for _f in FAMILIES:
    if _f["id"] in {"pasto", "cercado", "afazeres", "fofoca_vila", "colheita", "curiosidade_quadrado", "curiosidade_fundo", "curiosidade_portal",
                    "curiosidade_creeper", "curiosidade_abelha", "jogador_sem_nome", "sol", "chuva", "bicho_cheiro", "bicho_sombra"}:
        _f["bg"] = list(GENERIC_B)  # so depois de afirmacao (o motor nunca as usa para responder a uma pergunta)


def check():
    import re
    slot = re.compile(r"\{([^}]*)\}")
    combos = 0
    for f in FAMILIES:
        lists = [f["a"], f["b"]] + ([f["c"]] if f["c"] else [])
        n = 1
        for lst in lists:
            n *= len(lst)
            for t in lst:
                assert "—" not in t and "–" not in t, t
                assert len(t) <= 110, t
                for s in slot.findall(t):
                    assert s in ("jogador", "Jogador", "nome", "Nome", "item", "Item", "armadura") or (s.startswith("@") and s[1:] in POOLS), (f["id"], s)
                    if s in ("item", "Item"):
                        assert "holds" in f["need"], f["id"]
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
