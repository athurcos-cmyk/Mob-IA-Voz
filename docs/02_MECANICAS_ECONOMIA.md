# 02 — Mecânicas, transações e autonomia controlada

## 1. Separação fundamental: fala ≠ ação

A IA **não recebe permissão para chamar métodos arbitrários do jogo**. Ela descreve (em JSON estruturado) o que um mob pretende dizer e sugere **uma ação permitida**; o `ActionValidator` do servidor decide se há autorização, recursos e condições. O `ActionExecutor` altera o mundo **na thread apropriada**, como operação explícita, idempotente e registrável. Na sequência, o jogo fornece à IA o resultado verdadeiro da ação.

**Pipeline**: entrada do jogador / evento de mundo → seleção do mob → contexto real + personalidade + memória → LLM → resposta estruturada → validação → confirmação opcional → execução → registro → fala sobre resultado.

As ações **não devem** ser inventadas livremente pela IA, apenas escolhidas em catálogo estável:

| Ação permitida | Efeito pretendido | Pré-requisitos do servidor |
|---|---|---|
| `SPEAK` | Resposta vocal | Mob válido, alcance, orçamento de IA |
| `OFFER_TRADE` | Apresenta contrato de troca | Receita plausível e economia habilitada |
| `ACCEPT_OFFER` | Aceita oferta do jogador | Oferta existente, recursos, confiança |
| `PICK_UP_OFFERED_ITEM` | Recolhe item do chão | Item válido, sem dono protegido, distância/LoS |
| `COMPLETE_TRADE` | Consome oferta e entrega recompensa | Transação travada, recursos reservados, validação de cap |
| `RETURN_OFFER` | Devolve itens se oferta falhar | Reservas existentes, destino seguro |
| `FOLLOW_PLAYER` | Mob segue jogador | Capacidade de navegação, motivação e tempo máximos |
| `DEFEND_PLAYER` | Defende temporariamente | Relação >= limiar, alvo legal, não violações de segurança |
| `CEASE_HOSTILITY` | Suspende agressão | Mob permitido, duração/escopo de pacificação |
| `CHANGE_VILLAGER_REPUTATION` | Ajusta reputação/preço | Aldeão válido, limites, sem preço negativo |
| `SHARE_RUMOR` | Propaga informação entre mobs | Facção/canal permitido, rumor verificável ou marcado como rumor |
| `PLAN_GROUP_MOVE` | Combate coordenado simples | Mobs aliados, navmesh/IA de navegação, teto de participantes |
| `START_QUEST` | Oferta objetivo/busca | Modelo de contrato/quest aprovado |
| `BREAK_ALLIANCE` | Traição ou fim de acordo | Motivação/evidência, estado, cooldown, limites |
| `EMOTE` | Expressão visual/sonora | Não atrapalha combate nem UI |

A lista começa pequena e é ampliada por fases; **nem todas as ações existirão no primeiro executável**.

## 2. Trocas físicas com itens no chão

### Fluxo recomendado (exemplo: ouro ↔ recompensa de esqueleto Wither)

1. Jogador conversa: “Você me dá cabeças se eu conseguir ouro?”.
2. IA sugere contrato; **receitas válidas** são decididas por catálogo/configuração e situação do mundo, não improvisadas como item ilimitado.
3. Mob responde com condições, quantidades, preço, validade e custo de reputação.
4. Jogador joga ouro no chão, preferencialmente dentro de 3 blocos do mob e durante sessão aberta.
5. `OfferTracker` observa itens, verifica `ItemEntity` real, quem jogou, dimensão, duplicidade, quantidade e ownership. Opcional: exigir que o jogador esteja próximo para confirmar.
6. Mob caminha até oferta (se rota possível), coleta o item e **reserva** em inventário de contrato do servidor. Se IA/servidor cair, reserva fica persistente e recuperável.
7. Serviço executa a troca **atomicamente**: verifica oferta, reserva recompensa permitida, consome entrada, cria saída e registra `transaction_id`.
8. A recompensa é entregue em inventário ou jogada fisicamente ao chão, associada ao jogador, com proteção contra furto por terceiros durante curto período.
9. Memória do mob e registro do mundo recebem o resultado. A voz comenta a transação **após** o resultado ser confirmado.

### Regras de economia/survival

