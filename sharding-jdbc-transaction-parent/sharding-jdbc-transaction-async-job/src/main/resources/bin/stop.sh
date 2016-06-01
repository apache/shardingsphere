#!/bin/bash
cd `dirname $0`
BIN_DIR=`pwd`
cd ..
DEPLOY_DIR=`pwd`

SERVER_NAME=sharding-jdbc-transaction-async-job

PID=`ps -ef | grep java | grep "$DEPLOY_DIR" |awk '{print $2}'`
if [ -z "$PID" ]; then
    echo "ERROR: The $SERVER_NAME does not started!"
    exit 1
fi

echo -e "Stopping the $SERVER_NAME ...\c"
kill $PID > /dev/null 2>&1

COUNT=0
while [ $COUNT -lt 1 ]; do
    echo -e ".\c"
    sleep 1
    COUNT=1
    PID_EXIST=`ps -f -p $PID | grep java`
    if [ -n "$PID_EXIST" ]; then
        COUNT=0
        break
    fi
done

echo "OK!"
echo "PID: $PID"
