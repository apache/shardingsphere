package com.alibaba.druid.sql.dialect.postgresql.parser;

import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGUpdateStatement;
import com.alibaba.druid.util.JdbcConstants;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class PGSQLUpdateStatementParserTest {
    
    @Test
    public void parseStatementWithUpdate() {
        parseStatementWithUpdate("UPDATE ONLY TABLE_XXX xxx SET field1=1 WHERE field1<1 RETURNING *");
        parseStatementWithUpdate("UPDATE ONLY TABLE_XXX AS xxx SET field1=1 WHERE field1<1 RETURNING *");
    }
    
    private void parseStatementWithUpdate(final String sql) {
        PGSQLStatementParser statementParser = new PGSQLStatementParser(null, Collections.emptyList(), sql);
        PGUpdateStatement updateStatement = (PGUpdateStatement) statementParser.parseStatement();
        assertThat(updateStatement.getDbType(), is(JdbcConstants.POSTGRESQL));
        assertThat(updateStatement.getTableSource().toString(), is("TABLE_XXX"));
        assertThat(updateStatement.getTableSource().getAlias(), is("xxx"));
        assertThat(updateStatement.getItems().size(), is(1));
        assertThat(updateStatement.getItems().get(0).getColumn().toString(), is("field1"));
        assertThat(updateStatement.getItems().get(0).getValue().toString(), is("1"));
        assertThat(updateStatement.getIdentifiersBetweenUpdateAndTable().size(), is(1));
        assertThat(updateStatement.getIdentifiersBetweenUpdateAndTable().get(0), is("ONLY"));
        assertThat(updateStatement.getAppendices().size(), is(2));
        assertThat(updateStatement.getAppendices().get(0), is("RETURNING"));
        assertThat(updateStatement.getAppendices().get(1), is("*"));
        assertThat(updateStatement.getSqlContext().getSqlBuilder().toString(), is("UPDATE ONLY [Token(TABLE_XXX)] AS xxx SET field1=1 WHERE field1<1 RETURNING *"));
    }
    
    @Test
    public void parseStatementWithUpdateFrom() {
        PGSQLStatementParser statementParser = new PGSQLStatementParser(null, Collections.emptyList(), "UPDATE ONLY TABLE_XXX xxx SET field1=1 FROM TABLE_XXX");
        PGUpdateStatement updateStatement = (PGUpdateStatement) statementParser.parseStatement();
        assertThat(updateStatement.getDbType(), is(JdbcConstants.POSTGRESQL));
        assertThat(updateStatement.getTableSource().toString(), is("TABLE_XXX"));
        assertThat(updateStatement.getTableSource().getAlias(), is("xxx"));
        assertThat(updateStatement.getItems().size(), is(1));
        assertThat(updateStatement.getItems().get(0).getColumn().toString(), is("field1"));
        assertThat(updateStatement.getItems().get(0).getValue().toString(), is("1"));
        assertThat((updateStatement.getFrom()).toString(), is("TABLE_XXX"));
        assertThat(updateStatement.getSqlContext().getSqlBuilder().toString(), is("UPDATE ONLY [Token(TABLE_XXX)] AS xxx SET field1=1 FROM TABLE_XXX"));
    }
}
