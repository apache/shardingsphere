<#--
  ~ Licensed to the Apache Software Foundation (ASF) under one or more
  ~ contributor license agreements.  See the NOTICE file distributed with
  ~ this work for additional information regarding copyright ownership.
  ~ The ASF licenses this file to You under the Apache License, Version 2.0
  ~ (the "License"); you may not use this file except in compliance with
  ~ the License.  You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->
  - !DB_DISCOVERY
    dataSources:
      ds_0:
        dataSourceNames:
          - primary_ds_0_replica_0
          - primary_ds_0_replica_1
        discoveryHeartbeatName: mgr-heartbeat
        discoveryTypeName: mgr
      ds_1:
        dataSourceNames:
          - primary_ds_1_replica_0
          - primary_ds_1_replica_1
        discoveryHeartbeatName: mgr-heartbeat
        discoveryTypeName: mgr
    discoveryHeartbeats:
      mgr-heartbeat:
        props:
          keep-alive-cron: '0/5 * * * * ?'
    discoveryTypes:
      mgr:
        type: MGR
        props:
          group-name: 92504d5b-6dec-11e8-91ea-246e9612aaf1
