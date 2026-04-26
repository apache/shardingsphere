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
START_SCRIPT="${APP_HOME}/bin/start.sh"

if [ "$#" -gt 0 ]; then
  exec "${START_SCRIPT}" "$@"
fi

if [ -n "${SHARDINGSPHERE_MCP_CONFIG:-}" ]; then
  exec "${START_SCRIPT}" "${SHARDINGSPHERE_MCP_CONFIG}"
fi

case "${SHARDINGSPHERE_MCP_TRANSPORT:-http}" in
  http)
    exec "${START_SCRIPT}" "${APP_HOME}/conf/mcp.yaml"
    ;;
  stdio)
    exec "${START_SCRIPT}" "${APP_HOME}/conf/mcp-stdio.yaml"
    ;;
  *)
    echo "Error: SHARDINGSPHERE_MCP_TRANSPORT must be either 'http' or 'stdio'." 1>&2
    exit 1
    ;;
esac
