# -*- coding: utf-8 -*-
"""Gera site/funcionalidades.html (uma fonte so). Rode: python tools/gen/genfuncoes.py. Ao mudar CSS/JS do site, suba o ?v= nos links de todas as paginas."""
import html, io, os

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "site", "funcionalidades.html")

OK, WARN, OFF, FREE = "ok", "warn", "off", "free"
T_OK = (OK, "testado em jogo")
T_FREE = (FREE, "fala pronta, sem custo de IA")
T_MIC = (WARN, "microfone real: sem teste")
T_OFF = (OFF, "desligado por padrão")

# (id, titulo, icone-categoria, intro, [(titulo, icone, texto, [chips])])
CATS = [
 ("voz", "Voz e conversa", "mic", "Falar de verdade, em português, e ouvir a criatura responder.", [
  ("Fale com o microfone", "mic", "Mire numa criatura (uma estrela marca com quem você fala, até 6 blocos, uma por vez) e fale. A fala vira texto no seu PC e a resposta sai em voz, vinda dela, com som espacial (Simple Voice Chat).", [T_OK, T_MIC]),
  ("Modo chat, sem microfone", "bubble", "Digite <code>/mobvoz chat sim</code> e escreva no chat: o texto vai só para a criatura na mira, e a resposta vem como legenda (e em voz).", [T_OK]),
  ("Chame pelo nome, sem mirar", "tag", "“Mimosa, vem aqui!” funciona a até 32 blocos, digitado ou falado. O nome precisa estar no começo ou no fim da frase: falar <i>de</i> alguém no meio não chama ninguém.", [T_OK]),
  ("Conversa livre", "bubbles", "A criatura responde direto ao que você disse e lembra as últimas 6 trocas da conversa. Elogio, piada, medo, conselho e até perguntas fora do jogo.", [T_OK]),
  ("Uma voz para cada criatura", "feather", "Tom e velocidade próprios para cada uma, com a voz natural Kokoro ou o modo leve Piper. Estão no site: ouça as vozes na página inicial.", [T_OK]),
  ("Legendas", "eye", "O que a criatura diz aparece no chat, em amarelo, com o nome dela (ou o apelido). Ligue para quem joga sem som: <code>/mobvoz legenda sim</code>.", [T_OK]),
  ("Opinam sobre outras criaturas", "book", "Ao citar outra criatura (“o que acha das vacas?”), a resposta recebe a história da citada para opinar sem inventar.", [T_OK]),
  ("Assuntos delicados, com cuidado", "heart", "Proposta indecente: recusa com leveza. Autolesão: acolhe, sugere conversar com alguém de confiança e o CVV (188).", [T_OK]),
  ("Falas de reserva por espécie", "shield", "Se a IA falhar, a criatura diz uma de 5 frases do jeito da espécie (16 espécies mais a padrão), sempre pedindo para você repetir, em vez de um erro seco.", [T_OK]),
  ("Cache de áudio", "bolt", "As falas prontas se repetem sem sintetizar de novo (de 888 ms para 0 ms no teste), em um cache de 48 MB.", [T_OK]),
  ("Voz por frase", "bolt", "A resposta não espera a fala inteira ficar pronta: a primeira frase toca assim que é gerada e as outras são geradas enquanto ela toca. O som continua saindo do próprio mob.", [T_OK]),
  ("Papo simples, sem espera extra", "feather", "Pergunta ou cumprimento sem pedido, troca, item, mentira nem ameaça dispensa a primeira análise da IA: uma chamada a menos (cerca de 1,2 s com a API). Conferido com a IA real em 1.187 frases: nenhuma fala com pedido ou troca passou pelo atalho.", [T_OK]),
  ("Som de “pensando”", "eye", "Se a resposta demora, a criatura solta um som curto do jeito da espécie (“Muuu?”, “Hmm, oinc...”) e a resposta espera ele acabar. Se a resposta chega logo, nada é dito. Dá para desligar (<code>thinkingSounds</code>).", [T_OK, T_FREE]),
 ]),
 ("memoria", "Memória e relações", "book", "Cada criatura lembra o que viveu com você, e o que ela sabe vem de fatos reais.", [
  ("Memória de cada criatura", "book", "Presentes, trocas, golpes, consolo: tudo vira lembrança da criatura, guardada no próprio mob e mantida ao salvar e sair do mundo. Só é criada quando você interage.", [T_OK]),
  ("Fato × alegação × crença", "eye", "O que o servidor viu é fato e ninguém reescreve. O que você só disse é uma alegação: a criatura pode acreditar, duvidar ou rir, mas isso nunca vira fato.", [T_OK]),
  ("Confiança, afinidade e medo", "heart", "Uma barrinha na barra de ação mostra quanto falta para ela aceitar te seguir. Cada espécie exige um tanto: vaca e ovelha aceitam logo; porco, aldeão e golem pedem presentes.", [T_OK]),
  ("Presentes", "emerald", "Segure um item e use <kbd>SHIFT</kbd> + botão direito. Ela diz se gostou, ficou indiferente ou não gostou. Itens parecidos contam (cenoura dourada vale como cenoura).", [T_OK]),
  ("Mentiras e crenças", "skull", "Você pode mentir (“eu sou seu pai!”). Se ela acredita depende da personalidade da espécie: uns são crédulos, outros desconfiados.", [T_OK]),
  ("Seu nome, criatura a criatura", "tag", "Até saber seu nome você é “Humano”. Quando você diz (ou responde a uma pergunta), só aquela criatura aprende. As outras só sabem por fofoca entre elas.", [T_OK]),
  ("Apelidos por voz", "tag", "“Vou te chamar de Mimosa”: o servidor decide (ela precisa confiar e não ter medo) e põe uma etiqueta de nome sobre a cabeça. Chefes e palavrões ficam de fora.", [T_OK]),
  ("Fama da aldeia, só local", "shield", "Bater num aldeão só vira fama entre aldeões e golens a até 64 blocos; outra vila não sabe. Um golpe sem querer pesa pouco e a fama some com o tempo. Enderman sabe em qualquer lugar.", [T_OK]),
  ("Honestidade lembrada", "feather", "Devolver o que ganhou, sem ninguém pedir, conta: a criatura lembra, o grupo (aldeia ou Endermen) fica sabendo e o Dragão do Fim recebe o registro.", [T_OK]),
  ("/mobvoz memoria", "book", "Mostra o que a criatura da mira sabe de você: nome, confiança, afinidade e medo, quanto falta para ela te seguir, fatos, alegações e o que ela acha delas.", [T_OK]),
  ("Dificuldade da confiança", "bolt", "<code>/mobvoz dificuldade</code> muda quanto as criaturas exigem: Fácil (×0,6), Normal ou Difícil (×1,4). Chefes e vacas não mudam. Em servidor, só o admin troca.", [T_OK]),
  ("Cura de zumbi-aldeão", "heart", "Quando um zumbi-aldeão é curado, a memória segue com a mesma criatura: quem você ajudou continua lembrando.", [T_OK]),
 ]),
 ("ordens", "Ordens e alianças", "arrow", "Ordens em linguagem livre que viram ações reais, e vínculos de verdade.", [
  ("Ordens reais", "arrow", "“Me segue”, “fica aqui”, “não entra no portal”, “me defende”, “pode ir”. Elas obedecem de verdade, ou recusam se não confiam. Um bom argumento ajuda, mas com limite.", [T_OK]),
  ("Teletransporte seguro", "bolt", "Quem ficou para trás se teletransporta até você, sempre num lugar seguro.", [T_OK]),
  ("Aliados que defendem", "shield", "Quem segue também defende, e o dano amigo é bloqueado. Até 15 aliados por padrão (o máximo é 32).", [T_OK]),
  ("Trégua e traição", "skull", "Uma trégua protege você, mas pode ser rompida: ferir o aliado quebra o acordo e a traição fica registrada.", [T_OK]),
  ("Aliados avisam do perigo", "eye", "Quem anda com você avisa quando um inimigo se aproxima, com falas variadas e prontas.", [T_OK, T_FREE]),
  ("Domar conversando", "bone", "Cavalo, burro, mula, lhama, lobo, gato e papagaio se deixam domar se confiarem em você, com dono de verdade: cavalo montável, lobo e gato seus.", [T_OK]),
  ("Chefes aliados", "skull", "Warden e Wither seguem, guardam e defendem; o Dragão do Fim faz trégua. Aceitam com muita confiança ou por um bom pagamento, traem quem os fere e não destroem blocos sob contrato. Aliados, só atacam quem é hostil a você, nunca porco, vaca ou aldeão.", [T_OK, (WARN, "Dragão só testado no Overworld")]),
  ("Pague para ser seguido", "emerald", "Se a criatura recusa por falta de confiança, ela diz o preço em itens que aceita (um esqueleto, ossos e flechas). Jogue no chão, até aos poucos: ela pega só o necessário, avisa o que falta e, pago, passa a obedecer. O que sobra fica com você.", [T_OK]),
  ("Reagem ao seu tom", "bolt", "Ameace um porco e ele foge de verdade; um zumbi avança; o Warden, que é cego, nem se abala. Consolar acalma.", [T_OK]),
 ]),
 ("trocas", "Trocas e negociação", "emerald", "Dê e receba de verdade, com preço que muda e espaço para barganhar.", [
  ("Troca física", "emerald", "Peça algo e jogue os itens no chão, perto dela: ela diz o que aceita, devolve a sobra e entrega. Os itens ficam reservados até fechar, sem duplicar nem sumir.", [T_OK]),
  ("130 receitas, 46 criaturas", "book", "Um catálogo por criatura e profissão, pesquisado no wiki: ferro do golem, couro da vaca, produtos de 13 ofícios de aldeão e mais.", [T_OK]),
  ("Preço variável e barganha", "tag", "O preço varia com a confiança (±15%), o humor do dia (±10%) e um argumento seu. Fica travado na oferta e nunca foge de 70% a 125% do valor.", [T_OK, (WARN, "números a ajustar jogando")]),
  ("Peça qualquer item", "emerald", "Ferramentas, armaduras, composteira, farinha de osso, o que for: se não está na lista da criatura, a IA decide se ela teria (o golem tem peças de ferro, o peixe acha armadura em naufrágio, o zumbi herda o equipamento de quem morreu) e o servidor põe o preço pela raridade do item, baixo. Qualquer criatura aceita esmeralda, diamante, ouro e ferro como moeda, menos o mesmo material do que ela dá: ninguém paga ferro para ganhar ferro. Ao entregar, ela conta de onde veio o item.", [T_OK]),
  ("Entende seu jeito de falar", "bubbles", "Você não precisa dizer o nome certo: “uma armadura muito boa” vira a de diamante, “uma armadura boa” a de ferro, “a melhor espada” a de netherite, “uma armadura simples” a de couro, “armadura ferro” sem o “de” funciona, e “bonimil” é farinha de osso. Até erro de fala (“espada de dimante”) é corrigido. “Bora” e “vamos nessa” também chamam para seguir.", [T_OK]),
  ("Ameaça para conseguir", "skull", "“Me dá o couro, que eu não te mato.” A vaca medrosa larga o item de medo e perde a confiança em você; o zumbi valente só cede se estiver ferido ou se você parecer perigoso (espada, armadura forte); o Warden e o impassível nunca cedem.", [T_OK]),
  ("Todo mob negocia", "emerald", "Do creeper (pólvora) ao golfinho, ao Warden e ao aldeão sem ofício: cada criatura aceita o que combina com ela e só dá o que faz sentido, e fala pechinchando, não como balcão.", [T_OK]),
  ("Negociação aberta", "bubbles", "Fora do catálogo, a IA julga se aceitaria e se teria. Ela pode recusar, você pode convencer, e um pedido improvável pode dar certo por sorte.", [T_OK]),
  ("Quantidade negociável", "feather", "Um negócio pode ter várias unidades (3 caveiras = 1 negócio), com preço proporcional e falas que dizem o limite e o prazo.", [T_OK]),
  ("Pedido sem sentido", "eye", "Pedir uma picareta de diamante a uma galinha rende uma recusa improvisada pela IA, na voz da criatura.", [T_OK]),
  ("Hostis negociam sem atacar", "skull", "Um zumbi, esqueleto ou piglin que conversa ou negocia com você não ataca. Atacar o parceiro cancela a oferta.", [T_OK]),
 ]),
 ("mundo", "Mundo vivo", "bubbles", "Falas prontas, sem custo de IA, para o mundo parecer habitado mesmo quando você só passa.", [
  ("Conversas entre criaturas", "bubbles", "Aldeões com aldeões, vacas com vacas, zumbis com zumbis: 300 assuntos e mais de 2.000 falas únicas, prontas e sem custo de IA.", [T_OK, T_FREE, (WARN, "frequência: palpite a ajustar")]),
  ("Cada tipo de mob no seu assunto", "feather", "Só conversa quem faz sentido: aldeão com aldeão, animais de fazenda entre si, monstro só com a mesma espécie. Vaca nunca fala de “trocar”.", [T_OK]),
  ("Reparam no que você tem", "eye", "O trigo na sua mão, a armadura que você veste: a vaca pergunta se é para ela, e cada mob quer o que combina com ele.", [T_OK, T_FREE]),
  ("Falam do mundo", "bolt", "Hora, clima, bioma (neve, deserto, selva, pântano, floresta, montanha, praia, savana), lua cheia e nova, profundidade e altura.", [T_OK, T_FREE]),
  ("Falam do que você faz", "star", "Agachado, correndo, montado, nadando, pegando fogo, voando, com esmeraldas ou diamantes, ou acompanhado de aliados.", [T_OK, T_FREE]),
  ("Reagem aos acontecimentos", "bolt", "Creeper que explode perto, raio, você acordando, a volta de uma morte, monstro morto perto da vila, saqueadores, queda e o “Ai!” de quem apanha. 52 grupos de falas fixas.", [T_OK, T_FREE]),
  ("Golem, guarda da vila", "shield", "Ao voltar à vila, o golem cumprimenta e pede ajuda para protegê-la, lembrando de presente, pancada, consolo ou promessa quebrada. 7 grupos de falas.", [T_OK, T_FREE]),
  ("Atrás da parede, sem espiar", "lock", "Sem linha de visão até você, ninguém comenta o que você veste ou faz. Falam do clima, dos bichos e do que lembram de você.", [T_OK]),
  ("Cumprimentam quem conhecem", "heart", "Quem já te conhece reage ao te ver de novo, do jeito da espécie.", [T_OK, T_FREE]),
  ("A criatura puxa assunto", "star", "De vez em quando uma criatura pacífica, perto e que vê você, abre a conversa: pergunta, adivinha, piada, dica, segredo, assunto do ofício (aldeões), da mata, do mar. Cerca de 400 aberturas, sem custo de IA; hostis nunca. Quando você responde, a IA continua o MESMO assunto (a abertura entra no histórico).", [T_OK, T_FREE]),
  ("Variedade sem repetir", "feather","As últimas 120 cenas ficam de fora do sorteio e cada acontecimento tem tempo de espera, para um creeper nunca gerar sempre a mesma fala.", [T_OK]),
 ]),
 ("criaturas", "Personalidade", "heart", "Nenhuma criatura é igual à outra, nem entre espécies nem dentro da mesma.", [
  ("66 criaturas com história", "book", "Origem, sentidos, motivações e medos de 66 criaturas, pesquisados no wiki (com as lendas marcadas como lendas).", [T_OK]),
  ("Perfis por espécie", "tag", "Dieta, habitat, gostos, credulidade e jeito de falar: dezenas de perfis, e um perfil derivado por categoria para o resto.", [T_OK]),
  ("Cada criatura é única", "star", "Memória própria, humor do dia e um tom de voz levemente diferente de uma vaca para outra.", [T_OK]),
 ]),
 ("tecnologia", "IA, privacidade e servidor", "lock", "Você escolhe a IA, e o servidor do jogo manda em tudo o que acontece.", [
  ("Programa de instalação", "arrow", "Um <code>MobVozIA.exe</code> com janela: instala tudo, guarda a chave da API <b>uma vez</b> (testa na hora e mostra só o final), liga e para o serviço sem janela preta e abre o Minecraft. Atualizar para uma versão nova reaproveita o que já foi baixado.", [T_OK]),
  ("Sua IA, seu ritmo", "feather","Modelo local grátis (Ollama) ou a sua própria API, como a DeepSeek. Voz principal Kokoro (natural) ou Piper (leve e rápida).", [T_OK]),
  ("Privacidade por desenho", "lock", "O áudio nunca sai do seu PC e não é gravado. Nada de jogador vai para o log. Só o texto vai à API, e só se você escolher uma. Sem o seu consentimento, o microfone não é usado.", [T_OK]),
  ("A IA só sugere", "shield", "Ela devolve um JSON estrito; o servidor só executa o que está numa lista permitida e passa pelas regras. Nada de console ou <code>/give</code> vindo do modelo.", [T_OK]),
  ("Servidor e multijogador", "bubbles", "Consentimento por jogador, conferido no servidor. Testado num servidor dedicado com 1 jogador conectado.", [T_OK, (WARN, "2+ jogadores: sem teste")]),
  ("Leve e ajustável", "bolt", "Presets <code>low</code>, <code>balanced</code> e <code>high</code>, limites de conversas por região e por minuto, e <code>/mobvoz status</code> com os tempos de resposta.", [T_OK]),
  ("Ajuda dentro do jogo", "tag", "<code>/mobvoz ajuda</code> lista todos os comandos e explica como falar, ganhar confiança e dar apelido. Uma dica aparece na primeira vez que você entra em um mundo.", [T_OK]),
  ("Diagnóstico em português", "eye", "Se a IA não responde, a janela do serviço explica o motivo (chave errada, sem saldo, Ollama desligado) e o que fazer.", [T_OK]),
 ]),
]

