@echo off
setlocal
cd /d "%~dp0"
if exist ".venv\Scripts\python.exe" (
  ".venv\Scripts\python.exe" -m app.api %*
  echo.
  echo O servico parou. Se foi por erro, leia a mensagem acima.
  pause
) else (
  echo Rode primeiro o instalar-sidecar.bat, com dois cliques.
  pause
  exit /b 1
)
