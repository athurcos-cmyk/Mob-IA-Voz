# 20 — Página do projeto e repositório

## Repositório

`github.com/athurcos-cmyk/Mob-IA-Voz` (privado a princípio; o dono o torna **público**; autorizado em 2026-10-31). Nunca vão para ele: chaves de API (a do dono fica em `%USERPROFILE%\.mobvoz\api.json`, fora do projeto), o jar do Simple Voice Chat (licença não permite redistribuir), modelos de voz/fala/LLM, `.venv`, JDK e pastas de build (ver `.gitignore`). O pacote para instalar é `dist/MobVozIA-<versão>.zip` (`empacotar.ps1`), que leva o jar do mod, o `ai-service` (sem venv e sem modelos), docs e licenças.

## A página (`site/`)

Página estática (HTML + CSS + JS, sem framework nem build), tema "blocos". Texturas e ícones são **arte própria** gerada por `tools/gen/gensite.py` (nada do Minecraft). Seções: início, em ação (prints + vozes), o que o mod faz, como funciona, instalação passo a passo (13 passos com checklist salvo no navegador, botões de copiar e "deve aparecer"), comandos e controles, IA própria × local × modo leve, solução de problemas, estado do projeto (Alpha) e créditos. Os prints saem de `ShowcaseGameTest` (rode `gradlew runClientGameTest -Ponly=ShowcaseGameTest[:farm|:village|:warden]`; usa a IA de verdade, melhor com a API) e são copiados à mão para `site/assets/img/`.

Para ver local: `python -m http.server 4173 --directory site` e abra `http://localhost:4173`.

### Publicar no Vercel (feito pelo dono, na conta dele)

1. Vercel > Add New > Project > importe o repositório (a conta precisa dar acesso ao repositório privado).
2. **Root Directory: `site`**; Framework Preset: Other; sem comando de build; sem diretório de saída (é estático). `site/vercel.json` já traz os cabeçalhos.
3. Deploy. Para atualizar: `git push` (o Vercel publica sozinho).

Antes de divulgar: gere uma chave nova de API (a antiga foi colada em chat), o link de download da etapa 4 aponta para `dist/MobVozIA-0.1.0.zip` no repositório privado (quem baixa precisa de acesso e estar logado no GitHub); para divulgar além dos amigos, publique o zip num lugar público e revise o texto de "Estado do projeto" (a página diz que ninguém testou com microfone real).

### O que a página afirma e onde está a prova

| Afirmação | Onde está verificado |
|---|---|
| Funções "testado em jogo" | testes `*GameTest` (`docs/06`, `STATUS.md`) |
| 48/48 frases entendidas, 0 injeção | `docs/18_BATERIA_DE_CONVERSACAO.md` |
| Conversas ambiente 4,06/5 | `docs/relatorios/bateria_ambiente3.md` (juiz de IA exigente) |
| Medidas de peso, voz | `docs/relatorios/audicao_vozes/LEIAME.md` |
| Instalação do zero | `ai-service/instalar-sidecar.ps1` testado numa cópia limpa (89 s, com cache); serviço sobe e fala |

Ninguém seguiu o passo a passo em outro PC ainda: é o primeiro teste real desta Alpha.
