package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.clause.facade;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.HavingClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.OrderByClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.SelectListClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.WhereClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.facade.AbstractSelectClauseParserFacade;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.clause.MySQLDistinctClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.clause.MySQLGroupByClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.clause.MySQLSelectRestClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.clause.MySQLTableReferencesClauseParser;

/**
 * Select clause parser facade for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLSelectClauseParserFacade extends AbstractSelectClauseParserFacade {
    
    public MySQLSelectClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new MySQLDistinctClauseParser(lexerEngine), new SelectListClauseParser(shardingRule, lexerEngine),
                new MySQLTableReferencesClauseParser(shardingRule, lexerEngine), new WhereClauseParser(lexerEngine), new MySQLGroupByClauseParser(lexerEngine),
                new HavingClauseParser(lexerEngine), new OrderByClauseParser(lexerEngine), new MySQLSelectRestClauseParser(lexerEngine));
    }
}
