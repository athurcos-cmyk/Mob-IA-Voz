# MobVoz sidecar (IA local)

Serviço Python **só em `127.0.0.1`** que faz STT (faster-whisper), conversa (Ollama) e TTS (**Kokoro** pt-BR, mais natural; **Piper** como modo leve) para o mod. Sem APIs pagas.

## Instalação (Windows, uma vez)

```powershell
cd ai-service
python -m venv .venv
.venv\Scripts\python.exe -m pip install faster-whisper piper-tts scipy kokoro-onnx==0.4.7
.venv\Scripts\python.exe -m piper.download_voices pt_BR-faber-medium pt_BR-cadu-medium pt_BR-jeff-medium --data-dir models
.venv\Scripts\python.exe baixar_kokoro.py     # voz principal (~120 MB, hash conferido)
```

**Voz.** O padrão é o **Kokoro** (mais natural e nítido; ~360 MB de memória; ~1,5 s para gerar uma fala de 5 s no processador). O **Piper** é o modo leve (muito mais rápido: ~0,25 s por fala; ~630 MB com as 3 vozes carregadas). Sem os modelos do Kokoro o sidecar usa o Piper sozinho e avisa no log. Para escolher: variável `MOBVOZ_TTS=piper|kokoro` no sidecar ou `"voiceEngine"` em `server.json` do mundo (`auto|kokoro|piper`; o do mundo vence). `GET /health` mostra o motor em uso (`tts`).

Os modelos Whisper `base` (rápido, ~150 MB) e `small` (reserva quando o rápido fica em dúvida, ~460 MB) são baixados pelo instalador; a transcrição usa só 4 núcleos da CPU e o serviço roda com prioridade baixa, para o jogo ter preferência. O LLM vem do Ollama (`ollama pull qwen3:4b`).

## Rodar

```powershell
.\run-sidecar.bat          # http://127.0.0.1:8765
```

Variáveis: `MOBVOZ_TTS` (`kokoro`|`piper`), `MOBVOZ_MODEL` (padrão `qwen3:4b`), `MOBVOZ_STT_MODEL` (`small`), `MOBVOZ_STT_DEVICE` (`cpu`), `MOBVOZ_TOKEN` (se definido, exige header `X-MobVoz-Token`), `MOBVOZ_PORT`.

## API

| Rota | Entrada | Saída |
|---|---|---|
| `GET /health` | — | status do Ollama e modelos |
| `POST /v1/reply` | JSON `{entity_type, text, capabilities[], facts[]}` | `{speech_text, language:"pt-BR", emotion, intent, proposals[], source:"llm"\|"fallback"}` |
| `POST /v1/transcribe` | PCM int16 LE mono 48 kHz | `{text, duration_s}` |
| `POST /v1/synthesize` | JSON `{text, voice, pitch, speed}` | PCM int16 LE mono 48 kHz |

O LLM **só propõe**: `proposals` é filtrado por `capabilities` (que vem do servidor do jogo) e ações desconhecidas invalidam a resposta. Nada aqui executa comandos. Falas em inglês, emoji e JSON inválido são rejeitados (1 retry, depois fallback curto em pt-BR). Áudio/texto de jogadores não são logados.

## Testes

```powershell
.venv\Scripts\python.exe -m unittest discover -s tests          # unitários (sem rede)
.venv\Scripts\python.exe tests\live_bench.py                    # 10 falas reais contra o Ollama
.venv\Scripts\python.exe tests\live_speech.py                   # TTS -> STT com o sidecar rodando
```
