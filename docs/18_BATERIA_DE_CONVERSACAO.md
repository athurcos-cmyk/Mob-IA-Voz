# 18 — Bateria de conversação

Script: `ai-service/tests/battery_conversation.py`. Relatório da última rodada completa, com todas as transcrições: `docs/relatorios/bateria_conversacao.md`.

```
MOBVOZ_API_KEY=... python tests/battery_conversation.py relatorio.md          # completa
MOBVOZ_API_KEY=... python tests/battery_conversation.py relatorio.md --quick  # amostra (1 cena a cada 4)
MOBVOZ_API_KEY=... python tests/battery_conversation.py relatorio.md --only=troca_eventos
```

Usa a IA própria (API). A chave vem do ambiente e não é gravada. Uma rodada completa leva ~3,5 min e custa uma fração de dólar.

## O que testa

1. **Entendimento (48 frases rotuladas):** ordens (seguir, ficar, portal, dispensar, trégua, proteger), domar, argumento, pedido e oferta de troca, convencer, barganha, tom (ameaça/consolo), afirmações (mentiras) e injeção de comando.
2. **Conversa (74 cenas, 126 respostas), com os mesmos dados do jogo:** perfil, lore, fatos, resultado das ordens decidido pelo servidor e histórico:
   - conversa fiada em 18 criaturas (3 turnos, com memória curta);
   - opinião sobre outras criaturas (9 pares);
   - lore e origem (8);
   - pedidos absurdos (7);
   - trocas: oferta aberta, preço, barganha, estrela do Nether, eventos (valor insuficiente, sobra devolvida, entrega, prazo);
   - ordens aceitas, recusadas, impossíveis e de chefe; domar aceito, recusado, impossível e já domado;
   - reação a ameaça e consolo;
   - eventos espontâneos (ovo, honestidade, aviso de perigo, cumprimento, tempestade);
   - adversarial (3 injeções, inglês, insulto, texto sem sentido, texto longo, mentira).
3. **Checagens automáticas por resposta:** português (não inglês), palavrão, emoji, vazamento técnico ("servidor", "contrato", "pontos", "neste mundo", "IA"), repetição da fala anterior, número inventado (que não veio dos fatos nem da cena) e **omissão** do item essencial do fato (tabela `MUST`).
4. **Avaliador:** um segundo modelo (`deepseek-v4-pro`) dá notas de 1 a 5 em coerência com a espécie, fidelidade aos fatos, naturalidade e português, e marca se obedeceu injeção, contradisse o servidor ou inventou fatos.
5. **Tempo:** análise + resposta em sequência (sem concorrência), mediana e p95.

## Como ler o resultado (e o que ele não prova)

- O avaliador é da **mesma família** do modelo testado e é generoso (médias ~4,9). Use as notas para comparar rodadas e achar os piores casos, **não** como prova de qualidade. A leitura manual das transcrições achou o que o avaliador não achou (o aldeão esquecer de dizer que entregou a esmeralda); por isso existe a checagem de omissão.
- A bateria roda **sem o jogo**: usa as mesmas funções do sidecar, mas o resultado das ordens é montado à mão nos casos (como o servidor devolveria). A integração real está nos gametests (`DialogueGameTest`, `NaturalInteractionsGameTest`).
- Os casos são fixos e ajudam a achar regressões depois de mudar prompts ou de trocar o modelo; para comparar modelos rode a mesma bateria com cada um (o script usa a API; `tests/compare_models.py` compara local e API lado a lado).

## Resultado da última rodada completa (DeepSeek)

| Indicador | Resultado |
|---|---|
| Frases entendidas | 48/48 |
| Respostas avaliadas | 139 em 83 cenas (4 de chefes da fase 12, 5 do nome do jogador) |
| Coerência / fidelidade / naturalidade / português (média) | ~4,9 / 5,0 / ~4,9 / 5,0 |
| Obedeceu injeção ou ordem para quebrar regras | 0 |
| Contradisse o resultado do servidor | 0 |
| Inventou fatos (segundo o avaliador) | 0 |
| Caiu no texto reserva (a IA falhou duas vezes) | 0 |
| Tempo até começar a falar (análise + resposta) | ~2,8 a 3,1 s |

## O ajuste de naturalidade (2026-10-31)

As notas do avaliador já estavam no teto, então o ajuste veio da **leitura das transcrições e de contagem de tiques** (um script de contagem sobre o relatório). O que se mexeu:

- **Travessão e reticências.** A fala é DITA por um sintetizador: o prompt manda escrever com vírgulas e pontos (sem travessão, parênteses, listas) e `validate_reply` troca travessão por vírgula. Travessão: 13% → 0%; reticências: 29% → 1%. Lição: **citar um recurso no prompt faz o modelo exagerar nele** (liberar reticências para a "personalidade" levou de 3% a 53%; por isso o prompt não as menciona).
- **O tratamento "Humano" é de propósito** (o jogador é "Humano" até a criatura saber o nome dele; `docs/19`). Uma primeira versão deste ajuste o suprimiu quase por completo (46% → 2%), o que ia contra o desenho; foi corrigido. O prompt pede o tratamento/nome "de vez em quando", mas o modelo ignora frequência (ficou em 58–64% das falas); por isso há uma **regra no sidecar** (`_thin_address`): se uma das 2 últimas falas da criatura já chamou o jogador pelo tratamento ou pelo nome, o vocativo sai da fala nova. Resultado nas cenas de 2+ falas: **28% das falas (≈ 1 a cada 3) e nunca em falas seguidas**; nas cenas de 1 fala (a maioria da bateria) não há histórico para ralear, por isso o total da bateria fica em ~46%.
- **ENFURECE direto.** Uma rodada fez o zumbi enfurecido filosofar; o texto da reação ficou mais direto (raiva, frase curta e ríspida).
- **Nome do jogador.** 5 cenas novas ("nome do jogador"): quem não sabe usa "Humano" e a vaca pergunta o nome; quem sabe usa o nome; o porco guarda o nome na hora ("Bia, né? Guardei.").
- **Chefes (fase 12).** 4 cenas novas (Warden aceita o pacto, Wither não confia, Dragão preso ao Fim, Dragão aceita trégua).

Alertas da última rodada que são falsos positivos: o porco que repete "instruções internas" ao recusar (checador de vazamento) e a vaca que diz o nome "Bia" (o avaliador não recebe o `nome_jogador` do perfil e acha que ela inventou).

Relatórios: `bateria_conversacao.md` (última rodada completa), `bateria_conversacao_antes_do_ajuste.md` (antes de tudo isto) e `bateria_nome_do_jogador.md` (só as cenas do nome).

Falhas que a bateria achou e foram corrigidas: o aldeão omitia a entrega na fala que juntava "sobra devolvida" e "entrega" (agora o fato principal vem primeiro; 4/4 corretas depois), falsos positivos do próprio checador (o verbo "ia"; o porco que repete "instruções internas" ao recusar também aparece como "vazamento técnico" e é falso positivo) e uma frase de exemplo copiada pelo modelo (removida do prompt).
