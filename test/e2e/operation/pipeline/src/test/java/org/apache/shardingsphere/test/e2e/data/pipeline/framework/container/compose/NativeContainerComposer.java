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

package org.apache.shardingsphere.test.e2e.data.pipeline.framework.container.compose;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.core.url.JdbcUrlAppender;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.PipelineE2EEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.DataSourceEnvironment;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Native composed container, you need start ShardingSphere-Proxy at firstly.
 */
public final class NativeContainerComposer extends BaseContainerComposer {
    
    private static final PipelineE2EEnvironment ENV = PipelineE2EEnvironment.getInstance();
    
    private final JdbcUrlAppender jdbcUrlAppender = new JdbcUrlAppender();
    
    private final DatabaseType databaseType;
    
    public NativeContainerComposer(final DatabaseType databaseType) {
        this.databaseType = databaseType;
    }
    
    @SneakyThrows(SQLException.class)
    @Override
    public void cleanUpDatabase(final String databaseName) {
        int actualDatabasePort = ENV.getActualDatabasePort(databaseType);
        String username = ENV.getActualDataSourceUsername(databaseType);
        String password = ENV.getActualDataSourcePassword(databaseType);
        String jdbcUrl;
        switch (databaseType.getType()) {
            case "MySQL":
                String queryAllTables = String.format("select table_name from information_schema.tables where table_schema='%s' and table_type='BASE TABLE'", databaseName);
                jdbcUrl = DataSourceEnvironment.getURL(databaseType, "localhost", actualDatabasePort, databaseName);
                try (
                        Connection connection = DriverManager.getConnection(
                                jdbcUrlAppender.appendQueryProperties(jdbcUrl, PropertiesBuilder.build(new Property("allowPublicKeyRetrieval", Boolean.TRUE.toString()))), username, password)) {
                    try (ResultSet resultSet = connection.createStatement().executeQuery(queryAllTables)) {
                        List<String> actualTableNames = getFirstColumnValueFromResult(resultSet);
                        for (String each : actualTableNames) {
                            connection.createStatement().executeUpdate(String.format("drop table %s", each));
                        }
                    }
                }
                break;
            case "openGauss":
            case "PostgreSQL":
                jdbcUrl = DataSourceEnvironment.getURL(databaseType, "localhost", actualDatabasePort, databaseName);
                try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                    dropTableWithSchema(connection, "public");
                    dropTableWithSchema(connection, "test");
                    connection.createStatement().execute("DROP SCHEMA IF EXISTS test;");
                }
                break;
            default:
        }
    }
    
    private List<String> getFirstColumnValueFromResult(final ResultSet resultSet) throws SQLException {
        List<String> result = new LinkedList<>();
        while (resultSet.next()) {
            result.add(resultSet.getString(1));
        }
        return result;
    }
    
    private void dropTableWithSchema(final Connection connection, final String schema) throws SQLException {
        String queryAllTables = "select tablename from pg_tables where schemaname='%s'";
        try (ResultSet resultSet = connection.createStatement().executeQuery(String.format(queryAllTables, schema))) {
            List<String> actualTableNames = getFirstColumnValueFromResult(resultSet);
            for (String each : actualTableNames) {
                connection.createStatement().executeUpdate(String.format("drop table %s.%s", schema, each));
            }
        }
    }
    
    @Override
    public String getProxyJdbcUrl(final String databaseName) {
        return DataSourceEnvironment.getURL(databaseType, "localhost", 3307, databaseName);
    }
    
    @Override
    public int getProxyCDCPort() {
        return 33071;
    }
}
