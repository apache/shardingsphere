package com.alibaba.druid.sql.dialect.oracle.parser;

import org.junit.Test;

public class OracleStatementParserTest {
    
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
}
