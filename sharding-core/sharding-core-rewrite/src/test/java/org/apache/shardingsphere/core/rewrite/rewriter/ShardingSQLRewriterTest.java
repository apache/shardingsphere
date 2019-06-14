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
import org.apache.shardingsphere.core.optimize.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.condition.ShardingConditions;
import org.apache.shardingsphere.core.optimize.pagination.Pagination;
import org.apache.shardingsphere.core.optimize.result.OptimizeResult;
import org.apache.shardingsphere.core.optimize.result.insert.InsertOptimizeResult;
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
import org.apache.shardingsphere.core.parse.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.rewrite.SQLRewriteEngine;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.builder.SQLBuilder;
import org.apache.shardingsphere.core.rewrite.rewriter.parameter.ParameterRewriter;
import org.apache.shardingsphere.core.rewrite.rewriter.parameter.ShardingParameterRewriter;
import org.apache.shardingsphere.core.rewrite.rewriter.sql.EncryptSQLRewriter;
import org.apache.shardingsphere.core.rewrite.rewriter.sql.SQLRewriter;
import org.apache.shardingsphere.core.rewrite.rewriter.sql.ShardingSQLRewriter;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class ShardingSQLRewriterTest {
    
    private ShardingRule shardingRule;
    
    private SQLRouteResult routeResult;
    
    private SelectStatement selectStatement;
    
    private InsertStatement insertStatement;
    
    private DALStatement showTablesStatement;
    
    private UpdateStatement updateStatement;
    
    private DeleteStatement deleteStatement;
    
    private Map<String, String> tableTokens;
    
    @Before
    public void setUp() throws IOException {
        URL url = ShardingSQLRewriterTest.class.getClassLoader().getResource("yaml/rewrite-rule.yaml");
        Preconditions.checkNotNull(url, "Cannot found rewrite rule yaml configuration.");
        YamlRootShardingConfiguration yamlShardingConfig = YamlEngine.unmarshal(new File(url.getFile()), YamlRootShardingConfiguration.class);
        shardingRule = new ShardingRule(new ShardingRuleConfigurationYamlSwapper().swap(yamlShardingConfig.getShardingRule()), yamlShardingConfig.getDataSources().keySet());
        selectStatement = new SelectStatement();
        insertStatement = new InsertStatement();
        showTablesStatement = new DALStatement();
        updateStatement = new UpdateStatement();
        deleteStatement = new DeleteStatement();
        tableTokens = new HashMap<>(1, 1);
        tableTokens.put("table_x", "table_1");
    }
    
    @Test
    public void assertRewriteWithoutChange() {
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        selectStatement.setLogicSQL("SELECT table_y.id FROM table_y WHERE table_y.id=?");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.<Object>singletonList(1));
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is("SELECT table_y.id FROM table_y WHERE table_y.id=?"));
    }
    
    @Test
    public void assertRewriteForTableName() {
        List<Object> parameters = new ArrayList<>(2);
        parameters.add(1);
        parameters.add("x");
        selectStatement.getSQLSegments().add(new TableSegment(7, 13, "table_x"));
        selectStatement.getSQLSegments().add(new TableSegment(31, 37, "table_x"));
        selectStatement.getSQLSegments().add(new TableSegment(47, 53, "table_x"));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        selectStatement.setLogicSQL("SELECT table_x.id, x.name FROM table_x x WHERE table_x.id=? AND x.name=?");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(parameters);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is("SELECT table_1.id, x.name FROM table_1 x WHERE table_1.id=? AND x.name=?"));
    }
    
    @Test
    public void assertRewriteForOrderByAndGroupByDerivedColumns() {
        selectStatement.getSQLSegments().add(new TableSegment(18, 24, "table_x"));
        DerivedCommonSelectItem selectItem1 = new DerivedCommonSelectItem("x.id", Optional.of("GROUP_BY_DERIVED_0"));
        DerivedCommonSelectItem selectItem2 = new DerivedCommonSelectItem("x.name", Optional.of("ORDER_BY_DERIVED_0"));
        selectStatement.getItems().add(selectItem1);
        selectStatement.getItems().add(selectItem2);
        selectStatement.setSelectListStopIndex(11);
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        selectStatement.setLogicSQL("SELECT x.age FROM table_x x GROUP BY x.id ORDER BY x.name");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is(
                "SELECT x.age , x.id AS GROUP_BY_DERIVED_0 , x.name AS ORDER_BY_DERIVED_0 FROM table_1 x GROUP BY x.id ORDER BY x.name"));
    }
    
    @Test
    public void assertRewriteForAggregationDerivedColumns() {
        selectStatement.getSQLSegments().add(new TableSegment(23, 29, "table_x"));
        AggregationSelectItem countSelectItem = new AggregationSelectItem(AggregationType.COUNT, "(x.age)", Optional.of("AVG_DERIVED_COUNT_0"));
        AggregationSelectItem sumSelectItem = new AggregationSelectItem(AggregationType.SUM, "(x.age)", Optional.of("AVG_DERIVED_SUM_0"));
        AggregationSelectItem avgSelectItem = new AggregationSelectItem(AggregationType.AVG, "(x.age)", Optional.<String>absent());
        avgSelectItem.getDerivedAggregationSelectItems().add(countSelectItem);
        avgSelectItem.getDerivedAggregationSelectItems().add(sumSelectItem);
        selectStatement.getItems().add(avgSelectItem);
        selectStatement.setSelectListStopIndex(16);
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        selectStatement.setLogicSQL("SELECT AVG(x.age) FROM table_x x");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is(
                "SELECT AVG(x.age) , COUNT(x.age) AS AVG_DERIVED_COUNT_0 , SUM(x.age) AS AVG_DERIVED_SUM_0 FROM table_1 x"));
    }
    
    @Test
    public void assertRewriteForAutoGeneratedKeyColumn() {
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.getColumnNames().add("name");
        insertStatement.getColumnNames().add("age");
        insertStatement.getSQLSegments().add(new InsertColumnsSegment(30, 30, Collections.singleton(mock(ColumnSegment.class))));
        insertStatement.getSQLSegments().add(new InsertValuesSegment(39, 44, Collections.<ExpressionSegment>emptyList()));
        insertStatement.getSQLSegments().add(new TableSegment(12, 18, "table_x"));
        InsertOptimizeResult insertOptimizeResult = new InsertOptimizeResult(Arrays.asList("name", "age", "id"));
        Object[] parameters = {"x", 1, 1};
        ExpressionSegment[] expressionSegments = {new ParameterMarkerExpressionSegment(0, 0, 0), new ParameterMarkerExpressionSegment(0, 0, 1), new ParameterMarkerExpressionSegment(0, 0, 2)};
        insertOptimizeResult.addUnit(expressionSegments, parameters, 3);
        insertOptimizeResult.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        RoutingUnit routingUnit = new RoutingUnit("db0");
        routingUnit.getTableUnits().add(new TableUnit("table_x", "table_1"));
        routeResult = new SQLRouteResult(insertStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(insertOptimizeResult));
        insertStatement.setLogicSQL("INSERT INTO table_x (name, age) VALUES (?, ?)");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Arrays.asList(parameters));
        assertThat(getSQLBuilder(rewriteEngine).toSQL(routingUnit, tableTokens), is("INSERT INTO table_1 (name, age, id) VALUES (?, ?, ?)"));
    }
    
    @Test
    public void assertRewriteForAutoGeneratedKeyColumnWithoutColumnsWithParameter() {
        insertStatement.getColumnNames().add("name");
        insertStatement.getSQLSegments().add(new InsertColumnsSegment(21, 21, Collections.<ColumnSegment>emptyList()));
        insertStatement.getSQLSegments().add(new InsertValuesSegment(29, 31, Collections.<ExpressionSegment>emptyList()));
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.getSQLSegments().add(new TableSegment(12, 20, "`table_x`"));
        InsertOptimizeResult insertOptimizeResult = new InsertOptimizeResult(Arrays.asList("name", "id"));
        Object[] parameters = {"Bill", 1};
        ExpressionSegment[] expressionSegments = {new ParameterMarkerExpressionSegment(0, 0, 0), new ParameterMarkerExpressionSegment(0, 0, 1)};
        insertOptimizeResult.addUnit(expressionSegments, parameters, 2);
        insertOptimizeResult.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        RoutingUnit routingUnit = new RoutingUnit("db0");
        routingUnit.getTableUnits().add(new TableUnit("table_x", "table_1"));
        routeResult = new SQLRouteResult(insertStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(insertOptimizeResult));
        insertStatement.setLogicSQL("INSERT INTO `table_x` VALUES (?)");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Arrays.asList(parameters));
        assertThat(getSQLBuilder(rewriteEngine).toSQL(routingUnit, tableTokens), is("INSERT INTO `table_1`(name, id) VALUES (?, ?)"));
    }
    
    @Test
    public void assertRewriteForAutoGeneratedKeyColumnWithoutColumnsWithoutParameter() {
        insertStatement.getColumnNames().add("name");
        insertStatement.getSQLSegments().add(new InsertColumnsSegment(21, 21, Collections.<ColumnSegment>emptyList()));
        insertStatement.getSQLSegments().add(new InsertValuesSegment(29, 32, Collections.<ExpressionSegment>emptyList()));
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.getSQLSegments().add(new TableSegment(12, 20, "`table_x`"));
        InsertOptimizeResult insertOptimizeResult = new InsertOptimizeResult(Arrays.asList("name", "id"));
        ExpressionSegment[] expressionSegments = {new LiteralExpressionSegment(0, 0, 10), new LiteralExpressionSegment(0, 0, 1)};
        insertOptimizeResult.addUnit(expressionSegments, new Object[0], 0);
        insertOptimizeResult.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        RoutingUnit routingUnit = new RoutingUnit("db0");
        routingUnit.getTableUnits().add(new TableUnit("table_x", "table_1"));
        routeResult = new SQLRouteResult(insertStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(insertOptimizeResult));
        insertStatement.setLogicSQL("INSERT INTO `table_x` VALUES (10)");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(routingUnit, tableTokens), is("INSERT INTO `table_1`(name, id) VALUES (10, 1)"));
    }
    
    @Test
    public void assertRewriteForDuplicateKeyWithoutColumnsWithoutParameter() {
        insertStatement.getColumnNames().add("name");
        insertStatement.getSQLSegments().add(new InsertColumnsSegment(21, 21, Collections.<ColumnSegment>emptyList()));
        insertStatement.getSQLSegments().add(new InsertValuesSegment(29, 32, Collections.<ExpressionSegment>emptyList()));
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.getSQLSegments().add(new TableSegment(12, 20, "`table_x`"));
        InsertOptimizeResult insertOptimizeResult = new InsertOptimizeResult(Arrays.asList("name", "id"));
        ExpressionSegment[] expressionSegments = {new LiteralExpressionSegment(0, 0, 10), new LiteralExpressionSegment(0, 0, 1)};
        insertOptimizeResult.addUnit(expressionSegments, new Object[0], 0);
        insertOptimizeResult.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        RoutingUnit routingUnit = new RoutingUnit("db0");
        routingUnit.getTableUnits().add(new TableUnit("table_x", "table_1"));
        routeResult = new SQLRouteResult(insertStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(insertOptimizeResult));
        insertStatement.setLogicSQL("INSERT INTO `table_x` VALUES (10) ON DUPLICATE KEY UPDATE name = VALUES(name)");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(routingUnit, tableTokens), is("INSERT INTO `table_1`(name, id) VALUES (10, 1) ON DUPLICATE KEY UPDATE name = VALUES(name)"));
    }
    
    @Test
    public void assertRewriteForDuplicateKeyWithSetWithoutParameter() {
        insertStatement.getColumnNames().add("name");
        insertStatement.getColumnNames().add("id");
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.getSQLSegments().add(new TableSegment(12, 20, "`table_x`"));
        InsertOptimizeResult insertOptimizeResult = new InsertOptimizeResult(Arrays.asList("name", "id"));
        ExpressionSegment[] expressionSegments = {new LiteralExpressionSegment(0, 0, 10), new LiteralExpressionSegment(0, 0, 1)};
        insertOptimizeResult.addUnit(expressionSegments, new Object[0], 0);
        insertOptimizeResult.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        RoutingUnit routingUnit = new RoutingUnit("db0");
        routingUnit.getTableUnits().add(new TableUnit("table_x", "table_1"));
        routeResult = new SQLRouteResult(insertStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(insertOptimizeResult));
        insertStatement.setLogicSQL("INSERT INTO `table_x` set name = 10, id = 1 ON DUPLICATE KEY UPDATE name = VALUES(name)");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(routingUnit, tableTokens), is("INSERT INTO `table_1` set name = 10, id = 1 ON DUPLICATE KEY UPDATE name = VALUES(name)"));
    }
    
    @Test
    public void assertRewriteColumnWithoutColumnsWithoutParameter() {
        insertStatement.getColumnNames().add("name");
        insertStatement.getColumnNames().add("id");
        insertStatement.getTables().add(new Table("table_x", null));
        List<Object> parameters = new ArrayList<>(2);
        parameters.add("x");
        parameters.add(1);
        insertStatement.getSQLSegments().add(new TableSegment(12, 20, "`table_x`"));
        insertStatement.getSQLSegments().add(new InsertColumnsSegment(21, 21, Collections.<ColumnSegment>emptyList()));
        insertStatement.getSQLSegments().add(new InsertValuesSegment(29, 35, Collections.<ExpressionSegment>emptyList()));
        InsertOptimizeResult insertOptimizeResult = new InsertOptimizeResult(Arrays.asList("name", "id"));
        ExpressionSegment[] expressionSegments = {new LiteralExpressionSegment(0, 0, 10), new LiteralExpressionSegment(0, 0, 1)};
        insertOptimizeResult.addUnit(expressionSegments, new Object[0], 0);
        insertOptimizeResult.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        RoutingUnit routingUnit = new RoutingUnit("db0");
        routingUnit.getTableUnits().add(new TableUnit("table_x", "table_1"));
        routeResult = new SQLRouteResult(insertStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(insertOptimizeResult));
        insertStatement.setLogicSQL("INSERT INTO `table_x` VALUES (10, 1)");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(parameters);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(routingUnit, tableTokens), is("INSERT INTO `table_1`(name, id) VALUES (10, 1)"));
    }
    
    @Test
    public void assertRewriteColumnWithoutColumnsWithParameter() {
        insertStatement.getColumnNames().add("name");
        insertStatement.getSQLSegments().add(new InsertColumnsSegment(21, 21, Collections.<ColumnSegment>emptyList()));
        insertStatement.getSQLSegments().add(new InsertValuesSegment(29, 34, Collections.<ExpressionSegment>emptyList()));
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.getSQLSegments().add(new TableSegment(12, 20, "`table_x`"));
        InsertOptimizeResult insertOptimizeResult = new InsertOptimizeResult(Arrays.asList("name", "id"));
        ExpressionSegment[] expressionSegments = {new ParameterMarkerExpressionSegment(0, 0, 0), new ParameterMarkerExpressionSegment(0, 0, 1)};
        Object[] parameters = {"x", 1};
        insertOptimizeResult.addUnit(expressionSegments, parameters, 2);
        insertOptimizeResult.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        RoutingUnit routingUnit = new RoutingUnit("db0");
        routingUnit.getTableUnits().add(new TableUnit("table_x", "table_1"));
        routeResult = new SQLRouteResult(insertStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(insertOptimizeResult));
        insertStatement.setLogicSQL("INSERT INTO `table_x` VALUES (?, ?)");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Arrays.asList(parameters));
        assertThat(getSQLBuilder(rewriteEngine).toSQL(routingUnit, tableTokens), is("INSERT INTO `table_1`(name, id) VALUES (?, ?)"));
    }
    
    @Test
    public void assertRewriteForLimit() {
        PaginationValueSegment offsetSegment = new NumberLiteralLimitValueSegment(33, 33, 2);
        PaginationValueSegment rowCountSegment = new NumberLiteralLimitValueSegment(36, 36, 2);
        selectStatement.setOffset(offsetSegment);
        selectStatement.setRowCount(rowCountSegment);
        selectStatement.getSQLSegments().add(new TableSegment(17, 23, "table_x"));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        routeResult.getOptimizeResult().setPagination(new Pagination(offsetSegment, rowCountSegment, Collections.emptyList()));
        selectStatement.setLogicSQL("SELECT x.id FROM table_x x LIMIT 2, 2");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is("SELECT x.id FROM table_1 x LIMIT 0, 4"));
    }
    
    @Test
    public void assertRewriteForRowNumber() {
        PaginationValueSegment offsetSegment = new NumberLiteralRowNumberValueSegment(119, 119, 2, true);
        PaginationValueSegment rowCountSegment = new NumberLiteralRowNumberValueSegment(98, 98, 4, false);
        selectStatement.setOffset(offsetSegment);
        selectStatement.setRowCount(rowCountSegment);
        selectStatement.getSQLSegments().add(new TableSegment(68, 74, "table_x"));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        routeResult.getOptimizeResult().setPagination(new Pagination(offsetSegment, rowCountSegment, Collections.emptyList()));
        selectStatement.setLogicSQL("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_x x) row_ WHERE rownum<=4) t WHERE t.rownum_>2");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens),
                is("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_1 x) row_ WHERE rownum<=4) t WHERE t.rownum_>0"));
    }
    
    @Test
    public void assertRewriteForTopAndRowNumber() {
        PaginationValueSegment offsetSegment = new NumberLiteralRowNumberValueSegment(123, 123, 2, true);
        PaginationValueSegment rowCountSegment = new NumberLiteralRowNumberValueSegment(26, 26, 4, false);
        selectStatement.setOffset(offsetSegment);
        selectStatement.setRowCount(rowCountSegment);
        selectStatement.getSQLSegments().add(new TableSegment(85, 91, "table_x"));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        routeResult.getOptimizeResult().setPagination(new Pagination(offsetSegment, rowCountSegment, Collections.emptyList()));
        selectStatement.setLogicSQL("SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_x x) AS row_ WHERE row_.rownum_>2");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens),
                is("SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_1 x) AS row_ WHERE row_.rownum_>0"));
    }
    
    @Test
    public void assertRewriteForLimitForMemoryGroupBy() {
        PaginationValueSegment offsetSegment = new NumberLiteralLimitValueSegment(33, 33, 2);
        PaginationValueSegment rowCountSegment = new NumberLiteralLimitValueSegment(36, 36, 2);
        selectStatement.setOffset(offsetSegment);
        selectStatement.setRowCount(rowCountSegment);
        ColumnSegment columnSegment = new ColumnSegment(0, 0, "id");
        columnSegment.setOwner(new TableSegment(0, 0, "x"));
        selectStatement.getOrderByItems().add(new ColumnOrderByItemSegment(0, 0, columnSegment, OrderDirection.ASC, OrderDirection.ASC));
        selectStatement.getGroupByItems().add(new ColumnOrderByItemSegment(0, 0, columnSegment, OrderDirection.DESC, OrderDirection.ASC));
        selectStatement.getSQLSegments().add(new TableSegment(17, 23, "table_x"));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        routeResult.getOptimizeResult().setPagination(new Pagination(offsetSegment, rowCountSegment, Collections.emptyList()));
        selectStatement.setLogicSQL("SELECT x.id FROM table_x x LIMIT 2, 2");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is("SELECT x.id FROM table_1 x LIMIT 0, 2147483647"));
    }
    
    @Test
    public void assertRewriteForRowNumForMemoryGroupBy() {
        PaginationValueSegment offsetSegment = new NumberLiteralRowNumberValueSegment(119, 119, 2, true);
        PaginationValueSegment rowCountSegment = new NumberLiteralRowNumberValueSegment(98, 98, 4, false);
        selectStatement.setOffset(offsetSegment);
        selectStatement.setRowCount(rowCountSegment);
        selectStatement.getSQLSegments().add(new TableSegment(68, 74, "table_x"));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, "id");
        columnSegment.setOwner(new TableSegment(0, 0, "x"));
        selectStatement.getOrderByItems().add(new ColumnOrderByItemSegment(0, 0, columnSegment, OrderDirection.ASC, OrderDirection.ASC));
        selectStatement.getGroupByItems().add(new ColumnOrderByItemSegment(0, 0, columnSegment, OrderDirection.DESC, OrderDirection.ASC));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        routeResult.getOptimizeResult().setPagination(new Pagination(offsetSegment, rowCountSegment, Collections.emptyList()));
        selectStatement.setLogicSQL("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_x x) row_ WHERE rownum<=4) t WHERE t.rownum_>2");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens),
                is("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_1 x) row_ WHERE rownum<=2147483647) t WHERE t.rownum_>0"));
    }
    
    @Test
    public void assertRewriteForTopAndRowNumberForMemoryGroupBy() {
        PaginationValueSegment offsetSegment = new NumberLiteralRowNumberValueSegment(123, 123, 2, false);
        PaginationValueSegment rowCountSegment = new NumberLiteralRowNumberValueSegment(26, 26, 4, false);
        selectStatement.setOffset(offsetSegment);
        selectStatement.setRowCount(rowCountSegment);
        selectStatement.getSQLSegments().add(new TableSegment(85, 91, "table_x"));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, "id");
        columnSegment.setOwner(new TableSegment(0, 0, "x"));
        selectStatement.getOrderByItems().add(new ColumnOrderByItemSegment(0, 0, columnSegment, OrderDirection.ASC, OrderDirection.ASC));
        selectStatement.getGroupByItems().add(new ColumnOrderByItemSegment(0, 0, columnSegment, OrderDirection.DESC, OrderDirection.ASC));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        routeResult.getOptimizeResult().setPagination(new Pagination(offsetSegment, rowCountSegment, Collections.emptyList()));
        selectStatement.setLogicSQL("SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_x x) AS row_ WHERE row_.rownum_>2");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens),
                is("SELECT * FROM (SELECT TOP(2147483647) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_1 x) AS row_ WHERE row_.rownum_>0"));
    }
    
    @Test
    public void assertRewriteForLimitForNotRewritePagination() {
        PaginationValueSegment offsetSegment = new NumberLiteralLimitValueSegment(33, 33, 2);
        PaginationValueSegment rowCountSegment = new NumberLiteralLimitValueSegment(36, 36, 2);
        selectStatement.setOffset(offsetSegment);
        selectStatement.setRowCount(rowCountSegment);
        selectStatement.getSQLSegments().add(new TableSegment(17, 23, "table_x"));
        routeResult = new SQLRouteResult(selectStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        routeResult.getOptimizeResult().setPagination(new Pagination(offsetSegment, rowCountSegment, Collections.emptyList()));
        selectStatement.setLogicSQL("SELECT x.id FROM table_x x LIMIT 2, 2");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is("SELECT x.id FROM table_1 x LIMIT 2, 2"));
    }
    
    @Test
    public void assertRewriteForRowNumForNotRewritePagination() {
        PaginationValueSegment offsetSegment = new NumberLiteralRowNumberValueSegment(119, 119, 2, true);
        PaginationValueSegment rowCountSegment = new NumberLiteralRowNumberValueSegment(98, 98, 4, false);
        selectStatement.setOffset(offsetSegment);
        selectStatement.setRowCount(rowCountSegment);
        selectStatement.getSQLSegments().add(new TableSegment(68, 74, "table_x"));
        routeResult = new SQLRouteResult(selectStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        routeResult.getOptimizeResult().setPagination(new Pagination(offsetSegment, rowCountSegment, Collections.emptyList()));
        selectStatement.setLogicSQL("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_x x) row_ WHERE rownum<=4) t WHERE t.rownum_>2");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens),
                is("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_1 x) row_ WHERE rownum<=4) t WHERE t.rownum_>2"));
    }
    
    @Test
    public void assertRewriteForTopAndRowNumberForNotRewritePagination() {
        PaginationValueSegment offsetSegment = new NumberLiteralRowNumberValueSegment(123, 123, 2, true);
        PaginationValueSegment rowCountSegment = new NumberLiteralRowNumberValueSegment(26, 26, 4, false);
        selectStatement.setOffset(offsetSegment);
        selectStatement.setRowCount(rowCountSegment);
        selectStatement.getSQLSegments().add(new TableSegment(85, 91, "table_x"));
        routeResult = new SQLRouteResult(selectStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        routeResult.getOptimizeResult().setPagination(new Pagination(offsetSegment, rowCountSegment, Collections.emptyList()));
        selectStatement.setLogicSQL("SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_x x) AS row_ WHERE row_.rownum_>2");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens),
                is("SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_1 x) AS row_ WHERE row_.rownum_>2"));
    }
    
    @Test
    public void assertRewriteForDerivedOrderBy() {
        selectStatement.setGroupByLastIndex(60);
        selectStatement.setToAppendOrderByItems(true);
        ColumnSegment columnSegment1 = new ColumnSegment(0, 0, "id");
        columnSegment1.setOwner(new TableSegment(0, 0, "x"));
        ColumnSegment columnSegment2 = new ColumnSegment(0, 0, "name");
        columnSegment2.setOwner(new TableSegment(0, 0, "x"));
        selectStatement.getGroupByItems().add(new ColumnOrderByItemSegment(0, 0, columnSegment1, OrderDirection.ASC, OrderDirection.ASC));
        selectStatement.getOrderByItems().add(new ColumnOrderByItemSegment(0, 0, columnSegment1, OrderDirection.ASC, OrderDirection.ASC));
        selectStatement.getGroupByItems().add(new ColumnOrderByItemSegment(0, 0, columnSegment2, OrderDirection.DESC, OrderDirection.ASC));
        selectStatement.getOrderByItems().add(new ColumnOrderByItemSegment(0, 0, columnSegment2, OrderDirection.DESC, OrderDirection.ASC));
        selectStatement.getSQLSegments().add(new TableSegment(25, 31, "table_x"));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        selectStatement.setLogicSQL("SELECT x.id, x.name FROM table_x x GROUP BY x.id, x.name DESC");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is("SELECT x.id, x.name FROM table_1 x GROUP BY x.id, x.name DESC ORDER BY x.id ASC,x.name DESC "));
    }
    
    @Test
    public void assertGenerateSQL() {
        List<Object> parameters = new ArrayList<>(2);
        parameters.add(1);
        parameters.add("x");
        selectStatement.getTables().add(new Table("table_x", "x"));
        selectStatement.getTables().add(new Table("table_y", "y"));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        selectStatement.setLogicSQL("SELECT table_x.id, x.name FROM table_x x, table_y y WHERE table_x.id=? AND x.name=?");
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, routeResult.getSqlStatement(), parameters, routeResult.getRoutingResult().isSingleRouting());
        rewriteEngine.init(Collections.<ParameterRewriter>singletonList(new ShardingParameterRewriter(routeResult)), 
                Arrays.asList(new ShardingSQLRewriter(shardingRule, routeResult, null), 
                        new EncryptSQLRewriter(shardingRule.getEncryptRule().getEncryptorEngine(), (DMLStatement) routeResult.getSqlStatement(), routeResult.getOptimizeResult())));
        RoutingUnit routingUnit = new RoutingUnit("db0");
        routingUnit.getTableUnits().add(new TableUnit("table_x", "table_x"));
        assertThat(rewriteEngine.generateSQL(routingUnit).getSql(), is("SELECT table_x.id, x.name FROM table_x x, table_y y WHERE table_x.id=? AND x.name=?"));
    }
    
    @Test
    public void assertIndexTokenForIndexNameTableName() {
        selectStatement.getTables().add(new Table("table_x", null));
        selectStatement.getSQLSegments().add(new IndexSegment(13, 22, "index_name", QuoteCharacter.NONE));
        selectStatement.getSQLSegments().add(new TableSegment(27, 33, "table_x"));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        selectStatement.setLogicSQL("CREATE INDEX index_name ON table_x ('column')");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is("CREATE INDEX index_name_table_1 ON table_1 ('column')"));
    }
    
    @SneakyThrows
    private SQLBuilder getSQLBuilder(final SQLRewriteEngine rewriteEngine) {
        Field field = rewriteEngine.getClass().getDeclaredField("sqlBuilder");
        field.setAccessible(true);
        return (SQLBuilder) field.get(rewriteEngine);
    }
    
    @SneakyThrows
    private ParameterBuilder getParameterBuilder(final SQLRewriteEngine rewriteEngine) {
        Field field = rewriteEngine.getClass().getDeclaredField("parameterBuilder");
        field.setAccessible(true);
        return (ParameterBuilder) field.get(rewriteEngine);
    }
    
    @Test
    public void assertIndexTokenForIndexNameTableNameWithoutLogicTableName() {
        selectStatement.getTables().add(new Table("table_x", null));
        selectStatement.getSQLSegments().add(new IndexSegment(13, 23, "logic_index", QuoteCharacter.NONE));
        selectStatement.getSQLSegments().add(new TableSegment(28, 34, "table_x"));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        selectStatement.setLogicSQL("CREATE INDEX logic_index ON table_x ('column')");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is("CREATE INDEX logic_index_table_1 ON table_1 ('column')"));
    }
    
    @Test
    public void assertTableTokenWithoutBackQuoteForShow() {
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 24, "table_x"));
        routeResult = new SQLRouteResult(showTablesStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM table_x");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is("SHOW COLUMNS FROM table_1"));
    }
    
    @Test
    public void assertTableTokenWithoutBackQuoteFromSchemaForShow() {
        showTablesStatement.getSQLSegments().add(new FromSchemaSegment(25, 43));
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 24, "table_x"));
        routeResult = new SQLRouteResult(showTablesStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM table_x FROM 'sharding_db'");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        Map<String, String> logicAndActualTableMap = new LinkedHashMap<>();
        logicAndActualTableMap.put("table_x", "table_x");
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, logicAndActualTableMap), is("SHOW COLUMNS FROM table_x"));
    }
    
    @Test
    public void assertTableTokenWithBackQuoteForShow() {
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 26, "`table_x`"));
        routeResult = new SQLRouteResult(showTablesStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM `table_x`");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is("SHOW COLUMNS FROM `table_1`"));
    }
    
    @Test
    public void assertTableTokenWithBackQuoteFromSchemaForShow() {
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 26, "`table_x`"));
        routeResult = new SQLRouteResult(showTablesStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM `table_x` FROM 'sharding_db'");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is("SHOW COLUMNS FROM `table_1` FROM 'sharding_db'"));
    }
    
    @Test
    public void assertTableTokenWithSchemaForShow() {
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 36, "table_x"));
        routeResult = new SQLRouteResult(showTablesStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM sharding_db.table_x");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is("SHOW COLUMNS FROM table_1"));
    }
    
    @Test
    public void assertTableTokenWithSchemaFromSchemaForShow() {
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 36, "table_x"));
        routeResult = new SQLRouteResult(showTablesStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM sharding_db.table_x FROM sharding_db");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is("SHOW COLUMNS FROM table_1 FROM sharding_db"));
    }
    
    @Test
    public void assertTableTokenWithBackQuoteWithSchemaForShow() {
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 38, "`table_x`"));
        routeResult = new SQLRouteResult(showTablesStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM sharding_db.`table_x`");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is("SHOW COLUMNS FROM `table_1`"));
    }
    
    @Test
    public void assertTableTokenWithBackQuoteWithSchemaFromSchemaForShow() {
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 38, "`table_x`"));
        routeResult = new SQLRouteResult(showTablesStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM sharding_db.`table_x` FROM sharding_db");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is("SHOW COLUMNS FROM `table_1` FROM sharding_db"));
    }
    
    @Test
    public void assertTableTokenWithSchemaWithBackQuoteForShow() {
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 40, "`table_x`"));
        routeResult = new SQLRouteResult(showTablesStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM `sharding_db`.`table_x`");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is("SHOW COLUMNS FROM `table_1`"));
    }
    
    @Test
    public void assertTableTokenWithSchemaWithBackQuoteFromSchemaForShow() {
        showTablesStatement.getSQLSegments().add(new TableSegment(18, 40, "`table_x`"));
        routeResult = new SQLRouteResult(showTablesStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        showTablesStatement.setLogicSQL("SHOW COLUMNS FROM `sharding_db`.`table_x` FROM sharding_db");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is("SHOW COLUMNS FROM `table_1` FROM sharding_db"));
    }
    
    @Test
    public void assertTableTokenWithSchemaForSelect() {
        selectStatement.getTables().add(new Table("table_x", null));
        TableSegment tableSegment = new TableSegment(14, 32, "table_x");
        tableSegment.setOwner(new SchemaSegment(14, 24, "sharding_db"));
        selectStatement.getSQLSegments().add(tableSegment);
        routeResult = new SQLRouteResult(selectStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        selectStatement.setLogicSQL("SELECT * FROM sharding_db.table_x");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is("SELECT * FROM table_1"));
    }
    
    @Test
    public void assertTableTokenWithSchemaForInsert() {
        insertStatement.getSQLSegments().add(new TableSegment(12, 30, "table_x"));
        routeResult = new SQLRouteResult(insertStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        insertStatement.setLogicSQL("INSERT INTO sharding_db.table_x (order_id, user_id, status) values (1, 1, 'OK')");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(
                null, tableTokens), is("INSERT INTO table_1 (order_id, user_id, status) values (1, 1, 'OK')"));
    }
    
    @Test
    public void assertTableTokenWithSchemaForUpdate() {
        updateStatement.getSQLSegments().add(new TableSegment(7, 27, "table_x"));
        routeResult = new SQLRouteResult(updateStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        updateStatement.setLogicSQL("UPDATE `sharding_db`.table_x SET user_id=1 WHERE order_id=1");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is("UPDATE table_1 SET user_id=1 WHERE order_id=1"));
    }
    
    @Test
    public void assertTableTokenWithSchemaForDelete() {
        deleteStatement.getSQLSegments().add(new TableSegment(12, 34, "`table_x`"));
        routeResult = new SQLRouteResult(deleteStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        deleteStatement.setLogicSQL("DELETE FROM `sharding_db`.`table_x` WHERE user_id=1");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is("DELETE FROM `table_1` WHERE user_id=1"));
    }
    
    @Test
    public void assertSelectEqualWithShardingEncryptor() {
        List<Object> parameters = new ArrayList<>(2);
        parameters.add(1);
        parameters.add("x");
        Column column = new Column("id", "table_z");
        selectStatement.getSQLSegments().add(new TableSegment(15, 21, "table_z"));
        selectStatement.getEncryptConditions().getOrConditions().add(new AndCondition());
        selectStatement.getEncryptConditions().getOrConditions().get(0).getConditions().add(
                new Condition(column, new PredicateSegment(29, 32, null, null), new ParameterMarkerExpressionSegment(0, 0, 0)));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        selectStatement.setLogicSQL("SELECT id FROM table_z WHERE id=? AND name=?");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(parameters);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(), is("SELECT id FROM table_z WHERE id = ? AND name=?"));
        assertThat(getParameterBuilder(rewriteEngine).getParameters().get(0), is((Object) "encryptValue"));
    }
    
    @Test
    public void assertSelectInWithShardingEncryptor() {
        selectStatement.getSQLSegments().add(new TableSegment(15, 21, "table_z"));
        selectStatement.getEncryptConditions().getOrConditions().add(new AndCondition());
        List<ExpressionSegment> expressionSegments = new LinkedList<>();
        expressionSegments.add(new LiteralExpressionSegment(0, 0, 3));
        expressionSegments.add(new LiteralExpressionSegment(0, 0, 5));
        Column column = new Column("id", "table_z");
        selectStatement.getEncryptConditions().getOrConditions().get(0).getConditions().add(new Condition(column, new PredicateSegment(29, 39, null, null), expressionSegments));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        selectStatement.setLogicSQL("SELECT id FROM table_z WHERE id in (3,5)");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is("SELECT id FROM table_z WHERE id IN ('encryptValue', 'encryptValue')"));
    }
    
    @Test
    public void assertSelectInWithShardingEncryptorWithParameter() {
        List<Object> parameters = new ArrayList<>(2);
        parameters.add(1);
        parameters.add(2);
        selectStatement.getSQLSegments().add(new TableSegment(15, 21, "table_z"));
        selectStatement.getEncryptConditions().getOrConditions().add(new AndCondition());
        selectStatement.getEncryptConditions().getOrConditions().add(new AndCondition());
        List<ExpressionSegment> expressionSegments = new LinkedList<>();
        expressionSegments.add(new ParameterMarkerExpressionSegment(0, 0, 0));
        expressionSegments.add(new ParameterMarkerExpressionSegment(0, 0, 1));
        Column column = new Column("id", "table_z");
        selectStatement.getEncryptConditions().getOrConditions().get(0).getConditions().add(new Condition(column, new PredicateSegment(29, 40, null, null), expressionSegments));
        selectStatement.getEncryptConditions().getOrConditions().get(0).getConditions().add(new Condition(column, new PredicateSegment(45, 50, null, null), new LiteralExpressionSegment(0, 0, 3)));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        selectStatement.setLogicSQL("SELECT id FROM table_z WHERE id in (?, ?) or id = 3");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(parameters);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is("SELECT id FROM table_z WHERE id IN (?, ?) or id = 'encryptValue'"));
        assertThat(getParameterBuilder(rewriteEngine).getParameters().get(0), is((Object) "encryptValue"));
        assertThat(getParameterBuilder(rewriteEngine).getParameters().get(1), is((Object) "encryptValue"));
    }
    
    @Test
    public void assertSelectEqualWithQueryAssistedShardingEncryptor() {
        List<Object> parameters = new ArrayList<>(2);
        parameters.add(1);
        parameters.add("k");
        Column column = new Column("id", "table_k");
        selectStatement.getSQLSegments().add(new TableSegment(15, 21, "table_k"));
        selectStatement.getEncryptConditions().getOrConditions().add(new AndCondition());
        selectStatement.getEncryptConditions().getOrConditions().get(0).getConditions().add(
                new Condition(column, new PredicateSegment(29, 32, null, null), new ParameterMarkerExpressionSegment(0, 0, 0)));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        selectStatement.setLogicSQL("SELECT id FROM table_k WHERE id=? AND name=?");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(parameters);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is("SELECT id FROM table_k WHERE query_id = ? AND name=?"));
        assertThat(getParameterBuilder(rewriteEngine).getParameters().get(0), is((Object) "assistedEncryptValue"));
    }
    
    @Test
    public void assertSelectInWithQueryAssistedShardingEncryptor() {
        selectStatement.getSQLSegments().add(new TableSegment(15, 21, "table_k"));
        selectStatement.getEncryptConditions().getOrConditions().add(new AndCondition());
        List<ExpressionSegment> expressionSegments = new LinkedList<>();
        expressionSegments.add(new LiteralExpressionSegment(0, 0, 3));
        expressionSegments.add(new LiteralExpressionSegment(0, 0, 5));
        Column column = new Column("id", "table_k");
        selectStatement.getEncryptConditions().getOrConditions().get(0).getConditions().add(new Condition(column, new PredicateSegment(29, 39, null, null), expressionSegments));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        selectStatement.setLogicSQL("SELECT id FROM table_k WHERE id in (3,5)");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), 
                is("SELECT id FROM table_k WHERE query_id IN ('assistedEncryptValue', 'assistedEncryptValue')"));
    }
    
    @Test
    public void assertUpdateWithShardingEncryptor() {
        updateStatement.getTables().add(new Table("table_z", ""));
        updateStatement.getSQLSegments().add(new TableSegment(7, 13, "table_z"));
        SetAssignmentsSegment setAssignmentsSegment = new SetAssignmentsSegment(15, 24, Collections.singleton(new AssignmentSegment(19, 24, new ColumnSegment(19, 20, "id"), null)));
        updateStatement.getSQLSegments().add(setAssignmentsSegment);
        Column column = new Column("id", "table_z");
        updateStatement.getAssignments().put(column, new LiteralExpressionSegment(0, 0, 1));
        updateStatement.getEncryptConditions().getOrConditions().add(new AndCondition());
        updateStatement.getEncryptConditions().getOrConditions().get(0).getConditions().add(new Condition(column, new PredicateSegment(32, 37, null, null), new LiteralExpressionSegment(0, 0, 2)));
        routeResult = new SQLRouteResult(updateStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        updateStatement.setLogicSQL("UPDATE table_z SET id = 1 WHERE id = 2");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is("UPDATE table_z SET id = 'encryptValue' WHERE id = 'encryptValue'"));
    }
    
    @Test
    public void assertInsertWithQueryAssistedShardingEncryptor() {
        insertStatement.getColumnNames().add("name");
        ColumnSegment columnSegment = new ColumnSegment(26, 29, "name");
        LiteralExpressionSegment expressionSegment = new LiteralExpressionSegment(33, 34, 10);
        insertStatement.getSQLSegments().add(new SetAssignmentsSegment(22, 34, Collections.singleton(new AssignmentSegment(22, 34, columnSegment, expressionSegment))));
        insertStatement.getSQLSegments().add(new TableSegment(12, 20, "`table_w`"));
        insertStatement.getTables().add(new Table("table_w", null));
        InsertOptimizeResult insertOptimizeResult = new InsertOptimizeResult(Arrays.asList("name", "id", "query_name"));
        ExpressionSegment[] expressionSegments = {new LiteralExpressionSegment(0, 0, 10), new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, 10)};
        insertOptimizeResult.addUnit(expressionSegments, new Object[0], 0);
        insertOptimizeResult.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        RoutingUnit routingUnit = new RoutingUnit("db0");
        routingUnit.getTableUnits().add(new TableUnit("table_w", "table_1"));
        routeResult = new SQLRouteResult(insertStatement);
        routeResult.setOptimizeResult(new OptimizeResult(insertOptimizeResult));
        routeResult.setRoutingResult(new RoutingResult());
        insertStatement.setLogicSQL("INSERT INTO `table_w` set name = 10 ON DUPLICATE KEY UPDATE name = VALUES(name)");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(routingUnit, tableTokens), 
                is("INSERT INTO `table_w` set name = 'encryptValue', id = 1, query_name = 'assistedEncryptValue' ON DUPLICATE KEY UPDATE name = VALUES(name)"));
    }
    
    @Test
    public void assertSelectInWithAggregationDistinct() {
        selectStatement.getSQLSegments().add(new TableSegment(49, 55, "table_z"));
        AggregationDistinctSelectItemSegment selectItemSegment1 = new AggregationDistinctSelectItemSegment(7, 24, "DISTINCT id", AggregationType.COUNT, 12, "id");
        selectItemSegment1.setAlias("a");
        AggregationDistinctSelectItemSegment selectItemSegment2 = new AggregationDistinctSelectItemSegment(27, 42, "DISTINCT id", AggregationType.SUM, 30, "id");
        selectItemSegment2.setAlias("b");
        SelectItemsSegment selectItemsSegment = new SelectItemsSegment(7, 42, true);
        selectItemsSegment.getSelectItems().add(selectItemSegment1);
        selectItemsSegment.getSelectItems().add(selectItemSegment2);
        selectStatement.getSQLSegments().add(selectItemsSegment);
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList())));
        selectStatement.setLogicSQL("SELECT COUNT(DISTINCT id), SUM(DISTINCT id) FROM table_z WHERE id in (3,5)");
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(Collections.emptyList());
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens), is("SELECT DISTINCT id, id FROM table_z WHERE id in (3,5)"));
    }
    
    private SQLRewriteEngine createSQLRewriteEngine(final List<Object> parameters) {
        SQLRewriteEngine result = new SQLRewriteEngine(shardingRule, routeResult.getSqlStatement(), parameters, routeResult.getRoutingResult().isSingleRouting());
        Collection<SQLRewriter> sqlRewriters = new LinkedList<>();
        sqlRewriters.add(new ShardingSQLRewriter(shardingRule, routeResult, routeResult.getOptimizeResult()));
        if (routeResult.getSqlStatement() instanceof DMLStatement) {
            sqlRewriters.add(new EncryptSQLRewriter(shardingRule.getEncryptRule().getEncryptorEngine(), (DMLStatement) routeResult.getSqlStatement(), routeResult.getOptimizeResult()));
        }
        result.init(Collections.<ParameterRewriter>singletonList(new ShardingParameterRewriter(routeResult)), sqlRewriters);
        return result;
    }
}
