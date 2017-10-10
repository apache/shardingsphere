package io.shardingjdbc.core.parsing.parser.clause;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.token.Assist;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Keyword;
import io.shardingjdbc.core.parsing.lexer.token.Literals;
import io.shardingjdbc.core.parsing.lexer.token.Symbol;
import io.shardingjdbc.core.parsing.parser.context.condition.Column;
import io.shardingjdbc.core.parsing.parser.context.condition.Condition;
import io.shardingjdbc.core.parsing.parser.expression.SQLExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLIgnoreExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLNumberExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLPlaceholderExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLTextExpression;
import io.shardingjdbc.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingjdbc.core.util.SQLUtil;
import lombok.RequiredArgsConstructor;

/**
 * Insert set clause parser.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public class InsertSetClauseParser implements SQLClauseParser {
    
    private final ShardingRule shardingRule;
    
    private final LexerEngine lexerEngine;
    
    /**
     * Parse insert set.
     *
     * @param insertStatement insert statement
     */
    public void parse(final InsertStatement insertStatement) {
        if (!lexerEngine.skipIfEqual(getCustomizedInsertKeywords())) {
            return;
        }
        do {
            Column column = new Column(SQLUtil.getExactlyValue(lexerEngine.getCurrentToken().getLiterals()), insertStatement.getTables().getSingleTableName());
            lexerEngine.nextToken();
            lexerEngine.accept(Symbol.EQ);
            SQLExpression sqlExpression;
            if (lexerEngine.equalAny(Literals.INT)) {
                sqlExpression = new SQLNumberExpression(Integer.parseInt(lexerEngine.getCurrentToken().getLiterals()));
            } else if (lexerEngine.equalAny(Literals.FLOAT)) {
                sqlExpression = new SQLNumberExpression(Double.parseDouble(lexerEngine.getCurrentToken().getLiterals()));
            } else if (lexerEngine.equalAny(Literals.CHARS)) {
                sqlExpression = new SQLTextExpression(lexerEngine.getCurrentToken().getLiterals());
            } else if (lexerEngine.equalAny(DefaultKeyword.NULL)) {
                sqlExpression = new SQLIgnoreExpression(DefaultKeyword.NULL.name());
            } else if (lexerEngine.equalAny(Symbol.QUESTION)) {
                sqlExpression = new SQLPlaceholderExpression(insertStatement.getParametersIndex());
                insertStatement.increaseParametersIndex();
            } else {
                throw new UnsupportedOperationException("");
            }
            lexerEngine.nextToken();
            if (lexerEngine.equalAny(Symbol.COMMA, DefaultKeyword.ON, Assist.END)) {
                insertStatement.getConditions().add(new Condition(column, sqlExpression), shardingRule);
            } else {
                lexerEngine.skipUntil(Symbol.COMMA, DefaultKeyword.ON);
            }
        } while (lexerEngine.skipIfEqual(Symbol.COMMA));
    }
    
    protected Keyword[] getCustomizedInsertKeywords() {
        return new Keyword[0];
    }
}
