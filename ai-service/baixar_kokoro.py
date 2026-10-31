"""Baixa os modelos do Kokoro (voz principal do MobVoz) para ai-service/models, conferindo o hash.

Uso (no .venv do ai-service):   .venv\\Scripts\\python.exe baixar_kokoro.py
Precisa tambem de:               .venv\\Scripts\\python.exe -m pip install kokoro-onnx==0.4.7

Origem: release model-files-v1.0 de github.com/thewh1teagle/kokoro-onnx (pesos do hexgrad/Kokoro-82M, Apache-2.0). ~120 MB no total.
Os hashes abaixo sao os dos arquivos que testamos; se um download nao bater, o arquivo e apagado e nada e usado.
Sem o Kokoro, o sidecar funciona no modo leve (Piper).
"""
import hashlib
import sys
import urllib.request
from pathlib import Path

BASE = "https://github.com/thewh1teagle/kokoro-onnx/releases/download/model-files-v1.0/"
FILES = {
    "kokoro-v1.0.int8.onnx": "6e742170d309016e5891a994e1ce1559c702a2ccd0075e67ef7157974f6406cb",  # 92 MB (versao int8: a mais leve)
    "voices-v1.0.bin": "bca610b8308e8d99f32e6fe4197e7ec01679264efed0cac9140fe9c29f1fbf7d",          # 28 MB
}


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for block in iter(lambda: f.read(1 << 20), b""):
            h.update(block)
    return h.hexdigest()


def main() -> int:
    dest = Path(__file__).resolve().parent / "models"
    dest.mkdir(exist_ok=True)
    for name, want in FILES.items():
        target = dest / name
        if target.exists() and sha256(target) == want:
            print(f"ok (ja baixado): {name}")
            continue
        print(f"baixando {name} ...")
        tmp = target.with_suffix(target.suffix + ".part")
        urllib.request.urlretrieve(BASE + name, tmp)
        if sha256(tmp) != want:
            tmp.unlink()
            print(f"ERRO: o hash de {name} nao confere; arquivo descartado.", file=sys.stderr)
            return 1
        tmp.replace(target)
        print(f"ok: {name}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
