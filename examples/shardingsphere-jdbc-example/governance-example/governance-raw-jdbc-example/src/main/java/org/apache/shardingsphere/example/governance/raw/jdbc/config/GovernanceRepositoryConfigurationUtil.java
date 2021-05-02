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

package org.apache.shardingsphere.example.governance.raw.jdbc.config;

import org.apache.shardingsphere.example.type.ShardingType;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceCenterConfiguration;

import java.util.Properties;

public final class GovernanceRepositoryConfigurationUtil {
    
    private static final String ZOOKEEPER_CONNECTION_STRING = "localhost:2181";
    
    public static GovernanceConfiguration getZooKeeperConfiguration(final boolean overwrite, final ShardingType shardingType) {
        GovernanceCenterConfiguration governanceCenterConfig = new GovernanceCenterConfiguration("ZooKeeper", ZOOKEEPER_CONNECTION_STRING, new Properties());
        switch (shardingType) {
            case SHARDING_DATABASES_AND_TABLES:
                return new GovernanceConfiguration("governance-sharding-data-source", governanceCenterConfig, overwrite);
            case READWRITE_SPLITTING:
                return new GovernanceConfiguration("governance-readwrite-splitting-data-source", governanceCenterConfig, overwrite);
            case ENCRYPT:
                return new GovernanceConfiguration("governance-encrypt-data-source", governanceCenterConfig, overwrite);
            case SHADOW:
                return new GovernanceConfiguration("governance-shadow-data-source", governanceCenterConfig, overwrite);
            default:
                throw new UnsupportedOperationException(shardingType.toString());
        }
    }
    
    public static GovernanceConfiguration getNacosConfiguration(final boolean overwrite, final ShardingType shardingType) {
        Properties zookeeperProperties = new Properties();
        GovernanceCenterConfiguration zookeeperConfig = new GovernanceCenterConfiguration("ZooKeeper", ZOOKEEPER_CONNECTION_STRING, zookeeperProperties);
        switch (shardingType) {
            case SHARDING_DATABASES_AND_TABLES:
                return new GovernanceConfiguration("governance-zookeeper-sharding-data-source", zookeeperConfig, overwrite);
            case READWRITE_SPLITTING:
                return new GovernanceConfiguration("governance-zookeeper-readwrite-splitting-data-source", zookeeperConfig, overwrite);
            case ENCRYPT:
                return new GovernanceConfiguration("governance-zookeeper-encrypt-data-source", zookeeperConfig, overwrite);
            case SHADOW:
                return new GovernanceConfiguration("governance-zookeeper-shadow-data-source", zookeeperConfig, overwrite);
            default:
                throw new UnsupportedOperationException(shardingType.toString());
        }
    }
}
