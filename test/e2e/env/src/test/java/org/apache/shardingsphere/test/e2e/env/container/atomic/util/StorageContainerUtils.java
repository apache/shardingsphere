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

package org.apache.shardingsphere.test.e2e.env.container.atomic.util;

import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.runtime.DataSourceEnvironment;

import javax.sql.DataSource;

/**
 * Storage container utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StorageContainerUtils {
    
    /**
     * Generate datasource.
     *
     * @param jdbcUrl JDBC URL for generating datasource
     * @param username username
     * @param password password
     * @return data source
     */
    public static DataSource generateDataSource(final String jdbcUrl, final String username, final String password) {
        return generateDataSource(jdbcUrl, username, password, 2);
    }
    
    /**
     * Generate datasource.
     *
     * @param jdbcUrl JDBC URL for generating datasource
     * @param username username
     * @param password password
     * @param maximumPoolSize maximum pool size
     * @return data source
     */
    public static DataSource generateDataSource(final String jdbcUrl, final String username, final String password, final int maximumPoolSize) {
        DatabaseType databaseType = DatabaseTypeFactory.get(jdbcUrl);
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName(DataSourceEnvironment.getDriverClassName(databaseType));
        result.setJdbcUrl(jdbcUrl);
        result.setUsername(username);
        result.setPassword(password);
        result.setMaximumPoolSize(maximumPoolSize);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        return result;
    }
}
