package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
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

public final class DeleteStatementParserTest extends AbstractStatementParserTest {
    
    @Test
    public void parseWithoutCondition() throws SQLException {
        MySqlStatementParser statementParser = new MySqlStatementParser(createShardingRule(), Collections.emptyList(), "DELETE FROM TABLE_XXX");
        SQLDeleteStatement deleteStatement = (SQLDeleteStatement) statementParser.parseStatement();
        assertThat(deleteStatement.getSqlContext().getTable().getName(), is("TABLE_XXX"));
        assertTrue(deleteStatement.getSqlContext().getConditionContexts().isEmpty());
        assertThat(deleteStatement.getSqlContext().toSqlBuilder().toString(), is("DELETE FROM [Token(TABLE_XXX)]"));
    }
    
    @Test
    public void parseWithoutParameter() throws SQLException {
        MySqlStatementParser statementParser = new MySqlStatementParser(createShardingRule(), Collections.emptyList(), 
                "DELETE FROM TABLE_XXX xxx WHERE field4<10 AND TABLE_XXX.field1=1 AND field5>10 AND xxx.field2 IN (1,3) AND field6<=10 AND field3 BETWEEN 5 AND 20 AND field7>=10");
        SQLDeleteStatement deleteStatement = (SQLDeleteStatement) statementParser.parseStatement();
        assertDeleteStatement(deleteStatement);
        assertThat(deleteStatement.getSqlContext().toSqlBuilder().toString(), is(
                "DELETE FROM [Token(TABLE_XXX)] xxx WHERE field4<10 AND [Token(TABLE_XXX)].field1=1 AND field5>10 AND xxx.field2 IN (1,3) AND field6<=10 AND field3 BETWEEN 5 AND 20 AND field7>=10"));
    }
    
    @Test
    public void parseWithParameter() throws SQLException {
        MySqlStatementParser statementParser = new MySqlStatementParser(createShardingRule(), Arrays.<Object>asList(10, 1, 10, 1, 3, 10, 5, 20, 10),
                "DELETE FROM TABLE_XXX xxx WHERE field4<? AND field1=? AND field5>? AND field2 IN (?,?) AND field6<=? AND field3 BETWEEN ? AND ? AND field7>=?");
        SQLDeleteStatement deleteStatement = (SQLDeleteStatement) statementParser.parseStatement();
        assertDeleteStatement(deleteStatement);
        assertThat(deleteStatement.getSqlContext().toSqlBuilder().toString(), is(
                "DELETE FROM [Token(TABLE_XXX)] xxx WHERE field4<? AND field1=? AND field5>? AND field2 IN (?,?) AND field6<=? AND field3 BETWEEN ? AND ? AND field7>=?"));
    }
    
