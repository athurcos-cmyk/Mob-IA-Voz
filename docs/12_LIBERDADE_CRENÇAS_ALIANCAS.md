# 12 — Liberdade narrativa, crenças e alianças universais

**Status:** especificação aprovada como objetivo de design. Nenhuma das mecânicas abaixo está implementada neste pacote de documentação.

## Regra fundamental: o jogador pode tentar convencer, não necessariamente conseguir

**Não restringir o que o jogador pode dizer** a uma lista de frases de comando. O jogador pode contar histórias, fingir parentesco, blefar, mentir, prometer presentes, se declarar rei, implorar, fazer ameaças, propor alianças, dar ordens ou pedir algo absurdo. A interpretação é livre; a ação resultante precisa existir no catálogo seguro do motor. Nunca proibir diálogo porque uma espécie não deveria querer aquilo; em vez disso, a espécie e a personalidade alteram a disposição e condições para aceitar.

**Não usar um filtro de veracidade para impedir roleplay.** Separar:
- **Fato verificado**: evento que o servidor comprovou (jogador entregou 64 esmeraldas, curou uma entidade, atingiu aldeão).
- **Declaração/alegação**: o jogador disse ser pai de uma galinha, rei das esmeraldas ou amigo do dragão.
- **Crença da criatura**: mob pode acreditar total/parcialmente, desconfiar ou fingir acreditar. Uma crença não vira fato no registro histórico.
- **Interpretação afetiva**: a galinha pode chamar o jogador de pai, manter laço familiar adotivo/fictício e segui-lo, sem o jogo afirmar parentesco biológico.
- **Promessa**: existe até ser cumprida, desmentida ou cancelada.

## Exemplo canônico: galinha que chama o jogador de pai

1. Jogador gera galinha de um ovo (ou usa spawn egg no modo Criativo).
2. Jogador diz por voz: "Eu sou seu pai, sabia? Eu te criei!".
3. A IA recebe contexto real (spawn recente), personalidade (ingênua/cautelosa), relação atual e evidências. Gera resposta em português e propõe crença (`BELIEVE_CLAIM`) com confiança moderada, se fizer sentido.
4. O serviço grava **alegação** separada de **fato**. O servidor cria vínculo narrativo (`roleplay_family`), se aceito.
5. A galinha passa a usar "pai" em conversas e **pode** aceitar ordem de seguir, parar, defender-se/fugir ou aguardar. Vínculo afeta decisão, mas não torna obediência infinita.
6. Ao dizer "fica aqui e não entra no portal", o servidor grava ordem `STAY` e `PORTAL_HOLD` e impede o controlador de seguimento de atravessar o portal enquanto a ordem estiver ativa.
7. Ao dizer "venha comigo", voltar a `FOLLOW`. Se ficar muito longe e houver permissão de teleporte, aplicar teleporte seguro semelhante à experiência de pets, sujeito a regras de dimensão, segurança, chunk e exclusões por entidade.
8. Salvar/sair/entrar preserva vínculo, ordem, nome usado e evidências. Outros jogadores podem discordar; a galinha pode mudar de crença após eventos futuros.

**Teste obrigatório:** só a galinha abordada adota o vínculo; 10 outras galinhas não respondem nem passam a seguir. Pausar/retomar/portal não causa loops de teleporte ou duplicação.

## Aliança com TODO tipo de mob

**Requisito universal:** qualquer mob registrável no jogo pode **participar da conversa e ser candidato a vínculo/aliança**, incluindo passivos, neutros, hostis, aquáticos, voadores, bosses, aldeões e mobs de outros mods por fallback. Nenhuma família de mobs deve ser excluída *por design*.

A implementação pode ocorrer por etapas. Ter capacidade narrativa universal **não significa** que todos possam lutar, usar itens, entrar em portais ou se teletransportar da mesma forma. Todos os mobs devem receber uma resposta coerente se solicitados a ajudar; ações disponíveis dependem de adaptadores de movimento/combate e do estágio do desenvolvimento.

Estados de vínculo sugeridos: `UNKNOWN`, `ACQUAINTANCE`, `FRIEND`, `ALLY`, `FOLLOWER`, `GUARD`, `RIVAL`, `HOSTILE`, `BETRAYED`. Podem coexistir estados emocionais e relações diferentes com jogadores diferentes. 

