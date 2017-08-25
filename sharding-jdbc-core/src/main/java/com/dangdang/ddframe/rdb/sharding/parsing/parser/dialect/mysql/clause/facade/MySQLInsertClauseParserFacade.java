package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.clause.facade;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.InsertColumnsClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.facade.AbstractInsertClauseParserFacade;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.clause.MySQLInsertIntoClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.clause.MySQLInsertSetClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.clause.MySQLInsertValuesClauseParser;

/**
 * Insert clause parser facade for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLInsertClauseParserFacade extends AbstractInsertClauseParserFacade {
    
    public MySQLInsertClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new MySQLInsertIntoClauseParser(shardingRule, lexerEngine), new InsertColumnsClauseParser(shardingRule, lexerEngine), 
                new MySQLInsertValuesClauseParser(shardingRule, lexerEngine), new MySQLInsertSetClauseParser(shardingRule, lexerEngine));
    }
}
