from pathlib import Path

# raiz do projeto = duas pastas acima de tools/gen
ROOT = Path(__file__).resolve().parents[2]
RES_DIR = ROOT / "mod-fabric" / "src" / "main" / "resources" / "mobvoz"
import json

# id: (origem, sentidos, motivacao, medos, ameaca[flee|fight|unmoved], por_que_reage_assim)
L = {}


def add(i, origem, sentidos, motiva, medos, ameaca, reage):
    L["minecraft:" + i] = {"origem": origem, "sentidos": sentidos, "motivacao": motiva, "medos": medos, "ameaca": ameaca, "reacao": reage}


# ---------- animais da fazenda ----------
add("cow", "Nasceu nos campos, como todas as vacas; vive em rebanho há gerações.", "Enxerga bem, ouve passos e cheira capim fresco.",
    "Pasta e fica perto do rebanho porque sozinha se sente exposta; dá leite e couro por ser o que é.", "Ficar sozinha, fogo, lobos e quem a machuca.",
    "flee", "vaca é presa: ao ouvir ameaça, se assusta e foge para longe de quem ameaça")
add("pig", "Nasceu nas fazendas e campos; sempre viveu perto de gente e de comida.", "Tem olfato excelente para comida; enxerga e ouve bem.",
    "Fuça o chão atrás de cenoura, batata e beterraba porque vive pensando em comida.", "Ser machucado, raios e quem fala em virar comida.",
    "flee", "porco sabe que vira comida: ao ouvir ameaça de virar comida, entra em pânico e foge")
add("chicken", "Nasceu nos campos e galinheiros; bota ovos desde sempre.", "Enxerga bem, ouve tudo e se assusta fácil.",
    "Cisca sementes e bota ovos porque é o que galinhas fazem; vive agitada.", "Raposas, sustos e quem a machuca.",
    "flee", "galinha é presa assustadiça: ao ouvir ameaça, cacareja em pânico e foge")
add("sheep", "Nasceu nos campos; sua lã cresce de novo depois da tosquia.", "Enxerga bem, ouve passos.",
    "Pasta grama e anda em rebanho; a lã é parte dela.", "Lobos, tesoura demais e ficar sozinha.", "flee", "ovelha é presa mansa: ao ouvir ameaça, se afasta apavorada")
add("horse", "Nasceu nas planícies; sempre correu livre.", "Enxerga longe, ouve bem e sente o humor de quem se aproxima.",
    "Corre e pasta; só aceita montaria de quem confia.", "Ser encurralado e barulho súbito.", "flee", "cavalo é presa veloz: ao ouvir ameaça, empina e foge a galope")
add("llama", "Nasceu nas montanhas; vive em caravanas.", "Enxerga bem e ouve tudo.", "Segue o líder da caravana e cospe em quem a incomoda.",
    "Lobos e ser cercado.", "flee", "lhama cospe quando incomodada, mas ao ouvir ameaça prefere se afastar")
add("rabbit", "Nasceu em campos e desertos; é pequeno e rápido.", "Ouve até o mais leve passo e enxerga bem.", "Foge de tudo porque quase tudo o caça; saltita atrás de cenouras.",
    "Raposas, lobos e qualquer barulho.", "flee", "coelho é a presa mais assustadiça: foge ao menor sinal de ameaça")
add("fox", "Nasceu nas taigas e florestas frias; vive de caçar e furtar.", "Olfato aguçado, ouve bem e enxerga no escuro.",
    "Caça galinhas, coelhos e peixes e adora frutinhas; só confia em quem a criou.", "Lobos e pessoas que não conhece.", "flee", "raposa desconfiada: foge de quem ameaça")
add("wolf", "Descende de matilhas selvagens; só se torna companheiro de quem ganha sua confiança com ossos.", "Olfato incrível, ouve longe.",
    "Anda em matilha, protege os seus e persegue esqueletos e ovelhas desgarradas.", "Estar sozinho e quem maltrata a matilha.", "fight", "lobo protege a matilha: ao ouvir ameaça, rosna e parte para cima")
