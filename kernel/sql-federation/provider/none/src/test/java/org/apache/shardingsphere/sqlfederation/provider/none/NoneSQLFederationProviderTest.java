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

package org.apache.shardingsphere.sqlfederation.provider.none;

import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationCacheOption;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.context.SQLFederationContext;
import org.apache.shardingsphere.sqlfederation.engine.SQLFederationEngine;
import org.apache.shardingsphere.sqlfederation.exception.SQLFederationProviderNotFoundException;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationProvider;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NoneSQLFederationProviderTest {
    
    @Test
    void assertLoadNoneProvider() {
        assertThat(TypedSPILoader.getService(SQLFederationProvider.class, "NONE"), isA(NoneSQLFederationProvider.class));
    }
    
    @Test
    void assertDefaultCalciteProviderMissingWhenOnlyNoneInstalled() {
        SQLFederationRuleConfiguration config = new SQLFederationRuleConfiguration(true, false, new SQLFederationCacheOption(4, 64L));
        assertThrows(SQLFederationProviderNotFoundException.class, () -> new SQLFederationRule(config, Collections.emptyList()));
    }
    
    @Test
    void assertOrdinarySelectDoesNotUseFederation() throws SQLException {
        SQLFederationRule rule = createRule(false);
        RuleMetaData globalRuleMetaData = new RuleMetaData(Collections.singleton(rule));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        when(metaData.getDatabase("foo_db")).thenReturn(database);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(SelectStatement.class));
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.singleton("foo_db"));
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(queryContext.getUsedDatabase()).thenReturn(database);
        try (SQLFederationEngine engine = new SQLFederationEngine("foo_db", "foo_schema", metaData, mock(ShardingSphereStatistics.class), null)) {
            assertFalse(engine.decide(queryContext, globalRuleMetaData));
        }
    }
    
    @Test
    void assertFederationExecutionFailsClearly() throws SQLException {
        SQLFederationRule rule = createRule(true);
        RuleMetaData globalRuleMetaData = new RuleMetaData(Collections.singleton(rule));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(SelectStatement.class));
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        try (SQLFederationEngine engine = new SQLFederationEngine("foo_db", "foo_schema", metaData, mock(ShardingSphereStatistics.class), null)) {
            assertTrue(engine.decide(queryContext, globalRuleMetaData));
            SQLFederationProviderUnsupportedException actual = assertThrows(SQLFederationProviderUnsupportedException.class,
                    () -> engine.executeQuery(null, null, mock(SQLFederationContext.class)));
            assertThat(actual.getMessage(), is("SQL_FEDERATION-00003: SQL Federation provider 'NONE' does not support federation execution."));
        }
    }
    
    private SQLFederationRule createRule(final boolean allQueryUseSQLFederation) {
        SQLFederationRuleConfiguration config = new SQLFederationRuleConfiguration(true, allQueryUseSQLFederation, new SQLFederationCacheOption(4, 64L), "NONE");
        return new SQLFederationRule(config, Collections.emptyList());
    }
}
