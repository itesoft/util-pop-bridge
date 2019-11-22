@ECHO OFF

::---------------------------------------------
::- Author: Itesoft
::- Date: 11/2019
::- http://www.itesoft.com
::---------------------------------------------
::- Arguments : 
::-				1 CLIENTID
::-				2 TENANT
::-  			3 PORT
::

:: Check permission level
echo Detecting permissions...
net session >nul 2>&1
if %ERRORLEVEL%==0 (
	echo Administravive permissions granted
	) else (
		echo Administative permission required. Plase restart with administrative right
		goto :end
	)

:: Continue if admin right ok

if "%OS%" == "Windows_NT" setlocal

set "CURRENT_DIR=%~dp0%"
set "APPLICATION_SERVICE_HOME=%CURRENT_DIR:~0,-1%"
echo Current Directory : %APPLICATION_SERVICE_HOME%
cd "%CURRENT_DIR%"

::-- 1. Set default program port
set PR_PORT=111

::-- 2. Set Driver (GMAIL, MSGRAPH, EWS)
set PR_DRIVER=MSGRAPH

::-- 3. Set Authentication Flow (PASSWORD, INTERACTIVE, IWA, CODE)
set PR_AUTHENTICATION_FLOW=PASSWORD

rem Make sure prerequisite environment variables are set
if not "%JAVA_HOME%" == "" goto gotCustomJavaHome

:gotCustomJavaHome
set JAVA_HOME=C:\Program Files\Java\jre1.8.0_151

::--- Client ID
if "%1" == "" goto stepClientIdRequired
set PR_CLIENT_ID=%~1
echo Installing with ClientID  '%PR_CLIENT_ID%' ..

::--- Tenant
if "%2" == "" goto stepTenantRequired
set PR_TENANT=%~2
echo Installing with Tenant  '%PR_TENANT%' ..

::--- Program Port
if "%3" == "" goto nextStepInstall
set PR_PORT=%~2
echo Installing with program Port  '%PR_TENANT%' ..
:nextStepInstall

rem Try to use the server jvm
set JVM="%JAVA_HOME%\bin\java.exe"
if exist %JVM% goto foundJvm
echo Warning: JVM: %JVM% was not found at JAVA_HOME: %JAVA_HOME%.
goto endProgram
:foundJvm
echo Using JVM:              "%JVM%"

for /f "tokens=*" %%G in ('dir /b /o "util-pop-bridge*.jar" ') do set appname=%%G
echo Using JarFile:              "%appname%"

%JVM% -Xms128m -Xmx256m -jar "%appname%" -d MSGRAPH -p "%PR_PORT%" -o authflow=PASSWORD -o clientid="%PR_CLIENT_ID%" -o tenant="%PR_TENANT%"

pause
goto endProgram


:stepClientIdRequired
echo client_id: is Missing in argument line
goto displayUsage

:stepTenantRequired
echo Tenant: is Missing in argument line
goto displayUsage

:displayUsage
echo Usage: startupInteractive.cmd [client_id]* [tenant]* [program_port]
goto endProgram


:endProgram
exit /b %errorlevel%
echo End Program........
ENDLOCAL
