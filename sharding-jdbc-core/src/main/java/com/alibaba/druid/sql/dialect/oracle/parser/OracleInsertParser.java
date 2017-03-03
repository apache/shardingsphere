package com.alibaba.druid.sql.dialect.oracle.parser;

import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.AbstractInsertParser;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

/**
 * Oracle Insert语句解析器.
 *
 * @author zhangliang
 */
public final class OracleInsertParser extends AbstractInsertParser {
    
    public OracleInsertParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(shardingRule, parameters, exprParser);
    }
    
    @Override
    protected Set<Token> getUnsupportedTokens() {
        return Sets.newHashSet(Token.ALL, Token.FIRST);
    }
}
