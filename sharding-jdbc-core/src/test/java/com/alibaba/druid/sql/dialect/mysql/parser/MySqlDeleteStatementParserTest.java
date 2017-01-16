package com.alibaba.druid.sql.dialect.mysql.parser;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.util.JdbcConstants;
import org.junit.Test;

import java.util.Collections;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class MySqlDeleteStatementParserTest {
    
    @Test
    public void parseStatementWithDeleteSingleTable() {
        parseStatementWithDeleteSingleTable("DELETE LOW_PRIORITY QUICK IGNORE FROM TABLE_XXX WHERE field1<1 ORDER BY field1 LIMIT 10");
        parseStatementWithDeleteSingleTable("DELETE LOW_PRIORITY QUICK IGNORE TABLE_XXX WHERE field1<1 ORDER BY field1 LIMIT 10");
    }
    
    private void parseStatementWithDeleteSingleTable(final String sql) {
        MySqlStatementParser statementParser = new MySqlStatementParser(null, Collections.emptyList(), sql);
        MySqlDeleteStatement deleteStatement = (MySqlDeleteStatement) statementParser.parseStatement();
        assertThat(deleteStatement.getDbType(), is(JdbcConstants.MYSQL));
        assertThat(deleteStatement.getTableSource().toString(), is("TABLE_XXX"));
        assertThat(((SQLBinaryOpExpr) deleteStatement.getWhere()).getLeft().toString(), is("field1"));
        assertThat(((SQLBinaryOpExpr) deleteStatement.getWhere()).getRight().toString(), is("1"));
        assertThat(((SQLBinaryOpExpr) deleteStatement.getWhere()).getOperator().getName(), is("<"));
        assertThat(deleteStatement.getOrderBy().getItems().size(), is(1));
        assertThat(deleteStatement.getOrderBy().getItems().get(0).getExpr().toString(), is("field1"));
        assertThat(((SQLIntegerExpr) deleteStatement.getLimit().getRowCount()).getNumber().intValue(), is(10));
        assertThat(deleteStatement.getIdentifiersBetweenDeleteAndFrom().size(), is(3));
        assertThat(deleteStatement.getIdentifiersBetweenDeleteAndFrom().get(0), is("LOW_PRIORITY"));
        assertThat(deleteStatement.getIdentifiersBetweenDeleteAndFrom().get(1), is("QUICK"));
        assertThat(deleteStatement.getIdentifiersBetweenDeleteAndFrom().get(2), is("IGNORE"));
        assertThat(deleteStatement.toString(), is("DELETE LOW_PRIORITY QUICK IGNORE FROM TABLE_XXX\nWHERE field1 < 1\nORDER BY field1\nLIMIT 10"));
    }
    
    @Test
    public void parseStatementWithDeleteSingleTableAndOnePartition() {
        MySqlStatementParser statementParser = new MySqlStatementParser(null, Collections.emptyList(), "DELETE FROM TABLE_XXX PARTITION(partition_1) WHERE field1<1");
        MySqlDeleteStatement deleteStatement = (MySqlDeleteStatement) statementParser.parseStatement();
        assertThat(deleteStatement.getDbType(), is(JdbcConstants.MYSQL));
        assertThat(deleteStatement.getTableSource().toString(), is("TABLE_XXX"));
        assertThat(deleteStatement.getPartitionNames().size(), is(1));
        assertThat(deleteStatement.getPartitionNames().get(0), is("partition_1"));
        assertThat(((SQLBinaryOpExpr) deleteStatement.getWhere()).getLeft().toString(), is("field1"));
        assertThat(((SQLBinaryOpExpr) deleteStatement.getWhere()).getRight().toString(), is("1"));
        assertThat(((SQLBinaryOpExpr) deleteStatement.getWhere()).getOperator().getName(), is("<"));
        assertTrue(deleteStatement.getIdentifiersBetweenDeleteAndFrom().isEmpty());
        assertThat(deleteStatement.toString(), is("DELETE FROM TABLE_XXX PARTITION (partition_1)\nWHERE field1 < 1"));
    }
    
    @Test
    public void parseStatementWithDeleteSingleTableAndMultiplePartitions() {
        MySqlStatementParser statementParser = new MySqlStatementParser(null, Collections.emptyList(), "DELETE FROM TABLE_XXX PARTITION (partition_1, partition_2,partition_3) WHERE field1<1");
        MySqlDeleteStatement deleteStatement = (MySqlDeleteStatement) statementParser.parseStatement();
        assertThat(deleteStatement.getDbType(), is(JdbcConstants.MYSQL));
        assertThat(deleteStatement.getTableSource().toString(), is("TABLE_XXX"));
        assertThat(deleteStatement.getPartitionNames().size(), is(3));
        assertThat(deleteStatement.getPartitionNames().get(0), is("partition_1"));
        assertThat(deleteStatement.getPartitionNames().get(1), is("partition_2"));
        assertThat(deleteStatement.getPartitionNames().get(2), is("partition_3"));
        assertThat(((SQLBinaryOpExpr) deleteStatement.getWhere()).getLeft().toString(), is("field1"));
        assertThat(((SQLBinaryOpExpr) deleteStatement.getWhere()).getRight().toString(), is("1"));
        assertThat(((SQLBinaryOpExpr) deleteStatement.getWhere()).getOperator().getName(), is("<"));
        assertTrue(deleteStatement.getIdentifiersBetweenDeleteAndFrom().isEmpty());
        assertThat(deleteStatement.toString(), is("DELETE FROM TABLE_XXX PARTITION (partition_1,partition_2,partition_3)\nWHERE field1 < 1"));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void parseStatementWithDeleteMultipleTable() {
        MySqlStatementParser statementParser = new MySqlStatementParser(null, Collections.emptyList(), "DELETE TABLE_XXX1, TABLE_xxx2 FROM TABLE_XXX1 JOIN TABLE_XXX2");
        statementParser.parseStatement();
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void parseStatementWithDeleteMultipleTableWithUsing() {
        MySqlStatementParser statementParser = new MySqlStatementParser(null, Collections.emptyList(), "DELETE FROM TABLE_XXX1, TABLE_xxx2 USING TABLE_XXX1 JOIN TABLE_XXX2");
        statementParser.parseStatement();
    }
}
