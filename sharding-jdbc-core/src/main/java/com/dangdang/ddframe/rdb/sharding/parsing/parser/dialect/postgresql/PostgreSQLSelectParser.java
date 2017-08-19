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

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.postgresql.PostgreSQLKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Literals;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.AbstractSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.Limit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.LimitValue;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.AbstractSelectParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.OffsetToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.RowCountToken;
import com.dangdang.ddframe.rdb.sharding.util.NumberUtil;
import com.google.common.base.Optional;

/**
 * PostgreSQL Select语句解析器.
 *
 * @author zhangliang
 */
public final class PostgreSQLSelectParser extends AbstractSelectParser {
    
    public PostgreSQLSelectParser(final ShardingRule shardingRule, final AbstractSQLParser sqlParser) {
        super(shardingRule, sqlParser);
    }
    
    @Override
    protected void customizedSelect(final SelectStatement selectStatement) {
        if (getSqlParser().equalAny(PostgreSQLKeyword.WINDOW)) {
            throw new SQLParsingUnsupportedException(PostgreSQLKeyword.WINDOW);
        }
        parseLimit(selectStatement);
        if (getSqlParser().skipIfEqual(DefaultKeyword.FETCH)) {
            throw new SQLParsingUnsupportedException(DefaultKeyword.FETCH);
        }
        if (getSqlParser().skipIfEqual(DefaultKeyword.FOR)) {
            getSqlParser().skipIfEqual(DefaultKeyword.UPDATE, PostgreSQLKeyword.SHARE);
            if (getSqlParser().equalAny(DefaultKeyword.OF)) {
                throw new SQLParsingUnsupportedException(DefaultKeyword.OF);
            }
            getSqlParser().skipIfEqual(PostgreSQLKeyword.NOWAIT);
        }
    }
    
    private void parseLimit(final SelectStatement selectStatement) {
        Optional<LimitValue> offset = Optional.absent();
        Optional<LimitValue> rowCount = Optional.absent();
        while (true) {
            if (getSqlParser().skipIfEqual(PostgreSQLKeyword.LIMIT)) {
                rowCount = buildRowCount(selectStatement);
            } else if (getSqlParser().skipIfEqual(PostgreSQLKeyword.OFFSET)) {
                offset = buildOffset(selectStatement);
            } else {
                break;
            }
        }
        if (offset.isPresent() || rowCount.isPresent()) {
            setLimit(offset, rowCount, selectStatement);
        }
    }
    
    private Optional<LimitValue> buildRowCount(final SelectStatement selectStatement) {
        int parameterIndex = getParametersIndex();
        int rowCountValue = -1;
        int rowCountIndex = -1;
        int valueBeginPosition = getSqlParser().getLexer().getCurrentToken().getEndPosition();
        if (getSqlParser().equalAny(DefaultKeyword.ALL)) {
            getSqlParser().getLexer().nextToken();
        } else {
            if (getSqlParser().equalAny(Literals.INT, Literals.FLOAT)) {
                rowCountValue = NumberUtil.roundHalfUp(getSqlParser().getLexer().getCurrentToken().getLiterals());
                valueBeginPosition = valueBeginPosition - (rowCountValue + "").length();
                selectStatement.getSqlTokens().add(new RowCountToken(valueBeginPosition, rowCountValue));
            } else if (getSqlParser().equalAny(Symbol.QUESTION)) {
                rowCountIndex = parameterIndex++;
                setParametersIndex(parameterIndex);
                rowCountValue = -1;
            } else {
                throw new SQLParsingException(getSqlParser().getLexer());
            }
            getSqlParser().getLexer().nextToken();
        }
        return Optional.of(new LimitValue(rowCountValue, rowCountIndex));
    }
    
    private Optional<LimitValue> buildOffset(final SelectStatement selectStatement) {
        int parameterIndex = getParametersIndex();
        int offsetValue = -1;
        int offsetIndex = -1;
        int offsetBeginPosition = getSqlParser().getLexer().getCurrentToken().getEndPosition();
        if (getSqlParser().equalAny(Literals.INT, Literals.FLOAT)) {
            offsetValue = NumberUtil.roundHalfUp(getSqlParser().getLexer().getCurrentToken().getLiterals());
            offsetBeginPosition = offsetBeginPosition - (offsetValue + "").length();
            selectStatement.getSqlTokens().add(new OffsetToken(offsetBeginPosition, offsetValue));
        } else if (getSqlParser().equalAny(Symbol.QUESTION)) {
            offsetIndex = parameterIndex++;
            setParametersIndex(parameterIndex);
        } else {
            throw new SQLParsingException(getSqlParser().getLexer());
        }
        getSqlParser().getLexer().nextToken();
        getSqlParser().skipIfEqual(DefaultKeyword.ROW, PostgreSQLKeyword.ROWS);
        return Optional.of(new LimitValue(offsetValue, offsetIndex));
    }
    
    private void setLimit(final Optional<LimitValue> offset, final Optional<LimitValue> rowCount, final SelectStatement selectStatement) {
        Limit limit = new Limit(true);
        if (offset.isPresent()) {
            limit.setOffset(offset.get());
        }
        if (rowCount.isPresent()) {
            limit.setRowCount(rowCount.get());
        }
        selectStatement.setLimit(limit);
    }
}
