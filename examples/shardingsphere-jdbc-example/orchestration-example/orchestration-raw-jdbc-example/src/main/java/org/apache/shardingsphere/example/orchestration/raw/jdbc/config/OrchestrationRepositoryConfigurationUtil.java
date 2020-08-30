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
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationCenterConfiguration;

import java.util.Properties;

public final class OrchestrationRepositoryConfigurationUtil {
    
    private static final String ZOOKEEPER_CONNECTION_STRING = "localhost:2181";
    
    private static final String NACOS_CONNECTION_STRING = "localhost:8848";
    
    public static OrchestrationConfiguration getZooKeeperConfiguration(final boolean overwrite, final ShardingType shardingType) {
        OrchestrationCenterConfiguration orchestrationCenterConfig = new OrchestrationCenterConfiguration("ZooKeeper", ZOOKEEPER_CONNECTION_STRING, new Properties());
        switch (shardingType) {
            case SHARDING_DATABASES_AND_TABLES:
                return new OrchestrationConfiguration("orchestration-sharding-data-source", orchestrationCenterConfig, overwrite);
            case MASTER_SLAVE:
                return new OrchestrationConfiguration("orchestration-ms-data-source", orchestrationCenterConfig, overwrite);
            case ENCRYPT:
                return new OrchestrationConfiguration("orchestration-encrypt-data-source", orchestrationCenterConfig, overwrite);
            case SHADOW:
                return new OrchestrationConfiguration("orchestration-shadow-data-source", orchestrationCenterConfig, overwrite);
            default:
                throw new UnsupportedOperationException(shardingType.toString());
        }
    }
    
    public static OrchestrationConfiguration getNacosConfiguration(final boolean overwrite, final ShardingType shardingType) {
        Properties nacosProperties = new Properties();
        nacosProperties.setProperty("group", "SHARDING_SPHERE_DEFAULT_GROUP");
        nacosProperties.setProperty("timeout", "3000");
        OrchestrationCenterConfiguration nacosConfig = new OrchestrationCenterConfiguration("Nacos", NACOS_CONNECTION_STRING, nacosProperties);
        Properties zookeeperProperties = new Properties();
        OrchestrationCenterConfiguration zookeeperConfig = new OrchestrationCenterConfiguration("ZooKeeper", ZOOKEEPER_CONNECTION_STRING, zookeeperProperties);
        switch (shardingType) {
            case SHARDING_DATABASES_AND_TABLES:
                return new OrchestrationConfiguration("orchestration-zookeeper-sharding-data-source", zookeeperConfig, nacosConfig, overwrite);
            case MASTER_SLAVE:
                return new OrchestrationConfiguration("orchestration-zookeeper-ms-data-source", zookeeperConfig, nacosConfig, overwrite);
            case ENCRYPT:
                return new OrchestrationConfiguration("orchestration-zookeeper-encrypt-data-source", zookeeperConfig, nacosConfig, overwrite);
            case SHADOW:
                return new OrchestrationConfiguration("orchestration-zookeeper-shadow-data-source", zookeeperConfig, nacosConfig, overwrite);
            default:
                throw new UnsupportedOperationException(shardingType.toString());
        }
    }
}
