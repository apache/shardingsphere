package io.shardingjdbc.core.parsing.parser.dialect.oracle.clause;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.dialect.oracle.OracleKeyword;
import io.shardingjdbc.core.parsing.parser.clause.SQLClauseParser;
import lombok.RequiredArgsConstructor;

/**
 * Model clause parser for Oracle.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class OracleModelClauseParser implements SQLClauseParser {
    
    private final LexerEngine lexerEngine;
    
    /**
     * Parse model.
     */
    public void parse() {
        lexerEngine.unsupportedIfEqual(OracleKeyword.MODEL);
    }
}
