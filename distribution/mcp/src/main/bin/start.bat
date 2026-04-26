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

@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
for %%i in ("%SCRIPT_DIR%..") do set "APP_HOME=%%~fi"

if "%~1"=="" (
    set "CONF_FILE=%APP_HOME%\conf\mcp.yaml"
) else (
    set "CONF_FILE=%~1"
)
set "LIB_DIR=%APP_HOME%\lib"
set "PLUGINS_DIR=%APP_HOME%\plugins"
set "DATA_DIR=%APP_HOME%\data"
set "LOG_DIR=%APP_HOME%\logs"

if not exist "%CONF_FILE%" (
    >&2 echo Error: MCP configuration file '%CONF_FILE%' does not exist.
    exit /b 1
)

if not exist "%LIB_DIR%" (
    >&2 echo Error: MCP runtime libraries are missing under '%LIB_DIR%'.
    exit /b 1
)

if not exist "%DATA_DIR%" (
    mkdir "%DATA_DIR%"
)
if not exist "%PLUGINS_DIR%" (
    mkdir "%PLUGINS_DIR%"
)
if not exist "%LOG_DIR%" (
    mkdir "%LOG_DIR%"
)

if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA=%JAVA_HOME%\bin\java.exe"
    )
)
if not defined JAVA (
    for %%i in (java.exe) do set "JAVA=%%~$PATH:i"
)
if not defined JAVA (
    >&2 echo Error: JAVA_HOME is not set and java could not be found in PATH.
    exit /b 1
)

set "CLASSPATH=%APP_HOME%\conf;%LIB_DIR%\*"
if exist "%PLUGINS_DIR%" (
    set "CLASSPATH=%CLASSPATH%;%PLUGINS_DIR%\*"
)

cd /d "%APP_HOME%"

"%JAVA%" %JAVA_OPTS% ^
  -DAPP_HOME="%APP_HOME%" ^
  -Dlogback.configurationFile="%APP_HOME%\conf\logback.xml" ^
  -cp "%CLASSPATH%" ^
  org.apache.shardingsphere.mcp.bootstrap.MCPBootstrap "%CONF_FILE%"

exit /b %ERRORLEVEL%
