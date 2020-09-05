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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.config.datasource.JDBCParameterDecorator;

/**
 * JDBC parameter decorator for HikariCP.
 */
public final class HikariJDBCParameterDecorator implements JDBCParameterDecorator<HikariDataSource> {
    
    @Override
    public void decorate(final HikariDataSource dataSource) {
        dataSource.getDataSourceProperties().setProperty("useServerPrepStmts", Boolean.TRUE.toString());
        dataSource.getDataSourceProperties().setProperty("cachePrepStmts", Boolean.TRUE.toString());
        dataSource.getDataSourceProperties().setProperty("prepStmtCacheSize", "250");
        dataSource.getDataSourceProperties().setProperty("prepStmtCacheSqlLimit", "2048");
        dataSource.getDataSourceProperties().setProperty("useLocalSessionState", Boolean.TRUE.toString());
        dataSource.getDataSourceProperties().setProperty("rewriteBatchedStatements", Boolean.TRUE.toString());
        dataSource.getDataSourceProperties().setProperty("cacheResultSetMetadata", Boolean.FALSE.toString());
        dataSource.getDataSourceProperties().setProperty("cacheServerConfiguration", Boolean.TRUE.toString());
        dataSource.getDataSourceProperties().setProperty("elideSetAutoCommits", Boolean.TRUE.toString());
        dataSource.getDataSourceProperties().setProperty("maintainTimeStats", Boolean.FALSE.toString());
        dataSource.getDataSourceProperties().setProperty("netTimeoutForStreamingResults", "0");
        dataSource.getDataSourceProperties().setProperty("tinyInt1isBit", Boolean.FALSE.toString());
    }
    
    @Override
    public Class<HikariDataSource> getType() {
        return HikariDataSource.class;
    }
}
