package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.mysql.MySQLKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.InsertIntoClauseParser;

/**
 * Insert into clause parser for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLInsertIntoClauseParser extends InsertIntoClauseParser {
    
    public MySQLInsertIntoClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(lexerEngine, new MySQLTableReferencesClauseParser(shardingRule, lexerEngine));
    }
    
    @Override
    protected Keyword[] getSkippedKeywordsBetweenTableAndValues() {
        return new Keyword[] {MySQLKeyword.PARTITION};
    }
}
