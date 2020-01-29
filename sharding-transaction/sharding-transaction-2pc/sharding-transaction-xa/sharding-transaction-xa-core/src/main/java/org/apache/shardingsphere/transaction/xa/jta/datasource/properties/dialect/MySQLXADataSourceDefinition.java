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

package org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect;

import com.google.common.base.Optional;
import org.apache.shardingsphere.underlying.common.config.DatabaseAccessConfiguration;
import org.apache.shardingsphere.transaction.xa.jta.datasource.properties.XADataSourceDefinition;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

/**
 * XA data source definition for MySQL.
 *
 * @author zhaojun
 */
public final class MySQLXADataSourceDefinition implements XADataSourceDefinition {
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
    
    @Override
    public Collection<String> getXADriverClassName() {
        return Arrays.asList("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource", "com.mysql.cj.jdbc.MysqlXADataSource");
    }
    
    @Override
    public Properties getXAProperties(final DatabaseAccessConfiguration databaseAccessConfiguration) {
        Properties result = new Properties();
        result.setProperty("user", databaseAccessConfiguration.getUsername());
        result.setProperty("password", Optional.fromNullable(databaseAccessConfiguration.getPassword()).or(""));
        result.setProperty("URL", databaseAccessConfiguration.getUrl());
        result.setProperty("pinGlobalTxToPhysicalConnection", Boolean.TRUE.toString());
        result.setProperty("autoReconnect", Boolean.TRUE.toString());
        result.setProperty("useServerPrepStmts", Boolean.TRUE.toString());
        result.setProperty("cachePrepStmts", Boolean.TRUE.toString());
        result.setProperty("prepStmtCacheSize", "250");
        result.setProperty("prepStmtCacheSqlLimit", "2048");
        result.setProperty("useLocalSessionState", Boolean.TRUE.toString());
        result.setProperty("rewriteBatchedStatements", Boolean.TRUE.toString());
        result.setProperty("cacheResultSetMetadata", Boolean.FALSE.toString());
        result.setProperty("cacheServerConfiguration", Boolean.TRUE.toString());
        result.setProperty("elideSetAutoCommits", Boolean.TRUE.toString());
        result.setProperty("maintainTimeStats", Boolean.FALSE.toString());
        result.setProperty("netTimeoutForStreamingResults", "0");
        return result;
    }
}
