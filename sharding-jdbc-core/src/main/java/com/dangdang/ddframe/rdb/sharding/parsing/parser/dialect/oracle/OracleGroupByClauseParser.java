package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.GroupByClauseParser;

/**
 * Oracle分组从句解析器.
 *
 * @author zhangliang
 */
public final class OracleGroupByClauseParser extends GroupByClauseParser {
    
    public OracleGroupByClauseParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected Keyword[] getUnsupportedKeywordBeforeGroupByItem() {
        return new Keyword[] {OracleKeyword.ROLLUP, OracleKeyword.CUBE, OracleKeyword.GROUPING};
    }
}
