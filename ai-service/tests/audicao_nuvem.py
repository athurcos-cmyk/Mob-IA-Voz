"""Audicao da voz na nuvem (Azure) lado a lado com o Kokoro: UMA frase por criatura (+ tres vacas diferentes). Gasta uns 800 caracteres do Azure
(centavos de centavo; cabe no gratis), por isso so roda com --confirmo e NUNCA entra em teste ou bateria automatica.

Uso:  python tests/audicao_nuvem.py --confirmo [pasta]     (padrao: docs/relatorios/audicao_nuvem). Precisa da chave: configurar-voz-nuvem.ps1."""
import os
import sys
import wave
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE.parent))
os.environ.pop("MOBVOZ_NO_CLOUD", None)

import numpy as np  # noqa: E402

from app import nuvem, speech  # noqa: E402

LINES = [  # especie, seed, frase
    ("minecraft:cow", 1, "Muuu... eu só quero pastar em paz, humano."), ("minecraft:cow", 2, "Muuu... eu só quero pastar em paz, humano."),
    ("minecraft:cow", 3, "Muuu... eu só quero pastar em paz, humano."), ("minecraft:pig", 1, "Eu topo trocar, mas só se for cenoura das boas."),
    ("minecraft:chicken", 1, "Có! Tem semente? Eu troco pena por semente."), ("minecraft:sheep", 1, "Béé, a lã é minha e ninguém mexe nela."),
    ("minecraft:wolf", 1, "Eu seguro a sua trilha, se você me der um osso."), ("minecraft:piglin", 1, "Ouro! Ouro! Onde tem ouro?"),
    ("minecraft:iron_golem", 1, "Eu protejo esta vila. Foi feito de ferro, para durar."), ("minecraft:zombie", 1, "Carne... eu só sinto fome, humano."),
    ("minecraft:skeleton", 1, "Minha mira nunca falha. Quer testar?"), ("minecraft:warden", 1, "Eu escuto o seu coração daqui."),
    ("minecraft:wither", 1, "Tudo vai virar cinzas ao meu redor."), ("minecraft:bee", 1, "Que dia lindo para voar entre as flores!"),
    ("minecraft:witch", 1, "Uma poção para você? Custa uma esmeralda."), ("minecraft:villager#farmer", 1, "A colheita foi boa, quer trocar trigo por esmeralda?"),
    ("minecraft:villager#librarian", 1, "Este livro vale mais do que parece."), ("minecraft:enderman", 1, "Não me olhe nos olhos. Eu não gosto."),
]


def save(path: Path, pcm: bytes) -> None:
    with wave.open(str(path), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(speech.SVC_RATE)
        w.writeframes(pcm)


def main() -> None:
    sys.stdout.reconfigure(encoding="utf-8")
    args = [a for a in sys.argv[1:] if a != "--confirmo"]
    if "--confirmo" not in sys.argv:
        sys.exit("Isto gasta uns 800 caracteres do Azure. Rode de novo com --confirmo se for isso mesmo.")
    if not nuvem.enabled():
        sys.exit("Sem chave do Azure (rode configurar-voz-nuvem.ps1) ou MOBVOZ_NO_CLOUD ligado.")
    out = Path(args[0]) if args else HERE.parents[1] / "docs" / "relatorios" / "audicao_nuvem_v3"
    out.mkdir(parents=True, exist_ok=True)
    chars = 0
    for sp, seed, text in LINES:
        if nuvem.uses_local(sp, seed):
            print(f"{sp} {seed}: fica com a voz local (Kokoro), sem Azure")
            continue
        name = sp.replace("minecraft:", "").replace("#", "_") + f"_{seed}"
        raw = nuvem.synthesize(speech.clean_for_speech(text), sp, seed)
        if not raw:
            sys.exit(f"O Azure nao respondeu para {name} (veja o aviso acima). Nada foi misturado com a voz local.")
        azure = speech._to_pcm48(np.frombuffer(raw, dtype="<i2").astype(np.float32), nuvem.RATE)
        save(out / f"{name}_azure.wav", azure)
        chars += len(text)
        print(f"{name}: {nuvem.choose(sp, seed, '', nuvem.voices(*nuvem.credentials()))}  {len(azure) / 96000:.1f}s")
    # catalogo: cada voz do Azure falando normal, sem ajuste nenhum, para escolher de ouvido
    cat = out / "catalogo"
    cat.mkdir(exist_ok=True)
    key, region = nuvem.credentials()
    for g, nomes in nuvem.voices(key, region).items():
        for v in nomes:
            n = v.replace("pt-BR-", "").replace("Neural", "")
            frase = f"Oi, eu sou {'a' if g == 'F' else 'o'} {n}. Tudo bem com você?"
            raw = nuvem._request(f"https://{region}.tts.speech.microsoft.com/cognitiveservices/v1", key, nuvem.ssml(frase, v, 0.0, 1.1).encode("utf-8"),
                                 {"Content-Type": "application/ssml+xml", "X-Microsoft-OutputFormat": "raw-24khz-16bit-mono-pcm", "User-Agent": "MobVozIA"})
            save(cat / f"{g}_{n}.wav", speech._to_pcm48(np.frombuffer(raw, dtype="<i2").astype(np.float32), nuvem.RATE))
            chars += len(frase)
    print(f"\nPronto em {out}  ({chars} caracteres enviados ao Azure)")


if __name__ == "__main__":
    main()
