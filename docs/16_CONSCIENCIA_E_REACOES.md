# 16 — Consciência dos mobs e reações naturais

Cada criatura **sabe quem é**: de onde veio, como percebe o mundo, por que faz o que faz, do que tem medo e como reage a ameaças. A base é `lore.json` (66 criaturas), pesquisada no [Minecraft Wiki](https://minecraft.wiki) e **resumida em palavras próprias**. O que é lenda ou teoria de fã fica marcado como lenda ("dizem…"), e o que o jogo não revela a criatura admite não saber.

## Exemplos pesquisados (e como entram na fala)

| Criatura | O que o wiki diz | Como a criatura se entende |
|---|---|---|
| Warden | é totalmente **cego**: percebe por vibrações e cheiro; cada ruído o enfurece; lã abafa passos; some após ~1 min sem estímulos | fala de chão que treme e de barulho; *"Não ouço o que diz, mas sinto a tensão no ar"*; ameaça não o move |
| Creeper | nasceu de um **modelo de porco com medidas trocadas** (história de desenvolvimento); ninguém sabe por que explode | conta como *lenda*: "dizem que nasci de um porco errado"; não sabe o motivo |
| Enderman | vem do Fim; move blocos; enfurece se encarado; sofre com água | "movo porque são bonitos"; fica furioso ao ser encarado |
| Piglin / Zombificado | obcecado por ouro; vira zumbificado fora do Nether | explica o ouro e a transformação |
| Phantom | aparece para quem passa noites sem dormir | "o sono perdido te chama" |
| Wither Skeleton, Blaze, Ghast… | só na fortaleza/vales do Nether; origem do Blaze é *teoria* | Blaze conta a origem como lenda; Ghast diz que ninguém sabe por que chora |

## Reagir ao tom, não só às ordens

O analisador reconhece o **tom** da sua fala: `THREAT` (ameaça, "vou te comer/matar/transformar em comida") e `COMFORT` (acalmar). O **servidor** decide a reação pela natureza da criatura (`ameaca` em `lore.json`):

- **foge** (presas: porco, vaca, galinha, coelho, aldeão, peixes…): corre de verdade, para longe de você, por ~10 s, por terreno seguro; fala em pânico;
- **luta** (zumbi, esqueleto, lobo, abelha…): mira você e avança;
- **não se abala** (Warden, Dragão, Wither, golem): nenhuma reação física; explica pela própria natureza;
- **congela** se quer reagir mas não consegue se mover; **aliados, donos e quem tem trégua com você nunca fogem nem atacam por palavras**.

Palavras só mudam **medo e confiança** (fato "ameaçou com palavras", nunca "feriu"); consolo reduz o medo. Nada disso cria dano nem recompensa.

## Limites honestos
- Quem reconhece o tom é o LLM de 4B; ameaça sarcástica ou ambígua pode ser lida errado. A reação, depois de reconhecida, é regra determinística.
- A riqueza emocional da fala depende do modelo (o porco assustado fala corretamente, mas sem muito drama).
- Criaturas fora das 66 recebem uma consciência derivada da categoria (monstro/animal/aquático), mais genérica.
- Para acrescentar uma criatura: uma entrada em `lore.json` (e, se conversa muito, um perfil em `profiles.json`).
