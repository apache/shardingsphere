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

package org.apache.shardingsphere.core.rewrite;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.constant.OrderDirection;
import org.apache.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import org.apache.shardingsphere.core.optimize.result.OptimizeResult;
import org.apache.shardingsphere.core.optimize.result.insert.InsertOptimizeResult;
import org.apache.shardingsphere.core.optimize.result.insert.InsertType;
import org.apache.shardingsphere.core.parse.antlr.constant.QuoteCharacter;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dal.DALStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.token.EncryptColumnToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.IndexToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.InsertValuesToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.ItemsToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.OffsetToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.OrderByToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.RowCountToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.SchemaToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.TableToken;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Column;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parse.old.parser.context.limit.Limit;
import org.apache.shardingsphere.core.parse.old.parser.context.limit.LimitValue;
import org.apache.shardingsphere.core.parse.old.parser.context.orderby.OrderItem;
import org.apache.shardingsphere.core.parse.old.parser.context.table.Table;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.type.RoutingTable;
import org.apache.shardingsphere.core.route.type.TableUnit;
import org.apache.shardingsphere.core.rule.DataNode;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlRootShardingConfiguration;
import org.apache.shardingsphere.core.yaml.engine.YamlEngine;
import org.apache.shardingsphere.core.yaml.swapper.impl.ShardingRuleConfigurationYamlSwapper;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class SQLRewriteEngineTest {
    
    private ShardingRule shardingRule;
    
    private SQLRouteResult routeResult;
    
    private SelectStatement selectStatement;
    
    private InsertStatement insertStatement;
    
    private DALStatement showTablesStatement;
    
    private UpdateStatement updateStatement;
    
    private DeleteStatement deleteStatement;
    
    private Map<String, String> tableTokens;
    
    private ShardingDataSourceMetaData shardingDataSourceMetaData;
    
    @Before
    public void setUp() throws IOException, SQLException {
        URL url = SQLRewriteEngineTest.class.getClassLoader().getResource("yaml/rewrite-rule.yaml");
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
        shardingDataSourceMetaData = new ShardingDataSourceMetaData(getDataSourceURLs(yamlShardingConfig), shardingRule, DatabaseType.H2);
    }
    
    private Map<String, String> getDataSourceURLs(final YamlRootShardingConfiguration yamlShardingConfig) throws SQLException {
        Map<String, DataSource> dataSources = yamlShardingConfig.getDataSources();
        Map<String, String> result = new LinkedHashMap<>();
        for (Entry<String, DataSource> entry : dataSources.entrySet()) {
            try (Connection connection = entry.getValue().getConnection()) {
                result.put(entry.getKey(), connection.getMetaData().getURL());
            }
        }
        return result;
    }
    
    @Test
    public void assertRewriteWithoutChange() {
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, 
                "SELECT table_y.id FROM table_y WHERE table_y.id=?", DatabaseType.MySQL, routeResult, Collections.<Object>singletonList(1), null);
        assertThat(rewriteEngine.rewrite(false).toSQL(null, tableTokens, null, shardingDataSourceMetaData).getSql(), is("SELECT table_y.id FROM table_y WHERE table_y.id=?"));
    }
    
    @Test
    public void assertRewriteForTableName() {
        List<Object> parameters = new ArrayList<>(2);
        parameters.add(1);
        parameters.add("x");
        selectStatement.addSQLToken(new TableToken(7, "table_x", QuoteCharacter.NONE, 0));
        selectStatement.addSQLToken(new TableToken(31, "table_x", QuoteCharacter.NONE, 0));
        selectStatement.addSQLToken(new TableToken(47, "table_x", QuoteCharacter.NONE, 0));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, 
                "SELECT table_x.id, x.name FROM table_x x WHERE table_x.id=? AND x.name=?", DatabaseType.MySQL, routeResult, parameters, null);
        assertThat(rewriteEngine.rewrite(false).toSQL(null, tableTokens, null, shardingDataSourceMetaData).getSql(), is("SELECT table_1.id, x.name FROM table_1 x WHERE table_1.id=? AND x.name=?"));
    }
    
    @Test
    public void assertRewriteForOrderByAndGroupByDerivedColumns() {
        selectStatement.addSQLToken(new TableToken(18, "table_x", QuoteCharacter.NONE, 0));
        ItemsToken itemsToken = new ItemsToken(12);
        itemsToken.getItems().addAll(Arrays.asList("x.id as GROUP_BY_DERIVED_0", "x.name as ORDER_BY_DERIVED_0"));
        selectStatement.addSQLToken(itemsToken);
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, 
                "SELECT x.age FROM table_x x GROUP BY x.id ORDER BY x.name", DatabaseType.MySQL, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(false).toSQL(null, tableTokens, null, shardingDataSourceMetaData).getSql(), is(
                "SELECT x.age, x.id as GROUP_BY_DERIVED_0, x.name as ORDER_BY_DERIVED_0 FROM table_1 x GROUP BY x.id ORDER BY x.name"));
    }
    
    @Test
    public void assertRewriteForAggregationDerivedColumns() {
        selectStatement.addSQLToken(new TableToken(23, "table_x", QuoteCharacter.NONE, 0));
        ItemsToken itemsToken = new ItemsToken(17);
        itemsToken.getItems().addAll(Arrays.asList("COUNT(x.age) as AVG_DERIVED_COUNT_0", "SUM(x.age) as AVG_DERIVED_SUM_0"));
        selectStatement.addSQLToken(itemsToken);
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SELECT AVG(x.age) FROM table_x x", DatabaseType.MySQL, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(false).toSQL(null, tableTokens, null, shardingDataSourceMetaData).getSql(), is(
                "SELECT AVG(x.age), COUNT(x.age) as AVG_DERIVED_COUNT_0, SUM(x.age) as AVG_DERIVED_SUM_0 FROM table_1 x"));
    }
    
    @Test
    public void assertRewriteForAutoGeneratedKeyColumn() {
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.setParametersIndex(2);
        insertStatement.addSQLToken(new TableToken(12, "table_x", QuoteCharacter.NONE, 0));
        insertStatement.addSQLToken(new InsertValuesToken(19));
        InsertOptimizeResult insertOptimizeResult = new InsertOptimizeResult(InsertType.VALUES, Arrays.asList("name", "age", "id"));
        Object[] parameters = {"x", 1, 1};
        SQLExpression[] sqlExpressions = {new SQLPlaceholderExpression(0), new SQLPlaceholderExpression(1), new SQLPlaceholderExpression(2)};
        insertOptimizeResult.addUnit(sqlExpressions, parameters);
        insertOptimizeResult.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        TableUnit tableUnit = new TableUnit("db0");
        tableUnit.getRoutingTables().add(new RoutingTable("table_x", "table_1"));
        routeResult = new SQLRouteResult(insertStatement);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "INSERT INTO table_x (name, age) VALUES (?, ?)", 
                DatabaseType.MySQL, routeResult, Arrays.asList(parameters), new OptimizeResult(insertOptimizeResult));
        assertThat(rewriteEngine.rewrite(false).toSQL(tableUnit, tableTokens, null, shardingDataSourceMetaData).getSql(), is("INSERT INTO table_1 (name, age, id) VALUES (?, ?, ?)"));
    }
    
    @Test
    public void assertRewriteForAutoGeneratedKeyColumnWithoutColumnsWithParameter() {
        insertStatement.getColumnNames().add("name");
        insertStatement.getColumnNames().add("id");
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.setParametersIndex(1);
        insertStatement.addSQLToken(new TableToken(12, "`table_x`", QuoteCharacter.BACK_QUOTE, 0));
        insertStatement.addSQLToken(new InsertValuesToken(21));
        InsertOptimizeResult insertOptimizeResult = new InsertOptimizeResult(InsertType.VALUES, Arrays.asList("name", "id"));
        Object[] parameters = {"Bill", 1};
        SQLExpression[] sqlExpressions = {new SQLPlaceholderExpression(0), new SQLPlaceholderExpression(1)};
        insertOptimizeResult.addUnit(sqlExpressions, parameters);
        insertOptimizeResult.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        TableUnit tableUnit = new TableUnit("db0");
        tableUnit.getRoutingTables().add(new RoutingTable("table_x", "table_1"));
        routeResult = new SQLRouteResult(insertStatement);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, 
                "INSERT INTO `table_x` VALUES (?)", DatabaseType.MySQL, routeResult, Arrays.asList(parameters), new OptimizeResult(insertOptimizeResult));
        assertThat(rewriteEngine.rewrite(false).toSQL(tableUnit, tableTokens, null, shardingDataSourceMetaData).getSql(), is("INSERT INTO `table_1` (name, id) VALUES (?, ?)"));
    }
    
    @Test
    public void assertRewriteForAutoGeneratedKeyColumnWithoutColumnsWithoutParameter() {
        insertStatement.getColumnNames().add("name");
        insertStatement.getColumnNames().add("id");
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.addSQLToken(new TableToken(12, "`table_x`", QuoteCharacter.BACK_QUOTE, 0));
        insertStatement.addSQLToken(new InsertValuesToken(21));
        InsertOptimizeResult insertOptimizeResult = new InsertOptimizeResult(InsertType.VALUES, Arrays.asList("name", "id"));
        SQLExpression[] sqlExpressions = {new SQLNumberExpression(10), new SQLNumberExpression(1)};
        insertOptimizeResult.addUnit(sqlExpressions, new Object[0]);
        insertOptimizeResult.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        TableUnit tableUnit = new TableUnit("db0");
        tableUnit.getRoutingTables().add(new RoutingTable("table_x", "table_1"));
        routeResult = new SQLRouteResult(insertStatement);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "INSERT INTO `table_x` VALUES (10)", 
                DatabaseType.MySQL, routeResult, Collections.emptyList(), new OptimizeResult(insertOptimizeResult));
        assertThat(rewriteEngine.rewrite(false).toSQL(tableUnit, tableTokens, null, shardingDataSourceMetaData).getSql(), is("INSERT INTO `table_1` (name, id) VALUES (10, 1)"));
    }
    
    @Test
    public void assertRewriteColumnWithoutColumnsWithoutParameter() {
        insertStatement.getColumnNames().add("name");
        insertStatement.getColumnNames().add("id");
        insertStatement.getTables().add(new Table("table_x", null));
        List<Object> parameters = new ArrayList<>(2);
        parameters.add("x");
        parameters.add(1);
        insertStatement.addSQLToken(new TableToken(12, "`table_x`", QuoteCharacter.BACK_QUOTE, 0));
        insertStatement.addSQLToken(new InsertValuesToken(21));
        InsertOptimizeResult insertOptimizeResult = new InsertOptimizeResult(InsertType.VALUES, Arrays.asList("name", "id"));
        SQLExpression[] sqlExpressions = {new SQLNumberExpression(10), new SQLNumberExpression(1)};
        insertOptimizeResult.addUnit(sqlExpressions, new Object[0]);
        insertOptimizeResult.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        TableUnit tableUnit = new TableUnit("db0");
        tableUnit.getRoutingTables().add(new RoutingTable("table_x", "table_1"));
        routeResult = new SQLRouteResult(insertStatement);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, 
                "INSERT INTO `table_x` VALUES (10, 1)", DatabaseType.MySQL, routeResult, parameters, new OptimizeResult(insertOptimizeResult));
        assertThat(rewriteEngine.rewrite(false).toSQL(tableUnit, tableTokens, null, shardingDataSourceMetaData).getSql(), is("INSERT INTO `table_1` (name, id) VALUES (10, 1)"));
    }
    
    @Test
    public void assertRewriteColumnWithoutColumnsWithParameter() {
        insertStatement.getColumnNames().add("name");
        insertStatement.getColumnNames().add("id");
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.addSQLToken(new TableToken(12, "`table_x`", QuoteCharacter.BACK_QUOTE, 0));
        insertStatement.addSQLToken(new InsertValuesToken(21));
        InsertOptimizeResult insertOptimizeResult = new InsertOptimizeResult(InsertType.VALUES, Arrays.asList("name", "id"));
        SQLExpression[] sqlExpressions = {new SQLPlaceholderExpression(0), new SQLPlaceholderExpression(1)};
        Object[] parameters = {"x", 1};
        insertOptimizeResult.addUnit(sqlExpressions, parameters);
        insertOptimizeResult.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        TableUnit tableUnit = new TableUnit("db0");
        tableUnit.getRoutingTables().add(new RoutingTable("table_x", "table_1"));
        routeResult = new SQLRouteResult(insertStatement);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, 
                "INSERT INTO `table_x` VALUES (?, ?)", DatabaseType.MySQL, routeResult, Arrays.asList(parameters), new OptimizeResult(insertOptimizeResult));
        assertThat(rewriteEngine.rewrite(false).toSQL(tableUnit, tableTokens, null, shardingDataSourceMetaData).getSql(), is("INSERT INTO `table_1` (name, id) VALUES (?, ?)"));
    }
    
    @Test
    public void assertRewriteForLimit() {
        selectStatement.setLimit(new Limit());
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(2, -1, false));
        selectStatement.addSQLToken(new TableToken(17, "table_x", QuoteCharacter.NONE, 0));
        selectStatement.addSQLToken(new OffsetToken(33, 2));
        selectStatement.addSQLToken(new RowCountToken(36, 2));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SELECT x.id FROM table_x x LIMIT 2, 2", DatabaseType.MySQL, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(false).toSQL(null, tableTokens, null, shardingDataSourceMetaData).getSql(), is("SELECT x.id FROM table_1 x LIMIT 0, 4"));
    }
    
    @Test
    public void assertRewriteForRowNum() {
        selectStatement.setLimit(new Limit());
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(4, -1, false));
        selectStatement.addSQLToken(new TableToken(68, "table_x", QuoteCharacter.NONE, 0));
        selectStatement.addSQLToken(new OffsetToken(119, 2));
        selectStatement.addSQLToken(new RowCountToken(98, 4));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_x x) row_ WHERE rownum<=4) t WHERE t.rownum_>2", 
                DatabaseType.Oracle, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(false).toSQL(null, tableTokens, null, shardingDataSourceMetaData).getSql(),
                is("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_1 x) row_ WHERE rownum<=4) t WHERE t.rownum_>0"));
    }
    
    @Test
    public void assertRewriteForTopAndRowNumber() {
        selectStatement.setLimit(new Limit());
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(4, -1, false));
        selectStatement.addSQLToken(new TableToken(85, "table_x", QuoteCharacter.NONE, 0));
        selectStatement.addSQLToken(new OffsetToken(123, 2));
        selectStatement.addSQLToken(new RowCountToken(26, 4));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, 
                "SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_x x) AS row_ WHERE row_.rownum_>2", 
                DatabaseType.SQLServer, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(false).toSQL(null, tableTokens, null, shardingDataSourceMetaData).getSql(),
                is("SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_1 x) AS row_ WHERE row_.rownum_>0"));
    }
    
    @Test
    public void assertRewriteForLimitForMemoryGroupBy() {
        selectStatement.setLimit(new Limit());
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(2, -1, false));
        selectStatement.getOrderByItems().add(new OrderItem("x", "id", OrderDirection.ASC, OrderDirection.ASC));
        selectStatement.getGroupByItems().add(new OrderItem("x", "id", OrderDirection.DESC, OrderDirection.ASC));
        selectStatement.addSQLToken(new TableToken(17, "table_x", QuoteCharacter.NONE, 0));
        selectStatement.addSQLToken(new OffsetToken(33, 2));
        selectStatement.addSQLToken(new RowCountToken(36, 2));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SELECT x.id FROM table_x x LIMIT 2, 2", DatabaseType.MySQL, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(false).toSQL(null, tableTokens, null, shardingDataSourceMetaData).getSql(), is("SELECT x.id FROM table_1 x LIMIT 0, 2147483647"));
    }
    
    @Test
    public void assertRewriteForRowNumForMemoryGroupBy() {
        selectStatement.setLimit(new Limit());
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(4, -1, false));
        selectStatement.addSQLToken(new TableToken(68, "table_x", QuoteCharacter.NONE, 0));
        selectStatement.addSQLToken(new OffsetToken(119, 2));
        selectStatement.addSQLToken(new RowCountToken(98, 4));
        selectStatement.getOrderByItems().add(new OrderItem("x", "id", OrderDirection.ASC, OrderDirection.ASC));
        selectStatement.getGroupByItems().add(new OrderItem("x", "id", OrderDirection.DESC, OrderDirection.ASC));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_x x) row_ WHERE rownum<=4) t WHERE t.rownum_>2", 
                DatabaseType.Oracle, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(false).toSQL(null, tableTokens, null, shardingDataSourceMetaData).getSql(),
                is("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_1 x) row_ WHERE rownum<=2147483647) t WHERE t.rownum_>0"));
    }
    
    @Test
    public void assertRewriteForTopAndRowNumberForMemoryGroupBy() {
        selectStatement.setLimit(new Limit());
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(4, -1, false));
        selectStatement.addSQLToken(new TableToken(85, "table_x", QuoteCharacter.NONE, 0));
        selectStatement.addSQLToken(new OffsetToken(123, 2));
        selectStatement.addSQLToken(new RowCountToken(26, 4));
        selectStatement.getOrderByItems().add(new OrderItem("x", "id", OrderDirection.ASC, OrderDirection.ASC));
        selectStatement.getGroupByItems().add(new OrderItem("x", "id", OrderDirection.DESC, OrderDirection.ASC));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, 
                "SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_x x) AS row_ WHERE row_.rownum_>2", 
                DatabaseType.SQLServer, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(false).toSQL(null, tableTokens, null, shardingDataSourceMetaData).getSql(),
                is("SELECT * FROM (SELECT TOP(2147483647) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_1 x) AS row_ WHERE row_.rownum_>0"));
    }
    
    @Test
    public void assertRewriteForLimitForNotRewriteLimit() {
        selectStatement.setLimit(new Limit());
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(2, -1, false));
        selectStatement.addSQLToken(new TableToken(17, "table_x", QuoteCharacter.NONE, 0));
        selectStatement.addSQLToken(new OffsetToken(33, 2));
        selectStatement.addSQLToken(new RowCountToken(36, 2));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SELECT x.id FROM table_x x LIMIT 2, 2", DatabaseType.MySQL, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(true).toSQL(null, tableTokens, null, shardingDataSourceMetaData).getSql(), is("SELECT x.id FROM table_1 x LIMIT 2, 2"));
    }
    
    @Test
    public void assertRewriteForRowNumForNotRewriteLimit() {
        selectStatement.setLimit(new Limit());
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(4, -1, false));
        selectStatement.addSQLToken(new TableToken(68, "table_x", QuoteCharacter.NONE, 0));
        selectStatement.addSQLToken(new OffsetToken(119, 2));
        selectStatement.addSQLToken(new RowCountToken(98, 4));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_x x) row_ WHERE rownum<=4) t WHERE t.rownum_>2", 
                DatabaseType.Oracle, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(true).toSQL(null, tableTokens, null, shardingDataSourceMetaData).getSql(),
                is("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_1 x) row_ WHERE rownum<=4) t WHERE t.rownum_>2"));
    }
    
    @Test
    public void assertRewriteForTopAndRowNumberForNotRewriteLimit() {
        selectStatement.setLimit(new Limit());
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(4, -1, false));
        selectStatement.addSQLToken(new TableToken(85, "table_x", QuoteCharacter.NONE, 0));
        selectStatement.addSQLToken(new OffsetToken(123, 2));
        selectStatement.addSQLToken(new RowCountToken(26, 4));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, 
                "SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_x x) AS row_ WHERE row_.rownum_>2", 
                DatabaseType.SQLServer, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(true).toSQL(null, tableTokens, null, shardingDataSourceMetaData).getSql(),
                is("SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_1 x) AS row_ WHERE row_.rownum_>2"));
    }
    
    @Test
    public void assertRewriteForDerivedOrderBy() {
        selectStatement.setGroupByLastIndex(60);
        selectStatement.getOrderByItems().add(new OrderItem("x", "id", OrderDirection.ASC, OrderDirection.ASC));
        selectStatement.getOrderByItems().add(new OrderItem("x", "name", OrderDirection.DESC, OrderDirection.ASC));
        selectStatement.addSQLToken(new TableToken(25, "table_x", QuoteCharacter.NONE, 0));
        selectStatement.addSQLToken(new OrderByToken(61));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, 
                "SELECT x.id, x.name FROM table_x x GROUP BY x.id, x.name DESC", DatabaseType.MySQL, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(false).toSQL(null, tableTokens, null, shardingDataSourceMetaData).getSql(), is(
                "SELECT x.id, x.name FROM table_1 x GROUP BY x.id, x.name DESC ORDER BY id ASC,name DESC "));
    }
    
    @Test
    public void assertGenerateSQL() {
        List<Object> parameters = new ArrayList<>(2);
        parameters.add(1);
        parameters.add("x");
        selectStatement.addSQLToken(new TableToken(7, "table_x", QuoteCharacter.NONE, 0));
        selectStatement.addSQLToken(new TableToken(31, "table_x", QuoteCharacter.NONE, 0));
        selectStatement.addSQLToken(new TableToken(58, "table_x", QuoteCharacter.NONE, 0));
        selectStatement.getTables().add(new Table("table_x", "x"));
        selectStatement.getTables().add(new Table("table_y", "y"));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        SQLRewriteEngine sqlRewriteEngine =
                new SQLRewriteEngine(shardingRule, "SELECT table_x.id, x.name FROM table_x x, table_y y WHERE table_x.id=? AND x.name=?", DatabaseType.MySQL, routeResult, parameters, null);
        SQLBuilder sqlBuilder = sqlRewriteEngine.rewrite(false);
        TableUnit tableUnit = new TableUnit("db0");
        tableUnit.getRoutingTables().add(new RoutingTable("table_x", "table_x"));
        assertThat(sqlRewriteEngine.generateSQL(tableUnit, sqlBuilder, shardingDataSourceMetaData).getSql(), is("SELECT table_x.id, x.name FROM table_x x, table_y y WHERE table_x.id=? AND x.name=?"));
    }
    
    @Test
    public void assertSchemaTokenRewriteForTableName() {
        tableTokens = new HashMap<>(1, 1);
        tableTokens.put("table_x", "table_y");
        selectStatement.addSQLToken(new TableToken(18, "table_x", QuoteCharacter.NONE, 0));
        selectStatement.addSQLToken(new SchemaToken(29, 35, "table_x"));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SHOW CREATE TABLE table_x ON table_x", DatabaseType.MySQL, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(false).toSQL(null, tableTokens, shardingRule, shardingDataSourceMetaData).getSql(), is("SHOW CREATE TABLE table_y ON db0"));
    }
    
    @Test
    public void assertIndexTokenForIndexNameTableName() {
        selectStatement.addSQLToken(new IndexToken(13, 22, "table_x"));
        selectStatement.addSQLToken(new TableToken(27, "table_x", QuoteCharacter.NONE, 0));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "CREATE INDEX index_name ON table_x ('column')", DatabaseType.MySQL, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(false).toSQL(null, tableTokens, shardingRule, shardingDataSourceMetaData).getSql(), is("CREATE INDEX index_name_table_1 ON table_1 ('column')"));
    }
    
    @Test
    public void assertIndexTokenForIndexNameTableNameWithoutLogicTableName() {
        selectStatement.addSQLToken(new IndexToken(13, 23, ""));
        selectStatement.addSQLToken(new TableToken(28, "table_x", QuoteCharacter.NONE, 0));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "CREATE INDEX logic_index ON table_x ('column')", DatabaseType.MySQL, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(false).toSQL(null, tableTokens, shardingRule, shardingDataSourceMetaData).getSql(), is("CREATE INDEX logic_index_table_1 ON table_1 ('column')"));
    }
    
    @Test
    public void assertTableTokenWithoutBackQuoteForShow() {
        showTablesStatement.addSQLToken(new TableToken(18, "table_x", QuoteCharacter.NONE, 0));
        routeResult = new SQLRouteResult(showTablesStatement);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SHOW COLUMNS FROM table_x", DatabaseType.MySQL, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(true).toSQL(null, tableTokens, shardingRule, shardingDataSourceMetaData).getSql(), is("SHOW COLUMNS FROM table_1"));
    }
    
    @Test
    public void assertTableTokenWithoutBackQuoteFromSchemaForShow() {
        showTablesStatement.addSQLToken(new TableToken(18, "table_x", QuoteCharacter.NONE, 0));
        showTablesStatement.addSQLToken(new SchemaToken(31, 43, "table_x"));
        routeResult = new SQLRouteResult(showTablesStatement);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SHOW COLUMNS FROM table_x FROM 'sharding_db'", DatabaseType.MySQL, routeResult, Collections.emptyList(), null);
        Map<String, String> logicAndActualTableMap = new LinkedHashMap<>();
        logicAndActualTableMap.put("table_x", "table_x");
        assertThat(rewriteEngine.rewrite(true).toSQL(null, logicAndActualTableMap, shardingRule, shardingDataSourceMetaData).getSql(), is("SHOW COLUMNS FROM table_x FROM db0"));
    }
    
    @Test
    public void assertTableTokenWithBackQuoteForShow() {
        showTablesStatement.addSQLToken(new TableToken(18, "`table_x`", QuoteCharacter.BACK_QUOTE, 0));
        routeResult = new SQLRouteResult(showTablesStatement);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SHOW COLUMNS FROM `table_x`", DatabaseType.MySQL, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(true).toSQL(null, tableTokens, shardingRule, shardingDataSourceMetaData).getSql(), is("SHOW COLUMNS FROM `table_1`"));
    }
    
    @Test
    public void assertTableTokenWithBackQuoteFromSchemaForShow() {
        showTablesStatement.addSQLToken(new TableToken(18, "`table_x`", QuoteCharacter.BACK_QUOTE, 0));
        routeResult = new SQLRouteResult(showTablesStatement);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SHOW COLUMNS FROM `table_x` FROM 'sharding_db'", DatabaseType.MySQL, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(true).toSQL(null, tableTokens, shardingRule, shardingDataSourceMetaData).getSql(), is("SHOW COLUMNS FROM `table_1` FROM 'sharding_db'"));
    }
    
    @Test
    public void assertTableTokenWithSchemaForShow() {
        showTablesStatement.addSQLToken(new TableToken(18, "table_x", QuoteCharacter.NONE, "sharding_db".length() + 1));
        routeResult = new SQLRouteResult(showTablesStatement);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SHOW COLUMNS FROM sharding_db.table_x", DatabaseType.MySQL, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(true).toSQL(null, tableTokens, shardingRule, shardingDataSourceMetaData).getSql(), is("SHOW COLUMNS FROM table_1"));
    }
    
    @Test
    public void assertTableTokenWithSchemaFromSchemaForShow() {
        showTablesStatement.addSQLToken(new TableToken(18, "table_x", QuoteCharacter.NONE, "sharding_db".length() + 1));
        routeResult = new SQLRouteResult(showTablesStatement);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, 
                "SHOW COLUMNS FROM sharding_db.table_x FROM sharding_db", DatabaseType.MySQL, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(true).toSQL(null, tableTokens, shardingRule, shardingDataSourceMetaData).getSql(), is("SHOW COLUMNS FROM table_1 FROM sharding_db"));
    }
    
    @Test
    public void assertTableTokenWithBackQuoteWithSchemaForShow() {
        showTablesStatement.addSQLToken(new TableToken(18, "`table_x`", QuoteCharacter.BACK_QUOTE, "sharding_db".length() + 1));
        routeResult = new SQLRouteResult(showTablesStatement);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SHOW COLUMNS FROM sharding_db.`table_x`", DatabaseType.MySQL, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(true).toSQL(null, tableTokens, shardingRule, shardingDataSourceMetaData).getSql(), is("SHOW COLUMNS FROM `table_1`"));
    }
    
    @Test
    public void assertTableTokenWithBackQuoteWithSchemaFromSchemaForShow() {
        showTablesStatement.addSQLToken(new TableToken(18, "`table_x`", QuoteCharacter.BACK_QUOTE, "sharding_db".length() + 1));
        routeResult = new SQLRouteResult(showTablesStatement);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, 
                "SHOW COLUMNS FROM sharding_db.`table_x` FROM sharding_db", DatabaseType.MySQL, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(true).toSQL(null, tableTokens, shardingRule, shardingDataSourceMetaData).getSql(), is("SHOW COLUMNS FROM `table_1` FROM sharding_db"));
    }
    
    @Test
    public void assertTableTokenWithSchemaWithBackQuoteForShow() {
        showTablesStatement.addSQLToken(new TableToken(18, "`table_x`", QuoteCharacter.BACK_QUOTE, "`sharding_db`".length() + 1));
        routeResult = new SQLRouteResult(showTablesStatement);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SHOW COLUMNS FROM `sharding_db`.`table_x`", DatabaseType.MySQL, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(true).toSQL(null, tableTokens, shardingRule, shardingDataSourceMetaData).getSql(), is("SHOW COLUMNS FROM `table_1`"));
    }
    
    @Test
    public void assertTableTokenWithSchemaWithBackQuoteFromSchemaForShow() {
        showTablesStatement.addSQLToken(new TableToken(18, "`table_x`", QuoteCharacter.BACK_QUOTE, "`sharding_db`".length() + 1));
        routeResult = new SQLRouteResult(showTablesStatement);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, 
                "SHOW COLUMNS FROM `sharding_db`.`table_x` FROM sharding_db", DatabaseType.MySQL, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(true).toSQL(null, tableTokens, shardingRule, shardingDataSourceMetaData).getSql(), is("SHOW COLUMNS FROM `table_1` FROM sharding_db"));
    }
    
    @Test
    public void assertTableTokenWithSchemaForSelect() {
        selectStatement.addSQLToken(new TableToken(14, "table_x", QuoteCharacter.NONE, "sharding_db".length() + 1));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SELECT * FROM sharding_db.table_x", DatabaseType.MySQL, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(true).toSQL(null, tableTokens, shardingRule, shardingDataSourceMetaData).getSql(), is("SELECT * FROM table_1"));
    }
    
    @Test
    public void assertTableTokenWithSchemaForInsert() {
        insertStatement.addSQLToken(new TableToken(12, "table_x", QuoteCharacter.NONE, "sharding_db".length() + 1));
        routeResult = new SQLRouteResult(insertStatement);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, 
                "INSERT INTO sharding_db.table_x (order_id, user_id, status) values (1, 1, 'OK')", DatabaseType.MySQL, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(false).toSQL(
                null, tableTokens, shardingRule, shardingDataSourceMetaData).getSql(), is("INSERT INTO table_1 (order_id, user_id, status) values (1, 1, 'OK')"));
    }
    
    @Test
    public void assertTableTokenWithSchemaForUpdate() {
        updateStatement.addSQLToken(new TableToken(7, "table_x", QuoteCharacter.NONE, "`sharding_db`".length() + 1));
        routeResult = new SQLRouteResult(updateStatement);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, 
                "UPDATE `sharding_db`.table_x SET user_id=1 WHERE order_id=1", DatabaseType.MySQL, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(false).toSQL(null, tableTokens, shardingRule, shardingDataSourceMetaData).getSql(), is("UPDATE table_1 SET user_id=1 WHERE order_id=1"));
    }
    
    @Test
    public void assertTableTokenWithSchemaForDelete() {
        deleteStatement.addSQLToken(new TableToken(12, "`table_x`", QuoteCharacter.BACK_QUOTE, "`sharding_db`".length() + 1));
        routeResult = new SQLRouteResult(deleteStatement);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "DELETE FROM `sharding_db`.`table_x` WHERE user_id=1", DatabaseType.MySQL, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(true).toSQL(null, tableTokens, shardingRule, shardingDataSourceMetaData).getSql(), is("DELETE FROM `table_1` WHERE user_id=1"));
    }
    
    @Test
    public void assertSelectEqualWithShardingEncryptor() {
        List<Object> parameters = new ArrayList<>(2);
        parameters.add(1);
        parameters.add("x");
        Column column = new Column("id", "table_z");
        selectStatement.addSQLToken(new TableToken(15, "table_z", QuoteCharacter.NONE, 0));
        selectStatement.addSQLToken(new EncryptColumnToken(29, 32, column, true));
        selectStatement.getEncryptConditions().getOrCondition().getAndConditions().add(new AndCondition());
        selectStatement.getEncryptConditions().getOrCondition().getAndConditions().get(0).getConditions().add(new Condition(column, new SQLPlaceholderExpression(0)));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule,
                "SELECT id FROM table_z WHERE id=? AND name=?", DatabaseType.MySQL, routeResult, parameters, null);
        assertThat(rewriteEngine.rewrite(false).toSQL(null, tableTokens, shardingRule, shardingDataSourceMetaData).getSql(), is("SELECT id FROM table_z WHERE id = ? AND name=?"));
        assertThat(parameters.get(0), is((Object) "encryptValue"));
    }
    
    @Test
    public void assertSelectBetweenWithShardingEncryptor() {
        Column column = new Column("id", "table_z");
        selectStatement.addSQLToken(new TableToken(15, "table_z", QuoteCharacter.NONE, 0));
        selectStatement.addSQLToken(new EncryptColumnToken(29, 46, column, true));
        selectStatement.getEncryptConditions().getOrCondition().getAndConditions().add(new AndCondition());
        selectStatement.getEncryptConditions().getOrCondition().getAndConditions().get(0).getConditions().add(new Condition(column, new SQLNumberExpression(3), new SQLNumberExpression(5)));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule,
                "SELECT id FROM table_z WHERE id between 3 and 5", DatabaseType.MySQL, routeResult, new LinkedList<>(), null);
        assertThat(rewriteEngine.rewrite(false).toSQL(null, tableTokens, shardingRule, shardingDataSourceMetaData).getSql(), 
                is("SELECT id FROM table_z WHERE id BETWEEN 'encryptValue' AND 'encryptValue'"));
    }
    
    @Test
    public void assertSelectInWithShardingEncryptor() {
        Column column = new Column("id", "table_z");
        selectStatement.addSQLToken(new TableToken(15, "table_z", QuoteCharacter.NONE, 0));
        selectStatement.addSQLToken(new EncryptColumnToken(29, 39, column, true));
        selectStatement.getEncryptConditions().getOrCondition().getAndConditions().add(new AndCondition());
        List<SQLExpression> sqlExpressions = new LinkedList<>();
        sqlExpressions.add(new SQLNumberExpression(3));
        sqlExpressions.add(new SQLNumberExpression(5));
        selectStatement.getEncryptConditions().getOrCondition().getAndConditions().get(0).getConditions().add(new Condition(column, sqlExpressions));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule,
                "SELECT id FROM table_z WHERE id in (3,5)", DatabaseType.MySQL, routeResult, new LinkedList<>(), null);
        assertThat(rewriteEngine.rewrite(false).toSQL(null, tableTokens, shardingRule, shardingDataSourceMetaData).getSql(), 
                is("SELECT id FROM table_z WHERE id IN ('encryptValue', 'encryptValue')"));
    }
    
    @Test
    public void assertSelectInWithShardingEncryptorWithParameter() {
        List<Object> parameters = new ArrayList<>(2);
        parameters.add(1);
        parameters.add(2);
        Column column = new Column("id", "table_z");
        selectStatement.addSQLToken(new TableToken(15, "table_z", QuoteCharacter.NONE, 0));
        selectStatement.addSQLToken(new EncryptColumnToken(29, 40, column, true));
        selectStatement.addSQLToken(new EncryptColumnToken(45, 50, column, true));
        selectStatement.getEncryptConditions().getOrCondition().getAndConditions().add(new AndCondition());
        selectStatement.getEncryptConditions().getOrCondition().getAndConditions().add(new AndCondition());
        List<SQLExpression> sqlExpressions = new LinkedList<>();
        sqlExpressions.add(new SQLPlaceholderExpression(0));
        sqlExpressions.add(new SQLPlaceholderExpression(1));
        selectStatement.getEncryptConditions().getOrCondition().getAndConditions().get(0).getConditions().add(new Condition(column, sqlExpressions));
        selectStatement.getEncryptConditions().getOrCondition().getAndConditions().get(1).getConditions().add(new Condition(column, new SQLNumberExpression(3)));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule,
                "SELECT id FROM table_z WHERE id in (?, ?) or id = 3", DatabaseType.MySQL, routeResult, parameters, null);
        assertThat(rewriteEngine.rewrite(false).toSQL(null, tableTokens, shardingRule, shardingDataSourceMetaData).getSql(), is("SELECT id FROM table_z WHERE id IN (?, ?) or id = 'encryptValue'"));
        assertThat(parameters.get(0), is((Object) "encryptValue"));
        assertThat(parameters.get(1), is((Object) "encryptValue"));
    }
    
    @Test
    public void assertSelectEqualWithQueryAssistedShardingEncryptor() {
        List<Object> parameters = new ArrayList<>(2);
        parameters.add(1);
        parameters.add("k");
        Column column = new Column("id", "table_k");
        selectStatement.addSQLToken(new TableToken(15, "table_k", QuoteCharacter.NONE, 0));
        selectStatement.addSQLToken(new EncryptColumnToken(29, 32, column, true));
        selectStatement.getEncryptConditions().getOrCondition().getAndConditions().add(new AndCondition());
        selectStatement.getEncryptConditions().getOrCondition().getAndConditions().get(0).getConditions().add(new Condition(column, new SQLPlaceholderExpression(0)));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule,
                "SELECT id FROM table_k WHERE id=? AND name=?", DatabaseType.MySQL, routeResult, parameters, null);
        assertThat(rewriteEngine.rewrite(false).toSQL(null, tableTokens, shardingRule, shardingDataSourceMetaData).getSql(), is("SELECT id FROM table_k WHERE query_id = ? AND name=?"));
        assertThat(parameters.get(0), is((Object) "assistedEncryptValue"));
    }
    
    @Test
    public void assertSelectInWithQueryAssistedShardingEncryptor() {
        Column column = new Column("id", "table_k");
        selectStatement.addSQLToken(new TableToken(15, "table_k", QuoteCharacter.NONE, 0));
        selectStatement.addSQLToken(new EncryptColumnToken(29, 39, column, true));
        selectStatement.getEncryptConditions().getOrCondition().getAndConditions().add(new AndCondition());
        List<SQLExpression> sqlExpressions = new LinkedList<>();
        sqlExpressions.add(new SQLNumberExpression(3));
        sqlExpressions.add(new SQLNumberExpression(5));
        selectStatement.getEncryptConditions().getOrCondition().getAndConditions().get(0).getConditions().add(new Condition(column, sqlExpressions));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule,
                "SELECT id FROM table_k WHERE id in (3,5)", DatabaseType.MySQL, routeResult, new LinkedList<>(), null);
        assertThat(rewriteEngine.rewrite(false).toSQL(null, tableTokens, shardingRule, shardingDataSourceMetaData).getSql(), 
                is("SELECT id FROM table_k WHERE query_id IN ('assistedEncryptValue', 'assistedEncryptValue')"));
    }
    
    @Test
    public void assertUpdateWithShardingEncryptor() {
        Column column = new Column("id", "table_z");
        updateStatement.addSQLToken(new TableToken(7, "table_z", QuoteCharacter.NONE, 0));
        updateStatement.addSQLToken(new EncryptColumnToken(19, 24, column, false));
        updateStatement.getAssignments().put(column, new SQLNumberExpression(1));
        updateStatement.addSQLToken(new EncryptColumnToken(32, 37, column, true));
        updateStatement.getEncryptConditions().getOrCondition().getAndConditions().add(new AndCondition());
        updateStatement.getEncryptConditions().getOrCondition().getAndConditions().get(0).getConditions().add(new Condition(column, new SQLNumberExpression(2)));
        routeResult = new SQLRouteResult(updateStatement);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule,
                "UPDATE table_z SET id = 1 WHERE id = 2", DatabaseType.MySQL, routeResult, Collections.emptyList(), null);
        assertThat(rewriteEngine.rewrite(false).toSQL(null, tableTokens, shardingRule, shardingDataSourceMetaData).getSql(), 
                is("UPDATE table_z SET id = 'encryptValue' WHERE id = 'encryptValue'"));
    }
    
    @Test
    public void assertUpdateWithQueryAssistedShardingEncryptor() {
        List<Object> parameters = new ArrayList<>(2);
        parameters.add(1);
        parameters.add(5);
        Column column = new Column("id", "table_k");
        updateStatement.addSQLToken(new TableToken(7, "table_k", QuoteCharacter.NONE, 0));
        updateStatement.addSQLToken(new EncryptColumnToken(19, 24, column, false));
        updateStatement.getAssignments().put(column, new SQLPlaceholderExpression(0));
        updateStatement.addSQLToken(new EncryptColumnToken(32, 49, column, true));
        updateStatement.getEncryptConditions().getOrCondition().getAndConditions().add(new AndCondition());
        updateStatement.getEncryptConditions().getOrCondition().getAndConditions().get(0).getConditions().add(new Condition(column, new SQLNumberExpression(3), new SQLPlaceholderExpression(1)));
        routeResult = new SQLRouteResult(updateStatement);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule,
                "UPDATE table_k SET id = ? WHERE id between 3 and ?", DatabaseType.MySQL, routeResult, parameters, null);
        assertThat(rewriteEngine.rewrite(false).toSQL(null, tableTokens, shardingRule, shardingDataSourceMetaData).getSql(), 
                is("UPDATE table_k SET id = ?, query_id = ? WHERE query_id BETWEEN 'assistedEncryptValue' AND ?"));
        assertThat(parameters.get(0), is((Object) "encryptValue"));
        assertThat(parameters.get(1), is((Object) "assistedEncryptValue"));
        assertThat(parameters.get(1), is((Object) "assistedEncryptValue"));
    }
}
