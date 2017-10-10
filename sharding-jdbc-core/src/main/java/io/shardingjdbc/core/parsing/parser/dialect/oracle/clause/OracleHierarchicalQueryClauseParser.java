package io.shardingjdbc.core.parsing.parser.dialect.oracle.clause;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.dialect.oracle.OracleKeyword;
import io.shardingjdbc.core.parsing.parser.clause.SQLClauseParser;
import lombok.RequiredArgsConstructor;

/**
 * Hierarchical query clause parser for Oracle.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class OracleHierarchicalQueryClauseParser implements SQLClauseParser {
    
    private final LexerEngine lexerEngine;
    
    /**
     * Parse hierarchical query.
     */
    public void parse() {
        lexerEngine.unsupportedIfEqual(OracleKeyword.CONNECT, OracleKeyword.START);
    }
}
