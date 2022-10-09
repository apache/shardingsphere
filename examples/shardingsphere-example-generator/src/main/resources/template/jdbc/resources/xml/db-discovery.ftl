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
    
    <database-discovery:rule id="dbDiscoveryRule">
        <database-discovery:data-source-rule id="db" data-source-names="ds_0,ds_1,ds_2" discovery-heartbeat-name="mgr-heartbeat" discovery-type-name="mgr" />
        <database-discovery:discovery-heartbeat id="mgr-heartbeat">
            <props>
                <prop key="keep-alive-cron">0/5 * * * * ?</prop>
            </props>
        </database-discovery:discovery-heartbeat>
    </database-discovery:rule>
    
    <database-discovery:discovery-type id="mgr" type="MySQL.MGR">
        <props>
            <prop key="keep-alive-cron">0/5 * * * * ?</prop>
            <prop key="group-name">aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa</prop>
        </props>
    </database-discovery:discovery-type>
