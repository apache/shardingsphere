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

package com.dangdang.ddframe.rdb.sharding.spring.datasource;

import com.dangdang.ddframe.rdb.sharding.rule.MasterSlaveRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.slave.MasterSlaveLoadBalanceStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.slave.MasterSlaveLoadBalanceStrategyType;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.MasterSlaveDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

/**
 * Sharding datasource for spring namespace.
 *
 * @author zhangliang
 */
public class SpringMasterSlaveDataSource extends MasterSlaveDataSource {
    
    public SpringMasterSlaveDataSource(final String name, final String masterDataSourceName,
                                       final DataSource masterDataSource, final Map<String, DataSource> slaveDataSourceMap) throws SQLException {
        super(new MasterSlaveRule(name, masterDataSourceName, masterDataSource, slaveDataSourceMap));
    }
    
    public SpringMasterSlaveDataSource(final String name, final String masterDataSourceName,
                                       final DataSource masterDataSource, final Map<String, DataSource> slaveDataSourceMap, final MasterSlaveLoadBalanceStrategy strategy) throws SQLException {
        super(new MasterSlaveRule(name, masterDataSourceName, masterDataSource, slaveDataSourceMap, strategy));
    }
    
    public SpringMasterSlaveDataSource(final String name, final String masterDataSourceName,
                                       final DataSource masterDataSource, final Map<String, DataSource> slaveDataSourceMap, final MasterSlaveLoadBalanceStrategyType strategyType) throws SQLException {
        super(new MasterSlaveRule(name, masterDataSourceName, masterDataSource, slaveDataSourceMap, strategyType.getStrategy()));
    }
}
