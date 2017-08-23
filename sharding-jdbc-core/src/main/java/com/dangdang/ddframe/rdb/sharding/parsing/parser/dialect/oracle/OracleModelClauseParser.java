package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.SQLClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectStatement;
import lombok.RequiredArgsConstructor;

/**
 * Oracle Model解析器.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class OracleModelClauseParser implements SQLClauseParser {
    
    private final LexerEngine lexerEngine;
    
    /**
     * 解析Model语句.
     * 
     * @param selectStatement Select SQL语句对象
     */
    public void parse(final SelectStatement selectStatement) {
        if (!lexerEngine.skipIfEqual(OracleKeyword.MODEL)) {
            return;
        }
        skipCellReferenceOptions();
        lexerEngine.skipIfEqual(OracleKeyword.RETURN);
        lexerEngine.skipIfEqual(DefaultKeyword.ALL);
        lexerEngine.skipIfEqual(OracleKeyword.UPDATED);
        lexerEngine.skipIfEqual(OracleKeyword.ROWS);
        while (lexerEngine.skipIfEqual(OracleKeyword.REFERENCE)) {
            lexerEngine.nextToken();
            lexerEngine.accept(DefaultKeyword.ON);
            lexerEngine.skipParentheses(selectStatement);
            skipModelColumnClause();
            skipCellReferenceOptions();
        }
        skipMainModelClause(selectStatement);
    }
    
    private void skipCellReferenceOptions() {
        if (lexerEngine.skipIfEqual(OracleKeyword.IGNORE)) {
            lexerEngine.accept(OracleKeyword.NAV);
        } else if (lexerEngine.skipIfEqual(OracleKeyword.KEEP)) {
            lexerEngine.accept(OracleKeyword.NAV);
        }
        if (lexerEngine.skipIfEqual(DefaultKeyword.UNIQUE)) {
            lexerEngine.skipIfEqual(OracleKeyword.DIMENSION, OracleKeyword.SINGLE);
            lexerEngine.skipIfEqual(OracleKeyword.REFERENCE);
        }
    }
    
    private void skipMainModelClause(final SelectStatement selectStatement) {
        if (lexerEngine.skipIfEqual(OracleKeyword.MAIN)) {
            lexerEngine.nextToken();
        }
        skipQueryPartitionClause(selectStatement);
        lexerEngine.accept(OracleKeyword.DIMENSION);
        lexerEngine.accept(DefaultKeyword.BY);
        lexerEngine.skipParentheses(selectStatement);
        lexerEngine.accept(OracleKeyword.MEASURES);
        lexerEngine.skipParentheses(selectStatement);
        skipCellReferenceOptions();
        skipModelRulesClause(selectStatement);
    }
    
    private void skipModelRulesClause(final SelectStatement selectStatement) {
        if (lexerEngine.skipIfEqual(OracleKeyword.RULES)) {
            lexerEngine.skipIfEqual(DefaultKeyword.UPDATE);
            lexerEngine.skipIfEqual(OracleKeyword.UPSERT);
            if (lexerEngine.skipIfEqual(OracleKeyword.AUTOMATIC)) {
                lexerEngine.accept(DefaultKeyword.ORDER);
            } else if (lexerEngine.skipIfEqual(OracleKeyword.SEQUENTIAL)) {
                lexerEngine.accept(DefaultKeyword.ORDER);
            }
        }
        if (lexerEngine.skipIfEqual(DefaultKeyword.ITERATE)) {
            lexerEngine.skipParentheses(selectStatement);
            if (lexerEngine.skipIfEqual(DefaultKeyword.UNTIL)) {
                lexerEngine.skipParentheses(selectStatement);
            }
        }
        lexerEngine.skipParentheses(selectStatement);
    }
    
    private void skipQueryPartitionClause(final SelectStatement selectStatement) {
        if (!lexerEngine.skipIfEqual(OracleKeyword.PARTITION)) {
            return;
        }
        lexerEngine.accept(DefaultKeyword.BY);
        if (!lexerEngine.equalAny(Symbol.LEFT_PAREN)) {
            throw new UnsupportedOperationException("Cannot support PARTITION BY without ()");
        }
        lexerEngine.skipParentheses(selectStatement);
    }
    
    private void skipModelColumnClause() {
        throw new SQLParsingUnsupportedException(lexerEngine.getCurrentToken().getType());
    }
}
