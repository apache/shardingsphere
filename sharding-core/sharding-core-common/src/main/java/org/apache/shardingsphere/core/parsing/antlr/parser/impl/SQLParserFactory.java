/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.parsing.antlr.parser.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.parsing.antlr.autogen.MySQLStatementLexer;
import org.apache.shardingsphere.core.parsing.antlr.autogen.OracleStatementLexer;
import org.apache.shardingsphere.core.parsing.antlr.autogen.PostgreSQLStatementLexer;
import org.apache.shardingsphere.core.parsing.antlr.autogen.SQLServerStatementLexer;
import org.apache.shardingsphere.core.parsing.antlr.parser.impl.dialect.MySQLParser;
import org.apache.shardingsphere.core.parsing.antlr.parser.impl.dialect.OracleParser;
import org.apache.shardingsphere.core.parsing.antlr.parser.impl.dialect.PostgreSQLParser;
import org.apache.shardingsphere.core.parsing.antlr.parser.impl.dialect.SQLServerParser;

/**
 * SQL parser factory.
 * 
 * @author duhongjun
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLParserFactory {
    
    /** 
     * New instance of SQL parser.
     * 
     * @param databaseType database type
     * @param sql SQL
     * @return SQL parser
     */
    public static SQLParser newInstance(final DatabaseType databaseType, final String sql) {
        return createSQLParser(databaseType, createLexer(databaseType, sql));
    }
    
    private static Lexer createLexer(final DatabaseType databaseType, final String sql) {
        CharStream sqlCharStream = CharStreams.fromString(sql);
        switch (databaseType) {
            case H2:
            case MySQL:
                return new MySQLStatementLexer(sqlCharStream);
            case PostgreSQL:
                return new PostgreSQLStatementLexer(sqlCharStream);
            case SQLServer:
                return new SQLServerStatementLexer(sqlCharStream);
            case Oracle:
                return new OracleStatementLexer(sqlCharStream);
            default:
                throw new UnsupportedOperationException(String.format("Can not support database type [%s].", databaseType));
        }
    }
    
    private static SQLParser createSQLParser(final DatabaseType databaseType, final Lexer lexer) {
        TokenStream tokenStream = new CommonTokenStream(lexer);
        switch (databaseType) {
            case H2:
            case MySQL:
                return new MySQLParser(tokenStream);
            case PostgreSQL:
                return new PostgreSQLParser(tokenStream);
            case SQLServer:
                return new SQLServerParser(tokenStream);
            case Oracle:
                return new OracleParser(tokenStream);
            default:
                throw new UnsupportedOperationException(String.format("Can not support database type [%s].", databaseType));
        }
    }
}
