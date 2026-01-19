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

package org.apache.shardingsphere.infra.executor.sql.context;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rewrite.engine.result.GenericSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.RouteSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteUnit;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExecutionContextBuilderTest {
    
    @Test
    void assertBuildGenericSQLRewriteResultWithInstanceDataSourceNames() {
        GenericSQLRewriteResult genericSQLRewriteResult = new GenericSQLRewriteResult(new SQLRewriteUnit("sql", Collections.singletonList("foo_param")));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData().getAllInstanceDataSourceNames()).thenReturn(Collections.emptyList());
        assertTrue(ExecutionContextBuilder.build(database, genericSQLRewriteResult, mock(SQLStatementContext.class)).isEmpty());
    }
    
    @Test
    void assertBuildGenericSQLRewriteResultWithoutTableAvailableSQLStatement() {
        String sql = "sql";
        GenericSQLRewriteResult genericSQLRewriteResult = new GenericSQLRewriteResult(new SQLRewriteUnit(sql, Collections.singletonList("foo_param")));
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class);
        String firstDataSourceName = "firstDataSourceName";
        when(resourceMetaData.getAllInstanceDataSourceNames()).thenReturn(Arrays.asList(firstDataSourceName, "lastDataSourceName"));
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resourceMetaData, ruleMetaData, buildSchemas());
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        Collection<ExecutionUnit> actual = ExecutionContextBuilder.build(database, genericSQLRewriteResult, sqlStatementContext);
        Collection<ExecutionUnit> expected = Collections.singletonList(new ExecutionUnit(firstDataSourceName, new SQLUnit(sql, Collections.singletonList("foo_param"))));
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertBuildGenericSQLRewriteResultWithTableAvailableSQLStatement() {
        String sql = "sql";
        GenericSQLRewriteResult genericSQLRewriteResult = new GenericSQLRewriteResult(new SQLRewriteUnit(sql, Collections.singletonList("foo_param")));
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class);
        String firstDataSourceName = "firstDataSourceName";
        when(resourceMetaData.getAllInstanceDataSourceNames()).thenReturn(Arrays.asList(firstDataSourceName, "lastDataSourceName"));
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resourceMetaData, ruleMetaData, buildSchemas());
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        Collection<ExecutionUnit> actual = ExecutionContextBuilder.build(database, genericSQLRewriteResult, sqlStatementContext);
        Collection<ExecutionUnit> expected = Collections.singletonList(new ExecutionUnit(firstDataSourceName, new SQLUnit(sql, Collections.singletonList("foo_param"))));
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertBuildRouteSQLRewriteResult() {
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.emptyMap());
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resourceMetaData, ruleMetaData, buildSchemas());
        Collection<ExecutionUnit> actual = ExecutionContextBuilder.build(database, new RouteSQLRewriteResult(createRouteUnitSQLRewriteUnitMap()), mock(SQLStatementContext.class));
        ExecutionUnit expectedUnit1 = new ExecutionUnit("actual_db_1", new SQLUnit("sql1", Collections.singletonList("parameter1")));
        ExecutionUnit expectedUnit2 = new ExecutionUnit("actual_db_2", new SQLUnit("sql2", Collections.singletonList("parameter2")));
        Collection<ExecutionUnit> expected = new LinkedHashSet<>(2, 1F);
        expected.add(expectedUnit1);
        expected.add(expectedUnit2);
        assertThat(actual, is(expected));
    }
    
    private Map<RouteUnit, SQLRewriteUnit> createRouteUnitSQLRewriteUnitMap() {
        RouteUnit routeUnit1 = new RouteUnit(new RouteMapper("foo_db_1", "actual_db_1"), Collections.singletonList(new RouteMapper("foo_tbl", "actual_tbl")));
        SQLRewriteUnit sqlRewriteUnit1 = new SQLRewriteUnit("sql1", Collections.singletonList("parameter1"));
        RouteUnit routeUnit2 = new RouteUnit(new RouteMapper("foo_db_2", "actual_db_2"), Collections.singletonList(new RouteMapper("foo_tbl", "actual_tbl")));
        SQLRewriteUnit sqlRewriteUnit2 = new SQLRewriteUnit("sql2", Collections.singletonList("parameter2"));
        Map<RouteUnit, SQLRewriteUnit> result = new HashMap<>(2, 1F);
        result.put(routeUnit1, sqlRewriteUnit1);
        result.put(routeUnit2, sqlRewriteUnit2);
        return result;
    }
    
    @Test
    void assertBuildRouteSQLRewriteResultWithEmptyPrimaryKeyMeta() {
        RouteUnit routeUnit2 = new RouteUnit(new RouteMapper("logicName2", "actualName2"), Collections.singletonList(new RouteMapper("logicName2", "actualName2")));
        SQLRewriteUnit sqlRewriteUnit2 = new SQLRewriteUnit("sql2", Collections.singletonList("parameter2"));
        Map<RouteUnit, SQLRewriteUnit> sqlRewriteUnits = new HashMap<>(2, 1F);
        sqlRewriteUnits.put(routeUnit2, sqlRewriteUnit2);
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.emptyMap());
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resourceMetaData, ruleMetaData, buildSchemasWithoutPrimaryKey());
        Collection<ExecutionUnit> actual = ExecutionContextBuilder.build(database, new RouteSQLRewriteResult(sqlRewriteUnits), mock(SQLStatementContext.class));
        ExecutionUnit expectedUnit2 = new ExecutionUnit("actualName2", new SQLUnit("sql2", Collections.singletonList("parameter2")));
        assertThat(actual, is(Collections.singleton(expectedUnit2)));
    }
    
    private Collection<ShardingSphereSchema> buildSchemas() {
        Collection<ShardingSphereTable> tables = new LinkedList<>();
        tables.add(new ShardingSphereTable("logicName1", Arrays.asList(new ShardingSphereColumn("order_id", Types.INTEGER, true, false, "int", false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, "int", false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, "int", false, true, false, false)), Collections.emptySet(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("logicName2", Arrays.asList(new ShardingSphereColumn("item_id", Types.INTEGER, true, false, "int", false, true, false, false),
                new ShardingSphereColumn("order_id", Types.INTEGER, false, false, "int", false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, "int", false, true, false, false),
                new ShardingSphereColumn("status", Types.VARCHAR, false, false, "varchar", false, true, false, false),
                new ShardingSphereColumn("c_date", Types.TIMESTAMP, false, false, "timestamp", false, true, false, false)), Collections.emptySet(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_other", Collections.singletonList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, "int", false, true, false, false)), Collections.emptySet(), Collections.emptyList()));
        return Collections.singleton(new ShardingSphereSchema("name", tables, Collections.emptyList()));
    }
    
    private Collection<ShardingSphereSchema> buildSchemasWithoutPrimaryKey() {
        List<ShardingSphereTable> tables = new LinkedList<>();
        tables.add(new ShardingSphereTable("logicName1", Arrays.asList(new ShardingSphereColumn("order_id", Types.INTEGER, true, false, "int", false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, "int", false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, "int", false, true, false, false)), Collections.emptySet(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_other", Collections.singletonList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, "int", false, true, false, false)), Collections.emptySet(), Collections.emptyList()));
        return Collections.singleton(new ShardingSphereSchema("name", tables, Collections.emptyList()));
    }
}
