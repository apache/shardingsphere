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
import org.apache.shardingsphere.test.e2e.env.runtime.datasource.DataSourceEnvironment;
import org.apache.shardingsphere.test.e2e.operation.pipeline.env.PipelineE2EEnvironment;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.container.compose.PipelineBaseContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.ProxyDatabaseTypeUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Pipeline native composed container, need to start ShardingSphere-Proxy at firstly.
 */
public final class PipelineNativeContainerComposer extends PipelineBaseContainerComposer {
    
    private static final PipelineE2EEnvironment ENV = PipelineE2EEnvironment.getInstance();
    
    private final DatabaseType databaseType;
    
    private final DialectPipelineNativeContainerDropTableOption dropTableOption;
    
    public PipelineNativeContainerComposer(final DatabaseType databaseType) {
        this.databaseType = databaseType;
        dropTableOption = DatabaseTypedSPILoader.getService(DialectPipelineNativeContainerDropTableOption.class, databaseType);
    }
    
    @SneakyThrows(SQLException.class)
    @Override
    public void cleanUpDatabase(final String databaseName) {
        int actualDatabasePort = ENV.getActualDatabasePort(databaseType);
        String username = ENV.getActualDataSourceUsername(databaseType);
        String password = ENV.getActualDataSourcePassword(databaseType);
        String jdbcUrl = dropTableOption.getJdbcUrl(DatabaseTypedSPILoader.getService(DataSourceEnvironment.class, databaseType), actualDatabasePort, databaseName);
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            dropTable(connection, databaseName);
        }
    }
    
    private void dropTable(final Connection connection, final String databaseName) throws SQLException {
        Map<String, String> schemaAndTableMapper = getSchemaAndTableMapper(connection, databaseName);
        try (Statement statement = connection.createStatement()) {
            for (Entry<String, String> entry : schemaAndTableMapper.entrySet()) {
                statement.executeUpdate(String.format("DROP TABLE %s.%s", entry.getKey(), entry.getValue()));
            }
            Optional<String> dropSchemaSQL = dropTableOption.getDropSchemaSQL();
            if (dropSchemaSQL.isPresent()) {
                statement.executeUpdate(dropSchemaSQL.get());
            }
        }
    }
    
    @SuppressWarnings("CollectionWithoutInitialCapacity")
    private Map<String, String> getSchemaAndTableMapper(final Connection connection, final String databaseName) throws SQLException {
        Map<String, String> result = new HashMap<>();
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(dropTableOption.getQueryAllSchemaAndTableMapperSQL(databaseName))) {
            while (resultSet.next()) {
                result.put(resultSet.getString(1), resultSet.getString(2));
            }
        }
        return result;
    }
    
    @Override
    public String getProxyJdbcUrl(final String databaseName) {
        return DatabaseTypedSPILoader.getService(DataSourceEnvironment.class, ProxyDatabaseTypeUtils.getProxyDatabaseType(databaseType)).getURL("localhost", 3307, databaseName);
    }
    
    @Override
    public int getProxyCDCPort() {
        return 33071;
    }
}
