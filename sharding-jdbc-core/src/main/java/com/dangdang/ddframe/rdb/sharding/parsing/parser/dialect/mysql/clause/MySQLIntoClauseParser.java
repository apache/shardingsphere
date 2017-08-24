package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.mysql.MySQLKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.IntoClauseParser;

/**
 * MySQL的INTO从句解析器.
 *
 * @author zhangliang
 */
public final class MySQLIntoClauseParser extends IntoClauseParser {
    
    public MySQLIntoClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(lexerEngine, new MySQLTableClauseParser(shardingRule, lexerEngine));
    }
    
    @Override
    protected Keyword[] getSkippedKeywordsBetweenTableAndValues() {
        return new Keyword[] {MySQLKeyword.PARTITION};
    }
}
