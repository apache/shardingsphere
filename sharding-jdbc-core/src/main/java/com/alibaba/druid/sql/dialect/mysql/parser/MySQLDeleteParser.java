package com.alibaba.druid.sql.dialect.mysql.parser;

import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.AbstractDeleteParser;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;

import java.util.List;

/**
 * MySQL Delete语句解析器.
 *
 * @author zhangliang
 */
public final class MySQLDeleteParser extends AbstractDeleteParser {
    
    public MySQLDeleteParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(shardingRule, parameters, exprParser);
    }
    
    @Override
    protected void skipBetweenDeleteAndTable() {
        while (getExprParser().getLexer().equalToken(Token.LOW_PRIORITY) || getExprParser().getLexer().equalToken(Token.QUICK) || getExprParser().getLexer().equalToken(Token.IGNORE)) {
            getExprParser().getLexer().nextToken();
        }
        getExprParser().getLexer().skipIfEqual(Token.FROM);
    }
}
