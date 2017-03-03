package com.alibaba.druid.sql.dialect.mysql.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.context.InsertSQLContext;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.AbstractInsertParser;
import com.alibaba.druid.sql.parser.ParserUtil;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.ParseContext;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.List;
import java.util.Set;

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
        ParserUtil parserUtil = new ParserUtil(getExprParser(), getShardingRule(), getParameters(), sqlContext.getTables().get(0), sqlContext, 0);
        ParseContext parseContext = parserUtil.getParseContext();
        Collection<String> autoIncrementColumns = parseContext.getShardingRule().getAutoIncrementColumns(sqlContext.getTables().get(0).getName());
        do {
            getLexer().nextToken();
            Condition.Column column = getColumn(sqlContext, autoIncrementColumns);
            getLexer().nextToken();
            getLexer().accept(Token.EQ);
            SQLExpr value = getExprParser().expr();
            parseContext.addCondition(column.getColumnName(), column.getTableName(), Condition.BinaryOperator.EQUAL, value, getParameters());
        } while (getLexer().equalToken(Token.COMMA));
        sqlContext.getConditionContexts().add(parseContext.getCurrentConditionContext());
    }
    
    @Override
    protected Set<Token> getSkippedTokensBetweenTableAndValues() {
        return Sets.newHashSet(Token.PARTITION);
    }
    
    @Override
    protected Set<Token> getValuesTokens() {
        return Sets.newHashSet(Token.VALUES, Token.VALUE);
    }
    
    @Override
    protected Set<Token> getCustomizedInsertTokens() {
        return Sets.newHashSet(Token.SET);
    }
}
