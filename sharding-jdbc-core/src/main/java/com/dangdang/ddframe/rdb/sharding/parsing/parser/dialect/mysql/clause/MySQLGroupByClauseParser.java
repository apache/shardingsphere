package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.clause;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.mysql.MySQLKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.GroupByClauseParser;

/**
 * Group by clause parser for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLGroupByClauseParser extends GroupByClauseParser {
    
    public MySQLGroupByClauseParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected Keyword[] getSkippedKeywordAfterGroupBy() {
        return new Keyword[] {DefaultKeyword.WITH, MySQLKeyword.ROLLUP};
    }
}
