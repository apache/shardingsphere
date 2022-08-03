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
    
    private DatabaseDiscoveryRuleConfiguration createDatabaseDiscoveryRuleConfiguration() {
        return new DatabaseDiscoveryRuleConfiguration(createDataSources(), createDiscoveryHeartbeats(), createDiscoveryTypes());
    }
    
    private Collection<DatabaseDiscoveryDataSourceRuleConfiguration> createDataSources() {
        DatabaseDiscoveryDataSourceRuleConfiguration result = new DatabaseDiscoveryDataSourceRuleConfiguration("rule", Arrays.asList("ds_0", "ds_1", "ds_2"), "mgr-heartbeat", "mgr");
        return Collections.singleton(result);
    }
    
    private Map<String, DatabaseDiscoveryHeartBeatConfiguration> createDiscoveryHeartbeats() {
        Map<String, DatabaseDiscoveryHeartBeatConfiguration> result = new LinkedHashMap<>();
        Properties props = new Properties();
        props.put("keep-alive-cron", "0/5 * * * * ?");
        result.put("mgr-heartbeat", new DatabaseDiscoveryHeartBeatConfiguration(props));
        return result;
    }
    
    private Map<String, AlgorithmConfiguration> createDiscoveryTypes() {
        Map<String, AlgorithmConfiguration> result = new LinkedHashMap<>();
        Properties props = new Properties();
        props.put("keep-alive-cron", "0/5 * * * * ?");
        props.put("group-name", "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        result.put("mgr", new AlgorithmConfiguration("MySQL.MGR", props));
        return result;
    }
