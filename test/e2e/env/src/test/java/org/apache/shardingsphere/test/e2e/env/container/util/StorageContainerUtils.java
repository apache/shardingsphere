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

package org.apache.shardingsphere.test.e2e.env.container.util;

import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerOption;

import javax.sql.DataSource;

/**
 * Storage container utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StorageContainerUtils {
    
    /**
     * Generate data source.
     *
     * @param jdbcUrl JDBC URL
     * @param username username
     * @param password password
     * @param maximumPoolSize maximum pool size
     * @return data source
     */
    public static DataSource generateDataSource(final String jdbcUrl, final String username, final String password, final int maximumPoolSize) {
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName(DatabaseTypedSPILoader.getService(StorageContainerOption.class, DatabaseTypeFactory.get(jdbcUrl)).getConnectOption().getDriverClassName());
        result.setJdbcUrl(jdbcUrl);
        result.setUsername(username);
        result.setPassword(password);
        result.setMaximumPoolSize(maximumPoolSize);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        result.setLeakDetectionThreshold(10000L);
        return result;
    }
}
