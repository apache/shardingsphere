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

package org.apache.shardingsphere.infra.binder.decider.engine;

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.decider.context.SQLFederationDeciderContext;
import org.apache.shardingsphere.infra.binder.decider.fixture.rule.SQLFederationDeciderRuleMatchFixture;
import org.apache.shardingsphere.infra.binder.decider.fixture.rule.SQLFederationDeciderRuleNotMatchFixture;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SQLFederationDeciderEngineTest {
    
    @Test
    public void assertDecideWhenSelectStatementContainsSystemSchema() {
        SQLFederationDeciderEngine deciderEngine = new SQLFederationDeciderEngine(Collections.emptyList(), new ConfigurationProperties(new Properties()));
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.singletonList("information_schema"));
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS), mock(ShardingSphereRuleMetaData.class), Collections.emptyMap());
        SQLFederationDeciderContext actual = deciderEngine.decide(queryContext, database);
        assertTrue(actual.isUseSQLFederation());
    }
    
    @Test
    public void assertDecideWhenNotConfigSqlFederationEnabled() {
        Collection<ShardingSphereRule> rules = Collections.singletonList(new SQLFederationDeciderRuleMatchFixture());
        SQLFederationDeciderEngine deciderEngine = new SQLFederationDeciderEngine(rules, new ConfigurationProperties(new Properties()));
        QueryContext queryContext = new QueryContext(mock(CommonSQLStatementContext.class), "", Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS), new ShardingSphereRuleMetaData(rules), Collections.emptyMap());
        SQLFederationDeciderContext actual = deciderEngine.decide(queryContext, database);
        assertFalse(actual.isUseSQLFederation());
    }
    
    @Test
    public void assertDecideWhenExecuteNotSelectStatement() {
        Collection<ShardingSphereRule> rules = Collections.singletonList(new SQLFederationDeciderRuleMatchFixture());
        Properties props = new Properties();
        props.put(ConfigurationPropertyKey.SQL_FEDERATION_ENABLED.getKey(), true);
        SQLFederationDeciderEngine deciderEngine = new SQLFederationDeciderEngine(rules, new ConfigurationProperties(props));
        QueryContext queryContext = new QueryContext(mock(CommonSQLStatementContext.class), "", Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS), new ShardingSphereRuleMetaData(rules), Collections.emptyMap());
        SQLFederationDeciderContext actual = deciderEngine.decide(queryContext, database);
        assertFalse(actual.isUseSQLFederation());
    }
    
    @Test
    public void assertDecideWhenConfigSingleMatchedRule() {
        Collection<ShardingSphereRule> rules = Collections.singletonList(new SQLFederationDeciderRuleMatchFixture());
        Properties props = new Properties();
        props.put(ConfigurationPropertyKey.SQL_FEDERATION_ENABLED.getKey(), true);
        SQLFederationDeciderEngine deciderEngine = new SQLFederationDeciderEngine(rules, new ConfigurationProperties(props));
        QueryContext queryContext = new QueryContext(mock(SelectStatementContext.class, RETURNS_DEEP_STUBS), "", Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS), new ShardingSphereRuleMetaData(rules), Collections.emptyMap());
        SQLFederationDeciderContext actual = deciderEngine.decide(queryContext, database);
        assertTrue(actual.isUseSQLFederation());
    }
    
    @Test
    public void assertDecideWhenConfigSingleNotMatchedRule() {
        Collection<ShardingSphereRule> rules = Collections.singletonList(new SQLFederationDeciderRuleNotMatchFixture());
        Properties props = new Properties();
        props.put(ConfigurationPropertyKey.SQL_FEDERATION_ENABLED.getKey(), true);
        SQLFederationDeciderEngine deciderEngine = new SQLFederationDeciderEngine(rules, new ConfigurationProperties(props));
        QueryContext queryContext = new QueryContext(mock(SelectStatementContext.class, RETURNS_DEEP_STUBS), "", Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS), new ShardingSphereRuleMetaData(rules), Collections.emptyMap());
        SQLFederationDeciderContext actual = deciderEngine.decide(queryContext, database);
        assertFalse(actual.isUseSQLFederation());
    }
    
    @Test
    public void assertDecideWhenConfigMultiRule() {
        Collection<ShardingSphereRule> rules = Arrays.asList(new SQLFederationDeciderRuleNotMatchFixture(), new SQLFederationDeciderRuleMatchFixture());
        Properties props = new Properties();
        props.put(ConfigurationPropertyKey.SQL_FEDERATION_ENABLED.getKey(), true);
        SQLFederationDeciderEngine deciderEngine = new SQLFederationDeciderEngine(rules, new ConfigurationProperties(props));
        QueryContext queryContext = new QueryContext(mock(SelectStatementContext.class, RETURNS_DEEP_STUBS), "", Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS), new ShardingSphereRuleMetaData(rules), Collections.emptyMap());
        SQLFederationDeciderContext actual = deciderEngine.decide(queryContext, database);
        assertTrue(actual.isUseSQLFederation());
    }
}
