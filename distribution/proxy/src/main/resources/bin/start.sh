#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

SERVER_NAME=ShardingSphere-Proxy

DEPLOY_BIN="$(dirname "${BASH_SOURCE-$0}")"
cd "${DEPLOY_BIN}/../" || exit;
DEPLOY_DIR="$(pwd)"

LOGS_DIR=${DEPLOY_DIR}/logs
if [ ! -d "${LOGS_DIR}" ]; then
    mkdir "${LOGS_DIR}"
fi


STDOUT_FILE=${LOGS_DIR}/stdout.log
EXT_LIB=${DEPLOY_DIR}/ext-lib

CLASS_PATH=".:${DEPLOY_DIR}/lib/*:${EXT_LIB}/*"

if [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    JAVA="$JAVA_HOME/bin/java"
elif type -p java; then
    JAVA="$(which java)"
else
    echo "Error: JAVA_HOME is not set and java could not be found in PATH." 1>&2
    exit 1
fi

is_openjdk=$($JAVA -version 2>&1 | tail -1 | awk '{print ($1 == "OpenJDK") ? "true" : "false"}')
total_version=$($JAVA -version 2>&1 | grep version | sed '1!d' | sed -e 's/"//g' | awk '{print $3}')
int_version=${total_version%%.*}
if [ "$int_version" = '1' ] ; then
    int_version=${total_version%.*}
    int_version=${int_version:2}
fi
echo "we find java version: java${int_version}, full_version=${total_version}, full_path=$JAVA"

case "$OSTYPE" in
*solaris*)
  GREP=/usr/xpg4/bin/grep
  ;;
*)
  GREP=grep
  ;;
esac

VERSION_OPTS=""
if [ "$int_version" = '8' ] ; then
    VERSION_OPTS="-XX:+UseConcMarkSweepGC -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70"
elif [ "$int_version" = '11' ] ; then
    VERSION_OPTS="-XX:+SegmentedCodeCache -XX:+AggressiveHeap"
    if $is_openjdk; then
      VERSION_OPTS="$VERSION_OPTS -XX:+UnlockExperimentalVMOptions -XX:+UseJVMCICompiler"
    fi
elif [ "$int_version" = '17' ] ; then
    VERSION_OPTS="-XX:+SegmentedCodeCache -XX:+AggressiveHeap"
else
    echo "unadapted java version, please notice..."
fi

DEFAULT_CGROUP_MEM_OPTS=""
if [ "$int_version" = '8' ] ; then
	DEFAULT_CGROUP_MEM_OPTS=" -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:InitialRAMPercentage=80.0 -XX:MinRAMPercentage=80.0 -XX:MaxRAMPercentage=80.0 "
else
	DEFAULT_CGROUP_MEM_OPTS=" -XX:InitialRAMPercentage=80.0 -XX:MinRAMPercentage=80.0 -XX:MaxRAMPercentage=80.0 "
fi

CGROUP_MEM_OPTS="${CGROUP_MEM_OPTS:-${DEFAULT_CGROUP_MEM_OPTS}}"

JAVA_OPTS=" -Djava.awt.headless=true "
if [ -n "${JVM_OPTS}" ]; then
    JAVA_OPTS="${JAVA_OPTS} ${JVM_OPTS}"
fi
DEFAULT_JAVA_MEM_COMMON_OPTS=" -Xmx2g -Xms2g -Xmn1g "
if [ -n "${IS_DOCKER}" ]; then
	JAVA_MEM_COMMON_OPTS="${CGROUP_MEM_OPTS}"
else
	JAVA_MEM_COMMON_OPTS="${JAVA_MEM_COMMON_OPTS:-${DEFAULT_JAVA_MEM_COMMON_OPTS}}"
fi

JAVA_MEM_OPTS=" -server ${JAVA_MEM_COMMON_OPTS} -Xss1m -XX:AutoBoxCacheMax=4096 -XX:+UseNUMA -XX:+DisableExplicitGC -XX:LargePageSizeInBytes=128m ${VERSION_OPTS} -Dio.netty.leakDetection.level=DISABLED "




MAIN_CLASS=org.apache.shardingsphere.proxy.Bootstrap

unset -v PORT
unset -v ADDRESSES
unset -v CONF_PATH
unset -v SOCKET_FILE

print_usage() {
    echo "usage:"
    echo "start.sh [port] [config_dir]"
    echo "  port: proxy listen port, default is 3307"
    echo "  config_dir: proxy config directory, default is 'conf'"
    echo ""
    echo "start.sh [-a addresses] [-p port] [-c /path/to/conf] [-g] [-s /path/to/socket]"
    echo "The options are unordered."
    echo "-a  Bind addresses, can be IPv4, IPv6, hostname. In"
    echo "    case more than one address is specified in a"
    echo "    comma-separated list. The default value is '0.0.0.0'."
    echo "-p  Bind port, default is '3307', which could be changed in global.yaml"
    echo "-c  Path to config directory of ShardingSphere-Proxy, default is 'conf'"
    echo "-g  Enable agent if shardingsphere-agent deployed in 'agent' directory"
    echo "-s  The socket file to use for connection."
    exit 0
}

