package com.dangdang.ddframe.rdb.sharding.parsing.parser.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Column;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Condition;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.Limit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.LimitValue;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.table.Table;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.table.Tables;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLIdentifierExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLNumberExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPlaceholderExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPropertyExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLTextExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.dql.select.SelectStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.OffsetToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.RowCountToken;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;

import java.util.LinkedList;
import java.util.List;

/**
 * Where clause parser.
 *
 * @author zhangliang
 */
public class WhereClauseParser implements SQLClauseParser {
    
    private final LexerEngine lexerEngine;
    
    private final AliasClauseParser aliasClauseParser;
    
    private final ExpressionClauseParser expressionClauseParser;
    
    public WhereClauseParser(final LexerEngine lexerEngine) {
        this.lexerEngine = lexerEngine;
        aliasClauseParser = new AliasClauseParser(lexerEngine);
        expressionClauseParser = new ExpressionClauseParser(lexerEngine);
    }
    
    /**
     * Parse where.
     *
     * @param shardingRule databases and tables sharding rule
     * @param sqlStatement SQL statement
     * @param items select items
     */
    public void parse(final ShardingRule shardingRule, final SQLStatement sqlStatement, final List<SelectItem> items) {
        aliasClauseParser.parse();
        if (lexerEngine.skipIfEqual(DefaultKeyword.WHERE)) {
            parseConditions(shardingRule, sqlStatement, items);
        }
    }
    
    private void parseConditions(final ShardingRule shardingRule, final SQLStatement sqlStatement, final List<SelectItem> items) {
        do {
            parseComparisonCondition(shardingRule, sqlStatement, items);
        } while (lexerEngine.skipIfEqual(DefaultKeyword.AND));
        lexerEngine.unsupportedIfEqual(DefaultKeyword.OR);
    }
    
    private void parseComparisonCondition(final ShardingRule shardingRule, final SQLStatement sqlStatement, final List<SelectItem> items) {
        lexerEngine.skipIfEqual(Symbol.LEFT_PAREN);
        SQLExpression left = expressionClauseParser.parse(sqlStatement);
        if (lexerEngine.equalAny(Symbol.EQ)) {
            parseEqualCondition(shardingRule, sqlStatement, left);
            lexerEngine.skipIfEqual(Symbol.RIGHT_PAREN);
            return;
        }
        if (lexerEngine.equalAny(DefaultKeyword.IN)) {
            parseInCondition(shardingRule, sqlStatement, left);
            lexerEngine.skipIfEqual(Symbol.RIGHT_PAREN);
            return;
        }
        if (lexerEngine.equalAny(DefaultKeyword.BETWEEN)) {
            parseBetweenCondition(shardingRule, sqlStatement, left);
            lexerEngine.skipIfEqual(Symbol.RIGHT_PAREN);
            return;
        }
        if (lexerEngine.equalAny(Symbol.LT, Symbol.GT, Symbol.LT_EQ, Symbol.GT_EQ)) {
            if (left instanceof SQLIdentifierExpression && sqlStatement instanceof SelectStatement
                    && isRowNumberCondition(items, ((SQLIdentifierExpression) left).getName())) {
                parseRowNumberCondition((SelectStatement) sqlStatement);
            } else if (left instanceof SQLPropertyExpression && sqlStatement instanceof SelectStatement
                    && isRowNumberCondition(items, ((SQLPropertyExpression) left).getName())) {
                parseRowNumberCondition((SelectStatement) sqlStatement);
            } else {
                parseOtherCondition(sqlStatement);
            }
        } else if (lexerEngine.equalAny(Symbol.LT_GT, DefaultKeyword.LIKE)) {
            parseOtherCondition(sqlStatement);
        }
        lexerEngine.skipIfEqual(Symbol.RIGHT_PAREN);
    }
    
