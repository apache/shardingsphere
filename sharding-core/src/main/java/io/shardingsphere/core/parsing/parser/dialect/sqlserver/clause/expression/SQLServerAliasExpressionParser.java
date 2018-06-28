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

package io.shardingsphere.core.parsing.parser.dialect.sqlserver.clause.expression;

import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.dialect.sqlserver.SQLServerKeyword;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.TokenType;
import io.shardingsphere.core.parsing.parser.clause.expression.AliasExpressionParser;

/**
 * Alias clause parser for SQLServer.
 *
 * @author zhangliang
 */
public final class SQLServerAliasExpressionParser extends AliasExpressionParser {
    
    public SQLServerAliasExpressionParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected TokenType[] getCustomizedAvailableKeywordsForSelectItemAlias() {
        return new TokenType[] {
            DefaultKeyword.TABLESPACE, DefaultKeyword.SEQUENCE, DefaultKeyword.DO, DefaultKeyword.NO, DefaultKeyword.WITHOUT, DefaultKeyword.TRUE, DefaultKeyword.FALSE, DefaultKeyword.TEMPORARY, 
            DefaultKeyword.TEMP, DefaultKeyword.COMMENT, DefaultKeyword.REPLACE, DefaultKeyword.BEFORE, DefaultKeyword.AFTER, DefaultKeyword.INSTEAD, DefaultKeyword.EACH, DefaultKeyword.ROW, 
            DefaultKeyword.STATEMENT, DefaultKeyword.FULLTEXT, DefaultKeyword.MODIFY, DefaultKeyword.RENAME, DefaultKeyword.ENABLE, DefaultKeyword.DISABLE, DefaultKeyword.VALIDATE, 
            DefaultKeyword.IDENTIFIED, DefaultKeyword.USING, DefaultKeyword.NATURAL, DefaultKeyword.CAST, DefaultKeyword.LOCK, DefaultKeyword.LEAVE, DefaultKeyword.ITERATE, DefaultKeyword.REPEAT, 
            DefaultKeyword.UNTIL, DefaultKeyword.OUT, DefaultKeyword.INOUT, DefaultKeyword.LOOP, DefaultKeyword.EXPLAIN, DefaultKeyword.PASSWORD, DefaultKeyword.LOCAL, DefaultKeyword.GLOBAL, 
            DefaultKeyword.STORAGE, DefaultKeyword.DATA, DefaultKeyword.CHAR, DefaultKeyword.CHARACTER, DefaultKeyword.VARCHAR, DefaultKeyword.VARCHAR2, DefaultKeyword.INTEGER, DefaultKeyword.INT, 
            DefaultKeyword.SMALLINT, DefaultKeyword.DECIMAL, DefaultKeyword.DEC, DefaultKeyword.NUMERIC, DefaultKeyword.FLOAT, DefaultKeyword.REAL, DefaultKeyword.PRECISION, DefaultKeyword.DATE, 
            DefaultKeyword.TIME, DefaultKeyword.INTERVAL, DefaultKeyword.BOOLEAN, DefaultKeyword.BLOB, DefaultKeyword.XOR, DefaultKeyword.GREATEST, DefaultKeyword.LEAST, DefaultKeyword.POSITION, 
            DefaultKeyword.SUBSTRING, DefaultKeyword.TRIM, DefaultKeyword.BOTH, DefaultKeyword.LEADING, DefaultKeyword.TRAILING,
            SQLServerKeyword.TIES, SQLServerKeyword.ROW_NUMBER, SQLServerKeyword.PARTITION, SQLServerKeyword.ONLY, SQLServerKeyword.OUTPUT, SQLServerKeyword.AUTO, SQLServerKeyword.TYPE, 
            SQLServerKeyword.ELEMENTS, SQLServerKeyword.XML, SQLServerKeyword.XSINIL, SQLServerKeyword.XMLSCHEMA, SQLServerKeyword.TYP, SQLServerKeyword.APPLY, SQLServerKeyword.REDUCE, 
            SQLServerKeyword.REPLICATE, SQLServerKeyword.EXTRACT, SQLServerKeyword.REDISTRIBUTE,
        };
    }
    
    @Override
    protected TokenType[] getCustomizedAvailableKeywordsForTableAlias() {
        return new TokenType[] {
            DefaultKeyword.TRUE, DefaultKeyword.FALSE, DefaultKeyword.COMMENT, DefaultKeyword.REPLACE, DefaultKeyword.BEFORE, DefaultKeyword.EACH, DefaultKeyword.ROW, 
            DefaultKeyword.FULLTEXT, DefaultKeyword.IDENTIFIED, DefaultKeyword.USING, DefaultKeyword.NATURAL, DefaultKeyword.CAST, DefaultKeyword.LOCK, DefaultKeyword.LEAVE, 
            DefaultKeyword.ITERATE, DefaultKeyword.REPEAT, DefaultKeyword.OUT, DefaultKeyword.INOUT, DefaultKeyword.LOOP, DefaultKeyword.EXPLAIN, DefaultKeyword.CHAR, DefaultKeyword.CHARACTER, 
            DefaultKeyword.VARCHAR, DefaultKeyword.VARCHAR2, DefaultKeyword.INTEGER, DefaultKeyword.INT, DefaultKeyword.SMALLINT, DefaultKeyword.DECIMAL, DefaultKeyword.DEC, 
            DefaultKeyword.NUMERIC, DefaultKeyword.FLOAT, DefaultKeyword.REAL, DefaultKeyword.PRECISION, DefaultKeyword.DATE, DefaultKeyword.INTERVAL, DefaultKeyword.BLOB, 
            DefaultKeyword.XOR, DefaultKeyword.BOTH, DefaultKeyword.LEADING, DefaultKeyword.TRAILING,
            SQLServerKeyword.TIES, SQLServerKeyword.ONLY, SQLServerKeyword.OUTPUT, SQLServerKeyword.AUTO, SQLServerKeyword.TYPE, SQLServerKeyword.ELEMENTS, 
            SQLServerKeyword.XML, SQLServerKeyword.XSINIL, SQLServerKeyword.XMLSCHEMA, SQLServerKeyword.TYP, SQLServerKeyword.APPLY, SQLServerKeyword.REDUCE, SQLServerKeyword.REPLICATE, 
            SQLServerKeyword.EXTRACT, SQLServerKeyword.REDISTRIBUTE,
        };
    }
}
