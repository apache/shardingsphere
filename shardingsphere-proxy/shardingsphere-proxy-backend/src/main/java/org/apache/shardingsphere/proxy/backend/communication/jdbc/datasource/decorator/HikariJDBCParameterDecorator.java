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
import org.apache.shardingsphere.infra.config.datasource.JDBCParameterDecorator;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.ConnectionUrlParser;

import java.util.Map;

/**
 * JDBC parameter decorator for HikariCP.
 */
public final class HikariJDBCParameterDecorator implements JDBCParameterDecorator<HikariDataSource> {
    
    @Override
    public HikariDataSource decorate(final HikariDataSource dataSource) {
        Map<String, String> urlProps = new ConnectionUrlParser(dataSource.getJdbcUrl()).getQueryMap();
        addJDBCProperty(dataSource, urlProps, "useServerPrepStmts", Boolean.TRUE.toString());
        addJDBCProperty(dataSource, urlProps, "useServerPrepStmts", Boolean.TRUE.toString());
        addJDBCProperty(dataSource, urlProps, "cachePrepStmts", Boolean.TRUE.toString());
        addJDBCProperty(dataSource, urlProps, "prepStmtCacheSize", "200000");
        addJDBCProperty(dataSource, urlProps, "prepStmtCacheSqlLimit", "2048");
        addJDBCProperty(dataSource, urlProps, "useLocalSessionState", Boolean.TRUE.toString());
        addJDBCProperty(dataSource, urlProps, "rewriteBatchedStatements", Boolean.TRUE.toString());
        addJDBCProperty(dataSource, urlProps, "cacheResultSetMetadata", Boolean.FALSE.toString());
        addJDBCProperty(dataSource, urlProps, "cacheServerConfiguration", Boolean.TRUE.toString());
        addJDBCProperty(dataSource, urlProps, "elideSetAutoCommits", Boolean.TRUE.toString());
        addJDBCProperty(dataSource, urlProps, "maintainTimeStats", Boolean.FALSE.toString());
        addJDBCProperty(dataSource, urlProps, "netTimeoutForStreamingResults", "0");
        addJDBCProperty(dataSource, urlProps, "tinyInt1isBit", Boolean.FALSE.toString());
        addJDBCProperty(dataSource, urlProps, "useSSL", Boolean.FALSE.toString());
        addJDBCProperty(dataSource, urlProps, "serverTimezone", "UTC");
        HikariDataSource result = new HikariDataSource(dataSource);
        dataSource.close();
        return result;
    }
    
    private void addJDBCProperty(final HikariConfig config, final Map<String, String> urlProps, final String key, final String value) {
        if (urlProps.isEmpty() || !urlProps.containsKey(key)) {
            config.addDataSourceProperty(key, value);
        }
    }
    
    @Override
    public Class<HikariDataSource> getType() {
        return HikariDataSource.class;
    }
}
