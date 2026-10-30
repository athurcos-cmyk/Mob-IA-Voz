# 10 — Começar agora: guia rápido e primeiro prompt

## Preparação no Windows

1. Descompacte o ZIP em uma pasta, por exemplo `C:\Projetos\MobVozIA`, sem apagar arquivos de documentação.
2. Confirme que o projeto de Minecraft de experimentos anteriores (CreaturePals e CognitoCraft) está em **perfis separados**. Não misture instalações Fabric/NeoForge.
3. Instale/verifique **JDK 21** (`java -version`). O Codex deverá confirmar o JDK no ambiente de build.
4. Já existe Ollama funcional com `llama3.2` no computador, conforme teste anterior. **Não é preciso reinstalar** para iniciar a fase 0.
5. Abra no Codex a pasta extraída (pasta raiz que contém `AGENTS.md`).
6. Cole o prompt abaixo. A primeira tarefa é uma fundação que **compila**, não a implementação completa.

## Prompt exato para a primeira tarefa do Codex

```text
Você é o engenheiro responsável pelo desenvolvimento do mod MobVoz IA.

Primeiro leia integralmente AGENTS.md e os arquivos README.md,
docs/01_VISAO_REQUISITOS.md,
docs/02_MECANICAS_ECONOMIA.md,
docs/03_CATALOGO_MOBS.md,
docs/04_ARQUITETURA.md,
docs/05_ROADMAP.md,
docs/06_TESTES_ACEITACAO.md,
docs/07_MULTIPLAYER_PERFORMANCE.md,
docs/09_FONTES_DECISOES.md e docs/10_COMECAR_AGORA.md.

Produto final desejado: Minecraft Java 1.21.8 com Fabric e Java 21. Mobs
selecionados pela mira conversam por microfone e respondem por voz em pt-BR
por meio de Simple Voice Chat + IA local Ollama + STT + TTS. Terão personalidade,
memória, ações reais, troca de itens no chão, relações, conversas entre mobs e,
mais tarde, multiplayer. O LLM nunca executará ações no mundo diretamente.

NESTA TAREFA faça SOMENTE a FASE 0 do docs/05_ROADMAP.md:
1) Consulte documentação oficial e confirme versões compatíveis exatas.
2) Crie um projeto mínimo Fabric 1.21.8 em `mod-fabric/`, modid `mobvoz`, Java 21.
3) Configure Gradle wrapper, arquivos essenciais e entrypoint, sem criar gameplay ainda.
4) Adicione README de build e teste no Windows, com perfil de jogo isolado.
5) Execute o build/testes disponíveis e reporte saída/erros reais.
6) Informe arquivos criados, decisões de dependência e próximo teste manual.
7) PARE aqui. Não implemente voz, IA, estrelas, trocas, quests nem multiplayer.

Restrições: não invente classes, versões, comandos ou resultados. Não publique
GitHub/repositório remoto. Não crie API paga. Não copie código de mods de terceiros.
Não altere configurações globais do meu Minecraft sem autorização.
```

## Depois da fase 0

- Abra `mod-fabric/` e execute `gradlew.bat build` no Windows.
- Teste o perfil isolado Java/Fabric no Launcher e confirme que o mod está presente.
- Copie o log de erro caso não compile, **não avance** para a estrela antes de corrigir.
- Para a fase 1, use o objetivo e critérios da linha correspondente em `docs/05_ROADMAP.md`. Peça ao Codex um plano curto de implementação e teste daquela fase antes do código.

## Prompt genérico de próxima fase

```text
Leia AGENTS.md e todo o contexto relevante em docs/. Verifique o estado atual
do repositório e qual foi a última fase validada. Implemente EXCLUSIVAMENTE a
fase N de docs/05_ROADMAP.md e cumpra os testes pertinentes de
`docs/06_TESTES_ACEITACAO.md`. Não avance para fases seguintes. Prefira
alterações pequenas, explique API reais/assinaturas usadas, compile, execute
testes possíveis e reporte o que NÃO foi verificado no jogo.
```

## Se quiser usar Claude para revisar

Enviar diff + prompt específico de `docs/08_USAR_CODEX_CLAUDE.md`; Claude revisa, Codex corrige, você testa. Não deixe os dois modificarem os mesmos arquivos em paralelo.

## Aviso de expectativa

Os documentos descrevem um projeto ambicioso. **Não garantem** que todos os bosses ou conversas espontâneas funcionem na primeira versão. O marco de qualidade inicial é apenas uma vaca respondendo por voz em português; o marco seguinte é uma transação real segura.

## Atualização v2.1

Leia também `docs/12_LIBERDADE_CRENÇAS_ALIANCAS.md`, `docs/13_IDEIAS_DIFERENCIAIS.md` e `docs/14_COMO_USAR_O_PACOTE.md`. Esses documentos **não autorizam** implementar alianças na fase 0. A fase 0 continua sendo **somente a base compilável**.
