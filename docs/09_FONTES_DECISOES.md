# 09 — Referências e registro de decisões

Consultado em 30/10/2026. A documentação do projeto deve seguir **a versão de dependência realmente fixada** no build, não apenas o resultado mais recente de buscas.

## Fontes de tecnologia

- Simple Voice Chat — versões compatíveis: https://modrepo.de/minecraft/voicechat/wiki/supported_versions
- Simple Voice Chat — projeto e voz espacial: https://github.com/henkelmax/simple-voice-chat
- SVC Java API — entrada de áudio do cliente: https://voicechat.modrepo.de/de/maxhenkel/voicechat/api/events/ClientSoundEvent.html
- SVC Java API — pacotes de microfone no servidor: https://voicechat.modrepo.de/de/maxhenkel/voicechat/api/events/MicrophonePacketEvent.html
- SVC exemplos — `EntityAudioChannel` e reprodução: https://modrepo.de/minecraft/voicechat/api/examples
- Fabric documentação: https://docs.fabricmc.net/
- Ollama modelo Llama 3.2: https://ollama.com/library/llama3.2
- Ollama modelo Qwen3: https://ollama.com/library/qwen3
- Ollama outputs estruturados: https://ollama.com/blog/structured-outputs
- faster-whisper (STT): https://github.com/SYSTRAN/faster-whisper
- Piper (TTS; vozes pt_BR): https://github.com/rhasspy/piper/blob/master/VOICES.md

## Decisões aprovadas para começar

- Java Edition, 1.21.8, Fabric, Java 21.
- Ninguém precisa contratar APIs de LLM para jogar; Ollama local por padrão.
- Mob deve ser selecionado individualmente sem clique; voz pt-BR com resposta espacial.
- O servidor valida as ações. LLM escolhe intenção dentro de contrato restrito.
- Item oferecido e recompensa fazem parte de transação real com anticópia e histórico.
- Diálogo NPC-NPC acontece por eventos e limites de carga, não por tick.
- Multiplayer entra depois do modo individual e da persistência da economia.

## Questões que Codex DEVE verificar antes de cada marco

1. Versões reais atuais de Fabric Loader, Fabric API, Loom, Gradle e Simple Voice Chat compatíveis com 1.21.8.
2. Como se registra plugin Simple Voice Chat para captura e reprodução no lado correto (cliente x servidor).
3. Em singleplayer integrado e servidor dedicado, como os segmentos de áudio PCM chegam ao serviço local sem violar arquitetura de rede nem consentimento.
4. Como implementar corretamente persistência por entidade/identidade e evento de cura zumbi-aldeão na API Fabric 1.21.8.
5. Compatibilidade de áudio PCM (sample rate, frame size) e codecs.
6. Disponibilidade, qualidade e licenças exatas de Piper pt-BR e modelos LLM selecionados, antes de redistribuí-los.
7. Recursos do PC e baseline real de FPS/ticks e latência de conversa.

## Cuidados com qualidade da fonte

Disponibilidade de uma API **não prova** que a integração inteira funciona. Os exemplos da API descrevem eventos e canais, mas lógica de seleção, STT, identidade, economia e multiplayer devem ser implementadas e testadas neste projeto. URLs externas e versões podem mudar. Não fixar artefatos por palpites.
