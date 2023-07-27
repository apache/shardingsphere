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

import org.apache.shardingsphere.infra.database.core.connector.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.core.connector.DataSourceMetaDataBuilder;
import org.apache.shardingsphere.infra.database.core.connector.JdbcUrl;
import org.apache.shardingsphere.infra.database.core.connector.StandardDataSourceMetaData;
import org.apache.shardingsphere.infra.database.core.connector.StandardJdbcUrlParser;

import java.util.Properties;

/**
 * Data source meta data builder of PostgreSQL.
 */
public final class PostgreSQLDataSourceMetaDataBuilder implements DataSourceMetaDataBuilder {
    
    private static final int DEFAULT_PORT = 5432;
    
    @Override
    public DataSourceMetaData build(final String url, final String username, final String catalog) {
        JdbcUrl jdbcUrl = new StandardJdbcUrlParser().parse(url);
        return new StandardDataSourceMetaData(jdbcUrl.getHostname(), jdbcUrl.getPort(DEFAULT_PORT), jdbcUrl.getDatabase(), null, jdbcUrl.getQueryProperties(), new Properties());
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