SHORT = {"voz": "Voz", "memoria": "Memória", "ordens": "Ordens", "trocas": "Trocas", "mundo": "Mundo vivo", "criaturas": "Personalidade", "tecnologia": "IA e privacidade"}
CLS = {OK: "ok", WARN: "warn", OFF: "off", FREE: "free"}

def chips(cs):
    return "".join('<span class="chip %s">%s</span>' % (c, html.escape(t)) for c, t in cs)

total = sum(len(c[4]) for c in CATS)
sections, pills = [], ['<button class="pill" type="button" data-cat="todas" aria-pressed="true">Todas<small>%d</small></button>' % total]
for cid, title, icon, intro, feats in CATS:
    pills.append('<button class="pill" type="button" data-cat="%s" aria-pressed="false">%s<small>%d</small></button>' % (cid, SHORT[cid], len(feats)))
    cards = []
    for t, ic, txt, cs in feats:
        cards.append('      <article class="card feat"><div class="slot"><img class="pixel" src="assets/img/ui/icon-%s.png" alt=""></div><h3>%s</h3><p>%s</p><div class="chips">%s</div></article>' % (ic, t, txt, chips(cs)))
    sections.append('''  <section class="cat" id="%s" data-cat="%s">
    <h2>%s <small>%d funcionalidades</small></h2>
    <p class="lead">%s</p>
    <div class="grid">
%s
    </div>
  </section>''' % (cid, cid, title, len(feats), intro, "\n".join(cards)))

