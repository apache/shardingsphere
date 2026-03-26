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

package org.apache.shardingsphere.test.e2e.mcp;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

final class H2ProductionRuntimeTestSupport {
    
    private H2ProductionRuntimeTestSupport() {
    }
    
    static String createJdbcUrl(final Path tempDir, final String databaseName) {
        return String.format("jdbc:h2:file:%s;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
                tempDir.resolve(databaseName).toAbsolutePath());
    }
    
    static Map<String, String> createRuntimeDatabase(final String jdbcUrl) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("databaseType", "H2");
        result.put("jdbcUrl", jdbcUrl);
        result.put("username", "");
        result.put("password", "");
        result.put("driverClassName", "org.h2.Driver");
        return result;
    }
    
    static Map<String, String> createRuntimeDatabase(final String jdbcUrl, final String databaseType) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("databaseType", databaseType);
        result.put("jdbcUrl", jdbcUrl);
        result.put("username", "");
        result.put("password", "");
        result.put("driverClassName", "org.h2.Driver");
        return result;
    }
    
    static void initializeDatabase(final String jdbcUrl) throws SQLException {
        executeStatements(jdbcUrl,
                "CREATE SCHEMA IF NOT EXISTS public",
                "SET SCHEMA public",
                "CREATE TABLE IF NOT EXISTS orders (order_id INT PRIMARY KEY, status VARCHAR(32), amount INT)",
                "CREATE TABLE IF NOT EXISTS order_items (item_id INT PRIMARY KEY, order_id INT, sku VARCHAR(64))",
                "MERGE INTO orders (order_id, status, amount) KEY (order_id) VALUES (1, 'NEW', 10)",
                "MERGE INTO orders (order_id, status, amount) KEY (order_id) VALUES (2, 'DONE', 20)",
                "MERGE INTO order_items (item_id, order_id, sku) KEY (item_id) VALUES (1, 1, 'sku-1')",
                "CREATE VIEW IF NOT EXISTS active_orders AS SELECT order_id, status FROM orders WHERE status <> 'DONE'",
                "CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status)");
    }
    
    static void executeStatements(final String jdbcUrl, final String... sqls) throws SQLException {
        try (
                Connection connection = DriverManager.getConnection(jdbcUrl);
                Statement statement = connection.createStatement()) {
            for (String each : sqls) {
                statement.execute(each);
            }
        }
    }
    
    static String querySingleString(final String jdbcUrl) throws SQLException {
        try (
                Connection connection = DriverManager.getConnection(jdbcUrl);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT status FROM public.orders WHERE order_id = 1")) {
            resultSet.next();
            return resultSet.getString(1);
        }
    }
}
