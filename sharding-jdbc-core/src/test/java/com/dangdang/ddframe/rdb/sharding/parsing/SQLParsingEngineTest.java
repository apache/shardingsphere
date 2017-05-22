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
    public void assertGetStatementTypeForSelect() {
        assertThat(new SQLParsingEngine(DatabaseType.MySQL, " /*COMMENT*/  \t \n  \r \fsElecT\t\n  * from table  ", null).getStatementType(), instanceOf(SelectSQLContext.class));
    }
    
    @Test
    public void assertGetStatementTypeForInsert() {
        assertThat(new SQLParsingEngine(DatabaseType.MySQL, " - - COMMENT  \t \n  \r \fInsert\t\n  into table  ", null).getStatementType(), instanceOf(InsertSQLContext.class));
    }
    
    @Test
    public void assertGetStatementTypeForUpdate() {
        assertThat(new SQLParsingEngine(DatabaseType.MySQL, " /*+ HINT SELECT * FROM TT*/  \t \n  \r \fuPdAte\t\n  table  ", null).getStatementType(), instanceOf(UpdateSQLContext.class));
    }
    
    @Test
    public void assertGetStatementTypeForDelete() {
        assertThat(new SQLParsingEngine(DatabaseType.MySQL, " /*+ HINT SELECT * FROM TT*/  \t \n  \r \fdelete\t\n  table  ", null).getStatementType(), instanceOf(DeleteSQLContext.class));
    }
    
    @Test(expected = SQLParsingException.class)
    public void assertGetStatementTypeForInvalidSQL() {
        new SQLParsingEngine(DatabaseType.MySQL, "int i = 0", null).getStatementType();
    }
}
