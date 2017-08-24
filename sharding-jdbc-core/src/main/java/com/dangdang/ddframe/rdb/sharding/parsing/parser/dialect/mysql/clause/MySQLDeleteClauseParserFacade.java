package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.AbstractDeleteClauseParserFacade;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.WhereClauseParser;

/**
 * MySQL的DELETE从句解析器门面类.
 *
 * @author zhangliang
 */
public final class MySQLDeleteClauseParserFacade extends AbstractDeleteClauseParserFacade {
    
    public MySQLDeleteClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new MySQLTableClauseParser(shardingRule, lexerEngine), new WhereClauseParser(lexerEngine));
    }
}
