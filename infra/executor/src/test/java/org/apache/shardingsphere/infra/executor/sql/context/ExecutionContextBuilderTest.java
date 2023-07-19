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

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
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
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExecutionContextBuilderTest {
    
    @Test
    void assertBuildGenericSQLRewriteResult() {
        String sql = "sql";
        GenericSQLRewriteResult genericSQLRewriteResult = new GenericSQLRewriteResult(new SQLRewriteUnit(sql, Collections.singletonList("foo_param")));
        ShardingSphereResourceMetaData resourceMetaData = mock(ShardingSphereResourceMetaData.class);
        String firstDataSourceName = "firstDataSourceName";
        when(resourceMetaData.getAllInstanceDataSourceNames()).thenReturn(Arrays.asList(firstDataSourceName, "lastDataSourceName"));
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME, mock(DatabaseType.class), resourceMetaData, ruleMetaData, buildDatabase());
        Collection<ExecutionUnit> actual = ExecutionContextBuilder.build(database, genericSQLRewriteResult, mock(SQLStatementContext.class));
        Collection<ExecutionUnit> expected = Collections.singletonList(new ExecutionUnit(firstDataSourceName, new SQLUnit(sql, Collections.singletonList("foo_param"))));
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertBuildRouteSQLRewriteResult() {
        RouteUnit routeUnit1 = new RouteUnit(new RouteMapper("logicName1", "actualName1"), Collections.singletonList(new RouteMapper("logicName1", "actualName1")));
        SQLRewriteUnit sqlRewriteUnit1 = new SQLRewriteUnit("sql1", Collections.singletonList("parameter1"));
        RouteUnit routeUnit2 = new RouteUnit(new RouteMapper("logicName2", "actualName2"), Collections.singletonList(new RouteMapper("logicName1", "actualName1")));
        SQLRewriteUnit sqlRewriteUnit2 = new SQLRewriteUnit("sql2", Collections.singletonList("parameter2"));
        Map<RouteUnit, SQLRewriteUnit> sqlRewriteUnits = new HashMap<>(2, 1F);
        sqlRewriteUnits.put(routeUnit1, sqlRewriteUnit1);
        sqlRewriteUnits.put(routeUnit2, sqlRewriteUnit2);
        ShardingSphereResourceMetaData resourceMetaData = new ShardingSphereResourceMetaData("sharding_db", Collections.emptyMap());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME, mock(DatabaseType.class), resourceMetaData, ruleMetaData, buildDatabase());
        Collection<ExecutionUnit> actual = ExecutionContextBuilder.build(database, new RouteSQLRewriteResult(sqlRewriteUnits), mock(SQLStatementContext.class));
        ExecutionUnit expectedUnit1 = new ExecutionUnit("actualName1", new SQLUnit("sql1", Collections.singletonList("parameter1")));
        ExecutionUnit expectedUnit2 = new ExecutionUnit("actualName2", new SQLUnit("sql2", Collections.singletonList("parameter2")));
        Collection<ExecutionUnit> expected = new LinkedHashSet<>(2, 1F);
        expected.add(expectedUnit1);
        expected.add(expectedUnit2);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertBuildRouteSQLRewriteResultWithEmptyPrimaryKeyMeta() {
        RouteUnit routeUnit2 = new RouteUnit(new RouteMapper("logicName2", "actualName2"), Collections.singletonList(new RouteMapper("logicName2", "actualName2")));
        SQLRewriteUnit sqlRewriteUnit2 = new SQLRewriteUnit("sql2", Collections.singletonList("parameter2"));
        Map<RouteUnit, SQLRewriteUnit> sqlRewriteUnits = new HashMap<>(2, 1F);
        sqlRewriteUnits.put(routeUnit2, sqlRewriteUnit2);
        ShardingSphereResourceMetaData resourceMetaData = new ShardingSphereResourceMetaData("sharding_db", Collections.emptyMap());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME, mock(DatabaseType.class), resourceMetaData, ruleMetaData, buildDatabaseWithoutPrimaryKey());
        Collection<ExecutionUnit> actual = ExecutionContextBuilder.build(database, new RouteSQLRewriteResult(sqlRewriteUnits), mock(SQLStatementContext.class));
        ExecutionUnit expectedUnit2 = new ExecutionUnit("actualName2", new SQLUnit("sql2", Collections.singletonList("parameter2")));
        assertThat(actual, is(Collections.singleton(expectedUnit2)));
    }
    
    private Map<String, ShardingSphereSchema> buildDatabaseWithoutPrimaryKey() {
        Map<String, ShardingSphereTable> tables = new HashMap<>(3, 1F);
        tables.put("logicName1", new ShardingSphereTable("logicName1", Arrays.asList(new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false)), Collections.emptySet(), Collections.emptyList()));
        tables.put("t_other", new ShardingSphereTable("t_other", Collections.singletonList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false)), Collections.emptySet(), Collections.emptyList()));
        return Collections.singletonMap("name", new ShardingSphereSchema(tables, Collections.emptyMap()));
    }
    
    private Map<String, ShardingSphereSchema> buildDatabase() {
        Map<String, ShardingSphereTable> tables = new HashMap<>(3, 1F);
        tables.put("logicName1", new ShardingSphereTable("logicName1", Arrays.asList(new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false)), Collections.emptySet(), Collections.emptyList()));
        tables.put("logicName2", new ShardingSphereTable("logicName2", Arrays.asList(new ShardingSphereColumn("item_id", Types.INTEGER, true, false, false, true, false),
                new ShardingSphereColumn("order_id", Types.INTEGER, false, false, false, true, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false),
                new ShardingSphereColumn("status", Types.VARCHAR, false, false, false, true, false),
                new ShardingSphereColumn("c_date", Types.TIMESTAMP, false, false, false, true, false)), Collections.emptySet(), Collections.emptyList()));
        tables.put("t_other", new ShardingSphereTable("t_other", Collections.singletonList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false)), Collections.emptySet(), Collections.emptyList()));
        return Collections.singletonMap("name", new ShardingSphereSchema(tables, Collections.emptyMap()));
    }
}
