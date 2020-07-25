/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.example.orchestration.raw.jdbc.config;

import org.apache.shardingsphere.example.type.ShardingType;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationRepositoryConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class OrchestrationRepositoryConfigurationUtil {
    
    private static final String ZOOKEEPER_CONNECTION_STRING = "localhost:2181";
    
    private static final String NAMESPACE = "orchestration-java-demo";
    
    private static final String NACOS_CONNECTION_STRING = "localhost:8848";

    private static final String NACOS_NAMESPACE = "";
    
    public static Map<String, OrchestrationRepositoryConfiguration> getZooKeeperConfiguration(final String overwrite, final ShardingType shardingType) {
        Map<String, OrchestrationRepositoryConfiguration> result = new HashMap<>();
        Properties props = new Properties();
        props.setProperty("overwrite", overwrite);
        OrchestrationRepositoryConfiguration orchestrationRepositoryConfiguration = new OrchestrationRepositoryConfiguration("zookeeper", props);
        orchestrationRepositoryConfiguration.setServerLists(ZOOKEEPER_CONNECTION_STRING);
        orchestrationRepositoryConfiguration.setNamespace(NAMESPACE);
        orchestrationRepositoryConfiguration.setOrchestrationType("registry_center,config_center,metadata_center");
        switch (shardingType) {
            case SHARDING_DATABASES_AND_TABLES:
                result.put("orchestration-sharding-data-source", orchestrationRepositoryConfiguration);
                break;
            case MASTER_SLAVE:
                result.put("orchestration-ms-data-source", orchestrationRepositoryConfiguration);
                break;
            case ENCRYPT:
                result.put("orchestration-encrypt-data-source", orchestrationRepositoryConfiguration);
                break;
            case SHADOW:
                result.put("orchestration-shadow-data-source", orchestrationRepositoryConfiguration);
                break;
        }
        return result;
    }
    
    public static Map<String, OrchestrationRepositoryConfiguration> getNacosConfiguration(final String overwrite, final ShardingType shardingType) {
        Map<String, OrchestrationRepositoryConfiguration> result = new HashMap<>();
        Properties nacosProperties = new Properties();
        nacosProperties.setProperty("group", "SHARDING_SPHERE_DEFAULT_GROUP");
        nacosProperties.setProperty("timeout", "3000");
        nacosProperties.setProperty("overwrite", overwrite);
        OrchestrationRepositoryConfiguration nacosResult = new OrchestrationRepositoryConfiguration("nacos", nacosProperties);
        nacosResult.setServerLists(NACOS_CONNECTION_STRING);
        nacosResult.setNamespace(NACOS_NAMESPACE);
        nacosResult.setOrchestrationType("config_center");
        Properties zookeeperProperties = new Properties();
        zookeeperProperties.setProperty("overwrite", overwrite);
        OrchestrationRepositoryConfiguration zookeeperResult = new OrchestrationRepositoryConfiguration("zookeeper", zookeeperProperties);
        zookeeperResult.setServerLists(ZOOKEEPER_CONNECTION_STRING);
        zookeeperResult.setNamespace(NAMESPACE);
        zookeeperResult.setOrchestrationType("registry_center,metadata_center");
        switch (shardingType) {
            case SHARDING_DATABASES_AND_TABLES:
                result.put("orchestration-sharding-data-source", nacosResult);
                result.put("orchestration-zookeeper-sharding-data-source", zookeeperResult);
                break;
            case MASTER_SLAVE:
                result.put("orchestration-ms-data-source", nacosResult);
                result.put("orchestration-zookeeper-ms-data-source", zookeeperResult);
                break;
            case ENCRYPT:
                result.put("orchestration-encrypt-data-source", nacosResult);
                result.put("orchestration-zookeeper-encrypt-data-source", zookeeperResult);
                break;
            case SHADOW:
                result.put("orchestration-shadow-data-source", nacosResult);
                result.put("orchestration-zookeeper-shadow-data-source", zookeeperResult);
                break;
        }
        return result;
    }
}