PAGE = '''<!doctype html>
<html lang="pt-BR">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
  <title>Funcionalidades — MobVoz IA</title>
  <meta name="description" content="Tudo o que o MobVoz IA faz: voz, memória, ordens, trocas, mundo vivo, personalidade e privacidade. %(total)d funcionalidades.">
  <meta name="theme-color" content="#0d110f">
  <meta property="og:title" content="Funcionalidades — MobVoz IA">
  <meta property="og:description" content="%(total)d funcionalidades do mod que faz os mobs do Minecraft falarem com você.">
  <meta property="og:image" content="assets/img/conversa-vaca.png">
  <link rel="icon" type="image/png" href="assets/favicon.png">
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Figtree:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;600&family=Pixelify+Sans:wght@500;600;700&family=Press+Start+2P&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="assets/css/style.css?v=14">
</head>
<body>
<a class="skip" href="#main">Ir para o conteúdo</a>
%(header)s
<main id="main">

<section class="page-hero">
  <div class="wrap">
    <p class="eyebrow">Tudo o que o mod faz</p>
    <h1><span class="num">%(total)d</span> funcionalidades, <em>uma a uma</em></h1>
    <p class="lead">Não é um chatbot colado no jogo: o que a criatura diz tem consequência de verdade, e o que ela decide fazer, ela faz. Use a busca ou as categorias.</p>
  </div>
</section>

<div class="toolbar"><div class="wrap">
  <input class="search" id="q" type="search" placeholder="Buscar (ex.: troca, golem, nome)" aria-label="Buscar funcionalidades" autocomplete="off">
  <div class="pills" role="group" aria-label="Categorias">
    %(pills)s
  </div>
</div></div>

<div class="section" style="padding-top:34px">
<div class="wrap">
  <p class="legend">
    <span><span class="chip ok">testado em jogo</span> funciona nos testes automáticos dentro do Minecraft</span>
    <span><span class="chip warn">sem teste humano</span> ninguém jogou isso ainda</span>
    <span><span class="chip off">desligado por padrão</span> o dono do mundo liga</span>
    <span><span class="chip free">sem custo de IA</span> fala pronta, grátis</span>
  </p>

%(sections)s

  <p class="empty" id="empty">Nenhuma funcionalidade encontrada. Tente outra palavra.</p>
</div>
</div>

<section class="section cta">
  <div class="wrap">
    <h2>Quer ver isso <em>funcionando</em>?</h2>
    <p class="lead">Baixe o pacote e instale em poucos cliques, ou veja como tudo isso se encaixa por dentro.</p>
    <div class="btn-row">
      <a class="btn big" href="instalar.html">Instalar o mod</a>
      <a class="btn big ghost" href="como-funciona.html">Como funciona</a>
    </div>
  </div>
</section>

</main>
%(footer)s
<script src="assets/js/app.js?v=14" defer></script>
</body>
</html>
'''

