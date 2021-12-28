/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.decorator;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.config.datasource.pool.decorator.DataSourcePoolParameterDecorator;

import java.util.Map;

/**
 * Data source pool parameter decorator for Hikari.
 */
public final class HikariParameterDecorator implements DataSourcePoolParameterDecorator<HikariDataSource> {
    
    @Override
    public HikariDataSource decorate(final HikariDataSource dataSource) {
        Map<String, String> props = new ConnectionURLParser(dataSource.getJdbcUrl()).getProperties();
        setProperty(dataSource, props, "useServerPrepStmts", Boolean.TRUE.toString());
        setProperty(dataSource, props, "cachePrepStmts", Boolean.TRUE.toString());
        setProperty(dataSource, props, "prepStmtCacheSize", "200000");
        setProperty(dataSource, props, "prepStmtCacheSqlLimit", "2048");
        setProperty(dataSource, props, "useLocalSessionState", Boolean.TRUE.toString());
        setProperty(dataSource, props, "rewriteBatchedStatements", Boolean.TRUE.toString());
        setProperty(dataSource, props, "cacheResultSetMetadata", Boolean.FALSE.toString());
        setProperty(dataSource, props, "cacheServerConfiguration", Boolean.TRUE.toString());
        setProperty(dataSource, props, "elideSetAutoCommits", Boolean.TRUE.toString());
        setProperty(dataSource, props, "maintainTimeStats", Boolean.FALSE.toString());
        setProperty(dataSource, props, "netTimeoutForStreamingResults", "0");
        setProperty(dataSource, props, "tinyInt1isBit", Boolean.FALSE.toString());
        setProperty(dataSource, props, "useSSL", Boolean.FALSE.toString());
        setProperty(dataSource, props, "serverTimezone", "UTC");
        HikariDataSource result = new HikariDataSource(dataSource);
        dataSource.close();
        return result;
    }
    
    private void setProperty(final HikariConfig config, final Map<String, String> props, final String key, final String value) {
        if (props.isEmpty() || !props.containsKey(key)) {
            config.getDataSourceProperties().setProperty(key, value);
        }
    }
    
    @Override
    public String getType() {
        return HikariDataSource.class.getCanonicalName();
    }
}
