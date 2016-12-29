package com.alibaba.druid.sql.dialect.mysql.parser;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.alibaba.druid.util.JdbcConstants;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class MySqlUpdateStatementParserTest {
    
    @Test
    public void parseStatementWithUpdateSingleTable() {
        MySqlStatementParser statementParser = new MySqlStatementParser("UPDATE LOW_PRIORITY IGNORE TABLE_XXX xxx SET field1=1 WHERE field1<1 ORDER BY field1 LIMIT 10");
        MySqlUpdateStatement updateStatement = (MySqlUpdateStatement) statementParser.parseStatement();
        assertThat(updateStatement.getDbType(), is(JdbcConstants.MYSQL));
        assertThat(updateStatement.getTableSource().toString(), is("TABLE_XXX"));
        assertThat(updateStatement.getTableSource().getAlias(), is("xxx"));
        assertThat(updateStatement.getItems().size(), is(1));
        assertThat(updateStatement.getItems().get(0).getColumn().toString(), is("field1"));
        assertThat(updateStatement.getItems().get(0).getValue().toString(), is("1"));
        assertThat(((SQLBinaryOpExpr) updateStatement.getWhere()).getLeft().toString(), is("field1"));
        assertThat(((SQLBinaryOpExpr) updateStatement.getWhere()).getRight().toString(), is("1"));
        assertThat(((SQLBinaryOpExpr) updateStatement.getWhere()).getOperator().getName(), is("<"));
        assertThat(updateStatement.getOrderBy().getItems().size(), is(1));
        assertThat(updateStatement.getOrderBy().getItems().get(0).getExpr().toString(), is("field1"));
        assertThat(((SQLIntegerExpr) updateStatement.getLimit().getRowCount()).getNumber().intValue(), is(10));
        assertThat(updateStatement.getIdentifiersBetweenUpdateAndTable().size(), is(2));
        assertThat(updateStatement.getIdentifiersBetweenUpdateAndTable().get(0), is("LOW_PRIORITY"));
        assertThat(updateStatement.getIdentifiersBetweenUpdateAndTable().get(1), is("IGNORE"));
        assertThat(updateStatement.toString(), is("UPDATE LOW_PRIORITY IGNORE TABLE_XXX xxx\nSET field1 = 1\nWHERE field1 < 1\nORDER BY field1\nLIMIT 10"));
    }
}
