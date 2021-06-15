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

if "%PORT%"=="" (
set MAIN_CLASS=org.apache.shardingsphere.proxy.Bootstrap
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

echo Starting the %SERVER_NAME% ...

java -server -Xmx2g -Xms2g -Xmn1g -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 -Dfile.encoding=UTF-8 -classpath %CLASS_PATH% %MAIN_CLASS%

goto exit

:print_usage
 echo "usage: start.bat [port] [config_dir]"
 echo "  port: proxy listen port, default is 3307"
 echo "  config_dir: proxy config directory, default is conf"
 pause

:exit
 pause