if [ "$1" == "-h" ] || [ "$1" == "--help" ] ; then
    print_usage
fi

print_version() {
    $JAVA ${JAVA_OPTS} ${JAVA_MEM_OPTS} -classpath ${CLASS_PATH} org.apache.shardingsphere.infra.version.ShardingSphereVersion
    exit 0
}

if [ "$1" == "-v" ] || [ "$1" == "--version" ] ; then
    print_version
fi

AGENT_FILE=${DEPLOY_DIR}/agent/shardingsphere-agent.jar
function set_agent_name() {
    if [ -d "${DEPLOY_DIR}/agent" ]; then
        AGENT_NAME=$(ls "${DEPLOY_DIR}/agent/shardingsphere-agent"*)
        if [ -n "${AGENT_NAME}" ]; then
          AGENT_FILE=${AGENT_NAME}
        fi
    fi
}

function set_agent_parameter() {
    AGENT_PARAM="";
    if [ -f "$AGENT_FILE" ]; then
      AGENT_PARAM=" -javaagent:${AGENT_FILE} "
    fi
}

PARAMETER_INDEX=0
PARAMETERS=( $* )
for arg in $*
do
  if [ "$arg" == "-g" ] || [ "$arg" == "--agent" ] ; then
    set_agent_name
    set_agent_parameter
    unset PARAMETERS[PARAMETER_INDEX]
    set -- "${PARAMETERS[@]}"
    break
  fi
  let PARAMETER_INDEX+=1
done

if [ $# == 0 ]; then
    CLASS_PATH=${DEPLOY_DIR}/conf:${CLASS_PATH}
fi

if [[ $1 == -a ]] || [[ $1 == -p ]] || [[ $1 == -c ]] || [[ $1 == -s ]]; then
    while getopts ":a:p:c:s:" opt
    do
        case $opt in
        a)
          echo "The address is $OPTARG"
          ADDRESSES=$OPTARG;;
        p)
          echo "The port is $OPTARG"
          PORT=$OPTARG;;
        c)
          echo "The configuration path is $OPTARG"
          CONF_PATH=$OPTARG;;
        s)
          echo "The socket file is $OPTARG"
          SOCKET_FILE=$OPTARG;;
        ?)
          print_usage;;
        esac
    done

elif [ $# == 1 ]; then
    PORT=$1
    echo "The port is $1"

elif [ $# == 2 ]; then
    PORT=$1
    CONF_PATH=$2
    echo "The port is $1"
    echo "The configuration path is $2"
fi

if [ -z "$CONF_PATH" ]; then
    CONF_PATH=${DEPLOY_DIR}/conf
fi

if [ -z "$PORT" ]; then
    PORT=-1
fi

if [ -z "$ADDRESSES" ]; then
    ADDRESSES="0.0.0.0"
fi

if [ "$SOCKET_FILE" ]; then
    ADDRESSES="${ADDRESSES},${SOCKET_FILE}"
fi

CLASS_PATH=${CONF_PATH}:${CLASS_PATH}
MAIN_CLASS="${MAIN_CLASS} ${PORT} ${CONF_PATH} ${ADDRESSES}"

echo "The classpath is ${CLASS_PATH}"
echo "main class ${MAIN_CLASS}"

if [ -n "${IS_DOCKER}" ]; then
  exec $JAVA ${JAVA_OPTS} ${JAVA_MEM_OPTS} -classpath ${CLASS_PATH} ${AGENT_PARAM} ${MAIN_CLASS}
  exit 0
fi

echo -e "Starting the $SERVER_NAME ...\c"

nohup $JAVA ${JAVA_OPTS} ${JAVA_MEM_OPTS} -classpath ${CLASS_PATH} ${AGENT_PARAM} ${MAIN_CLASS} >> ${STDOUT_FILE} 2>&1 &
if [ $? -eq 0 ]; then
  case "$OSTYPE" in
  *solaris*)
    pid=$(/bin/echo "${!}\\c")
    ;;
  *)
    pid=$(/bin/echo -n $!)
    ;;
  esac
  if [ $? -eq 0 ]; then
      sleep 1;
      if ps -p "${pid}" > /dev/null 2>&1; then
        echo " PID: $pid"
        echo "Please check the STDOUT file: $STDOUT_FILE"
        exit 0
      fi
  else
    echo " FAILED TO GET PID"
  fi
else
  echo " SERVER DID NOT START"
fi
echo "Please check the STDOUT file: $STDOUT_FILE"
exit 1
