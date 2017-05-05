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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.parser;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.exception.SQLParserException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.LimitContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OffsetLimitToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.RowCountLimitToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.lexer.MySQLKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.lexer.MySQLLexer;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Literals;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.ParserException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;

import java.util.List;

/**
 * MySQL解析器.
 *
 * @author zhangliang
 */
public final class MySQLParser extends SQLParser {
    
    public MySQLParser(final String sql, final ShardingRule shardingRule, final List<Object> parameters) {
        super(new MySQLLexer(sql), shardingRule, parameters);
        getLexer().nextToken();
    }
    
    /**
     * 解析分页上.
     * 
     * @param parametersIndex 参数索引
     * @return 分页上下文
     */
    public LimitContext parseLimit(final int parametersIndex) {
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
            value = (int) getParameters().get(valueIndex);
            valueBeginPosition--;
            isParameterForValue = true;
        } else {
            throw new ParserException(getLexer());
        }
        getLexer().nextToken();
        if (skipIfEqual(Symbol.COMMA)) {
            return getLimitContextWithComma(parametersIndex, valueIndex, valueBeginPosition, value, isParameterForValue);
        }
        if (skipIfEqual(MySQLKeyword.OFFSET)) {
            return getLimitContextWithOffset(parametersIndex, valueIndex, valueBeginPosition, value, isParameterForValue);
        }
        if (!isParameterForValue) {
            getSqlBuilderContext().getSqlTokens().add(new RowCountLimitToken(valueBeginPosition, value));
        }
        if (value < 0) {
            throw new SQLParserException("LIMIT offset and row count can not be a negative value");
        }
        return new LimitContext(value, valueIndex);
    }
    
    private LimitContext getLimitContextWithComma(
            final int parametersIndex, final int valueIndex, final int valueBeginPosition, final int value, final boolean isParameterForValue) {
        int rowCountBeginPosition = getLexer().getCurrentToken().getEndPosition();
        int rowCount;
        int rowCountIndex = -1;
        boolean isParameterForRowCount = false;
        if (equalAny(Literals.INT)) {
            rowCount = Integer.parseInt(getLexer().getCurrentToken().getLiterals());
            rowCountBeginPosition = rowCountBeginPosition - (rowCount + "").length();
        } else if (equalAny(Symbol.QUESTION)) {
            rowCountIndex = -1 == valueIndex ? parametersIndex : valueIndex + 1;
            rowCount = (int) getParameters().get(rowCountIndex);
            rowCountBeginPosition--;
            isParameterForRowCount = true;
        } else {
            throw new ParserException(getLexer());
        }
        getLexer().nextToken();
        if (!isParameterForValue) {
            getSqlBuilderContext().getSqlTokens().add(new OffsetLimitToken(valueBeginPosition, value));
        }
        if (!isParameterForRowCount) {
            getSqlBuilderContext().getSqlTokens().add(new RowCountLimitToken(rowCountBeginPosition, rowCount));
        }
        if (value < 0 || rowCount < 0) {
            throw new SQLParserException("LIMIT offset and row count can not be a negative value");
        }
        return new LimitContext(value, rowCount, valueIndex, rowCountIndex);
    }
    
    private LimitContext getLimitContextWithOffset(
            final int parametersIndex, final int valueIndex, final int valueBeginPosition, final int value, final boolean isParameterForValue) {
        int offsetBeginPosition = getLexer().getCurrentToken().getEndPosition();
        int offset;
        int offsetIndex = -1;
        boolean isParameterForOffset = false;
        if (equalAny(Literals.INT)) {
            offset = Integer.parseInt(getLexer().getCurrentToken().getLiterals());
            offsetBeginPosition = offsetBeginPosition - (offset + "").length();
        } else if (equalAny(Symbol.QUESTION)) {
            offsetIndex = -1 == valueIndex ? parametersIndex : valueIndex + 1;
            offset = (int) getParameters().get(offsetIndex);
            offsetBeginPosition--;
            isParameterForOffset = true;
        } else {
            throw new ParserException(getLexer());
        }
        getLexer().nextToken();
        if (!isParameterForOffset) {
            getSqlBuilderContext().getSqlTokens().add(new OffsetLimitToken(offsetBeginPosition, offset));
        }
        if (!isParameterForValue) {
            getSqlBuilderContext().getSqlTokens().add(new RowCountLimitToken(valueBeginPosition, value));
        }
        if (value < 0 || offset < 0) {
            throw new SQLParserException("LIMIT offset and row count can not be a negative value");
        }
        return new LimitContext(offset, value, offsetIndex, valueIndex);
    }
}
