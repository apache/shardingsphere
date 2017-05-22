package com.dangdang.ddframe.rdb.sharding.parsing;

import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
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
        assertThat(new SQLParsingEngine(DatabaseType.MySQL, " /*COMMENT*/  \t \n  \r \fsElecT\t\n  * from table  ", null).prepareParse(), instanceOf(SelectSQLContext.class));
    }
    
    @Test
    public void assertPrepareParseForInsert() {
        assertThat(new SQLParsingEngine(DatabaseType.MySQL, " - - COMMENT  \t \n  \r \fInsert\t\n  into table  ", null).prepareParse(), instanceOf(InsertSQLContext.class));
    }
    
    @Test
    public void assertPrepareParseForUpdate() {
        assertThat(new SQLParsingEngine(DatabaseType.MySQL, " /*+ HINT SELECT * FROM TT*/  \t \n  \r \fuPdAte\t\n  table  ", null).prepareParse(), instanceOf(UpdateSQLContext.class));
    }
    
    @Test
    public void assertPrepareParseForDelete() {
        assertThat(new SQLParsingEngine(DatabaseType.MySQL, " /*+ HINT SELECT * FROM TT*/  \t \n  \r \fdelete\t\n  table  ", null).prepareParse(), instanceOf(DeleteSQLContext.class));
    }
    
    @Test(expected = SQLParsingException.class)
    public void assertPrepareParseForInvalidSQL() {
        new SQLParsingEngine(DatabaseType.MySQL, "int i = 0", null).prepareParse();
    }
}
