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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dml.update;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.ExpressionClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.TableClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.WhereClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatementParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dml.DMLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.Collections;

/**
 * Update语句解析器.
 *
 * @author zhangliang
 */
public abstract class AbstractUpdateParser implements SQLStatementParser {
    
    private final ShardingRule shardingRule;
    
    private final LexerEngine lexerEngine;
    
    private final ExpressionClauseParser expressionClauseParser;
    
    private final TableClauseParser tableClauseParser;
    
    private final WhereClauseParser whereClauseParser;
    
    @Getter(AccessLevel.NONE)
    private int parametersIndex;
    
    public AbstractUpdateParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        this.shardingRule = shardingRule;
        this.lexerEngine = lexerEngine;
        expressionClauseParser = new ExpressionClauseParser(lexerEngine);
        tableClauseParser = new TableClauseParser(shardingRule, lexerEngine);
        whereClauseParser = new WhereClauseParser(lexerEngine);
    }
    
    @Override
    public DMLStatement parse() {
        lexerEngine.nextToken();
        lexerEngine.skipAll(getSkippedKeywordsBetweenUpdateAndTable());
        if (lexerEngine.equalAny(getUnsupportedKeywordsBetweenUpdateAndTable())) {
            throw new SQLParsingUnsupportedException(lexerEngine.getCurrentToken().getType());
        }
        DMLStatement result = new DMLStatement();
        tableClauseParser.parseSingleTable(result);
        parseSetItems(result);
        lexerEngine.skipUntil(DefaultKeyword.WHERE);
        result.setParametersIndex(parametersIndex);
        whereClauseParser.parse(shardingRule, result, Collections.<SelectItem>emptyList());
        return result;
    }
    
    protected Keyword[] getSkippedKeywordsBetweenUpdateAndTable() {
        return new Keyword[0];
    }
    
    protected Keyword[] getUnsupportedKeywordsBetweenUpdateAndTable() {
        return new Keyword[0];
    }
    
    private void parseSetItems(final DMLStatement updateStatement) {
        lexerEngine.accept(DefaultKeyword.SET);
        do {
            parseSetItem(updateStatement);
        } while (lexerEngine.skipIfEqual(Symbol.COMMA));
    }
    
    private void parseSetItem(final DMLStatement updateStatement) {
        parseSetColumn(updateStatement);
        lexerEngine.skipIfEqual(Symbol.EQ, Symbol.COLON_EQ);
        parseSetValue(updateStatement);
    }
    
    private void parseSetColumn(final DMLStatement updateStatement) {
        if (lexerEngine.equalAny(Symbol.LEFT_PAREN)) {
            lexerEngine.skipParentheses(updateStatement);
            return;
        }
        int beginPosition = lexerEngine.getCurrentToken().getEndPosition();
        String literals = lexerEngine.getCurrentToken().getLiterals();
        lexerEngine.nextToken();
        if (lexerEngine.skipIfEqual(Symbol.DOT)) {
            if (updateStatement.getTables().getSingleTableName().equalsIgnoreCase(SQLUtil.getExactlyValue(literals))) {
                updateStatement.getSqlTokens().add(new TableToken(beginPosition - literals.length(), literals));
            }
            lexerEngine.nextToken();
        }
    }
    
    private void parseSetValue(final DMLStatement updateStatement) {
        expressionClauseParser.parse(updateStatement);
        parametersIndex = updateStatement.getParametersIndex();
    }
}
