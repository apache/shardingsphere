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

ARG BASE_IMAGE
FROM ${BASE_IMAGE}
ARG SERVER_RUNTIME
ARG MODEL_REPOSITORY
ARG MODEL_QUANTIZATION
ARG MODEL_REFERENCE
ARG MODEL_REVISION
ARG MODEL_FILE_NAME
ARG MODEL_SHA256

LABEL org.opencontainers.image.title="Apache ShardingSphere MCP LLM E2E Runtime"
LABEL org.opencontainers.image.description="Prepackaged model server for MCP LLM E2E score evidence"
LABEL org.opencontainers.image.licenses="Apache-2.0"
LABEL org.apache.shardingsphere.mcp.llm.server-runtime="${SERVER_RUNTIME}"
LABEL org.apache.shardingsphere.mcp.llm.model-repository="${MODEL_REPOSITORY}"
LABEL org.apache.shardingsphere.mcp.llm.model-quantization="${MODEL_QUANTIZATION}"
LABEL org.apache.shardingsphere.mcp.llm.model-reference="${MODEL_REFERENCE}"
LABEL org.apache.shardingsphere.mcp.llm.model-revision="${MODEL_REVISION}"
LABEL org.apache.shardingsphere.mcp.llm.model-sha256="${MODEL_SHA256}"

ENV LD_LIBRARY_PATH=/app

RUN set -eux; \
    mkdir -p /models; \
    curl --fail --location --retry 3 --output "/models/${MODEL_FILE_NAME}" \
        "https://huggingface.co/${MODEL_REPOSITORY}/resolve/${MODEL_REVISION}/${MODEL_FILE_NAME}"; \
    printf '%s  %s\n' "${MODEL_SHA256}" "/models/${MODEL_FILE_NAME}" | sha256sum -c -

EXPOSE 8080
