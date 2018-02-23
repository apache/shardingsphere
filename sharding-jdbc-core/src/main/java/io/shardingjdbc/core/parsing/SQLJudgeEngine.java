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

package io.shardingjdbc.core.parsing;

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.LexerEngineFactory;
import io.shardingjdbc.core.parsing.lexer.dialect.mysql.MySQLKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Assist;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Keyword;
import io.shardingjdbc.core.parsing.lexer.token.TokenType;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.statement.DescStatement;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.statement.ShowStatement;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.statement.ShowType;
import io.shardingjdbc.core.parsing.parser.exception.SQLParsingException;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.parsing.parser.sql.ddl.DDLStatement;
import io.shardingjdbc.core.parsing.parser.sql.dml.DMLStatement;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingjdbc.core.parsing.parser.sql.ignore.IgnoreStatement;
import io.shardingjdbc.core.parsing.parser.sql.tcl.TCLStatement;
import lombok.RequiredArgsConstructor;

/**
 * SQL judge engine.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SQLJudgeEngine {
    
    private final String sql;
    
    /**
     * judge SQL Type only.
     *
     * @return SQL statement
     */
    public SQLStatement judge() {
        LexerEngine lexerEngine = LexerEngineFactory.newInstance(DatabaseType.MySQL, sql);
        lexerEngine.nextToken();
        while (true) {
            TokenType tokenType = lexerEngine.getCurrentToken().getType();
            if (tokenType instanceof Keyword) {
                if (DefaultKeyword.SELECT == tokenType) {
                    return new SelectStatement();
                }
                if (DefaultKeyword.INSERT == tokenType || DefaultKeyword.UPDATE == tokenType || DefaultKeyword.DELETE == tokenType) {
                    return new DMLStatement();
                }
                if (DefaultKeyword.CREATE == tokenType || DefaultKeyword.ALTER == tokenType || DefaultKeyword.DROP == tokenType || DefaultKeyword.TRUNCATE == tokenType) {
                    return new DDLStatement();
                }
                if (DefaultKeyword.SET == tokenType || DefaultKeyword.COMMIT == tokenType || DefaultKeyword.ROLLBACK == tokenType 
                        || DefaultKeyword.SAVEPOINT == tokenType || DefaultKeyword.BEGIN == tokenType) {
                    return new TCLStatement();
                }
                if (DefaultKeyword.USE == tokenType) {
                    return new IgnoreStatement();
                }
                if (DefaultKeyword.DESC == tokenType) {
                    return new DescStatement();
                }
                if (MySQLKeyword.SHOW == tokenType) {
                    return new ShowStatement(ShowType.OTHER);
                }
            }
            if (tokenType instanceof Assist && Assist.END == tokenType) {
                throw new SQLParsingException("Unsupported SQL statement: [%s]", sql);
            }
            lexerEngine.nextToken();
        }
    }
}
