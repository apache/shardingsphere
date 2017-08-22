package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.SQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.WhereSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectStatement;

import java.util.Collections;

/**
 * Oracle HierarchicalQuery解析器.
 *
 * @author zhangliang
 */
public final class OracleHierarchicalQueryClauseParser implements SQLParser {
    
    private final ShardingRule shardingRule;
    
    private final LexerEngine lexerEngine;
    
    private final WhereSQLParser whereSQLParser;
    
    public OracleHierarchicalQueryClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        this.shardingRule = shardingRule;
        this.lexerEngine = lexerEngine;
        whereSQLParser = new WhereSQLParser(lexerEngine);
        
    }
    
    /**
     * 解析HierarchicalQuery语句.
     * 
     * @param selectStatement Select SQL语句对象
     */
    public void parse(final SelectStatement selectStatement) {
        skipConnect(selectStatement);
        skipStart(selectStatement);
        skipConnect(selectStatement);
    }
    
    private void skipStart(final SelectStatement selectStatement) {
        if (!lexerEngine.skipIfEqual(OracleKeyword.START)) {
            return;
        }
        lexerEngine.accept(DefaultKeyword.WITH);
        whereSQLParser.parseComparisonCondition(shardingRule, selectStatement, Collections.<SelectItem>emptyList());
    }
    
    private void skipConnect(final SelectStatement selectStatement) {
        if (!lexerEngine.skipIfEqual(OracleKeyword.CONNECT)) {
            return;
        }
        lexerEngine.accept(DefaultKeyword.BY);
        lexerEngine.skipIfEqual(OracleKeyword.PRIOR);
        if (lexerEngine.skipIfEqual(OracleKeyword.NOCYCLE)) {
            lexerEngine.skipIfEqual(OracleKeyword.PRIOR);
        }
        whereSQLParser.parseComparisonCondition(shardingRule, selectStatement, Collections.<SelectItem>emptyList());
    }
}
