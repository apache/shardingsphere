#!/bin/sh
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

set -eu

print_usage() {
  echo "Usage: sh $0 [--dry-run|--print]"
}

if [ "$#" -gt 1 ]; then
  echo "Only one argument is supported." >&2
  print_usage >&2
  exit 1
fi

MODE="${1:-build}"

case "${MODE}" in
  build | --dry-run | --print)
    ;;
  --help)
    print_usage
    exit 0
    ;;
  *)
    echo "Unsupported argument: $1" >&2
    print_usage >&2
    exit 1
    ;;
esac

IMAGE_TAG="${MCP_LLM_SERVER_IMAGE:-apache/shardingsphere-mcp-llm-runtime:local}"
ARCHITECTURE="${MCP_LLM_ARCHITECTURE:-$(uname -m)}"
SCRIPT_DIR="$(CDPATH= cd "$(dirname "$0")" && pwd -P)"
DOCKERFILE_PATH="${SCRIPT_DIR}/Dockerfile"

case "${ARCHITECTURE}" in
  amd64|x86_64)
    BASE_DIGEST="sha256:988d2695631987e28a29d98970aaf0e979e23b843a26824abb790ac4245d1d57"
    ;;
  arm64|aarch64)
    BASE_DIGEST="sha256:a478a81b2606aa5bb4c5864c01894fe1d8851adad8b6710f14b9519944d013ca"
    ;;
  *)
    echo "Unsupported local architecture for MCP LLM Docker score mode: ${ARCHITECTURE}" >&2
    exit 1
    ;;
esac

BASE_IMAGE="ghcr.io/ggml-org/llama.cpp@${BASE_DIGEST}"

if [ "--dry-run" = "${MODE}" ] || [ "--print" = "${MODE}" ]; then
  echo "architecture=${ARCHITECTURE}"
  echo "base_image=${BASE_IMAGE}"
  echo "image_tag=${IMAGE_TAG}"
  echo "dockerfile=${DOCKERFILE_PATH}"
  echo "context=${SCRIPT_DIR}"
  exit 0
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker is required to build the MCP LLM runtime image." >&2
  exit 1
fi

docker build \
  --build-arg "BASE_IMAGE=${BASE_IMAGE}" \
  -t "${IMAGE_TAG}" \
  -f "${DOCKERFILE_PATH}" \
  "${SCRIPT_DIR}"
