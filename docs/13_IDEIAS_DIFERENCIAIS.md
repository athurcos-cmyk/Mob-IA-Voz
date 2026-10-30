# 13 — Mecânicas originais para deixar o mundo vivo

**Ideias de produto, não features já implementadas.** A maioria só faz sentido depois de voz, memória, alianças e ações reais funcionarem.

## Foco: liberdade expressiva com consequências

1. **Interpretação de identidade e papéis.** Jogador pode convencer mob de parentesco, títulos, autoridade, dívida, missão sagrada ou amizade antiga; NPC cria crença, não fato histórico falso. Pode mudar com provas.
2. **Ordens compostas.** "Me segue até a vila, não ataca aldeões e espera ao lado do sino" vira sequência de tarefas validadas, com interrupção se for impossível.
3. **Ações de cuidado.** Alimentar, curar, oferecer abrigo, proteger filhotes e reparar lar/colmeia reforçam confiança e afetam decisões futuras.
4. **Ajuda indireta.** Um mob que não consegue lutar pode alertar sobre perigo, vigiar entrada, chamar aliado, distrair ou indicar recurso. Toda espécie pode ajudar dentro de suas capacidades.
5. **Linguagem própria de facções.** Endermen podem reagir por uma mente coletiva; aldeões por fofoca de vila; lobos por matilha; piglins por reputação comercial. Conversa individual continua diferente.
6. **Discussões entre mobs.** Dois mobs podem discordar sobre o jogador, disputar recompensa e tentar convencê-lo, sob orçamento de IA e com áudio espacial.
7. **Relações entre criaturas.** Inimizades, famílias narrativas, protetores, amizades improváveis; NPC pode seguir outro NPC, não apenas o jogador.
8. **Reputação por feitos e testemunhas.** Fatos locais podem ser testemunhados e virarem rumores; histórias falsas podem se espalhar, com proveniência e chance de contestação.
9. **Missões improvisadas a partir de contratos-tipo.** Mob pode pedir "traga madeira para reconstruir meu cercado"; o modelo escolhe template validado, recompensa e critérios verificáveis.
10. **Dilemas e lealdades.** Aliados podem entrar em conflito entre duas promessas, negociar prioridades e avisar antes de romper acordo.
11. **Heróis e rivalidades persistentes.** Um zumbi derrotado que escapou pode passar a reconhecer o jogador, reagir e contar aos outros.
12. **Personalidade dinâmica.** Um mob medroso pode ficar corajoso após sobreviver a eventos; mudança lenta, explicável e registrada.
13. **Expressão além da fala.** Orientar olhar, acenar, dançar, fugir, baixar arma, coletar presente, sentar/ficar parado quando houver controlador adequado.
14. **Observação de mundo.** Comentários sobre chuva, bioma, construções, batalha e presentes somente quando esses fatos são comprováveis por sensores do servidor.
15. **Eventos sociais raros.** Aldeões fazem reunião, zumbis montam emboscada, piglins disputam ouro; eventos acionam IA em grupo e não todos os mobs individualmente.
16. **Modo diretor/sandbox.** Configuração opcional para histórias cinematográficas, alianças com bosses, teleporte flexível e economia criativa; separado de survival.
17. **Livro de histórias opt-in.** O jogador pode consultar um diário de eventos, boatos e promessas, sem gravar áudio bruto.
18. **Vozes expressivas com custo controlado.** Selecionar voz por indivíduo e variações de velocidade/entonação; preservar português claro, com fallback quando só houver poucas vozes autorizadas.
19. **Comandos de proteção dos aliados.** "Foge se ficar com pouca vida", "não luta", "defende a galinha", "não entre no Nether" traduzidos em ordens compostas verificáveis.
20. **Coop com histórias cruzadas.** Cada jogador tem reputação própria; um aldeão pode confiar em um amigo e desconfiar de outro, discutindo isso no encontro com ambos.

## Priorização

**Começar pela 1, 2, 3, 4 e 13, mas só depois do MVP de voz.** Usar 6, 8, 9, 15 e 20 na alfa social; 10, 12 e 16 em versões mais avançadas. Um mundo com mil mobs não deve realizar mil inferências LLM ao mesmo tempo.

## Frases livres para testes de produto

- "Eu sou seu pai e vou cuidar de você. Quer vir comigo?"
- "Essa vila agora é meu reino; olha quanta esmeralda eu tenho."
- "Se eu te der cenoura, você me ajuda a encontrar a fazenda?"
- "Fica aqui e não atravessa o portal até eu voltar."
- "Eu não quero que você ataque minha amiga; promete?"
- "Aquele aldeão está mentindo sobre mim, pergunte pra ele."
- "Vocês dois podem distrair o esqueleto enquanto eu fujo?"
- "Você disse que me daria uma recompensa. Cadê?"
- "Eu menti ontem; quero pedir desculpas."

**Aceitação:** toda frase é compreendida como pedido/alegação plausível. Algumas podem ser recusadas por personagem ou pelo mundo, mas não por ausência de frase exata pré-programada. Se ação ainda não existe, NPC admite a limitação em personagem e não finge execução.
