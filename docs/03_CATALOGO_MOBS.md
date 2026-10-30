# 03 — Catálogo de comportamento por espécie, papel e indivíduo

## Como combinar perfis

`persona = baseDaEspécie + papel/profissão + temperamentoIndividual + ambiente + condiçãoAtual + memórias + relações + objetivos`.

- **Espécie** define instintos, interesses, capacidades físicas e ameaças.
- **Profissão** (aldeões) altera prioridades, serviços e valorização de itens, sem substituir personalidade.
- **Individualidade**: sementes estáveis; dois fazendeiros não pensam igual.
- **Contexto**: noite, lua, bioma, perigo, inventário, proximidade de filhotes/aldeia/colmeia.
- **Capacidade de ação**: oferecer algo verbalmente não concede habilidades impossíveis, por exemplo vaca não pode voar ou gerar itens exclusivos sem receita autorizada.

Tabela de design inicial (valores e receitas são **ideias a validar**, não equilíbrio final):

| Grupo | Gostos/contexto | Ações/reações candidatas | Restrição |
|---|---|---|---|
| Vaca | Trigo, pasto, proteção, outros bovinos | pastar, seguir por comida, conversar sobre rebanho | Não vira fonte infinita de couro/leite sem regras |
| Porco | Cenoura, batata, alimento, segurança | negociar favor curto, seguir, fugir de ameaça | Sem transformar porco em combatente poderoso |
| Ovelha | Trigo, pasto, cor de lã, rebanho | aceitar tosquia com relação, pedir proteção | Lã respeita crescimento/cooldown |
| Galinha | Sementes, ninho, ovos | aproximar, chocar conversa, buscar segurança | Ovos respeitam tempo |
| Abelha | Flores, mel como contexto, colmeia segura | voar perto, defender colmeia, pedir flores | Mel oferecido não garante domínio de enxame |
| Lobo | Carne, matilha, proteção | seguir, defender, lembrar de ajuda | Mantém regras de domesticação/ataque |
| Gato | Peixe, curiosidade, território | aproximar-se por vontade própria, recusar ordem | Autonomia maior |
| Cavalo | Feno, maçã, descanso | seguir, aceitar montaria sob condições | Velocidade condicionada a atributos reais |
| Papagaio | Sementes, música, curiosidade | repetir boatos curtos, alertar ameaça | Não fornecer informação inacessível |
| Axolote | Água, sobrevivência, companhia | apoio aquático contextual | Não locomover em terra como cachorro |
| Aldeão fazendeiro | Trigo, feno, sementes, campo protegido | trocas agrícolas, favor por colheita | Descontos limitados |
| Aldeão ferreiro/ferramenteiro | Ferro, carvão, ferramentas, bancada | negociar ferramentas, ajudar em reparos quando implementado | Não aceitar feno como metal valioso |
| Aldeão bibliotecário | Livros, papel, conhecimento local | pistas/encantamentos só de tabela permitida | Sem todos os encantamentos grátis |
| Aldeão clérigo | Itens místicos, poções, cura | dicas, oferta de itens restringidos | Não curar magicamente sem sistema |
| Aldeão pescador | Peixes e clima | favores de pesca | Recompensa não infinita |
| Aldeão desempregado | Oportunidades, segurança, comida | pedir emprego, mudar reputação | Não oferecer profissão instantânea arbitrária |
| Zumbi | Fome, luz solar, grupo | perseguir, falar de carne, coordenar ataque | Não esquece hostilidade só com qualquer frase |
| Esqueleto | Arco, estratégia, precisão | comentar disparos, escoltar sob pacto raro | Usar IA vanilla de combate com ajustes |
| Creeper | Pólvora, autopreservação, tensão | pacificar por custo/tempo, ameaçar explodir | Não desarmar permanentemente sem limites |
| Enderman | Blocos, olhar, mente coletiva | negociar passagem, espalhar impressão de jogador | Teleporte limitado e regras de olhar |
| Piglin | Ouro, status, Nether | coletar ouro, barganhar, proteger rota | Reaproveitar regras de barter seguras |
| Esqueleto Wither | Ouro, Nether, prestígio | negociar serviço/recompensas raras | Cabeças somente por regra balanceada |
| Warden | Vibração, silêncio, ameaça, respeito | trégua, escolta em caso extremo | Limites severos, sem quebrar jogo |
| Wither | Destruição, poder, rivalidade | acordo provisório, ataque dirigido, traição | Sem destruição global sem opt-in |
| Ender Dragon | End, história, território, destino | narrar grandes eventos registrados, pacto excepcional | Nunca conhecimento fora do log real |

