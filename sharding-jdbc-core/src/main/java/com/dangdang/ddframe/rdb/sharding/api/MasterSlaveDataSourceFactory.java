/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.api;

import com.dangdang.ddframe.rdb.sharding.api.strategy.slave.MasterSlaveLoadBalanceStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.slave.MasterSlaveLoadBalanceStrategyType;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.MasterSlaveDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Master-slave data source factory.
 * 
 * @author zhangliang 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MasterSlaveDataSourceFactory {
    
    /**
     * Create master-slave data source.
     * 
     * <p>One master data source can configure multiple slave data source.</p>
     * 
     * @deprecated will remove at 1.6.0
     * @param name data source name
     * @param masterDataSource data source for master
     * @param slaveDataSource data source for slave
     * @param otherSlaveDataSources other data sources for slave
     * @return master-slave data source
     * @throws SQLException SQL exception
     */
    @Deprecated
    public static DataSource createDataSource(final String name, final DataSource masterDataSource, final DataSource slaveDataSource, final DataSource... otherSlaveDataSources) throws SQLException {
        Map<String, DataSource> slaveDataSourceMap = new LinkedHashMap<>(otherSlaveDataSources.length + 1);
        slaveDataSourceMap.put(slaveDataSource.toString(), slaveDataSource);
        for (DataSource each : otherSlaveDataSources) {
            slaveDataSourceMap.put(each.toString(), each);
        }
        return new MasterSlaveDataSource(name, masterDataSource.toString(), masterDataSource, slaveDataSourceMap, MasterSlaveLoadBalanceStrategyType.getDefaultStrategyType());
    }
    
    /**
     * Create master-slave data source.
     *
     * <p>One master data source can configure multiple slave data source.</p>
     *
     * @param name data source name
     * @param masterDataSourceName name of data source for master
     * @param masterDataSource data source for master
     * @param slaveDataSourceMap map of data source name and data source for slave
     * @return master-slave data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final String name, final String masterDataSourceName, final DataSource masterDataSource, 
                                              final Map<String, DataSource> slaveDataSourceMap) throws SQLException {
        return new MasterSlaveDataSource(name, masterDataSourceName, masterDataSource, slaveDataSourceMap, MasterSlaveLoadBalanceStrategyType.getDefaultStrategyType());
    }
    
    /**
     * Create master-slave data source.
     *
     * <p>One master data source can configure multiple slave data source.</p>
     *
     * @param name data source name
     * @param masterDataSourceName name of data source for master
     * @param masterDataSource data source for master
     * @param slaveDataSourceMap map of data source name and data source for slave
     * @param strategyType master-slave database load-balance strategy type
     * @return master-slave data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final String name, final String masterDataSourceName, final DataSource masterDataSource, 
                                              final Map<String, DataSource> slaveDataSourceMap, final MasterSlaveLoadBalanceStrategyType strategyType) throws SQLException {
        return new MasterSlaveDataSource(name, masterDataSourceName, masterDataSource, slaveDataSourceMap, strategyType);
    }
    
    
    /**
     * Create master-slave data source.
     *
     * <p>One master data source can configure multiple slave data source.</p>
     *
     * @param name data source name
     * @param masterDataSourceName name of data source for master
     * @param masterDataSource data source for master
     * @param slaveDataSourceMap map of data source name and data source for slave
     * @param strategy master-slave database load-balance strategy
     * @return master-slave data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final String name, final String masterDataSourceName, final DataSource masterDataSource, 
                                              final Map<String, DataSource> slaveDataSourceMap, final MasterSlaveLoadBalanceStrategy strategy) throws SQLException {
        return new MasterSlaveDataSource(name, masterDataSourceName, masterDataSource, slaveDataSourceMap, strategy);
    }
}
