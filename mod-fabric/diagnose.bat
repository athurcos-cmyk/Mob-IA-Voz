@echo off
setlocal
cd /d "%~dp0"
if exist "%~dp0..\.tools\jdk-21.0.13+9\bin\java.exe" set "JAVA_HOME=%~dp0..\.tools\jdk-21.0.13+9"
set "GRADLE_USER_HOME=%~dp0.gradle-user-home"
echo JAVA_HOME=%JAVA_HOME%
if defined JAVA_HOME ("%JAVA_HOME%\bin\java.exe" -version) else (java -version)
if errorlevel 1 exit /b 1
call "%~dp0gradlew.bat" --version
exit /b %ERRORLEVEL%

