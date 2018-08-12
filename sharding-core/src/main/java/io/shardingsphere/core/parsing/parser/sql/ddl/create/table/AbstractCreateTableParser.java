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

package io.shardingsphere.core.parsing.parser.sql.ddl.create.table;

import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Keyword;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.parser.clause.TableReferencesClauseParser;
import io.shardingsphere.core.parsing.parser.exception.SQLParsingException;
import io.shardingsphere.core.parsing.parser.sql.SQLParser;
import io.shardingsphere.core.parsing.parser.sql.ddl.DDLStatement;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Create parser.
 *
 * @author zhangliang
 * @author panjuan
 */
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractCreateTableParser implements SQLParser {
    
    private final ShardingRule shardingRule;
    
    private final LexerEngine lexerEngine;
    
    private final TableReferencesClauseParser tableReferencesClauseParser;
    
    public AbstractCreateTableParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        this.shardingRule = shardingRule;
        this.lexerEngine = lexerEngine;
        tableReferencesClauseParser = new TableReferencesClauseParser(shardingRule, lexerEngine);
    }
    
    @Override
    public final DDLStatement parse() {
        lexerEngine.skipAll(getSkippedKeywordsBetweenCreateIndexAndKeyword());
        lexerEngine.skipAll(getSkippedKeywordsBetweenCreateAndKeyword());
        CreateTableStatement result = new CreateTableStatement();
        if (lexerEngine.skipIfEqual(DefaultKeyword.TABLE)) {
            lexerEngine.skipAll(getSkippedKeywordsBetweenCreateTableAndTableName());
        } else {
            throw new SQLParsingException("Can't support other CREATE grammar unless CREATE TABLE.");
        }
        tableReferencesClauseParser.parseSingleTableWithoutAlias(result);
        lexerEngine.accept(Symbol.LEFT_PAREN);
        do {
            parseCreateDefinition(result);
        } while (lexerEngine.skipIfEqual(Symbol.COMMA));
        lexerEngine.accept(Symbol.RIGHT_PAREN);
        return result;
    }
    
    protected abstract Keyword[] getSkippedKeywordsBetweenCreateIndexAndKeyword();
    
    protected abstract Keyword[] getSkippedKeywordsBetweenCreateAndKeyword();
    
    protected abstract Keyword[] getSkippedKeywordsBetweenCreateTableAndTableName();
    
    private void parseCreateDefinition(final CreateTableStatement statement) {
        String columnName = parseColumnName(statement);
        parseColumnDefinition(columnName, statement);
    }
    
    private String parseColumnName(final CreateTableStatement statement) {
        String result = lexerEngine.getCurrentToken().getLiterals();
        statement.getColumnNames().add(result);
        return result;
    }
    
    private void parseColumnDefinition(final String columnName, final CreateTableStatement statement) {
        parseDataType(statement);
        lexerEngine.skipUntil(DefaultKeyword.PRIMARY, Symbol.COMMA, Symbol.RIGHT_PAREN);
        if (lexerEngine.skipIfEqual(DefaultKeyword.PRIMARY)) {
            lexerEngine.accept(DefaultKeyword.KEY);
            lexerEngine.skipAll(getSkippedKeywordsBeforeTableConstraint());
            if (lexerEngine.skipIfEqual(Symbol.LEFT_PAREN)) {
                parseTableConstraint(statement);
            } else {
                parseInlineConstraint(columnName, statement);
            }
        }
    }
    
    private void parseDataType(final CreateTableStatement statement) {
        lexerEngine.nextToken();
        statement.getColumnTypes().add(lexerEngine.getCurrentToken().getLiterals());
        lexerEngine.skipParentheses(statement);
    }
    
    protected abstract Keyword[] getSkippedKeywordsBeforeTableConstraint();
    
    private void parseTableConstraint(final CreateTableStatement statement) {
        Collection<String> columnNames = new LinkedList<>();
        do {
            columnNames.add(lexerEngine.getCurrentToken().getLiterals());
            lexerEngine.nextToken();
            lexerEngine.skipParentheses(statement);
            lexerEngine.skipUntil(Symbol.COMMA, Symbol.RIGHT_PAREN);
        } while (lexerEngine.skipIfEqual(Symbol.COMMA));
        statement.getPrimaryKeyColumns().addAll(columnNames);
    }
    
    private void parseInlineConstraint(final String columnName, final CreateTableStatement statement) {
        statement.getPrimaryKeyColumns().add(columnName);
        lexerEngine.skipUntil(Symbol.COMMA, Symbol.RIGHT_PAREN);
    }
}
