package com.alibaba.druid.sql.dialect.oracle.parser;

import com.alibaba.druid.sql.ast.SQLCommentHint;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleInsertStatement;
import com.alibaba.druid.util.JdbcConstants;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class OracleInsertStatementParserTest {
    
    @Test(expected = UnsupportedOperationException.class)
    public void parseStatementWithInsertAllValue() {
        OracleStatementParser statementParser = new OracleStatementParser(null, Collections.emptyList(), "INSERT ALL INTO TABLE_XXX (field1) VALUES (field1) SELECT field1 FROM TABLE_XXX2");
        statementParser.parseStatement();
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void parseStatementWithInsertFirst() {
        OracleStatementParser statementParser = new OracleStatementParser(null, Collections.emptyList(), "INSERT FIRST INTO TABLE_XXX (field1) VALUES (field1) SELECT field1 FROM TABLE_XXX2");
        statementParser.parseStatement();
    }
    
    @Test
    public void parseStatementWithInsertValues() {
        OracleStatementParser statementParser = new OracleStatementParser(null, Collections.emptyList(), 
                "INSERT /*+ index(field1) */ INTO TABLE_XXX XXX (`field1`, `field2`) VALUES (1, 'value_char') RETURNING field1*2 LOG ERRORS INTO TABLE_LOG");
        OracleInsertStatement insertStatement = (OracleInsertStatement) statementParser.parseStatement();
        assertThat(insertStatement.getDbType(), is(JdbcConstants.ORACLE));
        assertThat(insertStatement.getHints().size(), is(1));
        assertThat(((SQLCommentHint) insertStatement.getHints().get(0)).getText(), is(" index(field1) "));
        assertThat(insertStatement.getTableSource().getExpr().toString(), is("TABLE_XXX"));
        assertThat(insertStatement.getTableSource().getAlias(), is("XXX"));
        assertThat(insertStatement.getColumns().size(), is(2));
        assertThat(((SQLIdentifierExpr) insertStatement.getColumns().get(0)).getSimpleName(), is("`field1`"));
        assertThat(((SQLIdentifierExpr) insertStatement.getColumns().get(1)).getSimpleName(), is("`field2`"));
        assertThat(insertStatement.getValuesList().size(), is(1));
        assertThat(insertStatement.getValuesList().get(0).getValues().size(), is(2));
        assertThat(((SQLIntegerExpr) insertStatement.getValuesList().get(0).getValues().get(0)).getNumber().intValue(), is(1));
        assertThat(insertStatement.getValuesList().get(0).getValues().get(1).toString(), is("'value_char'"));
        assertThat(((SQLCharExpr) insertStatement.getValuesList().get(0).getValues().get(1)).getText(), is("value_char"));
        assertTrue(insertStatement.getIdentifiersBetweenInsertAndInto().isEmpty());
        assertTrue(insertStatement.getIdentifiersBetweenTableAndValues().isEmpty());
        assertThat(insertStatement.getAppendices().size(), is(8));
        assertThat(insertStatement.getAppendices().get(0), is("RETURNING"));
        assertThat(insertStatement.getAppendices().get(1), is("field1"));
        assertThat(insertStatement.getAppendices().get(2), is("*"));
        assertThat(insertStatement.getAppendices().get(3), is("2"));
        assertThat(insertStatement.getAppendices().get(4), is("LOG"));
        assertThat(insertStatement.getAppendices().get(5), is("ERRORS"));
        assertThat(insertStatement.getAppendices().get(6), is("INTO"));
        assertThat(insertStatement.getAppendices().get(7), is("TABLE_LOG"));
        assertThat(insertStatement.toString(), is(
                "INSERT /*+ index(field1) */ INTO TABLE_XXX XXX\n\t(`field1`, `field2`)\nVALUES\n(1, 'value_char') RETURNING field1 * 2 LOG ERRORS INTO TABLE_LOG"));
    }
    
    @Test
    public void parseStatementWithInsertSelect() {
        parseStatementWithInsertSelect("INSERT INTO TABLE_XXX (field1, field2) SELECT field1, field2 FROM TABLE_XXX2 WHERE 1=1 LOG ERRORS INTO TABLE_LOG");
        parseStatementWithInsertSelect("INSERT INTO TABLE_XXX (field1, field2) (SELECT field1, field2 FROM TABLE_XXX2 WHERE 1=1) LOG ERRORS INTO TABLE_LOG");
        parseStatementWithInsertSelect("INSERT INTO TABLE_XXX (field1, field2) (((SELECT field1, field2 FROM TABLE_XXX2 WHERE 1=1))) LOG ERRORS INTO TABLE_LOG");
    }
    
    public void parseStatementWithInsertSelect(final String sql) {
        OracleStatementParser statementParser = new OracleStatementParser(null, Collections.emptyList(), sql);
        OracleInsertStatement insertStatement = (OracleInsertStatement) statementParser.parseStatement();
        assertThat(insertStatement.getDbType(), is(JdbcConstants.ORACLE));
        assertThat(insertStatement.getTableSource().getExpr().toString(), is("TABLE_XXX"));
        assertNull(insertStatement.getTableSource().getAlias());
        assertThat(insertStatement.getColumns().size(), is(2));
        assertThat(((SQLIdentifierExpr) insertStatement.getColumns().get(0)).getSimpleName(), is("field1"));
        assertThat(((SQLIdentifierExpr) insertStatement.getColumns().get(1)).getSimpleName(), is("field2"));
        assertThat(((SQLSelectQueryBlock) insertStatement.getQuery().getQuery()).getSelectList().size(), is(2));
        assertThat(((SQLSelectQueryBlock) insertStatement.getQuery().getQuery()).getSelectList().get(0).toString(), is("field1"));
        assertThat(((SQLSelectQueryBlock) insertStatement.getQuery().getQuery()).getSelectList().get(1).toString(), is("field2"));
        assertThat(((SQLSelectQueryBlock) insertStatement.getQuery().getQuery()).getFrom().toString(), is("TABLE_XXX2"));
        assertThat(((SQLSelectQueryBlock) insertStatement.getQuery().getQuery()).getWhere().toString(), is("1 = 1"));
        assertThat(insertStatement.getQuery().getParent(), is((SQLObject) insertStatement));
        assertThat(insertStatement.getAppendices().size(), is(4));
        assertThat(insertStatement.getAppendices().get(0), is("LOG"));
        assertThat(insertStatement.getAppendices().get(1), is("ERRORS"));
        assertThat(insertStatement.getAppendices().get(2), is("INTO"));
        assertThat(insertStatement.getAppendices().get(3), is("TABLE_LOG"));
        assertThat(insertStatement.toString(), is("INSERT INTO TABLE_XXX\n\t(field1, field2)\nSELECT field1, field2\nFROM TABLE_XXX2\nWHERE 1 = 1 LOG ERRORS INTO TABLE_LOG"));
    }
}
