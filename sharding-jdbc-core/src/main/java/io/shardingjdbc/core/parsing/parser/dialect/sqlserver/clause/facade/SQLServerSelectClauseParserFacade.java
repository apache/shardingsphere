package io.shardingjdbc.core.parsing.parser.dialect.sqlserver.clause.facade;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.clause.DistinctClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.GroupByClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.HavingClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.facade.AbstractSelectClauseParserFacade;
import io.shardingjdbc.core.parsing.parser.dialect.sqlserver.clause.SQLServerOrderByClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.sqlserver.clause.SQLServerSelectListClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.sqlserver.clause.SQLServerSelectRestClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.sqlserver.clause.SQLServerTableReferencesClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.sqlserver.clause.SQLServerWhereClauseParser;

/**
 * Select clause parser facade for SQLServer.
 *
 * @author zhangliang
 */
public final class SQLServerSelectClauseParserFacade extends AbstractSelectClauseParserFacade {
    
    public SQLServerSelectClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new DistinctClauseParser(lexerEngine), new SQLServerSelectListClauseParser(shardingRule, lexerEngine),
                new SQLServerTableReferencesClauseParser(shardingRule, lexerEngine), new SQLServerWhereClauseParser(lexerEngine), new GroupByClauseParser(lexerEngine),
                new HavingClauseParser(lexerEngine), new SQLServerOrderByClauseParser(lexerEngine), new SQLServerSelectRestClauseParser(lexerEngine));
    }
}
