@echo off
rem Liga o servico de IA usando a SUA API (DeepSeek etc.) em vez do modelo local. A chave vem de %USERPROFILE%\.mobvoz\api.json
rem (crie com: powershell -ExecutionPolicy Bypass -File configurar-api.ps1). Voz e reconhecimento de fala continuam locais.
setlocal
cd /d "%~dp0"
set MOBVOZ_PROVIDER=api
if not exist "%USERPROFILE%\.mobvoz\api.json" (
  echo Falta a chave da API. Rode antes: powershell -ExecutionPolicy Bypass -File configurar-api.ps1
  exit /b 1
)
if not exist ".venv\Scripts\python.exe" (
  echo Rode primeiro: powershell -ExecutionPolicy Bypass -File instalar-sidecar.ps1
  exit /b 1
)
".venv\Scripts\python.exe" -m app.api %*
