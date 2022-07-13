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

cd `dirname $0`
cd ..
DEPLOY_DIR=`pwd`

LOGS_DIR=${DEPLOY_DIR}/logs
if [ ! -d ${LOGS_DIR} ]; then
    mkdir ${LOGS_DIR}
fi

STDOUT_FILE=${LOGS_DIR}/stdout.log
EXT_LIB=${DEPLOY_DIR}/ext-lib

CLASS_PATH=.:${DEPLOY_DIR}/lib/*:${EXT_LIB}/*

is_openjdk=$(java -version 2>&1 | tail -1 | awk '{print ($1 == "OpenJDK") ? "true" : "false"}')
total_version=`java -version 2>&1 | grep version | sed '1!d' | sed -e 's/"//g' | awk '{print $3}'`
int_version=${total_version%%.*}
if [ $int_version = '1' ] ; then
    int_version=${total_version%.*}
    int_version=${int_version:2}
fi
echo "we find java version: java${int_version}, full_version=${total_version}"

VERSION_OPTS=""
if [ $int_version = '8' ] ; then
    VERSION_OPTS="-XX:+UseConcMarkSweepGC -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70"
elif [ $int_version = '11' ] ; then
    VERSION_OPTS="-XX:+SegmentedCodeCache -XX:+AggressiveHeap"
    if $is_openjdk; then
      VERSION_OPTS="$VERSION_OPTS -XX:+UnlockExperimentalVMOptions -XX:+UseJVMCICompiler"
    fi
elif [ $int_version = '17' ] ; then
    VERSION_OPTS="-XX:+SegmentedCodeCache -XX:+AggressiveHeap"
else
    echo "unadapted java version, please notice..."
fi

JAVA_OPTS=" -Djava.awt.headless=true "

JAVA_MEM_OPTS=" -server -Xmx2g -Xms2g -Xmn1g -Xss1m -XX:AutoBoxCacheMax=4096 -XX:+UseNUMA -XX:+DisableExplicitGC -XX:LargePageSizeInBytes=128m ${VERSION_OPTS} -Dio.netty.leakDetection.level=DISABLED "

MAIN_CLASS=org.apache.shardingsphere.proxy.Bootstrap

unset -v PORT
unset -v ADDRESSES
unset -v CONF_PATH

print_usage() {
    echo "usage:"
    echo "start.sh [port] [config_dir]"
    echo "  port: proxy listen port, default is 3307"
    echo "  config_dir: proxy config directory, default is 'conf'"
    echo ""
    echo "start.sh [-a addresses] [-p port] [-c /path/to/conf]"
    echo "The options are unordered."
    echo "-a  Bind addresses, can be IPv4, IPv6, hostname. In"
    echo "    case more than one address is specified in a"
    echo "    comma-separated list. The default value is '0.0.0.0'."
    echo "-p  Bind port, default is '3307', which could be changed in server.yaml"
    echo "-c  Path to config directory of ShardingSphere-Proxy, default is 'conf'"
    exit 0
}

if [ "$1" == "-h" ] || [ "$1" == "--help" ] ; then
    print_usage
fi

print_version() {
    java ${JAVA_OPTS} ${JAVA_MEM_OPTS} -classpath ${CLASS_PATH} org.apache.shardingsphere.infra.autogen.version.ShardingSphereVersion
    exit 0
}

if [ "$1" == "-v" ] || [ "$1" == "--version" ] ; then
    print_version
fi

if [ $# == 0 ]; then
    CLASS_PATH=${DEPLOY_DIR}/conf:${CLASS_PATH}
fi

if [[ $1 == -a ]] || [[ $1 == -p ]] || [[ $1 == -c ]] ; then
    while getopts ":a:p:c:" opt
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

CLASS_PATH=${CONF_PATH}:${CLASS_PATH}
MAIN_CLASS=${MAIN_CLASS}" "${PORT}" "${CONF_PATH}" "${ADDRESSES}

echo "Starting the $SERVER_NAME ..."
echo "The classpath is ${CLASS_PATH}"
echo "main class ${MAIN_CLASS}"

nohup java ${JAVA_OPTS} ${JAVA_MEM_OPTS} -classpath ${CLASS_PATH} ${MAIN_CLASS} >> ${STDOUT_FILE} 2>&1 &
sleep 1
echo "Please check the STDOUT file: $STDOUT_FILE"
