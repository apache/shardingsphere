/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.rewrite;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.constant.OrderDirection;
import io.shardingjdbc.core.parsing.parser.context.OrderItem;
import io.shardingjdbc.core.parsing.parser.context.limit.Limit;
import io.shardingjdbc.core.parsing.parser.context.limit.LimitValue;
import io.shardingjdbc.core.parsing.parser.context.table.Table;
import io.shardingjdbc.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingjdbc.core.parsing.parser.token.GeneratedKeyToken;
import io.shardingjdbc.core.parsing.parser.token.IndexToken;
import io.shardingjdbc.core.parsing.parser.token.ItemsToken;
import io.shardingjdbc.core.parsing.parser.token.OffsetToken;
import io.shardingjdbc.core.parsing.parser.token.OrderByToken;
import io.shardingjdbc.core.parsing.parser.token.RowCountToken;
import io.shardingjdbc.core.parsing.parser.token.SchemaToken;
import io.shardingjdbc.core.parsing.parser.token.TableToken;
import io.shardingjdbc.core.routing.condition.GeneratedKey;
import io.shardingjdbc.core.routing.type.TableUnit;
import io.shardingjdbc.core.routing.type.complex.CartesianTableReference;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.yaml.sharding.YamlShardingConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class SQLRewriteEngineTest {
    
    private ShardingRule shardingRule;
    
    private SelectStatement selectStatement;
    
    private InsertStatement insertStatement;
    
    private Map<String, String> tableTokens;
    
    @Before
    public void setUp() throws IOException {
        URL url = SQLRewriteEngineTest.class.getClassLoader().getResource("yaml/rewrite-rule.yaml");
        Preconditions.checkNotNull(url, "Cannot found rewrite rule yaml configuration.");
        YamlShardingConfiguration yamlShardingConfig = YamlShardingConfiguration.unmarshal(new File(url.getFile()));
        shardingRule = yamlShardingConfig.getShardingRule(yamlShardingConfig.getDataSources().keySet());
        selectStatement = new SelectStatement();
        insertStatement = new InsertStatement();
        tableTokens = new HashMap<>(1, 1);
        tableTokens.put("table_x", "table_1");
    }
    
    @Test
    public void assertRewriteWithoutChange() {
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SELECT table_y.id FROM table_y WHERE table_y.id=?", DatabaseType.MySQL, selectStatement, null);
        assertThat(rewriteEngine.rewrite(true).toSQL(tableTokens, null), is("SELECT table_y.id FROM table_y WHERE table_y.id=?"));
    }
    
    @Test
    public void assertRewriteForTableName() {
        selectStatement.getSqlTokens().add(new TableToken(7, "table_x"));
        selectStatement.getSqlTokens().add(new TableToken(31, "table_x"));
        selectStatement.getSqlTokens().add(new TableToken(47, "table_x"));
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SELECT table_x.id, x.name FROM table_x x WHERE table_x.id=? AND x.name=?", DatabaseType.MySQL, selectStatement, null);
        assertThat(rewriteEngine.rewrite(true).toSQL(tableTokens, null), is("SELECT table_1.id, x.name FROM table_1 x WHERE table_1.id=? AND x.name=?"));
    }
    
    @Test
    public void assertRewriteForOrderByAndGroupByDerivedColumns() {
        selectStatement.getSqlTokens().add(new TableToken(18, "table_x"));
        ItemsToken itemsToken = new ItemsToken(12);
        itemsToken.getItems().addAll(Arrays.asList("x.id as ORDER_BY_DERIVED_0", "x.name as GROUP_BY_DERIVED_0"));
        selectStatement.getSqlTokens().add(itemsToken);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SELECT x.age FROM table_x x GROUP BY x.id ORDER BY x.name", DatabaseType.MySQL, selectStatement, null);
        assertThat(rewriteEngine.rewrite(true).toSQL(tableTokens, null), is("SELECT x.age, x.id as ORDER_BY_DERIVED_0, x.name as GROUP_BY_DERIVED_0 FROM table_1 x GROUP BY x.id ORDER BY x.name"));
    }
    
    @Test
    public void assertRewriteForAggregationDerivedColumns() {
        selectStatement.getSqlTokens().add(new TableToken(23, "table_x"));
        ItemsToken itemsToken = new ItemsToken(17);
        itemsToken.getItems().addAll(Arrays.asList("COUNT(x.age) as AVG_DERIVED_COUNT_0", "SUM(x.age) as AVG_DERIVED_SUM_0"));
        selectStatement.getSqlTokens().add(itemsToken);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SELECT AVG(x.age) FROM table_x x", DatabaseType.MySQL, selectStatement, null);
        assertThat(rewriteEngine.rewrite(true).toSQL(tableTokens, null), is("SELECT AVG(x.age), COUNT(x.age) as AVG_DERIVED_COUNT_0, SUM(x.age) as AVG_DERIVED_SUM_0 FROM table_1 x"));
    }
    
    @Test
    public void assertRewriteForAutoGeneratedKeyColumn() {
        insertStatement.setParametersIndex(2);
        insertStatement.getSqlTokens().add(new TableToken(12, "table_x"));
        ItemsToken itemsToken = new ItemsToken(30);
        itemsToken.getItems().add("id");
        insertStatement.getSqlTokens().add(itemsToken);
        insertStatement.getSqlTokens().add(new GeneratedKeyToken(44));
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "INSERT INTO table_x (name, age) VALUES (?, ?)", DatabaseType.MySQL, insertStatement, new GeneratedKey("id", 2, null));
        assertThat(rewriteEngine.rewrite(true).toSQL(tableTokens, null), is("INSERT INTO table_1 (name, age, id) VALUES (?, ?, ?)"));
    }
    
    @Test
    public void assertRewriteForLimit() {
        selectStatement.setLimit(new Limit(DatabaseType.MySQL));
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(2, -1, false));
        selectStatement.getSqlTokens().add(new TableToken(17, "table_x"));
        selectStatement.getSqlTokens().add(new OffsetToken(33, 2));
        selectStatement.getSqlTokens().add(new RowCountToken(36, 2));
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SELECT x.id FROM table_x x LIMIT 2, 2", DatabaseType.MySQL, selectStatement, null);
        assertThat(rewriteEngine.rewrite(true).toSQL(tableTokens, null), is("SELECT x.id FROM table_1 x LIMIT 0, 4"));
    }
    
    @Test
    public void assertRewriteForRowNum() {
        selectStatement.setLimit(new Limit(DatabaseType.Oracle));
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(4, -1, false));
        selectStatement.getSqlTokens().add(new TableToken(68, "table_x"));
        selectStatement.getSqlTokens().add(new OffsetToken(119, 2));
        selectStatement.getSqlTokens().add(new RowCountToken(98, 4));
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule,
                "SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_x x) row_ WHERE rownum<=4) t WHERE t.rownum_>2", DatabaseType.Oracle, selectStatement, null);
        assertThat(rewriteEngine.rewrite(true).toSQL(tableTokens, null), 
                is("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_1 x) row_ WHERE rownum<=4) t WHERE t.rownum_>0"));
    }
    
    @Test
    public void assertRewriteForTopAndRowNumber() {
        selectStatement.setLimit(new Limit(DatabaseType.SQLServer));
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(4, -1, false));
        selectStatement.getSqlTokens().add(new TableToken(85, "table_x"));
        selectStatement.getSqlTokens().add(new OffsetToken(123, 2));
        selectStatement.getSqlTokens().add(new RowCountToken(26, 4));
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule,
                "SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_x x) AS row_ WHERE row_.rownum_>2", DatabaseType.SQLServer, selectStatement, null);
        assertThat(rewriteEngine.rewrite(true).toSQL(tableTokens, null), 
                is("SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_1 x) AS row_ WHERE row_.rownum_>0"));
    }
    
    @Test
    public void assertRewriteForLimitForMemoryGroupBy() {
        selectStatement.setLimit(new Limit(DatabaseType.MySQL));
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(2, -1, false));
        selectStatement.getOrderByItems().add(new OrderItem("x", "id", OrderDirection.ASC, OrderDirection.ASC, Optional.<String>absent()));
        selectStatement.getGroupByItems().add(new OrderItem("x", "id", OrderDirection.DESC, OrderDirection.ASC, Optional.<String>absent()));
        selectStatement.getSqlTokens().add(new TableToken(17, "table_x"));
        selectStatement.getSqlTokens().add(new OffsetToken(33, 2));
        selectStatement.getSqlTokens().add(new RowCountToken(36, 2));
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SELECT x.id FROM table_x x LIMIT 2, 2", DatabaseType.MySQL, selectStatement, null);
        assertThat(rewriteEngine.rewrite(true).toSQL(tableTokens, null), is("SELECT x.id FROM table_1 x LIMIT 0, 2147483647"));
    }
    
    @Test
    public void assertRewriteForRowNumForMemoryGroupBy() {
        selectStatement.setLimit(new Limit(DatabaseType.Oracle));
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(4, -1, false));
        selectStatement.getSqlTokens().add(new TableToken(68, "table_x"));
        selectStatement.getSqlTokens().add(new OffsetToken(119, 2));
        selectStatement.getSqlTokens().add(new RowCountToken(98, 4));
        selectStatement.getOrderByItems().add(new OrderItem("x", "id", OrderDirection.ASC, OrderDirection.ASC, Optional.<String>absent()));
        selectStatement.getGroupByItems().add(new OrderItem("x", "id", OrderDirection.DESC, OrderDirection.ASC, Optional.<String>absent()));
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule,
                "SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_x x) row_ WHERE rownum<=4) t WHERE t.rownum_>2", DatabaseType.Oracle, selectStatement, null);
        assertThat(rewriteEngine.rewrite(true).toSQL(tableTokens, null), 
                is("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_1 x) row_ WHERE rownum<=2147483647) t WHERE t.rownum_>0"));
    }
    
    @Test
    public void assertRewriteForTopAndRowNumberForMemoryGroupBy() {
        selectStatement.setLimit(new Limit(DatabaseType.SQLServer));
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(4, -1, false));
        selectStatement.getSqlTokens().add(new TableToken(85, "table_x"));
        selectStatement.getSqlTokens().add(new OffsetToken(123, 2));
        selectStatement.getSqlTokens().add(new RowCountToken(26, 4));
        selectStatement.getOrderByItems().add(new OrderItem("x", "id", OrderDirection.ASC, OrderDirection.ASC, Optional.<String>absent()));
        selectStatement.getGroupByItems().add(new OrderItem("x", "id", OrderDirection.DESC, OrderDirection.ASC, Optional.<String>absent()));
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule,
                "SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_x x) AS row_ WHERE row_.rownum_>2", DatabaseType.SQLServer, selectStatement, null);
        assertThat(rewriteEngine.rewrite(true).toSQL(tableTokens, null), 
                is("SELECT * FROM (SELECT TOP(2147483647) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_1 x) AS row_ WHERE row_.rownum_>0"));
    }
    
    @Test
    public void assertRewriteForLimitForNotRewriteLimit() {
        selectStatement.setLimit(new Limit(DatabaseType.MySQL));
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(2, -1, false));
        selectStatement.getSqlTokens().add(new TableToken(17, "table_x"));
        selectStatement.getSqlTokens().add(new OffsetToken(33, 2));
        selectStatement.getSqlTokens().add(new RowCountToken(36, 2));
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SELECT x.id FROM table_x x LIMIT 2, 2", DatabaseType.MySQL, selectStatement, null);
        assertThat(rewriteEngine.rewrite(false).toSQL(tableTokens, null), is("SELECT x.id FROM table_1 x LIMIT 2, 2"));
    }
    
    @Test
    public void assertRewriteForRowNumForNotRewriteLimit() {
        selectStatement.setLimit(new Limit(DatabaseType.Oracle));
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(4, -1, false));
        selectStatement.getSqlTokens().add(new TableToken(68, "table_x"));
        selectStatement.getSqlTokens().add(new OffsetToken(119, 2));
        selectStatement.getSqlTokens().add(new RowCountToken(98, 4));
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule,
                "SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_x x) row_ WHERE rownum<=4) t WHERE t.rownum_>2", DatabaseType.Oracle, selectStatement, null);
        assertThat(rewriteEngine.rewrite(false).toSQL(tableTokens, null), 
                is("SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT x.id FROM table_1 x) row_ WHERE rownum<=4) t WHERE t.rownum_>2"));
    }
    
    @Test
    public void assertRewriteForTopAndRowNumberForNotRewriteLimit() {
        selectStatement.setLimit(new Limit(DatabaseType.SQLServer));
        selectStatement.getLimit().setOffset(new LimitValue(2, -1, true));
        selectStatement.getLimit().setRowCount(new LimitValue(4, -1, false));
        selectStatement.getSqlTokens().add(new TableToken(85, "table_x"));
        selectStatement.getSqlTokens().add(new OffsetToken(123, 2));
        selectStatement.getSqlTokens().add(new RowCountToken(26, 4));
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule,
                "SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_x x) AS row_ WHERE row_.rownum_>2", DatabaseType.SQLServer, selectStatement, null);
        assertThat(rewriteEngine.rewrite(false).toSQL(tableTokens, null), 
                is("SELECT * FROM (SELECT TOP(4) row_number() OVER (ORDER BY x.id) AS rownum_, x.id FROM table_1 x) AS row_ WHERE row_.rownum_>2"));
    }
    
    @Test
    public void assertRewriteForDerivedOrderBy() {
        selectStatement.setGroupByLastPosition(61);
        selectStatement.getOrderByItems().add(new OrderItem("x", "id", OrderDirection.ASC, OrderDirection.ASC, Optional.<String>absent()));
        selectStatement.getOrderByItems().add(new OrderItem("x", "name", OrderDirection.DESC, OrderDirection.ASC, Optional.<String>absent()));
        selectStatement.getSqlTokens().add(new TableToken(25, "table_x"));
        selectStatement.getSqlTokens().add(new OrderByToken(61));
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SELECT x.id, x.name FROM table_x x GROUP BY x.id, x.name DESC", DatabaseType.MySQL, selectStatement, null);
        assertThat(rewriteEngine.rewrite(true).toSQL(tableTokens, null), is("SELECT x.id, x.name FROM table_1 x GROUP BY x.id, x.name DESC ORDER BY id ASC,name DESC "));
    }
    
    @Test
    public void assertGenerateSQL() {
        selectStatement.getSqlTokens().add(new TableToken(7, "table_x"));
        selectStatement.getSqlTokens().add(new TableToken(31, "table_x"));
        selectStatement.getSqlTokens().add(new TableToken(58, "table_x"));
        selectStatement.getTables().add(new Table("table_x", Optional.of("x")));
        selectStatement.getTables().add(new Table("table_y", Optional.of("y")));
        SQLRewriteEngine sqlRewriteEngine = 
                new SQLRewriteEngine(shardingRule, "SELECT table_x.id, x.name FROM table_x x, table_y y WHERE table_x.id=? AND x.name=?", DatabaseType.MySQL, selectStatement, null);
        SQLBuilder sqlBuilder = sqlRewriteEngine.rewrite(true);
        assertThat(sqlRewriteEngine.generateSQL(new TableUnit("db0", "table_x", "table_x"), sqlBuilder), is("SELECT table_x.id, x.name FROM table_x x, table_y y WHERE table_x.id=? AND x.name=?"));
    }
    
    @Test
    public void assertGenerateSQLForCartesian() {
        selectStatement.getSqlTokens().add(new TableToken(7, "table_x"));
        selectStatement.getSqlTokens().add(new TableToken(31, "table_x"));
        selectStatement.getSqlTokens().add(new TableToken(47, "table_x"));
        SQLRewriteEngine sqlRewriteEngine = new SQLRewriteEngine(shardingRule, "SELECT table_x.id, x.name FROM table_x x WHERE table_x.id=? AND x.name=?", DatabaseType.MySQL, selectStatement, null);
        SQLBuilder sqlBuilder = sqlRewriteEngine.rewrite(true);
        CartesianTableReference cartesianTableReference = new CartesianTableReference(Collections.singletonList(new TableUnit("db0", "table_x", "table_x")));
        assertThat(sqlRewriteEngine.generateSQL(cartesianTableReference, sqlBuilder), is("SELECT table_x.id, x.name FROM table_x x WHERE table_x.id=? AND x.name=?"));
    }
    
    @Test
    public void assertSchemaTokenRewriteForTableName() {
        tableTokens = new HashMap<>(1, 1);
        tableTokens.put("table_x", "table_y");
        selectStatement.getSqlTokens().add(new TableToken(18, "table_x"));
        selectStatement.getSqlTokens().add(new SchemaToken(29, "table_x", "table_x"));
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "SHOW CREATE TABLE table_x ON table_x", DatabaseType.MySQL, selectStatement, null);
        assertThat(rewriteEngine.rewrite(true).toSQL(tableTokens, shardingRule), is("SHOW CREATE TABLE table_y ON db0"));
    }
    
    @Test
    public void assertIndexTokenForIndexNameTableName() {
        selectStatement.getSqlTokens().add(new IndexToken(13, "index_name", "table_x"));
        selectStatement.getSqlTokens().add(new TableToken(27, "table_x"));
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "CREATE INDEX index_name ON table_x ('column')", DatabaseType.MySQL, selectStatement, null);
        assertThat(rewriteEngine.rewrite(true).toSQL(tableTokens, shardingRule), is("CREATE INDEX index_name_table_1 ON table_1 ('column')"));
    }
    
    @Test
    public void assertIndexTokenForIndexNameTableNameWithoutLogicTableName() {
        selectStatement.getSqlTokens().add(new IndexToken(13, "logic_index", ""));
        selectStatement.getSqlTokens().add(new TableToken(28, "table_x"));
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, "CREATE INDEX index_names ON table_x ('column')", DatabaseType.MySQL, selectStatement, null);
        assertThat(rewriteEngine.rewrite(true).toSQL(tableTokens, shardingRule), is("CREATE INDEX logic_index_table_1 ON table_1 ('column')"));
    }
}
