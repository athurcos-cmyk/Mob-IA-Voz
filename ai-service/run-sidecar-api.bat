@echo off
rem Liga o servico de IA usando a SUA API (DeepSeek etc.) em vez do modelo local. A chave vem de %USERPROFILE%\.mobvoz\api.json
rem (crie com: powershell -ExecutionPolicy Bypass -File configurar-api.ps1). Voz e reconhecimento de fala continuam locais.
setlocal
set HF_HUB_DISABLE_SYMLINKS_WARNING=1
cd /d "%~dp0"
set MOBVOZ_PROVIDER=api
if not exist "%USERPROFILE%\.mobvoz\api.json" (
  echo Falta a chave da API. De dois cliques em 2-CONFIGURAR-API.bat e cole a chave.
  pause
  exit /b 1
)
if not exist ".venv\Scripts\python.exe" (
  echo Rode primeiro o 1-INSTALAR.bat, com dois cliques.
  pause
  exit /b 1
)
".venv\Scripts\python.exe" -m app.api %*
echo.
echo O servico parou. Se foi por erro, leia a mensagem acima.
pause
