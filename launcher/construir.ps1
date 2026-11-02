# Compila o MobVozIA.exe com o csc.exe que ja vem no Windows (.NET Framework 4; nao precisa instalar nada). So ASCII.
# Uso: powershell -ExecutionPolicy Bypass -File launcher\construir.ps1 [-Payload caminho\componentes.zip]   (gera launcher\build\MobVozIA.exe)
# Com -Payload, os componentes (mod, scripts, servico de IA) ficam DENTRO do exe e o pacote e so o programa.
param([string]$Payload = '')
$ErrorActionPreference = 'Stop'
$aqui = Split-Path -Parent $MyInvocation.MyCommand.Path
$csc = Join-Path $env:WINDIR 'Microsoft.NET\Framework64\v4.0.30319\csc.exe'
if (-not (Test-Path $csc)) { throw "csc.exe nao encontrado em $csc" }
New-Item -ItemType Directory -Force "$aqui\build" | Out-Null
$saida = "$aqui\build\MobVozIA.exe"
$args = @('/nologo', '/target:winexe', '/optimize+', "/out:$saida", "/win32icon:$aqui\icone.ico", "/win32manifest:$aqui\app.manifest",
    '/reference:System.Windows.Forms.dll', '/reference:System.Drawing.dll', '/reference:System.IO.Compression.dll')
if ($Payload) { $args += "/resource:$Payload,componentes.zip" }
& $csc @args "$aqui\MobVozIA.cs" "$aqui\AssemblyInfo.cs"
if ($LASTEXITCODE -ne 0) { throw "A compilacao falhou (veja os erros acima)" }
Write-Host "OK: $saida ($([math]::Round((Get-Item $saida).Length / 1KB)) KB)"
