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

package org.apache.shardingsphere.core.rewrite.rewriter;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.constant.AggregationType;
import org.apache.shardingsphere.core.constant.OrderDirection;
import org.apache.shardingsphere.core.optimize.statement.OptimizedStatement;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.ShardingWhereOptimizedStatement;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.condition.ShardingConditions;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.insert.ShardingInsertColumns;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.insert.ShardingInsertOptimizedStatement;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.groupby.GroupBy;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.orderby.OrderBy;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.orderby.OrderByItem;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.pagination.Pagination;
import org.apache.shardingsphere.core.optimize.statement.transparent.TransparentOptimizedStatement;
import org.apache.shardingsphere.core.parse.constant.QuoteCharacter;
import org.apache.shardingsphere.core.parse.sql.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.sql.context.condition.Column;
import org.apache.shardingsphere.core.parse.sql.context.condition.Condition;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.AggregationSelectItem;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.DerivedCommonSelectItem;
import org.apache.shardingsphere.core.parse.sql.context.table.Table;
import org.apache.shardingsphere.core.parse.sql.segment.common.SchemaSegment;
import org.apache.shardingsphere.core.parse.sql.segment.common.TableSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dal.FromSchemaSegment;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.InsertValuesSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.SelectItemsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.AggregationDistinctSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dal.DALStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.rewrite.SQLRewriteEngine;
import org.apache.shardingsphere.core.rewrite.builder.BaseParameterBuilder;
import org.apache.shardingsphere.core.rewrite.builder.SQLBuilder;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.type.RoutingResult;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.route.type.TableUnit;
import org.apache.shardingsphere.core.rule.DataNode;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlRootShardingConfiguration;
import org.apache.shardingsphere.core.yaml.engine.YamlEngine;
import org.apache.shardingsphere.core.yaml.swapper.impl.ShardingRuleConfigurationYamlSwapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingSQLRewriterTest {
    
    private ShardingRule shardingRule;
    
    private Map<String, String> logicTableAndActualTables = Collections.singletonMap("table_x", "table_1");
    
    @Before
    public void setUp() throws IOException {
        URL url = ShardingSQLRewriterTest.class.getClassLoader().getResource("yaml/rewrite-rule.yaml");
        Preconditions.checkNotNull(url, "Cannot found rewrite rule yaml configuration.");
        YamlRootShardingConfiguration yamlShardingConfig = YamlEngine.unmarshal(new File(url.getFile()), YamlRootShardingConfiguration.class);
        shardingRule = new ShardingRule(new ShardingRuleConfigurationYamlSwapper().swap(yamlShardingConfig.getShardingRule()), yamlShardingConfig.getDataSources().keySet());
    }
    
    @Test
    public void assertRewriteWithoutChange() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultWithoutChange(), Collections.<Object>singletonList(1));
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT table_y.id FROM table_y WHERE table_y.id=?"));
        assertThat(rewriteEngine.generateSQL().getSql(), is("SELECT table_y.id FROM table_y WHERE table_y.id=?"));
    }
    
    private SQLRouteResult createSQLRouteResultWithoutChange() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setLogicSQL("SELECT table_y.id FROM table_y WHERE table_y.id=?");
        SQLRouteResult result = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), new AndCondition(), 
                selectStatement.getItems(), new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), 
                new Pagination(null, null, Collections.emptyList())));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteTableName() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForTableName(), Arrays.<Object>asList(1, "x"));
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT table_1.id, x.name FROM table_1 x WHERE table_1.id=? AND x.name=?"));
    }
    
    private SQLRouteResult createRouteResultForTableName() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setLogicSQL("SELECT table_x.id, x.name FROM table_x x WHERE table_x.id=? AND x.name=?");
        selectStatement.getSQLSegments().add(new TableSegment(7, 13, "table_x"));
        selectStatement.getSQLSegments().add(new TableSegment(31, 37, "table_x"));
        selectStatement.getSQLSegments().add(new TableSegment(47, 53, "table_x"));
        SQLRouteResult result = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), new AndCondition(), 
                selectStatement.getItems(), new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), 
                new Pagination(null, null, Collections.emptyList())));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteOrderByAndGroupByDerivedColumns() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForOrderByAndGroupByDerivedColumns(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is(
                "SELECT x.age , x.id AS GROUP_BY_DERIVED_0 , x.name AS ORDER_BY_DERIVED_0 FROM table_1 x GROUP BY x.id ORDER BY x.name"));
    }
    
    private SQLRouteResult createRouteResultForOrderByAndGroupByDerivedColumns() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setLogicSQL("SELECT x.age FROM table_x x GROUP BY x.id ORDER BY x.name");
        selectStatement.getSQLSegments().add(new TableSegment(18, 24, "table_x"));
        DerivedCommonSelectItem selectItem1 = new DerivedCommonSelectItem("x.id", Optional.of("GROUP_BY_DERIVED_0"));
        DerivedCommonSelectItem selectItem2 = new DerivedCommonSelectItem("x.name", Optional.of("ORDER_BY_DERIVED_0"));
        selectStatement.getItems().add(selectItem1);
        selectStatement.getItems().add(selectItem2);
        selectStatement.setSelectListStopIndex(11);
        SQLRouteResult result = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), new AndCondition(), 
                selectStatement.getItems(), new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), 
                new Pagination(null, null, Collections.emptyList())));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteAggregationDerivedColumns() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForAggregationDerivedColumns(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is(
                "SELECT AVG(x.age) , COUNT(x.age) AS AVG_DERIVED_COUNT_0 , SUM(x.age) AS AVG_DERIVED_SUM_0 FROM table_1 x"));
    }
    
    private SQLRouteResult createRouteResultForAggregationDerivedColumns() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setLogicSQL("SELECT AVG(x.age) FROM table_x x");
        selectStatement.getSQLSegments().add(new TableSegment(23, 29, "table_x"));
        AggregationSelectItem countSelectItem = new AggregationSelectItem(AggregationType.COUNT, "(x.age)", Optional.of("AVG_DERIVED_COUNT_0"));
        AggregationSelectItem sumSelectItem = new AggregationSelectItem(AggregationType.SUM, "(x.age)", Optional.of("AVG_DERIVED_SUM_0"));
        AggregationSelectItem avgSelectItem = new AggregationSelectItem(AggregationType.AVG, "(x.age)", Optional.<String>absent());
        avgSelectItem.getDerivedAggregationSelectItems().add(countSelectItem);
        avgSelectItem.getDerivedAggregationSelectItems().add(sumSelectItem);
        selectStatement.setSelectListStopIndex(16);
        selectStatement.getItems().add(avgSelectItem);
        SQLRouteResult routeResult = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), new AndCondition(), 
                selectStatement.getItems(), new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), 
                new Pagination(null, null, Collections.emptyList())));
        routeResult.setRoutingResult(new RoutingResult());
        return routeResult;
    }
    
    @Test
    public void assertRewriteAutoGeneratedKeyColumn() {
        RoutingUnit routingUnit = new RoutingUnit("db0");
        routingUnit.getTableUnits().add(new TableUnit("table_x", "table_1"));
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForAutoGeneratedKeyColumn(), Arrays.<Object>asList("Bill", 1));
        assertThat(rewriteEngine.generateSQL(routingUnit, logicTableAndActualTables).getSql(), is("INSERT INTO table_1 (name, age, id) VALUES (?, ?, ?)"));
    }
    
    private SQLRouteResult createRouteResultForAutoGeneratedKeyColumn() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setLogicSQL("INSERT INTO table_x (name, age) VALUES (?, ?)");
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.getColumnNames().add("name");
        insertStatement.getColumnNames().add("age");
        insertStatement.getSQLSegments().add(new InsertColumnsSegment(30, 30, Collections.singleton(mock(ColumnSegment.class))));
        insertStatement.getSQLSegments().add(new InsertValuesSegment(39, 44, Collections.<ExpressionSegment>emptyList()));
        insertStatement.getSQLSegments().add(new TableSegment(12, 18, "table_x"));
        ShardingInsertColumns insertColumns = mock(ShardingInsertColumns.class);
        when(insertColumns.getRegularColumnNames()).thenReturn(Arrays.asList("name", "age"));
        when(insertColumns.getAllColumnNames()).thenReturn(Arrays.asList("name", "age", "id"));
        ShardingInsertOptimizedStatement optimizedStatement = new ShardingInsertOptimizedStatement(insertStatement, Collections.<ShardingCondition>emptyList(), insertColumns, null);
        ExpressionSegment[] expressionSegments = {new ParameterMarkerExpressionSegment(0, 0, 0), new ParameterMarkerExpressionSegment(0, 0, 1), new ParameterMarkerExpressionSegment(0, 0, 2)};
        optimizedStatement.addUnit(expressionSegments, new Object[] {"x", 1, 1}, 3);
        optimizedStatement.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteAutoGeneratedKeyColumnWithoutColumnsWithParameter() {
        RoutingUnit routingUnit = new RoutingUnit("db0");
        routingUnit.getTableUnits().add(new TableUnit("table_x", "table_1"));
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForAutoGeneratedKeyColumnWithoutColumnsWithParameter(), Arrays.<Object>asList("Bill", 1));
        assertThat(rewriteEngine.generateSQL(routingUnit, logicTableAndActualTables).getSql(), is("INSERT INTO `table_1`(name, id) VALUES (?, ?)"));
    }
    
    private SQLRouteResult createRouteResultForAutoGeneratedKeyColumnWithoutColumnsWithParameter() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setLogicSQL("INSERT INTO `table_x` VALUES (?)");
        insertStatement.getColumnNames().add("name");
        insertStatement.getSQLSegments().add(new InsertColumnsSegment(21, 21, Collections.<ColumnSegment>emptyList()));
        insertStatement.getSQLSegments().add(new InsertValuesSegment(29, 31, Collections.<ExpressionSegment>emptyList()));
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.getSQLSegments().add(new TableSegment(12, 20, "`table_x`"));
        ShardingInsertColumns insertColumns = mock(ShardingInsertColumns.class);
        when(insertColumns.getRegularColumnNames()).thenReturn(Collections.singletonList("name"));
        when(insertColumns.getAllColumnNames()).thenReturn(Arrays.asList("name", "id"));
        ShardingInsertOptimizedStatement optimizedStatement = new ShardingInsertOptimizedStatement(insertStatement, Collections.<ShardingCondition>emptyList(), insertColumns, null);
        ExpressionSegment[] expressionSegments = {new ParameterMarkerExpressionSegment(0, 0, 0), new ParameterMarkerExpressionSegment(0, 0, 1)};
        optimizedStatement.addUnit(expressionSegments, new Object[] {"Bill", 1}, 2);
        optimizedStatement.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteAutoGeneratedKeyColumnWithoutColumnsWithoutParameter() {
        RoutingUnit routingUnit = new RoutingUnit("db0");
        routingUnit.getTableUnits().add(new TableUnit("table_x", "table_1"));
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForAutoGeneratedKeyColumnWithoutColumnsWithoutParameter(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(routingUnit, logicTableAndActualTables).getSql(), is("INSERT INTO `table_1`(name, id) VALUES (10, 1)"));
    }
    
    private SQLRouteResult createRouteResultForAutoGeneratedKeyColumnWithoutColumnsWithoutParameter() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setLogicSQL("INSERT INTO `table_x` VALUES (10)");
        insertStatement.getColumnNames().add("name");
        insertStatement.getSQLSegments().add(new InsertColumnsSegment(21, 21, Collections.<ColumnSegment>emptyList()));
        insertStatement.getSQLSegments().add(new InsertValuesSegment(29, 32, Collections.<ExpressionSegment>emptyList()));
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.getSQLSegments().add(new TableSegment(12, 20, "`table_x`"));
        ShardingInsertColumns insertColumns = mock(ShardingInsertColumns.class);
        when(insertColumns.getRegularColumnNames()).thenReturn(Collections.singletonList("name"));
        when(insertColumns.getAllColumnNames()).thenReturn(Arrays.asList("name", "id"));
        ShardingInsertOptimizedStatement optimizedStatement = new ShardingInsertOptimizedStatement(insertStatement, Collections.<ShardingCondition>emptyList(), insertColumns, null);
        ExpressionSegment[] expressionSegments = {new LiteralExpressionSegment(0, 0, 10), new LiteralExpressionSegment(0, 0, 1)};
        optimizedStatement.addUnit(expressionSegments, new Object[0], 0);
        optimizedStatement.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        SQLRouteResult routeResult = new SQLRouteResult(optimizedStatement);
        routeResult.setRoutingResult(new RoutingResult());
        return routeResult;
    }
    
    @Test
    public void assertRewriteDuplicateKeyWithoutColumnsWithoutParameter() {
        RoutingUnit routingUnit = new RoutingUnit("db0");
        routingUnit.getTableUnits().add(new TableUnit("table_x", "table_1"));
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForDuplicateKeyWithoutColumnsWithoutParameter(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(routingUnit, logicTableAndActualTables).getSql(), is("INSERT INTO `table_1`(name, id) VALUES (10, 1) ON DUPLICATE KEY UPDATE name = VALUES(name)"));
    }
    
    private SQLRouteResult createRouteResultForDuplicateKeyWithoutColumnsWithoutParameter() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setLogicSQL("INSERT INTO `table_x` VALUES (10) ON DUPLICATE KEY UPDATE name = VALUES(name)");
        insertStatement.getColumnNames().add("name");
        insertStatement.getSQLSegments().add(new InsertColumnsSegment(21, 21, Collections.<ColumnSegment>emptyList()));
        insertStatement.getSQLSegments().add(new InsertValuesSegment(29, 32, Collections.<ExpressionSegment>emptyList()));
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.getSQLSegments().add(new TableSegment(12, 20, "`table_x`"));
        ShardingInsertColumns insertColumns = mock(ShardingInsertColumns.class);
        when(insertColumns.getRegularColumnNames()).thenReturn(Collections.singletonList("name"));
        when(insertColumns.getAllColumnNames()).thenReturn(Arrays.asList("name", "id"));
        ShardingInsertOptimizedStatement optimizedStatement = new ShardingInsertOptimizedStatement(insertStatement, Collections.<ShardingCondition>emptyList(), insertColumns, null);
        ExpressionSegment[] expressionSegments = {new LiteralExpressionSegment(0, 0, 10), new LiteralExpressionSegment(0, 0, 1)};
        optimizedStatement.addUnit(expressionSegments, new Object[0], 0);
        optimizedStatement.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteDuplicateKeyWithSetWithoutParameter() {
        RoutingUnit routingUnit = new RoutingUnit("db0");
        routingUnit.getTableUnits().add(new TableUnit("table_x", "table_1"));
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForDuplicateKeyWithSetWithoutParameter(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(routingUnit, logicTableAndActualTables).getSql(), is("INSERT INTO `table_1` set name = 10, id = 1 ON DUPLICATE KEY UPDATE name = VALUES(name)"));
    }
    
    private SQLRouteResult createRouteResultForDuplicateKeyWithSetWithoutParameter() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setLogicSQL("INSERT INTO `table_x` set name = 10, id = 1 ON DUPLICATE KEY UPDATE name = VALUES(name)");
        insertStatement.getColumnNames().add("name");
        insertStatement.getColumnNames().add("id");
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.getSQLSegments().add(new TableSegment(12, 20, "`table_x`"));
        ShardingInsertColumns insertColumns = mock(ShardingInsertColumns.class);
        when(insertColumns.getRegularColumnNames()).thenReturn(Arrays.asList("name", "id"));
        when(insertColumns.getAllColumnNames()).thenReturn(Arrays.asList("name", "id"));
        ShardingInsertOptimizedStatement optimizedStatement = new ShardingInsertOptimizedStatement(insertStatement, Collections.<ShardingCondition>emptyList(), insertColumns, null);
        ExpressionSegment[] expressionSegments = {new LiteralExpressionSegment(0, 0, 10), new LiteralExpressionSegment(0, 0, 1)};
        optimizedStatement.addUnit(expressionSegments, new Object[0], 0);
        optimizedStatement.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteColumnWithoutColumnsWithoutParameter() {
        RoutingUnit routingUnit = new RoutingUnit("db0");
        routingUnit.getTableUnits().add(new TableUnit("table_x", "table_1"));
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForColumnWithoutColumnsWithoutParameter(), Arrays.<Object>asList("x", 1));
        assertThat(rewriteEngine.generateSQL(routingUnit, logicTableAndActualTables).getSql(), is("INSERT INTO `table_1`(name, id) VALUES (10, 1)"));
    }
    
    private SQLRouteResult createRouteResultForColumnWithoutColumnsWithoutParameter() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setLogicSQL("INSERT INTO `table_x` VALUES (10, 1)");
        insertStatement.getColumnNames().add("name");
        insertStatement.getColumnNames().add("id");
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.getSQLSegments().add(new TableSegment(12, 20, "`table_x`"));
        insertStatement.getSQLSegments().add(new InsertColumnsSegment(21, 21, Collections.<ColumnSegment>emptyList()));
        insertStatement.getSQLSegments().add(new InsertValuesSegment(29, 35, Collections.<ExpressionSegment>emptyList()));
        ShardingInsertColumns insertColumns = mock(ShardingInsertColumns.class);
        when(insertColumns.getRegularColumnNames()).thenReturn(Arrays.asList("name", "id"));
        when(insertColumns.getAllColumnNames()).thenReturn(Arrays.asList("name", "id"));
        ShardingInsertOptimizedStatement optimizedStatement = new ShardingInsertOptimizedStatement(insertStatement, Collections.<ShardingCondition>emptyList(), insertColumns, null);
        ExpressionSegment[] expressionSegments = {new LiteralExpressionSegment(0, 0, 10), new LiteralExpressionSegment(0, 0, 1)};
        optimizedStatement.addUnit(expressionSegments, new Object[0], 0);
        optimizedStatement.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteColumnWithoutColumnsWithParameter() {
        RoutingUnit routingUnit = new RoutingUnit("db0");
        routingUnit.getTableUnits().add(new TableUnit("table_x", "table_1"));
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForColumnWithoutColumnsWithParameter(), Arrays.<Object>asList("x", 1));
        assertThat(rewriteEngine.generateSQL(routingUnit, logicTableAndActualTables).getSql(), is("INSERT INTO `table_1`(name, id) VALUES (?, ?)"));
    }
    
    private SQLRouteResult createRouteResultForColumnWithoutColumnsWithParameter() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setLogicSQL("INSERT INTO `table_x` VALUES (?, ?)");
        insertStatement.getColumnNames().add("name");
        insertStatement.getSQLSegments().add(new InsertColumnsSegment(21, 21, Collections.<ColumnSegment>emptyList()));
        insertStatement.getSQLSegments().add(new InsertValuesSegment(29, 34, Collections.<ExpressionSegment>emptyList()));
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.getSQLSegments().add(new TableSegment(12, 20, "`table_x`"));
        ShardingInsertColumns insertColumns = mock(ShardingInsertColumns.class);
        when(insertColumns.getRegularColumnNames()).thenReturn(Collections.singletonList("name"));
        when(insertColumns.getAllColumnNames()).thenReturn(Arrays.asList("name", "id"));
        ShardingInsertOptimizedStatement optimizedStatement = new ShardingInsertOptimizedStatement(insertStatement, Collections.<ShardingCondition>emptyList(), insertColumns, null);
        ExpressionSegment[] expressionSegments = {new ParameterMarkerExpressionSegment(0, 0, 0), new ParameterMarkerExpressionSegment(0, 0, 1)};
        optimizedStatement.addUnit(expressionSegments, new Object[] {"x", 1}, 2);
        optimizedStatement.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteLimit() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForLimit(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT x.id FROM table_1 x LIMIT 0, 4"));
    }
    
    private SQLRouteResult createRouteResultForLimit() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setLogicSQL("SELECT x.id FROM table_x x LIMIT 2, 2");
        selectStatement.getSQLSegments().add(new TableSegment(17, 23, "table_x"));
        PaginationValueSegment offsetSegment = new NumberLiteralLimitValueSegment(33, 33, 2);
        PaginationValueSegment rowCountSegment = new NumberLiteralLimitValueSegment(36, 36, 2);
        selectStatement.setOffset(offsetSegment);
        selectStatement.setRowCount(rowCountSegment);
        OptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), new AndCondition(), 
                selectStatement.getItems(), new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), 
                new Pagination(offsetSegment, rowCountSegment, Collections.emptyList()));
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteRowNumber() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForRowNumber(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(),
                is("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_1 x) row_ WHERE rownum<=4) t WHERE t.rownum_>0"));
    }
    
    private SQLRouteResult createRouteResultForRowNumber() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setLogicSQL("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_x x) row_ WHERE rownum<=4) t WHERE t.rownum_>2");
        selectStatement.getSQLSegments().add(new TableSegment(68, 74, "table_x"));
        PaginationValueSegment offsetSegment = new NumberLiteralRowNumberValueSegment(119, 119, 2, true);
        PaginationValueSegment rowCountSegment = new NumberLiteralRowNumberValueSegment(98, 98, 4, false);
        selectStatement.setOffset(offsetSegment);
        selectStatement.setRowCount(rowCountSegment);
        OptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), new AndCondition(), 
                selectStatement.getItems(), new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), 
                new Pagination(offsetSegment, rowCountSegment, Collections.emptyList()));
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteTopAndRowNumber() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForTopAndRowNumber(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(),
                is("SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_1 x) AS row_ WHERE row_.rownum_>0"));
    }
    
    private SQLRouteResult createRouteResultForTopAndRowNumber() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setLogicSQL("SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_x x) AS row_ WHERE row_.rownum_>2");
        selectStatement.getSQLSegments().add(new TableSegment(85, 91, "table_x"));
        PaginationValueSegment offsetSegment = new NumberLiteralRowNumberValueSegment(123, 123, 2, true);
        PaginationValueSegment rowCountSegment = new NumberLiteralRowNumberValueSegment(26, 26, 4, false);
        selectStatement.setOffset(offsetSegment);
        selectStatement.setRowCount(rowCountSegment);
        OptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), new AndCondition(), 
                selectStatement.getItems(), new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), 
                new Pagination(offsetSegment, rowCountSegment, Collections.emptyList()));
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteLimitForMemoryGroupBy() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForLimitForMemoryGroupBy(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT x.id FROM table_1 x LIMIT 0, 2147483647"));
    }
    
    private SQLRouteResult createRouteResultForLimitForMemoryGroupBy() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setLogicSQL("SELECT x.id FROM table_x x LIMIT 2, 2");
        ColumnSegment columnSegment = new ColumnSegment(0, 0, "id");
        columnSegment.setOwner(new TableSegment(0, 0, "x"));
        selectStatement.getSQLSegments().add(new TableSegment(17, 23, "table_x"));
        PaginationValueSegment offsetSegment = new NumberLiteralLimitValueSegment(33, 33, 2);
        PaginationValueSegment rowCountSegment = new NumberLiteralLimitValueSegment(36, 36, 2);
        selectStatement.setOffset(offsetSegment);
        selectStatement.setRowCount(rowCountSegment);
        OptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(
                selectStatement, Collections.<ShardingCondition>emptyList(), new AndCondition(), selectStatement.getItems(), 
                new GroupBy(Collections.singletonList(new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment, OrderDirection.DESC, OrderDirection.ASC))), 0), 
                new OrderBy(Collections.singletonList(new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment, OrderDirection.ASC, OrderDirection.ASC))), false), 
                new Pagination(offsetSegment, rowCountSegment, Collections.emptyList()));
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteRowNumForMemoryGroupBy() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForRowNumForMemoryGroupBy(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(),
                is("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_1 x) row_ WHERE rownum<=2147483647) t WHERE t.rownum_>0"));
    }
    
    private SQLRouteResult createRouteResultForRowNumForMemoryGroupBy() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setLogicSQL("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_x x) row_ WHERE rownum<=4) t WHERE t.rownum_>2");
        selectStatement.getSQLSegments().add(new TableSegment(68, 74, "table_x"));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, "id");
        columnSegment.setOwner(new TableSegment(0, 0, "x"));
        PaginationValueSegment offsetSegment = new NumberLiteralRowNumberValueSegment(119, 119, 2, true);
        PaginationValueSegment rowCountSegment = new NumberLiteralRowNumberValueSegment(98, 98, 4, false);
        selectStatement.setOffset(offsetSegment);
        selectStatement.setRowCount(rowCountSegment);
        OptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(
                selectStatement, Collections.<ShardingCondition>emptyList(), new AndCondition(), selectStatement.getItems(),
                new GroupBy(Collections.singletonList(new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment, OrderDirection.DESC, OrderDirection.ASC))), 0),
                new OrderBy(Collections.singletonList(new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment, OrderDirection.ASC, OrderDirection.ASC))), false), 
                new Pagination(offsetSegment, rowCountSegment, Collections.emptyList()));
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteTopAndRowNumberForMemoryGroupBy() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForTopAndRowNumberForMemoryGroupBy(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(),
                is("SELECT * FROM (SELECT TOP(2147483647) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_1 x) AS row_ WHERE row_.rownum_>0"));
    }
    
    private SQLRouteResult createRouteResultForTopAndRowNumberForMemoryGroupBy() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setLogicSQL("SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_x x) AS row_ WHERE row_.rownum_>2");
        selectStatement.getSQLSegments().add(new TableSegment(85, 91, "table_x"));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, "id");
        columnSegment.setOwner(new TableSegment(0, 0, "x"));
        PaginationValueSegment offsetSegment = new NumberLiteralRowNumberValueSegment(123, 123, 2, false);
        PaginationValueSegment rowCountSegment = new NumberLiteralRowNumberValueSegment(26, 26, 4, false);
        selectStatement.setOffset(offsetSegment);
        selectStatement.setRowCount(rowCountSegment);
        OptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(
                selectStatement, Collections.<ShardingCondition>emptyList(), new AndCondition(), selectStatement.getItems(),
                new GroupBy(Collections.singletonList(new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment, OrderDirection.DESC, OrderDirection.ASC))), 0),
                new OrderBy(Collections.singletonList(new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment, OrderDirection.ASC, OrderDirection.ASC))), false), 
                new Pagination(offsetSegment, rowCountSegment, Collections.emptyList()));
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteLimitForNotRewritePagination() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForLimitForNotRewritePagination(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT x.id FROM table_1 x LIMIT 2, 2"));
    }
    
    private SQLRouteResult createRouteResultForLimitForNotRewritePagination() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setLogicSQL("SELECT x.id FROM table_x x LIMIT 2, 2");
        selectStatement.getSQLSegments().add(new TableSegment(17, 23, "table_x"));
        PaginationValueSegment offsetSegment = new NumberLiteralLimitValueSegment(33, 33, 2);
        PaginationValueSegment rowCountSegment = new NumberLiteralLimitValueSegment(36, 36, 2);
        selectStatement.setOffset(offsetSegment);
        selectStatement.setRowCount(rowCountSegment);
        OptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), new AndCondition(), 
                selectStatement.getItems(), new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), 
                new Pagination(offsetSegment, rowCountSegment, Collections.emptyList()));
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteRowNumForNotRewritePagination() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForRowNumForNotRewritePagination(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(),
                is("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_1 x) row_ WHERE rownum<=4) t WHERE t.rownum_>2"));
    }
    
    private SQLRouteResult createRouteResultForRowNumForNotRewritePagination() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setLogicSQL("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_x x) row_ WHERE rownum<=4) t WHERE t.rownum_>2");
        selectStatement.getSQLSegments().add(new TableSegment(68, 74, "table_x"));
        PaginationValueSegment offsetSegment = new NumberLiteralRowNumberValueSegment(119, 119, 2, true);
        PaginationValueSegment rowCountSegment = new NumberLiteralRowNumberValueSegment(98, 98, 4, false);
        selectStatement.setOffset(offsetSegment);
        selectStatement.setRowCount(rowCountSegment);
        OptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), new AndCondition(), 
                selectStatement.getItems(), new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), 
                new Pagination(offsetSegment, rowCountSegment, Collections.emptyList()));
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteTopAndRowNumberForNotRewritePagination() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForTopAndRowNumberForNotRewritePagination(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(),
                is("SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_1 x) AS row_ WHERE row_.rownum_>2"));
    }
    
    private SQLRouteResult createRouteResultForTopAndRowNumberForNotRewritePagination() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setLogicSQL("SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_x x) AS row_ WHERE row_.rownum_>2");
        selectStatement.getSQLSegments().add(new TableSegment(85, 91, "table_x"));
        PaginationValueSegment offsetSegment = new NumberLiteralRowNumberValueSegment(123, 123, 2, true);
        PaginationValueSegment rowCountSegment = new NumberLiteralRowNumberValueSegment(26, 26, 4, false);
        selectStatement.setOffset(offsetSegment);
        selectStatement.setRowCount(rowCountSegment);
        OptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), new AndCondition(), 
                selectStatement.getItems(), new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), 
                new Pagination(offsetSegment, rowCountSegment, Collections.emptyList()));
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteDerivedOrderBy() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForDerivedOrderBy(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT x.id, x.name FROM table_1 x GROUP BY x.id, x.name DESC ORDER BY x.id ASC,x.name DESC "));
    }
    
    private SQLRouteResult createRouteResultForDerivedOrderBy() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getSQLSegments().add(new TableSegment(25, 31, "table_x"));
        selectStatement.setLogicSQL("SELECT x.id, x.name FROM table_x x GROUP BY x.id, x.name DESC");
        ColumnSegment columnSegment1 = new ColumnSegment(0, 0, "id");
        columnSegment1.setOwner(new TableSegment(0, 0, "x"));
        ColumnSegment columnSegment2 = new ColumnSegment(0, 0, "name");
        columnSegment2.setOwner(new TableSegment(0, 0, "x"));
        OptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(
                selectStatement, Collections.<ShardingCondition>emptyList(), new AndCondition(), selectStatement.getItems(), 
                new GroupBy(Arrays.asList(
                        new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment1, OrderDirection.ASC, OrderDirection.ASC)), 
                        new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment2, OrderDirection.DESC, OrderDirection.ASC))), 60),
                new OrderBy(Arrays.asList(
                        new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment1, OrderDirection.ASC, OrderDirection.ASC)),
                        new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment2, OrderDirection.DESC, OrderDirection.ASC))), true), new Pagination(null, null, Collections.emptyList()));
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteIndexTokenForIndexNameTableName() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForIndexTokenForIndexNameTableName(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("CREATE INDEX index_name_table_1 ON table_1 ('column')"));
    }
    
    private SQLRouteResult createRouteResultForIndexTokenForIndexNameTableName() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setLogicSQL("CREATE INDEX index_name ON table_x ('column')");
        selectStatement.getTables().add(new Table("table_x", null));
        selectStatement.getSQLSegments().add(new IndexSegment(13, 22, "index_name", QuoteCharacter.NONE));
        selectStatement.getSQLSegments().add(new TableSegment(27, 33, "table_x"));
        SQLRouteResult result = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), new AndCondition(), 
                selectStatement.getItems(), new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), 
                new Pagination(null, null, Collections.emptyList())));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertIndexTokenForIndexNameTableNameWithoutLogicTableName() {
        SelectStatement selectStatement = new SelectStatement();
    
        selectStatement.getTables().add(new Table("table_x", null));
        selectStatement.getSQLSegments().add(new IndexSegment(13, 23, "logic_index", QuoteCharacter.NONE));
        selectStatement.getSQLSegments().add(new TableSegment(28, 34, "table_x"));
        SQLRouteResult routeResult = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), new AndCondition(), 
                selectStatement.getItems(), new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), 
                new Pagination(null, null, Collections.emptyList())));
        routeResult.setRoutingResult(new RoutingResult());
        selectStatement.setLogicSQL("CREATE INDEX logic_index ON table_x ('column')");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("CREATE INDEX logic_index_table_1 ON table_1 ('column')"));
    }
    
    @Test
    public void assertTableTokenWithoutBackQuoteForShow() {
        DALStatement showTablesStatement = new DALStatement();
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 24, "table_x"));
        SQLRouteResult routeResult = new SQLRouteResult(new TransparentOptimizedStatement(showTablesStatement));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM table_x");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM table_1"));
    }
    
    @Test
    public void assertTableTokenWithoutBackQuoteFromSchemaForShow() {
        DALStatement showTablesStatement = new DALStatement();
    
        showTablesStatement.getSQLSegments().add(new FromSchemaSegment(25, 43));
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 24, "table_x"));
        SQLRouteResult routeResult = new SQLRouteResult(new TransparentOptimizedStatement(showTablesStatement));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM table_x FROM 'sharding_db'");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        Map<String, String> logicAndActualTableMap = new LinkedHashMap<>();
        logicAndActualTableMap.put("table_x", "table_x");
        assertThat(rewriteEngine.generateSQL(null, logicAndActualTableMap).getSql(), is("SHOW COLUMNS FROM table_x"));
    }
    
    @Test
    public void assertTableTokenWithBackQuoteForShow() {
        DALStatement showTablesStatement = new DALStatement();
    
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 26, "`table_x`"));
        SQLRouteResult routeResult = new SQLRouteResult(new TransparentOptimizedStatement(showTablesStatement));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM `table_x`");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM `table_1`"));
    }
    
    @Test
    public void assertTableTokenWithBackQuoteFromSchemaForShow() {
        DALStatement showTablesStatement = new DALStatement();
    
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 26, "`table_x`"));
        SQLRouteResult routeResult = new SQLRouteResult(new TransparentOptimizedStatement(showTablesStatement));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM `table_x` FROM 'sharding_db'");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM `table_1` FROM 'sharding_db'"));
    }
    
    @Test
    public void assertTableTokenWithSchemaForShow() {
        DALStatement showTablesStatement = new DALStatement();
    
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 36, "table_x"));
        SQLRouteResult routeResult = new SQLRouteResult(new TransparentOptimizedStatement(showTablesStatement));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM sharding_db.table_x");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM table_1"));
    }
    
    @Test
    public void assertTableTokenWithSchemaFromSchemaForShow() {
        DALStatement showTablesStatement = new DALStatement();
    
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 36, "table_x"));
        SQLRouteResult routeResult = new SQLRouteResult(new TransparentOptimizedStatement(showTablesStatement));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM sharding_db.table_x FROM sharding_db");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM table_1 FROM sharding_db"));
    }
    
    @Test
    public void assertTableTokenWithBackQuoteWithSchemaForShow() {
        DALStatement showTablesStatement = new DALStatement();
    
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 38, "`table_x`"));
        SQLRouteResult routeResult = new SQLRouteResult(new TransparentOptimizedStatement(showTablesStatement));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM sharding_db.`table_x`");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM `table_1`"));
    }
    
    @Test
    public void assertTableTokenWithBackQuoteWithSchemaFromSchemaForShow() {
        DALStatement showTablesStatement = new DALStatement();
    
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 38, "`table_x`"));
        SQLRouteResult routeResult = new SQLRouteResult(new TransparentOptimizedStatement(showTablesStatement));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM sharding_db.`table_x` FROM sharding_db");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM `table_1` FROM sharding_db"));
    }
    
    @Test
    public void assertTableTokenWithSchemaWithBackQuoteForShow() {
        DALStatement showTablesStatement = new DALStatement();
    
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 40, "`table_x`"));
        SQLRouteResult routeResult = new SQLRouteResult(new TransparentOptimizedStatement(showTablesStatement));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM `sharding_db`.`table_x`");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM `table_1`"));
    }
    
    @Test
    public void assertTableTokenWithSchemaWithBackQuoteFromSchemaForShow() {
        DALStatement showTablesStatement = new DALStatement();
    
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 40, "`table_x`"));
        SQLRouteResult routeResult = new SQLRouteResult(new TransparentOptimizedStatement(showTablesStatement));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM `sharding_db`.`table_x` FROM sharding_db");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM `table_1` FROM sharding_db"));
    }
    
    @Test
    public void assertTableTokenWithSchemaForSelect() {
        SelectStatement selectStatement = new SelectStatement();
    
        selectStatement.getTables().add(new Table("table_x", null));
        TableSegment tableSegment = new TableSegment(14, 32, "table_x");
        tableSegment.setOwner(new SchemaSegment(14, 24, "sharding_db"));
        selectStatement.getSQLSegments().add(tableSegment);
        SQLRouteResult routeResult = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), new AndCondition(), 
                selectStatement.getItems(), new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), 
                new Pagination(null, null, Collections.emptyList())));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        selectStatement.setLogicSQL("SELECT * FROM sharding_db.table_x");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT * FROM table_1"));
    }
    
    @Test
    public void assertTableTokenWithSchemaForInsert() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.getSQLSegments().add(new TableSegment(12, 30, "table_x"));
        ShardingInsertColumns insertColumns = mock(ShardingInsertColumns.class);
        when(insertColumns.getRegularColumnNames()).thenReturn(Arrays.asList("order_id", "user_id", "status", "id"));
        when(insertColumns.getAllColumnNames()).thenReturn(Arrays.asList("order_id", "user_id", "status", "id"));
        SQLRouteResult routeResult = new SQLRouteResult(new ShardingInsertOptimizedStatement(insertStatement, Collections.<ShardingCondition>emptyList(), insertColumns, null));
        routeResult.setRoutingResult(new RoutingResult());
        insertStatement.setLogicSQL("INSERT INTO sharding_db.table_x (order_id, user_id, status) values (1, 1, 'OK')");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(
                null, logicTableAndActualTables).getSql(), is("INSERT INTO table_1 (order_id, user_id, status) values (1, 1, 'OK')"));
    }
    
    @Test
    public void assertTableTokenWithSchemaForUpdate() {
        UpdateStatement updateStatement = new UpdateStatement();
        updateStatement.getSQLSegments().add(new TableSegment(7, 27, "table_x"));
        SQLRouteResult routeResult = new SQLRouteResult(new ShardingWhereOptimizedStatement(updateStatement, new ShardingConditions(Collections.<ShardingCondition>emptyList()), new AndCondition()));
        routeResult.setRoutingResult(new RoutingResult());
        updateStatement.setLogicSQL("UPDATE `sharding_db`.table_x SET user_id=1 WHERE order_id=1");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("UPDATE table_1 SET user_id=1 WHERE order_id=1"));
    }
    
    @Test
    public void assertTableTokenWithSchemaForDelete() {
        DeleteStatement deleteStatement = new DeleteStatement();
        deleteStatement.getSQLSegments().add(new TableSegment(12, 34, "`table_x`"));
        SQLRouteResult routeResult = new SQLRouteResult(new ShardingWhereOptimizedStatement(deleteStatement, new ShardingConditions(Collections.<ShardingCondition>emptyList()), new AndCondition()));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        deleteStatement.setLogicSQL("DELETE FROM `sharding_db`.`table_x` WHERE user_id=1");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("DELETE FROM `table_1` WHERE user_id=1"));
    }
    
    @Test
    public void assertSelectEqualWithShardingEncryptor() {
        SelectStatement selectStatement = new SelectStatement();
    
        List<Object> parameters = new ArrayList<>(2);
        parameters.add(1);
        parameters.add("x");
        Column column = new Column("id", "table_z");
        selectStatement.getTables().add(new Table("table_z", null));
        selectStatement.getSQLSegments().add(new TableSegment(15, 21, "table_z"));
        AndCondition encryptConditions = new AndCondition();
        encryptConditions.getConditions().add(new Condition(column, new PredicateSegment(29, 32, null, null), new ParameterMarkerExpressionSegment(0, 0, 0)));
        SQLRouteResult routeResult = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), encryptConditions, 
                selectStatement.getItems(), new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), 
                new Pagination(null, null, Collections.emptyList())));
        routeResult.setRoutingResult(new RoutingResult());
        selectStatement.setLogicSQL("SELECT id FROM table_z WHERE id=? AND name=?");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(parameters);
        assertThat(rewriteEngine.generateSQL().getSql(), is("SELECT id FROM table_z WHERE id = ? AND name=?"));
        assertThat(getParameterBuilder(rewriteEngine).getParameters().get(0), is((Object) "encryptValue"));
    }
    
    @Test
    public void assertSelectInWithShardingEncryptor() {
        SelectStatement selectStatement = new SelectStatement();
    
        selectStatement.getTables().add(new Table("table_z", null));
        selectStatement.getSQLSegments().add(new TableSegment(15, 21, "table_z"));
        List<ExpressionSegment> expressionSegments = new LinkedList<>();
        expressionSegments.add(new LiteralExpressionSegment(0, 0, 3));
        expressionSegments.add(new LiteralExpressionSegment(0, 0, 5));
        Column column = new Column("id", "table_z");
        AndCondition encryptConditions = new AndCondition();
        encryptConditions.getConditions().add(new Condition(column, new PredicateSegment(29, 39, null, null), expressionSegments));
        SQLRouteResult routeResult = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), encryptConditions, 
                selectStatement.getItems(), new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), 
                new Pagination(null, null, Collections.emptyList())));
        routeResult.setRoutingResult(new RoutingResult());
        selectStatement.setLogicSQL("SELECT id FROM table_z WHERE id in (3,5)");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT id FROM table_z WHERE id IN ('encryptValue', 'encryptValue')"));
    }
    
    @Test
    public void assertSelectInWithShardingEncryptorWithParameter() {
        SelectStatement selectStatement = new SelectStatement();
    
        List<Object> parameters = new ArrayList<>(2);
        parameters.add(1);
        parameters.add(2);
        selectStatement.getTables().add(new Table("table_z", null));
        selectStatement.getSQLSegments().add(new TableSegment(15, 21, "table_z"));
        List<ExpressionSegment> expressionSegments = new LinkedList<>();
        expressionSegments.add(new ParameterMarkerExpressionSegment(0, 0, 0));
        expressionSegments.add(new ParameterMarkerExpressionSegment(0, 0, 1));
        Column column = new Column("id", "table_z");
        AndCondition encryptConditions = new AndCondition();
        encryptConditions.getConditions().add(new Condition(column, new PredicateSegment(29, 40, null, null), expressionSegments));
        encryptConditions.getConditions().add(new Condition(column, new PredicateSegment(45, 50, null, null), new LiteralExpressionSegment(0, 0, 3)));
        SQLRouteResult routeResult = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), encryptConditions, 
                selectStatement.getItems(), new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), 
                new Pagination(null, null, Collections.emptyList())));
        routeResult.setRoutingResult(new RoutingResult());
        selectStatement.setLogicSQL("SELECT id FROM table_z WHERE id in (?, ?) or id = 3");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(parameters);
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT id FROM table_z WHERE id IN (?, ?) or id = 'encryptValue'"));
        assertThat(getParameterBuilder(rewriteEngine).getParameters().get(0), is((Object) "encryptValue"));
        assertThat(getParameterBuilder(rewriteEngine).getParameters().get(1), is((Object) "encryptValue"));
    }
    
    @Test
    public void assertSelectEqualWithQueryAssistedShardingEncryptor() {
        List<Object> parameters = new ArrayList<>(2);
        parameters.add(1);
        parameters.add("k");
        SelectStatement selectStatement = new SelectStatement();
    
        selectStatement.getTables().add(new Table("table_k", null));
        Column column = new Column("id", "table_k");
        selectStatement.getSQLSegments().add(new TableSegment(15, 21, "table_k"));
        AndCondition encryptConditions = new AndCondition();
        encryptConditions.getConditions().add(new Condition(column, new PredicateSegment(29, 32, null, null), new ParameterMarkerExpressionSegment(0, 0, 0)));
        SQLRouteResult routeResult = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), encryptConditions, 
                selectStatement.getItems(), new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), 
                new Pagination(null, null, Collections.emptyList())));
        routeResult.setRoutingResult(new RoutingResult());
        selectStatement.setLogicSQL("SELECT id FROM table_k WHERE id=? AND name=?");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(parameters);
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT id FROM table_k WHERE query_id = ? AND name=?"));
        assertThat(getParameterBuilder(rewriteEngine).getParameters().get(0), is((Object) "assistedEncryptValue"));
    }
    
    @Test
    public void assertSelectInWithQueryAssistedShardingEncryptor() {
        SelectStatement selectStatement = new SelectStatement();
    
        selectStatement.getTables().add(new Table("table_k", null));
        selectStatement.getSQLSegments().add(new TableSegment(15, 21, "table_k"));
        List<ExpressionSegment> expressionSegments = new LinkedList<>();
        expressionSegments.add(new LiteralExpressionSegment(0, 0, 3));
        expressionSegments.add(new LiteralExpressionSegment(0, 0, 5));
        Column column = new Column("id", "table_k");
        AndCondition encryptConditions = new AndCondition();
        encryptConditions.getConditions().add(new Condition(column, new PredicateSegment(29, 39, null, null), expressionSegments));
        SQLRouteResult routeResult = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), encryptConditions, 
                selectStatement.getItems(), new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), 
                new Pagination(null, null, Collections.emptyList())));
        routeResult.setRoutingResult(new RoutingResult());
        selectStatement.setLogicSQL("SELECT id FROM table_k WHERE id in (3,5)");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), 
                is("SELECT id FROM table_k WHERE query_id IN ('assistedEncryptValue', 'assistedEncryptValue')"));
    }
    
    @Test
    public void assertUpdateWithShardingEncryptor() {
        UpdateStatement updateStatement = new UpdateStatement();
        updateStatement.getTables().add(new Table("table_z", ""));
        updateStatement.getSQLSegments().add(new TableSegment(7, 13, "table_z"));
        SetAssignmentsSegment setAssignmentsSegment = new SetAssignmentsSegment(15, 24, Collections.singleton(new AssignmentSegment(19, 24, new ColumnSegment(19, 20, "id"), null)));
        updateStatement.getSQLSegments().add(setAssignmentsSegment);
        Column column = new Column("id", "table_z");
        updateStatement.getAssignments().put(column, new LiteralExpressionSegment(0, 0, 1));
        AndCondition encryptConditions = new AndCondition();
        encryptConditions.getConditions().add(new Condition(column, new PredicateSegment(32, 37, null, null), new LiteralExpressionSegment(0, 0, 2)));
        SQLRouteResult routeResult = new SQLRouteResult(new ShardingWhereOptimizedStatement(updateStatement, new ShardingConditions(Collections.<ShardingCondition>emptyList()), encryptConditions));
        routeResult.setRoutingResult(new RoutingResult());
        updateStatement.setLogicSQL("UPDATE table_z SET id = 1 WHERE id = 2");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("UPDATE table_z SET id = 'encryptValue' WHERE id = 'encryptValue'"));
    }
    
    @Test
    public void assertInsertWithQueryAssistedShardingEncryptor() {
        InsertStatement insertStatement = new InsertStatement();
    
        insertStatement.getColumnNames().add("name");
        ColumnSegment columnSegment = new ColumnSegment(26, 29, "name");
        LiteralExpressionSegment expressionSegment = new LiteralExpressionSegment(33, 34, 10);
        insertStatement.getSQLSegments().add(new SetAssignmentsSegment(22, 34, Collections.singleton(new AssignmentSegment(22, 34, columnSegment, expressionSegment))));
        insertStatement.getSQLSegments().add(new TableSegment(12, 20, "`table_w`"));
        insertStatement.getTables().add(new Table("table_w", null));
        ShardingInsertColumns insertColumns = mock(ShardingInsertColumns.class);
        when(insertColumns.getRegularColumnNames()).thenReturn(Collections.singletonList("name"));
        when(insertColumns.getAllColumnNames()).thenReturn(Arrays.asList("name", "id", "query_name"));
        ShardingInsertOptimizedStatement optimizedStatement = 
                new ShardingInsertOptimizedStatement(insertStatement, Collections.<ShardingCondition>emptyList(), insertColumns, null);
        ExpressionSegment[] expressionSegments = {new LiteralExpressionSegment(0, 0, 10), new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, 10)};
        optimizedStatement.addUnit(expressionSegments, new Object[0], 0);
        optimizedStatement.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        RoutingUnit routingUnit = new RoutingUnit("db0");
        routingUnit.getTableUnits().add(new TableUnit("table_w", "table_1"));
        SQLRouteResult routeResult = new SQLRouteResult(optimizedStatement);
        routeResult.setRoutingResult(new RoutingResult());
        insertStatement.setLogicSQL("INSERT INTO `table_w` set name = 10 ON DUPLICATE KEY UPDATE name = VALUES(name)");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(routingUnit, logicTableAndActualTables).getSql(), 
                is("INSERT INTO `table_w` set name = 'encryptValue', id = 1, query_name = 'assistedEncryptValue' ON DUPLICATE KEY UPDATE name = VALUES(name)"));
    }
    
    @Test
    public void assertSelectInWithAggregationDistinct() {
        SelectStatement selectStatement = new SelectStatement();
    
        selectStatement.getSQLSegments().add(new TableSegment(49, 55, "table_z"));
        AggregationDistinctSelectItemSegment selectItemSegment1 = new AggregationDistinctSelectItemSegment(7, 24, "DISTINCT id", AggregationType.COUNT, 12, "id");
        selectItemSegment1.setAlias("a");
        AggregationDistinctSelectItemSegment selectItemSegment2 = new AggregationDistinctSelectItemSegment(27, 42, "DISTINCT id", AggregationType.SUM, 30, "id");
        selectItemSegment2.setAlias("b");
        SelectItemsSegment selectItemsSegment = new SelectItemsSegment(7, 42, true);
        selectItemsSegment.getSelectItems().add(selectItemSegment1);
        selectItemsSegment.getSelectItems().add(selectItemSegment2);
        selectStatement.getSQLSegments().add(selectItemsSegment);
        SQLRouteResult routeResult = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), new AndCondition(), 
                selectStatement.getItems(), new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), 
                new Pagination(null, null, Collections.emptyList())));
        routeResult.setRoutingResult(new RoutingResult());
        selectStatement.setLogicSQL("SELECT COUNT(DISTINCT id), SUM(DISTINCT id) FROM table_z WHERE id in (3,5)");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT DISTINCT id, id FROM table_z WHERE id in (3,5)"));
    }
    
    private SQLRewriteEngine createSQLRewriteEngine(final SQLRouteResult routeResult, final List<Object> parameters) {
        return new SQLRewriteEngine(shardingRule, routeResult, parameters, routeResult.getRoutingResult().isSingleRouting());
    }
    
    @SneakyThrows
    private BaseParameterBuilder getParameterBuilder(final SQLRewriteEngine rewriteEngine) {
        Field field = rewriteEngine.getClass().getDeclaredField("parameterBuilder");
        field.setAccessible(true);
        return (BaseParameterBuilder) field.get(rewriteEngine);
    }
}
