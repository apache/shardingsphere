package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.clause.facade;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.facade.AbstractUpdateClauseParserFacade;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.UpdateSetItemsClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.clause.SQLServerTableReferenceClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.clause.SQLServerWhereClauseParser;

/**
 * SQLServer的UPDATE从句解析器门面类.
 *
 * @author zhangliang
 */
public final class SQLServerUpdateClauseParserFacade extends AbstractUpdateClauseParserFacade {
    
    public SQLServerUpdateClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new SQLServerTableReferenceClauseParser(shardingRule, lexerEngine), new UpdateSetItemsClauseParser(lexerEngine), new SQLServerWhereClauseParser(lexerEngine));
    }
}
