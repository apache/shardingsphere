#!/bin/sh

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

set -eu

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
APP_HOME=$(cd "${SCRIPT_DIR}/.." && pwd)
CONF_FILE=${1:-"${APP_HOME}/conf/mcp.yaml"}
LIB_DIR="${APP_HOME}/lib"
PLUGINS_DIR="${APP_HOME}/plugins"
DATA_DIR="${APP_HOME}/data"
LOG_DIR="${APP_HOME}/logs"

if [ ! -f "${CONF_FILE}" ]; then
  echo "Error: MCP configuration file '${CONF_FILE}' does not exist." 1>&2
  exit 1
fi

if [ ! -d "${LIB_DIR}" ]; then
  echo "Error: MCP runtime libraries are missing under '${LIB_DIR}'." 1>&2
  exit 1
fi

mkdir -p "${DATA_DIR}" "${PLUGINS_DIR}" "${LOG_DIR}"

if [ -n "${JAVA_HOME:-}" ] && [ -x "${JAVA_HOME}/bin/java" ]; then
  JAVA="${JAVA_HOME}/bin/java"
elif command -v java >/dev/null 2>&1; then
  JAVA=$(command -v java)
else
  echo "Error: JAVA_HOME is not set and java could not be found in PATH." 1>&2
  exit 1
fi

CLASSPATH="${APP_HOME}/conf:${LIB_DIR}/*"
if [ -d "${PLUGINS_DIR}" ]; then
  CLASSPATH="${CLASSPATH}:${PLUGINS_DIR}/*"
fi

cd "${APP_HOME}"

exec "${JAVA}" ${JAVA_OPTS:-} \
  -DAPP_HOME="${APP_HOME}" \
  -Dlogback.configurationFile="${APP_HOME}/conf/logback.xml" \
  -cp "${CLASSPATH}" \
  org.apache.shardingsphere.mcp.bootstrap.MCPBootstrap "${CONF_FILE}"
