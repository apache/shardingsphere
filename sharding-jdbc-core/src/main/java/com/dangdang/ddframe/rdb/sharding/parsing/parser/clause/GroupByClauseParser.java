package com.dangdang.ddframe.rdb.sharding.parsing.parser.clause;

import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLIdentifierExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLIgnoreExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPropertyExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.dql.select.SelectStatement;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;

/**
 * Group by clause parser.
 *
 * @author zhangliang
 */
public class GroupByClauseParser implements SQLClauseParser {
    
    private final LexerEngine lexerEngine;
    
    private final ExpressionClauseParser expressionClauseParser;
    
    public GroupByClauseParser(final LexerEngine lexerEngine) {
        this.lexerEngine = lexerEngine;
        expressionClauseParser = new ExpressionClauseParser(lexerEngine);
    }
    
    /**
     * Parse group by.
     *
     * @param selectStatement select statement
     */
    public final void parse(final SelectStatement selectStatement) {
        if (!lexerEngine.skipIfEqual(DefaultKeyword.GROUP)) {
            return;
        }
        lexerEngine.accept(DefaultKeyword.BY);
        while (true) {
            addGroupByItem(expressionClauseParser.parse(selectStatement), selectStatement);
            if (!lexerEngine.equalAny(Symbol.COMMA)) {
                break;
            }
            lexerEngine.nextToken();
        }
        lexerEngine.skipAll(getSkippedKeywordAfterGroupBy());
        selectStatement.setGroupByLastPosition(lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length());
    }
    
    private void addGroupByItem(final SQLExpression sqlExpression, final SelectStatement selectStatement) {
        lexerEngine.unsupportedIfEqual(getUnsupportedKeywordBeforeGroupByItem());
        OrderType orderByType = OrderType.ASC;
        if (lexerEngine.equalAny(DefaultKeyword.ASC)) {
            lexerEngine.nextToken();
        } else if (lexerEngine.skipIfEqual(DefaultKeyword.DESC)) {
            orderByType = OrderType.DESC;
        }
        OrderItem orderItem;
        if (sqlExpression instanceof SQLPropertyExpression) {
            SQLPropertyExpression sqlPropertyExpression = (SQLPropertyExpression) sqlExpression;
            orderItem = new OrderItem(SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner().getName()), SQLUtil.getExactlyValue(sqlPropertyExpression.getName()), orderByType, OrderType.ASC,
                    selectStatement.getAlias(SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner() + "." + SQLUtil.getExactlyValue(sqlPropertyExpression.getName()))));
        } else if (sqlExpression instanceof SQLIdentifierExpression) {
            SQLIdentifierExpression sqlIdentifierExpression = (SQLIdentifierExpression) sqlExpression;
            orderItem = new OrderItem(
                    SQLUtil.getExactlyValue(sqlIdentifierExpression.getName()), orderByType, OrderType.ASC, selectStatement.getAlias(SQLUtil.getExactlyValue(sqlIdentifierExpression.getName())));
        } else if (sqlExpression instanceof SQLIgnoreExpression) {
            SQLIgnoreExpression sqlIgnoreExpression = (SQLIgnoreExpression) sqlExpression;
            orderItem = new OrderItem(sqlIgnoreExpression.getExpression(), orderByType, OrderType.ASC, selectStatement.getAlias(sqlIgnoreExpression.getExpression()));
        } else {
            return;
        }
        selectStatement.getGroupByItems().add(orderItem);
    }
    
    protected Keyword[] getUnsupportedKeywordBeforeGroupByItem() {
        return new Keyword[0];
    }
    
    protected Keyword[] getSkippedKeywordAfterGroupBy() {
        return new Keyword[0];
    }
}
