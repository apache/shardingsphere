package com.alibaba.druid.sql.dialect.mysql.parser;

import com.alibaba.druid.sql.ast.SQLStatement;
import org.junit.Test;

public final class MySqlStatementParserTest {
    
    @Test
    public void parseStatementWithInsert() {
        MySqlStatementParser statementParser = new MySqlStatementParser("INSERT LOW_PRIORITY IGNORE INTO TABLE_XXX VALUES (1, 'value_char')");
        SQLStatement sqlStatement = statementParser.parseStatement();
        System.out.println(sqlStatement);
    }
}
