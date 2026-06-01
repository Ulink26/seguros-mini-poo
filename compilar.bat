@echo off
setlocal
set "JAVAC=javac"
where javac >nul 2>nul
if errorlevel 1 set "JAVAC=C:\Program Files (x86)\Minecraft Launcher\runtime\java-runtime-gamma\windows-x64\java-runtime-gamma\bin\javac.exe"
if not exist "%JAVAC%" (
  echo No se encontro javac. Instala un JDK 8 o superior y agrega javac al PATH.
  exit /b 1
)
if not exist out mkdir out
"%JAVAC%" -encoding UTF-8 -source 8 -target 8 -d out src\seguros\*.java src\seguros\model\*.java src\seguros\persistence\*.java src\seguros\service\*.java src\seguros\ui\*.java
if errorlevel 1 exit /b 1
echo Compilacion terminada.
