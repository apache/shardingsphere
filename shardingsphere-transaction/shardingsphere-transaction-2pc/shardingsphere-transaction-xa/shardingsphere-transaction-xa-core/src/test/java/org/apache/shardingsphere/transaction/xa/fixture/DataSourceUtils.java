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

package org.apache.shardingsphere.transaction.xa.fixture;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.transaction.xa.jta.datasource.XADataSourceFactory;
import org.apache.shardingsphere.infra.database.type.DatabaseType;

import javax.sql.DataSource;

/**
 * Data source utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceUtils {
    
    /**
     * Build data source.
     *
     * @param dataSourceClass data source
     * @param databaseType database type
     * @param databaseName database name
     * @return data source
     */
    public static DataSource build(final Class<? extends DataSource> dataSourceClass, final DatabaseType databaseType, final String databaseName) {
        if (HikariDataSource.class == dataSourceClass) {
            return createHikariDataSource(databaseType, databaseName);
        } else if (AtomikosDataSourceBean.class == dataSourceClass) {
            return createAtomikosDataSourceBean(databaseType, createHikariDataSource(databaseType, databaseName), databaseName);
        }
        
        throw new UnsupportedOperationException(dataSourceClass.getName());
    }
    
    private static HikariDataSource createHikariDataSource(final DatabaseType databaseType, final String databaseName) {
        HikariDataSource result = new HikariDataSource();
        result.setJdbcUrl(getURL(databaseType, databaseName));
        result.setUsername("root");
        result.setPassword("root");
        result.setMaximumPoolSize(10);
        result.setMinimumIdle(2);
        result.setConnectionTimeout(15 * 1000);
        result.setIdleTimeout(40 * 1000);
        return result;
    }
    
    private static AtomikosDataSourceBean createAtomikosDataSourceBean(final DatabaseType databaseType, final DataSource dataSource, final String databaseName) {
        AtomikosDataSourceBean result = new AtomikosDataSourceBean();
        result.setUniqueResourceName(databaseName);
        result.setXaDataSource(XADataSourceFactory.build(databaseType, dataSource));
        return result;
    }
    
    private static String getURL(final DatabaseType databaseType, final String databaseName) {
        switch (databaseType.getName()) {
            case "MySQL":
                return String.format("jdbc:mysql://localhost:3306/%s", databaseName);
            case "MariaDB":
                return String.format("jdbc:mariadb://localhost:3306/%s", databaseName);
            case "PostgreSQL":
                return String.format("jdbc:postgresql://localhost:5432/%s", databaseName);
            case "Oracle":
                return String.format("jdbc:oracle:thin:@//localhost:1521/%s", databaseName);
            case "SQLServer":
                return String.format("jdbc:sqlserver://localhost:1433;DatabaseName=%s", databaseName);
            case "H2":
                return String.format("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL", databaseName);
            default:
                throw new UnsupportedOperationException(databaseType.getName());
        }
    }
}
