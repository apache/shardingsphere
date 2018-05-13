package io.shardingjdbc.core.parsing.parser.dialect.mysql.clause;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.dialect.mysql.MySQLKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Keyword;
import io.shardingjdbc.core.parsing.parser.clause.InsertValuesClauseParser;

/**
 * Insert values clause parser for MySQL.
 *
 * @author zhangliang
 */
public class MySQLInsertValuesClauseParser extends InsertValuesClauseParser {
    
    public MySQLInsertValuesClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine);
    }
    
    @Override
    protected Keyword[] getSynonymousKeywordsForValues() {
        return new Keyword[] {MySQLKeyword.VALUE};
    }
}
