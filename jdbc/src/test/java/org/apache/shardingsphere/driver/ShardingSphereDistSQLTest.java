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

package org.apache.shardingsphere.driver;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingSphereDistSQLTest {
    
    private static final String URL = "jdbc:shardingsphere:classpath:config/driver/driver-fixture-h2-mysql.yaml";
    
    private static final String PARSE_SQL = "PARSE SELECT * FROM t_order";
    
    private static final String UPDATE_SQL = "SET DIST VARIABLE SQL_SHOW='true'";
    
    @Test
    void assertStatementQueriesAndResultSetLifecycle() throws SQLException {
        try (Connection connection = DriverManager.getConnection(URL); Statement statement = connection.createStatement()) {
            ResultSet parsed = statement.executeQuery(PARSE_SQL);
            assertThat(statement.getResultSet(), sameInstance(parsed));
            assertThat(parsed.getStatement(), sameInstance(statement));
            assertThat(parsed.getType(), is(statement.getResultSetType()));
            assertThat(parsed.getConcurrency(), is(statement.getResultSetConcurrency()));
            assertThat(statement.getUpdateCount(), is(-1));
            assertColumn(parsed, "parsed_statement", 2);
            assertTrue(parsed.next());
            assertThat(parsed.getString("parsed_statement"), is("SelectStatement"));
            assertNotNull(parsed.getObject(2));
            assertFalse(parsed.next());
            try (ResultSet preview = statement.executeQuery("PREVIEW SELECT 1")) {
                assertTrue(parsed.isClosed());
                assertColumn(preview, "data_source_name", 2);
                assertTrue(preview.next());
                assertNotNull(preview.getString("actual_sql"));
            }
            try (ResultSet rules = statement.executeQuery("SHOW SHARDING TABLE RULES")) {
                assertColumn(rules, "table", 16);
                assertTrue(rules.next());
                assertThat(rules.getString("table"), is("t_order"));
                assertThat(rules.getString("actual_data_nodes"), is(""));
                assertFalse(rules.wasNull());
            }
            try (ResultSet empty = statement.executeQuery("SHOW DIST VARIABLES LIKE 'not_a_dist_variable'")) {
                assertColumn(empty, "variable_name", 2);
                assertFalse(empty.next());
            }
            try (ResultSet count = statement.executeQuery("SHOW DIST VARIABLES LIKE 'cached_connections'")) {
                assertTrue(count.next());
                assertTrue(count.getInt("variable_value") >= 0);
                assertFalse(count.wasNull());
            }
            try (ResultSet variables = statement.executeQuery("SHOW DIST VARIABLES")) {
                String previous = "";
                while (variables.next()) {
                    String current = variables.getString("variable_name");
                    assertTrue(previous.compareTo(current) <= 0);
                    previous = current;
                }
            }
        }
    }
    
    @Test
    void assertStatementExecuteAndUpdateCounts() throws SQLException {
        try (Connection connection = DriverManager.getConnection(URL); Statement statement = connection.createStatement()) {
            assertTrue(statement.execute(PARSE_SQL));
            try (ResultSet resultSet = statement.getResultSet()) {
                assertNotNull(resultSet);
                assertTrue(resultSet.next());
            }
            assertThat(statement.getUpdateCount(), is(-1));
            assertThat(statement.executeUpdate(UPDATE_SQL), is(0));
            assertNull(statement.getResultSet());
            assertThat(statement.getUpdateCount(), is(0));
            assertFalse(statement.execute(UPDATE_SQL));
            assertNull(statement.getResultSet());
            assertThat(statement.getUpdateCount(), is(0));
            assertFalse(statement.getMoreResults());
            assertThat(statement.getUpdateCount(), is(-1));
            assertThrows(SQLException.class, () -> statement.executeQuery(UPDATE_SQL));
            assertThrows(SQLException.class, () -> statement.executeUpdate(PARSE_SQL));
            ResultSet previous = statement.executeQuery(PARSE_SQL);
            try (ResultSet normal = statement.executeQuery("SELECT 1")) {
                assertTrue(previous.isClosed());
                assertTrue(normal.next());
                assertThat(normal.getInt(1), is(1));
            }
            assertThat(statement.getUpdateCount(), is(-1));
        }
    }
    
    @Test
    void assertPreparedStatementQueriesAndUpdates() throws SQLException {
        try (Connection connection = DriverManager.getConnection(URL); PreparedStatement statement = connection.prepareStatement(PARSE_SQL)) {
            ResultSet first = statement.executeQuery();
            assertThat(statement.getResultSet(), sameInstance(first));
            assertColumn(first, "parsed_statement", 2);
            assertTrue(first.next());
            assertThat(first.getString(1), is("SelectStatement"));
            assertTrue(statement.execute());
            assertTrue(first.isClosed());
            try (ResultSet second = statement.getResultSet()) {
                assertNotNull(second);
                assertTrue(second.next());
            }
            assertThat(statement.getUpdateCount(), is(-1));
            assertThrows(SQLException.class, statement::executeUpdate);
        }
        try (Connection connection = DriverManager.getConnection(URL); PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            assertThat(statement.executeUpdate(), is(0));
            assertThat(statement.getUpdateCount(), is(0));
            assertNull(statement.getResultSet());
            assertFalse(statement.execute());
            assertThat(statement.getUpdateCount(), is(0));
            assertNull(statement.getResultSet());
            assertFalse(statement.getMoreResults());
            assertThat(statement.getUpdateCount(), is(-1));
            assertThrows(SQLException.class, statement::executeQuery);
        }
    }
    
    @Test
    void assertPreparedPreviewAndRuleQuery() throws SQLException {
        try (Connection connection = DriverManager.getConnection(URL); PreparedStatement preview = connection.prepareStatement("PREVIEW SELECT 1")) {
            try (ResultSet resultSet = preview.executeQuery()) {
                assertColumn(resultSet, "data_source_name", 2);
                assertTrue(resultSet.next());
                assertNotNull(resultSet.getString("actual_sql"));
            }
            assertThat(preview.getUpdateCount(), is(-1));
        }
        try (Connection connection = DriverManager.getConnection(URL); PreparedStatement rules = connection.prepareStatement("SHOW SHARDING TABLE RULES")) {
            assertTrue(rules.execute());
            try (ResultSet resultSet = rules.getResultSet()) {
                assertColumn(resultSet, "table", 16);
                assertTrue(resultSet.next());
                assertThat(resultSet.getString(1), is("t_order"));
            }
        }
    }
    
    @Test
    void assertStatementCloseClosesDistSQLResultSet() throws SQLException {
        try (Connection connection = DriverManager.getConnection(URL)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(PARSE_SQL);
            statement.close();
            assertTrue(resultSet.isClosed());
        }
    }
    
    @Test
    void assertTransactionRestrictionsAndOrdinarySQL() throws SQLException {
        try (Connection connection = DriverManager.getConnection(URL); Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            try (ResultSet resultSet = statement.executeQuery(PARSE_SQL)) {
                assertTrue(resultSet.next());
            }
            try (ResultSet resultSet = statement.executeQuery("SHOW SHARDING TABLE RULES")) {
                assertTrue(resultSet.next());
            }
            try (ResultSet resultSet = statement.executeQuery("SHOW DIST VARIABLES LIKE 'cached_connections'")) {
                assertTrue(resultSet.next());
            }
            assertThrows(SQLException.class, () -> statement.executeUpdate(UPDATE_SQL));
            assertThrows(SQLException.class, () -> statement.execute(UPDATE_SQL));
            connection.rollback();
            connection.setAutoCommit(true);
            try (ResultSet resultSet = statement.executeQuery("SELECT 1")) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getInt(1), is(1));
            }
            assertThat(statement.getUpdateCount(), is(-1));
        }
        try (Connection connection = DriverManager.getConnection(URL); PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            connection.setAutoCommit(false);
            assertThrows(SQLException.class, statement::executeUpdate);
            connection.rollback();
        }
    }
    
    private void assertColumn(final ResultSet resultSet, final String firstColumnName, final int count) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        assertThat(metaData.getColumnCount(), is(count));
        assertThat(metaData.getColumnLabel(1), is(firstColumnName));
        assertThat(metaData.getColumnName(1), is(firstColumnName));
        assertThat(metaData.getColumnType(1), is(Types.CHAR));
        assertThat(resultSet.findColumn(firstColumnName), is(1));
    }
}
