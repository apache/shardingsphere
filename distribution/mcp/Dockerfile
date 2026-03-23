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

LABEL org.opencontainers.image.authors="ShardingSphere dev@shardingsphere.apache.org"

WORKDIR /opt/shardingsphere-mcp

COPY distribution/mcp/target/apache-shardingsphere-mcp-*/ /opt/shardingsphere-mcp/

RUN chmod +x /opt/shardingsphere-mcp/bin/start.sh \
    && mkdir -p /opt/shardingsphere-mcp/logs

EXPOSE 18088

ENTRYPOINT ["/opt/shardingsphere-mcp/bin/start.sh", "/opt/shardingsphere-mcp/conf/mcp.yaml"]
