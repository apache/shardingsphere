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

package org.apache.shardingsphere.sqlfederation.optimizer;

import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.SQLNodeConverterEngine;
import org.apache.shardingsphere.sqlfederation.optimizer.metadata.translatable.TranslatableSchema;
import org.apache.shardingsphere.sqlfederation.optimizer.util.SQLFederationPlannerUtil;
import org.junit.Before;
import org.junit.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public final class SQLOptimizeEngineTest {
    
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
            + "WHERE user_id BETWEEN (SELECT user_id FROM t_user_info WHERE user_id = 1) "
            + "AND (SELECT user_id FROM t_user_info WHERE user_id = 3)";
    
    private static final String SELECT_UNION = "SELECT order_id, user_id FROM t_order_federate UNION SELECT 1, user_id FROM t_user_info WHERE information = 'before'";
    
    private static final String SELECT_LIMIT = "SELECT order_id, user_id FROM t_order_federate LIMIT 1";
    
    private static final String SELECT_AGGREGATION = "SELECT MAX(order_id), MIN(order_id), SUM(order_id), AVG(order_id), COUNT(1) FROM t_order_federate GROUP BY user_id";
    
    private static final String SCHEMA_NAME = "federate_jdbc";
    
    private final SQLParserRule sqlParserRule = new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build());
    
    private SQLOptimizeEngine optimizeEngine;
    
    @Before
    public void init() {
        Map<String, ShardingSphereTable> tables = new HashMap<>(2, 1);
        tables.put("t_order_federate", createOrderTableMetaData());
        tables.put("t_user_info", createUserInfoTableMetaData());
        ShardingSphereSchema schema = new ShardingSphereSchema(tables, Collections.emptyMap());
        SqlToRelConverter converter = createSqlToRelConverter(schema);
        optimizeEngine = new SQLOptimizeEngine(converter, SQLFederationPlannerUtil.createHepPlanner());
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
    
    private SqlToRelConverter createSqlToRelConverter(final ShardingSphereSchema schema) {
        CalciteConnectionConfig connectionConfig = new CalciteConnectionConfigImpl(new Properties());
        RelDataTypeFactory relDataTypeFactory = new JavaTypeFactoryImpl();
        TranslatableSchema federationSchema = new TranslatableSchema(SCHEMA_NAME, schema, null);
        CalciteCatalogReader catalogReader = SQLFederationPlannerUtil.createCatalogReader(SCHEMA_NAME, federationSchema, relDataTypeFactory, connectionConfig);
        H2DatabaseType databaseType = new H2DatabaseType();
        SqlValidator validator = SQLFederationPlannerUtil.createSqlValidator(catalogReader, relDataTypeFactory, databaseType, connectionConfig);
        RelOptCluster cluster = RelOptCluster.create(SQLFederationPlannerUtil.createVolcanoPlanner(), new RexBuilder(relDataTypeFactory));
        return SQLFederationPlannerUtil.createSqlToRelConverter(catalogReader, validator, cluster, mock(SQLParserRule.class), databaseType, false);
    }
    
    @Test
    public void assertSelectCrossJoinCondition() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_CROSS_JOIN_CONDITION, false);
        SqlNode sqlNode = SQLNodeConverterEngine.convert(sqlStatement);
        String actual = optimizeEngine.optimize(sqlNode).getBestPlan().explain();
        String expected = "EnumerableCalc(expr#0..4=[{inputs}], proj#0..1=[{exprs}], user_id0=[$t3])" + LINE_SEPARATOR
                + "  EnumerableHashJoin(condition=[=($2, $4)], joinType=[inner])" + LINE_SEPARATOR
                + "    EnumerableCalc(expr#0..1=[{inputs}], expr#2=[CAST($t1):VARCHAR], proj#0..2=[{exprs}])" + LINE_SEPARATOR
                + "      TranslatableTableScan(table=[[federate_jdbc, t_order_federate]], fields=[[0, 1]])" + LINE_SEPARATOR
                + "    EnumerableCalc(expr#0=[{inputs}], expr#1=[CAST($t0):VARCHAR], proj#0..1=[{exprs}])" + LINE_SEPARATOR
                + "      TranslatableTableScan(table=[[federate_jdbc, t_user_info]], fields=[[0]], filters=[[=(CAST($0):INTEGER, 13), null]])" + LINE_SEPARATOR;
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectWhereAllFields() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_WHERE_ALL_FIELDS, false);
        SqlNode sqlNode = SQLNodeConverterEngine.convert(sqlStatement);
        String actual = optimizeEngine.optimize(sqlNode).getBestPlan().explain();
        String expected = "TranslatableTableScan(table=[[federate_jdbc, t_user_info]], fields=[[0, 1]], filters=[[=(CAST($0):INTEGER, 12), null]])" + LINE_SEPARATOR;
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectWhereSingleField() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_WHERE_SINGLE_FIELD, false);
        SqlNode sqlNode = SQLNodeConverterEngine.convert(sqlStatement);
        String actual = optimizeEngine.optimize(sqlNode).getBestPlan().explain();
        String expected = "TranslatableTableScan(table=[[federate_jdbc, t_user_info]], fields=[[0]], filters=[[=(CAST($0):INTEGER, 12)]])" + LINE_SEPARATOR;
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectCrossWhere() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_CROSS_WHERE, false);
        SqlNode sqlNode = SQLNodeConverterEngine.convert(sqlStatement);
        String actual = optimizeEngine.optimize(sqlNode).getBestPlan().explain();
        String expected = "EnumerableCalc(expr#0..4=[{inputs}], proj#0..1=[{exprs}], user_id0=[$t3])" + LINE_SEPARATOR
                + "  EnumerableHashJoin(condition=[=($2, $4)], joinType=[inner])" + LINE_SEPARATOR
                + "    EnumerableCalc(expr#0..1=[{inputs}], expr#2=[CAST($t1):VARCHAR], proj#0..2=[{exprs}])" + LINE_SEPARATOR
                + "      TranslatableTableScan(table=[[federate_jdbc, t_order_federate]], fields=[[0, 1]])" + LINE_SEPARATOR
                + "    EnumerableCalc(expr#0=[{inputs}], expr#1=[CAST($t0):VARCHAR], proj#0..1=[{exprs}])" + LINE_SEPARATOR
                + "      TranslatableTableScan(table=[[federate_jdbc, t_user_info]], fields=[[0]])" + LINE_SEPARATOR;
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectCrossJoin() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_CROSS_JOIN, false);
        SqlNode sqlNode = SQLNodeConverterEngine.convert(sqlStatement);
        String actual = optimizeEngine.optimize(sqlNode).getBestPlan().explain();
        String expected = "EnumerableCalc(expr#0..4=[{inputs}], proj#0..1=[{exprs}], user_id0=[$t3])" + LINE_SEPARATOR
                + "  EnumerableHashJoin(condition=[=($2, $4)], joinType=[inner])" + LINE_SEPARATOR
                + "    EnumerableCalc(expr#0..1=[{inputs}], expr#2=[CAST($t1):VARCHAR], proj#0..2=[{exprs}])" + LINE_SEPARATOR
                + "      TranslatableTableScan(table=[[federate_jdbc, t_order_federate]], fields=[[0, 1]])" + LINE_SEPARATOR
                + "    EnumerableCalc(expr#0=[{inputs}], expr#1=[CAST($t0):VARCHAR], proj#0..1=[{exprs}])" + LINE_SEPARATOR
                + "      TranslatableTableScan(table=[[federate_jdbc, t_user_info]], fields=[[0]])" + LINE_SEPARATOR;
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectJoinWhere() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_CROSS_WHERE_CONDITION, false);
        SqlNode sqlNode = SQLNodeConverterEngine.convert(sqlStatement);
        String actual = optimizeEngine.optimize(sqlNode).getBestPlan().explain();
        String expected = "EnumerableCalc(expr#0..4=[{inputs}], proj#0..1=[{exprs}], user_id0=[$t3])" + LINE_SEPARATOR
                + "  EnumerableHashJoin(condition=[=($2, $4)], joinType=[inner])" + LINE_SEPARATOR
                + "    EnumerableCalc(expr#0..1=[{inputs}], expr#2=[CAST($t1):VARCHAR], proj#0..2=[{exprs}])" + LINE_SEPARATOR
                + "      TranslatableTableScan(table=[[federate_jdbc, t_order_federate]], fields=[[0, 1]])" + LINE_SEPARATOR
                + "    EnumerableCalc(expr#0=[{inputs}], expr#1=[CAST($t0):VARCHAR], proj#0..1=[{exprs}])" + LINE_SEPARATOR
                + "      TranslatableTableScan(table=[[federate_jdbc, t_user_info]], fields=[[0]], filters=[[=(CAST($0):INTEGER, 13), null]])" + LINE_SEPARATOR;
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectSubQueryFrom() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_SUBQUERY_FROM, false);
        SqlNode sqlNode = SQLNodeConverterEngine.convert(sqlStatement);
        String actual = optimizeEngine.optimize(sqlNode).getBestPlan().explain();
        String expected = "TranslatableTableScan(table=[[federate_jdbc, t_user_info]], fields=[[0, 1]], filters=[[>(CAST($0):INTEGER, 1), null]])" + LINE_SEPARATOR;
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectSubQueryWhereExist() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_SUBQUERY_WHERE_EXIST, false);
        SqlNode sqlNode = SQLNodeConverterEngine.convert(sqlStatement);
        String actual = optimizeEngine.optimize(sqlNode).getBestPlan().explain();
        String expected = "EnumerableCalc(expr#0..3=[{inputs}], expr#4=[IS NOT NULL($t3)], proj#0..1=[{exprs}], $condition=[$t4])" + LINE_SEPARATOR
                + "  EnumerableCorrelate(correlation=[$cor0], joinType=[left], requiredColumns=[{1}])" + LINE_SEPARATOR
                + "    TranslatableTableScan(table=[[federate_jdbc, t_order_federate]], fields=[[0, 1, 2]])" + LINE_SEPARATOR
                + "    EnumerableAggregate(group=[{}], agg#0=[MIN($0)])" + LINE_SEPARATOR
                + "      EnumerableCalc(expr#0..1=[{inputs}], expr#2=[true], $f0=[$t2])" + LINE_SEPARATOR
                + "        TranslatableTableScan(table=[[federate_jdbc, t_user_info]], fields=[[0, 1]], filters=[[=(CAST($cor0.user_id):VARCHAR, CAST($0):VARCHAR), null]])" + LINE_SEPARATOR;
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectSubQueryWhereIn() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_SUBQUERY_WHERE_IN, false);
        SqlNode sqlNode = SQLNodeConverterEngine.convert(sqlStatement);
        String actual = optimizeEngine.optimize(sqlNode).getBestPlan().explain();
        String expected = "EnumerableCalc(expr#0..2=[{inputs}], proj#0..1=[{exprs}])" + LINE_SEPARATOR
                + "  EnumerableHashJoin(condition=[=($1, $2)], joinType=[inner])" + LINE_SEPARATOR
                + "    TranslatableTableScan(table=[[federate_jdbc, t_order_federate]], fields=[[0, 1]])" + LINE_SEPARATOR
                + "    EnumerableAggregate(group=[{0}])" + LINE_SEPARATOR
                + "      TranslatableTableScan(table=[[federate_jdbc, t_user_info]], fields=[[0]])" + LINE_SEPARATOR;
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectSubQueryWhereBetween() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_SUBQUERY_WHERE_BETWEEN, false);
        SqlNode sqlNode = SQLNodeConverterEngine.convert(sqlStatement);
        String actual = optimizeEngine.optimize(sqlNode).getBestPlan().explain();
        String expected = "EnumerableCalc(expr#0..2=[{inputs}], proj#0..1=[{exprs}])" + LINE_SEPARATOR
                + "  EnumerableNestedLoopJoin(condition=[<=($1, $2)], joinType=[inner])" + LINE_SEPARATOR
                + "    EnumerableCalc(expr#0..2=[{inputs}], proj#0..1=[{exprs}])" + LINE_SEPARATOR
                + "      EnumerableNestedLoopJoin(condition=[>=($1, $2)], joinType=[inner])" + LINE_SEPARATOR
                + "        TranslatableTableScan(table=[[federate_jdbc, t_order_federate]], fields=[[0, 1]])" + LINE_SEPARATOR
                + "        EnumerableAggregate(group=[{}], agg#0=[SINGLE_VALUE($0)])" + LINE_SEPARATOR
                + "          TranslatableTableScan(table=[[federate_jdbc, t_user_info]], fields=[[0]], filters=[[=(CAST($0):INTEGER, 1)]])" + LINE_SEPARATOR
                + "    EnumerableAggregate(group=[{}], agg#0=[SINGLE_VALUE($0)])" + LINE_SEPARATOR
                + "      TranslatableTableScan(table=[[federate_jdbc, t_user_info]], fields=[[0]], filters=[[=(CAST($0):INTEGER, 3)]])" + LINE_SEPARATOR;
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectUnion() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_UNION, false);
        SqlNode sqlNode = SQLNodeConverterEngine.convert(sqlStatement);
        String actual = optimizeEngine.optimize(sqlNode).getBestPlan().explain();
        String expected = "EnumerableUnion(all=[false])" + LINE_SEPARATOR
                + "  TranslatableTableScan(table=[[federate_jdbc, t_order_federate]], fields=[[0, 1]])" + LINE_SEPARATOR
                + "  EnumerableCalc(expr#0=[{inputs}], expr#1=['1':VARCHAR], EXPR$0=[$t1], user_id=[$t0])" + LINE_SEPARATOR
                + "    TranslatableTableScan(table=[[federate_jdbc, t_user_info]], fields=[[0]], filters=[[=(CAST($1):VARCHAR, 'before'), null]])" + LINE_SEPARATOR;
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectLimit() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_LIMIT, false);
        SqlNode sqlNode = SQLNodeConverterEngine.convert(sqlStatement);
        String actual = optimizeEngine.optimize(sqlNode).getBestPlan().explain();
        String expected = "EnumerableLimit(fetch=[1])" + LINE_SEPARATOR
                + "  TranslatableTableScan(table=[[federate_jdbc, t_order_federate]], fields=[[0, 1]])" + LINE_SEPARATOR;
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectAggregationFunction() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_AGGREGATION, false);
        String actual = optimizeEngine.optimize(sqlStatement).getBestPlan().explain();
        String expected =
                "EnumerableCalc(expr#0..5=[{inputs}], expr#6=[0], expr#7=[=($t4, $t6)], expr#8=[null:DECIMAL(19, 9)], "
                        + "expr#9=[CASE($t7, $t8, $t3)], expr#10=[/($t9, $t4)], EXPR$0=[$t1], EXPR$1=[$t2], EXPR$2=[$t9], EXPR$3=[$t10], EXPR$4=[$t5])"
                        + LINE_SEPARATOR
                        + "  EnumerableAggregate(group=[{0}], EXPR$0=[MAX($1)], EXPR$1=[MIN($1)], EXPR$2=[$SUM0($2)], agg#3=[COUNT($2)], EXPR$4=[COUNT()])" + LINE_SEPARATOR
                        + "    EnumerableCalc(expr#0..1=[{inputs}], expr#2=[CAST($t0):DECIMAL(19, 9)], user_id=[$t1], order_id=[$t0], $f2=[$t2])" + LINE_SEPARATOR
                        + "      TranslatableTableScan(table=[[federate_jdbc, t_order_federate]], fields=[[1, 0]])" + LINE_SEPARATOR;
        assertThat(actual, is(expected));
    }
}
