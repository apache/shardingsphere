@rem
@rem Licensed to the Apache Software Foundation (ASF) under one or more
@rem contributor license agreements.  See the NOTICE file distributed with
@rem this work for additional information regarding copyright ownership.
@rem The ASF licenses this file to You under the Apache License, Version 2.0
@rem (the "License"); you may not use this file except in compliance with
@rem the License.  You may obtain a copy of the License at
@rem
@rem     http://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@echo off & setlocal enabledelayedexpansion

cd %~dp0

set LOGS_DIR=..\logs

if not exist %LOGS_DIR% (
    mkdir %LOGS_DIR%
)

set STDOUT_FILE=%LOGS_DIR%\stdout.log

set SERVER_NAME=ShardingSphere-Proxy

set CLASS_PATH="..;..\lib\*;..\ext-lib\*"

set PORT=%1

set CONFIG=%2

if "%PORT%"=="-h" (
    goto print_usage
)
if "%PORT%"=="--help" (
    goto print_usage
)

if "%PORT%"=="-v" (
    goto print_version
)

if "%PORT%"=="--version" (
    goto print_version
)

if "%PORT%"=="" (
set MAIN_CLASS=org.apache.shardingsphere.proxy.Bootstrap
set CLASS_PATH=../conf;%CLASS_PATH%
) else ( if "%CONFIG%"=="" (
    set MAIN_CLASS=org.apache.shardingsphere.proxy.Bootstrap %PORT%
    echo The port is configured as %PORT%
    set CLASS_PATH=../conf;%CLASS_PATH%
    ) else (
    set MAIN_CLASS=org.apache.shardingsphere.proxy.Bootstrap %PORT% %CONFIG%
    echo The port is configured as %PORT%
    echo The configuration path is %CONFIG%
    set CLASS_PATH=../%CONFIG%;%CLASS_PATH%
    )
    echo The classpath is %CLASS_PATH%
)

for /f "tokens=3" %%a in ('java -version 2^>^&1 ^| findstr /i "version"') do set total_version=%%a
for /f "tokens=1,2 delims=." %%a in (%total_version%) do (
    if %%a == 1 (
        set int_version=%%b
    ) else (
        set int_version=%%a
    )
)
echo we find java version: java%int_version%, full_version=%total_version:~1,9%
set VERSION_OPTS=
if %int_version% == 8 (
    set VERSION_OPTS=-XX:+UseConcMarkSweepGC -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70
) else if %int_version% == 11 (
    set VERSION_OPTS=-XX:+SegmentedCodeCache -XX:+AggressiveHeap
    @rem TODO Consider using -XX:+UnlockExperimentalVMOptions -XX:+UseJVMCICompiler in OpenJDK 11 for Performance
) else if %int_version% == 17 (
    set VERSION_OPTS=-XX:+SegmentedCodeCache -XX:+AggressiveHeap
) else (
    echo unadapted java version, please notice...
)

echo Starting the %SERVER_NAME% ...

javaw -server -Xmx2g -Xms2g -Xmn1g -Xss1m -XX:AutoBoxCacheMax=4096 -XX:+DisableExplicitGC -XX:LargePageSizeInBytes=128m %VERSION_OPTS% -Dfile.encoding=UTF-8 -Dio.netty.leakDetection.level=DISABLED -classpath %CLASS_PATH% %MAIN_CLASS% >> %STDOUT_FILE% 2>&1

goto exit

:print_usage
 echo "usage: start.bat [port] [config_dir]"
 echo "  port: proxy listen port, default is 3307"
 echo "  config_dir: proxy config directory, default is conf"
 goto exit

:print_version
 java -classpath %CLASS_PATH% org.apache.shardingsphere.infra.version.ShardingSphereVersion
 goto exit

:exit
 pause
