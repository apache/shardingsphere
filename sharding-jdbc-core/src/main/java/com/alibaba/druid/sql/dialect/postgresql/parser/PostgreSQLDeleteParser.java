package com.alibaba.druid.sql.dialect.postgresql.parser;

import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.AbstractDeleteParser;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;

import java.util.List;

/**
 * PostgreSQL Delete语句解析器.
 *
 * @author zhangliang
 */
public final class PostgreSQLDeleteParser extends AbstractDeleteParser {
    
    public PostgreSQLDeleteParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(shardingRule, parameters, exprParser);
    }
    
    @Override
    protected void skipBetweenDeleteAndTable() {
        getLexer().skipIfEqual(Token.FROM);
        getLexer().skipIfEqual(Token.ONLY);
    }
}
