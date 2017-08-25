package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.clause.facade;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.facade.AbstractUpdateClauseParserFacade;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.UpdateSetItemsClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.clause.OracleTableReferencesClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.clause.OracleWhereClauseParser;

/**
 * Update clause parser facade for Oracle.
 *
 * @author zhangliang
 */
public final class OracleUpdateClauseParserFacade extends AbstractUpdateClauseParserFacade {
    
    public OracleUpdateClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new OracleTableReferencesClauseParser(shardingRule, lexerEngine), new UpdateSetItemsClauseParser(lexerEngine), new OracleWhereClauseParser(lexerEngine));
    }
}
