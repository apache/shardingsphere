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
import io.shardingsphere.example.jdbc.orche.config.local.LocalShardingDatabasesAndTablesConfigurationPrecise;
import io.shardingsphere.example.jdbc.orche.config.local.LocalShardingDatabasesConfigurationPrecise;
import io.shardingsphere.example.jdbc.orche.config.local.LocalShardingMasterSlaveConfigurationPrecise;
import io.shardingsphere.example.jdbc.orche.config.local.LocalShardingTablesConfigurationPrecise;
import io.shardingsphere.example.repository.api.service.TransactionService;
import io.shardingsphere.example.repository.jdbc.repository.JDBCOrderItemTransactionRepositotyImpl;
import io.shardingsphere.example.repository.jdbc.repository.JDBCOrderTransactionRepositoryImpl;
import io.shardingsphere.example.repository.jdbc.service.RawPojoTransactionService;
import io.shardingsphere.example.type.RegistryCenterType;
import io.shardingsphere.example.type.ShardingType;
import org.apache.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;

public class CommonTransactionServiceFactory {
    
    public static TransactionService newInstance(final ShardingType shardingType, final RegistryCenterType registryCenterType, final boolean loadConfigFromRegCenter) throws SQLException {
        RegistryCenterConfiguration registryCenterConfig = getRegistryCenterConfiguration(registryCenterType);
        ExampleConfiguration exampleConfig;
        switch (shardingType) {
            case SHARDING_DATABASES:
                exampleConfig = loadConfigFromRegCenter ? new CloudShardingDatabasesConfiguration(registryCenterConfig) : new LocalShardingDatabasesConfigurationPrecise(registryCenterConfig);
                return createTransactionService(exampleConfig);
            case SHARDING_TABLES:
                exampleConfig = loadConfigFromRegCenter ? new CloudShardingTablesConfiguration(registryCenterConfig) : new LocalShardingTablesConfigurationPrecise(registryCenterConfig);
                return createTransactionService(exampleConfig);
            case SHARDING_DATABASES_AND_TABLES:
                exampleConfig = loadConfigFromRegCenter ? new CloudShardingDatabasesAndTablesConfiguration(registryCenterConfig) : new LocalShardingDatabasesAndTablesConfigurationPrecise(registryCenterConfig);
                return createTransactionService(exampleConfig);
            case MASTER_SLAVE:
                exampleConfig = loadConfigFromRegCenter ? new CloudMasterSlaveConfiguration(registryCenterConfig) : new LocalMasterSlaveConfiguration(registryCenterConfig);
                return createTransactionService(exampleConfig);
            case SHARDING_MASTER_SLAVE:
                exampleConfig = loadConfigFromRegCenter ? new CloudShardingMasterSlaveConfiguration(registryCenterConfig) : new LocalShardingMasterSlaveConfigurationPrecise(registryCenterConfig);
                return createTransactionService(exampleConfig);
            default:
                throw new UnsupportedOperationException(shardingType.name());
        }
    }
    
    private static RegistryCenterConfiguration getRegistryCenterConfiguration(final RegistryCenterType registryCenterType) {
        return RegistryCenterType.ZOOKEEPER == registryCenterType ? RegistryCenterConfigurationUtil.getZooKeeperConfiguration() : RegistryCenterConfigurationUtil.getEtcdConfiguration();
    }
    
    private static TransactionService createTransactionService(final ExampleConfiguration exampleConfiguration) throws SQLException {
        DataSource dataSource = exampleConfiguration.getDataSource();
        return new RawPojoTransactionService(new JDBCOrderTransactionRepositoryImpl(dataSource), new JDBCOrderItemTransactionRepositotyImpl(dataSource), dataSource);
    }
}
