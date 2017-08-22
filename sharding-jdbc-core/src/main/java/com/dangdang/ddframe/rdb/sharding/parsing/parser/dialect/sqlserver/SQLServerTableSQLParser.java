package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.TableSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectStatement;

/**
 * SQLServer Table解析器.
 *
 * @author zhangliang
 */
public final class SQLServerTableSQLParser extends TableSQLParser {
    
    public SQLServerTableSQLParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine);
    }
    
    @Override
    public void parseJoinTable(final SelectStatement selectStatement) {
        if (getLexerEngine().skipIfEqual(DefaultKeyword.WITH)) {
            getLexerEngine().skipParentheses(selectStatement);
        }
        super.parseJoinTable(selectStatement);
    }
}
