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
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.mysql.MySQLKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.mysql.MySQLLexer;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Literals;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.Limit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.LimitValue;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.SelectStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.OffsetToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.RowCountToken;

/**
 * MySQL解析器.
 *
 * @author zhangliang
 */
public final class MySQLParser extends SQLParser {
    
    public MySQLParser(final String sql, final ShardingRule shardingRule) {
        super(new MySQLLexer(sql), shardingRule);
        getLexer().nextToken();
    }
    
    /**
     * 解析分页.
     * 
     * @param selectStatement SQL语句对象
     * @param parametersIndex 参数索引
     */
    public void parseLimit(final SelectStatement selectStatement, final int parametersIndex) {
        skipIfEqual(MySQLKeyword.LIMIT);
        int valueIndex = -1;
        int valueBeginPosition = getLexer().getCurrentToken().getEndPosition();
        int value;
        boolean isParameterForValue = false;
        if (equalAny(Literals.INT)) {
            value = Integer.parseInt(getLexer().getCurrentToken().getLiterals());
            valueBeginPosition = valueBeginPosition - (value + "").length();
        } else if (equalAny(Symbol.QUESTION)) {
            valueIndex = parametersIndex;
            value = -1;
            valueBeginPosition--;
            isParameterForValue = true;
        } else {
            throw new SQLParsingException(getLexer());
        }
        getLexer().nextToken();
        if (skipIfEqual(Symbol.COMMA)) {
            selectStatement.setLimit(getLimitWithComma(selectStatement, parametersIndex, valueIndex, valueBeginPosition, value, isParameterForValue));
            return;
        }
        if (skipIfEqual(MySQLKeyword.OFFSET)) {
            selectStatement.setLimit(getLimitWithOffset(selectStatement, parametersIndex, valueIndex, valueBeginPosition, value, isParameterForValue));
            return;
        }
        if (!isParameterForValue) {
            selectStatement.getSqlTokens().add(new RowCountToken(valueBeginPosition, value));
        }
        Limit limit = new Limit(true);
        limit.setRowCount(new LimitValue(value, valueIndex));
        selectStatement.setLimit(limit);
    }
    
    private Limit getLimitWithComma(final SQLStatement sqlStatement, final int parametersIndex, final int index, final int valueBeginPosition, final int value, final boolean isParameterForValue) {
        int rowCountBeginPosition = getLexer().getCurrentToken().getEndPosition();
        int rowCountValue;
        int rowCountIndex = -1;
        boolean isParameterForRowCount = false;
        if (equalAny(Literals.INT)) {
            rowCountValue = Integer.parseInt(getLexer().getCurrentToken().getLiterals());
            rowCountBeginPosition = rowCountBeginPosition - (rowCountValue + "").length();
        } else if (equalAny(Symbol.QUESTION)) {
            rowCountIndex = -1 == index ? parametersIndex : index + 1;
            rowCountValue = -1;
            rowCountBeginPosition--;
            isParameterForRowCount = true;
        } else {
            throw new SQLParsingException(getLexer());
        }
        getLexer().nextToken();
        if (!isParameterForValue) {
            sqlStatement.getSqlTokens().add(new OffsetToken(valueBeginPosition, value));
        }
        if (!isParameterForRowCount) {
            sqlStatement.getSqlTokens().add(new RowCountToken(rowCountBeginPosition, rowCountValue));
        }
        Limit result = new Limit(true);
        result.setRowCount(new LimitValue(rowCountValue, rowCountIndex));
        result.setOffset(new LimitValue(value, index));
        return result;
    }
    
    private Limit getLimitWithOffset(final SQLStatement sqlStatement, final int parametersIndex, final int index, final int valueBeginPosition, final int value, final boolean isParameterForValue) {
        int offsetBeginPosition = getLexer().getCurrentToken().getEndPosition();
        int offsetValue = -1;
        int offsetIndex = -1;
        boolean isParameterForOffset = false;
        if (equalAny(Literals.INT)) {
            offsetValue = Integer.parseInt(getLexer().getCurrentToken().getLiterals());
            offsetBeginPosition = offsetBeginPosition - (offsetValue + "").length();
        } else if (equalAny(Symbol.QUESTION)) {
            offsetIndex = -1 == index ? parametersIndex : index + 1;
            offsetBeginPosition--;
            isParameterForOffset = true;
        } else {
            throw new SQLParsingException(getLexer());
        }
        getLexer().nextToken();
        if (!isParameterForOffset) {
            sqlStatement.getSqlTokens().add(new OffsetToken(offsetBeginPosition, offsetValue));
        }
        if (!isParameterForValue) {
            sqlStatement.getSqlTokens().add(new RowCountToken(valueBeginPosition, value));
        }
        Limit result = new Limit(true);
        result.setRowCount(new LimitValue(value, index));
        result.setOffset(new LimitValue(offsetValue, offsetIndex));
        return result;
    }
}
