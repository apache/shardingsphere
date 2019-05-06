/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.parser.clause;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Keyword;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.parser.clause.condition.NullCondition;
import io.shardingsphere.core.parsing.parser.clause.expression.AliasExpressionParser;
import io.shardingsphere.core.parsing.parser.clause.expression.BasicExpressionParser;
import io.shardingsphere.core.parsing.parser.context.condition.AndCondition;
import io.shardingsphere.core.parsing.parser.context.condition.Column;
import io.shardingsphere.core.parsing.parser.context.condition.Condition;
import io.shardingsphere.core.parsing.parser.context.condition.OrCondition;
import io.shardingsphere.core.parsing.parser.context.limit.Limit;
import io.shardingsphere.core.parsing.parser.context.limit.LimitValue;
import io.shardingsphere.core.parsing.parser.context.selectitem.SelectItem;
import io.shardingsphere.core.parsing.parser.context.table.Table;
import io.shardingsphere.core.parsing.parser.context.table.Tables;
import io.shardingsphere.core.parsing.parser.dialect.ExpressionParserFactory;
import io.shardingsphere.core.parsing.parser.expression.SQLExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLIdentifierExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLPlaceholderExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLPropertyExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLTextExpression;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.parsing.parser.token.OffsetToken;
import io.shardingsphere.core.parsing.parser.token.RowCountToken;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.util.SQLUtil;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Where clause parser.
 *
 * @author zhangliang
 * @author maxiaoguang
 */
public abstract class WhereClauseParser implements SQLClauseParser {
    
    private final DatabaseType databaseType;
    
    private final LexerEngine lexerEngine;
    
    private final AliasExpressionParser aliasExpressionParser;
    
    private final BasicExpressionParser basicExpressionParser;
    
    public WhereClauseParser(final DatabaseType databaseType, final LexerEngine lexerEngine) {
        this.databaseType = databaseType;
        this.lexerEngine = lexerEngine;
        aliasExpressionParser = ExpressionParserFactory.createAliasExpressionParser(lexerEngine);
        basicExpressionParser = ExpressionParserFactory.createBasicExpressionParser(lexerEngine);
    }
    
    /**
     * Parse where.
     *
     * @param shardingRule databases and tables sharding rule
     * @param sqlStatement SQL statement
     * @param items select items
     */
    public void parse(final ShardingRule shardingRule, final SQLStatement sqlStatement, final List<SelectItem> items) {
        aliasExpressionParser.parseTableAlias();
        if (lexerEngine.skipIfEqual(DefaultKeyword.WHERE)) {
            parseWhere(shardingRule, sqlStatement, items);
        }
    }
    
    private void parseWhere(final ShardingRule shardingRule, final SQLStatement sqlStatement, final List<SelectItem> items) {
        OrCondition orCondition = parseOr(shardingRule, sqlStatement, items).optimize();
        if (1 != orCondition.getAndConditions().size() || !(orCondition.getAndConditions().get(0).getConditions().get(0) instanceof NullCondition)) {
            sqlStatement.getConditions().getOrCondition().getAndConditions().addAll(orCondition.getAndConditions());
        }
    }
    
    private OrCondition parseOr(final ShardingRule shardingRule, final SQLStatement sqlStatement, final List<SelectItem> items) {
        OrCondition result = new OrCondition();
        do {
            if (lexerEngine.skipIfEqual(Symbol.LEFT_PAREN)) {
                OrCondition subOrCondition = parseOr(shardingRule, sqlStatement, items);
                lexerEngine.skipIfEqual(Symbol.RIGHT_PAREN);
                OrCondition orCondition = null;
                if (lexerEngine.skipIfEqual(DefaultKeyword.AND)) {
                    orCondition = parseAnd(shardingRule, sqlStatement, items);
                }
                result.getAndConditions().addAll(merge(subOrCondition, orCondition).getAndConditions());
            } else {
                OrCondition orCondition = parseAnd(shardingRule, sqlStatement, items);
                result.getAndConditions().addAll(orCondition.getAndConditions());
            }
        } while (lexerEngine.skipIfEqual(DefaultKeyword.OR));
        return result;
    }
    
