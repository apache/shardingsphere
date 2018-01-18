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

package io.shardingjdbc.core.parsing.parser.sql.ddl.create;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Keyword;
import io.shardingjdbc.core.parsing.lexer.token.Token;
import io.shardingjdbc.core.parsing.parser.clause.TableReferencesClauseParser;
import io.shardingjdbc.core.parsing.parser.exception.SQLParsingException;
import io.shardingjdbc.core.parsing.parser.sql.SQLParser;
import io.shardingjdbc.core.parsing.parser.sql.ddl.DDLStatement;
import io.shardingjdbc.core.parsing.parser.token.IndexToken;
import io.shardingjdbc.core.rule.ShardingRule;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * Create parser.
 *
 * @author zhangliang
 */
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractCreateParser implements SQLParser {
    
    private final ShardingRule shardingRule;
    
    private final LexerEngine lexerEngine;
    
    private final TableReferencesClauseParser tableReferencesClauseParser;
    
    public AbstractCreateParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        this.shardingRule = shardingRule;
        this.lexerEngine = lexerEngine;
        tableReferencesClauseParser = new TableReferencesClauseParser(shardingRule, lexerEngine);
    }
    
    @Override
    public DDLStatement parse() {
        lexerEngine.nextToken();
        lexerEngine.skipAll(getSkippedKeywordsBetweenCreateIndexAndKeyword());
        lexerEngine.skipAll(getSkippedKeywordsBetweenCreateAndKeyword());
        DDLStatement result = new DDLStatement();
        if (lexerEngine.skipIfEqual(DefaultKeyword.INDEX)) {
            lexerEngine.skipAll(getSkippedKeywordsBetweenCreateIndexAndIndexName());
            parseIndex(result);
        } else if (lexerEngine.skipIfEqual(DefaultKeyword.TABLE)) {
            lexerEngine.skipAll(getSkippedKeywordsBetweenCreateTableAndTableName());
        } else {
            throw new SQLParsingException("Can't support other CREATE grammar unless CREATE TABLE, CREATE INDEX.");
        }
        tableReferencesClauseParser.parseSingleTableWithoutAlias(result);
        return result;
    }
    
    protected abstract Keyword[] getSkippedKeywordsBetweenCreateIndexAndKeyword();
    
    protected abstract Keyword[] getSkippedKeywordsBetweenCreateAndKeyword();
    
    protected Keyword[] getSkippedKeywordsBetweenCreateIndexAndIndexName() {
        return new Keyword[] {};
    }
    
    private void parseIndex(final DDLStatement ddlStatement) {
        Token currentToken = lexerEngine.getCurrentToken();
        int beginPosition = currentToken.getEndPosition() - currentToken.getLiterals().length();
        String literals = currentToken.getLiterals();
        lexerEngine.skipUntil(DefaultKeyword.ON);
        lexerEngine.nextToken();
        String tableName = lexerEngine.getCurrentToken().getLiterals();
        ddlStatement.getSqlTokens().add(new IndexToken(beginPosition, literals, tableName));
    }
    
    protected abstract Keyword[] getSkippedKeywordsBetweenCreateTableAndTableName();
}
