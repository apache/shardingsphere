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

DEPLOY_DIR="$(pwd)"
LOGS_DIR=${DEPLOY_DIR}/logs
if [ ! -d "${LOGS_DIR}" ]; then
    mkdir "${LOGS_DIR}"
fi
STDOUT_FILE=${LOGS_DIR}/stdout.log

CLASS_PATH=".:${DEPLOY_DIR}/lib/*:${DEPLOY_DIR}/conf"
MAIN_CLASS=org.apache.shardingsphere.test.e2e.agent.jdbc.project.JdbcProjectApplication

AGENT_FILE=${DEPLOY_DIR}/agent/shardingsphere-agent.jar
function set_agent_name() {
    if [ -d "${DEPLOY_DIR}/agent" ]; then
        AGENT_NAME=$(ls "${DEPLOY_DIR}/agent/shardingsphere-agent"*)
        if [ -n "${AGENT_NAME}" ]; then
          AGENT_FILE=${AGENT_NAME}
        fi
    fi
}

AGENT_PARAM="";
function set_agent_parameter() {
    if [ -f "$AGENT_FILE" ]; then
      AGENT_PARAM=" -javaagent:${AGENT_FILE} "
    fi
}

for arg in $*
do
  if [ "$arg" == "--agent" ] ; then
    set_agent_name
    set_agent_parameter
    break
  fi
  let PARAMETER_INDEX+=1
done

nohup java -classpath ${CLASS_PATH} ${AGENT_PARAM} ${MAIN_CLASS} >> ${STDOUT_FILE} 2>&1 &
