# 05 — Roadmap de implementação com critérios de saída

**Não pedir o projeto inteiro de uma vez.** Cada fase vira tarefas pequenas; uma etapa termina somente após `build`, testes automatizados e teste manual descrito. O usuário aprova antes de avançar. Não há prazo garantido.

| Fase | Entrega observável | Critério para avançar |
|---|---|---|
| 0 — Fundação | Projeto Fabric 1.21.8, Java 21, Gradle wrapper, estrutura e comando build | `gradlew.bat build` passa; jogo inicia no ambiente do usuário |
| 1 — Alvo e interface | Uma vaca marcada por pequena estrela se jogador mirar até 6 blocos | Com 10 vacas, apenas 1 alvo; estrela some ao desviar |
| 2 — Entrada de voz | API Simple Voice Chat, consentimento, VAD e segmentação | 10 falas detectadas sem chat, sem vazamento/duplicação |
| 3 — Serviço de IA | Sidecar Python + Ollama, API JSON, persona de vaca | 10 respostas pt-BR coerentes; sem ações reais ainda; erro de IA não trava jogo |
| 4 — Fala espacial | STT faster-whisper + Piper pt-BR + entidade SVC | Uma vaca responde por voz; som acompanha posição; ninguém escreve |
| 5 — Memória e crenças | Identidade, memórias, alegações e crenças persistentes | Vaca lembra presente e diferencia afirmação do jogador de fato do mundo após recarregar |
| 6 — Ações e ordens | Executor: seguir, ficar, dispensar, trégua; ordens persistentes | Vaca/galinha seguem ou aguardam; portal não anula ordem; ações só se válidas |
| 7 — Troca real | Item no chão, coleta, contrato, reserva, recompensa | Sucesso e cancelamento não criam/duplicam itens, mesmo reiniciando jogo |
| 8 — Profissões/espécies | 8 mobs + aldeões por profissão, vozes e gostos | Fazendeiro prefere feno; ferreiro não; língua e memória corretas |
| 9 — Conversa mob-mob | Motor de evento, scheduler e orçamento de voz | 2 zumbis conversam brevemente em combate; não vira spam |
| 10 — Alianças universais | Vínculos com qualquer mob, reputações, trégua e defesa por adaptadores | Galinha pode acreditar no jogador, chamar "pai" e segui-lo; Creeper pactuado respeita trégua |
| 11 — Memória avançada | Cura zumbi-aldeão + Endermen + evento histórico do dragão | Testes preservam identidade e respeitam fatos do servidor |
| 12 — Bosses/quests | Warden, Wither, Ender Dragon, lealdade, missões, traição | Boss não destrói mundo sem política, mudanças coerentes e testadas |
| 13 — Multiplayer alpha | Fabric dedicado/LAN, sidecar no host, vozes para próximos | Dois jogadores veem estado igual; transação única; IA sem leak de dados |
| 14 — Otimização/release | Configuração, métricas, pacote de instalação, licença e guia | Teste de carga, crash recovery, versão distribuível sem serviços pagos |

## Fase 0 detalhada — a primeira tarefa para Codex

1. Ler documentação toda, `AGENTS.md` primeiro.
2. Confirmar JDK 21, Fabric 1.21.8, compatibilidade Gradle/Fabric Loom/Fabric API por documentação atual; não inventar número.
3. Criar `mod-fabric/` funcional e perfil dedicado.
4. Mod ID `mobvoz`, nome `MobVoz IA` e entrypoint Java mínimo.
5. Criar script/bat Windows para build e diagnóstico local sem alterar outros perfis.
6. Implementar *testes básicos* ou tarefa Gradle de validação.
7. Executar build onde possível e registrar sucesso/erro reais; se indisponível, deixar instruções reproduzíveis.
8. Parar sem criar mecânica de voz ou ação ainda.

## Fases 1 a 4 — prova principal

