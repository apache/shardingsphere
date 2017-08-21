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
import com.dangdang.ddframe.rdb.sharding.parsing.parser.AbstractSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.CommonParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.ExpressionSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.WhereSQLParser;
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
    
    private final CommonParser commonParser;
    
    private final AbstractSQLParser sqlParser;
    
    private final ExpressionSQLParser expressionSQLParser;
    
    private final WhereSQLParser whereSQLParser;
    
    @Getter(AccessLevel.NONE)
    private int parametersIndex;
    
    public AbstractUpdateParser(final ShardingRule shardingRule, final CommonParser commonParser, final AbstractSQLParser sqlParser) {
        this.shardingRule = shardingRule;
        this.commonParser = commonParser;
        this.sqlParser = sqlParser;
        expressionSQLParser = new ExpressionSQLParser(commonParser);
        whereSQLParser = new WhereSQLParser(commonParser);
    }
    
    @Override
    public DMLStatement parse() {
        commonParser.getLexer().nextToken();
        commonParser.skipAll(getSkippedKeywordsBetweenUpdateAndTable());
        if (commonParser.equalAny(getUnsupportedKeywordsBetweenUpdateAndTable())) {
            throw new SQLParsingUnsupportedException(commonParser.getLexer().getCurrentToken().getType());
        }
        DMLStatement result = new DMLStatement();
        sqlParser.parseSingleTable(result);
        parseSetItems(result);
        commonParser.skipUntil(DefaultKeyword.WHERE);
        result.setParametersIndex(parametersIndex);
        whereSQLParser.parseWhere(shardingRule, result, Collections.<SelectItem>emptyList());
        return result;
    }
    
    protected Keyword[] getSkippedKeywordsBetweenUpdateAndTable() {
        return new Keyword[0];
    }
    
    protected Keyword[] getUnsupportedKeywordsBetweenUpdateAndTable() {
        return new Keyword[0];
    }
    
    private void parseSetItems(final DMLStatement updateStatement) {
        commonParser.accept(DefaultKeyword.SET);
        do {
            parseSetItem(updateStatement);
        } while (commonParser.skipIfEqual(Symbol.COMMA));
    }
    
    private void parseSetItem(final DMLStatement updateStatement) {
        parseSetColumn(updateStatement);
        commonParser.skipIfEqual(Symbol.EQ, Symbol.COLON_EQ);
        parseSetValue(updateStatement);
    }
    
    private void parseSetColumn(final DMLStatement updateStatement) {
        if (commonParser.equalAny(Symbol.LEFT_PAREN)) {
            commonParser.skipParentheses(updateStatement);
            return;
        }
        int beginPosition = commonParser.getLexer().getCurrentToken().getEndPosition();
        String literals = commonParser.getLexer().getCurrentToken().getLiterals();
        commonParser.getLexer().nextToken();
        if (commonParser.skipIfEqual(Symbol.DOT)) {
            if (updateStatement.getTables().getSingleTableName().equalsIgnoreCase(SQLUtil.getExactlyValue(literals))) {
                updateStatement.getSqlTokens().add(new TableToken(beginPosition - literals.length(), literals));
            }
            commonParser.getLexer().nextToken();
        }
    }
    
    private void parseSetValue(final DMLStatement updateStatement) {
        expressionSQLParser.parse(updateStatement);
        parametersIndex = updateStatement.getParametersIndex();
    }
}
