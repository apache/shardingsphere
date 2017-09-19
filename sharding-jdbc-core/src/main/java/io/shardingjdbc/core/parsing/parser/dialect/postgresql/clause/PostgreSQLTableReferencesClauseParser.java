package io.shardingjdbc.core.parsing.parser.dialect.postgresql.clause;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.dialect.postgresql.PostgreSQLKeyword;
import io.shardingjdbc.core.parsing.parser.clause.TableReferencesClauseParser;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;

/**
 * Table references clause parser for PostgreSQL.
 *
 * @author zhangliang
 */
public final class PostgreSQLTableReferencesClauseParser extends TableReferencesClauseParser {
    
    public PostgreSQLTableReferencesClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine);
    }
    
    @Override
    protected void parseTableReference(final SQLStatement sqlStatement, final boolean isSingleTableOnly) {
        parseOnly();
        parseTableFactor(sqlStatement, isSingleTableOnly);
    }
    
    private void parseOnly() {
        getLexerEngine().skipIfEqual(PostgreSQLKeyword.ONLY);
    }
}
