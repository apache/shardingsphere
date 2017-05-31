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
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.Lexer;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Literals;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.Column;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Condition;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.table.Table;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.table.Tables;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLIdentifierExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLIgnoreExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLNumberExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPlaceholderExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPropertyExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLTextExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * SQL解析器.
 *
 * @author zhangliang
 */
@Getter
@Setter
public class SQLParser extends AbstractParser {
    
    private final ShardingRule shardingRule;
    
    private int parametersIndex;
    
    public SQLParser(final Lexer lexer, final ShardingRule shardingRule) {
        super(lexer);
        this.shardingRule = shardingRule;
    }
    
    /**
     * 解析表达式.
     *
     * @param sqlStatement SQL语句对象
     * @return 表达式
     */
    public final SQLExpression parseExpression(final SQLStatement sqlStatement) {
        int beginPosition = getLexer().getCurrentToken().getEndPosition();
        SQLExpression result = parseExpression();
        if (result instanceof SQLPropertyExpression) {
            setTableToken(sqlStatement, beginPosition, (SQLPropertyExpression) result);
        }
        return result;
    }
    
    /**
     * 解析表达式.
     *
     * @return 表达式
     */
    public final SQLExpression parseExpression() {
        String literals = getLexer().getCurrentToken().getLiterals();
        final SQLExpression expression = getExpression(literals);
        if (skipIfEqual(Literals.IDENTIFIER)) {
            if (skipIfEqual(Symbol.DOT)) {
                String property = getLexer().getCurrentToken().getLiterals();
                getLexer().nextToken();
                return skipIfCompositeExpression() ? new SQLIgnoreExpression() : new SQLPropertyExpression(new SQLIdentifierExpression(literals), property);
            }
            if (equalAny(Symbol.LEFT_PAREN)) {
                skipParentheses();
                skipRestCompositeExpression();
                return new SQLIgnoreExpression();
            }
            return skipIfCompositeExpression() ? new SQLIgnoreExpression() : expression;
        }
        getLexer().nextToken();
        return skipIfCompositeExpression() ? new SQLIgnoreExpression() : expression;
    }
    
    private SQLExpression getExpression(final String literals) {
        if (equalAny(Symbol.QUESTION)) {
            parametersIndex++;
            return new SQLPlaceholderExpression(parametersIndex - 1);
        }
        if (equalAny(Literals.CHARS)) {
            return new SQLTextExpression(literals);
        }
        // TODO 考虑long的情况
        if (equalAny(Literals.INT)) {
            return new SQLNumberExpression(Integer.parseInt(literals));
        }
        if (equalAny(Literals.FLOAT)) {
            return new SQLNumberExpression(Double.parseDouble(literals));
        }
        // TODO 考虑long的情况
        if (equalAny(Literals.HEX)) {
            return new SQLNumberExpression(Integer.parseInt(literals, 16));
        }
        if (equalAny(Literals.IDENTIFIER)) {
            return new SQLIdentifierExpression(SQLUtil.getExactlyValue(literals));
        }
        return new SQLIgnoreExpression();
    }
    
    private boolean skipIfCompositeExpression() {
        if (equalAny(Symbol.PLUS, Symbol.SUB, Symbol.STAR, Symbol.SLASH, Symbol.PERCENT, Symbol.AMP, Symbol.BAR, Symbol.DOUBLE_AMP, Symbol.DOUBLE_BAR, Symbol.CARET, Symbol.DOT, Symbol.LEFT_PAREN)) {
            skipParentheses();
            skipRestCompositeExpression();
            return true;
        }
        return false;
    }
    
    private void skipRestCompositeExpression() {
        while (skipIfEqual(Symbol.PLUS, Symbol.SUB, Symbol.STAR, Symbol.SLASH, Symbol.PERCENT, Symbol.AMP, Symbol.BAR, Symbol.DOUBLE_AMP, Symbol.DOUBLE_BAR, Symbol.CARET, Symbol.DOT)) {
            if (equalAny(Symbol.QUESTION)) {
                parametersIndex++;
            }
            getLexer().nextToken();
            skipParentheses();
        }
    }
    
    private void setTableToken(final SQLStatement sqlStatement, final int beginPosition, final SQLPropertyExpression propertyExpr) {
        String owner = propertyExpr.getOwner().getName();
        if (sqlStatement.getTables().getSingleTableName().equalsIgnoreCase(SQLUtil.getExactlyValue(owner))) {
            sqlStatement.getSqlTokens().add(new TableToken(beginPosition - owner.length(), owner));
        }
    }
    
    /**
     * 解析别名.
     *
     * @return 别名
     */
    public Optional<String> parseAlias() {
        if (skipIfEqual(DefaultKeyword.AS)) {
            if (equalAny(Symbol.values())) {
                return Optional.absent();
            }
            String result = SQLUtil.getExactlyValue(getLexer().getCurrentToken().getLiterals());
            getLexer().nextToken();
            return Optional.of(result);
        }
        // TODO 增加哪些数据库识别哪些关键字作为别名的配置
        if (equalAny(Literals.IDENTIFIER, Literals.CHARS, DefaultKeyword.USER, DefaultKeyword.END, DefaultKeyword.CASE, DefaultKeyword.KEY, DefaultKeyword.INTERVAL, DefaultKeyword.CONSTRAINT)) {
            String result = SQLUtil.getExactlyValue(getLexer().getCurrentToken().getLiterals());
            getLexer().nextToken();
            return Optional.of(result);
        }
        return Optional.absent();
    }
    
