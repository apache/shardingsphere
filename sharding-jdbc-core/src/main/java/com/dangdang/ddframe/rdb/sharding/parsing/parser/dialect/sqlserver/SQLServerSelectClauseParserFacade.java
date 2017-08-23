package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.AbstractSelectClauseParserFacade;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.DistinctClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.GroupByClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.HavingClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.SelectRestClauseParser;

/**
 * SQLServer的SELECT从句解析器门面类.
 *
 * @author zhangliang
 */
public final class SQLServerSelectClauseParserFacade extends AbstractSelectClauseParserFacade {
    
    public SQLServerSelectClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new DistinctClauseParser(lexerEngine), new SQLServerSelectListClauseParser(shardingRule, lexerEngine),
                new SQLServerTableClauseParser(shardingRule, lexerEngine), new SQLServerWhereClauseParser(lexerEngine), new GroupByClauseParser(lexerEngine),
                new HavingClauseParser(lexerEngine), new SQLServerOrderByClauseParser(lexerEngine), new SelectRestClauseParser(lexerEngine));
    }
}
