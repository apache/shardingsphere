package com.alibaba.druid.sql.dialect.sqlserver.parser;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.dialect.sqlserver.ast.stmt.SQLServerUpdateStatement;
import com.alibaba.druid.util.JdbcConstants;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class SQLServerUpdateStatementParserTest {
    
    @Test
    public void parseStatementWithUpdate() {
        SQLServerStatementParser statementParser = new SQLServerStatementParser("UPDATE TOP(10) TABLE_XXX xxx SET field1=1 OUTPUT (inserted.field1) WHERE field1<1");
        SQLServerUpdateStatement sqlInsertStatement = (SQLServerUpdateStatement) statementParser.parseStatement();
        assertThat(sqlInsertStatement.getDbType(), is(JdbcConstants.SQL_SERVER));
        assertThat(((SQLIntegerExpr) sqlInsertStatement.getTop().getExpr()).getNumber().intValue(), is(10));
        assertThat(sqlInsertStatement.getTableSource().toString(), is("TABLE_XXX"));
        assertThat(sqlInsertStatement.getTableSource().getAlias(), is("xxx"));
        assertThat(sqlInsertStatement.getItems().size(), is(1));
        assertThat(sqlInsertStatement.getItems().get(0).getColumn().toString(), is("field1"));
        assertThat(sqlInsertStatement.getItems().get(0).getValue().toString(), is("1"));
        assertThat(sqlInsertStatement.getOutput().getSelectList().size(), is(1));
        assertThat(sqlInsertStatement.getOutput().getSelectList().get(0).toString(), is("inserted.field1"));
        assertThat(((SQLBinaryOpExpr) sqlInsertStatement.getWhere()).getLeft().toString(), is("field1"));
        assertThat(((SQLBinaryOpExpr) sqlInsertStatement.getWhere()).getRight().toString(), is("1"));
        assertThat(((SQLBinaryOpExpr) sqlInsertStatement.getWhere()).getOperator().getName(), is("<"));
        assertTrue(sqlInsertStatement.getIdentifiersBetweenUpdateAndTable().isEmpty());
        assertTrue(sqlInsertStatement.getAppendices().isEmpty());
        assertThat(sqlInsertStatement.toString(), is("UPDATE TOP (10) TABLE_XXX xxx\nSET field1 = 1\nOUTPUT inserted.field1\nWHERE field1 < 1"));
    }
}
