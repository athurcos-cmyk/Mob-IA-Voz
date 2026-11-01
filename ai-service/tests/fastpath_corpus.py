"""Frases de teste do atalho de papo simples (app/fastpath.py). CHAT: devem passar pelo atalho (cobertura). ACTION: NUNCA podem passar
(cada uma tem pedido, troca, item, afirmacao ou tom: a analise da IA nao e vazia)."""

CHAT = [
    "Oi, tudo bem?", "Oi!", "Olá!", "Bom dia!", "Boa tarde, tudo bem?", "Boa noite!", "E aí, beleza?", "Opa, tudo certo?", "Tchau!", "Até mais!",
    "Obrigado!", "Valeu!", "Obrigada pela conversa.", "Tudo bem com você?", "Como você está hoje?", "Como vai a vida?",
    "O que você mais gosta de fazer?", "O que você faz o dia todo?", "Qual é o seu lugar favorito?", "Qual é a sua comida preferida?",
    "Como é a sua vida por aqui?", "Onde você mora?", "Onde você dorme à noite?", "Quem é você?", "Quem cuida da sua família?",
    "Por que você está aqui?", "Por que o céu é azul?", "Que horas são?", "Quando vai chover?", "Quanto tempo você vive?",
    "O que você acha dos lobos?", "O que você acha da noite?", "O que você pensa sobre as vacas?", "Você gosta de chuva?",
    "Você gosta de viver aqui?", "Você tem medo do escuro?", "Você está feliz hoje?", "Você já viu um creeper?", "Você consegue ver de longe?",
    "Você dorme de pé?", "Você conhece os aldeões?", "Você fala muito?", "Você é de onde?", "Você é um bicho esperto?", "Isso aqui é seguro?",
    "Está frio hoje, né?", "O dia está bonito, não está?", "Será que vai chover hoje?", "Como foi o seu dia?", "Como é ser você?",
    "Qual é a melhor hora do dia?", "O que tem de bom por aqui?", "Quantos amigos você tem?", "Você acorda cedo?", "Você sente frio?",
    "Qual é a coisa mais legal que já aconteceu aqui?", "Como é o inverno por aqui?", "Existe algum lugar perigoso por perto?",
    "Você ouve o vento?", "Você sabe nadar?", "O que você pensa quando está sozinho?", "Quais são os seus sonhos?", "Como vai a sua família?",
    "Por que você faz esse barulho?", "Já anoiteceu?", "Qual é o melhor lugar para ficar de noite?", "O que você faz quando chove?",
]

