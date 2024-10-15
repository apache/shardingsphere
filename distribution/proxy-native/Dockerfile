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
FROM container-registry.oracle.com/os/oraclelinux:9-slim
LABEL org.opencontainers.image.authors="ShardingSphere dev@shardingsphere.apache.org"
ENV LOCAL_PATH=/opt/shardingsphere-proxy-native-bin
ARG OUTPUT_DIRECTORY_NAME
COPY target/${OUTPUT_DIRECTORY_NAME} ${LOCAL_PATH}
ENTRYPOINT ["${LOCAL_PATH}/proxy-native", "3307", "${LOCAL_PATH}/conf", "0.0.0.0", "false"]
