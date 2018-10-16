/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.example.jdbc.orche;

import io.shardingsphere.example.config.ExampleConfiguration;
import io.shardingsphere.example.jdbc.orche.config.etcd.EtcdMasterSlaveConfiguration;
import io.shardingsphere.example.jdbc.orche.config.etcd.EtcdShardingDatabasesAndTablesConfiguration;
import io.shardingsphere.example.jdbc.orche.config.etcd.EtcdShardingDatabasesConfiguration;
import io.shardingsphere.example.jdbc.orche.config.etcd.EtcdShardingMasterSlaveConfiguration;
import io.shardingsphere.example.jdbc.orche.config.etcd.EtcdShardingTablesConfiguration;
import io.shardingsphere.example.jdbc.orche.config.zookeeper.ZooKeeperMasterSlaveConfiguration;
import io.shardingsphere.example.jdbc.orche.config.zookeeper.ZooKeeperShardingDatabasesAndTablesConfiguration;
import io.shardingsphere.example.jdbc.orche.config.zookeeper.ZooKeeperShardingDatabasesConfiguration;
import io.shardingsphere.example.jdbc.orche.config.zookeeper.ZooKeeperShardingMasterSlaveConfiguration;
import io.shardingsphere.example.jdbc.orche.config.zookeeper.ZooKeeperShardingTablesConfiguration;
import io.shardingsphere.example.repository.api.service.CommonService;
import io.shardingsphere.example.repository.jdbc.repository.JDBCOrderItemRepositoryImpl;
import io.shardingsphere.example.repository.jdbc.repository.JDBCOrderRepositoryImpl;
import io.shardingsphere.example.repository.jdbc.service.RawPojoService;
import io.shardingsphere.example.type.RegistryCenterType;
import io.shardingsphere.example.type.ShardingType;
import io.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationMasterSlaveDataSource;
import io.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationShardingDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

public class JavaConfigurationExample {
    
    private static ShardingType shardingType = ShardingType.SHARDING_DATABASES;
//    private static ShardingType shardingType = ShardingType.SHARDING_TABLES;
//    private static ShardingType shardingType = ShardingType.SHARDING_DATABASES_AND_TABLES;
//    private static ShardingType shardingType = ShardingType.MASTER_SLAVE;
//    private static ShardingType shardingType = ShardingType.SHARDING_MASTER_SLAVE;
    
    private static RegistryCenterType registryCenterType = RegistryCenterType.ZOOKEEPER;
//    private static RegistryCenterType registryCenterType = RegistryCenterType.ETCD;
    
    private static boolean loadConfigFromRegCenter = false;
//    private static boolean loadConfigFromRegCenter = true;
    
    public static void main(final String[] args) throws SQLException {
        process(getDataSource());
    }
    
    private static DataSource getDataSource() throws SQLException {
        ExampleConfiguration exampleConfig;
        switch (shardingType) {
            case SHARDING_DATABASES:
                if (RegistryCenterType.ZOOKEEPER == registryCenterType) {
                    exampleConfig = new ZooKeeperShardingDatabasesConfiguration(loadConfigFromRegCenter);
                } else {
                    exampleConfig = new EtcdShardingDatabasesConfiguration(loadConfigFromRegCenter);
                }
                break;
            case SHARDING_TABLES:
                if (RegistryCenterType.ZOOKEEPER == registryCenterType) {
                    exampleConfig = new ZooKeeperShardingTablesConfiguration(loadConfigFromRegCenter);
                } else {
                    exampleConfig = new EtcdShardingTablesConfiguration(loadConfigFromRegCenter);
                }
                break;
            case SHARDING_DATABASES_AND_TABLES:
                if (RegistryCenterType.ZOOKEEPER == registryCenterType) {
                    exampleConfig = new ZooKeeperShardingDatabasesAndTablesConfiguration(loadConfigFromRegCenter);
                } else {
                    exampleConfig = new EtcdShardingDatabasesAndTablesConfiguration(loadConfigFromRegCenter);
                }
                break;
            case MASTER_SLAVE:
                if (RegistryCenterType.ZOOKEEPER == registryCenterType) {
                    exampleConfig = new ZooKeeperMasterSlaveConfiguration(loadConfigFromRegCenter);
                } else {
                    exampleConfig = new EtcdMasterSlaveConfiguration(loadConfigFromRegCenter);
                }
                break;
            case SHARDING_MASTER_SLAVE:
                if (RegistryCenterType.ZOOKEEPER == registryCenterType) {
                    exampleConfig = new ZooKeeperShardingMasterSlaveConfiguration(loadConfigFromRegCenter);
                } else {
                    exampleConfig = new EtcdShardingMasterSlaveConfiguration(loadConfigFromRegCenter);
                }
                break;
            default:
                throw new UnsupportedOperationException(shardingType.name());
        }
        return exampleConfig.getDataSource();
    }
    
    private static void process(final DataSource dataSource) {
        CommonService commonService = getCommonService(dataSource);
        commonService.initEnvironment();
        commonService.processSuccess();
        commonService.cleanEnvironment();
        closeDataSource(dataSource);
    }
    
    private static CommonService getCommonService(final DataSource dataSource) {
        return new RawPojoService(new JDBCOrderRepositoryImpl(dataSource), new JDBCOrderItemRepositoryImpl(dataSource));
    }
    
    private static void closeDataSource(final DataSource dataSource) {
        if (dataSource instanceof OrchestrationMasterSlaveDataSource) {
            ((OrchestrationMasterSlaveDataSource) dataSource).close();
        } else {
            ((OrchestrationShardingDataSource) dataSource).close();
        }
    }
}
