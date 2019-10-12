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

package org.apache.shardingsphere.core.rewrite.feature.sharding;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.core.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.parse.SQLParseEngine;
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
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dal.DALStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.preprocessor.segment.insert.expression.DerivedLiteralExpressionSegment;
import org.apache.shardingsphere.core.preprocessor.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.core.preprocessor.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.core.preprocessor.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.core.preprocessor.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.core.preprocessor.segment.select.projection.Projection;
import org.apache.shardingsphere.core.preprocessor.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.core.preprocessor.statement.SQLStatementContext;
import org.apache.shardingsphere.core.preprocessor.statement.impl.CommonSQLStatementContext;
import org.apache.shardingsphere.core.preprocessor.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.core.preprocessor.statement.impl.SelectSQLStatementContext;
import org.apache.shardingsphere.core.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.core.rewrite.engine.SQLRewriteResult;
import org.apache.shardingsphere.core.rewrite.feature.encrypt.context.EncryptSQLRewriteContextDecorator;
import org.apache.shardingsphere.core.rewrite.feature.sharding.context.ShardingSQLRewriteContextDecorator;
import org.apache.shardingsphere.core.rewrite.feature.sharding.engine.ShardingSQLRewriteEngine;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.router.sharding.ShardingRouter;
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
    
    private Map<String, String> logicAndActualTables = Collections.singletonMap("table_x", "table_1");
    
    private SQLParseEngine parseEngine;
    
    @Before
    public void setUp() throws IOException {
        shardingRule = createShardingRule();
        routingUnit = createRoutingUnit();
        parseEngine = new SQLParseEngine(DatabaseTypes.getActualDatabaseType("MySQL"));
    }
    
    private ShardingRule createShardingRule() throws IOException {
        URL url = ShardingSQLRewriteEngineTest.class.getClassLoader().getResource("yaml/sharding-rewrite-rule.yaml");
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
        SQLRewriteResult actual = getSQLRewriteResult("SELECT table_y.id FROM table_y WHERE table_y.id=?", Collections.<Object>singletonList(1), true);
        assertThat(actual.getSql(), is("SELECT table_y.id FROM table_y WHERE table_y.id=?"));
        assertThat(actual.getParameters(), is(Collections.<Object>singletonList(1)));
    }
    
    @Test
    public void assertRewriteTableName() {
        // TODO case maybe incorrect
//        SQLRewriteResult actual = getSQLRewriteResult("SELECT table_x.id, x.name FROM table_x x WHERE table_x.id=? AND x.name=?", Arrays.<Object>asList(1, "x"), true);
//        assertThat(actual.getSql(), is("SELECT table_1.id, x.name FROM table_1 x WHERE table_1.id=? AND x.name=?"));
//        assertThat(actual.getParameters(), is(Arrays.<Object>asList(1, "x")));
        SQLRouteResult sqlRouteResult = createRouteResultForTableName();
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(sqlRouteResult, "SELECT table_x.id, x.name FROM table_x x WHERE table_x.id=? AND x.name=?", Arrays.<Object>asList(1, "x"));
        assertThat(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), null, logicAndActualTables).rewrite(sqlRewriteContext).getSql(),
                is("SELECT table_1.id, x.name FROM table_1 x WHERE table_1.id=? AND x.name=?"));
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
        SQLRewriteResult actual = getSQLRewriteResult("SELECT x.age FROM table_x x GROUP BY x.id ORDER BY x.name", Collections.emptyList(), true);
        assertThat(actual.getSql(), is("SELECT x.age , x.id AS GROUP_BY_DERIVED_0 , x.name AS ORDER_BY_DERIVED_0 FROM table_1 x GROUP BY x.id ORDER BY x.name"));
        assertThat(actual.getParameters(), is(Collections.emptyList()));
    }
    
    @Test
    public void assertRewriteAggregationDerivedColumns() {
        SQLRewriteResult actual = getSQLRewriteResult("SELECT AVG(x.age) FROM table_x x", Collections.emptyList(), true);
        assertThat(actual.getSql(), is("SELECT AVG(x.age) , COUNT(x.age) AS AVG_DERIVED_COUNT_0 , SUM(x.age) AS AVG_DERIVED_SUM_0 FROM table_1 x"));
        assertThat(actual.getParameters(), is(Collections.emptyList()));
    }
    
    @Test
    public void assertRewriteAutoGeneratedKeyColumn() {
        // TODO case maybe incorrect
//        SQLRewriteResult actual = getSQLRewriteResult("INSERT INTO table_x (name, age) VALUES (?, ?)", Collections.<Object>singletonList("Bill"), true);
//        assertThat(actual.getSql(), is("INSERT INTO table_1 (name, age, id) VALUES (?, ?, ?)"));
//        assertThat(actual.getParameters(), is(Arrays.<Object>asList("Bill", 1)));
        SQLRouteResult sqlRouteResult = createRouteResultForAutoGeneratedKeyColumn();
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(sqlRouteResult, "INSERT INTO table_x (name, age) VALUES (?, ?)", Arrays.<Object>asList("Bill", 1));
        assertThat(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), routingUnit, logicAndActualTables).rewrite(sqlRewriteContext).getSql(), 
                is("INSERT INTO table_1 (name, age, id) VALUES (?, ?, ?)"));
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
        ShardingConditions shardingConditions = new ShardingConditions(Collections.singletonList(shardingCondition));
        GeneratedKey generatedKey = new GeneratedKey("id", true);
        generatedKey.getGeneratedValues().add(1);
        SQLRouteResult result = new SQLRouteResult(insertSQLStatementContext, shardingConditions, generatedKey);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteAutoGeneratedKeyColumnWithoutColumnsWithParameter() {
        // TODO case maybe incorrect
//        SQLRewriteResult actual = getSQLRewriteResult("INSERT INTO `table_x` VALUES (?)", Collections.<Object>singletonList("Bill"), true);
//        assertThat(actual.getSql(), is("INSERT INTO `table_1`(name, id) VALUES (?, ?)"));
//        assertThat(actual.getParameters(), is(Arrays.<Object>asList("Bill", 1)));
        SQLRouteResult sqlRouteResult = createRouteResultForAutoGeneratedKeyColumnWithoutColumnsWithParameter();
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(sqlRouteResult, "INSERT INTO `table_x` VALUES (?)", Arrays.<Object>asList("Bill", 1));
        assertThat(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), routingUnit, logicAndActualTables).rewrite(sqlRewriteContext).getSql(), 
                is("INSERT INTO `table_1`(name, id) VALUES (?, ?)"));
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
        // TODO case maybe incorrect
