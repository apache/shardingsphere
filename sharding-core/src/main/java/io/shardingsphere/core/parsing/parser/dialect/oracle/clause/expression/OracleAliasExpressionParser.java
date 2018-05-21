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

package io.shardingsphere.core.parsing.parser.dialect.oracle.clause.expression;

import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.dialect.oracle.OracleKeyword;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.TokenType;
import io.shardingsphere.core.parsing.parser.clause.expression.AliasExpressionParser;

/**
 * Alias clause parser for Oracle.
 *
 * @author zhangliang
 */
public final class OracleAliasExpressionParser extends AliasExpressionParser {
    
    public OracleAliasExpressionParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected TokenType[] getCustomizedAvailableKeywordsForSelectItemAlias() {
        return new TokenType[]{
            DefaultKeyword.SCHEMA, DefaultKeyword.DATABASE, DefaultKeyword.PROCEDURE, DefaultKeyword.TABLESPACE, DefaultKeyword.FUNCTION, DefaultKeyword.SEQUENCE, DefaultKeyword.CURSOR, 
            DefaultKeyword.IF, DefaultKeyword.WHILE, DefaultKeyword.DO, DefaultKeyword.NO, DefaultKeyword.WITHOUT, DefaultKeyword.TRUE, DefaultKeyword.FALSE, DefaultKeyword.TEMPORARY, 
            DefaultKeyword.TEMP, DefaultKeyword.REPLACE, DefaultKeyword.BEFORE, DefaultKeyword.AFTER, DefaultKeyword.INSTEAD, DefaultKeyword.EACH, DefaultKeyword.STATEMENT, 
            DefaultKeyword.EXECUTE, DefaultKeyword.FULLTEXT, DefaultKeyword.ENABLE, DefaultKeyword.DISABLE, DefaultKeyword.TRUNCATE, DefaultKeyword.CASCADE, DefaultKeyword.AS, 
            DefaultKeyword.CASE, DefaultKeyword.WHEN, DefaultKeyword.END, DefaultKeyword.LEFT, DefaultKeyword.RIGHT, DefaultKeyword.FULL, DefaultKeyword.INNER, DefaultKeyword.OUTER, 
            DefaultKeyword.CROSS, DefaultKeyword.JOIN, DefaultKeyword.USE, DefaultKeyword.USING, DefaultKeyword.NATURAL, DefaultKeyword.DECLARE, DefaultKeyword.FETCH, DefaultKeyword.CLOSE, 
            DefaultKeyword.CAST, DefaultKeyword.ESCAPE, DefaultKeyword.SOME, DefaultKeyword.LEAVE, DefaultKeyword.ITERATE, DefaultKeyword.REPEAT, DefaultKeyword.UNTIL, DefaultKeyword.OPEN, 
            DefaultKeyword.OUT, DefaultKeyword.INOUT, DefaultKeyword.OVER, DefaultKeyword.LOOP, DefaultKeyword.EXPLAIN, DefaultKeyword.PASSWORD, DefaultKeyword.LOCAL, DefaultKeyword.GLOBAL, 
            DefaultKeyword.STORAGE, DefaultKeyword.DATA, DefaultKeyword.COALESCE, DefaultKeyword.CHARACTER, DefaultKeyword.VARYING, DefaultKeyword.INT, DefaultKeyword.DEC, DefaultKeyword.NUMERIC, 
            DefaultKeyword.REAL, DefaultKeyword.DOUBLE, DefaultKeyword.PRECISION, DefaultKeyword.TIME, DefaultKeyword.INTERVAL, DefaultKeyword.BOOLEAN, DefaultKeyword.BLOB, DefaultKeyword.XOR, 
            DefaultKeyword.GREATEST, DefaultKeyword.LEAST, DefaultKeyword.POSITION, DefaultKeyword.SUBSTRING, DefaultKeyword.TRIM, DefaultKeyword.BOTH, DefaultKeyword.LEADING, 
            DefaultKeyword.TRAILING, DefaultKeyword.CONVERT, DefaultKeyword.CONSTRAINT, DefaultKeyword.PRIMARY, DefaultKeyword.FOREIGN, DefaultKeyword.KEY, 
            DefaultKeyword.REFERENCES, DefaultKeyword.COMMIT, DefaultKeyword.BEGIN, DefaultKeyword.SAVEPOINT,
            OracleKeyword.LOCKED, OracleKeyword.CREATION, OracleKeyword.UPDATED, OracleKeyword.UPSERT, OracleKeyword.CONNECT_BY_ROOT, 
            OracleKeyword.STORE, OracleKeyword.MERGE, OracleKeyword.PURGE, OracleKeyword.GOTO, OracleKeyword.ONLY, OracleKeyword.AUTOMATIC, OracleKeyword.MAIN, 
            OracleKeyword.PCTINCREASE, OracleKeyword.CHUNK, OracleKeyword.LIMIT, OracleKeyword.GROUPING, OracleKeyword.ROLLUP, OracleKeyword.CUBE, 
            OracleKeyword.UNLIMITED, OracleKeyword.SIBLINGS, OracleKeyword.INCLUDE, OracleKeyword.EXCLUDE, OracleKeyword.PIVOT, OracleKeyword.UNPIVOT, 
            OracleKeyword.EXCEPTION, OracleKeyword.EXCEPTIONS, OracleKeyword.ERRORS, OracleKeyword.DEFERRED, OracleKeyword.NAV, OracleKeyword.VERSIONS, 
            OracleKeyword.WAIT, OracleKeyword.SAMPLE, OracleKeyword.CONTINUE, OracleKeyword.TIMESTAMP, OracleKeyword.SEGMENT, OracleKeyword.PARTITION, 
            OracleKeyword.SUBPARTITION, OracleKeyword.RETURN, OracleKeyword.RETURNING, OracleKeyword.REJECT, OracleKeyword.MAXTRANS, OracleKeyword.MINEXTENTS, 
            OracleKeyword.MATCHED, OracleKeyword.LOB, OracleKeyword.DIMENSION, OracleKeyword.FORCE, OracleKeyword.FIRST, 
            OracleKeyword.NEXT, OracleKeyword.LAST, OracleKeyword.EXTRACT, OracleKeyword.RULES, OracleKeyword.INITIALLY, OracleKeyword.KEEP, OracleKeyword.KEEP_DUPLICATES, 
            OracleKeyword.REFERENCE, OracleKeyword.SEED, OracleKeyword.IGNORE, OracleKeyword.MEASURES, OracleKeyword.LOGGING, OracleKeyword.MAXSIZE, OracleKeyword.FLASH_CACHE, 
            OracleKeyword.CELL_FLASH_CACHE, OracleKeyword.SKIP, OracleKeyword.NONE, OracleKeyword.NULLS, OracleKeyword.SINGLE, OracleKeyword.SCN, OracleKeyword.INITRANS, 
            OracleKeyword.BLOCK, OracleKeyword.SEQUENTIAL, OracleKeyword.BINARY, OracleKeyword.INSENSITIVE, OracleKeyword.SCROLL, OracleKeyword.XML, OracleKeyword.MINVALUE, 
            OracleKeyword.MAXVALUE, OracleKeyword.CACHE, OracleKeyword.NOCACHE, OracleKeyword.CYCLE, OracleKeyword.NOCYCLE,
        };
    }
    
