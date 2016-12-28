package com.alibaba.druid.sql.dialect.mysql.parser;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.util.JdbcConstants;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class MySqlUpdateStatementParserTest {
    
    @Test
    public void parseStatementWithUpdateSingleTable() {
        MySqlStatementParser statementParser = new MySqlStatementParser("UPDATE LOW_PRIORITY IGNORE TABLE_XXX xxx SET field1=1 WHERE field1<1 ORDER BY field1 LIMIT 10");
        SQLUpdateStatement sqlInsertStatement = (SQLUpdateStatement) statementParser.parseStatement();
        assertThat(sqlInsertStatement.getDbType(), is(JdbcConstants.MYSQL));
        assertThat(sqlInsertStatement.getTableSource().toString(), is("TABLE_XXX"));
        assertThat(sqlInsertStatement.getTableSource().getAlias(), is("xxx"));
        assertThat(sqlInsertStatement.getItems().size(), is(1));
        assertThat(sqlInsertStatement.getItems().get(0).getColumn().toString(), is("field1"));
        assertThat(sqlInsertStatement.getItems().get(0).getValue().toString(), is("1"));
        assertThat(((SQLBinaryOpExpr) sqlInsertStatement.getWhere()).getLeft().toString(), is("field1"));
        assertThat(((SQLBinaryOpExpr) sqlInsertStatement.getWhere()).getRight().toString(), is("1"));
        assertThat(((SQLBinaryOpExpr) sqlInsertStatement.getWhere()).getOperator().getName(), is("<"));
        assertThat(sqlInsertStatement.getIdentifiersBetweenUpdateAndTable().size(), is(2));
        assertThat(sqlInsertStatement.getIdentifiersBetweenUpdateAndTable().get(0), is("LOW_PRIORITY"));
        assertThat(sqlInsertStatement.getIdentifiersBetweenUpdateAndTable().get(1), is("IGNORE"));
        assertThat(sqlInsertStatement.toString(), is("UPDATE LOW_PRIORITY IGNORE TABLE_XXX xxx\nSET field1 = 1\nWHERE field1 < 1\nORDER BY field1\nLIMIT 10"));
    }
}
