package com.dangdang.ddframe.rdb.sharding.parsing.parser.clause;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.dml.DMLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;

/**
 * UPDATE SET从句解析器.
 *
 * @author zhangliang
 */
public final class UpdateSetItemsClauseParser implements SQLClauseParser {
    
    private final LexerEngine lexerEngine;
    
    private final ExpressionClauseParser expressionClauseParser;
    
    public UpdateSetItemsClauseParser(final LexerEngine lexerEngine) {
        this.lexerEngine = lexerEngine;
        expressionClauseParser = new ExpressionClauseParser(lexerEngine);
    }
    
    /**
     * 解析SET.
     *
     * @param updateStatement DML语句对象
     */
    public void parse(final DMLStatement updateStatement) {
        lexerEngine.accept(DefaultKeyword.SET);
        do {
            parseSetItem(updateStatement);
        } while (lexerEngine.skipIfEqual(Symbol.COMMA));
    }
    
    private void parseSetItem(final DMLStatement updateStatement) {
        parseSetColumn(updateStatement);
        lexerEngine.skipIfEqual(Symbol.EQ, Symbol.COLON_EQ);
        parseSetValue(updateStatement);
    }
    
    private void parseSetColumn(final DMLStatement updateStatement) {
        if (lexerEngine.equalAny(Symbol.LEFT_PAREN)) {
            lexerEngine.skipParentheses(updateStatement);
            return;
        }
        int beginPosition = lexerEngine.getCurrentToken().getEndPosition();
        String literals = lexerEngine.getCurrentToken().getLiterals();
        lexerEngine.nextToken();
        if (lexerEngine.skipIfEqual(Symbol.DOT)) {
            if (updateStatement.getTables().getSingleTableName().equalsIgnoreCase(SQLUtil.getExactlyValue(literals))) {
                updateStatement.getSqlTokens().add(new TableToken(beginPosition - literals.length(), literals));
            }
            lexerEngine.nextToken();
        }
    }
    
    private void parseSetValue(final DMLStatement updateStatement) {
        expressionClauseParser.parse(updateStatement);
    }
}
