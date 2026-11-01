# MobVoz IA: instala tudo que da para automatizar (Python, Fabric, pasta de jogo, 3 mods, servico de IA).
# Rodado pelo 1-INSTALAR.bat. Pode rodar de novo sem problema. So ASCII neste arquivo (o PowerShell 5 nao le acento sem BOM).
param([switch]$Leve)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

function Passo($t) { Write-Host "`n=== $t" -ForegroundColor Green }

function Atualizar-Path {
    $m = [Environment]::GetEnvironmentVariable('Path', 'Machine'); $u = [Environment]::GetEnvironmentVariable('Path', 'User')
    $env:Path = "$m;$u"
}

. "$PSScriptRoot\ai-service\python-compativel.ps1"

Passo "1/3 Python (3.10 a 3.14; o 3.15 ainda nao serve para as bibliotecas de voz)"
$achado = Achar-Python
if (-not $achado) {
    # Sem winget (muito Windows 10 nao tem): baixa o instalador oficial do Python 3.12 em python.org e instala so para o seu usuario,
    # ao lado de qualquer outro Python (nao mexe no PATH nem nos seus outros Pythons). Nao pede senha de administrador.
    Write-Host "Nao ha um Python compativel neste PC. Baixando o Python 3.12 de python.org (uns 27 MB)..."
    $alvo = if ($env:MOBVOZ_PY_TARGET) { $env:MOBVOZ_PY_TARGET } else { "$env:LOCALAPPDATA\Programs\Python\Python312" }
    $exe = Join-Path $env:TEMP "python-3.12.10-amd64.exe"
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    try { Invoke-WebRequest "https://www.python.org/ftp/python/3.12.10/python-3.12.10-amd64.exe" -OutFile $exe -UseBasicParsing }
    catch { throw "Nao consegui baixar o Python 3.12 (sem internet?). Instale-o em python.org/downloads/release/python-3120/ e de dois cliques no 1-INSTALAR.bat de novo." }
    Write-Host "Instalando o Python 3.12 (alguns minutos, sem janela)..."
    $r = Start-Process $exe -ArgumentList "/quiet", "InstallAllUsers=0", "PrependPath=0", "Include_launcher=0", "Include_test=0", "Include_doc=0", "Shortcuts=0", "AssociateFiles=0", "TargetDir=`"$alvo`"" -Wait -PassThru
    Remove-Item $exe -ErrorAction SilentlyContinue
    $env:MOBVOZ_PYTHON = Join-Path $alvo "python.exe"
    $achado = Achar-Python
    if (-not $achado) { throw "O Python 3.12 nao foi instalado (codigo $($r.ExitCode)). Instale-o em python.org/downloads/release/python-3120/ (marque 'Add python.exe to PATH') e de dois cliques no 1-INSTALAR.bat de novo." }
}
$py = $achado.Exe
Write-Host "Python $($achado.Ver): $py"
$env:MOBVOZ_PYTHON = $py   # o instalar-sidecar.ps1 usa o mesmo

Passo "2/3 Minecraft: Fabric, pasta de jogo e mods"
& $py "$PSScriptRoot\instalar_jogo.py"
if ($LASTEXITCODE -ne 0) { throw "A preparacao do Minecraft falhou (veja a mensagem acima)." }

Passo "3/3 Servico de IA (vozes e reconhecimento de fala; uns 1,2 GB, leva alguns minutos)"
if ($Leve) { & "$PSScriptRoot\ai-service\instalar-sidecar.ps1" -Leve } else { & "$PSScriptRoot\ai-service\instalar-sidecar.ps1" }

Write-Host "`n=== TUDO PRONTO!" -ForegroundColor Cyan
Write-Host "Proximo passo: escolha a IA que conversa:"
Write-Host "  - Sua propria API (recomendado): de dois cliques em 2-CONFIGURAR-API.bat"
Write-Host "  - Modelo local gratis: instale o Ollama (ollama.com/download) e rode:  ollama pull qwen3:4b"
Write-Host "Depois, de dois cliques em 3-LIGAR-SERVICO.bat e deixe a janela aberta enquanto joga."
