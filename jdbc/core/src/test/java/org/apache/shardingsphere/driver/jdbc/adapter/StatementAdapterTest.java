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

package org.apache.shardingsphere.driver.jdbc.adapter;

import lombok.SneakyThrows;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.statement.ShardingSphereStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.sqlfederation.rule.builder.DefaultSQLFederationRuleConfigurationBuilder;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.traffic.rule.builder.DefaultTrafficRuleConfigurationBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StatementAdapterTest {
    
    @Test
    void assertClose() throws SQLException {
        Statement statement = mock(Statement.class);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        actual.close();
        assertTrue(actual.isClosed());
        assertTrue(actual.getRoutedStatements().isEmpty());
        verify(statement).close();
    }
    
    @Test
    void assertSetPoolable() throws SQLException {
        Statement statement = mock(Statement.class);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        actual.setPoolable(true);
        assertTrue(actual.isPoolable());
        verify(statement).setPoolable(true);
    }
    
    @Test
    void assertSetFetchSize() throws SQLException {
        Statement statement = mock(Statement.class);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        actual.setFetchSize(100);
        assertThat(actual.getFetchSize(), is(100));
        verify(statement).setFetchSize(100);
    }
    
    @Test
    void assertSetFetchDirection() throws SQLException {
        Statement statement = mock(Statement.class);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        actual.setFetchDirection(ResultSet.FETCH_REVERSE);
        assertThat(actual.getFetchDirection(), is(ResultSet.FETCH_REVERSE));
        verify(statement).setFetchDirection(ResultSet.FETCH_REVERSE);
    }
    
    @Test
    void assertSetEscapeProcessing() throws SQLException {
        Statement statement = mock(Statement.class);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        actual.setEscapeProcessing(true);
        verify(statement).setEscapeProcessing(true);
    }
    
    @Test
    void assertCancel() throws SQLException {
        Statement statement = mock(Statement.class);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        actual.cancel();
        verify(statement).cancel();
    }
    
    @Test
    void assertGetUpdateCountWithoutAccumulate() throws SQLException {
        Statement statement1 = mock(Statement.class);
        when(statement1.getUpdateCount()).thenReturn(Integer.MAX_VALUE);
        Statement statement2 = mock(Statement.class);
        when(statement2.getUpdateCount()).thenReturn(Integer.MAX_VALUE);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement1, statement2);
        assertThat(actual.getUpdateCount(), is(Integer.MAX_VALUE));
    }
    
    @Test
    void assertGetUpdateCountWithoutAccumulateAndInvalidResult() throws SQLException {
        Statement statement = mock(Statement.class);
        when(statement.getUpdateCount()).thenReturn(-1);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        assertThat(actual.getUpdateCount(), is(-1));
    }
    
    @Test
    void assertGetUpdateCountWithoutAccumulateAndEmptyResult() throws SQLException {
        ShardingSphereStatement actual = mockShardingSphereStatement();
        assertThat(actual.getUpdateCount(), is(-1));
    }
    
    @Test
    void assertGetUpdateCountWithAccumulate() throws SQLException {
        Statement statement1 = mock(Statement.class);
        when(statement1.getUpdateCount()).thenReturn(Integer.MAX_VALUE);
        Statement statement2 = mock(Statement.class);
        when(statement2.getUpdateCount()).thenReturn(Integer.MAX_VALUE);
        ShardingSphereStatement actual = mockShardingSphereStatementWithNeedAccumulate(statement1, statement2);
        assertThat(actual.getUpdateCount(), is(Integer.MAX_VALUE));
    }
    
    @Test
    void assertGetWarnings() throws SQLException {
        try (ShardingSphereStatement actual = mockShardingSphereStatement()) {
            assertNull(actual.getWarnings());
        }
    }
    
    @Test
    void assertClearWarnings() throws SQLException {
        try (ShardingSphereStatement actual = mockShardingSphereStatement()) {
            assertDoesNotThrow(actual::clearWarnings);
        }
    }
    
    @Test
    void assertGetMoreResults() throws SQLException {
        Statement statement = mock(Statement.class);
        when(statement.getMoreResults()).thenReturn(true);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        assertTrue(actual.getMoreResults());
    }
    
    @Test
    void assertGetMoreResultsWithCurrent() {
        assertFalse(mockShardingSphereStatement().getMoreResults(Statement.KEEP_CURRENT_RESULT));
    }
    
    @Test
    void assertGetMaxFieldSizeWithoutRoutedStatements() throws SQLException {
        assertThat(mockShardingSphereStatement().getMaxFieldSize(), is(0));
    }
    
    @Test
    void assertGetMaxFieldSizeWithRoutedStatements() throws SQLException {
        Statement statement = mock(Statement.class);
        when(statement.getMaxFieldSize()).thenReturn(10);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        assertThat(actual.getMaxFieldSize(), is(10));
    }
    
    @Test
    void assertSetMaxFieldSize() throws SQLException {
        Statement statement = mock(Statement.class);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        actual.setMaxFieldSize(10);
        verify(statement).setMaxFieldSize(10);
    }
    
    @Test
    void assertGetMaxRowsWitRoutedStatements() throws SQLException {
        assertThat(mockShardingSphereStatement().getMaxRows(), is(-1));
    }
    
    @Test
    void assertGetMaxRowsWithoutRoutedStatements() throws SQLException {
        Statement statement = mock(Statement.class);
        when(statement.getMaxRows()).thenReturn(10);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        assertThat(actual.getMaxRows(), is(10));
    }
    
    @Test
    void assertSetMaxRows() throws SQLException {
        Statement statement = mock(Statement.class);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        actual.setMaxRows(10);
        verify(statement).setMaxRows(10);
    }
    
    @Test
    void assertGetQueryTimeoutWithoutRoutedStatements() throws SQLException {
        assertThat(mockShardingSphereStatement().getQueryTimeout(), is(0));
    }
    
    @Test
    void assertGetQueryTimeoutWithRoutedStatements() throws SQLException {
        Statement statement = mock(Statement.class);
        when(statement.getQueryTimeout()).thenReturn(10);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        assertThat(actual.getQueryTimeout(), is(10));
    }
    
    @Test
    void assertSetQueryTimeout() throws SQLException {
        Statement statement = mock(Statement.class);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        actual.setQueryTimeout(10);
        verify(statement).setQueryTimeout(10);
    }
    
    private ShardingSphereStatement mockShardingSphereStatement(final Statement... statements) {
        ShardingSphereConnection connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        ShardingSphereRuleMetaData globalRuleMetaData = new ShardingSphereRuleMetaData(Arrays.asList(
                new TrafficRule(new DefaultTrafficRuleConfigurationBuilder().build()),
                new SQLFederationRule(new DefaultSQLFederationRuleConfigurationBuilder().build(), Collections.emptyMap(), mock(ConfigurationProperties.class)),
                new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build())));
        when(connection.getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(connection.getContextManager().getMetaDataContexts().getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        when(connection.getDatabaseName()).thenReturn("db");
        ShardingSphereStatement result = new ShardingSphereStatement(connection);
        result.getRoutedStatements().addAll(Arrays.asList(statements));
        return result;
    }
    
    private ShardingSphereStatement mockShardingSphereStatementWithNeedAccumulate(final Statement... statements) {
        ShardingSphereConnection connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        DataNodeContainedRule rule = mock(DataNodeContainedRule.class);
        when(rule.isNeedAccumulate(any())).thenReturn(true);
        when(connection.getContextManager().getMetaDataContexts().getMetaData().getDatabase(DefaultDatabase.LOGIC_NAME).getRuleMetaData().getRules()).thenReturn(Collections.singleton(rule));
        when(connection.getDatabaseName()).thenReturn("db");
        when(connection.getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Arrays.asList(
                new TrafficRule(new DefaultTrafficRuleConfigurationBuilder().build()),
                new SQLFederationRule(new DefaultSQLFederationRuleConfigurationBuilder().build(), Collections.emptyMap(), mock(ConfigurationProperties.class)),
                new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build()))));
        when(connection.getContextManager().getMetaDataContexts().getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        ShardingSphereStatement result = new ShardingSphereStatement(connection);
        result.getRoutedStatements().addAll(Arrays.asList(statements));
        ExecutionContext executionContext = mock(ExecutionContext.class, RETURNS_DEEP_STUBS);
        setExecutionContext(result, executionContext);
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setExecutionContext(final ShardingSphereStatement statement, final ExecutionContext executionContext) {
        Plugins.getMemberAccessor().set(statement.getClass().getDeclaredField("executionContext"), statement, executionContext);
    }
}
