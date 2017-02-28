package com.alibaba.druid.sql.dialect.sqlserver.parser;

import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.AbstractUpdateParser;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;

import java.util.List;

/**
 * SQLServer Update语句解析器.
 *
 * @author zhangliang
 */
public final class SQLServerUpdateParser extends AbstractUpdateParser {
    
    public SQLServerUpdateParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(shardingRule, parameters, exprParser);
    }
    
    @Override
    protected void parseBetweenUpdateAndTable() {
        ((SQLServerExprParser) getExprParser()).parseTop();
    }
    
    @Override
    protected void parseBetweenSetAndWhere() {
        ((SQLServerExprParser) getExprParser()).parserOutput();
        if (getLexer().equalToken(Token.FROM)) {
            getLexer().nextToken();
            getExprParser().createSelectParser(getShardingRule(), getParameters()).parseTableSource();
        }
    }
}
