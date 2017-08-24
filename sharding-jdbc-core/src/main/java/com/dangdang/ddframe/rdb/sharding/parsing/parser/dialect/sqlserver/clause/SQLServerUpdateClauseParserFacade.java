package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.AbstractUpdateClauseParserFacade;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.SetItemsClauseParser;

/**
 * SQLServer的UPDATE从句解析器门面类.
 *
 * @author zhangliang
 */
public final class SQLServerUpdateClauseParserFacade extends AbstractUpdateClauseParserFacade {
    
    public SQLServerUpdateClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new SQLServerTableClauseParser(shardingRule, lexerEngine), new SetItemsClauseParser(lexerEngine), new SQLServerWhereClauseParser(lexerEngine));
    }
}