//        SQLRewriteResult actual = getSQLRewriteResult("INSERT INTO `table_x` VALUES (10)", Collections.emptyList(), true);
//        assertThat(actual.getSql(), is("INSERT INTO `table_1`(name, id) VALUES (10, 1)"));
//        assertThat(actual.getParameters(), is(Collections.emptyList()));
        SQLRouteResult sqlRouteResult = createRouteResultForAutoGeneratedKeyColumnWithoutColumnsWithoutParameter();
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(sqlRouteResult, "INSERT INTO `table_x` VALUES (10)", Collections.emptyList());
        assertThat(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), routingUnit, logicAndActualTables).rewrite(sqlRewriteContext).getSql(), 
                is("INSERT INTO `table_1`(name, id) VALUES (10, 1)"));
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
        // TODO case maybe incorrect
//        SQLRewriteResult actual = getSQLRewriteResult("INSERT INTO `table_x` VALUES (10) ON DUPLICATE KEY UPDATE name = VALUES(name)", Collections.emptyList(), true);
//        assertThat(actual.getSql(), is("INSERT INTO `table_1`(name, id) VALUES (10, 1) ON DUPLICATE KEY UPDATE name = VALUES(name)"));
//        assertThat(actual.getParameters(), is(Collections.emptyList()));
        SQLRouteResult sqlRouteResult = createRouteResultForDuplicateKeyWithoutColumnsWithoutParameter();
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(sqlRouteResult, "INSERT INTO `table_x` VALUES (10) ON DUPLICATE KEY UPDATE name = VALUES(name)", Collections.emptyList());
        assertThat(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), routingUnit, logicAndActualTables).rewrite(sqlRewriteContext).getSql(), 
                is("INSERT INTO `table_1`(name, id) VALUES (10, 1) ON DUPLICATE KEY UPDATE name = VALUES(name)"));
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
        // TODO case maybe incorrect
//        SQLRewriteResult actual = getSQLRewriteResult("INSERT INTO `table_x` set name = 10, id = 1 ON DUPLICATE KEY UPDATE name = VALUES(name)", Collections.emptyList(), true);
//        assertThat(actual.getSql(), is("INSERT INTO `table_1` set name = 10, id = 1 ON DUPLICATE KEY UPDATE name = VALUES(name)"));
//        assertThat(actual.getParameters(), is(Collections.emptyList()));
        SQLRouteResult sqlRouteResult = createRouteResultForDuplicateKeyWithSetWithoutParameter();
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(sqlRouteResult,
                "INSERT INTO `table_x` set name = 10, id = 1 ON DUPLICATE KEY UPDATE name = VALUES(name)", Collections.emptyList());
        assertThat(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), routingUnit, logicAndActualTables).rewrite(sqlRewriteContext).getSql(), 
                is("INSERT INTO `table_1` set name = 10, id = 1 ON DUPLICATE KEY UPDATE name = VALUES(name)"));
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
        // TODO case maybe incorrect
