/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingsphere.core.jdbc.core.datasource;

import io.shardingsphere.core.api.ConfigMapContext;
import io.shardingsphere.core.constant.ShardingProperties;
import io.shardingsphere.core.constant.ShardingPropertiesConstant;
import io.shardingsphere.core.exception.ShardingJdbcException;
import io.shardingsphere.core.executor.ExecutorEngine;
import io.shardingsphere.core.jdbc.adapter.AbstractDataSourceAdapter;
import io.shardingsphere.core.jdbc.core.ShardingContext;
import io.shardingsphere.core.jdbc.core.connection.ShardingConnection;
import io.shardingsphere.core.jdbc.metadata.JDBCShardingMetaData;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.api.ConfigMapContext;
import io.shardingsphere.core.constant.ShardingProperties;
import io.shardingsphere.core.constant.ShardingPropertiesConstant;
import io.shardingsphere.core.exception.ShardingJdbcException;
import io.shardingsphere.core.executor.ExecutorEngine;
import io.shardingsphere.core.jdbc.adapter.AbstractDataSourceAdapter;
import io.shardingsphere.core.jdbc.metadata.JDBCShardingMetaData;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.Getter;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Database that support sharding.
 * 
 * @author zhangliang
 */
public class ShardingDataSource extends AbstractDataSourceAdapter implements AutoCloseable {
    
    @Getter
    private ShardingProperties shardingProperties;
    
    private ExecutorEngine executorEngine;
    
    private ShardingContext shardingContext;
    
    public ShardingDataSource(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule) throws SQLException {
        this(dataSourceMap, shardingRule, new ConcurrentHashMap<String, Object>(), new Properties());
    }
    
    public ShardingDataSource(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule, final Map<String, Object> configMap, final Properties props) throws SQLException {
        super(dataSourceMap.values());
        if (!configMap.isEmpty()) {
            ConfigMapContext.getInstance().getShardingConfig().putAll(configMap);
        }
        shardingProperties = new ShardingProperties(null == props ? new Properties() : props);
        int executorSize = shardingProperties.getValue(ShardingPropertiesConstant.EXECUTOR_SIZE);
        executorEngine = new ExecutorEngine(executorSize);
        ShardingMetaData shardingMetaData = new JDBCShardingMetaData(dataSourceMap, shardingRule, getDatabaseType());
        shardingMetaData.init(shardingRule);
        boolean showSQL = shardingProperties.getValue(ShardingPropertiesConstant.SQL_SHOW);
        shardingContext = new ShardingContext(dataSourceMap, shardingRule, getDatabaseType(), executorEngine, shardingMetaData, showSQL);
    }
    
    /**
     * Renew sharding data source.
     *
     * @param newDataSourceMap new data source map
     * @param newShardingRule new sharding rule
     * @param newProps new sharding properties
     */
    public void renew(final Map<String, DataSource> newDataSourceMap, final ShardingRule newShardingRule, final Properties newProps) {
        ShardingProperties newShardingProperties = new ShardingProperties(null == newProps ? new Properties() : newProps);
        int originalExecutorSize = shardingProperties.getValue(ShardingPropertiesConstant.EXECUTOR_SIZE);
        int newExecutorSize = newShardingProperties.getValue(ShardingPropertiesConstant.EXECUTOR_SIZE);
        if (originalExecutorSize != newExecutorSize) {
            ExecutorEngine originalExecutorEngine = executorEngine;
            executorEngine = new ExecutorEngine(newExecutorSize);
            originalExecutorEngine.close();
        }
        boolean newShowSQL = newShardingProperties.getValue(ShardingPropertiesConstant.SQL_SHOW);
        ShardingMetaData shardingMetaData = new JDBCShardingMetaData(newDataSourceMap, newShardingRule, getDatabaseType());
        try {
            shardingMetaData.init(newShardingRule);
        } catch (SQLException ex) {
            throw new ShardingJdbcException(ex);
        }
        
        shardingProperties = newShardingProperties;
        shardingContext = new ShardingContext(newDataSourceMap, newShardingRule, getDatabaseType(), executorEngine, shardingMetaData, newShowSQL);
    }
    
    @Override
    public ShardingConnection getConnection() {
        return new ShardingConnection(shardingContext);
    }
    
    @Override
    public void close() {
        executorEngine.close();
    }
}
