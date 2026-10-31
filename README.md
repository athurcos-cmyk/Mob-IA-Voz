# MobVoz IA 2.1 — Minecraft Java com mobs sociais, voz e ações

**Status:** fase 0 compilada em `mod-fabric/`; as mecânicas descritas abaixo continuam como especificação.  
**Data-base:** 2026-10-30.  
**Alvo fixado:** Minecraft **Java 1.21.8**, **Fabric**, **JDK 21**.  
**Licenciamento do projeto:** a definir antes da publicação; preferir licença permissiva para código próprio, revisar licenças de dependências e modelos separadamente.

## Objetivo

Criaturas conversam **em português brasileiro por voz**, reconhecem o jogador naturalmente, possuem identidade/voz/interesses/memória, conversam entre si e podem executar **ações verdadeiras no mundo**: negociar itens, recolher ofertas, oferecer recompensas, seguir, defender, recusar, trair, contar boatos e formar alianças. O comportamento do jogo é controlado por regras e um servidor autoritativo, nunca por instruções executadas diretamente da IA.

**Crítico:** a IA deve ser **local/gratuita**, sem cobrança por API e sem tornar um serviço externo obrigatório. Já existe Ollama no computador do usuário. O processamento tem custo de hardware/energia; memória, latência e qualidade serão medidos.

## Não repetir os erros dos mods anteriores

- Não obrigar a digitar ou clicar em um balão sobre a cabeça de cada mob.
- Não responder em inglês nem utilizar gírias incoerentes com a personalidade.
- Não ativar todos os mobs próximos quando a intenção era falar com apenas um.
- Não criar infinitos itens por mera decisão textual de um LLM.
- Não encher o mundo de áudio ou requisições de IA incessantes.
- Não afirmar que a criatura executou uma ação antes de o servidor confirmá-la.

## Leia nesta ordem

1. `docs/01_VISAO_REQUISITOS.md` — escopo, experiência e requisitos funcionais.
2. `docs/02_MECANICAS_ECONOMIA.md` — ações, negociação, confiança, alianças, traição e governança.
3. `docs/03_CATALOGO_MOBS.md` — perfis de mob, profissões, memória coletiva, bosses e ideias extras.
4. `docs/04_ARQUITETURA.md` — sistemas, fluxo de dados, salvamento, áudio, servidor e IA.
5. `docs/05_ROADMAP.md` — marcos incrementais com dependências e definição de pronto.
6. `docs/06_TESTES_ACEITACAO.md` — cenários reproduzíveis para testar com Minecraft.
7. `docs/07_MULTIPLAYER_PERFORMANCE.md` — arquitetura multiplayer, privacidade, cargas e degradação.
8. `docs/08_USAR_CODEX_CLAUDE.md` — como orientar os agentes sem gerar mudanças conflitantes.
9. `docs/09_FONTES_DECISOES.md` — links oficiais e decisões ainda a validar.
10. `docs/10_COMECAR_AGORA.md` — primeiro prompt executável.
11. `AGENTS.md` — instruções obrigatórias para agentes de programação.
12. `docs/11_PROMPTS_FASE_A_FASE.md` — prompt pronto para cada etapa posterior.
13. `config/` — contratos de ação, perfis e equilíbrio propostos (não são implementação funcional).

## Escopo de entrega

**MVP-voz:** UMA vaca que ouve o jogador sem chat, responde por voz em pt-BR, tem memória básica e não aciona outras vacas.

**MVP-jogável:** um mob negociador recolhe um item válido e entrega recompensa realmente registrada, com diálogo e áudio.

**Alfa social:** várias espécies, profissões, acordos, reputação e conversas espontâneas entre mobs, com orçamento de IA e distância.

**Beta:** bosses, transformação zumbi-aldeão, memória coletiva, contratos, traição e mundo persistente.

**Coop:** servidor dedicado/LAN, estado compartilhado, voz espacial e gestão de concorrência/recursos.

Este pacote contém a base Fabric da fase 0 em `mod-fabric/`. Ela inicia no cliente de desenvolvimento, mas ainda **não implementa** as mecânicas do produto. As instruções de build e teste isolado estão em `mod-fabric/README.md`.

## Novidades v2.1: o jogador pode mentir, convencer e criar vínculos

- **Aliança negociável com qualquer mob**, não só chefes. Ações executáveis variam por adaptação de navegação/combate e pela fase implementada, mas **conversa e possibilidade de vínculo são universais**.
- Declarações como "sou seu pai" ou "sou rei das esmeraldas" podem se tornar **crenças de personagens**, mas não fatos falsos do servidor. Mobs podem acreditar, desconfiar, fingir acreditar ou confrontar depois.
- Vinculações como galinha chamar jogador de "pai", segui-lo, aguardar e respeitar ordem de não entrar em portal fazem parte dos requisitos, com teste de persistência.
- Adicionamos `docs/12_LIBERDADE_CRENÇAS_ALIANCAS.md`, `docs/13_IDEIAS_DIFERENCIAIS.md`, `docs/14_COMO_USAR_O_PACOTE.md` e um exemplo `config/social_state.sample.json`.
- **Estado real do projeto:** `STATUS.md` (fases, como retomar, limites), `CHANGELOG.md`, `INSTALL.md`. Documentos do que foi construído depois do roadmap: `docs/15_NEGOCIACAO_E_TROCAS.md`, `docs/16_CONSCIENCIA_E_REACOES.md`, `docs/17_INTERACOES_NATURAIS.md`, `docs/18_BATERIA_DE_CONVERSACAO.md` e `tools/gen/README.md` (geradores de dados).
- **Leia `docs/14_COMO_USAR_O_PACOTE.md` primeiro** se não souber como usar os arquivos com Codex ou Claude.
