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

package io.shardingjdbc.core.parsing.parser.sql.ddl.drop;

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
 * Drop parser.
 *
 * @author zhangliang
 */
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractDropParser implements SQLParser {
    
    private final ShardingRule shardingRule;
    
    private final LexerEngine lexerEngine;
    
    private final TableReferencesClauseParser tableReferencesClauseParser;
    
    public AbstractDropParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        this.shardingRule = shardingRule;
        this.lexerEngine = lexerEngine;
        tableReferencesClauseParser = new TableReferencesClauseParser(shardingRule, lexerEngine);
    }
    
    @Override
    public DDLStatement parse() {
        lexerEngine.nextToken();
        lexerEngine.skipAll(getSkippedKeywordsBetweenDropAndTable());
        DDLStatement result = new DDLStatement();
        if (lexerEngine.skipIfEqual(DefaultKeyword.INDEX)) {
            lexerEngine.skipAll(getSkippedKeywordsBetweenDropIndexAndIndexName());
            parseIndex(result);
        } else if (lexerEngine.skipIfEqual(DefaultKeyword.TABLE)) {
            lexerEngine.skipAll(getSkippedKeywordsBetweenDropTableAndTableName());
            tableReferencesClauseParser.parseSingleTableWithoutAlias(result);
        } else {
            throw new SQLParsingException("Can't support other DROP grammar unless DROP TABLE, DROP INDEX.");
        }
        return result;
    }
    
    protected Keyword[] getSkippedKeywordsBetweenDropAndTable() {
        return new Keyword[0];
    }
    
    protected Keyword[] getSkippedKeywordsBetweenDropIndexAndIndexName() {
        return new Keyword[] {};
    }
    
    private void parseIndex(final DDLStatement ddlStatement) {
        Token currentToken = lexerEngine.getCurrentToken();
        int beginPosition = currentToken.getEndPosition() - currentToken.getLiterals().length();
        String literals = currentToken.getLiterals();
        lexerEngine.skipUntil(DefaultKeyword.ON);
        if (lexerEngine.skipIfEqual(DefaultKeyword.ON)) {
            tableReferencesClauseParser.parseSingleTableWithoutAlias(ddlStatement);
            ddlStatement.getSqlTokens().add(new IndexToken(beginPosition, literals, ddlStatement.getTables().getSingleTableName()));
        } else {
            ddlStatement.getSqlTokens().add(new IndexToken(beginPosition, literals, ""));
        }
    }
    
    protected abstract Keyword[] getSkippedKeywordsBetweenDropTableAndTableName();
}
