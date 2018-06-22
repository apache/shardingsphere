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
import io.shardingsphere.core.rule.DataSourceParameter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Create default proxy raw datasource using {@code HikariDataSource}.
 *
 * @author zhaojun
 */
public class DefaultProxyRawDataSource extends ProxyRawDataSource {
    
    public DefaultProxyRawDataSource(final Map<String, DataSourceParameter> dataSourceParameters) {
        super(dataSourceParameters);
    }
    
    @Override
    protected Map<String, DataSource> buildInternal(final String key, final DataSourceParameter dataSourceParameter) {
        final Map<String, DataSource> result = new HashMap<>(128, 1);
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
        result.put(key, new HikariDataSource(config));
        return result;
    }
}
