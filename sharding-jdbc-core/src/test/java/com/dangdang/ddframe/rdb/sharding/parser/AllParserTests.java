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

package com.dangdang.ddframe.rdb.sharding.parser;

import com.dangdang.ddframe.rdb.sharding.parser.mysql.MySQLPreparedStatementForOneParameterTest;
import com.dangdang.ddframe.rdb.sharding.parser.mysql.MySQLPreparedStatementForTowParametersTest;
import com.dangdang.ddframe.rdb.sharding.parser.mysql.MySQLStatementTest;
import com.dangdang.ddframe.rdb.sharding.parser.mysql.OrParseTest;
import com.dangdang.ddframe.rdb.sharding.parser.result.SQLParsedResultTest;
import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.mysql.lexer.MySQLLexerTest;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.LexerTest;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.DeleteStatementParserTest;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.InsertStatementParserTest;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.UpdateStatementParserTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        LexerTest.class,
        MySQLLexerTest.class,
        InsertStatementParserTest.class,
        UpdateStatementParserTest.class,
        DeleteStatementParserTest.class,
        SQLParsedResultTest.class, 
        MySQLStatementTest.class, 
        MySQLPreparedStatementForOneParameterTest.class, 
        MySQLPreparedStatementForTowParametersTest.class,  
        OrParseTest.class, 
        UnsupportedParseTest.class
    })
public class AllParserTests {
}
