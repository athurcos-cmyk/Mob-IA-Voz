# Geradores de dados do mod

Os arquivos `mod-fabric/src/main/resources/mobvoz/{profiles,trades,values,lore}.json` são **gerados** por estes scripts (fonte única). Edite aqui e rode; não edite os JSON à mão.

| Script | Gera | O que contém |
|---|---|---|
| `genprofiles.py` | `profiles.json`, `trades.json`, `values.json` | perfis de criatura (estilo, dieta, voz, credulidade…); chama `catalog.py` para o catálogo |
| `catalog.py` | (usado pelo anterior) | itens novos com valor e nomes pt-BR (`NEW_VALUES`), o que cada criatura **aceita** e as **receitas** que ela oferece (`CATALOG`); preço por unidade = valor da saída × margem |
| `genlore.py` | `lore.json` | origem, sentidos, motivação, medos e reação a ameaça de 66 criaturas (pesquisa no minecraft.wiki) |

```
cd tools/gen
python genprofiles.py     # perfis + receitas + valores
python genlore.py         # lore
```

Regras que os testes de unidade (`gradlew test`) cobram depois de gerar: toda saída de receita está em `drops` do perfil; uma criatura nunca aceita e dá o mesmo item; o preço nunca é menor que o valor do que entrega; nada passa de uma pilha por negócio nem exige mais de 32 itens do aceito mais valioso por unidade; todo item de `values.json` existe no jogo (`AllTradersGameTest`).

Depois de mudar o catálogo: rode `gradlew test` e o gametest `AllTradersGameTest`, e reinicie o sidecar se mudou algo que vai para o prompt.
