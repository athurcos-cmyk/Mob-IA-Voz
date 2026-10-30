# 14 — O que está pronto e como entregar ao Codex

## O que este ZIP era antes da fase 0

O pacote original reunia **somente documentação e arquivos JSON de EXEMPLO**. Após a fase 0, `mod-fabric/` contém a base Fabric compilável e o JAR gerado em `mod-fabric/build/libs/`. Ainda não há serviço de áudio ou IA integrada. O nome `config` significa **modelos propostos de configurações**, não um programa pronto. Os arquivos terminados em `.json` são dados estruturados; o Codex ainda precisa escrever o código que os interpreta.

## Preciso mandar todos os arquivos para IA?

**Sim, mas não precisa colar os arquivos no chat.** Extraia a pasta toda (`MobVozIA_2.1/`) no PC. Abra **essa pasta como projeto** no Codex (ou Claude Code). O agente lê `AGENTS.md`, `README.md`, `docs/` e `config/` no diretório local. A pasta `config/` é **referência e contrato proposto**; não colocar esses JSON em `.minecraft/config` até haver código que os suporte.

Se usar uma versão do ChatGPT/Claude somente pelo navegador sem acesso à pasta, envie o ZIP ou os arquivos relevantes e peça ao agente que os leia. Se Codex e Claude compartilharem a mesma pasta local, evite edição simultânea dos mesmos arquivos.

## Passo imediato: fase 0, só estrutura

1. Extraia este ZIP numa pasta de projeto própria, p.ex. `C:\Projetos\MobVozIA`.
2. Abra a pasta no Codex.
3. Peça ao Codex para ler `AGENTS.md`, `README.md`, `docs/05_ROADMAP.md`, `docs/10_COMECAR_AGORA.md`, `docs/12_LIBERDADE_CRENÇAS_ALIANCAS.md`.
4. Copie o prompt **da fase 0** em `docs/10_COMECAR_AGORA.md`.
5. Depois da implementação, faça build (`gradlew.bat build`) no local definido pelo projeto gerado e execute o perfil de desenvolvimento. Confirme o resultado; não alegar que já compilou.
6. Só após a fase 0 ser validada, passe para fase 1 do roteiro.

## O que é cada pasta

- `README.md`: resumo do que será construído.
- `AGENTS.md`: regras permanentes que os agentes seguem.
- `docs/01` a `docs/09`: especificação, mecânicas, arquitetura, fases, testes e fontes.
- `docs/10`: prompt de partida (fase 0).
- `docs/11`: prompts das próximas fases.
- `docs/12`: **novo** — crenças, mentira, liberdade narrativa, alianças com qualquer mob, ordens seguir/ficar/portal.
- `docs/13`: **novo** — ideias originais para desenvolver depois.
- `docs/14`: este guia.
- `config/mob_profiles.sample.json`: exemplos iniciais de gostos/perfis (não catálogo completo).
- `config/balance.sample.json`: valores de exemplo; precisam virar controles reais no mod.
- `config/action_contract.schema.json`: contrato **inicial** de proposta da IA. Deve evoluir por versões para suportar crenças, ordens e relações.
- `config/social_state.sample.json`: exemplo **novo** de dados sociais de uma galinha — sem execução real.
- `mod-fabric/`: base compilável da fase 0; comandos e teste isolado em `mod-fabric/README.md`.

## Prompt da fase 0 — alternativa curta

> Leia `AGENTS.md`, `README.md`, `docs/05_ROADMAP.md`, `docs/10_COMECAR_AGORA.md` e os requisitos novos de `docs/12_LIBERDADE_CRENÇAS_ALIANCAS.md`. Crie somente a estrutura compilável da fase 0 para Minecraft Java 1.21.8, Fabric e Java 21. Verifique versões reais, gere Gradle Wrapper, rode o build quando possível e informe o resultado real. Não implemente voz, IA, crenças ou mobs ainda. Pare após a fase 0.
