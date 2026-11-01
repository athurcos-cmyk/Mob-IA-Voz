# Instala o servico de IA (sidecar) do MobVoz num Windows limpo: ambiente Python, bibliotecas, vozes e modelo de reconhecimento de fala.
# Rode (a partir da pasta ai-service):   powershell -ExecutionPolicy Bypass -File .\instalar-sidecar.ps1
# Modo leve (so a voz Piper, sem o Kokoro):  ... -File .\instalar-sidecar.ps1 -Leve
# Precisa de: Python 3.10 a 3.14 (testado com 3.14; o 3.15 ainda NAO serve) e ~1,5 GB de disco livre.
param([switch]$Leve)
$ErrorActionPreference = 'Stop'
$env:HF_HUB_DISABLE_SYMLINKS_WARNING = '1'  # aviso inofensivo do Hugging Face no Windows sem modo desenvolvedor
Set-Location $PSScriptRoot

function Passo($t) { Write-Host "`n==> $t" -ForegroundColor Green }
function Rodar($exe, [string[]]$argumentos) {
    & $exe @argumentos
    if ($LASTEXITCODE -ne 0) { throw "Falhou: $exe $($argumentos -join ' ') (codigo $LASTEXITCODE)" }
}

. "$PSScriptRoot\python-compativel.ps1"
$achado = Achar-Python
if (-not $achado) {
    throw "Nao achei um Python compativel (3.10 a 3.14). Se voce tem o Python 3.15 ou mais novo, ele ainda NAO serve (faltam pacotes prontos das bibliotecas de voz): instale o Python 3.12 em python.org/downloads ou rode no PowerShell:  winget install -e --id Python.Python.3.12   e depois rode este script de novo."
}
$py = $achado.Exe
Write-Host "Python $($achado.Ver) encontrado: $py"

Passo "Criando o ambiente (.venv)"
if (Test-Path .venv\Scripts\python.exe) {
    # um .venv feito com Python incompativel (ex.: 3.15) nao serve: apaga e refaz
    $vv = Versao-Python '.venv\Scripts\python.exe'
    if (-not $vv -or $vv -lt $script:PyMin -or $vv -gt $script:PyMax) { Write-Host "Refazendo o ambiente (o anterior usava Python $vv)."; Remove-Item -Recurse -Force .venv }
}
if (-not (Test-Path .venv\Scripts\python.exe)) { Rodar $py @('-m', 'venv', '.venv') }
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

Write-Host "`nPronto! Proximo passo: instale o Ollama e rode 'ollama pull qwen3:4b' (IA local), OU configure a sua chave de API (dois cliques em 2-CONFIGURAR-API.bat)." -ForegroundColor Cyan
Write-Host "Para ligar o servico: de dois cliques em 3-LIGAR-SERVICO.bat (na pasta do pacote)."
