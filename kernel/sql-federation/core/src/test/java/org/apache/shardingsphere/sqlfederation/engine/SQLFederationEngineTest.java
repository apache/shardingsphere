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

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sqlfederation.api.config.SQLFederationRuleConfiguration;
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
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereMetaData metaData;
    
    @Test
    void assertDecideWhenSelectStatementContainsSystemSchema() throws SQLException {
        Collection<ShardingSphereRule> globalRules =
                Collections.singletonList(new SQLFederationRule(new SQLFederationRuleConfiguration(false, mock(CacheOption.class)), Collections.emptyMap(), mock(ConfigurationProperties.class)));
        SQLFederationEngine engine = createSQLFederationEngine(globalRules, Collections.emptyList());
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.singletonList("information_schema"));
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResourceMetaData.class, RETURNS_DEEP_STUBS), mock(ShardingSphereRuleMetaData.class), Collections.emptyMap());
        assertTrue(engine.decide(sqlStatementContext, Collections.emptyList(), database, mock(ShardingSphereRuleMetaData.class)));
        engine.close();
    }
    
    private SQLFederationEngine createSQLFederationEngine(final Collection<ShardingSphereRule> globalRules, final Collection<ShardingSphereRule> databaseRules) {
        when(metaData.getDatabase(DefaultDatabase.LOGIC_NAME).getRuleMetaData().getRules()).thenReturn(databaseRules);
        when(metaData.getGlobalRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(globalRules));
        return new SQLFederationEngine(DefaultDatabase.LOGIC_NAME, DefaultDatabase.LOGIC_NAME, metaData, mock(ShardingSphereStatistics.class), mock(JDBCExecutor.class));
    }
    
    @Test
    void assertDecideWhenNotConfigSqlFederationEnabled() throws SQLException {
        Collection<ShardingSphereRule> globalRules =
                Collections.singletonList(new SQLFederationRule(new SQLFederationRuleConfiguration(false, mock(CacheOption.class)), Collections.emptyMap(), mock(ConfigurationProperties.class)));
        SQLFederationEngine engine = createSQLFederationEngine(globalRules, Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResourceMetaData.class, RETURNS_DEEP_STUBS), new ShardingSphereRuleMetaData(globalRules), Collections.emptyMap());
        ShardingSphereRuleMetaData globalRuleMetaData = new ShardingSphereRuleMetaData(globalRules);
        assertFalse(engine.decide(mock(CommonSQLStatementContext.class), Collections.emptyList(), database, globalRuleMetaData));
        engine.close();
    }
    
    @Test
    void assertDecideWhenExecuteNotSelectStatement() throws SQLException {
        Collection<ShardingSphereRule> globalRules =
                Collections.singletonList(new SQLFederationRule(new SQLFederationRuleConfiguration(true, mock(CacheOption.class)), Collections.emptyMap(), mock(ConfigurationProperties.class)));
        SQLFederationEngine engine = createSQLFederationEngine(globalRules, Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResourceMetaData.class, RETURNS_DEEP_STUBS), new ShardingSphereRuleMetaData(globalRules), Collections.emptyMap());
        ShardingSphereRuleMetaData globalRuleMetaData = new ShardingSphereRuleMetaData(globalRules);
        assertFalse(engine.decide(mock(CommonSQLStatementContext.class), Collections.emptyList(), database, globalRuleMetaData));
        engine.close();
    }
    
    @Test
    void assertDecideWhenConfigSingleMatchedRule() throws SQLException {
        Collection<ShardingSphereRule> globalRules =
                Collections.singletonList(new SQLFederationRule(new SQLFederationRuleConfiguration(true, mock(CacheOption.class)), Collections.emptyMap(), mock(ConfigurationProperties.class)));
        Collection<ShardingSphereRule> databaseRules = Collections.singletonList(new SQLFederationDeciderRuleMatchFixture());
        SQLFederationEngine engine = createSQLFederationEngine(globalRules, databaseRules);
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResourceMetaData.class, RETURNS_DEEP_STUBS), new ShardingSphereRuleMetaData(globalRules), Collections.emptyMap());
        ShardingSphereRuleMetaData globalRuleMetaData = new ShardingSphereRuleMetaData(globalRules);
        assertTrue(engine.decide(mock(SelectStatementContext.class, RETURNS_DEEP_STUBS), Collections.emptyList(), database, globalRuleMetaData));
        engine.close();
    }
    
    @Test
    void assertDecideWhenConfigSingleNotMatchedRule() throws SQLException {
        Collection<ShardingSphereRule> globalRules =
                Collections.singletonList(new SQLFederationRule(new SQLFederationRuleConfiguration(true, mock(CacheOption.class)), Collections.emptyMap(), mock(ConfigurationProperties.class)));
        Collection<ShardingSphereRule> databaseRules = Collections.singletonList(new SQLFederationDeciderRuleNotMatchFixture());
        SQLFederationEngine engine = createSQLFederationEngine(globalRules, databaseRules);
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResourceMetaData.class, RETURNS_DEEP_STUBS), new ShardingSphereRuleMetaData(databaseRules), Collections.emptyMap());
        assertFalse(engine.decide(mock(SelectStatementContext.class, RETURNS_DEEP_STUBS), Collections.emptyList(), database, new ShardingSphereRuleMetaData(globalRules)));
        engine.close();
    }
    
    @Test
    void assertDecideWhenConfigMultiRule() throws SQLException {
        Collection<ShardingSphereRule> globalRules =
                Collections.singletonList(new SQLFederationRule(new SQLFederationRuleConfiguration(true, mock(CacheOption.class)), Collections.emptyMap(), mock(ConfigurationProperties.class)));
        Collection<ShardingSphereRule> databaseRules = Arrays.asList(new SQLFederationDeciderRuleNotMatchFixture(),
                new SQLFederationDeciderRuleMatchFixture());
        SQLFederationEngine engine = createSQLFederationEngine(globalRules, databaseRules);
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResourceMetaData.class, RETURNS_DEEP_STUBS), new ShardingSphereRuleMetaData(databaseRules), Collections.emptyMap());
        assertTrue(engine.decide(mock(SelectStatementContext.class, RETURNS_DEEP_STUBS), Collections.emptyList(), database, new ShardingSphereRuleMetaData(globalRules)));
        engine.close();
    }
}
