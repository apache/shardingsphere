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

set SERVER_NAME=ShardingSphere-UI

set CLASS_PATH=".;..\conf;..\lib\*"

set JAVA_OPTS=-server -Xmx1g -Xms1g -Xmn512m -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70

set MAIN_CLASS=org.apache.shardingsphere.ui.Bootstrap

echo Starting the %SERVER_NAME% ...

java %JAVA_OPTS% -Dfile.encoding=UTF-8 -classpath %CLASS_PATH% %MAIN_CLASS%

pause
