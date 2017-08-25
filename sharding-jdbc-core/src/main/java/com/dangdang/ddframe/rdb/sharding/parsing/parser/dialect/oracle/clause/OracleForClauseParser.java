package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.clause;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.ExpressionClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.SQLClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.dql.select.SelectStatement;

/**
 * Select for clause parser for Oracle.
 *
 * @author zhangliang
 */
public final class OracleForClauseParser implements SQLClauseParser {
    
    private final LexerEngine lexerEngine;
    
    private final ExpressionClauseParser expressionClauseParser;
    
    public OracleForClauseParser(final LexerEngine lexerEngine) {
        this.lexerEngine = lexerEngine;
        expressionClauseParser = new ExpressionClauseParser(lexerEngine);
    }
    
    /**
     * Parse for.
     * 
     * @param selectStatement select statement
     */
    public void parse(final SelectStatement selectStatement) {
        if (!lexerEngine.skipIfEqual(DefaultKeyword.FOR)) {
            return;
        }
        lexerEngine.accept(DefaultKeyword.UPDATE);
        if (lexerEngine.skipIfEqual(DefaultKeyword.OF)) {
            do {
                expressionClauseParser.parse(selectStatement);
            } while (lexerEngine.skipIfEqual(Symbol.COMMA));
        }
        if (lexerEngine.equalAny(OracleKeyword.NOWAIT, OracleKeyword.WAIT)) {
            lexerEngine.nextToken();
        } else if (lexerEngine.skipIfEqual(OracleKeyword.SKIP)) {
            lexerEngine.accept(OracleKeyword.LOCKED);
        }
    }
}
