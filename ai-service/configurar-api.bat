@echo off
rem Duplo clique: guarda a sua chave de API (so no seu PC, em %USERPROFILE%\.mobvoz\api.json).
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0configurar-api.ps1"
echo.
pause