- No survival padrão, não dar itens raros apenas porque jogador convenceu modelo; recompensas saem de **estoque preexistente**, loot regulamentado/custo de produção ou orçamento limitado por mundo/tempo.
- `Wither Skeleton Skull`: **receita especial** definida pelo criador do mundo; limites de quantidade, cooldown, preço e contexto. Desabilitada por padrão na dificuldade equilibrada até existir uma política convincente de origem.
- Equipamentos de bosses, diamantes, netherite, totens e ovos: nenhuma duplicação ilimitada; use tabelas de recompensa restritas ou moeda interna.
- Se jogador oferecer 64 itens e só 10 forem necessários, devolver 54 com segurança.
- Explosões, morte, chunk unload, desconexão e cancelamento precisam fazer rollback/reembolso ou finalizar de modo idempotente.
- Nunca completar duas vezes a mesma transação; gravar ID antes de enviar resultado e assegurar estado estável após restart.
- Autorização no servidor em LAN/dedicado; cliente não pode criar item mandando pacote falso.
- Modo sandbox **opcional e explicitamente ativado** permite recompensas generosas/criativas, separado do modo survival equilibrado.

## 3. Sistema de relacionamento

**Por jogador e por mob**: confiança (-100..100), afinidade, medo, respeito, dívida, lealdade e hostilidade. **Entre mobs**: rivalidade, alianças, facções, parentela opcional.

- Cada entidade tem traços estáveis: curiosidade, coragem, ganância, sociabilidade, instinto, agressividade; gerados deterministicamente e armazenados.
- Gostos da **espécie/profissão** definem incentivos iniciais; personalidade ajusta resposta. Porco pode recusar mesmo diante de cenoura; cavalo valoriza proteção, abelha valoriza colmeia etc.
- Recusar é comportamento legítimo. Status de humor, dano recente, perigo, horário e reputação alteram probabilidade.
- Promessas viram contratos ou eventos de memória, nunca efeitos automáticos sem execução.
- Traição exige gatilho justificável, chance com teto, estado de aliança, consequências e janela em que o jogador possa perceber sinais. Evitar reviravolta toda hora.
- NPC lembra **ações verificadas**, não meras declarações do jogador. Pode formar opinião errada e espalhar rumor, mas o registro distingue fato de rumor.

## 4. Memória duradoura

Três níveis, para eficiência e consistência:

1. **Curta:** últimas falas relevantes na sessão ativa, com limite de tokens.
2. **Episódica:** eventos persistentes em `world + entity + player` (cura, ataque, presente, troca, traição, ajuda, local importante), com hora/dimensão/participantes.
3. **Social/compartilhada:** resumos por aldeia, colônia, bando, Endermen ou chefe conforme regras; não simplesmente transferir memórias privadas de todos para todos.

Uma transformação zumbi-aldeão → aldeão pode criar **nova entidade UUID**. Interceptar evento de conversão e persistir `lineageId`/`identityId` para religar lembranças, se versão API permitir. Testar conversão real com save/restart.

O Ender Dragon recebe um **resumo seletivo de eventos relevantes do mundo**: chefes derrotados, grandes alianças, negócios raros, exploração do End e relacionamento com Endermen. É “onisciente” na ficção, mas tecnicamente acessa o registro do servidor com limites, filtros de privacidade e recorte de eventos.

Endermen compartilham `collectiveMemory` de facção por mundo/dimensão e reputação coletiva por jogador, com transmissão por evento, cooldown e teto de alcance ou propagação narrativa. Cada indivíduo mantém voz e humor próprios.

## 5. Conversas autônomas entre mobs

### Gatilhos concretos

- Jogador ataca um zumbi e o segundo zumbi está perto: evento de ameaça → diálogo coordenado de duas falas e plano de flanqueamento **apenas se ação aprovada**.
- Esqueleto erra repetidamente flechas: comentário de frustração com cooldown.
- Aldeão recebe presente: outro aldeão comenta ou espalha rumor depois, sem preencher chat.
- Creeper desiste de explodir por acordo: outro mob reage conforme relação.
- Reunião de aldeões, patrulha, colmeia danificada, Enderman observando bloco movido: diálogos ambiente com baixo orçamento.

### Orçamento para não travar o PC

