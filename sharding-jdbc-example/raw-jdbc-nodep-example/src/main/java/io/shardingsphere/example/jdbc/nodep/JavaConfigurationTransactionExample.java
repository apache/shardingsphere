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

package io.shardingsphere.example.jdbc.nodep;

import io.shardingsphere.example.config.ExampleConfiguration;
import io.shardingsphere.example.jdbc.nodep.config.MasterSlaveConfiguration;
import io.shardingsphere.example.jdbc.nodep.config.ShardingDatabasesAndTablesConfigurationPrecise;
import io.shardingsphere.example.jdbc.nodep.config.ShardingDatabasesAndTablesConfigurationRange;
import io.shardingsphere.example.jdbc.nodep.config.ShardingDatabasesConfigurationPrecise;
import io.shardingsphere.example.jdbc.nodep.config.ShardingDatabasesConfigurationRange;
import io.shardingsphere.example.jdbc.nodep.config.ShardingMasterSlaveConfigurationPrecise;
import io.shardingsphere.example.jdbc.nodep.config.ShardingMasterSlaveConfigurationRange;
import io.shardingsphere.example.jdbc.nodep.config.ShardingTablesConfigurationPrecise;
import io.shardingsphere.example.jdbc.nodep.config.ShardingTablesConfigurationRange;
import io.shardingsphere.example.repository.api.service.TransactionService;
import io.shardingsphere.example.repository.jdbc.repository.JDBCOrderItemTransactionRepositotyImpl;
import io.shardingsphere.example.repository.jdbc.repository.JDBCOrderTransactionRepositoryImpl;
import io.shardingsphere.example.repository.jdbc.service.RawPojoTransactionService;
import io.shardingsphere.example.type.ShardingType;
import org.apache.shardingsphere.transaction.core.TransactionType;

import javax.sql.DataSource;
import java.sql.SQLException;

/*
 * Please make sure master-slave data sync on MySQL is running correctly. Otherwise this example will query empty data from slave.
 */
public class JavaConfigurationTransactionExample {
    
    private static ShardingType type = ShardingType.SHARDING_DATABASES;
//    private static ShardingType type = ShardingType.SHARDING_TABLES;
//    private static ShardingType type = ShardingType.SHARDING_DATABASES_AND_TABLES;
//    private static ShardingType type = ShardingType.MASTER_SLAVE;
//    private static ShardingType type = ShardingType.SHARDING_MASTER_SLAVE;
    
//    private static boolean isRangeSharding = true;
    private static boolean isRangeSharding = false;
    
    public static void main(final String[] args) throws SQLException {
        process(isRangeSharding ? getDataSourceRange() : getDataSourcePrecise());
    }
    
    private static DataSource getDataSourcePrecise() throws SQLException {
        ExampleConfiguration exampleConfig;
        switch (type) {
            case SHARDING_DATABASES:
                exampleConfig = new ShardingDatabasesConfigurationPrecise();
                break;
            case SHARDING_TABLES:
                exampleConfig = new ShardingTablesConfigurationPrecise();
                break;
            case SHARDING_DATABASES_AND_TABLES:
                exampleConfig = new ShardingDatabasesAndTablesConfigurationPrecise();
                break;
            case MASTER_SLAVE:
                exampleConfig = new MasterSlaveConfiguration();
                break;
            case SHARDING_MASTER_SLAVE:
                exampleConfig = new ShardingMasterSlaveConfigurationPrecise();
                break;
            default:
                throw new UnsupportedOperationException(type.name());
        }
        return exampleConfig.getDataSource();
    }
    
    private static DataSource getDataSourceRange() throws SQLException {
        ExampleConfiguration exampleConfig;
        switch (type) {
            case SHARDING_DATABASES:
                exampleConfig = new ShardingDatabasesConfigurationRange();
                break;
            case SHARDING_TABLES:
                exampleConfig = new ShardingTablesConfigurationRange();
                break;
            case SHARDING_DATABASES_AND_TABLES:
                exampleConfig = new ShardingDatabasesAndTablesConfigurationRange();
                break;
            case MASTER_SLAVE:
                exampleConfig = new MasterSlaveConfiguration();
                break;
            case SHARDING_MASTER_SLAVE:
                exampleConfig = new ShardingMasterSlaveConfigurationRange();
                break;
            default:
                throw new UnsupportedOperationException(type.name());
        }
        return exampleConfig.getDataSource();
    }
    
    private static void process(final DataSource dataSource) throws SQLException {
        TransactionService transactionService = getTransactionService(dataSource);
        transactionService.initEnvironment();
        transactionService.processSuccess();
        processFailureSingleTransaction(transactionService, TransactionType.LOCAL);
        processFailureSingleTransaction(transactionService, TransactionType.XA);
        processFailureSingleTransaction(transactionService, TransactionType.BASE);
        processFailureSingleTransaction(transactionService, TransactionType.LOCAL);
        transactionService.cleanEnvironment();
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
}
