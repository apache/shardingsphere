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

package org.apache.shardingsphere.test.integration.env.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.IntegrateTestEnvironment;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Data source builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceBuilder {
    
    private static final DataSourcePoolType DATA_SOURCE_POOL_TYPE = DataSourcePoolType.HikariCP;
    
    private static final Map<DataSourceCacheKey, DataSource> CACHE = new HashMap<>();
    
    /**
     * Build data source.
     *
     * @param name data source name
     * @param databaseType database type
     * @return data source
     */
    public static DataSource build(final String name, final DatabaseType databaseType) {
        DataSourceCacheKey cacheKey = new DataSourceCacheKey(name, databaseType);
        if (CACHE.containsKey(cacheKey)) {
            return CACHE.get(cacheKey);
        }
        DataSource result = createDataSource(name, databaseType);
        CACHE.put(cacheKey, result);
        return result;
    }
    
    private static DataSource createDataSource(final String name, final DatabaseType databaseType) {
        DatabaseEnvironment databaseEnvironment = IntegrateTestEnvironment.getInstance().getDatabaseEnvironments().get(databaseType);
        switch (DATA_SOURCE_POOL_TYPE) {
            case DBCP:
                return createDBCP(name, databaseType, databaseEnvironment);
            case HikariCP:
                return createHikariCP(name, databaseType, databaseEnvironment);
            default:
                throw new UnsupportedOperationException(DATA_SOURCE_POOL_TYPE.name());
        }
    }

    private static DataSource createDBCP(final String dataSourceName, final DatabaseType databaseType, final DatabaseEnvironment databaseEnvironment) {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(databaseEnvironment.getDriverClassName());
        result.setUrl(null == dataSourceName ? databaseEnvironment.getURL() : databaseEnvironment.getURL(dataSourceName));
        result.setUsername(databaseEnvironment.getUsername());
        result.setPassword(databaseEnvironment.getPassword());
        result.setMaxTotal(2);
        getConnectionInitSQL(dataSourceName, databaseType).ifPresent(optional -> result.setConnectionInitSqls(Collections.singleton(optional)));
        return result;
    }
    
    private static DataSource createHikariCP(final String dataSourceName, final DatabaseType databaseType, final DatabaseEnvironment databaseEnvironment) {
        HikariConfig result = new HikariConfig();
        result.setDriverClassName(databaseEnvironment.getDriverClassName());
        result.setJdbcUrl(null == dataSourceName ? databaseEnvironment.getURL() : databaseEnvironment.getURL(dataSourceName));
        result.setUsername(databaseEnvironment.getUsername());
        result.setPassword(databaseEnvironment.getPassword());
        result.setMaximumPoolSize(2);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        getConnectionInitSQL(dataSourceName, databaseType).ifPresent(result::setConnectionInitSql);
        return new HikariDataSource(result);
    }
    
    private static Optional<String> getConnectionInitSQL(final String dataSourceName, final DatabaseType databaseType) {
        switch (databaseType.getName()) {
            case "Oracle":
                return Optional.of(String.format("ALTER SESSION SET CURRENT_SCHEMA = %s", dataSourceName));
            case "MySQL":
                return Optional.of("SET GLOBAL sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''))");
            default:
                return Optional.empty();
        }
    }
    
    @RequiredArgsConstructor
    @EqualsAndHashCode
    private static class DataSourceCacheKey {
        
        private final String dataSourceName;
        
        private final DatabaseType databaseType;
    }
}
