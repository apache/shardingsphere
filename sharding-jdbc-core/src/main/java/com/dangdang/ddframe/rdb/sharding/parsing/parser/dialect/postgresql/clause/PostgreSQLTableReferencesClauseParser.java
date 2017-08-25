package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.postgresql.PostgreSQLKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.TableReferencesClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.SQLStatement;

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
