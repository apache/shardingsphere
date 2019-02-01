/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.core.parsing.lexer;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.lexer.dialect.h2.H2Lexer;
import io.shardingsphere.core.parsing.lexer.dialect.mysql.MySQLLexer;
import io.shardingsphere.core.parsing.lexer.dialect.oracle.OracleLexer;
import io.shardingsphere.core.parsing.lexer.dialect.postgresql.PostgreSQLLexer;
import io.shardingsphere.core.parsing.lexer.dialect.sqlserver.SQLServerLexer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Lexical analysis factory.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LexerEngineFactory {
    
    /**
     * Create lexical analysis engine instance.
     * 
     * @param dbType database type
     * @param sql SQL
     * @return lexical analysis engine instance
     */
    public static LexerEngine newInstance(final DatabaseType dbType, final String sql) {
        switch (dbType) {
            case H2:
                return new LexerEngine(new H2Lexer(sql));
            case MySQL:
                return new LexerEngine(new MySQLLexer(sql));
            case Oracle:
                return new LexerEngine(new OracleLexer(sql));
            case SQLServer:
                return new LexerEngine(new SQLServerLexer(sql));
            case PostgreSQL:
                return new LexerEngine(new PostgreSQLLexer(sql));
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database [%s].", dbType));
        }
    }
}
