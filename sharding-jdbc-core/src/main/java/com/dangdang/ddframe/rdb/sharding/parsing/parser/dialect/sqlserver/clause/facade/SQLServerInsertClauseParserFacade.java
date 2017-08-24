package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.clause.facade;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.InsertColumnsClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.facade.AbstractInsertClauseParserFacade;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.clause.SQLServerInsertIntoClauseParser;

/**
 * SQLServer的INSERT从句解析器门面类.
 *
 * @author zhangliang
 */
public final class SQLServerInsertClauseParserFacade extends AbstractInsertClauseParserFacade {
    
    public SQLServerInsertClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new SQLServerInsertIntoClauseParser(shardingRule, lexerEngine), new InsertColumnsClauseParser(shardingRule, lexerEngine));
    }
}
