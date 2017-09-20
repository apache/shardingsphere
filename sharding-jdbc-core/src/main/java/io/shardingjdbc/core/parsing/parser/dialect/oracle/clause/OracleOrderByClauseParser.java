package io.shardingjdbc.core.parsing.parser.dialect.oracle.clause;

import io.shardingjdbc.core.constant.OrderType;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.dialect.oracle.OracleKeyword;
import io.shardingjdbc.core.parsing.parser.exception.SQLParsingException;
import io.shardingjdbc.core.parsing.parser.clause.OrderByClauseParser;

/**
 * Order by clause parser for Oracle.
 *
 * @author zhangliang
 */
public final class OracleOrderByClauseParser extends OrderByClauseParser {
    
    public OracleOrderByClauseParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected OrderType getNullOrderType() {
        if (!getLexerEngine().skipIfEqual(OracleKeyword.NULLS)) {
            return OrderType.ASC;
        }
        if (getLexerEngine().skipIfEqual(OracleKeyword.FIRST)) {
            return OrderType.ASC;
        }
        if (getLexerEngine().skipIfEqual(OracleKeyword.LAST)) {
            return OrderType.DESC;
        }
        throw new SQLParsingException(getLexerEngine());
    }
}
