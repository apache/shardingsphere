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

package io.shardingsphere.example.jdbc.orche.factory;

import io.shardingsphere.example.config.ExampleConfiguration;
import io.shardingsphere.example.jdbc.orche.config.RegistryCenterConfigurationUtil;
import io.shardingsphere.example.jdbc.orche.config.cloud.CloudMasterSlaveConfiguration;
import io.shardingsphere.example.jdbc.orche.config.cloud.CloudShardingDatabasesAndTablesConfiguration;
import io.shardingsphere.example.jdbc.orche.config.cloud.CloudShardingDatabasesConfiguration;
import io.shardingsphere.example.jdbc.orche.config.cloud.CloudShardingMasterSlaveConfiguration;
import io.shardingsphere.example.jdbc.orche.config.cloud.CloudShardingTablesConfiguration;
import io.shardingsphere.example.jdbc.orche.config.local.LocalMasterSlaveConfiguration;
import io.shardingsphere.example.jdbc.orche.config.local.LocalShardingDatabasesAndTablesConfiguration;
import io.shardingsphere.example.jdbc.orche.config.local.LocalShardingDatabasesConfiguration;
import io.shardingsphere.example.jdbc.orche.config.local.LocalShardingMasterSlaveConfiguration;
import io.shardingsphere.example.jdbc.orche.config.local.LocalShardingTablesConfiguration;
import io.shardingsphere.example.type.RegistryCenterType;
import io.shardingsphere.example.type.ShardingType;
import org.apache.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;

public class OrchestrationDataSourceFactory {
    
    public static DataSource newInstance(final ShardingType shardingType, final RegistryCenterType registryCenterType, final boolean loadConfigFromRegCenter) throws SQLException {
        RegistryCenterConfiguration registryCenterConfig = getRegistryCenterConfiguration(registryCenterType);
        ExampleConfiguration configuration;
        switch (shardingType) {
            case SHARDING_DATABASES:
                configuration = loadConfigFromRegCenter ? new CloudShardingDatabasesConfiguration(registryCenterConfig) : new LocalShardingDatabasesConfiguration(registryCenterConfig);
                break;
            case SHARDING_TABLES:
                configuration = loadConfigFromRegCenter ? new CloudShardingTablesConfiguration(registryCenterConfig) : new LocalShardingTablesConfiguration(registryCenterConfig);
                break;
            case SHARDING_DATABASES_AND_TABLES:
                configuration = loadConfigFromRegCenter ? new CloudShardingDatabasesAndTablesConfiguration(registryCenterConfig) : new LocalShardingDatabasesAndTablesConfiguration(registryCenterConfig);
                break;
            case MASTER_SLAVE:
                configuration = loadConfigFromRegCenter ? new CloudMasterSlaveConfiguration(registryCenterConfig) : new LocalMasterSlaveConfiguration(registryCenterConfig);
                break;
            case SHARDING_MASTER_SLAVE:
                configuration = loadConfigFromRegCenter ? new CloudShardingMasterSlaveConfiguration(registryCenterConfig) : new LocalShardingMasterSlaveConfiguration(registryCenterConfig);
                break;
            default:
                throw new UnsupportedOperationException(shardingType.name());
        }
        return configuration.getDataSource();
    }
    
    private static RegistryCenterConfiguration getRegistryCenterConfiguration(final RegistryCenterType registryCenterType) {
        return RegistryCenterType.ZOOKEEPER == registryCenterType ? RegistryCenterConfigurationUtil.getZooKeeperConfiguration() : RegistryCenterConfigurationUtil.getEtcdConfiguration();
    }
}
