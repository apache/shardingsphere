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
import org.apache.shardingsphere.test.integration.env.IntegrateTestEnvironment;
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
     * @return proxy data source
     */
    public static DataSource build(final String name, final DatabaseType databaseType) {
        DataSourceCacheKey cacheKey = new DataSourceCacheKey(name, databaseType);
        if (CACHE.containsKey(cacheKey)) {
            return CACHE.get(cacheKey);
        }
        DataSource result = createHikariCP(databaseType, name);
        CACHE.put(cacheKey, result);
        return result;
    }
    
    private static DataSource createHikariCP(final DatabaseType databaseType, final String dataSourceName) {
        HikariConfig result = new HikariConfig();
        DatabaseEnvironment databaseEnvironment = IntegrateTestEnvironment.getInstance().getDatabaseEnvironments().get(databaseType);
        result.setDriverClassName(databaseEnvironment.getDriverClassName());
        result.setJdbcUrl(getURL(dataSourceName, databaseType));
        result.setUsername("root");
        result.setPassword("root");
        result.setMaximumPoolSize(2);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        return new HikariDataSource(result);
    }
    
    private static String getURL(final String dataSourceName, final DatabaseType databaseType) {
        switch (databaseType.getName()) {
            case "MySQL":
                return String.format("jdbc:mysql://127.0.0.1:33070/%s?serverTimezone=UTC&useSSL=false&useLocalSessionState=true", dataSourceName);
            case "PostgreSQL":
                return String.format("jdbc:postgresql://127.0.0.1:33070/%s", dataSourceName);
            default:
                throw new UnsupportedOperationException(databaseType.getName());
        }
    }
}
