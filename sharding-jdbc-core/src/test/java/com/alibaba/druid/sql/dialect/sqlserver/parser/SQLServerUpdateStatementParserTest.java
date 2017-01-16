package com.alibaba.druid.sql.dialect.sqlserver.parser;

import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.dialect.sqlserver.ast.stmt.SQLServerUpdateStatement;
import com.alibaba.druid.util.JdbcConstants;
import org.junit.Test;

import java.util.Collections;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class SQLServerUpdateStatementParserTest {
    
    @Test
    public void parseStatementWithUpdate() {
        SQLServerStatementParser statementParser = new SQLServerStatementParser(null, Collections.emptyList(), "UPDATE TOP(10) TABLE_XXX xxx SET field1=1 OUTPUT (inserted.field1) WHERE field1<1");
        SQLServerUpdateStatement updateStatement = (SQLServerUpdateStatement) statementParser.parseStatement();
        assertThat(updateStatement.getDbType(), is(JdbcConstants.SQL_SERVER));
        assertThat(((SQLIntegerExpr) updateStatement.getTop().getExpr()).getNumber().intValue(), is(10));
        assertThat(updateStatement.getTableSource().toString(), is("TABLE_XXX"));
        assertThat(updateStatement.getTableSource().getAlias(), is("xxx"));
        assertThat(updateStatement.getItems().size(), is(1));
        assertThat(updateStatement.getItems().get(0).getColumn().toString(), is("field1"));
        assertThat(updateStatement.getItems().get(0).getValue().toString(), is("1"));
        assertThat(updateStatement.getOutput().getSelectList().size(), is(1));
        assertThat(updateStatement.getOutput().getSelectList().get(0).toString(), is("inserted.field1"));
        assertTrue(updateStatement.getIdentifiersBetweenUpdateAndTable().isEmpty());
        assertTrue(updateStatement.getAppendices().isEmpty());
        assertThat(updateStatement.getSqlContext().getSqlBuilder().toString(), is("UPDATE TOP(10) [Token(TABLE_XXX)] AS xxx SET field1=1 OUTPUT (inserted.field1) WHERE field1<1"));
    }
}