NAV = [("index.html", "star", "Início", "Início"), ("funcionalidades.html", "bubbles", "Funcionalidades", "Recursos"),
       ("como-funciona.html", "bolt", "Como funciona", "Como"), ("instalar.html", "arrow", "Instalar", "Instalar"),
       ("comandos.html", "tag", "Comandos", "Comandos"), ("ajuda.html", "shield", "Ajuda", "Ajuda")]

def header(cur):
    items = []
    for href, ic, lg, sh in NAV:
        a = ' aria-current="page"' if href == cur else ""
        items.append('    <li><a href="%s"%s><img class="pixel" src="assets/img/ui/icon-%s.png" alt=""><span class="long">%s</span><span class="short">%s</span></a></li>' % (href, a, ic, lg, sh))
    return '''<header class="site-head"><div class="wrap">
  <a class="brand" href="index.html"><i aria-hidden="true"></i>MobVoz <b>IA</b></a>
  <nav class="nav" aria-label="Principal"><ul>
%s
  </ul></nav>
  <a class="btn sm head-cta" href="downloads/MobVozIA-0.1.0.zip" download>&#11015; Baixar</a>
</div></header>''' % "\n".join(items)

FOOTER = '''<footer class="site-foot"><div class="wrap">
  <div class="cols">
    <div><h4>MobVoz IA</h4><p>Alpha 0.1 · Mod Fabric para Minecraft Java 1.21.8. Código próprio sob licença MIT. Projeto de fã: não é um produto oficial do Minecraft, nem aprovado ou associado à Mojang ou à Microsoft.</p></div>
    <div><h4>Páginas</h4><ul>
      <li><a href="funcionalidades.html">Funcionalidades</a></li><li><a href="como-funciona.html">Como funciona</a></li><li><a href="instalar.html">Instalar</a></li><li><a href="comandos.html">Comandos</a></li></ul></div>
    <div><h4>Ajuda</h4><ul>
      <li><a href="ajuda.html">Problemas comuns</a></li><li><a href="ajuda.html#estado">Estado do projeto</a></li><li><a href="ajuda.html#creditos">Créditos e licenças</a></li></ul></div>
  </div>
  <p class="legal">“Minecraft” é marca da Mojang Studios. As texturas e ícones desta página são arte própria.</p>
</div></footer>

<div class="lightbox" id="lightbox" role="dialog" aria-label="Imagem ampliada"><img alt=""></div>'''

out = PAGE % dict(total=total, header=header("funcionalidades.html"), footer=FOOTER, pills="\n    ".join(pills), sections="\n\n".join(sections))
with io.open(OUT, "w", encoding="utf-8", newline="\n") as f:
    f.write(out)
print("features:", total, "categories:", len(CATS))
