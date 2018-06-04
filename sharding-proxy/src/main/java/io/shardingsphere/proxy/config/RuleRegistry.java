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

package io.shardingsphere.proxy.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.shardingsphere.core.constant.ShardingProperties;
import io.shardingsphere.core.constant.ShardingPropertiesConstant;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.rule.MasterSlaveRule;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.yaml.proxy.YamlProxyConfiguration;
import io.shardingsphere.core.yaml.sharding.DataSourceParameter;
import io.shardingsphere.proxy.metadata.ProxyShardingMetaData;
import lombok.Getter;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sharding rule registry.
 *
 * @author zhangliang
 * @author zhangyonglun
 * @author panjuan
 */
@Getter
public final class RuleRegistry {
    
    private static final int MAX_EXECUTOR_THREADS = Runtime.getRuntime().availableProcessors() * 50;
    
    private static final RuleRegistry INSTANCE = new RuleRegistry();
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final ShardingRule shardingRule;
    
    private final MasterSlaveRule masterSlaveRule;
    
    private final ShardingMetaData shardingMetaData;
    
    private final boolean isOnlyMasterSlave;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(MAX_EXECUTOR_THREADS);
    
    private final String proxyMode;
    
    private final boolean showSQL;
    
    private RuleRegistry() {
        YamlProxyConfiguration yamlProxyConfiguration;
        try {
            yamlProxyConfiguration = YamlProxyConfiguration.unmarshal(new File(getClass().getResource("/conf/config.yaml").getFile()));
        } catch (final IOException ex) {
            throw new ShardingException(ex);
        }
        dataSourceMap = new HashMap<>(128, 1);
        Map<String, DataSourceParameter> dataSourceParameters = yamlProxyConfiguration.getDataSources();
        for (Map.Entry<String, DataSourceParameter> entry : dataSourceParameters.entrySet()) {
            dataSourceMap.put(entry.getKey(), getDataSource(entry.getValue()));
        }
        shardingRule = yamlProxyConfiguration.obtainShardingRule(Collections.<String>emptyList());
        masterSlaveRule = yamlProxyConfiguration.obtainMasterSlaveRule();
        isOnlyMasterSlave = shardingRule.getTableRules().isEmpty() && !masterSlaveRule.getMasterDataSourceName().isEmpty();
        Properties properties = yamlProxyConfiguration.getShardingRule().getProps();
        ShardingProperties shardingProperties = new ShardingProperties(null == properties ? new Properties() : properties);
        proxyMode = shardingProperties.getValue(ShardingPropertiesConstant.PROXY_MODE);
        showSQL = shardingProperties.getValue(ShardingPropertiesConstant.SQL_SHOW);
        try {
            shardingMetaData = new ProxyShardingMetaData(dataSourceMap);
            if (!isOnlyMasterSlave) {
                shardingMetaData.init(shardingRule);
            }
        } catch (final SQLException ex) {
            throw new ShardingException(ex);
        }
    }
    
    private DataSource getDataSource(final DataSourceParameter dataSourceParameter) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setJdbcUrl(dataSourceParameter.getUrl());
        config.setUsername(dataSourceParameter.getUsername());
        config.setPassword(dataSourceParameter.getPassword());
        config.setAutoCommit(dataSourceParameter.getAutoCommit());
        config.setConnectionTimeout(dataSourceParameter.getConnectionTimeout());
        config.setIdleTimeout(dataSourceParameter.getIdleTimeout());
        config.setMaxLifetime(dataSourceParameter.getMaxLifetime());
        config.setMaximumPoolSize(dataSourceParameter.getMaximumPoolSize());
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
