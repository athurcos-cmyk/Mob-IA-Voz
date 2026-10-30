# 01 — Visão do produto e requisitos

## Princípio norteador

O jogador deve sentir que interage com personagens com interesses próprios, e não com um chatbot colado em cada criatura. A “consciência” é uma **simulação narrativa/comportamental**, com limites claros de processamento e validação.

### Regras de experiência

- **Voz, não chat:** jogador olha para um mob, fica no alcance e fala naturalmente; o mob responde de forma audível em português brasileiro. Sem chat escrito nem clique obrigatório. Há **modo diagnóstico de texto** apenas para desenvolvedores.
- **Alvo individual:** pequeno indicador visual (estrela), no máximo um alvo por jogador por enunciado. Seleção por mira/visibilidade/distância (padrão experimental: 6 blocos); opção de foco temporário após primeira fala.
- **Conversas naturais:** respostas curtas/expressivas e contextualizadas. Mob pode pedir esclarecimento, recusar, negociar, lembrar, mudar de ideia.
- **Identidade:** cada entidade possui um identificador persistente e perfil inicial derivado de espécie, profissão, local, história e traços. Variação por indivíduo sem perder identidade.
- **Vozes diferenciadas:** mapear timbre/pitch/velocidade/voz por espécie e indivíduo, sem comprometer compreensão. Piper pt-BR primeiro, qualidade avaliada com usuários.
- **Ações reais:** respostas podem propor ações, mas **só o motor de jogo** executa comandos autorizados sob validação de contexto e condições.
- **Consequências persistentes:** aceitar/trair acordos, ajudar, agredir, doar, curar, destruir lar ou salvar mobs altera confiança, reputação e memória.
- **Conversas entre criaturas:** proximidade + eventos relevantes podem gerar falas espontâneas entre NPCs (ouvidas perto deles), sem chamar a IA para cada tick.
- **Conflitos e surpresas:** mobs podem mentir, prometer, negociar, trair ou agir por instinto; respeitar restrições e lógica de jogo.
- **Multiplayer:** os diálogos, inventários negociados e ações pertencem a um único estado de mundo; jogadores próximos escutam; relações podem ser por jogador e por facção.
- **Personalização:** configurações de dificuldade, economia, linguagem, frequência de falas, intensidade de personalidade, privacidade, comportamento dos bosses e compatibilidade com survival.

## Prioridades

**P0 / protótipo:** build Fabric 1.21.8; um mob selecionado; microfone; STT pt-BR; LLM local; TTS espacial; sem chat em produção; fala em pt-BR; testes de 10 interações.

**P1 / jogabilidade central:** oferta no chão, coleta deliberada, inventário de transação, contratos e recompensa; relações; regras por mob e profissão; salvar/carregar; ações de seguir/defender/parar ataque; falas pontuais entre mobs.

**P2 / profundidade:** eventos de memória, transformação zumbi→aldeão, membros de facção/mente coletiva, chefes com visão narrativa de histórico, alianças/duplicidade, conversas coordenadas em combate, comportamento emergente controlado.

**P3 / multiplayer:** dois ou mais jogadores, servidor responsável, voz em entidade para todos próximos, negociação atômica, fila de diálogos por mob, sidecar no host, permissões e testes de carga.

## Casos clássicos inspiradores (requisitos de comportamento, não cópia de código)

1. **Esqueleto Wither:** aceita ouro que foi lançado perto dele, cobra por cabeças de Wither sob limites configuráveis e entrega apenas quando o servidor aprovar o contrato.
2. **Creeper:** pode aceitar pólvora para temporariamente tornar-se não hostil a uma pessoa; mudança expira e pode ser rompida por agressão.
3. **Porco:** resiste a seguir sem um incentivo coerente, aceita cenoura, leva em conta temperamento e situação.
4. **Abelhas:** respondem positivamente a flores, criação de jardim e proteção da colmeia; eventualmente escoltam o jogador por tempo/raio limitado, com política de agressão própria.
5. **Aldeão fazendeiro:** interessa-se por feno, trigo e segurança da plantação; ferreiro prioriza ferro, carvão, ferramentas; ofertas negociadas podem alterar temporariamente preços dentro de tetos definidos.
6. **Zumbi aldeão curado:** lembra eventos-chave da fase anterior após o vínculo de identidade persistente entre entidades antigas e novas.
7. **Warden aliado:** sob condições excepcionalmente difíceis deixa de perseguir o jogador, segue e ajuda em combate sem ignorar totalmente sentidos/limitações.
8. **Wither:** pode suspender a hostilidade diante de um acordo, lutar como aliado dentro de regras de segurança e eventualmente traí-lo conforme motivação/probabilidade e eventos.
9. **Ender Dragon:** narra eventos importantes ocorridos antes, como se tivesse uma visão mítica do mundo; a informação vem do registro de eventos do servidor, não de onisciência ilimitada da IA.
10. **Endermen:** compartilham memórias resumidas como facção e podem alterar comportamento coletivo após eventos significativos.
11. **Mobs entre si:** zumbis criam plano simples de flanquear, esqueleto comenta tiros errados; diálogos são curtos, espaçados, coerentes e podem disparar ordens AI validadas.

## Regras de qualidade

- Nenhuma resposta em inglês em diálogo pt-BR, salvo nome de item próprio (ex.: Nether); validação automática e tentativa limitada de corrigir.
- Nada de expressões padronizadas sem cabimento (“bruh”, “fam”) aplicadas a todas as espécies.
- Uma única transcrição/solicitação por fala; um único alvo, salvo ordem explícita de fala em grupo.
- Não criar recursos, matar entidades, teletransportar, alterar clima ou trocar de dimensão por texto não validado.
- NPCs não precisam “ganhar” todas as negociações: negativa, silêncio, desconfiança e limitações também fazem parte da fantasia.
- Sem back-end em nuvem obrigatório nem chave paga.
- Se serviço de IA falhar, Minecraft permanece jogável; o mod exibe status discreto e não congela o tick.
- Alcance da escuta limitado e configurável; moradores conversam entre si apenas se atividade habilitada.

## Acessibilidade e segurança de experiência

- Legendas opcionais (não obrigatórias para interagir); volume separado para falas dos mobs.
- Opção de ativação por voz e de pressionar tecla para falar; modo de tecla não deve ser obrigatório por padrão.
- Opt-in claro para escuta do microfone, política de dados e bloqueio de áudios de terceiros no multiplayer sem permissão.
- Quadro de status: ouvindo, pensando, falando, acordo pendente, erro de IA.
- Recurso para silenciar mobs, diminuir interrupções em combate e desativar conversas espontâneas.

## Adendo v2.1 — liberdade narrativa e vínculos universais

O jogador pode dizer **qualquer coisa** aos mobs, inclusive mentiras e histórias absurdas. O jogo separa fato realmente verificado, alegação verbal e crença individual. Mobs podem acreditar em mentiras com resultados narrativos reais (por exemplo, galinha passar a chamar o jogador de pai e aceitar segui-lo). Todos os mobs são candidatos a alianças; a variedade de ações concretas dependerá dos controladores implementados. Ver `12_LIBERDADE_CRENÇAS_ALIANCAS.md` e `13_IDEIAS_DIFERENCIAIS.md`.
