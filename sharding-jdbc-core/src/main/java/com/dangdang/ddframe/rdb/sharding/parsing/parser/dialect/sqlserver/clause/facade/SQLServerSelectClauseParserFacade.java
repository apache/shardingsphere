package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.clause.facade;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.DistinctClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.GroupByClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.HavingClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.facade.AbstractSelectClauseParserFacade;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.clause.SQLServerOrderByClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.clause.SQLServerSelectListClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.clause.SQLServerSelectRestClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.clause.SQLServerTableReferencesClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.clause.SQLServerWhereClauseParser;

/**
 * Select clause parser facade for SQLServer.
 *
 * @author zhangliang
 */
public final class SQLServerSelectClauseParserFacade extends AbstractSelectClauseParserFacade {
    
    public SQLServerSelectClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new DistinctClauseParser(lexerEngine), new SQLServerSelectListClauseParser(shardingRule, lexerEngine),
                new SQLServerTableReferencesClauseParser(shardingRule, lexerEngine), new SQLServerWhereClauseParser(lexerEngine), new GroupByClauseParser(lexerEngine),
                new HavingClauseParser(lexerEngine), new SQLServerOrderByClauseParser(lexerEngine), new SQLServerSelectRestClauseParser(lexerEngine));
    }
}
