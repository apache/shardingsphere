package io.shardingjdbc.core.parsing.parser.dialect.sqlserver.clause;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.dialect.sqlserver.SQLServerKeyword;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Keyword;
import io.shardingjdbc.core.parsing.parser.clause.TableReferencesClauseParser;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;

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
