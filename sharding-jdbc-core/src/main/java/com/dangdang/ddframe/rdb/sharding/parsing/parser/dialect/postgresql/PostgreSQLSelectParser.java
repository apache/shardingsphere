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
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.LimitValue;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.AbstractSelectParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.OffsetLimitToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.RowCountLimitToken;
import com.google.common.base.Optional;

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
        Optional<LimitValue> offsetLimit = Optional.absent();
        Optional<LimitValue> rowCountLimit = Optional.absent();
        while (true) {
            if (getSqlParser().skipIfEqual(PostgreSQLKeyword.LIMIT)) {
                rowCountLimit = buildRowCount();
            } else if (getSqlParser().skipIfEqual(PostgreSQLKeyword.OFFSET)) {
                offsetLimit = buildOffsetLimit();
            } else {
                break;
            }
        }
        setLimit(offsetLimit, rowCountLimit);
    }
    
    private Optional<LimitValue> buildRowCount() {
        int parameterIndex = getParametersIndex();
        int rowCount = -1;
        int rowCountParameterIndex = -1;
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
        return Optional.of(new LimitValue(rowCount, rowCountParameterIndex));
    }
    
    private Optional<LimitValue> buildOffsetLimit() {
        int parameterIndex = getParametersIndex();
        int offset = -1;
        int offsetParameterIndex = -1;
        int offsetBeginPosition = getSqlParser().getLexer().getCurrentToken().getEndPosition();
        if (getSqlParser().equalAny(Literals.INT, Literals.FLOAT)) {
            offset = roundHalfUp(getSqlParser().getLexer().getCurrentToken().getLiterals());
            offsetBeginPosition = offsetBeginPosition - (offset + "").length();
            getSelectStatement().getSqlTokens().add(new OffsetLimitToken(offsetBeginPosition, offset));
        } else if (getSqlParser().equalAny(Symbol.QUESTION)) {
            offsetParameterIndex = parameterIndex++;
            setParametersIndex(parameterIndex);
        } else {
            throw new SQLParsingException(getSqlParser().getLexer());
        }
        getSqlParser().getLexer().nextToken();
        getSqlParser().skipIfEqual(PostgreSQLKeyword.ROW, PostgreSQLKeyword.ROWS);
        return Optional.of(new LimitValue(offset, offsetParameterIndex));
    }
    
    
    private void setLimit(final Optional<LimitValue> offsetLimit, final Optional<LimitValue> rowCountLimit) {
        Limit limit = new Limit(true);
        if (offsetLimit.isPresent()) {
            limit.setOffsetLimit(offsetLimit.get());
        }
        if (rowCountLimit.isPresent()) {
            limit.setRowCountLimit(rowCountLimit.get());
        }
        getSelectStatement().setLimit(limit);
    }
    
    protected boolean hasDistinctOn() {
        return true;
    }
}
