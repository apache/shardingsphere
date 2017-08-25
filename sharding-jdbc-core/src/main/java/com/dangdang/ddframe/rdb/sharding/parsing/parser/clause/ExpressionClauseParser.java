package com.dangdang.ddframe.rdb.sharding.parsing.parser.clause;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Literals;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLIdentifierExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLIgnoreExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLNumberExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPlaceholderExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPropertyExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLTextExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken;
import com.dangdang.ddframe.rdb.sharding.util.NumberUtil;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import lombok.RequiredArgsConstructor;

/**
 * Expression clause parser.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ExpressionClauseParser implements SQLClauseParser {
    
    private final LexerEngine lexerEngine;
    
    /**
     * Parse expression.
     *
     * @param sqlStatement SQL statement
     * @return 表达式
     */
    public SQLExpression parse(final SQLStatement sqlStatement) {
        int beginPosition = lexerEngine.getCurrentToken().getEndPosition();
        SQLExpression result = parseExpression(sqlStatement);
        if (result instanceof SQLPropertyExpression) {
            setTableToken(sqlStatement, beginPosition, (SQLPropertyExpression) result);
        }
        return result;
    }
    
    // TODO complete more expression parse
    private SQLExpression parseExpression(final SQLStatement sqlStatement) {
        String literals = lexerEngine.getCurrentToken().getLiterals();
        final int beginPosition = lexerEngine.getCurrentToken().getEndPosition() - literals.length();
        final SQLExpression expression = getExpression(literals, sqlStatement);
        lexerEngine.nextToken();
        if (lexerEngine.skipIfEqual(Symbol.DOT)) {
            String property = lexerEngine.getCurrentToken().getLiterals();
            lexerEngine.nextToken();
            return skipIfCompositeExpression(sqlStatement)
                    ? new SQLIgnoreExpression(lexerEngine.getInput().substring(beginPosition, lexerEngine.getCurrentToken().getEndPosition()))
                    : new SQLPropertyExpression(new SQLIdentifierExpression(literals), property);
        }
        if (lexerEngine.equalAny(Symbol.LEFT_PAREN)) {
            lexerEngine.skipParentheses(sqlStatement);
            skipRestCompositeExpression(sqlStatement);
            return new SQLIgnoreExpression(lexerEngine.getInput().substring(beginPosition,
                    lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length()).trim());
        }
        return skipIfCompositeExpression(sqlStatement)
                ? new SQLIgnoreExpression(lexerEngine.getInput().substring(beginPosition, lexerEngine.getCurrentToken().getEndPosition())) : expression;
    }
    
    private SQLExpression getExpression(final String literals, final SQLStatement sqlStatement) {
        if (lexerEngine.equalAny(Symbol.QUESTION)) {
            sqlStatement.increaseParametersIndex();
            return new SQLPlaceholderExpression(sqlStatement.getParametersIndex() - 1);
        }
        if (lexerEngine.equalAny(Literals.CHARS)) {
            return new SQLTextExpression(literals);
        }
        if (lexerEngine.equalAny(Literals.INT)) {
            return new SQLNumberExpression(NumberUtil.getExactlyNumber(literals, 10));
        }
        if (lexerEngine.equalAny(Literals.FLOAT)) {
            return new SQLNumberExpression(Double.parseDouble(literals));
        }
        if (lexerEngine.equalAny(Literals.HEX)) {
            return new SQLNumberExpression(NumberUtil.getExactlyNumber(literals, 16));
        }
        if (lexerEngine.equalAny(Literals.IDENTIFIER)) {
            return new SQLIdentifierExpression(SQLUtil.getExactlyValue(literals));
        }
        return new SQLIgnoreExpression(literals);
    }
    
    private boolean skipIfCompositeExpression(final SQLStatement sqlStatement) {
        if (lexerEngine.equalAny(
                Symbol.PLUS, Symbol.SUB, Symbol.STAR, Symbol.SLASH, Symbol.PERCENT, Symbol.AMP, Symbol.BAR, Symbol.DOUBLE_AMP, Symbol.DOUBLE_BAR, Symbol.CARET, Symbol.DOT, Symbol.LEFT_PAREN)) {
            lexerEngine.skipParentheses(sqlStatement);
            skipRestCompositeExpression(sqlStatement);
            return true;
        }
        return false;
    }
    
    private void skipRestCompositeExpression(final SQLStatement sqlStatement) {
        while (lexerEngine.skipIfEqual(Symbol.PLUS, Symbol.SUB, Symbol.STAR, Symbol.SLASH, Symbol.PERCENT, Symbol.AMP, Symbol.BAR, Symbol.DOUBLE_AMP, Symbol.DOUBLE_BAR, Symbol.CARET, Symbol.DOT)) {
            if (lexerEngine.equalAny(Symbol.QUESTION)) {
                sqlStatement.increaseParametersIndex();
            }
            lexerEngine.nextToken();
            lexerEngine.skipParentheses(sqlStatement);
        }
    }
    
    private void setTableToken(final SQLStatement sqlStatement, final int beginPosition, final SQLPropertyExpression propertyExpr) {
        String owner = propertyExpr.getOwner().getName();
        if (sqlStatement.getTables().getTableNames().contains(SQLUtil.getExactlyValue(propertyExpr.getOwner().getName()))) {
            sqlStatement.getSqlTokens().add(new TableToken(beginPosition - owner.length(), owner));
        }
    }
}
