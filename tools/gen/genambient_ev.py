"""Reacoes ao MUNDO (familias ev_*, sem IA) e assuntos novos de ambiente (bioma, lua, o que o jogador faz). Usado por genambient.py.

Familias ev_* so acontecem quando o servidor marca o acontecimento (tag ev:...): ver WorldReactions.java.
solo=True: reacao de UMA criatura (so a lista a; b e c nao existem). Sem solo: uma criatura abre (a), a outra responde (b) e a primeira fecha (c);
se so houver uma criatura por perto, so a abertura (a) e dita, entao TODA abertura tem de funcionar sozinha.
Espaco {coisa}/{Coisa}: o que a reacao pede (o inimigo que se aproxima, o presente, o que foi devolvido, ...), sem artigo.
Regras: nada de adjetivo com genero para a propria criatura (nem "obrigado": use "valeu", "agradeco"); nunca "{jogador}" como vocativo
("Bom dia, o humano" fica errado): use-o como sujeito ou depois de preposicao.
"""


def add(fam):
    # ------------------------------------------------------------------ creeper explodiu perto
    fam("ev_boom_vila", ["villager", "villager"], ["ev:boom"],
        ["Que explosão! Foi um creeper?", "Meu Deus, que estrondo!", "Isso foi um creeper, tenho certeza!", "Ouviu isso? O chão inteiro tremeu!",
         "Alguém acabou de encontrar um creeper...", "Lá se foi mais um pedaço do mundo.", "Que barulho horrível!", "Socorro! Explodiu algo aqui perto!",
         "Esse cheiro de pólvora não deixa dúvida: foi um creeper.", "Por pouco não foi dentro da vila!"],
        ["Ouvi, quase caí de susto.", "Foi perto demais para o meu gosto.", "Esses bichos verdes vão acabar com a vila.", "Vamos olhar com cuidado, pode ter mais.",
         "Tomara que ninguém tenha se machucado.", "Meu coração ainda está batendo forte."],
        ["Melhor ficar longe de buraco novo.", "Vou conferir se a minha casa está de pé.", "Respira fundo, já passou.", "Com creeper não se brinca."], weight=3)
    fam("ev_boom_bicho", ["animals", "animals"], ["ev:boom"],
        ["Que susto! O que foi isso?", "Aquele estrondo me deu um arrepio!", "Fugir! Explodiu alguma coisa!", "Meu coração disparou com esse barulho.",
         "Eu pulei três vezes de susto!", "Que barulhão foi esse?", "O chão tremeu embaixo de mim!", "Aquilo foi um trovão? Não, foi pior."],
        ["Foi lá perto, senti o chão tremer.", "Cheiro de pólvora no ar.", "Fica junto de mim, vai que tem mais.", "Eu não saio daqui por nada.",
         "Será que o humano viu?"],
        ["Vamos ficar longe daquele lado.", "Melhor esperar tudo se acalmar.", "Passou o susto, passou."], weight=3)
    fam("ev_boom_golem", ["golem", "golem"], ["ev:boom"],
        ["Explosão! Atrás de mim!", "Creeper. Eu cuido disso.", "Pode haver mais. Cuidado!", "Explodiu perto da vila! Fiquem comigo.",
         "Foi um creeper. Eu fico de guarda."], solo=True, weight=4, b=[])
    # ------------------------------------------------------------------ raio caiu perto
    fam("ev_raio_vila", ["villager", "villager"], ["ev:thunder"],
        ["Esse raio caiu pertinho!", "Que clarão! Quase perdi a vista.", "Ouviu o trovão? Tremeu a vila toda.", "Raio atrás de raio, hoje o céu está bravo.",
         "Foi ali perto que caiu, eu vi!", "Estalou tão forte que meus ouvidos zumbem."],
        ["Fica longe de árvore e de metal.", "Dizem que raio não cai duas vezes no mesmo lugar.", "Meu cabelo ficou arrepiado!", "Vamos para dentro, é mais seguro.",
         "Só queria que passasse logo."],
        ["Respira, já foi.", "Se cair outro, eu me escondo.", "Pelo menos limpou o ar."], weight=3)
    fam("ev_raio_bicho", ["animals", "animals"], ["ev:thunder"],
        ["Que clarão! Fiquei sem enxergar por um instante.", "O céu desabou em cima de mim!", "Esse trovão me fez pular!", "Caiu um raio ali, eu vi tudo!",
         "Quero me esconder em algum lugar."],
        ["Fica pertinho de mim.", "Cheiro de queimado no ar.", "Eu não gosto de tempestade.", "Será que cai de novo?"],
        ["Vamos ficar juntos até passar.", "Passou, mas o susto ficou."], weight=3)
    fam("ev_raio_golem", ["golem", "golem"], ["ev:thunder"],
        ["Raio registrado. A vila está bem.", "Um raio e tanto. Ferro aguenta, mas não vou ficar debaixo de árvore.", "Trovão forte. Fiquem juntos.",
         "O ferro atrai raio, dizem. Vou ficar longe dos topos."], solo=True, weight=4, b=[])

    # ------------------------------------------------------------------ o jogador acordou (manha)
    fam("ev_acordou_vila", ["villager", "villager"], ["ev:wake"],
        ["{Jogador} acordou! Bom dia!", "Olha só quem levantou. Bom dia para {jogador}!", "O sol já está alto, e {jogador} acabou de acordar.",
         "Bom dia! A cama foi gentil com {jogador}?", "Já é de manhã, e a vila está em pé desde cedo."],
        ["Espero que os pesadelos tenham ficado lá.", "Um dia novo começa, quem sabe o que vem.", "Eu já tomei meu café, e vocês?", "Dormir faz bem para qualquer um."],
        ["Vai ser um bom dia.", "Cuidem-se por aí."], weight=3)
    fam("ev_acordou_bicho", ["animals", "animals"], ["ev:wake"],
        ["O humano acordou, finalmente.", "Bom dia! O sol já subiu.", "Dia novo, capim novo.", "O humano dormiu até agora?"],
        ["Pelo menos descansou.", "Eu acordo com o sol, sem despertador nenhum.", "Será que vem ver a gente?"],
        ["Bom dia para todo mundo.", "Vamos ver o que o dia traz."], weight=3)

    # ------------------------------------------------------------------ o jogador voltou de uma morte (so quem o conhece)
    fam("ev_revive_vila", ["villager", "villager"], ["ev:revive"],
        ["{Jogador} voltou! Achei que tinha acontecido o pior.", "Olha só, {jogador} está de pé de novo!", "Eu não esperava ver {jogador} tão cedo.",
         "Que bom ver {jogador} de volta."],
        ["Deve ter sido uma queda feia.", "Esse mundo não perdoa ninguém.", "Fica mais atento da próxima vez.", "O importante é que voltou."],
        ["Bem-vindo de volta.", "Vamos fingir que nada aconteceu."], weight=3)
    fam("ev_revive_bicho", ["animals", "animals"], ["ev:revive"],
        ["O humano voltou! Achei que tinha sumido.", "Olha ele aí de novo, de pé.", "Que bom, o humano está de volta."],
        ["Eu sabia que ele não ia sumir.", "Parecia mal da última vez.", "Quem é que cai tanto assim?"],
        ["Bom ter ele por aqui.", "Que não se repita."], weight=3)

    # ------------------------------------------------------------------ o jogador matou um monstro perto da vila
    fam("ev_obrigado_vila", ["villager", "villager"], ["ev:thanks"],
        ["Valeu por nos proteger!", "Mais um perigo a menos, graças a {jogador}.", "Salvou a vila de novo!", "Eu vi tudo, que coragem!",
         "Sem {jogador}, esse monstro teria entrado na vila."],
        ["A vila inteira deve isso a ele.", "Nem sei como agradecer.", "Dorme-se melhor com alguém assim por perto.", "Eu tremi só de olhar."],
        ["Bom que alguém corajoso apareceu.", "Vamos preparar um agradecimento."], weight=3)
    fam("ev_obrigado_golem", ["golem", "golem"], ["ev:thanks"],
        ["Ameaça neutralizada. Respeito.", "Bom trabalho. A vila agradece.", "Você luta bem. Fico menos sozinho nessa guarda.", "Um a menos para eu cuidar. Valeu."],
        solo=True, weight=4, b=[])

    # ------------------------------------------------------------------ saqueadores perto da vila
    fam("ev_saque_vila", ["villager", "villager"], ["ev:raiders"],
        ["Saqueadores! Corram para as casas!", "São os saqueadores, eu reconheço o capacete!", "Perigo! Tem gente armada ali!", "Alguém chame o golem!",
         "Ai, não, saqueadores de novo!"],
        ["Escondam as esmeraldas!", "Tranquem a porta, já!", "Onde está o golem quando precisa?", "Eu fico atrás de você, combinado?", "Que a vila aguente mais uma vez."],
        ["Todo mundo para dentro!", "Que ninguém saia agora."], weight=3)
    fam("ev_saque_golem", ["golem", "golem"], ["ev:raiders"],
        ["Saqueadores à vista. Todos atrás de mim.", "Hoje ninguém passa por mim.", "Eu seguro a linha, ninguém corre perigo.", "Alerta: invasores. Eu cuido deles."],
        solo=True, weight=4, b=[])

    # ------------------------------------------------------------------ o jogador caiu de muito alto
    fam("ev_queda_vila", ["villager", "villager"], ["ev:fall"],
        ["Ui, essa queda deve ter doído!", "Caiu feio, hein? Está bem?", "{Jogador} despencou lá de cima!", "Que tombo! Ouvi o barulho daqui."],
        ["Parece que sobreviveu.", "Eu teria chorado.", "Não pula de novo, por favor.", "Alguém dê um pouco de comida a ele."],
        ["Se cuide.", "Chão é duro, melhor lembrar."], weight=3)
    fam("ev_queda_bicho", ["animals", "animals"], ["ev:fall"],
        ["Ui! O humano despencou de lá!", "Que tombo feio!", "Nossa, o humano caiu do alto!"], ["Aposto que doeu.", "Humanos não sabem cair.", "Pelo menos ele levantou."],
        ["Aqui o chão é firme, ninguém precisa voar.", "Que tome cuidado com os ossos.", "Melhor ficar com os pés no chão."], weight=3)
    # ------------------------------------------------------------------ o "Ai!" de quem apanhou do jogador (solo)
    fam("ev_ai_vila", ["villager", "villager"], ["ev:hit"],
        ["Ai! Por que fez isso?", "Ei! Isso doeu!", "Está louco? Para com isso!", "Não precisa bater!", "Ai, meu braço! Foi de propósito?",
         "Ei! Eu estava só de passagem!", "Isso não se faz, ouviu?", "Ui! Pode parar?", "Ai! Eu não fiz nada!", "Isso foi sem querer, ou foi de propósito?"],
        solo=True, weight=4, b=[])
    fam("ev_ai_golem", ["golem", "golem"], ["ev:hit"],
        ["Isso não vai me machucar, mas não faça.", "Tente de novo e vai ver o que acontece.", "Aviso uma vez: pare.", "Você acabou de arranjar um problema.",
         "Bateu em mim? Cuidado, humano."], solo=True, weight=4, b=[])
    fam("ev_ai_bicho", ["animals", "animals"], ["ev:hit"],
        ["Ai! Isso doeu!", "Ei! Que susto!", "Para! Eu não fiz nada!", "Ui! Por que bateu em mim?", "Ai ai ai! Isso dói!", "Assim eu fujo, hein!",
         "Eu só estava aqui em sossego!"], solo=True, b=[])
    fam("ev_ai_vaca", ["cow", "cow"], ["ev:hit"],
        ["Muuu! Isso doeu!", "Muu! Por que isso?", "Ai, muu! Eu estava pastando!", "Muuuu! Para com isso!", "Ui! Assim não dá leite!"], solo=True, weight=4, b=[])
    fam("ev_ai_porco", ["pig", "pig"], ["ev:hit"],
        ["Oinc! Ai!", "Oinc oinc! Isso doeu!", "Ei! Eu não sou bacon!", "Oinc! Para com isso!", "Ui! Assim eu fujo!"], solo=True, weight=4, b=[])
    fam("ev_ai_ovelha", ["sheep", "sheep"], ["ev:hit"],
        ["Méé! Ai!", "Méé! Isso doeu na lã!", "Beee! Para!", "Ei! Eu estava só pastando!", "Méé! Que falta de educação!"], solo=True, weight=4, b=[])
    fam("ev_ai_galinha", ["chicken", "chicken"], ["ev:hit"],
        ["Có có có! Ai!", "Cocoricó! Socorro!", "Có! Isso dói!", "Ai! Minhas penas!", "Có có! Para com isso!"], solo=True, weight=4, b=[])
    fam("ev_ai_lobo", ["wolf", "wolf"], ["ev:hit"],
        ["Grrr! Isso doeu!", "Au! Por que fez isso?", "Cuidado, eu mordo!", "Au! Eu confiava em você!", "Grrr... não faça de novo."], solo=True, weight=4, b=[])
    fam("ev_ai_gato", ["cat", "cat"], ["ev:hit"],
        ["Miau! Ai!", "Miau! Isso doeu!", "Psss! Não faça isso!", "Mrrr... isso foi desnecessário.", "Miau! Eu arranho, hein!"], solo=True, weight=4, b=[])
    fam("ev_ai_cavalo", ["horse", "horse"], ["ev:hit"],
        ["Iiirrr! Ai!", "Ei! Quase te dei um coice!", "Isso doeu, sabia?", "Calma aí, não precisa bater!", "Iirrr! Para com isso!"], solo=True, weight=4, b=[])

    # ------------------------------------------------------------------ presente: gostou / indiferente / nao gostou (solo)
    fam("ev_presente_gosta_vila", ["villager", "villager"], ["ev:gift_like"],
        ["Que alegria! {Coisa} é do que eu mais gosto!", "{Coisa}! Valeu, de verdade.", "Nossa, {coisa}! Que gesto bonito.", "Gostei muito, agradeço de coração!",
         "Você acertou em cheio: {coisa}!", "Receber {coisa} alegrou o meu dia.", "Presente assim eu guardo com carinho."], solo=True, weight=3, b=[])
    fam("ev_presente_gosta_bicho", ["animals", "animals"], ["ev:gift_like"],
        ["Hmm, {coisa}! Que delícia!", "Oba, {coisa}! É o meu favorito!", "Nhac, nhac... que bom!", "Valeu! Eu estava com fome disso.", "Mais, mais, quero mais!",
         "Isso é tudo o que eu queria hoje!"], solo=True, weight=3, b=[])
    fam("ev_presente_gosta_golem", ["golem", "golem"], ["ev:gift_like"],
        ["Hm! {Coisa}! Eu gosto disso.", "Para mim? Gostei, humano.", "{Coisa}... Fiquei feliz.", "Gostei do presente. Vou proteger você melhor."], solo=True, weight=3, b=[])
    fam("ev_presente_indiferente_vila", ["villager", "villager"], ["ev:gift_meh"],
        ["Hmm, {coisa}. Não é bem o que eu uso, mas agradeço.", "{Coisa}? Pensei que fosse outra coisa, mas tudo bem.", "Aceito, mas não sei o que fazer com {coisa}.",
         "Você lembrou de mim, e isso já é bom.", "Não é o que eu pediria, mas a intenção conta."], solo=True, weight=3, b=[])
    fam("ev_presente_indiferente_bicho", ["animals", "animals"], ["ev:gift_meh"],
        ["Hmm, {coisa}. Cheirei, mas não é o meu favorito.", "{Coisa}? Não é o que eu como, mas agradeço a intenção.", "Isso eu não como, mas gostei do carinho.",
         "Hmm, {coisa}... Eu esperava algo mais gostoso.", "Não é ruim, {coisa}. Só não é o meu tipo de comida."], solo=True, weight=3, b=[])
    fam("ev_presente_indiferente_golem", ["golem", "golem"], ["ev:gift_meh"],
        ["{Coisa}? Não preciso disso, mas agradeço.", "Hm. Não sei para que serve, mas aceito.", "{Coisa}. Tudo bem, fico com ele."],
        solo=True, weight=3, b=[])
    fam("ev_presente_odeia_vila", ["villager", "villager"], ["ev:gift_dislike"],
        ["{Coisa}? Que ideia é essa! Eu detesto isso.", "Eca! Leve {coisa} de volta, por favor.", "Prefiro que fique com {coisa}.",
         "Isso eu não aceito, me dá nojo.", "Está brincando? {Coisa} não presta."], solo=True, weight=3, b=[])
    fam("ev_presente_odeia_bicho", ["animals", "animals"], ["ev:gift_dislike"],
        ["Eca! {Coisa}? Eu não como isso!", "Blergh! Tira {coisa} daqui, o cheiro é horrível!", "Nem de graça eu aceito {coisa}.",
         "Que mal eu te fiz para ganhar {coisa}?", "Leva {coisa} embora, eu não quero nem sentir o cheiro!"], solo=True, weight=3, b=[])
    fam("ev_presente_odeia_golem", ["golem", "golem"], ["ev:gift_dislike"],
        ["{Coisa}? Não gosto. Leve embora.", "Isso me incomoda. Fique com {coisa}.", "Não quero {coisa}, humano."],
        solo=True, weight=3, b=[])
    # ------------------------------------------------------------------ aviso de perigo (um aliado do jogador fala; {coisa} = o inimigo) (solo)
    fam("ev_perigo", ["calm", "calm"], ["ev:danger"],
        ["Cuidado! Perigo à vista: {coisa}!", "Atenção! {Coisa} se aproximando!", "Olha lá! {Coisa} vem vindo!", "Fica atento! Tem {coisa} por perto!",
         "Perigo! {Coisa} está muito perto!", "Psiu! {Coisa} se aproximando, muito cuidado!", "Quieto aí... {coisa} bem na nossa frente!"], solo=True, b=[])
    fam("ev_perigo_pet", ["pet", "pet"], ["ev:danger"],
        ["Grrr! {Coisa} por perto, fica atrás de mim!", "Cuidado, {coisa} vem aí! Eu te protejo!", "Estou sentindo {coisa}. Fica comigo!",
         "Atenção! {Coisa} se aproxima, eu fico de guarda!"], solo=True, weight=3, b=[])
    fam("ev_perigo_monstro", ["monsters", "monsters"], ["ev:danger"],
        ["Atenção! {Coisa} se aproxima, eu cubro você!", "Fica atrás de mim, {coisa} vem aí!", "Grrr! {Coisa} por perto, eu cuido disso!",
         "Um intruso: {coisa}. Ninguém toca em você."], solo=True, weight=3, b=[])
    fam("ev_perigo_golem", ["golem", "golem"], ["ev:danger"],
        ["Hostil detectado: {coisa}. Fique atrás de mim.", "{Coisa} se aproxima. Eu cuido disso.", "Alerta: {coisa} no raio de visão. Fique perto.",
         "Ameaça: {coisa}. Deixe comigo."], solo=True, weight=3, b=[])

    # ------------------------------------------------------------------ reencontro com quem a criatura conhece (por fato verificado) (solo)
    fam("ev_volta", ["calm", "calm"], ["ev:greet"],
        ["Olha só, {jogador} por aqui.", "Nos vemos de novo!", "Ainda lembro de {jogador}.", "{Jogador} voltou! Por onde andou?", "Bom ver {jogador} outra vez.",
         "Você de novo por perto, que bom.", "Faz tempo que não vejo {jogador} por aqui."], solo=True, b=[])
    fam("ev_volta_presente", ["calm", "calm"], ["ev:greet", "f0:GIFT"],
        ["Olha quem voltou! Ainda lembro do presente.", "{Jogador} por aqui de novo! Valeu pelo presente de antes.", "Que bom ver {jogador}. Não esqueci a gentileza.",
         "O dono daquele presente! Como vai?", "Aquele presente ainda me deixa contente, viu?"], solo=True, weight=4, b=[])
    fam("ev_volta_troca", ["calm", "calm"], ["ev:greet", "f0:TRADE"],
        ["Ora, {jogador}! Nossa última troca foi boa.", "Volte sempre, negociar com você é fácil.", "Quer fazer outro negócio, como da outra vez?",
         "Lembro bem da nossa troca. Foi justa."], solo=True, weight=4, b=[])
    fam("ev_volta_honesto", ["calm", "calm"], ["ev:greet", "f0:HONEST_RETURN"],
        ["{Jogador}! O mais honesto que eu já conheci.", "Nunca esqueci que você devolveu o que não era seu.", "Gente honesta assim é rara. Bem-vindo de volta!",
         "Sua honestidade ficou na minha memória."], solo=True, weight=4, b=[])
    fam("ev_volta_bateu", ["calm", "calm"], ["ev:greet", "f0:DAMAGED_BY_PLAYER"],
        ["Ah, {jogador}. Ainda lembro daquela pancada.", "Olha quem voltou... Vê se fica de mãos quietas.", "Mantenha distância. Não esqueci o que fez.",
         "Você de novo? Meu corpo ainda lembra.", "Vai bater de novo? Prefiro que não."], solo=True, weight=4, b=[])
    fam("ev_volta_traiu", ["calm", "calm"], ["ev:greet", "f0:PACT_BROKEN"],
        ["Você de novo. Não esqueci o acordo que quebrou.", "Voltou para quebrar mais algum acordo?", "Prometeu e não cumpriu, e eu lembro.",
         "Cuidado comigo: palavra quebrada não se esquece."], solo=True, weight=4, b=[])
    fam("ev_volta_ameacou", ["calm", "calm"], ["ev:greet", "f0:THREAT_SPOKEN"],
        ["Já se acalmou? Ainda lembro das suas ameaças.", "Lembro das palavras duras que ouvi de {jogador}.", "Tomara que não venha ameaçar de novo.",
         "Suas ameaças ainda ecoam na minha cabeça."], solo=True, weight=4, b=[])
    fam("ev_volta_acalmou", ["calm", "calm"], ["ev:greet", "f0:COMFORTED"],
        ["{Jogador}! Foi quem me acalmou naquele dia.", "Lembro de como me ajudou. Valeu.", "Gente que acalma os outros é rara, e você é uma delas.",
         "Fico em paz só de ver {jogador}."], solo=True, weight=4, b=[])
    # ---- o golem: guarda da vila; cumprimenta pedindo ajuda para proteger a vila e lembra do que o jogador fez (fixo, sem IA)
    fam("ev_volta_golem", ["golem", "golem"], ["ev:greet"],
        ["Bem-vindo de volta. A vila precisa de gente como você.", "Você voltou. Bom, a guarda precisa de reforço.", "Ei, humano! Me ajuda a proteger a vila hoje?",
         "Eu fico de olho nos monstros; você fica de olho em mim, combinado?", "Já te conheço. Pode andar à vontade, a vila é sua amiga.",
         "Humano! Se vir um monstro perto, me avise que eu cuido dele.", "Que bom te ver. Juntos, protegemos a vila melhor.",
         "Voltou! Se quiser ajudar a vigiar a vila, estou à disposição."], solo=True, weight=8, b=[])
    fam("ev_volta_golem_presente", ["golem", "golem"], ["ev:greet", "f0:GIFT"],
        ["O humano do presente! Eu não esqueci. Conte comigo na guarda.", "Você me deu um presente. Eu protejo quem é gentil comigo.",
         "Bom te ver de novo. Guardo a vila, e você, por causa daquele presente.", "Aquele presente ainda me deixa feliz. A vila e você estão seguros comigo."],
        solo=True, weight=12, b=[])
    fam("ev_volta_golem_bateu", ["golem", "golem"], ["ev:greet", "f0:DAMAGED_BY_PLAYER"],
        ["Você bateu em mim. Eu lembro. Fique quieto e a gente se entende.", "Você de novo. Se machucar alguém da vila, eu paro você.",
         "Não esqueci a pancada. Mantenha as mãos quietas na minha vila.", "Estou de olho em você, humano. Quem bate num guarda pensa duas vezes."],
        solo=True, weight=12, b=[])
    fam("ev_volta_golem_honesto", ["golem", "golem"], ["ev:greet", "f0:HONEST_RETURN"],
        ["Você é honesto. Gente assim ajuda a proteger a vila.", "Lembro da sua honestidade. Pode contar comigo.", "Honesto de volta à vila. Bom."],
        solo=True, weight=12, b=[])
    fam("ev_volta_golem_acalmou", ["golem", "golem"], ["ev:greet", "f0:COMFORTED"],
        ["Você me acalmou uma vez. Eu guardo isso com respeito.", "Foi você que me trouxe calma. A vila e eu agradecemos."], solo=True, weight=12, b=[])
    fam("ev_volta_golem_traiu", ["golem", "golem"], ["ev:greet", "f0:PACT_BROKEN"],
        ["Você quebrou a palavra. A vila ainda vigia você.", "Palavra quebrada eu não esqueço. Cuidado por aqui."], solo=True, weight=12, b=[])
    fam("ev_volta_golem_ameacou", ["golem", "golem"], ["ev:greet", "f0:THREAT_SPOKEN"],
        ["Você me ameaçou com palavras. Cuidado com o que diz perto da vila.", "Ainda lembro das suas ameaças. Aqui, é melhor ser gentil."],
        solo=True, weight=12, b=[])
    fam("ev_volta_dono", ["pet", "pet"], ["ev:greet", "f0:TAMED"],
        ["Meu dono voltou!", "Que bom que está aqui, fiquei esperando.", "Senti sua falta! Vamos brincar?", "Eu sabia que ia voltar para mim.",
         "Agora o dia ficou melhor!"], solo=True, weight=5, b=[])

    # ------------------------------------------------------------------ galinha que teve o ovo pego / devolucao honesta (solo)
    fam("ev_ovo", ["chicken", "chicken"], ["ev:egg"],
        ["Có có! Você pegou o meu ovo!", "Ei, esse ovo era meu!", "Có! Devolve, eu acabei de botar!", "Cocoricó! Roubo de ovo!", "Meu ovo! Meu ovo sumiu!",
         "Có, có, có! Isso é abuso!", "Eu choco, você pega? Assim não vale!"], solo=True, b=[])
    fam("ev_devolveu", ["villager", "villager"], ["ev:honest"],
        ["Você devolveu {coisa}? Isso é raro!", "Devolveu {coisa} sem eu pedir! Que honestidade.", "Gente honesta assim eu não esqueço.",
         "Eu nem ia notar, mas você devolveu {coisa}. Valeu!", "Muita gente teria ficado com {coisa}. Você não."], solo=True, b=[])
    fam("ev_devolveu_bicho", ["animals", "animals"], ["ev:honest"],
        ["Você devolveu {coisa}! Agora eu gosto mais de você.", "Devolveu {coisa} sem eu pedir. Muito bem!", "Que bom! Você é de confiança, humano.",
         "Eu nem esperava. Você devolveu {coisa} de volta para mim!"], solo=True, b=[])
    fam("ev_devolveu_golem", ["golem", "golem"], ["ev:honest"],
        ["Você devolveu {coisa}. Isso é raro.", "Honestidade. Gosto disso.", "Devolveu sem eu pedir. Eu vi.", "A vila precisa de gente assim."], solo=True, weight=3, b=[])
    # ================================================================== NOVOS ASSUNTOS DE AMBIENTE
    # ---- bioma
    fam("bioma_neve", ["calm", "calm"], ["snow"],
        ["Esse frio entra até nos ossos.", "A neve não para de cair, não é?", "Branco para todo lado, só vejo neve.", "Quem inventou tanto gelo?",
         "Pelo menos a neve deixa tudo quietinho."],
        ["Eu preciso me mexer para esquentar.", "Cuidado para não escorregar no gelo.", "Fico aqui me encolhendo até passar.", "Bonito de ver, difícil de aguentar."],
        ["Quem tem abrigo quente é feliz.", "Vamos nos juntar para esquentar.", "O inverno aqui é assim mesmo."], weight=2)
    fam("bioma_deserto", ["calm", "calm"], ["desert"],
        ["Esse sol seca até o pensamento.", "Tanta areia e nenhuma sombra.", "Está um calor de derreter.", "Aqui até o vento queima.", "Água, preciso de água."],
        ["Não vejo nem um poço por aqui.", "A areia entra em tudo.", "Quem escolheu viver num lugar assim?", "Por favor, uma sombra."],
        ["Vamos achar uma sombra.", "Quando escurece fica mais fresco.", "Aguenta firme."], weight=2)
    fam("bioma_selva", ["calm", "calm"], ["jungle"],
        ["O ar aqui é pesado de tão úmido.", "Cada folha aqui é do tamanho de uma porta.", "Essa selva não acaba nunca.", "Tanto bicho barulhento por aqui!"],
        ["Cuidado com o que se esconde nos cipós.", "Eu sempre me perco por aqui.", "Os papagaios não calam a boca.", "Tem cheiro de chuva em todo canto."],
        ["Fica perto para não se perder.", "A selva cuida de quem a respeita."], weight=2)
    fam("bioma_pantano", ["calm", "calm"], ["swamp"],
        ["Esse pântano cheira a lodo.", "Tanta lama que o pé afunda.", "Os sapos aqui não dão sossego.", "Neblina e lodo, que lugar!",
         "Cuidado onde pisa, a água esconde buracos."],
        ["Eu detesto lama.", "À noite aparece cada coisa estranha.", "As bruxas gostam daqui, dizem.", "Mas o coaxar até acalma."],
        ["Vamos para um terreno firme.", "Pântano é pântano, né?"], weight=2)
    fam("bioma_floresta", ["calm", "calm"], ["forest"],
        ["Gosto do cheiro de madeira por aqui.", "Essas árvores são antigas, dá para sentir.", "A floresta está tão calma hoje.", "Quanta sombra boa para descansar.",
         "Ouviu os passarinhos?"],
        ["A sombra daqui é a melhor.", "Cuidado para não se perder entre as árvores.", "Cogumelo nasce em todo canto.", "É um lugar bom de se viver."],
        ["Vamos aproveitar a paz.", "Floresta cuida de quem fica."], weight=2)
    fam("bioma_montanha", ["calm", "calm"], ["mountain"],
        ["O vento aqui no alto é de arrancar.", "Esse ar é fino, até cansa de respirar.", "Lá de cima se vê o mundo inteiro.", "Esses penhascos dão tontura.",
         "As montanhas parecem tocar o céu."],
        ["Cuidado para não escorregar.", "Aqui o frio vem sem aviso.", "A vista compensa o cansaço.", "Cabras gostam disso; eu, nem tanto."],
        ["Vamos andar devagar.", "Cada passo aqui conta."], weight=2)
    fam("bioma_praia", ["calm", "calm"], ["beach"],
        ["Escuta o barulho das ondas.", "A areia está quentinha hoje.", "Que cheiro bom de maré!", "O mar nunca fica parado, né?", "Dá vontade de ficar olhando o horizonte."],
        ["Cuidado com as ondas, elas puxam.", "Já achou alguma concha?", "O vento daqui é salgado.", "Pena que não sei nadar bem."],
        ["Eu ficaria aqui o dia todo.", "O mar acalma qualquer um."], weight=2)
    fam("bioma_savana", ["calm", "calm"], ["savanna"],
        ["Que relva alta e dourada!", "O sol é forte, mas a sombra das acácias salva.", "Aqui a terra parece não ter fim.", "Faz tempo que não chove, a terra está seca."],
        ["É mesmo, dá gosto de olhar.", "Pelo menos o vento refresca.", "Eu só queria um pouco de chuva.", "Aqui o céu parece enorme."],
        ["Logo a chuva vem.", "Vamos aproveitar a sombra.", "Fim de tarde aqui é lindo."], weight=2)
    # ---- lua, altura, profundidade
    fam("lua_cheia", ["calm", "calm"], ["fullmoon"],
        ["Que lua cheia linda hoje!", "A lua está enorme, dá para ver tudo.", "Noite clara, lua cheia, nada a temer... talvez.", "Lua cheia deixa a noite menos escura."],
        ["Dizem que ela mexe com o humor de muita gente.", "É bonito, mas os monstros também gostam.", "Dá para enxergar até longe.", "Hoje eu quase não sinto medo."],
        ["Vamos aproveitar a claridade.", "Só não vamos longe de casa."], weight=2)
    fam("lua_nova", ["calm", "calm"], ["newmoon"],
        ["A lua sumiu, hoje está escuro demais.", "Noite sem lua, não enxergo um palmo.", "Nem a lua apareceu hoje."],
        ["Melhor ficar perto de tocha.", "Esse breu me dá arrepios.", "Qualquer barulho me assusta."],
        ["Fiquem perto da luz.", "Amanhã a lua volta."], weight=2)
    fam("lua_cheia_monstros", ["monsters", "monsters"], ["fullmoon"],
        ["A lua cheia chama a gente para a caça.", "Noite perfeita para uma boa caçada.", "Com essa lua, ninguém escapa da nossa vista."],
        ["Hoje vamos longe.", "Eu enxergo tudo com essa claridade.", "Os humanos que se cuidem."], ["Vamos nos espalhar.", "Que a lua nos guie."], weight=3)
    fam("fundo_monstros", ["monsters", "monsters"], ["deep"],
        ["Aqui embaixo o escuro é todo nosso.", "Quanto mais fundo, mais quieto.", "Nenhum sol chega até aqui."],
        ["Eu gosto desse eco de pedra.", "Humanos raramente descem tanto.", "Tem cheiro de minério."], ["Fica de olho em quem desce.", "Aqui a gente manda."], weight=3)
    fam("altura_calm", ["calm", "calm"], ["high"],
        ["Que altura! Dá tontura só de olhar.", "O ar aqui em cima é rarefeito.", "Estamos mais perto das nuvens que do chão."],
        ["Cuidado para não cair.", "A vista é bonita, mas o frio incomoda."], ["Vamos descer logo.", "Aqui só se fica com cuidado."], weight=2)

    # ---- o que o jogador esta fazendo agora (so se uma das duas o enxerga: o gerador acrescenta "!unseen")
    fam("jogador_agachado", ["calm", "calm"], ["sneak"],
        ["{Jogador} está andando na pontinha dos pés.", "Olha como {jogador} se esconde pelos cantos.", "{Jogador} anda de mansinho, o que será?", "Por que {jogador} está tão quieto?"],
        ["Deve estar com medo de acordar alguém.", "Talvez esteja planejando alguma coisa.", "Gente cuidadosa vive mais.", "Ou só está brincando."],
        ["Eu fico de olho.", "Cada um com o seu jeito de andar."], weight=3)
    fam("jogador_correndo", ["calm", "calm"], ["run"],
        ["{Jogador} passou correndo!", "Que pressa tem {jogador}?", "Olha {jogador} disparando por aí.", "Aonde {jogador} vai com tanta pressa?"],
        ["Quem corre assim está fugindo de algo.", "Ou atrasado para algum compromisso.", "Deve ter visto um creeper!", "Nunca vi alguém tão agitado."],
        ["Tomara que esteja tudo bem.", "Vamos ver se volta."], weight=3)
    fam("jogador_montado", ["calm", "calm"], ["ride"],
        ["{Jogador} anda montado, que chique!", "Olha só, {jogador} vai de carona por aí.", "Quem dera eu andar assim sem esforço.", "{Jogador} está bem acomodado nesse transporte."],
        ["Isso é que é viajar bem.", "Dá para ir bem mais longe assim.", "Cuidado para não cair.", "Eu preferia andar com meus próprios pés."],
        ["Boa viagem.", "Quem anda assim chega rápido."], weight=3)
    fam("jogador_nadando", ["calm", "calm"], ["swim"],
        ["{Jogador} está na água!", "Olha só, {jogador} dando um mergulho.", "Será que {jogador} sabe nadar mesmo?", "A água deve estar gelada hoje."],
        ["Espero que sim, a água é perigosa.", "Eu nem chego perto de rio.", "Deve ser refrescante.", "Afogamento é coisa séria."],
        ["Que saia logo da água.", "Tomara que não se afogue.", "Vamos ficar de olho."], weight=3)
    fam("jogador_pegando_fogo", ["calm", "calm"], ["burning"],
        ["{Jogador} está pegando fogo!", "Fogo! {Jogador} está em chamas!", "Socorro, {jogador} está queimando!", "Alguém ajuda! Tem fogo em {jogador}!"],
        ["Corre para a água!", "Rolar no chão não adianta nada!", "Tem balde de água por aqui?", "Meu Deus, que cheiro de queimado!"],
        ["Água, água, rápido!", "Tomara que apague a tempo."], weight=5)
    fam("jogador_voando", ["calm", "calm"], ["fly"],
        ["Olha! {Jogador} está voando!", "{Jogador} cruzou o céu como um pássaro!", "Viram {jogador} planando?", "Que coisa mais estranha, {jogador} no ar!"],
        ["Como será que ele faz isso?", "Parece liberdade, deve ser bom.", "Me dá tontura só de olhar.", "Cuidado com a aterrissagem!"],
        ["Eu ficaria no chão mesmo.", "Quem dera voar assim."], weight=4)
    fam("jogador_esmeraldas", ["villager", "villager"], ["emeralds"],
        ["{Jogador} anda com esmeraldas, vi o brilho verde.", "Ouvi dizer que {jogador} carrega esmeraldas.", "Esmeraldas! {Jogador} deve ser um bom freguês.",
         "Dizem que {jogador} paga bem."],
        ["Então vale a pena atender com carinho.", "Vou guardar meu melhor preço.", "Será que quer trocar?", "Eu tenho muita coisa para vender."],
        ["Quem sabe passa por aqui.", "Negócio bom é negócio honesto."], weight=3)
    fam("jogador_diamantes", ["villager", "villager"], ["diamonds"],
        ["Vi brilho azul na bolsa de {jogador}: diamantes!", "{Jogador} carrega diamantes, que sorte!", "Aqueles diamantes de {jogador} brilham de longe!"],
        ["Deve ter cavado muito para juntar.", "Eu só queria ver um de perto.", "Quanto valor naquela bolsa!", "Cuidado para ninguém roubar."],
        ["Que guarde bem.", "Boa sorte para ele."], weight=3)
    fam("jogador_seguidores", ["calm", "calm"], ["followers"],
        ["Quanta gente anda atrás de {jogador}!", "{Jogador} nunca anda sozinho, reparou?", "Olha só o bando que segue {jogador}!"],
        ["Parece um passeio em grupo.", "Deve ser gente de confiança.", "Eu queria entrar nesse grupo.", "Será que tem lugar para mais um?"],
        ["Boa companhia vale muito.", "Quem sabe eu me junto também."], weight=3)
    fam("jogador_companheiro", ["calm", "calm"], ["ally"],
        ["Olha, aquele que segue {jogador} parece contente.", "Aquele ali anda sempre com {jogador}, reparou?", "{Jogador} tem companhia, olha só."],
        ["Gosto de ver os dois juntos.", "Deve ser uma boa companhia.", "Eu também queria alguém assim por perto.", "Os dois combinam."],
        ["Que continuem assim.", "É bonito ver amizade.", "Fico contente por eles."], weight=3)