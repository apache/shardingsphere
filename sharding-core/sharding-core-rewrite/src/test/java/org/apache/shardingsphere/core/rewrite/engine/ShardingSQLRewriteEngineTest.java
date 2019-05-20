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

package org.apache.shardingsphere.core.rewrite.engine;

import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.constant.OrderDirection;
import org.apache.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import org.apache.shardingsphere.core.optimize.result.OptimizeResult;
import org.apache.shardingsphere.core.optimize.result.insert.InsertOptimizeResult;
import org.apache.shardingsphere.core.parse.constant.QuoteCharacter;
import org.apache.shardingsphere.core.parse.sql.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.sql.context.condition.Column;
import org.apache.shardingsphere.core.parse.sql.context.condition.Condition;
import org.apache.shardingsphere.core.parse.sql.context.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.sql.context.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parse.sql.context.expression.SQLParameterMarkerExpression;
import org.apache.shardingsphere.core.parse.sql.context.limit.Limit;
import org.apache.shardingsphere.core.parse.sql.context.limit.LimitValue;
import org.apache.shardingsphere.core.parse.sql.context.orderby.OrderItem;
import org.apache.shardingsphere.core.parse.sql.context.table.Table;
import org.apache.shardingsphere.core.parse.sql.statement.dal.DALStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.parse.sql.token.impl.EncryptColumnToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.IndexToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.InsertColumnsToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.InsertValuesToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.OffsetToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.OrderByToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.RowCountToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.SchemaToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.SelectItemsToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.TableToken;
import org.apache.shardingsphere.core.rewrite.builder.SQLBuilder;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.SQLUnit;
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

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
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