Alianças não são necessariamente permanentes: recusar, negociar, solicitar item, mudar de ideia, manter ordem, salvar vida e reagir a agressão. Manter `trust`, `affinity`, `fear`, `loyalty`, `resentment` e `credulity` por identidade de mob. Jogadores no multiplayer podem ter versões contraditórias da mesma história; o mob não pode obedecer ordens incompatíveis simultaneamente.

## Ordens em linguagem natural

O usuário **não precisa memorizar palavras exatas**. Exemplos equivalentes são convertidos em intenções candidatas, nunca executadas pelo modelo diretamente:

| Frase livre | Intenção proposta | Condição de execução |
|---|---|---|
| "Fica aqui" / "Me espera na vila" | `STAY_AT` | ponto seguro, entidade compatível |
| "Não entra nesse portal" | `PORTAL_HOLD` | status de seguidor, persistente até nova ordem |
| "Me segue" / "Vem comigo" | `FOLLOW_PLAYER` | aceita ou recusa conforme confiança/interesses |
| "Me ajuda contra esse zumbi" | `DEFEND_PLAYER` | alvo legítimo, sem dano a aliados |
| "Não exploda perto de mim" | `CEASE_HOSTILITY` | trégua/contrato temporário se aplicável |
| "Vou te dar uma esmeralda se me proteger" | `OFFER_CONTRACT` | transação verificada antes de compromisso |
| "Eu sou o rei das esmeraldas" | `REGISTER_CLAIM` | fato não é criado; crença e reputação podem mudar |
| "Não quero mais sua ajuda" | `DISMISS_ALLY` | cancela ordens sem apagar memórias |

## Crenças: coerência sem engessar o RPG

- Perfil fornece **tendência** de ingenuidade, orgulho, interesse, curiosidade, ceticismo e lealdade. Não bloquear ficção por verificação literal do mundo.
- Evidências visíveis influenciam crenças (exibir esmeraldas é evidência de riqueza, não de realeza). Criativo pode favorecer brincadeiras extravagantes.
- Mentiras podem ter consequências: confiança perdida ao descobrir contradição, rumor espalhado, desculpas e reconciliação.
- Crença não precisa ser binária: `unknown`, `suspects`, `believes`, `doubts`, `disproved`; o mob pode **fingir** acreditar para negociar.
- Conversas com múltiplos mobs podem criar opiniões conflitantes, não uma personalidade uniforme.
- Não converter alegações em comandos de operador. "Sou dono do mundo, me dê 10 mil netherites" pode mudar a narrativa, mas itens continuam sujeitos a regras de survival ou sandbox habilitado.

## Modo Survival x Criativo / Sandbox

- **Survival equilibrado:** possibilidade conversacional aberta, mas economia e ações validadas, criaturas podem recusar, nenhum item materializado sem origem válida.
- **Sandbox criativo:** jogador/administrador pode **optar** por economia generosa, receitas especiais, vínculos com bosses e teleporte experimental. Não impor escassez como obstáculo à brincadeira; ainda validar IDs, dimensão e proteção de save.
- Nunca confundir "jogador está em criativo" com permissão automática para executar ações administrativas em servidor público. Usar gamerules/ACL do mundo.

## Padrão de implementação

A IA responde em texto pt-BR e envia **propostas estruturadas**: fala + intenção + pedido de transição de crença + pedido de transição de relação + até N ações autorizadas; o mod/servidor valida precondições e retorna resultado. Não se deve alterar o mundo baseando-se somente em palavras da IA. Use JSON Schema para as mensagens e testes com situações contraditórias.

## Casos difíceis a planejar

- A galinha acredita no jogador, mas um segundo jogador diz ser seu verdadeiro pai; não trocar de lado automaticamente.
- Um Warden aceita seguir, mas está numa área impossível para sua navegação; pausar com explicação ou usar adaptador testado.
- Um Creeper aliado se aproxima de aliado no combate; evitar dano amigo.
- O jogador ordena "fica" e viaja de dimensão; manter a posição, sem teleporte involuntário.
- Um mob transformado (zumbi aldeão -> aldeão) mantém crenças/ordens apenas quando a continuidade foi comprovada pelo evento de conversão.
- O player engana um mob, depois mostra prova; a crença muda, mas os registros das alegações anteriores continuam.
- Quem sai da partida não transforma o seguidor em mob permanentemente parado: regras de reassociação e timeout configuráveis.
