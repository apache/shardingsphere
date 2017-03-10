/*
 * Copyright 1999-2101 Alibaba Group Holding Ltd.
 *
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
 */

package com.alibaba.druid.sql.dialect.mysql.parser;

import com.alibaba.druid.sql.context.LimitContext;
import com.alibaba.druid.sql.context.OffsetLimitToken;
import com.alibaba.druid.sql.context.RowCountLimitToken;
import com.alibaba.druid.sql.context.SelectSQLContext;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.exception.SQLParserException;

import java.util.List;

public class MySqlExprParser extends SQLExprParser {
    
    public MySqlExprParser(final ShardingRule shardingRule, final List<Object> parameters, final String sql) {
        super(shardingRule, parameters, new MySqlLexer(sql));
        getLexer().nextToken();
    }
    
    public LimitContext parseLimit(final int parametersIndex, final SelectSQLContext sqlContext) {
        getLexer().skipIfEqual(Token.LIMIT);
        int valueIndex = -1;
        int valueBeginPosition = getLexer().getCurrentPosition();
        int value;
        boolean isParameterForValue = false;
        if (getLexer().equalToken(Token.LITERAL_INT)) {
            value = Integer.parseInt(getLexer().getLiterals());
            valueBeginPosition = valueBeginPosition - (value + "").length();
        } else if (getLexer().equalToken(Token.QUESTION)) {
            valueIndex = parametersIndex;
            value = (int) getParameters().get(valueIndex);
            valueBeginPosition--;
            isParameterForValue = true;
        } else {
            throw new ParserException(getLexer());
        }
        getLexer().nextToken();
        if (getLexer().skipIfEqual(Token.COMMA)) {
            return getLimitContextWithComma(parametersIndex, sqlContext, valueIndex, valueBeginPosition, value, isParameterForValue);
        }
        if (getLexer().skipIfEqual(Token.OFFSET)) {
            return getLimitContextWithOffset(parametersIndex, sqlContext, valueIndex, valueBeginPosition, value, isParameterForValue);
        }
        if (!isParameterForValue) {
            sqlContext.getSqlTokens().add(new RowCountLimitToken(valueBeginPosition, value));
        }
        if (value < 0) {
            throw new SQLParserException("LIMIT offset and row count can not be a negative value");
        }
        return new LimitContext(value, valueIndex);
    }
    
    private LimitContext getLimitContextWithComma(final int parametersIndex, final SelectSQLContext sqlContext, final int valueIndex, final int valueBeginPosition, final int value, final boolean isParameterForValue) {
        int rowCountBeginPosition = getLexer().getCurrentPosition();
        int rowCount;
        int rowCountIndex = -1;
        boolean isParameterForRowCount = false;
        if (getLexer().equalToken(Token.LITERAL_INT)) {
            rowCount = Integer.parseInt(getLexer().getLiterals());
            rowCountBeginPosition = rowCountBeginPosition - (rowCount + "").length();
        } else if (getLexer().equalToken(Token.QUESTION)) {
            rowCountIndex = -1 == valueIndex ? parametersIndex : valueIndex + 1;
            rowCount = (int) getParameters().get(rowCountIndex);
            rowCountBeginPosition--;
            isParameterForRowCount = true;
        } else {
            throw new ParserException(getLexer());
        }
        getLexer().nextToken();
        if (!isParameterForValue) {
            sqlContext.getSqlTokens().add(new OffsetLimitToken(valueBeginPosition, value));
        }
        if (!isParameterForRowCount) {
            sqlContext.getSqlTokens().add(new RowCountLimitToken(rowCountBeginPosition, rowCount));
        }
        if (value < 0 || rowCount < 0) {
            throw new SQLParserException("LIMIT offset and row count can not be a negative value");
        }
        return new LimitContext(value, rowCount, valueIndex, rowCountIndex);
    }
    
    private LimitContext getLimitContextWithOffset(final int parametersIndex, final SelectSQLContext sqlContext, final int valueIndex, final int valueBeginPosition, final int value, final boolean isParameterForValue) {
        int offsetBeginPosition = getLexer().getCurrentPosition();
        int offset;
        int offsetIndex = -1;
        boolean isParameterForOffset = false;
        if (getLexer().equalToken(Token.LITERAL_INT)) {
            offset = Integer.parseInt(getLexer().getLiterals());
            offsetBeginPosition = offsetBeginPosition - (offset + "").length();
        } else if (getLexer().equalToken(Token.QUESTION)) {
            offsetIndex = -1 == valueIndex ? parametersIndex : valueIndex + 1;
            offset = (int) getParameters().get(offsetIndex);
            offsetBeginPosition--;
            isParameterForOffset = true;
        } else {
            throw new ParserException(getLexer());
        }
        getLexer().nextToken();
        if (!isParameterForOffset) {
            sqlContext.getSqlTokens().add(new OffsetLimitToken(offsetBeginPosition, offset));
        }
        if (!isParameterForValue) {
            sqlContext.getSqlTokens().add(new RowCountLimitToken(valueBeginPosition, value));
        }
        if (value < 0 || offset < 0) {
            throw new SQLParserException("LIMIT offset and row count can not be a negative value");
        }
        return new LimitContext(offset, value, offsetIndex, valueIndex);
    }
}
