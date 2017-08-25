package com.dangdang.ddframe.rdb.sharding.parsing.parser.clause;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.dml.insert.InsertStatement;
import lombok.RequiredArgsConstructor;

/**
 * Insert into clause parser.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public class InsertIntoClauseParser implements SQLClauseParser {
    
    private final LexerEngine lexerEngine;
    
    private final TableReferencesClauseParser tableReferencesClauseParser;
    
    /**
     * Parse insert into.
     *
     * @param insertStatement insert statement
     */
    public void parse(final InsertStatement insertStatement) {
        lexerEngine.unsupportedIfEqual(getUnsupportedKeywordsBeforeInto());
        lexerEngine.skipUntil(DefaultKeyword.INTO);
        lexerEngine.nextToken();
        tableReferencesClauseParser.parse(insertStatement, true);
        skipBetweenTableAndValues(insertStatement);
    }
    
    protected Keyword[] getUnsupportedKeywordsBeforeInto() {
        return new Keyword[0];
    }
    
    private void skipBetweenTableAndValues(final InsertStatement insertStatement) {
        while (lexerEngine.skipIfEqual(getSkippedKeywordsBetweenTableAndValues())) {
            lexerEngine.nextToken();
            if (lexerEngine.equalAny(Symbol.LEFT_PAREN)) {
                lexerEngine.skipParentheses(insertStatement);
            }
        }
    }
    
    protected Keyword[] getSkippedKeywordsBetweenTableAndValues() {
        return new Keyword[0];
    }
}
