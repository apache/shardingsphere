@echo off & setlocal enabledelayedexpansion

cd %~dp0

set CLASS_PATH="..;..\conf;..\lib\*"

set PORT=%1

set CONFIG=%2

if "%PORT%"=="" (
set MAIN_CLASS=io.shardingsphere.shardingproxy.Bootstrap
) else ( if "%CONFIG%"=="" (
    set MAIN_CLASS=io.shardingsphere.shardingproxy.Bootstrap %PORT%
    echo The port is configured as %PORT%
    ) else (
    set MAIN_CLASS=io.shardingsphere.shardingproxy.Bootstrap %PORT% %CONFIG%
    echo The port is configured as %PORT%
    echo The configuration file is %CONFIG%
    )
)

java -server -Xmx2g -Xms2g -Xmn1g -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 -Dfile.encoding=UTF-8 -classpath %CLASS_PATH% %MAIN_CLASS%

pause
