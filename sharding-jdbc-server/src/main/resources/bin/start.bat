@echo off & setlocal enabledelayedexpansion

cd %~dp0

set LIB_JARS="..\lib\*"

set PORT=%1

if "%PORT%"=="" (
set MAIN_CLASS=io.shardingjdbc.server.Bootstrap
) else (
set MAIN_CLASS=io.shardingjdbc.server.Bootstrap %PORT%
)

java -server -Xmx2g -Xms2g -Xmn256m -XX:PermSize=128m -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 -classpath ..;%LIB_JARS% %MAIN_CLASS%

pause
