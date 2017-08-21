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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.mysql.MySQLKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Literals;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.AbstractSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.CommonParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.Limit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.LimitValue;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.AbstractSelectParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.OffsetToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.RowCountToken;

/**
 * MySQL Select语句解析器.
 *
 * @author zhangliang
 */
public final class MySQLSelectParser extends AbstractSelectParser {
    
    public MySQLSelectParser(final ShardingRule shardingRule, final CommonParser commonParser, final AbstractSQLParser sqlParser) {
        super(shardingRule, commonParser, sqlParser);
    }
    
    @Override
    protected void parseInternal(final SelectStatement selectStatement) {
        parseDistinct();
        skipBeforeSelectList();
        parseSelectList(selectStatement);
        parseFrom(selectStatement);
        parseWhere(selectStatement);
        parseGroupBy(selectStatement);
        parseHaving();
        parseOrderBy(selectStatement);
        parseLimit(selectStatement);
        parseRest();
    }
    
    private void skipBeforeSelectList() {
        getCommonParser().skipAll(MySQLKeyword.HIGH_PRIORITY, DefaultKeyword.STRAIGHT_JOIN, MySQLKeyword.SQL_SMALL_RESULT, MySQLKeyword.SQL_BIG_RESULT, MySQLKeyword.SQL_BUFFER_RESULT, 
                MySQLKeyword.SQL_CACHE, MySQLKeyword.SQL_NO_CACHE, MySQLKeyword.SQL_CALC_FOUND_ROWS);
    }
    
    private void parseLimit(final SelectStatement selectStatement) {
        if (!getCommonParser().skipIfEqual(MySQLKeyword.LIMIT)) {
            return;
        }
        int valueIndex = -1;
        int valueBeginPosition = getCommonParser().getLexer().getCurrentToken().getEndPosition();
        int value;
        boolean isParameterForValue = false;
        if (getCommonParser().equalAny(Literals.INT)) {
            value = Integer.parseInt(getCommonParser().getLexer().getCurrentToken().getLiterals());
            valueBeginPosition = valueBeginPosition - (value + "").length();
        } else if (getCommonParser().equalAny(Symbol.QUESTION)) {
            valueIndex = getParametersIndex();
            value = -1;
            valueBeginPosition--;
            isParameterForValue = true;
        } else {
            throw new SQLParsingException(getCommonParser().getLexer());
        }
        getCommonParser().getLexer().nextToken();
        if (getCommonParser().skipIfEqual(Symbol.COMMA)) {
            selectStatement.setLimit(getLimitWithComma(valueIndex, valueBeginPosition, value, isParameterForValue, selectStatement));
            return;
        }
        if (getCommonParser().skipIfEqual(MySQLKeyword.OFFSET)) {
            selectStatement.setLimit(getLimitWithOffset(valueIndex, valueBeginPosition, value, isParameterForValue, selectStatement));
            return;
        }
        if (!isParameterForValue) {
            selectStatement.getSqlTokens().add(new RowCountToken(valueBeginPosition, value));
        }
        Limit limit = new Limit(true);
        limit.setRowCount(new LimitValue(value, valueIndex));
        selectStatement.setLimit(limit);
    }
    
    private Limit getLimitWithComma(final int index, final int valueBeginPosition, final int value, final boolean isParameterForValue, final SelectStatement selectStatement) {
        int rowCountBeginPosition = getCommonParser().getLexer().getCurrentToken().getEndPosition();
        int rowCountValue;
        int rowCountIndex = -1;
        boolean isParameterForRowCount = false;
        if (getCommonParser().equalAny(Literals.INT)) {
            rowCountValue = Integer.parseInt(getCommonParser().getLexer().getCurrentToken().getLiterals());
            rowCountBeginPosition = rowCountBeginPosition - (rowCountValue + "").length();
        } else if (getCommonParser().equalAny(Symbol.QUESTION)) {
            rowCountIndex = -1 == index ? getParametersIndex() : index + 1;
            rowCountValue = -1;
            rowCountBeginPosition--;
            isParameterForRowCount = true;
        } else {
            throw new SQLParsingException(getCommonParser().getLexer());
        }
        getCommonParser().getLexer().nextToken();
        if (!isParameterForValue) {
            selectStatement.getSqlTokens().add(new OffsetToken(valueBeginPosition, value));
        }
        if (!isParameterForRowCount) {
            selectStatement.getSqlTokens().add(new RowCountToken(rowCountBeginPosition, rowCountValue));
        }
        Limit result = new Limit(true);
        result.setRowCount(new LimitValue(rowCountValue, rowCountIndex));
        result.setOffset(new LimitValue(value, index));
        return result;
    }
    
