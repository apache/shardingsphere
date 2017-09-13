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

import com.dangdang.ddframe.rdb.sharding.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constant.ShardingProperties;
import com.dangdang.ddframe.rdb.sharding.constant.ShardingPropertiesConstant;
import com.dangdang.ddframe.rdb.sharding.executor.ExecutorEngine;
import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractDataSourceAdapter;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.ShardingContext;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.connection.ShardingConnection;
import com.google.common.base.Preconditions;

import java.sql.SQLException;
import java.util.Properties;

/**
 * Database that support sharding.
 * 
 * @author zhangliang
 */
public class ShardingDataSource extends AbstractDataSourceAdapter implements AutoCloseable {
    
    private ShardingProperties shardingProperties;
    
    private ExecutorEngine executorEngine;
    
    private ShardingContext shardingContext;
    
    public ShardingDataSource(final ShardingRule shardingRule) throws SQLException {
        this(shardingRule, new Properties());
    }
    
    public ShardingDataSource(final ShardingRule shardingRule, final Properties props) throws SQLException {
        super(shardingRule.getDataSourceMap().values());
        shardingProperties = new ShardingProperties(null == props ? new Properties() : props);
        int executorSize = shardingProperties.getValue(ShardingPropertiesConstant.EXECUTOR_SIZE);
        executorEngine = new ExecutorEngine(executorSize);
        boolean showSQL = shardingProperties.getValue(ShardingPropertiesConstant.SQL_SHOW);
        shardingContext = new ShardingContext(shardingRule, getDatabaseType(), executorEngine, showSQL);
    }
    
    /**
     * Renew sharding data source.
     *
     * @param newShardingRule new sharding rule
     * @param newProps new sharding properties
     * @throws SQLException SQL exception
     */
    public void renew(final ShardingRule newShardingRule, final Properties newProps) throws SQLException {
        Preconditions.checkState(getDatabaseType() == getDatabaseType(newShardingRule.getDataSourceMap().values()), "Cannot change database type dynamically.");
        ShardingProperties newShardingProperties = new ShardingProperties(null == newProps ? new Properties() : newProps);
        int originalExecutorSize = shardingProperties.getValue(ShardingPropertiesConstant.EXECUTOR_SIZE);
        int newExecutorSize = newShardingProperties.getValue(ShardingPropertiesConstant.EXECUTOR_SIZE);
        if (originalExecutorSize != newExecutorSize) {
            executorEngine.close();
            executorEngine = new ExecutorEngine(newExecutorSize);
        }
        boolean newShowSQL = newShardingProperties.getValue(ShardingPropertiesConstant.SQL_SHOW);
        shardingProperties = newShardingProperties;
        shardingContext = new ShardingContext(newShardingRule, getDatabaseType(), executorEngine, newShowSQL);
    }
    
    @Override
    public ShardingConnection getConnection() throws SQLException {
        return new ShardingConnection(shardingContext);
    }
    
    @Override
    public void close() {
        executorEngine.close();
    }
}
