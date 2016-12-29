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

import com.alibaba.druid.sql.dialect.mysql.parser.MySqlDeleteStatementParserTest;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlInsertStatementParserTest;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlLexerTest;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlUpdateStatementParserTest;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleInsertStatementParserTest;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleUpdateStatementParserTest;
import com.alibaba.druid.sql.dialect.postgresql.parser.PGSQLInsertStatementParserTest;
import com.alibaba.druid.sql.dialect.postgresql.parser.PGSQLUpdateStatementParserTest;
import com.alibaba.druid.sql.dialect.sqlserver.parser.SQLServerInsertStatementParserTest;
import com.alibaba.druid.sql.dialect.sqlserver.parser.SQLServerUpdateStatementParserTest;
import com.alibaba.druid.sql.parser.LexerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        LexerTest.class, 
        MySqlLexerTest.class, 
        MySqlInsertStatementParserTest.class, 
        MySqlUpdateStatementParserTest.class,
        MySqlDeleteStatementParserTest.class, 
        PGSQLInsertStatementParserTest.class,
        PGSQLUpdateStatementParserTest.class, 
        OracleInsertStatementParserTest.class,
        OracleUpdateStatementParserTest.class, 
        SQLServerInsertStatementParserTest.class,
        SQLServerUpdateStatementParserTest.class
    })
public class AllParserTests {
}
