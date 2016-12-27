package com.alibaba.druid.sql.dialect.postgresql.parser;

import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.statement.AbstractSQLInsertStatement;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGInsertStatement;
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
        AbstractSQLInsertStatement sqlInsertStatement = (AbstractSQLInsertStatement) statementParser.parseStatement();
        assertThat(sqlInsertStatement.getDbType(), is(JdbcConstants.POSTGRESQL));
        assertThat(sqlInsertStatement.getTableSource().getExpr().toString(), is("TABLE_XXX"));
        assertNull(sqlInsertStatement.getTableSource().getAlias());
        assertThat(sqlInsertStatement.getColumns().size(), is(2));
        assertThat(((SQLIdentifierExpr) sqlInsertStatement.getColumns().get(0)).getSimpleName(), is("field1"));
        assertThat(((SQLIdentifierExpr) sqlInsertStatement.getColumns().get(1)).getSimpleName(), is("field2"));
        assertThat(sqlInsertStatement.getValuesList().get(0).getValues().size(), is(2));
        assertThat(((SQLIntegerExpr) sqlInsertStatement.getValuesList().get(0).getValues().get(0)).getNumber().intValue(), is(1));
        assertThat(sqlInsertStatement.getValuesList().get(0).getValues().get(1).toString(), is("'char'"));
        assertThat(((SQLCharExpr) sqlInsertStatement.getValuesList().get(0).getValues().get(1)).getText(), is("char"));
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
        AbstractSQLInsertStatement sqlInsertStatement = (AbstractSQLInsertStatement) statementParser.parseStatement();
        assertThat(sqlInsertStatement.getDbType(), is(JdbcConstants.POSTGRESQL));
        assertThat(sqlInsertStatement.getTableSource().getExpr().toString(), is("TABLE_XXX"));
        assertNull(sqlInsertStatement.getTableSource().getAlias());
        assertTrue(sqlInsertStatement.getColumns().isEmpty());
        assertTrue(sqlInsertStatement.getValuesList().isEmpty());
        assertTrue(sqlInsertStatement.getIdentifiersBetweenInsertAndInto().isEmpty());
        assertTrue(sqlInsertStatement.getIdentifiersBetweenIntoAndTable().isEmpty());
        assertThat(sqlInsertStatement.getAppendices().size(), is(4));
        assertThat(sqlInsertStatement.getAppendices().get(0), is("DEFAULT"));
        assertThat(sqlInsertStatement.getAppendices().get(1), is("VALUES"));
        assertThat(sqlInsertStatement.getAppendices().get(2), is("RETURNING"));
        assertThat(sqlInsertStatement.getAppendices().get(3), is("*"));
        assertThat(sqlInsertStatement.toString(), is("INSERT INTO TABLE_XXX DEFAULT VALUES RETURNING *"));
    }
    
    @Test
    public void parseStatementWithInsertBatchValues() {
        PGSQLStatementParser statementParser = new PGSQLStatementParser("INSERT INTO TABLE_XXX VALUES (1, 'char1'), (2, 'char2')");
        PGInsertStatement sqlInsertStatement = (PGInsertStatement) statementParser.parseStatement();
        assertThat(sqlInsertStatement.getDbType(), is(JdbcConstants.POSTGRESQL));
        assertThat(sqlInsertStatement.getTableSource().getExpr().toString(), is("TABLE_XXX"));
        assertNull(sqlInsertStatement.getTableSource().getAlias());
        assertTrue(sqlInsertStatement.getColumns().isEmpty());
        assertThat(sqlInsertStatement.getValuesList().size(), is(2));
        assertThat(sqlInsertStatement.getValuesList().get(0).getValues().size(), is(2));
        assertThat(((SQLIntegerExpr) sqlInsertStatement.getValuesList().get(0).getValues().get(0)).getNumber().intValue(), is(1));
        assertThat(((SQLCharExpr) sqlInsertStatement.getValuesList().get(0).getValues().get(1)).getText(), is("char1"));
        assertThat(((SQLIntegerExpr) sqlInsertStatement.getValuesList().get(1).getValues().get(0)).getNumber().intValue(), is(2));
        assertThat(((SQLCharExpr) sqlInsertStatement.getValuesList().get(1).getValues().get(1)).getText(), is("char2"));
        assertTrue(sqlInsertStatement.getIdentifiersBetweenInsertAndInto().isEmpty());
        assertTrue(sqlInsertStatement.getIdentifiersBetweenTableAndValues().isEmpty());
        assertThat(sqlInsertStatement.toString(), is("INSERT INTO TABLE_XXX\nVALUES (1, 'char1'), \n(2, 'char2')"));
    }
}
