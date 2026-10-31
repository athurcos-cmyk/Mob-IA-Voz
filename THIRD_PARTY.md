# Componentes de terceiros e licenças (verificado em 2026-10-30)

O pacote do MobVoz **não inclui** nenhum dos itens abaixo; o usuário os obtém das fontes oficiais.

| Componente | Uso | Licença | Observação |
|---|---|---|---|
| Minecraft Java 1.21.8 | jogo | EULA da Mojang | necessário ao usuário |
| Fabric Loader / Loom / Fabric API | base do mod | Apache-2.0 | dependência de build/execução |
| **Simple Voice Chat 2.6.27** | captura/saída de voz | **All Rights Reserved** (Modrinth) | **não redistribuir o jar**; baixe em modrinth.com/mod/simple-voice-chat. A API (`voicechat-api`) é usada só para compilar |
| Ollama | executa o LLM local | MIT | instalado pelo usuário |
| `qwen3:4b` (padrão) | LLM | Apache-2.0 (Qwen3) | baixado via `ollama pull`; confira a licença do modelo antes de redistribuir |
| `llama3.2` (alternativa) | LLM | Llama 3.2 Community License | idem |
| faster-whisper 1.2.1 / modelo Whisper `small` | STT | MIT | baixado na primeira transcrição |
| **piper-tts 1.8.0** | TTS | **GPL-3.0-or-later** | roda como **processo separado** (sidecar). **Não empacote** o Piper com o mod; instale via `pip` |
| Vozes Piper `pt_BR-faber/cadu/jeff-medium` | TTS (modo leve) | CC0 | `edresson` seria CC BY 4.0 (exige atribuição); não usado |
| **Kokoro-82M** (pesos `kokoro-v1.0.int8.onnx` e `voices-v1.0.bin`, vozes `pf_dora`, `pm_alex`, `pm_santa`) | TTS principal | Apache-2.0 | baixados por `ai-service/baixar_kokoro.py` do release `model-files-v1.0` de thewh1teagle/kokoro-onnx (hash conferido); não ficam no pacote |
| `kokoro-onnx 0.4.7` | roda o Kokoro | MIT | via pip |
| **`phonemizer-fork` e `espeakng-loader` (espeak-ng)** | fonemas do Kokoro (e do Piper) | **GPL-3.0** | vêm junto do `kokoro-onnx`; mesma situação do Piper: ficam só no `.venv` do usuário, no processo separado do sidecar; **não empacote**. Os pesos do Kokoro são Apache-2.0, mas o programa que os roda arrasta GPL, então o Kokoro **não** resolve a questão de licença do Piper |
| numpy / scipy | áudio | BSD | via pip |
| JUnit 5 | testes | EPL-2.0 | só desenvolvimento |

Se for **distribuir** o mod: distribua só o JAR do MobVoz (MIT) + `ai-service` (código) + documentação, e aponte o usuário para instalar o resto. Para publicar fora do ambiente pessoal, reconfirme licenças e versões.
