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

FROM eclipse-temurin:17-jre

ARG MCP_SERVER_NAME=io.github.apache/shardingsphere-mcp
ARG MCP_IMAGE_VERSION=unknown
ARG MCP_SOURCE_URL=https://github.com/apache/shardingsphere
ARG MCP_DESCRIPTION="Standalone MCP runtime for Apache ShardingSphere metadata and SQL access"

LABEL org.opencontainers.image.authors="ShardingSphere dev@shardingsphere.apache.org" \
      org.opencontainers.image.description="${MCP_DESCRIPTION}" \
      org.opencontainers.image.licenses="Apache-2.0" \
      org.opencontainers.image.source="${MCP_SOURCE_URL}" \
      org.opencontainers.image.title="Apache ShardingSphere MCP" \
      org.opencontainers.image.url="${MCP_SOURCE_URL}/tree/master/mcp" \
      org.opencontainers.image.version="${MCP_IMAGE_VERSION}" \
      io.modelcontextprotocol.server.name="${MCP_SERVER_NAME}"

WORKDIR /opt/shardingsphere-mcp

COPY apache-shardingsphere-mcp-*/ /opt/shardingsphere-mcp/

RUN chmod +x /opt/shardingsphere-mcp/bin/start.sh /opt/shardingsphere-mcp/bin/docker-entrypoint.sh \
    && mkdir -p /opt/shardingsphere-mcp/logs

EXPOSE 18088

ENTRYPOINT ["/opt/shardingsphere-mcp/bin/docker-entrypoint.sh"]
