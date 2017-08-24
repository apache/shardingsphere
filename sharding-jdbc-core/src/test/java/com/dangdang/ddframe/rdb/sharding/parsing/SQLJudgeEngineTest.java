package com.dangdang.ddframe.rdb.sharding.parsing;

import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.dml.DMLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.dql.DQLStatement;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class SQLJudgeEngineTest {
    
    @Test
    public void assertPrepareParseForSelect() {
        assertThat(new SQLJudgeEngine(" /*COMMENT*/  \t \n  \r \fsElecT\t\n  * from table  ").judge(), instanceOf(DQLStatement.class));
    }
    
    @Test
    public void assertPrepareParseForInsert() {
        assertThat(new SQLJudgeEngine(" - - COMMENT  \t \n  \r \fInsert\t\n  into table  ").judge(), instanceOf(DMLStatement.class));
    }
    
    @Test
    public void assertPrepareParseForUpdate() {
        assertThat(new SQLJudgeEngine(" /*+ HINT SELECT * FROM TT*/  \t \n  \r \fuPdAte\t\n  table  ").judge(), instanceOf(DMLStatement.class));
    }
    
    @Test
    public void assertPrepareParseForDelete() {
        assertThat(new SQLJudgeEngine(" /*+ HINT SELECT * FROM TT*/  \t \n  \r \fdelete\t\n  table  ").judge(), instanceOf(DMLStatement.class));
    }
    
    @Test(expected = SQLParsingException.class)
    public void assertPrepareParseForInvalidSQL() {
        new SQLJudgeEngine("int i = 0").judge();
    }
}
