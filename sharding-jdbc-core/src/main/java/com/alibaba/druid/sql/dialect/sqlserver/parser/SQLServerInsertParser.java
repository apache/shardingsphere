package com.alibaba.druid.sql.dialect.sqlserver.parser;

import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.AbstractInsertParser;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * SQLServer Insert语句解析器.
 *
 * @author zhangliang
 */
public final class SQLServerInsertParser extends AbstractInsertParser {
    
    public SQLServerInsertParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(shardingRule, parameters, exprParser);
    }
    
    @Override
    protected Set<String> getIdentifiersBetweenIntoAndTable() {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(Token.OUTPUT.getName());
        return result;
    }
}
