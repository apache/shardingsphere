package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.InsertIntoClauseParser;

/**
 * Insert into clause parser for Oracle.
 *
 * @author zhangliang
 */
public final class OracleInsertIntoClauseParser extends InsertIntoClauseParser {
    
    public OracleInsertIntoClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(lexerEngine, new OracleTableReferencesClauseParser(shardingRule, lexerEngine));
    }
    
    @Override
    protected Keyword[] getUnsupportedKeywordsBeforeInto() {
        return new Keyword[] {DefaultKeyword.ALL, OracleKeyword.FIRST};
    }
}
