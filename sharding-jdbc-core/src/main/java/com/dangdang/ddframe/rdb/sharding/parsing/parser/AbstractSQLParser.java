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

package com.dangdang.ddframe.rdb.sharding.parsing.parser;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Column;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Condition;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.Limit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.LimitValue;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.table.Table;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.table.Tables;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLIdentifierExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLNumberExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPlaceholderExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPropertyExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLTextExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.AliasSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.ExpressionSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.OffsetToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.RowCountToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.Getter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL解析器.
 *
 * @author zhangliang
 */
public abstract class AbstractSQLParser {
    
    @Getter
    private final CommonParser commonParser;
    
    private final AliasSQLParser aliasSQLParser;
    
    private final ExpressionSQLParser expressionSQLParser;
    
    public AbstractSQLParser(final CommonParser commonParser) {
        this.commonParser = commonParser;
        aliasSQLParser = new AliasSQLParser(commonParser);
        expressionSQLParser = new ExpressionSQLParser(commonParser);
    }
    
    /**
     * 解析单表.
     *
     * @param sqlStatement SQL语句对象
     */
    public final void parseSingleTable(final SQLStatement sqlStatement) {
        boolean hasParentheses = false;
        if (commonParser.skipIfEqual(Symbol.LEFT_PAREN)) {
            if (commonParser.equalAny(DefaultKeyword.SELECT)) {
                throw new UnsupportedOperationException("Cannot support subquery");
            }
            hasParentheses = true;
        }
        Table table;
        final int beginPosition = commonParser.getLexer().getCurrentToken().getEndPosition() - commonParser.getLexer().getCurrentToken().getLiterals().length();
        String literals = commonParser.getLexer().getCurrentToken().getLiterals();
        commonParser.getLexer().nextToken();
        if (commonParser.skipIfEqual(Symbol.DOT)) {
            commonParser.getLexer().nextToken();
            if (hasParentheses) {
                commonParser.accept(Symbol.RIGHT_PAREN);
            }
            table = new Table(SQLUtil.getExactlyValue(literals), aliasSQLParser.parse());
        } else {
            if (hasParentheses) {
                commonParser.accept(Symbol.RIGHT_PAREN);
            }
            table = new Table(SQLUtil.getExactlyValue(literals), aliasSQLParser.parse());
        }
        if (skipJoin()) {
            throw new UnsupportedOperationException("Cannot support Multiple-Table.");
        }
        sqlStatement.getSqlTokens().add(new TableToken(beginPosition, literals));
        sqlStatement.getTables().add(table);
    }
    
