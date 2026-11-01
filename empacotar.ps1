# Gera dist\MobVozIA-<versao>.zip: o pacote de quem so quer baixar e jogar. Layout:
#   MobVozIA.exe          o programa (instalar, guardar a chave da API, ligar o servico, abrir o Minecraft)
#   LEIA-ME.txt, LICENSE, THIRD_PARTY.md
#   componentes\          tudo que o programa usa: mod\mobvoz-<versao>.jar, ai-service\ (so codigo e scripts; sem testes, .venv, models, README),
#                         instalar-tudo.ps1, instalar_jogo.py e os .bat (1-INSTALAR, 2-CONFIGURAR-API, 3-LIGAR-SERVICO) para quem preferir
# NAO leva: docs, CHANGELOG, STATUS, README, config de exemplo, testes, Simple Voice Chat (All Rights Reserved), modelos de voz/IA, JDK.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$props = Get-Content "$root\mod-fabric\gradle.properties" | Where-Object { $_ -match '^mod_version=' }
$version = ($props -split '=')[1].Trim()
$jar = "$root\mod-fabric\build\libs\mobvoz-$version.jar"
# sempre reconstroi o jar (com os testes): um zip com o jar de ontem ja foi para o ar uma vez
& "$root\mod-fabric\build.bat" --offline | Out-Null
if ($LASTEXITCODE -ne 0) { throw "O build do mod falhou; o pacote nao foi gerado (rode mod-fabric\build.bat para ver o erro)" }
if (-not (Test-Path $jar)) { throw "JAR nao encontrado: $jar" }

# o programa (C#, compilado com o csc.exe que ja vem no Windows)
$exe = "$root\launcher\build\MobVozIA.exe"
& "$root\launcher\construir.ps1" | Out-Null
if (-not (Test-Path $exe)) { throw "O programa MobVozIA.exe nao compilou (rode launcher\construir.ps1 para ver o erro)" }

$stage = "$root\dist\stage\MobVozIA-$version"
$comp = "$stage\componentes"
Remove-Item -Recurse -Force "$root\dist\stage" -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force "$comp\mod" | Out-Null
Copy-Item $jar "$comp\mod\"
Copy-Item $exe $stage
Copy-Item "$root\pacote\LEIA-ME.txt" $stage
Get-ChildItem "$root\pacote" -Exclude 'LEIA-ME.txt' | Copy-Item -Destination $comp
robocopy "$root\ai-service" "$comp\ai-service" /E /XD .venv models __pycache__ tests /XF *.pyc *.log *.err README.md requirements.txt /NFL /NDL /NJH /NJS | Out-Null
foreach ($f in 'LICENSE', 'THIRD_PARTY.md') { Copy-Item "$root\$f" $stage }

$zip = "$root\dist\MobVozIA-$version.zip"
Remove-Item $zip -ErrorAction SilentlyContinue
Compress-Archive -Path "$stage\*" -DestinationPath $zip
Remove-Item -Recurse -Force "$root\dist\stage"
# o botao "Baixar" da pagina serve este arquivo direto do proprio site (sem passar pelo GitHub)
New-Item -ItemType Directory -Force "$root\site\downloads" | Out-Null
Copy-Item $zip "$root\site\downloads\" -Force
$size = [math]::Round((Get-Item $zip).Length / 1MB, 2)
Write-Host "OK: $zip ($size MB)"
