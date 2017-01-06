package com.alibaba.druid.sql.dialect.mysql.parser;

import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.util.JdbcConstants;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class MySqlInsertStatementParserTest {
    
    @Test
    public void parseStatementWithInsertValue() {
        MySqlStatementParser statementParser = new MySqlStatementParser("INSERT INTO TABLE_XXX VALUE (1, 'value_char') ");
        MySqlInsertStatement insertStatement = (MySqlInsertStatement) statementParser.parseStatement();
        assertThat(insertStatement.getDbType(), is(JdbcConstants.MYSQL));
        assertThat(insertStatement.getTableSource().getExpr().toString(), is("TABLE_XXX"));
        assertNull(insertStatement.getTableSource().getAlias());
        assertTrue(insertStatement.getColumns().isEmpty());
        assertThat(insertStatement.getValuesList().size(), is(1));
        assertThat(insertStatement.getValuesList().get(0).getValues().size(), is(2));
        assertThat(((SQLIntegerExpr) insertStatement.getValuesList().get(0).getValues().get(0)).getNumber().intValue(), is(1));
        assertThat(insertStatement.getValuesList().get(0).getValues().get(1).toString(), is("'value_char'"));
        assertThat(((SQLCharExpr) insertStatement.getValuesList().get(0).getValues().get(1)).getText(), is("value_char"));
        assertTrue(insertStatement.getIdentifiersBetweenInsertAndInto().isEmpty());
        assertTrue(insertStatement.getIdentifiersBetweenTableAndValues().isEmpty());
        assertThat(insertStatement.toString(), is("INSERT INTO TABLE_XXX\nVALUES (1, 'value_char')"));
    }
    
    @Test
    public void parseStatementWithInsertValues() {
        MySqlStatementParser statementParser = new MySqlStatementParser("INSERT LOW_PRIORITY IGNORE INTO TABLE_XXX PARTITION (partition1,partition2) (`field1`, `field2`) VALUES (1, 'value_char')");
        MySqlInsertStatement insertStatement = (MySqlInsertStatement) statementParser.parseStatement();
        assertThat(insertStatement.getDbType(), is(JdbcConstants.MYSQL));
        assertThat(insertStatement.getTableSource().getExpr().toString(), is("TABLE_XXX"));
        assertNull(insertStatement.getTableSource().getAlias());
        assertThat(insertStatement.getColumns().size(), is(2));
        assertThat(((SQLIdentifierExpr) insertStatement.getColumns().get(0)).getSimpleName(), is("`field1`"));
        assertThat(((SQLIdentifierExpr) insertStatement.getColumns().get(1)).getSimpleName(), is("`field2`"));
        assertThat(insertStatement.getValuesList().size(), is(1));
        assertThat(insertStatement.getValuesList().get(0).getValues().size(), is(2));
        assertThat(((SQLIntegerExpr) insertStatement.getValuesList().get(0).getValues().get(0)).getNumber().intValue(), is(1));
        assertThat(insertStatement.getValuesList().get(0).getValues().get(1).toString(), is("'value_char'"));
        assertThat(((SQLCharExpr) insertStatement.getValuesList().get(0).getValues().get(1)).getText(), is("value_char"));
        assertThat(insertStatement.getPartitionNames().size(), is(2));
        assertThat(insertStatement.getPartitionNames().get(0), is("partition1"));
        assertThat(insertStatement.getPartitionNames().get(1), is("partition2"));
        assertThat(insertStatement.getIdentifiersBetweenInsertAndInto().size(), is(2));
        assertThat(insertStatement.getIdentifiersBetweenInsertAndInto().get(0), is("LOW_PRIORITY"));
        assertThat(insertStatement.getIdentifiersBetweenInsertAndInto().get(1), is("IGNORE"));
        assertTrue(insertStatement.getIdentifiersBetweenTableAndValues().isEmpty());
        assertThat(insertStatement.toString(), is("INSERT LOW_PRIORITY IGNORE INTO TABLE_XXX PARTITION (partition1,partition2) (`field1`, `field2`)\nVALUES (1, 'value_char')"));
    }
    
    @Test
    public void parseStatementWithInsertBatchValues() {
        MySqlStatementParser statementParser = new MySqlStatementParser("INSERT INTO TABLE_XXX VALUE (1, 'char1'), (2, 'char2')");
        MySqlInsertStatement insertStatement = (MySqlInsertStatement) statementParser.parseStatement();
        assertThat(insertStatement.getDbType(), is(JdbcConstants.MYSQL));
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
        assertThat(insertStatement.toString(), is("INSERT INTO TABLE_XXX\nVALUES (1, 'char1'),\n\t(2, 'char2')"));
    }
    
    @Test
    public void parseStatementWithInsertSelect() {
        parseStatementWithInsertSelect("INSERT INTO TABLE_XXX (field1, field2) SELECT field1, field2 FROM TABLE_XXX2 ON DUPLICATE KEY UPDATE field1=field1+1");
        parseStatementWithInsertSelect("INSERT INTO TABLE_XXX (field1, field2) (SELECT field1, field2 FROM TABLE_XXX2) ON DUPLICATE KEY UPDATE field1=field1+1");
        parseStatementWithInsertSelect("INSERT INTO TABLE_XXX (field1, field2) (((SELECT field1, field2 FROM TABLE_XXX2))) ON DUPLICATE KEY UPDATE field1=field1+1");
    }
    
    public void parseStatementWithInsertSelect(final String sql) {
        MySqlStatementParser statementParser = new MySqlStatementParser(sql);
        MySqlInsertStatement insertStatement = (MySqlInsertStatement) statementParser.parseStatement();
        assertThat(insertStatement.getDbType(), is(JdbcConstants.MYSQL));
        assertThat(insertStatement.getTableSource().getExpr().toString(), is("TABLE_XXX"));
        assertNull(insertStatement.getTableSource().getAlias());
        assertThat(insertStatement.getColumns().size(), is(2));
        assertThat(((SQLIdentifierExpr) insertStatement.getColumns().get(0)).getSimpleName(), is("field1"));
        assertThat(((SQLIdentifierExpr) insertStatement.getColumns().get(1)).getSimpleName(), is("field2"));
        assertThat(((SQLSelectQueryBlock) insertStatement.getQuery().getQuery()).getSelectList().size(), is(2));
        assertThat(((SQLSelectQueryBlock) insertStatement.getQuery().getQuery()).getSelectList().get(0).toString(), is("field1"));
        assertThat(((SQLSelectQueryBlock) insertStatement.getQuery().getQuery()).getSelectList().get(1).toString(), is("field2"));
        assertThat(((SQLSelectQueryBlock) insertStatement.getQuery().getQuery()).getFrom().toString(), is("TABLE_XXX2"));
        assertThat(insertStatement.getQuery().getParent(), is((SQLObject) insertStatement));
        assertThat(insertStatement.getAppendices().size(), is(9));
        assertThat(insertStatement.getAppendices().get(0), is("ON"));
        assertThat(insertStatement.getAppendices().get(1), is("DUPLICATE"));
        assertThat(insertStatement.getAppendices().get(2), is("KEY"));
        assertThat(insertStatement.getAppendices().get(3), is("UPDATE"));
        assertThat(insertStatement.getAppendices().get(4), is("field1"));
        assertThat(insertStatement.getAppendices().get(5), is("="));
        assertThat(insertStatement.getAppendices().get(6), is("field1"));
        assertThat(insertStatement.getAppendices().get(7), is("+"));
        assertThat(insertStatement.getAppendices().get(8), is("1"));
        assertThat(insertStatement.toString(), is("INSERT INTO TABLE_XXX (field1, field2)\nSELECT field1, field2\nFROM TABLE_XXX2 ON DUPLICATE KEY UPDATE field1 = field1 + 1"));
    }
    
    @Test
    public void parseStatementWithInsertSet() {
        MySqlStatementParser statementParser = new MySqlStatementParser("INSERT INTO TABLE_XXX SET field1=1, field2='char'");
        MySqlInsertStatement insertStatement = (MySqlInsertStatement) statementParser.parseStatement();
        assertThat(insertStatement.getDbType(), is(JdbcConstants.MYSQL));
        assertThat(insertStatement.getTableSource().getExpr().toString(), is("TABLE_XXX"));
        assertNull(insertStatement.getTableSource().getAlias());
        assertThat(insertStatement.getColumns().size(), is(2));
        assertThat(((SQLIdentifierExpr) insertStatement.getColumns().get(0)).getSimpleName(), is("field1"));
        assertThat(((SQLIdentifierExpr) insertStatement.getColumns().get(1)).getSimpleName(), is("field2"));
        assertThat(insertStatement.getValuesList().size(), is(1));
        assertThat(insertStatement.getValuesList().get(0).getValues().size(), is(2));
        assertThat(((SQLIntegerExpr) insertStatement.getValuesList().get(0).getValues().get(0)).getNumber().intValue(), is(1));
        assertThat(((SQLCharExpr) insertStatement.getValuesList().get(0).getValues().get(1)).getText(), is("char"));
        assertTrue(insertStatement.getIdentifiersBetweenInsertAndInto().isEmpty());
        assertTrue(insertStatement.getIdentifiersBetweenTableAndValues().isEmpty());
        assertThat(insertStatement.toString(), is("INSERT INTO TABLE_XXX (field1, field2)\nVALUES (1, 'char')"));
    }
}
