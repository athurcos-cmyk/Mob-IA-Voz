@echo off
rem Duplo clique (OPCIONAL, so se quiser vozes na nuvem do Azure): guarda a chave so no seu PC.
title MobVoz IA - voz na nuvem (Azure)
cd /d "%~dp0ai-service"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0ai-service\configurar-voz-nuvem.ps1"
echo.
pause
