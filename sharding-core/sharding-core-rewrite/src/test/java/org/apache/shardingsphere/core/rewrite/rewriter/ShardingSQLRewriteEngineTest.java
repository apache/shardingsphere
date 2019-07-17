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

import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.constant.AggregationType;
import org.apache.shardingsphere.core.constant.OrderDirection;
import org.apache.shardingsphere.core.optimize.statement.InsertValue;
import org.apache.shardingsphere.core.optimize.statement.OptimizedStatement;
import org.apache.shardingsphere.core.optimize.statement.encrypt.condition.EncryptCondition;
import org.apache.shardingsphere.core.optimize.statement.encrypt.condition.EncryptConditions;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.ShardingWhereOptimizedStatement;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.condition.ShardingConditions;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.insert.ShardingInsertColumns;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.insert.ShardingInsertOptimizedStatement;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.groupby.GroupBy;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.item.AggregationSelectItem;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.item.DerivedCommonSelectItem;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.item.SelectItem;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.item.SelectItems;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.orderby.OrderBy;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.orderby.OrderByItem;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.pagination.Pagination;
import org.apache.shardingsphere.core.optimize.statement.transparent.TransparentOptimizedStatement;
import org.apache.shardingsphere.core.parse.constant.QuoteCharacter;
import org.apache.shardingsphere.core.parse.sql.context.Table;
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
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dal.DALStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.rewrite.SQLRewriteEngine;
import org.apache.shardingsphere.core.rewrite.builder.BaseParameterBuilder;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingSQLRewriteEngineTest {
    
    private ShardingRule shardingRule;
    
    private RoutingUnit routingUnit;
    
    private Map<String, String> logicTableAndActualTables = Collections.singletonMap("table_x", "table_1");
    
    @Before
    public void setUp() throws IOException {
        shardingRule = createShardingRule();
        routingUnit = createRoutingUnit();
    }
    
    private ShardingRule createShardingRule() throws IOException {
        URL url = ShardingSQLRewriteEngineTest.class.getClassLoader().getResource("yaml/rewrite-rule.yaml");
        Preconditions.checkNotNull(url, "Cannot found rewrite rule yaml configuration.");
        YamlRootShardingConfiguration yamlShardingConfig = YamlEngine.unmarshal(new File(url.getFile()), YamlRootShardingConfiguration.class);
        return new ShardingRule(new ShardingRuleConfigurationYamlSwapper().swap(yamlShardingConfig.getShardingRule()), yamlShardingConfig.getDataSources().keySet());
    }
    
    private RoutingUnit createRoutingUnit() {
        RoutingUnit result = new RoutingUnit("db0");
        result.getTableUnits().add(new TableUnit("table_x", "table_1"));
        result.getTableUnits().add(new TableUnit("table_w", "table_1"));
        return result;
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
        SQLRouteResult result = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                new SelectItems(Collections.<SelectItem>emptyList(), false, 0), new Pagination(null, null, Collections.emptyList())));
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
        SQLRouteResult result = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                new SelectItems(Collections.<SelectItem>emptyList(), false, 0), new Pagination(null, null, Collections.emptyList())));
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
        DerivedCommonSelectItem selectItem1 = new DerivedCommonSelectItem("x.id", "GROUP_BY_DERIVED_0");
        DerivedCommonSelectItem selectItem2 = new DerivedCommonSelectItem("x.name", "ORDER_BY_DERIVED_0");
        SelectItems selectItems = new SelectItems(Arrays.<SelectItem>asList(selectItem1, selectItem2), false, 11);
        SQLRouteResult result = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                selectItems, new Pagination(null, null, Collections.emptyList())));
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
        AggregationSelectItem countSelectItem = new AggregationSelectItem(AggregationType.COUNT, "(x.age)", "AVG_DERIVED_COUNT_0");
        AggregationSelectItem sumSelectItem = new AggregationSelectItem(AggregationType.SUM, "(x.age)", "AVG_DERIVED_SUM_0");
        AggregationSelectItem avgSelectItem = new AggregationSelectItem(AggregationType.AVG, "(x.age)", null);
        avgSelectItem.getDerivedAggregationItems().add(countSelectItem);
        avgSelectItem.getDerivedAggregationItems().add(sumSelectItem);
        SelectItems selectItems = new SelectItems(Collections.<SelectItem>singletonList(avgSelectItem), false, 16);
        SQLRouteResult result = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                selectItems, new Pagination(null, null, Collections.emptyList())));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteAutoGeneratedKeyColumn() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForAutoGeneratedKeyColumn(), Arrays.<Object>asList("Bill", 1));
        assertThat(rewriteEngine.generateSQL(routingUnit, logicTableAndActualTables).getSql(), is("INSERT INTO table_1 (name, age, id) VALUES (?, ?, ?)"));
    }
    
    private SQLRouteResult createRouteResultForAutoGeneratedKeyColumn() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setLogicSQL("INSERT INTO table_x (name, age) VALUES (?, ?)");
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.getColumns().add(new ColumnSegment(0, 0, "name"));
        insertStatement.getColumns().add(new ColumnSegment(0, 0, "age"));
        insertStatement.getSQLSegments().add(new InsertColumnsSegment(30, 30, Collections.singleton(mock(ColumnSegment.class))));
        insertStatement.getSQLSegments().add(new InsertValuesSegment(39, 44, Collections.<ExpressionSegment>emptyList()));
        insertStatement.getSQLSegments().add(new TableSegment(12, 18, "table_x"));
        ShardingInsertColumns insertColumns = mock(ShardingInsertColumns.class);
        when(insertColumns.getRegularColumnNames()).thenReturn(Arrays.asList("name", "age"));
        when(insertColumns.getAllColumnNames()).thenReturn(Arrays.asList("name", "age", "id"));
        ShardingInsertOptimizedStatement optimizedStatement = new ShardingInsertOptimizedStatement(
                insertStatement, Collections.<ShardingCondition>emptyList(), insertColumns, Collections.singletonList(new InsertValue(Collections.<ExpressionSegment>emptyList())), null);
        ExpressionSegment[] expressionSegments = {new ParameterMarkerExpressionSegment(0, 0, 0), new ParameterMarkerExpressionSegment(0, 0, 1), new ParameterMarkerExpressionSegment(0, 0, 2)};
        optimizedStatement.addUnit(expressionSegments, new Object[] {"x", 1, 1}, 3);
        optimizedStatement.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteAutoGeneratedKeyColumnWithoutColumnsWithParameter() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForAutoGeneratedKeyColumnWithoutColumnsWithParameter(), Arrays.<Object>asList("Bill", 1));
        assertThat(rewriteEngine.generateSQL(routingUnit, logicTableAndActualTables).getSql(), is("INSERT INTO `table_1`(name, id) VALUES (?, ?)"));
    }
    
    private SQLRouteResult createRouteResultForAutoGeneratedKeyColumnWithoutColumnsWithParameter() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setLogicSQL("INSERT INTO `table_x` VALUES (?)");
        insertStatement.getColumns().add(new ColumnSegment(0, 0, "name"));
        insertStatement.getSQLSegments().add(new InsertColumnsSegment(21, 21, Collections.<ColumnSegment>emptyList()));
        insertStatement.getSQLSegments().add(new InsertValuesSegment(29, 31, Collections.<ExpressionSegment>emptyList()));
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.getSQLSegments().add(new TableSegment(12, 20, "`table_x`"));
        ShardingInsertColumns insertColumns = mock(ShardingInsertColumns.class);
        when(insertColumns.getRegularColumnNames()).thenReturn(Collections.singletonList("name"));
        when(insertColumns.getAllColumnNames()).thenReturn(Arrays.asList("name", "id"));
        ShardingInsertOptimizedStatement optimizedStatement = new ShardingInsertOptimizedStatement(
                insertStatement, Collections.<ShardingCondition>emptyList(), insertColumns, Collections.singletonList(new InsertValue(Collections.<ExpressionSegment>emptyList())), null);
        ExpressionSegment[] expressionSegments = {new ParameterMarkerExpressionSegment(0, 0, 0), new ParameterMarkerExpressionSegment(0, 0, 1)};
        optimizedStatement.addUnit(expressionSegments, new Object[] {"Bill", 1}, 2);
        optimizedStatement.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteAutoGeneratedKeyColumnWithoutColumnsWithoutParameter() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForAutoGeneratedKeyColumnWithoutColumnsWithoutParameter(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(routingUnit, logicTableAndActualTables).getSql(), is("INSERT INTO `table_1`(name, id) VALUES (10, 1)"));
    }
    
    private SQLRouteResult createRouteResultForAutoGeneratedKeyColumnWithoutColumnsWithoutParameter() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setLogicSQL("INSERT INTO `table_x` VALUES (10)");
        insertStatement.getColumns().add(new ColumnSegment(0, 0, "name"));
        insertStatement.getSQLSegments().add(new InsertColumnsSegment(21, 21, Collections.<ColumnSegment>emptyList()));
        insertStatement.getSQLSegments().add(new InsertValuesSegment(29, 32, Collections.<ExpressionSegment>emptyList()));
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.getSQLSegments().add(new TableSegment(12, 20, "`table_x`"));
        ShardingInsertColumns insertColumns = mock(ShardingInsertColumns.class);
        when(insertColumns.getRegularColumnNames()).thenReturn(Collections.singletonList("name"));
        when(insertColumns.getAllColumnNames()).thenReturn(Arrays.asList("name", "id"));
        ShardingInsertOptimizedStatement optimizedStatement = new ShardingInsertOptimizedStatement(
                insertStatement, Collections.<ShardingCondition>emptyList(), insertColumns, Collections.singletonList(new InsertValue(Collections.<ExpressionSegment>emptyList())), null);
        ExpressionSegment[] expressionSegments = {new LiteralExpressionSegment(0, 0, 10), new LiteralExpressionSegment(0, 0, 1)};
        optimizedStatement.addUnit(expressionSegments, new Object[0], 0);
        optimizedStatement.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteDuplicateKeyWithoutColumnsWithoutParameter() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForDuplicateKeyWithoutColumnsWithoutParameter(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(routingUnit, logicTableAndActualTables).getSql(), is("INSERT INTO `table_1`(name, id) VALUES (10, 1) ON DUPLICATE KEY UPDATE name = VALUES(name)"));
    }
    
    private SQLRouteResult createRouteResultForDuplicateKeyWithoutColumnsWithoutParameter() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setLogicSQL("INSERT INTO `table_x` VALUES (10) ON DUPLICATE KEY UPDATE name = VALUES(name)");
        insertStatement.getColumns().add(new ColumnSegment(0, 0, "name"));
        insertStatement.getSQLSegments().add(new InsertColumnsSegment(21, 21, Collections.<ColumnSegment>emptyList()));
        insertStatement.getSQLSegments().add(new InsertValuesSegment(29, 32, Collections.<ExpressionSegment>emptyList()));
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.getSQLSegments().add(new TableSegment(12, 20, "`table_x`"));
        ShardingInsertColumns insertColumns = mock(ShardingInsertColumns.class);
        when(insertColumns.getRegularColumnNames()).thenReturn(Collections.singletonList("name"));
        when(insertColumns.getAllColumnNames()).thenReturn(Arrays.asList("name", "id"));
        ShardingInsertOptimizedStatement optimizedStatement = new ShardingInsertOptimizedStatement(
                insertStatement, Collections.<ShardingCondition>emptyList(), insertColumns, Collections.singletonList(new InsertValue(Collections.<ExpressionSegment>emptyList())), null);
        ExpressionSegment[] expressionSegments = {new LiteralExpressionSegment(0, 0, 10), new LiteralExpressionSegment(0, 0, 1)};
        optimizedStatement.addUnit(expressionSegments, new Object[0], 0);
        optimizedStatement.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteDuplicateKeyWithSetWithoutParameter() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForDuplicateKeyWithSetWithoutParameter(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(routingUnit, logicTableAndActualTables).getSql(), is("INSERT INTO `table_1` set name = 10, id = 1 ON DUPLICATE KEY UPDATE name = VALUES(name)"));
    }
    
    private SQLRouteResult createRouteResultForDuplicateKeyWithSetWithoutParameter() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setLogicSQL("INSERT INTO `table_x` set name = 10, id = 1 ON DUPLICATE KEY UPDATE name = VALUES(name)");
        insertStatement.getColumns().add(new ColumnSegment(0, 0, "name"));
        insertStatement.getColumns().add(new ColumnSegment(0, 0, "id"));
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.getSQLSegments().add(new TableSegment(12, 20, "`table_x`"));
        ShardingInsertColumns insertColumns = mock(ShardingInsertColumns.class);
        when(insertColumns.getRegularColumnNames()).thenReturn(Arrays.asList("name", "id"));
        when(insertColumns.getAllColumnNames()).thenReturn(Arrays.asList("name", "id"));
        ShardingInsertOptimizedStatement optimizedStatement = new ShardingInsertOptimizedStatement(
                insertStatement, Collections.<ShardingCondition>emptyList(), insertColumns, Collections.singletonList(new InsertValue(Collections.<ExpressionSegment>emptyList())), null);
        ExpressionSegment[] expressionSegments = {new LiteralExpressionSegment(0, 0, 10), new LiteralExpressionSegment(0, 0, 1)};
        optimizedStatement.addUnit(expressionSegments, new Object[0], 0);
        optimizedStatement.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteColumnWithoutColumnsWithoutParameter() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForColumnWithoutColumnsWithoutParameter(), Arrays.<Object>asList("x", 1));
        assertThat(rewriteEngine.generateSQL(routingUnit, logicTableAndActualTables).getSql(), is("INSERT INTO `table_1`(name, id) VALUES (10, 1)"));
    }
    
    private SQLRouteResult createRouteResultForColumnWithoutColumnsWithoutParameter() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setLogicSQL("INSERT INTO `table_x` VALUES (10, 1)");
        insertStatement.getColumns().add(new ColumnSegment(0, 0, "name"));
        insertStatement.getColumns().add(new ColumnSegment(0, 0, "id"));
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.getSQLSegments().add(new TableSegment(12, 20, "`table_x`"));
        insertStatement.getSQLSegments().add(new InsertColumnsSegment(21, 21, Collections.<ColumnSegment>emptyList()));
        insertStatement.getSQLSegments().add(new InsertValuesSegment(29, 35, Collections.<ExpressionSegment>emptyList()));
        ShardingInsertColumns insertColumns = mock(ShardingInsertColumns.class);
        when(insertColumns.getRegularColumnNames()).thenReturn(Arrays.asList("name", "id"));
        when(insertColumns.getAllColumnNames()).thenReturn(Arrays.asList("name", "id"));
        ShardingInsertOptimizedStatement optimizedStatement = new ShardingInsertOptimizedStatement(
                insertStatement, Collections.<ShardingCondition>emptyList(), insertColumns, Collections.singletonList(new InsertValue(Collections.<ExpressionSegment>emptyList())), null);
        ExpressionSegment[] expressionSegments = {new LiteralExpressionSegment(0, 0, 10), new LiteralExpressionSegment(0, 0, 1)};
        optimizedStatement.addUnit(expressionSegments, new Object[0], 0);
        optimizedStatement.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteColumnWithoutColumnsWithParameter() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForColumnWithoutColumnsWithParameter(), Arrays.<Object>asList("x", 1));
        assertThat(rewriteEngine.generateSQL(routingUnit, logicTableAndActualTables).getSql(), is("INSERT INTO `table_1`(name, id) VALUES (?, ?)"));
    }
    
    private SQLRouteResult createRouteResultForColumnWithoutColumnsWithParameter() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setLogicSQL("INSERT INTO `table_x` VALUES (?, ?)");
        insertStatement.getColumns().add(new ColumnSegment(0, 0, "name"));
        insertStatement.getSQLSegments().add(new InsertColumnsSegment(21, 21, Collections.<ColumnSegment>emptyList()));
        insertStatement.getSQLSegments().add(new InsertValuesSegment(29, 34, Collections.<ExpressionSegment>emptyList()));
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.getSQLSegments().add(new TableSegment(12, 20, "`table_x`"));
        ShardingInsertColumns insertColumns = mock(ShardingInsertColumns.class);
        when(insertColumns.getRegularColumnNames()).thenReturn(Collections.singletonList("name"));
        when(insertColumns.getAllColumnNames()).thenReturn(Arrays.asList("name", "id"));
        ShardingInsertOptimizedStatement optimizedStatement = new ShardingInsertOptimizedStatement(
                insertStatement, Collections.<ShardingCondition>emptyList(), insertColumns, Collections.singletonList(new InsertValue(Collections.<ExpressionSegment>emptyList())), null);
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
        OptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), new SelectItems(Collections.<SelectItem>emptyList(), false, 0), 
                new Pagination(new NumberLiteralLimitValueSegment(33, 33, 2), new NumberLiteralLimitValueSegment(36, 36, 2), Collections.emptyList()));
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
        OptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), new SelectItems(Collections.<SelectItem>emptyList(), false, 0), 
                new Pagination(new NumberLiteralRowNumberValueSegment(119, 119, 2, true), new NumberLiteralRowNumberValueSegment(98, 98, 4, false), Collections.emptyList()));
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
        OptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), new SelectItems(Collections.<SelectItem>emptyList(), false, 0), 
                new Pagination(new NumberLiteralRowNumberValueSegment(123, 123, 2, true), new NumberLiteralRowNumberValueSegment(26, 26, 4, false), Collections.emptyList()));
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
        OptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(
                selectStatement, Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.singletonList(new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment, OrderDirection.DESC, OrderDirection.ASC))), 0),
                new OrderBy(Collections.singletonList(new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment, OrderDirection.ASC, OrderDirection.ASC))), false),
                new SelectItems(Collections.<SelectItem>emptyList(), false, 0), 
                new Pagination(new NumberLiteralLimitValueSegment(33, 33, 2), new NumberLiteralLimitValueSegment(36, 36, 2), Collections.emptyList()));
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
        OptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(
                selectStatement, Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.singletonList(new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment, OrderDirection.DESC, OrderDirection.ASC))), 0),
                new OrderBy(Collections.singletonList(new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment, OrderDirection.ASC, OrderDirection.ASC))), false), 
                new SelectItems(Collections.<SelectItem>emptyList(), false, 0), 
                new Pagination(new NumberLiteralRowNumberValueSegment(119, 119, 2, true), new NumberLiteralRowNumberValueSegment(98, 98, 4, false), Collections.emptyList()));
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
        OptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(
                selectStatement, Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.singletonList(new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment, OrderDirection.DESC, OrderDirection.ASC))), 0),
                new OrderBy(Collections.singletonList(new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment, OrderDirection.ASC, OrderDirection.ASC))), false), 
                new SelectItems(Collections.<SelectItem>emptyList(), false, 0), 
                new Pagination(new NumberLiteralRowNumberValueSegment(123, 123, 2, false), new NumberLiteralRowNumberValueSegment(26, 26, 4, false), Collections.emptyList()));
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
        OptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), new SelectItems(Collections.<SelectItem>emptyList(), false, 0), 
                new Pagination(new NumberLiteralLimitValueSegment(33, 33, 2), new NumberLiteralLimitValueSegment(36, 36, 2), Collections.emptyList()));
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
        OptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), new SelectItems(Collections.<SelectItem>emptyList(), false, 0), 
                new Pagination(new NumberLiteralRowNumberValueSegment(119, 119, 2, true), new NumberLiteralRowNumberValueSegment(98, 98, 4, false), Collections.emptyList()));
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
        OptimizedStatement optimizedStatement = new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false), new SelectItems(Collections.<SelectItem>emptyList(), false, 0), 
                new Pagination(new NumberLiteralRowNumberValueSegment(123, 123, 2, true), new NumberLiteralRowNumberValueSegment(26, 26, 4, false), Collections.emptyList()));
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
                selectStatement, Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Arrays.asList(
                        new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment1, OrderDirection.ASC, OrderDirection.ASC)),
                        new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment2, OrderDirection.DESC, OrderDirection.ASC))), 60),
                new OrderBy(Arrays.asList(
                        new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment1, OrderDirection.ASC, OrderDirection.ASC)),
                        new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment2, OrderDirection.DESC, OrderDirection.ASC))), true),
                new SelectItems(Collections.<SelectItem>emptyList(), false, 0), new Pagination(null, null, Collections.emptyList()));
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
        SQLRouteResult result = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                new SelectItems(Collections.<SelectItem>emptyList(), false, 0), new Pagination(null, null, Collections.emptyList())));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteIndexTokenForIndexNameTableNameWithoutLogicTableName() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForIndexTokenForIndexNameTableNameWithoutLogicTableName(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("CREATE INDEX logic_index_table_1 ON table_1 ('column')"));
    }
    
    private SQLRouteResult createRouteResultForIndexTokenForIndexNameTableNameWithoutLogicTableName() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setLogicSQL("CREATE INDEX logic_index ON table_x ('column')");
        selectStatement.getTables().add(new Table("table_x", null));
        selectStatement.getSQLSegments().add(new IndexSegment(13, 23, "logic_index", QuoteCharacter.NONE));
        selectStatement.getSQLSegments().add(new TableSegment(28, 34, "table_x"));
        SQLRouteResult result = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                new SelectItems(Collections.<SelectItem>emptyList(), false, 0), new Pagination(null, null, Collections.emptyList())));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithoutBackQuoteForShow() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForTableTokenWithoutBackQuoteForShow(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM table_1"));
    }
    
    private SQLRouteResult createRouteResultForTableTokenWithoutBackQuoteForShow() {
        DALStatement showTablesStatement = new DALStatement();
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 24, "table_x"));
        SQLRouteResult result = new SQLRouteResult(new TransparentOptimizedStatement(showTablesStatement));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM table_x");
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithoutBackQuoteFromSchemaForShow() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForTableTokenWithoutBackQuoteFromSchemaForShow(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, Collections.singletonMap("table_x", "table_x")).getSql(), is("SHOW COLUMNS FROM table_x"));
    }
    
    private SQLRouteResult createRouteResultForTableTokenWithoutBackQuoteFromSchemaForShow() {
        DALStatement showTablesStatement = new DALStatement();
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM table_x FROM 'sharding_db'");
        showTablesStatement.getSQLSegments().add(new FromSchemaSegment(25, 43));
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 24, "table_x"));
        SQLRouteResult result = new SQLRouteResult(new TransparentOptimizedStatement(showTablesStatement));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithBackQuoteForShow() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForTableTokenWithBackQuoteForShow(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM `table_1`"));
    }
    
    private SQLRouteResult createRouteResultForTableTokenWithBackQuoteForShow() {
        DALStatement showTablesStatement = new DALStatement();
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM `table_x`");
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 26, "`table_x`"));
        SQLRouteResult result = new SQLRouteResult(new TransparentOptimizedStatement(showTablesStatement));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithBackQuoteFromSchemaForShow() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForTableTokenWithBackQuoteFromSchemaForShow(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM `table_1` FROM 'sharding_db'"));
    }
    
    private SQLRouteResult createSQLRouteResultForTableTokenWithBackQuoteFromSchemaForShow() {
        DALStatement showTablesStatement = new DALStatement();
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM `table_x` FROM 'sharding_db'");
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 26, "`table_x`"));
        SQLRouteResult result = new SQLRouteResult(new TransparentOptimizedStatement(showTablesStatement));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithSchemaForShow() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForTableTokenWithSchemaForShow(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM table_1"));
    }
    
    private SQLRouteResult createSQLRouteResultForTableTokenWithSchemaForShow() {
        DALStatement showTablesStatement = new DALStatement();
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM sharding_db.table_x");
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 36, "table_x"));
        SQLRouteResult result = new SQLRouteResult(new TransparentOptimizedStatement(showTablesStatement));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithSchemaFromSchemaForShow() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForTableTokenWithSchemaFromSchemaForShow(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM table_1 FROM sharding_db"));
    }
    
    private SQLRouteResult createSQLRouteResultForTableTokenWithSchemaFromSchemaForShow() {
        DALStatement showTablesStatement = new DALStatement();
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM sharding_db.table_x FROM sharding_db");
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 36, "table_x"));
        SQLRouteResult result = new SQLRouteResult(new TransparentOptimizedStatement(showTablesStatement));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithBackQuoteWithSchemaForShow() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForTableTokenWithBackQuoteWithSchemaForShow(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM `table_1`"));
    }
    
    private SQLRouteResult createSQLRouteResultForTableTokenWithBackQuoteWithSchemaForShow() {
        DALStatement showTablesStatement = new DALStatement();
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM sharding_db.`table_x`");
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 38, "`table_x`"));
        SQLRouteResult result = new SQLRouteResult(new TransparentOptimizedStatement(showTablesStatement));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithBackQuoteWithSchemaFromSchemaForShow() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForTableTokenWithBackQuoteWithSchemaFromSchemaForShow(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM `table_1` FROM sharding_db"));
    }
    
    private SQLRouteResult createSQLRouteResultForTableTokenWithBackQuoteWithSchemaFromSchemaForShow() {
        DALStatement showTablesStatement = new DALStatement();
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM sharding_db.`table_x` FROM sharding_db");
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 38, "`table_x`"));
        SQLRouteResult result = new SQLRouteResult(new TransparentOptimizedStatement(showTablesStatement));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithSchemaWithBackQuoteForShow() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForTableTokenWithSchemaWithBackQuoteForShow(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM `table_1`"));
    }
    
    private SQLRouteResult createSQLRouteResultForTableTokenWithSchemaWithBackQuoteForShow() {
        DALStatement showTablesStatement = new DALStatement();
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM `sharding_db`.`table_x`");
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 40, "`table_x`"));
        SQLRouteResult result = new SQLRouteResult(new TransparentOptimizedStatement(showTablesStatement));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithSchemaWithBackQuoteFromSchemaForShow() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForTableTokenWithSchemaWithBackQuoteFromSchemaForShow(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM `table_1` FROM sharding_db"));
    }
    
    private SQLRouteResult createSQLRouteResultForTableTokenWithSchemaWithBackQuoteFromSchemaForShow() {
        DALStatement showTablesStatement = new DALStatement();
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM `sharding_db`.`table_x` FROM sharding_db");
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 40, "`table_x`"));
        SQLRouteResult result = new SQLRouteResult(new TransparentOptimizedStatement(showTablesStatement));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithSchemaForSelect() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForTableTokenWithSchemaForSelect(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT * FROM table_1"));
    }
    
    private SQLRouteResult createSQLRouteResultForTableTokenWithSchemaForSelect() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setLogicSQL("SELECT * FROM sharding_db.table_x");
        selectStatement.getTables().add(new Table("table_x", null));
        TableSegment tableSegment = new TableSegment(14, 32, "table_x");
        tableSegment.setOwner(new SchemaSegment(14, 24, "sharding_db"));
        selectStatement.getSQLSegments().add(tableSegment);
        SQLRouteResult result = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                new SelectItems(Collections.<SelectItem>emptyList(), false, 0), new Pagination(null, null, Collections.emptyList())));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithSchemaForInsert() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForTableTokenWithSchemaForInsert(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(
                null, logicTableAndActualTables).getSql(), is("INSERT INTO table_1 (order_id, user_id, status) values (1, 1, 'OK')"));
    }
    
    private SQLRouteResult createSQLRouteResultForTableTokenWithSchemaForInsert() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setLogicSQL("INSERT INTO sharding_db.table_x (order_id, user_id, status) values (1, 1, 'OK')");
        insertStatement.getSQLSegments().add(new TableSegment(12, 30, "table_x"));
        ShardingInsertColumns insertColumns = mock(ShardingInsertColumns.class);
        when(insertColumns.getRegularColumnNames()).thenReturn(Arrays.asList("order_id", "user_id", "status", "id"));
        when(insertColumns.getAllColumnNames()).thenReturn(Arrays.asList("order_id", "user_id", "status", "id"));
        SQLRouteResult result = new SQLRouteResult(new ShardingInsertOptimizedStatement(
                insertStatement, Collections.<ShardingCondition>emptyList(), insertColumns, Collections.singletonList(new InsertValue(Collections.<ExpressionSegment>emptyList())), null));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithSchemaForUpdate() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForTableTokenWithSchemaForUpdate(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("UPDATE table_1 SET user_id=1 WHERE order_id=1"));
    }
    
    private SQLRouteResult createSQLRouteResultForTableTokenWithSchemaForUpdate() {
        UpdateStatement updateStatement = new UpdateStatement();
        updateStatement.setLogicSQL("UPDATE `sharding_db`.table_x SET user_id=1 WHERE order_id=1");
        updateStatement.getSQLSegments().add(new TableSegment(7, 27, "table_x"));
        updateStatement.getTables().add(new Table("table_x", null));
        updateStatement.setSetAssignment(
                new SetAssignmentsSegment(28, 42, Collections.singleton(new AssignmentSegment(33, 42, new ColumnSegment(33, 40, "id"), new LiteralExpressionSegment(41, 42, 1)))));
        SQLRouteResult result = new SQLRouteResult(new ShardingWhereOptimizedStatement(updateStatement, 
                new ShardingConditions(Collections.<ShardingCondition>emptyList()), new EncryptConditions(Collections.<EncryptCondition>emptyList())));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithSchemaForDelete() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForTableTokenWithSchemaForDelete(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("DELETE FROM `table_1` WHERE user_id=1"));
    }
    
    private SQLRouteResult createSQLRouteResultForTableTokenWithSchemaForDelete() {
        DeleteStatement deleteStatement = new DeleteStatement();
        deleteStatement.setLogicSQL("DELETE FROM `sharding_db`.`table_x` WHERE user_id=1");
        deleteStatement.getSQLSegments().add(new TableSegment(12, 34, "`table_x`"));
        SQLRouteResult result = new SQLRouteResult(new ShardingWhereOptimizedStatement(deleteStatement, 
                new ShardingConditions(Collections.<ShardingCondition>emptyList()), new EncryptConditions(Collections.<EncryptCondition>emptyList())));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteSelectInWithShardingEncryptor() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForSelectInWithShardingEncryptor(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT id FROM table_z WHERE id IN ('encryptValue', 'encryptValue')"));
    }
    
    private SQLRouteResult createSQLRouteResultForSelectInWithShardingEncryptor() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setLogicSQL("SELECT id FROM table_z WHERE id in (3,5)");
        selectStatement.getTables().add(new Table("table_z", null));
        selectStatement.getSQLSegments().add(new TableSegment(15, 21, "table_z"));
        List<ExpressionSegment> expressionSegments = new LinkedList<>();
        expressionSegments.add(new LiteralExpressionSegment(0, 0, 3));
        expressionSegments.add(new LiteralExpressionSegment(0, 0, 5));
        List<EncryptCondition> encryptConditions = new LinkedList<>();
        encryptConditions.add(new EncryptCondition("id", "table_z", 29, 39, expressionSegments));
        SQLRouteResult result = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), encryptConditions,
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                new SelectItems(Collections.<SelectItem>emptyList(), false, 0), new Pagination(null, null, Collections.emptyList())));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteSelectInWithQueryAssistedShardingEncryptor() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForSelectInWithQueryAssistedShardingEncryptor(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(),
                is("SELECT id FROM table_k WHERE query_id IN ('assistedEncryptValue', 'assistedEncryptValue')"));
    }
    
    private SQLRouteResult createSQLRouteResultForSelectInWithQueryAssistedShardingEncryptor() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setLogicSQL("SELECT id FROM table_k WHERE id in (3,5)");
        selectStatement.getTables().add(new Table("table_k", null));
        selectStatement.getSQLSegments().add(new TableSegment(15, 21, "table_k"));
        List<ExpressionSegment> expressionSegments = new LinkedList<>();
        expressionSegments.add(new LiteralExpressionSegment(0, 0, 3));
        expressionSegments.add(new LiteralExpressionSegment(0, 0, 5));
        List<EncryptCondition> encryptConditions = new LinkedList<>();
        encryptConditions.add(new EncryptCondition("id", "table_k", 29, 39, expressionSegments));
        SQLRouteResult result = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), encryptConditions,
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                new SelectItems(Collections.<SelectItem>emptyList(), false, 0), new Pagination(null, null, Collections.emptyList())));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteUpdateWithShardingEncryptor() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForUpdateWithShardingEncryptor(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("UPDATE table_z SET id = 'encryptValue' WHERE id = 'encryptValue'"));
    }
    
    private SQLRouteResult createSQLRouteResultForUpdateWithShardingEncryptor() {
        UpdateStatement updateStatement = new UpdateStatement();
        updateStatement.setLogicSQL("UPDATE table_z SET id = 1 WHERE id = 2");
        updateStatement.getTables().add(new Table("table_z", ""));
        updateStatement.getSQLSegments().add(new TableSegment(7, 13, "table_z"));
        updateStatement.setSetAssignment(
                new SetAssignmentsSegment(15, 24, Collections.singleton(new AssignmentSegment(19, 24, new ColumnSegment(19, 20, "id"), new LiteralExpressionSegment(0, 0, 2)))));
        List<EncryptCondition> encryptConditions = new LinkedList<>();
        encryptConditions.add(new EncryptCondition("id", "table_z", 32, 37, new LiteralExpressionSegment(0, 0, 2)));
        SQLRouteResult result = new SQLRouteResult(new ShardingWhereOptimizedStatement(updateStatement, 
                new ShardingConditions(Collections.<ShardingCondition>emptyList()), new EncryptConditions(encryptConditions)));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteInsertWithQueryAssistedShardingEncryptor() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForInsertWithQueryAssistedShardingEncryptor(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(routingUnit, logicTableAndActualTables).getSql(),
                is("INSERT INTO `table_w` set name = 'encryptValue', id = 1, query_name = 'assistedEncryptValue' ON DUPLICATE KEY UPDATE name = VALUES(name)"));
    }
    
    private SQLRouteResult createSQLRouteResultForInsertWithQueryAssistedShardingEncryptor() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setLogicSQL("INSERT INTO `table_w` set name = 10 ON DUPLICATE KEY UPDATE name = VALUES(name)");
        insertStatement.getColumns().add(new ColumnSegment(0, 0, "name"));
        ColumnSegment columnSegment = new ColumnSegment(26, 29, "name");
        LiteralExpressionSegment expressionSegment = new LiteralExpressionSegment(33, 34, 10);
        insertStatement.getSQLSegments().add(new SetAssignmentsSegment(22, 34, Collections.singleton(new AssignmentSegment(22, 34, columnSegment, expressionSegment))));
        insertStatement.getSQLSegments().add(new TableSegment(12, 20, "`table_w`"));
        insertStatement.getTables().add(new Table("table_w", null));
        ShardingInsertColumns insertColumns = mock(ShardingInsertColumns.class);
        when(insertColumns.getRegularColumnNames()).thenReturn(Collections.singletonList("name"));
        when(insertColumns.getAllColumnNames()).thenReturn(Arrays.asList("name", "id", "query_name"));
        ShardingInsertOptimizedStatement optimizedStatement = new ShardingInsertOptimizedStatement(insertStatement, Collections.<ShardingCondition>emptyList(), insertColumns, 
                Collections.singletonList(new InsertValue(Collections.<ExpressionSegment>singletonList(new LiteralExpressionSegment(33, 34, 10)))), null);
        ExpressionSegment[] expressionSegments = {new LiteralExpressionSegment(0, 0, 10), new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, 10)};
        optimizedStatement.addUnit(expressionSegments, new Object[0], 0);
        optimizedStatement.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteSelectInWithAggregationDistinct() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForSelectInWithAggregationDistinct(), Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT DISTINCT id, id FROM table_z WHERE id in (3,5)"));
    }
    
    private SQLRouteResult createSQLRouteResultForSelectInWithAggregationDistinct() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setLogicSQL("SELECT COUNT(DISTINCT id), SUM(DISTINCT id) FROM table_z WHERE id in (3,5)");
        selectStatement.getSQLSegments().add(new TableSegment(49, 55, "table_z"));
        AggregationDistinctSelectItemSegment selectItemSegment1 = new AggregationDistinctSelectItemSegment(7, 24, "DISTINCT id", AggregationType.COUNT, 12, "id");
        selectItemSegment1.setAlias("a");
        AggregationDistinctSelectItemSegment selectItemSegment2 = new AggregationDistinctSelectItemSegment(27, 42, "DISTINCT id", AggregationType.SUM, 30, "id");
        selectItemSegment2.setAlias("b");
        SelectItemsSegment selectItemsSegment = new SelectItemsSegment(7, 42, true);
        selectItemsSegment.getSelectItems().add(selectItemSegment1);
        selectItemsSegment.getSelectItems().add(selectItemSegment2);
        selectStatement.getSQLSegments().add(selectItemsSegment);
        SQLRouteResult result = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), Collections.<EncryptCondition>emptyList(),
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                new SelectItems(Collections.<SelectItem>emptyList(), false, 0), new Pagination(null, null, Collections.emptyList())));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteSelectEqualWithShardingEncryptor() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForSelectEqualWithShardingEncryptor(), Arrays.<Object>asList(1, "x"));
        assertThat(rewriteEngine.generateSQL().getSql(), is("SELECT id FROM table_z WHERE id = ? AND name=?"));
        assertThat(getParameterBuilder(rewriteEngine).getParameters().get(0), is((Object) "encryptValue"));
    }
    
    private SQLRouteResult createSQLRouteResultForSelectEqualWithShardingEncryptor() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setLogicSQL("SELECT id FROM table_z WHERE id=? AND name=?");
        selectStatement.getTables().add(new Table("table_z", null));
        selectStatement.getSQLSegments().add(new TableSegment(15, 21, "table_z"));
        List<EncryptCondition> encryptConditions = new LinkedList<>();
        encryptConditions.add(new EncryptCondition("id", "table_z", 29, 32, new ParameterMarkerExpressionSegment(0, 0, 0)));
        SQLRouteResult result = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), encryptConditions,
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                new SelectItems(Collections.<SelectItem>emptyList(), false, 0), new Pagination(null, null, Collections.emptyList())));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteSelectInWithShardingEncryptorWithParameter() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForSelectInWithShardingEncryptorWithParameter(), Arrays.<Object>asList(1, 2));
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT id FROM table_z WHERE id IN (?, ?) or id = 'encryptValue'"));
        assertThat(getParameterBuilder(rewriteEngine).getParameters().get(0), is((Object) "encryptValue"));
        assertThat(getParameterBuilder(rewriteEngine).getParameters().get(1), is((Object) "encryptValue"));
    }
    
    private SQLRouteResult createSQLRouteResultForSelectInWithShardingEncryptorWithParameter() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setLogicSQL("SELECT id FROM table_z WHERE id in (?, ?) or id = 3");
        selectStatement.getTables().add(new Table("table_z", null));
        selectStatement.getSQLSegments().add(new TableSegment(15, 21, "table_z"));
        List<ExpressionSegment> expressionSegments = new LinkedList<>();
        expressionSegments.add(new ParameterMarkerExpressionSegment(0, 0, 0));
        expressionSegments.add(new ParameterMarkerExpressionSegment(0, 0, 1));
        List<EncryptCondition> encryptConditions = new LinkedList<>();
        encryptConditions.add(new EncryptCondition("id", "table_z", 29, 40, expressionSegments));
        encryptConditions.add(new EncryptCondition("id", "table_z", 45, 50, new LiteralExpressionSegment(0, 0, 3)));
        SQLRouteResult result = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), encryptConditions,
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                new SelectItems(Collections.<SelectItem>emptyList(), false, 0), new Pagination(null, null, Collections.emptyList())));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteSelectEqualWithQueryAssistedShardingEncryptor() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForSelectEqualWithQueryAssistedShardingEncryptor(), Arrays.<Object>asList(1, "k"));
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT id FROM table_k WHERE query_id = ? AND name=?"));
        assertThat(getParameterBuilder(rewriteEngine).getParameters().get(0), is((Object) "assistedEncryptValue"));
    }
    
    private SQLRouteResult createSQLRouteResultForSelectEqualWithQueryAssistedShardingEncryptor() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setLogicSQL("SELECT id FROM table_k WHERE id=? AND name=?");
        selectStatement.getTables().add(new Table("table_k", null));
        selectStatement.getSQLSegments().add(new TableSegment(15, 21, "table_k"));
        List<EncryptCondition> encryptConditions = new LinkedList<>();
        encryptConditions.add(new EncryptCondition("id", "table_k", 29, 32, new ParameterMarkerExpressionSegment(0, 0, 0)));
        SQLRouteResult result = new SQLRouteResult(new ShardingSelectOptimizedStatement(selectStatement, Collections.<ShardingCondition>emptyList(), encryptConditions,
                new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
                new SelectItems(Collections.<SelectItem>emptyList(), false, 0), new Pagination(null, null, Collections.emptyList())));
        result.setRoutingResult(new RoutingResult());
        return result;
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