    @Override
    protected TokenType[] getCustomizedAvailableKeywordsForTableAlias() {
        return new TokenType[] {
            DefaultKeyword.PROCEDURE, DefaultKeyword.FUNCTION, DefaultKeyword.CURSOR, DefaultKeyword.TRUE, DefaultKeyword.FALSE, DefaultKeyword.REPLACE, DefaultKeyword.BEFORE, 
            DefaultKeyword.EACH, DefaultKeyword.EXECUTE, DefaultKeyword.FULLTEXT, DefaultKeyword.CASE, DefaultKeyword.WHEN, DefaultKeyword.END, DefaultKeyword.USE,  
            DefaultKeyword.NATURAL, DefaultKeyword.DECLARE, DefaultKeyword.FETCH, DefaultKeyword.CLOSE, DefaultKeyword.CAST, DefaultKeyword.ESCAPE, 
            DefaultKeyword.SOME, DefaultKeyword.LEAVE, DefaultKeyword.ITERATE, DefaultKeyword.REPEAT, DefaultKeyword.OPEN, DefaultKeyword.OUT, DefaultKeyword.INOUT, 
            DefaultKeyword.OVER, DefaultKeyword.LOOP, DefaultKeyword.EXPLAIN, DefaultKeyword.COALESCE, DefaultKeyword.CHARACTER, DefaultKeyword.VARYING, DefaultKeyword.INT, 
            DefaultKeyword.DEC, DefaultKeyword.NUMERIC, DefaultKeyword.REAL, DefaultKeyword.DOUBLE, DefaultKeyword.PRECISION, DefaultKeyword.INTERVAL, DefaultKeyword.BLOB, 
            DefaultKeyword.XOR, DefaultKeyword.BOTH, DefaultKeyword.LEADING, DefaultKeyword.TRAILING, DefaultKeyword.CONVERT, DefaultKeyword.CONSTRAINT, DefaultKeyword.PRIMARY, 
            DefaultKeyword.FOREIGN, DefaultKeyword.KEY, DefaultKeyword.REFERENCES, DefaultKeyword.BEGIN, DefaultKeyword.SAVEPOINT, DefaultKeyword.COMMIT,
            OracleKeyword.LOCKED, OracleKeyword.CREATION, OracleKeyword.UPDATED, OracleKeyword.UPSERT, OracleKeyword.STORE, OracleKeyword.MERGE, 
            OracleKeyword.PURGE, OracleKeyword.GOTO, OracleKeyword.ONLY, OracleKeyword.AUTOMATIC, OracleKeyword.MAIN, OracleKeyword.PCTINCREASE, OracleKeyword.CHUNK, 
            OracleKeyword.LIMIT, OracleKeyword.GROUPING, OracleKeyword.ROLLUP, OracleKeyword.CUBE, OracleKeyword.UNLIMITED, OracleKeyword.SIBLINGS, OracleKeyword.INCLUDE, 
            OracleKeyword.EXCLUDE, OracleKeyword.PIVOT, OracleKeyword.UNPIVOT, OracleKeyword.EXCEPTION, OracleKeyword.EXCEPTIONS, OracleKeyword.ERRORS, OracleKeyword.DEFERRED, 
            OracleKeyword.NAV, OracleKeyword.VERSIONS, OracleKeyword.WAIT, OracleKeyword.CONTINUE, OracleKeyword.TIMESTAMP, OracleKeyword.SEGMENT, OracleKeyword.RETURN, 
            OracleKeyword.RETURNING, OracleKeyword.REJECT, OracleKeyword.MAXTRANS, OracleKeyword.MINEXTENTS, OracleKeyword.MATCHED, 
            OracleKeyword.LOB, OracleKeyword.DIMENSION, OracleKeyword.FORCE, OracleKeyword.FIRST, OracleKeyword.NEXT, OracleKeyword.LAST, OracleKeyword.EXTRACT, OracleKeyword.RULES, 
            OracleKeyword.INITIALLY, OracleKeyword.KEEP, OracleKeyword.KEEP_DUPLICATES, OracleKeyword.REFERENCE, OracleKeyword.SEED, OracleKeyword.IGNORE, OracleKeyword.MEASURES, 
            OracleKeyword.LOGGING, OracleKeyword.MAXSIZE, OracleKeyword.FLASH_CACHE, OracleKeyword.CELL_FLASH_CACHE, OracleKeyword.SKIP, OracleKeyword.NONE, OracleKeyword.NULLS, 
            OracleKeyword.SINGLE, OracleKeyword.SCN, OracleKeyword.INITRANS, OracleKeyword.BLOCK, OracleKeyword.SEQUENTIAL, OracleKeyword.BINARY, OracleKeyword.INSENSITIVE, 
            OracleKeyword.SCROLL, OracleKeyword.XML, OracleKeyword.MINVALUE, OracleKeyword.MAXVALUE, OracleKeyword.CACHE, OracleKeyword.NOCACHE, OracleKeyword.CYCLE, OracleKeyword.NOCYCLE,
        };
    }
}
