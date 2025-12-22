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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.show;

import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromDatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.MySQLShowTransactionStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MySQLShowTransactionExecutorTest {
    
    private static final String DATABASE_NAME = "test_db";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Test
    void assertExecuteWithoutFromAndWhere() {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        MySQLShowTransactionExecutor executor = new MySQLShowTransactionExecutor(sqlStatement);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> executor.execute(mockConnectionSession(DATABASE_NAME), mockMetaData(createDatabases())));
        assertThat(exception.getMessage(), is("SHOW TRANSACTION requires WHERE clause with 'id = <transaction_id>' or 'label = <label_name>'"));
    }
    
    @Test
    void assertExecuteWithFromDatabase() {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        FromDatabaseSegment fromDatabaseSegment = new FromDatabaseSegment(0, new DatabaseSegment(0, 0, new IdentifierValue(DATABASE_NAME)));
        sqlStatement.setFromDatabase(fromDatabaseSegment);
        MySQLShowTransactionExecutor executor = new MySQLShowTransactionExecutor(sqlStatement);
        assertThrows(IllegalArgumentException.class,
                () -> executor.execute(mockConnectionSession("other_db"), mockMetaData(createDatabases())));
    }
    
    @Test
    void assertExecuteWithWhereId() {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        WhereSegment whereSegment = createWhereSegment("ID", 4005);
        sqlStatement.setWhere(whereSegment);
        MySQLShowTransactionExecutor executor = new MySQLShowTransactionExecutor(sqlStatement);
        assertThrows(UnsupportedOperationException.class,
                () -> executor.execute(mockConnectionSession(DATABASE_NAME), mockMetaData(createDatabases())));
    }
    
    @Test
    void assertExecuteWithWhereLabel() {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        WhereSegment whereSegment = createWhereSegment("label", "test_label");
        sqlStatement.setWhere(whereSegment);
        MySQLShowTransactionExecutor executor = new MySQLShowTransactionExecutor(sqlStatement);
        assertThrows(UnsupportedOperationException.class,
                () -> executor.execute(mockConnectionSession(DATABASE_NAME), mockMetaData(createDatabases())));
    }
    
    @Test
    void assertExecuteWithFromAndWhereId() {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        FromDatabaseSegment fromDatabaseSegment = new FromDatabaseSegment(0, new DatabaseSegment(0, 0, new IdentifierValue(DATABASE_NAME)));
        sqlStatement.setFromDatabase(fromDatabaseSegment);
        WhereSegment whereSegment = createWhereSegment("ID", 4005);
        sqlStatement.setWhere(whereSegment);
        MySQLShowTransactionExecutor executor = new MySQLShowTransactionExecutor(sqlStatement);
        assertThrows(UnsupportedOperationException.class,
                () -> executor.execute(mockConnectionSession("other_db"), mockMetaData(createDatabases())));
    }
    
    @Test
    void assertExecuteWithFromAndWhereLabel() {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        FromDatabaseSegment fromDatabaseSegment = new FromDatabaseSegment(0, new DatabaseSegment(0, 0, new IdentifierValue(DATABASE_NAME)));
        sqlStatement.setFromDatabase(fromDatabaseSegment);
        WhereSegment whereSegment = createWhereSegment("label", "test_label");
        sqlStatement.setWhere(whereSegment);
        MySQLShowTransactionExecutor executor = new MySQLShowTransactionExecutor(sqlStatement);
        assertThrows(UnsupportedOperationException.class,
                () -> executor.execute(mockConnectionSession("other_db"), mockMetaData(createDatabases())));
    }
    
    @Test
    void assertExecuteWithUnknownDatabase() {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        FromDatabaseSegment fromDatabaseSegment = new FromDatabaseSegment(0, new DatabaseSegment(0, 0, new IdentifierValue("unknown_db")));
        sqlStatement.setFromDatabase(fromDatabaseSegment);
        WhereSegment whereSegment = createWhereSegment("id", 1001L);
        sqlStatement.setWhere(whereSegment);
        MySQLShowTransactionExecutor executor = new MySQLShowTransactionExecutor(sqlStatement);
        assertThrows(UnknownDatabaseException.class, () -> executor.execute(mockConnectionSession("other_db"), mockMetaData(createDatabases())));
    }
    
    @Test
    void assertExecuteWithUncompletedDatabase() {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        FromDatabaseSegment fromDatabaseSegment = new FromDatabaseSegment(0, new DatabaseSegment(0, 0, new IdentifierValue("uncompleted")));
        sqlStatement.setFromDatabase(fromDatabaseSegment);
        WhereSegment whereSegment = createWhereSegment("id", 1001L);
        sqlStatement.setWhere(whereSegment);
        MySQLShowTransactionExecutor executor = new MySQLShowTransactionExecutor(sqlStatement);
        executor.execute(mockConnectionSession("uncompleted"), mockMetaData(createDatabases()));
    }
    
    @Test
    void assertTransactionStatusConstants() {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        MySQLShowTransactionExecutor executor = new MySQLShowTransactionExecutor(sqlStatement);
        assertThrows(IllegalArgumentException.class,
                () -> executor.execute(mockConnectionSession(DATABASE_NAME), mockMetaData(createDatabases())));
    }
    
    @Test
    void assertWhereIdFilterWithNumberLiteral() {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        WhereSegment whereSegment = createWhereSegment("id", 1001L);
        sqlStatement.setWhere(whereSegment);
        MySQLShowTransactionExecutor executor = new MySQLShowTransactionExecutor(sqlStatement);
        assertThrows(UnsupportedOperationException.class,
                () -> executor.execute(mockConnectionSession(DATABASE_NAME), mockMetaData(createDatabases())));
    }
    
    @Test
    void assertWhereIdFilterWithStringLiteral() {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        WhereSegment whereSegment = createWhereSegment("id", "1001");
        sqlStatement.setWhere(whereSegment);
        MySQLShowTransactionExecutor executor = new MySQLShowTransactionExecutor(sqlStatement);
        assertThrows(UnsupportedOperationException.class,
                () -> executor.execute(mockConnectionSession(DATABASE_NAME), mockMetaData(createDatabases())));
    }
    
    @Test
    void assertWhereIdFilterCaseInsensitive() {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        WhereSegment whereSegment = createWhereSegment("ID", 1001L);
        sqlStatement.setWhere(whereSegment);
        MySQLShowTransactionExecutor executor = new MySQLShowTransactionExecutor(sqlStatement);
        assertThrows(UnsupportedOperationException.class,
                () -> executor.execute(mockConnectionSession(DATABASE_NAME), mockMetaData(createDatabases())));
    }
    
    @Test
    void assertWhereLabelFilterCaseInsensitive() {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        WhereSegment whereSegment = createWhereSegment("LABEL", "test_label");
        sqlStatement.setWhere(whereSegment);
        MySQLShowTransactionExecutor executor = new MySQLShowTransactionExecutor(sqlStatement);
        assertThrows(UnsupportedOperationException.class,
                () -> executor.execute(mockConnectionSession(DATABASE_NAME), mockMetaData(createDatabases())));
    }
    
    @Test
    void assertQueryResultMetaDataWithFullColumnSet() throws SQLException {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        WhereSegment whereSegment = createWhereSegment("id", 1001L);
        sqlStatement.setWhere(whereSegment);
        TestableShowTransactionExecutor executor = new TestableShowTransactionExecutor(sqlStatement);
        executor.setTestData(Collections.emptyList());
        executor.execute(mockConnectionSession(DATABASE_NAME), mockMetaData(createDatabases()));
        QueryResultMetaData metaData = executor.getQueryResultMetaData();
        assertThat(metaData.getColumnCount(), is(12));
        assertThat(metaData.getColumnLabel(1), is("TransactionId"));
        assertThat(metaData.getColumnType(1), is(Types.BIGINT));
        assertThat(metaData.getColumnLabel(2), is("Label"));
        assertThat(metaData.getColumnType(2), is(Types.VARCHAR));
        assertThat(metaData.getColumnLabel(3), is("Coordinator"));
        assertThat(metaData.getColumnType(3), is(Types.VARCHAR));
        assertThat(metaData.getColumnLabel(4), is("TransactionStatus"));
        assertThat(metaData.getColumnType(4), is(Types.VARCHAR));
        assertThat(metaData.getColumnLabel(5), is("LoadJobSourceType"));
        assertThat(metaData.getColumnType(5), is(Types.VARCHAR));
        assertThat(metaData.getColumnLabel(6), is("PrepareTime"));
        assertThat(metaData.getColumnType(6), is(Types.VARCHAR));
        assertThat(metaData.getColumnLabel(7), is("CommitTime"));
        assertThat(metaData.getColumnType(7), is(Types.VARCHAR));
        assertThat(metaData.getColumnLabel(8), is("FinishTime"));
        assertThat(metaData.getColumnType(8), is(Types.VARCHAR));
        assertThat(metaData.getColumnLabel(9), is("Reason"));
        assertThat(metaData.getColumnType(9), is(Types.VARCHAR));
        assertThat(metaData.getColumnLabel(10), is("ErrorReplicasCount"));
        assertThat(metaData.getColumnType(10), is(Types.INTEGER));
        assertThat(metaData.getColumnLabel(11), is("ListenerId"));
        assertThat(metaData.getColumnType(11), is(Types.BIGINT));
        assertThat(metaData.getColumnLabel(12), is("TimeoutMs"));
        assertThat(metaData.getColumnType(12), is(Types.BIGINT));
    }
    
    @Test
    void assertExecuteWithDataPresent() throws SQLException {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        WhereSegment whereSegment = createWhereSegment("id", 1001L);
        sqlStatement.setWhere(whereSegment);
        TestableShowTransactionExecutor executor = new TestableShowTransactionExecutor(sqlStatement);
        executor.setTestData(createMockTransactions());
        executor.execute(mockConnectionSession(DATABASE_NAME), mockMetaData(createDatabases()));
        MergedResult mergedResult = executor.getMergedResult();
        assertTrue(mergedResult.next());
        assertThat(mergedResult.getValue(1, String.class), is("1001"));
        assertThat(mergedResult.getValue(2, String.class), is("load_job_1"));
        assertThat(mergedResult.getValue(3, String.class), is("coordinator_node_1"));
        assertThat(mergedResult.getValue(4, String.class), is("VISIBLE"));
        assertFalse(mergedResult.next());
    }
    
    @Test
    void assertExecuteWithDataAbsent() throws SQLException {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        WhereSegment whereSegment = createWhereSegment("id", 1001L);
        sqlStatement.setWhere(whereSegment);
        TestableShowTransactionExecutor executor = new TestableShowTransactionExecutor(sqlStatement);
        executor.setTestData(Collections.emptyList());
        executor.execute(mockConnectionSession(DATABASE_NAME), mockMetaData(createDatabases()));
        MergedResult mergedResult = executor.getMergedResult();
        assertFalse(mergedResult.next());
    }
    
    @Test
    void assertWhereIdFilterHit() throws SQLException {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        WhereSegment whereSegment = createWhereSegment("id", 1001L);
        sqlStatement.setWhere(whereSegment);
        TestableShowTransactionExecutor executor = new TestableShowTransactionExecutor(sqlStatement);
        executor.setTestData(createMockTransactions());
        executor.execute(mockConnectionSession(DATABASE_NAME), mockMetaData(createDatabases()));
        MergedResult mergedResult = executor.getMergedResult();
        assertTrue(mergedResult.next());
        assertThat(mergedResult.getValue(1, String.class), is("1001"));
        assertThat(mergedResult.getValue(2, String.class), is("load_job_1"));
        assertFalse(mergedResult.next());
    }
    
    @Test
    void assertWhereIdFilterMiss() throws SQLException {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        WhereSegment whereSegment = createWhereSegment("id", 9999L);
        sqlStatement.setWhere(whereSegment);
        TestableShowTransactionExecutor executor = new TestableShowTransactionExecutor(sqlStatement);
        executor.setTestData(createMockTransactions());
        executor.execute(mockConnectionSession(DATABASE_NAME), mockMetaData(createDatabases()));
        MergedResult mergedResult = executor.getMergedResult();
        assertFalse(mergedResult.next());
    }
    
    @Test
    void assertWhereLabelFilterHit() throws SQLException {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        WhereSegment whereSegment = createWhereSegment("label", "load_job_2");
        sqlStatement.setWhere(whereSegment);
        TestableShowTransactionExecutor executor = new TestableShowTransactionExecutor(sqlStatement);
        executor.setTestData(createMockTransactions());
        executor.execute(mockConnectionSession(DATABASE_NAME), mockMetaData(createDatabases()));
        MergedResult mergedResult = executor.getMergedResult();
        assertTrue(mergedResult.next());
        assertThat(mergedResult.getValue(1, String.class), is("1002"));
        assertThat(mergedResult.getValue(2, String.class), is("load_job_2"));
        assertFalse(mergedResult.next());
    }
    
    @Test
    void assertWhereLabelFilterMiss() throws SQLException {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        WhereSegment whereSegment = createWhereSegment("label", "non_existent_label");
        sqlStatement.setWhere(whereSegment);
        TestableShowTransactionExecutor executor = new TestableShowTransactionExecutor(sqlStatement);
        executor.setTestData(createMockTransactions());
        executor.execute(mockConnectionSession(DATABASE_NAME), mockMetaData(createDatabases()));
        MergedResult mergedResult = executor.getMergedResult();
        assertFalse(mergedResult.next());
    }
    
    @Test
    void assertWhereIdWithStringLiteralFilterHit() throws SQLException {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        WhereSegment whereSegment = createWhereSegment("id", "1002");
        sqlStatement.setWhere(whereSegment);
        TestableShowTransactionExecutor executor = new TestableShowTransactionExecutor(sqlStatement);
        executor.setTestData(createMockTransactions());
        executor.execute(mockConnectionSession(DATABASE_NAME), mockMetaData(createDatabases()));
        MergedResult mergedResult = executor.getMergedResult();
        assertTrue(mergedResult.next());
        assertThat(mergedResult.getValue(1, String.class), is("1002"));
        assertFalse(mergedResult.next());
    }
    
    @Test
    void assertWhereIdCaseInsensitiveFilterHit() throws SQLException {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        WhereSegment whereSegment = createWhereSegment("ID", 1001L);
        sqlStatement.setWhere(whereSegment);
        TestableShowTransactionExecutor executor = new TestableShowTransactionExecutor(sqlStatement);
        executor.setTestData(createMockTransactions());
        executor.execute(mockConnectionSession(DATABASE_NAME), mockMetaData(createDatabases()));
        MergedResult mergedResult = executor.getMergedResult();
        assertTrue(mergedResult.next());
        assertThat(mergedResult.getValue(1, String.class), is("1001"));
        assertFalse(mergedResult.next());
    }
    
    @Test
    void assertWhereLabelCaseInsensitiveFilterHit() throws SQLException {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        WhereSegment whereSegment = createWhereSegment("LABEL", "load_job_1");
        sqlStatement.setWhere(whereSegment);
        TestableShowTransactionExecutor executor = new TestableShowTransactionExecutor(sqlStatement);
        executor.setTestData(createMockTransactions());
        executor.execute(mockConnectionSession(DATABASE_NAME), mockMetaData(createDatabases()));
        MergedResult mergedResult = executor.getMergedResult();
        assertTrue(mergedResult.next());
        assertThat(mergedResult.getValue(1, String.class), is("1001"));
        assertThat(mergedResult.getValue(2, String.class), is("load_job_1"));
        assertFalse(mergedResult.next());
    }
    
    @Test
    void assertMissingWhereClause() {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        TestableShowTransactionExecutor executor = new TestableShowTransactionExecutor(sqlStatement);
        executor.setTestData(createMockTransactions());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> executor.execute(mockConnectionSession(DATABASE_NAME), mockMetaData(createDatabases())));
        assertThat(exception.getMessage(), is("SHOW TRANSACTION requires WHERE clause with 'id = <transaction_id>' or 'label = <label_name>'"));
    }
    
    @Test
    void assertWhereWithUnsupportedColumn() {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        WhereSegment whereSegment = createWhereSegment("status", "VISIBLE");
        sqlStatement.setWhere(whereSegment);
        TestableShowTransactionExecutor executor = new TestableShowTransactionExecutor(sqlStatement);
        executor.setTestData(createMockTransactions());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> executor.execute(mockConnectionSession(DATABASE_NAME), mockMetaData(createDatabases())));
        assertThat(exception.getMessage(), is("WHERE clause only supports 'id' or 'label' columns, got: status"));
    }
    
    @Test
    void assertWhereWithInvalidIdValue() {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        WhereSegment whereSegment = createWhereSegment("id", "not_a_number");
        sqlStatement.setWhere(whereSegment);
        TestableShowTransactionExecutor executor = new TestableShowTransactionExecutor(sqlStatement);
        executor.setTestData(createMockTransactions());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> executor.execute(mockConnectionSession(DATABASE_NAME), mockMetaData(createDatabases())));
        assertThat(exception.getMessage(), is("Invalid transaction id value: not_a_number"));
    }
    
    @Test
    void assertWhereWithNonEqualityOperator() {
        MySQLShowTransactionStatement sqlStatement = new MySQLShowTransactionStatement(databaseType);
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("id"));
        LiteralExpressionSegment literalSegment = new LiteralExpressionSegment(0, 0, 1000L);
        BinaryOperationExpression expression = new BinaryOperationExpression(0, 0, columnSegment, literalSegment, ">", "");
        WhereSegment whereSegment = new WhereSegment(0, 0, expression);
        sqlStatement.setWhere(whereSegment);
        TestableShowTransactionExecutor executor = new TestableShowTransactionExecutor(sqlStatement);
        executor.setTestData(createMockTransactions());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> executor.execute(mockConnectionSession(DATABASE_NAME), mockMetaData(createDatabases())));
        assertThat(exception.getMessage(), is("WHERE clause only supports '=' operator, got: >"));
    }
    
    private WhereSegment createWhereSegment(final String columnName, final Object value) {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue(columnName));
        LiteralExpressionSegment literalSegment = new LiteralExpressionSegment(0, 0, value);
        BinaryOperationExpression expression = new BinaryOperationExpression(0, 0, columnSegment, literalSegment, "=", "");
        return new WhereSegment(0, 0, expression);
    }
    
    private ShardingSphereMetaData mockMetaData(final Collection<ShardingSphereDatabase> databases) {
        return new ShardingSphereMetaData(databases, mock(ResourceMetaData.class), new RuleMetaData(Collections.singleton(mock(AuthorityRule.class))), new ConfigurationProperties(new Properties()));
    }
    
    private Collection<ShardingSphereDatabase> createDatabases() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn(DATABASE_NAME);
        when(database.getProtocolType()).thenReturn(databaseType);
        when(database.isComplete()).thenReturn(true);
        ShardingSphereDatabase uncompletedDatabase = new ShardingSphereDatabase("uncompleted", mock(), mock(), mock(), Collections.emptyList());
        return Arrays.asList(database, uncompletedDatabase);
    }
    
    private ConnectionSession mockConnectionSession(final String usedDatabaseName) {
        ConnectionSession result = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        when(result.getUsedDatabaseName()).thenReturn(usedDatabaseName);
        return result;
    }
    
    private Collection<MySQLShowTransactionExecutor.TransactionInfo> createMockTransactions() {
        List<MySQLShowTransactionExecutor.TransactionInfo> mockTransactions = new ArrayList<>(2);
        mockTransactions.add(new MySQLShowTransactionExecutor.TransactionInfo(
                1001L, "load_job_1", "coordinator_node_1", "VISIBLE", "ROUTINE_LOAD",
                "2025-01-01 10:00:00", "2025-01-01 10:01:00", "2025-01-01 10:02:00",
                "", 0, 10001L, 60000L));
        mockTransactions.add(new MySQLShowTransactionExecutor.TransactionInfo(
                1002L, "load_job_2", "coordinator_node_2", "COMMITTED", "BROKER_LOAD",
                "2025-01-01 11:00:00", "2025-01-01 11:01:00", "",
                "", 0, 10002L, 120000L));
        return mockTransactions;
    }
    
    /**
     * Testable executor for testing data flow and filtering logic.
     */
    static class TestableShowTransactionExecutor extends MySQLShowTransactionExecutor {
        
        private Collection<MySQLShowTransactionExecutor.TransactionInfo> testData;
        
        TestableShowTransactionExecutor(final MySQLShowTransactionStatement sqlStatement) {
            super(sqlStatement);
        }
        
        void setTestData(final Collection<MySQLShowTransactionExecutor.TransactionInfo> data) {
            this.testData = data;
        }
        
        @Override
        protected Collection<MySQLShowTransactionExecutor.TransactionInfo> loadTransactions() {
            if (null != testData) {
                return testData;
            }
            return super.loadTransactions();
        }
    }
}
