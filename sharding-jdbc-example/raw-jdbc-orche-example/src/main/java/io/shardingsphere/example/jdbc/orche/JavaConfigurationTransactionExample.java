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
import io.shardingsphere.example.jdbc.orche.config.cloud.CloudMasterSlaveConfiguration;
import io.shardingsphere.example.jdbc.orche.config.cloud.CloudShardingDatabasesAndTablesConfiguration;
import io.shardingsphere.example.jdbc.orche.config.cloud.CloudShardingDatabasesConfiguration;
import io.shardingsphere.example.jdbc.orche.config.cloud.CloudShardingMasterSlaveConfiguration;
import io.shardingsphere.example.jdbc.orche.config.cloud.CloudShardingTablesConfiguration;
import io.shardingsphere.example.jdbc.orche.config.local.LocalMasterSlaveConfiguration;
import io.shardingsphere.example.jdbc.orche.config.local.LocalShardingDatabasesAndTablesConfigurationPrecise;
import io.shardingsphere.example.jdbc.orche.config.local.LocalShardingDatabasesAndTablesConfigurationRange;
import io.shardingsphere.example.jdbc.orche.config.local.LocalShardingDatabasesConfigurationPrecise;
import io.shardingsphere.example.jdbc.orche.config.local.LocalShardingDatabasesConfigurationRange;
import io.shardingsphere.example.jdbc.orche.config.local.LocalShardingMasterSlaveConfigurationPrecise;
import io.shardingsphere.example.jdbc.orche.config.local.LocalShardingMasterSlaveConfigurationRange;
import io.shardingsphere.example.jdbc.orche.config.local.LocalShardingTablesConfigurationPrecise;
import io.shardingsphere.example.jdbc.orche.config.local.LocalShardingTablesConfigurationRange;
import io.shardingsphere.example.repository.api.service.TransactionService;
import io.shardingsphere.example.repository.jdbc.repository.JDBCOrderItemTransactionRepositotyImpl;
import io.shardingsphere.example.repository.jdbc.repository.JDBCOrderTransactionRepositoryImpl;
import io.shardingsphere.example.repository.jdbc.service.RawPojoTransactionService;
import io.shardingsphere.example.type.RegistryCenterType;
import io.shardingsphere.example.type.ShardingType;
import io.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import io.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationMasterSlaveDataSource;
import io.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationShardingDataSource;
import io.shardingsphere.transaction.api.TransactionType;

import javax.sql.DataSource;
import java.sql.SQLException;

/*
 * 1. Please make sure master-slave data sync on MySQL is running correctly. Otherwise this example will query empty data from slave.
 * 2. Please make sure sharding-orchestration-reg-zookeeper-curator in your pom if registryCenterType = RegistryCenterType.ZOOKEEPER.
 * 3. Please make sure sharding-orchestration-reg-etcd in your pom if registryCenterType = RegistryCenterType.ETCD.
 */
public class JavaConfigurationTransactionExample {
    
    private static ShardingType shardingType = ShardingType.SHARDING_DATABASES;
//    private static ShardingType shardingType = ShardingType.SHARDING_TABLES;
//    private static ShardingType shardingType = ShardingType.SHARDING_DATABASES_AND_TABLES;
//    private static ShardingType shardingType = ShardingType.MASTER_SLAVE;
//    private static ShardingType shardingType = ShardingType.SHARDING_MASTER_SLAVE;
    
//    private static boolean isRangeSharding = true;
    private static boolean isRangeSharding = false;
    
    private static RegistryCenterType registryCenterType = RegistryCenterType.ZOOKEEPER;
//    private static RegistryCenterType registryCenterType = RegistryCenterType.ETCD;
    
    private static boolean loadConfigFromRegCenter = false;
//    private static boolean loadConfigFromRegCenter = true;
    
    public static void main(final String[] args) throws Exception {
        process(isRangeSharding ? getDataSourceRange() : getDataSourcePrecise());
    }
    
