"""MobVoz IA: prepara o Minecraft sozinho (Fabric, pasta de jogo exclusiva e os 3 mods). So biblioteca padrao do Python.
Rodado pelo 1-INSTALAR.bat. Pode rodar de novo sem problema (ele confere o que ja esta pronto).
Opcoes (para testes): --minecraft-dir, --game-dir, --java.
"""
import argparse
import glob
import hashlib
import json
import os
import shutil
import subprocess
import sys
import tempfile
import urllib.request

MC = "1.21.8"
FABRIC_API = "0.136.1+1.21.8"             # combinacao testada
VOICECHAT = "fabric-1.21.8-2.6.27"        # combinacao testada
UA = {"User-Agent": "MobVozIA-instalador/0.1 (instalacao local)"}
HERE = os.path.dirname(os.path.abspath(__file__))


def say(msg):
    print(msg, flush=True)


def fail(msg, code=2):
    say("\nERRO: " + msg)
    sys.exit(code)


def fetch(url):
    with urllib.request.urlopen(urllib.request.Request(url, headers=UA), timeout=60) as r:
        return r.read()


def fetch_json(url):
    return json.loads(fetch(url))


def download(url, dest, sha512=None):
    data = fetch(url)
    if sha512 and hashlib.sha512(data).hexdigest() != sha512:
        fail(f"o arquivo baixado de {url} nao confere com o hash oficial (download corrompido). Tente de novo.")
    with open(dest, "wb") as f:
        f.write(data)


def find_java(explicit):
    """Qualquer Java serve: o instalador do Fabric roda ate no Java 8."""
    cands = [explicit] if explicit else []
    jh = os.environ.get("JAVA_HOME")
    if jh:
        cands.append(os.path.join(jh, "bin", "java.exe"))
    cands.append(shutil.which("java"))
    pf = [os.environ.get("ProgramFiles", ""), os.environ.get("ProgramFiles(x86)", "")]
    la = os.environ.get("LOCALAPPDATA", "")
    ap = os.environ.get("APPDATA", "")
    pats = [os.path.join(p, "Eclipse Adoptium", "*", "bin", "java.exe") for p in pf]
    pats += [os.path.join(p, "Java", "*", "bin", "java.exe") for p in pf]
    pats += [os.path.join(ap, ".minecraft", "runtime", "*", "windows-x64", "*", "bin", "java.exe"),
             os.path.join(la, "Packages", "Microsoft.4297127D64EC6_*", "LocalCache", "Local", "runtime", "*", "windows-x64", "*", "bin", "java.exe")]
    for p in pats:
        cands += sorted(glob.glob(p), reverse=True)
    return next((c for c in cands if c and os.path.isfile(c)), None)


def ensure_java(explicit):
    java = find_java(explicit)
    if java:
        return java
    say("Java nao encontrado. Instalando o Java 21 (Temurin) pelo winget...")
    if not shutil.which("winget"):
        fail("nao achei o Java e nem o winget. Instale o Java 21 em https://adoptium.net/temurin/releases/?version=21 e rode o 1-INSTALAR.bat de novo.")
    subprocess.run(["winget", "install", "-e", "--id", "EclipseAdoptium.Temurin.21.JRE", "--silent", "--accept-package-agreements", "--accept-source-agreements"])
    java = find_java(None)
    if not java:
        fail("o Java nao foi instalado. Instale o Java 21 em https://adoptium.net/temurin/releases/?version=21 e rode o 1-INSTALAR.bat de novo.")
    return java


