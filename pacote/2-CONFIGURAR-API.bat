@echo off
rem Duplo clique (so se for usar a SUA API de IA, como a DeepSeek): guarda a chave so no seu PC.
title MobVoz IA - chave da API
cd /d "%~dp0ai-service"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0ai-service\configurar-api.ps1"
echo.
pause