    private static DataSource getDataSourcePrecise() throws SQLException {
        ExampleConfiguration exampleConfig;
        RegistryCenterConfiguration registryCenterConfig = getRegistryCenterConfiguration();
        switch (shardingType) {
            case SHARDING_DATABASES:
                exampleConfig = loadConfigFromRegCenter ? new CloudShardingDatabasesConfiguration(registryCenterConfig) : new LocalShardingDatabasesConfigurationPrecise(registryCenterConfig);
                break;
            case SHARDING_TABLES:
                exampleConfig = loadConfigFromRegCenter ? new CloudShardingTablesConfiguration(registryCenterConfig) : new LocalShardingTablesConfigurationPrecise(registryCenterConfig);
                break;
            case SHARDING_DATABASES_AND_TABLES:
                exampleConfig = loadConfigFromRegCenter
                        ? new CloudShardingDatabasesAndTablesConfiguration(registryCenterConfig) : new LocalShardingDatabasesAndTablesConfigurationPrecise(registryCenterConfig);
                break;
            case MASTER_SLAVE:
                exampleConfig = loadConfigFromRegCenter ? new CloudMasterSlaveConfiguration(registryCenterConfig) : new LocalMasterSlaveConfiguration(registryCenterConfig);
                break;
            case SHARDING_MASTER_SLAVE:
                exampleConfig = loadConfigFromRegCenter ? new CloudShardingMasterSlaveConfiguration(registryCenterConfig) : new LocalShardingMasterSlaveConfigurationPrecise(registryCenterConfig);
                break;
            default:
                throw new UnsupportedOperationException(shardingType.name());
        }
        return exampleConfig.getDataSource();
    }
    
    private static DataSource getDataSourceRange() throws Exception {
        ExampleConfiguration exampleConfig;
        RegistryCenterConfiguration registryCenterConfig = getRegistryCenterConfiguration();
        switch (shardingType) {
            case SHARDING_DATABASES:
                exampleConfig = loadConfigFromRegCenter ? new CloudShardingDatabasesConfiguration(registryCenterConfig) : new LocalShardingDatabasesConfigurationRange(registryCenterConfig);
                break;
            case SHARDING_TABLES:
                exampleConfig = loadConfigFromRegCenter ? new CloudShardingTablesConfiguration(registryCenterConfig) : new LocalShardingTablesConfigurationRange(registryCenterConfig);
                break;
            case SHARDING_DATABASES_AND_TABLES:
                exampleConfig = loadConfigFromRegCenter
                    ? new CloudShardingDatabasesAndTablesConfiguration(registryCenterConfig) : new LocalShardingDatabasesAndTablesConfigurationRange(registryCenterConfig);
                break;
            case MASTER_SLAVE:
                exampleConfig = loadConfigFromRegCenter ? new CloudMasterSlaveConfiguration(registryCenterConfig) : new LocalMasterSlaveConfiguration(registryCenterConfig);
                break;
            case SHARDING_MASTER_SLAVE:
                exampleConfig = loadConfigFromRegCenter ? new CloudShardingMasterSlaveConfiguration(registryCenterConfig) : new LocalShardingMasterSlaveConfigurationRange(registryCenterConfig);
                break;
            default:
                throw new UnsupportedOperationException(shardingType.name());
        }
        return exampleConfig.getDataSource();
    }
    
    private static RegistryCenterConfiguration getRegistryCenterConfiguration() {
        return RegistryCenterType.ZOOKEEPER == registryCenterType ? RegistryCenterConfigurationUtil.getZooKeeperConfiguration() : RegistryCenterConfigurationUtil.getEtcdConfiguration();
    }
    
    private static void process(final DataSource dataSource) throws Exception {
        TransactionService transactionService = getTransactionService(dataSource);
        transactionService.initEnvironment();
        transactionService.processSuccess(isRangeSharding);
        processFailureSingleTransaction(transactionService, TransactionType.LOCAL);
        processFailureSingleTransaction(transactionService, TransactionType.XA);
        processFailureSingleTransaction(transactionService, TransactionType.BASE);
        processFailureSingleTransaction(transactionService, TransactionType.LOCAL);
        transactionService.cleanEnvironment();
        closeDataSource(dataSource);
    }
    
    private static void processFailureSingleTransaction(final TransactionService transactionService, final TransactionType type) {
        try {
            switch (type) {
                case LOCAL:
                    transactionService.processFailureWithLocal();
                    break;
                case XA:
                    transactionService.processFailureWithXa();
                    break;
                case BASE:
                    transactionService.processFailureWithBase();
                    break;
                default:
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            transactionService.printData(false);
        }
    }
    
    private static TransactionService getTransactionService(final DataSource dataSource) throws SQLException {
        return new RawPojoTransactionService(new JDBCOrderTransactionRepositoryImpl(dataSource), new JDBCOrderItemTransactionRepositotyImpl(dataSource), dataSource);
    }
    
    private static void closeDataSource(final DataSource dataSource) throws Exception {
        if (dataSource instanceof OrchestrationMasterSlaveDataSource) {
            ((OrchestrationMasterSlaveDataSource) dataSource).close();
        } else {
            ((OrchestrationShardingDataSource) dataSource).close();
        }
    }
}
