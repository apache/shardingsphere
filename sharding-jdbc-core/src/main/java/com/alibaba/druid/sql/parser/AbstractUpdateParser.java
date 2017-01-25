package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLListExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.context.TableToken;
import com.alibaba.druid.sql.context.UpdateSQLContext;
import com.alibaba.druid.sql.lexer.Token;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Table;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.Getter;

import java.util.List;

/**
 * Update语句解析器.
 *
 * @author zhangliang
 */
public abstract class AbstractUpdateParser extends SQLParser {
    
    @Getter
    private final SQLExprParser exprParser;
    
    private final ShardingRule shardingRule;
    
    private final List<Object> parameters;
    
    private int parametersIndex;
    
    public AbstractUpdateParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(exprParser.getLexer(), exprParser.getDbType());
        this.exprParser = exprParser;
        this.shardingRule = shardingRule;
        this.parameters = parameters;
    }
    
    /**
     * 解析Update语句.
     *
     * @return 解析结果
     */
    public SQLUpdateStatement parse() {
        getLexer().nextToken();
        parseBetweenUpdateAndTable();
        UpdateSQLContext result = new UpdateSQLContext(getLexer().getInput());
        Table table = parseTable(result);
        parseSetItems(result);
        parseBetweenSetAndWhere();
        Optional<ConditionContext> conditionContext = new ParserUtil(exprParser, shardingRule, parameters, table, result, parametersIndex).parseWhere();
        if (conditionContext.isPresent()) {
            result.getConditionContexts().add(conditionContext.get());
        }
        return new SQLUpdateStatement(result);
    }
    
    protected abstract void parseBetweenUpdateAndTable();
    
    private Table parseTable(final UpdateSQLContext updateSQLContext) {
        int beginPosition = getLexer().getCurrentPosition() - getLexer().getLiterals().length();
        SQLTableSource tableSource = new SQLSelectParser(exprParser).parseTableSource();
        if (tableSource instanceof SQLJoinTableSource) {
            throw new UnsupportedOperationException("Cannot support update Multiple-Table.");
        }
        Table result = new Table(SQLUtil.getExactlyValue(tableSource.toString()), Optional.fromNullable(SQLUtil.getExactlyValue(tableSource.getAlias())));
        updateSQLContext.getSqlTokens().add(new TableToken(beginPosition, tableSource.toString(), result.getName()));
        updateSQLContext.setTable(result);
        if (!getLexer().equalToken(Token.SET)) {
            getLexer().nextToken();
        }
        return result;
    }
    
    private void parseSetItems(final UpdateSQLContext sqlContext) {
        accept(Token.SET);
        do {
            if (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
            }
            parseSetItem(sqlContext);
        } while (getLexer().equalToken(Token.COMMA));
    }
    
    private void parseSetItem(final UpdateSQLContext sqlContext) {
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            while (!getLexer().equalToken(Token.RIGHT_PAREN)) {
                getLexer().nextToken();
            }
            getLexer().nextToken();
        } else {
            String literals = getLexer().getLiterals();
            int beginPosition = getLexer().getCurrentPosition();
            SQLExpr sqlExpr = exprParser.primary();
            if (sqlExpr instanceof SQLPropertyExpr && sqlContext.getTable().getName().equalsIgnoreCase(SQLUtil.getExactlyValue(literals))) {
                sqlContext.getSqlTokens().add(new TableToken(beginPosition - literals.length(), literals, sqlContext.getTable().getName()));
            }
        }
        if (getLexer().equalToken(Token.COLON_EQ)) {
            getLexer().nextToken();
        } else {
            accept(Token.EQ);
        }
        SQLExpr value = exprParser.expr();
        if (value instanceof SQLBinaryOpExpr) {
            if (((SQLBinaryOpExpr) value).getLeft() instanceof SQLVariantRefExpr) {
                parametersIndex++;
            }
            if (((SQLBinaryOpExpr) value).getRight() instanceof SQLVariantRefExpr) {
                parametersIndex++;
            }
            // TODO 二元操作替换table token
        } else if (value instanceof SQLListExpr) {
            for (SQLExpr each : ((SQLListExpr) value).getItems()) {
                if (each instanceof SQLVariantRefExpr) {
                    parametersIndex++;
                }
            }
        } else if (value instanceof SQLVariantRefExpr) {
            parametersIndex++;
        }
    }
    
    protected void parseBetweenSetAndWhere() {
    }
}
