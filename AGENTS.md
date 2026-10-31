# AGENTS.md — regras permanentes para Codex e demais agentes

Leia `README.md` e os documentos em `docs/` antes de implementar. Esta raiz contém **especificação, não código pronto**. Não tratar ideias de fases futuras como código funcional.

1. **Alvo fixo:** Minecraft Java 1.21.8, Fabric, JDK 21. Usar bibliotecas/versionamentos reais e compatíveis. Confirmar fontes oficiais antes de adicionar dependência.
2. **Uma fase por tarefa:** siga `docs/05_ROADMAP.md`. Pare ao fim, reporte arquivos, comandos e testes; nunca avançar automaticamente.
3. **Compile/teste:** execute o build e os testes possíveis; seja explícito sobre tarefas não executadas ou impossíveis no ambiente.
4. **LLM não controla o jogo:** nunca `exec`, console, comando `give`, método Java ou SQL arbitrário baseado em texto do modelo. Intenções são **JSON estrito** validado, com allowlist, autorizações e regras de economia no servidor.
5. **Integridade:** transações idempotentes com `transactionId`, rollback, reserva de itens e logs. Sem dupes nem itens grátis arbitrários em survival.
6. **Threading:** operações de I/O/IA/TTS/STT em threads/executores dedicados, alterações world no thread oficial de servidor. Nenhuma consulta a Ollama em tick do jogo.
7. **Sem voz espúria:** até haver consentimento explícito do usuário, não capturar áudio para nosso serviço. Não registrar áudio ou transcrição em logs por padrão.
8. **Fala:** pt-BR audível e espacial, sem digitar/clicar na experiência final, **um alvo por enunciado**. Chat de texto apenas como ferramenta de diagnóstico, não como substituto.
9. **Comportamento:** espécies têm interesses plausíveis, individualidade, memória real; LLM apenas sugere efeitos autorizados. Bosses e atravessar dimensão exigem testes próprios.
10. **Eficiência:** limitadores para diálogos NPC-NPC, filas, timers, cache, cancelamento e sobrecarga; usar modelo local gratuito por padrão.
11. **Segurança:** sidecar e Ollama ligados somente a localhost no início, sem portas externas e sem credenciais reais em repositório. Multiplayer exige autenticação/limites de acesso e política de voz.
12. **Licenças:** não copie código/assets de mods concorrentes ou o mod não publicado de criadores. Verifique licença de modelos de TTS/LLM antes de distribuir.
13. **Compatibilidade:** isole os perfis; não mova arquivos para `.minecraft/mods` global sem autorização.
14. **Qualidade:** mudanças pequenas, testes reproduzíveis, diagnóstico de falhas antes de novos recursos, commits locais. **Publicação:** só com autorização do dono; em 2026-10-31 ele autorizou o repositório `github.com/athurcos-cmyk/Mob-IA-Voz` (primeiro privado; depois ele o torna público) (nunca publicar chaves, o jar do Simple Voice Chat, modelos nem o `.venv`).

## Relatório obrigatório após tarefa

- Fase executada / critério de aceitação.
- Arquivos criados/alterados e motivos.
- Dependências/versões verificadas e links consultados.
- Comandos executados com resultado real.
- Testes manuais necessários no Minecraft.
- Limitações e riscos pendentes.
- Indicação clara de que **parou** após a fase.

## Requisitos transversais incorporados na v2.1

15. **Qualquer mob pode ser candidato a vínculo ou aliança**: não codificar regra geral "hostis nunca seguem" ou "apenas bosses se aliam". Adaptações de mobilidade e combate são específicas e desenvolvidas por fases; negociação e vínculo usam modelo geral.
16. **Diferencie fato, alegação e crença**: mentiras de jogadores são permitidas e podem convencer mobs, nunca reescrever fatos verificados no log. `docs/12_LIBERDADE_CRENÇAS_ALIANCAS.md` é normativo.
17. **Ordens persistentes em linguagem natural**: seguir, ficar, esperar, não entrar em portal, dispensar, proteger. Executar somente após validação e persistir estado; evitar teleporte inseguro.
18. **Mundo aberto, executores fechados**: compreender livremente as frases, mas chamar apenas capacidades versionadas e testadas. Não limitar conversa a comandos fixos.
19. **Estado real do projeto**: a base Fabric da fase 0 está em `mod-fabric/`. Arquivos JSON são modelos; não alegar que voz, IA, crenças, alianças ou ações estão funcionais antes de implementar e testar cada fase.