- Acontecimento no mundo **não obriga** consultar a IA. Primeiro filtre evento, relevância, distância, cooldown e chance.
- Para diálogos espontâneos, limite experimental: **um grupo ativo por região; 2 a 3 turnos por conversa; intervalo mínimo 30–90 s por grupo**, configurável.
- Outras criaturas podem ter falas pré-geradas/templated quando LLM está ocupado (sem fingir que foi gerado dinamicamente).
- Em combate, falas rápidas ou pré-geradas ganham prioridade; não esperar resposta da IA para ações essenciais do Minecraft.
- Se o jogador se afastar, bloquear a emissão de áudio e cancelar cálculos não prioritários. Não criar IA em milhares de chunks descarregados.
- Áudio espacial e prioridade: conversa dirigida ao jogador > reação crítica > conversa NPC-NPC > ruído ambiente.

## 6. Cooperação, seguir e chefes

- `FollowController` usa AI de navegação adequada à espécie, distância de seguimento, teleporte **restrito** em caso de impasse e interrupção segura. Regras específicas para voadores, aquáticos e bosses.
- `DefendController` distingue aliados, jogadores, pets e mobs neutros; não deve causar dano em área a aliados sem opção explícita de risco.
- **Warden**: pacificação e seguimento são recursos especiais, com custos, prazo e limites; respeitar sensores e animações. Não prometer atravessar dimensões antes de implementar controle seguro.
- **Wither**: suspensão de ataque temporária, conflito de motivações e traição narrativa. Como causa destruição grande, opção padrão conserva limitações fortes e config de boss destrutivo.
- **Ender Dragon**: comportamento de chefe personalizado somente em fase avançada; o objetivo principal é conversa/contexto. Não garantir que ele vire pet fora do End, o que exigiria sistema de navegação, colisão e dimensões substancial.
- **Morte/ressurreição**: respeito às regras do jogo; memória histórica permanece em mundo/eventos mas não ressuscita mob sem mecânica autorizada.

## 7. Missões e possibilidades adicionais

1. **Contratos de trabalho:** aldeão aceita proteger plantação; ferreiro conserta item com custo real; pescador oferece recompensa pela ajuda (itens/tempo verificáveis).
2. **Consequências regionais:** aldeia fala sobre assaltos, ajuda, defesa; reputação altera taxas e acolhimento.
3. **Fofocas e mal-entendidos:** boato pode ser verdadeiro ou falso; NPCs discutem e confrontam, e o jogador consegue provar/desmentir.
4. **Personalidades persistentes:** um porco medroso, uma abelha territorial, um aldeão desconfiado, uma vaca curiosa — sem caricatura repetitiva.
5. **Histórias emergentes:** um mob pode lembrar quem o salvou e apresentar esse personagem aos demais.
6. **Diplomacia de facções:** Endermen, piglins, aldeões, illagers e monstros podem reagir conforme alianças locais; não precisam todos ser amigos.
7. **Mercadores viajantes com ofertas narrativas:** trocas com inventário/regra, sem itens impossíveis.
8. **Sinais não verbais:** balançar cabeça, olhar para item, mover-se até oferta, parar hostilidade e sons ambientais antes de falar.
9. **Diários do jogador:** registro opcional de acordos e dívidas, sem transcript integral obrigatório.
10. **Eventos conjuntos:** um esqueleto e um zumbi planejam emboscada; se escutados, jogador pode negociar ou fugir.

## 8. Escopo de implementação

Não implementar este documento inteiro de uma vez. Usar `05_ROADMAP.md`. A IA gera **intenções**, regras de gameplay garantem **consequências**. Tudo que não estiver implementado retorna recusa coerente, não uma falsa promessa de sucesso.

## 9. Atualização v2.1 — papel, crenças, vínculos e liberdade

- O jogador pode oferecer qualquer história/mentira. O modelo propõe `REGISTER_CLAIM`, `UPDATE_BELIEF` e `UPDATE_RELATIONSHIP`, mas o registro factual do servidor permanece intacto.
- Qualquer mob pode considerar um vínculo (`FRIEND`, `ALLY`, `FOLLOWER` etc.) mesmo se for normalmente hostil. Comportamento resulta de confiança, medo, risco, interesse e condição do ambiente.
- O controlador social implementará `STAY_AT`, `PORTAL_HOLD`, `DISMISS_ALLY` e `FOLLOW_PLAYER` por tipo de entidade. Um pedido não pode causar teleporte, fuga ou ataque sem adaptador validado.
- Acesso em modo Criativo/Sandbox pode oferecer economia flexível mediante autorização do mundo, mas não abre permissão arbitrária de console para a IA.
- Ver `12_LIBERDADE_CRENÇAS_ALIANCAS.md` para cenário completo da galinha que reconhece jogador como pai.
