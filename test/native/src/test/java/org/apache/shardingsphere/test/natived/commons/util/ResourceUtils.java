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

import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * When creating {@link com.zaxxer.hikari.HikariDataSource} through {@link org.apache.shardingsphere.driver.ShardingSphereDriver}
 * or starting ShardingSphere Proxy through {@link org.apache.shardingsphere.proxy.Bootstrap},
 * the internal real data source will not be closed directly.
 * This causes the hooks for closing the data source of third-party dependencies such as Seata Client to not take effect.
 * Refer to the changes in <a href="https://github.com/apache/incubator-seata/pull/7044">apache/incubator-seata#7044</a>.
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
     *                          it is {@link org.apache.shardingsphere.infra.database.core.DefaultDatabase#LOGIC_NAME}
     * @throws SQLException SQL exception
     */
    public static void closeJdbcDataSource(final DataSource dataSource, final String logicDataBaseName) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            ContextManager contextManager = connection.unwrap(ShardingSphereConnection.class).getContextManager();
            contextManager.getStorageUnits(logicDataBaseName).values().stream().map(StorageUnit::getDataSource).forEach(ResourceUtils::close);
            contextManager.close();
        }
    }
    
    /**
     * Close Proxy dataSource.
     *
     * @param logicDataBaseNameList List of logical database names created by Proxy.
     */
    public static void closeProxyDataSource(final List<String> logicDataBaseNameList) {
        ContextManager contextManager = ProxyContext.getInstance().getContextManager();
        logicDataBaseNameList.forEach(logicDataBaseName -> contextManager.getStorageUnits(logicDataBaseName)
                .values()
                .stream()
                .map(StorageUnit::getDataSource)
                .forEach(ResourceUtils::close));
        contextManager.close();
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
