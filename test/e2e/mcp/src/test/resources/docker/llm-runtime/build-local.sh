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

SCRIPT_DIR="$(CDPATH= cd "$(dirname "$0")" && pwd -P)"
DOCKERFILE_PATH="${SCRIPT_DIR}/Dockerfile"
ENV_FILE="${SCRIPT_DIR}/../../env/e2e-env.properties"

if [ ! -f "${ENV_FILE}" ]; then
  echo "MCP E2E environment properties file is required: ${ENV_FILE}" >&2
  exit 1
fi

read_property() {
  awk -F= -v key="$1" '$1 == key {sub(/^[^=]*=/, ""); print; found = 1; exit} END {if (!found) exit 1}' "${ENV_FILE}"
}

read_required_property() {
  PROPERTY_VALUE="$(read_property "$1" || true)"
  if [ -z "${PROPERTY_VALUE}" ]; then
    echo "MCP E2E property is required: $1" >&2
    exit 1
  fi
  echo "${PROPERTY_VALUE}"
}

read_optional_property() {
  read_property "$1" || true
}

IMAGE_TAG="${MCP_LLM_SERVER_IMAGE:-$(read_required_property "mcp.llm.server-image")}"
BASE_IMAGE="${MCP_LLM_BASE_SERVER_IMAGE:-$(read_required_property "mcp.llm.base-server-image")}"
BASE_DIGEST="${MCP_LLM_BASE_SERVER_IMAGE_DIGEST:-$(read_optional_property "mcp.llm.base-server-image-digest")}"
MODEL_SHA256="${MCP_LLM_MODEL_SHA256:-$(read_required_property "mcp.llm.model-sha256")}"

case "${BASE_IMAGE}" in
  *@sha256:*)
    ;;
  *)
    if [ -n "${BASE_DIGEST}" ]; then
      BASE_IMAGE="${BASE_IMAGE}@${BASE_DIGEST}"
    fi
    ;;
esac

if [ "--dry-run" = "${MODE}" ] || [ "--print" = "${MODE}" ]; then
  echo "base_image=${BASE_IMAGE}"
  echo "model_sha256=${MODEL_SHA256}"
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
  --build-arg "MODEL_SHA256=${MODEL_SHA256}" \
  -t "${IMAGE_TAG}" \
  -f "${DOCKERFILE_PATH}" \
  "${SCRIPT_DIR}"
