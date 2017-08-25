package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.sqlserver.SQLServerKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.TableReferencesClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.SQLStatement;

/**
 * Table references clause parser for SQLServer.
 *
 * @author zhangliang
 */
public final class SQLServerTableReferencesClauseParser extends TableReferencesClauseParser {
    
    public SQLServerTableReferencesClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine);
    }
    
    @Override
    protected void parseTableReference(final SQLStatement sqlStatement, final boolean isSingleTableOnly) {
        parseTableFactor(sqlStatement, isSingleTableOnly);
        parseTableSampleClause();
        parseTableHint(sqlStatement);
    }
    
    private void parseTableSampleClause() {
        getLexerEngine().unsupportedIfEqual(SQLServerKeyword.TABLESAMPLE);
    }
    
    private void parseTableHint(final SQLStatement sqlStatement) {
        if (getLexerEngine().skipIfEqual(DefaultKeyword.WITH)) {
            getLexerEngine().skipParentheses(sqlStatement);
        }
    }
    
    @Override
    protected Keyword[] getKeywordsForJoinType() {
        return new Keyword[] {SQLServerKeyword.APPLY, SQLServerKeyword.REDUCE, SQLServerKeyword.REPLICATE, SQLServerKeyword.REDISTRIBUTE};
    }
}