    private void assertDeleteStatement(final SQLDeleteStatement statement) {
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
    
    @Test(expected = UnsupportedOperationException.class)
    public void parseStatementWithDeleteMultipleTable() {
        new MySqlStatementParser(null, Collections.emptyList(), "DELETE TABLE_XXX1, TABLE_xxx2 FROM TABLE_XXX1 JOIN TABLE_XXX2").parseStatement();
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void parseStatementWithDeleteMultipleTableWithUsing() {
        new MySqlStatementParser(null, Collections.emptyList(), "DELETE FROM TABLE_XXX1, TABLE_xxx2 USING TABLE_XXX1 JOIN TABLE_XXX2").parseStatement();
    }
    
    @Test
    public void parseWithSpecialSyntax() throws SQLException {
        parseWithSpecialSyntax(JdbcConstants.MYSQL, "DELETE `TABLE_XXX` WHERE `field1`=1", "DELETE [Token(TABLE_XXX)] WHERE `field1`=1");
        parseWithSpecialSyntax(JdbcConstants.MYSQL, "DELETE LOW_PRIORITY QUICK IGNORE TABLE_XXX PARTITION (partition_1) WHERE field1=1 ORDER BY field1 LIMIT 10",
                "DELETE LOW_PRIORITY QUICK IGNORE [Token(TABLE_XXX)] PARTITION (partition_1) WHERE field1=1 ORDER BY field1 LIMIT 10");
        parseWithSpecialSyntax(JdbcConstants.MYSQL, "DELETE FROM TABLE_XXX PARTITION (partition_1, partition_2,partition_3) WHERE field1=1",
                "DELETE FROM [Token(TABLE_XXX)] PARTITION (partition_1, partition_2,partition_3) WHERE field1=1");
        parseWithSpecialSyntax(JdbcConstants.ORACLE, "DELETE /*+ index(field1) */ ONLY (TABLE_XXX) WHERE field1=1 RETURN * LOG ERRORS INTO TABLE_LOG",
                "DELETE /*+ index(field1) */ ONLY ([Token(TABLE_XXX)]) WHERE field1=1 RETURN * LOG ERRORS INTO TABLE_LOG");
        parseWithSpecialSyntax(JdbcConstants.ORACLE, "DELETE /*+ index(field1) */ ONLY (TABLE_XXX) WHERE field1=1 RETURNING *",
                "DELETE /*+ index(field1) */ ONLY ([Token(TABLE_XXX)]) WHERE field1=1 RETURNING *");
        parseWithSpecialSyntax(JdbcConstants.SQL_SERVER,
                "WITH field_query (field1, field2) AS (SELECT field1, field2 FROM TABLE_XXX AS xxx GROUP BY field1) DELETE TOP(10) OUTPUT (inserted.field1) FROM TABLE_XXX WHERE field1=1",
                "WITH field_query (field1, field2) AS (SELECT field1, field2 FROM TABLE_XXX AS xxx GROUP BY field1) DELETE TOP(10) OUTPUT (inserted.field1) FROM [Token(TABLE_XXX)] WHERE field1=1");
        parseWithSpecialSyntax(JdbcConstants.POSTGRESQL,
                "WITH RECURSIVE field_query (field1) AS (SELECT field1 FROM TABLE_XXX AS xxx ORDER BY field1 DESC) DELETE FROM ONLY TABLE_XXX USING producers WHERE field1=1 RETURNING *",
                "WITH RECURSIVE field_query (field1) AS (SELECT field1 FROM TABLE_XXX AS xxx ORDER BY field1 DESC) DELETE FROM ONLY [Token(TABLE_XXX)] USING producers WHERE field1=1 RETURNING *");
        parseWithSpecialSyntax(JdbcConstants.POSTGRESQL,
                "WITH field1_query AS (SELECT field1 FROM TABLE_XXX), field2_query AS (SELECT field2 FROM TABLE_XXX) DELETE FROM ONLY TABLE_XXX USING producers WHERE field1=1 OUTPUT *",
                "WITH field1_query AS (SELECT field1 FROM TABLE_XXX), field2_query AS (SELECT field2 FROM TABLE_XXX) DELETE FROM ONLY [Token(TABLE_XXX)] USING producers WHERE field1=1 OUTPUT *");
    }
    
    private void parseWithSpecialSyntax(final String dbType, final String actualSQL, final String expectedSQL) throws SQLException {
        SQLDeleteStatement deleteStatement = (SQLDeleteStatement) getSqlStatementParser(dbType, actualSQL).parseStatement();
        assertThat(deleteStatement.getSqlContext().getTable().getName(), is("TABLE_XXX"));
        assertFalse(deleteStatement.getSqlContext().getTable().getAlias().isPresent());
        Iterator<Condition> conditions = deleteStatement.getSqlContext().getConditionContexts().iterator().next().getAllConditions().iterator();
        Condition condition = conditions.next();
        assertThat(condition.getColumn().getTableName(), is("TABLE_XXX"));
        assertThat(condition.getColumn().getColumnName(), is("field1"));
        assertThat(condition.getOperator(), is(Condition.BinaryOperator.EQUAL));
        assertThat(condition.getValues().size(), is(1));
        assertThat(condition.getValues().get(0), is((Comparable) 1));
        assertFalse(conditions.hasNext());
        assertThat(deleteStatement.getSqlContext().toSqlBuilder().toString().replace("([Token(TABLE_XXX)] )", "([Token(TABLE_XXX)])"), is(expectedSQL));
    }
}
