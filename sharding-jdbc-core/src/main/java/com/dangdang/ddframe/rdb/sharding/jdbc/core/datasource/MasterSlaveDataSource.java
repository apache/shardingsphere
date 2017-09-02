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

package com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource;

import com.dangdang.ddframe.rdb.sharding.api.strategy.slave.MasterSlaveLoadBalanceStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.slave.MasterSlaveLoadBalanceStrategyType;
import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import com.dangdang.ddframe.rdb.sharding.hint.HintManagerHolder;
import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractDataSourceAdapter;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.connection.MasterSlaveConnection;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import lombok.Getter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Database that support master-slave.
 *
 * @author zhangliang
 */
public final class MasterSlaveDataSource extends AbstractDataSourceAdapter {
    
    private static final ThreadLocal<Boolean> DML_FLAG = new ThreadLocal<Boolean>() {
        
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };
    
    private final String name;
    
    private final String masterDataSourceName;
    
    @Getter
    private final DataSource masterDataSource;
    
    @Getter
    private final Map<String, DataSource> slaveDataSources;
    
    private final MasterSlaveLoadBalanceStrategy masterSlaveLoadBalanceStrategy;
    
    public MasterSlaveDataSource(final String name, final String masterDataSourceName, final DataSource masterDataSource,
                                 final Map<String, DataSource> slaveDataSources, final MasterSlaveLoadBalanceStrategyType strategyType) throws SQLException {
        this(name, masterDataSourceName, masterDataSource, slaveDataSources, strategyType.getStrategy());
    }
    
    public MasterSlaveDataSource(final String name, final String masterDataSourceName, final DataSource masterDataSource, 
                                 final Map<String, DataSource> slaveDataSources, final MasterSlaveLoadBalanceStrategy masterSlaveLoadBalanceStrategy) throws SQLException {
        super(getAllDataSources(masterDataSource, slaveDataSources.values()));
        this.name = name;
        this.masterDataSourceName = masterDataSourceName;
        this.masterDataSource = masterDataSource;
        this.slaveDataSources = slaveDataSources;
        this.masterSlaveLoadBalanceStrategy = masterSlaveLoadBalanceStrategy;
    }
    
    private static Collection<DataSource> getAllDataSources(final DataSource masterDataSource, final Collection<DataSource> slaveDataSources) {
        Collection<DataSource> result = new LinkedList<>(slaveDataSources);
        result.add(masterDataSource);
        return result;
    }
    
    /**
     * Get map of all actual data source name and all actual data sources.
     *
     * @return map of all actual data source name and all actual data sources
     */
    public Map<String, DataSource> getAllDataSources() {
        Map<String, DataSource> result = new HashMap<>(slaveDataSources.size() + 1, 1);
        result.put(masterDataSourceName, masterDataSource);
        result.putAll(slaveDataSources);
        return result;
    }
    
    /**
     * Get data source name from master-slave data source.
     *
     * @param dataSourceName data source name
     * @param sqlType SQL type
     * @return data source name from master-slave data source
     */
    public static String getDataSourceName(final String dataSourceName, final SQLType sqlType) {
        return isMasterRoute(sqlType) ? getMasterDataSourceName(dataSourceName) : getSlaveDataSourceName(dataSourceName);
    }
    
    private static boolean isMasterRoute(final SQLType sqlType) {
        return SQLType.DQL != sqlType || DML_FLAG.get() || HintManagerHolder.isMasterRouteOnly();
    }
    
    private static String getMasterDataSourceName(final String dataSourceName) {
        return Joiner.on("-").join(dataSourceName, "MASTER");
    }
    
    private static String getSlaveDataSourceName(final String dataSourceName) {
        return Joiner.on("-").join(dataSourceName, "SLAVE");
    }
    
    /**
     * reset DML flag.
     */
    public static void resetDMLFlag() {
        DML_FLAG.remove();
    }
    
    /**
     * Get data source from master-slave data source.
     *
     * @param sqlType SQL type
     * @return data source from master-slave data source
     */
    public NamedDataSource getDataSource(final SQLType sqlType) {
        if (isMasterRoute(sqlType)) {
            DML_FLAG.set(true);
            return new NamedDataSource(masterDataSourceName, masterDataSource);
        }
        String selectedSourceName = masterSlaveLoadBalanceStrategy.getDataSource(name, masterDataSourceName, new ArrayList<>(slaveDataSources.keySet()));
        DataSource result = selectedSourceName.equals(masterDataSourceName) ? masterDataSource : slaveDataSources.get(selectedSourceName);
        Preconditions.checkNotNull(result, "");
        return new NamedDataSource(selectedSourceName, result);
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        return new MasterSlaveConnection(this);
    }
}