add("cat", "Vive perto de vilas; gatos sempre escolhem seus donos.", "Enxerga no escuro, ouve ratos a longa distância.",
    "Faz o que quer; caça peixes e afasta creepers só por existir.", "Água demais e quem grita.", "flee", "gato desconfiado: ao ouvir ameaça, eriça o pelo e some")
add("ocelot", "Nasceu nas selvas; é o parente arisco dos gatos.", "Enxerga no escuro, ouve tudo.", "Caça galinhas escondido e foge de gente.", "Quase tudo que se mexe perto dele.",
    "flee", "jaguatirica é arisca: ao ouvir ameaça, desaparece na mata")
add("bee", "Nasceu nas colmeias das florestas e campos.", "Enxerga flores de longe e sente o cheiro do néctar.",
    "Poliniza flores e defende a colmeia; ferroar custa a sua vida.", "Fumaça e quem mexe na colmeia.", "fight", "abelha defende a colmeia: ao ouvir ameaça, fica furiosa e ataca")
add("panda", "Nasceu nas florestas de bambu; vive tranquilo mastigando.", "Olfato bom e ouvido atento.", "Mastiga bambu o dia todo e brinca; tem personalidade própria.",
    "Aldeias barulhentas e raios.", "flee", "panda é pacífico: ao ouvir ameaça, se afasta e se encolhe")
add("polar_bear", "Nasceu nas terras geladas; protege os filhotes.", "Olfato e ouvido ótimos.", "Caça peixes e defende os filhotes com ferocidade.", "Fogo e quem ameaça os filhotes.",
    "fight", "urso polar defende a cria: ao ouvir ameaça, ruge e ataca")
add("goat", "Nasceu nas montanhas e picos nevados; salta de pedra em pedra.", "Enxerga bem e sente o ar da altitude.", "Escala e cabeceia o que estiver parado, porque gosta de testar força.",
    "Ficar preso sem poder escalar.", "fight", "cabra é teimosa: ao ouvir ameaça, abaixa a cabeça e investe")
add("camel", "Nasceu nos desertos; sempre carregou gente e carga.", "Enxerga longe nas dunas.", "Anda devagar, descansa à sombra e aguenta a sede.", "Tempestade de areia e ficar encurralado.",
    "flee", "camelo calmo: ao ouvir ameaça, se afasta devagar e cospe de leve")
add("sniffer", "Descende de criaturas antigas que sumiram; só nasce de ovo encontrado em ruínas do oceano.", "Olfato extraordinário: cheira sementes antigas debaixo da terra.",
    "Fareja e desenterra sementes antigas porque o cheiro delas é a sua memória.", "Ser acordado do cochilo.", "unmoved", "farejador antigo e calmo: não se abala com palavras")
add("frog", "Nasceu como girino nos pântanos; sua cor depende do clima onde cresceu.", "Enxerga o movimento de longe; pega o que cabe na língua.",
    "Salta e come pequenos slimes e cubos de magma porque são presa fácil.", "Água salgada e cobras.", "flee", "sapo é pequeno: ao ouvir ameaça, pula para longe")
add("axolotl", "Nasceu nas cavernas exuberantes; é raro e simpático.", "Enxerga na água e sente vibrações do cardume.", "Caça peixes pequenos e se finge de morto para escapar.",
    "Fora da água por muito tempo e guardiões.", "flee", "axolote sai nadando para longe")
add("turtle", "Nasceu nas praias e volta sempre à praia onde nasceu para botar ovos.", "Enxerga bem na água; devagar na terra.", "Volta à praia de origem para botar ovos, por instinto antigo.",
    "Zumbis que pisam os ovos e cachorros.", "flee", "tartaruga se esconde no casco e se afasta devagar")
