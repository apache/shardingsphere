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

package com.alibaba.druid.sql;

import com.alibaba.druid.sql.dialect.mysql.parser.MySqlLexerTest;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParserTest;
import com.alibaba.druid.sql.dialect.postgresql.parser.PGSQLStatementParserTest;
import com.alibaba.druid.sql.dialect.sqlserver.parser.SQLServerStatementParserTest;
import com.alibaba.druid.sql.parser.LexerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        LexerTest.class,
        MySqlLexerTest.class,
        MySqlStatementParserTest.class,
        PGSQLStatementParserTest.class,
        SQLServerStatementParserTest.class
    })
public class AllParserTests {
}