public final class ShardingSQLRewriteEngineTest {
    
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
        URL url = ShardingSQLRewriteEngineTest.class.getClassLoader().getResource("yaml/rewrite-rule.yaml");
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
        routeResult.setRoutingResult(new RoutingResult());
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, 
                "SELECT table_y.id FROM table_y WHERE table_y.id=?", DatabaseType.MySQL, routeResult, Collections.<Object>singletonList(1), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), is("SELECT table_y.id FROM table_y WHERE table_y.id=?"));
    }
    
    @Test
    public void assertRewriteForTableName() {
        List<Object> parameters = new ArrayList<>(2);
        parameters.add(1);
        parameters.add("x");
        selectStatement.addSQLToken(new TableToken(7, 13, "table_x", QuoteCharacter.NONE));
        selectStatement.addSQLToken(new TableToken(31, 37, "table_x", QuoteCharacter.NONE));
        selectStatement.addSQLToken(new TableToken(47, 53, "table_x", QuoteCharacter.NONE));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        routeResult.setRoutingResult(new RoutingResult());
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, 
                "SELECT table_x.id, x.name FROM table_x x WHERE table_x.id=? AND x.name=?", DatabaseType.MySQL, routeResult, parameters, shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), is("SELECT table_1.id, x.name FROM table_1 x WHERE table_1.id=? AND x.name=?"));
    }
    
    @Test
    public void assertRewriteForOrderByAndGroupByDerivedColumns() {
        selectStatement.addSQLToken(new TableToken(18, 24, "table_x", QuoteCharacter.NONE));
        SelectItemsToken selectItemsToken = new SelectItemsToken(12);
        selectItemsToken.getItems().addAll(Arrays.asList("x.id as GROUP_BY_DERIVED_0", "x.name as ORDER_BY_DERIVED_0"));
        selectStatement.addSQLToken(selectItemsToken);
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        routeResult.setRoutingResult(new RoutingResult());
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, 
                "SELECT x.age FROM table_x x GROUP BY x.id ORDER BY x.name", DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), is(
                "SELECT x.age, x.id as GROUP_BY_DERIVED_0, x.name as ORDER_BY_DERIVED_0 FROM table_1 x GROUP BY x.id ORDER BY x.name"));
    }
    
    @Test
    public void assertRewriteForAggregationDerivedColumns() {
        selectStatement.addSQLToken(new TableToken(23, 29, "table_x", QuoteCharacter.NONE));
        SelectItemsToken selectItemsToken = new SelectItemsToken(17);
        selectItemsToken.getItems().addAll(Arrays.asList("COUNT(x.age) as AVG_DERIVED_COUNT_0", "SUM(x.age) as AVG_DERIVED_SUM_0"));
        selectStatement.addSQLToken(selectItemsToken);
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        routeResult.setRoutingResult(new RoutingResult());
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, "SELECT AVG(x.age) FROM table_x x", 
                DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), is(
                "SELECT AVG(x.age), COUNT(x.age) as AVG_DERIVED_COUNT_0, SUM(x.age) as AVG_DERIVED_SUM_0 FROM table_1 x"));
    }
    
    @Test
    public void assertRewriteForAutoGeneratedKeyColumn() {
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.setParametersIndex(2);
        insertStatement.addSQLToken(new TableToken(12, 18, "table_x", QuoteCharacter.NONE));
        insertStatement.addSQLToken(new InsertValuesToken(39, 44));
        InsertColumnsToken insertColumnsToken = new InsertColumnsToken(30, true);
        insertColumnsToken.getColumns().add("id");
        insertStatement.addSQLToken(insertColumnsToken);
        InsertOptimizeResult insertOptimizeResult = new InsertOptimizeResult(Arrays.asList("name", "age", "id"));
        Object[] parameters = {"x", 1, 1};
        SQLExpression[] sqlExpressions = {new SQLParameterMarkerExpression(0), new SQLParameterMarkerExpression(1), new SQLParameterMarkerExpression(2)};
        insertOptimizeResult.addUnit(sqlExpressions, parameters, 3);
        insertOptimizeResult.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        RoutingUnit routingUnit = new RoutingUnit("db0");
        routingUnit.getTableUnits().add(new TableUnit("table_x", "table_1"));
        routeResult = new SQLRouteResult(insertStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(insertOptimizeResult));
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, "INSERT INTO table_x (name, age) VALUES (?, ?)", 
                DatabaseType.MySQL, routeResult, Arrays.asList(parameters), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(routingUnit, tableTokens).getSql(), is("INSERT INTO table_1 (name, age, id) VALUES (?, ?, ?)"));
    }
    
    @Test
    public void assertRewriteForAutoGeneratedKeyColumnWithoutColumnsWithParameter() {
        insertStatement.getColumnNames().add("name");
        insertStatement.getColumnNames().add("id");
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.setParametersIndex(1);
        insertStatement.addSQLToken(new TableToken(12, 20, "`table_x`", QuoteCharacter.BACK_QUOTE));
        insertStatement.addSQLToken(new InsertValuesToken(29, 31));
        InsertColumnsToken insertColumnsToken = new InsertColumnsToken(21, false);
        insertColumnsToken.getColumns().add("name");
        insertColumnsToken.getColumns().add("id");
        insertStatement.addSQLToken(insertColumnsToken);
        InsertOptimizeResult insertOptimizeResult = new InsertOptimizeResult(Arrays.asList("name", "id"));
        Object[] parameters = {"Bill", 1};
        SQLExpression[] sqlExpressions = {new SQLParameterMarkerExpression(0), new SQLParameterMarkerExpression(1)};
        insertOptimizeResult.addUnit(sqlExpressions, parameters, 2);
        insertOptimizeResult.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        RoutingUnit routingUnit = new RoutingUnit("db0");
        routingUnit.getTableUnits().add(new TableUnit("table_x", "table_1"));
        routeResult = new SQLRouteResult(insertStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(insertOptimizeResult));
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, 
                "INSERT INTO `table_x` VALUES (?)", DatabaseType.MySQL, routeResult, Arrays.asList(parameters), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(routingUnit, tableTokens).getSql(), is("INSERT INTO `table_1`(name, id) VALUES (?, ?)"));
    }
    
    @Test
    public void assertRewriteForAutoGeneratedKeyColumnWithoutColumnsWithoutParameter() {
        insertStatement.getColumnNames().add("name");
        insertStatement.getColumnNames().add("id");
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.addSQLToken(new TableToken(12, 20, "`table_x`", QuoteCharacter.BACK_QUOTE));
        insertStatement.addSQLToken(new InsertValuesToken(29, 32));
        InsertColumnsToken insertColumnsToken = new InsertColumnsToken(21, false);
        insertColumnsToken.getColumns().add("name");
        insertColumnsToken.getColumns().add("id");
        insertStatement.addSQLToken(insertColumnsToken);
        InsertOptimizeResult insertOptimizeResult = new InsertOptimizeResult(Arrays.asList("name", "id"));
        SQLExpression[] sqlExpressions = {new SQLNumberExpression(10), new SQLNumberExpression(1)};
        insertOptimizeResult.addUnit(sqlExpressions, new Object[0], 0);
        insertOptimizeResult.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        RoutingUnit routingUnit = new RoutingUnit("db0");
        routingUnit.getTableUnits().add(new TableUnit("table_x", "table_1"));
        routeResult = new SQLRouteResult(insertStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(insertOptimizeResult));
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, "INSERT INTO `table_x` VALUES (10)", 
                DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(routingUnit, tableTokens).getSql(), is("INSERT INTO `table_1`(name, id) VALUES (10, 1)"));
    }
    
    @Test
    public void assertRewriteForDuplicateKeyWithoutColumnsWithoutParameter() {
        insertStatement.getColumnNames().add("name");
        insertStatement.getColumnNames().add("id");
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.addSQLToken(new TableToken(12, 20, "`table_x`", QuoteCharacter.BACK_QUOTE));
        insertStatement.addSQLToken(new InsertValuesToken(29, 32));
        InsertColumnsToken insertColumnsToken = new InsertColumnsToken(21, false);
        insertColumnsToken.getColumns().add("name");
        insertColumnsToken.getColumns().add("id");
        insertStatement.addSQLToken(insertColumnsToken);
        InsertOptimizeResult insertOptimizeResult = new InsertOptimizeResult(Arrays.asList("name", "id"));
        SQLExpression[] sqlExpressions = {new SQLNumberExpression(10), new SQLNumberExpression(1)};
        insertOptimizeResult.addUnit(sqlExpressions, new Object[0], 0);
        insertOptimizeResult.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        RoutingUnit routingUnit = new RoutingUnit("db0");
        routingUnit.getTableUnits().add(new TableUnit("table_x", "table_1"));
        routeResult = new SQLRouteResult(insertStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setOptimizeResult(new OptimizeResult(insertOptimizeResult));
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, "INSERT INTO `table_x` VALUES (10) ON DUPLICATE KEY UPDATE name = VALUES(name)",
                DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(routingUnit, tableTokens).getSql(),
                is("INSERT INTO `table_1`(name, id) VALUES (10, 1) ON DUPLICATE KEY UPDATE name = VALUES(name)"));
    }
    
    @Test
    public void assertRewriteForDuplicateKeyWithSetWithoutParameter() {
        insertStatement.getColumnNames().add("name");
        insertStatement.getColumnNames().add("id");
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.addSQLToken(new TableToken(12, 20, "`table_x`", QuoteCharacter.BACK_QUOTE));
        InsertOptimizeResult insertOptimizeResult = new InsertOptimizeResult(Arrays.asList("name", "id"));
        SQLExpression[] sqlExpressions = {new SQLNumberExpression(10), new SQLNumberExpression(1)};
        insertOptimizeResult.addUnit(sqlExpressions, new Object[0], 0);
        insertOptimizeResult.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        RoutingUnit routingUnit = new RoutingUnit("db0");
        routingUnit.getTableUnits().add(new TableUnit("table_x", "table_1"));
        routeResult = new SQLRouteResult(insertStatement);
        routeResult.setOptimizeResult(new OptimizeResult(insertOptimizeResult));
        routeResult.setRoutingResult(new RoutingResult());
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, "INSERT INTO `table_x` set name = 10, id = 1 ON DUPLICATE KEY UPDATE name = VALUES(name)",
                DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(routingUnit, tableTokens).getSql(),
                is("INSERT INTO `table_1` set name = 10, id = 1 ON DUPLICATE KEY UPDATE name = VALUES(name)"));
    }
    
    @Test
    public void assertRewriteColumnWithoutColumnsWithoutParameter() {
        insertStatement.getColumnNames().add("name");
        insertStatement.getColumnNames().add("id");
        insertStatement.getTables().add(new Table("table_x", null));
        List<Object> parameters = new ArrayList<>(2);
        parameters.add("x");
        parameters.add(1);
        insertStatement.addSQLToken(new TableToken(12, 20, "`table_x`", QuoteCharacter.BACK_QUOTE));
        insertStatement.addSQLToken(new InsertValuesToken(29, 35));
        InsertColumnsToken insertColumnsToken = new InsertColumnsToken(21, false);
        insertColumnsToken.getColumns().add("name");
        insertColumnsToken.getColumns().add("id");
        insertStatement.addSQLToken(insertColumnsToken);
        InsertOptimizeResult insertOptimizeResult = new InsertOptimizeResult(Arrays.asList("name", "id"));
        SQLExpression[] sqlExpressions = {new SQLNumberExpression(10), new SQLNumberExpression(1)};
        insertOptimizeResult.addUnit(sqlExpressions, new Object[0], 0);
        insertOptimizeResult.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        RoutingUnit routingUnit = new RoutingUnit("db0");
        routingUnit.getTableUnits().add(new TableUnit("table_x", "table_1"));
        routeResult = new SQLRouteResult(insertStatement);
        routeResult.setOptimizeResult(new OptimizeResult(insertOptimizeResult));
        routeResult.setRoutingResult(new RoutingResult());
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, 
                "INSERT INTO `table_x` VALUES (10, 1)", DatabaseType.MySQL, routeResult, parameters, shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(routingUnit, tableTokens).getSql(), is("INSERT INTO `table_1`(name, id) VALUES (10, 1)"));
    }
    
    @Test
    public void assertRewriteColumnWithoutColumnsWithParameter() {
        insertStatement.getColumnNames().add("name");
        insertStatement.getColumnNames().add("id");
        insertStatement.getTables().add(new Table("table_x", null));
        insertStatement.addSQLToken(new TableToken(12, 20, "`table_x`", QuoteCharacter.BACK_QUOTE));
        insertStatement.addSQLToken(new InsertValuesToken(29, 34));
        InsertColumnsToken insertColumnsToken = new InsertColumnsToken(21, false);
        insertColumnsToken.getColumns().add("name");
        insertColumnsToken.getColumns().add("id");
        insertStatement.addSQLToken(insertColumnsToken);
        InsertOptimizeResult insertOptimizeResult = new InsertOptimizeResult(Arrays.asList("name", "id"));
        SQLExpression[] sqlExpressions = {new SQLParameterMarkerExpression(0), new SQLParameterMarkerExpression(1)};
        Object[] parameters = {"x", 1};
        insertOptimizeResult.addUnit(sqlExpressions, parameters, 2);
        insertOptimizeResult.getUnits().get(0).getDataNodes().add(new DataNode("db0.table_1"));
        RoutingUnit routingUnit = new RoutingUnit("db0");
        routingUnit.getTableUnits().add(new TableUnit("table_x", "table_1"));
        routeResult = new SQLRouteResult(insertStatement);
        routeResult.setOptimizeResult(new OptimizeResult(insertOptimizeResult));
        routeResult.setRoutingResult(new RoutingResult());
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, 
                "INSERT INTO `table_x` VALUES (?, ?)", DatabaseType.MySQL, routeResult, Arrays.asList(parameters), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(routingUnit, tableTokens).getSql(), is("INSERT INTO `table_1`(name, id) VALUES (?, ?)"));
    }
    
    @Test
    public void assertRewriteForLimit() {
        selectStatement.setLimit(new Limit());
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(2, -1, false));
        selectStatement.addSQLToken(new TableToken(17, 23, "table_x", QuoteCharacter.NONE));
        selectStatement.addSQLToken(new OffsetToken(33, 33, 2));
        selectStatement.addSQLToken(new RowCountToken(36, 36, 2));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        routeResult.setRoutingResult(new RoutingResult());
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, "SELECT x.id FROM table_x x LIMIT 2, 2", 
                DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), is("SELECT x.id FROM table_1 x LIMIT 0, 4"));
    }
    
    @Test
    public void assertRewriteForRowNum() {
        selectStatement.setLimit(new Limit());
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(4, -1, false));
        selectStatement.addSQLToken(new TableToken(68, 74, "table_x", QuoteCharacter.NONE));
        selectStatement.addSQLToken(new OffsetToken(119, 119, 2));
        selectStatement.addSQLToken(new RowCountToken(98, 98, 4));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        routeResult.setRoutingResult(new RoutingResult());
        ShardingSQLRewriteEngine rewriteEngine = 
                new ShardingSQLRewriteEngine(shardingRule, "SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_x x) row_ WHERE rownum<=4) t WHERE t.rownum_>2", 
                DatabaseType.Oracle, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(),
                is("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_1 x) row_ WHERE rownum<=4) t WHERE t.rownum_>0"));
    }
    
    @Test
    public void assertRewriteForTopAndRowNumber() {
        selectStatement.setLimit(new Limit());
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(4, -1, false));
        selectStatement.addSQLToken(new TableToken(85, 91, "table_x", QuoteCharacter.NONE));
        selectStatement.addSQLToken(new OffsetToken(123, 123, 2));
        selectStatement.addSQLToken(new RowCountToken(26, 26, 4));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        routeResult.setRoutingResult(new RoutingResult());
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, 
                "SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_x x) AS row_ WHERE row_.rownum_>2", 
                DatabaseType.SQLServer, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(),
                is("SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_1 x) AS row_ WHERE row_.rownum_>0"));
    }
    
    @Test
    public void assertRewriteForLimitForMemoryGroupBy() {
        selectStatement.setLimit(new Limit());
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(2, -1, false));
        selectStatement.getOrderByItems().add(new OrderItem("x", "id", OrderDirection.ASC, OrderDirection.ASC));
        selectStatement.getGroupByItems().add(new OrderItem("x", "id", OrderDirection.DESC, OrderDirection.ASC));
        selectStatement.addSQLToken(new TableToken(17, 23, "table_x", QuoteCharacter.NONE));
        selectStatement.addSQLToken(new OffsetToken(33, 33, 2));
        selectStatement.addSQLToken(new RowCountToken(36, 36, 2));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        routeResult.setRoutingResult(new RoutingResult());
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, "SELECT x.id FROM table_x x LIMIT 2, 2", 
                DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), is("SELECT x.id FROM table_1 x LIMIT 0, 2147483647"));
    }
    
    @Test
    public void assertRewriteForRowNumForMemoryGroupBy() {
        selectStatement.setLimit(new Limit());
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(4, -1, false));
        selectStatement.addSQLToken(new TableToken(68, 74, "table_x", QuoteCharacter.NONE));
        selectStatement.addSQLToken(new OffsetToken(119, 119, 2));
        selectStatement.addSQLToken(new RowCountToken(98, 98, 4));
        selectStatement.getOrderByItems().add(new OrderItem("x", "id", OrderDirection.ASC, OrderDirection.ASC));
        selectStatement.getGroupByItems().add(new OrderItem("x", "id", OrderDirection.DESC, OrderDirection.ASC));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        routeResult.setRoutingResult(new RoutingResult());
        ShardingSQLRewriteEngine rewriteEngine = 
                new ShardingSQLRewriteEngine(shardingRule, "SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_x x) row_ WHERE rownum<=4) t WHERE t.rownum_>2", 
                DatabaseType.Oracle, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(),
                is("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_1 x) row_ WHERE rownum<=2147483647) t WHERE t.rownum_>0"));
    }
    
    @Test
    public void assertRewriteForTopAndRowNumberForMemoryGroupBy() {
        selectStatement.setLimit(new Limit());
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(4, -1, false));
        selectStatement.addSQLToken(new TableToken(85, 91, "table_x", QuoteCharacter.NONE));
        selectStatement.addSQLToken(new OffsetToken(123, 123, 2));
        selectStatement.addSQLToken(new RowCountToken(26, 26, 4));
        selectStatement.getOrderByItems().add(new OrderItem("x", "id", OrderDirection.ASC, OrderDirection.ASC));
        selectStatement.getGroupByItems().add(new OrderItem("x", "id", OrderDirection.DESC, OrderDirection.ASC));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        routeResult.setRoutingResult(new RoutingResult());
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, 
                "SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_x x) AS row_ WHERE row_.rownum_>2", 
                DatabaseType.SQLServer, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(),
                is("SELECT * FROM (SELECT TOP(2147483647) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_1 x) AS row_ WHERE row_.rownum_>0"));
    }
    
    @Test
    public void assertRewriteForLimitForNotRewriteLimit() {
        selectStatement.setLimit(new Limit());
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(2, -1, false));
        selectStatement.addSQLToken(new TableToken(17, 23, "table_x", QuoteCharacter.NONE));
        selectStatement.addSQLToken(new OffsetToken(33, 33, 2));
        selectStatement.addSQLToken(new RowCountToken(36, 36, 2));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, "SELECT x.id FROM table_x x LIMIT 2, 2", 
                DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), is("SELECT x.id FROM table_1 x LIMIT 2, 2"));
    }
    
    @Test
    public void assertRewriteForRowNumForNotRewriteLimit() {
        selectStatement.setLimit(new Limit());
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(4, -1, false));
        selectStatement.addSQLToken(new TableToken(68, 74, "table_x", QuoteCharacter.NONE));
        selectStatement.addSQLToken(new OffsetToken(119, 119, 2));
        selectStatement.addSQLToken(new RowCountToken(98, 98, 4));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        ShardingSQLRewriteEngine rewriteEngine = 
                new ShardingSQLRewriteEngine(shardingRule, "SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_x x) row_ WHERE rownum<=4) t WHERE t.rownum_>2", 
                        DatabaseType.Oracle, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(),
                is("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_1 x) row_ WHERE rownum<=4) t WHERE t.rownum_>2"));
    }
    
    @Test
    public void assertRewriteForTopAndRowNumberForNotRewriteLimit() {
        selectStatement.setLimit(new Limit());
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(4, -1, false));
        selectStatement.addSQLToken(new TableToken(85, 91, "table_x", QuoteCharacter.NONE));
        selectStatement.addSQLToken(new OffsetToken(123, 123, 2));
        selectStatement.addSQLToken(new RowCountToken(26, 26, 4));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, 
                "SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_x x) AS row_ WHERE row_.rownum_>2", 
                DatabaseType.SQLServer, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(),
                is("SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_1 x) AS row_ WHERE row_.rownum_>2"));
    }
    
    @Test
    public void assertRewriteForDerivedOrderBy() {
        selectStatement.setGroupByLastIndex(60);
        selectStatement.getOrderByItems().add(new OrderItem("x", "id", OrderDirection.ASC, OrderDirection.ASC));
        selectStatement.getOrderByItems().add(new OrderItem("x", "name", OrderDirection.DESC, OrderDirection.ASC));
        selectStatement.addSQLToken(new TableToken(25, 31, "table_x", QuoteCharacter.NONE));
        selectStatement.addSQLToken(new OrderByToken(61));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setRoutingResult(new RoutingResult());
        routeResult.setLimit(selectStatement.getLimit());
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, 
                "SELECT x.id, x.name FROM table_x x GROUP BY x.id, x.name DESC", DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), is(
                "SELECT x.id, x.name FROM table_1 x GROUP BY x.id, x.name DESC ORDER BY id ASC,name DESC "));
    }
    
    @Test
    public void assertGenerateSQL() {
        List<Object> parameters = new ArrayList<>(2);
        parameters.add(1);
        parameters.add("x");
        selectStatement.addSQLToken(new TableToken(7, 13, "table_x", QuoteCharacter.NONE));
        selectStatement.addSQLToken(new TableToken(31, 37, "table_x", QuoteCharacter.NONE));
        selectStatement.addSQLToken(new TableToken(58, 64, "table_x", QuoteCharacter.NONE));
        selectStatement.getTables().add(new Table("table_x", "x"));
        selectStatement.getTables().add(new Table("table_y", "y"));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        routeResult.setRoutingResult(new RoutingResult());
        ShardingSQLRewriteEngine shardingSqlRewriteEngine =
                new ShardingSQLRewriteEngine(shardingRule, "SELECT table_x.id, x.name FROM table_x x, table_y y WHERE table_x.id=? AND x.name=?", DatabaseType.MySQL, routeResult, parameters, null);
        RoutingUnit routingUnit = new RoutingUnit("db0");
        routingUnit.getTableUnits().add(new TableUnit("table_x", "table_x"));
        assertThat(shardingSqlRewriteEngine.generateSQL(routingUnit).getSql(), is("SELECT table_x.id, x.name FROM table_x x, table_y y WHERE table_x.id=? AND x.name=?"));
    }
    
    @Test
    public void assertSchemaTokenRewriteForTableName() {
        tableTokens = new HashMap<>(1, 1);
        tableTokens.put("table_x", "table_y");
        selectStatement.addSQLToken(new TableToken(18, 24, "table_x", QuoteCharacter.NONE));
        selectStatement.addSQLToken(new SchemaToken(29, 35, "db0", "table_x"));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        routeResult.setRoutingResult(new RoutingResult());
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, "SHOW CREATE TABLE table_x ON table_x", 
                DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), is("SHOW CREATE TABLE table_y ON db0"));
    }
    
    @Test
    public void assertIndexTokenForIndexNameTableName() {
        selectStatement.addSQLToken(new IndexToken(13, 22, "index_name", QuoteCharacter.NONE, "table_x"));
        selectStatement.addSQLToken(new TableToken(27, 33, "table_x", QuoteCharacter.NONE));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        routeResult.setRoutingResult(new RoutingResult());
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, "CREATE INDEX index_name ON table_x ('column')", 
                DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), is("CREATE INDEX index_name_table_1 ON table_1 ('column')"));
    }
    
    @SneakyThrows
    private SQLBuilder getSQLBuilder(final ShardingSQLRewriteEngine rewriteEngine) {
        Field field = rewriteEngine.getClass().getDeclaredField("sqlBuilder");
        field.setAccessible(true);
        return (SQLBuilder) field.get(rewriteEngine);
    }
    
    @Test
    public void assertIndexTokenForIndexNameTableNameWithoutLogicTableName() {
        selectStatement.addSQLToken(new IndexToken(13, 23, "logic_index", QuoteCharacter.NONE, "table_x"));
        selectStatement.addSQLToken(new TableToken(28, 34, "table_x", QuoteCharacter.NONE));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        routeResult.setRoutingResult(new RoutingResult());
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, "CREATE INDEX logic_index ON table_x ('column')", 
                DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), is("CREATE INDEX logic_index_table_1 ON table_1 ('column')"));
    }
    
    @Test
    public void assertTableTokenWithoutBackQuoteForShow() {
        showTablesStatement.addSQLToken(new TableToken(18, 24, "table_x", QuoteCharacter.NONE));
        routeResult = new SQLRouteResult(showTablesStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, "SHOW COLUMNS FROM table_x", 
                DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), is("SHOW COLUMNS FROM table_1"));
    }
    
    @Test
    public void assertTableTokenWithoutBackQuoteFromSchemaForShow() {
        showTablesStatement.addSQLToken(new TableToken(18, 24, "table_x", QuoteCharacter.NONE));
        showTablesStatement.addSQLToken(new SchemaToken(31, 43, "db0", "table_x"));
        routeResult = new SQLRouteResult(showTablesStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, "SHOW COLUMNS FROM table_x FROM 'sharding_db'", 
                DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        Map<String, String> logicAndActualTableMap = new LinkedHashMap<>();
        logicAndActualTableMap.put("table_x", "table_x");
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, logicAndActualTableMap).getSql(), is("SHOW COLUMNS FROM table_x FROM db0"));
    }
    
    @Test
    public void assertTableTokenWithBackQuoteForShow() {
        showTablesStatement.addSQLToken(new TableToken(18, 26, "`table_x`", QuoteCharacter.BACK_QUOTE));
        routeResult = new SQLRouteResult(showTablesStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, "SHOW COLUMNS FROM `table_x`", 
                DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), is("SHOW COLUMNS FROM `table_1`"));
    }
    
    @Test
    public void assertTableTokenWithBackQuoteFromSchemaForShow() {
        showTablesStatement.addSQLToken(new TableToken(18, 26, "`table_x`", QuoteCharacter.BACK_QUOTE));
        routeResult = new SQLRouteResult(showTablesStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, "SHOW COLUMNS FROM `table_x` FROM 'sharding_db'", 
                DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), is("SHOW COLUMNS FROM `table_1` FROM 'sharding_db'"));
    }
    
    @Test
    public void assertTableTokenWithSchemaForShow() {
        showTablesStatement.addSQLToken(new TableToken(18, 36, "table_x", QuoteCharacter.NONE));
        routeResult = new SQLRouteResult(showTablesStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, "SHOW COLUMNS FROM sharding_db.table_x", 
                DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), is("SHOW COLUMNS FROM table_1"));
    }
    
    @Test
    public void assertTableTokenWithSchemaFromSchemaForShow() {
        showTablesStatement.addSQLToken(new TableToken(18, 36, "table_x", QuoteCharacter.NONE));
        routeResult = new SQLRouteResult(showTablesStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, 
                "SHOW COLUMNS FROM sharding_db.table_x FROM sharding_db", DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), is("SHOW COLUMNS FROM table_1 FROM sharding_db"));
    }
    
    @Test
    public void assertTableTokenWithBackQuoteWithSchemaForShow() {
        showTablesStatement.addSQLToken(new TableToken(18, 38, "`table_x`", QuoteCharacter.BACK_QUOTE));
        routeResult = new SQLRouteResult(showTablesStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, "SHOW COLUMNS FROM sharding_db.`table_x`", 
                DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), is("SHOW COLUMNS FROM `table_1`"));
    }
    
    @Test
    public void assertTableTokenWithBackQuoteWithSchemaFromSchemaForShow() {
        showTablesStatement.addSQLToken(new TableToken(18, 38, "`table_x`", QuoteCharacter.BACK_QUOTE));
        routeResult = new SQLRouteResult(showTablesStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, 
                "SHOW COLUMNS FROM sharding_db.`table_x` FROM sharding_db", DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), is("SHOW COLUMNS FROM `table_1` FROM sharding_db"));
    }
    
    @Test
    public void assertTableTokenWithSchemaWithBackQuoteForShow() {
        showTablesStatement.addSQLToken(new TableToken(18, 40, "`table_x`", QuoteCharacter.BACK_QUOTE));
        routeResult = new SQLRouteResult(showTablesStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, "SHOW COLUMNS FROM `sharding_db`.`table_x`", 
                DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), is("SHOW COLUMNS FROM `table_1`"));
    }
    
    @Test
    public void assertTableTokenWithSchemaWithBackQuoteFromSchemaForShow() {
        showTablesStatement.addSQLToken(new TableToken(18, 40, "`table_x`", QuoteCharacter.BACK_QUOTE));
        routeResult = new SQLRouteResult(showTablesStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, 
                "SHOW COLUMNS FROM `sharding_db`.`table_x` FROM sharding_db", DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), is("SHOW COLUMNS FROM `table_1` FROM sharding_db"));
    }
    
    @Test
    public void assertTableTokenWithSchemaForSelect() {
        selectStatement.addSQLToken(new TableToken(14, 32, "table_x", QuoteCharacter.NONE));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, "SELECT * FROM sharding_db.table_x", 
                DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), is("SELECT * FROM table_1"));
    }
    
    @Test
    public void assertTableTokenWithSchemaForInsert() {
        insertStatement.addSQLToken(new TableToken(12, 30, "table_x", QuoteCharacter.NONE));
        routeResult = new SQLRouteResult(insertStatement);
        routeResult.setRoutingResult(new RoutingResult());
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, 
                "INSERT INTO sharding_db.table_x (order_id, user_id, status) values (1, 1, 'OK')", DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(
                null, tableTokens).getSql(), is("INSERT INTO table_1 (order_id, user_id, status) values (1, 1, 'OK')"));
    }
    
    @Test
    public void assertTableTokenWithSchemaForUpdate() {
        updateStatement.addSQLToken(new TableToken(7, 27, "table_x", QuoteCharacter.NONE));
        routeResult = new SQLRouteResult(updateStatement);
        routeResult.setRoutingResult(new RoutingResult());
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, 
                "UPDATE `sharding_db`.table_x SET user_id=1 WHERE order_id=1", DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), is("UPDATE table_1 SET user_id=1 WHERE order_id=1"));
    }
    
    @Test
    public void assertTableTokenWithSchemaForDelete() {
        deleteStatement.addSQLToken(new TableToken(12, 34, "`table_x`", QuoteCharacter.BACK_QUOTE));
        routeResult = new SQLRouteResult(deleteStatement);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit("ds"));
        routeResult.setRoutingResult(routingResult);
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule, "DELETE FROM `sharding_db`.`table_x` WHERE user_id=1", 
                DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), is("DELETE FROM `table_1` WHERE user_id=1"));
    }
    
    @Test
    public void assertSelectEqualWithShardingEncryptor() {
        List<Object> parameters = new ArrayList<>(2);
        parameters.add(1);
        parameters.add("x");
        Column column = new Column("id", "table_z");
        selectStatement.addSQLToken(new TableToken(15, 21, "table_z", QuoteCharacter.NONE));
        selectStatement.addSQLToken(new EncryptColumnToken(29, 32, column, true));
        selectStatement.getEncryptCondition().getOrConditions().add(new AndCondition());
        selectStatement.getEncryptCondition().getOrConditions().get(0).getConditions().add(new Condition(column, new SQLParameterMarkerExpression(0)));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        routeResult.setRoutingResult(new RoutingResult());
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule,
                "SELECT id FROM table_z WHERE id=? AND name=?", DatabaseType.MySQL, routeResult, parameters, null);
        SQLUnit sqlUnit = getSQLBuilder(rewriteEngine).toSQL(null, tableTokens);
        assertThat(sqlUnit.getSql(), is("SELECT id FROM table_z WHERE id = ? AND name=?"));
        assertThat(sqlUnit.getParameters().get(0), is((Object) "encryptValue"));
    }
    
    @Test
    public void assertSelectInWithShardingEncryptor() {
        Column column = new Column("id", "table_z");
        selectStatement.addSQLToken(new TableToken(15, 21, "table_z", QuoteCharacter.NONE));
        selectStatement.addSQLToken(new EncryptColumnToken(29, 39, column, true));
        selectStatement.getEncryptCondition().getOrConditions().add(new AndCondition());
        List<SQLExpression> sqlExpressions = new LinkedList<>();
        sqlExpressions.add(new SQLNumberExpression(3));
        sqlExpressions.add(new SQLNumberExpression(5));
        selectStatement.getEncryptCondition().getOrConditions().get(0).getConditions().add(new Condition(column, sqlExpressions));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        routeResult.setRoutingResult(new RoutingResult());
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule,
                "SELECT id FROM table_z WHERE id in (3,5)", DatabaseType.MySQL, routeResult, new LinkedList<>(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), 
                is("SELECT id FROM table_z WHERE id IN ('encryptValue', 'encryptValue')"));
    }
    
    @Test
    public void assertSelectInWithShardingEncryptorWithParameter() {
        List<Object> parameters = new ArrayList<>(2);
        parameters.add(1);
        parameters.add(2);
        Column column = new Column("id", "table_z");
        selectStatement.addSQLToken(new TableToken(15, 21, "table_z", QuoteCharacter.NONE));
        selectStatement.addSQLToken(new EncryptColumnToken(29, 40, column, true));
        selectStatement.addSQLToken(new EncryptColumnToken(45, 50, column, true));
        selectStatement.getEncryptCondition().getOrConditions().add(new AndCondition());
        selectStatement.getEncryptCondition().getOrConditions().add(new AndCondition());
        List<SQLExpression> sqlExpressions = new LinkedList<>();
        sqlExpressions.add(new SQLParameterMarkerExpression(0));
        sqlExpressions.add(new SQLParameterMarkerExpression(1));
        selectStatement.getEncryptCondition().getOrConditions().get(0).getConditions().add(new Condition(column, sqlExpressions));
        selectStatement.getEncryptCondition().getOrConditions().get(1).getConditions().add(new Condition(column, new SQLNumberExpression(3)));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        routeResult.setRoutingResult(new RoutingResult());
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule,
                "SELECT id FROM table_z WHERE id in (?, ?) or id = 3", DatabaseType.MySQL, routeResult, parameters, null);
        SQLUnit sqlUnit = getSQLBuilder(rewriteEngine).toSQL(null, tableTokens);
        assertThat(sqlUnit.getSql(), is("SELECT id FROM table_z WHERE id IN (?, ?) or id = 'encryptValue'"));
        assertThat(sqlUnit.getParameters().get(0), is((Object) "encryptValue"));
        assertThat(sqlUnit.getParameters().get(1), is((Object) "encryptValue"));
    }
    
    @Test
    public void assertSelectEqualWithQueryAssistedShardingEncryptor() {
        List<Object> parameters = new ArrayList<>(2);
        parameters.add(1);
        parameters.add("k");
        Column column = new Column("id", "table_k");
        selectStatement.addSQLToken(new TableToken(15, 21, "table_k", QuoteCharacter.NONE));
        selectStatement.addSQLToken(new EncryptColumnToken(29, 32, column, true));
        selectStatement.getEncryptCondition().getOrConditions().add(new AndCondition());
        selectStatement.getEncryptCondition().getOrConditions().get(0).getConditions().add(new Condition(column, new SQLParameterMarkerExpression(0)));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        routeResult.setRoutingResult(new RoutingResult());
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule,
                "SELECT id FROM table_k WHERE id=? AND name=?", DatabaseType.MySQL, routeResult, parameters, null);
        SQLUnit sqlUnit = getSQLBuilder(rewriteEngine).toSQL(null, tableTokens);
        assertThat(sqlUnit.getSql(), is("SELECT id FROM table_k WHERE query_id = ? AND name=?"));
        assertThat(sqlUnit.getParameters().get(0), is((Object) "assistedEncryptValue"));
    }
    
    @Test
    public void assertSelectInWithQueryAssistedShardingEncryptor() {
        Column column = new Column("id", "table_k");
        selectStatement.addSQLToken(new TableToken(15, 21, "table_k", QuoteCharacter.NONE));
        selectStatement.addSQLToken(new EncryptColumnToken(29, 39, column, true));
        selectStatement.getEncryptCondition().getOrConditions().add(new AndCondition());
        List<SQLExpression> sqlExpressions = new LinkedList<>();
        sqlExpressions.add(new SQLNumberExpression(3));
        sqlExpressions.add(new SQLNumberExpression(5));
        selectStatement.getEncryptCondition().getOrConditions().get(0).getConditions().add(new Condition(column, sqlExpressions));
        routeResult = new SQLRouteResult(selectStatement);
        routeResult.setLimit(selectStatement.getLimit());
        routeResult.setRoutingResult(new RoutingResult());
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule,
                "SELECT id FROM table_k WHERE id in (3,5)", DatabaseType.MySQL, routeResult, new LinkedList<>(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), 
                is("SELECT id FROM table_k WHERE query_id IN ('assistedEncryptValue', 'assistedEncryptValue')"));
    }
    
    @Test
    public void assertUpdateWithShardingEncryptor() {
        Column column = new Column("id", "table_z");
        updateStatement.addSQLToken(new TableToken(7, 13, "table_z", QuoteCharacter.NONE));
        updateStatement.addSQLToken(new EncryptColumnToken(19, 24, column, false));
        updateStatement.getAssignments().put(column, new SQLNumberExpression(1));
        updateStatement.addSQLToken(new EncryptColumnToken(32, 37, column, true));
        updateStatement.getEncryptCondition().getOrConditions().add(new AndCondition());
        updateStatement.getEncryptCondition().getOrConditions().get(0).getConditions().add(new Condition(column, new SQLNumberExpression(2)));
        routeResult = new SQLRouteResult(updateStatement);
        routeResult.setRoutingResult(new RoutingResult());
        ShardingSQLRewriteEngine rewriteEngine = new ShardingSQLRewriteEngine(shardingRule,
                "UPDATE table_z SET id = 1 WHERE id = 2", DatabaseType.MySQL, routeResult, Collections.emptyList(), shardingDataSourceMetaData);
        assertThat(getSQLBuilder(rewriteEngine).toSQL(null, tableTokens).getSql(), 
                is("UPDATE table_z SET id = 'encryptValue' WHERE id = 'encryptValue'"));
    }
}
