package com.alibaba.druid.sql.dialect.oracle.parser;

import com.alibaba.druid.sql.ast.SQLCommentHint;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectTableReference;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleUpdateStatement;
import com.alibaba.druid.util.JdbcConstants;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class OracleUpdateStatementParserTest {
    
    @Test
    public void parseStatementWithUpdateWithReturn() {
        OracleStatementParser statementParser = new OracleStatementParser("UPDATE /*+ index(field1) */ ONLY TABLE_XXX as xxx SET field1=1 WHERE field1<1 RETURN * LOG ERRORS INTO TABLE_LOG");
        OracleUpdateStatement updateStatement = (OracleUpdateStatement) statementParser.parseStatement();
        assertThat(updateStatement.getDbType(), is(JdbcConstants.ORACLE));
        assertThat(updateStatement.getHints().size(), is(1));
        assertThat(((SQLCommentHint) updateStatement.getHints().get(0)).getText(), is(" index(field1) "));
        assertThat(((OracleSelectTableReference) updateStatement.getTableSource()).getExpr().toString(), is("TABLE_XXX"));
        assertThat(updateStatement.getTableSource().getAlias(), is("xxx"));
        assertThat(updateStatement.getItems().size(), is(1));
        assertThat(updateStatement.getItems().get(0).getColumn().toString(), is("field1"));
        assertThat(updateStatement.getItems().get(0).getValue().toString(), is("1"));
        assertThat(((SQLBinaryOpExpr) updateStatement.getWhere()).getLeft().toString(), is("field1"));
        assertThat(((SQLBinaryOpExpr) updateStatement.getWhere()).getRight().toString(), is("1"));
        assertThat(((SQLBinaryOpExpr) updateStatement.getWhere()).getOperator().getName(), is("<"));
        assertThat(updateStatement.getIdentifiersBetweenUpdateAndTable().size(), is(1));
        assertThat(updateStatement.getIdentifiersBetweenUpdateAndTable().get(0), is("ONLY"));
        assertThat(updateStatement.getAppendices().size(), is(6));
        assertThat(updateStatement.getAppendices().get(0), is("RETURN"));
        assertThat(updateStatement.getAppendices().get(1), is("*"));
        assertThat(updateStatement.getAppendices().get(2), is("LOG"));
        assertThat(updateStatement.getAppendices().get(3), is("ERRORS"));
        assertThat(updateStatement.getAppendices().get(4), is("INTO"));
        assertThat(updateStatement.getAppendices().get(5), is("TABLE_LOG"));
        assertThat(updateStatement.toString(), is("UPDATE /*+ index(field1) */ ONLY TABLE_XXX xxx\nSET field1 = 1\nWHERE field1 < 1 RETURN * LOG ERRORS INTO TABLE_LOG"));
    }
    
    @Test
    public void parseStatementWithUpdateWithReturning() {
        OracleStatementParser statementParser = new OracleStatementParser("UPDATE /*+ index(field1) */ ONLY TABLE_XXX xxx SET field1=1 WHERE field1<1 RETURNING *");
        OracleUpdateStatement updateStatement = (OracleUpdateStatement) statementParser.parseStatement();
        assertThat(updateStatement.getDbType(), is(JdbcConstants.ORACLE));
        assertThat(updateStatement.getHints().size(), is(1));
        assertThat(((SQLCommentHint) updateStatement.getHints().get(0)).getText(), is(" index(field1) "));
        assertThat(((OracleSelectTableReference) updateStatement.getTableSource()).getExpr().toString(), is("TABLE_XXX"));
        assertThat(updateStatement.getTableSource().getAlias(), is("xxx"));
        assertThat(updateStatement.getItems().size(), is(1));
        assertThat(updateStatement.getItems().get(0).getColumn().toString(), is("field1"));
        assertThat(updateStatement.getItems().get(0).getValue().toString(), is("1"));
        assertThat(((SQLBinaryOpExpr) updateStatement.getWhere()).getLeft().toString(), is("field1"));
        assertThat(((SQLBinaryOpExpr) updateStatement.getWhere()).getRight().toString(), is("1"));
        assertThat(((SQLBinaryOpExpr) updateStatement.getWhere()).getOperator().getName(), is("<"));
        assertThat(updateStatement.getIdentifiersBetweenUpdateAndTable().size(), is(1));
        assertThat(updateStatement.getIdentifiersBetweenUpdateAndTable().get(0), is("ONLY"));
        assertThat(updateStatement.getAppendices().size(), is(2));
        assertThat(updateStatement.getAppendices().get(0), is("RETURNING"));
        assertThat(updateStatement.getAppendices().get(1), is("*"));
        assertThat(updateStatement.toString(), is("UPDATE /*+ index(field1) */ ONLY TABLE_XXX xxx\nSET field1 = 1\nWHERE field1 < 1 RETURNING *"));
    }
    
    @Test
    public void parseStatementWithUpdateWithLog() {
        OracleStatementParser statementParser = new OracleStatementParser("UPDATE /*+ index(field1) */ ONLY TABLE_XXX as xxx SET field1=1 WHERE field1<1 LOG ERRORS INTO TABLE_LOG");
        OracleUpdateStatement updateStatement = (OracleUpdateStatement) statementParser.parseStatement();
        assertThat(updateStatement.getDbType(), is(JdbcConstants.ORACLE));
        assertThat(updateStatement.getHints().size(), is(1));
        assertThat(((SQLCommentHint) updateStatement.getHints().get(0)).getText(), is(" index(field1) "));
        assertThat(((OracleSelectTableReference) updateStatement.getTableSource()).getExpr().toString(), is("TABLE_XXX"));
        assertThat(updateStatement.getTableSource().getAlias(), is("xxx"));
        assertThat(updateStatement.getItems().size(), is(1));
        assertThat(updateStatement.getItems().get(0).getColumn().toString(), is("field1"));
        assertThat(updateStatement.getItems().get(0).getValue().toString(), is("1"));
        assertThat(((SQLBinaryOpExpr) updateStatement.getWhere()).getLeft().toString(), is("field1"));
        assertThat(((SQLBinaryOpExpr) updateStatement.getWhere()).getRight().toString(), is("1"));
        assertThat(((SQLBinaryOpExpr) updateStatement.getWhere()).getOperator().getName(), is("<"));
        assertThat(updateStatement.getIdentifiersBetweenUpdateAndTable().size(), is(1));
        assertThat(updateStatement.getIdentifiersBetweenUpdateAndTable().get(0), is("ONLY"));
        assertThat(updateStatement.getAppendices().size(), is(4));
        assertThat(updateStatement.getAppendices().get(0), is("LOG"));
        assertThat(updateStatement.getAppendices().get(1), is("ERRORS"));
        assertThat(updateStatement.getAppendices().get(2), is("INTO"));
        assertThat(updateStatement.getAppendices().get(3), is("TABLE_LOG"));
        assertThat(updateStatement.toString(), is("UPDATE /*+ index(field1) */ ONLY TABLE_XXX xxx\nSET field1 = 1\nWHERE field1 < 1 LOG ERRORS INTO TABLE_LOG"));
    }
}
