package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.IntoClauseParser;

/**
 * SQLServer的INTO从句解析器.
 *
 * @author zhangliang
 */
public final class SQLServerIntoClauseParser extends IntoClauseParser {
    
    public SQLServerIntoClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(lexerEngine, new SQLServerTableClauseParser(shardingRule, lexerEngine));
    }
}
