package com.dangdang.ddframe.rdb.sharding.parsing.parser.clause;

import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLIdentifierExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLIgnoreExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPropertyExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.dql.select.SelectStatement;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;

/**
 * 分组从句解析器.
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
     * 解析分组.
     *
     * @param selectStatement Select语句对象
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
                    getAlias(SQLUtil.getExactlyValue(sqlPropertyExpression.getOwner() + "." + SQLUtil.getExactlyValue(sqlPropertyExpression.getName())), selectStatement));
        } else if (sqlExpression instanceof SQLIdentifierExpression) {
            SQLIdentifierExpression sqlIdentifierExpression = (SQLIdentifierExpression) sqlExpression;
            orderItem = new OrderItem(
                    SQLUtil.getExactlyValue(sqlIdentifierExpression.getName()), orderByType, OrderType.ASC, getAlias(SQLUtil.getExactlyValue(sqlIdentifierExpression.getName()), selectStatement));
        } else if (sqlExpression instanceof SQLIgnoreExpression) {
            SQLIgnoreExpression sqlIgnoreExpression = (SQLIgnoreExpression) sqlExpression;
            orderItem = new OrderItem(sqlIgnoreExpression.getExpression(), orderByType, OrderType.ASC, getAlias(sqlIgnoreExpression.getExpression(), selectStatement));
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
