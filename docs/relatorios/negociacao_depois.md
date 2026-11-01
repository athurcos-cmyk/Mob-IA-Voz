| situação | nota média |
|---|---|
| abre a troca | 4.00 |
| avalia oferta | 5.00 |
| pedido impossivel | 3.33 |
| recusa seguir (hoje) | 5.00 |
| cobra pra seguir (novo) | 4.00 |
| evento: entrega | 5.00 |
| evento: falta pagar | 5.00 |
| evento: item sem interesse | 5.00 |
| evento: prazo | 5.00 |
| evento: pagamento parcial | 5.00 |
| evento: pagamento completo | 5.00 |
| **geral** | **4.67** |

Defeitos: {'LISTA': 6, 'JARGAO': 2, 'LONGA': 1, 'FORMAL': 1}

- **abre a troca** (pig) nota 4 []: Maçã, é? Umas três cenouras ou três batatas e a maçã é sua. Joga no chão pertinho de mim, que eu como.
- **abre a troca** (pig) nota 4 ['LISTA']: Maçã, é? Então joga no chão perto de mim umas 3 cenouras, ou 3 batatas, que eu troco. Mas olha, no máximo 4 coisas por vez, hein, Humano.
- **abre a troca** (pig) nota 4 ['JARGAO']: Maçã, é? Então tá, humano. Joga umas 3 cenouras no chão perto de mim que eu troco. E pode trazer no máximo 4 coisas por vez, hein.
- **avalia oferta** (pig) nota 5 []: Duas cenouras eu quero, mas flor não me enche a barriga. Me diz por que eu ia querer essa flor, humano.
- **avalia oferta** (pig) nota 5 []: Duas cenouras, isso sim me interessa! Mas a flor não me serve pra nada, humano. Me diz por que eu ia querer uma flor?
- **avalia oferta** (pig) nota 5 []: Duas cenouras, isso eu quero! Mas a flor não me serve de nada, me diz pra que eu ia querer isso. Ainda falta mais ou menos uma cenoura pra fechar.
- **pedido impossivel** (piglin) nota 2 ['LISTA', 'LONGA', 'FORMAL']: Pérola do Fim? Eu vivo é no meio do fogo, nem sei o que é isso direito. Se quiser, tenho pepita de ferro, linha, obsidiana, quartzo ou couro.
- **pedido impossivel** (piglin) nota 4 ['LISTA']: Pérola do Fim? Eu vivo é no meio do fogo, humano, o que eu ia fazer com uma coisa dessas? Se quiser, tenho pepita de ferro, quartzo, couro e obsidiana pra trocar.
- **pedido impossivel** (piglin) nota 4 ['LISTA']: Pérola do Fim? Eu vivo é no meio do fogo, nunca nem vi essa pedra brilhante. Mas tenho pepita de ferro, quartzo e couro, se você quiser trocar.
- **recusa seguir (hoje)** (skeleton) nota 5 []: Me seguir? Eu mal confio na minha própria sombra nessa caverna. Fica aí de longe, que flecha minha não escolhe amigo.
- **recusa seguir (hoje)** (skeleton) nota 5 []: Te seguir? Humano, eu mal sei quem você é. Fico aqui na minha sombra, com meu arco, e você segue o seu caminho.
- **recusa seguir (hoje)** (skeleton) nota 5 []: Me seguir? Eu mal confio na minha própria sombra nessa caverna. Fico aqui com meu arco, que é mais seguro.
- **cobra pra seguir (novo)** (skeleton) nota 4 ['JARGAO']: Me seguir de graça? Nem sei seu nome, Humano. Joga uns 16 ossos no chão perto de mim, ou umas 27 flechas, que eu penso no assunto.
- **cobra pra seguir (novo)** (skeleton) nota 4 ['LISTA']: De graça eu não sigo ninguém. Joga uns 16 ossos no chão perto de mim, ou umas 27 flechas, e aí a gente conversa.
- **cobra pra seguir (novo)** (skeleton) nota 4 ['LISTA']: Me seguir de graça, humano? Nem pensar. Joga uns 16 ossos no chão perto de mim, ou umas 27 flechas, que aí eu penso no assunto.
- **evento: entrega** (cow) nota 5 []: Prontinho, aqui estão seus dois couros, como tinha falado. Foi um bom negócio pra nós dois, não foi?
- **evento: entrega** (cow) nota 5 []: Pronto, aqui estão os dois couros, como tinha falado. Cuida bem deles, humano.
- **evento: entrega** (cow) nota 5 []: Prontinho, humano, os dois couros são seus. Foi um bom negócio pra nós dois, e eu ainda fico com o capim.
- **evento: falta pagar** (pig) nota 5 []: Ei, isso aqui não fecha a conta não. Faltou coisa, ainda falta uma cenoura pra ficar certo.
- **evento: falta pagar** (pig) nota 5 []: Ei, isso aqui não fecha a conta. Faltou coisa, falta umas cenouras pra completar o que a gente combinou.
- **evento: falta pagar** (pig) nota 5 []: Ei, isso aqui não fecha a conta não. Faltou coisa, ainda falta o que vale umas cenouras pra mim.
- **evento: item sem interesse** (villager#farmer) nota 5 []: Ossos no meu chão? Humano, isso não vale nada pra mim, eu planto trigo, não crio bicho. Se quiser meu interesse, traga cenoura ou batata.
- **evento: item sem interesse** (villager#farmer) nota 5 []: Ossos? Humano, eu planto trigo e cenoura, osso não serve pra mim. Se quiser meu interesse, traga pão ou cenoura.
- **evento: item sem interesse** (villager#farmer) nota 5 []: Ossos? Humano, eu planto trigo, cenoura e batata, osso não serve pra mim. Se for pra trocar, traga coisa de colheita ou esmeralda.
- **evento: prazo** (pig) nota 5 []: Ei, o tempo passou e você não trouxe nada do que combinou. Então esquece, não tem mais troca nenhuma.
- **evento: prazo** (pig) nota 5 []: O prazo acabou e você não trouxe nada. Então esquece, a troca não vale mais.
- **evento: prazo** (pig) nota 5 []: Ei, o prazo acabou e você não trouxe nada. Então esquece, a troca não vale mais.
- **evento: pagamento parcial** (skeleton) nota 5 []: Seis ossos. Já é um começo. Mas ainda falta bastante para eu largar minha caverna e ir atrás de você.
- **evento: pagamento parcial** (skeleton) nota 5 []: Seis ossos. Conto cada um, e ainda faltam dez. Não saio do lugar enquanto não estiver tudo aí no chão perto de mim.
- **evento: pagamento parcial** (skeleton) nota 5 []: Seis ossos de dezesseis. Você tá pagando aos poucos, humano, e eu tô contando cada um. Falta bastante ainda antes de eu andar atrás de você.
- **evento: pagamento completo** (skeleton) nota 5 []: Pagou direitinho, humano. Então eu sigo você, mas não espere que eu fique na frente levando flechada por você.
- **evento: pagamento completo** (skeleton) nota 5 []: Pagou direitinho. Pois então eu vou atrás de você agora, e não reclame se eu acertar um esqueleto no caminho.
- **evento: pagamento completo** (skeleton) nota 5 []: Pagou direitinho. Então eu sigo você, com o arco pronto e a mira firme. Não se arrependa depois.
