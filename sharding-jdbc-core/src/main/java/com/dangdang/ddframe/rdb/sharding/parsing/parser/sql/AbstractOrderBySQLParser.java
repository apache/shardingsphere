package com.dangdang.ddframe.rdb.sharding.parsing.parser.sql;

import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLIdentifierExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLIgnoreExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLNumberExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPropertyExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectStatement;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

/**
 * Order By解析器.
 *
 * @author zhangliang
 */
public abstract class AbstractOrderBySQLParser implements SQLParser {
    
    @Getter
    private final LexerEngine lexerEngine;
    
    private final ExpressionSQLParser expressionSQLParser;
    
    public AbstractOrderBySQLParser(final LexerEngine lexerEngine) {
        this.lexerEngine = lexerEngine;
        expressionSQLParser = new ExpressionSQLParser(lexerEngine);
    }
    
    /**
     * 解析排序.
     *
     * @param selectStatement Select语句对象
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
        SQLExpression sqlExpression = expressionSQLParser.parse(selectStatement);
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
                    orderByType, getNullOrderType(), getAlias(SQLUtil.getExactlyValue(((SQLIdentifierExpression) sqlExpression).getName()), selectStatement));
        }
        if (sqlExpression instanceof SQLPropertyExpression) {
            SQLPropertyExpression sqlPropertyExpression = (SQLPropertyExpression) sqlExpression;
            return new OrderItem(SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner().getName()), SQLUtil.getExactlyValue(sqlPropertyExpression.getName()), orderByType, getNullOrderType(),
                    getAlias(SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner().getName()) + "." + SQLUtil.getExactlyValue(sqlPropertyExpression.getName()), selectStatement));
        }
        if (sqlExpression instanceof SQLIgnoreExpression) {
            SQLIgnoreExpression sqlIgnoreExpression = (SQLIgnoreExpression) sqlExpression;
            return new OrderItem(sqlIgnoreExpression.getExpression(), orderByType, getNullOrderType(), getAlias(sqlIgnoreExpression.getExpression(), selectStatement));
        }
        throw new SQLParsingException(lexerEngine);
    }
    
    protected abstract OrderType getNullOrderType();
    
    private Optional<String> getAlias(final String name, final SelectStatement selectStatement) {
        if (selectStatement.isContainStar()) {
            return Optional.absent();
        }
        String rawName = SQLUtil.getExactlyValue(name);
        for (SelectItem each : selectStatement.getItems()) {
            if (rawName.equalsIgnoreCase(SQLUtil.getExactlyValue(each.getExpression()))) {
                return each.getAlias();
            }
            if (rawName.equalsIgnoreCase(each.getAlias().orNull())) {
                return Optional.of(rawName);
            }
        }
        return Optional.absent();
    }
}
