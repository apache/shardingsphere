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

package org.apache.shardingsphere.test.integration.env.datasource.builder;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.datasource.DatabaseEnvironment;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Proxy data source builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProxyDataSourceBuilder {
    
    private static final Map<DataSourceCacheKey, DataSource> CACHE = new HashMap<>();
    
    /**
     * Build proxy data source.
     *
     * @param name data source name
     * @param databaseType database type
     * @param databaseEnvironment database environment
     * @return proxy data source
     */
    public static DataSource build(final String name, final DatabaseType databaseType, final DatabaseEnvironment databaseEnvironment) {
        DataSourceCacheKey cacheKey = new DataSourceCacheKey(name, databaseType);
        if (CACHE.containsKey(cacheKey)) {
            return CACHE.get(cacheKey);
        }
        DataSource result = createHikariCP(name, databaseEnvironment);
        CACHE.put(cacheKey, result);
        return result;
    }
    
    private static DataSource createHikariCP(final String dataSourceName, final DatabaseEnvironment databaseEnvironment) {
        HikariConfig result = new HikariConfig();
        result.setDriverClassName(databaseEnvironment.getDriverClassName());
        result.setJdbcUrl(databaseEnvironment.getURL(dataSourceName));
        result.setUsername(databaseEnvironment.getUsername());
        result.setPassword(databaseEnvironment.getPassword());
        result.setMaximumPoolSize(2);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        return new HikariDataSource(result);
    }
}
