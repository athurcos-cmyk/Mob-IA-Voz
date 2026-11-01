# Changelog

## 0.1.0 — 2026-10-30 (primeira versão completa do roadmap, ainda SEM teste com microfone real)

- **Fase 1** alvo único com estrela (qualquer `Mob`, 6 blocos, linha de visão).
- **Fase 2** consentimento (`/mobvoz voz sim|nao`), VAD/segmentação, HUD de estado; áudio nunca em disco/log.
- **Fase 3** sidecar Python local (Ollama `qwen3:4b`), contrato JSON estrito, fallback em pt-BR.
- **Fase 4** STT faster-whisper + TTS Piper pt-BR; fala espacial pelo canal de entidade do Simple Voice Chat.
- **Fase 5** memória persistente por criatura: fatos verificados × alegações × crenças; presentes e agressão viram fatos.
- **Fase 6** ordens reais em linguagem livre: seguir, ficar, portal, dispensar, trégua (mixin `PORTAL_HOLD`).
- **Fase 7** troca física (porco/vaca/galinha): reserva, excedente intacto, idempotência, orçamento, recuperação após reinício.
- **Fase 8** perfis (8 espécies + 4 profissões + fallback), gostos, voz e tom individuais.
- **Fase 9** conversa mob-mob por evento com orçamento (região, criatura, teto por minuto, prioridade do jogador).
- **Fase 10** alianças: defesa, dano amigo bloqueado, trégua que protege e pode ser rompida (traição registrada).
- **Fase 11** continuidade na cura zumbi-aldeão, memória coletiva de facções, histórico verificado (Ender Dragon).
- **Fase 12** chefes (Warden, Wither, Dragão do Fim): governança em `server.json` (contratos com chefes DESLIGADOS por padrão, limite de seguidores) e, com `bossContractsEnabled`, adaptadores reais. Warden e Wither seguem, ficam de guarda e defendem; o Dragão (preso ao Fim) faz trégua e defende, mas não segue. O chefe só firma pacto com confiança alta (>= 0,75; trégua >= 0,5) e nunca fere o dono. **Trai** quem o feriu (contrato rompido, ele se volta contra o dono). Sob contrato não quebra blocos (`bossDestructionOnAlliance`: Wither, caveiras e Dragão). O Warden aliado não escava. Sem loot novo e sem missões/quests.
- **Nome do jogador e mundo vivo** (`docs/19_MUNDO_VIVO_E_NOMES.md`): cada criatura aprende o nome do jogador por conta própria (ele diz, ou responde a uma pergunta) e passa a usá-lo; até lá ele é "Humano". Conversas ambiente entre criaturas calmas, sem IA (55 famílias de assunto, ~1.400 cenas base, só o que é verdade agora: hora, clima, o que o jogador segura, seu estado, o que cada criatura sabe dele); o nome se espalha por fofoca. Chaves `ambientChatter` e `ambientCooldownSeconds`.
- **Voz Kokoro** (`ai-service/app/speech.py`, `baixar_kokoro.py`, `docs/relatorios/audicao_vozes/`): o TTS principal passa a ser o Kokoro-82M (muito mais natural; um pouco mais lento para gerar), com o Piper como modo leve e queda automática se o Kokoro faltar. Escolha por `MOBVOZ_TTS` ou `voiceEngine` em `server.json`. O Piper agora compensa o tempo ao mudar o tom (o tom grave não alonga mais a fala nem o agudo a encurta).
- **Conversas ambiente por tipo de mob** (docs/19): só conversa quem faz sentido (aldeão×aldeão, animais de fazenda entre si, monstro só com a mesma espécie), vocabulário próprio de cada mundo, mobs que querem o que você segura e reparam na armadura, bateria de coerência com juiz de IA (3,98/5). 77 famílias, ~1.750 cenas base.
- **Página do projeto** (`site/`, docs/20): página estática tema "blocos" com prints reais, vozes, funções, instalação passo a passo para um PC do zero, comandos, IA própria × local, problemas e estado Alpha. Scripts novos: `ai-service/instalar-sidecar.ps1`, `configurar-api.ps1`, `run-sidecar-api.bat`.
- **Fase 13** consentimento por jogador validado no servidor; teste em servidor dedicado com um cliente.
- **Fase 14** métricas (`/mobvoz status`), presets, documentação, licenças e script de empacotamento.

- **Modo chat** (sem microfone): `/mobvoz chat sim`, texto digitado vai privado à criatura na mira, resposta como legenda; `/mobvoz falar`, `/mobvoz legenda`. Texto digitado não exige o consentimento de microfone (é ação explícita), mas segue todas as mesmas validações do servidor.

- **Negociação e trocas por profissão:** valores de itens, interesse por criatura, convencimento com limites, 13 profissões de aldeão com o que pedem/dão, falas sobre sobra/falta/negócio desfeito, drops só coerentes com a criatura (`docs/15_NEGOCIACAO_E_TROCAS.md`); mais 22 perfis de espécie (dieta/habitat) e perfil derivado por categoria para criaturas sem perfil.

- **Consciência dos mobs e reações naturais** (`docs/16_CONSCIENCIA_E_REACOES.md`): `lore.json` com 66 criaturas (origem, sentidos, motivações, medos, reação a ameaça, pesquisadas no wiki; lendas marcadas); o mob reage ao tom da fala — ameaça faz o porco fugir de verdade, o zumbi avançar e o Warden (cego) não se abalar; consolo acalma.

