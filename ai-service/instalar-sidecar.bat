@echo off
rem Duplo clique: instala o servico de IA (qualquer pasta serve, inclusive Downloads). Use "instalar-sidecar.bat -Leve" para o modo leve.
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0instalar-sidecar.ps1" %*
echo.
pause
