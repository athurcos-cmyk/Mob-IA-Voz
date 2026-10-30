# 08 — Como usar Codex e Claude/Claude Code juntos

O usuário mencionou “Codex e Cloud”; se “Cloud” significar Claude ou Claude Code, este fluxo evita conflitos. Se referir-se a outra ferramenta, a regra é a mesma: **um agente escreve e o outro revisa**, não dois agentes alterando os mesmos arquivos ao mesmo tempo.

## Papel de cada ferramenta

- **Codex:** implementa um **marco pequeno**, roda comandos, testa/build e relata mudanças/limitações.
- **Claude/segundo agente:** lê diff, `AGENTS.md`, contrato e critérios; realiza **code review**, lista bugs, riscos, APIs inexistentes, casos de teste faltantes. **Não modifica os mesmos arquivos** durante revisão.
- **Você:** valida no Minecraft, confirma a experiência visual e por voz, decide se passa ao próximo marco.

## Ciclo ideal

1. Mantenha uma pasta Git local. Não é necessário publicar repositório ou expor perfil.
2. Para cada fase, use branch ou commit separado; copie prompt de `10_COMECAR_AGORA.md` (fase 0) ou `05_ROADMAP.md`.
3. Codex implementa. Exija `gradlew.bat build`, testes e resumo honesto do que foi executado.
4. Envie ao segundo agente o diff e peça revisão dos requisitos **da fase**, não refatoração livre de tudo.
5. Codex corrige **somente** problemas confirmados; recompila/testa.
6. Você testa no jogo, compara com `06_TESTES_ACEITACAO.md` e registra resultado.
7. Faça commit local e só então escolha a próxima fase.

## Prompt de revisão (para segundo agente)

```text
Atue como revisor técnico, não como implementador. Leia AGENTS.md, docs/04_ARQUITETURA.md,
docs/05_ROADMAP.md e docs/06_TESTES_ACEITACAO.md. Revise somente o diff da fase atual.
Procure falhas reais, incompatibilidades de API do Minecraft Java 1.21.8/Fabric,
Java 21, problemas de thread, duplicação de itens, validação servidor/cliente,
privacidade de áudio, latência e lacunas de testes. Priorize achados por severidade,
com arquivo/linha e forma reproduzível de comprovar. Não invente problemas, não
modifique arquivos e não presuma que um build passou sem log.
```

## Prompt de correção (para Codex)

```text
Leia AGENTS.md e o relatório de revisão. Corrija somente bugs confirmados
relacionados ao marco atual. Para cada correção informe causa, mudança e teste
que falhava antes e passa agora. Não avance etapas. Execute build e testes
possíveis e informe limitações ambientais.
```

## Fluxo anti-erro

- Se um agente citar comando ou evento inexistente, exigir documentação oficial ou localizar no código da dependência.
- Se o código não compilar, não adicionar novos mobs.
- Se Ollama responder em inglês, corrigir prompt/validação do sidecar antes de criar perfis de 60 espécies.
- Se áudio funcionar só no chat, não declarar MVP pronto.
- Se transação funcionar só em Criativo, testar duplicação e reentrada antes de Survival.
- Se o usuário não tiver PC suficientemente forte, reduzir modelo e frequência, não dizer que “100 mobs com IA em tempo real” serão instantâneos.

## Mudanças de requisitos — obrigatório antes da fase seguinte

Ao receber nova ideia, modificar docs, JSON e testes **antes de pedir código**. Codex deve começar lendo `docs/12_LIBERDADE_CRENÇAS_ALIANCAS.md`; Claude pode revisar as mudanças no contrato de crença e na política de alianças. Não iniciar todo o sistema social antecipadamente.
