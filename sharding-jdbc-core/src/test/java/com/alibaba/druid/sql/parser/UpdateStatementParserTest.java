package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.util.JdbcConstants;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class UpdateStatementParserTest extends AbstractStatementParserTest {
    
    @Test
    public void parseWithoutCondition() throws SQLException {
        MySqlStatementParser statementParser = new MySqlStatementParser(createShardingRule(), Collections.emptyList(), "UPDATE TABLE_XXX SET field1=field1+1");
        SQLUpdateStatement updateStatement = (SQLUpdateStatement) statementParser.parseStatement();
        assertThat(updateStatement.getSqlContext().getTable().getName(), is("TABLE_XXX"));
        assertTrue(updateStatement.getSqlContext().getConditionContexts().isEmpty());
        assertThat(updateStatement.getSqlContext().toSqlBuilder().toString(), is("UPDATE [Token(TABLE_XXX)] SET field1=field1+1"));
    }
    
    @Test
    public void parseWithoutParameter()  {
        MySqlStatementParser statementParser = new MySqlStatementParser(createShardingRule(), Collections.emptyList(), "UPDATE TABLE_XXX xxx SET TABLE_XXX.field1=field1+1,xxx.field2=2 "
                + "WHERE TABLE_XXX.field4<10 AND TABLE_XXX.field1=1 AND xxx.field5>10 AND TABLE_XXX.field2 IN (1,3) AND xxx.field6<=10 AND TABLE_XXX.field3 BETWEEN 5 AND 20 AND xxx.field7>=10");
        SQLUpdateStatement updateStatement = (SQLUpdateStatement) statementParser.parseStatement();
        assertUpdateStatement(updateStatement);
        assertThat(updateStatement.getSqlContext().toSqlBuilder().toString(), is("UPDATE [Token(TABLE_XXX)] xxx SET [Token(TABLE_XXX)].field1=field1+1,xxx.field2=2 WHERE [Token(TABLE_XXX)].field4<10 "
                + "AND [Token(TABLE_XXX)].field1=1 AND xxx.field5>10 AND [Token(TABLE_XXX)].field2 IN (1,3) AND xxx.field6<=10 AND [Token(TABLE_XXX)].field3 BETWEEN 5 AND 20 AND xxx.field7>=10"));
    }
    
    @Test
    public void parseWithParameter() {
        MySqlStatementParser statementParser = new MySqlStatementParser(createShardingRule(), Arrays.<Object>asList(2, 10, 1, 10, 1, 3, 10, 5, 20, 10),
                "UPDATE TABLE_XXX AS xxx SET field1=field1+? WHERE field4<? AND xxx.field1=? AND field5>? AND xxx.field2 IN (?, ?) AND field6<=? AND xxx.field3 BETWEEN ? AND ? AND field7>=?");
        SQLUpdateStatement updateStatement = (SQLUpdateStatement) statementParser.parseStatement();
        assertUpdateStatement(updateStatement);
        assertThat(updateStatement.getSqlContext().toSqlBuilder().toString(), is("UPDATE [Token(TABLE_XXX)] AS xxx SET field1=field1+? "
                + "WHERE field4<? AND xxx.field1=? AND field5>? AND xxx.field2 IN (?, ?) AND field6<=? AND xxx.field3 BETWEEN ? AND ? AND field7>=?"));
    }
    
    private void assertUpdateStatement(final SQLUpdateStatement statement) {
        assertThat(statement.getSqlContext().getTable().getName(), is("TABLE_XXX"));
        assertThat(statement.getSqlContext().getTable().getAlias().get(), is("xxx"));
        Iterator<Condition> conditions = statement.getSqlContext().getConditionContexts().iterator().next().getAllConditions().iterator();
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
        new MySqlStatementParser(createShardingRule(), Collections.emptyList(), "UPDATE TABLE_XXX SET field1=1 WHERE field1<1 AND (field1 >2 OR field2 =1)").parseStatement();
    }
    
    @Test
    public void parseWithSpecialSyntax() {
        parseWithSpecialSyntax(JdbcConstants.MYSQL, "UPDATE `TABLE_XXX` SET `field1`=1 WHERE `field1`=1", "UPDATE [Token(TABLE_XXX)] SET `field1`=1 WHERE `field1`=1");
        parseWithSpecialSyntax(JdbcConstants.MYSQL, "UPDATE LOW_PRIORITY IGNORE TABLE_XXX SET field1=1 WHERE field1=1 ORDER BY field1 LIMIT 10",
                "UPDATE LOW_PRIORITY IGNORE [Token(TABLE_XXX)] SET field1=1 WHERE field1=1 ORDER BY field1 LIMIT 10");
        parseWithSpecialSyntax(JdbcConstants.ORACLE, "UPDATE /*+ index(field1) */ ONLY TABLE_XXX SET field1=1 WHERE field1=1 RETURN * LOG ERRORS INTO TABLE_LOG",
                "UPDATE /*+ index(field1) */ ONLY [Token(TABLE_XXX)] SET field1=1 WHERE field1=1 RETURN * LOG ERRORS INTO TABLE_LOG");
        parseWithSpecialSyntax(JdbcConstants.ORACLE, "UPDATE /*+ index(field1) */ ONLY TABLE_XXX SET field1=1 WHERE field1=1 RETURNING *",
                "UPDATE /*+ index(field1) */ ONLY [Token(TABLE_XXX)] SET field1=1 WHERE field1=1 RETURNING *");
        parseWithSpecialSyntax(JdbcConstants.ORACLE, "UPDATE /*+ index(field1) */ ONLY TABLE_XXX SET field1=1 WHERE field1=1 LOG ERRORS INTO TABLE_LOG",
                "UPDATE /*+ index(field1) */ ONLY [Token(TABLE_XXX)] SET field1=1 WHERE field1=1 LOG ERRORS INTO TABLE_LOG");
        parseWithSpecialSyntax(JdbcConstants.SQL_SERVER, 
                "WITH field_query (field1) AS (SELECT field1 FROM TABLE_XXX AS xxx GROUP BY field1) UPDATE TOP(10) TABLE_XXX SET field1=1 OUTPUT (inserted.field1) WHERE field1=1",
                "WITH field_query (field1) AS (SELECT field1 FROM TABLE_XXX AS xxx GROUP BY field1) UPDATE TOP(10) [Token(TABLE_XXX)] SET field1=1 OUTPUT (inserted.field1) WHERE field1=1");
        parseWithSpecialSyntax(JdbcConstants.POSTGRESQL,
                "WITH RECURSIVE field_query (field1) AS (SELECT field1 FROM TABLE_XXX AS xxx ORDER BY field1 DESC) UPDATE ONLY TABLE_XXX SET field1=1 WHERE field1=1 RETURNING *",
                "WITH RECURSIVE field_query (field1) AS (SELECT field1 FROM TABLE_XXX AS xxx ORDER BY field1 DESC) UPDATE ONLY [Token(TABLE_XXX)] SET field1=1 WHERE field1=1 RETURNING *");
        parseWithSpecialSyntax(JdbcConstants.POSTGRESQL,
                "WITH field1_query AS (SELECT field1 FROM TABLE_XXX), field2_query AS (SELECT field2 FROM TABLE_XXX) UPDATE ONLY TABLE_XXX SET (field1,field2)=(1,?) WHERE field1=1",
                "WITH field1_query AS (SELECT field1 FROM TABLE_XXX), field2_query AS (SELECT field2 FROM TABLE_XXX) UPDATE ONLY [Token(TABLE_XXX)] SET (field1,field2)=(1,?) WHERE field1=1");
    }
    
    private void parseWithSpecialSyntax(final String dbType, final String actualSQL, final String expectedSQL) {
        SQLUpdateStatement updateStatement = (SQLUpdateStatement) getSqlStatementParser(dbType, actualSQL).parseStatement();
        assertThat(updateStatement.getSqlContext().getTable().getName(), is("TABLE_XXX"));
        assertFalse(updateStatement.getSqlContext().getTable().getAlias().isPresent());
        Iterator<Condition> conditions = updateStatement.getSqlContext().getConditionContexts().iterator().next().getAllConditions().iterator();
        Condition condition = conditions.next();
        assertThat(condition.getColumn().getTableName(), is("TABLE_XXX"));
        assertThat(condition.getColumn().getColumnName(), is("field1"));
        assertThat(condition.getOperator(), is(Condition.BinaryOperator.EQUAL));
        assertThat(condition.getValues().size(), is(1));
        assertThat(condition.getValues().get(0), is((Comparable) 1));
        assertFalse(conditions.hasNext());
        assertThat(updateStatement.getSqlContext().toSqlBuilder().toString(), is(expectedSQL));
    }
}
