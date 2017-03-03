package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.SQLEvalConstants;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumberExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.context.InsertSQLContext;
import com.alibaba.druid.sql.context.ItemsToken;
import com.alibaba.druid.sql.context.SQLContext;
import com.alibaba.druid.sql.context.TableContext;
import com.alibaba.druid.sql.lexer.Token;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.ParseContext;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.collect.Sets;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Insert语句解析器.
 *
 * @author zhangliang
 */
@Getter
public abstract class AbstractInsertParser extends SQLParser {
    
    private final SQLExprParser exprParser;
    
    private final ShardingRule shardingRule;
    
    private final List<Object> parameters;
    
    public AbstractInsertParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(exprParser.getLexer());
        this.exprParser = exprParser;
        this.shardingRule = shardingRule;
        this.parameters = parameters;
    }
    
    /**
     * 解析Insert语句.
     * 
     * @return 解析结果
     */
    public final InsertSQLContext parse() {
        InsertSQLContext result = new InsertSQLContext(getLexer().getInput());
        getLexer().nextToken();
        parseInto(result);
        Collection<Condition.Column> columns = parseColumns(result);
        if (getLexer().equalToken(Token.SELECT, Token.LEFT_PAREN)) {
            throw new UnsupportedOperationException("Cannot support subquery");
        }
        if (getValuesTokens().contains(getLexer().getToken())) {
            parseValues(columns, result);
        } else if (getCustomizedInsertTokens().contains(getLexer().getToken())) {
            parseCustomizedInsert(result);
        }
        return result;
    }
    
    protected Set<Token> getUnsupportedTokens() {
        return Collections.emptySet();
    }
    
    private void parseInto(final InsertSQLContext sqlContext) {
        getLexer().skipIfEqual(Token.HINT);
        if (getUnsupportedTokens().contains(getLexer().getToken())) {
            throw new ParserUnsupportedException(getLexer().getToken());
        }
        getLexer().skipUntil(Token.INTO);
        getLexer().nextToken();
        parseSingleTable(sqlContext);
        skipBetweenTableAndValues();
    }
    
    private void skipBetweenTableAndValues() {
        while (getSkippedTokensBetweenTableAndValues().contains(getLexer().getToken())) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                getLexer().skipParentheses();
            }
        }
    }
    
    protected Set<Token> getSkippedTokensBetweenTableAndValues() {
        return Collections.emptySet();
    }
    
    private Collection<Condition.Column> parseColumns(final InsertSQLContext sqlContext) {
        Collection<Condition.Column> result = new LinkedList<>();
        Collection<String> autoIncrementColumns = shardingRule.getAutoIncrementColumns(sqlContext.getTables().get(0).getName());
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            do {
                getLexer().nextToken();
                result.add(getColumn(sqlContext, autoIncrementColumns));
                getLexer().nextToken();
            } while (!getLexer().equalToken(Token.RIGHT_PAREN) && !getLexer().equalToken(Token.EOF));
            ItemsToken itemsToken = new ItemsToken(getLexer().getCurrentPosition() - getLexer().getLiterals().length());
            for (String each : autoIncrementColumns) {
                itemsToken.getItems().add(each);
                result.add(new Condition.Column(each, sqlContext.getTables().get(0).getName(), true));
            }
            if (!itemsToken.getItems().isEmpty()) {
                sqlContext.getSqlTokens().add(itemsToken);
            }
            getLexer().nextToken();
        }
        return result;
    }
    
    protected final Condition.Column getColumn(final InsertSQLContext sqlContext, final Collection<String> autoIncrementColumns) {
        String columnName = SQLUtil.getExactlyValue(getLexer().getLiterals());
        if (autoIncrementColumns.contains(columnName)) {
            autoIncrementColumns.remove(columnName);
        }
        return new Condition.Column(columnName, sqlContext.getTables().get(0).getName());
    }
    
    protected Set<Token> getValuesTokens() {
        return Sets.newHashSet(Token.VALUES);
    }
    
    private void parseValues(final Collection<Condition.Column> columns, final InsertSQLContext sqlContext) {
        ParseContext parseContext = getParseContext(sqlContext);
        boolean parsed = false;
        do {
            if (parsed) {
                throw new UnsupportedOperationException("Cannot support multiple insert");
            }
            getLexer().nextToken();
            getLexer().accept(Token.LEFT_PAREN);
            List<SQLExpr> sqlExprs = getExprParser().exprList(new SQLIdentifierExpr(""));
            ItemsToken itemsToken = new ItemsToken(getLexer().getCurrentPosition() - getLexer().getLiterals().length());
            int count = 0;
            int parameterCount = 0;
            for (Condition.Column each : columns) {
                if (each.isAutoIncrement()) {
                    Number autoIncrementedValue = (Number) parseContext.getShardingRule().findTableRule(sqlContext.getTables().get(0).getName()).generateId(each.getColumnName());
                    if (parameters.isEmpty()) {
                        itemsToken.getItems().add(autoIncrementedValue.toString());
                        sqlExprs.add(new SQLNumberExpr(autoIncrementedValue));
                    } else {
                        itemsToken.getItems().add("?");
                        parameters.add(autoIncrementedValue);
                        SQLVariantRefExpr sqlVariantRefExpr = new SQLVariantRefExpr("?");
                        sqlVariantRefExpr.setIndex(parameters.size());
                        sqlVariantRefExpr.getAttributes().put(SQLEvalConstants.EVAL_VALUE, autoIncrementedValue);
                        sqlVariantRefExpr.getAttributes().put(SQLEvalConstants.EVAL_VAR_INDEX, parameters.size() - 1);
                        sqlExprs.add(sqlVariantRefExpr);
                    }
                    sqlContext.getGeneratedKeyContext().getColumns().add(each.getColumnName());
                    sqlContext.getGeneratedKeyContext().putValue(each.getColumnName(), autoIncrementedValue);
                } else if (sqlExprs.get(count) instanceof SQLVariantRefExpr) {
                    sqlExprs.get(count).getAttributes().put(SQLEvalConstants.EVAL_VALUE, parameters.get(parameterCount));
                    sqlExprs.get(count).getAttributes().put(SQLEvalConstants.EVAL_VAR_INDEX, parameterCount);
                    parameterCount++;
                }
                parseContext.addCondition(each.getColumnName(), each.getTableName(), Condition.BinaryOperator.EQUAL, sqlExprs.get(count), parameters);
                count++;
            }
            if (!itemsToken.getItems().isEmpty()) {
                sqlContext.getSqlTokens().add(itemsToken);
            }
            getLexer().accept(Token.RIGHT_PAREN);
            parsed = true;
        }
        while (getLexer().equalToken(Token.COMMA));
        sqlContext.getConditionContexts().add(parseContext.getCurrentConditionContext());
    }
    
    private ParseContext getParseContext(final SQLContext sqlContext) {
        ParseContext result = new ParseContext(1);
        result.setShardingRule(shardingRule);
        for (TableContext each : sqlContext.getTables()) {
            result.setCurrentTable(each.getName(), each.getAlias());
        }
        return result;
    }
    
    protected Set<Token> getCustomizedInsertTokens() {
        return Collections.emptySet();
    }
    
    protected void parseCustomizedInsert(final InsertSQLContext sqlContext) {
    }
}
