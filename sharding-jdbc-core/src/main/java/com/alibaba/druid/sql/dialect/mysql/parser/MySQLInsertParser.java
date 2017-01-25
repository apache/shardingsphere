package com.alibaba.druid.sql.dialect.mysql.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.context.InsertSQLContext;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.AbstractInsertParser;
import com.alibaba.druid.sql.parser.ParserUtil;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.ParseContext;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * MySQL Insert语句解析器.
 *
 * @author zhangliang
 */
public final class MySQLInsertParser extends AbstractInsertParser {
    
    public MySQLInsertParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(shardingRule, parameters, exprParser);
    }
    
    @Override
    protected void parseCustomizedInsert(final InsertSQLContext sqlContext) {
        parseInsertSet(sqlContext);
    }
    
    private void parseInsertSet(final InsertSQLContext sqlContext) {
        ParserUtil parserUtil = new ParserUtil(getExprParser(), getShardingRule(), getParameters(), sqlContext.getTable(), sqlContext, 0);
        ParseContext parseContext = parserUtil.getParseContext();
        do {
            getLexer().nextToken();
            SQLName column = getExprParser().name();
            accept(Token.EQ);
            SQLExpr value = getExprParser().expr();
            parseContext.addCondition(column.getSimpleName(), sqlContext.getTable().getName(), Condition.BinaryOperator.EQUAL, value, getExprParser().getDbType(), getParameters());
        } while (getLexer().equalToken(Token.COMMA));
        sqlContext.getConditionContexts().add(parseContext.getCurrentConditionContext());
    }
    
    @Override
    protected Set<String> getIdentifiersBetweenTableAndValues() {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(Token.PARTITION.getName());
        return result;
    }
    
    @Override
    protected Set<String> getValuesIdentifiers() {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(Token.VALUES.getName());
        result.add(Token.VALUE.getName());
        return result;
    }
    
    @Override
    protected Set<String> getCustomizedInsertIdentifiers() {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(Token.SET.getName());
        return result;
    }
}