    private Limit getLimitWithOffset(final int index, final int valueBeginPosition, final int value, final boolean isParameterForValue, final SelectStatement selectStatement) {
        int offsetBeginPosition = getCommonParser().getLexer().getCurrentToken().getEndPosition();
        int offsetValue = -1;
        int offsetIndex = -1;
        boolean isParameterForOffset = false;
        if (getCommonParser().equalAny(Literals.INT)) {
            offsetValue = Integer.parseInt(getCommonParser().getLexer().getCurrentToken().getLiterals());
            offsetBeginPosition = offsetBeginPosition - (offsetValue + "").length();
        } else if (getCommonParser().equalAny(Symbol.QUESTION)) {
            offsetIndex = -1 == index ? getParametersIndex() : index + 1;
            offsetBeginPosition--;
            isParameterForOffset = true;
        } else {
            throw new SQLParsingException(getCommonParser().getLexer());
        }
        getCommonParser().getLexer().nextToken();
        if (!isParameterForOffset) {
            selectStatement.getSqlTokens().add(new OffsetToken(offsetBeginPosition, offsetValue));
        }
        if (!isParameterForValue) {
            selectStatement.getSqlTokens().add(new RowCountToken(valueBeginPosition, value));
        }
        Limit result = new Limit(true);
        result.setRowCount(new LimitValue(value, index));
        result.setOffset(new LimitValue(offsetValue, offsetIndex));
        return result;
    }
    
    @Override
    protected Keyword[] getSynonymousKeywordsForDistinct() {
        return new Keyword[] {MySQLKeyword.DISTINCTROW};
    }
    
    @Override
    protected void parseJoinTable(final SelectStatement selectStatement) {
        if (getCommonParser().equalAny(DefaultKeyword.USING)) {
            return;
        }
        if (getCommonParser().equalAny(DefaultKeyword.USE)) {
            getCommonParser().getLexer().nextToken();
            skipIndexHint(selectStatement);
        }
        if (getCommonParser().equalAny(OracleKeyword.IGNORE)) {
            getCommonParser().getLexer().nextToken();
            skipIndexHint(selectStatement);
        }
        if (getCommonParser().equalAny(OracleKeyword.FORCE)) {
            getCommonParser().getLexer().nextToken();
            skipIndexHint(selectStatement);
        }
        super.parseJoinTable(selectStatement);
    }
    
    private void skipIndexHint(final SelectStatement selectStatement) {
        if (getCommonParser().equalAny(DefaultKeyword.INDEX)) {
            getCommonParser().getLexer().nextToken();
        } else {
            getCommonParser().accept(DefaultKeyword.KEY);
        }
        if (getCommonParser().equalAny(DefaultKeyword.FOR)) {
            getCommonParser().getLexer().nextToken();
            if (getCommonParser().equalAny(DefaultKeyword.JOIN)) {
                getCommonParser().getLexer().nextToken();
            } else if (getCommonParser().equalAny(DefaultKeyword.ORDER)) {
                getCommonParser().getLexer().nextToken();
                getCommonParser().accept(DefaultKeyword.BY);
            } else {
                getCommonParser().accept(DefaultKeyword.GROUP);
                getCommonParser().accept(DefaultKeyword.BY);
            }
        }
        getCommonParser().skipParentheses(selectStatement);
    }
    
    @Override
    protected Keyword[] getSkippedKeywordAfterGroupBy() {
        return new Keyword[] {DefaultKeyword.WITH, MySQLKeyword.ROLLUP};
    }
    
    @Override
    protected OrderType getNullOrderType() {
        return OrderType.ASC;
    }
    
    @Override
    protected Keyword[] getUnsupportedKeywordsRest() {
        return new Keyword[] {DefaultKeyword.PROCEDURE, DefaultKeyword.INTO};
    }
}
