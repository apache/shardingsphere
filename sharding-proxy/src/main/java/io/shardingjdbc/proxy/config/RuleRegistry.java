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

package io.shardingjdbc.proxy.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.shardingjdbc.core.exception.ShardingJdbcException;
import io.shardingjdbc.core.metadata.ShardingMetaData;
import io.shardingjdbc.core.rule.MasterSlaveRule;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.yaml.proxy.YamlProxyConfiguration;
import io.shardingjdbc.core.yaml.sharding.DataSourceParameter;
import io.shardingjdbc.proxy.metadata.ProxyShardingMetaData;
import lombok.Getter;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Sharding rule registry.
 *
 * @author zhangliang
 * @author zhangyonglun
 * @author panjuan
 */
@Getter
public final class RuleRegistry {
    
    private static final RuleRegistry INSTANCE = new RuleRegistry();
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final ShardingRule shardingRule;
    
    private final MasterSlaveRule masterSlaveRule;
    
    private final ShardingMetaData shardingMetaData;
    
    private final boolean isOnlyMasterSlave;
    
    private RuleRegistry() {
        YamlProxyConfiguration yamlProxyConfiguration;
        try {
            yamlProxyConfiguration = YamlProxyConfiguration.unmarshal(new File(getClass().getResource("/conf/config.yaml").getFile()));
        } catch (final IOException ex) {
            throw new ShardingJdbcException(ex);
        }
        dataSourceMap = new HashMap<>(128, 1);
        Map<String, DataSourceParameter> dataSourceParameters = yamlProxyConfiguration.getDataSources();
        for (String each : dataSourceParameters.keySet()) {
            dataSourceMap.put(each, getDataSource(dataSourceParameters.get(each)));
        }
        shardingRule = yamlProxyConfiguration.obtainShardingRule(Collections.<String>emptyList());
        masterSlaveRule = yamlProxyConfiguration.obtainMasterSlaveRule();
        isOnlyMasterSlave = shardingRule.getTableRules().isEmpty() && !masterSlaveRule.getMasterDataSourceName().isEmpty();
        try {
            shardingMetaData = new ProxyShardingMetaData(dataSourceMap);
            if (!isOnlyMasterSlave) {
                shardingMetaData.init(shardingRule);
            }
        } catch (final SQLException ex) {
            throw new ShardingJdbcException(ex);
        }
    }
    
    private DataSource getDataSource(final DataSourceParameter dataSourceParameter) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setJdbcUrl(dataSourceParameter.getUrl());
        config.setUsername(dataSourceParameter.getUsername());
        config.setPassword(dataSourceParameter.getPassword());
        config.setAutoCommit(true);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(60000);
        config.setMaxLifetime(1800000);
        config.setMaximumPoolSize(100);
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("cachePrepStmts", "true");
        return new HikariDataSource(config);
    }
    
    /**
     * Get instance of sharding rule registry.
     *
     * @return instance of sharding rule registry
     */
    public static RuleRegistry getInstance() {
        return INSTANCE;
    }
}
