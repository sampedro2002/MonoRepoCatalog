@echo off
setlocal
set DIRNAME=%~dp0
set APP_HOME=%DIRNAME%
set JAVA_EXE=java.exe
if defined JAVA_HOME set JAVA_EXE=%JAVA_HOME%\bin\java.exe
if not exist "%JAVA_EXE%" (
  echo ERROR: No se encontro Java. Define JAVA_HOME.
  exit /b 1
)
set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
"%JAVA_EXE%" "-Xmx64m" "-Xms64m" "-Dorg.gradle.appname=gradlew" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
endlocal
