# MobVoz IA — guia de instalação e uso (Windows)

Criaturas do Minecraft que **ouvem você pelo microfone e respondem por voz em português**, com memória, ordens reais, trocas físicas e alianças. Tudo roda **localmente e de graça** (Ollama + Whisper + Piper). Alvo: **Minecraft Java 1.21.8, Fabric, Java 21**.

> Estado honesto: todas as fases foram implementadas e testadas **de forma automatizada em jogo real** (com fala sintética no lugar do microfone). **Falta o seu teste com microfone e ouvidos de verdade** — veja "Primeiro teste" abaixo e reporte o que falhar.

## Requisitos

- Windows 10/11, ~16 GB de RAM (testado com 16 GB e GTX 1660 SUPER 6 GB; o STT roda na CPU, o LLM na GPU).
- JDK 21 (o repositório traz `.tools/jdk-21...`; ou instale o seu e defina `JAVA_HOME`).
- [Ollama](https://ollama.com) com o modelo: `ollama pull qwen3:4b` (alternativa mais leve: `llama3.2`).
- Python 3.12 a 3.14 (testado com 3.14).
- Perfil de jogo **separado** (não use seu `.minecraft` principal).

## Instalação

1. **Sidecar de IA** (uma vez; cria o ambiente Python, instala as bibliotecas e baixa as vozes e o modelo de fala, ~1,2 GB):
   ```powershell
   cd ai-service
   powershell -ExecutionPolicy Bypass -File .\instalar-sidecar.ps1          # completo (voz Kokoro + Piper)
   powershell -ExecutionPolicy Bypass -File .\instalar-sidecar.ps1 -Leve    # PC fraco: so a voz Piper
   ```
   Atalho sem digitar nada: dê dois cliques em `ai-service\instalar-sidecar.bat` (e em `configurar-api.bat`). Funciona em qualquer pasta, inclusive `Downloads\MobVozIA-0.1.0 (2)`.
   (`piper-tts` e o espeak-ng do Kokoro são GPL: ficam só no seu computador, rodando como processo separado — veja `THIRD_PARTY.md`.)
   **IA que conversa:** a sua API (`configurar-api.ps1` guarda a chave em `%USERPROFILE%\.mobvoz\api.json`; ligue com `run-sidecar-api.bat`) **ou** o modelo local (`ollama pull qwen3:4b`; ligue com `run-sidecar.bat`).
   O passo a passo ilustrado, para um PC do zero, está na página do projeto (`site/`, ver `docs/20_PAGINA_E_REPOSITORIO.md`).
2. **Mod no jogo** — escolha um:
   - *Desenvolvimento/teste rápido:* `cd mod-fabric` e `.\run-client.bat` (usa a pasta isolada `mod-fabric/run/` e baixa o Simple Voice Chat 2.6.27 com hash verificado).
   - *Launcher oficial:* crie uma instalação Fabric 1.21.8 com **pasta de jogo exclusiva**; coloque em `mods/`: `mobvoz-0.1.0.jar` (`mod-fabric/build/libs/` após `.\build.bat`), o **Fabric API 0.136.1+1.21.8** e o **Simple Voice Chat** para 1.21.8 (baixe você mesmo em modrinth.com — a licença dele não permite redistribuição).

## Uso

1. Inicie o sidecar: `ai-service\run-sidecar.bat` (escuta só em `127.0.0.1:8765`). A primeira fala demora ~10–15 s (carrega modelos).
2. Abra o jogo. No Simple Voice Chat (tecla **V**): escolha o microfone e use **ativação por voz** (se usar "aperte para falar", só ouvirá enquanto a tecla estiver pressionada).
3. Digite **uma vez** `/mobvoz voz sim` (consentimento; `nao` desliga). Sem isso nada do seu microfone é usado.
4. **Mire numa criatura** (até 6 blocos): aparece uma estrelinha sobre **uma só**. Fale normalmente:
   - conversa: "O que você gosta de comer?", "Eu sou seu pai!" (vira *alegação*, não fato — ela pode acreditar ou duvidar);
   - ordens: "Me segue", "Fica aqui e não entra no portal", "Não quero mais sua ajuda", "Não explode perto de mim", "Me ajuda contra esse zumbi";
   - troca: "Quer trocar cenouras comigo?" → jogue as cenouras no chão perto dela; ela anda até elas e devolve a recompensa;
   - negociar de verdade: *"Ei esqueleto Wither, preciso de 3 cabeças e te dou 3 ferros e 1 picareta de diamante"* — ele diz o que interessa e o que não; você pode **convencê-lo** com um bom motivo (efeito limitado). Detalhes: `docs/15_NEGOCIACAO_E_TROCAS.md`;
   - **presente:** segure um item e use **SHIFT + botão direito** na criatura.
5. **Sem microfone? Modo chat:** digite `/mobvoz chat sim`. Com uma criatura na mira (estrela), o que você escrever no chat vai **só para ela** (não aparece para os outros jogadores) e a resposta chega como **legenda no chat** (e em voz, se o Simple Voice Chat/Piper estiverem funcionando). Sem criatura na mira o chat é normal. Também há `/mobvoz falar <texto>` (uma mensagem, qualquer modo) e `/mobvoz legenda sim|nao` (legendas também para quem usa voz). `/mobvoz chat nao` desliga.
6. `/mobvoz status` mostra o sidecar, o servidor e as métricas (latências). `/mobvoz diag falar <texto>` testa a IA por texto (só diagnóstico).

## Servidor / multiplayer

- Cada jogador precisa do mod + Simple Voice Chat e do **seu próprio sidecar local** (a transcrição acontece no computador de quem fala; o servidor recebe só o texto e o alvo). O servidor precisa do mod, do Simple Voice Chat e do sidecar (Ollama + TTS) na máquina do host.
- O servidor **ignora** quem não deu consentimento (`/mobvoz voz sim`), mesmo com pacotes forjados.
- Configuração do mundo: `config/mobvoz/server.json` — `preset` (`low|balanced|high`), `maxActiveFollowersPerPlayer` (padrão 15, máximo 32; os presets não o reduzem), `requireVoiceConsent`, `voiceEngine` (`auto`|`kokoro`|`piper`: **Kokoro** é a voz principal, mais natural e mais lenta de gerar; **Piper** é o modo leve), `chatterEnabled`, `ambientChatter` (conversas ambiente entre criaturas, padrão ligado; precisa de `chatterEnabled`) e `ambientCooldownSeconds` (padrão 60), e as travas **desligadas por padrão** `bossContractsEnabled` (liga pactos com Warden/Wither/Dragão) e `bossDestructionOnAlliance` (chefe aliado ou em trégua não quebra blocos enquanto estiver `false`) (um `server.json` antigo com `rareRecipes` ainda carrega; a chave é ignorada).
- Nunca exponha o sidecar/Ollama na internet (a API só escuta em localhost; `MOBVOZ_TOKEN` exige um token se você precisar mudar isso).

## Solução de problemas

| Sintoma | O que fazer |
|---|---|
| HUD mostra "Sem resposta (IA offline?)" | o sidecar/Ollama não está rodando: `/mobvoz status`; o jogo continua normal |
| Estrela não aparece | mire mais perto (≤ 6 blocos) e sem bloco no meio; fechar telas abertas |
| Nada é ouvido ("Voz ativa" parada) | SVC com push-to-talk? use ativação por voz; confira o microfone no SVC (V) |
| "Não entendi a fala" | fale mais perto do microfone; o STT padrão é `small` (CPU); veja `MOBVOZ_STT_MODEL` |
| Muito lento | `MOBVOZ_MODEL=llama3.2`, preset `low`, feche outros programas de GPU |
| Criatura não obedece | é de propósito: confiança, espécie e adaptador decidem; presenteie e converse mais |

## Privacidade

Nenhum áudio ou texto de jogador é gravado em disco ou em log (só números de latência). O áudio vai apenas ao sidecar em `127.0.0.1`. Memória das criaturas fica no próprio save do mundo.

## Empacotar uma versão

`.\empacotar.ps1` gera `dist\MobVozIA-0.1.0.zip` (JAR do mod, sidecar sem venv/modelos, docs e licenças).

## IA própria (opcional): usar uma API em vez do modelo local

Por padrão tudo roda na sua máquina (Ollama). Se você preferir uma API (testada com a **DeepSeek**, modelo `deepseek-flash`; serve qualquer API compatível com OpenAI), cada pessoa usa a **própria chave**:

1. Crie uma conta e uma chave no site do provedor (a chave é sua; **nunca** a coloque no mod, no zip ou num repositório).
2. Guarde a chave em um destes lugares: a variável de ambiente `MOBVOZ_API_KEY`, ou o arquivo `%USERPROFILE%\.mobvoz\api.json` com `{"key": "sk-..."}`.
3. Ligue o modo antes de iniciar o sidecar: `MOBVOZ_PROVIDER=api` (opcional: `MOBVOZ_API_URL`, padrão `https://api.deepseek.com`; `MOBVOZ_API_MODEL`, padrão `deepseek-flash`).

**Privacidade:** nesse modo o **texto** da conversa e o contexto da criatura vão para o servidor do provedor (a política da DeepSeek diz guardar dados na China e fontes divergem sobre uso para treino). O **áudio** continua sendo transcrito localmente. Em multiplayer, quem usa a API é quem hospeda o mundo: os outros jogadores falam com a IA do anfitrião. O custo é por uso (fração de centavo por fala), cobrado na conta do dono da chave. Para voltar ao local, remova `MOBVOZ_PROVIDER`.