//        SQLRewriteResult actual = getSQLRewriteResult("INSERT INTO `table_x` VALUES (10, 1)", Collections.emptyList(), true);
//        assertThat(actual.getSql(), is("INSERT INTO `table_1`(name, id) VALUES (10, 1)"));
//        assertThat(actual.getParameters(), is(Collections.emptyList()));
        SQLRouteResult sqlRouteResult = createRouteResultForColumnWithoutColumnsWithoutParameter();
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(
                sqlRouteResult, "INSERT INTO `table_x` VALUES (10, 1)", Arrays.<Object>asList("x", 1));
        assertThat(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), routingUnit, logicAndActualTables).rewrite(sqlRewriteContext).getSql(), 
                is("INSERT INTO `table_1`(name, id) VALUES (10, 1)"));
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
        // TODO case maybe incorrect
//        SQLRewriteResult actual = getSQLRewriteResult("INSERT INTO `table_x` VALUES (?, ?)", Arrays.<Object>asList("x", 1), true);
//        assertThat(actual.getSql(), is("INSERT INTO `table_1`(name, id) VALUES (?, ?)"));
//        assertThat(actual.getParameters(), is(Arrays.<Object>asList("x", 1)));
        SQLRouteResult sqlRouteResult = createRouteResultForColumnWithoutColumnsWithParameter();
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(sqlRouteResult, "INSERT INTO `table_x` VALUES (?, ?)", Arrays.<Object>asList("x", 1));
        assertThat(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), routingUnit, logicAndActualTables).rewrite(sqlRewriteContext).getSql(), 
                is("INSERT INTO `table_1`(name, id) VALUES (?, ?)"));
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
        SQLRewriteResult actual = getSQLRewriteResult("SELECT x.id FROM table_x x LIMIT 2, 2", Collections.emptyList(), true);
        assertThat(actual.getSql(), is("SELECT x.id FROM table_1 x LIMIT 0, 4"));
        assertThat(actual.getParameters(), is(Collections.emptyList()));
    }
    
    @Test
    public void assertRewriteRowNumber() {
        // TODO case maybe incorrect
//        SQLRewriteResult actual = getSQLRewriteResult("SELECT * FROM 
        // (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_x x) row_ WHERE rownum<=4) t WHERE t.rownum_>2", Collections.emptyList(), true);
//        assertThat(actual.getSql(), is("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_1 x) row_ WHERE rownum<=4) t WHERE t.rownum_>0"));
//        assertThat(actual.getParameters(), is(Collections.emptyList()));
        SQLRouteResult sqlRouteResult = createRouteResultForRowNumber();
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(sqlRouteResult,
                "SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_x x) row_ WHERE rownum<=4) t WHERE t.rownum_>2", Collections.emptyList());
        assertThat(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), null, logicAndActualTables).rewrite(sqlRewriteContext).getSql(),
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
        // TODO case maybe incorrect
//        SQLRewriteResult actual = getSQLRewriteResult("SELECT * FROM (SELECT TOP(4) row_number() 
        // OVER (ORDER BY x.id) AS rownum_, x.id FROM table_x x) AS row_ WHERE row_.rownum_>2", Collections.emptyList(), true);
//        assertThat(actual.getSql(), is("SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_1 x) AS row_ WHERE row_.rownum_>0"));
//        assertThat(actual.getParameters(), is(Collections.emptyList()));
        SQLRouteResult sqlRouteResult = createRouteResultForTopAndRowNumber();
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(createRouteResultForTopAndRowNumber(),
                "SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_x x) AS row_ WHERE row_.rownum_>2", Collections.emptyList());
        assertThat(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), null, logicAndActualTables).rewrite(sqlRewriteContext).getSql(),
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
        // TODO case maybe incorrect
//        SQLRewriteResult actual = getSQLRewriteResult("SELECT x.id FROM table_x x LIMIT 2, 2", Collections.emptyList(), true);
//        assertThat(actual.getSql(), is("SELECT x.id FROM table_1 x LIMIT 0, 2147483647"));
//        assertThat(actual.getParameters(), is(Collections.emptyList()));
        SQLRouteResult sqlRouteResult = createRouteResultForLimitForMemoryGroupBy();
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(sqlRouteResult, "SELECT x.id FROM table_x x LIMIT 2, 2", Collections.emptyList());
        assertThat(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), null, logicAndActualTables).rewrite(sqlRewriteContext).getSql(), 
                is("SELECT x.id FROM table_1 x LIMIT 0, 2147483647"));
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
        // TODO case maybe incorrect
//        SQLRewriteResult actual = getSQLRewriteResult("SELECT * FROM 
        // (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_x x) row_ WHERE rownum<=4) t WHERE t.rownum_>2", Collections.emptyList(), true);
