@echo off
setlocal
cd /d "%~dp0"
if exist "%~dp0..\.tools\jdk-21.0.13+9\bin\java.exe" set "JAVA_HOME=%~dp0..\.tools\jdk-21.0.13+9"
set "GRADLE_USER_HOME=%~dp0.gradle-user-home"
call "%~dp0gradlew.bat" build %*
exit /b %ERRORLEVEL%

