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
import io.shardingsphere.example.jdbc.orche.config.RegistryCenterConfigurationUtil;
import io.shardingsphere.example.jdbc.orche.config.type.MasterSlaveConfiguration;
import io.shardingsphere.example.jdbc.orche.config.type.ShardingDatabasesAndTablesConfiguration;
import io.shardingsphere.example.jdbc.orche.config.type.ShardingDatabasesConfiguration;
import io.shardingsphere.example.jdbc.orche.config.type.ShardingMasterSlaveConfiguration;
import io.shardingsphere.example.jdbc.orche.config.type.ShardingTablesConfiguration;
import io.shardingsphere.example.repository.api.service.CommonService;
import io.shardingsphere.example.repository.jdbc.repository.JDBCOrderItemRepositoryImpl;
import io.shardingsphere.example.repository.jdbc.repository.JDBCOrderRepositoryImpl;
import io.shardingsphere.example.repository.jdbc.service.RawPojoService;
import io.shardingsphere.example.type.RegistryCenterType;
import io.shardingsphere.example.type.ShardingType;
import io.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
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
        RegistryCenterConfiguration registryCenterConfig = getRegistryCenterConfiguration();
        switch (shardingType) {
            case SHARDING_DATABASES:
                exampleConfig = new ShardingDatabasesConfiguration(registryCenterConfig, loadConfigFromRegCenter);
                break;
            case SHARDING_TABLES:
                exampleConfig = new ShardingTablesConfiguration(registryCenterConfig, loadConfigFromRegCenter);
                break;
            case SHARDING_DATABASES_AND_TABLES:
                exampleConfig = new ShardingDatabasesAndTablesConfiguration(registryCenterConfig, loadConfigFromRegCenter);
                break;
            case MASTER_SLAVE:
                exampleConfig = new MasterSlaveConfiguration(registryCenterConfig, loadConfigFromRegCenter);
                break;
            case SHARDING_MASTER_SLAVE:
                exampleConfig = new ShardingMasterSlaveConfiguration(registryCenterConfig, loadConfigFromRegCenter);
                break;
            default:
                throw new UnsupportedOperationException(shardingType.name());
        }
        return exampleConfig.getDataSource();
    }
    
    private static RegistryCenterConfiguration getRegistryCenterConfiguration() {
        return RegistryCenterType.ZOOKEEPER == registryCenterType ? RegistryCenterConfigurationUtil.getZooKeeperConfiguration() : RegistryCenterConfigurationUtil.getEtcdConfiguration();
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
