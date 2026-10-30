# 11 — Prompts de continuação para Codex (uma tarefa por vez)

**Antes de cada prompt:** somente execute após os testes da fase anterior. Todos os prompts devem ser precedidos pela instrução: “Leia `AGENTS.md`, estado atual do projeto, arquitetura e critérios de aceitação da fase.”

## 1 — Seleção/estrela

```text
Implemente apenas fase 1 do roadmap: target raycast de entidade, a princípio vaca,
distância até 6 blocos, linha de visão e pequena estrela acima de UMA vaca.
Sem chat/IA. Valide com dez vacas e desvio de mira. Build e pare.
```

## 2 — Microfone

```text
Implemente apenas fase 2: integração oficial com Simple Voice Chat para Fabric
1.21.8, consentimento, captura de PCM por evento documentado, VAD, one-shot por
fala, HUD de ouvindo, sem persistência de áudio. Testes com silêncio/10 frases,
sem ainda STT/IA/TTS. Build e pare.
```

## 3 — IA local

```text
Implemente apenas fase 3: serviço Python localhost + Ollama + esquema JSON estrito,
LLM configurável (Llama3.2 e Qwen3 4B Instruct para teste), persona vaca pt-BR,
contexto validado e fallback. Texto apenas para diagnóstico. LLM não executa
jogo. Testes unitários e build. Pare.
```

## 4 — STT/TTS espacial

```text
Implemente apenas fase 4: ligar áudio segmentado ao faster-whisper em pt, enviar
transcrição ao serviço local e sintetizar resposta com Piper pt_BR. Emitir via
EntityAudioChannel da vaca respeitando API/PCM e cancelamento. Teste sem chat,
um único alvo. Não avançar para novas espécies. Build/testes e pare.
```

## 5 — Memória

```text
Implemente somente fase 5: identidade estável por mob, eventos verificados,
relação jogador/mob, memória episódica resumida e persistência no mundo.
Salvar/reabrir deve preservar fatos; não armazenar áudio bruto. Testes e pare.
```

## 6 — Seguir e trégua

```text
Implemente apenas fase 6: motor de propostas e validador servidor com allowlist;
ações FOLLOW_PLAYER e CEASE_HOSTILITY em mob de teste com tempo, condições,
cooldown e recusa. Nenhuma chamada Java arbitrária vinda da IA. Testes e pare.
```

## 7 — Troca física

```text
Implemente apenas fase 7: primeiro contrato simples com um mob, receber item do
chão fisicamente, reservar, aceitar/recusar, devolver excesso, entregar recompensa
permitida, event log e transação idempotente. Testes de crash/duplicação/conflito,
sem recompensas raras. Build e pare.
```

## 8 — Perfis

```text
Implemente apenas fase 8: perfis de oito espécies, aldeões por profissão,
identidade individual, fallback de fala para outros mobs, estilos e vozes
coerentes. Não permitir habilidades fora da capability registry. Teste fazendeiro
vs ferreiro com feno, pt-BR e persistência. Pare.
```

## 9 — Mobs entre si

```text
Implemente apenas fase 9: conversas NPC-NPC acionadas por evento real, limite de
região, cooldown, 2-3 turnos, prioridade de diálogo de jogador e fila de voz.
Dois zumbis conversam no combate sem todas as criaturas falarem juntas. Pare.
```

## 10 — Relações e alianças

```text
Implemente somente fase 10: reputação por jogador e facção, pacificação de Creeper
por acordo com prazo/itens e controlador de acompanhamento/defesa de espécie
simples. Segurança, interrupção, cooldown e configuração. Testes e pare.
```

## 11 — Memória especial

```text
Implemente apenas fase 11: identidade sobrevive à cura zumbi aldeão, Endermen
têm memória coletiva filtrada por facção e Ender Dragon consulta resumos de fatos
registrados. Nunca inventar fatos passados. Testar save/reload e privacidade.
```

## 12 — Bosses e quests

```text
Implemente apenas fase 12 após todas as prévias: contratos complexos com Warden,
Wither e Ender Dragon, truces, lealdade, traição por gatilho, quests aprovadas,
restrições de destruição/follow e eventos verificados. Teste em mundo descartável;
sem geração ilimitada de loot. Pare.
```

## 13 — Multiplayer

```text
Implemente apenas fase 13: servidor Fabric autoritativo com estado compartilhado,
Simple Voice Chat no cliente e servidor, sidecar apenas no host, áudio de entidade
para jogadores próximos, filas/contratos idempotentes e consentimento por jogador.
Teste mínimo de dois clientes e um servidor dedicado/LAN. Pare.
```

## 14 — Otimização e release

```text
Implemente apenas fase 14: presets de performance, UI de opções, benchmarks,
tratamento de perda de conexão, política de voz/privacidade, documentação de
instalação, instruções de licença, empacotamento gratuito e changelog. Não
redistribuir modelos/artefatos de terceiros sem licença válida. Pare.
```

## Contexto obrigatório das fases 5, 6, 8, 10, 12

Ao implementar essas fases, acrescente ao prompt: **"Leia `docs/12_LIBERDADE_CRENÇAS_ALIANCAS.md` e casos T35–T44 em `docs/06_TESTES_ACEITACAO.md`. Qualquer mob deve ser candidato a vínculo; mentira deve poder produzir crença sem alterar fato. Antes de aceitar, implemente/verifique executor de ação real e persistência; respeite ordens ficar/seguir/portal. Não invente capacidades ainda não implementadas."**
