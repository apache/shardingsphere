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

package org.apache.shardingsphere.sqlfederation.decider.engine;

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sqlfederation.api.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.decider.SQLFederationDecideEngine;
import org.apache.shardingsphere.sqlfederation.decider.fixture.rule.SQLFederationDeciderRuleMatchFixture;
import org.apache.shardingsphere.sqlfederation.decider.fixture.rule.SQLFederationDeciderRuleNotMatchFixture;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SQLFederationDecideEngineTest {
    
    @Test
    void assertDecideWhenSelectStatementContainsSystemSchema() {
        SQLFederationDecideEngine engine = new SQLFederationDecideEngine(Collections.emptyList());
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.singletonList("information_schema"));
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResourceMetaData.class, RETURNS_DEEP_STUBS), mock(ShardingSphereRuleMetaData.class), Collections.emptyMap());
        assertTrue(engine.decide(sqlStatementContext, Collections.emptyList(), database, mock(ShardingSphereRuleMetaData.class)));
    }
    
    @Test
    void assertDecideWhenNotConfigSqlFederationEnabled() {
        Collection<ShardingSphereRule> rules = Collections.singletonList(new SQLFederationRule(new SQLFederationRuleConfiguration(false, mock(CacheOption.class))));
        SQLFederationDecideEngine engine = new SQLFederationDecideEngine(rules);
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResourceMetaData.class, RETURNS_DEEP_STUBS), new ShardingSphereRuleMetaData(rules), Collections.emptyMap());
        ShardingSphereRuleMetaData globalRuleMetaData = new ShardingSphereRuleMetaData(rules);
        assertFalse(engine.decide(mock(CommonSQLStatementContext.class), Collections.emptyList(), database, globalRuleMetaData));
    }
    
    @Test
    void assertDecideWhenExecuteNotSelectStatement() {
        Collection<ShardingSphereRule> rules = Collections.singletonList(new SQLFederationRule(new SQLFederationRuleConfiguration(true, mock(CacheOption.class))));
        SQLFederationDecideEngine engine = new SQLFederationDecideEngine(rules);
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResourceMetaData.class, RETURNS_DEEP_STUBS), new ShardingSphereRuleMetaData(rules), Collections.emptyMap());
        ShardingSphereRuleMetaData globalRuleMetaData = new ShardingSphereRuleMetaData(rules);
        assertFalse(engine.decide(mock(CommonSQLStatementContext.class), Collections.emptyList(), database, globalRuleMetaData));
    }
    
    @Test
    void assertDecideWhenConfigSingleMatchedRule() {
        Collection<ShardingSphereRule> rules = Arrays.asList(new SQLFederationRule(new SQLFederationRuleConfiguration(true, mock(CacheOption.class))), new SQLFederationDeciderRuleMatchFixture());
        SQLFederationDecideEngine engine = new SQLFederationDecideEngine(rules);
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResourceMetaData.class, RETURNS_DEEP_STUBS), new ShardingSphereRuleMetaData(rules), Collections.emptyMap());
        ShardingSphereRuleMetaData globalRuleMetaData = new ShardingSphereRuleMetaData(rules);
        assertTrue(engine.decide(mock(SelectStatementContext.class, RETURNS_DEEP_STUBS), Collections.emptyList(), database, globalRuleMetaData));
    }
    
    @Test
    void assertDecideWhenConfigSingleNotMatchedRule() {
        Collection<ShardingSphereRule> rules = Arrays.asList(new SQLFederationRule(new SQLFederationRuleConfiguration(true, mock(CacheOption.class))), new SQLFederationDeciderRuleNotMatchFixture());
        SQLFederationDecideEngine engine = new SQLFederationDecideEngine(rules);
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResourceMetaData.class, RETURNS_DEEP_STUBS), new ShardingSphereRuleMetaData(rules), Collections.emptyMap());
        ShardingSphereRuleMetaData globalRuleMetaData = new ShardingSphereRuleMetaData(rules);
        assertFalse(engine.decide(mock(SelectStatementContext.class, RETURNS_DEEP_STUBS), Collections.emptyList(), database, globalRuleMetaData));
    }
    
    @Test
    void assertDecideWhenConfigMultiRule() {
        Collection<ShardingSphereRule> rules = Arrays.asList(new SQLFederationRule(new SQLFederationRuleConfiguration(true, mock(CacheOption.class))), new SQLFederationDeciderRuleNotMatchFixture(),
                new SQLFederationDeciderRuleMatchFixture());
        SQLFederationDecideEngine engine = new SQLFederationDecideEngine(rules);
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResourceMetaData.class, RETURNS_DEEP_STUBS), new ShardingSphereRuleMetaData(rules), Collections.emptyMap());
        ShardingSphereRuleMetaData globalRuleMetaData = new ShardingSphereRuleMetaData(rules);
        assertTrue(engine.decide(mock(SelectStatementContext.class, RETURNS_DEEP_STUBS), Collections.emptyList(), database, globalRuleMetaData));
    }
}