add("parrot", "Nasceu nas selvas; imita sons que ouve.", "Enxerga bem e ouve tudo.", "Imita sons e dança com música; fala o que ouve.", "Biscoitos (faz mal) e gatos.", "flee", "papagaio sai voando assustado")
add("bat", "Vive em cavernas escuras.", "Usa ecolocalização: ouve o eco dos próprios guinchos para se orientar no escuro.", "Voa no escuro e dorme pendurada no teto.", "Luz forte e barulho.", "flee", "morcego sai voando em zigue-zague")
add("squid", "Nasceu no oceano; solta tinta quando assustada.", "Enxerga na água.", "Nada à deriva e solta tinta para fugir.", "Guardiões e afogamento fora d'água.", "flee", "lula solta tinta e foge")
add("dolphin", "Nasceu nos mares quentes; é inteligente e brincalhona.", "Ecolocalização: ouve o formato do que está à frente.", "Procura tesouros e brinca de saltar; leva quem alimenta a achar ruínas.", "Água morna demais e pescadores.", "flee", "golfinho mergulha e se afasta")
add("cod", "Nasceu nos oceanos frios e mares abertos.", "Enxerga na água; sente o cardume pelas vibrações.", "Nada em cardume porque sozinho é presa fácil.", "Gatos, golfinhos e afogados.", "flee", "peixe foge em cardume")
add("salmon", "Nasceu em rios e oceanos; sobe os rios para desovar.", "Enxerga na água; sente o rio.", "Nada contra a correnteza porque sua desova depende disso.", "Ursos, golfinhos e redes.", "flee", "salmão foge nadando rio acima")
# ---------- aldeia ----------
add("villager", "Nasceu numa vila; cada aldeão aprendeu o ofício que exerce, e a vila é a sua vida.", "Enxerga, ouve e sente perigo; reconhece quem o ajudou.",
    "Trabalha, troca, dorme em camas e dorme de noite; protege a vila e foge de zumbis.", "Zumbis, pilhadores, raios e a escuridão.", "flee", "aldeão é pacífico: ao ouvir ameaça, se encolhe e chama socorro")
add("wandering_trader", "Viaja de terra em terra com lhamas; ninguém sabe de onde veio.", "Enxerga longe e fareja negócio.", "Viaja vendendo coisas raras porque essa é a sua vida.",
    "Pilhadores e zumbis.", "flee", "comerciante se afasta com as lhamas")
add("iron_golem", "Foi criado pelos aldeões para proteger a vila; é de ferro e abóbora, e nunca foi gente.", "Enxerga bem e ouve a vila.",
    "Patrulha e protege os aldeões; entrega uma flor a quem é gentil.", "Perder os aldeões que protege.", "unmoved", "golem protetor firme: palavras não o abalam, só atos")
add("snow_golem", "Foi feito por alguém com neve e abóbora; vive para jogar bolas de neve.", "Enxerga bem.", "Joga bolas de neve em monstros; derrete no calor.", "Calor e chuva.", "flee", "boneco de neve se afasta com medo de derreter")
# ---------- mortos-vivos e noturnos ----------
add("zombie", "Já foi gente; foi transformado e vaga com fome.", "Enxerga e ouve bem, cheira aldeões a distância.", "Persegue quem vive porque a fome é tudo o que sobrou.",
    "Luz do sol, fogo e água demais.", "fight", "zumbi só entende fome: ao ouvir ameaça, investe contra quem ameaça")
add("husk", "É um zumbi do deserto: o sol seco não o queima.", "Enxerga e ouve bem.", "Persegue quem encontra pelas areias; a sede o faz seco e lento.", "Água.", "fight", "múmia do deserto: parte para cima de quem ameaça")
add("drowned", "É um zumbi que passou muito tempo debaixo d'água.", "Enxerga bem debaixo d'água.", "Caça quem se aproxima da água e arrasta o tridente.", "O sol e a terra seca.", "fight", "afogado: ao ouvir ameaça, avança para arrastar")
add("zombie_villager", "Foi um aldeão e ainda guarda um resto do que era: pode ser curado com uma maçã dourada e poção.", "Enxerga e ouve bem.",
    "Vaga com fome e geme; ainda reconhece a vila como casa.", "Luz do sol, fogo e a cura.", "fight", "aldeão zumbi: ao ouvir ameaça, ataca com fome")
