package com.dangdang.ddframe.rdb.sharding.parsing;

import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.DeleteSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.InsertSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SelectSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.UpdateSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class SQLParsingEngineTest {
    
    @Test
    public void assertPrepareParseForSelect() {
        assertThat(new SQLJudgeEngine(" /*COMMENT*/  \t \n  \r \fsElecT\t\n  * from table  ").judge(), instanceOf(SelectSQLContext.class));
    }
    
    @Test
    public void assertPrepareParseForInsert() {
        assertThat(new SQLJudgeEngine(" - - COMMENT  \t \n  \r \fInsert\t\n  into table  ").judge(), instanceOf(InsertSQLContext.class));
    }
    
    @Test
    public void assertPrepareParseForUpdate() {
        assertThat(new SQLJudgeEngine(" /*+ HINT SELECT * FROM TT*/  \t \n  \r \fuPdAte\t\n  table  ").judge(), instanceOf(UpdateSQLContext.class));
    }
    
    @Test
    public void assertPrepareParseForDelete() {
        assertThat(new SQLJudgeEngine(" /*+ HINT SELECT * FROM TT*/  \t \n  \r \fdelete\t\n  table  ").judge(), instanceOf(DeleteSQLContext.class));
    }
    
    @Test(expected = SQLParsingException.class)
    public void assertPrepareParseForInvalidSQL() {
        new SQLJudgeEngine("int i = 0").judge();
    }
}
