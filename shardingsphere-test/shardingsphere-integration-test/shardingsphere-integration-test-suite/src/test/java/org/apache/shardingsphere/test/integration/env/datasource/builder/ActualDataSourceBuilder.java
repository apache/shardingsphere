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
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.test.integration.env.datasource.DatabaseEnvironment;
import org.apache.shardingsphere.test.integration.env.database.DatabaseEnvironmentManager;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Actual data source builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ActualDataSourceBuilder {
    
    private static final DataSourcePoolType DATA_SOURCE_POOL_TYPE = DataSourcePoolType.HikariCP;
    
    private static final Map<DataSourceCacheKey, DataSource> CACHE = new HashMap<>();
    
    /**
     * Create actual data sources.
     * 
     * @param scenario scenario
     * @param databaseType database type
     * @return actual data sources map
     * @throws IOException IO exception
     * @throws JAXBException JAXB exception
     */
    public static Map<String, DataSource> createActualDataSources(final String scenario, final DatabaseType databaseType) throws IOException, JAXBException {
        Collection<String> dataSourceNames = DatabaseEnvironmentManager.getDatabaseNames(scenario);
        Map<String, DataSource> result = new HashMap<>(dataSourceNames.size(), 1);
        for (String each : dataSourceNames) {
            result.put(each, build(each, scenario, databaseType));
        }
        return result;
    }
    
    /**
     * Build actual data source.
     *
     * @param name actual data source name
     * @param scenario scenario
     * @param databaseType database type
     * @return actual data source
     */
    public static DataSource build(final String name, final String scenario, final DatabaseType databaseType) {
        DataSourceCacheKey cacheKey = new DataSourceCacheKey(name, databaseType);
        if (CACHE.containsKey(cacheKey)) {
            return CACHE.get(cacheKey);
        }
        DataSource result = createDataSource(name, scenario, databaseType);
        CACHE.put(cacheKey, result);
        return result;
    }
    
    private static DataSource createDataSource(final String name, final String scenario, final DatabaseType databaseType) {
        DatabaseEnvironment databaseEnvironment = IntegrationTestEnvironment.getInstance().getDatabaseEnvironments().get(databaseType).get(scenario);
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
}
