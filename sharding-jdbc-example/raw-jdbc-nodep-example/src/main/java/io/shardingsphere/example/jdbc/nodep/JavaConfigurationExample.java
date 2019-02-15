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

import io.shardingsphere.example.jdbc.nodep.config.MasterSlaveConfiguration;
import io.shardingsphere.example.jdbc.nodep.config.ShardingDatabasesAndTablesConfigurationPrecise;
import io.shardingsphere.example.jdbc.nodep.config.ShardingDatabasesAndTablesConfigurationRange;
import io.shardingsphere.example.jdbc.nodep.config.ShardingDatabasesConfigurationPrecise;
import io.shardingsphere.example.jdbc.nodep.config.ShardingDatabasesConfigurationRange;
import io.shardingsphere.example.jdbc.nodep.config.ShardingMasterSlaveConfigurationPrecise;
import io.shardingsphere.example.jdbc.nodep.config.ShardingMasterSlaveConfigurationRange;
import io.shardingsphere.example.jdbc.nodep.config.ShardingTablesConfigurationPrecise;
import io.shardingsphere.example.jdbc.nodep.config.ShardingTablesConfigurationRange;
import io.shardingsphere.example.repository.api.service.CommonService;
import io.shardingsphere.example.repository.jdbc.repository.JDBCOrderItemRepositoryImpl;
import io.shardingsphere.example.repository.jdbc.repository.JDBCOrderRepositoryImpl;
import io.shardingsphere.example.repository.jdbc.service.RangeRawPojoService;
import io.shardingsphere.example.repository.jdbc.service.RawPojoService;
import io.shardingsphere.example.type.ShardingType;

import javax.sql.DataSource;
import java.sql.SQLException;

/*
 * Please make sure master-slave data sync on MySQL is running correctly. Otherwise this example will query empty data from slave.
 */
public class JavaConfigurationExample {
    
//    private static ShardingType type = ShardingType.SHARDING_DATABASES;
//    private static ShardingType type = ShardingType.SHARDING_TABLES;
    private static ShardingType type = ShardingType.SHARDING_DATABASES_AND_TABLES;
//    private static ShardingType type = ShardingType.MASTER_SLAVE;
//    private static ShardingType type = ShardingType.SHARDING_MASTER_SLAVE;
    
//    private static boolean isRangeSharding = true;
    
    public static void main(final String[] args) throws SQLException {
        process(getCommonService(type));
    }
    
    private static void process(final CommonService commonService) {
        commonService.initEnvironment();
        commonService.processSuccess();
        commonService.cleanEnvironment();
    }
    
    private static CommonService getCommonService(final ShardingType shardingType) throws SQLException {
        switch (shardingType) {
            case SHARDING_DATABASES:
                return getCommonService(new ShardingDatabasesConfigurationPrecise().getDataSource());
            case SHARDING_DATABASES_RANGE:
                return getRangeCommonService(new ShardingDatabasesConfigurationRange().getDataSource());
            case SHARDING_TABLES:
                return getCommonService(new ShardingTablesConfigurationPrecise().getDataSource());
            case SHARDING_TABLES_RANGE:
                return getRangeCommonService(new ShardingTablesConfigurationRange().getDataSource());
            case SHARDING_DATABASES_AND_TABLES:
                return getCommonService(new ShardingDatabasesAndTablesConfigurationPrecise().getDataSource());
            case SHARDING_DATABASES_AND_TABLES_RANGE:
                return getRangeCommonService(new ShardingDatabasesAndTablesConfigurationRange().getDataSource());
            case MASTER_SLAVE:
                return getCommonService(new MasterSlaveConfiguration().getDataSource());
            case SHARDING_MASTER_SLAVE:
                return getCommonService(new ShardingMasterSlaveConfigurationPrecise().getDataSource());
            case SHARDING_MASTER_SLAVE_RANGE:
                return getRangeCommonService(new ShardingMasterSlaveConfigurationRange().getDataSource());
            default:
                throw new UnsupportedOperationException(type.name());
        }
    }
    
    private static CommonService getCommonService(final DataSource dataSource) {
        return new RawPojoService(new JDBCOrderRepositoryImpl(dataSource), new JDBCOrderItemRepositoryImpl(dataSource));
    }
    
    private static CommonService getRangeCommonService(final DataSource dataSource) {
        return new RangeRawPojoService(new JDBCOrderRepositoryImpl(dataSource), new JDBCOrderItemRepositoryImpl(dataSource));
    }
}
