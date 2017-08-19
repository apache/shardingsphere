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
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Literals;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.AbstractSQLParser;
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
    
    public SQLServerSelectParser(final ShardingRule shardingRule, final AbstractSQLParser sqlParser) {
        super(shardingRule, sqlParser);
    }
    
    @Override
    protected void parseBeforeSelectList(final SelectStatement selectStatement) {
        parseTop(selectStatement);
    }
    
    private void parseTop(final SelectStatement selectStatement) {
        if (!getSqlParser().skipIfEqual(SQLServerKeyword.TOP)) {
            return;
        }
        int beginPosition = getSqlParser().getLexer().getCurrentToken().getEndPosition();
        if (!getSqlParser().skipIfEqual(Symbol.LEFT_PAREN)) {
            beginPosition = getSqlParser().getLexer().getCurrentToken().getEndPosition() - getSqlParser().getLexer().getCurrentToken().getLiterals().length();
        }
        SQLExpression sqlExpression = getSqlParser().parseExpression();
        getSqlParser().skipIfEqual(Symbol.RIGHT_PAREN);
        LimitValue rowCountValue;
        if (sqlExpression instanceof SQLNumberExpression) {
            int rowCount = ((SQLNumberExpression) sqlExpression).getNumber().intValue();
            rowCountValue = new LimitValue(rowCount, -1);
            selectStatement.getSqlTokens().add(new RowCountToken(beginPosition, rowCount));
        } else if (sqlExpression instanceof SQLPlaceholderExpression) {
            rowCountValue = new LimitValue(-1, ((SQLPlaceholderExpression) sqlExpression).getIndex());
        } else {
            throw new SQLParsingException(getSqlParser().getLexer());
        }
        if (getSqlParser().equalAny(SQLServerKeyword.PERCENT)) {
            throw new SQLParsingUnsupportedException(SQLServerKeyword.PERCENT);
        }
        getSqlParser().skipIfEqual(DefaultKeyword.WITH, SQLServerKeyword.TIES);
        if (null == selectStatement.getLimit()) {
            Limit limit = new Limit(false);
            limit.setRowCount(rowCountValue);
            selectStatement.setLimit(limit);
        } else {
            selectStatement.getLimit().setRowCount(rowCountValue);
        }
    }
    
    @Override
    protected boolean isRowNumberSelectItem() {
        return getSqlParser().skipIfEqual(SQLServerKeyword.ROW_NUMBER);
    }
    
    @Override
    protected SelectItem parseRowNumberSelectItem(final SelectStatement selectStatement) {
        getSqlParser().skipParentheses();
        getSqlParser().accept(DefaultKeyword.OVER);
        getSqlParser().accept(Symbol.LEFT_PAREN);
        if (getSqlParser().equalAny(SQLServerKeyword.PARTITION)) {
            throw new SQLParsingUnsupportedException(SQLServerKeyword.PARTITION);
        }
        parseOrderBy(selectStatement);
        getSqlParser().accept(Symbol.RIGHT_PAREN);
        return new CommonSelectItem(SQLServerKeyword.ROW_NUMBER.name(), getSqlParser().parseAlias());
    }
    
    @Override
    protected void parseJoinTable(final SelectStatement selectStatement) {
        if (getSqlParser().skipIfEqual(DefaultKeyword.WITH)) {
            getSqlParser().skipParentheses();
        }
        super.parseJoinTable(selectStatement);
    }
    
    @Override
    protected void customizedSelect(final SelectStatement selectStatement) {
        if (getSqlParser().equalAny(DefaultKeyword.FOR)) {
            parseFor();
        }
        if (getSqlParser().equalAny(SQLServerKeyword.OFFSET)) {
            parseOffset(selectStatement);
        }
    }
    
    private void parseOffset(final SelectStatement selectStatement) {
        getSqlParser().getLexer().nextToken();
        int offsetValue = -1;
        int offsetIndex = -1;
        if (getSqlParser().equalAny(Literals.INT)) {
            offsetValue = Integer.parseInt(getSqlParser().getLexer().getCurrentToken().getLiterals());
        } else if (getSqlParser().equalAny(Symbol.QUESTION)) {
            offsetIndex = getParametersIndex();
            getSqlParser().increaseParametersIndex();
        } else {
            throw new SQLParsingException(getSqlParser().getLexer());
        }
        getSqlParser().getLexer().nextToken();
        Limit limit = new Limit(true);
        if (getSqlParser().skipIfEqual(DefaultKeyword.FETCH)) {
            getSqlParser().getLexer().nextToken();
            int rowCountValue = -1;
            int rowCountIndex = -1;
            getSqlParser().getLexer().nextToken();
            if (getSqlParser().equalAny(Literals.INT)) {
                rowCountValue = Integer.parseInt(getSqlParser().getLexer().getCurrentToken().getLiterals());
            } else if (getSqlParser().equalAny(Symbol.QUESTION)) {
                rowCountIndex = getParametersIndex();
                getSqlParser().increaseParametersIndex();
            } else {
                throw new SQLParsingException(getSqlParser().getLexer());
            }
            getSqlParser().getLexer().nextToken();
            getSqlParser().getLexer().nextToken();
            limit.setRowCount(new LimitValue(rowCountValue, rowCountIndex));
            limit.setOffset(new LimitValue(offsetValue, offsetIndex));
        } else {
            limit.setOffset(new LimitValue(offsetValue, offsetIndex));
        }
        selectStatement.setLimit(limit);
    }
    
    private void parseFor() {
        getSqlParser().getLexer().nextToken();
        if (getSqlParser().equalAny(SQLServerKeyword.BROWSE)) {
            getSqlParser().getLexer().nextToken();
        } else if (getSqlParser().skipIfEqual(SQLServerKeyword.XML)) {
            while (true) {
                if (getSqlParser().equalAny(SQLServerKeyword.AUTO, SQLServerKeyword.TYPE, SQLServerKeyword.XMLSCHEMA)) {
                    getSqlParser().getLexer().nextToken();
                } else if (getSqlParser().skipIfEqual(SQLServerKeyword.ELEMENTS)) {
                    getSqlParser().skipIfEqual(SQLServerKeyword.XSINIL);
                } else {
                    break;
                }
                if (getSqlParser().equalAny(Symbol.COMMA)) {
                    getSqlParser().getLexer().nextToken();
                } else {
                    break;
                }
            }
        } else {
            throw new SQLParsingUnsupportedException(getSqlParser().getLexer().getCurrentToken().getType());
        }
    }
}
