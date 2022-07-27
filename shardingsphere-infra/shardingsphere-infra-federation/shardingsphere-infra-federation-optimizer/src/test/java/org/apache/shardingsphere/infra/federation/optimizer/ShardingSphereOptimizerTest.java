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

import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContextFactory;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Before;
import org.junit.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingSphereOptimizerTest {
    
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
    
    private final String databaseName = "sharding_db";
    
    private final String schemaName = "federate_jdbc";
    
    private final SQLParserRule sqlParserRule = new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build());
    
    private ShardingSphereOptimizer optimizer;
    
    @Before
    public void init() throws Exception {
        Map<String, ShardingSphereTable> tables = new HashMap<>(2, 1);
        tables.put("t_order_federate", createOrderTableMetaData());
        tables.put("t_user_info", createUserInfoTableMetaData());
        ShardingSphereDatabase database = new ShardingSphereDatabase(databaseName,
                new H2DatabaseType(), mockResource(), null, Collections.singletonMap(schemaName, new ShardingSphereSchema(tables)));
        optimizer = new ShardingSphereOptimizer(OptimizerContextFactory.create(Collections.singletonMap(databaseName, database), createGlobalRuleMetaData()));
    }
    
    private ShardingSphereRuleMetaData createGlobalRuleMetaData() {
        Collection<ShardingSphereRule> rules = new LinkedList<>();
        CacheOption cacheOption = new CacheOption(128, 1024L);
        rules.add(new SQLParserRule(new SQLParserRuleConfiguration(false, cacheOption, cacheOption)));
        return new ShardingSphereRuleMetaData(rules);
    }
    
    private ShardingSphereResource mockResource() {
        ShardingSphereResource result = mock(ShardingSphereResource.class);
        when(result.getDatabaseType()).thenReturn(new H2DatabaseType());
        return result;
    }
    
    private ShardingSphereTable createOrderTableMetaData() {
        ShardingSphereColumn orderIdColumn = new ShardingSphereColumn("order_id", Types.VARCHAR, true, false, false);
        ShardingSphereColumn userIdColumn = new ShardingSphereColumn("user_id", Types.VARCHAR, false, false, false);
        ShardingSphereColumn statusColumn = new ShardingSphereColumn("status", Types.VARCHAR, false, false, false);
        return new ShardingSphereTable("t_order_federate", Arrays.asList(orderIdColumn, userIdColumn, statusColumn), Collections.emptyList(), Collections.emptyList());
    }
    
    private ShardingSphereTable createUserInfoTableMetaData() {
        ShardingSphereColumn userIdColumn = new ShardingSphereColumn("user_id", Types.VARCHAR, true, false, false);
        ShardingSphereColumn informationColumn = new ShardingSphereColumn("information", Types.VARCHAR, false, false, false);
        return new ShardingSphereTable("t_user_info", Arrays.asList(userIdColumn, informationColumn), Collections.emptyList(), Collections.emptyList());
    }
    
    @Test
    public void assertSelectCrossJoinCondition() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_CROSS_JOIN_CONDITION, false);
        String actual = optimizer.optimize(databaseName, schemaName, sqlStatement).explain();
        String expected = "EnumerableCalc(expr#0..4=[{inputs}], expr#5=[CAST($t3):INTEGER], expr#6=[13], expr#7=[=($t5, $t6)], proj#0..1=[{exprs}], user_id1=[$t3], $condition=[$t7])\n"
                + "  EnumerableHashJoin(condition=[=($2, $4)], joinType=[inner])\n"
                + "    EnumerableCalc(expr#0..2=[{inputs}], expr#3=[CAST($t1):VARCHAR], proj#0..1=[{exprs}], user_id0=[$t3])\n"
                + "      EnumerableTableScan(table=[[federate_jdbc, t_order_federate]])\n"
                + "    EnumerableCalc(expr#0..1=[{inputs}], expr#2=[CAST($t0):VARCHAR], user_id=[$t0], user_id0=[$t2])\n"
                + "      EnumerableTableScan(table=[[federate_jdbc, t_user_info]])\n";
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectWhereAllFields() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_WHERE_ALL_FIELDS, false);
        String actual = optimizer.optimize(databaseName, schemaName, sqlStatement).explain();
        String expected = "EnumerableCalc(expr#0..1=[{inputs}], expr#2=[CAST($t0):INTEGER], expr#3=[12], expr#4=[=($t2, $t3)], proj#0..1=[{exprs}], $condition=[$t4])\n"
                + "  EnumerableTableScan(table=[[federate_jdbc, t_user_info]])\n";
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectWhereSingleField() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_WHERE_SINGLE_FIELD, false);
        String actual = optimizer.optimize(databaseName, schemaName, sqlStatement).explain();
        String expected = "EnumerableCalc(expr#0..1=[{inputs}], expr#2=[CAST($t0):INTEGER], expr#3=[12], expr#4=[=($t2, $t3)], user_id=[$t0], $condition=[$t4])\n"
                + "  EnumerableTableScan(table=[[federate_jdbc, t_user_info]])\n";
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectCrossWhere() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_CROSS_WHERE, false);
        String actual = optimizer.optimize(databaseName, schemaName, sqlStatement).explain();
        String expected = "EnumerableNestedLoopJoin(condition=[=(CAST($1):VARCHAR, CAST($2):VARCHAR)], joinType=[inner])\n"
                + "  EnumerableCalc(expr#0..2=[{inputs}], proj#0..1=[{exprs}])\n"
                + "    EnumerableTableScan(table=[[federate_jdbc, t_order_federate]])\n"
                + "  EnumerableCalc(expr#0..1=[{inputs}], user_id=[$t0])\n"
                + "    EnumerableTableScan(table=[[federate_jdbc, t_user_info]])\n";
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectCrossJoin() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_CROSS_JOIN, false);
        String actual = optimizer.optimize(databaseName, schemaName, sqlStatement).explain();
        String expected = "EnumerableCalc(expr#0..4=[{inputs}], proj#0..1=[{exprs}], user_id0=[$t3])\n"
                + "  EnumerableHashJoin(condition=[=($2, $4)], joinType=[inner])\n"
                + "    EnumerableCalc(expr#0..2=[{inputs}], expr#3=[CAST($t1):VARCHAR], proj#0..1=[{exprs}], user_id0=[$t3])\n"
                + "      EnumerableTableScan(table=[[federate_jdbc, t_order_federate]])\n"
                + "    EnumerableCalc(expr#0..1=[{inputs}], expr#2=[CAST($t0):VARCHAR], user_id=[$t0], user_id0=[$t2])\n"
                + "      EnumerableTableScan(table=[[federate_jdbc, t_user_info]])\n";
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectJoinWhere() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_CROSS_WHERE_CONDITION, false);
        String actual = optimizer.optimize(databaseName, schemaName, sqlStatement).explain();
        String expected = "EnumerableNestedLoopJoin(condition=[=(CAST($1):VARCHAR, CAST($2):VARCHAR)], joinType=[inner])\n"
                + "  EnumerableCalc(expr#0..2=[{inputs}], proj#0..1=[{exprs}])\n"
                + "    EnumerableTableScan(table=[[federate_jdbc, t_order_federate]])\n"
                + "  EnumerableCalc(expr#0..1=[{inputs}], expr#2=[CAST($t0):INTEGER], expr#3=[13], expr#4=[=($t2, $t3)], user_id=[$t0], $condition=[$t4])\n"
                + "    EnumerableTableScan(table=[[federate_jdbc, t_user_info]])\n";
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectSubQueryFrom() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_SUBQUERY_FROM, false);
        String actual = optimizer.optimize(databaseName, schemaName, sqlStatement).explain();
        String expected = "EnumerableCalc(expr#0..1=[{inputs}], expr#2=[CAST($t0):INTEGER], expr#3=[1], expr#4=[>($t2, $t3)], proj#0..1=[{exprs}], $condition=[$t4])\n"
                + "  EnumerableTableScan(table=[[federate_jdbc, t_user_info]])\n";
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectSubQueryWhereExist() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_SUBQUERY_WHERE_EXIST, false);
        String actual = optimizer.optimize(databaseName, schemaName, sqlStatement).explain();
        String expected = "EnumerableCalc(expr#0..4=[{inputs}], expr#5=[IS NOT NULL($t4)], proj#0..1=[{exprs}], $condition=[$t5])\n"
                + "  EnumerableHashJoin(condition=[=($2, $3)], joinType=[left])\n"
                + "    EnumerableCalc(expr#0..2=[{inputs}], expr#3=[CAST($t1):VARCHAR], proj#0..1=[{exprs}], user_id0=[$t3])\n"
                + "      EnumerableTableScan(table=[[federate_jdbc, t_order_federate]])\n"
                + "    EnumerableAggregate(group=[{0}], agg#0=[MIN($1)])\n"
                + "      EnumerableCalc(expr#0..1=[{inputs}], expr#2=[CAST($t0):VARCHAR], expr#3=[true], expr#4=[IS NOT NULL($t2)], user_id0=[$t2], $f0=[$t3], $condition=[$t4])\n"
                + "        EnumerableTableScan(table=[[federate_jdbc, t_user_info]])\n";
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectSubQueryWhereIn() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_SUBQUERY_WHERE_IN, false);
        String actual = optimizer.optimize(databaseName, schemaName, sqlStatement).explain();
        String expected = "EnumerableCalc(expr#0..2=[{inputs}], proj#0..1=[{exprs}])\n"
                + "  EnumerableHashJoin(condition=[=($1, $2)], joinType=[inner])\n"
                + "    EnumerableCalc(expr#0..2=[{inputs}], proj#0..1=[{exprs}])\n"
                + "      EnumerableTableScan(table=[[federate_jdbc, t_order_federate]])\n"
                + "    EnumerableAggregate(group=[{0}])\n"
                + "      EnumerableCalc(expr#0..1=[{inputs}], user_id=[$t0])\n"
                + "        EnumerableTableScan(table=[[federate_jdbc, t_user_info]])\n";
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertSelectSubQueryWhereBetween() {
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(DatabaseTypeEngine.getTrunkDatabaseTypeName(new H2DatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_SUBQUERY_WHERE_BETWEEN, false);
        String actual = optimizer.optimize(databaseName, schemaName, sqlStatement).explain();
        String expected = "EnumerableCalc(expr#0..3=[{inputs}], proj#0..1=[{exprs}])\n"
                + "  EnumerableNestedLoopJoin(condition=[<=($1, $3)], joinType=[inner])\n"
                + "    EnumerableNestedLoopJoin(condition=[>=($1, $2)], joinType=[inner])\n"
                + "      EnumerableCalc(expr#0..2=[{inputs}], proj#0..1=[{exprs}])\n"
                + "        EnumerableTableScan(table=[[federate_jdbc, t_order_federate]])\n"
                + "      EnumerableAggregate(group=[{}], agg#0=[SINGLE_VALUE($0)])\n"
                + "        EnumerableCalc(expr#0..1=[{inputs}], expr#2=[CAST($t1):VARCHAR], expr#3=['before':VARCHAR], expr#4=[=($t2, $t3)], user_id=[$t0], $condition=[$t4])\n"
                + "          EnumerableTableScan(table=[[federate_jdbc, t_user_info]])\n"
                + "    EnumerableAggregate(group=[{}], agg#0=[SINGLE_VALUE($0)])\n"
                + "      EnumerableCalc(expr#0..1=[{inputs}], expr#2=[CAST($t1):VARCHAR], expr#3=['after':VARCHAR], expr#4=[=($t2, $t3)], user_id=[$t0], $condition=[$t4])\n"
                + "        EnumerableTableScan(table=[[federate_jdbc, t_user_info]])\n";
        assertThat(actual, is(expected));
    }
}
