#!/bin/bash

cd `dirname $0`
cd ..
DEPLOY_DIR=`pwd`

CLASS_PATH=.:${DEPLOY_DIR}/conf:${DEPLOY_DIR}/lib/*

MAIN_CLASS=info.avalon566.shardingscaling.Engine

java -classpath ${CLASS_PATH} ${MAIN_CLASS} $*