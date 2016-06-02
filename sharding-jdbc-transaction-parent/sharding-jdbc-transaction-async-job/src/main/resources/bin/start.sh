#!/bin/bash
cd `dirname $0`
cd ..
DEPLOY_DIR=`pwd`
SERVER_NAME=sharding-jdbc-transaction-async-job

#LOG LOCATION
LOGS_DIR=$DEPLOY_DIR/logs
if [ ! -d $LOGS_DIR ]; then
    mkdir $LOGS_DIR
fi
PID=`ps -ef | grep java | grep "$DEPLOY_DIR" |awk '{print $2}'`
if [ -n "$PID" ]; then
    echo "ERROR: The $SERVER_NAME already started!"
    echo "PID: $PID"
    exit 1
fi

CONF_DIR=$DEPLOY_DIR/conf/*
LIB_DIR=$DEPLOY_DIR/lib/*
CONTAINER_MAIN=com.dangdang.ddframe.rdb.transaction.soft.bed.BestEffortsDeliveryJobMain
nohup java -classpath $CONF_DIR:$LIB_DIR:. $CONTAINER_MAIN >/dev/null 2>&1 &

CONSOLE_TXT="Started the $SERVER_NAME"
echo $CONSOLE_TXT
