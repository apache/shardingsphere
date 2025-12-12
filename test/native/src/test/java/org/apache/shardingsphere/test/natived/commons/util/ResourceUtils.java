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

package org.apache.shardingsphere.test.natived.commons.util;

import org.apache.shardingsphere.database.connector.core.DefaultDatabase;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.mode.manager.ContextManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * When creating {@link com.zaxxer.hikari.HikariDataSource} through {@link org.apache.shardingsphere.driver.ShardingSphereDriver},
 * the internal real data source will not be closed directly.
 * This causes the hooks for closing the data source of third-party dependencies such as Seata Client to not take effect.
 * Refer to the changes in <a href="https://github.com/apache/incubator-seata/issues/7523">apache/incubator-seata#7523</a>.
 *
 * @see org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource#close()
 */
public class ResourceUtils {
    
    /**
     * Close JDBC dataSource.
     *
     * @param dataSource Usually {@link com.zaxxer.hikari.HikariDataSource}
     * @throws SQLException SQL exception
     */
    public static void closeJdbcDataSource(final DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            ContextManager contextManager = connection.unwrap(ShardingSphereConnection.class).getContextManager();
            contextManager.getStorageUnits(DefaultDatabase.LOGIC_NAME).values().stream().map(StorageUnit::getDataSource).forEach(ResourceUtils::close);
            contextManager.close();
        }
    }
    
    /**
     * Close JDBC dataSource.
     *
     * @param dataSource        Usually {@link com.zaxxer.hikari.HikariDataSource}
     * @param logicDataBaseName The logical database name used in the shardingsphere metadata, if not set,
     *                          it is {@link DefaultDatabase#LOGIC_NAME}
     * @throws SQLException SQL exception
     */
    public static void closeJdbcDataSource(final DataSource dataSource, final String logicDataBaseName) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            ContextManager contextManager = connection.unwrap(ShardingSphereConnection.class).getContextManager();
            contextManager.getStorageUnits(logicDataBaseName).values().stream().map(StorageUnit::getDataSource).forEach(ResourceUtils::close);
            contextManager.close();
        }
    }
    
    private static void close(final DataSource dataSource) {
        if (dataSource instanceof AutoCloseable) {
            try {
                ((AutoCloseable) dataSource).close();
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                throw new RuntimeException(ex);
            }
        }
    }
}
