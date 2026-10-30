# 07 — Multiplayer, desempenho, privacidade e publicação

## Multiplayer é viável, mas não é primeira entrega

- Servidor Fabric dedicado (mesma versão Minecraft/mod) é **autoridade** sobre identidades, relações, inventários, tempo, negociação, combate, spawn e transações.
- Todos os clientes que quiserem participar instalam nosso mod e Simple Voice Chat compatíveis; apenas host/servidor roda o sidecar de IA se hospedagem tiver recursos.
- Áudio de NPC sai de um `EntityAudioChannel` do SVC e é ouvido espacialmente por jogadores próximos. O mod não deve copiar áudio do jogador para outros serviços sem consentimento explícito.
- LAN e mundo integrado devem usar arquitetura de servidor autoritativo também; não criar mecânicas incompatíveis só para singleplayer.
- Em dedicado, Simple Voice Chat requer configuração de rede/porta de voz (UDP 24454 padrão, verificável); Ollama/sidecar **não** expõem portas para Internet. Separar rede pública do servidor Minecraft da rede privada de IA.

## Concorrência

- Uma entidade pode atender um enunciado por vez. Fila curta e com timeout, com prioridade para jogador que iniciou diálogo; evitar starve.
- Dois jogadores podem ter relacionamento diferente com o mesmo mob: `relationship[mob][player]`. Memória do mundo é comum, mas eventos privados são filtrados por audiência e configuração.
- Recompensas e alterações de inventário são atomicamente processadas no servidor; idempotência por `transactionId`.
- Se um jogador sair no meio de contrato, o mundo persiste reserva e a política devolve/expira com segurança.
- Em LAN, host sair encerra o mundo; salvar transações e identidade antes de fechar.

## Qualidade e desempenho

- Executar **uma única instância** do modelo local no host; não abrir LLM para cada mob ou jogador.
- Fila limitada, TTL para eventos de fala, interrupções/cancelamentos, timeouts e cache de perfil/voz.
- `Active NPC cap` por área, com prioridade e cooldown. Foco do jogador > mob importante > conversa ambiente.
- Falar com 1 NPC não requer gerar falas para outros 20.
- Calcular humor e relações em eventos/intervalos, não todo tick. Persistência incremental/resumida.
- Ajustes adaptativos para PCs básicos: STT smaller, Qwen3 4B Instruct ou Llama3.2 3B em benchmark, limites de fala menor, diálogos autônomos opcionais e raros.
- A voz exige custo real de CPU/GPU; oferecer observabilidade e predefinições Baixo/Médio/Alto depois do MVP.
- Evitar LLM longo em batalha; usar réplicas pequenas de perfil pré-geradas sem bloquear IA de combate.

## Privacidade e controles

- Microfone do jogador **opt-in**; não transcrever vozes de amigos involuntariamente. Adicionar configuração de captura apenas das próprias falas após consentimento.
- Se posteriormente quiser analisar fala de terceiros para mobs, exige opt-in individual e política clara por servidor, não padrão oculto.
- Áudio bruto temporário em memória, descartado após STT. Sem gravação em disco por padrão.
- Memória guarda eventos de jogo e resumos, não conversas privadas completas a menos que o jogador escolha explicitamente.
- Botão para desativar sistema, reduzir volume, pausar mundo social e limitar eventos/falas.
- Logs não devem conter voz, chaves, tokens, IPs desnecessários ou transcrições privadas.

## Licença e distribuição gratuita

- Pretensão: distribuir código e mod gratuitamente para comunidade quando pronto. Escolher licença de código como MIT/Apache-2.0 **apenas para código criado por nós**, se quiser, depois de verificar direitos de terceiros.
- Não redistribuir o arquivo do Simple Voice Chat nem os modelos de voz/LLM sem conferir seus termos; informar downloads oficiais e licenças.
- Adoção de API pública e compatibilidade são diferentes de copiar código fechado de mods concorrentes. Não copiar implementação ou recursos do Geleia sem autorização.
- Conferir licenças específicas de pesos Llama/Qwen e modelos Piper, atribuição e condições de redistribuição antes de publicar pacote “com modelos inclusos”.

## Critério de multiplayer estável

Dois jogadores no mesmo servidor conversam com o mesmo mob, ouvem suas respostas posicionais e veem o mesmo estado de negociação; nenhuma duplicação de item, dados de relação consistentes, IA no host, perda de conexão não gera crash nem quebra mundo. A experiência é degradável sem obrigar uso de recursos pagos.
