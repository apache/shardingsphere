package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.mysql.MySQLKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.TableReferencesClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.SQLStatement;

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
