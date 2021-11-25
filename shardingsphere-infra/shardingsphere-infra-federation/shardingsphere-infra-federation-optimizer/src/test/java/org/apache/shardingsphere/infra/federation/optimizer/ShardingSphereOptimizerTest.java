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

package org.apache.shardingsphere.infra.federation.optimizer;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContextFactory;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Before;
import org.junit.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingSphereOptimizerTest {
    
    private static final String SELECT_CROSS_JOIN_CONDITION =
        "SELECT t_order_federate.order_id, t_order_federate.user_id, t_user_info.user_id "
            + "FROM t_order_federate JOIN t_user_info ON t_order_federate.user_id = t_user_info.user_id "
            + "WHERE t_user_info.user_id = 13";
    
    private static final String SELECT_WHERE_ALL_FIELDS =
        "SELECT user_id, information FROM t_user_info WHERE user_id = 12";
    
    private static final String SELECT_WHERE_SINGLE_FIELD =
        "SELECT user_id FROM t_user_info WHERE user_id = 12";
    
    private static final String SELECT_CROSS_WHERE =
        "SELECT t_order_federate.order_id, t_order_federate.user_id, t_user_info.user_id "
            + "FROM t_order_federate , t_user_info "
            + "WHERE t_order_federate.user_id = t_user_info.user_id";
    
    private static final String SELECT_CROSS_JOIN =
        "SELECT t_order_federate.order_id, t_order_federate.user_id, t_user_info.user_id "
            + "FROM t_order_federate JOIN t_user_info "
            + "ON t_order_federate.user_id = t_user_info.user_id";
    
    private static final String SELECT_CROSS_WHERE_CONDITION =
        "SELECT t_order_federate.order_id, t_order_federate.user_id, t_user_info.user_id "
            + "FROM t_order_federate ,t_user_info "
            + "WHERE t_order_federate.user_id = t_user_info.user_id AND t_user_info.user_id = 13";
    
    private static final String SELECT_SUBQUERY_FROM =
        "SELECT user.user_id, user.information "
            + "FROM (SELECT * FROM t_user_info WHERE user_id > 1) as user ";
    
    private static final String SELECT_SUBQUERY_WHERE_EXIST =
        "SELECT t_order_federate.order_id, t_order_federate.user_id FROM t_order_federate "
            + "WHERE EXISTS (SELECT * FROM t_user_info WHERE t_order_federate.user_id = t_user_info.user_id)";
    
    private static final String SELECT_SUBQUERY_WHERE_IN =
        "SELECT t_order_federate.order_id, t_order_federate.user_id FROM t_order_federate "
            + "WHERE t_order_federate.user_id IN (SELECT t_user_info.user_id FROM t_user_info)";
    
    private static final String SELECT_SUBQUERY_WHERE_BETWEEN =
        "SELECT t_order_federate.order_id, t_order_federate.user_id FROM t_order_federate "
            + "WHERE user_id BETWEEN (SELECT user_id FROM t_user_info WHERE information = 'before') "
            + "AND (SELECT user_id FROM t_user_info WHERE information = 'after')";
    
    private final String schemaName = "federate_jdbc";
    
    private ShardingSphereOptimizer optimizer;
    
    @Before
    public void init() throws Exception {
        Map<String, TableMetaData> tableMetaDataMap = new HashMap<>(2, 1);
        tableMetaDataMap.put("t_order_federate", createOrderTableMetaData());
        tableMetaDataMap.put("t_user_info", createUserInfoTableMetaData());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(schemaName, mockResource(), null, new ShardingSphereSchema(tableMetaDataMap));
        optimizer = new ShardingSphereOptimizer(OptimizerContextFactory.create(Collections.singletonMap(schemaName, metaData)));
    }
    
    private ShardingSphereResource mockResource() {
        ShardingSphereResource result = mock(ShardingSphereResource.class);
        when(result.getDatabaseType()).thenReturn(new H2DatabaseType());
        return result;
    }
    
    private TableMetaData createOrderTableMetaData() {
        ColumnMetaData orderIdColumn = new ColumnMetaData("order_id", Types.VARCHAR, true, false, false);
        ColumnMetaData userIdColumn = new ColumnMetaData("user_id", Types.VARCHAR, false, false, false);
        ColumnMetaData statusColumn = new ColumnMetaData("status", Types.VARCHAR, false, false, false);
        return new TableMetaData("t_order_federate", Arrays.asList(orderIdColumn, userIdColumn, statusColumn), Collections.emptyList());
    }
    
    private TableMetaData createUserInfoTableMetaData() {
        ColumnMetaData userIdColumn = new ColumnMetaData("user_id", Types.VARCHAR, true, false, false);
        ColumnMetaData informationColumn = new ColumnMetaData("information", Types.VARCHAR, false, false, false);
        return new TableMetaData("t_user_info", Arrays.asList(userIdColumn, informationColumn), Collections.emptyList());
    }
    
    @Test
    public void assertSelectCrossJoinCondition() {
        ShardingSphereSQLParserEngine sqlParserEngine = new ShardingSphereSQLParserEngine(
            DatabaseTypeRegistry.getTrunkDatabaseTypeName(new H2DatabaseType()), new ConfigurationProperties(new Properties()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_CROSS_JOIN_CONDITION, false);
        String actual = optimizer.optimize(schemaName, sqlStatement).explain();
        String expected = 
              "EnumerableCalc(expr#0..6=[{inputs}],order_id=[$t3],user_id=[$t4],user_id0=[$t0])"
            + "  EnumerableHashJoin(condition=[=($2,$6)],joinType=[inner])"
            + "    EnumerableCalc(expr#0..1=[{inputs}],expr#2=[CAST($t0):VARCHAR],proj#0..2=[{exprs}])"
            + "      EnumerableInterpreterBindableTableScan(table=[[federate_jdbc,t_user_info]],filters=[[=(CAST($0):INTEGER,13)]])"
            + "    EnumerableCalc(expr#0..2=[{inputs}],expr#3=[CAST($t1):VARCHAR],proj#0..3=[{exprs}])"
            + "      EnumerableTableScan(table=[[federate_jdbc,t_order_federate]])";
        assertThat(actual.replaceAll("\\s*", ""), is(expected.replaceAll("\\s*", "")));
    }
    
    @Test
    public void assertSelectWhereAllFields() {
        ShardingSphereSQLParserEngine sqlParserEngine = new ShardingSphereSQLParserEngine(
                DatabaseTypeRegistry.getTrunkDatabaseTypeName(new H2DatabaseType()), new ConfigurationProperties(new Properties()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_WHERE_ALL_FIELDS, false);
        String actual = optimizer.optimize(schemaName, sqlStatement).explain();
        String expected =
              "EnumerableInterpreter"
            + "  BindableTableScan(table=[[federate_jdbc,t_user_info]],filters=[[=(CAST($0):INTEGER,12)]])";
        assertThat(actual.replaceAll("\\s*", ""), is(expected.replaceAll("\\s*", "")));
    }
    
    @Test
    public void assertSelectWhereSingleField() {
        ShardingSphereSQLParserEngine sqlParserEngine = new ShardingSphereSQLParserEngine(
            DatabaseTypeRegistry.getTrunkDatabaseTypeName(new H2DatabaseType()), new ConfigurationProperties(new Properties()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_WHERE_SINGLE_FIELD, false);
        String actual = optimizer.optimize(schemaName, sqlStatement).explain();
        String expected =
              "EnumerableInterpreter"
            + "  BindableTableScan(table=[[federate_jdbc,t_user_info]],filters=[[=(CAST($0):INTEGER,12)]],projects=[[0]])";
        assertThat(actual.replaceAll("\\s*", ""), is(expected.replaceAll("\\s*", "")));
    }
    
    @Test
    public void assertSelectCrossWhere() {
        ShardingSphereSQLParserEngine sqlParserEngine = new ShardingSphereSQLParserEngine(
            DatabaseTypeRegistry.getTrunkDatabaseTypeName(new H2DatabaseType()), new ConfigurationProperties(new Properties()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_CROSS_WHERE, false);
        String actual = optimizer.optimize(schemaName, sqlStatement).explain();
        String expected =
              "EnumerableCalc(expr#0..6=[{inputs}],order_id=[$t3],user_id=[$t4],user_id0=[$t0])"
            + "  EnumerableMergeJoin(condition=[=($2,$6)],joinType=[inner])"
            + "    EnumerableSort(sort0=[$2],dir0=[ASC])"
            + "      EnumerableCalc(expr#0..1=[{inputs}],expr#2=[CAST($t0):VARCHAR],proj#0..2=[{exprs}])"
            + "        EnumerableTableScan(table=[[federate_jdbc,t_user_info]])"
            + "    EnumerableSort(sort0=[$3],dir0=[ASC])"
            + "      EnumerableCalc(expr#0..2=[{inputs}],expr#3=[CAST($t1):VARCHAR],proj#0..3=[{exprs}])"
            + "        EnumerableTableScan(table=[[federate_jdbc,t_order_federate]])";
        assertThat(actual.replaceAll("\\s*", ""), is(expected.replaceAll("\\s*", "")));
    }
    
    @Test
    public void assertSelectCrossJoin() {
        ShardingSphereSQLParserEngine sqlParserEngine = new ShardingSphereSQLParserEngine(
            DatabaseTypeRegistry.getTrunkDatabaseTypeName(new H2DatabaseType()), new ConfigurationProperties(new Properties()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_CROSS_JOIN, false);
        String actual = optimizer.optimize(schemaName, sqlStatement).explain();
        String expected = 
              "EnumerableCalc(expr#0..6=[{inputs}],proj#0..1=[{exprs}],user_id0=[$t4])"
            + "  EnumerableMergeJoin(condition=[=($3,$6)],joinType=[inner])"
            + "    EnumerableSort(sort0=[$3],dir0=[ASC])"
            + "      EnumerableCalc(expr#0..2=[{inputs}],expr#3=[CAST($t1):VARCHAR],proj#0..3=[{exprs}])"
            + "        EnumerableTableScan(table=[[federate_jdbc,t_order_federate]])"
            + "    EnumerableSort(sort0=[$2],dir0=[ASC])"
            + "      EnumerableCalc(expr#0..1=[{inputs}],expr#2=[CAST($t0):VARCHAR],proj#0..2=[{exprs}])"
            + "        EnumerableTableScan(table=[[federate_jdbc,t_user_info]])";
        assertThat(actual.replaceAll("\\s*", ""), is(expected.replaceAll("\\s*", "")));
    }
    
    @Test
    public void assertSelectJoinWhere() {
        ShardingSphereSQLParserEngine sqlParserEngine = new ShardingSphereSQLParserEngine(
            DatabaseTypeRegistry.getTrunkDatabaseTypeName(new H2DatabaseType()), new ConfigurationProperties(new Properties()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_CROSS_WHERE_CONDITION, false);
        String actual = optimizer.optimize(schemaName, sqlStatement).explain();
        String expected =
              "EnumerableCalc(expr#0..4=[{inputs}],proj#0..1=[{exprs}],user_id0=[$t3])"
            + "  EnumerableInterpreterBindableJoin(condition=[=(CAST($1):VARCHAR,CAST($3):VARCHAR)],joinType=[inner])"
            + "    BindableTableScan(table=[[federate_jdbc,t_order_federate]])"
            + "    BindableTableScan(table=[[federate_jdbc,t_user_info]],filters=[[=(CAST($0):INTEGER,13)]])";
        assertThat(actual.replaceAll("\\s*", ""), is(expected.replaceAll("\\s*", "")));
    }
    
    @Test
    public void assertSelectSubQueryFrom() {
        ShardingSphereSQLParserEngine sqlParserEngine = new ShardingSphereSQLParserEngine(
            DatabaseTypeRegistry.getTrunkDatabaseTypeName(new H2DatabaseType()), new ConfigurationProperties(new Properties()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_SUBQUERY_FROM, false);
        String actual = optimizer.optimize(schemaName, sqlStatement).explain();
        String expected =
              "EnumerableInterpreter"
            + "  BindableTableScan(table=[[federate_jdbc,t_user_info]],filters=[[>(CAST($0):INTEGER,1)]])";
        assertThat(actual.replaceAll("\\s*", ""), is(expected.replaceAll("\\s*", "")));
    }
    
    @Test
    public void assertSelectSubQueryWhereExist() {
        ShardingSphereSQLParserEngine sqlParserEngine = new ShardingSphereSQLParserEngine(
            DatabaseTypeRegistry.getTrunkDatabaseTypeName(new H2DatabaseType()), new ConfigurationProperties(new Properties()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_SUBQUERY_WHERE_EXIST, false);
        String actual = optimizer.optimize(schemaName, sqlStatement).explain();
        String expected = 
              "EnumerableCalc(expr#0..3=[{inputs}],expr#4=[ISNOTNULL($t3)],proj#0..1=[{exprs}],$condition=[$t4])"
            + "  EnumerableCorrelate(correlation=[$cor0],joinType=[left],requiredColumns=[{1}]) "
            + "    EnumerableTableScan(table=[[federate_jdbc,t_order_federate]]) "
            + "    EnumerableInterpreterBindableAggregate(group=[{}],agg#0=[MIN($0)]) "
            + "      BindableProject($f0=[true]) "
            + "        BindableTableScan(table=[[federate_jdbc,t_user_info]],filters=[[=(CAST($cor0.user_id):VARCHAR,CAST($0):VARCHAR)]],projects=[[0]]) ";
        assertThat(actual.replaceAll("\\s*", ""), is(expected.replaceAll("\\s*", "")));
    }
    
    @Test
    public void assertSelectSubQueryWhereIn() {
        ShardingSphereSQLParserEngine sqlParserEngine = new ShardingSphereSQLParserEngine(
            DatabaseTypeRegistry.getTrunkDatabaseTypeName(new H2DatabaseType()), new ConfigurationProperties(new Properties()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_SUBQUERY_WHERE_IN, false);
        String actual = optimizer.optimize(schemaName, sqlStatement).explain();
        String expected = 
                  "EnumerableInterpreter"
                + "  BindableProject(order_id=[$0],user_id=[$1])"
                + "    BindableJoin(condition=[=($1,$3)],joinType=[semi])"
                + "      BindableTableScan(table=[[federate_jdbc,t_order_federate]])"
                + "      BindableTableScan(table=[[federate_jdbc,t_user_info]],projects=[[0]])";
        assertThat(actual.replaceAll("\\s*", ""), is(expected.replaceAll("\\s*", "")));
    }
    
    @Test
    public void assertSelectSubQueryWhereBetween() {
        ShardingSphereSQLParserEngine sqlParserEngine = new ShardingSphereSQLParserEngine(
            DatabaseTypeRegistry.getTrunkDatabaseTypeName(new H2DatabaseType()), new ConfigurationProperties(new Properties()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_SUBQUERY_WHERE_BETWEEN, false);
        String actual = optimizer.optimize(schemaName, sqlStatement).explain();
        String expected = 
              "EnumerableCalc(expr#0..4=[{inputs}],proj#0..1=[{exprs}])"
            + "  EnumerableInterpreterBindableFilter(condition=[AND(>=($1,$3),<=($1,$4))])"
            + "    BindableJoin(condition=[true],joinType=[left])"
            + "      BindableJoin(condition=[true],joinType=[left])"
            + "        BindableTableScan(table=[[federate_jdbc,t_order_federate]])"
            + "        BindableAggregate(group=[{}],agg#0=[SINGLE_VALUE($0)])"
            + "          BindableTableScan(table=[[federate_jdbc,t_user_info]],filters=[[=(CAST($1):VARCHAR,'before')]],projects=[[0]])"
            + "        BindableAggregate(group=[{}],agg#0=[SINGLE_VALUE($0)])"
            + "          BindableTableScan(table=[[federate_jdbc,t_user_info]],filters=[[=(CAST($1):VARCHAR,'after')]],projects=[[0]])";
        assertThat(actual.replaceAll("\\s*", ""), is(expected.replaceAll("\\s*", "")));
    }
}
