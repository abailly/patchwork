@REM ----------------------------------------------------------------------------
@REM Patchwork startup script
@REM
@REM Needed environment variables:
@REM JAVA_HOME - Path to JDK
@REM 
@REM ----------------------------------------------------------------------------

if not "%JAVA_HOME%" == "" goto OkJHome
echo Error : variable JAVA_HOME not set
goto error

:OkJHome
if exist "%JAVA_HOME%\bin\java.exe" goto endInit
echo Error : variable JAVA_HOME does not points to java.exe
goto error

:endInit
SET JAVA_EXE="%JAVA_HOME%\bin\java.exe"

SET RUNTIME_DIR=%~dp0
%JAVA_EXE% "-Dlauncher.libdir=%RUNTIME_DIR%\..\lib" -Dlauncher.main=oqube.patchwork.TestRunner -jar "%RUNTIME_DIR%\..\patchwork-main-${version}.jar" %*

:error
