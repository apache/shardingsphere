package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.InsertSetClauseParser;

/**
 * Insert set clause parser for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLInsertSetClauseParser extends InsertSetClauseParser {
    
    public MySQLInsertSetClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine);
    }
    
    @Override
    protected Keyword[] getCustomizedInsertKeywords() {
        return new Keyword[] {DefaultKeyword.SET};
    }
}
