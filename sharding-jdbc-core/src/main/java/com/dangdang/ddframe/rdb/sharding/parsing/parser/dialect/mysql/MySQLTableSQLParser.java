package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.TableSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectStatement;

/**
 * MySQL Table解析器.
 *
 * @author zhangliang
 */
public final class MySQLTableSQLParser extends TableSQLParser {
    
    public MySQLTableSQLParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine);
    }
    
    @Override
    public void parseJoinTable(final SelectStatement selectStatement) {
        if (getLexerEngine().equalAny(DefaultKeyword.USING)) {
            return;
        }
        if (getLexerEngine().equalAny(DefaultKeyword.USE)) {
            getLexerEngine().nextToken();
            skipIndexHint(selectStatement);
        }
        if (getLexerEngine().equalAny(OracleKeyword.IGNORE)) {
            getLexerEngine().nextToken();
            skipIndexHint(selectStatement);
        }
        if (getLexerEngine().equalAny(OracleKeyword.FORCE)) {
            getLexerEngine().nextToken();
            skipIndexHint(selectStatement);
        }
        super.parseJoinTable(selectStatement);
    }
    
    private void skipIndexHint(final SelectStatement selectStatement) {
        if (getLexerEngine().equalAny(DefaultKeyword.INDEX)) {
            getLexerEngine().nextToken();
        } else {
            getLexerEngine().accept(DefaultKeyword.KEY);
        }
        if (getLexerEngine().equalAny(DefaultKeyword.FOR)) {
            getLexerEngine().nextToken();
            if (getLexerEngine().equalAny(DefaultKeyword.JOIN)) {
                getLexerEngine().nextToken();
            } else if (getLexerEngine().equalAny(DefaultKeyword.ORDER)) {
                getLexerEngine().nextToken();
                getLexerEngine().accept(DefaultKeyword.BY);
            } else {
                getLexerEngine().accept(DefaultKeyword.GROUP);
                getLexerEngine().accept(DefaultKeyword.BY);
            }
        }
        getLexerEngine().skipParentheses(selectStatement);
    }
}
