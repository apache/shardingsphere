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
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.sqlfederation.rule.builder.DefaultSQLFederationRuleConfigurationBuilder;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.traffic.rule.builder.DefaultTrafficRuleConfigurationBuilder;
import org.junit.Test;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class StatementAdapterTest {
    
    @Test
    public void assertClose() throws SQLException {
        Statement statement = mock(Statement.class);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        actual.close();
        assertTrue(actual.isClosed());
        assertTrue(actual.getRoutedStatements().isEmpty());
        verify(statement).close();
    }
    
    @Test
    public void assertSetPoolable() throws SQLException {
        Statement statement = mock(Statement.class);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        actual.setPoolable(true);
        assertTrue(actual.isPoolable());
        verify(statement).setPoolable(true);
    }
    
    @Test
    public void assertSetFetchSize() throws SQLException {
        Statement statement = mock(Statement.class);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        actual.setFetchSize(100);
        assertThat(actual.getFetchSize(), is(100));
        verify(statement).setFetchSize(100);
    }
    
    @Test
    public void assertSetFetchDirection() throws SQLException {
        Statement statement = mock(Statement.class);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        actual.setFetchDirection(ResultSet.FETCH_REVERSE);
        assertThat(actual.getFetchDirection(), is(ResultSet.FETCH_REVERSE));
        verify(statement).setFetchDirection(ResultSet.FETCH_REVERSE);
    }
    
    @Test
    public void assertSetEscapeProcessing() throws SQLException {
        Statement statement = mock(Statement.class);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        actual.setEscapeProcessing(true);
        verify(statement).setEscapeProcessing(true);
    }
    
    @Test
    public void assertCancel() throws SQLException {
        Statement statement = mock(Statement.class);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        actual.cancel();
        verify(statement).cancel();
    }
    
    @Test
    public void assertGetUpdateCountWithoutAccumulate() throws SQLException {
        Statement statement1 = mock(Statement.class);
        when(statement1.getUpdateCount()).thenReturn(Integer.MAX_VALUE);
        Statement statement2 = mock(Statement.class);
        when(statement2.getUpdateCount()).thenReturn(Integer.MAX_VALUE);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement1, statement2);
        assertThat(actual.getUpdateCount(), is(Integer.MAX_VALUE));
    }
    
    @Test
    public void assertGetUpdateCountWithoutAccumulateAndInvalidResult() throws SQLException {
        Statement statement = mock(Statement.class);
        when(statement.getUpdateCount()).thenReturn(-1);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        assertThat(actual.getUpdateCount(), is(-1));
    }
    
    @Test
    public void assertGetUpdateCountWithoutAccumulateAndEmptyResult() throws SQLException {
        ShardingSphereStatement actual = mockShardingSphereStatement();
        assertThat(actual.getUpdateCount(), is(-1));
    }
    
    @Test
    public void assertGetUpdateCountWithAccumulate() throws SQLException {
        Statement statement1 = mock(Statement.class);
        when(statement1.getUpdateCount()).thenReturn(Integer.MAX_VALUE);
        Statement statement2 = mock(Statement.class);
        when(statement2.getUpdateCount()).thenReturn(Integer.MAX_VALUE);
        ShardingSphereStatement actual = mockShardingSphereStatementWithNeedAccumulate(statement1, statement2);
        assertThat(actual.getUpdateCount(), is(Integer.MAX_VALUE));
    }
    
    @Test
    public void assertGetWarnings() {
        assertNull(mockShardingSphereStatement().getWarnings());
    }
    
    @Test
    public void assertClearWarnings() {
        mockShardingSphereStatement().clearWarnings();
    }
    
    @Test
    public void assertGetMoreResults() throws SQLException {
        Statement statement = mock(Statement.class);
        when(statement.getMoreResults()).thenReturn(true);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        assertTrue(actual.getMoreResults());
    }
    
    @Test
    public void assertGetMoreResultsWithCurrent() {
        assertFalse(mockShardingSphereStatement().getMoreResults(Statement.KEEP_CURRENT_RESULT));
    }
    
    @Test
    public void assertGetMaxFieldSizeWithoutRoutedStatements() throws SQLException {
        assertThat(mockShardingSphereStatement().getMaxFieldSize(), is(0));
    }
    
    @Test
    public void assertGetMaxFieldSizeWithRoutedStatements() throws SQLException {
        Statement statement = mock(Statement.class);
        when(statement.getMaxFieldSize()).thenReturn(10);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        assertThat(actual.getMaxFieldSize(), is(10));
    }
    
    @Test
    public void assertSetMaxFieldSize() throws SQLException {
        Statement statement = mock(Statement.class);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        actual.setMaxFieldSize(10);
        verify(statement).setMaxFieldSize(10);
    }
    
    @Test
    public void assertGetMaxRowsWitRoutedStatements() throws SQLException {
        assertThat(mockShardingSphereStatement().getMaxRows(), is(-1));
    }
    
    @Test
    public void assertGetMaxRowsWithoutRoutedStatements() throws SQLException {
        Statement statement = mock(Statement.class);
        when(statement.getMaxRows()).thenReturn(10);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        assertThat(actual.getMaxRows(), is(10));
    }
    
    @Test
    public void assertSetMaxRows() throws SQLException {
        Statement statement = mock(Statement.class);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        actual.setMaxRows(10);
        verify(statement).setMaxRows(10);
    }
    
    @Test
    public void assertGetQueryTimeoutWithoutRoutedStatements() throws SQLException {
        assertThat(mockShardingSphereStatement().getQueryTimeout(), is(0));
    }
    
    @Test
    public void assertGetQueryTimeoutWithRoutedStatements() throws SQLException {
        Statement statement = mock(Statement.class);
        when(statement.getQueryTimeout()).thenReturn(10);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        assertThat(actual.getQueryTimeout(), is(10));
    }
    
    @Test
    public void assertSetQueryTimeout() throws SQLException {
        Statement statement = mock(Statement.class);
        ShardingSphereStatement actual = mockShardingSphereStatement(statement);
        actual.setQueryTimeout(10);
        verify(statement).setQueryTimeout(10);
    }
    
    private ShardingSphereStatement mockShardingSphereStatement(final Statement... statements) {
        ShardingSphereConnection connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        ShardingSphereRuleMetaData globalRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(connection.getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(connection.getDatabaseName()).thenReturn("db");
        when(globalRuleMetaData.getSingleRule(TrafficRule.class)).thenReturn(new TrafficRule(new DefaultTrafficRuleConfigurationBuilder().build()));
        when(globalRuleMetaData.getSingleRule(SQLFederationRule.class)).thenReturn(new SQLFederationRule(new DefaultSQLFederationRuleConfigurationBuilder().build()));
        ShardingSphereStatement result = new ShardingSphereStatement(connection);
        result.getRoutedStatements().addAll(Arrays.asList(statements));
        return result;
    }
    
    private ShardingSphereStatement mockShardingSphereStatementWithNeedAccumulate(final Statement... statements) {
        ShardingSphereConnection connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        DataNodeContainedRule rule = mock(DataNodeContainedRule.class);
        when(rule.isNeedAccumulate(any())).thenReturn(true);
        when(connection.getContextManager()
                .getMetaDataContexts().getMetaData().getDatabase(DefaultDatabase.LOGIC_NAME).getRuleMetaData().getRules()).thenReturn(Collections.singletonList(rule));
        TrafficRule trafficRule = new TrafficRule(new DefaultTrafficRuleConfigurationBuilder().build());
        SQLFederationRule sqlFederationRule = new SQLFederationRule(new DefaultSQLFederationRuleConfigurationBuilder().build());
        ShardingSphereRuleMetaData globalRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(connection.getDatabaseName()).thenReturn("db");
        when(connection.getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(globalRuleMetaData.getSingleRule(TrafficRule.class)).thenReturn(trafficRule);
        when(globalRuleMetaData.getSingleRule(SQLFederationRule.class)).thenReturn(sqlFederationRule);
        ShardingSphereStatement result = new ShardingSphereStatement(connection);
        result.getRoutedStatements().addAll(Arrays.asList(statements));
        ExecutionContext executionContext = mock(ExecutionContext.class, RETURNS_DEEP_STUBS);
        setExecutionContext(result, executionContext);
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setExecutionContext(final ShardingSphereStatement statement, final ExecutionContext executionContext) {
        Field field = statement.getClass().getDeclaredField("executionContext");
        field.setAccessible(true);
        field.set(statement, executionContext);
    }
}
