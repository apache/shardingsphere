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
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.preprocessor.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.core.preprocessor.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.core.preprocessor.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.core.preprocessor.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.core.preprocessor.segment.select.projection.Projection;
import org.apache.shardingsphere.core.preprocessor.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.core.preprocessor.statement.SQLStatementContext;
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
import org.apache.shardingsphere.core.route.type.RoutingResult;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.route.type.TableUnit;
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
        result.getTableUnits().add(new TableUnit("table_y", "table_y"));
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
    public void assertRewriteTableTokenWithSchemaForSelect() {
        SQLRewriteResult actual = getSQLRewriteResult("SELECT * FROM sharding_db.table_x", Collections.emptyList(), true);
        assertThat(actual.getSql(), is("SELECT * FROM table_1"));
        assertThat(actual.getParameters(), is(Collections.emptyList()));
    }
    
    @Test
    public void assertRewriteInsertWithGeneratedKeyAndQueryAssistedShardingEncryptor() {
        SQLRewriteResult actual = getSQLRewriteResult("INSERT INTO `table_w` set name = 10 ON DUPLICATE KEY UPDATE name = VALUES(name)", Collections.emptyList(), true);
        assertThat(actual.getSql(), is("INSERT INTO `table_w` set cipher = 'encrypt_10', query = 'assisted_query_10', plain = 10, id = 1 ON DUPLICATE KEY UPDATE name = VALUES(name)"));
        assertThat(actual.getParameters(), is(Collections.emptyList()));
    }
    
    @Test
    public void assertRewriteSelectInWithAggregationDistinct() {
        SQLRewriteResult actual = getSQLRewriteResult("SELECT COUNT(DISTINCT id) a, SUM(DISTINCT id) a FROM table_x WHERE id IN (3,5)", Collections.emptyList(), true);
        assertThat(actual.getSql(), is("SELECT DISTINCT id a, id a FROM table_1 WHERE id IN (3,5)"));
        assertThat(actual.getParameters(), is(Collections.emptyList()));
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
