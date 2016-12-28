package com.alibaba.druid.sql.dialect.postgresql.parser;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.util.JdbcConstants;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class PGSQLUpdateStatementParserTest {
    
    @Test
    public void parseStatementWithUpdate() {
        parseStatementWithUpdate("UPDATE ONLY TABLE_XXX xxx SET field1=1 WHERE field1<1 RETURNING *");
        parseStatementWithUpdate("UPDATE ONLY TABLE_XXX AS xxx SET field1=1 WHERE field1<1 RETURNING *");
    }
    
    private void parseStatementWithUpdate(final String sql) {
        PGSQLStatementParser statementParser = new PGSQLStatementParser(sql);
        SQLUpdateStatement sqlInsertStatement = (SQLUpdateStatement) statementParser.parseStatement();
        assertThat(sqlInsertStatement.getDbType(), is(JdbcConstants.POSTGRESQL));
        assertThat(sqlInsertStatement.getTableSource().toString(), is("TABLE_XXX"));
        assertThat(sqlInsertStatement.getTableSource().getAlias(), is("xxx"));
        assertThat(sqlInsertStatement.getItems().size(), is(1));
        assertThat(sqlInsertStatement.getItems().get(0).getColumn().toString(), is("field1"));
        assertThat(sqlInsertStatement.getItems().get(0).getValue().toString(), is("1"));
        assertThat(((SQLBinaryOpExpr) sqlInsertStatement.getWhere()).getLeft().toString(), is("field1"));
        assertThat(((SQLBinaryOpExpr) sqlInsertStatement.getWhere()).getRight().toString(), is("1"));
        assertThat(((SQLBinaryOpExpr) sqlInsertStatement.getWhere()).getOperator().getName(), is("<"));
        assertThat(sqlInsertStatement.getIdentifiersBetweenUpdateAndTable().size(), is(1));
        assertThat(sqlInsertStatement.getIdentifiersBetweenUpdateAndTable().get(0), is("ONLY"));
        assertThat(sqlInsertStatement.getAppendices().size(), is(2));
        assertThat(sqlInsertStatement.getAppendices().get(0), is("RETURNING"));
        assertThat(sqlInsertStatement.getAppendices().get(1), is("*"));
        assertThat(sqlInsertStatement.toString(), is("UPDATE ONLY TABLE_XXX xxx\nSET field1 = 1\nWHERE field1 < 1 RETURNING *"));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void parseStatementWithUpdateFrom() {
        PGSQLStatementParser statementParser = new PGSQLStatementParser("UPDATE ONLY TABLE_XXX xxx SET field1=1 FROM TABLE_XXX");
        statementParser.parseStatement();
    }
}