**1:** raycast e estrela, única entidade, sem bugs visuais.  
**2:** captura pelo SVC, opt-in e VAD. Evitar gravar o microfone inadvertidamente; teste mic real.  
**3:** sidecar local, Ollama, JSON schema e qualidade em pt-BR; comparação Llama 3.2 vs Qwen3 4B Instruct conforme PC.  
**4:** STT em `pt`, TTS pt_BR, reprodução espacial; sessão volta ao IDLE após interrupção.

### Critério MVP VOZ

Jogador olha para vaca, fala “O que você gosta de comer?”, solta a fala e ouve uma resposta coerente em português que sai **daquela vaca**; outras vacas ficam silenciosas. O jogo permanece fluido e se recupera de queda do sidecar.

## Fases 5 a 8 — valor jogável

**5:** entidade tem identidade estável, confiança e memória verificável.  
**6:** `SPEAK`, `FOLLOW_PLAYER`, `CEASE_HOSTILITY` e `EMOTE` com executor autorizado, sem itens ainda.  
**7:** primeira transação **simples** (porco, cenoura, pequena recompensa permitida) antes de receitas raras. `PICK_UP_OFFERED_ITEM`, `OFFER_TRADE`, `COMPLETE_TRADE`, `RETURN_OFFER`.  
**8:** perfis por profissão/espécie, fallback universal de fala e catálogo de regras/vozes.

### Critério MVP JOGÁVEL

Jogador faz acordo com mob, joga item na frente dele, ele efetivamente se move até o item, pega, aceita/recusa segundo regra, entrega recompensa real e lembra do evento; recarregar o mundo não duplica nem apaga a dívida.

## Fases 9 a 12 — interatividade profunda

**9:** diálogos NPC-NPC com cronograma event-driven; falas sociais audíveis ao jogador, isolamento de alvo e filtro de ruído.  
**10:** relações, facções, trégua e aliados (zumbis, esqueleto, Creeper etc); traição somente motivada.  
**11:** continuidade de identidade na cura de zumbi aldeão; Endermen com memória coletiva; dragão consulta fatos do registro.  
**12:** bosses, missões e ações complexas; priorizar correção do jogo, dificuldade e jogabilidade antes da liberdade narrativa.

## Fases 13 a 14 — cooperação e publicação

**13:** servidor Fabric como autoridade, sidecar no host, cliente com Simple Voice Chat. Em servidor de terceiros confirmar UDP de voz e compatibilidade de versões; nunca hospedar sidecar sem autenticação.  
**14:** guias de instalação, licença clara, logs higienizados, benchmarking e publicação gratuita quando estiver estável.

## Regras de engenharia e checkpoints

- Mudanças pequenas: idealmente um único objetivo de cada vez e commits locais a cada marco estável.
- Proibir salto de fase após erro de compilação ou falha no teste manual.
- Nenhuma feature nova sem teste mínimo e sem mecanismo de desligar.
- Na falha de performance: reduzir frequência de conversas espontâneas e filas antes de reduzir integridade do servidor.
- Se mod não puder jogar por voz: primeiro comprovar captura, transcrição, modelo, TTS e canal individual separadamente; não misturar debugging.
- O arquivo `docs/06_TESTES_ACEITACAO.md` é a fonte de verificação para marcar cada fase como concluída.

## V2.1 — não mover recursos avançados para fase 0

Os requisitos de crença e vínculos valem desde já como arquitetura de longo prazo, mas **não devem ser implementados antes de voz básica funcionar**. Sequência mínima: fase 5 separa alegação de fato; fase 6 introduz `STAY_AT` e `PORTAL_HOLD`; fase 10 generaliza aliança para quaisquer tipos de mobs que possuam adaptadores de movimento/combate. Aliança apenas narrativa para os ainda sem adaptador. Critérios específicos em `docs/06_TESTES_ACEITACAO.md` e `docs/12_LIBERDADE_CRENÇAS_ALIANCAS.md`.
