package io.shardingjdbc.core.parsing.parser.dialect.mysql.clause;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.dialect.mysql.MySQLKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Keyword;
import io.shardingjdbc.core.parsing.parser.clause.InsertIntoClauseParser;

/**
 * Insert into clause parser for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLInsertIntoClauseParser extends InsertIntoClauseParser {
    
    public MySQLInsertIntoClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(lexerEngine, new MySQLTableReferencesClauseParser(shardingRule, lexerEngine));
    }
    
    @Override
    protected Keyword[] getSkippedKeywordsBetweenTableAndValues() {
        return new Keyword[] {MySQLKeyword.PARTITION};
    }
}
