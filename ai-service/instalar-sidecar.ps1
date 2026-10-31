# Instala o servico de IA (sidecar) do MobVoz num Windows limpo: ambiente Python, bibliotecas, vozes e modelo de reconhecimento de fala.
# Rode (a partir da pasta ai-service):   powershell -ExecutionPolicy Bypass -File .\instalar-sidecar.ps1
# Modo leve (so a voz Piper, sem o Kokoro):  ... -File .\instalar-sidecar.ps1 -Leve
# Precisa de: Python 3.10 ou mais novo no PATH (python.org: marque "Add python.exe to PATH") e ~1,5 GB de disco livre.
param([switch]$Leve)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

function Passo($t) { Write-Host "`n==> $t" -ForegroundColor Green }
function Rodar($exe, [string[]]$argumentos) {
    & $exe @argumentos
    if ($LASTEXITCODE -ne 0) { throw "Falhou: $exe $($argumentos -join ' ') (codigo $LASTEXITCODE)" }
}

$py = Get-Command python -ErrorAction SilentlyContinue
if (-not $py) { throw "Python nao encontrado. Instale em python.org (marque 'Add python.exe to PATH') e rode este script de novo." }
$ver = (& python -c "import sys; print('%d.%d' % sys.version_info[:2])").Trim()
if ([version]$ver -lt [version]'3.10') { throw "Python $ver e antigo demais; instale o 3.10 ou mais novo." }
Write-Host "Python $ver encontrado."

Passo "Criando o ambiente (.venv)"
if (-not (Test-Path .venv\Scripts\python.exe)) { Rodar python @('-m', 'venv', '.venv') }
$vpy = '.venv\Scripts\python.exe'

Passo "Instalando as bibliotecas (alguns minutos)"
Rodar $vpy @('-m', 'pip', 'install', '--upgrade', 'pip', '--quiet')
$libs = @('faster-whisper==1.2.1', 'piper-tts==1.8.0', 'scipy', 'numpy')
if (-not $Leve) { $libs += 'kokoro-onnx==0.4.7' }
Rodar $vpy (@('-m', 'pip', 'install') + $libs)

Passo "Baixando as vozes do Piper (modo leve, ~190 MB)"
Rodar $vpy @('-m', 'piper.download_voices', 'pt_BR-faber-medium', 'pt_BR-cadu-medium', 'pt_BR-jeff-medium', '--data-dir', 'models')

if (-not $Leve) {
    Passo "Baixando a voz principal, Kokoro (~120 MB, hash conferido)"
    Rodar $vpy @('baixar_kokoro.py')
}

Passo "Baixando o modelo de reconhecimento de fala (~460 MB)"
Rodar $vpy @('-c', "from faster_whisper import WhisperModel; WhisperModel('small', device='cpu', compute_type='int8'); print('ok')")

Write-Host "`nPronto! Proximo passo: instale o Ollama e rode 'ollama pull qwen3:4b' (IA local), OU configure a sua chave de API com configurar-api.ps1." -ForegroundColor Cyan
Write-Host "Para ligar o servico: run-sidecar.bat (IA local) ou run-sidecar-api.bat (sua API)."
