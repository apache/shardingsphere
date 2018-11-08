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

import io.shardingsphere.api.ConfigMapContext;
import io.shardingsphere.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.core.constant.properties.ShardingProperties;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.rule.MasterSlaveRule;
import io.shardingsphere.shardingjdbc.jdbc.adapter.AbstractDataSourceAdapter;
import io.shardingsphere.shardingjdbc.jdbc.core.connection.MasterSlaveConnection;
import io.shardingsphere.shardingjdbc.transaction.TransactionTypeHolder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Database that support master-slave.
 *
 * @author zhangliang
 * @author panjuan
 * @author zhaojun
 */
@Getter
@Slf4j
public class MasterSlaveDataSource extends AbstractDataSourceAdapter {
    
    private final MasterSlaveRule masterSlaveRule;
    
    private final ShardingProperties shardingProperties;
    
    public MasterSlaveDataSource(final Map<String, DataSource> dataSourceMap, final MasterSlaveRuleConfiguration masterSlaveRuleConfig,
                                 final Map<String, Object> configMap, final Properties props) throws SQLException {
        super(dataSourceMap);
        if (!configMap.isEmpty()) {
            ConfigMapContext.getInstance().getConfigMap().putAll(configMap);
        }
        this.masterSlaveRule = new MasterSlaveRule(masterSlaveRuleConfig);
        shardingProperties = new ShardingProperties(null == props ? new Properties() : props);
    }
    
    public MasterSlaveDataSource(final Map<String, DataSource> dataSourceMap, final MasterSlaveRule masterSlaveRule,
                                 final Map<String, Object> configMap, final Properties props) throws SQLException {
        super(dataSourceMap);
        if (!configMap.isEmpty()) {
            ConfigMapContext.getInstance().getConfigMap().putAll(configMap);
        }
        this.masterSlaveRule = masterSlaveRule;
        shardingProperties = new ShardingProperties(null == props ? new Properties() : props);
    }
    
    @Override
    public final MasterSlaveConnection getConnection() {
        if (TransactionType.XA == TransactionTypeHolder.get()) {
            if (null == getXaDataSourceMap() || getXaDataSourceMap().isEmpty()) {
                log.warn("XA transaction resource have not load, using Local transaction instead!");
            } else {
                return new MasterSlaveConnection(this, getXaDataSourceMap(), TransactionType.XA);
            }
        }
        return new MasterSlaveConnection(this, getDataSourceMap());
    }
}
