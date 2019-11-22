@echo off
::---------------------------------------------
::- Author: Itesoft
::- Date: 11/2019
::- http://www.itesoft.com
::---------------------------------------------
::- Arguments : 1 install/remove
::-				2 CLIENTID
::-				3 TENANT
::-  			4 PORT
::---------------------------------------------

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

::---------------------------------------------
:: -- Update this section to match your needs
::---------------------------------------------

::-- 1. This name should match the name you gave to the prunsrv executable
set SERVICE_NAME=Itesoft.Util-pop-bridge
set SERVICE_BASE_NAME=It_InteractiveSvc
set EXECUTABLE=%CURRENT_DIR%\%SERVICE_BASE_NAME%.exe

::-- 2. The fully qualified start and stop classes
set CG_START_CLASS=com.itesoft.contrib.popbridge.Main
set CG_STOP_CLASS=%CG_START_CLASS%

::-- 3. The start and stop methods for the class(es) in 2 above
set CG_START_METHOD=main
set CG_STOP_METHOD=main

::-- 4. and their respective arguments/params if any
set CG_START_PARAMS=start
set CG_STOP_PARAMS=stop

::-- 5. the classpath for all jars needed to run your service
set nbFile=0
for /f "tokens=*" %%G in ('dir /b /o "util-pop-bridge*.jar" ') do set appname=%%G
set jarFile=%APPLICATION_SERVICE_HOME%\%appname%

set CG_PATH_TO_JAR_CONTAINING_SERVICE=%jarFile%

::-- 6. Set to auto if you want the service to startup automatically. The default is manual
set CG_STARTUP_TYPE=auto
::-- 7. Set this if you want to use a different JVM than configured in your registry, or if it is not configured in windows registry
set EXEC_MODE=java
:descOk
set PR_DESCRIPTION=Itesoft.Util- POP BRIDGE RELAY SERVER
set PR_INSTALL=%EXECUTABLE%
set PR_LOGPATH=%APPLICATION_SERVICE_HOME%\logs
set PR_CLASSPATH=%APPLICATION_SERVICE_HOME%;%APPLICATION_SERVICE_HOME%\lib;%CG_PATH_TO_JAR_CONTAINING_SERVICE%

::-- 8. Set default program port
set PR_PORT=111

::-- 9. Set Driver (GMAIL, MSGRAPH, EWS)
set PR_DRIVER=MSGRAPH

::-- 10. Set Authentication Flow (PASSWORD, INTERACTIVE, IWA, CODE)
set PR_AUTHENTICATION_FLOW=PASSWORD

if "%1" == "" goto displayUsage
if /i %1 == install goto install
if /i %1 == remove goto  remove


:remove
::---- Remove the service -------
"%EXECUTABLE%" delete %SERVICE_NAME% --LogLevel=%LOG_LEVEL% --LogPrefix=%SERVICE_BASE_NAME%
echo The service '%SERVICE_NAME%' has been removed
goto end

:install
::---- Install the Service -------
echo Installing service '%SERVICE_NAME%' ..
echo.

::--- Client ID
if "%2" == "" goto stepClientIdRequired
set PR_CLIENT_ID=%~2
echo Installing with ClientID  '%PR_CLIENT_ID%' ..

::--- Tenant
if "%3" == "" goto stepTenantRequired
set PR_TENANT=%~3
echo Installing with Tenant  '%PR_TENANT%' ..

::--- Program Port
if "%4" == "" goto nextStepInstall
set PR_PORT=%~4
echo Installing with program Port  '%PR_TENANT%' ..
:nextStepInstall

set CG_STOP_PARAMS=stop

set EXECUTE_STRING= "%EXECUTABLE%" install %SERVICE_NAME% ^
--Description="%PR_DESCRIPTION%" ^
--DisplayName="%PR_DESCRIPTION%" ^
--Install="%EXECUTABLE%" ^
--StdOutput auto ^
--StdError auto ^
--Classpath="%PR_CLASSPATH%" ^
--StartMode %EXEC_MODE% ^
--StopMode %EXEC_MODE% ^
--Startup %CG_STARTUP_TYPE% ^
--StartPath "%APPLICATION_SERVICE_HOME%" ^
--StopPath "%APPLICATION_SERVICE_HOME%" ^
--StartClass %CG_START_CLASS% ^
--StopClass %CG_STOP_CLASS% ^
--StartMethod %CG_START_METHOD% --StopMethod  %CG_STOP_METHOD% ^
++StartParams "-d#%PR_DRIVER%" ^
++StartParams "-p#%PR_PORT%" ^
++StartParams "-o#clientid=%PR_CLIENT_ID%" ^
++StartParams "-o#tenant=%PR_TENANT%" ^
++StartParams "-o#authflow=%PR_AUTHENTICATION_FLOW%" ^
::++StartParams "-o#dataStoreDir=%APPLICATION_SERVICE_HOME%\secret-driver\msgraph" ^
--StopParams %CG_STOP_PARAMS%  ^
++JvmOptions "-Dfile.encoding=UTF8" ^
++JvmOptions "-Djava.net.preferIPv4Stack=true" ^
++JvmOptions "-Djava.io.tmpdir=%APPLICATION_SERVICE_HOME%\temp" ^
++JvmOptions "-Duser.timezone=GMT" ^
++JvmOptions "-Xms512m" ^
++JvmOptions "-Xmx512m" ^
--StopTimeout 60

::-- ++DependsOn Tcpip
call:executeAndPrint %EXECUTE_STRING%

echo.
echo The service '%SERVICE_NAME%' has been installed.

Net start %SERVICE_NAME%
goto end

:stepClientIdRequired
echo client_id: is Missing in argument line
goto displayUsage

:stepTenantRequired
echo Tenant: is Missing in argument line
goto displayUsage

:displayUsage
echo Usage: service.cmd install/remove [client_id] [tenant] [program_port]
goto end

::--------
::- Functions
::-------
:executeAndPrint
%*
echo %*

goto:eof

:end
EXIT /B %ERRORLEVEL%