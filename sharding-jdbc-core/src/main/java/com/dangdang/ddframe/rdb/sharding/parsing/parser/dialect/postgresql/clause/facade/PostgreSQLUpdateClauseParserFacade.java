package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.clause.facade;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.UpdateSetItemsClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.WhereClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.facade.AbstractUpdateClauseParserFacade;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.clause.PostgreSQLTableReferencesClauseParser;

/**
 * Update clause parser facade for PostgreSQL.
 *
 * @author zhangliang
 */
public final class PostgreSQLUpdateClauseParserFacade extends AbstractUpdateClauseParserFacade {
    
    public PostgreSQLUpdateClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new PostgreSQLTableReferencesClauseParser(shardingRule, lexerEngine), new UpdateSetItemsClauseParser(lexerEngine), new WhereClauseParser(lexerEngine));
    }
}
