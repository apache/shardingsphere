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
import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.sqlserver.SQLServerKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Literals;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.AbstractSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.CommonParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.Limit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.LimitValue;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.CommonSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLNumberExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPlaceholderExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.AbstractSelectParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.RowCountToken;

/**
 * SQLServer Select语句解析器.
 *
 * @author zhangliang
 */
public final class SQLServerSelectParser extends AbstractSelectParser {
    
    public SQLServerSelectParser(final ShardingRule shardingRule, final CommonParser commonParser, final AbstractSQLParser sqlParser) {
        super(shardingRule, commonParser, sqlParser, new SQLServerWhereSQLParser(commonParser));
    }
    
    @Override
    protected void parseInternal(final SelectStatement selectStatement) {
        parseDistinct();
        parseTop(selectStatement);
        parseSelectList(selectStatement);
        parseFrom(selectStatement);
        parseWhere(selectStatement);
        parseGroupBy(selectStatement);
        parseHaving();
        parseOrderBy(selectStatement);
        parseOffset(selectStatement);
        parseFor();
    }
    
    private void parseTop(final SelectStatement selectStatement) {
        if (!getCommonParser().skipIfEqual(SQLServerKeyword.TOP)) {
            return;
        }
        int beginPosition = getCommonParser().getLexer().getCurrentToken().getEndPosition();
        if (!getCommonParser().skipIfEqual(Symbol.LEFT_PAREN)) {
            beginPosition = getCommonParser().getLexer().getCurrentToken().getEndPosition() - getCommonParser().getLexer().getCurrentToken().getLiterals().length();
        }
        SQLExpression sqlExpression = getExpressionSQLParser().parse(selectStatement);
        getCommonParser().skipIfEqual(Symbol.RIGHT_PAREN);
        LimitValue rowCountValue;
        if (sqlExpression instanceof SQLNumberExpression) {
            int rowCount = ((SQLNumberExpression) sqlExpression).getNumber().intValue();
            rowCountValue = new LimitValue(rowCount, -1);
            selectStatement.getSqlTokens().add(new RowCountToken(beginPosition, rowCount));
        } else if (sqlExpression instanceof SQLPlaceholderExpression) {
            rowCountValue = new LimitValue(-1, ((SQLPlaceholderExpression) sqlExpression).getIndex());
        } else {
            throw new SQLParsingException(getCommonParser().getLexer());
        }
        if (getCommonParser().equalAny(SQLServerKeyword.PERCENT)) {
            throw new SQLParsingUnsupportedException(SQLServerKeyword.PERCENT);
        }
        getCommonParser().skipIfEqual(DefaultKeyword.WITH, SQLServerKeyword.TIES);
        if (null == selectStatement.getLimit()) {
            Limit limit = new Limit(false);
            limit.setRowCount(rowCountValue);
            selectStatement.setLimit(limit);
        } else {
            selectStatement.getLimit().setRowCount(rowCountValue);
        }
    }
    
    private void parseOffset(final SelectStatement selectStatement) {
        if (!getCommonParser().skipIfEqual(SQLServerKeyword.OFFSET)) {
            return;
        }
        int offsetValue = -1;
        int offsetIndex = -1;
        if (getCommonParser().equalAny(Literals.INT)) {
            offsetValue = Integer.parseInt(getCommonParser().getLexer().getCurrentToken().getLiterals());
        } else if (getCommonParser().equalAny(Symbol.QUESTION)) {
            offsetIndex = getParametersIndex();
            selectStatement.increaseParametersIndex();
        } else {
            throw new SQLParsingException(getCommonParser().getLexer());
        }
        getCommonParser().getLexer().nextToken();
        Limit limit = new Limit(true);
        if (getCommonParser().skipIfEqual(DefaultKeyword.FETCH)) {
            getCommonParser().getLexer().nextToken();
            int rowCountValue = -1;
            int rowCountIndex = -1;
            getCommonParser().getLexer().nextToken();
            if (getCommonParser().equalAny(Literals.INT)) {
                rowCountValue = Integer.parseInt(getCommonParser().getLexer().getCurrentToken().getLiterals());
            } else if (getCommonParser().equalAny(Symbol.QUESTION)) {
                rowCountIndex = getParametersIndex();
                selectStatement.increaseParametersIndex();
            } else {
                throw new SQLParsingException(getCommonParser().getLexer());
            }
            getCommonParser().getLexer().nextToken();
            getCommonParser().getLexer().nextToken();
            limit.setRowCount(new LimitValue(rowCountValue, rowCountIndex));
            limit.setOffset(new LimitValue(offsetValue, offsetIndex));
        } else {
            limit.setOffset(new LimitValue(offsetValue, offsetIndex));
        }
        selectStatement.setLimit(limit);
    }
    
    private void parseFor() {
        if (!getCommonParser().skipIfEqual(DefaultKeyword.FOR)) {
            return;
        }
        if (getCommonParser().equalAny(SQLServerKeyword.BROWSE)) {
            getCommonParser().getLexer().nextToken();
        } else if (getCommonParser().skipIfEqual(SQLServerKeyword.XML)) {
            while (true) {
                if (getCommonParser().equalAny(SQLServerKeyword.AUTO, SQLServerKeyword.TYPE, SQLServerKeyword.XMLSCHEMA)) {
                    getCommonParser().getLexer().nextToken();
                } else if (getCommonParser().skipIfEqual(SQLServerKeyword.ELEMENTS)) {
                    getCommonParser().skipIfEqual(SQLServerKeyword.XSINIL);
                } else {
                    break;
                }
                if (getCommonParser().equalAny(Symbol.COMMA)) {
                    getCommonParser().getLexer().nextToken();
                } else {
                    break;
                }
            }
        } else {
            throw new SQLParsingUnsupportedException(getCommonParser().getLexer().getCurrentToken().getType());
        }
    }
    
    @Override
    protected boolean isRowNumberSelectItem() {
        return getCommonParser().skipIfEqual(SQLServerKeyword.ROW_NUMBER);
    }
    
    @Override
    protected SelectItem parseRowNumberSelectItem(final SelectStatement selectStatement) {
        getCommonParser().skipParentheses(selectStatement);
        getCommonParser().accept(DefaultKeyword.OVER);
        getCommonParser().accept(Symbol.LEFT_PAREN);
        if (getCommonParser().equalAny(SQLServerKeyword.PARTITION)) {
            throw new SQLParsingUnsupportedException(SQLServerKeyword.PARTITION);
        }
        parseOrderBy(selectStatement);
        getCommonParser().accept(Symbol.RIGHT_PAREN);
        return new CommonSelectItem(SQLServerKeyword.ROW_NUMBER.name(), getAliasSQLParser().parse());
    }
    
    @Override
    protected void parseJoinTable(final SelectStatement selectStatement) {
        if (getCommonParser().skipIfEqual(DefaultKeyword.WITH)) {
            getCommonParser().skipParentheses(selectStatement);
        }
        super.parseJoinTable(selectStatement);
    }
    
    @Override
    protected OrderType getNullOrderType() {
        return OrderType.DESC;
    }
}