    /**
     * 跳过表关联.
     *
     * @return 是否表关联.
     */
    public final boolean skipJoin() {
        if (commonParser.skipIfEqual(DefaultKeyword.LEFT, DefaultKeyword.RIGHT, DefaultKeyword.FULL)) {
            commonParser.skipIfEqual(DefaultKeyword.OUTER);
            commonParser.accept(DefaultKeyword.JOIN);
            return true;
        } else if (commonParser.skipIfEqual(DefaultKeyword.INNER)) {
            commonParser.accept(DefaultKeyword.JOIN);
            return true;
        } else if (commonParser.skipIfEqual(DefaultKeyword.JOIN, Symbol.COMMA, DefaultKeyword.STRAIGHT_JOIN)) {
            return true;
        } else if (commonParser.skipIfEqual(DefaultKeyword.CROSS)) {
            if (commonParser.skipIfEqual(DefaultKeyword.JOIN, DefaultKeyword.APPLY)) {
                return true;
            }
        } else if (commonParser.skipIfEqual(DefaultKeyword.OUTER)) {
            if (commonParser.skipIfEqual(DefaultKeyword.APPLY)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 解析查询条件.
     *
     * @param shardingRule 分库分表规则配置
     * @param sqlStatement SQL语句对象
     */
    public final void parseWhere(final ShardingRule shardingRule, final SQLStatement sqlStatement) {
        parseWhere(shardingRule, sqlStatement, Collections.<SelectItem>emptyList());
    }
    
    /**
     * 解析查询条件.
     *
     * @param sqlStatement SQL语句对象
     * @param items 选择项集合
     */
    public final void parseWhere(final ShardingRule shardingRule, final SQLStatement sqlStatement, final List<SelectItem> items) {
        aliasSQLParser.parse();
        if (commonParser.skipIfEqual(DefaultKeyword.WHERE)) {
            parseConditions(shardingRule, sqlStatement, items);
        }
    }
    
    private void parseConditions(final ShardingRule shardingRule, final SQLStatement sqlStatement, final List<SelectItem> items) {
        do {
            parseComparisonCondition(shardingRule, sqlStatement, items);
        } while (commonParser.skipIfEqual(DefaultKeyword.AND));
        if (commonParser.equalAny(DefaultKeyword.OR)) {
            throw new SQLParsingUnsupportedException(commonParser.getLexer().getCurrentToken().getType());
        }
    }
    
    // TODO 解析组合expr
    public final void parseComparisonCondition(final ShardingRule shardingRule, final SQLStatement sqlStatement, final List<SelectItem> items) {
        commonParser.skipIfEqual(Symbol.LEFT_PAREN);
        SQLExpression left = expressionSQLParser.parse(sqlStatement);
        if (commonParser.equalAny(Symbol.EQ)) {
            parseEqualCondition(shardingRule, sqlStatement, left);
            commonParser.skipIfEqual(Symbol.RIGHT_PAREN);
            return;
        }
        if (commonParser.equalAny(DefaultKeyword.IN)) {
            parseInCondition(shardingRule, sqlStatement, left);
            commonParser.skipIfEqual(Symbol.RIGHT_PAREN);
            return;
        }
        if (commonParser.equalAny(DefaultKeyword.BETWEEN)) {
            parseBetweenCondition(shardingRule, sqlStatement, left);
            commonParser.skipIfEqual(Symbol.RIGHT_PAREN);
            return;
        }
        if (commonParser.equalAny(Symbol.LT, Symbol.GT, Symbol.LT_EQ, Symbol.GT_EQ)) {
            if (left instanceof SQLIdentifierExpression && sqlStatement instanceof SelectStatement
                    && isRowNumberCondition(items, ((SQLIdentifierExpression) left).getName())) {
                parseRowNumberCondition((SelectStatement) sqlStatement);
            } else if (left instanceof SQLPropertyExpression && sqlStatement instanceof SelectStatement
                    && isRowNumberCondition(items, ((SQLPropertyExpression) left).getName())) {
                parseRowNumberCondition((SelectStatement) sqlStatement);
            } else {
                parseOtherCondition(sqlStatement);
            }
        } else if (commonParser.equalAny(Symbol.LT_GT, DefaultKeyword.LIKE)) {
            parseOtherCondition(sqlStatement);
        }
        commonParser.skipIfEqual(Symbol.RIGHT_PAREN);
    }
    
    private void parseEqualCondition(final ShardingRule shardingRule, final SQLStatement sqlStatement, final SQLExpression left) {
        commonParser.getLexer().nextToken();
        SQLExpression right = expressionSQLParser.parse(sqlStatement);
        // TODO 如果有多表,且找不到column是哪个表的,则不加入condition,以后需要解析binding table
        if ((sqlStatement.getTables().isSingleTable() || left instanceof SQLPropertyExpression)
                && (right instanceof SQLNumberExpression || right instanceof SQLTextExpression || right instanceof SQLPlaceholderExpression)) {
            Optional<Column> column = find(sqlStatement.getTables(), left);
            if (column.isPresent()) {
                sqlStatement.getConditions().add(new Condition(column.get(), right), shardingRule);
            }
        }
    }
    
    private void parseInCondition(final ShardingRule shardingRule, final SQLStatement sqlStatement, final SQLExpression left) {
        commonParser.getLexer().nextToken();
        commonParser.accept(Symbol.LEFT_PAREN);
        List<SQLExpression> rights = new LinkedList<>();
        do {
            if (commonParser.equalAny(Symbol.COMMA)) {
                commonParser.getLexer().nextToken();
            }
            rights.add(expressionSQLParser.parse(sqlStatement));
        } while (!commonParser.equalAny(Symbol.RIGHT_PAREN));
        Optional<Column> column = find(sqlStatement.getTables(), left);
        if (column.isPresent()) {
            sqlStatement.getConditions().add(new Condition(column.get(), rights), shardingRule);
        }
        commonParser.getLexer().nextToken();
    }
    
    private void parseBetweenCondition(final ShardingRule shardingRule, final SQLStatement sqlStatement, final SQLExpression left) {
        commonParser.getLexer().nextToken();
        List<SQLExpression> rights = new LinkedList<>();
        rights.add(expressionSQLParser.parse(sqlStatement));
        commonParser.accept(DefaultKeyword.AND);
        rights.add(expressionSQLParser.parse(sqlStatement));
        Optional<Column> column = find(sqlStatement.getTables(), left);
        if (column.isPresent()) {
            sqlStatement.getConditions().add(new Condition(column.get(), rights.get(0), rights.get(1)), shardingRule);
        }
    }
    
    protected boolean isRowNumberCondition(final List<SelectItem> items, final String columnLabel) {
        return false;
    }
    
    private void parseRowNumberCondition(final SelectStatement selectStatement) {
        Symbol symbol = (Symbol) commonParser.getLexer().getCurrentToken().getType();
        commonParser.getLexer().nextToken();
        SQLExpression sqlExpression = expressionSQLParser.parse(selectStatement);
        if (null == selectStatement.getLimit()) {
            selectStatement.setLimit(new Limit(false));
        }
        if (Symbol.LT == symbol || Symbol.LT_EQ == symbol) {
            if (sqlExpression instanceof SQLNumberExpression) {
                int rowCount = ((SQLNumberExpression) sqlExpression).getNumber().intValue();
                selectStatement.getLimit().setRowCount(new LimitValue(rowCount, -1));
                selectStatement.getSqlTokens().add(new RowCountToken(
                        commonParser.getLexer().getCurrentToken().getEndPosition() - String.valueOf(rowCount).length() - commonParser.getLexer().getCurrentToken().getLiterals().length(), rowCount));
            } else if (sqlExpression instanceof SQLPlaceholderExpression) {
                selectStatement.getLimit().setRowCount(new LimitValue(-1, ((SQLPlaceholderExpression) sqlExpression).getIndex()));
            }
        } else if (Symbol.GT == symbol || Symbol.GT_EQ == symbol) {
            if (sqlExpression instanceof SQLNumberExpression) {
                int offset = ((SQLNumberExpression) sqlExpression).getNumber().intValue();
                selectStatement.getLimit().setOffset(new LimitValue(offset, -1));
                selectStatement.getSqlTokens().add(new OffsetToken(
                        commonParser.getLexer().getCurrentToken().getEndPosition() - String.valueOf(offset).length() - commonParser.getLexer().getCurrentToken().getLiterals().length(), offset));
            } else if (sqlExpression instanceof SQLPlaceholderExpression) {
                selectStatement.getLimit().setOffset(new LimitValue(-1, ((SQLPlaceholderExpression) sqlExpression).getIndex()));
            }
        }
    }
    
    private void parseOtherCondition(final SQLStatement sqlStatement) {
        commonParser.getLexer().nextToken();
        expressionSQLParser.parse(sqlStatement);
    }
    
    private Optional<Column> find(final Tables tables, final SQLExpression sqlExpression) {
        if (sqlExpression instanceof SQLPropertyExpression) {
            return getColumnWithOwner(tables, (SQLPropertyExpression) sqlExpression);
        }
        if (sqlExpression instanceof SQLIdentifierExpression) {
            return getColumnWithoutOwner(tables, (SQLIdentifierExpression) sqlExpression);
        }
        return Optional.absent();
    }
    
    private Optional<Column> getColumnWithOwner(final Tables tables, final SQLPropertyExpression propertyExpression) {
        Optional<Table> table = tables.find(SQLUtil.getExactlyValue((propertyExpression.getOwner()).getName()));
        return propertyExpression.getOwner() instanceof SQLIdentifierExpression && table.isPresent()
                ? Optional.of(new Column(SQLUtil.getExactlyValue(propertyExpression.getName()), table.get().getName())) : Optional.<Column>absent();
    }
    
    private Optional<Column> getColumnWithoutOwner(final Tables tables, final SQLIdentifierExpression identifierExpression) {
        return tables.isSingleTable() ? Optional.of(new Column(SQLUtil.getExactlyValue(identifierExpression.getName()), tables.getSingleTableName())) : Optional.<Column>absent();
    }
}