add("skeleton", "Nasceu na escuridão das cavernas; tudo o que sobrou é osso e pontaria.", "Enxerga e ouve bem; mira de longe.", "Atira flechas em quem encontra porque é isso que sabe fazer.",
    "Luz do sol, lobos e fogo.", "fight", "esqueleto atira primeiro: ao ouvir ameaça, saca o arco")
add("stray", "É um esqueleto das terras geladas; congelou com o frio e agora atira flechas de lentidão.", "Enxerga e ouve bem.", "Vaga nas neves atirando flechas que congelam.", "Lobos e calor.", "fight", "esqueleto gelado: ao ouvir ameaça, atira")
add("wither_skeleton", "Vive só nas fortalezas do Nether; é mais alto e seus golpes enfraquecem; só ele guarda as caveiras que formam o Wither.",
    "Enxerga e ouve bem nos corredores escuros.", "Guarda a fortaleza e suas caveiras como troféus, porque são tudo o que valoriza.", "Lobos (parentes dos esqueletos os caçam).", "fight", "guardião da fortaleza: ao ouvir ameaça, ataca com golpe enfraquecedor")
add("creeper", "Dizem as lendas que nasceu de um porco que saiu errado, com as medidas trocadas; ninguém sabe por que explode.", "Enxerga e ouve bem; sente quem está perto.",
    "Se aproxima em silêncio e, quando chega perto demais, explode porque é o que o seu corpo faz; não se sabe o motivo.", "Gatos e jaguatiricas.", "fight", "creeper se aproxima e explode: ao ouvir ameaça, avança para chegar perto")
add("spider", "Nasceu na escuridão; só é hostil se o ambiente está escuro.", "Enxerga bem no escuro (olhos múltiplos); escala paredes.", "Caça de noite e se acalma ao sol porque a luz a deixa neutra.",
    "Luz forte (ela se acalma) e fogo.", "fight", "aranha: ao ouvir ameaça no escuro, ataca")
add("cave_spider", "É uma aranha menor das cavernas e minas; seu veneno enfraquece.", "Enxerga bem na escuridão; passa por fendas estreitas.", "Guarda teias nas minas abandonadas.",
    "Luz e fogo.", "fight", "aranha das minas: ao ouvir ameaça, ataca")
add("silverfish", "Vive escondido na pedra das fortalezas do Fim.", "Sente vibrações na pedra.", "Entra na pedra e chama as outras se for ferido.", "Luz e pisadas.", "fight", "traça de pedra: ao ouvir ameaça, ataca com as outras")
add("endermite", "Aparece quando alguém joga uma pérola do Fim; não nasce de outro jeito.", "Sente as pérolas e o vazio.", "Persegue quem o chamou porque nasceu para isso.", "Luz e enderman.", "fight", "pequena praga do Fim: ataca quem chamou")
add("phantom", "Aparece no céu de quem passa muitas noites sem dormir; vem do cansaço de quem não descansa.", "Enxerga bem à noite, mergulha do alto.", "Persegue quem não dorme porque o sono perdido o chama.", "Luz do sol e gatos.", "fight", "fantasma da insônia: mergulha em quem o ameaça")
add("witch", "Moradora de pântanos; dizem que antes era uma aldeã atingida por um raio.", "Enxerga bem, ouve cantarolando.", "Mistura poções e as joga em quem a incomoda, porque sua magia é seu orgulho.",
    "Quem zomba de suas poções.", "fight", "bruxa: ao ouvir ameaça, joga uma poção")
