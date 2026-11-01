@echo off
setlocal
set HF_HUB_DISABLE_SYMLINKS_WARNING=1
cd /d "%~dp0"
rem O ambiente fica numa pasta fixa do usuario (compartilhada entre versoes); .venv local so para instalacoes antigas.
set "MH=%MOBVOZ_HOME%"
if "%MH%"=="" set "MH=%USERPROFILE%\.mobvoz"
set "VPY=%MH%\sidecar\venv\Scripts\python.exe"
if not exist "%VPY%" goto antigo
set "MOBVOZ_MODELS=%MH%\sidecar\models"
goto rodar
:antigo
set "VPY=.venv\Scripts\python.exe"
if not exist "%VPY%" goto semvenv
:rodar
"%VPY%" -m app.api %*
echo.
echo O servico parou. Se foi por erro, leia a mensagem acima.
pause
exit /b 0
:semvenv
echo Rode primeiro o 1-INSTALAR.bat, com dois cliques.
pause
exit /b 1
