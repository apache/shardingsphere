package com.alibaba.druid.sql.dialect.oracle.parser;

import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.statement.AbstractSQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.util.JdbcConstants;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class OracleInsertStatementParserTest {
    
    @Test(expected = UnsupportedOperationException.class)
    public void parseStatementWithInsertAllValue() {
        OracleStatementParser statementParser = new OracleStatementParser("INSERT ALL INTO TABLE_XXX (field1) VALUES (field1) SELECT field1 FROM TABLE_XXX2");
        statementParser.parseStatement();
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void parseStatementWithInsertFirst() {
        OracleStatementParser statementParser = new OracleStatementParser("INSERT FIRST INTO TABLE_XXX (field1) VALUES (field1) SELECT field1 FROM TABLE_XXX2");
        statementParser.parseStatement();
    }
    
    @Test
    public void parseStatementWithInsertValues() {
        OracleStatementParser statementParser = new OracleStatementParser(
                "INSERT /*+ index(field1) */ INTO TABLE_XXX XXX (`field1`, `field2`) VALUES (1, 'value_char') RETURNING field1*2 LOG ERRORS INTO TABLE_LOG");
        AbstractSQLInsertStatement sqlInsertStatement = (AbstractSQLInsertStatement) statementParser.parseStatement();
        assertThat(sqlInsertStatement.getDbType(), is(JdbcConstants.ORACLE));
        assertThat(sqlInsertStatement.getTableSource().getExpr().toString(), is("TABLE_XXX"));
        assertThat(sqlInsertStatement.getTableSource().getAlias(), is("XXX"));
        assertThat(sqlInsertStatement.getColumns().size(), is(2));
        assertThat(((SQLIdentifierExpr) sqlInsertStatement.getColumns().get(0)).getSimpleName(), is("`field1`"));
        assertThat(((SQLIdentifierExpr) sqlInsertStatement.getColumns().get(1)).getSimpleName(), is("`field2`"));
        assertThat(sqlInsertStatement.getValuesList().size(), is(1));
        assertThat(sqlInsertStatement.getValuesList().get(0).getValues().size(), is(2));
        assertThat(((SQLIntegerExpr) sqlInsertStatement.getValuesList().get(0).getValues().get(0)).getNumber().intValue(), is(1));
        assertThat(sqlInsertStatement.getValuesList().get(0).getValues().get(1).toString(), is("'value_char'"));
        assertThat(((SQLCharExpr) sqlInsertStatement.getValuesList().get(0).getValues().get(1)).getText(), is("value_char"));
        assertTrue(sqlInsertStatement.getIdentifiersBetweenInsertAndInto().isEmpty());
        assertTrue(sqlInsertStatement.getIdentifiersBetweenTableAndValues().isEmpty());
        assertThat(sqlInsertStatement.getAppendices().size(), is(8));
        assertThat(sqlInsertStatement.getAppendices().get(0), is("RETURNING"));
        assertThat(sqlInsertStatement.getAppendices().get(1), is("field1"));
        assertThat(sqlInsertStatement.getAppendices().get(2), is("*"));
        assertThat(sqlInsertStatement.getAppendices().get(3), is("2"));
        assertThat(sqlInsertStatement.getAppendices().get(4), is("LOG"));
        assertThat(sqlInsertStatement.getAppendices().get(5), is("ERRORS"));
        assertThat(sqlInsertStatement.getAppendices().get(6), is("INTO"));
        assertThat(sqlInsertStatement.getAppendices().get(7), is("TABLE_LOG"));
        assertThat(sqlInsertStatement.toString(), is(
                "INSERT /*+ index(field1) */ INTO TABLE_XXX XXX\n\t(`field1`, `field2`)\nVALUES\n(1, 'value_char') RETURNING field1 * 2 LOG ERRORS INTO TABLE_LOG"));
    }
    
    @Test
    public void parseStatementWithInsertSelect() {
        parseStatementWithInsertSelect("INSERT INTO TABLE_XXX (field1, field2) SELECT field1, field2 FROM TABLE_XXX2 WHERE 1=1 LOG ERRORS INTO TABLE_LOG");
        parseStatementWithInsertSelect("INSERT INTO TABLE_XXX (field1, field2) (SELECT field1, field2 FROM TABLE_XXX2 WHERE 1=1) LOG ERRORS INTO TABLE_LOG");
        parseStatementWithInsertSelect("INSERT INTO TABLE_XXX (field1, field2) (((SELECT field1, field2 FROM TABLE_XXX2 WHERE 1=1))) LOG ERRORS INTO TABLE_LOG");
    }
    
    public void parseStatementWithInsertSelect(final String sql) {
        OracleStatementParser statementParser = new OracleStatementParser(sql);
        AbstractSQLInsertStatement sqlInsertStatement = (AbstractSQLInsertStatement) statementParser.parseStatement();
        assertThat(sqlInsertStatement.getDbType(), is(JdbcConstants.ORACLE));
        assertThat(sqlInsertStatement.getTableSource().getExpr().toString(), is("TABLE_XXX"));
        assertNull(sqlInsertStatement.getTableSource().getAlias());
        assertThat(sqlInsertStatement.getColumns().size(), is(2));
        assertThat(((SQLIdentifierExpr) sqlInsertStatement.getColumns().get(0)).getSimpleName(), is("field1"));
        assertThat(((SQLIdentifierExpr) sqlInsertStatement.getColumns().get(1)).getSimpleName(), is("field2"));
        assertThat(((SQLSelectQueryBlock) sqlInsertStatement.getQuery().getQuery()).getSelectList().size(), is(2));
        assertThat(((SQLSelectQueryBlock) sqlInsertStatement.getQuery().getQuery()).getSelectList().get(0).toString(), is("field1"));
        assertThat(((SQLSelectQueryBlock) sqlInsertStatement.getQuery().getQuery()).getSelectList().get(1).toString(), is("field2"));
        assertThat(((SQLSelectQueryBlock) sqlInsertStatement.getQuery().getQuery()).getFrom().toString(), is("TABLE_XXX2"));
        assertThat(((SQLSelectQueryBlock) sqlInsertStatement.getQuery().getQuery()).getWhere().toString(), is("1 = 1"));
        assertThat(sqlInsertStatement.getQuery().getParent(), is((SQLObject) sqlInsertStatement));
        assertThat(sqlInsertStatement.getAppendices().size(), is(4));
        assertThat(sqlInsertStatement.getAppendices().get(0), is("LOG"));
        assertThat(sqlInsertStatement.getAppendices().get(1), is("ERRORS"));
        assertThat(sqlInsertStatement.getAppendices().get(2), is("INTO"));
        assertThat(sqlInsertStatement.getAppendices().get(3), is("TABLE_LOG"));
        assertThat(sqlInsertStatement.toString(), is("INSERT INTO TABLE_XXX\n\t(field1, field2)\nSELECT field1, field2\nFROM TABLE_XXX2\nWHERE 1 = 1 LOG ERRORS INTO TABLE_LOG"));
    }
}