## Todos os mobs são conversáveis

**Cobertura de fala universal** deve ser possível por fallback genérico: `entity_type` + tags + hostilidade + ambiente permitem ao LLM falar como qualquer mob, inclusive mobs de mods externos via integração de registry. **Cobertura de ações universais não significa que todo mob possa fazer tudo.** A capacidade é explicitamente mapeada por espécie, comportamento e estágio do projeto.

- Passivos: gostos e troca/seguir simples.
- Hostis: negociação de cessar fogo, risco de traição, comportamento de combate.
- Chefes: capacidades exclusivas e limites/config de mundo.
- Entidades especiais: dragon, Warden, iron golem, allay, villagers, piglins, etc., exigir adaptadores validados.
- Modded mobs: perfis de fallback e lista de permissões; jamais assumir API de outros mods sem evidência.

## Personalidade não pode ser aleatoriedade sem memória

Cada mob tem `identityId`, `voiceProfile`, `temperament`, `motivation`, `relationshipMap` e `episodeHistory`. Um mob inicialmente ganancioso não fica altruísta de uma fala para outra sem evento marcante.

**Vozes diferenciadas:** mapear vozes pt-BR disponíveis e aplicar ajustes moderados de ritmo/pitch; usar vozes diferentes quando licenças/performance permitirem, com fallback honesto para biblioteca reduzida. Não clonar voz de youtubers ou pessoas reais sem autorização.

## Mudança de forma, dimensão e facção

- Conversão zumbi-aldeão preserva `identityId` através de evento de conversão; nova entidade aponta para ancestral.
- Endermen consultam memória de facção e preservam humor próprio; compartilhar impressões, não transcript bruto entre todos.
- Ender Dragon recebe **resumo de registro de eventos importantes**, sem acesso mágico a conversas privadas de jogadores.
- Em dimensões diferentes, relação pode persistir, mas acompanhamento de boss não atravessa portais automaticamente antes de mecanismo específico e teste.

## Exemplos de fala e ação

**Creeper:** “Se me trouxer pólvora, eu deixo você passar por este campo sem explodir... por enquanto.” O servidor cria proposta com prazo e valor; não desarma antes do pagamento.

**Esqueleto em batalha:** “Como você desvia dessas flechas? Vou mirar mais baixo.” Pode disparar `combat_tactic=lower_aim` apenas se permitido e implementado; se não, é fala narrativa, não falsa mudança do algoritmo.

**Fazendeiro:** “Esses fenos salvam minha criação! Posso melhorar sua próxima compra.” Reputação aumenta modestamente e preço fica dentro dos limites do jogo.

**Ferreiro:** “Feno não vai consertar minha bigorna. Procure ferro ou carvão.” Recusa coerente.

**Wither:** “Nós tínhamos um pacto, mortal, mas você invadiu meu território.” Evento de quebra de pacto antes da hostilidade.

## Requisito transversal v2.1 — alianças e opiniões não limitadas por espécie

O perfil de espécie é um **ponto de partida**, não uma proibição permanente de laços sociais. Qualquer mob pode aceitar, recusar, acreditar, duvidar ou negociar uma aliança (inclusive galinha, vaca, Creeper, zumbi, golem, Warden, Wither e Ender Dragon). Nem todo mob consegue acompanhar o jogador, voar, defender-se ou usar portais sem controlador próprio. Individualidade + crenças + fatos preservados, `12_LIBERDADE_CRENÇAS_ALIANCAS.md`.
