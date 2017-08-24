package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.IntoClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.TableClauseParser;

/**
 * PostgreSQL的INTO从句解析器.
 *
 * @author zhangliang
 */
public final class PostgreSQLIntoClauseParser extends IntoClauseParser {
    
    public PostgreSQLIntoClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(lexerEngine, new TableClauseParser(shardingRule, lexerEngine));
    }
}
