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
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.constant.ShardingOperator;
import com.dangdang.ddframe.rdb.sharding.parsing.SQLParsingEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.Column;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Condition;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.update.UpdateStatement;
import com.dangdang.ddframe.rdb.sharding.rewrite.SQLRewriteEngine;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public final class UpdateStatementParserTest extends AbstractStatementParserTest {
    
    @Test
    public void parseWithoutCondition() {
        String sql = "UPDATE TABLE_XXX SET field1=field1+1";
        ShardingRule shardingRule = createShardingRule();
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL, sql, shardingRule);
        UpdateStatement updateStatement = (UpdateStatement) statementParser.parse();
        assertThat(updateStatement.getTables().find("TABLE_XXX").get().getName(), is("TABLE_XXX"));
        // TODO 放入rewrite模块断言
        assertThat(new SQLRewriteEngine(sql, updateStatement).rewrite().toString(), is("UPDATE [Token(TABLE_XXX)] SET field1=field1+1"));
    }
    
    @Test
    public void parseWithoutParameter()  {
        String sql = "UPDATE TABLE_XXX xxx SET TABLE_XXX.field1=field1+1,xxx.field2=2 WHERE TABLE_XXX.field4<10 AND"
                + " TABLE_XXX.field1=1 AND xxx.field5>10 AND TABLE_XXX.field2 IN (1,3) AND xxx.field6<=10 AND TABLE_XXX.field3 BETWEEN 5 AND 20 AND xxx.field7>=10";
        ShardingRule shardingRule = createShardingRule();
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL, sql, shardingRule);
        UpdateStatement updateStatement = (UpdateStatement) statementParser.parse();
        assertUpdateStatementWithoutParameter(updateStatement);
        // TODO 放入rewrite模块断言
        assertThat(new SQLRewriteEngine(sql, updateStatement).rewrite().toString(),
                is("UPDATE [Token(TABLE_XXX)] xxx SET [Token(TABLE_XXX)].field1=field1+1,xxx.field2=2 WHERE [Token(TABLE_XXX)].field4<10 "
                + "AND [Token(TABLE_XXX)].field1=1 AND xxx.field5>10 AND [Token(TABLE_XXX)].field2 IN (1,3) AND xxx.field6<=10 AND [Token(TABLE_XXX)].field3 BETWEEN 5 AND 20 AND xxx.field7>=10"));
    }
    
    private void assertUpdateStatementWithoutParameter(final UpdateStatement updateStatement) {
        assertThat(updateStatement.getTables().find("TABLE_XXX").get().getName(), is("TABLE_XXX"));
        assertThat(updateStatement.getTables().find("TABLE_XXX").get().getAlias().get(), is("xxx"));
        Condition condition1 = updateStatement.getConditions().find(new Column("field1", "TABLE_XXX")).get();
        assertThat(condition1.getOperator(), is(ShardingOperator.EQUAL));
        assertThat(condition1.getValues(Collections.emptyList()).size(), is(1));
        assertThat(condition1.getValues(Collections.emptyList()).get(0), is((Comparable) 1));
        Condition condition2 = updateStatement.getConditions().find(new Column("field2", "TABLE_XXX")).get();
        assertThat(condition2.getOperator(), is(ShardingOperator.IN));
        assertThat(condition2.getValues(Collections.emptyList()).size(), is(2));
        assertThat(condition2.getValues(Collections.emptyList()).get(0), is((Comparable) 1));
        assertThat(condition2.getValues(Collections.emptyList()).get(1), is((Comparable) 3));
        Condition condition3 = updateStatement.getConditions().find(new Column("field3", "TABLE_XXX")).get();
        assertThat(condition3.getOperator(), is(ShardingOperator.BETWEEN));
        assertThat(condition3.getValues(Collections.emptyList()).size(), is(2));
        assertThat(condition3.getValues(Collections.emptyList()).get(0), is((Comparable) 5));
        assertThat(condition3.getValues(Collections.emptyList()).get(1), is((Comparable) 20));
    }
    
    @Test
    public void parseWithParameter() {
        String sql = "UPDATE TABLE_XXX AS xxx SET field1=field1+? WHERE field4<? AND xxx.field1=? AND field5>? AND xxx.field2 IN (?, ?) AND field6<=? AND xxx.field3 BETWEEN ? AND ? AND field7>=?";
        ShardingRule shardingRule = createShardingRule();
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL, sql, shardingRule);
        UpdateStatement updateStatement = (UpdateStatement) statementParser.parse();
        assertUpdateStatementWitParameter(updateStatement);
        // TODO 放入rewrite模块断言
        assertThat(new SQLRewriteEngine(sql, updateStatement).rewrite().toString(), is("UPDATE [Token(TABLE_XXX)] AS xxx SET field1=field1+? "
                + "WHERE field4<? AND xxx.field1=? AND field5>? AND xxx.field2 IN (?, ?) AND field6<=? AND xxx.field3 BETWEEN ? AND ? AND field7>=?"));
    }
    
    private void assertUpdateStatementWitParameter(final UpdateStatement updateStatement) {
        assertThat(updateStatement.getTables().find("TABLE_XXX").get().getName(), is("TABLE_XXX"));
        assertThat(updateStatement.getTables().find("TABLE_XXX").get().getAlias().get(), is("xxx"));
        List<Object> actualParameters = Arrays.<Object>asList(0, 10, 20, 30, 40, 50, 60, 70, 80);
        Condition condition1 = updateStatement.getConditions().find(new Column("field1", "TABLE_XXX")).get();
        assertThat(condition1.getOperator(), is(ShardingOperator.EQUAL));
        assertThat(condition1.getValues(actualParameters).size(), is(1));
        assertThat(condition1.getValues(actualParameters).get(0), is((Comparable) 20));
        Condition condition2 = updateStatement.getConditions().find(new Column("field2", "TABLE_XXX")).get();
        assertThat(condition2.getOperator(), is(ShardingOperator.IN));
        assertThat(condition2.getValues(actualParameters).size(), is(2));
        assertThat(condition2.getValues(actualParameters).get(0), is((Comparable) 40));
        assertThat(condition2.getValues(actualParameters).get(1), is((Comparable) 50));
        Condition condition3 = updateStatement.getConditions().find(new Column("field3", "TABLE_XXX")).get();
        assertThat(condition3.getOperator(), is(ShardingOperator.BETWEEN));
        assertThat(condition3.getValues(actualParameters).size(), is(2));
        assertThat(condition3.getValues(actualParameters).get(0), is((Comparable) 70));
        assertThat(condition3.getValues(actualParameters).get(1), is((Comparable) 80));
    }
    
    @Test(expected = SQLParsingUnsupportedException.class)
    public void parseWithOr() {
        ShardingRule shardingRule = createShardingRule();
        new SQLParsingEngine(DatabaseType.Oracle, "UPDATE TABLE_XXX SET field1=1 WHERE field1<1 AND (field1 >2 OR field2 =1)", shardingRule).parse();
    }
    
    @Test
    public void parseWithSpecialSyntax() {
        parseWithSpecialSyntax(DatabaseType.MySQL, "UPDATE `TABLE_XXX` SET `field1`=1 WHERE `field1`=1", "UPDATE [Token(TABLE_XXX)] SET `field1`=1 WHERE `field1`=1");
        parseWithSpecialSyntax(DatabaseType.MySQL, "UPDATE LOW_PRIORITY IGNORE TABLE_XXX SET field1=1 WHERE field1=1 ORDER BY field1 LIMIT 10",
                "UPDATE LOW_PRIORITY IGNORE [Token(TABLE_XXX)] SET field1=1 WHERE field1=1 ORDER BY field1 LIMIT 10");
        parseWithSpecialSyntax(DatabaseType.Oracle, "UPDATE /*+ index(field1) */ ONLY TABLE_XXX SET field1=1 WHERE field1=1 RETURN * LOG ERRORS INTO TABLE_LOG",
                "UPDATE /*+ index(field1) */ ONLY [Token(TABLE_XXX)] SET field1=1 WHERE field1=1 RETURN * LOG ERRORS INTO TABLE_LOG");
        parseWithSpecialSyntax(DatabaseType.Oracle, "UPDATE /*+ index(field1) */ ONLY TABLE_XXX SET field1=1 WHERE field1=1 RETURNING *",
                "UPDATE /*+ index(field1) */ ONLY [Token(TABLE_XXX)] SET field1=1 WHERE field1=1 RETURNING *");
        parseWithSpecialSyntax(DatabaseType.Oracle, "UPDATE /*+ index(field1) */ ONLY TABLE_XXX SET field1=1 WHERE field1=1 LOG ERRORS INTO TABLE_LOG",
                "UPDATE /*+ index(field1) */ ONLY [Token(TABLE_XXX)] SET field1=1 WHERE field1=1 LOG ERRORS INTO TABLE_LOG");
        /* // TODO 不支持
        parseWithSpecialSyntax(DatabaseType.SQLServer, 
                "WITH field_query (field1) AS (SELECT field1 FROM TABLE_XXX AS xxx GROUP BY field1) UPDATE TOP(10) TABLE_XXX SET field1=1 OUTPUT (inserted.field1) WHERE field1=1",
                "WITH field_query (field1) AS (SELECT field1 FROM TABLE_XXX AS xxx GROUP BY field1) UPDATE TOP(10) [Token(TABLE_XXX)] SET field1=1 OUTPUT (inserted.field1) WHERE field1=1");
        */
        parseWithSpecialSyntax(DatabaseType.PostgreSQL,
                "WITH RECURSIVE field_query (field1) AS (SELECT field1 FROM TABLE_XXX AS xxx ORDER BY field1 DESC) UPDATE ONLY TABLE_XXX SET field1=1 WHERE field1=1 RETURNING *",
                "WITH RECURSIVE field_query (field1) AS (SELECT field1 FROM TABLE_XXX AS xxx ORDER BY field1 DESC) UPDATE ONLY [Token(TABLE_XXX)] SET field1=1 WHERE field1=1 RETURNING *");
        parseWithSpecialSyntax(DatabaseType.PostgreSQL,
                "WITH field1_query AS (SELECT field1 FROM TABLE_XXX), field2_query AS (SELECT field2 FROM TABLE_XXX) UPDATE ONLY TABLE_XXX SET (field1,field2)=(1,?) WHERE field1=1",
                "WITH field1_query AS (SELECT field1 FROM TABLE_XXX), field2_query AS (SELECT field2 FROM TABLE_XXX) UPDATE ONLY [Token(TABLE_XXX)] SET (field1,field2)=(1,?) WHERE field1=1");
    }
    
    private void parseWithSpecialSyntax(final DatabaseType dbType, final String actualSQL, final String expectedSQL) {
        UpdateStatement updateStatement = (UpdateStatement) new SQLParsingEngine(dbType, actualSQL, createShardingRule()).parse();
        assertThat(updateStatement.getTables().find("TABLE_XXX").get().getName(), is("TABLE_XXX"));
        assertFalse(updateStatement.getTables().find("TABLE_XXX").get().getAlias().isPresent());
        Condition condition = updateStatement.getConditions().find(new Column("field1", "TABLE_XXX")).get();
        assertThat(condition.getOperator(), is(ShardingOperator.EQUAL));
        assertThat(condition.getValues(Collections.emptyList()).size(), is(1));
        assertThat(condition.getValues(Collections.emptyList()).get(0), is((Comparable) 1));
        // TODO 放入rewrite模块断言
        assertThat(new SQLRewriteEngine(actualSQL, updateStatement).rewrite().toString(), is(expectedSQL));
    }
}