    private OrCondition parseAnd(final ShardingRule shardingRule, final SQLStatement sqlStatement, final List<SelectItem> items) {
        OrCondition result = new OrCondition();
        do {
            if (lexerEngine.skipIfEqual(Symbol.LEFT_PAREN)) {
                OrCondition subOrCondition = parseOr(shardingRule, sqlStatement, items);
                lexerEngine.skipIfEqual(Symbol.RIGHT_PAREN);
                result = merge(result, subOrCondition);
            } else {
                Condition condition = parseComparisonCondition(shardingRule, sqlStatement, items);
                skipsDoubleColon();
                result = merge(result, new OrCondition(condition));
            }
        } while (lexerEngine.skipIfEqual(DefaultKeyword.AND));
        return result;
    }
    
    private OrCondition merge(final OrCondition orCondition1, final OrCondition orCondition2) {
        if (null == orCondition1 || orCondition1.getAndConditions().isEmpty()) {
            return orCondition2;
        }
        if (null == orCondition2 || orCondition2.getAndConditions().isEmpty()) {
            return orCondition1;
        }
        OrCondition result = new OrCondition();
        for (AndCondition each1 : orCondition1.getAndConditions()) {
            for (AndCondition each2 : orCondition2.getAndConditions()) {
                result.getAndConditions().add(merge(each1, each2));
            }
        }
        return result;
    }
    
    private AndCondition merge(final AndCondition andCondition1, final AndCondition andCondition2) {
        AndCondition result = new AndCondition();
        for (Condition each : andCondition1.getConditions()) {
            result.getConditions().add(each);
        }
        for (Condition each : andCondition2.getConditions()) {
            result.getConditions().add(each);
        }
        return result.optimize();
    }
    
    private Condition parseComparisonCondition(final ShardingRule shardingRule, final SQLStatement sqlStatement, final List<SelectItem> items) {
        Condition result;
        SQLExpression left = basicExpressionParser.parse(sqlStatement);
        if (lexerEngine.skipIfEqual(Symbol.EQ)) {
            result = parseEqualCondition(shardingRule, sqlStatement, left);
            return result;
        }
        if (lexerEngine.skipIfEqual(DefaultKeyword.IN)) {
            result = parseInCondition(shardingRule, sqlStatement, left);
            return result;
        }
        if (lexerEngine.skipIfEqual(DefaultKeyword.BETWEEN)) {
            result = parseBetweenCondition(shardingRule, sqlStatement, left);
            return result;
        }
        result = new NullCondition();
        if (sqlStatement instanceof SelectStatement && isRowNumberCondition(items, left)) {
            if (lexerEngine.skipIfEqual(Symbol.LT)) {
                parseRowCountCondition((SelectStatement) sqlStatement, false);
                return result;
            }
            if (lexerEngine.skipIfEqual(Symbol.LT_EQ)) {
                parseRowCountCondition((SelectStatement) sqlStatement, true);
                return result;
            }
            if (lexerEngine.skipIfEqual(Symbol.GT)) {
                parseOffsetCondition((SelectStatement) sqlStatement, false);
                return result;
            }
            if (lexerEngine.skipIfEqual(Symbol.GT_EQ)) {
                parseOffsetCondition((SelectStatement) sqlStatement, true);
                return result;
            }
        }
        List<Keyword> otherConditionOperators = new LinkedList<>(Arrays.asList(getCustomizedOtherConditionOperators()));
        otherConditionOperators.addAll(
                Arrays.asList(Symbol.LT, Symbol.LT_EQ, Symbol.GT, Symbol.GT_EQ, Symbol.LT_GT, Symbol.BANG_EQ, Symbol.BANG_GT, Symbol.BANG_LT, DefaultKeyword.LIKE, DefaultKeyword.IS));
        if (lexerEngine.skipIfEqual(otherConditionOperators.toArray(new Keyword[otherConditionOperators.size()]))) {
            lexerEngine.skipIfEqual(DefaultKeyword.NOT);
            parseOtherCondition(sqlStatement);
        }
        if (lexerEngine.skipIfEqual(DefaultKeyword.NOT)) {
            parseNotCondition(sqlStatement);
        }
        return result;
    }
    
