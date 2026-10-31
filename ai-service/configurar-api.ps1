# Guarda a SUA chave de API (DeepSeek ou outra compativel com OpenAI) para o MobVoz usar. A chave fica so no seu computador,
# em %USERPROFILE%\.mobvoz\api.json (fora do projeto), e so e usada por run-sidecar-api.bat.
# Rode:   powershell -ExecutionPolicy Bypass -File .\configurar-api.ps1
$ErrorActionPreference = 'Stop'
$pasta = Join-Path $env:USERPROFILE '.mobvoz'
$arq = Join-Path $pasta 'api.json'
New-Item -ItemType Directory -Force $pasta | Out-Null
$seguro = Read-Host 'Cole a sua chave de API (o texto nao aparece enquanto voce cola)' -AsSecureString
$ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($seguro)
try { $chave = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) } finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }
$chave = $chave.Trim()
if ($chave.Length -lt 16) { throw 'Essa chave parece curta demais. Confira e rode de novo.' }
@{ key = $chave } | ConvertTo-Json | Set-Content -Path $arq -Encoding UTF8
Write-Host "Chave guardada em $arq" -ForegroundColor Green
Write-Host "Ligue o servico com run-sidecar-api.bat. NUNCA compartilhe esse arquivo nem poste a chave."
