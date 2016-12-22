package com.alibaba.druid.sql.dialect.postgresql.parser;

import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.util.JdbcConstants;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PGSQLStatementParserTest {
    
    @Test
    public void parseStatementWithInsert() {
        PGSQLStatementParser statementParser = new PGSQLStatementParser("INSERT INTO TABLE_XXX (field1, field2) VALUES (1, 'char') RETURNING id");
        SQLInsertStatement sqlInsertStatement = (SQLInsertStatement) statementParser.parseStatement();
        assertThat(sqlInsertStatement.getDbType(), is(JdbcConstants.POSTGRESQL));
        assertThat(sqlInsertStatement.getTableName().getSimpleName(), is("TABLE_XXX"));
        assertNull(sqlInsertStatement.getTableSource().getAlias());
        assertNull(sqlInsertStatement.getAlias());
        assertThat(sqlInsertStatement.getColumns().size(), is(2));
        assertThat(((SQLIdentifierExpr) sqlInsertStatement.getColumns().get(0)).getSimpleName(), is("field1"));
        assertThat(((SQLIdentifierExpr) sqlInsertStatement.getColumns().get(1)).getSimpleName(), is("field2"));
        assertThat(sqlInsertStatement.getValues().getValues().size(), is(2));
        assertThat(((SQLIntegerExpr) sqlInsertStatement.getValues().getValues().get(0)).getNumber().intValue(), is(1));
        assertThat(sqlInsertStatement.getValues().getValues().get(1).toString(), is("'char'"));
        assertThat(((SQLCharExpr) sqlInsertStatement.getValues().getValues().get(1)).getText(), is("char"));
        assertTrue(sqlInsertStatement.getIdentifiersBetweenInsertAndInto().isEmpty());
        assertTrue(sqlInsertStatement.getIdentifiersBetweenIntoAndTable().isEmpty());
        assertThat(sqlInsertStatement.getAppendices().size(), is(2));
        assertThat(sqlInsertStatement.getAppendices().get(0), is("RETURNING"));
        assertThat(sqlInsertStatement.getAppendices().get(1), is("id"));
        assertThat(sqlInsertStatement.toString(), is("INSERT INTO TABLE_XXX\n\t(field1, field2)\nVALUES (1, 'char') RETURNING id"));
    }
    
    @Test
    public void parseStatementWithInsertDefaultValue() {
        PGSQLStatementParser statementParser = new PGSQLStatementParser("INSERT INTO TABLE_XXX DEFAULT VALUES RETURNING *");
        SQLInsertStatement sqlInsertStatement = (SQLInsertStatement) statementParser.parseStatement();
        assertThat(sqlInsertStatement.getDbType(), is(JdbcConstants.POSTGRESQL));
        assertThat(sqlInsertStatement.getTableName().getSimpleName(), is("TABLE_XXX"));
        assertNull(sqlInsertStatement.getTableSource().getAlias());
        assertNull(sqlInsertStatement.getAlias());
        assertTrue(sqlInsertStatement.getColumns().isEmpty());
        assertNull(sqlInsertStatement.getValues());
        assertTrue(sqlInsertStatement.getIdentifiersBetweenInsertAndInto().isEmpty());
        assertTrue(sqlInsertStatement.getIdentifiersBetweenIntoAndTable().isEmpty());
        assertThat(sqlInsertStatement.getAppendices().size(), is(4));
        assertThat(sqlInsertStatement.getAppendices().get(0), is("DEFAULT"));
        assertThat(sqlInsertStatement.getAppendices().get(1), is("VALUES"));
        assertThat(sqlInsertStatement.getAppendices().get(2), is("RETURNING"));
        assertThat(sqlInsertStatement.getAppendices().get(3), is("*"));
        assertThat(sqlInsertStatement.toString(), is("INSERT INTO TABLE_XXX DEFAULT VALUES RETURNING *"));
    }
}