- **Quantidade negociável e preço em itens:** um negócio pode ter várias unidades (3 caveiras = 1 negócio, preço proporcional, limite de 3 por criatura a cada 3 dias de jogo); a criatura fala preços em itens e porcentagem, nunca em "pontos", e anuncia o limite e o prazo certo; corrigidos o plural de nomes compostos e o início da janela (agora na primeira entrega).

- **Negociação para todas as criaturas:** receitas para golem de ferro (flores → ferro), enderman (blocos → pérola), aranha (carne → teia), esqueleto (material de flecha → flechas) e blaze (carvão → vara), usando o mesmo motor; teste que percorre as 24 receitas; regra de "sem criar valor" e teto por janela; mob hostil negociando não ataca o parceiro e atacá-lo cancela a oferta.

- **Catálogo por criatura, sem receita rara nem cooldown:** 130 receitas em 46 perfis (golem: ferro, bloco, armadura e pedra; camelo: cacto; peixe: só coisas do mar; vaca: couro; pastos: flores diferentes; aldeões: esmeralda e produtos do ofício), pesquisadas no wiki; preço = valor da saída × margem, validado para o catálogo inteiro; removidos a receita rara, o opt-in `rareRecipes` e a janela/limite por prazo. Pedido sem sentido (picareta de diamante à galinha) recebe recusa **improvisada pela IA** na voz da criatura. Hostil que conversa ou negocia com o jogador não o ataca (`Parley`).

- **Negociação aberta:** o catálogo é só o ponto de partida. Pagamentos e pedidos fora dele são **julgados pela IA** (`/v1/judge`: aceitaria? teria?), a criatura pode recusar e o jogador pode convencer; pedido improvável pode dar certo **por sorte** (galinha que "achou uma picareta jogada por aí"); receitas improvisadas nunca criam valor nem passam do teto de preço. Mais 28 itens conhecidos (ferro/ouro bruto, ferramentas de pedra/madeira, comida assada, blocos…).

- **Conversa mais natural e mais rápida:** a criatura lembra as últimas 6 trocas da conversa; ao citar outra criatura ("o que acha das vacas?") ela recebe a lore da citada para opinar sem inventar; novas regras de fala (responder direto, sem "Humano" em toda frase, sem recitar dieta, sem prometer o que não pode fazer); reage em voz alta a eventos (pegar o ovo que a galinha acabou de botar). Falas da troca agora geradas pela IA. Sidecar aquece modelo, vozes e STT ao ligar (1ª fala ~17 s -> ~2 s) e a resposta/análise saem em JSON enxuto (mediana ~3,5 s -> ~2,4 s).
- **Aliados:** limite padrão 15 (máx. 32, presets não reduzem); seguir e proteger agora andam juntos (quem segue também defende; defender sem estar parado também segue). Estrela do Nether passa a ser receita comum do esqueleto Wither.

- **Preço variável e barganha:** o preço da troca varia com a confiança no jogador (±15%), o humor do dia da criatura (±10%) e uma barganha por argumento (a IA avalia, o servidor limita); fica travado na oferta; nunca abaixo do valor do item nem fora de 70%..125% do base (`dynamicPrices` no `server.json`). Corrigido: a criatura (ex.: piglin) recolhia do chão parte do que tinha acabado de entregar ou devolver. Modo "IA própria" opcional via API (DeepSeek testada): ver `INSTALL.md`.

- **Interações naturais** (`docs/17_INTERACOES_NATURAIS.md`): domar por conversa (`TAME_ME`: cavalo, burro, mula, lhama, lobo, gato, papagaio; confiança + argumento, sem maçã); devolver o que a criatura entregou vira **honestidade lembrada** pela criatura, pelo grupo (Endermen/aldeia) e pelo registro do mundo que o Ender Dragon recebe; a cena real (hora, clima, vida, o que o jogador segura, quem está perto) entra nas conversas; aliados avisam de inimigos que se aproximam; criaturas cumprimentam quem já conhecem. Um argumento forte também ajuda em seguir/proteger/trégua (`appeal`, limitado). Chave `naturalEvents` desliga as falas espontâneas. Trocas e devoluções honestas não têm limite diário.

### Limitações conhecidas

Veja `STATUS.md` ("Limitações conhecidas").
- **Ambiente +60% (2026-10-31)**: de 85 famílias / 695 falas únicas para 140 famílias / 1.112 (ofícios de aldeão, vila, hábitos dos bichos, monstros entre si); 28 famílias reescritas depois do juiz por família (`ai-service/tests/battery_families.py`, média 4,58, nenhuma abaixo de 3,8). Pares: 3,85 → 4,06. Conversa livre: bateria `livre_` (44 cenas). O tratamento genérico "Humano" agora aparece em ~1 de cada 3 falas (antes ~48%). "de o humano" vira "do humano".
- **Pronto para a Alpha (2026-10-31)**: tutorial conferido contra o pacote real (instalacao do zero em 82 s; resposta, voz Kokoro e analise funcionando pela API a partir da copia limpa; teste ambiente dentro do Minecraft passou). Links diretos para o Fabric API 0.136.1+1.21.8 e o Simple Voice Chat 2.6.27 (beta no Modrinth), avisos de Python/Windows SmartScreen/Microsoft Store, Python recomendado 3.12 a 3.14, GitHub fora do site, pacote sem relatorios de teste, log de inicio mostra a IA em uso.
- **Tutorial sem caminho fixo (2026-10-31)**: o zip pode ser extraido em qualquer pasta (ex.: Downloads\MobVozIA-0.1.0 (2)); novos atalhos de duplo clique `instalar-sidecar.bat` e `configurar-api.bat`; as janelas de `run-sidecar*.bat` nao fecham sozinhas em caso de erro. Testado extraindo num caminho com espaco e parenteses.
