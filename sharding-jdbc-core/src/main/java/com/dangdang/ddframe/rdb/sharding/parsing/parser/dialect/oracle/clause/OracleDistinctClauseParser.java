package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.clause;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.DistinctClauseParser;

/**
 * Distinct clause parser for Oracle.
 *
 * @author zhangliang
 */
public class OracleDistinctClauseParser extends DistinctClauseParser {
    
    public OracleDistinctClauseParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected Keyword[] getSynonymousKeywordsForDistinct() {
        return new Keyword[] {DefaultKeyword.UNIQUE};
    }
}
