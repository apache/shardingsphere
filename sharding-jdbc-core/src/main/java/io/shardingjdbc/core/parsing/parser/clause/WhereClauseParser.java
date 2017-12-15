package io.shardingjdbc.core.parsing.parser.clause;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Keyword;
import io.shardingjdbc.core.parsing.lexer.token.Symbol;
import io.shardingjdbc.core.parsing.parser.context.condition.Column;
import io.shardingjdbc.core.parsing.parser.context.condition.Condition;
import io.shardingjdbc.core.parsing.parser.context.limit.Limit;
import io.shardingjdbc.core.parsing.parser.context.limit.LimitValue;
import io.shardingjdbc.core.parsing.parser.context.selectitem.SelectItem;
import io.shardingjdbc.core.parsing.parser.context.table.Table;
import io.shardingjdbc.core.parsing.parser.context.table.Tables;
import io.shardingjdbc.core.parsing.parser.expression.SQLExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLIdentifierExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLNumberExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLPlaceholderExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLPropertyExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLTextExpression;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingjdbc.core.parsing.parser.token.OffsetToken;
import io.shardingjdbc.core.parsing.parser.token.RowCountToken;
import io.shardingjdbc.core.util.SQLUtil;
import com.google.common.base.Optional;

import java.util.Arrays;
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
        if (lexerEngine.skipIfEqual(Symbol.EQ)) {
            parseEqualCondition(shardingRule, sqlStatement, left);
            lexerEngine.skipIfEqual(Symbol.RIGHT_PAREN);
            return;
        }
        if (lexerEngine.skipIfEqual(DefaultKeyword.IN)) {
            parseInCondition(shardingRule, sqlStatement, left);
            lexerEngine.skipIfEqual(Symbol.RIGHT_PAREN);
            return;
        }
        if (lexerEngine.skipIfEqual(DefaultKeyword.BETWEEN)) {
            parseBetweenCondition(shardingRule, sqlStatement, left);
            lexerEngine.skipIfEqual(Symbol.RIGHT_PAREN);
            return;
        }
        if (sqlStatement instanceof SelectStatement && isRowNumberCondition(items, left)) {
            if (lexerEngine.skipIfEqual(Symbol.LT, Symbol.LT_EQ)) {
                parseRowCountCondition((SelectStatement) sqlStatement);
                return;
            }
            if (lexerEngine.skipIfEqual(Symbol.GT, Symbol.GT_EQ)) {
                parseOffsetCondition((SelectStatement) sqlStatement);
                return;
            }
        }
        List<Keyword> otherConditionOperators = new LinkedList<>(Arrays.asList(getCustomizedOtherConditionOperators()));
        otherConditionOperators.addAll(
                Arrays.asList(Symbol.LT, Symbol.LT_EQ, Symbol.GT, Symbol.GT_EQ, Symbol.LT_GT, Symbol.BANG_EQ, Symbol.BANG_GT, Symbol.BANG_LT, DefaultKeyword.LIKE, DefaultKeyword.IS));
        if (lexerEngine.skipIfEqual(otherConditionOperators.toArray(new Keyword[otherConditionOperators.size()]))) {
            parseOtherCondition(sqlStatement);
        }
        if (lexerEngine.skipIfEqual(DefaultKeyword.NOT)) {
            lexerEngine.nextToken();
            lexerEngine.skipIfEqual(Symbol.LEFT_PAREN);
            parseOtherCondition(sqlStatement);
            lexerEngine.skipIfEqual(Symbol.RIGHT_PAREN);
        }
        lexerEngine.skipIfEqual(Symbol.RIGHT_PAREN);
    }
    
    private void parseEqualCondition(final ShardingRule shardingRule, final SQLStatement sqlStatement, final SQLExpression left) {
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
        lexerEngine.accept(Symbol.LEFT_PAREN);
        List<SQLExpression> rights = new LinkedList<>();
        do {
            lexerEngine.skipIfEqual(Symbol.COMMA);
            rights.add(expressionClauseParser.parse(sqlStatement));
        } while (!lexerEngine.equalAny(Symbol.RIGHT_PAREN));
        Optional<Column> column = find(sqlStatement.getTables(), left);
        if (column.isPresent()) {
            sqlStatement.getConditions().add(new Condition(column.get(), rights), shardingRule);
        }
        lexerEngine.nextToken();
    }
    
    private void parseBetweenCondition(final ShardingRule shardingRule, final SQLStatement sqlStatement, final SQLExpression left) {
        List<SQLExpression> rights = new LinkedList<>();
        rights.add(expressionClauseParser.parse(sqlStatement));
        lexerEngine.accept(DefaultKeyword.AND);
        rights.add(expressionClauseParser.parse(sqlStatement));
        Optional<Column> column = find(sqlStatement.getTables(), left);
        if (column.isPresent()) {
            sqlStatement.getConditions().add(new Condition(column.get(), rights.get(0), rights.get(1)), shardingRule);
        }
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
    
    protected boolean isRowNumberCondition(final List<SelectItem> items, final String columnLabel) {
        return false;
    }
    
    private void parseRowCountCondition(final SelectStatement selectStatement) {
        SQLExpression sqlExpression = expressionClauseParser.parse(selectStatement);
        if (null == selectStatement.getLimit()) {
            selectStatement.setLimit(new Limit(false));
        }
        if (sqlExpression instanceof SQLNumberExpression) {
            int rowCount = ((SQLNumberExpression) sqlExpression).getNumber().intValue();
            selectStatement.getLimit().setRowCount(new LimitValue(rowCount, -1));
            selectStatement.getSqlTokens().add(new RowCountToken(
                    lexerEngine.getCurrentToken().getEndPosition() - String.valueOf(rowCount).length() - lexerEngine.getCurrentToken().getLiterals().length(), rowCount));
        } else if (sqlExpression instanceof SQLPlaceholderExpression) {
            selectStatement.getLimit().setRowCount(new LimitValue(-1, ((SQLPlaceholderExpression) sqlExpression).getIndex()));
        }
    }
    
    private void parseOffsetCondition(final SelectStatement selectStatement) {
        SQLExpression sqlExpression = expressionClauseParser.parse(selectStatement);
        if (null == selectStatement.getLimit()) {
            selectStatement.setLimit(new Limit(false));
        }
        if (sqlExpression instanceof SQLNumberExpression) {
            int offset = ((SQLNumberExpression) sqlExpression).getNumber().intValue();
            selectStatement.getLimit().setOffset(new LimitValue(offset, -1));
            selectStatement.getSqlTokens().add(new OffsetToken(
                    lexerEngine.getCurrentToken().getEndPosition() - String.valueOf(offset).length() - lexerEngine.getCurrentToken().getLiterals().length(), offset));
        } else if (sqlExpression instanceof SQLPlaceholderExpression) {
            selectStatement.getLimit().setOffset(new LimitValue(-1, ((SQLPlaceholderExpression) sqlExpression).getIndex()));
        }
    }
    
    protected Keyword[] getCustomizedOtherConditionOperators() {
        return new Keyword[0];
    }
    
    private void parseOtherCondition(final SQLStatement sqlStatement) {
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
