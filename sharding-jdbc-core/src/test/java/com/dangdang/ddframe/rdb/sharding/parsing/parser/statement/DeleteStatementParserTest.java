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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.statement;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.parsing.SQLParsingEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.contstant.ShardingOperator;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.DeleteSQLContext;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public final class DeleteStatementParserTest extends AbstractStatementParserTest {
    
    @Test
    public void parseWithoutCondition() throws SQLException {
        ShardingRule shardingRule = createShardingRule();
        List<Object> parameters = Collections.emptyList();
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL, "DELETE FROM TABLE_XXX", shardingRule, parameters);
        DeleteSQLContext sqlContext = (DeleteSQLContext) statementParser.parseStatement();
        assertThat(sqlContext.getTables().get(0).getName(), is("TABLE_XXX"));
        assertThat(sqlContext.getSqlBuilder().toString(), is("DELETE FROM [Token(TABLE_XXX)]"));
    }
    
    @Test
    public void parseWithoutParameter() throws SQLException {
        ShardingRule shardingRule = createShardingRule();
        List<Object> parameters = Collections.emptyList();
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL,
                "DELETE FROM TABLE_XXX xxx WHERE field4<10 AND TABLE_XXX.field1=1 AND field5>10 AND xxx.field2 IN (1,3) AND field6<=10 AND field3 BETWEEN 5 AND 20 AND field7>=10",
                shardingRule, parameters);
        DeleteSQLContext sqlContext = (DeleteSQLContext) statementParser.parseStatement();
        assertDeleteStatement(sqlContext);
        assertThat(sqlContext.getSqlBuilder().toString(), is(
                "DELETE FROM [Token(TABLE_XXX)] xxx WHERE field4<10 AND [Token(TABLE_XXX)].field1=1 AND field5>10 AND xxx.field2 IN (1,3) AND field6<=10 AND field3 BETWEEN 5 AND 20 AND field7>=10"));
    }
    
    @Test
    public void parseWithParameter() throws SQLException {
        ShardingRule shardingRule = createShardingRule();
        List<Object> parameters = Arrays.<Object>asList(10, 1, 10, 1, 3, 10, 5, 20, 10);
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL, 
                "DELETE FROM TABLE_XXX xxx WHERE field4<? AND field1=? AND field5>? AND field2 IN (?,?) AND field6<=? AND field3 BETWEEN ? AND ? AND field7>=?", shardingRule, parameters);
        DeleteSQLContext sqlContext = (DeleteSQLContext) statementParser.parseStatement();
        assertDeleteStatement(sqlContext);
        assertThat(sqlContext.getSqlBuilder().toString(), is(
                "DELETE FROM [Token(TABLE_XXX)] xxx WHERE field4<? AND field1=? AND field5>? AND field2 IN (?,?) AND field6<=? AND field3 BETWEEN ? AND ? AND field7>=?"));
    }
    
    private void assertDeleteStatement(final DeleteSQLContext sqlContext) {
        assertThat(sqlContext.getTables().get(0).getName(), is("TABLE_XXX"));
        assertThat(sqlContext.getTables().get(0).getAlias().get(), is("xxx"));
        ConditionContext.Condition condition1 = sqlContext.getConditionContext().find("TABLE_XXX", "field1").get();
        assertThat(condition1.getOperator(), is(ShardingOperator.EQUAL));
        assertThat(condition1.getValues().size(), is(1));
        assertThat(condition1.getValues().get(0), is((Comparable) 1));
        ConditionContext.Condition condition2 = sqlContext.getConditionContext().find("TABLE_XXX", "field2").get();
        assertThat(condition2.getOperator(), is(ShardingOperator.IN));
        assertThat(condition2.getValues().size(), is(2));
        assertThat(condition2.getValues().get(0), is((Comparable) 1));
        assertThat(condition2.getValues().get(1), is((Comparable) 3));
        ConditionContext.Condition condition3 = sqlContext.getConditionContext().find("TABLE_XXX", "field3").get();
        assertThat(condition3.getOperator(), is(ShardingOperator.BETWEEN));
        assertThat(condition3.getValues().size(), is(2));
        assertThat(condition3.getValues().get(0), is((Comparable) 5));
        assertThat(condition3.getValues().get(1), is((Comparable) 20));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void parseStatementWithDeleteMultipleTable() {
        ShardingRule shardingRule = createShardingRule();
        List<Object> parameters = Collections.emptyList();
        new SQLParsingEngine(DatabaseType.MySQL, "DELETE TABLE_XXX1, TABLE_xxx2 FROM TABLE_XXX1 JOIN TABLE_XXX2", shardingRule, parameters)
                .parseStatement();
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void parseStatementWithDeleteMultipleTableWithUsing() {
        ShardingRule shardingRule = createShardingRule();
        List<Object> parameters = Collections.emptyList();
        new SQLParsingEngine(DatabaseType.MySQL, "DELETE FROM TABLE_XXX1, TABLE_xxx2 USING TABLE_XXX1 JOIN TABLE_XXX2", shardingRule, parameters).parseStatement();
    }
    
    @Test
    public void parseWithSpecialSyntax() throws SQLException {
        parseWithSpecialSyntax(DatabaseType.MySQL, "DELETE `TABLE_XXX` WHERE `field1`=1", "DELETE [Token(TABLE_XXX)] WHERE `field1`=1");
        parseWithSpecialSyntax(DatabaseType.MySQL, "DELETE LOW_PRIORITY QUICK IGNORE TABLE_XXX PARTITION (partition_1) WHERE field1=1 ORDER BY field1 LIMIT 10",
                "DELETE LOW_PRIORITY QUICK IGNORE [Token(TABLE_XXX)] PARTITION (partition_1) WHERE field1=1 ORDER BY field1 LIMIT 10");
        parseWithSpecialSyntax(DatabaseType.MySQL, "DELETE FROM TABLE_XXX PARTITION (partition_1, partition_2,partition_3) WHERE field1=1",
                "DELETE FROM [Token(TABLE_XXX)] PARTITION (partition_1, partition_2,partition_3) WHERE field1=1");
        parseWithSpecialSyntax(DatabaseType.Oracle, "DELETE /*+ index(field1) */ ONLY (TABLE_XXX) WHERE field1=1 RETURN * LOG ERRORS INTO TABLE_LOG",
                "DELETE /*+ index(field1) */ ONLY ([Token(TABLE_XXX)]) WHERE field1=1 RETURN * LOG ERRORS INTO TABLE_LOG");
        parseWithSpecialSyntax(DatabaseType.Oracle, "DELETE /*+ index(field1) */ ONLY (TABLE_XXX) WHERE field1=1 RETURNING *",
                "DELETE /*+ index(field1) */ ONLY ([Token(TABLE_XXX)]) WHERE field1=1 RETURNING *");
        /* // TODO 不支持
        parseWithSpecialSyntax(DatabaseType.SQLServer,
                "WITH field_query (field1, field2) AS (SELECT field1, field2 FROM TABLE_XXX AS xxx GROUP BY field1) DELETE TOP(10) OUTPUT (inserted.field1) FROM TABLE_XXX WHERE field1=1",
                "WITH field_query (field1, field2) AS (SELECT field1, field2 FROM TABLE_XXX AS xxx GROUP BY field1) DELETE TOP(10) OUTPUT (inserted.field1) FROM [Token(TABLE_XXX)] WHERE field1=1");
                */
        parseWithSpecialSyntax(DatabaseType.PostgreSQL,
                "WITH RECURSIVE field_query (field1) AS (SELECT field1 FROM TABLE_XXX AS xxx ORDER BY field1 DESC) DELETE FROM ONLY TABLE_XXX USING producers WHERE field1=1 RETURNING *",
                "WITH RECURSIVE field_query (field1) AS (SELECT field1 FROM TABLE_XXX AS xxx ORDER BY field1 DESC) DELETE FROM ONLY [Token(TABLE_XXX)] USING producers WHERE field1=1 RETURNING *");
        parseWithSpecialSyntax(DatabaseType.PostgreSQL,
                "WITH field1_query AS (SELECT field1 FROM TABLE_XXX), field2_query AS (SELECT field2 FROM TABLE_XXX) DELETE FROM ONLY TABLE_XXX USING producers WHERE field1=1 OUTPUT *",
                "WITH field1_query AS (SELECT field1 FROM TABLE_XXX), field2_query AS (SELECT field2 FROM TABLE_XXX) DELETE FROM ONLY [Token(TABLE_XXX)] USING producers WHERE field1=1 OUTPUT *");
    }
    
    private void parseWithSpecialSyntax(final DatabaseType dbType, final String actualSQL, final String expectedSQL) {
        DeleteSQLContext sqlContext = (DeleteSQLContext) new SQLParsingEngine(dbType, actualSQL, createShardingRule(), Collections.emptyList()).parseStatement();
        assertThat(sqlContext.getTables().get(0).getName(), is("TABLE_XXX"));
        assertFalse(sqlContext.getTables().get(0).getAlias().isPresent());
        ConditionContext.Condition condition = sqlContext.getConditionContext().find("TABLE_XXX", "field1").get();
        assertThat(condition.getOperator(), is(ShardingOperator.EQUAL));
        assertThat(condition.getValues().size(), is(1));
        assertThat(condition.getValues().get(0), is((Comparable) 1));
        assertThat(sqlContext.getSqlBuilder().toString().replace("([Token(TABLE_XXX)] )", "([Token(TABLE_XXX)])"), is(expectedSQL));
    }
}
