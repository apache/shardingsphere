package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLListExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
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
    
    @Getter
    private final UpdateSQLContext updateSQLContext;
    
    private final ShardingRule shardingRule;
    
    private final List<Object> parameters;
    
    private int parametersIndex;
    
    public AbstractUpdateParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(exprParser.getLexer(), exprParser.getDbType());
        this.exprParser = exprParser;
        this.updateSQLContext = new UpdateSQLContext();
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
        updateSQLContext.append(getLexer().getInput().substring(0, getLexer().getCurrentPosition() - getLexer().getLiterals().length()));
        Table table = parseTable();
        updateSQLContext.append(" " + getLexer().getInput().substring(getLexer().getCurrentPosition() - getLexer().getLiterals().length(), getLexer().getInput().length()));
        parseSetItems();
        parseBetweenSetAndWhere();
        Optional<ConditionContext> conditionContext = new ParserUtil(shardingRule, parameters, exprParser, parametersIndex).parseWhere(table);
        if (conditionContext.isPresent()) {
            updateSQLContext.getConditionContexts().add(conditionContext.get());
        }
        return new SQLUpdateStatement(updateSQLContext);
    }
    
    protected abstract void parseBetweenUpdateAndTable();
    
    private Table parseTable() {
        String tableName = SQLUtil.getExactlyValue(getLexer().getLiterals());
        getLexer().nextToken();
        if (getLexer().equalToken(Token.AS)) {
            getLexer().nextToken();
        }
        Optional<String> alias = getLexer().equalToken(Token.IDENTIFIER) ? Optional.of(SQLUtil.getExactlyValue(getLexer().getLiterals())) : Optional.<String>absent();
        Table result = new Table(tableName, alias);
        updateSQLContext.setTable(result);
        updateSQLContext.appendToken(tableName);
        // TODO 应该使用计算offset而非output AS + alias的方式生成sql
        if (alias.isPresent()) {
            updateSQLContext.append(" AS " + alias.get());
        }
        if (!getLexer().equalToken(Token.SET)) {
            getLexer().nextToken();
        }
        return result;
    }
    
    private void parseSetItems() {
        accept(Token.SET);
        do {
            if (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
            }
            parseSetItem();
        } while (getLexer().equalToken(Token.COMMA));
    }
    
    private void parseSetItem() {
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            while (!getLexer().equalToken(Token.RIGHT_PAREN)) {
                getLexer().nextToken();
            }
            getLexer().nextToken();
        } else {
            exprParser.primary();
        }
        if (getLexer().equalToken(Token.COLON_EQ)) {
            getLexer().nextToken();
        } else {
            accept(Token.EQ);
        }
        SQLExpr value = exprParser.expr();
        if (value instanceof SQLListExpr) {
            for (SQLExpr each : ((SQLListExpr) value).getItems()) {
                if (each instanceof SQLVariantRefExpr) {
                    parametersIndex++;
                }
            }
        }
        if (value instanceof SQLVariantRefExpr) {
            parametersIndex++;
        }
    }
    
    protected void parseBetweenSetAndWhere() {
    }
}
