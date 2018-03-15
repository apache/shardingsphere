@echo off & setlocal enabledelayedexpansion

cd %~dp0

set CLASS_PATH="..;..\conf;..\lib\*"

set PORT=%1

if "%PORT%"=="" (
set MAIN_CLASS=io.shardingjdbc.proxy.Bootstrap
) else (
set MAIN_CLASS=io.shardingjdbc.proxy.Bootstrap %PORT%
)

java -server -Xmx2g -Xms2g -Xmn256m -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 -classpath %CLASS_PATH% %MAIN_CLASS%

pause
