package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.clause.facade;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.WhereClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.facade.AbstractDeleteClauseParserFacade;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.clause.PostgreSQLTableReferencesClauseParser;

/**
 * Delete clause parser facade for PostgreSQL.
 *
 * @author zhangliang
 */
public final class PostgreSQLDeleteClauseParserFacade extends AbstractDeleteClauseParserFacade {
    
    public PostgreSQLDeleteClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new PostgreSQLTableReferencesClauseParser(shardingRule, lexerEngine), new WhereClauseParser(lexerEngine));
    }
}
