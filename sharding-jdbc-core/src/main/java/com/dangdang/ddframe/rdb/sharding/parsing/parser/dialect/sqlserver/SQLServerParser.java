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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.sqlserver.SQLServerKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.sqlserver.SQLServerLexer;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Literals;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.Limit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.LimitValue;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLNumberExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPlaceholderExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.SelectStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.RowCountToken;
import com.google.common.base.Optional;

/**
 * SQLServer解析器.
 *
 * @author zhangliang
 */
public final class SQLServerParser extends SQLParser {
    
    public SQLServerParser(final String sql, final ShardingRule shardingRule) {
        super(new SQLServerLexer(sql), shardingRule);
        getLexer().nextToken();
    }
    
    @Override
    protected boolean isRowNumberCondition(final SelectStatement selectStatement, final String columnLabel) {
        Optional<String> rowNumberAlias = Optional.absent();
        for (SelectItem each : selectStatement.getItems()) {
            if (each.getAlias().isPresent() && "ROW_NUMBER".equalsIgnoreCase(each.getExpression())) {
                rowNumberAlias = each.getAlias();
            }
        }
        return columnLabel.equalsIgnoreCase(rowNumberAlias.orNull());
    }
    
    /**
     * 解析TOP.
     * 
     * @param selectStatement SQL语句对象
     */
    public void parseTop(final SelectStatement selectStatement) {
        if (skipIfEqual(SQLServerKeyword.TOP)) {
            int beginPosition = getLexer().getCurrentToken().getEndPosition();
            if (!skipIfEqual(Symbol.LEFT_PAREN)) {
                beginPosition = getLexer().getCurrentToken().getEndPosition() - getLexer().getCurrentToken().getLiterals().length();
            }
            SQLExpression sqlExpression = parseExpression();
            skipIfEqual(Symbol.RIGHT_PAREN);
            LimitValue rowCountValue;
            if (sqlExpression instanceof SQLNumberExpression) {
                int rowCount = ((SQLNumberExpression) sqlExpression).getNumber().intValue();
                rowCountValue = new LimitValue(rowCount, -1);
                selectStatement.getSqlTokens().add(new RowCountToken(beginPosition, rowCount));
            } else if (sqlExpression instanceof SQLPlaceholderExpression) {
                rowCountValue = new LimitValue(-1, ((SQLPlaceholderExpression) sqlExpression).getIndex());
            } else {
                throw new SQLParsingException(getLexer());
            }
            if (skipIfEqual(SQLServerKeyword.PERCENT)) {
                return;
            }
            if (null == selectStatement.getLimit()) {
                Limit limit = new Limit(false);
                limit.setRowCount(rowCountValue);
                selectStatement.setLimit(limit);
            } else {
                selectStatement.getLimit().setRowCount(rowCountValue);
            }
        }
    }
    
    protected void skipOutput() {
        if (equalAny(SQLServerKeyword.OUTPUT)) {
            throw new SQLParsingUnsupportedException(SQLServerKeyword.OUTPUT);
        }
    }
    
    public void parseOffset(final SelectStatement selectStatement) {
        getLexer().nextToken();
        int offsetValue = -1;
        int offsetIndex = -1;
        if (equalAny(Literals.INT)) {
            offsetValue = Integer.parseInt(getLexer().getCurrentToken().getLiterals());
        } else if (equalAny(Symbol.QUESTION)) {
            offsetIndex = getParametersIndex();
            increaseParametersIndex();
        } else {
            throw new SQLParsingException(getLexer());
        }
        getLexer().nextToken();
        Limit limit = new Limit(true);
        if (skipIfEqual(DefaultKeyword.FETCH)) {
            getLexer().nextToken();
            int rowCountValue = -1;
            int rowCountIndex = -1;
            getLexer().nextToken();
            if (equalAny(Literals.INT)) {
                rowCountValue = Integer.parseInt(getLexer().getCurrentToken().getLiterals());
            } else if (equalAny(Symbol.QUESTION)) {
                rowCountIndex = getParametersIndex();
                increaseParametersIndex();
            } else {
                throw new SQLParsingException(getLexer());
            }
            getLexer().nextToken();
            getLexer().nextToken();
            limit.setRowCount(new LimitValue(rowCountValue, rowCountIndex));
            limit.setOffset(new LimitValue(offsetValue, offsetIndex));
        } else {
            limit.setOffset(new LimitValue(offsetValue, offsetIndex));
        }
        selectStatement.setLimit(limit);
    }
}
