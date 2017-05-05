/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.util;

import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SQLUtilTest {
    
    @Test
    public void testGetExactlyValue() throws Exception {
        assertThat(SQLUtil.getExactlyValue("`xxx`"), is("xxx"));
        assertThat(SQLUtil.getExactlyValue("[xxx]"), is("xxx"));
        assertThat(SQLUtil.getExactlyValue("\"xxx\""), is("xxx"));
        assertThat(SQLUtil.getExactlyValue("'xxx'"), is("xxx"));
    }
    
    @Test
    public void assertSQLType() {
        assertThat(SQLUtil.getTypeByStart(" /*COMMENT*/  \t \n  \r \fsElecT\t\n  * from table  "), is(SQLType.SELECT));
        assertThat(SQLUtil.getTypeByStart(" - - COMMENT  \t \n  \r \fInsert\t\n  into table  "), is(SQLType.INSERT));
        assertThat(SQLUtil.getTypeByStart(" /*+ HINT SELECT * FROM TT*/  \t \n  \r \fuPdAte\t\n  table  "), is(SQLType.UPDATE));
        assertThat(SQLUtil.getTypeByStart(" /*+ HINT SELECT * FROM TT*/  \t \n  \r \fdelete\t\n  table  "), is(SQLType.DELETE));
    }
    
    @Test(expected = SQLParsingException.class)
    public void assertNoSQL() {
        SQLUtil.getTypeByStart("int i = 0");
    }
}
