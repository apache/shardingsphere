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
import io.shardingsphere.example.repository.jdbc.repository.JDBCOrderItemTransactionRepositotyImpl;
import io.shardingsphere.example.repository.jdbc.repository.JDBCOrderRepositoryImpl;
import io.shardingsphere.example.repository.jdbc.repository.JDBCOrderTransactionRepositoryImpl;
import io.shardingsphere.example.repository.jdbc.service.RangeRawPojoService;
import io.shardingsphere.example.repository.jdbc.service.RawPojoService;
import io.shardingsphere.example.repository.jdbc.service.RawPojoTransactionService;
import io.shardingsphere.example.type.ConfigurationType;
import io.shardingsphere.example.type.ServiceType;
import io.shardingsphere.example.type.ShardingType;
import org.apache.shardingsphere.shardingjdbc.api.yaml.YamlMasterSlaveDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.api.yaml.YamlShardingDataSourceFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class CommonServiceFactory {
    
    public static CommonService newInstance(final ConfigurationType configurationType, final ShardingType shardingType) throws SQLException, IOException {
        return newInstance(configurationType, ServiceType.RAW, shardingType);
    }
    
    public static CommonService newInstance(final ConfigurationType configurationType, final ServiceType serviceType, final ShardingType shardingType) throws SQLException, IOException {
        switch (configurationType) {
            case RAW:
                return createRawCommonService(serviceType, shardingType);
            case YAML:
                return createYamlCommonService(serviceType, shardingType);
            default:
                throw new UnsupportedOperationException(configurationType.name());
        }
    }
    
    private static CommonService createRawCommonService(final ServiceType serviceType, final ShardingType shardingType) throws SQLException {
        switch (shardingType) {
            case SHARDING_DATABASES:
                return getCommonService(serviceType, new ShardingDatabasesConfigurationPrecise().getDataSource());
            case SHARDING_DATABASES_RANGE:
                return getRangeCommonService(serviceType, new ShardingDatabasesConfigurationRange().getDataSource());
            case SHARDING_TABLES:
                return getCommonService(serviceType, new ShardingTablesConfigurationPrecise().getDataSource());
            case SHARDING_TABLES_RANGE:
                return getRangeCommonService(serviceType, new ShardingTablesConfigurationRange().getDataSource());
            case SHARDING_DATABASES_AND_TABLES:
                return getCommonService(serviceType, new ShardingDatabasesAndTablesConfigurationPrecise().getDataSource());
            case SHARDING_DATABASES_AND_TABLES_RANGE:
                return getRangeCommonService(serviceType, new ShardingDatabasesAndTablesConfigurationRange().getDataSource());
            case MASTER_SLAVE:
                return getCommonService(serviceType, new MasterSlaveConfiguration().getDataSource());
            case SHARDING_MASTER_SLAVE:
                return getCommonService(serviceType, new ShardingMasterSlaveConfigurationPrecise().getDataSource());
            case SHARDING_MASTER_SLAVE_RANGE:
                return getRangeCommonService(serviceType, new ShardingMasterSlaveConfigurationRange().getDataSource());
            default:
                throw new UnsupportedOperationException(shardingType.name());
        }
    }
    
    private static CommonService createYamlCommonService(final ServiceType serviceType, final ShardingType shardingType) throws SQLException, IOException {
        switch (shardingType) {
            case SHARDING_DATABASES:
                return getCommonService(serviceType, YamlShardingDataSourceFactory.createDataSource(getFile("/META-INF/sharding-databases.yaml")));
            case SHARDING_TABLES:
                return getCommonService(serviceType, YamlShardingDataSourceFactory.createDataSource(getFile("/META-INF/sharding-tables.yaml")));
            case SHARDING_DATABASES_AND_TABLES:
                return getCommonService(serviceType, YamlShardingDataSourceFactory.createDataSource(getFile("/META-INF/sharding-databases-tables.yaml")));
            case MASTER_SLAVE:
                return getCommonService(serviceType, YamlMasterSlaveDataSourceFactory.createDataSource(getFile("/META-INF/master-slave.yaml")));
            case SHARDING_MASTER_SLAVE:
                return getCommonService(serviceType, YamlShardingDataSourceFactory.createDataSource(getFile("/META-INF/sharding-master-slave.yaml")));
            default:
                throw new UnsupportedOperationException(shardingType.name());
        }
    }
    
    private static CommonService getCommonService(final ServiceType serviceType, final DataSource dataSource) throws SQLException {
        switch (serviceType) {
            case RAW:
                return new RawPojoService(new JDBCOrderRepositoryImpl(dataSource), new JDBCOrderItemRepositoryImpl(dataSource));
            case TRANSACTION:
                return new RawPojoTransactionService(new JDBCOrderTransactionRepositoryImpl(dataSource), new JDBCOrderItemTransactionRepositotyImpl(dataSource), dataSource);
            default:
                throw new UnsupportedOperationException(serviceType.name());
        }
    }
    
    private static CommonService getRangeCommonService(final ServiceType serviceType, final DataSource dataSource) {
        switch (serviceType) {
            case RAW:
                return new RangeRawPojoService(new JDBCOrderRepositoryImpl(dataSource), new JDBCOrderItemRepositoryImpl(dataSource));
            default:
                throw new UnsupportedOperationException(serviceType.name());
        }
    }
    
    private static File getFile(final String fileName) {
        return new File(Thread.currentThread().getClass().getResource(fileName).getFile());
    }
}
