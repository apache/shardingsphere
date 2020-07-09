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

JAVA_OPTS=" -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true "

JAVA_MEM_OPTS=" -server -Xmx2g -Xms2g -Xmn1g -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 "

MAIN_CLASS=org.apache.shardingsphere.proxy.Bootstrap

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

if [ $# == 1 ]; then
    MAIN_CLASS=${MAIN_CLASS}" "$1
    echo "The port is $1"
    set CLASS_PATH=../conf;%CLASS_PATH%
fi

if [ $# == 2 ]; then
    MAIN_CLASS=${MAIN_CLASS}" "$1" "$2
    echo "The port is $1"
    echo "The configuration path is $DEPLOY_DIR/$2"
    CLASS_PATH=${DEPLOY_DIR}/$2:${CLASS_PATH}
fi

echo "The classpath is ${CLASS_PATH}"

nohup java ${JAVA_OPTS} ${JAVA_MEM_OPTS} -classpath ${CLASS_PATH} ${MAIN_CLASS} >> ${STDOUT_FILE} 2>&1 &
sleep 1
echo "Please check the STDOUT file: $STDOUT_FILE"