//        assertThat(actual.getSql(), is("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_1 x) row_ WHERE rownum<=2147483647) t WHERE t.rownum_>0"));
//        assertThat(actual.getParameters(), is(Collections.emptyList()));
        SQLRouteResult sqlRouteResult = createRouteResultForRowNumForMemoryGroupBy();
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(sqlRouteResult, 
                "SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_x x) row_ WHERE rownum<=4) t WHERE t.rownum_>2", Collections.emptyList());
        assertThat(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), null, logicAndActualTables).rewrite(sqlRewriteContext).getSql(),
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
        // TODO case maybe incorrect
//        SQLRewriteResult actual = getSQLRewriteResult("SELECT * FROM (SELECT TOP(4) row_number() 
        // OVER (ORDER BY x.id) AS rownum_, x.id FROM table_x x) AS row_ WHERE row_.rownum_>2", Collections.emptyList(), true);
//        assertThat(actual.getSql(), is("SELECT * FROM (SELECT TOP(2147483647) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_1 x) AS row_ WHERE row_.rownum_>0"));
//        assertThat(actual.getParameters(), is(Collections.emptyList()));
        SQLRouteResult sqlRouteResult = createRouteResultForTopAndRowNumberForMemoryGroupBy();
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(sqlRouteResult, 
                "SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_x x) AS row_ WHERE row_.rownum_>2", Collections.emptyList());
        assertThat(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), null, logicAndActualTables).rewrite(sqlRewriteContext).getSql(),
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
        // TODO case maybe incorrect
//        SQLRewriteResult actual = getSQLRewriteResult("SELECT x.id FROM table_x x LIMIT 2, 2", Collections.emptyList(), true);
//        assertThat(actual.getSql(), is("SELECT x.id FROM table_1 x LIMIT 2, 2"));
//        assertThat(actual.getParameters(), is(Collections.emptyList()));
        SQLRouteResult sqlRouteResult = createRouteResultForLimitForNotRewritePagination();
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(sqlRouteResult, "SELECT x.id FROM table_x x LIMIT 2, 2", Collections.emptyList());
        assertThat(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), null, logicAndActualTables).rewrite(sqlRewriteContext).getSql(), 
                is("SELECT x.id FROM table_1 x LIMIT 2, 2"));
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
        // TODO case maybe incorrect
//        SQLRewriteResult actual = getSQLRewriteResult("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM 
        // (SELECT x.id FROM table_x x) row_ WHERE rownum<=4) t WHERE t.rownum_>2", Collections.emptyList(), true);
//        assertThat(actual.getSql(), is("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_1 x) row_ WHERE rownum<=4) t WHERE t.rownum_>2"));
//        assertThat(actual.getParameters(), is(Collections.emptyList()));
        SQLRouteResult sqlRouteResult = createRouteResultForRowNumForNotRewritePagination();
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(sqlRouteResult, 
                "SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_x x) row_ WHERE rownum<=4) t WHERE t.rownum_>2", Collections.emptyList());
        assertThat(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), null, logicAndActualTables).rewrite(sqlRewriteContext).getSql(),
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
        // TODO case maybe incorrect
//        SQLRewriteResult actual = getSQLRewriteResult("SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_x x) AS 
        // row_ WHERE row_.rownum_>2", Collections.emptyList(), true);
//        assertThat(actual.getSql(), is("SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_1 x) AS row_ WHERE row_.rownum_>2"));
//        assertThat(actual.getParameters(), is(Collections.emptyList()));
        SQLRouteResult sqlRouteResult = createRouteResultForTopAndRowNumberForNotRewritePagination();
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(sqlRouteResult, 
                "SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_x x) AS row_ WHERE row_.rownum_>2", Collections.emptyList());
        assertThat(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), null, logicAndActualTables).rewrite(sqlRewriteContext).getSql(),
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
        SQLRewriteResult actual = getSQLRewriteResult("SELECT x.id, x.name FROM table_x x GROUP BY x.id, x.name DESC", Collections.emptyList(), true);
        assertThat(actual.getSql(), is("SELECT x.id, x.name FROM table_1 x GROUP BY x.id, x.name DESC ORDER BY x.id ASC,x.name DESC "));
        assertThat(actual.getParameters(), is(Collections.emptyList()));
    }
    
    @Test
    public void assertRewriteIndexTokenForIndexNameTableName() {
        // TODO case maybe incorrect
//        SQLRewriteResult actual = getSQLRewriteResult("CREATE INDEX index_name ON table_x ('column')", Collections.emptyList(), true);
//        assertThat(actual.getSql(), is("CREATE INDEX index_name_table_1 ON table_1 ('column')"));
//        assertThat(actual.getParameters(), is(Collections.emptyList()));
        SQLRouteResult sqlRouteResult = createRouteResultForIndexTokenForIndexNameTableName();
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(sqlRouteResult, "CREATE INDEX index_name ON table_x ('column')", Collections.emptyList());
        assertThat(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), null, logicAndActualTables).rewrite(sqlRewriteContext).getSql(), 
                is("CREATE INDEX index_name_table_1 ON table_1 ('column')"));
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
        // TODO case maybe incorrect
