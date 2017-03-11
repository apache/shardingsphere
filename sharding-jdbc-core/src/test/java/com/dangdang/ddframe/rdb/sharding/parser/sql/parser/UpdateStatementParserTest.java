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

package com.dangdang.ddframe.rdb.sharding.parser.sql.parser;

import com.dangdang.ddframe.rdb.sharding.parser.sql.context.UpdateSQLContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.mysql.parser.MySQLExprParser;
import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.oracle.parser.OracleExprParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class UpdateStatementParserTest extends AbstractStatementParserTest {
    
    @Test
    public void parseWithoutCondition() throws SQLException {
        ShardingRule shardingRule = createShardingRule();
        List<Object> parameters = Collections.emptyList();
        SQLStatementParser statementParser = new SQLStatementParser(DatabaseType.MySQL, shardingRule, parameters, new MySQLExprParser(shardingRule, parameters,
                "UPDATE TABLE_XXX SET field1=field1+1"));
        UpdateSQLContext sqlContext = (UpdateSQLContext) statementParser.parseStatement();
        assertThat(sqlContext.getTables().get(0).getName(), is("TABLE_XXX"));
        assertTrue(sqlContext.getConditionContexts().isEmpty());
        assertThat(sqlContext.toSqlBuilder().toString(), is("UPDATE [Token(TABLE_XXX)] SET field1=field1+1"));
    }
    
    @Test
    public void parseWithoutParameter()  {
        ShardingRule shardingRule = createShardingRule();
        List<Object> parameters = Collections.emptyList();
        SQLStatementParser statementParser = new SQLStatementParser(DatabaseType.MySQL, shardingRule, parameters, new MySQLExprParser(shardingRule, parameters,
                "UPDATE TABLE_XXX xxx SET TABLE_XXX.field1=field1+1,xxx.field2=2 WHERE "
                        + "TABLE_XXX.field4<10 AND TABLE_XXX.field1=1 AND xxx.field5>10 AND TABLE_XXX.field2 IN (1,3) AND xxx.field6<=10 AND TABLE_XXX.field3 BETWEEN 5 AND 20 AND xxx.field7>=10"));
        UpdateSQLContext sqlContext = (UpdateSQLContext) statementParser.parseStatement();
        assertUpdateStatement(sqlContext);
        assertThat(sqlContext.toSqlBuilder().toString(), is("UPDATE [Token(TABLE_XXX)] xxx SET [Token(TABLE_XXX)].field1=field1+1,xxx.field2=2 WHERE [Token(TABLE_XXX)].field4<10 "
                + "AND [Token(TABLE_XXX)].field1=1 AND xxx.field5>10 AND [Token(TABLE_XXX)].field2 IN (1,3) AND xxx.field6<=10 AND [Token(TABLE_XXX)].field3 BETWEEN 5 AND 20 AND xxx.field7>=10"));
    }
    
    @Test
    public void parseWithParameter() {
        ShardingRule shardingRule = createShardingRule();
        List<Object> parameters = Arrays.<Object>asList(2, 10, 1, 10, 1, 3, 10, 5, 20, 10);
        SQLStatementParser statementParser = new SQLStatementParser(DatabaseType.MySQL, shardingRule, parameters, new MySQLExprParser(shardingRule, parameters,
                "UPDATE TABLE_XXX AS xxx SET field1=field1+? WHERE field4<? AND xxx.field1=? AND field5>? AND xxx.field2 IN (?, ?) AND field6<=? AND xxx.field3 BETWEEN ? AND ? AND field7>=?"));
        UpdateSQLContext sqlContext = (UpdateSQLContext) statementParser.parseStatement();
        assertUpdateStatement(sqlContext);
        assertThat(sqlContext.toSqlBuilder().toString(), is("UPDATE [Token(TABLE_XXX)] AS xxx SET field1=field1+? "
                + "WHERE field4<? AND xxx.field1=? AND field5>? AND xxx.field2 IN (?, ?) AND field6<=? AND xxx.field3 BETWEEN ? AND ? AND field7>=?"));
    }
    
    private void assertUpdateStatement(final UpdateSQLContext sqlContext) {
        assertThat(sqlContext.getTables().get(0).getName(), is("TABLE_XXX"));
        assertThat(sqlContext.getTables().get(0).getAlias().get(), is("xxx"));
        Iterator<Condition> conditions = sqlContext.getConditionContexts().iterator().next().getAllConditions().iterator();
        Condition condition = conditions.next();
        assertThat(condition.getColumn().getTableName(), is("TABLE_XXX"));
        assertThat(condition.getColumn().getColumnName(), is("field1"));
        assertThat(condition.getOperator(), is(Condition.BinaryOperator.EQUAL));
        assertThat(condition.getValues().size(), is(1));
        assertThat(condition.getValues().get(0), is((Comparable) 1));
        condition = conditions.next();
        assertThat(condition.getColumn().getTableName(), is("TABLE_XXX"));
        assertThat(condition.getColumn().getColumnName(), is("field2"));
        assertThat(condition.getOperator(), is(Condition.BinaryOperator.IN));
        assertThat(condition.getValues().size(), is(2));
        assertThat(condition.getValues().get(0), is((Comparable) 1));
        assertThat(condition.getValues().get(1), is((Comparable) 3));
        condition = conditions.next();
        assertThat(condition.getColumn().getTableName(), is("TABLE_XXX"));
        assertThat(condition.getColumn().getColumnName(), is("field3"));
        assertThat(condition.getOperator(), is(Condition.BinaryOperator.BETWEEN));
        assertThat(condition.getValues().size(), is(2));
        assertThat(condition.getValues().get(0), is((Comparable) 5));
        assertThat(condition.getValues().get(1), is((Comparable) 20));
        assertFalse(conditions.hasNext());
    }
    
    @Test(expected = ParserUnsupportedException.class)
    public void parseWithOr() {
        ShardingRule shardingRule = createShardingRule();
        List<Object> parameters = Collections.emptyList();
        new SQLStatementParser(DatabaseType.Oracle, shardingRule, parameters, new OracleExprParser(shardingRule, parameters,
                "UPDATE TABLE_XXX SET field1=1 WHERE field1<1 AND (field1 >2 OR field2 =1)")).parseStatement();
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
        UpdateSQLContext sqlContext = (UpdateSQLContext) getSqlStatementParser(dbType, actualSQL).parseStatement();
        assertThat(sqlContext.getTables().get(0).getName(), is("TABLE_XXX"));
        assertFalse(sqlContext.getTables().get(0).getAlias().isPresent());
        Iterator<Condition> conditions = sqlContext.getConditionContexts().iterator().next().getAllConditions().iterator();
        Condition condition = conditions.next();
        assertThat(condition.getColumn().getTableName(), is("TABLE_XXX"));
        assertThat(condition.getColumn().getColumnName(), is("field1"));
        assertThat(condition.getOperator(), is(Condition.BinaryOperator.EQUAL));
        assertThat(condition.getValues().size(), is(1));
        assertThat(condition.getValues().get(0), is((Comparable) 1));
        assertFalse(conditions.hasNext());
        assertThat(sqlContext.toSqlBuilder().toString(), is(expectedSQL));
    }
}
