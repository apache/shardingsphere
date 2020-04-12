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

SERVER_NAME=ShardingSphere-UI

cd `dirname $0`
cd ..
DEPLOY_DIR=`pwd`

LOGS_DIR=${DEPLOY_DIR}/logs
if [ ! -d ${LOGS_DIR} ]; then
    mkdir ${LOGS_DIR}
fi

STDOUT_FILE=${LOGS_DIR}/stdout.log

PIDS=`ps -ef | grep java | grep "$DEPLOY_DIR" | grep -v grep | awk '{print $2}'`
if [ -n "$PIDS" ]; then
    echo "ERROR: The $SERVER_NAME already started!"
    echo "PID: $PIDS"
    exit 1
fi

CLASS_PATH=.:${DEPLOY_DIR}/conf:${DEPLOY_DIR}/lib/*
JAVA_OPTS=" -server -Xmx1g -Xms1g -Xmn512m -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 "

MAIN_CLASS=org.apache.shardingsphere.ui.Bootstrap
echo "Starting the $SERVER_NAME ..."

nohup java ${JAVA_OPTS} -classpath ${CLASS_PATH} ${MAIN_CLASS} > ${STDOUT_FILE} 2>&1 &
sleep 1
echo "Please check the STDOUT file: $STDOUT_FILE"
