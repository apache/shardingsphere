package com.alibaba.druid.sql.dialect.sqlserver.parser;

import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.dialect.sqlserver.ast.stmt.SQLServerInsertStatement;
import com.alibaba.druid.util.JdbcConstants;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SQLServerInsertStatementParserTest {
    
    @Test
    public void parseStatementWithInsert() {
        SQLServerStatementParser statementParser = new SQLServerStatementParser("INSERT TOP(10) INTO OUTPUT TABLE_XXX VALUES (1, 'char')");
        SQLServerInsertStatement insertStatement = (SQLServerInsertStatement) statementParser.parseStatement();
        assertThat(insertStatement.getDbType(), is(JdbcConstants.SQL_SERVER));
        assertThat(insertStatement.getTableSource().getExpr().toString(), is("TABLE_XXX"));
        assertNull(insertStatement.getTableSource().getAlias());
        assertTrue(insertStatement.getColumns().isEmpty());
        assertThat(insertStatement.getValuesList().size(), is(1));
        assertThat(insertStatement.getValuesList().get(0).getValues().size(), is(2));
        assertThat(((SQLIntegerExpr) insertStatement.getValuesList().get(0).getValues().get(0)).getNumber().intValue(), is(1));
        assertThat(insertStatement.getValuesList().get(0).getValues().get(1).toString(), is("'char'"));
        assertThat(((SQLCharExpr) insertStatement.getValuesList().get(0).getValues().get(1)).getText(), is("char"));
        assertThat(insertStatement.getIdentifiersBetweenInsertAndInto().size(), is(4));
        assertThat(insertStatement.getIdentifiersBetweenInsertAndInto().get(0), is("TOP"));
        assertThat(insertStatement.getIdentifiersBetweenInsertAndInto().get(1), is("("));
        assertThat(insertStatement.getIdentifiersBetweenInsertAndInto().get(2), is("10"));
        assertThat(insertStatement.getIdentifiersBetweenInsertAndInto().get(3), is(")"));
        assertThat(insertStatement.getIdentifiersBetweenIntoAndTable().size(), is(1));
        assertThat(insertStatement.getIdentifiersBetweenIntoAndTable().get(0), is("OUTPUT"));
        assertTrue(insertStatement.getAppendices().isEmpty());
        assertThat(insertStatement.toString(), is("INSERT TOP ( 10 ) INTO OUTPUT TABLE_XXX\nVALUES\n(1, 'char')"));
    }
    
    @Test
    public void parseStatementWithInsertDefaultValue() {
        SQLServerStatementParser statementParser = new SQLServerStatementParser("INSERT INTO TABLE_XXX (field1, field2) DEFAULT VALUES");
        SQLServerInsertStatement insertStatement = (SQLServerInsertStatement) statementParser.parseStatement();
        assertThat(insertStatement.getDbType(), is(JdbcConstants.SQL_SERVER));
        assertThat(insertStatement.getTableSource().getExpr().toString(), is("TABLE_XXX"));
        assertNull(insertStatement.getTableSource().getAlias());
        assertThat(insertStatement.getColumns().size(), is(2));
        assertThat(((SQLIdentifierExpr) insertStatement.getColumns().get(0)).getSimpleName(), is("field1"));
        assertThat(((SQLIdentifierExpr) insertStatement.getColumns().get(1)).getSimpleName(), is("field2"));
        assertTrue(insertStatement.getValuesList().isEmpty());
        assertTrue(insertStatement.getIdentifiersBetweenInsertAndInto().isEmpty());
        assertTrue(insertStatement.getIdentifiersBetweenIntoAndTable().isEmpty());
        assertThat(insertStatement.getAppendices().size(), is(2));
        assertThat(insertStatement.getAppendices().get(0), is("DEFAULT"));
        assertThat(insertStatement.getAppendices().get(1), is("VALUES"));
        assertThat(insertStatement.toString(), is("INSERT INTO TABLE_XXX\n\t(field1, field2) DEFAULT VALUES"));
    }
    
    @Test
    public void parseStatementWithInsertBatchValues() {
        SQLServerStatementParser statementParser = new SQLServerStatementParser("INSERT INTO TABLE_XXX VALUES (1, 'char1'), (2, 'char2')");
        SQLServerInsertStatement insertStatement = (SQLServerInsertStatement) statementParser.parseStatement();
        assertThat(insertStatement.getDbType(), is(JdbcConstants.SQL_SERVER));
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
        assertThat(insertStatement.toString(), is("INSERT INTO TABLE_XXX\nVALUES\n(1, 'char1'),\n(2, 'char2')"));
    }
}
