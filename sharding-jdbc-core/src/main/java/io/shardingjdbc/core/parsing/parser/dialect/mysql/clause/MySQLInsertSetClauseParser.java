package io.shardingjdbc.core.parsing.parser.dialect.mysql.clause;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Keyword;
import io.shardingjdbc.core.parsing.parser.clause.InsertSetClauseParser;

/**
 * Insert set clause parser for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLInsertSetClauseParser extends InsertSetClauseParser {
    
    public MySQLInsertSetClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine);
    }
    
    @Override
    protected Keyword[] getCustomizedInsertKeywords() {
        return new Keyword[] {DefaultKeyword.SET};
    }
}
