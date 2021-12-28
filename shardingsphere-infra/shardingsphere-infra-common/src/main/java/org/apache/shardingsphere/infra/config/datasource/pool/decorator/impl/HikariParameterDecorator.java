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

package org.apache.shardingsphere.infra.config.datasource.pool.decorator.impl;

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
        Map<String, String> urlProps = new ConnectionURLParser(dataSource.getJdbcUrl()).getQueryMap();
        setProperty(dataSource, urlProps, "useServerPrepStmts", Boolean.TRUE.toString());
        setProperty(dataSource, urlProps, "cachePrepStmts", Boolean.TRUE.toString());
        setProperty(dataSource, urlProps, "prepStmtCacheSize", "200000");
        setProperty(dataSource, urlProps, "prepStmtCacheSqlLimit", "2048");
        setProperty(dataSource, urlProps, "useLocalSessionState", Boolean.TRUE.toString());
        setProperty(dataSource, urlProps, "rewriteBatchedStatements", Boolean.TRUE.toString());
        setProperty(dataSource, urlProps, "cacheResultSetMetadata", Boolean.FALSE.toString());
        setProperty(dataSource, urlProps, "cacheServerConfiguration", Boolean.TRUE.toString());
        setProperty(dataSource, urlProps, "elideSetAutoCommits", Boolean.TRUE.toString());
        setProperty(dataSource, urlProps, "maintainTimeStats", Boolean.FALSE.toString());
        setProperty(dataSource, urlProps, "netTimeoutForStreamingResults", "0");
        setProperty(dataSource, urlProps, "tinyInt1isBit", Boolean.FALSE.toString());
        setProperty(dataSource, urlProps, "useSSL", Boolean.FALSE.toString());
        setProperty(dataSource, urlProps, "serverTimezone", "UTC");
        HikariDataSource result = new HikariDataSource(dataSource);
        dataSource.close();
        return result;
    }
    
    private void setProperty(final HikariConfig config, final Map<String, String> urlProps, final String key, final String value) {
        if (urlProps.isEmpty() || !urlProps.containsKey(key)) {
            config.getDataSourceProperties().setProperty(key, value);
        }
    }
    
    @Override
    public String getType() {
        return HikariDataSource.class.getCanonicalName();
    }
}
