package com.alibaba.druid.sql.dialect.mysql.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLNullExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.AbstractInsertParser;
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
    protected void parseCustomizedInsert() {
        parseInsertSet();
    }
    
    private void parseInsertSet() {
        ParseContext parseContext = getParseContext();
        Collection<String> autoIncrementColumns = getShardingRule().getAutoIncrementColumns(getSqlContext().getTables().get(0).getName());
        do {
            getExprParser().getLexer().nextToken();
            Condition.Column column = getColumn(autoIncrementColumns);
            getExprParser().getLexer().nextToken();
            getExprParser().getLexer().accept(Token.EQ);
            SQLExpr sqlExpr;
            if (getExprParser().getLexer().equalToken(Token.LITERAL_INT)) {
                sqlExpr = new SQLIntegerExpr(Integer.parseInt(getExprParser().getLexer().getLiterals()));
            } else if (getExprParser().getLexer().equalToken(Token.LITERAL_FLOAT)) {
                sqlExpr = new SQLIntegerExpr(Double.parseDouble(getExprParser().getLexer().getLiterals()));
            } else if (getExprParser().getLexer().equalToken(Token.LITERAL_CHARS)) {
                sqlExpr = new SQLCharExpr(getExprParser().getLexer().getLiterals());
            } else if (getExprParser().getLexer().equalToken(Token.NULL)) {
                sqlExpr = new SQLNullExpr();
            } else if (getExprParser().getLexer().equalToken(Token.VARIANT)) {
                sqlExpr = new SQLVariantRefExpr("?");
            } else {
                throw new UnsupportedOperationException("");
            }
            getExprParser().getLexer().nextToken();
            if (getExprParser().getLexer().equalToken(Token.COMMA, Token.ON, Token.EOF)) {
                parseContext.addCondition(column.getColumnName(), column.getTableName(), Condition.BinaryOperator.EQUAL, sqlExpr, getParameters());
            } else {
                getExprParser().getLexer().skipUntil(Token.COMMA, Token.ON);
            }
        } while (getExprParser().getLexer().equalToken(Token.COMMA));
        getSqlContext().getConditionContexts().add(parseContext.getCurrentConditionContext());
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
