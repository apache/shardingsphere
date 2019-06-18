#!/bin/bash
SERVER_NAME=Sharding-UI

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
JAVA_OPTS="${JAVA_OPTS} -jar ${DEPLOY_DIR}/lib/sharding-ui.jar"
JAVA_OPTS="${JAVA_OPTS} --spring.config.location=${DEPLOY_DIR}/conf/application.properties"
JAVA_OPTS="${JAVA_OPTS} --logging.config=${DEPLOY_DIR}/conf/logback.xml"

echo "Starting the $SERVER_NAME ..."

nohup java ${JAVA_OPTS} > ${STDOUT_FILE} 2>&1 &
sleep 1
echo "Please check the STDOUT file: $STDOUT_FILE"
