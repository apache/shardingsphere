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

package org.apache.shardingsphere.infra.database.postgresql;

import lombok.Getter;
import org.apache.shardingsphere.infra.database.spi.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.core.url.JdbcUrl;
import org.apache.shardingsphere.infra.database.core.url.StandardJdbcUrlParser;

import java.util.Properties;

/**
 * Data source meta data for PostgreSQL.
 */
@Getter
public final class PostgreSQLDataSourceMetaData implements DataSourceMetaData {
    
    private static final int DEFAULT_PORT = 5432;
    
    private final String hostname;
    
    private final int port;
    
    private final String catalog;
    
    private final String schema;
    
    private final Properties queryProperties;
    
    public PostgreSQLDataSourceMetaData(final String url) {
        JdbcUrl jdbcUrl = new StandardJdbcUrlParser().parse(url);
        hostname = jdbcUrl.getHostname();
        port = -1 == jdbcUrl.getPort() ? DEFAULT_PORT : jdbcUrl.getPort();
        catalog = jdbcUrl.getDatabase();
        schema = null;
        queryProperties = jdbcUrl.getQueryProperties();
    }
    
    @Override
    public Properties getDefaultQueryProperties() {
        return new Properties();
    }
}
