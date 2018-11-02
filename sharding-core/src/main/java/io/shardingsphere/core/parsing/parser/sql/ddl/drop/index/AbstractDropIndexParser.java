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

package io.shardingsphere.core.parsing.parser.sql.ddl.drop.index;

import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Keyword;
import io.shardingsphere.core.parsing.lexer.token.Token;
import io.shardingsphere.core.parsing.parser.clause.TableReferencesClauseParser;
import io.shardingsphere.core.parsing.parser.exception.SQLParsingException;
import io.shardingsphere.core.parsing.parser.sql.SQLParser;
import io.shardingsphere.core.parsing.parser.sql.ddl.DDLStatement;
import io.shardingsphere.core.parsing.parser.token.IndexToken;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * Drop parser.
 *
 * @author zhangliang
 * @author panjuan
 */
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractDropIndexParser implements SQLParser {
    
    private final ShardingRule shardingRule;
    
    private final LexerEngine lexerEngine;
    
    private final TableReferencesClauseParser tableReferencesClauseParser;
    
    public AbstractDropIndexParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        this.shardingRule = shardingRule;
        this.lexerEngine = lexerEngine;
        tableReferencesClauseParser = new TableReferencesClauseParser(shardingRule, lexerEngine);
    }
    
    @Override
    public final DDLStatement parse() {
        lexerEngine.skipAll(getSkippedKeywordsBetweenDropAndTable());
        DDLStatement result = new DDLStatement();
        if (lexerEngine.skipIfEqual(DefaultKeyword.INDEX)) {
            lexerEngine.skipAll(getSkippedKeywordsBetweenDropIndexAndIndexName());
            parseIndex(result);
        } else {
            throw new SQLParsingException("Can't support other DROP grammar unless DROP INDEX.");
        }
        return result;
    }
    
    protected abstract Keyword[] getSkippedKeywordsBetweenDropAndTable();
    
    protected abstract Keyword[] getSkippedKeywordsBetweenDropIndexAndIndexName();
    
    private void parseIndex(final DDLStatement ddlStatement) {
        Token currentToken = lexerEngine.getCurrentToken();
        int beginPosition = currentToken.getEndPosition() - currentToken.getLiterals().length();
        String literals = currentToken.getLiterals();
        lexerEngine.skipUntil(DefaultKeyword.ON);
        if (lexerEngine.skipIfEqual(DefaultKeyword.ON)) {
            tableReferencesClauseParser.parseSingleTableWithoutAlias(ddlStatement);
            ddlStatement.addSQLToken(new IndexToken(beginPosition, literals, ddlStatement.getTables().getSingleTableName()));
        } else {
            ddlStatement.addSQLToken(new IndexToken(beginPosition, literals, ""));
        }
    }
}
