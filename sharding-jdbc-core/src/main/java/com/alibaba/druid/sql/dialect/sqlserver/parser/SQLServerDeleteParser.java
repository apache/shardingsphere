package com.alibaba.druid.sql.dialect.sqlserver.parser;

import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.AbstractDeleteParser;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;

import java.util.List;

/**
 * SQLServer Delete语句解析器.
 *
 * @author zhangliang
 */
public final class SQLServerDeleteParser extends AbstractDeleteParser {
    
    public SQLServerDeleteParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(shardingRule, parameters, exprParser);
    }
    
    @Override
    protected void parseBetweenDeleteAndTable() {
        ((SQLServerExprParser) getExprParser()).parseTop();
        ((SQLServerExprParser) getExprParser()).parserOutput();
        if (getLexer().equalToken(Token.FROM)) {
            getLexer().nextToken();
        }
    }
    
    @Override
    protected void parseBetweenTableAndWhere() {
    }
}
