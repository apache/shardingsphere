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

# You can get more configuration items about proxy conf from the following URL:
# https://shardingsphere.apache.org/document/current/en/user-manual/shardingsphere-proxy/configuration/

mode:
  type: Cluster
  repository:
    type: ZooKeeper
    props:
      namespace: demo_yaml
      server-lists: localhost:2181
  overwrite: false

rules:
  - !AUTHORITY
    users:
      - root@:root
      - sharding@:sharding
    provider:
      type: ALL_PRIVILEGES_PERMITTED

props:
  max-connections-size-per-query: 1
  executor-size: 16  # Infinite by default.
  proxy-frontend-flush-threshold: 128  # The default value is 128.
  proxy-hint-enabled: false
  sql-show: false
  check-table-metadata-enabled: false
  sql-simple: false
  check-duplicate-table-enabled: false
