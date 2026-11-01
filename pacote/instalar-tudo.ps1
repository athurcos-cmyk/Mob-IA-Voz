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

function Achar-Python {
    # ignora o "atalho" da Microsoft Store (WindowsApps), que abre a loja em vez de rodar o Python
    $cands = @()
    $c = Get-Command python -ErrorAction SilentlyContinue
    if ($c -and $c.Source -notlike '*WindowsApps*') { $cands += $c.Source }
    $cands += Get-ChildItem "$env:LOCALAPPDATA\Programs\Python\Python3*\python.exe" -ErrorAction SilentlyContinue | Sort-Object FullName -Descending | ForEach-Object FullName
    $cands += Get-ChildItem "$env:ProgramFiles\Python3*\python.exe" -ErrorAction SilentlyContinue | Sort-Object FullName -Descending | ForEach-Object FullName
    foreach ($p in $cands) {
        try { $v = [version](& $p -c "import sys; print('%d.%d' % sys.version_info[:2])").Trim() } catch { continue }
        if ($v -ge [version]'3.10') { return $p }
    }
    return $null
}

Passo "1/3 Python"
$py = Achar-Python
if (-not $py) {
    Write-Host "Python nao encontrado. Instalando o Python 3.12 pelo winget (pode pedir permissao)..."
    if (-not (Get-Command winget -ErrorAction SilentlyContinue)) {
        throw "Nao achei o Python e nem o winget. Instale o Python em python.org/downloads (marque 'Add python.exe to PATH') e rode o 1-INSTALAR.bat de novo."
    }
    winget install -e --id Python.Python.3.12 --silent --accept-package-agreements --accept-source-agreements
    Atualizar-Path
    $py = Achar-Python
    if (-not $py) { throw "O Python nao ficou disponivel. Feche esta janela, abra o 1-INSTALAR.bat de novo (o Windows precisa recarregar o PATH) ou instale em python.org/downloads." }
}
Write-Host "Python: $py"
# as proximas etapas usam este Python: coloca a pasta dele na frente do PATH
$env:Path = (Split-Path $py) + ";" + (Split-Path $py) + "\Scripts;" + $env:Path

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
