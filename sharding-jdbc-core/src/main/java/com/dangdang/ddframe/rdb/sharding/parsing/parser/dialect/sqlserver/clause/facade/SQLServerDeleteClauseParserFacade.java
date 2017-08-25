package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.clause.facade;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.facade.AbstractDeleteClauseParserFacade;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.clause.SQLServerTableReferencesClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.clause.SQLServerWhereClauseParser;

/**
 * Delete clause parser facade for SQLServer.
 *
 * @author zhangliang
 */
public final class SQLServerDeleteClauseParserFacade extends AbstractDeleteClauseParserFacade {
    
    public SQLServerDeleteClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new SQLServerTableReferencesClauseParser(shardingRule, lexerEngine), new SQLServerWhereClauseParser(lexerEngine));
    }
}
