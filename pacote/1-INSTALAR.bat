@echo off
rem Duplo clique: instala tudo que da para automatizar (Python, Fabric, pasta de jogo, 3 mods, servico de IA).
rem Para um PC fraco (so a voz leve), rode pelo PowerShell:  1-INSTALAR.bat -Leve
title MobVoz IA - instalacao
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0instalar-tudo.ps1" %*
echo.
pause
