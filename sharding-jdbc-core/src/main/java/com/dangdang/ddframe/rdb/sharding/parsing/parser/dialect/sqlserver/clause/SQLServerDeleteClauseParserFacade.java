package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.AbstractDeleteClauseParserFacade;

/**
 * SQLServer的DELETE从句解析器门面类.
 *
 * @author zhangliang
 */
public final class SQLServerDeleteClauseParserFacade extends AbstractDeleteClauseParserFacade {
    
    public SQLServerDeleteClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new SQLServerTableClauseParser(shardingRule, lexerEngine), new SQLServerWhereClauseParser(lexerEngine));
    }
}
