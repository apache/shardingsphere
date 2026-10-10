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

package org.apache.shardingsphere.driver.jdbc.core.resultset;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.driver.jdbc.core.statement.ShardingSpherePreparedStatement;
import org.apache.shardingsphere.driver.jdbc.core.statement.ShardingSphereStatement;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingSphereResultSetTest {
    
    private MergedResult mergeResultSet;
    
    private ShardingSphereResultSet shardingSphereResultSet;
    
    private DatabaseType protocolType;
    
    private ShardingSphereConnection connection;
    
    private MetaDataContexts metaDataContexts;
    
    private ShardingSphereStatement statement;
    
    @Test
    void assertFindColumn() throws SQLException {
        assertThat(shardingSphereResultSet.findColumn("LABEL"), is(1));
    }
    
    @Test
    void assertFindColumnWithUnknownLabel() {
        assertThrows(SQLException.class, () -> shardingSphereResultSet.findColumn("absent_label"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("aggregationDistinctQueries")
    void assertAggregationDistinctWithSchemaDrift(final String name, final int routeCount, final boolean schemaDrift, final String sql, final String expectedLabel) throws SQLException {
        JdbcDataSource backend = new JdbcDataSource();
        backend.setURL("jdbc:h2:mem:foo_distinct_drift;MODE=MySQL;DATABASE_TO_UPPER=false");
        try (Connection backendConnection = backend.getConnection(); Statement backendStatement = backendConnection.createStatement()) {
            for (int index = 0; index < routeCount; index++) {
                backendStatement.execute("CREATE TABLE t_order_" + index + " (order_id INT PRIMARY KEY, user_id INT)");
                backendStatement.execute("INSERT INTO t_order_" + index + " VALUES (" + index + ", " + (index + 8) + ")");
            }
            ShardingRuleConfiguration rule = new ShardingRuleConfiguration();
            rule.getTables().add(new ShardingTableRuleConfiguration("t_order", "ds.t_order_${0.." + (routeCount - 1) + "}"));
            try (
                    ShardingSphereDataSource dataSource = (ShardingSphereDataSource) ShardingSphereDataSourceFactory.createDataSource("foo_db", new ModeConfiguration("Standalone", null),
                            Collections.singletonMap("ds", backend), Collections.singleton(rule), new Properties())) {
                if (schemaDrift) {
                    for (int index = 0; index < routeCount; index++) {
                        backendStatement.execute("ALTER TABLE t_order_" + index + " ADD add_test INT DEFAULT 99 BEFORE user_id");
                    }
                }
                assertAggregationDistinctResult(dataSource, routeCount, schemaDrift, sql, expectedLabel);
            }
        }
    }
    
    private void assertAggregationDistinctResult(final ShardingSphereDataSource dataSource, final int expectedRowCount, final boolean schemaDrift,
                                                 final String sql, final String expectedLabel) throws SQLException {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement(); ResultSet actual = statement.executeQuery(sql)) {
            String[] expected = schemaDrift ? new String[]{expectedLabel, "order_id", "add_test", "user_id"} : new String[]{expectedLabel, "order_id", "user_id"};
            ResultSetMetaData actualMetadata = actual.getMetaData();
            assertThat(actualMetadata.getColumnCount(), is(expected.length));
            for (int columnIndex = 1; columnIndex <= expected.length; columnIndex++) {
                assertThat(actualMetadata.getColumnLabel(columnIndex), is(expected[columnIndex - 1]));
                assertThat(actualMetadata.getColumnName(columnIndex), is(expected[columnIndex - 1]));
                assertThat(actual.findColumn(expected[columnIndex - 1]), is(columnIndex));
            }
            int actualRowCount = 0;
            while (actual.next()) {
                assertThat(actual.getInt(expectedLabel), is(1));
                assertThat(actual.getInt("user_id"), is(actual.getInt("order_id") + 8));
                if (schemaDrift) {
                    assertThat(actual.getInt("add_test"), is(99));
                }
                actualRowCount++;
            }
            assertThat(actualRowCount, is(expectedRowCount));
        }
    }
    
    private static Collection<Arguments> aggregationDistinctQueries() {
        return Arrays.asList(
                Arguments.of("multiple routes without drift", 2, false, "SELECT COUNT(DISTINCT user_id), t_order.* FROM t_order GROUP BY order_id", "COUNT(DISTINCT user_id)"),
                Arguments.of("multiple routes with drift", 2, true, "SELECT COUNT(DISTINCT user_id), t_order.* FROM t_order GROUP BY order_id + 0", "COUNT(DISTINCT user_id)"),
                Arguments.of("explicit alias with drift", 2, true, "SELECT COUNT(DISTINCT user_id) AS foo_count, t_order.* FROM t_order GROUP BY order_id", "foo_count"),
                Arguments.of("explicit derived alias with drift", 2, true, "SELECT COUNT(DISTINCT user_id) AS AGGREGATION_DISTINCT_DERIVED_0, t_order.* FROM t_order GROUP BY order_id",
                        "AGGREGATION_DISTINCT_DERIVED_0"),
                Arguments.of("single route with drift", 1, true, "SELECT COUNT(DISTINCT user_id), t_order.* FROM t_order GROUP BY order_id", "COUNT(DISTINCT user_id)"));
    }
    
    @Test
    void assertFindColumnAfterPreparedStatementReuseWithSchemaDrift() throws SQLException {
        JdbcDataSource backend = new JdbcDataSource();
        backend.setURL("jdbc:h2:mem:foo_statement_reuse;MODE=MySQL;DATABASE_TO_UPPER=false");
        try (Connection backendConnection = backend.getConnection(); Statement backendStatement = backendConnection.createStatement()) {
            backendStatement.execute("CREATE TABLE t_order_0 (order_id INT PRIMARY KEY, user_id INT)");
            backendStatement.execute("CREATE TABLE t_order_1 (order_id INT PRIMARY KEY, user_id INT)");
            backendStatement.execute("INSERT INTO t_order_0 VALUES (0, 8)");
            backendStatement.execute("INSERT INTO t_order_1 VALUES (1, 9)");
            ShardingRuleConfiguration rule = new ShardingRuleConfiguration();
            rule.getTables().add(new ShardingTableRuleConfiguration("t_order", "ds.t_order_${0..1}"));
            try (
                    ShardingSphereDataSource dataSource = (ShardingSphereDataSource) ShardingSphereDataSourceFactory.createDataSource("foo_db", new ModeConfiguration("Standalone", null),
                            Collections.singletonMap("ds", backend), Collections.singleton(rule), new Properties())) {
                assertReusedPreparedStatementResult(dataSource, backendStatement);
            }
        }
    }
    
    private void assertReusedPreparedStatementResult(final ShardingSphereDataSource dataSource, final Statement backendStatement) throws SQLException {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM t_order")) {
            try (ResultSet actual = statement.executeQuery()) {
                assertThat(actual.findColumn("user_id"), is(2));
            }
            backendStatement.execute("ALTER TABLE t_order_0 ADD add_test INT DEFAULT 99 BEFORE user_id");
            backendStatement.execute("ALTER TABLE t_order_1 ADD add_test INT DEFAULT 99 BEFORE user_id");
            try (ResultSet actual = statement.executeQuery()) {
                assertThat(actual.getMetaData().getColumnLabel(3), is("user_id"));
                assertThat(actual.findColumn("user_id"), is(3));
                assertThat(actual.findColumn("add_test"), is(2));
                assertTrue(actual.next());
                assertThat(actual.getInt(actual.findColumn("user_id")), is(actual.getInt("order_id") + 8));
                assertThat(actual.getInt("user_id"), is(actual.getInt("order_id") + 8));
                assertThat(actual.getInt("add_test"), is(99));
            }
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("derivedAliasQueries")
    void assertFindColumnWithGenuineDerivedAlias(final String name, final boolean schemaDrift, final String sql, final String expectedLabel,
                                                 final int expectedColumnCount, final int expectedValue) throws SQLException {
        JdbcDataSource backend = new JdbcDataSource();
        backend.setURL("jdbc:h2:mem:foo_alias_collision;MODE=MySQL;DATABASE_TO_UPPER=false");
        try (Connection backendConnection = backend.getConnection(); Statement backendStatement = backendConnection.createStatement()) {
            backendStatement.execute("CREATE TABLE t_order_0 (order_id INT PRIMARY KEY, user_id INT)");
            backendStatement.execute("INSERT INTO t_order_0 VALUES (0, 8)");
            ShardingRuleConfiguration rule = new ShardingRuleConfiguration();
            rule.getTables().add(new ShardingTableRuleConfiguration("t_order", "ds.t_order_0"));
            try (
                    ShardingSphereDataSource dataSource = (ShardingSphereDataSource) ShardingSphereDataSourceFactory.createDataSource("foo_db", new ModeConfiguration("Standalone", null),
                            Collections.singletonMap("ds", backend), Collections.singleton(rule), new Properties())) {
                if (schemaDrift) {
                    backendStatement.execute("ALTER TABLE t_order_0 ADD ORDER_BY_DERIVED_0 INT DEFAULT 99");
                }
                assertGenuineDerivedAliasResult(dataSource, sql, expectedLabel, expectedColumnCount, expectedValue);
            }
        }
    }
    
    private void assertGenuineDerivedAliasResult(final ShardingSphereDataSource dataSource, final String sql, final String expectedLabel,
                                                 final int expectedColumnCount, final int expectedValue) throws SQLException {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement(); ResultSet actual = statement.executeQuery(sql)) {
            ResultSetMetaData actualMetadata = actual.getMetaData();
            assertThat(actualMetadata.getColumnCount(), is(expectedColumnCount));
            assertThat(actualMetadata.getColumnLabel(expectedColumnCount), is(expectedLabel));
            assertThat(actualMetadata.getColumnName(expectedColumnCount), is(expectedLabel));
            assertThat(actual.findColumn(expectedLabel), is(expectedColumnCount));
            assertTrue(actual.next());
            assertThat(actual.getInt(expectedLabel), is(expectedValue));
        }
    }
    
    private static Collection<Arguments> derivedAliasQueries() {
        return Arrays.asList(
                Arguments.of("genuine explicit alias", false, "SELECT order_id AS ORDER_BY_DERIVED_9 FROM t_order", "ORDER_BY_DERIVED_9", 1, 0),
                Arguments.of("genuine aggregate alias", false, "SELECT COUNT(user_id) AS ORDER_BY_DERIVED_9 FROM t_order", "ORDER_BY_DERIVED_9", 1, 1),
                Arguments.of("genuine distinct aggregate alias", false, "SELECT COUNT(DISTINCT user_id) AS AGGREGATION_DISTINCT_DERIVED_0 FROM t_order",
                        "AGGREGATION_DISTINCT_DERIVED_0", 1, 1),
                Arguments.of("single route exact alias collision", true, "SELECT * FROM t_order ORDER BY user_id + 0", "ORDER_BY_DERIVED_0", 3, 99));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("aggregationAliasCollisionQueries")
    void assertFindColumnWithAggregationAliasCollision(final String name, final int routeCount, final String sql, final int expectedColumnIndex) throws SQLException {
        JdbcDataSource backend = new JdbcDataSource();
        backend.setURL("jdbc:h2:mem:foo_aggregation_alias_collision;MODE=MySQL;DATABASE_TO_UPPER=false");
        try (Connection backendConnection = backend.getConnection(); Statement backendStatement = backendConnection.createStatement()) {
            for (int index = 0; index < routeCount; index++) {
                backendStatement.execute("CREATE TABLE t_order_" + index + " (order_id INT PRIMARY KEY, user_id INT)");
                backendStatement.execute("INSERT INTO t_order_" + index + " VALUES (" + index + ", 8)");
            }
            ShardingRuleConfiguration rule = new ShardingRuleConfiguration();
            rule.getTables().add(new ShardingTableRuleConfiguration("t_order", "ds.t_order_${0.." + (routeCount - 1) + "}"));
            try (
                    ShardingSphereDataSource dataSource = (ShardingSphereDataSource) ShardingSphereDataSourceFactory.createDataSource("foo_db", new ModeConfiguration("Standalone", null),
                            Collections.singletonMap("ds", backend), Collections.singleton(rule), new Properties())) {
                addAggregationAliasCollisionColumn(backendStatement, routeCount);
                assertAggregationAliasCollisionResult(dataSource, sql, expectedColumnIndex);
            }
        }
    }
    
    private void addAggregationAliasCollisionColumn(final Statement backendStatement, final int routeCount) throws SQLException {
        for (int index = 0; index < routeCount; index++) {
            backendStatement.execute("ALTER TABLE t_order_" + index + " ADD AGGREGATION_DISTINCT_DERIVED_0 INT DEFAULT 99");
        }
    }
    
    private void assertAggregationAliasCollisionResult(final ShardingSphereDataSource dataSource, final String sql, final int expectedColumnIndex) throws SQLException {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement(); ResultSet actual = statement.executeQuery(sql)) {
            ResultSetMetaData actualMetadata = actual.getMetaData();
            assertThat(actualMetadata.getColumnCount(), is(4));
            assertThat(actualMetadata.getColumnLabel(expectedColumnIndex), is("AGGREGATION_DISTINCT_DERIVED_0"));
            assertThat(actualMetadata.getColumnName(expectedColumnIndex), is("AGGREGATION_DISTINCT_DERIVED_0"));
            assertThat(actual.findColumn("AGGREGATION_DISTINCT_DERIVED_0"), is(expectedColumnIndex));
            assertTrue(actual.next());
            assertThat(actual.getInt("AGGREGATION_DISTINCT_DERIVED_0"), is(99));
            assertThat(actual.getInt("COUNT(DISTINCT user_id)"), is(1));
        }
    }
    
    private static Collection<Arguments> aggregationAliasCollisionQueries() {
        return Arrays.asList(
                Arguments.of("single route count first", 1, "SELECT COUNT(DISTINCT user_id), t_order.* FROM t_order GROUP BY order_id", 4),
                Arguments.of("single route wildcard first", 1, "SELECT t_order.*, COUNT(DISTINCT user_id) FROM t_order GROUP BY order_id", 3),
                Arguments.of("multiple routes count first", 2, "SELECT COUNT(DISTINCT user_id), t_order.* FROM t_order GROUP BY order_id", 4));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("mixedAggregationAliasQueries")
    void assertMixedAggregationsWithGenuineAliases(final String name, final String sql, final String[] expectedLabels, final int[] expectedValues) throws SQLException {
        JdbcDataSource backend = new JdbcDataSource();
        backend.setURL("jdbc:h2:mem:foo_mixed_aggregation_aliases;MODE=MySQL;DATABASE_TO_UPPER=false");
        try (Connection backendConnection = backend.getConnection(); Statement backendStatement = backendConnection.createStatement()) {
            backendStatement.execute("CREATE TABLE t_order_0 (order_id INT PRIMARY KEY, user_id INT)");
            backendStatement.execute("CREATE TABLE t_order_1 (order_id INT PRIMARY KEY, user_id INT)");
            backendStatement.execute("INSERT INTO t_order_0 VALUES (2, 8)");
            backendStatement.execute("INSERT INTO t_order_1 VALUES (5, 8)");
            ShardingRuleConfiguration rule = new ShardingRuleConfiguration();
            rule.getTables().add(new ShardingTableRuleConfiguration("t_order", "ds.t_order_${0..1}"));
            try (
                    ShardingSphereDataSource dataSource = (ShardingSphereDataSource) ShardingSphereDataSourceFactory.createDataSource("foo_db", new ModeConfiguration("Standalone", null),
                            Collections.singletonMap("ds", backend), Collections.singleton(rule), new Properties())) {
                assertMixedAggregationResult(dataSource, sql, expectedLabels, expectedValues);
            }
        }
    }
    
    private void assertMixedAggregationResult(final ShardingSphereDataSource dataSource, final String sql, final String[] expectedLabels, final int[] expectedValues) throws SQLException {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement(); ResultSet actual = statement.executeQuery(sql)) {
            ResultSetMetaData actualMetadata = actual.getMetaData();
            assertThat(actualMetadata.getColumnCount(), is(expectedLabels.length));
            assertTrue(actual.next());
            for (int index = 0; index < expectedLabels.length; index++) {
                assertThat(actualMetadata.getColumnLabel(index + 1), is(expectedLabels[index]));
                assertThat(actualMetadata.getColumnName(index + 1), is(expectedLabels[index]));
                assertThat(actual.findColumn(expectedLabels[index]), is(Arrays.asList(expectedLabels).indexOf(expectedLabels[index]) + 1));
                assertThat(actual.getInt(index + 1), is(expectedValues[index]));
                assertThat(actual.getInt(expectedLabels[index]), is(expectedValues[index]));
            }
            assertFalse(actual.next());
        }
    }
    
    private static Collection<Arguments> mixedAggregationAliasQueries() {
        return Arrays.asList(
                Arguments.of("explicit distinct alias first", "SELECT COUNT(DISTINCT user_id) AS AGGREGATION_DISTINCT_DERIVED_0, SUM(DISTINCT order_id) FROM t_order",
                        new String[]{"AGGREGATION_DISTINCT_DERIVED_0", "SUM(DISTINCT order_id)"}, new int[]{1, 7}),
                Arguments.of("folded distinct alias last", "SELECT COUNT(DISTINCT user_id), SUM(DISTINCT order_id) AS aggregation_distinct_derived_0 FROM t_order",
                        new String[]{"COUNT(DISTINCT user_id)", "aggregation_distinct_derived_0"}, new int[]{1, 7}),
                Arguments.of("mixed average alias collision", "SELECT COUNT(DISTINCT user_id) AS AVG_DERIVED_SUM_0, SUM(DISTINCT order_id), AVG(DISTINCT user_id) FROM t_order",
                        new String[]{"AVG_DERIVED_SUM_0", "SUM(DISTINCT order_id)", "AVG(DISTINCT user_id)"}, new int[]{1, 7, 8}),
                Arguments.of("ordinary average alias collision", "SELECT COUNT(user_id) AS AVG_DERIVED_SUM_0, AVG(user_id) FROM t_order",
                        new String[]{"AVG_DERIVED_SUM_0", "AVG(user_id)"}, new int[]{2, 8}),
                Arguments.of("repeated distinct expressions", "SELECT COUNT(DISTINCT user_id), COUNT(DISTINCT user_id), SUM(DISTINCT order_id), AVG(DISTINCT user_id) FROM t_order",
                        new String[]{"COUNT(DISTINCT user_id)", "COUNT(DISTINCT user_id)", "SUM(DISTINCT order_id)", "AVG(DISTINCT user_id)"}, new int[]{1, 1, 7, 8}));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("knownAggregationAliasCollisionQueries")
    void assertFindColumnWithKnownAggregationAliasCollision(final String name, final boolean schemaDrift) throws SQLException {
        JdbcDataSource backend = new JdbcDataSource();
        backend.setURL("jdbc:h2:mem:foo_known_aggregation_alias_collision;MODE=MySQL;DATABASE_TO_UPPER=false");
        try (Connection backendConnection = backend.getConnection(); Statement backendStatement = backendConnection.createStatement()) {
            backendStatement.execute("CREATE TABLE t_order_0 (order_id INT PRIMARY KEY, user_id INT, AGGREGATION_DISTINCT_DERIVED_0 INT DEFAULT 99)");
            backendStatement.execute("CREATE TABLE t_order_1 (order_id INT PRIMARY KEY, user_id INT, AGGREGATION_DISTINCT_DERIVED_0 INT DEFAULT 99)");
            backendStatement.execute("INSERT INTO t_order_0(order_id, user_id) VALUES (0, 8)");
            backendStatement.execute("INSERT INTO t_order_1(order_id, user_id) VALUES (1, 8)");
            ShardingRuleConfiguration rule = new ShardingRuleConfiguration();
            rule.getTables().add(new ShardingTableRuleConfiguration("t_order", "ds.t_order_${0..1}"));
            try (
                    ShardingSphereDataSource dataSource = (ShardingSphereDataSource) ShardingSphereDataSourceFactory.createDataSource("foo_db", new ModeConfiguration("Standalone", null),
                            Collections.singletonMap("ds", backend), Collections.singleton(rule), new Properties())) {
                if (schemaDrift) {
                    backendStatement.execute("ALTER TABLE t_order_0 ADD add_test INT DEFAULT 42");
                    backendStatement.execute("ALTER TABLE t_order_1 ADD add_test INT DEFAULT 42");
                }
                assertKnownAggregationAliasCollisionResult(dataSource, schemaDrift);
            }
        }
    }
    
    private void assertKnownAggregationAliasCollisionResult(final ShardingSphereDataSource dataSource, final boolean schemaDrift) throws SQLException {
        try (
                Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
                ResultSet actual = statement.executeQuery("SELECT AGGREGATION_DISTINCT_DERIVED_0, COUNT(DISTINCT user_id), t_order.* FROM t_order GROUP BY order_id")) {
            ResultSetMetaData actualMetadata = actual.getMetaData();
            assertThat(actualMetadata.getColumnCount(), is(schemaDrift ? 6 : 5));
            assertThat(actualMetadata.getColumnLabel(1), is("AGGREGATION_DISTINCT_DERIVED_0"));
            assertThat(actualMetadata.getColumnName(1), is("AGGREGATION_DISTINCT_DERIVED_0"));
            assertThat(actualMetadata.getColumnLabel(2), is("COUNT(DISTINCT user_id)"));
            assertThat(actualMetadata.getColumnName(2), is("COUNT(DISTINCT user_id)"));
            assertThat(actual.findColumn("AGGREGATION_DISTINCT_DERIVED_0"), is(1));
            assertThat(actual.findColumn("COUNT(DISTINCT user_id)"), is(2));
            assertTrue(actual.next());
            assertThat(actual.getInt("AGGREGATION_DISTINCT_DERIVED_0"), is(99));
            assertThat(actual.getInt("COUNT(DISTINCT user_id)"), is(1));
            if (schemaDrift) {
                assertThat(actual.getInt("add_test"), is(42));
            }
        }
    }
    
    private static Collection<Arguments> knownAggregationAliasCollisionQueries() {
        return Arrays.asList(Arguments.of("known real column without drift", false), Arguments.of("known real column with drift", true));
    }
    
    @BeforeEach
    void setUp() throws SQLException {
        mergeResultSet = mock(MergedResult.class);
        shardingSphereResultSet = new ShardingSphereResultSet(getResultSets(), mergeResultSet, getShardingSphereStatement(), createSQLStatementContext());
    }
    
    private SQLStatementContext createSQLStatementContext() {
        SQLStatementContext result = mock(SQLStatementContext.class);
        TablesContext tablesContext = mock(TablesContext.class);
        when(tablesContext.getTableNames()).thenReturn(Collections.emptyList());
        when(result.getTablesContext()).thenReturn(tablesContext);
        return result;
    }
    
    private List<ResultSet> getResultSets() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("label");
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        return Collections.singletonList(resultSet);
    }
    
    private ShardingSphereStatement getShardingSphereStatement() {
        connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        when(connection.getCurrentDatabaseName()).thenReturn("logic_db");
        protocolType = mock(DatabaseType.class);
        when(protocolType.getType()).thenReturn("MySQL");
        when(protocolType.getTrunkDatabaseType()).thenReturn(Optional.empty());
        when(metaDataContexts.getMetaData().getDatabase("logic_db").getProtocolType()).thenReturn(protocolType);
        when(connection.getContextManager().getMetaDataContexts()).thenReturn(metaDataContexts);
        statement = mock(ShardingSphereStatement.class);
        when(statement.getConnection()).thenReturn(connection);
        when(statement.getUsedDatabaseName()).thenReturn("logic_db");
        return statement;
    }
    
    @Test
    void assertNext() throws SQLException {
        when(mergeResultSet.next()).thenReturn(true);
        assertTrue(shardingSphereResultSet.next());
    }
    
    @Test
    void assertWasNull() throws SQLException {
        assertFalse(shardingSphereResultSet.wasNull());
    }
    
    @Test
    void assertGetBooleanWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, boolean.class)).thenReturn(true);
        assertTrue(shardingSphereResultSet.getBoolean(1));
    }
    
    @Test
    void assertGetBooleanWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, boolean.class)).thenReturn(true);
        assertTrue(shardingSphereResultSet.getBoolean("label"));
    }
    
    @Test
    void assertGetBooleanWithColumnLabelCaseInsensitive() throws SQLException {
        when(mergeResultSet.getValue(1, boolean.class)).thenReturn(true);
        assertTrue(shardingSphereResultSet.getBoolean("lABel"));
    }
    
    @Test
    void assertGetByteWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, byte.class)).thenReturn((byte) 1);
        assertThat(shardingSphereResultSet.getByte(1), is((byte) 1));
    }
    
    @Test
    void assertGetByteWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, byte.class)).thenReturn((byte) 1);
        assertThat(shardingSphereResultSet.getByte("label"), is((byte) 1));
    }
    
    @Test
    void assertGetShortWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, short.class)).thenReturn((short) 1);
        assertThat(shardingSphereResultSet.getShort(1), is((short) 1));
    }
    
    @Test
    void assertGetShortWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, short.class)).thenReturn((short) 1);
        assertThat(shardingSphereResultSet.getShort("label"), is((short) 1));
    }
    
    @Test
    void assertGetIntWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, int.class)).thenReturn(1);
        assertThat(shardingSphereResultSet.getInt(1), is(1));
    }
    
    @Test
    void assertGetIntWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, int.class)).thenReturn((short) 1);
        assertThat(shardingSphereResultSet.getInt("label"), is(1));
    }
    
    @Test
    void assertGetLongWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, long.class)).thenReturn(1L);
        assertThat(shardingSphereResultSet.getLong(1), is(1L));
    }
    
    @Test
    void assertGetLongWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, long.class)).thenReturn(1L);
        assertThat(shardingSphereResultSet.getLong("label"), is(1L));
    }
    
    @Test
    void assertGetFloatWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, float.class)).thenReturn(1.0F);
        assertThat(shardingSphereResultSet.getFloat(1), is(1.0F));
    }
    
    @Test
    void assertGetFloatWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, float.class)).thenReturn(1.0F);
        assertThat(shardingSphereResultSet.getFloat("label"), is(1.0F));
    }
    
    @Test
    void assertGetDoubleWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, double.class)).thenReturn(1.0D);
        assertThat(shardingSphereResultSet.getDouble(1), is(1.0D));
    }
    
    @Test
    void assertGetDoubleWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, double.class)).thenReturn(1.0D);
        assertThat(shardingSphereResultSet.getDouble("label"), is(1.0D));
    }
    
    @Test
    void assertGetStringWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, String.class)).thenReturn("value");
        LocalDateTime tempTime = LocalDateTime.of(2022, 12, 14, 0, 0);
        when(mergeResultSet.getValue(2, String.class)).thenReturn(tempTime);
        when(mergeResultSet.getValue(3, String.class)).thenReturn(Timestamp.valueOf(tempTime));
        assertThat(shardingSphereResultSet.getString(1), is("value"));
        assertThat(shardingSphereResultSet.getString(2), is("2022-12-14T00:00"));
        assertThat(shardingSphereResultSet.getString(3), is("2022-12-14 00:00:00.0"));
    }
    
    @Test
    void assertGetStringWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, String.class)).thenReturn("value");
        assertThat(shardingSphereResultSet.getString("label"), is("value"));
    }
    
    @Test
    void assertGetNStringWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, String.class)).thenReturn("value");
        assertThat(shardingSphereResultSet.getNString(1), is("value"));
    }
    
    @Test
    void assertGetNStringWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, String.class)).thenReturn("value");
        assertThat(shardingSphereResultSet.getNString("label"), is("value"));
    }
    
    @Test
    void assertGetBigDecimalWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, BigDecimal.class)).thenReturn(new BigDecimal("1"));
        assertThat(shardingSphereResultSet.getBigDecimal(1), is(new BigDecimal("1")));
    }
    
    @Test
    void assertGetBigDecimalWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, BigDecimal.class)).thenReturn(new BigDecimal("1"));
        assertThat(shardingSphereResultSet.getBigDecimal("label"), is(new BigDecimal("1")));
    }
    
    @Test
    void assertGetBigDecimalAndScaleWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, BigDecimal.class)).thenReturn(new BigDecimal("1"));
        assertThat(shardingSphereResultSet.getBigDecimal(1, 10), is(new BigDecimal("1")));
    }
    
    @Test
    void assertGetBigDecimalAndScaleWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, BigDecimal.class)).thenReturn(new BigDecimal("1"));
        assertThat(shardingSphereResultSet.getBigDecimal("label", 10), is(new BigDecimal("1")));
    }
    
    @Test
    void assertGetBytesWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, byte[].class)).thenReturn(new byte[]{(byte) 1});
        assertThat(shardingSphereResultSet.getBytes(1), is(new byte[]{(byte) 1}));
    }
    
    @Test
    void assertGetBytesWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, byte[].class)).thenReturn(new byte[]{(byte) 1});
        assertThat(shardingSphereResultSet.getBytes("label"), is(new byte[]{(byte) 1}));
    }
    
    @Test
    void assertGetBytesWithStringValue() throws SQLException {
        when(mergeResultSet.getValue(1, byte[].class)).thenReturn("张三");
        assertThat(shardingSphereResultSet.getBytes(1), is("张三".getBytes(StandardCharsets.UTF_8)));
    }
    
    @Test
    void assertGetDateWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, Date.class)).thenReturn(new Date(0L));
        assertThat(shardingSphereResultSet.getDate(1), is(new Date(0L)));
    }
    
    @Test
    void assertGetDateConvertedByLocalDateWithColumnIndex() throws SQLException {
        LocalDate localDate = LocalDate.now();
        when(mergeResultSet.getValue(1, Date.class)).thenReturn(localDate);
        assertThat(shardingSphereResultSet.getDate(1), is(Date.valueOf(localDate)));
    }
    
    @Test
    void assertGetDateWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, Date.class)).thenReturn(new Date(0L));
        assertThat(shardingSphereResultSet.getDate("label"), is(new Date(0L)));
    }
    
    @Test
    void assertGetDateAndCalendarWithColumnIndex() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(mergeResultSet.getCalendarValue(1, Date.class, calendar)).thenReturn(new Date(0L));
        assertThat(shardingSphereResultSet.getDate(1, calendar), is(new Date(0L)));
    }
    
    @Test
    void assertGetDateAndCalendarWithColumnLabel() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(mergeResultSet.getCalendarValue(1, Date.class, calendar)).thenReturn(new Date(0L));
        assertThat(shardingSphereResultSet.getDate("label", calendar), is(new Date(0L)));
    }
    
    @Test
    void assertGetTimeWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, Time.class)).thenReturn(new Time(0L));
        assertThat(shardingSphereResultSet.getTime(1), is(new Time(0L)));
    }
    
    @Test
    void assertGetTimeWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, Time.class)).thenReturn(new Time(0L));
        assertThat(shardingSphereResultSet.getTime("label"), is(new Time(0L)));
    }
    
    @Test
    void assertGetTimeAndCalendarWithColumnIndex() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(mergeResultSet.getCalendarValue(1, Time.class, calendar)).thenReturn(new Time(0L));
        assertThat(shardingSphereResultSet.getTime(1, calendar), is(new Time(0L)));
    }
    
    @Test
    void assertGetTimeAndCalendarWithColumnLabel() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(mergeResultSet.getCalendarValue(1, Time.class, calendar)).thenReturn(new Time(0L));
        assertThat(shardingSphereResultSet.getTime("label", calendar), is(new Time(0L)));
    }
    
    @Test
    void assertGetTimestampWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, Timestamp.class)).thenReturn(new Timestamp(0L));
        assertThat(shardingSphereResultSet.getTimestamp(1), is(new Timestamp(0L)));
    }
    
    @Test
    void assertGetTimestampConvertedByLocalDateWithColumnIndex() throws SQLException {
        LocalDate localDate = LocalDate.now();
        when(mergeResultSet.getValue(1, Timestamp.class)).thenReturn(localDate);
        assertThat(shardingSphereResultSet.getTimestamp(1), is(Timestamp.valueOf(localDate.atStartOfDay())));
    }
    
    @Test
    void assertGetTimestampWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, Timestamp.class)).thenReturn(new Timestamp(0L));
        assertThat(shardingSphereResultSet.getTimestamp("label"), is(new Timestamp(0L)));
    }
    
    @Test
    void assertGetTimestampAndCalendarWithColumnIndex() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(mergeResultSet.getCalendarValue(1, Timestamp.class, calendar)).thenReturn(new Timestamp(0L));
        assertThat(shardingSphereResultSet.getTimestamp(1, calendar), is(new Timestamp(0L)));
    }
    
    @Test
    void assertGetTimestampAndCalendarWithColumnLabel() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(mergeResultSet.getCalendarValue(1, Timestamp.class, calendar)).thenReturn(new Timestamp(0L));
        assertThat(shardingSphereResultSet.getTimestamp("label", calendar), is(new Timestamp(0L)));
    }
    
    @Test
    void assertGetAsciiStreamWithColumnIndex() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        when(mergeResultSet.getInputStream(1, "Ascii")).thenReturn(inputStream);
        assertThat(shardingSphereResultSet.getAsciiStream(1), isA(InputStream.class));
    }
    
    @Test
    void assertGetAsciiStreamWithColumnLabel() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        when(mergeResultSet.getInputStream(1, "Ascii")).thenReturn(inputStream);
        assertThat(shardingSphereResultSet.getAsciiStream("label"), isA(InputStream.class));
    }
    
    @Test
    void assertGetUnicodeStreamWithColumnIndex() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        when(mergeResultSet.getInputStream(1, "Unicode")).thenReturn(inputStream);
        assertThat(shardingSphereResultSet.getUnicodeStream(1), isA(InputStream.class));
    }
    
    @Test
    void assertGetUnicodeStreamWithColumnLabel() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        when(mergeResultSet.getInputStream(1, "Unicode")).thenReturn(inputStream);
        assertThat(shardingSphereResultSet.getUnicodeStream("label"), isA(InputStream.class));
    }
    
    @Test
    void assertGetBinaryStreamWithColumnIndex() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        when(mergeResultSet.getInputStream(1, "Binary")).thenReturn(inputStream);
        assertThat(shardingSphereResultSet.getBinaryStream(1), isA(InputStream.class));
    }
    
    @Test
    void assertGetBinaryStreamWithColumnLabel() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        when(mergeResultSet.getInputStream(1, "Binary")).thenReturn(inputStream);
        assertThat(shardingSphereResultSet.getBinaryStream("label"), isA(InputStream.class));
    }
    
    @Test
    void assertGetCharacterStreamWithColumnIndex() throws SQLException {
        Reader reader = mock(Reader.class);
        when(mergeResultSet.getCharacterStream(1)).thenReturn(reader);
        assertThat(shardingSphereResultSet.getCharacterStream(1), is(reader));
    }
    
    @Test
    void assertGetCharacterStreamWithColumnLabel() throws SQLException {
        Reader reader = mock(Reader.class);
        when(mergeResultSet.getCharacterStream(1)).thenReturn(reader);
        assertThat(shardingSphereResultSet.getCharacterStream("label"), is(reader));
    }
    
    @Test
    void assertGetBlobWithColumnIndex() throws SQLException {
        Blob blob = mock(Blob.class);
        when(mergeResultSet.getValue(1, Blob.class)).thenReturn(blob);
        assertThat(shardingSphereResultSet.getBlob(1), is(blob));
    }
    
    @Test
    void assertGetBlobWithColumnLabel() throws SQLException {
        Blob blob = mock(Blob.class);
        when(mergeResultSet.getValue(1, Blob.class)).thenReturn(blob);
        assertThat(shardingSphereResultSet.getBlob("label"), is(blob));
    }
    
    @Test
    void assertGetClobWithColumnIndex() throws SQLException {
        Clob clob = mock(Clob.class);
        when(mergeResultSet.getValue(1, Clob.class)).thenReturn(clob);
        assertThat(shardingSphereResultSet.getClob(1), is(clob));
    }
    
    @Test
    void assertGetClobWithColumnLabel() throws SQLException {
        Clob clob = mock(Clob.class);
        when(mergeResultSet.getValue(1, Clob.class)).thenReturn(clob);
        assertThat(shardingSphereResultSet.getClob("label"), is(clob));
    }
    
    @Test
    void assertGetArrayWithColumnIndex() throws SQLException {
        Array array = mock(Array.class);
        when(mergeResultSet.getValue(1, Array.class)).thenReturn(array);
        assertThat(shardingSphereResultSet.getArray(1), is(array));
    }
    
    @Test
    void assertGetArrayWithColumnLabel() throws SQLException {
        Array array = mock(Array.class);
        when(mergeResultSet.getValue(1, Array.class)).thenReturn(array);
        assertThat(shardingSphereResultSet.getArray("label"), is(array));
    }
    
    @Test
    void assertGetURLWithColumnIndex() throws SQLException, MalformedURLException {
        when(mergeResultSet.getValue(1, URL.class)).thenReturn(new URL("http://xxx.xxx"));
        assertThat(shardingSphereResultSet.getURL(1), is(new URL("http://xxx.xxx")));
    }
    
    @Test
    void assertGetURLWithColumnLabel() throws SQLException, MalformedURLException {
        when(mergeResultSet.getValue(1, URL.class)).thenReturn(new URL("http://xxx.xxx"));
        assertThat(shardingSphereResultSet.getURL("label"), is(new URL("http://xxx.xxx")));
    }
    
    @Test
    void assertGetSQLXMLWithColumnIndex() throws SQLException {
        SQLXML sqlxml = mock(SQLXML.class);
        when(mergeResultSet.getValue(1, SQLXML.class)).thenReturn(sqlxml);
        assertThat(shardingSphereResultSet.getSQLXML(1), is(sqlxml));
    }
    
    @Test
    void assertGetSQLXMLWithColumnLabel() throws SQLException {
        SQLXML sqlxml = mock(SQLXML.class);
        when(mergeResultSet.getValue(1, SQLXML.class)).thenReturn(sqlxml);
        assertThat(shardingSphereResultSet.getSQLXML("label"), is(sqlxml));
    }
    
    @Test
    void assertGetObjectWithColumnIndex() throws SQLException {
        when(mergeResultSet.getValue(1, Object.class)).thenReturn("object_value");
        assertThat(shardingSphereResultSet.getObject(1), is("object_value"));
    }
    
    @Test
    void assertGetObjectWithColumnLabel() throws SQLException {
        when(mergeResultSet.getValue(1, Object.class)).thenReturn("object_value");
        assertThat(shardingSphereResultSet.getObject("label"), is("object_value"));
    }
    
    @Test
    void assertGetObjectWithString() throws SQLException {
        String result = "foo";
        when(mergeResultSet.getValue(1, String.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, String.class), is(result));
    }
    
    @Test
    void assertGetObjectWithBoolean() throws SQLException {
        boolean result = true;
        when(mergeResultSet.getValue(1, boolean.class)).thenReturn(result);
        assertTrue(shardingSphereResultSet.getObject(1, boolean.class));
        when(mergeResultSet.getValue(1, Boolean.class)).thenReturn(result);
        assertTrue(shardingSphereResultSet.getObject(1, Boolean.class));
    }
    
    @Test
    void assertGetObjectWithByte() throws SQLException {
        Byte result = (byte) 1;
        when(mergeResultSet.getValue(1, byte.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, byte.class), is(result));
    }
    
    @Test
    void assertGetObjectWithByteArray() throws SQLException {
        byte[] result = new byte[0];
        when(mergeResultSet.getValue(1, byte[].class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, byte[].class), is(result));
    }
    
    @Test
    void assertGetObjectWithBigDecimal() throws SQLException {
        BigDecimal result = new BigDecimal("0");
        when(mergeResultSet.getValue(1, BigDecimal.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, BigDecimal.class), is(result));
    }
    
    @Test
    void assertGetObjectWithBigInteger() throws SQLException {
        BigInteger result = BigInteger.valueOf(0L);
        when(mergeResultSet.getValue(1, BigInteger.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, BigInteger.class), is(result));
    }
    
    @Test
    void assertGetObjectWithDouble() throws SQLException {
        double result = 0.0;
        when(mergeResultSet.getValue(1, double.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, double.class), is(result));
        when(mergeResultSet.getValue(1, Double.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, Double.class), is(result));
    }
    
    @Test
    void assertGetObjectWithFloat() throws SQLException {
        float result = 0.0F;
        when(mergeResultSet.getValue(1, float.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, float.class), is(result));
        when(mergeResultSet.getValue(1, Float.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, Float.class), is(result));
    }
    
    @Test
    void assertGetObjectWithInteger() throws SQLException {
        int result = 0;
        when(mergeResultSet.getValue(1, int.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, int.class), is(result));
        when(mergeResultSet.getValue(1, Integer.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, Integer.class), is(result));
    }
    
    @Test
    void assertGetObjectWithIntegerFromStringInMySQLProtocol() throws SQLException {
        when(mergeResultSet.getValue(1, Integer.class)).thenReturn("123");
        assertThat(shardingSphereResultSet.getObject(1, Integer.class), is(123));
    }
    
    @Test
    void assertGetObjectWithIntegerFromStringInNonMySQLProtocol() throws SQLException {
        when(mergeResultSet.getValue(1, Integer.class)).thenReturn("123");
        DatabaseType actualProtocolType = mock(DatabaseType.class);
        when(actualProtocolType.getType()).thenReturn("PostgreSQL");
        when(actualProtocolType.getTrunkDatabaseType()).thenReturn(Optional.empty());
        when(metaDataContexts.getMetaData().getDatabase("logic_db").getProtocolType()).thenReturn(actualProtocolType);
        ShardingSphereResultSet actualResultSet = new ShardingSphereResultSet(getResultSets(), mergeResultSet, statement, createSQLStatementContext());
        assertThrows(SQLException.class, () -> actualResultSet.getObject(1, Integer.class));
    }
    
    @Test
    void assertGetObjectWithIntegerFromStringInTrunkMySQLProtocol() throws SQLException {
        when(mergeResultSet.getValue(1, Integer.class)).thenReturn("123");
        DatabaseType trunkDatabaseType = mock(DatabaseType.class);
        when(trunkDatabaseType.getType()).thenReturn("MySQL");
        when(trunkDatabaseType.getTrunkDatabaseType()).thenReturn(Optional.empty());
        DatabaseType actualProtocolType = mock(DatabaseType.class);
        when(actualProtocolType.getType()).thenReturn("MariaDB");
        when(actualProtocolType.getTrunkDatabaseType()).thenReturn(Optional.of(trunkDatabaseType));
        when(metaDataContexts.getMetaData().getDatabase("logic_db").getProtocolType()).thenReturn(actualProtocolType);
        ShardingSphereResultSet actualResultSet = new ShardingSphereResultSet(getResultSets(), mergeResultSet, statement, createSQLStatementContext());
        assertThrows(SQLException.class, () -> actualResultSet.getObject(1, Integer.class));
    }
    
    @Test
    void assertGetObjectWithIntegerFromStringUsesUsedDatabaseProtocolForStatement() throws SQLException {
        ShardingSphereConnection statementConnection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        MetaDataContexts statementMetaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(statementMetaDataContexts.getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        DatabaseType currentDatabaseProtocolType = mock(DatabaseType.class);
        when(currentDatabaseProtocolType.getType()).thenReturn("PostgreSQL");
        when(currentDatabaseProtocolType.getTrunkDatabaseType()).thenReturn(Optional.empty());
        DatabaseType usedDatabaseProtocolType = mock(DatabaseType.class);
        when(usedDatabaseProtocolType.getType()).thenReturn("MySQL");
        when(usedDatabaseProtocolType.getTrunkDatabaseType()).thenReturn(Optional.empty());
        ShardingSphereStatement usedDatabaseStatement = mock(ShardingSphereStatement.class);
        when(statementConnection.getCurrentDatabaseName()).thenReturn("current_db");
        when(statementConnection.getContextManager().getMetaDataContexts()).thenReturn(statementMetaDataContexts);
        when(statementMetaDataContexts.getMetaData().getDatabase("current_db").getProtocolType()).thenReturn(currentDatabaseProtocolType);
        when(statementMetaDataContexts.getMetaData().getDatabase("used_db").getProtocolType()).thenReturn(usedDatabaseProtocolType);
        when(usedDatabaseStatement.getConnection()).thenReturn(statementConnection);
        when(usedDatabaseStatement.getUsedDatabaseName()).thenReturn("used_db");
        ShardingSphereResultSet usedDatabaseResultSet = new ShardingSphereResultSet(getResultSets(), mergeResultSet, usedDatabaseStatement, createSQLStatementContext());
        when(mergeResultSet.getValue(1, Integer.class)).thenReturn("123");
        assertThat(usedDatabaseResultSet.getObject(1, Integer.class), is(123));
    }
    
    @Test
    void assertGetObjectWithIntegerFromStringUsesUsedDatabaseProtocolForPreparedStatement() throws SQLException {
        ShardingSpherePreparedStatement preparedStatement = mock(ShardingSpherePreparedStatement.class);
        ShardingSphereDatabase usedDatabase = mock(ShardingSphereDatabase.class);
        DatabaseType usedDatabaseProtocolType = mock(DatabaseType.class);
        when(usedDatabaseProtocolType.getType()).thenReturn("MySQL");
        when(usedDatabaseProtocolType.getTrunkDatabaseType()).thenReturn(Optional.empty());
        when(usedDatabase.getProtocolType()).thenReturn(usedDatabaseProtocolType);
        when(preparedStatement.getUsedDatabase()).thenReturn(usedDatabase);
        ShardingSphereResultSet preparedResultSet = new ShardingSphereResultSet(getResultSets(), mergeResultSet, preparedStatement, createSQLStatementContext());
        when(mergeResultSet.getValue(1, Integer.class)).thenReturn("123");
        assertThat(preparedResultSet.getObject(1, Integer.class), is(123));
    }
    
    @Test
    void assertGetObjectWithLong() throws SQLException {
        long result = 0L;
        when(mergeResultSet.getValue(1, long.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, long.class), is(result));
        when(mergeResultSet.getValue(1, Long.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, Long.class), is(result));
    }
    
    @Test
    void assertGetObjectWithShort() throws SQLException {
        short result = 0;
        when(mergeResultSet.getValue(1, short.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, short.class), is(result));
        when(mergeResultSet.getValue(1, Short.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, Short.class), is(result));
    }
    
    @Test
    void assertGetObjectWithDate() throws SQLException {
        Date result = mock(Date.class);
        when(mergeResultSet.getValue(1, Date.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, Date.class), is(result));
    }
    
    @Test
    void assertGetObjectWithTime() throws SQLException {
        Time result = mock(Time.class);
        when(mergeResultSet.getValue(1, Time.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, Time.class), is(result));
    }
    
    @Test
    void assertGetObjectWithTimestamp() throws SQLException {
        Timestamp result = mock(Timestamp.class);
        when(mergeResultSet.getValue(1, Timestamp.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, Timestamp.class), is(result));
    }
    
    @Test
    void assertGetObjectWithLocalDateTime() throws SQLException {
        LocalDateTime result = LocalDateTime.now();
        when(mergeResultSet.getValue(1, Timestamp.class)).thenReturn(Timestamp.valueOf(result));
        assertThat(shardingSphereResultSet.getObject(1, LocalDateTime.class), is(result));
    }
    
    @Test
    void assertGetObjectWithOffsetDateTime() throws SQLException {
        OffsetDateTime result = OffsetDateTime.now();
        when(mergeResultSet.getValue(1, Timestamp.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, OffsetDateTime.class), is(result));
    }
    
    @Test
    void assertGetObjectWithBlob() throws SQLException {
        Blob result = mock(Blob.class);
        when(mergeResultSet.getValue(1, Blob.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, Blob.class), is(result));
    }
    
    @Test
    void assertGetObjectWithClob() throws SQLException {
        Clob result = mock(Clob.class);
        when(mergeResultSet.getValue(1, Clob.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, Clob.class), is(result));
    }
    
    @Test
    void assertGetObjectWithRef() throws SQLException {
        Ref result = mock(Ref.class);
        when(mergeResultSet.getValue(1, Ref.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, Ref.class), is(result));
    }
    
    @Test
    void assertGetObjectWithURL() throws SQLException {
        URL result = mock(URL.class);
        when(mergeResultSet.getValue(1, URL.class)).thenReturn(result);
        assertThat(shardingSphereResultSet.getObject(1, URL.class), is(result));
    }
}
