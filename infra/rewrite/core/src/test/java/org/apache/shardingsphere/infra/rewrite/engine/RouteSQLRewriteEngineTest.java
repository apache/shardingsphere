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
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.sqltranslator.rule.builder.DefaultSQLTranslatorRuleConfigurationBuilder;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

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
}
