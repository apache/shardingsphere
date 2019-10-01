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
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.core.optimize.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.core.optimize.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.core.optimize.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.core.optimize.segment.select.projection.Projection;
import org.apache.shardingsphere.core.optimize.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.core.optimize.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.core.optimize.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.core.optimize.segment.select.projection.impl.DerivedProjection;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.optimize.statement.impl.CommonSQLStatementContext;
import org.apache.shardingsphere.core.optimize.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.core.optimize.statement.impl.SelectSQLStatementContext;
import org.apache.shardingsphere.core.parse.core.constant.AggregationType;
import org.apache.shardingsphere.core.parse.core.constant.OrderDirection;
import org.apache.shardingsphere.core.parse.core.constant.QuoteCharacter;
import org.apache.shardingsphere.core.parse.sql.segment.dal.FromSchemaSegment;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ColumnSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateInRightValue;
import org.apache.shardingsphere.core.parse.sql.segment.generic.SchemaSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dal.DALStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.rewrite.SQLRewriteEngine;
import org.apache.shardingsphere.core.rewrite.parameter.builder.standard.StandardParameterBuilder;
import org.apache.shardingsphere.core.rewrite.constant.ShardingDerivedColumnType;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingCondition;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingConditions;
import org.apache.shardingsphere.core.route.router.sharding.keygen.GeneratedKey;
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
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultWithoutChange(), "SELECT table_y.id FROM table_y WHERE table_y.id=?", Collections.<Object>singletonList(1));
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT table_y.id FROM table_y WHERE table_y.id=?"));
        assertThat(rewriteEngine.generateSQL().getSql(), is("SELECT table_y.id FROM table_y WHERE table_y.id=?"));
    }
    
    private SQLRouteResult createSQLRouteResultWithoutChange() {
        SelectStatement selectStatement = new SelectStatement();
        SQLRouteResult result = new SQLRouteResult(new SelectSQLStatementContext(selectStatement, 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList()), new PaginationContext(null, null, Collections.emptyList())), 
                new ShardingConditions(Collections.<ShardingCondition>emptyList()), null);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteTableName() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(
                createRouteResultForTableName(), "SELECT table_x.id, x.name FROM table_x x WHERE table_x.id=? AND x.name=?", Arrays.<Object>asList(1, "x"));
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT table_1.id, x.name FROM table_1 x WHERE table_1.id=? AND x.name=?"));
    }
    
    private SQLRouteResult createRouteResultForTableName() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(new TableSegment(7, 13, "table_x"));
        selectStatement.getAllSQLSegments().add(new TableSegment(31, 37, "table_x"));
        selectStatement.getAllSQLSegments().add(new TableSegment(47, 53, "table_x"));
        SQLRouteResult result = new SQLRouteResult(new SelectSQLStatementContext(selectStatement,  
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList()), new PaginationContext(null, null, Collections.emptyList())),
                new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteOrderByAndGroupByDerivedColumns() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(
                createRouteResultForOrderByAndGroupByDerivedColumns(), "SELECT x.age FROM table_x x GROUP BY x.id ORDER BY x.name", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is(
                "SELECT x.age , x.id AS GROUP_BY_DERIVED_0 , x.name AS ORDER_BY_DERIVED_0 FROM table_1 x GROUP BY x.id ORDER BY x.name"));
    }
    
    private SQLRouteResult createRouteResultForOrderByAndGroupByDerivedColumns() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(new TableSegment(18, 24, "table_x"));
        DerivedProjection derivedProjection1 = new DerivedProjection("x.id", "GROUP_BY_DERIVED_0");
        DerivedProjection derivedProjection2 = new DerivedProjection("x.name", "ORDER_BY_DERIVED_0");
        ProjectionsContext projectionsContext = new ProjectionsContext(6, 11, false, Arrays.<Projection>asList(derivedProjection1, derivedProjection2));
        SQLRouteResult result = new SQLRouteResult(new SelectSQLStatementContext(selectStatement,  
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                projectionsContext, new PaginationContext(null, null, Collections.emptyList())),
                new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteAggregationDerivedColumns() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForAggregationDerivedColumns(), "SELECT AVG(x.age) FROM table_x x", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is(
                "SELECT AVG(x.age) , COUNT(x.age) AS AVG_DERIVED_COUNT_0 , SUM(x.age) AS AVG_DERIVED_SUM_0 FROM table_1 x"));
    }
    
    private SQLRouteResult createRouteResultForAggregationDerivedColumns() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(new TableSegment(23, 29, "table_x"));
        AggregationProjection countProjection = new AggregationProjection(AggregationType.COUNT, "(x.age)", "AVG_DERIVED_COUNT_0");
        AggregationProjection sumProjection = new AggregationProjection(AggregationType.SUM, "(x.age)", "AVG_DERIVED_SUM_0");
        AggregationProjection avgProjection = new AggregationProjection(AggregationType.AVG, "(x.age)", null);
        avgProjection.getDerivedAggregationProjections().add(countProjection);
        avgProjection.getDerivedAggregationProjections().add(sumProjection);
        ProjectionsContext projectionsContext = new ProjectionsContext(6, 16, false, Collections.<Projection>singletonList(avgProjection));
        SQLRouteResult result = new SQLRouteResult(new SelectSQLStatementContext(selectStatement, 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                projectionsContext, new PaginationContext(null, null, Collections.emptyList())),
                new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteAutoGeneratedKeyColumn() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForAutoGeneratedKeyColumn(), "INSERT INTO table_x (name, age) VALUES (?, ?)", Arrays.<Object>asList("Bill", 1));
        assertThat(rewriteEngine.generateSQL(routingUnit, logicTableAndActualTables).getSql(), is("INSERT INTO table_1 (name, age, id) VALUES (?, ?, ?)"));
    }
    
    private SQLRouteResult createRouteResultForAutoGeneratedKeyColumn() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setTable(new TableSegment(0, 0, "table_x"));
        insertStatement.getColumns().add(new ColumnSegment(0, 0, "name"));
        insertStatement.getColumns().add(new ColumnSegment(0, 0, "age"));
        insertStatement.getAllSQLSegments().add(new InsertColumnsSegment(30, 30, Collections.singleton(mock(ColumnSegment.class))));
        insertStatement.getAllSQLSegments().add(new InsertValuesSegment(39, 44, 
                Arrays.<ExpressionSegment>asList(new ParameterMarkerExpressionSegment(0, 0, 0), new ParameterMarkerExpressionSegment(0, 0, 1))));
        insertStatement.getValues().add(new InsertValuesSegment(39, 44, 
                Arrays.<ExpressionSegment>asList(new ParameterMarkerExpressionSegment(0, 0, 0), new ParameterMarkerExpressionSegment(0, 0, 1))));
        insertStatement.getAllSQLSegments().add(new TableSegment(12, 18, "table_x"));
        InsertSQLStatementContext insertSQLStatementContext = new InsertSQLStatementContext(null, Arrays.<Object>asList("x", 1), insertStatement);
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getDataNodes().add(new DataNode("db0.table_1"));
        GeneratedKey generatedKey = new GeneratedKey("id", true);
        generatedKey.getGeneratedValues().add(1);
        SQLRouteResult result = new SQLRouteResult(insertSQLStatementContext, new ShardingConditions(Collections.singletonList(shardingCondition)), generatedKey);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteAutoGeneratedKeyColumnWithoutColumnsWithParameter() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(
                createRouteResultForAutoGeneratedKeyColumnWithoutColumnsWithParameter(), "INSERT INTO `table_x` VALUES (?)", Arrays.<Object>asList("Bill", 1));
        assertThat(rewriteEngine.generateSQL(routingUnit, logicTableAndActualTables).getSql(), is("INSERT INTO `table_1`(name, id) VALUES (?, ?)"));
    }
    
    private SQLRouteResult createRouteResultForAutoGeneratedKeyColumnWithoutColumnsWithParameter() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.getAllSQLSegments().add(new InsertColumnsSegment(21, 21, Collections.<ColumnSegment>emptyList()));
        insertStatement.getAllSQLSegments().add(new InsertValuesSegment(29, 31, Collections.<ExpressionSegment>singletonList(new ParameterMarkerExpressionSegment(0, 0, 0))));
        insertStatement.getValues().add(new InsertValuesSegment(29, 31, Collections.<ExpressionSegment>singletonList(new ParameterMarkerExpressionSegment(0, 0, 0))));
        insertStatement.setTable(new TableSegment(0, 0, "table_x"));
        insertStatement.getAllSQLSegments().add(new TableSegment(12, 20, "`table_x`"));
        TableMetas tableMetas = mock(TableMetas.class);
        when(tableMetas.getAllColumnNames("table_x")).thenReturn(Arrays.asList("name", "id"));
        InsertSQLStatementContext insertSQLStatementContext = new InsertSQLStatementContext(tableMetas, Collections.<Object>singletonList("Bill"), insertStatement);
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getDataNodes().add(new DataNode("db0.table_1"));
        GeneratedKey generatedKey = new GeneratedKey("id", true);
        generatedKey.getGeneratedValues().add(1);
        SQLRouteResult result = new SQLRouteResult(insertSQLStatementContext, new ShardingConditions(Collections.singletonList(shardingCondition)), generatedKey);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteAutoGeneratedKeyColumnWithoutColumnsWithoutParameter() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(
                createRouteResultForAutoGeneratedKeyColumnWithoutColumnsWithoutParameter(), "INSERT INTO `table_x` VALUES (10)", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(routingUnit, logicTableAndActualTables).getSql(), is("INSERT INTO `table_1`(name, id) VALUES (10, 1)"));
    }
    
    private SQLRouteResult createRouteResultForAutoGeneratedKeyColumnWithoutColumnsWithoutParameter() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.getAllSQLSegments().add(new InsertColumnsSegment(21, 21, Collections.<ColumnSegment>emptyList()));
        insertStatement.getAllSQLSegments().add(new InsertValuesSegment(29, 32, Collections.<ExpressionSegment>singletonList(new LiteralExpressionSegment(0, 0, 10))));
        insertStatement.getValues().add(new InsertValuesSegment(29, 32, Collections.<ExpressionSegment>singletonList(new LiteralExpressionSegment(0, 0, 10))));
        insertStatement.setTable(new TableSegment(0, 0, "table_x"));
        insertStatement.getAllSQLSegments().add(new TableSegment(12, 20, "`table_x`"));
        TableMetas tableMetas = mock(TableMetas.class);
        when(tableMetas.getAllColumnNames("table_x")).thenReturn(Arrays.asList("name", "id"));
        InsertSQLStatementContext insertSQLStatementContext = new InsertSQLStatementContext(tableMetas, Collections.emptyList(), insertStatement);
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getDataNodes().add(new DataNode("db0.table_1"));
        GeneratedKey generatedKey = new GeneratedKey("id", true);
        generatedKey.getGeneratedValues().add(1);
        SQLRouteResult result = new SQLRouteResult(insertSQLStatementContext, new ShardingConditions(Collections.singletonList(shardingCondition)), generatedKey);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteDuplicateKeyWithoutColumnsWithoutParameter() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForDuplicateKeyWithoutColumnsWithoutParameter(),
                "INSERT INTO `table_x` VALUES (10) ON DUPLICATE KEY UPDATE name = VALUES(name)", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(routingUnit, logicTableAndActualTables).getSql(), is("INSERT INTO `table_1`(name, id) VALUES (10, 1) ON DUPLICATE KEY UPDATE name = VALUES(name)"));
    }
    
    private SQLRouteResult createRouteResultForDuplicateKeyWithoutColumnsWithoutParameter() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.getAllSQLSegments().add(new InsertColumnsSegment(21, 21, Collections.<ColumnSegment>emptyList()));
        insertStatement.getAllSQLSegments().add(new InsertValuesSegment(29, 32, Collections.<ExpressionSegment>singletonList(new LiteralExpressionSegment(0, 0, 10))));
        insertStatement.getValues().add(new InsertValuesSegment(29, 32, Collections.<ExpressionSegment>singletonList(new LiteralExpressionSegment(0, 0, 10))));
        insertStatement.setTable(new TableSegment(0, 0, "table_x"));
        insertStatement.getAllSQLSegments().add(new TableSegment(12, 20, "`table_x`"));
        TableMetas tableMetas = mock(TableMetas.class);
        when(tableMetas.getAllColumnNames("table_x")).thenReturn(Arrays.asList("name", "id"));
        InsertSQLStatementContext insertSQLStatementContext = new InsertSQLStatementContext(tableMetas, Collections.emptyList(), insertStatement);
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getDataNodes().add(new DataNode("db0.table_1"));
        GeneratedKey generatedKey = new GeneratedKey("id", true);
        generatedKey.getGeneratedValues().add(1);
        SQLRouteResult result = new SQLRouteResult(insertSQLStatementContext, new ShardingConditions(Collections.singletonList(shardingCondition)), generatedKey);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteDuplicateKeyWithSetWithoutParameter() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForDuplicateKeyWithSetWithoutParameter(),
                "INSERT INTO `table_x` set name = 10, id = 1 ON DUPLICATE KEY UPDATE name = VALUES(name)", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(routingUnit, logicTableAndActualTables).getSql(), is("INSERT INTO `table_1` set name = 10, id = 1 ON DUPLICATE KEY UPDATE name = VALUES(name)"));
    }
    
    private SQLRouteResult createRouteResultForDuplicateKeyWithSetWithoutParameter() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.getColumns().add(new ColumnSegment(0, 0, "name"));
        insertStatement.getColumns().add(new ColumnSegment(0, 0, "id"));
        insertStatement.setTable(new TableSegment(0, 0, "table_x"));
        insertStatement.getAllSQLSegments().add(new TableSegment(12, 20, "`table_x`"));
        InsertSQLStatementContext insertSQLStatementContext = new InsertSQLStatementContext(null, Collections.emptyList(), insertStatement);
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getDataNodes().add(new DataNode("db0.table_1"));
        SQLRouteResult result = new SQLRouteResult(insertSQLStatementContext, new ShardingConditions(Collections.singletonList(shardingCondition)));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteColumnWithoutColumnsWithoutParameter() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForColumnWithoutColumnsWithoutParameter(), "INSERT INTO `table_x` VALUES (10, 1)", Arrays.<Object>asList("x", 1));
        assertThat(rewriteEngine.generateSQL(routingUnit, logicTableAndActualTables).getSql(), is("INSERT INTO `table_1`(name, id) VALUES (10, 1)"));
    }
    
    private SQLRouteResult createRouteResultForColumnWithoutColumnsWithoutParameter() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setTable(new TableSegment(0, 0, "table_x"));
        insertStatement.getAllSQLSegments().add(new TableSegment(12, 20, "`table_x`"));
        insertStatement.getAllSQLSegments().add(new InsertColumnsSegment(21, 21, Collections.<ColumnSegment>emptyList()));
        insertStatement.getAllSQLSegments().add(new InsertValuesSegment(29, 35, Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(0, 0, 10), new LiteralExpressionSegment(0, 0, 1))));
        insertStatement.getValues().add(new InsertValuesSegment(29, 35, Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(0, 0, 10), new LiteralExpressionSegment(0, 0, 1))));
        TableMetas tableMetas = mock(TableMetas.class);
        when(tableMetas.getAllColumnNames("table_x")).thenReturn(Arrays.asList("name", "id"));
        InsertSQLStatementContext insertSQLStatementContext = new InsertSQLStatementContext(tableMetas, Collections.emptyList(), insertStatement);
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getDataNodes().add(new DataNode("db0.table_1"));
        SQLRouteResult result = new SQLRouteResult(insertSQLStatementContext, new ShardingConditions(Collections.singletonList(shardingCondition)));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteColumnWithoutColumnsWithParameter() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForColumnWithoutColumnsWithParameter(), "INSERT INTO `table_x` VALUES (?, ?)", Arrays.<Object>asList("x", 1));
        assertThat(rewriteEngine.generateSQL(routingUnit, logicTableAndActualTables).getSql(), is("INSERT INTO `table_1`(name, id) VALUES (?, ?)"));
    }
    
    private SQLRouteResult createRouteResultForColumnWithoutColumnsWithParameter() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.getAllSQLSegments().add(new InsertColumnsSegment(21, 21, Collections.<ColumnSegment>emptyList()));
        insertStatement.getAllSQLSegments().add(
                new InsertValuesSegment(29, 34, Arrays.<ExpressionSegment>asList(new ParameterMarkerExpressionSegment(0, 0, 0), new ParameterMarkerExpressionSegment(0, 0, 1))));
        insertStatement.getValues().add(
                new InsertValuesSegment(29, 34, Arrays.<ExpressionSegment>asList(new ParameterMarkerExpressionSegment(0, 0, 0), new ParameterMarkerExpressionSegment(0, 0, 1))));
        insertStatement.setTable(new TableSegment(0, 0, "table_x"));
        insertStatement.getAllSQLSegments().add(new TableSegment(12, 20, "`table_x`"));
        TableMetas tableMetas = mock(TableMetas.class);
        when(tableMetas.getAllColumnNames("table_x")).thenReturn(Arrays.asList("name", "id"));
        InsertSQLStatementContext insertSQLStatementContext = new InsertSQLStatementContext(tableMetas, Arrays.<Object>asList("x", 1), insertStatement);
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getDataNodes().add(new DataNode("db0.table_1"));
        GeneratedKey generatedKey = new GeneratedKey("id", false);
        generatedKey.getGeneratedValues().add(1);
        SQLRouteResult result = new SQLRouteResult(insertSQLStatementContext, new ShardingConditions(Collections.singletonList(shardingCondition)), generatedKey);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteLimit() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForLimit(), "SELECT x.id FROM table_x x LIMIT 2, 2", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT x.id FROM table_1 x LIMIT 0, 4"));
    }
    
    private SQLRouteResult createRouteResultForLimit() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(new TableSegment(17, 23, "table_x"));
        SQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(selectStatement, 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false), 
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList()),
                new PaginationContext(new NumberLiteralLimitValueSegment(33, 33, 2), new NumberLiteralLimitValueSegment(36, 36, 2), Collections.emptyList()));
        SQLRouteResult result = new SQLRouteResult(selectSQLStatementContext, new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteRowNumber() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForRowNumber(),
                "SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_x x) row_ WHERE rownum<=4) t WHERE t.rownum_>2", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(),
                is("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_1 x) row_ WHERE rownum<=4) t WHERE t.rownum_>0"));
    }
    
    private SQLRouteResult createRouteResultForRowNumber() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(new TableSegment(68, 74, "table_x"));
        SQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(selectStatement, 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false), 
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList()),
                new PaginationContext(new NumberLiteralRowNumberValueSegment(119, 119, 2, true), new NumberLiteralRowNumberValueSegment(98, 98, 4, false), Collections.emptyList()));
        SQLRouteResult result = new SQLRouteResult(selectSQLStatementContext, new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteTopAndRowNumber() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForTopAndRowNumber(),
                "SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_x x) AS row_ WHERE row_.rownum_>2", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(),
                is("SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_1 x) AS row_ WHERE row_.rownum_>0"));
    }
    
    private SQLRouteResult createRouteResultForTopAndRowNumber() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(new TableSegment(85, 91, "table_x"));
        SQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(selectStatement, 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false), 
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList()),
                new PaginationContext(new NumberLiteralRowNumberValueSegment(123, 123, 2, true), new NumberLiteralRowNumberValueSegment(26, 26, 4, false), Collections.emptyList()));
        SQLRouteResult result = new SQLRouteResult(selectSQLStatementContext, new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteLimitForMemoryGroupBy() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForLimitForMemoryGroupBy(), "SELECT x.id FROM table_x x LIMIT 2, 2", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT x.id FROM table_1 x LIMIT 0, 2147483647"));
    }
    
    private SQLRouteResult createRouteResultForLimitForMemoryGroupBy() {
        SelectStatement selectStatement = new SelectStatement();
        ColumnSegment columnSegment = new ColumnSegment(0, 0, "id");
        columnSegment.setOwner(new TableSegment(0, 0, "x"));
        selectStatement.getAllSQLSegments().add(new TableSegment(17, 23, "table_x"));
        SQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(selectStatement, 
                new GroupByContext(Collections.singletonList(new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment, OrderDirection.DESC, OrderDirection.ASC))), 0),
                new OrderByContext(Collections.singletonList(new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment, OrderDirection.ASC, OrderDirection.ASC))), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList()),
                new PaginationContext(new NumberLiteralLimitValueSegment(33, 33, 2), new NumberLiteralLimitValueSegment(36, 36, 2), Collections.emptyList()));
        SQLRouteResult result = new SQLRouteResult(selectSQLStatementContext, new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteRowNumForMemoryGroupBy() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForRowNumForMemoryGroupBy(), 
                "SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_x x) row_ WHERE rownum<=4) t WHERE t.rownum_>2", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(),
                is("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_1 x) row_ WHERE rownum<=2147483647) t WHERE t.rownum_>0"));
    }
    
    private SQLRouteResult createRouteResultForRowNumForMemoryGroupBy() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(new TableSegment(68, 74, "table_x"));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, "id");
        columnSegment.setOwner(new TableSegment(0, 0, "x"));
        SQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(selectStatement,  
                new GroupByContext(Collections.singletonList(new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment, OrderDirection.DESC, OrderDirection.ASC))), 0),
                new OrderByContext(Collections.singletonList(new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment, OrderDirection.ASC, OrderDirection.ASC))), false), 
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList()),
                new PaginationContext(new NumberLiteralRowNumberValueSegment(119, 119, 2, true), new NumberLiteralRowNumberValueSegment(98, 98, 4, false), Collections.emptyList()));
        SQLRouteResult result = new SQLRouteResult(selectSQLStatementContext, new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteTopAndRowNumberForMemoryGroupBy() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForTopAndRowNumberForMemoryGroupBy(), 
                "SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_x x) AS row_ WHERE row_.rownum_>2", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(),
                is("SELECT * FROM (SELECT TOP(2147483647) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_1 x) AS row_ WHERE row_.rownum_>0"));
    }
    
    private SQLRouteResult createRouteResultForTopAndRowNumberForMemoryGroupBy() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(new TableSegment(85, 91, "table_x"));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, "id");
        columnSegment.setOwner(new TableSegment(0, 0, "x"));
        SQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(selectStatement, 
                new GroupByContext(Collections.singletonList(new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment, OrderDirection.DESC, OrderDirection.ASC))), 0),
                new OrderByContext(Collections.singletonList(new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment, OrderDirection.ASC, OrderDirection.ASC))), false), 
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList()),
                new PaginationContext(new NumberLiteralRowNumberValueSegment(123, 123, 2, false), new NumberLiteralRowNumberValueSegment(26, 26, 4, false), Collections.emptyList()));
        SQLRouteResult result = new SQLRouteResult(selectSQLStatementContext, new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteLimitForNotRewritePagination() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForLimitForNotRewritePagination(), "SELECT x.id FROM table_x x LIMIT 2, 2", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT x.id FROM table_1 x LIMIT 2, 2"));
    }
    
    private SQLRouteResult createRouteResultForLimitForNotRewritePagination() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(new TableSegment(17, 23, "table_x"));
        SQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(selectStatement, 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false), 
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList()),
                new PaginationContext(new NumberLiteralLimitValueSegment(33, 33, 2), new NumberLiteralLimitValueSegment(36, 36, 2), Collections.emptyList()));
        SQLRouteResult result = new SQLRouteResult(selectSQLStatementContext, new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteRowNumForNotRewritePagination() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForRowNumForNotRewritePagination(), 
                "SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_x x) row_ WHERE rownum<=4) t WHERE t.rownum_>2", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(),
                is("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_1 x) row_ WHERE rownum<=4) t WHERE t.rownum_>2"));
    }
    
    private SQLRouteResult createRouteResultForRowNumForNotRewritePagination() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(new TableSegment(68, 74, "table_x"));
        SQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(selectStatement, 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false), 
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList()),
                new PaginationContext(new NumberLiteralRowNumberValueSegment(119, 119, 2, true), new NumberLiteralRowNumberValueSegment(98, 98, 4, false), Collections.emptyList()));
        SQLRouteResult result = new SQLRouteResult(selectSQLStatementContext, new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteTopAndRowNumberForNotRewritePagination() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(
                createRouteResultForTopAndRowNumberForNotRewritePagination(), 
                "SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_x x) AS row_ WHERE row_.rownum_>2", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(),
                is("SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_1 x) AS row_ WHERE row_.rownum_>2"));
    }
    
    private SQLRouteResult createRouteResultForTopAndRowNumberForNotRewritePagination() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(new TableSegment(85, 91, "table_x"));
        SQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(selectStatement, 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false), 
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList()),
                new PaginationContext(new NumberLiteralRowNumberValueSegment(123, 123, 2, true), new NumberLiteralRowNumberValueSegment(26, 26, 4, false), Collections.emptyList()));
        SQLRouteResult result = new SQLRouteResult(selectSQLStatementContext, new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteDerivedOrderBy() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForDerivedOrderBy(), "SELECT x.id, x.name FROM table_x x GROUP BY x.id, x.name DESC", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT x.id, x.name FROM table_1 x GROUP BY x.id, x.name DESC ORDER BY x.id ASC,x.name DESC "));
    }
    
    private SQLRouteResult createRouteResultForDerivedOrderBy() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(new TableSegment(25, 31, "table_x"));
        ColumnSegment columnSegment1 = new ColumnSegment(0, 0, "id");
        columnSegment1.setOwner(new TableSegment(0, 0, "x"));
        ColumnSegment columnSegment2 = new ColumnSegment(0, 0, "name");
        columnSegment2.setOwner(new TableSegment(0, 0, "x"));
        SQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(selectStatement, 
                new GroupByContext(Arrays.asList(
                        new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment1, OrderDirection.ASC, OrderDirection.ASC)),
                        new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment2, OrderDirection.DESC, OrderDirection.ASC))), 60),
                new OrderByContext(Arrays.asList(
                        new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment1, OrderDirection.ASC, OrderDirection.ASC)),
                        new OrderByItem(new ColumnOrderByItemSegment(0, 0, columnSegment2, OrderDirection.DESC, OrderDirection.ASC))), true),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList()), new PaginationContext(null, null, Collections.emptyList()));
        SQLRouteResult result = new SQLRouteResult(selectSQLStatementContext, new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteIndexTokenForIndexNameTableName() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForIndexTokenForIndexNameTableName(), "CREATE INDEX index_name ON table_x ('column')", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("CREATE INDEX index_name_table_1 ON table_1 ('column')"));
    }
    
    private SQLRouteResult createRouteResultForIndexTokenForIndexNameTableName() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(new IndexSegment(13, 22, "index_name", QuoteCharacter.NONE));
        selectStatement.getAllSQLSegments().add(new TableSegment(27, 33, "table_x"));
        SQLRouteResult result = new SQLRouteResult(new SelectSQLStatementContext(selectStatement, 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList()), new PaginationContext(null, null, Collections.emptyList())),
                new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteIndexTokenForIndexNameTableNameWithoutLogicTableName() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(
                createRouteResultForIndexTokenForIndexNameTableNameWithoutLogicTableName(), "CREATE INDEX logic_index ON table_x ('column')", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("CREATE INDEX logic_index_table_1 ON table_1 ('column')"));
    }
    
    private SQLRouteResult createRouteResultForIndexTokenForIndexNameTableNameWithoutLogicTableName() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(new IndexSegment(13, 23, "logic_index", QuoteCharacter.NONE));
        selectStatement.getAllSQLSegments().add(new TableSegment(28, 34, "table_x"));
        SQLRouteResult result = new SQLRouteResult(new SelectSQLStatementContext(selectStatement, 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList()), new PaginationContext(null, null, Collections.emptyList())),
                new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithoutBackQuoteForShow() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForTableTokenWithoutBackQuoteForShow(), "SHOW COLUMNS FROM table_x", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM table_1"));
    }
    
    private SQLRouteResult createRouteResultForTableTokenWithoutBackQuoteForShow() {
        DALStatement showTablesStatement = new DALStatement();
        showTablesStatement.getAllSQLSegments().add(new TableSegment(18, 24, "table_x"));
        SQLRouteResult result = new SQLRouteResult(new CommonSQLStatementContext(showTablesStatement), new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithoutBackQuoteFromSchemaForShow() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(
                createRouteResultForTableTokenWithoutBackQuoteFromSchemaForShow(), "SHOW COLUMNS FROM table_x FROM 'sharding_db'", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, Collections.singletonMap("table_x", "table_x")).getSql(), is("SHOW COLUMNS FROM table_x"));
    }
    
    private SQLRouteResult createRouteResultForTableTokenWithoutBackQuoteFromSchemaForShow() {
        DALStatement showTablesStatement = new DALStatement();
        showTablesStatement.getAllSQLSegments().add(new FromSchemaSegment(25, 43));
        showTablesStatement.getAllSQLSegments().add(new TableSegment(18, 24, "table_x"));
        SQLRouteResult result = new SQLRouteResult(new CommonSQLStatementContext(showTablesStatement), new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithBackQuoteForShow() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createRouteResultForTableTokenWithBackQuoteForShow(), "SHOW COLUMNS FROM `table_x`", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM `table_1`"));
    }
    
    private SQLRouteResult createRouteResultForTableTokenWithBackQuoteForShow() {
        DALStatement showTablesStatement = new DALStatement();
        showTablesStatement.getAllSQLSegments().add(new TableSegment(18, 26, "`table_x`"));
        SQLRouteResult result = new SQLRouteResult(new CommonSQLStatementContext(showTablesStatement), new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithBackQuoteFromSchemaForShow() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(
                createSQLRouteResultForTableTokenWithBackQuoteFromSchemaForShow(), "SHOW COLUMNS FROM `table_x` FROM 'sharding_db'", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM `table_1` FROM 'sharding_db'"));
    }
    
    private SQLRouteResult createSQLRouteResultForTableTokenWithBackQuoteFromSchemaForShow() {
        DALStatement showTablesStatement = new DALStatement();
        showTablesStatement.getAllSQLSegments().add(new TableSegment(18, 26, "`table_x`"));
        SQLRouteResult result = new SQLRouteResult(new CommonSQLStatementContext(showTablesStatement), new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithSchemaForShow() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForTableTokenWithSchemaForShow(), "SHOW COLUMNS FROM sharding_db.table_x", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM table_1"));
    }
    
    private SQLRouteResult createSQLRouteResultForTableTokenWithSchemaForShow() {
        DALStatement showTablesStatement = new DALStatement();
        showTablesStatement.getAllSQLSegments().add(new TableSegment(18, 36, "table_x"));
        SQLRouteResult result = new SQLRouteResult(new CommonSQLStatementContext(showTablesStatement), new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithSchemaFromSchemaForShow() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(
                createSQLRouteResultForTableTokenWithSchemaFromSchemaForShow(), "SHOW COLUMNS FROM sharding_db.table_x FROM sharding_db", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM table_1 FROM sharding_db"));
    }
    
    private SQLRouteResult createSQLRouteResultForTableTokenWithSchemaFromSchemaForShow() {
        DALStatement showTablesStatement = new DALStatement();
        showTablesStatement.getAllSQLSegments().add(new TableSegment(18, 36, "table_x"));
        SQLRouteResult result = new SQLRouteResult(new CommonSQLStatementContext(showTablesStatement), new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithBackQuoteWithSchemaForShow() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForTableTokenWithBackQuoteWithSchemaForShow(), "SHOW COLUMNS FROM sharding_db.`table_x`", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM `table_1`"));
    }
    
    private SQLRouteResult createSQLRouteResultForTableTokenWithBackQuoteWithSchemaForShow() {
        DALStatement showTablesStatement = new DALStatement();
        showTablesStatement.getAllSQLSegments().add(new TableSegment(18, 38, "`table_x`"));
        SQLRouteResult result = new SQLRouteResult(new CommonSQLStatementContext(showTablesStatement), new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithBackQuoteWithSchemaFromSchemaForShow() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(
                createSQLRouteResultForTableTokenWithBackQuoteWithSchemaFromSchemaForShow(), "SHOW COLUMNS FROM sharding_db.`table_x` FROM sharding_db", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM `table_1` FROM sharding_db"));
    }
    
    private SQLRouteResult createSQLRouteResultForTableTokenWithBackQuoteWithSchemaFromSchemaForShow() {
        DALStatement showTablesStatement = new DALStatement();
        showTablesStatement.getAllSQLSegments().add(new TableSegment(18, 38, "`table_x`"));
        SQLRouteResult result = new SQLRouteResult(new CommonSQLStatementContext(showTablesStatement), new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithSchemaWithBackQuoteForShow() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(
                createSQLRouteResultForTableTokenWithSchemaWithBackQuoteForShow(), "SHOW COLUMNS FROM `sharding_db`.`table_x`", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM `table_1`"));
    }
    
    private SQLRouteResult createSQLRouteResultForTableTokenWithSchemaWithBackQuoteForShow() {
        DALStatement showTablesStatement = new DALStatement();
        showTablesStatement.getAllSQLSegments().add(new TableSegment(18, 40, "`table_x`"));
        SQLRouteResult result = new SQLRouteResult(new CommonSQLStatementContext(showTablesStatement), new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithSchemaWithBackQuoteFromSchemaForShow() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(
                createSQLRouteResultForTableTokenWithSchemaWithBackQuoteFromSchemaForShow(), "SHOW COLUMNS FROM `sharding_db`.`table_x` FROM sharding_db", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SHOW COLUMNS FROM `table_1` FROM sharding_db"));
    }
    
    private SQLRouteResult createSQLRouteResultForTableTokenWithSchemaWithBackQuoteFromSchemaForShow() {
        DALStatement showTablesStatement = new DALStatement();
        showTablesStatement.getAllSQLSegments().add(new TableSegment(18, 40, "`table_x`"));
        SQLRouteResult result = new SQLRouteResult(new CommonSQLStatementContext(showTablesStatement), new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithSchemaForSelect() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForTableTokenWithSchemaForSelect(), "SELECT * FROM sharding_db.table_x", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT * FROM table_1"));
    }
    
    private SQLRouteResult createSQLRouteResultForTableTokenWithSchemaForSelect() {
        SelectStatement selectStatement = new SelectStatement();
        TableSegment tableSegment = new TableSegment(14, 32, "table_x");
        tableSegment.setOwner(new SchemaSegment(14, 24, "sharding_db"));
        selectStatement.getAllSQLSegments().add(tableSegment);
        SQLRouteResult result = new SQLRouteResult(new SelectSQLStatementContext(selectStatement, 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList()), new PaginationContext(null, null, Collections.emptyList())), 
                new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithSchemaForInsert() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(
                createSQLRouteResultForTableTokenWithSchemaForInsert(), "INSERT INTO sharding_db.table_x (order_id, user_id, status) values (1, 1, 'OK')", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(
                null, logicTableAndActualTables).getSql(), is("INSERT INTO table_1 (order_id, user_id, status) values (1, 1, 'OK')"));
    }
    
    private SQLRouteResult createSQLRouteResultForTableTokenWithSchemaForInsert() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.getAllSQLSegments().add(new TableSegment(12, 30, "table_x"));
        insertStatement.getColumns().add(new ColumnSegment(33, 41, "order_id"));
        insertStatement.getColumns().add(new ColumnSegment(43, 50, "user_id"));
        insertStatement.getColumns().add(new ColumnSegment(52, 58, "status"));
        InsertSQLStatementContext insertSQLStatementContext = new InsertSQLStatementContext(null, Collections.emptyList(), insertStatement);
        SQLRouteResult result = new SQLRouteResult(insertSQLStatementContext, new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithSchemaForUpdate() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(
                createSQLRouteResultForTableTokenWithSchemaForUpdate(), "UPDATE `sharding_db`.table_x SET user_id=1 WHERE order_id=1", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("UPDATE table_1 SET user_id=1 WHERE order_id=1"));
    }
    
    private SQLRouteResult createSQLRouteResultForTableTokenWithSchemaForUpdate() {
        UpdateStatement updateStatement = new UpdateStatement();
        updateStatement.getAllSQLSegments().add(new TableSegment(7, 27, "table_x"));
        updateStatement.setSetAssignment(
                new SetAssignmentsSegment(28, 42, Collections.singleton(new AssignmentSegment(33, 42, new ColumnSegment(33, 40, "id"), new LiteralExpressionSegment(41, 42, 1)))));
        SQLRouteResult result = new SQLRouteResult(new CommonSQLStatementContext(updateStatement), new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteTableTokenWithSchemaForDelete() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForTableTokenWithSchemaForDelete(), "DELETE FROM `sharding_db`.`table_x` WHERE user_id=1", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("DELETE FROM `table_1` WHERE user_id=1"));
    }
    
    private SQLRouteResult createSQLRouteResultForTableTokenWithSchemaForDelete() {
        DeleteStatement deleteStatement = new DeleteStatement();
        deleteStatement.getAllSQLSegments().add(new TableSegment(12, 34, "`table_x`"));
        SQLRouteResult result = new SQLRouteResult(new CommonSQLStatementContext(deleteStatement), new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    @Test
    public void assertRewriteSelectInWithShardingEncryptorWithCipher() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForSelectInWithShardingEncryptor(), "SELECT id FROM table_z WHERE id in (3,5)", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT cipher FROM table_z WHERE cipher IN ('encryptValue', 'encryptValue')"));
    }
    
    @Test
    public void assertRewriteSelectInWithShardingEncryptorWithPlain() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForSelectInWithShardingEncryptor(), "SELECT id FROM table_z WHERE id in (3,5)", Collections.emptyList(), false);
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT plain FROM table_z WHERE plain IN ('3', '5')"));
    }
    
    private SQLRouteResult createSQLRouteResultForSelectInWithShardingEncryptor() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(new TableSegment(15, 21, "table_z"));
        SelectItemsSegment selectItemsSegment = new SelectItemsSegment(7, 8, false);
        selectItemsSegment.getSelectItems().add(new ColumnSelectItemSegment("id", new ColumnSegment(7, 8, "id")));
        selectStatement.getAllSQLSegments().add(selectItemsSegment);
        AndPredicate andPredicate = new AndPredicate();
        andPredicate.getPredicates().add(new PredicateSegment(29, 40, new ColumnSegment(29, 31, "id"),
                new PredicateInRightValue(Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(36, 37, 3), new LiteralExpressionSegment(38, 39, 5)))));
        WhereSegment whereSegment = new WhereSegment(23, 51, 2);
        whereSegment.getAndPredicates().add(andPredicate);
        selectStatement.setWhere(whereSegment);
        SQLRouteResult result = new SQLRouteResult(new SelectSQLStatementContext(selectStatement,
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList()), new PaginationContext(null, null, Collections.emptyList())),
                new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteSelectInWithQueryAssistedShardingEncryptorWithQuery() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(
                createSQLRouteResultForSelectInWithQueryAssistedShardingEncryptor(), "SELECT id, name FROM table_k WHERE id in (3,5)", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(),
                is("SELECT cipher, name FROM table_k WHERE query IN ('assistedEncryptValue', 'assistedEncryptValue')"));
    }
    
    private SQLRouteResult createSQLRouteResultForSelectInWithQueryAssistedShardingEncryptor() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(new TableSegment(21, 27, "table_k"));
        SelectItemsSegment selectItemsSegment = new SelectItemsSegment(7, 14, false);
        selectItemsSegment.getSelectItems().add(new ColumnSelectItemSegment("id", new ColumnSegment(7, 8, "id")));
        selectStatement.getAllSQLSegments().add(selectItemsSegment);
        AndPredicate andPredicate = new AndPredicate();
        andPredicate.getPredicates().add(new PredicateSegment(35, 45, new ColumnSegment(35, 37, "id"),
                new PredicateInRightValue(Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(42, 43, 3), new LiteralExpressionSegment(44, 45, 5)))));
        WhereSegment whereSegment = new WhereSegment(29, 46, 0);
        whereSegment.getAndPredicates().add(andPredicate);
        selectStatement.setWhere(whereSegment);
        SQLRouteResult result = new SQLRouteResult(new SelectSQLStatementContext(selectStatement, 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList()), new PaginationContext(null, null, Collections.emptyList())),
                new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteUpdateWithShardingEncryptor() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(createSQLRouteResultForUpdateWithShardingEncryptor(), "UPDATE table_z SET id = 1 WHERE id = 2", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("UPDATE table_z SET plain = 1, cipher = 'encryptValue' WHERE cipher = 'encryptValue'"));
    }
    
    private SQLRouteResult createSQLRouteResultForUpdateWithShardingEncryptor() {
        UpdateStatement updateStatement = new UpdateStatement();
        updateStatement.getAllSQLSegments().add(new TableSegment(7, 13, "table_z"));
        updateStatement.setSetAssignment(
                new SetAssignmentsSegment(15, 24, Collections.singleton(new AssignmentSegment(19, 24, new ColumnSegment(19, 20, "id"), new LiteralExpressionSegment(0, 0, 1)))));
        AndPredicate andPredicate = new AndPredicate();
        andPredicate.getPredicates().add(new PredicateSegment(32, 37, new ColumnSegment(32, 34, "id"), new PredicateCompareRightValue("=", new LiteralExpressionSegment(37, 38, 2))));
        WhereSegment whereSegment = new WhereSegment(26, 38, 0);
        whereSegment.getAndPredicates().add(andPredicate);
        updateStatement.setWhere(whereSegment);
        SQLRouteResult result = new SQLRouteResult(new CommonSQLStatementContext(updateStatement), new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteInsertWithQueryAssistedShardingEncryptor() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(
                createSQLRouteResultForInsertWithQueryAssistedShardingEncryptor(), "INSERT INTO `table_w` set name = 10 ON DUPLICATE KEY UPDATE name = VALUES(name)", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(routingUnit, logicTableAndActualTables).getSql(),
                is("INSERT INTO `table_w` set cipher = 'encryptValue', id = 1, query = 'assistedEncryptValue', plain = 10 ON DUPLICATE KEY UPDATE name = VALUES(name)"));
    }
    
    private SQLRouteResult createSQLRouteResultForInsertWithQueryAssistedShardingEncryptor() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.getAllSQLSegments().add(new TableSegment(12, 20, "`table_w`"));
        insertStatement.setTable(new TableSegment(0, 0, "table_w"));
        insertStatement.getColumns().add(new ColumnSegment(0, 0, "name"));
        ColumnSegment columnSegment = new ColumnSegment(26, 29, "name");
        LiteralExpressionSegment expressionSegment = new LiteralExpressionSegment(33, 34, 10);
        insertStatement.getAllSQLSegments().add(new SetAssignmentsSegment(26, 34, Collections.singleton(new AssignmentSegment(26, 34, columnSegment, expressionSegment))));
        insertStatement.setSetAssignment(new SetAssignmentsSegment(26, 34, Collections.singleton(new AssignmentSegment(26, 34, columnSegment, expressionSegment))));
        InsertSQLStatementContext insertSQLStatementContext = new InsertSQLStatementContext(null, Collections.emptyList(), insertStatement);
        insertSQLStatementContext.getInsertValueContexts().get(0).appendValue(1, ShardingDerivedColumnType.KEY_GEN);
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getDataNodes().add(new DataNode("db0.table_1"));
        SQLRouteResult result = new SQLRouteResult(insertSQLStatementContext, new ShardingConditions(Collections.singletonList(shardingCondition)));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteSelectInWithAggregationDistinct() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(
                createSQLRouteResultForSelectInWithAggregationDistinct(), "SELECT COUNT(DISTINCT id), SUM(DISTINCT id) FROM table_z WHERE id in (3,5)", Collections.emptyList());
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT DISTINCT id, id FROM table_z WHERE id in (3,5)"));
    }
    
    private SQLRouteResult createSQLRouteResultForSelectInWithAggregationDistinct() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(new TableSegment(49, 55, "table_z"));
        Projection projection1 = new AggregationDistinctProjection(7, 24, AggregationType.COUNT, "(DISTINCT id)", "a", "id");
        Projection projection2 = new AggregationDistinctProjection(27, 42, AggregationType.SUM, "(DISTINCT id)", "a", "id");
        ProjectionsContext projectionsContext = new ProjectionsContext(7, 42, true, Arrays.asList(projection1, projection2));
        SQLRouteResult result = new SQLRouteResult(new SelectSQLStatementContext(selectStatement, 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                projectionsContext, new PaginationContext(null, null, Collections.emptyList())), 
                new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteSelectEqualWithShardingEncryptorWithCipher() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(
                createSQLRouteResultForSelectEqualWithShardingEncryptor(), "SELECT id FROM table_z WHERE id=? AND name=?", Arrays.<Object>asList(1, "x"));
        assertThat(rewriteEngine.generateSQL().getSql(), is("SELECT cipher FROM table_z WHERE cipher = ? AND name=?"));
        assertThat(getParameterBuilder(rewriteEngine).getParameters(Arrays.<Object>asList(1, "x")).get(0), is((Object) "encryptValue"));
    }
    
    @Test
    public void assertRewriteSelectEqualWithShardingEncryptorWithPlain() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(
                createSQLRouteResultForSelectEqualWithShardingEncryptor(), "SELECT id FROM table_z WHERE id=? AND name=?", Arrays.<Object>asList(1, "x"), false);
        assertThat(rewriteEngine.generateSQL().getSql(), is("SELECT plain FROM table_z WHERE plain = ? AND name=?"));
        assertThat(getParameterBuilder(rewriteEngine).getParameters(Arrays.<Object>asList(1, "x")).get(0), is((Object) 1));
    }
    
    private SQLRouteResult createSQLRouteResultForSelectEqualWithShardingEncryptor() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(new TableSegment(15, 21, "table_z"));
        SelectItemsSegment selectItemsSegment = new SelectItemsSegment(7, 8, false);
        selectItemsSegment.getSelectItems().add(new ColumnSelectItemSegment("id", new ColumnSegment(7, 8, "id")));
        selectStatement.getAllSQLSegments().add(selectItemsSegment);
        AndPredicate andPredicate = new AndPredicate();
        andPredicate.getPredicates().add(new PredicateSegment(29, 32, new ColumnSegment(29, 31, "id"), new PredicateCompareRightValue("=", new ParameterMarkerExpressionSegment(32, 33, 0))));
        WhereSegment whereSegment = new WhereSegment(23, 44, 2);
        whereSegment.getAndPredicates().add(andPredicate);
        selectStatement.setWhere(whereSegment);
        SQLRouteResult result = new SQLRouteResult(new SelectSQLStatementContext(selectStatement,
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList()), new PaginationContext(null, null, Collections.emptyList())),
                new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteSelectInWithShardingEncryptorWithParameterWithCipher() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(
                createSQLRouteResultForSelectInWithShardingEncryptorWithParameter(), "SELECT id FROM table_z WHERE id in (?, ?) or id = 3", Arrays.<Object>asList(1, 2));
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT cipher FROM table_z WHERE cipher IN (?, ?) or cipher = 'encryptValue'"));
        assertThat(getParameterBuilder(rewriteEngine).getParameters(Arrays.<Object>asList(1, 2)).get(0), is((Object) "encryptValue"));
        assertThat(getParameterBuilder(rewriteEngine).getParameters(Arrays.<Object>asList(1, 2)).get(1), is((Object) "encryptValue"));
    }
    
    @Test
    public void assertRewriteSelectInWithShardingEncryptorWithParameterWithPlain() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(
                createSQLRouteResultForSelectInWithShardingEncryptorWithParameter(), "SELECT id FROM table_z WHERE id in (?, ?) or id = 3", Arrays.<Object>asList(1, 2), false);
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT plain FROM table_z WHERE plain IN (?, ?) or plain = '3'"));
        assertThat(getParameterBuilder(rewriteEngine).getParameters(Arrays.<Object>asList(1, 2)).get(0), is((Object) 1));
        assertThat(getParameterBuilder(rewriteEngine).getParameters(Arrays.<Object>asList(1, 2)).get(1), is((Object) 2));
    }
    
    private SQLRouteResult createSQLRouteResultForSelectInWithShardingEncryptorWithParameter() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(new TableSegment(15, 21, "table_z"));
        SelectItemsSegment selectItemsSegment = new SelectItemsSegment(7, 8, false);
        selectItemsSegment.getSelectItems().add(new ColumnSelectItemSegment("id", new ColumnSegment(7, 8, "id")));
        selectStatement.getAllSQLSegments().add(selectItemsSegment);
        AndPredicate andPredicate = new AndPredicate();
        andPredicate.getPredicates().add(new PredicateSegment(29, 40, new ColumnSegment(29, 31, "id"), 
                new PredicateInRightValue(Arrays.<ExpressionSegment>asList(new ParameterMarkerExpressionSegment(36, 37, 0), new ParameterMarkerExpressionSegment(39, 40, 1)))));
        andPredicate.getPredicates().add(new PredicateSegment(45, 50, new ColumnSegment(45, 47, "id"),
                new PredicateCompareRightValue("=", new LiteralExpressionSegment(50, 51, 3))));
        WhereSegment whereSegment = new WhereSegment(23, 51, 2);
        whereSegment.getAndPredicates().add(andPredicate);
        selectStatement.setWhere(whereSegment);
        SQLRouteResult result = new SQLRouteResult(new SelectSQLStatementContext(selectStatement, 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList()), new PaginationContext(null, null, Collections.emptyList())),
                new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteSelectEqualWithQueryAssistedShardingEncryptor() {
        SQLRewriteEngine rewriteEngine = createSQLRewriteEngine(
                createSQLRouteResultForSelectEqualWithQueryAssistedShardingEncryptor(), "SELECT id as alias FROM table_k WHERE id=? AND name=?", Arrays.<Object>asList(1, "k"));
        assertThat(rewriteEngine.generateSQL(null, logicTableAndActualTables).getSql(), is("SELECT cipher as alias FROM table_k WHERE query = ? AND name=?"));
        assertThat(getParameterBuilder(rewriteEngine).getParameters(Arrays.<Object>asList(1, "k")).get(0), is((Object) "assistedEncryptValue"));
    }
    
    private SQLRouteResult createSQLRouteResultForSelectEqualWithQueryAssistedShardingEncryptor() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getAllSQLSegments().add(new TableSegment(24, 30, "table_k"));
        SelectItemsSegment selectItemsSegment = new SelectItemsSegment(7, 8, false);
        ColumnSelectItemSegment columnSelectItemSegment = new ColumnSelectItemSegment("id", new ColumnSegment(7, 8, "id"));
        columnSelectItemSegment.setAlias("alias");
        selectItemsSegment.getSelectItems().add(columnSelectItemSegment);
        selectStatement.getAllSQLSegments().add(selectItemsSegment);
        AndPredicate andPredicate = new AndPredicate();
        andPredicate.getPredicates().add(new PredicateSegment(38, 41, new ColumnSegment(38, 40, "id"), new PredicateCompareRightValue("=", new ParameterMarkerExpressionSegment(41, 42, 0))));
        WhereSegment whereSegment = new WhereSegment(32, 53, 2);
        whereSegment.getAndPredicates().add(andPredicate);
        selectStatement.setWhere(whereSegment);
        SQLRouteResult result = new SQLRouteResult(new SelectSQLStatementContext(selectStatement, 
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList()), new PaginationContext(null, null, Collections.emptyList())),
                new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    private SQLRewriteEngine createSQLRewriteEngine(final SQLRouteResult routeResult, final String sql, final List<Object> parameters) {
        return createSQLRewriteEngine(routeResult, sql, parameters, true);
    }
    
    private SQLRewriteEngine createSQLRewriteEngine(final SQLRouteResult routeResult, final String sql, final List<Object> parameters, final boolean isQueryWithCipherColumn) {
        return new SQLRewriteEngine(shardingRule, null, routeResult, sql, parameters, isQueryWithCipherColumn);
    }
    
    @SneakyThrows
    private StandardParameterBuilder getParameterBuilder(final SQLRewriteEngine rewriteEngine) {
        Field field = rewriteEngine.getClass().getDeclaredField("parameterBuilder");
        field.setAccessible(true);
        return (StandardParameterBuilder) field.get(rewriteEngine);
    }
}
