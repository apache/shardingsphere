package com.alibaba.druid.sql.dialect.mysql.parser;

import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.util.JdbcConstants;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class MySqlStatementParserTest {
    
    @Test
    public void parseStatementWithInsert() {
        MySqlStatementParser statementParser = new MySqlStatementParser("INSERT LOW_PRIORITY IGNORE INTO TABLE_XXX PARTITION (partition1,partition2) VALUES (1, 'value_char')");
        SQLInsertStatement sqlInsertStatement = (SQLInsertStatement) statementParser.parseStatement();
        assertThat(sqlInsertStatement.getDbType(), is(JdbcConstants.MYSQL));
        assertThat(sqlInsertStatement.getTableName().getSimpleName(), is("TABLE_XXX"));
        assertNull(sqlInsertStatement.getTableSource().getAlias());
        assertNull(sqlInsertStatement.getAlias());
        assertThat(sqlInsertStatement.getValues().getValues().size(), is(2));
        assertThat(((SQLIntegerExpr) sqlInsertStatement.getValues().getValues().get(0)).getNumber().intValue(), is(1));
        assertThat(sqlInsertStatement.getValues().getValues().get(1).toString(), is("'value_char'"));
        assertThat(((SQLCharExpr) sqlInsertStatement.getValues().getValues().get(1)).getText(), is("value_char"));
        assertThat(sqlInsertStatement.getIdentifiersBetweenInsertAndInto().size(), is(2));
        assertThat(sqlInsertStatement.getIdentifiersBetweenInsertAndInto().get(0), is("LOW_PRIORITY"));
        assertThat(sqlInsertStatement.getIdentifiersBetweenInsertAndInto().get(1), is("IGNORE"));
        assertThat(sqlInsertStatement.getIdentifiersBetweenTableAndValues().size(), is(6));
        assertThat(sqlInsertStatement.getIdentifiersBetweenTableAndValues().get(0), is("PARTITION"));
        assertThat(sqlInsertStatement.getIdentifiersBetweenTableAndValues().get(1), is("("));
        assertThat(sqlInsertStatement.getIdentifiersBetweenTableAndValues().get(2), is("partition1"));
        assertThat(sqlInsertStatement.getIdentifiersBetweenTableAndValues().get(3), is(","));
        assertThat(sqlInsertStatement.getIdentifiersBetweenTableAndValues().get(4), is("partition2"));
        assertThat(sqlInsertStatement.getIdentifiersBetweenTableAndValues().get(5), is(")"));
        assertThat(sqlInsertStatement.toString(), is("INSERT LOW_PRIORITY IGNORE INTO TABLE_XXX PARTITION ( partition1 , partition2 )\nVALUES (1, 'value_char')"));
    }
}
