/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.parsing;

import io.shardingjdbc.core.parsing.parser.exception.SQLParsingException;
import io.shardingjdbc.core.parsing.parser.sql.dml.DMLStatement;
import io.shardingjdbc.core.parsing.parser.sql.dql.DQLStatement;
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
