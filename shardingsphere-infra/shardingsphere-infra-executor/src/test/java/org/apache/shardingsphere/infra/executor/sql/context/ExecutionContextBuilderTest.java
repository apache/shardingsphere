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
import org.apache.shardingsphere.infra.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.schema.model.addressing.TableAddressingMetaData;
import org.apache.shardingsphere.infra.schema.model.datasource.CachedDatabaseMetaData;
import org.apache.shardingsphere.infra.schema.model.datasource.DataSourcesMetaData;
import org.apache.shardingsphere.infra.schema.model.schema.physical.model.column.PhysicalColumnMetaData;
import org.apache.shardingsphere.infra.schema.model.schema.physical.model.schema.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.schema.model.schema.physical.model.table.PhysicalTableMetaData;
import org.apache.shardingsphere.infra.rewrite.engine.result.GenericSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.RouteSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteUnit;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.junit.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ExecutionContextBuilderTest {
    
    @Test
    public void assertBuildGenericSQLRewriteResult() {
        String sql = "sql";
        List<Object> parameters = Collections.singletonList("parameter");
        GenericSQLRewriteResult genericSQLRewriteResult = new GenericSQLRewriteResult(new SQLRewriteUnit(sql, parameters));
        DataSourcesMetaData dataSourcesMetaData = mock(DataSourcesMetaData.class);
        String firstDataSourceName = "firstDataSourceName";
        when(dataSourcesMetaData.getAllInstanceDataSourceNames()).thenReturn(Arrays.asList(firstDataSourceName, "lastDataSourceName"));
        ShardingSphereSchema schema = new ShardingSphereSchema(dataSourcesMetaData, mock(TableAddressingMetaData.class), buildPhysicalSchemaMetaData(), mock(CachedDatabaseMetaData.class));
        Collection<ExecutionUnit> actual = ExecutionContextBuilder.build(schema, genericSQLRewriteResult, mock(SQLStatementContext.class));
        Collection<ExecutionUnit> expected = Collections.singletonList(new ExecutionUnit(firstDataSourceName, new SQLUnit(sql, parameters)));
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertBuildRouteSQLRewriteResult() {
        RouteUnit routeUnit1 = new RouteUnit(new RouteMapper("logicName1", "actualName1"), Collections.singletonList(new RouteMapper("logicName1", "actualName1")));
        SQLRewriteUnit sqlRewriteUnit1 = new SQLRewriteUnit("sql1", Collections.singletonList("parameter1"));
        RouteUnit routeUnit2 = new RouteUnit(new RouteMapper("logicName2", "actualName2"), Collections.singletonList(new RouteMapper("logicName1", "actualName1")));
        SQLRewriteUnit sqlRewriteUnit2 = new SQLRewriteUnit("sql2", Collections.singletonList("parameter2"));
        Map<RouteUnit, SQLRewriteUnit> sqlRewriteUnits = new HashMap<>(2, 1);
        sqlRewriteUnits.put(routeUnit1, sqlRewriteUnit1);
        sqlRewriteUnits.put(routeUnit2, sqlRewriteUnit2);
        ShardingSphereSchema schema = new ShardingSphereSchema(
                mock(DataSourcesMetaData.class), mock(TableAddressingMetaData.class), buildPhysicalSchemaMetaData(), mock(CachedDatabaseMetaData.class));
        Collection<ExecutionUnit> actual = ExecutionContextBuilder.build(schema, new RouteSQLRewriteResult(sqlRewriteUnits), mock(SQLStatementContext.class));
        ExecutionUnit expectedUnit1 = new ExecutionUnit("actualName1", new SQLUnit("sql1", Collections.singletonList("parameter1")));
        ExecutionUnit expectedUnit2 = new ExecutionUnit("actualName2", new SQLUnit("sql2", Collections.singletonList("parameter2")));
        Collection<ExecutionUnit> expected = new LinkedHashSet<>(2, 1);
        expected.add(expectedUnit1);
        expected.add(expectedUnit2);
        assertThat(actual, is(expected));
        assertThat(actual.iterator().next().getSqlUnit().getSqlRuntimeContext().getPrimaryKeysMetaData().size(), is(1));
    }
    
    @Test
    public void assertBuildRouteSQLRewriteResultWithEmptyPrimaryKeyMeta() {
        RouteUnit routeUnit2 = new RouteUnit(new RouteMapper("logicName2", "actualName2"), Collections.singletonList(new RouteMapper("logicName2", "actualName2")));
        SQLRewriteUnit sqlRewriteUnit2 = new SQLRewriteUnit("sql2", Collections.singletonList("parameter2"));
        Map<RouteUnit, SQLRewriteUnit> sqlRewriteUnits = new HashMap<>(2, 1);
        sqlRewriteUnits.put(routeUnit2, sqlRewriteUnit2);
        ShardingSphereSchema schema = new ShardingSphereSchema(
                mock(DataSourcesMetaData.class), mock(TableAddressingMetaData.class), buildPhysicalSchemaMetaDataWithoutPrimaryKey(), mock(CachedDatabaseMetaData.class));
        Collection<ExecutionUnit> actual = ExecutionContextBuilder.build(schema, new RouteSQLRewriteResult(sqlRewriteUnits), mock(SQLStatementContext.class));
        ExecutionUnit expectedUnit2 = new ExecutionUnit("actualName2", new SQLUnit("sql2", Collections.singletonList("parameter2")));
        Collection<ExecutionUnit> expected = new LinkedHashSet<>(1, 1);
        expected.add(expectedUnit2);
        assertThat(actual, is(expected));
        assertThat(actual.iterator().next().getSqlUnit().getSqlRuntimeContext().getPrimaryKeysMetaData().size(), is(0));
    }
    
    private PhysicalSchemaMetaData buildPhysicalSchemaMetaDataWithoutPrimaryKey() {
        Map<String, PhysicalTableMetaData> tableMetaDataMap = new HashMap<>(3, 1);
        tableMetaDataMap.put("logicName1", new PhysicalTableMetaData(Arrays.asList(new PhysicalColumnMetaData("order_id", Types.INTEGER, "int", true, false, false),
                new PhysicalColumnMetaData("user_id", Types.INTEGER, "int", false, false, false),
                new PhysicalColumnMetaData("status", Types.INTEGER, "int", false, false, false)), Collections.emptySet()));
        tableMetaDataMap.put("t_other", new PhysicalTableMetaData(Collections.singletonList(new PhysicalColumnMetaData("order_id", Types.INTEGER, "int", true, false, false)), Collections.emptySet()));
        return new PhysicalSchemaMetaData(tableMetaDataMap);
    }
    
    private PhysicalSchemaMetaData buildPhysicalSchemaMetaData() {
        Map<String, PhysicalTableMetaData> tableMetaDataMap = new HashMap<>(3, 1);
        tableMetaDataMap.put("logicName1", new PhysicalTableMetaData(Arrays.asList(new PhysicalColumnMetaData("order_id", Types.INTEGER, "int", true, false, false),
                new PhysicalColumnMetaData("user_id", Types.INTEGER, "int", false, false, false),
                new PhysicalColumnMetaData("status", Types.INTEGER, "int", false, false, false)), Collections.emptySet()));
        tableMetaDataMap.put("logicName2", new PhysicalTableMetaData(Arrays.asList(new PhysicalColumnMetaData("item_id", Types.INTEGER, "int", true, false, false),
                new PhysicalColumnMetaData("order_id", Types.INTEGER, "int", false, false, false),
                new PhysicalColumnMetaData("user_id", Types.INTEGER, "int", false, false, false),
                new PhysicalColumnMetaData("status", Types.VARCHAR, "varchar", false, false, false),
                new PhysicalColumnMetaData("c_date", Types.TIMESTAMP, "timestamp", false, false, false)), Collections.emptySet()));
        tableMetaDataMap.put("t_other", new PhysicalTableMetaData(Collections.singletonList(new PhysicalColumnMetaData("order_id", Types.INTEGER, "int", true, false, false)), Collections.emptySet()));
        return new PhysicalSchemaMetaData(tableMetaDataMap);
    }
}
