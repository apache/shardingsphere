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

package org.apache.shardingsphere.driver.jdbc.core.statement;

import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.exception.RuleAndStorageMetaDataMismatchedException;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.database.exception.core.SQLExceptionTransformEngine;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.distsql.statement.type.rul.sql.ParseStatement;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.executor.engine.facade.DriverExecutorFacade;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.driver.jdbc.core.resultset.GeneratedKeysResultSet;
import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.sqlfederation.rule.builder.DefaultSQLFederationRuleConfigurationBuilder;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShardingSpherePreparedStatementTest {
    
    @ParameterizedTest
    @Tag("h2")
    @CsvSource(value = {"AVG(user_id)|AVG_DERIVED_COUNT_0", "AVG(user_id)|avg_derived_sum_0", "AVG(DISTINCT user_id)|AVG_DERIVED_COUNT_0"}, delimiter = '|')
    void assertAverageWithDerivedAliasCollision(final String aggregation, final String alias) throws SQLException {
        JdbcDataSource backend = new JdbcDataSource();
        backend.setURL("jdbc:h2:mem:foo_average_alias;MODE=MySQL;DATABASE_TO_UPPER=false");
        ShardingRuleConfiguration rule = new ShardingRuleConfiguration();
        rule.getTables().add(new ShardingTableRuleConfiguration("t_order", "ds.t_order_${0..1}"));
        try (
                Connection backendConnection = createBackendConnection(backend, "CREATE TABLE t_order_0 (order_id INT, user_id INT)", "CREATE TABLE t_order_1 (order_id INT, user_id INT)",
                        "INSERT INTO t_order_0 VALUES (0, 8)", "INSERT INTO t_order_1 VALUES (1, 10)");
                ShardingSphereDataSource dataSource = (ShardingSphereDataSource) ShardingSphereDataSourceFactory.createDataSource("foo_db", new ModeConfiguration("Standalone", null),
                        Collections.singletonMap("ds", backend), Collections.singleton(rule), new Properties());
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT " + aggregation + ", 100 AS " + alias + " FROM t_order");
                ResultSet actual = statement.executeQuery()) {
            assertThat(actual.getMetaData().getColumnCount(), is(2));
            assertThat(actual.getMetaData().getColumnLabel(2), is(alias));
            assertTrue(actual.next());
            assertThat(actual.getDouble(1), is(9D));
            assertThat(actual.getInt(alias), is(100));
            assertFalse(actual.next());
        }
    }
    
    private Connection createBackendConnection(final JdbcDataSource backend, final String... sqlStatements) throws SQLException {
        Connection result = backend.getConnection();
        boolean initialized = false;
        try {
            try (Statement statement = result.createStatement()) {
                for (String each : sqlStatements) {
                    statement.execute(each);
                }
            }
            initialized = true;
            return result;
        } finally {
            if (!initialized) {
                result.close();
            }
        }
    }
    
    @Test
    @Tag("h2")
    void assertDistinctAggregationWithExplicitDerivedAlias() throws SQLException {
        JdbcDataSource backend = new JdbcDataSource();
        backend.setURL("jdbc:h2:mem:foo_distinct_alias;MODE=MySQL;DATABASE_TO_UPPER=false");
        ShardingRuleConfiguration rule = new ShardingRuleConfiguration();
        rule.getTables().add(new ShardingTableRuleConfiguration("t_order", "ds.t_order_${0..1}"));
        try (
                Connection backendConnection = createBackendConnection(backend, "CREATE TABLE t_order_0 (order_id INT, user_id INT)", "CREATE TABLE t_order_1 (order_id INT, user_id INT)",
                        "INSERT INTO t_order_0 VALUES (0, 8)", "INSERT INTO t_order_1 VALUES (1, 10)");
                ShardingSphereDataSource dataSource = (ShardingSphereDataSource) ShardingSphereDataSourceFactory.createDataSource("foo_db", new ModeConfiguration("Standalone", null),
                        Collections.singletonMap("ds", backend), Collections.singleton(rule), new Properties());
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT COUNT(DISTINCT user_id) AS AGGREGATION_DISTINCT_DERIVED_0 FROM t_order");
                ResultSet actual = statement.executeQuery()) {
            assertThat(actual.getMetaData().getColumnCount(), is(1));
            assertThat(actual.getMetaData().getColumnLabel(1), is("AGGREGATION_DISTINCT_DERIVED_0"));
            assertTrue(actual.next());
            assertThat(actual.getInt("AGGREGATION_DISTINCT_DERIVED_0"), is(2));
            assertFalse(actual.next());
        }
    }
    
    @ParameterizedTest
    @Tag("h2")
    @ValueSource(strings = {"SELECT * FROM t_order", "SELECT * FROM (SELECT * FROM t_order) AS orders", "SELECT (SELECT user_id FROM t_order) AS user_id",
            "SELECT user_id FROM t_order UNION ALL SELECT user_id FROM t_order", "WITH orders AS (SELECT * FROM t_order) SELECT * FROM orders"})
    void assertExecuteQueryAfterMetadataRefresh(final String sql) throws SQLException {
        JdbcDataSource backend = new JdbcDataSource();
        backend.setURL("jdbc:h2:mem:foo_prepared_refresh;MODE=MySQL;DATABASE_TO_UPPER=false");
        ShardingRuleConfiguration rule = new ShardingRuleConfiguration();
        rule.getTables().add(new ShardingTableRuleConfiguration("t_order", "ds.t_order_0"));
        rule.getTables().add(new ShardingTableRuleConfiguration("t_other", "ds.t_other_0"));
        try (
                Connection backendConnection = createBackendConnection(backend, "CREATE TABLE t_order_0 (order_id INT PRIMARY KEY, user_id INT)", "INSERT INTO t_order_0 VALUES (0, 8)",
                        "CREATE TABLE t_other_0 (other_id INT)");
                Statement backendStatement = backendConnection.createStatement();
                ShardingSphereDataSource dataSource = (ShardingSphereDataSource) ShardingSphereDataSourceFactory.createDataSource("foo_db", new ModeConfiguration("Standalone", null),
                        Collections.singletonMap("ds", backend), Collections.singleton(rule), new Properties());
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            ContextManager contextManager = ((ShardingSphereConnection) connection).getContextManager();
            ShardingSphereDatabase database = contextManager.getDatabase("foo_db");
            contextManager.reloadTable(database, database.getDefaultSchemaName(), "t_order");
            backendStatement.execute("ALTER TABLE t_other_0 ADD unrelated_value INT");
            contextManager.reloadTable(database, database.getDefaultSchemaName(), "t_other");
            try (ResultSet actual = statement.executeQuery()) {
                int columnCount = actual.getMetaData().getColumnCount();
                backendStatement.execute("ALTER TABLE t_order_0 ADD added_value INT DEFAULT 99 BEFORE user_id");
                contextManager.reloadTable(database, database.getDefaultSchemaName(), "t_order");
                assertThat(actual.getMetaData().getColumnCount(), is(columnCount));
                assertTrue(actual.next());
                assertThat(actual.getInt("user_id"), is(8));
            }
            SQLException actual = assertThrows(SQLException.class, statement::executeQuery);
            assertThat(actual.getMessage(), is("Table metadata changed; prepare the statement again."));
            assertThat(actual.getSQLState(), is("HY000"));
            assertThat(actual.getErrorCode(), is(10025));
            try (PreparedStatement newStatement = connection.prepareStatement("SELECT * FROM t_order"); ResultSet actualResultSet = newStatement.executeQuery()) {
                assertThat(actualResultSet.getMetaData().getColumnCount(), is(3));
                assertTrue(actualResultSet.next());
                assertThat(actualResultSet.getInt("user_id"), is(8));
                assertThat(actualResultSet.getInt("added_value"), is(99));
            }
        }
    }
    
    @Test
    @Tag("h2")
    void assertRejectUpdatesAfterMetadataRefresh() throws SQLException {
        JdbcDataSource backend = new JdbcDataSource();
        backend.setURL("jdbc:h2:mem:foo_prepared_update_refresh;MODE=MySQL;DATABASE_TO_UPPER=false");
        ShardingRuleConfiguration rule = new ShardingRuleConfiguration();
        rule.getTables().add(new ShardingTableRuleConfiguration("t_order", "ds.t_order_0"));
        try (
                Connection backendConnection = createBackendConnection(backend, "CREATE TABLE t_order_0 (order_id INT PRIMARY KEY, user_id INT)");
                Statement backendStatement = backendConnection.createStatement();
                ShardingSphereDataSource dataSource = (ShardingSphereDataSource) ShardingSphereDataSourceFactory.createDataSource("foo_db", new ModeConfiguration("Standalone", null),
                        Collections.singletonMap("ds", backend), Collections.singleton(rule), new Properties());
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("INSERT INTO t_order (order_id, user_id) VALUES (?, ?)")) {
            statement.setInt(1, 0);
            statement.setInt(2, 8);
            statement.addBatch();
            backendStatement.execute("ALTER TABLE t_order_0 ADD added_value INT");
            ContextManager contextManager = ((ShardingSphereConnection) connection).getContextManager();
            ShardingSphereDatabase database = contextManager.getDatabase("foo_db");
            contextManager.reloadTable(database, database.getDefaultSchemaName(), "t_order");
            assertThat(assertThrows(SQLException.class, statement::executeUpdate).getErrorCode(), is(10025));
            assertThat(assertThrows(SQLException.class, statement::execute).getErrorCode(), is(10025));
            assertThat(assertThrows(SQLException.class, statement::executeBatch).getErrorCode(), is(10025));
            assertThat(assertThrows(SQLException.class, statement::addBatch).getErrorCode(), is(10025));
            try (ResultSet actual = backendStatement.executeQuery("SELECT COUNT(*) FROM t_order_0")) {
                assertTrue(actual.next());
                assertThat(actual.getInt(1), is(0));
            }
        }
    }
    
    @Test
    @Tag("h2")
    void assertRejectInconsistentMetadataRefresh() throws SQLException {
        JdbcDataSource backend = new JdbcDataSource();
        backend.setURL("jdbc:h2:mem:foo_strict_refresh;MODE=MySQL;DATABASE_TO_UPPER=false");
        ShardingRuleConfiguration rule = new ShardingRuleConfiguration();
        rule.getTables().add(new ShardingTableRuleConfiguration("t_order", "ds.t_order_${0..1}"));
        Properties props = PropertiesBuilder.build(new Property("check-table-metadata-enabled", "false"));
        try (
                Connection backendConnection = createBackendConnection(backend, "CREATE TABLE t_order_0 (order_id INT, user_id INT)", "CREATE TABLE t_order_1 (order_id INT, user_id INT)",
                        "INSERT INTO t_order_0 VALUES (0, 8)", "INSERT INTO t_order_1 VALUES (1, 9)");
                Statement backendStatement = backendConnection.createStatement();
                ShardingSphereDataSource dataSource = (ShardingSphereDataSource) ShardingSphereDataSourceFactory.createDataSource("foo_db", new ModeConfiguration("Standalone", null),
                        Collections.singletonMap("ds", backend), Collections.singleton(rule), props);
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT user_id FROM t_order ORDER BY user_id")) {
            ContextManager contextManager = ((ShardingSphereConnection) connection).getContextManager();
            ShardingSphereDatabase database = contextManager.getDatabase("foo_db");
            ShardingSphereTable table = database.getSchema(database.getDefaultSchemaName()).getTable("t_order");
            backendStatement.execute("ALTER TABLE t_order_1 ADD added_value INT DEFAULT 99 BEFORE user_id");
            assertThrows(RuleAndStorageMetaDataMismatchedException.class, () -> contextManager.reloadTable(database, database.getDefaultSchemaName(), "ds", "t_order"));
            assertThat(database.getSchema(database.getDefaultSchemaName()).getTable("t_order"), sameInstance(table));
            assertFalse(table.isBindingInvalidated());
            try (ResultSet actual = statement.executeQuery()) {
                assertTrue(actual.next());
                assertThat(actual.getInt(1), is(8));
                assertTrue(actual.next());
                assertThat(actual.getInt(1), is(9));
            }
            backendStatement.execute("ALTER TABLE t_order_0 ADD added_value INT DEFAULT 99 BEFORE user_id");
            contextManager.reloadTable(database, database.getDefaultSchemaName(), "ds", "t_order");
            assertThat(assertThrows(SQLException.class, statement::executeQuery).getErrorCode(), is(10025));
            try (PreparedStatement newStatement = connection.prepareStatement("SELECT * FROM t_order ORDER BY user_id"); ResultSet actual = newStatement.executeQuery()) {
                assertThat(actual.getMetaData().getColumnCount(), is(3));
                assertTrue(actual.next());
                assertThat(actual.getInt("user_id"), is(8));
                assertThat(actual.getInt("added_value"), is(99));
                assertTrue(actual.next());
                assertThat(actual.getInt("user_id"), is(9));
                assertThat(actual.getInt("added_value"), is(99));
            }
        }
    }
    
    @Test
    void assertCreatePreparedStatementAfterDatabaseRemoved() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        ShardingSphereMetaData metaData = createMetaData(databaseType);
        ShardingSphereConnection connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        when(connection.getContextManager().getMetaDataContexts().getMetaData()).thenReturn(metaData);
        when(connection.getCurrentDatabaseName()).thenReturn("foo_db");
        when(metaData.getProtocolType()).thenReturn(databaseType);
        when(metaData.getDatabase("foo_db")).thenReturn(null);
        SQLException expected = new SQLException("Unknown database 'foo_db'", "42000", 1049);
        try (MockedStatic<SQLExceptionTransformEngine> mockedEngine = mockStatic(SQLExceptionTransformEngine.class, CALLS_REAL_METHODS)) {
            mockedEngine.when(() -> SQLExceptionTransformEngine.toSQLException(
                    argThat(ex -> ex instanceof UnknownDatabaseException && "foo_db".equals(((UnknownDatabaseException) ex).getDatabaseName())), same(databaseType))).thenReturn(expected);
            SQLException actual = assertThrows(SQLException.class, () -> new ShardingSpherePreparedStatement(connection, "SELECT 1"));
            assertThat(actual, sameInstance(expected));
            assertThat(actual.getMessage(), is("Unknown database 'foo_db'"));
            assertThat(actual.getSQLState(), is("42000"));
            assertThat(actual.getErrorCode(), is(1049));
        }
        verify(connection, never()).registerStatementManager(any());
    }
    
    @Test
    void assertCreatePreparedStatementWhenDatabaseRemovedDuringParsing() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        ShardingSphereMetaData metaData = createMetaData(databaseType);
        ShardingSphereConnection connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        when(connection.getContextManager().getMetaDataContexts().getMetaData()).thenReturn(metaData);
        when(connection.getCurrentDatabaseName()).thenReturn("foo_db");
        when(metaData.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        SQLParserRule parserRule = mock(SQLParserRule.class, RETURNS_DEEP_STUBS);
        when(metaData.getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singletonList(parserRule)));
        when(parserRule.getSQLParserEngine(databaseType).parse("foo_distsql", true)).thenAnswer(invocation -> {
            when(metaData.getDatabase("foo_db")).thenReturn(null);
            return new ParseStatement("SELECT 1");
        });
        SQLException expected = new SQLException("Unknown database 'foo_db'", "42000", 1049);
        try (MockedStatic<SQLExceptionTransformEngine> mockedEngine = mockStatic(SQLExceptionTransformEngine.class, CALLS_REAL_METHODS)) {
            mockedEngine.when(() -> SQLExceptionTransformEngine.toSQLException(
                    argThat(ex -> ex instanceof UnknownDatabaseException && "foo_db".equals(((UnknownDatabaseException) ex).getDatabaseName())), same(databaseType))).thenReturn(expected);
            SQLException actual = assertThrows(SQLException.class, () -> new ShardingSpherePreparedStatement(connection, "foo_distsql"));
            assertThat(actual, sameInstance(expected));
            assertThat(actual.getMessage(), is("Unknown database 'foo_db'"));
            assertThat(actual.getSQLState(), is("42000"));
            assertThat(actual.getErrorCode(), is(1049));
        }
        verify(connection, never()).registerStatementManager(any());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertClearBatchResetsCachedGeneratedKeysResultSet() throws SQLException, ReflectiveOperationException {
        ShardingSpherePreparedStatement preparedStatement = createPreparedStatement();
        ResultSet cachedResultSet = new GeneratedKeysResultSet();
        Plugins.getMemberAccessor().set(ShardingSpherePreparedStatement.class.getDeclaredField("currentBatchGeneratedKeysResultSet"), preparedStatement, cachedResultSet);
        ((Collection<Comparable<?>>) Plugins.getMemberAccessor().get(ShardingSpherePreparedStatement.class.getDeclaredField("generatedValues"), preparedStatement)).add(1L);
        preparedStatement.clearBatch();
        ResultSet actual = preparedStatement.getGeneratedKeys();
        assertThat(actual, not(cachedResultSet));
        assertFalse(actual.isClosed());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertExecuteBatchClearsGeneratedValuesWithoutPendingBatches() throws SQLException, ReflectiveOperationException {
        ShardingSpherePreparedStatement preparedStatement = createPreparedStatement();
        ((Collection<Comparable<?>>) Plugins.getMemberAccessor().get(ShardingSpherePreparedStatement.class.getDeclaredField("generatedValues"), preparedStatement)).add(1L);
        preparedStatement.executeBatch();
        assertFalse(preparedStatement.getGeneratedKeys().next());
    }
    
    @Test
    void assertGetGeneratedKeysByDialectGeneratedKeyColumn() throws SQLException {
        ShardingSpherePreparedStatement preparedStatement = createPreparedStatement(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        ResultSet generatedKeys = mock(ResultSet.class);
        when(generatedKeys.next()).thenReturn(true, false);
        when(generatedKeys.getObject("GENERATED_KEY")).thenReturn(3L);
        setInsertStatementContext(preparedStatement);
        addPreparedStatement(preparedStatement, generatedKeys);
        try (ResultSet actual = preparedStatement.getGeneratedKeys()) {
            assertTrue(actual.next());
            assertThat(actual.getObject(1), is(3L));
        }
        verify(generatedKeys).getObject("GENERATED_KEY");
        verify(generatedKeys, never()).getObject("id");
        verify(generatedKeys, never()).getObject(1);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideTransactionStates")
    void assertExecuteUpdateAfterDatabaseRemoval(final String name, final boolean inTransaction, final boolean exceptionOccur) throws SQLException, ReflectiveOperationException {
        ShardingSpherePreparedStatement preparedStatement = createPreparedStatementWithSQLStatement(new SQLStatement(TypedSPILoader.getService(DatabaseType.class, "SQL92")));
        ShardingSphereConnection connection = preparedStatement.getConnection();
        TransactionConnectionContext transactionContext = connection.getDatabaseConnectionManager().getConnectionContext().getTransactionContext();
        when(transactionContext.isInTransaction()).thenReturn(inTransaction);
        when(transactionContext.isExceptionOccur()).thenReturn(exceptionOccur);
        SQLException expected = new SQLException("foo_failure", "foo_state", 123, new IllegalStateException("foo_cause"));
        DriverExecutorFacade executor = mockExecutor(preparedStatement);
        when(executor.executeUpdate(any(), any(), any(), any(), any(), any())).thenAnswer(invocation -> {
            when(connection.getContextManager().getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(null);
            throw expected;
        });
        assertThat(assertThrows(SQLException.class, preparedStatement::executeUpdate), sameInstance(expected));
        verify(transactionContext, never()).setExceptionOccur(anyBoolean());
    }
    
    private ShardingSpherePreparedStatement createPreparedStatementWithSQLStatement(final SQLStatement sqlStatement) throws SQLException {
        DatabaseType databaseType = sqlStatement.getDatabaseType();
        ShardingSphereMetaData metaData = createMetaData(databaseType);
        SQLParserRule parserRule = mock(SQLParserRule.class, RETURNS_DEEP_STUBS);
        when(parserRule.getSQLParserEngine(databaseType).parse("foo_sql", true)).thenReturn(sqlStatement);
        SQLFederationRule federationRule = metaData.getGlobalRuleMetaData().getSingleRule(SQLFederationRule.class);
        when(metaData.getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Arrays.asList(parserRule, federationRule)));
        ShardingSphereConnection connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        when(connection.getContextManager().getMetaDataContexts().getMetaData()).thenReturn(metaData);
        when(connection.getCurrentDatabaseName()).thenReturn("foo_db");
        ShardingSphereDatabase database = metaData.getDatabase("foo_db");
        when(connection.getContextManager().getDatabase("foo_db")).thenReturn(database);
        return new ShardingSpherePreparedStatement(connection, "foo_sql");
    }
    
    private DriverExecutorFacade mockExecutor(final ShardingSpherePreparedStatement preparedStatement) throws ReflectiveOperationException {
        DriverExecutorFacade result = mock(DriverExecutorFacade.class);
        Plugins.getMemberAccessor().set(ShardingSpherePreparedStatement.class.getDeclaredField("driverExecutorFacade"), preparedStatement, result);
        return result;
    }
    
    private static Stream<Arguments> provideTransactionStates() {
        return Stream.of(Arguments.of("outside transaction", false, false), Arguments.of("active transaction", true, false), Arguments.of("failed transaction", true, true));
    }
    
    @Test
    void assertExecuteUpdateWithRuntimeExceptionAfterDatabaseRemoval() throws SQLException, ReflectiveOperationException {
        ShardingSpherePreparedStatement preparedStatement = createPreparedStatementWithSQLStatement(new SQLStatement(TypedSPILoader.getService(DatabaseType.class, "SQL92")));
        ShardingSphereConnection connection = preparedStatement.getConnection();
        when(connection.getDatabaseConnectionManager().getConnectionContext().getTransactionContext().isInTransaction()).thenReturn(true);
        IllegalStateException expected = new IllegalStateException("foo_failure");
        DriverExecutorFacade executor = mockExecutor(preparedStatement);
        when(executor.executeUpdate(any(), any(), any(), any(), any(), any())).thenAnswer(invocation -> {
            when(connection.getContextManager().getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(null);
            throw expected;
        });
        assertThat(assertThrows(SQLException.class, preparedStatement::executeUpdate).getCause(), sameInstance(expected));
    }
    
    @Test
    void assertExecuteUpdateWhenFailedTransactionDatabaseIsMissing() throws SQLException {
        ShardingSpherePreparedStatement preparedStatement = createPreparedStatementWithSQLStatement(new SQLStatement(TypedSPILoader.getService(DatabaseType.class, "SQL92")));
        ShardingSphereConnection connection = preparedStatement.getConnection();
        when(connection.getDatabaseConnectionManager().getConnectionContext().getTransactionContext().isExceptionOccur()).thenReturn(true);
        when(connection.getContextManager().getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(null);
        UnknownDatabaseException expected = new UnknownDatabaseException("foo_db");
        when(connection.getContextManager().getDatabase("foo_db")).thenThrow(expected);
        assertThat(assertThrows(SQLException.class, preparedStatement::executeUpdate).getCause(), sameInstance(expected));
    }
    
    @Test
    void assertExecuteUpdateWhenPostgreSQLTransactionFailed() throws SQLException {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        ShardingSpherePreparedStatement preparedStatement = createPreparedStatementWithSQLStatement(new SQLStatement(databaseType));
        ShardingSphereConnection connection = preparedStatement.getConnection();
        when(connection.getDatabaseConnectionManager().getConnectionContext().getTransactionContext().isExceptionOccur()).thenReturn(true);
        when(connection.getContextManager().getMetaDataContexts().getMetaData().getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class, RETURNS_DEEP_STUBS);
        when(dialectDatabaseMetaData.getTransactionOption().isAllowCommitAndRollbackOnlyWhenTransactionFailed()).thenReturn(true);
        AtomicReference<DatabaseType> actualProtocolType = new AtomicReference<>();
        try (MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(DatabaseTypeRegistry.class, (mock, context) -> {
            actualProtocolType.set((DatabaseType) context.arguments().get(0));
            when(mock.getDialectDatabaseMetaData()).thenReturn(dialectDatabaseMetaData);
        })) {
            SQLException actual = assertThrows(SQLFeatureNotSupportedException.class, preparedStatement::executeUpdate);
            assertThat(actual.getMessage(), is("Current transaction is aborted, commands ignored until end of transaction block."));
            assertThat(actualProtocolType.get(), sameInstance(databaseType));
        }
    }
    
    @Test
    void assertExecuteUpdateCommitWhenPostgreSQLTransactionFailed() throws SQLException, ReflectiveOperationException {
        ShardingSpherePreparedStatement preparedStatement = createPreparedStatementWithSQLStatement(new CommitStatement(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL")));
        when(preparedStatement.getConnection().getDatabaseConnectionManager().getConnectionContext().getTransactionContext().isExceptionOccur()).thenReturn(true);
        mockExecutor(preparedStatement);
        try (MockedConstruction<DatabaseTypeRegistry> ignored = mockFailedTransactionOption()) {
            assertThat(preparedStatement.executeUpdate(), is(0));
        }
    }
    
    private MockedConstruction<DatabaseTypeRegistry> mockFailedTransactionOption() {
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class, RETURNS_DEEP_STUBS);
        when(dialectDatabaseMetaData.getTransactionOption().isAllowCommitAndRollbackOnlyWhenTransactionFailed()).thenReturn(true);
        return mockConstruction(DatabaseTypeRegistry.class, (mock, context) -> when(mock.getDialectDatabaseMetaData()).thenReturn(dialectDatabaseMetaData));
    }
    
    @Test
    void assertExecuteUpdateRollbackWhenPostgreSQLTransactionFailed() throws SQLException, ReflectiveOperationException {
        ShardingSpherePreparedStatement preparedStatement = createPreparedStatementWithSQLStatement(new RollbackStatement(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL")));
        when(preparedStatement.getConnection().getDatabaseConnectionManager().getConnectionContext().getTransactionContext().isExceptionOccur()).thenReturn(true);
        mockExecutor(preparedStatement);
        try (MockedConstruction<DatabaseTypeRegistry> ignored = mockFailedTransactionOption()) {
            assertThat(preparedStatement.executeUpdate(), is(0));
        }
    }
    
    @Test
    void assertExecuteUpdateWhenMySQLTransactionFailed() throws SQLException, ReflectiveOperationException {
        ShardingSpherePreparedStatement preparedStatement = createPreparedStatementWithSQLStatement(new SQLStatement(TypedSPILoader.getService(DatabaseType.class, "MySQL")));
        when(preparedStatement.getConnection().getDatabaseConnectionManager().getConnectionContext().getTransactionContext().isExceptionOccur()).thenReturn(true);
        mockExecutor(preparedStatement);
        assertThat(preparedStatement.executeUpdate(), is(0));
    }
    
    @Test
    void assertExecuteUpdateMarksPostgreSQLTransactionFailed() throws SQLException, ReflectiveOperationException {
        ShardingSpherePreparedStatement preparedStatement = createPreparedStatementWithSQLStatement(new SQLStatement(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL")));
        TransactionConnectionContext transactionContext = preparedStatement.getConnection().getDatabaseConnectionManager().getConnectionContext().getTransactionContext();
        when(transactionContext.isInTransaction()).thenReturn(true);
        DriverExecutorFacade executor = mockExecutor(preparedStatement);
        SQLException expected = new SQLException("foo_failure");
        when(executor.executeUpdate(any(), any(), any(), any(), any(), any())).thenThrow(expected);
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class, RETURNS_DEEP_STUBS);
        when(dialectDatabaseMetaData.getSchemaOption().getDefaultSchema()).thenReturn(Optional.of("public"));
        try (
                MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(DatabaseTypeRegistry.class,
                        (mock, context) -> when(mock.getDialectDatabaseMetaData()).thenReturn(dialectDatabaseMetaData))) {
            assertThat(assertThrows(SQLException.class, preparedStatement::executeUpdate), sameInstance(expected));
            verify(transactionContext).setExceptionOccur(true);
        }
    }
    
    @Test
    void assertExecuteUpdateKeepsMySQLTransactionState() throws SQLException, ReflectiveOperationException {
        ShardingSpherePreparedStatement preparedStatement = createPreparedStatementWithSQLStatement(new SQLStatement(TypedSPILoader.getService(DatabaseType.class, "MySQL")));
        TransactionConnectionContext transactionContext = preparedStatement.getConnection().getDatabaseConnectionManager().getConnectionContext().getTransactionContext();
        when(transactionContext.isInTransaction()).thenReturn(true);
        DriverExecutorFacade executor = mockExecutor(preparedStatement);
        SQLException expected = new SQLException("foo_failure");
        when(executor.executeUpdate(any(), any(), any(), any(), any(), any())).thenThrow(expected);
        assertThat(assertThrows(SQLException.class, preparedStatement::executeUpdate), sameInstance(expected));
        verify(transactionContext, never()).setExceptionOccur(anyBoolean());
    }
    
    private ShardingSpherePreparedStatement createPreparedStatement() throws SQLException {
        return createPreparedStatement(TypedSPILoader.getService(DatabaseType.class, "SQL92"));
    }
    
    private ShardingSpherePreparedStatement createPreparedStatement(final DatabaseType databaseType) throws SQLException {
        ShardingSphereMetaData metaData = createMetaData(databaseType);
        ShardingSphereConnection connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        when(connection.getContextManager().getMetaDataContexts().getMetaData()).thenReturn(metaData);
        when(connection.getCurrentDatabaseName()).thenReturn("foo_db");
        return new ShardingSpherePreparedStatement(connection, "SELECT 1", Statement.RETURN_GENERATED_KEYS);
    }
    
    private ShardingSphereMetaData createMetaData(final DatabaseType databaseType) {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getProtocolType()).thenReturn(databaseType);
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class);
        when(result.getDatabase("foo_db")).thenReturn(database);
        when(result.getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Arrays.asList(
                new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build()),
                new SQLFederationRule(new DefaultSQLFederationRuleConfigurationBuilder().build(), Collections.emptyList()))));
        when(result.getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setInsertStatementContext(final ShardingSpherePreparedStatement preparedStatement) {
        GeneratedKeyContext generatedKeyContext = mock(GeneratedKeyContext.class);
        when(generatedKeyContext.getColumnName()).thenReturn("id");
        when(generatedKeyContext.getGeneratedValues()).thenReturn(Collections.emptyList());
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(generatedKeyContext));
        Plugins.getMemberAccessor().set(ShardingSpherePreparedStatement.class.getDeclaredField("sqlStatementContext"), preparedStatement, insertStatementContext);
    }
    
    private void addPreparedStatement(final ShardingSpherePreparedStatement preparedStatement, final ResultSet generatedKeys) throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        when(statement.getGeneratedKeys()).thenReturn(generatedKeys);
        getPreparedStatements(preparedStatement).add(statement);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("unchecked")
    private Collection<PreparedStatement> getPreparedStatements(final ShardingSpherePreparedStatement preparedStatement) {
        Field statementsField = ShardingSpherePreparedStatement.class.getDeclaredField("statements");
        return (Collection<PreparedStatement>) Plugins.getMemberAccessor().get(statementsField, preparedStatement);
    }
}
