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

package org.apache.shardingsphere.mcp.bootstrap.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.test.fixture.jdbc.H2RuntimeTestSupport;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * Runtime fixture support for encrypt and mask workflow transport tests.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FeatureWorkflowRuntimeTestSupport {
    
    /**
     * Create one JDBC URL backed by the feature DistSQL test driver.
     *
     * @param tempDir temp directory
     * @param databaseName database name
     * @param accessMode H2 access mode
     * @return JDBC URL
     */
    public static String createJdbcUrl(final Path tempDir, final String databaseName, final H2RuntimeTestSupport.H2AccessMode accessMode) {
        return FeatureDistSQLTestDriver.createJdbcUrl(H2RuntimeTestSupport.createJdbcUrl(tempDir, databaseName, accessMode));
    }
    
    /**
     * Create runtime databases for the feature workflow runtime.
     *
     * @param databaseName logical database name
     * @param jdbcUrl JDBC URL
     * @return runtime database configurations
     */
    public static Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases(final String databaseName, final String jdbcUrl) {
        return Map.of(databaseName, new RuntimeDatabaseConfiguration("H2", jdbcUrl, "", "", FeatureDistSQLTestDriver.class.getName()));
    }
    
    /**
     * Initialize feature workflow tables.
     *
     * @param jdbcUrl JDBC URL
     * @throws SQLException SQL exception
     */
    public static void initializeDatabase(final String jdbcUrl) throws SQLException {
        executeStatements(jdbcUrl,
                "CREATE SCHEMA IF NOT EXISTS public",
                "SET SCHEMA public",
                "CREATE TABLE IF NOT EXISTS customer_profiles (customer_id INT PRIMARY KEY, phone VARCHAR(32), id_card VARCHAR(32), email VARCHAR(64))",
                "MERGE INTO customer_profiles (customer_id, phone, id_card, email) KEY (customer_id) VALUES (1, '13800000000', '310101199001011234', 'alice@example.com')",
                "MERGE INTO customer_profiles (customer_id, phone, id_card, email) KEY (customer_id) VALUES (2, '13900000000', '310101199202021234', 'bob@example.com')");
    }
    
    private static void executeStatements(final String jdbcUrl, final String... sqls) throws SQLException {
        try (
                Connection connection = DriverManager.getConnection(jdbcUrl);
                Statement statement = connection.createStatement()) {
            for (String each : sqls) {
                statement.execute(each);
            }
        }
    }
}
