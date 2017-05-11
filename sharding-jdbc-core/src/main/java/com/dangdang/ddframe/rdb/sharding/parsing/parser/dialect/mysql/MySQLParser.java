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
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.LimitContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.OffsetLimitToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.RowCountLimitToken;

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
     * 解析分页上.
     * 
     * @param sqlContext SQL上下文
     * @param parametersIndex 参数索引
     * @return 分页上下文
     */
    public LimitContext parseLimit(final SQLContext sqlContext, final int parametersIndex) {
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
            return getLimitContextWithComma(sqlContext, parametersIndex, valueIndex, valueBeginPosition, value, isParameterForValue);
        }
        if (skipIfEqual(MySQLKeyword.OFFSET)) {
            return getLimitContextWithOffset(sqlContext, parametersIndex, valueIndex, valueBeginPosition, value, isParameterForValue);
        }
        if (!isParameterForValue) {
            sqlContext.getSqlTokens().add(new RowCountLimitToken(valueBeginPosition, value));
        }
        return new LimitContext(value, valueIndex);
    }
    
    private LimitContext getLimitContextWithComma(final SQLContext sqlContext, 
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
            rowCount = -1;
            rowCountBeginPosition--;
            isParameterForRowCount = true;
        } else {
            throw new SQLParsingException(getLexer());
        }
        getLexer().nextToken();
        if (!isParameterForValue) {
            sqlContext.getSqlTokens().add(new OffsetLimitToken(valueBeginPosition, value));
        }
        if (!isParameterForRowCount) {
            sqlContext.getSqlTokens().add(new RowCountLimitToken(rowCountBeginPosition, rowCount));
        }
        return new LimitContext(value, rowCount, valueIndex, rowCountIndex);
    }
    
    private LimitContext getLimitContextWithOffset(final SQLContext sqlContext, 
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
            offset = -1;
            offsetBeginPosition--;
            isParameterForOffset = true;
        } else {
            throw new SQLParsingException(getLexer());
        }
        getLexer().nextToken();
        if (!isParameterForOffset) {
            sqlContext.getSqlTokens().add(new OffsetLimitToken(offsetBeginPosition, offset));
        }
        if (!isParameterForValue) {
            sqlContext.getSqlTokens().add(new RowCountLimitToken(valueBeginPosition, value));
        }
        return new LimitContext(offset, value, offsetIndex, valueIndex);
    }
}