    private Condition parseEqualCondition(final ShardingRule shardingRule, final SQLStatement sqlStatement, final SQLExpression left) {
        SQLExpression right = basicExpressionParser.parse(sqlStatement);
        // TODO if have more tables, and cannot find column belong to, should not add to condition, should parse binding table rule.
        if (!sqlStatement.getTables().isSingleTable() && !(left instanceof SQLPropertyExpression)) {
            return new NullCondition();
        }
        if (right instanceof SQLNumberExpression || right instanceof SQLTextExpression || right instanceof SQLPlaceholderExpression) {
            Optional<Column> column = find(sqlStatement.getTables(), left);
            if (column.isPresent() && shardingRule.isShardingColumn(column.get())) {
                return new Condition(column.get(), right);
            }
        }
        return new NullCondition();
    }
    
    private Condition parseInCondition(final ShardingRule shardingRule, final SQLStatement sqlStatement, final SQLExpression left) {
        lexerEngine.accept(Symbol.LEFT_PAREN);
        boolean hasComplexExpression = false;
        List<SQLExpression> rights = new LinkedList<>();
        do {
            SQLExpression right = basicExpressionParser.parse(sqlStatement);
            rights.add(right);
            if (!(right instanceof SQLNumberExpression || right instanceof SQLTextExpression || right instanceof SQLPlaceholderExpression)) {
                hasComplexExpression = true;
            }
            skipsDoubleColon();
        } while (lexerEngine.skipIfEqual(Symbol.COMMA));
        lexerEngine.accept(Symbol.RIGHT_PAREN);
        if (!sqlStatement.getTables().isSingleTable() && !(left instanceof SQLPropertyExpression)) {
            return new NullCondition();
        }
        if (!hasComplexExpression) {
            Optional<Column> column = find(sqlStatement.getTables(), left);
            if (column.isPresent() && shardingRule.isShardingColumn(column.get())) {
                return new Condition(column.get(), rights);
            }
        }
        return new NullCondition();
    }
    
    private Condition parseBetweenCondition(final ShardingRule shardingRule, final SQLStatement sqlStatement, final SQLExpression left) {
        boolean hasComplexExpression = false;
        List<SQLExpression> rights = new LinkedList<>();
        SQLExpression right1 = basicExpressionParser.parse(sqlStatement);
        rights.add(right1);
        if (!(right1 instanceof SQLNumberExpression || right1 instanceof SQLTextExpression || right1 instanceof SQLPlaceholderExpression)) {
            hasComplexExpression = true;
        }
        skipsDoubleColon();
        lexerEngine.accept(DefaultKeyword.AND);
        SQLExpression right2 = basicExpressionParser.parse(sqlStatement);
        rights.add(right2);
        if (!(right2 instanceof SQLNumberExpression || right2 instanceof SQLTextExpression || right2 instanceof SQLPlaceholderExpression)) {
            hasComplexExpression = true;
        }
        if (!sqlStatement.getTables().isSingleTable() && !(left instanceof SQLPropertyExpression)) {
            return new NullCondition();
        }
        if (!hasComplexExpression) {
            Optional<Column> column = find(sqlStatement.getTables(), left);
            if (column.isPresent() && shardingRule.isShardingColumn(column.get())) {
                return new Condition(column.get(), rights.get(0), rights.get(1));
            }
        }
        return new NullCondition();
    }
    
    private boolean isRowNumberCondition(final List<SelectItem> items, final SQLExpression sqlExpression) {
        String columnLabel = null;
        if (sqlExpression instanceof SQLIdentifierExpression) {
            columnLabel = ((SQLIdentifierExpression) sqlExpression).getName();
        } else if (sqlExpression instanceof SQLPropertyExpression) {
            columnLabel = ((SQLPropertyExpression) sqlExpression).getName();
        }
        return null != columnLabel && isRowNumberCondition(items, columnLabel);
    }
    
