# Gera dist\MobVozIA-<versao>.zip: o pacote de quem so quer baixar e jogar. Leva SO o necessario para funcionar:
#   - pacote\ (1-INSTALAR.bat, 2-CONFIGURAR-API.bat, 3-LIGAR-SERVICO.bat, LEIA-ME.txt e os scripts de instalacao)
#   - mod\mobvoz-<versao>.jar
#   - ai-service\ (so o codigo do servico e os scripts; sem testes, .venv, models, README)
#   - LICENSE e THIRD_PARTY.md (licencas)
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

$stage = "$root\dist\stage\MobVozIA-$version"
Remove-Item -Recurse -Force "$root\dist\stage" -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force "$stage\mod" | Out-Null
Copy-Item $jar "$stage\mod\"
Copy-Item "$root\pacote\*" $stage
robocopy "$root\ai-service" "$stage\ai-service" /E /XD .venv models __pycache__ tests /XF *.pyc README.md requirements.txt /NFL /NDL /NJH /NJS | Out-Null
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
