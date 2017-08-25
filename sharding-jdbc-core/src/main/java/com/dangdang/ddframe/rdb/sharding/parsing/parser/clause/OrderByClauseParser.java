package com.dangdang.ddframe.rdb.sharding.parsing.parser.clause;

import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLIdentifierExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLIgnoreExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLNumberExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPropertyExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.dql.select.SelectStatement;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

/**
 * Order by clause parser.
 *
 * @author zhangliang
 */
public class OrderByClauseParser implements SQLClauseParser {
    
    @Getter
    private final LexerEngine lexerEngine;
    
    private final ExpressionClauseParser expressionClauseParser;
    
    public OrderByClauseParser(final LexerEngine lexerEngine) {
        this.lexerEngine = lexerEngine;
        expressionClauseParser = new ExpressionClauseParser(lexerEngine);
    }
    
    /**
     * Parse order by.
     *
     * @param selectStatement select statement
     */
    public final void parse(final SelectStatement selectStatement) {
        if (!lexerEngine.skipIfEqual(DefaultKeyword.ORDER)) {
            return;
        }
        List<OrderItem> result = new LinkedList<>();
        lexerEngine.skipIfEqual(DefaultKeyword.SIBLINGS);
        lexerEngine.accept(DefaultKeyword.BY);
        do {
            result.add(parseSelectOrderByItem(selectStatement));
        }
        while (lexerEngine.skipIfEqual(Symbol.COMMA));
        selectStatement.getOrderByItems().addAll(result);
    }
    
    private OrderItem parseSelectOrderByItem(final SelectStatement selectStatement) {
        SQLExpression sqlExpression = expressionClauseParser.parse(selectStatement);
        OrderType orderByType = OrderType.ASC;
        if (lexerEngine.skipIfEqual(DefaultKeyword.ASC)) {
            orderByType = OrderType.ASC;
        } else if (lexerEngine.skipIfEqual(DefaultKeyword.DESC)) {
            orderByType = OrderType.DESC;
        }
        if (sqlExpression instanceof SQLNumberExpression) {
            return new OrderItem(((SQLNumberExpression) sqlExpression).getNumber().intValue(), orderByType, getNullOrderType());
        }
        if (sqlExpression instanceof SQLIdentifierExpression) {
            return new OrderItem(SQLUtil.getExactlyValue(((SQLIdentifierExpression) sqlExpression).getName()),
                    orderByType, getNullOrderType(), selectStatement.getAlias(SQLUtil.getExactlyValue(((SQLIdentifierExpression) sqlExpression).getName())));
        }
        if (sqlExpression instanceof SQLPropertyExpression) {
            SQLPropertyExpression sqlPropertyExpression = (SQLPropertyExpression) sqlExpression;
            return new OrderItem(SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner().getName()), SQLUtil.getExactlyValue(sqlPropertyExpression.getName()), orderByType, getNullOrderType(),
                    selectStatement.getAlias(SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner().getName()) + "." + SQLUtil.getExactlyValue(sqlPropertyExpression.getName())));
        }
        if (sqlExpression instanceof SQLIgnoreExpression) {
            SQLIgnoreExpression sqlIgnoreExpression = (SQLIgnoreExpression) sqlExpression;
            return new OrderItem(sqlIgnoreExpression.getExpression(), orderByType, getNullOrderType(), selectStatement.getAlias(sqlIgnoreExpression.getExpression()));
        }
        throw new SQLParsingException(lexerEngine);
    }
    
    protected OrderType getNullOrderType() {
        return OrderType.ASC;
    }
}