def modrinth_file(project, version):
    v = fetch_json(f"https://api.modrinth.com/v2/project/{project}/version/{urllib.request.quote(version)}")
    f = next((x for x in v["files"] if x.get("primary")), v["files"][0])
    return f["url"], f["filename"], f["hashes"]["sha512"]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--minecraft-dir", default=os.path.join(os.environ.get("APPDATA", ""), ".minecraft"))
    ap.add_argument("--game-dir", default=os.path.join(os.path.expanduser("~"), "MobVozJogo"))
    ap.add_argument("--java", default=None)
    a = ap.parse_args()
    mcdir, gamedir = os.path.abspath(a.minecraft_dir), os.path.abspath(a.game_dir)
    profiles_path = os.path.join(mcdir, "launcher_profiles.json")

    say("== 1/4 Conferindo o Minecraft Launcher")
    if not os.path.isfile(profiles_path):
        fail("nao achei o Minecraft Launcher configurado neste PC.\n"
             "Instale o launcher (minecraft.net/download), abra, entre na sua conta, depois feche e rode o 1-INSTALAR.bat de novo.")
    say("   ok: " + mcdir)

    say("== 2/4 Instalando o Fabric " + MC)
    loader = next(x["loader"]["version"] for x in fetch_json(f"https://meta.fabricmc.net/v2/versions/loader/{MC}") if x["loader"].get("stable"))
    inst = next(x for x in fetch_json("https://meta.fabricmc.net/v2/versions/installer") if x.get("stable"))
    version_dir = os.path.join(mcdir, "versions", f"fabric-loader-{loader}-{MC}")
    if os.path.isdir(version_dir):
        say(f"   Fabric Loader {loader} ja instalado")
    else:
        java = ensure_java(a.java)
        tmp = os.path.join(tempfile.gettempdir(), "fabric-installer.jar")
        download(inst["url"], tmp)
        r = subprocess.run([java, "-jar", tmp, "client", "-dir", mcdir, "-mcversion", MC, "-loader", loader], capture_output=True, text=True)
        say(r.stdout.strip()[-400:])
        if r.returncode != 0 or not os.path.isdir(version_dir):
            fail("o instalador do Fabric falhou:\n" + (r.stderr or r.stdout)[-600:])
        say(f"   Fabric Loader {loader} instalado")

    say("== 3/4 Criando a pasta de jogo exclusiva do MobVoz")
    mods = os.path.join(gamedir, "mods")
    os.makedirs(mods, exist_ok=True)
    shutil.copy2(profiles_path, profiles_path + ".bak-mobvoz")  # copia de seguranca do arquivo do launcher
    with open(profiles_path, encoding="utf-8") as f:
        data = json.load(f)
    key = next((k for k, v in data.get("profiles", {}).items() if str(v.get("lastVersionId", "")).startswith("fabric-loader-") and v["lastVersionId"].endswith("-" + MC)), None)
    if not key:
        fail("o Fabric foi instalado, mas nao achei o perfil dele no launcher. Abra o launcher e veja se existe 'fabric-loader-" + MC + "'.")
    data["profiles"][key]["name"] = "MobVoz IA"
    data["profiles"][key]["gameDir"] = gamedir
    with open(profiles_path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
    say("   ok: " + gamedir)

    say("== 4/4 Colocando os 3 mods na pasta mods")
    for old in glob.glob(os.path.join(mods, "mobvoz-*.jar")):
        os.remove(old)
    jars = glob.glob(os.path.join(HERE, "mod", "mobvoz-*.jar"))
    if not jars:
        fail("nao achei o arquivo do mod (pasta 'mod' do pacote). Extraia o zip inteiro e rode de novo.")
    shutil.copy2(jars[0], mods)
    say("   MobVoz IA: " + os.path.basename(jars[0]))
    for project, ver in (("fabric-api", FABRIC_API), ("simple-voice-chat", VOICECHAT)):
        url, name, sha = modrinth_file(project, ver)
        dest = os.path.join(mods, name)
        if os.path.isfile(dest) and hashlib.sha512(open(dest, "rb").read()).hexdigest() == sha:
            say("   ja tinha: " + name)
            continue
        say("   baixando " + name + " ...")
        download(url, dest, sha)
    say("\nPronto: o jogo esta preparado. No Minecraft Launcher, escolha a instalacao 'MobVoz IA' e clique Jogar.")


if __name__ == "__main__":
    main()
