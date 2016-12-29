package com.alibaba.druid.sql.dialect.postgresql.parser;

import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGInsertStatement;
import com.alibaba.druid.util.JdbcConstants;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PGSQLInsertStatementParserTest {
    
    @Test
    public void parseStatementWithInsert() {
        PGSQLStatementParser statementParser = new PGSQLStatementParser("INSERT INTO TABLE_XXX (field1, field2) VALUES (1, 'char') RETURNING id");
        PGInsertStatement insertStatement = (PGInsertStatement) statementParser.parseStatement();
        assertThat(insertStatement.getDbType(), is(JdbcConstants.POSTGRESQL));
        assertThat(insertStatement.getTableSource().getExpr().toString(), is("TABLE_XXX"));
        assertNull(insertStatement.getTableSource().getAlias());
        assertThat(insertStatement.getColumns().size(), is(2));
        assertThat(((SQLIdentifierExpr) insertStatement.getColumns().get(0)).getSimpleName(), is("field1"));
        assertThat(((SQLIdentifierExpr) insertStatement.getColumns().get(1)).getSimpleName(), is("field2"));
        assertThat(insertStatement.getValuesList().get(0).getValues().size(), is(2));
        assertThat(((SQLIntegerExpr) insertStatement.getValuesList().get(0).getValues().get(0)).getNumber().intValue(), is(1));
        assertThat(insertStatement.getValuesList().get(0).getValues().get(1).toString(), is("'char'"));
        assertThat(((SQLCharExpr) insertStatement.getValuesList().get(0).getValues().get(1)).getText(), is("char"));
        assertTrue(insertStatement.getIdentifiersBetweenInsertAndInto().isEmpty());
        assertTrue(insertStatement.getIdentifiersBetweenIntoAndTable().isEmpty());
        assertThat(insertStatement.getAppendices().size(), is(2));
        assertThat(insertStatement.getAppendices().get(0), is("RETURNING"));
        assertThat(insertStatement.getAppendices().get(1), is("id"));
        assertThat(insertStatement.toString(), is("INSERT INTO TABLE_XXX\n\t(field1, field2)\nVALUES (1, 'char') RETURNING id"));
    }
    
    @Test
    public void parseStatementWithInsertDefaultValue() {
        PGSQLStatementParser statementParser = new PGSQLStatementParser("INSERT INTO TABLE_XXX DEFAULT VALUES RETURNING *");
        PGInsertStatement insertStatement = (PGInsertStatement) statementParser.parseStatement();
        assertThat(insertStatement.getDbType(), is(JdbcConstants.POSTGRESQL));
        assertThat(insertStatement.getTableSource().getExpr().toString(), is("TABLE_XXX"));
        assertNull(insertStatement.getTableSource().getAlias());
        assertTrue(insertStatement.getColumns().isEmpty());
        assertTrue(insertStatement.getValuesList().isEmpty());
        assertTrue(insertStatement.getIdentifiersBetweenInsertAndInto().isEmpty());
        assertTrue(insertStatement.getIdentifiersBetweenIntoAndTable().isEmpty());
        assertThat(insertStatement.getAppendices().size(), is(4));
        assertThat(insertStatement.getAppendices().get(0), is("DEFAULT"));
        assertThat(insertStatement.getAppendices().get(1), is("VALUES"));
        assertThat(insertStatement.getAppendices().get(2), is("RETURNING"));
        assertThat(insertStatement.getAppendices().get(3), is("*"));
        assertThat(insertStatement.toString(), is("INSERT INTO TABLE_XXX DEFAULT VALUES RETURNING *"));
    }
    
    @Test
    public void parseStatementWithInsertBatchValues() {
        PGSQLStatementParser statementParser = new PGSQLStatementParser("INSERT INTO TABLE_XXX VALUES (1, 'char1'), (2, 'char2')");
        PGInsertStatement insertStatement = (PGInsertStatement) statementParser.parseStatement();
        assertThat(insertStatement.getDbType(), is(JdbcConstants.POSTGRESQL));
        assertThat(insertStatement.getTableSource().getExpr().toString(), is("TABLE_XXX"));
        assertNull(insertStatement.getTableSource().getAlias());
        assertTrue(insertStatement.getColumns().isEmpty());
        assertThat(insertStatement.getValuesList().size(), is(2));
        assertThat(insertStatement.getValuesList().get(0).getValues().size(), is(2));
        assertThat(((SQLIntegerExpr) insertStatement.getValuesList().get(0).getValues().get(0)).getNumber().intValue(), is(1));
        assertThat(((SQLCharExpr) insertStatement.getValuesList().get(0).getValues().get(1)).getText(), is("char1"));
        assertThat(((SQLIntegerExpr) insertStatement.getValuesList().get(1).getValues().get(0)).getNumber().intValue(), is(2));
        assertThat(((SQLCharExpr) insertStatement.getValuesList().get(1).getValues().get(1)).getText(), is("char2"));
        assertTrue(insertStatement.getIdentifiersBetweenInsertAndInto().isEmpty());
        assertTrue(insertStatement.getIdentifiersBetweenTableAndValues().isEmpty());
        assertThat(insertStatement.toString(), is("INSERT INTO TABLE_XXX\nVALUES (1, 'char1'), \n(2, 'char2')"));
    }
}
