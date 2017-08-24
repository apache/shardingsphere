package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.AbstractDeleteClauseParserFacade;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.TableClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.WhereClauseParser;

/**
 * PostgreSQL的DELETE从句解析器门面类.
 *
 * @author zhangliang
 */
public final class PostgreSQLDeleteClauseParserFacade extends AbstractDeleteClauseParserFacade {
    
    public PostgreSQLDeleteClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new TableClauseParser(shardingRule, lexerEngine), new WhereClauseParser(lexerEngine));
    }
}
