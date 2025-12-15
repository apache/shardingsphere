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

package org.apache.shardingsphere.infra.rewrite.engine;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.engine.result.RouteSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.ParameterFilterable;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.sqltranslator.rule.builder.DefaultSQLTranslatorRuleConfigurationBuilder;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RouteSQLRewriteEngineTest {
    
    @Test
    void assertRewriteWithStandardParameterBuilder() {
        DatabaseType databaseType = mock(DatabaseType.class);
        ShardingSphereDatabase database = mockDatabase(databaseType);
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        QueryContext queryContext = mockQueryContext(sqlStatementContext, "SELECT ?");
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(database, queryContext);
        RouteUnit routeUnit = new RouteUnit(new RouteMapper("ds", "ds_0"), Collections.singletonList(new RouteMapper("tbl", "tbl_0")));
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(routeUnit);
        RouteSQLRewriteResult actual = new RouteSQLRewriteEngine(
                new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build()), database, mock(RuleMetaData.class)).rewrite(sqlRewriteContext, routeContext, queryContext);
        assertThat(actual.getSqlRewriteUnits().size(), is(1));
        assertThat(actual.getSqlRewriteUnits().get(routeUnit).getSql(), is("SELECT ?"));
        assertThat(actual.getSqlRewriteUnits().get(routeUnit).getParameters(), is(Collections.singletonList(1)));
    }
    
    private QueryContext mockQueryContext(final SQLStatementContext sqlStatementContext, final String sql) {
        QueryContext result = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(result.getSql()).thenReturn(sql);
        when(result.getParameters()).thenReturn(Collections.singletonList(1));
        when(result.getHintValueContext()).thenReturn(new HintValueContext());
        return result;
    }
    
    private ShardingSphereDatabase mockDatabase(final DatabaseType databaseType) {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getProtocolType()).thenReturn(databaseType);
        Map<String, StorageUnit> storageUnits = mockStorageUnits(databaseType);
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        when(result.getName()).thenReturn("foo_db");
        when(result.getAllSchemas()).thenReturn(Collections.singleton(new ShardingSphereSchema("test")));
        return result;
    }
    
    @Test
    void assertRewriteWithStandardParameterBuilderWhenNeedAggregateRewrite() {
        SelectStatementContext statementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getOrderByContext().getItems()).thenReturn(Collections.emptyList());
        when(statementContext.getPaginationContext().isHasPagination()).thenReturn(false);
        DatabaseType databaseType = mock(DatabaseType.class);
        when(statementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        ShardingSphereDatabase database = mockDatabase(databaseType);
        QueryContext queryContext = mockQueryContext(statementContext, "SELECT ?");
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(database, queryContext);
        RouteContext routeContext = new RouteContext();
        RouteUnit firstRouteUnit = new RouteUnit(new RouteMapper("ds", "ds_0"), Collections.singletonList(new RouteMapper("tbl", "tbl_0")));
        RouteUnit secondRouteUnit = new RouteUnit(new RouteMapper("ds", "ds_0"), Collections.singletonList(new RouteMapper("tbl", "tbl_1")));
        routeContext.getRouteUnits().add(firstRouteUnit);
        routeContext.getRouteUnits().add(secondRouteUnit);
        RouteSQLRewriteResult actual = new RouteSQLRewriteEngine(
                new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build()), database, mock(RuleMetaData.class)).rewrite(sqlRewriteContext, routeContext, queryContext);
        assertThat(actual.getSqlRewriteUnits().size(), is(1));
        assertThat(actual.getSqlRewriteUnits().values().iterator().next().getSql(), is("SELECT ? UNION ALL SELECT ?"));
        assertThat(actual.getSqlRewriteUnits().values().iterator().next().getParameters(), is(Arrays.asList(1, 1)));
    }
    
    @Test
    void assertRewriteWithGroupedParameterBuilderForBroadcast() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getTablesContext().getDatabaseName().isPresent()).thenReturn(false);
        when(statementContext.getInsertSelectContext()).thenReturn(null);
        when(statementContext.getGroupedParameters()).thenReturn(Collections.singletonList(Collections.singletonList(1)));
        when(statementContext.getOnDuplicateKeyUpdateParameters()).thenReturn(Collections.emptyList());
        DatabaseType databaseType = mock(DatabaseType.class);
        when(statementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        ShardingSphereDatabase database = mockDatabase(databaseType);
        QueryContext queryContext = mockQueryContext(statementContext, "INSERT INTO tbl VALUES (?)");
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(database, queryContext);
        RouteUnit routeUnit = new RouteUnit(new RouteMapper("ds", "ds_0"), Collections.singletonList(new RouteMapper("tbl", "tbl_0")));
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(routeUnit);
        RouteSQLRewriteResult actual = new RouteSQLRewriteEngine(
                new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build()), database, mock(RuleMetaData.class)).rewrite(sqlRewriteContext, routeContext, queryContext);
        assertThat(actual.getSqlRewriteUnits().size(), is(1));
        assertThat(actual.getSqlRewriteUnits().get(routeUnit).getSql(), is("INSERT INTO tbl VALUES (?)"));
        assertThat(actual.getSqlRewriteUnits().get(routeUnit).getParameters(), is(Collections.singletonList(1)));
    }
    
    @Test
    void assertRewriteWithGroupedParameterBuilderForRouteWithSameDataNode() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getTablesContext().getDatabaseName().isPresent()).thenReturn(false);
        when(statementContext.getInsertSelectContext()).thenReturn(null);
        when(statementContext.getGroupedParameters()).thenReturn(Collections.singletonList(Collections.singletonList(1)));
        when(statementContext.getOnDuplicateKeyUpdateParameters()).thenReturn(Collections.emptyList());
        DatabaseType databaseType = mock(DatabaseType.class);
        when(statementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        ShardingSphereDatabase database = mockDatabase(databaseType);
        QueryContext queryContext = mockQueryContext(statementContext, "INSERT INTO tbl VALUES (?)");
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(database, queryContext);
        RouteUnit routeUnit = new RouteUnit(new RouteMapper("ds", "ds_0"), Collections.singletonList(new RouteMapper("tbl", "tbl_0")));
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(routeUnit);
        // TODO check why data node is "ds.tbl_0", not "ds_0.tbl_0"
        routeContext.getOriginalDataNodes().add(Collections.singletonList(new DataNode("ds.tbl_0")));
        RouteSQLRewriteResult actual = new RouteSQLRewriteEngine(
                new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build()), database, mock(RuleMetaData.class)).rewrite(sqlRewriteContext, routeContext, queryContext);
        assertThat(actual.getSqlRewriteUnits().size(), is(1));
        assertThat(actual.getSqlRewriteUnits().get(routeUnit).getSql(), is("INSERT INTO tbl VALUES (?)"));
        assertThat(actual.getSqlRewriteUnits().get(routeUnit).getParameters(), is(Collections.singletonList(1)));
    }
    
    @Test
    void assertRewriteWithGroupedParameterBuilderForRouteWithEmptyDataNode() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getTablesContext().getDatabaseName().isPresent()).thenReturn(false);
        when(statementContext.getInsertSelectContext()).thenReturn(null);
        when(statementContext.getGroupedParameters()).thenReturn(Collections.singletonList(Collections.singletonList(1)));
        when(statementContext.getOnDuplicateKeyUpdateParameters()).thenReturn(Collections.emptyList());
        DatabaseType databaseType = mock(DatabaseType.class);
        when(statementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        ShardingSphereDatabase database = mockDatabase(databaseType);
        QueryContext queryContext = mockQueryContext(statementContext, "INSERT INTO tbl VALUES (?)");
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(database, queryContext);
        RouteUnit routeUnit = new RouteUnit(new RouteMapper("ds", "ds_0"), Collections.singletonList(new RouteMapper("tbl", "tbl_0")));
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(routeUnit);
        routeContext.getOriginalDataNodes().add(Collections.emptyList());
        RouteSQLRewriteResult actual = new RouteSQLRewriteEngine(
                new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build()), database, mock(RuleMetaData.class)).rewrite(sqlRewriteContext, routeContext, queryContext);
        assertThat(actual.getSqlRewriteUnits().size(), is(1));
        assertThat(actual.getSqlRewriteUnits().get(routeUnit).getSql(), is("INSERT INTO tbl VALUES (?)"));
        assertThat(actual.getSqlRewriteUnits().get(routeUnit).getParameters(), is(Collections.singletonList(1)));
    }
    
    @Test
    void assertRewriteWithGroupedParameterBuilderForRouteWithNotSameDataNode() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getTablesContext().getDatabaseName().isPresent()).thenReturn(false);
        when(statementContext.getInsertSelectContext()).thenReturn(null);
        when(statementContext.getGroupedParameters()).thenReturn(Collections.singletonList(Collections.singletonList(1)));
        when(statementContext.getOnDuplicateKeyUpdateParameters()).thenReturn(Collections.emptyList());
        DatabaseType databaseType = mock(DatabaseType.class);
        when(statementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        ShardingSphereDatabase database = mockDatabase(databaseType);
        QueryContext queryContext = mockQueryContext(statementContext, "INSERT INTO tbl VALUES (?)");
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(database, queryContext);
        RouteUnit routeUnit = new RouteUnit(new RouteMapper("ds", "ds_0"), Collections.singletonList(new RouteMapper("tbl", "tbl_0")));
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(routeUnit);
        routeContext.getOriginalDataNodes().add(Collections.singletonList(new DataNode("ds_1.tbl_1")));
        RouteSQLRewriteResult actual = new RouteSQLRewriteEngine(
                new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build()), database, mock(RuleMetaData.class)).rewrite(sqlRewriteContext, routeContext, queryContext);
        assertThat(actual.getSqlRewriteUnits().size(), is(1));
        assertThat(actual.getSqlRewriteUnits().get(routeUnit).getSql(), is("INSERT INTO tbl VALUES (?)"));
        assertTrue(actual.getSqlRewriteUnits().get(routeUnit).getParameters().isEmpty());
    }
    
    private Map<String, StorageUnit> mockStorageUnits(final DatabaseType databaseType) {
        StorageUnit result = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(result.getStorageType()).thenReturn(databaseType);
        return Collections.singletonMap("ds_0", result);
    }
    
    /**
     * Helper method to inject test tokens into SQLRewriteContext.
     */
    @SuppressWarnings("unchecked")
    private void addTokenToContext(final SQLRewriteContext context, final SQLToken token) throws Exception {
        Field sqlTokensField = SQLRewriteContext.class.getDeclaredField("sqlTokens");
        sqlTokensField.setAccessible(true);
        List<SQLToken> sqlTokens = (List<SQLToken>) sqlTokensField.get(context);
        sqlTokens.add(token);
    }
    
    /**
     * Test that parameter filtering has zero performance impact when no ParameterFilterable tokens exist.
     *
     * Test scenario:
     * - Original parameters: [1, 2, 3]
     * - No filterable tokens
     * - Expected result: all parameters preserved [1, 2, 3]
     */
    @Test
    void assertRewriteWithNoFilterableTokens() {
        DatabaseType databaseType = mock(DatabaseType.class);
        ShardingSphereDatabase database = mockDatabase(databaseType);
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        QueryContext queryContext = mockQueryContext(sqlStatementContext, "SELECT ?, ?, ?");
        when(queryContext.getParameters()).thenReturn(Arrays.asList(1, 2, 3));
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(database, queryContext);
        RouteUnit routeUnit = new RouteUnit(new RouteMapper("ds", "ds_0"), Collections.singletonList(new RouteMapper("tbl", "tbl_0")));
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(routeUnit);
        RouteSQLRewriteResult actual = new RouteSQLRewriteEngine(
                new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build()), database, mock(RuleMetaData.class)).rewrite(sqlRewriteContext, routeContext, queryContext);
        assertThat(actual.getSqlRewriteUnits().get(routeUnit).getParameters(), is(Arrays.asList(1, 2, 3)));
    }
    
    /**
     * Test parameter filtering with a single ParameterFilterable token.
     *
     * Test scenario:
     * - Original parameters: [1, 2, 3]
     * - Single token removes index 1 (value 2)
     * - Expected result: [1, 3]
     */
    @Test
    void assertRewriteWithSingleFilterableToken() throws Exception {
        DatabaseType databaseType = mock(DatabaseType.class);
        ShardingSphereDatabase database = mockDatabase(databaseType);
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        QueryContext queryContext = mockQueryContext(sqlStatementContext, "SELECT ?, ?, ?");
        when(queryContext.getParameters()).thenReturn(Arrays.asList(1, 2, 3));
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(database, queryContext);
        RouteUnit routeUnit = new RouteUnit(new RouteMapper("ds", "ds_0"), Collections.singletonList(new RouteMapper("tbl", "tbl_0")));
        TestParameterFilterableToken filterableToken = new TestParameterFilterableToken(0, 10);
        Set<Integer> removedIndices = new HashSet<>();
        removedIndices.add(1);
        filterableToken.setRemovedIndices(routeUnit, removedIndices);
        addTokenToContext(sqlRewriteContext, filterableToken);
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(routeUnit);
        RouteSQLRewriteResult actual = new RouteSQLRewriteEngine(
                new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build()), database, mock(RuleMetaData.class)).rewrite(sqlRewriteContext, routeContext, queryContext);
        assertThat(actual.getSqlRewriteUnits().get(routeUnit).getParameters(), is(Arrays.asList(1, 3)));
    }
    
    /**
     * Test parameter filtering with multiple ParameterFilterable tokens.
     *
     * Test scenario:
     * - Original parameters: [1, 2, 3, 4, 5]
     * - First token removes indices {1, 3} (values 2, 4)
     * - Second token removes indices {0, 3} (values 1, 4)
     * - Merged removed indices: {0, 1, 3} (values 1, 2, 4)
     * - Expected result: [3, 5]
     */
    @Test
    void assertRewriteWithMultipleFilterableTokens() throws Exception {
        DatabaseType databaseType = mock(DatabaseType.class);
        ShardingSphereDatabase database = mockDatabase(databaseType);
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        QueryContext queryContext = mockQueryContext(sqlStatementContext, "SELECT ?, ?, ?, ?, ?");
        when(queryContext.getParameters()).thenReturn(Arrays.asList(1, 2, 3, 4, 5));
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(database, queryContext);
        RouteUnit routeUnit = new RouteUnit(new RouteMapper("ds", "ds_0"), Collections.singletonList(new RouteMapper("tbl", "tbl_0")));
        TestParameterFilterableToken firstToken = new TestParameterFilterableToken(0, 10);
        Set<Integer> firstRemovedIndices = new HashSet<>();
        firstRemovedIndices.add(1);
        firstRemovedIndices.add(3);
        firstToken.setRemovedIndices(routeUnit, firstRemovedIndices);
        TestParameterFilterableToken secondToken = new TestParameterFilterableToken(11, 20);
        Set<Integer> secondRemovedIndices = new HashSet<>();
        secondRemovedIndices.add(0);
        secondRemovedIndices.add(3);
        secondToken.setRemovedIndices(routeUnit, secondRemovedIndices);
        addTokenToContext(sqlRewriteContext, firstToken);
        addTokenToContext(sqlRewriteContext, secondToken);
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(routeUnit);
        RouteSQLRewriteResult actual = new RouteSQLRewriteEngine(
                new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build()), database, mock(RuleMetaData.class)).rewrite(sqlRewriteContext, routeContext, queryContext);
        assertThat(actual.getSqlRewriteUnits().get(routeUnit).getParameters(), is(Arrays.asList(3, 5)));
    }
    
    /**
     * Test that parameter order is preserved after filtering.
     *
     * Test scenario:
     * - Original parameters: ["a", "b", "c", "d", "e"]
     * - Token removes indices {1, 3} (values "b", "d")
     * - Expected result: ["a", "c", "e"] - order preserved
     */
    @Test
    void assertRewriteWithParameterOrderPreservation() throws Exception {
        DatabaseType databaseType = mock(DatabaseType.class);
        ShardingSphereDatabase database = mockDatabase(databaseType);
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        QueryContext queryContext = mockQueryContext(sqlStatementContext, "SELECT ?, ?, ?, ?, ?");
        when(queryContext.getParameters()).thenReturn(Arrays.asList("a", "b", "c", "d", "e"));
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(database, queryContext);
        RouteUnit routeUnit = new RouteUnit(new RouteMapper("ds", "ds_0"), Collections.singletonList(new RouteMapper("tbl", "tbl_0")));
        TestParameterFilterableToken filterableToken = new TestParameterFilterableToken(0, 10);
        Set<Integer> removedIndices = new HashSet<>();
        removedIndices.add(1);
        removedIndices.add(3);
        filterableToken.setRemovedIndices(routeUnit, removedIndices);
        addTokenToContext(sqlRewriteContext, filterableToken);
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(routeUnit);
        RouteSQLRewriteResult actual = new RouteSQLRewriteEngine(
                new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build()), database, mock(RuleMetaData.class)).rewrite(sqlRewriteContext, routeContext, queryContext);
        assertThat(actual.getSqlRewriteUnits().get(routeUnit).getParameters(), is(Arrays.asList("a", "c", "e")));
    }
    
    /**
     * Test implementation of ParameterFilterable for unit testing.
     * This mock token allows tests to specify which parameter indices should be removed for each route unit.
     * In production, this will be implemented by ShardingInPredicateToken to filter IN clause parameters.
     */
    private static class TestParameterFilterableToken extends SQLToken implements ParameterFilterable {
        
        private final Map<RouteUnit, Set<Integer>> removedIndicesMap = new HashMap<>();
        
        TestParameterFilterableToken(final int startIndex, final int stopIndex) {
            super(startIndex);
            this.stopIndex = stopIndex;
        }
        
        private final int stopIndex;
        
        @Override
        public int getStopIndex() {
            return stopIndex;
        }
        
        @Override
        public Set<Integer> getRemovedParameterIndices(final RouteUnit routeUnit) {
            return removedIndicesMap.getOrDefault(routeUnit, Collections.emptySet());
        }
        
        void setRemovedIndices(final RouteUnit routeUnit, final Set<Integer> indices) {
            removedIndicesMap.put(routeUnit, indices);
        }
    }
}
