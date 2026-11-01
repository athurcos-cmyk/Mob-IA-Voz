@echo off
rem Duplo clique: liga o servico de IA. Deixe esta janela ABERTA enquanto joga.
rem Se voce guardou uma chave de API (2-CONFIGURAR-API.bat), usa a API; senao, usa o modelo local (Ollama).
title MobVoz IA - servico de IA (deixe aberta)
cd /d "%~dp0ai-service"
if exist "%USERPROFILE%\.mobvoz\api.json" goto api
call "%~dp0ai-service\run-sidecar.bat"
goto fim
:api
call "%~dp0ai-service\run-sidecar-api.bat"
:fim
