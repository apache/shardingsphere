package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.clause.facade;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.DistinctClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.GroupByClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.HavingClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.SelectListClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.WhereClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.facade.AbstractSelectClauseParserFacade;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.clause.PostgreSQLOrderByClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.clause.PostgreSQLSelectRestClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.clause.PostgreSQLTableReferencesClauseParser;

/**
 * Select clause parser facade for PostgreSQL.
 *
 * @author zhangliang
 */
public final class PostgreSQLSelectClauseParserFacade extends AbstractSelectClauseParserFacade {
    
    public PostgreSQLSelectClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new DistinctClauseParser(lexerEngine), new SelectListClauseParser(shardingRule, lexerEngine),
                new PostgreSQLTableReferencesClauseParser(shardingRule, lexerEngine), new WhereClauseParser(lexerEngine), new GroupByClauseParser(lexerEngine), new HavingClauseParser(lexerEngine), 
                new PostgreSQLOrderByClauseParser(lexerEngine), new PostgreSQLSelectRestClauseParser(lexerEngine));
    }
}
