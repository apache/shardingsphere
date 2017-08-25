package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.clause.facade;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.InsertColumnsClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.InsertSetClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.InsertValuesClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.facade.AbstractInsertClauseParserFacade;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.clause.PostgreSQLInsertIntoClauseParser;

/**
 * Insert clause parser facade for PostgreSQL.
 *
 * @author zhangliang
 */
public final class PostgreSQLInsertClauseParserFacade extends AbstractInsertClauseParserFacade {
    
    public PostgreSQLInsertClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new PostgreSQLInsertIntoClauseParser(shardingRule, lexerEngine), new InsertColumnsClauseParser(shardingRule, lexerEngine), 
                new InsertValuesClauseParser(shardingRule, lexerEngine), new InsertSetClauseParser(shardingRule, lexerEngine));
    }
}
