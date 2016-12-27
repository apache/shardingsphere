package com.alibaba.druid.sql.dialect.sqlserver.parser;

import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.statement.AbstractSQLInsertStatement;
import com.alibaba.druid.util.JdbcConstants;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SQLServerStatementParserTest {
    
    @Test
    public void parseStatementWithInsert() {
        SQLServerStatementParser statementParser = new SQLServerStatementParser("INSERT TOP(10) INTO OUTPUT TABLE_XXX VALUES (1, 'char')");
        AbstractSQLInsertStatement sqlInsertStatement = (AbstractSQLInsertStatement) statementParser.parseStatement();
        assertThat(sqlInsertStatement.getDbType(), is(JdbcConstants.SQL_SERVER));
        assertThat(sqlInsertStatement.getTableSource().getExpr().toString(), is("TABLE_XXX"));
        assertNull(sqlInsertStatement.getTableSource().getAlias());
        assertTrue(sqlInsertStatement.getColumns().isEmpty());
        assertThat(sqlInsertStatement.getValues().getValues().size(), is(2));
        assertThat(((SQLIntegerExpr) sqlInsertStatement.getValues().getValues().get(0)).getNumber().intValue(), is(1));
        assertThat(sqlInsertStatement.getValues().getValues().get(1).toString(), is("'char'"));
        assertThat(((SQLCharExpr) sqlInsertStatement.getValues().getValues().get(1)).getText(), is("char"));
        assertThat(sqlInsertStatement.getIdentifiersBetweenInsertAndInto().size(), is(4));
        assertThat(sqlInsertStatement.getIdentifiersBetweenInsertAndInto().get(0), is("TOP"));
        assertThat(sqlInsertStatement.getIdentifiersBetweenInsertAndInto().get(1), is("("));
        assertThat(sqlInsertStatement.getIdentifiersBetweenInsertAndInto().get(2), is("10"));
        assertThat(sqlInsertStatement.getIdentifiersBetweenInsertAndInto().get(3), is(")"));
        assertThat(sqlInsertStatement.getIdentifiersBetweenIntoAndTable().size(), is(1));
        assertThat(sqlInsertStatement.getIdentifiersBetweenIntoAndTable().get(0), is("OUTPUT"));
        assertTrue(sqlInsertStatement.getAppendices().isEmpty());
        assertThat(sqlInsertStatement.toString(), is("INSERT TOP ( 10 ) INTO OUTPUT TABLE_XXX\nVALUES\n(1, 'char')"));
    }
    
    @Test
    public void parseStatementWithInsertDefaultValue() {
        SQLServerStatementParser statementParser = new SQLServerStatementParser("INSERT INTO TABLE_XXX (field1, field2) DEFAULT VALUES");
        AbstractSQLInsertStatement sqlInsertStatement = (AbstractSQLInsertStatement) statementParser.parseStatement();
        assertThat(sqlInsertStatement.getDbType(), is(JdbcConstants.SQL_SERVER));
        assertThat(sqlInsertStatement.getTableSource().getExpr().toString(), is("TABLE_XXX"));
        assertNull(sqlInsertStatement.getTableSource().getAlias());
        assertThat(sqlInsertStatement.getColumns().size(), is(2));
        assertThat(((SQLIdentifierExpr) sqlInsertStatement.getColumns().get(0)).getSimpleName(), is("field1"));
        assertThat(((SQLIdentifierExpr) sqlInsertStatement.getColumns().get(1)).getSimpleName(), is("field2"));
        assertNull(sqlInsertStatement.getValues());
        assertTrue(sqlInsertStatement.getIdentifiersBetweenInsertAndInto().isEmpty());
        assertTrue(sqlInsertStatement.getIdentifiersBetweenIntoAndTable().isEmpty());
        assertThat(sqlInsertStatement.getAppendices().size(), is(2));
        assertThat(sqlInsertStatement.getAppendices().get(0), is("DEFAULT"));
        assertThat(sqlInsertStatement.getAppendices().get(1), is("VALUES"));
        assertThat(sqlInsertStatement.toString(), is("INSERT INTO TABLE_XXX\n\t(field1, field2) DEFAULT VALUES"));
    }
}
