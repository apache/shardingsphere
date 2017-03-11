package com.alibaba.druid.sql.dialect.mysql.parser;

import com.alibaba.druid.sql.expr.SQLExpr;
import com.alibaba.druid.sql.expr.SQLCharExpr;
import com.alibaba.druid.sql.expr.SQLNullExpr;
import com.alibaba.druid.sql.expr.SQLNumberExpr;
import com.alibaba.druid.sql.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.dialect.mysql.lexer.MySQLKeyword;
import com.alibaba.druid.sql.lexer.DataType;
import com.alibaba.druid.sql.lexer.DefaultKeyword;
import com.alibaba.druid.sql.lexer.Keyword;
import com.alibaba.druid.sql.lexer.Symbol;
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
            getExprParser().getLexer().accept(Symbol.EQ);
            SQLExpr sqlExpr;
            if (getExprParser().getLexer().equalToken(DataType.LITERAL_INT)) {
                sqlExpr = new SQLNumberExpr(Integer.parseInt(getExprParser().getLexer().getLiterals()));
            } else if (getExprParser().getLexer().equalToken(DataType.LITERAL_FLOAT)) {
                sqlExpr = new SQLNumberExpr(Double.parseDouble(getExprParser().getLexer().getLiterals()));
            } else if (getExprParser().getLexer().equalToken(DataType.LITERAL_CHARS)) {
                sqlExpr = new SQLCharExpr(getExprParser().getLexer().getLiterals());
            } else if (getExprParser().getLexer().equalToken(DefaultKeyword.NULL)) {
                sqlExpr = new SQLNullExpr();
            } else if (getExprParser().getLexer().equalToken(DataType.VARIANT)) {
                sqlExpr = new SQLVariantRefExpr("?");
            } else {
                throw new UnsupportedOperationException("");
            }
            getExprParser().getLexer().nextToken();
            if (getExprParser().getLexer().equalToken(Symbol.COMMA, DefaultKeyword.ON, DataType.EOF)) {
                parseContext.addCondition(column.getColumnName(), column.getTableName(), Condition.BinaryOperator.EQUAL, sqlExpr, getParameters());
            } else {
                getExprParser().getLexer().skipUntil(Symbol.COMMA, DefaultKeyword.ON);
            }
        } while (getExprParser().getLexer().equalToken(Symbol.COMMA));
        getSqlContext().getConditionContexts().add(parseContext.getCurrentConditionContext());
    }
    
    @Override
    protected Set<Keyword> getSkippedTokensBetweenTableAndValues() {
        return Sets.<Keyword>newHashSet(MySQLKeyword.PARTITION);
    }
    
    @Override
    protected Set<Keyword> getValuesKeywords() {
        return Sets.<Keyword>newHashSet(DefaultKeyword.VALUES, MySQLKeyword.VALUE);
    }
    
    @Override
    protected Set<Keyword> getCustomizedInsertTokens() {
        return Sets.<Keyword>newHashSet(DefaultKeyword.SET);
    }
}