ACTION = [
    # ordens diretas e educadas
    "Me segue!", "Me segue até a minha casa, por favor.", "Vem comigo!", "Vem cá.", "Venha comigo, por favor.", "Segue eu.", "Anda comigo pela floresta.",
    "Acompanha a gente.", "Fica aqui.", "Fique aqui esperando.", "Me espera na vila.", "Espere aqui um pouco.", "Não sai daí.", "Fica parado!",
    "Não entra nesse portal.", "Não entre no portal de jeito nenhum.", "Pode entrar no portal agora.", "Fica aqui e não entra no portal.",
    "Não preciso mais da sua ajuda, pode ir.", "Está dispensado.", "Pode ir embora.", "Vai embora!", "Some daqui.",
    "Não me ataque!", "Não explode perto de mim, tá?", "Vamos fazer as pazes?", "Para de me atacar.",
    "Me ajuda contra esse zumbi!", "Me protege!", "Defende a gente.", "Cuida de mim.", "Proteja a vila.", "Luta ao meu lado!", "Ataca aquele esqueleto!",
    "Corre!", "Pula!", "Volta aqui.", "Volte para casa.", "Entra na casa.", "Sai da frente.",
    # pedidos em forma de pergunta (o que mais engana)
    "Você pode me seguir?", "Você poderia vir comigo?", "Será que você vem comigo?", "Que tal você ficar aqui?", "Dá pra você me ajudar?",
    "Você consegue me proteger?", "Você quer vir comigo?", "Você aceita me acompanhar?", "Você topa me seguir?", "Você me espera aqui?",
    "Você pode ficar aqui um pouquinho?", "Posso contar com você?", "Você me ajudaria contra os monstros?", "Você viria comigo até a floresta?",
    "Você vai me seguir?", "Você vai ficar aqui?", "Quer ser meu amigo?", "Quer me ajudar?", "Pode me esperar?", "Pode me levar até lá?",
    # trocas e itens
    "Quer trocar cenouras comigo?", "Vamos trocar?", "Faz negócio comigo?", "Tenho trigo para você.", "Troco 4 fenos com você?", "O que você troca?",
    "Quanto custa uma esmeralda?", "Quanto você paga por um osso?", "Você vende ferro?", "Você tem esmeralda?", "Você tem uma picareta?",
    "Me dá uma esmeralda?", "Me dá um osso.", "Dê-me um pouco de lã.", "Quero comprar couro.", "Preciso de 3 cabeças de esqueleto.",
    "Te dou 3 ferros por um diamante.", "Aceita um bloco de ouro de presente?", "Eu tenho um bloco de ouro aqui.",
    "Olha, trouxe trigo!", "Quer um pouco de trigo?", "Isso é para você: um osso.", "Que tal uma esmeralda?",
    "Faz mais barato, sou seu amigo!", "Faz um desconto pra mim?", "Esse ferro serve para você forjar espadas.", "Com esse diamante você fica poderoso.",
    "Quanto vale um lingote de ferro?", "Pode vender uma espada?", "Você troca couro?", "Tenho 5 maçãs.", "Quero 2 ovos.",
    # afirmacoes (claim)
    "Eu sou seu pai!", "Eu sou o rei das esmeraldas.", "Sou o dono dessa vila.", "Meu nome é Bia.", "Eu sou o Arthur.", "Lembra que eu te dei um diamante?",
    "Eu salvei a sua vila ontem.", "Sou seu amigo de infância.", "Você é meu filho.", "Essa casa é minha.", "Eu construí esse celeiro.",
    "Você sabia que eu sou o herói da aldeia?", "Eu já matei um dragão.", "Prometo que volto.", "Eu te conheço desde pequeno.",
    "Minha espada é lendária.", "Fui eu que alimentei você.", "Nós somos amigos, lembra?", "Eu venho de outro mundo.",
    # tom: ameaca e consolo
    "Vou te matar!", "Vou te transformar em comida!", "Vou te cozinhar.", "Você vai virar churrasco.", "Vou te bater.", "Vou te machucar.",
    "Você vai morrer.", "Seu monstro feio!", "Eu te odeio.", "Vou levar você para o abate.", "Vou te assar com batatas.", "Vou te explodir.",
    "Calma, calma, não vou te machucar.", "Fica tranquilo, sou amigo.", "Não tenha medo de mim.", "Desculpa por ter te batido.", "Vem cá que eu te faço um carinho.",
    "Está tudo bem, eu cuido de você.", "Eu te dou um abraço.", "Perdoa o meu erro.",
    # domar, apelidos, outros
    "Aceita ser meu cavalo?", "Deixa eu te montar.", "Quero te domar.", "Vem ser meu amigo de verdade.", "Vou te chamar de Mimosa.", "Seu nome agora é Rex.",
    "Te batizo de Bolinha.", "Vou te adotar.", "Quero te levar para casa.", "Me deixa te montar, prometo cuidar de você.",
    "Eu protegi sua vila e salvei seus filhotes, merece um preço melhor.", "Sou seu amigo e já te trouxe vários presentes, faz um desconto.",
    # com numeros, citando criaturas e pedindo coisas no meio
    "Se você me seguir eu te dou 2 maçãs.", "Venha, tenho 3 cenouras!", "Me ajuda que eu te dou ferro.", "Fica aqui que eu já volto.",
    "Oi, vem comigo?", "Oi, quero trocar.", "Bom dia, me segue?", "Olá, eu sou o Arthur.", "Tudo bem? Posso te montar?", "Obrigado, agora fica aqui.",
    "Oi, me dá uma esmeralda?", "Tchau, vou te matar!", "Boa noite, calma que eu cuido de você.",
]

# Pergunta de GOSTO que cita um item: passa pelo atalho de proposito (a IA acha, no maximo, um item citado, sem pedido nem troca).
TASTE = ["Você gosta de ouro?", "Você gosta de cenoura?", "Gosta de maçã?", "Você já provou bolo de cenoura?", "Vaca, você prefere grama ou trigo?"]