    /**
     * 解析单表.
     *
     * @param sqlStatement SQL语句对象
     */
    public final void parseSingleTable(final SQLStatement sqlStatement) {
        boolean hasParentheses = false;
        if (skipIfEqual(Symbol.LEFT_PAREN)) {
            if (equalAny(DefaultKeyword.SELECT)) {
                throw new UnsupportedOperationException("Cannot support subquery");
            }
            hasParentheses = true;
        }
        Table table;
        final int beginPosition = getLexer().getCurrentToken().getEndPosition() - getLexer().getCurrentToken().getLiterals().length();
        String literals = getLexer().getCurrentToken().getLiterals();
        getLexer().nextToken();
        if (skipIfEqual(Symbol.DOT)) {
            getLexer().nextToken();
            if (hasParentheses) {
                accept(Symbol.RIGHT_PAREN);
            }
            table = new Table(SQLUtil.getExactlyValue(literals), parseAlias());
        } else {
            if (hasParentheses) {
                accept(Symbol.RIGHT_PAREN);
            }
            table = new Table(SQLUtil.getExactlyValue(literals), parseAlias());
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
        if (skipIfEqual(DefaultKeyword.LEFT, DefaultKeyword.RIGHT, DefaultKeyword.FULL)) {
            skipIfEqual(DefaultKeyword.OUTER);
            accept(DefaultKeyword.JOIN);
            return true;
        } else if (skipIfEqual(DefaultKeyword.INNER)) {
            accept(DefaultKeyword.JOIN);
            return true;
        } else if (skipIfEqual(DefaultKeyword.JOIN, Symbol.COMMA, DefaultKeyword.STRAIGHT_JOIN)) {
            return true;
        } else if (skipIfEqual(DefaultKeyword.CROSS)) {
            if (skipIfEqual(DefaultKeyword.JOIN, DefaultKeyword.APPLY)) {
                return true;
            }
        } else if (skipIfEqual(DefaultKeyword.OUTER)) {
            if (skipIfEqual(DefaultKeyword.APPLY)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 解析查询条件.
     *
     * @param sqlStatement SQL语句对象
     */
    public final void parseWhere(final SQLStatement sqlStatement) {
        if (skipIfEqual(DefaultKeyword.WHERE)) {
            parseConditions(sqlStatement);
        }
    }
    
    private void parseConditions(final SQLStatement sqlStatement) {
        do {
            parseComparisonCondition(sqlStatement);
        } while (skipIfEqual(DefaultKeyword.AND));
        if (equalAny(DefaultKeyword.OR)) {
            throw new SQLParsingUnsupportedException(getLexer().getCurrentToken().getType());
        }
    }
    
    // TODO 解析组合expr
    public final void parseComparisonCondition(final SQLStatement sqlStatement) {
        skipIfEqual(Symbol.LEFT_PAREN);
        SQLExpression left = parseExpression(sqlStatement);
        if (equalAny(Symbol.EQ)) {
            parseEqualCondition(sqlStatement, left);
            return;
        }
        if (equalAny(DefaultKeyword.IN)) {
            parseInCondition(sqlStatement, left);
            return;
        }
        if (equalAny(DefaultKeyword.BETWEEN)) {
            parseBetweenCondition(sqlStatement, left);
            return;
        }
        if (equalAny(Symbol.LT) || equalAny(Symbol.GT) || equalAny(Symbol.LT_EQ) || equalAny(Symbol.GT_EQ)) {
            parserOtherCondition(sqlStatement);
        }
        skipIfEqual(Symbol.LEFT_PAREN);
    }
    
    private void parseEqualCondition(final SQLStatement sqlStatement, final SQLExpression left) {
        getLexer().nextToken();
        SQLExpression right = parseExpression(sqlStatement);
        // TODO 如果有多表,且找不到column是哪个表的,则不加入condition,以后需要解析binding table
        if ((sqlStatement.getTables().isSingleTable() || left instanceof SQLPropertyExpression)
                && (right instanceof SQLNumberExpression || right instanceof SQLTextExpression || right instanceof SQLPlaceholderExpression)) {
            Optional<Column> column = find(sqlStatement.getTables(), left);
            if (column.isPresent()) {
                sqlStatement.getConditions().add(new Condition(column.get(), right), shardingRule);
            }
        }
    }
    
    private void parseInCondition(final SQLStatement sqlStatement, final SQLExpression left) {
        getLexer().nextToken();
        accept(Symbol.LEFT_PAREN);
        List<SQLExpression> rights = new LinkedList<>();
        do {
            if (equalAny(Symbol.COMMA)) {
                getLexer().nextToken();
            }
            rights.add(parseExpression(sqlStatement));
        } while (!equalAny(Symbol.RIGHT_PAREN));
        Optional<Column> column = find(sqlStatement.getTables(), left);
        if (column.isPresent()) {
            sqlStatement.getConditions().add(new Condition(column.get(), rights), shardingRule);
        }
        getLexer().nextToken();
    }
    
    private void parseBetweenCondition(final SQLStatement sqlStatement, final SQLExpression left) {
        getLexer().nextToken();
        List<SQLExpression> rights = new LinkedList<>();
        rights.add(parseExpression(sqlStatement));
        accept(DefaultKeyword.AND);
        rights.add(parseExpression(sqlStatement));
        Optional<Column> column = find(sqlStatement.getTables(), left);
        if (column.isPresent()) {
            sqlStatement.getConditions().add(new Condition(column.get(), rights.get(0), rights.get(1)), shardingRule);
        }
    }
    
    private void parserOtherCondition(final SQLStatement sqlStatement) {
        getLexer().nextToken();
        parseExpression(sqlStatement);
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