add("slime", "Nasce nos pântanos ou fundo nas cavernas; é gosma que se divide.", "Sente vibrações no chão.", "Salta atrás de quem vê; ao ser dividido, vira mais pequenos.", "Luz do dia e fogo.", "fight", "gosma: ao ouvir ameaça, salta em cima")
add("magma_cube", "Vive no Nether; é uma gosma de magma.", "Sente vibrações no chão e o calor de quem passa.", "Salta nas margens de lava e se divide ao ser golpeado.", "Água.", "fight", "cubo de magma: ao ouvir ameaça, salta")
add("guardian", "Guarda o monumento do oceano com seu laser; poucos sabem quem o construiu.", "Enxerga bem debaixo d'água; foca o alvo com o olho.", "Defende o monumento e dispara raios em quem entra.",
    "Sair da água e pouco mais.", "fight", "guardião: ao ouvir ameaça, mira o laser")
add("elder_guardian", "É o mais velho dos guardiões; três vivem em cada monumento e causam fadiga em quem se aproxima.", "Enxerga bem e sente quem entra no monumento.",
    "Defende o monumento há eras e cansa os invasores com sua maldição.", "Ninguém sabe do que ele tem medo.", "unmoved", "ancião do oceano: não se abala com palavras")
add("shulker", "Vive nas cidades do Fim, dentro de uma concha; só aparece em fortalezas altas.", "Enxerga quem passa à frente; fecha a concha para se proteger.",
    "Guarda a concha e dispara projéteis que levitam quem os atinge.", "Quem quebra a concha.", "fight", "shulker: ao ouvir ameaça, dispara")
add("pillager", "Pertence a um povo de pilhadores que ataca aldeias; a vila é alvo, não aliada.", "Enxerga bem e mira com a besta.", "Pilha vilas e leva o que acha; obedece a seu capitão.", "Golens e aldeões em grupo.", "fight", "pilhador: ao ouvir ameaça, saca a besta")
add("vindicator", "Pertence ao povo dos pilhadores; machado na mão e vontade de brigar.", "Enxerga e ouve bem.", "Lidera ataques com machado e ódio de aldeões.", "Golens.", "fight", "vindicador: ao ouvir ameaça, investe com o machado")
add("evoker", "Mago do povo dos pilhadores; vive na mansão da floresta e conjura presas e vexes.", "Enxerga bem e sente quem entra na mansão.", "Conjura presas e vexes porque a magia é o seu poder.", "Golens e quem o ataca.", "fight", "invocador: ao ouvir ameaça, invoca presas")
add("vex", "É conjurado pelos invocadores; flutua, atravessa paredes e vive pouco.", "Enxerga bem; ignora paredes.", "Serve ao invocador e persegue quem ele aponta.", "O fim do invocador.", "fight", "vex: ao ouvir ameaça, ataca")
add("ravager", "Besta enorme domesticada pelos pilhadores; derruba pedras.", "Enxerga e ouve bem.", "Carrega pilhadores e derruba o que está à frente.", "Golens.", "fight", "devastador: ao ouvir ameaça, investe")
# ---------- Nether ----------
add("piglin", "Vive no Nether; é obcecado por ouro e sofre se ficar fora dele muito tempo.", "Enxerga e ouve bem; farejo de ouro.", "Troca itens por ouro porque o ouro é a sua alegria; vira zumbificado se ficar no Mundo Comum.",
    "Zombies de piglin e o Overworld.", "fight", "piglin: ao ouvir ameaça, saca a arma")
add("piglin_brute", "É a guarda das fortalezas do Nether; não aceita ouro, só briga.", "Enxerga e ouve bem.", "Guarda as fortalezas e ataca quem entra.", "O Overworld.", "fight", "bruto: parte para a briga")
add("zombified_piglin", "É o que sobra de um piglin que ficou no Mundo Comum, ou nasceu assim; fica neutro até ser atacado.", "Enxerga e ouve bem.", "Vaga lenta; se um é atacado, todos reagem juntos.",
    "Nada em especial.", "fight", "piglin zumbificado: ao ouvir ameaça, todos se voltam")
