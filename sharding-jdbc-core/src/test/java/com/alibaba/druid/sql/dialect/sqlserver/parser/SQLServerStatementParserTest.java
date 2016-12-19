package com.alibaba.druid.sql.dialect.sqlserver.parser;

import com.alibaba.druid.sql.ast.SQLStatement;
import org.junit.Test;

public class SQLServerStatementParserTest {
    
    @Test
    public void parseStatementWithInsert() {
        SQLServerStatementParser statementParser = new SQLServerStatementParser("INSERT TOP(10) INTO OUTPUT TABLE_XXX VALUES (1, 'value_char')");
        SQLStatement sqlStatement = statementParser.parseStatement();
        System.out.println(sqlStatement);
    }
}