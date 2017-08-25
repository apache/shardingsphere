package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.clause.facade;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.facade.AbstractDeleteClauseParserFacade;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.clause.OracleTableReferencesClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.clause.OracleWhereClauseParser;

/**
 * Delete clause parser facade for Oracle.
 *
 * @author zhangliang
 */
public final class OracleDeleteClauseParserFacade extends AbstractDeleteClauseParserFacade {
    
    public OracleDeleteClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new OracleTableReferencesClauseParser(shardingRule, lexerEngine), new OracleWhereClauseParser(lexerEngine));
    }
}
