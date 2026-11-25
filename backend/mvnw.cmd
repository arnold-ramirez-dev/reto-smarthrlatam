@echo off
setlocal
set "BASE_DIR=%~dp0"
set "WRAPPER_DIR=%BASE_DIR%.mvn\wrapper"
set "MAVEN_HOME=%WRAPPER_DIR%\apache-maven-3.9.9"
set "MAVEN_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip"

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  if not exist "%WRAPPER_DIR%" mkdir "%WRAPPER_DIR%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference = 'SilentlyContinue'; Invoke-WebRequest -UseBasicParsing '%MAVEN_URL%' -OutFile '%WRAPPER_DIR%\maven.zip'"
  if errorlevel 1 exit /b %errorlevel%
  tar -xf "%WRAPPER_DIR%\maven.zip" -C "%WRAPPER_DIR%"
  if errorlevel 1 exit /b %errorlevel%
  del /q "%WRAPPER_DIR%\maven.zip"
)

call "%MAVEN_HOME%\bin\mvn.cmd" %*
exit /b %errorlevel%

