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

package org.apache.shardingsphere.test.e2e.operation.pipeline.framework.container.compose.natived;

import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerOption;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.container.compose.PipelineBaseContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.ProxyDatabaseTypeUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Pipeline native composed container, need to start ShardingSphere-Proxy at firstly.
 */
public final class PipelineNativeContainerComposer extends PipelineBaseContainerComposer {
    
    private static final E2ETestEnvironment ENV = E2ETestEnvironment.getInstance();
    
    private final DatabaseType databaseType;
    
    private final DialectPipelineNativeContainerDropTableOption dropTableOption;
    
    public PipelineNativeContainerComposer(final DatabaseType databaseType) {
        this.databaseType = databaseType;
        dropTableOption = DatabaseTypedSPILoader.getService(DialectPipelineNativeContainerDropTableOption.class, databaseType);
    }
    
    @SneakyThrows(SQLException.class)
    @Override
    public void cleanUpDatabase(final String databaseName) {
        int port = ENV.getNativeDatabaseEnvironment().getPort(databaseType);
        String username = ENV.getNativeDatabaseEnvironment().getUser();
        String password = ENV.getNativeDatabaseEnvironment().getPassword();
        String jdbcUrl = dropTableOption.getJdbcUrl(DatabaseTypedSPILoader.getService(StorageContainerOption.class, databaseType).getConnectOption(), port, databaseName);
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            dropTable(connection, databaseName);
        }
    }
    
    private void dropTable(final Connection connection, final String databaseName) throws SQLException {
        Map<String, List<String>> schemaAndTableMapper = getSchemaAndTableMapper(connection, databaseName);
        try (Statement statement = connection.createStatement()) {
            for (Entry<String, List<String>> entry : schemaAndTableMapper.entrySet()) {
                for (String each : entry.getValue()) {
                    statement.executeUpdate(String.format("DROP TABLE %s.%s", entry.getKey(), each));
                }
            }
            Optional<String> dropSchemaSQL = dropTableOption.getDropSchemaSQL();
            if (dropSchemaSQL.isPresent()) {
                statement.executeUpdate(dropSchemaSQL.get());
            }
        }
    }
    
    @SuppressWarnings("CollectionWithoutInitialCapacity")
    private Map<String, List<String>> getSchemaAndTableMapper(final Connection connection, final String databaseName) throws SQLException {
        Map<String, List<String>> result = new HashMap<>();
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(dropTableOption.getQueryAllSchemaAndTableMapperSQL(databaseName))) {
            while (resultSet.next()) {
                result.computeIfAbsent(resultSet.getString(1), key -> new LinkedList<>()).add(resultSet.getString(2));
            }
        }
        return result;
    }
    
    @Override
    public String getProxyJdbcUrl(final String databaseName) {
        return DatabaseTypedSPILoader.getService(StorageContainerOption.class, ProxyDatabaseTypeUtils.getProxyDatabaseType(databaseType)).getConnectOption().getURL("localhost", 3307, databaseName);
    }
    
    @Override
    public int getProxyCDCPort() {
        return 33071;
    }
}
