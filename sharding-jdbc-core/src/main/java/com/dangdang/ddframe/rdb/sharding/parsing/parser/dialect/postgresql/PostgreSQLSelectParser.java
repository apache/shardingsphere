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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.postgresql.PostgreSQLKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Literals;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.Limit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.OffsetLimit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.RowCountLimit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.AbstractSelectParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.OffsetLimitToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.RowCountLimitToken;

import static com.dangdang.ddframe.rdb.sharding.util.NumberUtil.roundHalfUp;

public class PostgreSQLSelectParser extends AbstractSelectParser {
    
    public PostgreSQLSelectParser(final SQLParser sqlParser) {
        super(sqlParser);
    }
    
    @Override
    public void query() {
        if (getSqlParser().skipIfEqual(DefaultKeyword.SELECT)) {
            parseDistinct();
            parseSelectList();
            if (getSqlParser().skipIfEqual(DefaultKeyword.INTO)) {
                getSqlParser().skipIfEqual(PostgreSQLKeyword.TEMPORARY, PostgreSQLKeyword.TEMP, PostgreSQLKeyword.UNLOGGED);
                getSqlParser().skipIfEqual(DefaultKeyword.TABLE);
                // TODO
                // getSqlParser().name();
            }
        }
        parseFrom();
        parseWhere();
        parseGroupBy();
        if (getSqlParser().equalAny(PostgreSQLKeyword.WINDOW)) {
            throw new SQLParsingUnsupportedException(PostgreSQLKeyword.WINDOW);
        }
        getSelectStatement().getOrderByList().addAll(parseOrderBy());
        parseLimit();
        if (getSqlParser().skipIfEqual(DefaultKeyword.FETCH)) {
            throw new SQLParsingUnsupportedException(DefaultKeyword.FETCH);
        }
        if (getSqlParser().skipIfEqual(DefaultKeyword.FOR)) {
            getSqlParser().skipIfEqual(DefaultKeyword.UPDATE, PostgreSQLKeyword.SHARE);
            if (getSqlParser().equalAny(PostgreSQLKeyword.OF)) {
                throw new SQLParsingUnsupportedException(PostgreSQLKeyword.OF);
            }
            getSqlParser().skipIfEqual(PostgreSQLKeyword.NOWAIT);
        }
        queryRest();
    }
    
    private void parseLimit() {
        int rowCount = -1;
        int offset = -1;
        int rowCountParameterIndex = -1;
        int offsetParameterIndex = -1;
        boolean hasLimit = false;
        boolean hasOffset = false;
        while (true) {
            int parameterIndex = getParametersIndex();
            if (getSqlParser().skipIfEqual(PostgreSQLKeyword.LIMIT)) {
                hasLimit = true;
                int valueBeginPosition = getSqlParser().getLexer().getCurrentToken().getEndPosition();
                if (getSqlParser().equalAny(DefaultKeyword.ALL)) {
                    getSqlParser().getLexer().nextToken();
                } else {
                    if (getSqlParser().equalAny(Literals.INT, Literals.FLOAT)) {
                        rowCount = roundHalfUp(getSqlParser().getLexer().getCurrentToken().getLiterals());
                        valueBeginPosition = valueBeginPosition - (rowCount + "").length();
                        getSelectStatement().getSqlTokens().add(new RowCountLimitToken(valueBeginPosition, rowCount));
                    } else if (getSqlParser().equalAny(Symbol.QUESTION)) {
                        rowCountParameterIndex = parameterIndex++;
                        setParametersIndex(parameterIndex);
                        rowCount = -1;
                    } else {
                        throw new SQLParsingException(getSqlParser().getLexer());
                    }
                    getSqlParser().getLexer().nextToken();
                }
            } else if (getSqlParser().skipIfEqual(PostgreSQLKeyword.OFFSET)) {
                hasOffset = true;
                int offsetBeginPosition = getSqlParser().getLexer().getCurrentToken().getEndPosition();
                if (getSqlParser().equalAny(Literals.INT, Literals.FLOAT)) {
                    offset = roundHalfUp(getSqlParser().getLexer().getCurrentToken().getLiterals());
                    offsetBeginPosition = offsetBeginPosition - (offset + "").length();
                    getSelectStatement().getSqlTokens().add(new OffsetLimitToken(offsetBeginPosition, offset));
                } else if (getSqlParser().equalAny(Symbol.QUESTION)) {
                    offsetParameterIndex = parameterIndex++;
                    setParametersIndex(parameterIndex);
                    offset = -1;
                } else {
                    throw new SQLParsingException(getSqlParser().getLexer());
                }
                getSqlParser().getLexer().nextToken();
                getSqlParser().skipIfEqual(PostgreSQLKeyword.ROW, PostgreSQLKeyword.ROWS);
            } else {
                break;
            }
        }
        if (hasLimit || hasOffset) {
            Limit limit;
            // TODO 需处理只有OFFSET情况
            if (hasOffset && hasLimit) {
                limit = new Limit(new OffsetLimit(offset, offsetParameterIndex), new RowCountLimit(rowCount, rowCountParameterIndex));
            } else {
                limit = new Limit(new RowCountLimit(rowCount, rowCountParameterIndex));
            }
            getSelectStatement().setLimit(limit);
        }
    }
    
    protected boolean hasDistinctOn() {
        return true;
    }
}
