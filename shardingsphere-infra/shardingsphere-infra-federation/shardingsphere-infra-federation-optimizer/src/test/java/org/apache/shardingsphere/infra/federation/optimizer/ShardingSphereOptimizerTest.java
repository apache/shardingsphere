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

import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.federation.optimizer.context.planner.OptimizerPlannerContextFactory;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.filter.FilterableSchema;
import org.apache.shardingsphere.infra.federation.optimizer.planner.QueryOptimizePlannerFactory;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Before;
import org.junit.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingSphereOptimizerTest {
    
    private static final String LINE_SEPARATOR = System.lineSeparator();
    
    private static final String SELECT_CROSS_JOIN_CONDITION = "SELECT t_order_federate.order_id, t_order_federate.user_id, t_user_info.user_id "
            + "FROM t_order_federate JOIN t_user_info ON t_order_federate.user_id = t_user_info.user_id "
            + "WHERE t_user_info.user_id = 13";
    
    private static final String SELECT_WHERE_ALL_FIELDS = "SELECT user_id, information FROM t_user_info WHERE user_id = 12";
    
    private static final String SELECT_WHERE_SINGLE_FIELD = "SELECT user_id FROM t_user_info WHERE user_id = 12";
    
    private static final String SELECT_CROSS_WHERE = "SELECT t_order_federate.order_id, t_order_federate.user_id, t_user_info.user_id "
            + "FROM t_order_federate , t_user_info "
            + "WHERE t_order_federate.user_id = t_user_info.user_id";
    
    private static final String SELECT_CROSS_JOIN = "SELECT t_order_federate.order_id, t_order_federate.user_id, t_user_info.user_id "
            + "FROM t_order_federate JOIN t_user_info "
            + "ON t_order_federate.user_id = t_user_info.user_id";
    
    private static final String SELECT_CROSS_WHERE_CONDITION = "SELECT t_order_federate.order_id, t_order_federate.user_id, t_user_info.user_id "
            + "FROM t_order_federate ,t_user_info "
            + "WHERE t_order_federate.user_id = t_user_info.user_id AND t_user_info.user_id = 13";
    
    private static final String SELECT_SUBQUERY_FROM = "SELECT user.user_id, user.information "
            + "FROM (SELECT * FROM t_user_info WHERE user_id > 1) as user ";
    
    private static final String SELECT_SUBQUERY_WHERE_EXIST = "SELECT t_order_federate.order_id, t_order_federate.user_id FROM t_order_federate "
            + "WHERE EXISTS (SELECT * FROM t_user_info WHERE t_order_federate.user_id = t_user_info.user_id)";
    
    private static final String SELECT_SUBQUERY_WHERE_IN = "SELECT t_order_federate.order_id, t_order_federate.user_id FROM t_order_federate "
            + "WHERE t_order_federate.user_id IN (SELECT t_user_info.user_id FROM t_user_info)";
    
    private static final String SELECT_SUBQUERY_WHERE_BETWEEN = "SELECT t_order_federate.order_id, t_order_federate.user_id FROM t_order_federate "
            + "WHERE user_id BETWEEN (SELECT user_id FROM t_user_info WHERE information = 'before') "
            + "AND (SELECT user_id FROM t_user_info WHERE information = 'after')";
    
    private static final String SELECT_UNION = "SELECT order_id, user_id FROM t_order_federate UNION SELECT 1, user_id FROM t_user_info WHERE information = 'before'";
    
    private static final String SCHEMA_NAME = "federate_jdbc";
    
    private final SQLParserRule sqlParserRule = new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build());
    
    private ShardingSphereOptimizer optimizer;
    
    @Before
    public void init() throws Exception {
        Map<String, ShardingSphereTable> tables = new HashMap<>(2, 1);
        tables.put("t_order_federate", createOrderTableMetaData());
        tables.put("t_user_info", createUserInfoTableMetaData());
        ShardingSphereSchema schema = new ShardingSphereSchema(tables);
        SqlToRelConverter converter = createSqlToRelConverter(schema);
        optimizer = new ShardingSphereOptimizer(converter, QueryOptimizePlannerFactory.createHepPlannerWithoutCalc() , QueryOptimizePlannerFactory.createHepPlannerWithCalc());
    }
    
    private static SqlToRelConverter createSqlToRelConverter(final ShardingSphereSchema schema) {
        CalciteConnectionConfig connectionConfig = new CalciteConnectionConfigImpl(OptimizerPlannerContextFactory.createConnectionProperties());
        RelDataTypeFactory relDataTypeFactory = new JavaTypeFactoryImpl();
        FilterableSchema federationSchema = new FilterableSchema(SCHEMA_NAME, schema, null);
        CalciteCatalogReader catalogReader = OptimizerPlannerContextFactory.createCatalogReader(SCHEMA_NAME, federationSchema, relDataTypeFactory, connectionConfig);
        SqlValidator validator = OptimizerPlannerContextFactory.createValidator(catalogReader, relDataTypeFactory, connectionConfig);
        return OptimizerPlannerContextFactory.createConverter(catalogReader, validator, relDataTypeFactory);
    }
    
    private ShardingSphereTable createOrderTableMetaData() {
        ShardingSphereColumn orderIdColumn = new ShardingSphereColumn("order_id", Types.VARCHAR, true, false, false, true);
        ShardingSphereColumn userIdColumn = new ShardingSphereColumn("user_id", Types.VARCHAR, false, false, false, true);
        ShardingSphereColumn statusColumn = new ShardingSphereColumn("status", Types.VARCHAR, false, false, false, true);
        return new ShardingSphereTable("t_order_federate", Arrays.asList(orderIdColumn, userIdColumn, statusColumn), Collections.emptyList(), Collections.emptyList());
    }
    
    private ShardingSphereTable createUserInfoTableMetaData() {
        ShardingSphereColumn userIdColumn = new ShardingSphereColumn("user_id", Types.VARCHAR, true, false, false, true);
        ShardingSphereColumn informationColumn = new ShardingSphereColumn("information", Types.VARCHAR, false, false, false, true);
        return new ShardingSphereTable("t_user_info", Arrays.asList(userIdColumn, informationColumn), Collections.emptyList(), Collections.emptyList());
    }
    
    @Test
    public void assertSelectCrossJoinCondition() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_CROSS_JOIN_CONDITION, false);
        String actual = optimizer.optimize(sqlStatement).explain();
        String expected = "EnumerableCalc(expr#0..6=[{inputs}], proj#0..1=[{exprs}], user_id0=[$t4])" + LINE_SEPARATOR
                + "  EnumerableHashJoin(condition=[=($3, $6)], joinType=[inner])" + LINE_SEPARATOR
                + "    EnumerableCalc(expr#0..2=[{inputs}], expr#3=[CAST($t1):VARCHAR], proj#0..3=[{exprs}])" + LINE_SEPARATOR
                + "      EnumerableTableScan(table=[[federate_jdbc, t_order_federate]])" + LINE_SEPARATOR
                + "    EnumerableCalc(expr#0..1=[{inputs}], expr#2=[CAST($t0):VARCHAR], expr#3=[CAST($t0):INTEGER], expr#4=[13], expr#5=[=($t3, $t4)], proj#0..2=[{exprs}], $condition=[$t5])"
                + LINE_SEPARATOR
                + "      EnumerableTableScan(table=[[federate_jdbc, t_user_info]])" + LINE_SEPARATOR;
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectWhereAllFields() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_WHERE_ALL_FIELDS, false);
        String actual = optimizer.optimize(sqlStatement).explain();
        String expected = "EnumerableCalc(expr#0..1=[{inputs}], expr#2=[CAST($t0):INTEGER], expr#3=[12], expr#4=[=($t2, $t3)], proj#0..1=[{exprs}], $condition=[$t4])" + LINE_SEPARATOR
                + "  EnumerableTableScan(table=[[federate_jdbc, t_user_info]])" + LINE_SEPARATOR;
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectWhereSingleField() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_WHERE_SINGLE_FIELD, false);
        String actual = optimizer.optimize(sqlStatement).explain();
        String expected = "EnumerableCalc(expr#0..1=[{inputs}], expr#2=[CAST($t0):INTEGER], expr#3=[12], expr#4=[=($t2, $t3)], user_id=[$t0], $condition=[$t4])" + LINE_SEPARATOR
                + "  EnumerableTableScan(table=[[federate_jdbc, t_user_info]])" + LINE_SEPARATOR;
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectCrossWhere() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_CROSS_WHERE, false);
        String actual = optimizer.optimize(sqlStatement).explain();
        String expected = "EnumerableCalc(expr#0..6=[{inputs}], proj#0..1=[{exprs}], user_id0=[$t4])" + LINE_SEPARATOR
                + "  EnumerableHashJoin(condition=[=($3, $6)], joinType=[inner])" + LINE_SEPARATOR
                + "    EnumerableCalc(expr#0..2=[{inputs}], expr#3=[CAST($t1):VARCHAR], proj#0..3=[{exprs}])" + LINE_SEPARATOR
                + "      EnumerableTableScan(table=[[federate_jdbc, t_order_federate]])" + LINE_SEPARATOR
                + "    EnumerableCalc(expr#0..1=[{inputs}], expr#2=[CAST($t0):VARCHAR], proj#0..2=[{exprs}])" + LINE_SEPARATOR
                + "      EnumerableTableScan(table=[[federate_jdbc, t_user_info]])" + LINE_SEPARATOR;
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectCrossJoin() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_CROSS_JOIN, false);
        String actual = optimizer.optimize(sqlStatement).explain();
        String expected = "EnumerableCalc(expr#0..6=[{inputs}], proj#0..1=[{exprs}], user_id0=[$t4])" + LINE_SEPARATOR
                + "  EnumerableHashJoin(condition=[=($3, $6)], joinType=[inner])" + LINE_SEPARATOR
                + "    EnumerableCalc(expr#0..2=[{inputs}], expr#3=[CAST($t1):VARCHAR], proj#0..3=[{exprs}])" + LINE_SEPARATOR
                + "      EnumerableTableScan(table=[[federate_jdbc, t_order_federate]])" + LINE_SEPARATOR
                + "    EnumerableCalc(expr#0..1=[{inputs}], expr#2=[CAST($t0):VARCHAR], proj#0..2=[{exprs}])" + LINE_SEPARATOR
                + "      EnumerableTableScan(table=[[federate_jdbc, t_user_info]])" + LINE_SEPARATOR;
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectJoinWhere() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_CROSS_WHERE_CONDITION, false);
        String actual = optimizer.optimize(sqlStatement).explain();
        String expected = "EnumerableCalc(expr#0..6=[{inputs}], proj#0..1=[{exprs}], user_id0=[$t4])" + LINE_SEPARATOR
                + "  EnumerableHashJoin(condition=[=($3, $6)], joinType=[inner])" + LINE_SEPARATOR
                + "    EnumerableCalc(expr#0..2=[{inputs}], expr#3=[CAST($t1):VARCHAR], proj#0..3=[{exprs}])" + LINE_SEPARATOR
                + "      EnumerableTableScan(table=[[federate_jdbc, t_order_federate]])" + LINE_SEPARATOR
                + "    EnumerableCalc(expr#0..1=[{inputs}], expr#2=[CAST($t0):VARCHAR], expr#3=[CAST($t0):INTEGER], expr#4=[13], expr#5=[=($t3, $t4)], proj#0..2=[{exprs}], $condition=[$t5])"
                + LINE_SEPARATOR
                + "      EnumerableTableScan(table=[[federate_jdbc, t_user_info]])" + LINE_SEPARATOR;
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectSubQueryFrom() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_SUBQUERY_FROM, false);
        String actual = optimizer.optimize(sqlStatement).explain();
        String expected = "EnumerableCalc(expr#0..1=[{inputs}], expr#2=[CAST($t0):INTEGER], expr#3=[1], expr#4=[>($t2, $t3)], proj#0..1=[{exprs}], $condition=[$t4])" + LINE_SEPARATOR
                + "  EnumerableTableScan(table=[[federate_jdbc, t_user_info]])" + LINE_SEPARATOR;
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectSubQueryWhereExist() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_SUBQUERY_WHERE_EXIST, false);
        String actual = optimizer.optimize(sqlStatement).explain();
        String expected = "EnumerableCalc(expr#0..3=[{inputs}], expr#4=[IS NOT NULL($t3)], proj#0..1=[{exprs}], $condition=[$t4])" + LINE_SEPARATOR
                + "  EnumerableCorrelate(correlation=[$cor0], joinType=[left], requiredColumns=[{1}])" + LINE_SEPARATOR
                + "    EnumerableTableScan(table=[[federate_jdbc, t_order_federate]])" + LINE_SEPARATOR
                + "    EnumerableAggregate(group=[{}], agg#0=[MIN($0)])" + LINE_SEPARATOR
                + "      EnumerableCalc(expr#0..1=[{inputs}], expr#2=[true], expr#3=[$cor0], expr#4=[$t3.user_id],"
                + " expr#5=[CAST($t4):VARCHAR], expr#6=[CAST($t0):VARCHAR], expr#7=[=($t5, $t6)], $f0=[$t2], $condition=[$t7])" + LINE_SEPARATOR
                + "        EnumerableTableScan(table=[[federate_jdbc, t_user_info]])" + LINE_SEPARATOR;
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectSubQueryWhereIn() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_SUBQUERY_WHERE_IN, false);
        String actual = optimizer.optimize(sqlStatement).explain();
        String expected = "EnumerableCalc(expr#0..3=[{inputs}], proj#0..1=[{exprs}])" + LINE_SEPARATOR
                + "  EnumerableHashJoin(condition=[=($1, $3)], joinType=[inner])" + LINE_SEPARATOR
                + "    EnumerableTableScan(table=[[federate_jdbc, t_order_federate]])" + LINE_SEPARATOR
                + "    EnumerableAggregate(group=[{0}])" + LINE_SEPARATOR
                + "      EnumerableCalc(expr#0..1=[{inputs}], user_id=[$t0])" + LINE_SEPARATOR
                + "        EnumerableTableScan(table=[[federate_jdbc, t_user_info]])" + LINE_SEPARATOR;
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectSubQueryWhereBetween() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_SUBQUERY_WHERE_BETWEEN, false);
        String actual = optimizer.optimize(sqlStatement).explain();
        String expected = "EnumerableCalc(expr#0..4=[{inputs}], proj#0..1=[{exprs}])" + LINE_SEPARATOR
                + "  EnumerableNestedLoopJoin(condition=[<=($1, $4)], joinType=[inner])" + LINE_SEPARATOR
                + "    EnumerableNestedLoopJoin(condition=[>=($1, $3)], joinType=[inner])" + LINE_SEPARATOR
                + "      EnumerableTableScan(table=[[federate_jdbc, t_order_federate]])" + LINE_SEPARATOR
                + "      EnumerableAggregate(group=[{}], agg#0=[SINGLE_VALUE($0)])" + LINE_SEPARATOR
                + "        EnumerableCalc(expr#0..1=[{inputs}], expr#2=[CAST($t1):VARCHAR], expr#3=['before':VARCHAR], expr#4=[=($t2, $t3)], user_id=[$t0], $condition=[$t4])" + LINE_SEPARATOR
                + "          EnumerableTableScan(table=[[federate_jdbc, t_user_info]])" + LINE_SEPARATOR
                + "    EnumerableAggregate(group=[{}], agg#0=[SINGLE_VALUE($0)])" + LINE_SEPARATOR
                + "      EnumerableCalc(expr#0..1=[{inputs}], expr#2=[CAST($t1):VARCHAR], expr#3=['after':VARCHAR], expr#4=[=($t2, $t3)], user_id=[$t0], $condition=[$t4])" + LINE_SEPARATOR
                + "        EnumerableTableScan(table=[[federate_jdbc, t_user_info]])" + LINE_SEPARATOR;
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectUnion() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_UNION, false);
        String actual = optimizer.optimize(sqlStatement).explain();
        String expected = "EnumerableUnion(all=[false])" + LINE_SEPARATOR + "  EnumerableCalc(expr#0..2=[{inputs}], proj#0..1=[{exprs}])" + LINE_SEPARATOR
                + "    EnumerableTableScan(table=[[federate_jdbc, t_order_federate]])" + LINE_SEPARATOR
                + "  EnumerableCalc(expr#0..1=[{inputs}], expr#2=['1':VARCHAR], expr#3=[CAST($t1):VARCHAR], "
                + "expr#4=['before':VARCHAR], expr#5=[=($t3, $t4)], EXPR$0=[$t2], user_id=[$t0], $condition=[$t5])" + LINE_SEPARATOR
                + "    EnumerableTableScan(table=[[federate_jdbc, t_user_info]])" + LINE_SEPARATOR;
        assertThat(actual, is(expected));
    }
}
