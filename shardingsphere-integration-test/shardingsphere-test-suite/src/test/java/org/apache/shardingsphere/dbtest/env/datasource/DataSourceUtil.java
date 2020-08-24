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

package org.apache.shardingsphere.dbtest.env.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.dbtest.env.IntegrateTestEnvironment;
import org.apache.shardingsphere.infra.database.type.DatabaseType;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Data source utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceUtil {
    
    private static final DataSourcePoolType DATA_SOURCE_POOL_TYPE = DataSourcePoolType.HikariCP;
    
    private static final Map<DataSourceCacheKey, DataSource> CACHE = new HashMap<>();
    
    /**
     * Create data source.
     *
     * @param databaseType database type
     * @param dataSourceName data source name
     * @return data source
     */
    public static DataSource createDataSource(final DatabaseType databaseType, final String dataSourceName) {
        DataSourceCacheKey dataSourceCacheKey = new DataSourceCacheKey(databaseType, dataSourceName);
        if (CACHE.containsKey(dataSourceCacheKey)) {
            return CACHE.get(dataSourceCacheKey);
        }
        DataSource result;
        switch (DATA_SOURCE_POOL_TYPE) {
            case DBCP:
                result = createDBCP(databaseType, dataSourceName);
                break;
            case HikariCP:
                result = createHikariCP(databaseType, dataSourceName);
                break;
            default:
                throw new UnsupportedOperationException(DATA_SOURCE_POOL_TYPE.name());
        }
        CACHE.put(dataSourceCacheKey, result);
        return result;
    }
    
    private static DataSource createDBCP(final DatabaseType databaseType, final String dataSourceName) {
        BasicDataSource result = new BasicDataSource();
        DatabaseEnvironment databaseEnvironment = IntegrateTestEnvironment.getInstance().getDatabaseEnvironments().get(databaseType);
        result.setDriverClassName(databaseEnvironment.getDriverClassName());
        result.setUrl(null == dataSourceName ? databaseEnvironment.getURL() : databaseEnvironment.getURL(dataSourceName));
        result.setUsername(databaseEnvironment.getUsername());
        result.setPassword(databaseEnvironment.getPassword());
        result.setMaxTotal(2);
        result.setValidationQuery("SELECT 1");
        if ("Oracle".equals(databaseType.getName())) {
            result.setConnectionInitSqls(Collections.singleton("ALTER SESSION SET CURRENT_SCHEMA = " + dataSourceName));
        }
        if ("MySQL".equals(databaseType.getName())) {
            result.setConnectionInitSqls(Collections.singleton("SET GLOBAL sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''))"));
        }
        return result;
    }
    
    private static DataSource createHikariCP(final DatabaseType databaseType, final String dataSourceName) {
        HikariConfig result = new HikariConfig();
        DatabaseEnvironment databaseEnvironment = IntegrateTestEnvironment.getInstance().getDatabaseEnvironments().get(databaseType);
        result.setDriverClassName(databaseEnvironment.getDriverClassName());
        result.setJdbcUrl(null == dataSourceName ? databaseEnvironment.getURL() : databaseEnvironment.getURL(dataSourceName));
        result.setUsername(databaseEnvironment.getUsername());
        result.setPassword(databaseEnvironment.getPassword());
        result.setMaximumPoolSize(2);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        result.setConnectionTestQuery("SELECT 1");
        if ("Oracle".equals(databaseType.getName())) {
            result.setConnectionInitSql("ALTER SESSION SET CURRENT_SCHEMA = " + dataSourceName);
        }
        if ("MySQL".equals(databaseType.getName())) {
            result.setConnectionInitSql("SET GLOBAL sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''))");
        }
        return new HikariDataSource(result);
    }
    
    @RequiredArgsConstructor
    @EqualsAndHashCode
    private static class DataSourceCacheKey {
        
        private final DatabaseType databaseType;
        
        private final String dataSourceName;
    }
}
