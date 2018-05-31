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
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Sharding rule registry.
 *
 * @author zhangliang
 * @author zhangyonglun
 * @author panjuan
 */
@Getter
public final class RuleRegistry {

    private static final int MAXIMUM_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2 + 1;

    private static final RuleRegistry INSTANCE = new RuleRegistry();

    private final Map<String, DataSource> dataSourceMap;

    private final ShardingRule shardingRule;

    private final MasterSlaveRule masterSlaveRule;

    private final ShardingMetaData shardingMetaData;

    private final boolean isOnlyMasterSlave;

    private final boolean showSQL;

    private final HiKariCPParameter cpParameter;

    private RuleRegistry() {
        YamlProxyConfiguration yamlProxyConfiguration;
        try (FileInputStream fileInputStream = new FileInputStream(getClass().getResource("/conf/config-hikaricp.yaml").getFile());
             InputStreamReader in = new InputStreamReader(fileInputStream, "UTF-8")
        ) {
            yamlProxyConfiguration = YamlProxyConfiguration.unmarshal(new File(getClass().getResource("/conf/config.yaml").getFile()));
            HiKariCPParameter parameter = new Yaml(new Constructor(HiKariCPParameter.class)).loadAs(in, HiKariCPParameter.class);
            cpParameter = parameter == null ? new HiKariCPParameter() : parameter;
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
        config.setDriverClassName(cpParameter.getDriverClass());
        config.setJdbcUrl(dataSourceParameter.getUrl());
        config.setUsername(dataSourceParameter.getUsername());
        config.setPassword(dataSourceParameter.getPassword());
        config.setAutoCommit(cpParameter.getIsAutoCommit());
        config.setConnectionTimeout(cpParameter.getConnectionTimeout());
        config.setIdleTimeout(cpParameter.getIdleTimeout());
        config.setMaxLifetime(cpParameter.getMaxLifetime());
        config.setMaximumPoolSize(cpParameter.getMaxPoolSize());
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
