# syntax=docker/dockerfile:1.6
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
ARG MODEL_SHA256

LABEL org.opencontainers.image.title="Apache ShardingSphere MCP LLM E2E Runtime"
LABEL org.opencontainers.image.description="Prepackaged llama.cpp server plus Qwen3 1.7B Q4_K_M GGUF for MCP LLM E2E score evidence"
LABEL org.opencontainers.image.licenses="Apache-2.0"
LABEL org.apache.shardingsphere.mcp.llm.server-runtime="llama.cpp"
LABEL org.apache.shardingsphere.mcp.llm.model-reference="ggml-org/Qwen3-1.7B-GGUF:Q4_K_M"
LABEL org.apache.shardingsphere.mcp.llm.model-revision="daeb8e2d528a760970442092f6bf1e55c3b659eb"
LABEL org.apache.shardingsphere.mcp.llm.model-sha256="${MODEL_SHA256}"

ENV LD_LIBRARY_PATH=/app

ADD --checksum=sha256:${MODEL_SHA256} \
    https://huggingface.co/ggml-org/Qwen3-1.7B-GGUF/resolve/daeb8e2d528a760970442092f6bf1e55c3b659eb/Qwen3-1.7B-Q4_K_M.gguf \
    /models/Qwen3-1.7B-Q4_K_M.gguf

EXPOSE 8080
