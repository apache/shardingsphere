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

package io.shardingsphere.core.parsing.parser.dialect.postgresql.clause.expression;

import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.dialect.postgresql.PostgreSQLKeyword;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.TokenType;
import io.shardingsphere.core.parsing.parser.clause.expression.AliasExpressionParser;

/**
 * Alias clause parser for PostgreSQL.
 *
 * @author zhangliang
 */
public final class PostgreSQLAliasExpressionParser extends AliasExpressionParser {
    
    public PostgreSQLAliasExpressionParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected TokenType[] getCustomizedAvailableKeywordsForSelectItemAlias() {
        return new TokenType[] {
            DefaultKeyword.WHILE, DefaultKeyword.FULLTEXT, DefaultKeyword.MODIFY, DefaultKeyword.IDENTIFIED, DefaultKeyword.USE, DefaultKeyword.LEAVE, DefaultKeyword.ITERATE, 
            DefaultKeyword.REPEAT, DefaultKeyword.OPEN, DefaultKeyword.LOOP, DefaultKeyword.VARCHAR2, DefaultKeyword.DATE, DefaultKeyword.BLOB, DefaultKeyword.XOR, DefaultKeyword.CONVERT,
            PostgreSQLKeyword.PLAIN, PostgreSQLKeyword.EXTENDED, PostgreSQLKeyword.MAIN,
        };
    }
    
    @Override
    protected TokenType[] getCustomizedAvailableKeywordsForTableAlias() {
        return new TokenType[] {
            DefaultKeyword.TRIGGER, DefaultKeyword.PROCEDURE, DefaultKeyword.FUNCTION, DefaultKeyword.CURSOR, DefaultKeyword.BY, DefaultKeyword.COMMENT, 
            DefaultKeyword.REPLACE, DefaultKeyword.BEFORE, DefaultKeyword.EACH, DefaultKeyword.ROW, DefaultKeyword.EXECUTE, DefaultKeyword.FULLTEXT, DefaultKeyword.ALTER, 
            DefaultKeyword.MODIFY, DefaultKeyword.IDENTIFIED, DefaultKeyword.USE, DefaultKeyword.DECLARE, DefaultKeyword.REVOKE, DefaultKeyword.CLOSE, DefaultKeyword.ESCAPE, 
            DefaultKeyword.LOCK, DefaultKeyword.LEAVE, DefaultKeyword.ITERATE, DefaultKeyword.REPEAT, DefaultKeyword.OPEN, DefaultKeyword.OUT, DefaultKeyword.INOUT, 
            DefaultKeyword.OVER, DefaultKeyword.LOOP, DefaultKeyword.EXPLAIN, DefaultKeyword.COALESCE, DefaultKeyword.CHAR, DefaultKeyword.CHARACTER, DefaultKeyword.VARYING, 
            DefaultKeyword.VARCHAR, DefaultKeyword.VARCHAR2, DefaultKeyword.INTEGER, DefaultKeyword.INT, DefaultKeyword.SMALLINT, DefaultKeyword.DECIMAL, DefaultKeyword.DEC, 
            DefaultKeyword.NUMERIC, DefaultKeyword.FLOAT, DefaultKeyword.REAL, DefaultKeyword.DOUBLE, DefaultKeyword.PRECISION, DefaultKeyword.DATE, DefaultKeyword.INTERVAL, 
            DefaultKeyword.BLOB, DefaultKeyword.XOR, DefaultKeyword.BETWEEN, DefaultKeyword.EXISTS, DefaultKeyword.CONVERT, DefaultKeyword.KEY,
            PostgreSQLKeyword.SHOW, PostgreSQLKeyword.FIRST, PostgreSQLKeyword.NEXT, PostgreSQLKeyword.LAST, PostgreSQLKeyword.RESTART, PostgreSQLKeyword.RECURSIVE, PostgreSQLKeyword.CURRENT, 
            PostgreSQLKeyword.NOWAIT, PostgreSQLKeyword.TYPE, PostgreSQLKeyword.UNLOGGED, PostgreSQLKeyword.CONTINUE, PostgreSQLKeyword.ROWS, PostgreSQLKeyword.SHARE, 
            PostgreSQLKeyword.IDENTITY, PostgreSQLKeyword.STATISTICS, PostgreSQLKeyword.PLAIN, PostgreSQLKeyword.EXTERNAL, PostgreSQLKeyword.EXTENDED, PostgreSQLKeyword.MAIN, 
            PostgreSQLKeyword.VALID, PostgreSQLKeyword.ALWAYS, PostgreSQLKeyword.RULE, PostgreSQLKeyword.OIDS, PostgreSQLKeyword.INHERIT, 
            PostgreSQLKeyword.OWNER, PostgreSQLKeyword.DEFERRED, PostgreSQLKeyword.IMMEDIATE, PostgreSQLKeyword.EXTRACT,
        };
    }
}
