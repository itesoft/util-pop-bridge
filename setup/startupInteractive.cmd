@ECHO OFF

IF "%OS%" == "Windows_NT" SETLOCAL
set CURRENT_DIR=%~dps0
SET CLASSPATH=%CURRENT_DIR%\lib
SET JDK_1_8_0=%CURRENT_DIR%\lib

rem Make sure prerequisite environment variables are set
if not "%JAVA_HOME%" == "" goto gotCustomJavaHome

:gotCustomJavaHome
set JAVA_HOME=C:\Program Files\Java\jre1.8.0_151

rem Try to use the server jvm
set JVM="%JAVA_HOME%\bin\java.exe"
if exist %JVM% goto foundJvm
echo Warning: JVM: %JVM% was not found at JAVA_HOME: %JAVA_HOME%.
goto endProgram
:foundJvm
echo Using JVM:              "%JVM%"
%JVM% -Xms128m -Xmx256m -jar util-pop-bridge-1.0.0.jar -d MSGRAPH -p 111 -o authflow=PASSWORD -o clientid=abb692c4-ce68-4e00-887f-d2d11b244c98 -o tenant=itesoft.com

pause

exit /b %errorlevel%
:endProgram
echo End Program........
ENDLOCAL
