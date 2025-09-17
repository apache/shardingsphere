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

package org.apache.shardingsphere.test.e2e.operation.pipeline.framework.container.compose;

import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.jdbcurl.appender.JdbcUrlAppender;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.test.e2e.env.runtime.datasource.DataSourceEnvironment;
import org.apache.shardingsphere.test.e2e.operation.pipeline.env.PipelineE2EEnvironment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Pipeline native composed container, need to start ShardingSphere-Proxy at firstly.
 */
public final class PipelineNativeContainerComposer extends PipelineBaseContainerComposer {
    
    private static final PipelineE2EEnvironment ENV = PipelineE2EEnvironment.getInstance();
    
    private final DatabaseType databaseType;
    
    public PipelineNativeContainerComposer(final DatabaseType databaseType) {
        this.databaseType = databaseType;
    }
    
    @SneakyThrows(SQLException.class)
    @Override
    public void cleanUpDatabase(final String databaseName) {
        int actualDatabasePort = ENV.getActualDatabasePort(databaseType);
        String username = ENV.getActualDataSourceUsername(databaseType);
        String password = ENV.getActualDataSourcePassword(databaseType);
        DataSourceEnvironment dataSourceEnvironment = DatabaseTypedSPILoader.getService(DataSourceEnvironment.class, databaseType);
        String jdbcUrl;
        switch (databaseType.getType()) {
            case "MySQL":
            case "MariaDB":
                jdbcUrl = new JdbcUrlAppender().appendQueryProperties(dataSourceEnvironment.getURL("localhost", actualDatabasePort, databaseName),
                        PropertiesBuilder.build(new Property("allowPublicKeyRetrieval", Boolean.TRUE.toString())));
                try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                    dropTableWithMySQL(connection, databaseName);
                }
                break;
            case "openGauss":
            case "PostgreSQL":
                jdbcUrl = dataSourceEnvironment.getURL("localhost", actualDatabasePort, databaseName);
                try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                    dropTableWithPostgreSQL(connection);
                }
                break;
            case "Oracle":
                jdbcUrl = dataSourceEnvironment.getURL("localhost", actualDatabasePort, "");
                try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                    dropTableWithOracle(connection, databaseName);
                }
                break;
            default:
        }
    }
    
    private void dropTableWithMySQL(final Connection connection, final String databaseName) throws SQLException {
        String queryAllTables = String.format("SELECT table_name FROM information_schema.tables WHERE table_schema='%s' and table_type='BASE TABLE'", databaseName);
        try (ResultSet resultSet = connection.createStatement().executeQuery(queryAllTables)) {
            List<String> actualTableNames = getFirstColumnValueFromResult(resultSet);
            for (String each : actualTableNames) {
                connection.createStatement().executeUpdate(String.format("DROP TABLE %s", each));
            }
        }
    }
    
    private List<String> getFirstColumnValueFromResult(final ResultSet resultSet) throws SQLException {
        List<String> result = new LinkedList<>();
        while (resultSet.next()) {
            result.add(resultSet.getString(1));
        }
        return result;
    }
    
    private void dropTableWithPostgreSQL(final Connection connection) throws SQLException {
        dropTableWithPostgreSQL(connection, "public");
        dropTableWithPostgreSQL(connection, "test");
        connection.createStatement().execute("DROP SCHEMA IF EXISTS test;");
    }
    
    private void dropTableWithPostgreSQL(final Connection connection, final String schema) throws SQLException {
        String queryAllTables = "SELECT tablename FROM pg_tables WHERE='%s'";
        try (ResultSet resultSet = connection.createStatement().executeQuery(String.format(queryAllTables, schema))) {
            List<String> actualTableNames = getFirstColumnValueFromResult(resultSet);
            for (String each : actualTableNames) {
                connection.createStatement().executeUpdate(String.format("DROP TABLE %s.%s", schema, each));
            }
        }
    }
    
    private void dropTableWithOracle(final Connection connection, final String schema) throws SQLException {
        String queryAllTables = String.format("SELECT TABLE_NAME FROM ALL_TABLES WHERE OWNER='%s'", schema);
        try (ResultSet resultSet = connection.createStatement().executeQuery(String.format(queryAllTables, schema))) {
            List<String> actualTableNames = getFirstColumnValueFromResult(resultSet);
            for (String each : actualTableNames) {
                connection.createStatement().executeUpdate(String.format("DROP TABLE %s.%s", schema, each));
            }
        }
    }
    
    @Override
    public String getProxyJdbcUrl(final String databaseName) {
        DatabaseType databaseType = "Oracle".equals(this.databaseType.getType()) ? TypedSPILoader.getService(DatabaseType.class, "MySQL") : this.databaseType;
        return DatabaseTypedSPILoader.getService(DataSourceEnvironment.class, databaseType).getURL("localhost", 3307, databaseName);
    }
    
    @Override
    public int getProxyCDCPort() {
        return 33071;
    }
}
