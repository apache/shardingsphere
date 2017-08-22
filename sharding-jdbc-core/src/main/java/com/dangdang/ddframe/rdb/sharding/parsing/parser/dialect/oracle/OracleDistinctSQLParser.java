package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.DistinctSQLParser;

/**
 * Oracle Distinct解析器.
 *
 * @author zhangliang
 */
public class OracleDistinctSQLParser extends DistinctSQLParser {
    
    public OracleDistinctSQLParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected Keyword[] getSynonymousKeywordsForDistinct() {
        return new Keyword[] {DefaultKeyword.UNIQUE};
    }
}
