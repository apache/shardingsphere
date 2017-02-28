package com.alibaba.druid.sql.dialect.postgresql.parser;

import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.AbstractUpdateParser;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;

import java.util.List;

/**
 * PostgreSQL Update语句解析器.
 *
 * @author zhangliang
 */
public final class PostgreSQLUpdateParser extends AbstractUpdateParser {
    
    public PostgreSQLUpdateParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(shardingRule, parameters, exprParser);
    }
    
    @Override
    protected void parseBetweenUpdateAndTable() {
        if (getLexer().equalToken(Token.ONLY)) {
            getLexer().nextToken();
        }
    }
    
    @Override
    protected void parseBetweenSetAndWhere() {
        if (getLexer().equalToken(Token.FROM)) {
            getLexer().nextToken();
            getExprParser().createSelectParser(getShardingRule(), getParameters()).parseTableSource();
        }
    }
}
