package io.shardingjdbc.core.parsing.parser.dialect.oracle.clause;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.dialect.oracle.OracleKeyword;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Symbol;
import io.shardingjdbc.core.parsing.parser.clause.ExpressionClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.SQLClauseParser;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;

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
