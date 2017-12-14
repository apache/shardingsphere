package io.shardingjdbc.core.parsing.parser.dialect.postgresql.clause.facade;

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.clause.DistinctClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.GroupByClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.HavingClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.SelectListClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.WhereClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.facade.AbstractSelectClauseParserFacade;
import io.shardingjdbc.core.parsing.parser.dialect.postgresql.clause.PostgreSQLOrderByClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.postgresql.clause.PostgreSQLSelectRestClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.postgresql.clause.PostgreSQLTableReferencesClauseParser;

/**
 * Select clause parser facade for PostgreSQL.
 *
 * @author zhangliang
 */
public final class PostgreSQLSelectClauseParserFacade extends AbstractSelectClauseParserFacade {
    
    public PostgreSQLSelectClauseParserFacade(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(new DistinctClauseParser(lexerEngine), new SelectListClauseParser(shardingRule, lexerEngine),
                new PostgreSQLTableReferencesClauseParser(shardingRule, lexerEngine), 
                new WhereClauseParser(DatabaseType.PostgreSQL, lexerEngine), new GroupByClauseParser(lexerEngine), new HavingClauseParser(lexerEngine), 
                new PostgreSQLOrderByClauseParser(lexerEngine), new PostgreSQLSelectRestClauseParser(lexerEngine));
    }
}