    protected abstract boolean isRowNumberCondition(List<SelectItem> items, String columnLabel);
    
    private void parseRowCountCondition(final SelectStatement selectStatement, final boolean includeRowCount) {
        int endPosition = lexerEngine.getCurrentToken().getEndPosition();
        SQLExpression sqlExpression = basicExpressionParser.parse(selectStatement);
        if (null == selectStatement.getLimit()) {
            selectStatement.setLimit(new Limit());
        }
        if (sqlExpression instanceof SQLNumberExpression) {
            int rowCount = ((SQLNumberExpression) sqlExpression).getNumber().intValue();
            selectStatement.getLimit().setRowCount(new LimitValue(rowCount, -1, includeRowCount));
            selectStatement.addSQLToken(new RowCountToken(endPosition - String.valueOf(rowCount).length(), rowCount));
        } else if (sqlExpression instanceof SQLPlaceholderExpression) {
            selectStatement.getLimit().setRowCount(new LimitValue(-1, ((SQLPlaceholderExpression) sqlExpression).getIndex(), includeRowCount));
        }
    }
    
    private void parseOffsetCondition(final SelectStatement selectStatement, final boolean includeOffset) {
        SQLExpression sqlExpression = basicExpressionParser.parse(selectStatement);
        if (null == selectStatement.getLimit()) {
            selectStatement.setLimit(new Limit());
        }
        if (sqlExpression instanceof SQLNumberExpression) {
            int offset = ((SQLNumberExpression) sqlExpression).getNumber().intValue();
            selectStatement.getLimit().setOffset(new LimitValue(offset, -1, includeOffset));
            selectStatement.addSQLToken(new OffsetToken(
                    lexerEngine.getCurrentToken().getEndPosition() - String.valueOf(offset).length() - lexerEngine.getCurrentToken().getLiterals().length(), offset));
        } else if (sqlExpression instanceof SQLPlaceholderExpression) {
            selectStatement.getLimit().setOffset(new LimitValue(-1, ((SQLPlaceholderExpression) sqlExpression).getIndex(), includeOffset));
        }
    }
    
    protected abstract Keyword[] getCustomizedOtherConditionOperators();
    
    private void parseOtherCondition(final SQLStatement sqlStatement) {
        basicExpressionParser.parse(sqlStatement);
    }
    
    private void parseNotCondition(final SQLStatement sqlStatement) {
        if (lexerEngine.skipIfEqual(DefaultKeyword.BETWEEN)) {
            parseOtherCondition(sqlStatement);
            skipsDoubleColon();
            lexerEngine.accept(DefaultKeyword.AND);
            parseOtherCondition(sqlStatement);
            return;
        }
        if (lexerEngine.skipIfEqual(DefaultKeyword.IN)) {
            lexerEngine.accept(Symbol.LEFT_PAREN);
            do {
                parseOtherCondition(sqlStatement);
                skipsDoubleColon();
            } while (lexerEngine.skipIfEqual(Symbol.COMMA));
            lexerEngine.accept(Symbol.RIGHT_PAREN);
        } else {
            lexerEngine.nextToken();
            parseOtherCondition(sqlStatement);
        }
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
        return table.isPresent() ? Optional.of(new Column(SQLUtil.getExactlyValue(propertyExpression.getName()), table.get().getName())) : Optional.<Column>absent();
    }
    
    private Optional<Column> getColumnWithoutOwner(final Tables tables, final SQLIdentifierExpression identifierExpression) {
        return tables.isSingleTable() ? Optional.of(new Column(SQLUtil.getExactlyValue(identifierExpression.getName()), tables.getSingleTableName())) : Optional.<Column>absent();
    }
    
    private void skipsDoubleColon() {
        if (lexerEngine.skipIfEqual(Symbol.DOUBLE_COLON)) {
            lexerEngine.nextToken();
        }
    }
}
