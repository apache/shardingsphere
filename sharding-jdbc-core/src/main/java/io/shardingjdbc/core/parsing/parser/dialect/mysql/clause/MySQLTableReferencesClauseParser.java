package io.shardingjdbc.core.parsing.parser.dialect.mysql.clause;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.dialect.mysql.MySQLKeyword;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Keyword;
import io.shardingjdbc.core.parsing.parser.clause.TableReferencesClauseParser;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;

/**
 * Select table references clause parser for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLTableReferencesClauseParser extends TableReferencesClauseParser {
    
    public MySQLTableReferencesClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine);
    }
    
    @Override
    protected void parseTableReference(final SQLStatement sqlStatement, final boolean isSingleTableOnly) {
        parseTableFactor(sqlStatement, isSingleTableOnly);
        parsePartition();
        parseIndexHint(sqlStatement);
    }
    
    private void parsePartition() {
        getLexerEngine().unsupportedIfEqual(MySQLKeyword.PARTITION);
    }
    
    private void parseIndexHint(final SQLStatement sqlStatement) {
        if (getLexerEngine().skipIfEqual(DefaultKeyword.USE, MySQLKeyword.IGNORE, MySQLKeyword.FORCE)) {
            getLexerEngine().skipAll(DefaultKeyword.INDEX, DefaultKeyword.KEY, DefaultKeyword.FOR, DefaultKeyword.JOIN, DefaultKeyword.ORDER, DefaultKeyword.GROUP, DefaultKeyword.BY);
            getLexerEngine().skipParentheses(sqlStatement);
        }
    }
    
    @Override
    protected Keyword[] getKeywordsForJoinType() {
        return new Keyword[] {MySQLKeyword.STRAIGHT_JOIN};
    }
}