//        SQLRewriteResult actual = getSQLRewriteResult("CREATE INDEX logic_index ON table_x ('column')", Collections.emptyList(), true);
//        assertThat(actual.getSql(), is("CREATE INDEX logic_index_table_1 ON table_1 ('column')"));
//        assertThat(actual.getParameters(), is(Collections.emptyList()));
        SQLRouteResult sqlRouteResult = createRouteResultForIndexTokenForIndexNameTableNameWithoutLogicTableName();
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(sqlRouteResult, "CREATE INDEX logic_index ON table_x ('column')", Collections.emptyList());
        assertThat(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), null, logicAndActualTables).rewrite(sqlRewriteContext).getSql(), 
                is("CREATE INDEX logic_index_table_1 ON table_1 ('column')"));
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
        SQLRewriteResult actual = getSQLRewriteResult("SHOW COLUMNS FROM table_x", Collections.emptyList(), true);
        assertThat(actual.getSql(), is("SHOW COLUMNS FROM table_1"));
        assertThat(actual.getParameters(), is(Collections.emptyList()));
    }
    
    @Test
    public void assertRewriteTableTokenWithoutBackQuoteFromSchemaForShow() {
        // TODO case maybe incorrect
//        SQLRewriteResult actual = getSQLRewriteResult("SHOW COLUMNS FROM table_x FROM 'sharding_db'", Collections.emptyList(), true);
//        assertThat(actual.getSql(), is("SHOW COLUMNS FROM table_x"));
//        assertThat(actual.getParameters(), is(Collections.emptyList()));
        SQLRouteResult sqlRouteResult = createRouteResultForTableTokenWithoutBackQuoteFromSchemaForShow();
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(sqlRouteResult, "SHOW COLUMNS FROM table_x FROM 'sharding_db'", Collections.emptyList());
        assertThat(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), null, Collections.singletonMap("table_x", "table_x")).rewrite(sqlRewriteContext).getSql(), 
                is("SHOW COLUMNS FROM table_x"));
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
        SQLRewriteResult actual = getSQLRewriteResult("SHOW COLUMNS FROM `table_x`", Collections.emptyList(), true);
        assertThat(actual.getSql(), is("SHOW COLUMNS FROM `table_1`"));
        assertThat(actual.getParameters(), is(Collections.emptyList()));
    }
    
    @Test
    public void assertRewriteTableTokenWithBackQuoteFromSchemaForShow() {
        // TODO case maybe incorrect
//        SQLRewriteResult actual = getSQLRewriteResult("SHOW COLUMNS FROM `table_x` FROM 'sharding_db'", Collections.emptyList(), true);
//        assertThat(actual.getSql(), is("SHOW COLUMNS FROM `table_1` FROM 'sharding_db'"));
//        assertThat(actual.getParameters(), is(Collections.emptyList()));
        SQLRouteResult sqlRouteResult = createSQLRouteResultForTableTokenWithBackQuoteFromSchemaForShow();
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(sqlRouteResult, "SHOW COLUMNS FROM `table_x` FROM 'sharding_db'", Collections.emptyList());
        assertThat(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), null, logicAndActualTables).rewrite(sqlRewriteContext).getSql(),
                is("SHOW COLUMNS FROM `table_1` FROM 'sharding_db'"));
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
        SQLRewriteResult actual = getSQLRewriteResult("SHOW COLUMNS FROM sharding_db.table_x", Collections.emptyList(), true);
        assertThat(actual.getSql(), is("SHOW COLUMNS FROM table_1"));
        assertThat(actual.getParameters(), is(Collections.emptyList()));
    }
    
    @Test
    public void assertRewriteTableTokenWithSchemaFromSchemaForShow() {
        // TODO case maybe incorrect
//        SQLRewriteResult actual = getSQLRewriteResult("SHOW COLUMNS FROM sharding_db.table_x FROM sharding_db", Collections.emptyList(), true);
//        assertThat(actual.getSql(), is("SHOW COLUMNS FROM table_1 FROM sharding_db"));
//        assertThat(actual.getParameters(), is(Collections.emptyList()));
        SQLRouteResult sqlRouteResult = createSQLRouteResultForTableTokenWithSchemaFromSchemaForShow();
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(sqlRouteResult, "SHOW COLUMNS FROM sharding_db.table_x FROM sharding_db", Collections.emptyList());
        assertThat(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), null, logicAndActualTables).rewrite(sqlRewriteContext).getSql(), 
                is("SHOW COLUMNS FROM table_1 FROM sharding_db"));
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
        SQLRewriteResult actual = getSQLRewriteResult("SHOW COLUMNS FROM sharding_db.`table_x`", Collections.emptyList(), true);
        assertThat(actual.getSql(), is("SHOW COLUMNS FROM `table_1`"));
        assertThat(actual.getParameters(), is(Collections.emptyList()));
    }
    
    @Test
    public void assertRewriteTableTokenWithBackQuoteWithSchemaFromSchemaForShow() {
        // TODO case maybe incorrect
//        SQLRewriteResult actual = getSQLRewriteResult("SHOW COLUMNS FROM sharding_db.`table_x` FROM sharding_db", Collections.emptyList(), true);
//        assertThat(actual.getSql(), is("SHOW COLUMNS FROM `table_1` FROM sharding_db"));
//        assertThat(actual.getParameters(), is(Collections.emptyList()));
        SQLRouteResult sqlRouteResult = createSQLRouteResultForTableTokenWithBackQuoteWithSchemaFromSchemaForShow();
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(sqlRouteResult, "SHOW COLUMNS FROM sharding_db.`table_x` FROM sharding_db", Collections.emptyList());
        assertThat(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), null, logicAndActualTables).rewrite(sqlRewriteContext).getSql(), 
                is("SHOW COLUMNS FROM `table_1` FROM sharding_db"));
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
        SQLRewriteResult actual = getSQLRewriteResult("SHOW COLUMNS FROM `sharding_db`.`table_x`", Collections.emptyList(), true);
        assertThat(actual.getSql(), is("SHOW COLUMNS FROM `table_1`"));
        assertThat(actual.getParameters(), is(Collections.emptyList()));
    }
    
    @Test
    public void assertRewriteTableTokenWithSchemaWithBackQuoteFromSchemaForShow() {
        // TODO case maybe incorrect
//        SQLRewriteResult actual = getSQLRewriteResult("SHOW COLUMNS FROM `sharding_db`.`table_x` FROM sharding_db", Collections.emptyList(), true);
//        assertThat(actual.getSql(), is("SHOW COLUMNS FROM `table_1` FROM sharding_db"));
//        assertThat(actual.getParameters(), is(Collections.emptyList()));
        SQLRouteResult sqlRouteResult = createSQLRouteResultForTableTokenWithSchemaWithBackQuoteFromSchemaForShow();
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(sqlRouteResult, "SHOW COLUMNS FROM `sharding_db`.`table_x` FROM sharding_db", Collections.emptyList());
        assertThat(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), null, logicAndActualTables).rewrite(sqlRewriteContext).getSql(), 
                is("SHOW COLUMNS FROM `table_1` FROM sharding_db"));
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
        SQLRewriteResult actual = getSQLRewriteResult("SELECT * FROM sharding_db.table_x", Collections.emptyList(), true);
        assertThat(actual.getSql(), is("SELECT * FROM table_1"));
        assertThat(actual.getParameters(), is(Collections.emptyList()));
    }
    
    @Test
    public void assertRewriteTableTokenWithSchemaForInsert() {
        // TODO case maybe incorrect
//        SQLRewriteResult actual = getSQLRewriteResult("INSERT INTO sharding_db.table_x (order_id, user_id, status) values (1, 1, 'OK')", Collections.emptyList(), true);
//        assertThat(actual.getSql(), is("INSERT INTO table_1 (order_id, user_id, status) values (1, 1, 'OK')"));
//        assertThat(actual.getParameters(), is(Collections.emptyList()));
        SQLRouteResult sqlRouteResult = createSQLRouteResultForTableTokenWithSchemaForInsert();
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(sqlRouteResult, "INSERT INTO sharding_db.table_x (order_id, user_id, status) values (1, 1, 'OK')", Collections.emptyList());
        assertThat(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), null, logicAndActualTables).rewrite(sqlRewriteContext).getSql(), 
                is("INSERT INTO table_1 (order_id, user_id, status) values (1, 1, 'OK')"));
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
        SQLRewriteResult actual = getSQLRewriteResult("UPDATE `sharding_db`.table_x SET user_id=1 WHERE order_id=1", Collections.emptyList(), true);
        assertThat(actual.getSql(), is("UPDATE table_1 SET user_id=1 WHERE order_id=1"));
        assertThat(actual.getParameters(), is(Collections.emptyList()));
    }
    
    @Test
    public void assertRewriteTableTokenWithSchemaForDelete() {
        SQLRewriteResult actual = getSQLRewriteResult("DELETE FROM `sharding_db`.`table_x` WHERE user_id=1", Collections.emptyList(), true);
        assertThat(actual.getSql(), is("DELETE FROM `table_1` WHERE user_id=1"));
        assertThat(actual.getParameters(), is(Collections.emptyList()));
    }
    
    @Test
    public void assertRewriteSelectInWithShardingEncryptorWithCipher() {
        SQLRewriteResult actual = getSQLRewriteResult("SELECT id FROM table_z WHERE id in (3,5)", Collections.emptyList(), true);
        assertThat(actual.getSql(), is("SELECT cipher FROM table_z WHERE cipher IN ('encryptValue', 'encryptValue')"));
        assertThat(actual.getParameters(), is(Collections.emptyList()));
    }
    
    @Test
    public void assertRewriteSelectInWithShardingEncryptorWithPlain() {
        SQLRewriteResult actual = getSQLRewriteResult("SELECT id FROM table_z WHERE id in (3,5)", Collections.emptyList(), false);
        assertThat(actual.getSql(), is("SELECT plain FROM table_z WHERE plain IN ('3', '5')"));
        assertThat(actual.getParameters(), is(Collections.emptyList()));
    }
    
    @Test
    public void assertRewriteSelectInWithQueryAssistedShardingEncryptorWithQuery() {
        SQLRewriteResult actual = getSQLRewriteResult("SELECT id, name FROM table_k WHERE id in (3,5)", Collections.emptyList(), true);
        assertThat(actual.getSql(), is("SELECT cipher, name FROM table_k WHERE query IN ('assistedEncryptValue', 'assistedEncryptValue')"));
        assertThat(actual.getParameters(), is(Collections.emptyList()));
    }
    
    @Test
    public void assertRewriteUpdateWithShardingEncryptor() {
        SQLRewriteResult actual = getSQLRewriteResult("UPDATE table_z SET id = 1 WHERE id = 2", Collections.emptyList(), true);
        assertThat(actual.getSql(), is("UPDATE table_z SET cipher = 'encryptValue', plain = 1 WHERE cipher = 'encryptValue'"));
        assertThat(actual.getParameters(), is(Collections.emptyList()));
    }
    
    @Test
    public void assertRewriteInsertWithGeneratedKeyAndQueryAssistedShardingEncryptor() {
        // TODO case maybe incorrect
//        SQLRewriteResult actual = getSQLRewriteResult("INSERT INTO `table_w` set name = 10 ON DUPLICATE KEY UPDATE name = VALUES(name)", Collections.emptyList(), true);
//        assertThat(actual.getSql(), is("INSERT INTO `table_w` set cipher = 'encryptValue', query = 'assistedEncryptValue', plain = 10, id = 1 ON DUPLICATE KEY UPDATE name = VALUES(name)"));
//        assertThat(actual.getParameters(), is(Collections.emptyList()));
        SQLRouteResult sqlRouteResult = createSQLRouteResultForInsertWithGeneratedKeyAndQueryAssistedShardingEncryptor();
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext(sqlRouteResult, "INSERT INTO `table_w` set name = 10 ON DUPLICATE KEY UPDATE name = VALUES(name)", Collections.emptyList());
        assertThat(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), routingUnit, logicAndActualTables).rewrite(sqlRewriteContext).getSql(),
                is("INSERT INTO `table_w` set cipher = 'encryptValue', query = 'assistedEncryptValue', plain = 10, id = 1 ON DUPLICATE KEY UPDATE name = VALUES(name)"));
    }
    
    private SQLRouteResult createSQLRouteResultForInsertWithGeneratedKeyAndQueryAssistedShardingEncryptor() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.getAllSQLSegments().add(new TableSegment(12, 20, "`table_w`"));
        insertStatement.setTable(new TableSegment(0, 0, "table_w"));
        insertStatement.getColumns().add(new ColumnSegment(0, 0, "name"));
        ColumnSegment columnSegment = new ColumnSegment(26, 29, "name");
        LiteralExpressionSegment expressionSegment = new LiteralExpressionSegment(33, 34, 10);
        insertStatement.getAllSQLSegments().add(new SetAssignmentsSegment(26, 34, Collections.singletonList(new AssignmentSegment(26, 34, columnSegment, expressionSegment))));
        insertStatement.setSetAssignment(new SetAssignmentsSegment(26, 34, Collections.singletonList(new AssignmentSegment(26, 34, columnSegment, expressionSegment))));
        InsertSQLStatementContext insertSQLStatementContext = new InsertSQLStatementContext(null, Collections.emptyList(), insertStatement);
        insertSQLStatementContext.getInsertValueContexts().get(0).getValueExpressions().add(new DerivedLiteralExpressionSegment(1));
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getDataNodes().add(new DataNode("db0.table_1"));
        GeneratedKey generatedKey = new GeneratedKey("id", true);
        generatedKey.getGeneratedValues().add(1);
        SQLRouteResult result = new SQLRouteResult(insertSQLStatementContext, new ShardingConditions(Collections.singletonList(shardingCondition)), generatedKey);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertRewriteSelectInWithAggregationDistinct() {
        SQLRewriteResult actual = getSQLRewriteResult("SELECT COUNT(DISTINCT id) a, SUM(DISTINCT id) a FROM table_z WHERE id IN (3,5)", Collections.emptyList(), true);
        assertThat(actual.getSql(), is("SELECT DISTINCT id a, id a FROM table_z WHERE cipher IN ('encryptValue', 'encryptValue')"));
        assertThat(actual.getParameters(), is(Collections.emptyList()));
    }
    
    @Test
    public void assertRewriteSelectEqualWithShardingEncryptorWithCipher() {
        SQLRewriteResult actual = getSQLRewriteResult("SELECT id FROM table_z WHERE id=? AND name=?", Arrays.<Object>asList(1, "x"), true);
        assertThat(actual.getSql(), is("SELECT cipher FROM table_z WHERE cipher = ? AND name=?"));
        assertThat(actual.getParameters(), is(Arrays.<Object>asList("encryptValue", "x")));
    }
    
    @Test
    public void assertRewriteSelectEqualWithShardingEncryptorWithPlain() {
        SQLRewriteResult actual = getSQLRewriteResult("SELECT id FROM table_z WHERE id=? AND name=?", Arrays.<Object>asList(1, "x"), false);
        assertThat(actual.getSql(), is("SELECT plain FROM table_z WHERE plain = ? AND name=?"));
        assertThat(actual.getParameters(), is(Arrays.<Object>asList(1, "x")));
    }
    
    @Test
    public void assertRewriteSelectInWithShardingEncryptorWithParameterWithCipher() {
        SQLRewriteResult actual = getSQLRewriteResult("SELECT id FROM table_z WHERE id in (?, ?) or id = 3", Arrays.<Object>asList(1, 2), true);
        assertThat(actual.getSql(), is("SELECT cipher FROM table_z WHERE cipher IN (?, ?) or cipher = 'encryptValue'"));
        assertThat(actual.getParameters(), is(Arrays.<Object>asList("encryptValue", "encryptValue")));
    }
    
    @Test
    public void assertRewriteSelectInWithShardingEncryptorWithParameterWithPlain() {
        SQLRewriteResult actual = getSQLRewriteResult("SELECT id FROM table_z WHERE id in (?, ?) or id = 3", Arrays.<Object>asList(1, 2), false);
        assertThat(actual.getSql(), is("SELECT plain FROM table_z WHERE plain IN (?, ?) or plain = '3'"));
        assertThat(actual.getParameters(), is(Arrays.<Object>asList(1, 2)));
    }
    
    @Test
    public void assertRewriteSelectEqualWithQueryAssistedShardingEncryptor() {
        SQLRewriteResult actual = getSQLRewriteResult("SELECT id as alias FROM table_k WHERE id=? AND name=?", Arrays.<Object>asList(1, "k"), true);
        assertThat(actual.getSql(), is("SELECT cipher as alias FROM table_k WHERE query = ? AND name=?"));
        assertThat(actual.getParameters(), is(Arrays.<Object>asList("assistedEncryptValue", "k")));
    }
    
    private SQLRewriteContext createSQLRewriteContext(final SQLRouteResult routeResult, final String sql, final List<Object> parameters) {
        return createSQLRewriteContext(routeResult, sql, parameters, true);
    }
    
    private SQLRewriteContext createSQLRewriteContext(final SQLRouteResult sqlRouteResult, final String sql, final List<Object> parameters, final boolean isQueryWithCipherColumn) {
        SQLRewriteContext result = new SQLRewriteContext(mock(TableMetas.class), sqlRouteResult.getSqlStatementContext(), sql, parameters);
        new ShardingSQLRewriteContextDecorator(shardingRule, sqlRouteResult).decorate(result);
        new EncryptSQLRewriteContextDecorator(shardingRule.getEncryptRule(), isQueryWithCipherColumn).decorate(result);
        return result;
    }
    
    private SQLRewriteResult getSQLRewriteResult(final String sql, final List<Object> parameters, final boolean isQueryWithCipherColumn) {
        ShardingRouter shardingRouter = new ShardingRouter(shardingRule, mock(ShardingSphereMetaData.class), parseEngine);
        SQLStatement sqlStatement = shardingRouter.parse(sql, false);
        SQLRouteResult sqlRouteResult = shardingRouter.route(sql, parameters, sqlStatement);
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(mock(TableMetas.class), sqlRouteResult.getSqlStatementContext(), sql, parameters);
        new ShardingSQLRewriteContextDecorator(shardingRule, sqlRouteResult).decorate(sqlRewriteContext);
        new EncryptSQLRewriteContextDecorator(shardingRule.getEncryptRule(), isQueryWithCipherColumn).decorate(sqlRewriteContext);
        return new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), routingUnit, logicAndActualTables).rewrite(sqlRewriteContext);
    }
}
