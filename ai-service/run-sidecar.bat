@echo off
setlocal
cd /d "%~dp0"
if exist ".venv\Scripts\python.exe" (
  ".venv\Scripts\python.exe" -m app.api %*
) else (
  echo Crie o venv primeiro: veja ai-service\README.md
  exit /b 1
)
