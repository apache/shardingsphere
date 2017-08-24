package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.AbstractUpdateClauseParserFacade;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.SetItemsClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.WhereClauseParser;

/**
 * MySQL的UPDATE从句解析器门面类.
 *
 * @author zhangliang
 */
public final class MySQLUpdateClauseParserFacade extends AbstractUpdateClauseParserFacade {
    
    public MySQLUpdateClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new MySQLTableClauseParser(shardingRule, lexerEngine), new SetItemsClauseParser(lexerEngine), new WhereClauseParser(lexerEngine));
    }
}
