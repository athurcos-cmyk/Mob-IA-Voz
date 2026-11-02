# Guarda a SUA chave do Azure Speech (voz na nuvem, opcional) para o MobVoz usar. Fica so no seu computador, em
# %USERPROFILE%\.mobvoz\azure.json (fora do projeto). Antes de gravar, confere a chave com o Azure (essa consulta e gratis).
# Rode:   powershell -ExecutionPolicy Bypass -File .\configurar-voz-nuvem.ps1
$ErrorActionPreference = 'Stop'
$pasta = Join-Path $env:USERPROFILE '.mobvoz'
$arq = Join-Path $pasta 'azure.json'
New-Item -ItemType Directory -Force $pasta | Out-Null
$regiao = (Read-Host 'Regiao do recurso (ex.: brazilsouth ou eastus; Enter = brazilsouth)').Trim().ToLower().Replace(' ', '')
if (-not $regiao) { $regiao = 'brazilsouth' }
$seguro = Read-Host 'Cole a CHAVE 1 do Azure (o texto nao aparece enquanto voce cola)' -AsSecureString
$ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($seguro)
try { $chave = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) } finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }
$chave = $chave.Trim()
if ($chave.Length -lt 20) { throw 'Essa chave parece curta demais. Confira e rode de novo.' }
try {
    $v = Invoke-RestMethod -Uri "https://$regiao.tts.speech.microsoft.com/cognitiveservices/voices/list" -Headers @{ 'Ocp-Apim-Subscription-Key' = $chave } -TimeoutSec 20
    $n = @($v | Where-Object { $_.Locale -eq 'pt-BR' }).Count
    Write-Host "A chave funciona. O Azure tem $n vozes pt-BR nessa regiao." -ForegroundColor Green
} catch {
    Write-Host 'O Azure recusou essa chave/regiao. Confira os dois em "Chaves e Ponto de Extremidade" do recurso Speech e rode de novo.' -ForegroundColor Red
    throw
}
# sem BOM (o Set-Content -Encoding UTF8 do PowerShell 5 poe um BOM no comeco do arquivo)
[IO.File]::WriteAllText($arq, (@{ key = $chave; region = $regiao } | ConvertTo-Json), (New-Object System.Text.UTF8Encoding $false))
Write-Host "Guardado em $arq" -ForegroundColor Green
Write-Host 'Para usar no jogo: ligue o servico e digite no chat  /mobvoz motorvoz nuvem   (volte com  /mobvoz motorvoz auto).'
Write-Host 'Limite de gasto: 15.000 caracteres por dia (cabe no gratis do Azure). Passou disso, a voz local assume sozinha. NUNCA compartilhe o azure.json.'
