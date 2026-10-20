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

import org.apache.shardingsphere.infra.hint.HintManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for shorthand projection (SELECT T.*) with HintManager on sharded table t_order.
 *
 * <p>These tests reproduce issue #28841: when HintManager routes to a single datasource,
 * SQL bind is skipped and shorthand projections remain unexpanded, causing column count
 * and label mismatches in the ResultSet.</p>
 */
class ShardingSphereResultSetShorthandProjectionIT {
    
    private static final String SHARDING_JDBC_URL = "jdbc:shardingsphere:classpath:config/driver/driver-fixture-h2-mysql.yaml";
    
    private static final String H2_DS_0_URL = "jdbc:h2:mem:mysql_ds_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL";
    
    private static final String H2_DS_1_URL = "jdbc:h2:mem:mysql_ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL";
    
    private static Connection shardingSphereConnection;
    
    @BeforeAll
    static void setUp() throws SQLException {
        shardingSphereConnection = DriverManager.getConnection(SHARDING_JDBC_URL);
        initDataSource(H2_DS_0_URL);
        initDataSource(H2_DS_1_URL);
    }
    
    private static void initDataSource(final String url) throws SQLException {
        try (
                Connection connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT, status VARCHAR(50))");
            statement.execute("INSERT INTO t_order VALUES (1000, 10, 'ACTIVE')");
            statement.execute("INSERT INTO t_order VALUES (1001, 11, 'PENDING')");
        }
    }
    
    @AfterAll
    static void tearDown() throws SQLException {
        if (null != shardingSphereConnection) {
            shardingSphereConnection.close();
        }
    }
    
    /**
     * Case 1: SELECT T.*, T.status status_new FROM t_order T WHERE T.order_id = 1000
     * Expected columns: order_id, user_id, status, status_new (4 columns).
     */
    @Test
    void assertShorthandWithAlias() throws SQLException {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDataSourceName("ds_0");
            try (
                    Statement statement = shardingSphereConnection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT T.*, T.status status_new FROM t_order T WHERE T.order_id = 1000")) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                assertThat("column count should be 4", metaData.getColumnCount(), is(4));
                assertThat(metaData.getColumnLabel(1), is("order_id"));
                assertThat(metaData.getColumnLabel(2), is("user_id"));
                assertThat(metaData.getColumnLabel(3), is("status"));
                assertThat(metaData.getColumnLabel(4), is("status_new"));
                assertTrue(resultSet.next());
                assertThat(resultSet.getString("order_id"), is("1000"));
                assertThat(resultSet.getString("status_new"), is("ACTIVE"));
                assertFalse(resultSet.next());
            }
        }
    }
    
    /**
     * Case 2: SELECT T.order_id, T.status status_new FROM t_order T WHERE T.order_id = 1000
     * Expected columns: order_id, status_new (2 columns).
     */
    @Test
    void assertExplicitProjectionsOnly() throws SQLException {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDataSourceName("ds_0");
            try (
                    Statement statement = shardingSphereConnection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT T.order_id, T.status status_new FROM t_order T WHERE T.order_id = 1000")) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                assertThat("column count should be 2", metaData.getColumnCount(), is(2));
                assertThat(metaData.getColumnLabel(1), is("order_id"));
                assertThat(metaData.getColumnLabel(2), is("status_new"));
                assertTrue(resultSet.next());
                assertThat(resultSet.getString("order_id"), is("1000"));
                assertThat(resultSet.getString("status_new"), is("ACTIVE"));
                assertFalse(resultSet.next());
            }
        }
    }
    
    /**
     * Case 3: SELECT t_order.status FROM t_order ORDER BY t_order.order_id ASC
     * Internal ORDER_BY_DERIVED column must be hidden. Expected: 1 visible column (status).
     */
    @Test
    void assertOrderByDerivedColumnHidden() throws SQLException {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDataSourceName("ds_0");
            try (
                    Statement statement = shardingSphereConnection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT t_order.status FROM t_order ORDER BY t_order.order_id ASC")) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                assertThat("only status should be visible, ORDER_BY_DERIVED must be hidden", metaData.getColumnCount(), is(1));
                assertThat(metaData.getColumnLabel(1), is("status"));
                assertTrue(resultSet.next());
            }
        }
    }
    
    /**
     * Case 4: SELECT t_order.status AS ORDER_BY_DERIVED_0 FROM t_order WHERE order_id = 1000
     * User alias matches derived-column prefix — must remain visible. Expected: 1 column labeled ORDER_BY_DERIVED_0.
     */
    @Test
    void assertUserAliasMatchingDerivedPrefix() throws SQLException {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDataSourceName("ds_0");
            try (
                    Statement statement = shardingSphereConnection.createStatement();
                    ResultSet resultSet = statement.executeQuery(
                            "SELECT t_order.status AS ORDER_BY_DERIVED_0 FROM t_order WHERE order_id = 1000")) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                assertThat(metaData.getColumnCount(), is(1));
                assertThat(metaData.getColumnLabel(1), is("ORDER_BY_DERIVED_0"));
                assertTrue(resultSet.next());
                assertThat(resultSet.getString("ORDER_BY_DERIVED_0"), is("ACTIVE"));
                assertFalse(resultSet.next());
            }
        }
    }
    
    /**
     * Case 5: SELECT t_order.status AS ORDER_BY_DERIVED_0 FROM t_order ORDER BY t_order.order_id ASC
     * User alias ORDER_BY_DERIVED_0 must be visible; internal merge column must be hidden.
     * Expected: 1 visible column labeled ORDER_BY_DERIVED_0.
     */
    @Test
    void assertUserAliasWithRealOrderByDerived() throws SQLException {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDataSourceName("ds_0");
            try (
                    Statement statement = shardingSphereConnection.createStatement();
                    ResultSet resultSet = statement.executeQuery(
                            "SELECT t_order.status AS ORDER_BY_DERIVED_0 FROM t_order ORDER BY t_order.order_id ASC")) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                assertThat("user alias ORDER_BY_DERIVED_0 visible, internal one hidden", metaData.getColumnCount(), is(1));
                assertThat(metaData.getColumnLabel(1), is("ORDER_BY_DERIVED_0"));
                assertTrue(resultSet.next());
                assertThat(resultSet.getString("ORDER_BY_DERIVED_0"), is("ACTIVE"));
            }
        }
    }
    
    /**
     * Case 6: SELECT COUNT(DISTINCT user_id) FROM t_order
     * Expected: 1 column.
     */
    @Test
    void assertCountDistinct() throws SQLException {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDataSourceName("ds_0");
            try (
                    Statement statement = shardingSphereConnection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT COUNT(DISTINCT user_id) FROM t_order")) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                assertThat(metaData.getColumnCount(), is(1));
                assertTrue(resultSet.next());
                assertThat(resultSet.getInt(1), is(2));
                assertFalse(resultSet.next());
            }
        }
    }
}
