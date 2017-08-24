package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.AbstractUpdateClauseParserFacade;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.SetItemsClauseParser;

/**
 * Oracle的UPDATE从句解析器门面类.
 *
 * @author zhangliang
 */
public final class OracleUpdateClauseParserFacade extends AbstractUpdateClauseParserFacade {
    
    public OracleUpdateClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new OracleTableClauseParser(shardingRule, lexerEngine), new SetItemsClauseParser(lexerEngine), new OracleWhereClauseParser(lexerEngine));
    }
}
