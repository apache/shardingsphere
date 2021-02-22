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

package org.apache.shardingsphere.test.integration.junit;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.infra.database.type.DatabaseType;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

/**
 * DataSource builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceBuilder {
    
    private static final DataSourcePoolType DATA_SOURCE_POOL_TYPE = DataSourcePoolType.HikariCP;
    
    /**
     * Build actual data source.
     *
     * @param name datasource name
     * @param databaseType database type
     * @param properties connection properties
     * @return datasource
     */
    public static DataSource createDataSource(final String name, final DatabaseType databaseType, final Properties properties) {
        switch (DATA_SOURCE_POOL_TYPE) {
            case DBCP:
                return createDBCP(name, databaseType, properties);
            case HikariCP:
                return createHikariCP(name, databaseType, properties);
            default:
                throw new UnsupportedOperationException(DATA_SOURCE_POOL_TYPE.name());
        }
    }
    
    private static DataSource createDBCP(final String dataSourceName, final DatabaseType databaseType, final Properties properties) {
        BasicDataSource result = new BasicDataSource();
        result.setMaxTotal(2);
        properties.entrySet().forEach(e -> result.addConnectionProperty(String.valueOf(e.getKey()), String.valueOf(e.getValue())));
        getConnectionInitSQL(dataSourceName, databaseType).ifPresent(optional -> result.setConnectionInitSqls(Collections.singleton(optional)));
        return result;
    }
    
    private static DataSource createHikariCP(final String dataSourceName, final DatabaseType databaseType, final Properties properties) {
        HikariConfig result = new HikariConfig();
        result.setDataSourceProperties(properties);
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
    
    public enum DataSourcePoolType {
        
        DBCP, HikariCP
    }
    
}
