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

package org.apache.shardingsphere.sqlfederation.engine;

import org.apache.shardingsphere.infra.binder.context.statement.type.dal.ExplainStatementContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationCacheOption;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.context.SQLFederationContext;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationDecider;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationExecutor;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationProvider;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SQLFederationEngineTest {
    
    private final SQLFederationCacheOption cacheOption = new SQLFederationCacheOption(1, 1L);
    
    @Test
    void assertCreatePreviewEngineWithoutJDBCExecutor() throws SQLException {
        SQLFederationProvider provider = mock(SQLFederationProvider.class);
        SQLFederationExecutor executor = mock(SQLFederationExecutor.class);
        ShardingSphereStatistics statistics = mock(ShardingSphereStatistics.class);
        when(provider.createPreviewExecutor(eq("foo_db"), eq("foo_schema"), eq(statistics), any())).thenReturn(executor);
        ShardingSphereMetaData metaData = createMetaData(provider, new SQLFederationRuleConfiguration(false, cacheOption), Collections.emptyList(), true);
        try (SQLFederationEngine engine = new SQLFederationEngine("foo_db", "foo_schema", metaData, statistics)) {
            assertThat(engine.getExecution(), sameInstance(executor));
        }
    }
    
    private ShardingSphereMetaData createMetaData(final SQLFederationProvider provider, final SQLFederationRuleConfiguration config,
                                                  final Collection<ShardingSphereRule> databaseRules, final boolean enabled) {
        SQLFederationRule rule = mock(SQLFederationRule.class);
        when(rule.getConfiguration()).thenReturn(config);
        when(rule.getProvider()).thenReturn(provider);
        when(rule.isSqlFederationEnabled()).thenReturn(enabled);
        RuleMetaData globalRuleMetaData = mock(RuleMetaData.class);
        when(globalRuleMetaData.getSingleRule(SQLFederationRule.class)).thenReturn(rule);
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(result.getDatabase("foo_db").getRuleMetaData().getRules()).thenReturn(databaseRules);
        return result;
    }
    
    @Test
    void assertDecideWhenSQLFederationDisabled() throws SQLException {
        SQLFederationProvider provider = mock(SQLFederationProvider.class);
        ShardingSphereMetaData metaData = createMetaData(provider, new SQLFederationRuleConfiguration(false, cacheOption), Collections.emptyList(), false);
        try (SQLFederationEngine engine = new SQLFederationEngine("foo_db", "foo_schema", metaData, mock(), mock())) {
            assertFalse(engine.decide(mock(QueryContext.class), mock(RuleMetaData.class)));
        }
    }
    
    @Test
    void assertDecideWhenEnableAllQueryUseSQLFederation() throws SQLException {
        SQLFederationProvider provider = mock(SQLFederationProvider.class);
        SelectStatement statement = mock(SelectStatement.class);
        when(provider.isSupportedSQLStatement(statement)).thenReturn(true);
        QueryContext queryContext = createQueryContext(statement);
        ShardingSphereMetaData metaData = createMetaData(provider, new SQLFederationRuleConfiguration(true, cacheOption), Collections.emptyList(), true);
        try (SQLFederationEngine engine = new SQLFederationEngine("foo_db", "foo_schema", metaData, mock(), mock())) {
            assertTrue(engine.decide(queryContext, mock(RuleMetaData.class)));
        }
    }
    
    private QueryContext createQueryContext(final SQLStatement statement) {
        QueryContext result = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatementContext().getSqlStatement()).thenReturn(statement);
        return result;
    }
    
    @Test
    void assertDecideWhenProviderRejectsStatement() throws SQLException {
        SQLFederationProvider provider = mock(SQLFederationProvider.class);
        QueryContext queryContext = createQueryContext(mock(CreateTableStatement.class));
        ShardingSphereMetaData metaData = createMetaData(provider, new SQLFederationRuleConfiguration(true, cacheOption), Collections.emptyList(), true);
        try (SQLFederationEngine engine = new SQLFederationEngine("foo_db", "foo_schema", metaData, mock(), mock())) {
            assertFalse(engine.decide(queryContext, mock(RuleMetaData.class)));
        }
    }
    
    @Test
    void assertDecideWithProviderSupportingNonSelectStatement() throws SQLException {
        SQLFederationProvider provider = mock(SQLFederationProvider.class);
        CreateTableStatement statement = mock(CreateTableStatement.class);
        when(provider.isSupportedSQLStatement(statement)).thenReturn(true);
        QueryContext queryContext = createQueryContext(statement);
        ShardingSphereMetaData metaData = createMetaData(provider, new SQLFederationRuleConfiguration(true, cacheOption), Collections.emptyList(), true);
        try (SQLFederationEngine engine = new SQLFederationEngine("foo_db", "foo_schema", metaData, mock(), mock())) {
            assertTrue(engine.decide(queryContext, mock(RuleMetaData.class)));
        }
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertDecideWithNotMatchedRule() throws SQLException {
        SQLFederationProvider provider = mock(SQLFederationProvider.class);
        SelectStatement statement = mock(SelectStatement.class);
        when(provider.isSupportedSQLStatement(statement)).thenReturn(true);
        QueryContext queryContext = createQueryContext(statement);
        when(queryContext.getSqlStatementContext().getTablesContext().getDatabaseNames()).thenReturn(Collections.singleton("foo_db"));
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        Collection<ShardingSphereRule> databaseRules = Collections.singleton(rule);
        SQLFederationDecider decider = mock(SQLFederationDecider.class);
        Map<ShardingSphereRule, SQLFederationDecider> deciders = Collections.singletonMap(rule, decider);
        ShardingSphereMetaData metaData = createMetaData(provider, new SQLFederationRuleConfiguration(false, cacheOption), databaseRules, true);
        try (
                MockedStatic<OrderedSPILoader> loader = mockStatic(OrderedSPILoader.class);
                SQLFederationEngine engine = new SQLFederationEngine("foo_db", "foo_schema", metaData, mock(), mock())) {
            loader.when(() -> OrderedSPILoader.getServices(SQLFederationDecider.class, databaseRules)).thenReturn(deciders);
            assertFalse(engine.decide(queryContext, mock(RuleMetaData.class)));
            verify(decider).decide(any(), any(), any(), any(), eq(rule), any());
        }
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertDecideWithMultipleRules() throws SQLException {
        SQLFederationProvider provider = mock(SQLFederationProvider.class);
        SelectStatement statement = mock(SelectStatement.class);
        when(provider.isSupportedSQLStatement(statement)).thenReturn(true);
        QueryContext queryContext = createQueryContext(statement);
        when(queryContext.getSqlStatementContext().getTablesContext().getDatabaseNames()).thenReturn(Collections.singleton("foo_db"));
        ShardingSphereRule firstRule = mock(ShardingSphereRule.class);
        ShardingSphereRule secondRule = mock(ShardingSphereRule.class);
        Collection<ShardingSphereRule> databaseRules = Arrays.asList(firstRule, secondRule);
        SQLFederationDecider firstDecider = mock(SQLFederationDecider.class);
        SQLFederationDecider secondDecider = mock(SQLFederationDecider.class);
        when(secondDecider.decide(any(), any(), any(), any(), eq(secondRule), any())).thenReturn(true);
        Map<ShardingSphereRule, SQLFederationDecider> deciders = new LinkedHashMap<>(2);
        deciders.put(firstRule, firstDecider);
        deciders.put(secondRule, secondDecider);
        ShardingSphereMetaData metaData = createMetaData(provider, new SQLFederationRuleConfiguration(false, cacheOption), databaseRules, true);
        try (
                MockedStatic<OrderedSPILoader> loader = mockStatic(OrderedSPILoader.class);
                SQLFederationEngine engine = new SQLFederationEngine("foo_db", "foo_schema", metaData, mock(), mock())) {
            loader.when(() -> OrderedSPILoader.getServices(SQLFederationDecider.class, databaseRules)).thenReturn(deciders);
            assertTrue(engine.decide(queryContext, mock(RuleMetaData.class)));
            verify(firstDecider).decide(any(), any(), any(), any(), eq(firstRule), any());
            verify(secondDecider).decide(any(), any(), any(), any(), eq(secondRule), any());
        }
    }
    
    @Test
    void assertDecideWithMultipleDatabases() throws SQLException {
        SQLFederationProvider provider = mock(SQLFederationProvider.class);
        SelectStatement statement = mock(SelectStatement.class);
        when(provider.isSupportedSQLStatement(statement)).thenReturn(true);
        QueryContext queryContext = createQueryContext(statement);
        when(queryContext.getSqlStatementContext().getTablesContext().getDatabaseNames()).thenReturn(Arrays.asList("foo_db", "bar_db"));
        ShardingSphereMetaData metaData = createMetaData(provider, new SQLFederationRuleConfiguration(false, cacheOption), Collections.emptyList(), true);
        try (SQLFederationEngine engine = new SQLFederationEngine("foo_db", "foo_schema", metaData, mock(), mock())) {
            assertTrue(engine.decide(queryContext, mock(RuleMetaData.class)));
        }
    }
    
    @Test
    void assertDecideWithExplainStatement() throws SQLException {
        SQLFederationProvider provider = mock(SQLFederationProvider.class);
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(provider.isSupportedSQLStatement(selectStatement)).thenReturn(true);
        ExplainStatement explainStatement = mock(ExplainStatement.class);
        when(explainStatement.getExplainableSQLStatement()).thenReturn(selectStatement);
        ExplainStatementContext statementContext = mock(ExplainStatementContext.class);
        when(statementContext.getSqlStatement()).thenReturn(explainStatement);
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(statementContext);
        ShardingSphereMetaData metaData = createMetaData(provider, new SQLFederationRuleConfiguration(true, cacheOption), Collections.emptyList(), true);
        try (SQLFederationEngine engine = new SQLFederationEngine("foo_db", "foo_schema", metaData, mock(), mock())) {
            assertTrue(engine.decide(queryContext, mock(RuleMetaData.class)));
            verify(provider).isSupportedSQLStatement(selectStatement);
        }
    }
    
    @Test
    void assertGetResultSetForSelectStatement() throws SQLException {
        SQLFederationProvider provider = mock(SQLFederationProvider.class);
        SQLFederationExecutor executor = mock(SQLFederationExecutor.class);
        when(provider.createExecutor(anyString(), anyString(), any(), any(), any())).thenReturn(executor);
        ResultSet expectedResultSet = mock(ResultSet.class);
        SQLFederationContext federationContext = mock(SQLFederationContext.class);
        QueryContext queryContext = createQueryContext(mock(SelectStatement.class));
        when(federationContext.getQueryContext()).thenReturn(queryContext);
        when(executor.executeQuery(any(), any(), eq(federationContext))).thenReturn(expectedResultSet);
        ShardingSphereMetaData metaData = createMetaData(provider, new SQLFederationRuleConfiguration(false, cacheOption), Collections.emptyList(), true);
        try (SQLFederationEngine engine = new SQLFederationEngine("foo_db", "foo_schema", metaData, mock(), mock())) {
            engine.executeQuery(mock(), mock(), federationContext);
            assertThat(engine.getResultSet(), sameInstance(expectedResultSet));
        }
    }
    
    @Test
    void assertGetResultSetForNonSelectStatement() throws SQLException {
        SQLFederationProvider provider = mock(SQLFederationProvider.class);
        SQLFederationExecutor executor = mock(SQLFederationExecutor.class);
        when(provider.createExecutor(anyString(), anyString(), any(), any(), any())).thenReturn(executor);
        SQLFederationContext federationContext = mock(SQLFederationContext.class);
        QueryContext queryContext = createQueryContext(mock(CreateTableStatement.class));
        when(federationContext.getQueryContext()).thenReturn(queryContext);
        when(executor.executeQuery(any(), any(), eq(federationContext))).thenReturn(mock(ResultSet.class));
        ShardingSphereMetaData metaData = createMetaData(provider, new SQLFederationRuleConfiguration(false, cacheOption), Collections.emptyList(), true);
        try (SQLFederationEngine engine = new SQLFederationEngine("foo_db", "foo_schema", metaData, mock(), mock())) {
            engine.executeQuery(mock(), mock(), federationContext);
            assertNull(engine.getResultSet());
        }
    }
    
}
