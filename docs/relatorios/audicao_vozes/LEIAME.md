# Audição de vozes: Piper × Kokoro (2026-10-31)

Os `.wav` são as mesmas 6 frases em cada motor: `N_criatura_piper_hoje.wav` (como o jogo gera hoje), `N_criatura_piper_corrigido.wav` (Piper com o tempo compensado, para o tom não mudar a duração) e `N_criatura_kokoro.wav` (Kokoro int8, vozes pf_dora/pm_alex/pm_santa, só velocidade; o Kokoro não muda o tom).

Scripts: `run_piper.py` (usa o python do `ai-service/.venv`) e `run_kokoro.py` (venv de teste com `kokoro-onnx`, modelo `kokoro-v1.0.int8.onnx` + `voices-v1.0.bin` do release `model-files-v1.0` de thewh1teagle/kokoro-onnx); `common.py` tem as frases e a medição de memória. Os modelos do Kokoro **não** estão no projeto (foram baixados só para o teste).

## Decisão (2026-10-31)

O dono do projeto ouviu e escolheu o **Kokoro como voz principal** ("mil vezes melhor, mais nítido e com português melhor"; o único contra é o tempo a mais para gerar) e o **Piper como modo leve**. Integrado em `ai-service/app/speech.py` (`MOBVOZ_TTS` / `voiceEngine`). Os arquivos `final_*.wav` foram gerados pelo caminho real do jogo.

## Medições (CPU, esta máquina; modelo já carregado)

| | Piper (3 vozes) | Kokoro int8 (3 vozes) |
|---|---|---|
| Em disco | 189 MB (3 × 63 MB) | 92 MB + 28 MB = 120 MB |
| Memória do processo | ~626 MB (3 vozes carregadas) | ~363 MB |
| Carregar | 3 a 5 s por voz | 1,6 s |
| Gerar uma fala (~5 s de áudio) | 0,2 a 0,3 s (4% do tempo da fala) | 1,2 a 1,8 s (35% do tempo da fala) |
| Licença | GPL-3.0 (código); vozes CC0 (faber/jeff/cadu) | pesos Apache-2.0, **mas** o programa que os roda (phonemizer-fork/espeak-ng) é GPL-3.0, como o Piper |

O Piper gasta menos processador por fala; o Kokoro gasta menos memória e disco, mas leva ~6× mais tempo para gerar cada fala. Nenhuma das medições inclui a placa de vídeo (ambos rodam no processador). A qualidade não está medida aqui: foi por audição.

Tempo da fala hoje no Piper: zumbi 8,4 s e warden 7,8 s (tom grave) contra 2,7 s da galinha (tom agudo); com o tempo corrigido, 6,5 s e 6,3 s.
