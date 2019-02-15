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

package io.shardingsphere.example.jdbc.nodep.factory;

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

public class CommonServiceFactory {
    
    public static CommonService newInstance(final ShardingType shardingType) throws SQLException {
        switch (shardingType) {
            case SHARDING_DATABASES:
                return createCommonService(new ShardingDatabasesConfigurationPrecise().getDataSource());
            case SHARDING_DATABASES_RANGE:
                return CreateRangeCommonService(new ShardingDatabasesConfigurationRange().getDataSource());
            case SHARDING_TABLES:
                return createCommonService(new ShardingTablesConfigurationPrecise().getDataSource());
            case SHARDING_TABLES_RANGE:
                return CreateRangeCommonService(new ShardingTablesConfigurationRange().getDataSource());
            case SHARDING_DATABASES_AND_TABLES:
                return createCommonService(new ShardingDatabasesAndTablesConfigurationPrecise().getDataSource());
            case SHARDING_DATABASES_AND_TABLES_RANGE:
                return CreateRangeCommonService(new ShardingDatabasesAndTablesConfigurationRange().getDataSource());
            case MASTER_SLAVE:
                return createCommonService(new MasterSlaveConfiguration().getDataSource());
            case SHARDING_MASTER_SLAVE:
                return createCommonService(new ShardingMasterSlaveConfigurationPrecise().getDataSource());
            case SHARDING_MASTER_SLAVE_RANGE:
                return CreateRangeCommonService(new ShardingMasterSlaveConfigurationRange().getDataSource());
            default:
                throw new UnsupportedOperationException(shardingType.name());
        }
    }
    
    private static CommonService createCommonService(final DataSource dataSource) {
        return new RawPojoService(new JDBCOrderRepositoryImpl(dataSource), new JDBCOrderItemRepositoryImpl(dataSource));
    }
    
    private static CommonService CreateRangeCommonService(final DataSource dataSource) {
        return new RangeRawPojoService(new JDBCOrderRepositoryImpl(dataSource), new JDBCOrderItemRepositoryImpl(dataSource));
    }
}
