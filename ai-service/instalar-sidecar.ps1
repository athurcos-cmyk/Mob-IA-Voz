# Instala o servico de IA (sidecar) do MobVoz num Windows limpo: ambiente Python, bibliotecas, vozes e modelo de reconhecimento de fala.
# Rode (a partir da pasta ai-service):   powershell -ExecutionPolicy Bypass -File .\instalar-sidecar.ps1
# Modo leve (so a voz Piper, sem o Kokoro):  ... -File .\instalar-sidecar.ps1 -Leve
# Precisa de: Python 3.10 a 3.14 (testado com 3.14; o 3.15 ainda NAO serve) e ~1,5 GB de disco livre.
# O ambiente (venv) e as vozes ficam numa pasta FIXA do seu usuario (%USERPROFILE%\.mobvoz\sidecar), fora da pasta do pacote:
# assim uma versao nova do MobVoz, extraida em qualquer pasta, reaproveita tudo e nao baixa nada de novo. So ASCII neste arquivo.
param([switch]$Leve)
$ErrorActionPreference = 'Stop'
$env:HF_HUB_DISABLE_SYMLINKS_WARNING = '1'  # aviso inofensivo do Hugging Face no Windows sem modo desenvolvedor
Set-Location $PSScriptRoot

function Passo($t) { Write-Host "`n==> $t" -ForegroundColor Green }
function Rodar($exe, [string[]]$argumentos) {
    & $exe @argumentos
    if ($LASTEXITCODE -ne 0) { throw "Falhou: $exe $($argumentos -join ' ') (codigo $LASTEXITCODE)" }
}
function Funciona($exe, [string[]]$argumentos) {  # roda sem barulho e diz se deu certo
    # No PowerShell 5, com $ErrorActionPreference = 'Stop', qualquer linha que o programa escreva em stderr (o traceback do Python quando o modelo ainda
    # nao foi baixado) vira um erro FATAL, mesmo com 2>$null. Aqui o erro e esperado e so queremos o codigo de saida.
    $antes = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        & $exe @argumentos 2>&1 | Out-Null
        return ($LASTEXITCODE -eq 0)
    } finally {
        $ErrorActionPreference = $antes
    }
}

. "$PSScriptRoot\python-compativel.ps1"
$achado = Achar-Python
if (-not $achado) {
    throw "Nao achei um Python compativel (3.10 a 3.14). Se voce tem o Python 3.15 ou mais novo, ele ainda NAO serve (faltam pacotes prontos das bibliotecas de voz): instale o Python 3.12 em python.org/downloads ou rode no PowerShell:  winget install -e --id Python.Python.3.12   e depois rode este script de novo."
}
$py = $achado.Exe
Write-Host "Python $($achado.Ver) encontrado: $py"

$casa = if ($env:MOBVOZ_HOME) { $env:MOBVOZ_HOME } else { Join-Path $env:USERPROFILE '.mobvoz' }
$fixa = Join-Path $casa 'sidecar'
$venv = Join-Path $fixa 'venv'
$modelos = Join-Path $fixa 'models'
New-Item -ItemType Directory -Force $modelos | Out-Null
$env:MOBVOZ_MODELS = $modelos  # o baixar_kokoro.py usa a mesma pasta
Write-Host "Ambiente e vozes ficam em: $fixa (reaproveitados por qualquer versao do MobVoz)"

Passo "Ambiente Python"
$vpy = Join-Path $venv 'Scripts\python.exe'
if (Test-Path $vpy) {
    # um venv feito com Python incompativel (ex.: 3.15) nao serve: apaga e refaz
    $vv = Versao-Python $vpy
    if (-not $vv -or $vv -lt $script:PyMin -or $vv -gt $script:PyMax) { Write-Host "Refazendo o ambiente (o anterior usava Python $vv)."; Remove-Item -Recurse -Force $venv }
}
if (-not (Test-Path $vpy)) { Rodar $py @('-m', 'venv', $venv) } else { Write-Host "ja existe: pulando" }

Passo "Bibliotecas"
$libs = @('faster-whisper==1.2.1', 'piper-tts==1.8.0', 'scipy', 'numpy')
$mods = 'faster_whisper, piper, scipy, numpy'
if (-not $Leve) { $libs += 'kokoro-onnx==0.4.7'; $mods += ', kokoro_onnx' }
$marca = Join-Path $venv 'mobvoz-libs.txt'
$igual = (Test-Path $marca) -and ((Get-Content $marca -Raw).Trim() -eq ($libs -join ','))
if ($igual -and (Funciona $vpy @('-c', "import $mods"))) {
    Write-Host "ja instaladas (mesmas versoes): pulando"
} else {
    Write-Host "Instalando as bibliotecas (alguns minutos)"
    Rodar $vpy @('-m', 'pip', 'install', '--upgrade', 'pip', '--quiet')
    Rodar $vpy (@('-m', 'pip', 'install') + $libs)
    Set-Content -Path $marca -Value ($libs -join ',') -Encoding ASCII
}

Passo "Vozes do Piper (modo leve, ~190 MB)"
$vozes = 'pt_BR-faber-medium', 'pt_BR-cadu-medium', 'pt_BR-jeff-medium'
$faltam = $vozes | Where-Object { -not (Test-Path (Join-Path $modelos "$_.onnx")) }
if ($faltam) { Rodar $vpy (@('-m', 'piper.download_voices') + $vozes + @('--data-dir', $modelos)) } else { Write-Host "ja baixadas: pulando" }

if (-not $Leve) {
    Passo "Voz principal, Kokoro (~120 MB, hash conferido; pula se ja estiver ok)"
    Rodar $vpy @('baixar_kokoro.py')
}

Passo "Modelos de reconhecimento de fala (base ~150 MB, rapido; small ~460 MB, de reserva quando o rapido fica em duvida)"
foreach ($m in 'base', 'small') {
    if (Funciona $vpy @('-c', "from faster_whisper import WhisperModel; WhisperModel('$m', device='cpu', compute_type='int8', local_files_only=True)")) {
        Write-Host "$m ja baixado: pulando"
    } else {
        Rodar $vpy @('-c', "from faster_whisper import WhisperModel; WhisperModel('$m', device='cpu', compute_type='int8'); print('$m ok')")
    }
}

Write-Host "`nPronto! Proximo passo: instale o Ollama e rode 'ollama pull qwen3:4b' (IA local), OU configure a sua chave de API (dois cliques em 2-CONFIGURAR-API.bat)." -ForegroundColor Cyan
Write-Host "Para ligar o servico: de dois cliques em 3-LIGAR-SERVICO.bat (na pasta do pacote)."
