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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationCacheOption;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.engine.fixture.rule.SQLFederationDeciderRuleMatchFixture;
import org.apache.shardingsphere.sqlfederation.engine.fixture.rule.SQLFederationDeciderRuleNotMatchFixture;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SQLFederationEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereMetaData metaData;
    
    @Test
    void assertDecideWhenNotConfigSqlFederationEnabled() throws SQLException {
        Collection<ShardingSphereRule> globalRules =
                Collections
                        .singletonList(new SQLFederationRule(new SQLFederationRuleConfiguration(false, false, new SQLFederationCacheOption(1, 1)), Collections.emptyList()));
        SQLFederationEngine engine = createSQLFederationEngine(globalRules, Collections.emptyList());
        RuleMetaData globalRuleMetaData = new RuleMetaData(globalRules);
        assertFalse(engine.decide(mock(QueryContext.class), globalRuleMetaData));
        engine.close();
    }
    
    private SQLFederationEngine createSQLFederationEngine(final Collection<ShardingSphereRule> globalRules, final Collection<ShardingSphereRule> databaseRules) {
        when(metaData.getDatabase("foo_db").getRuleMetaData().getRules()).thenReturn(databaseRules);
        when(metaData.getGlobalRuleMetaData()).thenReturn(new RuleMetaData(globalRules));
        return new SQLFederationEngine("foo_db", "foo_db", metaData, mock(ShardingSphereStatistics.class), mock(JDBCExecutor.class));
    }
    
    @Test
    void assertDecideWhenConfigAllQueryUseSQLFederation() throws SQLException {
        Collection<ShardingSphereRule> globalRules =
                Collections.singletonList(new SQLFederationRule(new SQLFederationRuleConfiguration(true, true, new SQLFederationCacheOption(1, 1)), Collections.emptyList()));
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        when(selectStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        SQLFederationEngine engine = createSQLFederationEngine(globalRules, Collections.emptyList());
        RuleMetaData globalRuleMetaData = new RuleMetaData(globalRules);
        assertTrue(engine.decide(queryContext, globalRuleMetaData));
        engine.close();
    }
    
    @Test
    void assertDecideWhenExecuteNotSelectStatement() throws SQLException {
        Collection<ShardingSphereRule> globalRules =
                Collections.singletonList(new SQLFederationRule(new SQLFederationRuleConfiguration(true, false, new SQLFederationCacheOption(1, 1)), Collections.emptyList()));
        SQLFederationEngine engine = createSQLFederationEngine(globalRules, Collections.emptyList());
        RuleMetaData globalRuleMetaData = new RuleMetaData(globalRules);
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext().getSqlStatement()).thenReturn(mock(CreateTableStatement.class));
        assertFalse(engine.decide(queryContext, globalRuleMetaData));
        engine.close();
    }
    
    @Test
    void assertDecideWhenConfigSingleMatchedRule() throws SQLException {
        Collection<ShardingSphereRule> globalRules =
                Collections.singletonList(new SQLFederationRule(new SQLFederationRuleConfiguration(true, false, new SQLFederationCacheOption(1, 1)), Collections.emptyList()));
        Collection<ShardingSphereRule> databaseRules = Collections.singletonList(new SQLFederationDeciderRuleMatchFixture());
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db",
                databaseType, mock(ResourceMetaData.class, RETURNS_DEEP_STUBS), new RuleMetaData(globalRules), Collections.emptyList());
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        when(selectStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.singleton("foo_db"));
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        when(queryContext.getUsedDatabase()).thenReturn(database);
        SQLFederationEngine engine = createSQLFederationEngine(globalRules, databaseRules);
        RuleMetaData globalRuleMetaData = new RuleMetaData(globalRules);
        assertTrue(engine.decide(queryContext, globalRuleMetaData));
        engine.close();
    }
    
    @Test
    void assertDecideWhenConfigSingleNotMatchedRule() throws SQLException {
        Collection<ShardingSphereRule> globalRules =
                Collections.singletonList(new SQLFederationRule(new SQLFederationRuleConfiguration(true, false, new SQLFederationCacheOption(1, 1)), Collections.emptyList()));
        Collection<ShardingSphereRule> databaseRules = Collections.singletonList(new SQLFederationDeciderRuleNotMatchFixture());
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db",
                databaseType, mock(ResourceMetaData.class, RETURNS_DEEP_STUBS), new RuleMetaData(databaseRules), Collections.emptyList());
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        when(selectStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.singleton("foo_db"));
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        when(queryContext.getUsedDatabase()).thenReturn(database);
        SQLFederationEngine engine = createSQLFederationEngine(globalRules, databaseRules);
        assertFalse(engine.decide(queryContext, new RuleMetaData(globalRules)));
        engine.close();
    }
    
    @Test
    void assertDecideWhenConfigMultiRule() throws SQLException {
        Collection<ShardingSphereRule> globalRules =
                Collections.singletonList(new SQLFederationRule(new SQLFederationRuleConfiguration(true, false, new SQLFederationCacheOption(1, 1)), Collections.emptyList()));
        Collection<ShardingSphereRule> databaseRules = Arrays.asList(new SQLFederationDeciderRuleNotMatchFixture(),
                new SQLFederationDeciderRuleMatchFixture());
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db",
                databaseType, mock(ResourceMetaData.class, RETURNS_DEEP_STUBS), new RuleMetaData(databaseRules), Collections.emptyList());
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        when(selectStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.singleton("foo_db"));
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(selectStatementContext);
        when(queryContext.getParameters()).thenReturn(Collections.emptyList());
        when(queryContext.getUsedDatabase()).thenReturn(database);
        SQLFederationEngine engine = createSQLFederationEngine(globalRules, databaseRules);
        assertTrue(engine.decide(queryContext, new RuleMetaData(globalRules)));
        engine.close();
    }
}
