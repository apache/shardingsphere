package com.dangdang.ddframe.rdb.sharding.parsing.parser.sql;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Literals;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.CommonParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLIdentifierExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLIgnoreExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLNumberExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPlaceholderExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPropertyExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLTextExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken;
import com.dangdang.ddframe.rdb.sharding.util.NumberUtil;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import lombok.RequiredArgsConstructor;

/**
 * 表达式解析对象.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ExpressionSQLParser implements SQLParser {
    
    private final CommonParser commonParser;
    
    /**
     * 解析表达式.
     *
     * @param sqlStatement SQL语句对象
     * @return 表达式
     */
    public SQLExpression parse(final SQLStatement sqlStatement) {
        int beginPosition = commonParser.getLexer().getCurrentToken().getEndPosition();
        SQLExpression result = parseExpression(sqlStatement);
        if (result instanceof SQLPropertyExpression) {
            setTableToken(sqlStatement, beginPosition, (SQLPropertyExpression) result);
        }
        return result;
    }
    
    // TODO 完善Expression解析的各种场景
    private SQLExpression parseExpression(final SQLStatement sqlStatement) {
        String literals = commonParser.getLexer().getCurrentToken().getLiterals();
        final int beginPosition = commonParser.getLexer().getCurrentToken().getEndPosition() - literals.length();
        final SQLExpression expression = getExpression(literals, sqlStatement);
        commonParser.getLexer().nextToken();
        if (commonParser.skipIfEqual(Symbol.DOT)) {
            String property = commonParser.getLexer().getCurrentToken().getLiterals();
            commonParser.getLexer().nextToken();
            return skipIfCompositeExpression(sqlStatement)
                    ? new SQLIgnoreExpression(commonParser.getLexer().getInput().substring(beginPosition, commonParser.getLexer().getCurrentToken().getEndPosition()))
                    : new SQLPropertyExpression(new SQLIdentifierExpression(literals), property);
        }
        if (commonParser.equalAny(Symbol.LEFT_PAREN)) {
            commonParser.skipParentheses(sqlStatement);
            skipRestCompositeExpression(sqlStatement);
            return new SQLIgnoreExpression(commonParser.getLexer().getInput().substring(beginPosition,
                    commonParser.getLexer().getCurrentToken().getEndPosition() - commonParser.getLexer().getCurrentToken().getLiterals().length()).trim());
        }
        return skipIfCompositeExpression(sqlStatement)
                ? new SQLIgnoreExpression(commonParser.getLexer().getInput().substring(beginPosition, commonParser.getLexer().getCurrentToken().getEndPosition())) : expression;
    }
    
    private SQLExpression getExpression(final String literals, final SQLStatement sqlStatement) {
        if (commonParser.equalAny(Symbol.QUESTION)) {
            sqlStatement.increaseParametersIndex();
            return new SQLPlaceholderExpression(sqlStatement.getParametersIndex() - 1);
        }
        if (commonParser.equalAny(Literals.CHARS)) {
            return new SQLTextExpression(literals);
        }
        if (commonParser.equalAny(Literals.INT)) {
            return new SQLNumberExpression(NumberUtil.getExactlyNumber(literals, 10));
        }
        if (commonParser.equalAny(Literals.FLOAT)) {
            return new SQLNumberExpression(Double.parseDouble(literals));
        }
        if (commonParser.equalAny(Literals.HEX)) {
            return new SQLNumberExpression(NumberUtil.getExactlyNumber(literals, 16));
        }
        if (commonParser.equalAny(Literals.IDENTIFIER)) {
            return new SQLIdentifierExpression(SQLUtil.getExactlyValue(literals));
        }
        return new SQLIgnoreExpression(literals);
    }
    
    private boolean skipIfCompositeExpression(final SQLStatement sqlStatement) {
        if (commonParser.equalAny(
                Symbol.PLUS, Symbol.SUB, Symbol.STAR, Symbol.SLASH, Symbol.PERCENT, Symbol.AMP, Symbol.BAR, Symbol.DOUBLE_AMP, Symbol.DOUBLE_BAR, Symbol.CARET, Symbol.DOT, Symbol.LEFT_PAREN)) {
            commonParser.skipParentheses(sqlStatement);
            skipRestCompositeExpression(sqlStatement);
            return true;
        }
        return false;
    }
    
    private void skipRestCompositeExpression(final SQLStatement sqlStatement) {
        while (commonParser.skipIfEqual(Symbol.PLUS, Symbol.SUB, Symbol.STAR, Symbol.SLASH, Symbol.PERCENT, Symbol.AMP, Symbol.BAR, Symbol.DOUBLE_AMP, Symbol.DOUBLE_BAR, Symbol.CARET, Symbol.DOT)) {
            if (commonParser.equalAny(Symbol.QUESTION)) {
                sqlStatement.increaseParametersIndex();
            }
            commonParser.getLexer().nextToken();
            commonParser.skipParentheses(sqlStatement);
        }
    }
    
    private void setTableToken(final SQLStatement sqlStatement, final int beginPosition, final SQLPropertyExpression propertyExpr) {
        String owner = propertyExpr.getOwner().getName();
        if (sqlStatement.getTables().getTableNames().contains(SQLUtil.getExactlyValue(propertyExpr.getOwner().getName()))) {
            sqlStatement.getSqlTokens().add(new TableToken(beginPosition - owner.length(), owner));
        }
    }
}
