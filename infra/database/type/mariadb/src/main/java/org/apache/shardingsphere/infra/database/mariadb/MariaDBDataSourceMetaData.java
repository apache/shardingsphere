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

package org.apache.shardingsphere.infra.database.mariadb;

import lombok.Getter;
import org.apache.shardingsphere.infra.database.spi.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.core.url.JdbcUrl;
import org.apache.shardingsphere.infra.database.core.url.StandardJdbcUrlParser;

import java.util.Properties;

/**
 * Data source meta data for MariaDB.
 */
@Getter
public final class MariaDBDataSourceMetaData implements DataSourceMetaData {
    
    private static final int DEFAULT_PORT = 3306;
    
    private final String hostname;
    
    private final int port;
    
    private final String catalog;
    
    private final String schema;
    
    private final Properties queryProperties;
    
    private final Properties defaultQueryProperties = new Properties();
    
    public MariaDBDataSourceMetaData(final String url) {
        JdbcUrl jdbcUrl = new StandardJdbcUrlParser().parse(url);
        hostname = jdbcUrl.getHostname();
        port = -1 == jdbcUrl.getPort() ? DEFAULT_PORT : jdbcUrl.getPort();
        catalog = jdbcUrl.getDatabase();
        schema = null;
        queryProperties = jdbcUrl.getQueryProperties();
        buildDefaultQueryProperties();
    }
    
    private void buildDefaultQueryProperties() {
        defaultQueryProperties.setProperty("useServerPrepStmts", Boolean.TRUE.toString());
        defaultQueryProperties.setProperty("cachePrepStmts", Boolean.TRUE.toString());
        defaultQueryProperties.setProperty("prepStmtCacheSize", "200000");
        defaultQueryProperties.setProperty("prepStmtCacheSqlLimit", "2048");
        defaultQueryProperties.setProperty("useLocalSessionState", Boolean.TRUE.toString());
        defaultQueryProperties.setProperty("rewriteBatchedStatements", Boolean.TRUE.toString());
        defaultQueryProperties.setProperty("cacheResultSetMetadata", Boolean.FALSE.toString());
        defaultQueryProperties.setProperty("cacheServerConfiguration", Boolean.TRUE.toString());
        defaultQueryProperties.setProperty("elideSetAutoCommits", Boolean.TRUE.toString());
        defaultQueryProperties.setProperty("maintainTimeStats", Boolean.FALSE.toString());
        defaultQueryProperties.setProperty("netTimeoutForStreamingResults", "0");
        defaultQueryProperties.setProperty("tinyInt1isBit", Boolean.FALSE.toString());
        defaultQueryProperties.setProperty("useSSL", Boolean.FALSE.toString());
        defaultQueryProperties.setProperty("serverTimezone", "UTC");
    }
}
