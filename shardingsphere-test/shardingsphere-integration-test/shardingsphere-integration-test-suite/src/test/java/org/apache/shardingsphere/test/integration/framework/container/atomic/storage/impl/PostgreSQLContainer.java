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

package org.apache.shardingsphere.test.integration.framework.container.atomic.storage.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.test.integration.env.DataSourceEnvironment;
import org.apache.shardingsphere.test.integration.framework.container.atomic.storage.StorageContainer;
import org.postgresql.util.PSQLException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * PostgreSQL container.
 */
public final class PostgreSQLContainer extends StorageContainer {
    
    public PostgreSQLContainer(final String scenario) {
        super(DatabaseTypeRegistry.getActualDatabaseType("PostgreSQL"), "postgres:12.6", false, scenario);
    }
    
    @Override
    protected void configure() {
        withCommand("--max_connections=200");
        addEnv("POSTGRES_USER", "root");
        addEnv("POSTGRES_PASSWORD", "root");
        withInitSQLMapping("/env/" + getScenario() + "/init-sql/postgresql");
    }

    @Override
    @SneakyThrows({ClassNotFoundException.class, SQLException.class, InterruptedException.class})
    protected void execute() {
        Class.forName(DataSourceEnvironment.getDriverClassName("PostgreSQL"));
        String url = DataSourceEnvironment.getURL("PostgreSQL", getHost(), getPort());
        boolean connected = false;
        while (!connected) {
            try (Connection ignored = DriverManager.getConnection(url, getUsername(), getPassword())) {
                connected = true;
                break;
            } catch (final PSQLException ex) {
                Thread.sleep(500L);
            }
        }
    }
    
    @Override
    protected String getUsername() {
        return "root";
    }
    
    @Override
    protected String getPassword() {
        return "root";
    }
    
    @Override
    protected int getPort() {
        return getMappedPort(5432);
    }
    
    @Override
    public Optional<String> getPrimaryKeyColumnName(final DataSource dataSource, final String tableName) throws SQLException {
        String sql = String.format("SELECT a.attname, format_type(a.atttypid, a.atttypmod) AS data_type "
                + "FROM pg_index i JOIN pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = ANY(i.indkey) WHERE i.indrelid = '%s'::regclass AND i.indisprimary", tableName);
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                return Optional.of(resultSet.getString("attname"));
            }
            throw new SQLException(String.format("Can not get primary key of `%s`", tableName));
        }
    }
}
