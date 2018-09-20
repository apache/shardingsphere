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

package io.shardingsphere.core.jdbc.core.datasource;

import com.google.common.base.Preconditions;
import io.shardingsphere.core.api.ConfigMapContext;
import io.shardingsphere.core.constant.properties.ShardingProperties;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.core.executor.ShardingExecuteEngine;
import io.shardingsphere.core.jdbc.adapter.AbstractDataSourceAdapter;
import io.shardingsphere.core.jdbc.core.ShardingContext;
import io.shardingsphere.core.jdbc.core.connection.ShardingConnection;
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
 * @author zhaojun
 * @author panjuan
 */
@Getter
public class ShardingDataSource extends AbstractDataSourceAdapter implements AutoCloseable {
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final ShardingContext shardingContext;
    
    private final ShardingProperties shardingProperties;
    
    public ShardingDataSource(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule) throws SQLException {
        this(dataSourceMap, shardingRule, new ConcurrentHashMap<String, Object>(), new Properties());
    }
    
    public ShardingDataSource(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule, final Map<String, Object> configMap, final Properties props) throws SQLException {
        super(dataSourceMap.values());
        checkDataSourceType(dataSourceMap);
        if (!configMap.isEmpty()) {
            ConfigMapContext.getInstance().getShardingConfig().putAll(configMap);
        }
        this.dataSourceMap = dataSourceMap;
        this.shardingProperties = new ShardingProperties(null == props ? new Properties() : props);
        this.shardingContext = getShardingContext(shardingRule);
    }
    
    public ShardingDataSource(final Map<String, DataSource> dataSourceMap, final ShardingContext shardingContext, final ShardingProperties shardingProperties) {
        super(shardingContext.getDatabaseType());
        this.dataSourceMap = dataSourceMap;
        this.shardingContext = shardingContext;
        this.shardingProperties = shardingProperties;
    }
    
    private void checkDataSourceType(final Map<String, DataSource> dataSourceMap) {
        for (DataSource each : dataSourceMap.values()) {
            Preconditions.checkArgument(!(each instanceof MasterSlaveDataSource), "Initialized data sources can not be master-slave data sources.");
        }
    }
    
    private ShardingContext getShardingContext(final ShardingRule shardingRule) throws SQLException {
        int executorSize = shardingProperties.getValue(ShardingPropertiesConstant.EXECUTOR_SIZE);
        int maxConnectionsSizePerQuery = shardingProperties.getValue(ShardingPropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY);
        boolean showSQL = shardingProperties.getValue(ShardingPropertiesConstant.SQL_SHOW);
        return new ShardingContext(dataSourceMap, shardingRule, getDatabaseType(), new ShardingExecuteEngine(executorSize), maxConnectionsSizePerQuery, showSQL);
    }
    
    @Override
    public final ShardingConnection getConnection() {
        return new ShardingConnection(dataSourceMap, shardingContext);
    }
    
    @Override
    public final void close() {
        closeOriginalDataSources();
        shardingContext.close();
    }
    
    private void closeOriginalDataSources() {
        for (DataSource each : dataSourceMap.values()) {
            try {
                each.getClass().getDeclaredMethod("close").invoke(each);
            } catch (final ReflectiveOperationException ignored) {
            }
        }
    }
}
