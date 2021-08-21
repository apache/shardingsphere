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
import org.apache.shardingsphere.governance.repository.api.config.RegistryCenterConfiguration;
import org.apache.shardingsphere.infra.mode.config.ModeConfiguration;

import java.util.Properties;

public final class ClusterModeConfigurationUtil {
    
    private static final String ZOOKEEPER_CONNECTION_STRING = "localhost:2181";
    
    public static ModeConfiguration getZooKeeperConfiguration(final boolean overwrite, final ShardingType shardingType) {
        RegistryCenterConfiguration registryCenterConfig;
        switch (shardingType) {
            case SHARDING_DATABASES_AND_TABLES:
                registryCenterConfig = new RegistryCenterConfiguration("ZooKeeper", "governance-sharding-data-source", ZOOKEEPER_CONNECTION_STRING, new Properties());
                return new ModeConfiguration("Cluster", registryCenterConfig, overwrite);
            case READWRITE_SPLITTING:
                registryCenterConfig = new RegistryCenterConfiguration("ZooKeeper", "governance-readwrite-splitting-data-source", ZOOKEEPER_CONNECTION_STRING, new Properties());
                return new ModeConfiguration("Cluster", registryCenterConfig, overwrite);
            case ENCRYPT:
                registryCenterConfig = new RegistryCenterConfiguration("ZooKeeper", "governance-encrypt-data-source", ZOOKEEPER_CONNECTION_STRING, new Properties());
                return new ModeConfiguration("Cluster", registryCenterConfig, overwrite);
            case SHADOW:
                registryCenterConfig = new RegistryCenterConfiguration("ZooKeeper", "governance-shadow-data-source", ZOOKEEPER_CONNECTION_STRING, new Properties());
                return new ModeConfiguration("Cluster", registryCenterConfig, overwrite);
            default:
                throw new UnsupportedOperationException(shardingType.toString());
        }
    }
}
