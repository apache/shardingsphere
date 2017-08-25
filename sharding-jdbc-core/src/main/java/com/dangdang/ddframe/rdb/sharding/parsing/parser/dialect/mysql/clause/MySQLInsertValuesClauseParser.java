package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.mysql.MySQLKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.InsertValuesClauseParser;

/**
 * Insert values clause parser for MySQL.
 *
 * @author zhangliang
 */
public class MySQLInsertValuesClauseParser extends InsertValuesClauseParser {
    
    public MySQLInsertValuesClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine);
    }
    
    @Override
    protected Keyword[] getSynonymousKeywordsForValues() {
        return new Keyword[] {MySQLKeyword.VALUE};
    }
}
