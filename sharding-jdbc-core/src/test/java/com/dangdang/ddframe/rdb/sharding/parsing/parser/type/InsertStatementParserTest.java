/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parsing.parser.type;

import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.NoneTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.constant.ShardingOperator;
import com.dangdang.ddframe.rdb.sharding.keygen.fixture.IncrementKeyGenerator;
import com.dangdang.ddframe.rdb.sharding.parsing.SQLParsingEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.InsertSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.rewrite.SQLRewriteEngine;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class InsertStatementParserTest extends AbstractStatementParserTest {
    
    @Test
    public void parseWithoutParameter() throws SQLException {
        String sql = "INSERT INTO `TABLE_XXX` (`field1`, `field2`) VALUES (10, 1)";
        ShardingRule shardingRule = createShardingRule();
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL, sql, shardingRule);
        InsertSQLContext sqlContext = (InsertSQLContext) statementParser.parse();
        assertInsertStatementWithoutParameter(sqlContext);
        // TODO 放入rewrite模块断言
        assertThat(new SQLRewriteEngine(sql, sqlContext).rewrite().toString(), is("INSERT INTO [Token(TABLE_XXX)] (`field1`, `field2`) VALUES (10, 1)"));
    }
    
    @Test
    public void parseWithParameter() {
        String sql = "INSERT INTO TABLE_XXX (field1, field2) VALUES (?, ?)";
        ShardingRule shardingRule = createShardingRule();
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL, "INSERT INTO TABLE_XXX (field1, field2) VALUES (?, ?)", shardingRule);
        InsertSQLContext sqlContext = (InsertSQLContext) statementParser.parse();
        assertInsertStatementWithParameter(sqlContext);
        // TODO 放入rewrite模块断言
        assertThat(new SQLRewriteEngine(sql, sqlContext).rewrite().toString(), is("INSERT INTO [Token(TABLE_XXX)] (field1, field2) VALUES (?, ?)"));
    }
    
    @Test
    public void parseWithGenerateKeyColumnsWithoutParameter() throws SQLException {
        String sql = "INSERT INTO `TABLE_XXX` (`field1`) VALUES (10)";
        ShardingRule shardingRule = createShardingRuleWithGenerateKeyColumns();
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL, sql, shardingRule);
        InsertSQLContext sqlContext = (InsertSQLContext) statementParser.parse();
        assertInsertStatementWithoutParameter(sqlContext);
        // TODO 放入rewrite模块断言
        sqlContext.appendGenerateKeysToken(shardingRule);
        assertThat(new SQLRewriteEngine(sql, sqlContext).rewrite().toString(), is("INSERT INTO [Token(TABLE_XXX)] (`field1`, field2) VALUES (10, 1)"));
    }
    
    @Test
    public void parseWithGenerateKeyColumnsWithParameter() throws SQLException {
        String sql = "INSERT INTO `TABLE_XXX` (`field1`) VALUES (?)";
        ShardingRule shardingRule = createShardingRuleWithGenerateKeyColumns();
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL, "INSERT INTO `TABLE_XXX` (`field1`) VALUES (?)", shardingRule);
        InsertSQLContext sqlContext = (InsertSQLContext) statementParser.parse();
        assertInsertStatementWithParameter(sqlContext);
        // TODO 放入rewrite模块断言
        sqlContext.appendGenerateKeysToken(shardingRule);
        assertThat(new SQLRewriteEngine(sql, sqlContext).rewrite().toString(), is("INSERT INTO [Token(TABLE_XXX)] (`field1`, field2) VALUES (?, 1)"));
    }
    
    private void assertInsertStatementWithoutParameter(final InsertSQLContext sqlContext) {
        assertThat(sqlContext.getTables().get(0).getName(), is("TABLE_XXX"));
        ConditionContext.Condition condition = sqlContext.getConditionContext().find("TABLE_XXX", "field1").get();
        assertThat(condition.getOperator(), is(ShardingOperator.EQUAL));
        assertThat(condition.getValues().size(), is(1));
        assertThat(condition.getValues().get(0), is((Comparable) 10));
    }
    
    private void assertInsertStatementWithParameter(final InsertSQLContext sqlContext) {
        assertThat(sqlContext.getTables().get(0).getName(), is("TABLE_XXX"));
        ConditionContext.Condition condition = sqlContext.getConditionContext().find("TABLE_XXX", "field1").get();
        assertThat(condition.getOperator(), is(ShardingOperator.EQUAL));
        assertTrue(condition.getValues().isEmpty());
        assertThat(condition.getValueIndices().size(), is(1));
        assertThat(condition.getValueIndices().get(0), is(0));
    }
    
    private ShardingRule createShardingRuleWithGenerateKeyColumns() {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        try {
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.getMetaData()).thenReturn(databaseMetaData);
            when(databaseMetaData.getDatabaseProductName()).thenReturn("H2");
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
        Map<String, DataSource> dataSourceMap = new HashMap<>(1);
        dataSourceMap.put("ds", dataSource);
        DataSourceRule dataSourceRule = new DataSourceRule(dataSourceMap);
        TableRule tableRule = TableRule.builder("TABLE_XXX").actualTables(Arrays.asList("table_0", "table_1", "table_2")).dataSourceRule(dataSourceRule)
                .tableShardingStrategy(new TableShardingStrategy(Arrays.asList("field1", "field2", "field3", "field4", "field5", "field6", "field7"), new NoneTableShardingAlgorithm()))
                .generateKeyColumn("field1").generateKeyColumn("field2").build();
        return ShardingRule.builder().dataSourceRule(dataSourceRule).tableRules(Collections.singletonList(tableRule)).keyGenerator(IncrementKeyGenerator.class).build();
    }
    
    @Test
    public void parseWithSpecialSyntax() {
//        parseWithSpecialSyntax(DatabaseType.MySQL, "INSERT LOW_PRIORITY IGNORE INTO `TABLE_XXX` PARTITION (partition1,partition2) (`field1`) VALUE (1)", 
//                "INSERT LOW_PRIORITY IGNORE INTO [Token(TABLE_XXX)] PARTITION (partition1,partition2) (`field1`) VALUE (1)");
        parseWithSpecialSyntax(DatabaseType.MySQL, "INSERT INTO TABLE_XXX SET field1=1", "INSERT INTO [Token(TABLE_XXX)] SET field1=1");
        // TODO
//         parseWithSpecialSyntax(DatabaseType.MySQL, "INSERT INTO TABLE_XXX (field1) SELECT field1 FROM TABLE_XXX2 ON DUPLICATE KEY UPDATE field1=field1+1", 
//                 "INSERT INTO [Token(TABLE_XXX)] (field1) SELECT field1 FROM TABLE_XXX2 ON DUPLICATE KEY UPDATE field1=field1+1");
        parseWithSpecialSyntax(DatabaseType.MySQL, "INSERT /*+ index(field1) */ INTO TABLE_XXX (`field1`) VALUES (1) RETURNING field1*2 LOG ERRORS INTO TABLE_LOG",
                "INSERT /*+ index(field1) */ INTO [Token(TABLE_XXX)] (`field1`) VALUES (1) RETURNING field1*2 LOG ERRORS INTO TABLE_LOG");
        /* // TODO 不支持
        parseWithSpecialSyntax(DatabaseType.SQLServer, 
                "WITH field_query (field1, field2) AS (SELECT field1, field2 FROM TABLE_XXX AS xxx GROUP BY field1) INSERT TOP(10) INTO OUTPUT TABLE_XXX (`field1`) VALUES (1)", 
                "WITH field_query (field1, field2) AS (SELECT field1, field2 FROM TABLE_XXX AS xxx GROUP BY field1) INSERT TOP(10) INTO OUTPUT [Token(TABLE_XXX)] (`field1`) VALUES (1)");
        */
        parseWithSpecialSyntax(DatabaseType.PostgreSQL, 
                "WITH RECURSIVE field_query (field1, field2) AS (SELECT field1, field2 FROM TABLE_XXX AS xxx ORDER BY field1 DESC) INSERT INTO TABLE_XXX (field1) VALUES (1) RETURNING id",
                "WITH RECURSIVE field_query (field1, field2) AS (SELECT field1, field2 FROM TABLE_XXX AS xxx ORDER BY field1 DESC) INSERT INTO [Token(TABLE_XXX)] (field1) VALUES (1) RETURNING id");
        parseWithSpecialSyntax(DatabaseType.PostgreSQL,
                "WITH field1_query AS (SELECT field1 FROM TABLE_XXX), field2_query AS (SELECT field2 FROM TABLE_XXX) INSERT INTO TABLE_XXX (field1) VALUES (1) RETURNING *",
                "WITH field1_query AS (SELECT field1 FROM TABLE_XXX), field2_query AS (SELECT field2 FROM TABLE_XXX) INSERT INTO [Token(TABLE_XXX)] (field1) VALUES (1) RETURNING *");
    }
    
    private void parseWithSpecialSyntax(final DatabaseType dbType, final String actualSQL, final String expectedSQL) {
        InsertSQLContext sqlContext = (InsertSQLContext) new SQLParsingEngine(dbType, actualSQL, createShardingRule()).parse();
        assertThat(sqlContext.getTables().get(0).getName(), is("TABLE_XXX"));
        assertFalse(sqlContext.getTables().get(0).getAlias().isPresent());
        ConditionContext.Condition condition = sqlContext.getConditionContext().find("TABLE_XXX", "field1").get();
        assertThat(condition.getOperator(), is(ShardingOperator.EQUAL));
        assertThat(condition.getValues().size(), is(1));
        assertThat(condition.getValues().get(0), is((Comparable) 1));
        // TODO 放入rewrite模块断言
        assertThat(new SQLRewriteEngine(actualSQL, sqlContext).rewrite().toString(), is(expectedSQL));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void parseMultipleInsertForMySQL() {
        ShardingRule shardingRule = createShardingRule();
        new SQLParsingEngine(DatabaseType.Oracle, "INSERT INTO TABLE_XXX (`field1`, `field2`) VALUES (1, 'value_char'), (2, 'value_char')", shardingRule).parse();
    }
    
    @Test(expected = SQLParsingUnsupportedException.class)
    public void parseInsertAllForOracle() {
        ShardingRule shardingRule = createShardingRule();
        new SQLParsingEngine(DatabaseType.Oracle, "INSERT ALL INTO TABLE_XXX (field1) VALUES (field1) SELECT field1 FROM TABLE_XXX2", shardingRule).parse();
    }
    
    @Test(expected = SQLParsingUnsupportedException.class)
    public void parseInsertFirstForOracle() {
        ShardingRule shardingRule = createShardingRule();
        new SQLParsingEngine(DatabaseType.Oracle, "INSERT FIRST INTO TABLE_XXX (field1) VALUES (field1) SELECT field1 FROM TABLE_XXX2", shardingRule).parse();
    }
}
