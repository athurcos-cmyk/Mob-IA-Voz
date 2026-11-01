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
    Write-Host "Nao ha um Python compativel neste PC. Instalando o Python 3.12 pelo winget (fica ao lado do que voce ja tem; pode pedir permissao)..."
    if (-not (Get-Command winget -ErrorAction SilentlyContinue)) {
        throw "Nao achei um Python compativel e nem o winget. Instale o Python 3.12 em python.org/downloads (marque 'Add python.exe to PATH') e rode o 1-INSTALAR.bat de novo."
    }
    winget install -e --id Python.Python.3.12 --silent --accept-package-agreements --accept-source-agreements
    Atualizar-Path
    $achado = Achar-Python
    if (-not $achado) { throw "O Python 3.12 nao ficou disponivel. Feche esta janela e de dois cliques no 1-INSTALAR.bat de novo (o Windows precisa recarregar o PATH) ou instale o 3.12 em python.org/downloads." }
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