    private void parseEqualCondition(final ShardingRule shardingRule, final SQLStatement sqlStatement, final SQLExpression left) {
        lexerEngine.nextToken();
        SQLExpression right = expressionClauseParser.parse(sqlStatement);
        // TODO if have more tables, and cannot find column belong to, should not add to condition, should parse binding table rule.
        if ((sqlStatement.getTables().isSingleTable() || left instanceof SQLPropertyExpression)
                && (right instanceof SQLNumberExpression || right instanceof SQLTextExpression || right instanceof SQLPlaceholderExpression)) {
            Optional<Column> column = find(sqlStatement.getTables(), left);
            if (column.isPresent()) {
                sqlStatement.getConditions().add(new Condition(column.get(), right), shardingRule);
            }
        }
    }
    
    private void parseInCondition(final ShardingRule shardingRule, final SQLStatement sqlStatement, final SQLExpression left) {
        lexerEngine.nextToken();
        lexerEngine.accept(Symbol.LEFT_PAREN);
        List<SQLExpression> rights = new LinkedList<>();
        do {
            if (lexerEngine.equalAny(Symbol.COMMA)) {
                lexerEngine.nextToken();
            }
            rights.add(expressionClauseParser.parse(sqlStatement));
        } while (!lexerEngine.equalAny(Symbol.RIGHT_PAREN));
        Optional<Column> column = find(sqlStatement.getTables(), left);
        if (column.isPresent()) {
            sqlStatement.getConditions().add(new Condition(column.get(), rights), shardingRule);
        }
        lexerEngine.nextToken();
    }
    
    private void parseBetweenCondition(final ShardingRule shardingRule, final SQLStatement sqlStatement, final SQLExpression left) {
        lexerEngine.nextToken();
        List<SQLExpression> rights = new LinkedList<>();
        rights.add(expressionClauseParser.parse(sqlStatement));
        lexerEngine.accept(DefaultKeyword.AND);
        rights.add(expressionClauseParser.parse(sqlStatement));
        Optional<Column> column = find(sqlStatement.getTables(), left);
        if (column.isPresent()) {
            sqlStatement.getConditions().add(new Condition(column.get(), rights.get(0), rights.get(1)), shardingRule);
        }
    }
    
    protected boolean isRowNumberCondition(final List<SelectItem> items, final String columnLabel) {
        return false;
    }
    
    private void parseRowNumberCondition(final SelectStatement selectStatement) {
        Symbol symbol = (Symbol) lexerEngine.getCurrentToken().getType();
        lexerEngine.nextToken();
        SQLExpression sqlExpression = expressionClauseParser.parse(selectStatement);
        if (null == selectStatement.getLimit()) {
            selectStatement.setLimit(new Limit(false));
        }
        if (Symbol.LT == symbol || Symbol.LT_EQ == symbol) {
            if (sqlExpression instanceof SQLNumberExpression) {
                int rowCount = ((SQLNumberExpression) sqlExpression).getNumber().intValue();
                selectStatement.getLimit().setRowCount(new LimitValue(rowCount, -1));
                selectStatement.getSqlTokens().add(new RowCountToken(
                        lexerEngine.getCurrentToken().getEndPosition() - String.valueOf(rowCount).length() - lexerEngine.getCurrentToken().getLiterals().length(), rowCount));
            } else if (sqlExpression instanceof SQLPlaceholderExpression) {
                selectStatement.getLimit().setRowCount(new LimitValue(-1, ((SQLPlaceholderExpression) sqlExpression).getIndex()));
            }
        } else if (Symbol.GT == symbol || Symbol.GT_EQ == symbol) {
            if (sqlExpression instanceof SQLNumberExpression) {
                int offset = ((SQLNumberExpression) sqlExpression).getNumber().intValue();
                selectStatement.getLimit().setOffset(new LimitValue(offset, -1));
                selectStatement.getSqlTokens().add(new OffsetToken(
                        lexerEngine.getCurrentToken().getEndPosition() - String.valueOf(offset).length() - lexerEngine.getCurrentToken().getLiterals().length(), offset));
            } else if (sqlExpression instanceof SQLPlaceholderExpression) {
                selectStatement.getLimit().setOffset(new LimitValue(-1, ((SQLPlaceholderExpression) sqlExpression).getIndex()));
            }
        }
    }
    
    private void parseOtherCondition(final SQLStatement sqlStatement) {
        lexerEngine.nextToken();
        expressionClauseParser.parse(sqlStatement);
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
