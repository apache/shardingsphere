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

package io.shardingsphere.shardingjdbc.jdbc.core.datasource;

import com.google.common.base.Preconditions;
import io.shardingsphere.api.ConfigMapContext;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.shardingjdbc.jdbc.adapter.AbstractDataSourceAdapter;
import io.shardingsphere.shardingjdbc.jdbc.core.ShardingContext;
import io.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
import io.shardingsphere.shardingjdbc.transaction.TransactionTypeHolder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class ShardingDataSource extends AbstractDataSourceAdapter {
    
    private final ShardingContext shardingContext;
    
    public ShardingDataSource(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule) throws SQLException {
        this(dataSourceMap, shardingRule, new ConcurrentHashMap<String, Object>(), new Properties());
    }
    
    public ShardingDataSource(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule, final Map<String, Object> configMap, final Properties props) throws SQLException {
        super(dataSourceMap);
        checkDataSourceType(dataSourceMap);
        if (!configMap.isEmpty()) {
            ConfigMapContext.getInstance().getConfigMap().putAll(configMap);
        }
        shardingContext = new ShardingContext(getDataSourceMap(), shardingRule, getDatabaseType(), props);
    }
    
    private void checkDataSourceType(final Map<String, DataSource> dataSourceMap) {
        for (DataSource each : dataSourceMap.values()) {
            Preconditions.checkArgument(!(each instanceof MasterSlaveDataSource), "Initialized data sources can not be master-slave data sources.");
        }
    }
    
    @Override
    public final ShardingConnection getConnection() {
        if (TransactionType.XA == TransactionTypeHolder.get()) {
            if (null == getXaDataSourceMap() || getXaDataSourceMap().isEmpty()) {
                log.warn("XA transaction resource have not load, using Local transaction instead!");
            } else {
                return new ShardingConnection(getXaDataSourceMap(), shardingContext, TransactionType.XA);
            }
        }
        return new ShardingConnection(getDataSourceMap(), shardingContext);
    }
    
    @Override
    public final void close() {
        super.close();
        shardingContext.close();
    }
}
