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

EXT_LIB=${DEPLOY_DIR}/ext-lib

CLASS_PATH=.:${DEPLOY_DIR}/lib/*:${EXT_LIB}/*

JAVA_OPTS=" -Djava.awt.headless=true -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=3308"

JAVA_MEM_OPTS=" -server -Xmx2g -Xms2g -Xmn1g -Xss1m -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 "

MAIN_CLASS=org.apache.shardingsphere.proxy.Bootstrap

unset -v PORT
unset -v ADDRESSES
unset -v CONF_PATH
unset -v SOCKET_FILE

print_usage() {
    echo "usage: start.sh [port] [config_dir]"
    echo "  port: proxy listen port, default is 3307"
    echo "  config_dir: proxy config directory, default is conf"
    exit 0
}

if [ "$1" == "-h" ] || [ "$1" == "--help" ] ; then
    print_usage
fi

echo "Starting the $SERVER_NAME ..."

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
fi

if [ $# == 2 ]; then
    PORT=$1
    CONF_PATH=$2
    echo "The port is $1"
    echo "The configuration path is $DEPLOY_DIR/$2"
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

exec java ${JAVA_OPTS} ${JAVA_MEM_OPTS} -classpath ${CLASS_PATH} ${MAIN_CLASS}
