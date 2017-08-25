package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.clause.facade;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.InsertColumnsClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.InsertSetClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.InsertValuesClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.facade.AbstractInsertClauseParserFacade;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.clause.SQLServerInsertIntoClauseParser;

/**
 * Insert clause parser facade for SQLServer.
 *
 * @author zhangliang
 */
public final class SQLServerInsertClauseParserFacade extends AbstractInsertClauseParserFacade {
    
    public SQLServerInsertClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new SQLServerInsertIntoClauseParser(shardingRule, lexerEngine), new InsertColumnsClauseParser(shardingRule, lexerEngine), 
                new InsertValuesClauseParser(shardingRule, lexerEngine), new InsertSetClauseParser(shardingRule, lexerEngine));
    }
}
