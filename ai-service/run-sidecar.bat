@echo off
setlocal
set HF_HUB_DISABLE_SYMLINKS_WARNING=1
cd /d "%~dp0"
if exist ".venv\Scripts\python.exe" (
  ".venv\Scripts\python.exe" -m app.api %*
  echo.
  echo O servico parou. Se foi por erro, leia a mensagem acima.
  pause
) else (
  echo Rode primeiro o 1-INSTALAR.bat, com dois cliques.
  pause
  exit /b 1
)
