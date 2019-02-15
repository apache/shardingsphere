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

import io.shardingsphere.example.repository.api.service.TransactionService;
import io.shardingsphere.example.repository.jdbc.repository.JDBCOrderItemTransactionRepositotyImpl;
import io.shardingsphere.example.repository.jdbc.repository.JDBCOrderTransactionRepositoryImpl;
import io.shardingsphere.example.repository.jdbc.service.RawPojoTransactionService;
import io.shardingsphere.example.type.RegistryCenterType;
import io.shardingsphere.example.type.ShardingType;
import org.apache.shardingsphere.shardingjdbc.orchestration.api.yaml.YamlOrchestrationMasterSlaveDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.orchestration.api.yaml.YamlOrchestrationShardingDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationMasterSlaveDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationShardingDataSource;
import org.apache.shardingsphere.transaction.core.TransactionType;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/*
 * 1. Please make sure master-slave data sync on MySQL is running correctly. Otherwise this example will query empty data from slave.
 * 2. Please make sure sharding-orchestration-reg-zookeeper-curator in your pom if registryCenterType = RegistryCenterType.ZOOKEEPER.
 * 3. Please make sure sharding-orchestration-reg-etcd in your pom if registryCenterType = RegistryCenterType.ETCD.
 */
public class YamlConfigurationTransactionExample {
    
    private static ShardingType shardingType = ShardingType.SHARDING_DATABASES;
//    private static ShardingType shardingType = ShardingType.SHARDING_TABLES;
//    private static ShardingType shardingType = ShardingType.SHARDING_DATABASES_AND_TABLES;
//    private static ShardingType shardingType = ShardingType.MASTER_SLAVE;
//    private static ShardingType shardingType = ShardingType.SHARDING_MASTER_SLAVE;
    
    private static RegistryCenterType registryCenterType = RegistryCenterType.ZOOKEEPER;
//    private static RegistryCenterType registryCenterType = RegistryCenterType.ETCD;
    
    private static boolean loadConfigFromRegCenter = false;
//    private static boolean loadConfigFromRegCenter = true;
    
    public static void main(final String[] args) throws Exception {
        process(getDataSource());
    }
    
    private static DataSource getDataSource() throws IOException, SQLException {
        return ShardingType.MASTER_SLAVE == shardingType
                ? YamlOrchestrationMasterSlaveDataSourceFactory.createDataSource(getYamlFile()) : YamlOrchestrationShardingDataSourceFactory.createDataSource(getYamlFile());
    }
    
    private static File getYamlFile() {
        String result;
        switch (shardingType) {
            case SHARDING_DATABASES:
                result = String.format("/META-INF/%s/%s/sharding-databases.yaml", registryCenterType.name().toLowerCase(), loadConfigFromRegCenter ? "cloud" : "local");
                break;
            case SHARDING_TABLES:
                result = String.format("/META-INF/%s/%s/sharding-tables.yaml", registryCenterType.name().toLowerCase(), loadConfigFromRegCenter ? "cloud" : "local");
                break;
            case SHARDING_DATABASES_AND_TABLES:
                result = String.format("/META-INF/%s/%s/sharding-databases-tables.yaml", registryCenterType.name().toLowerCase(), loadConfigFromRegCenter ? "cloud" : "local");
                break;
            case MASTER_SLAVE:
                result = String.format("/META-INF/%s/%s/master-slave.yaml", registryCenterType.name().toLowerCase(), loadConfigFromRegCenter ? "cloud" : "local");
                break;
            case SHARDING_MASTER_SLAVE:
                result = String.format("/META-INF/%s/%s/sharding-master-slave.yaml", registryCenterType.name().toLowerCase(), loadConfigFromRegCenter ? "cloud" : "local");
                break;
            default:
                throw new UnsupportedOperationException(shardingType.name());
        }
        return new File(YamlConfigurationTransactionExample.class.getResource(result).getFile());
    }
    
    private static void process(final DataSource dataSource) throws Exception {
        TransactionService transactionService = getTransactionService(dataSource);
        transactionService.initEnvironment();
        transactionService.processSuccess();
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
            transactionService.printData();
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
