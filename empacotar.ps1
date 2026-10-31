# Gera dist\MobVozIA-<versao>.zip com: JAR do mod, ai-service (sem .venv, models, caches), docs e licencas.
# NAO inclui: Simple Voice Chat (All Rights Reserved), Piper/modelos de voz, Ollama/modelos de LLM, JDK.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$props = Get-Content "$root\mod-fabric\gradle.properties" | Where-Object { $_ -match '^mod_version=' }
$version = ($props -split '=')[1].Trim()
$jar = "$root\mod-fabric\build\libs\mobvoz-$version.jar"
if (-not (Test-Path $jar)) { throw "Rode primeiro: mod-fabric\build.bat (JAR nao encontrado: $jar)" }

$stage = "$root\dist\stage\MobVozIA-$version"
Remove-Item -Recurse -Force "$root\dist\stage" -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force "$stage\mod" | Out-Null
Copy-Item $jar "$stage\mod\"
robocopy "$root\ai-service" "$stage\ai-service" /E /XD .venv models __pycache__ /XF *.pyc /NFL /NDL /NJH /NJS | Out-Null
robocopy "$root\config" "$stage\config" /E /NFL /NDL /NJH /NJS | Out-Null
robocopy "$root\docs" "$stage\docs" /E /XD audicao_vozes /NFL /NDL /NJH /NJS | Out-Null  # sem os .wav de teste
foreach ($f in 'INSTALL.md','LICENSE','THIRD_PARTY.md','CHANGELOG.md','STATUS.md','README.md') { Copy-Item "$root\$f" $stage }

$zip = "$root\dist\MobVozIA-$version.zip"
Remove-Item $zip -ErrorAction SilentlyContinue
Compress-Archive -Path "$stage\*" -DestinationPath $zip
Remove-Item -Recurse -Force "$root\dist\stage"
$size = [math]::Round((Get-Item $zip).Length / 1MB, 2)
Write-Host "OK: $zip ($size MB)"