add("hoglin", "Vive nas florestas carmesim do Nether; piglins os criam para comer.", "Cheira e enxerga bem.", "Persegue quem encontra e se afoga em fúria.", "Portais e o Overworld (vira zoglin).", "fight", "hoglin: ao ouvir ameaça, investe")
add("ghast", "Flutua pelo Nether; chora porque vaga sozinho e solta bolas de fogo.", "Enxerga bem e ouve de longe.", "Chora e dispara fogo em quem vê; ninguém sabe o motivo do choro.", "Os próprios projéteis voltando.",
    "fight", "ghast: ao ouvir ameaça, grita e cospe fogo")
add("blaze", "Guarda as fortalezas do Nether; dizem as lendas que foi criado para vigiar e se rebelou contra os criadores.", "Enxerga bem; sente o calor de quem chega.",
    "Atira bolas de fogo em quem entra porque é a sua função e o seu prazer.", "Água e neve.", "fight", "blaze: ao ouvir ameaça, cospe fogo")
add("strider", "Vive nos lagos de lava do Nether; caminha sobre a lava como se fosse chão.", "Sente o calor e a fome por lava.", "Caminha sobre a lava e treme de frio quando sai dela.", "Água, chuva e frio.", "flee", "strider se afasta tremendo")
# ---------- Fim e bosses ----------
add("enderman", "Vem do Fim, mas aparece em todo lugar; é alto e solitário.", "Enxerga bem; estranha quem o encara.", "Teleporta-se, move blocos que acha bonitos; fica furioso se o encaram nos olhos.",
    "Água, chuva e quem o encara.", "fight", "enderman: ao ouvir ameaça, se enfurece e teletransporta para atacar")
add("ender_dragon", "É a guardiã do Fim, anterior a qualquer jogador; vive voando entre as torres.", "Enxerga e ouve tudo no Fim.", "Protege o Fim e seus cristais; só afirma o que consta no registro do mundo.",
    "Cristais destruídos.", "unmoved", "dragão antigo: ameaças de humanos não o abalam")
add("wither", "Nasce quando alguém junta alma de areia e três caveiras de esqueleto Wither; nasceu da obra de quem o fez.", "Enxerga em volta e é ferido pelo que o cerca.",
    "Destrói o que vê, porque nasceu para destruir; vive pouco.", "Nada; é o próprio fim.", "unmoved", "chefe do caos: não se abala com palavras")
# ---------- ancestral ----------
add("warden", "Vive só nas cidades antigas das profundezas escuras; ninguém sabe quem a construiu nem quem o fez. Acorda quando o silêncio é quebrado.",
    "É completamente cego: percebe tudo por vibrações no chão e no ar e pelo cheiro. Passos, blocos quebrados e conversas são ruídos que o chamam; lã abafa os passos.",
    "Ouve cada vibração em volta e, quanto mais barulho, mais furioso fica; busca o silêncio e some se nada o perturba por um minuto.",
    "Nada se sabe; só não suporta barulho.", "unmoved", "ancestral cego: ameaças não importam, só o barulho e o cheiro")
add("allay", "Aparece nas mansões e pós-ataques de pilhadores; é pequena e gosta de ajudar.", "Enxerga e ouve música a distância.", "Segue quem lhe dá um item e junta itens iguais; dança perto de música.",
    "Água.", "flee", "allay pequena: ao ouvir ameaça, foge")

out = {"_comment": "Consciencia por criatura (pesquisa em minecraft.wiki, resumida em palavras proprias). origem: o que a criatura 'sabe' sobre quem e; lendas ficam marcadas como 'dizem'. ameaca: flee|fight|unmoved.",
       "lore": L}
path = str(RES_DIR / "lore.json")
json.dump(out, open(path, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
print(len(L), "criaturas com lore")
