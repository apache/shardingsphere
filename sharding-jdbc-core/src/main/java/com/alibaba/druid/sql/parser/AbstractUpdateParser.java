package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLListExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.context.TableContext;
import com.alibaba.druid.sql.context.TableToken;
import com.alibaba.druid.sql.context.UpdateSQLContext;
import com.alibaba.druid.sql.lexer.Token;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
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
    private final ShardingRule shardingRule;
    
    @Getter
    private final List<Object> parameters;
    
    private int parametersIndex;
    
    public AbstractUpdateParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(exprParser.getLexer());
        this.exprParser = exprParser;
        this.shardingRule = shardingRule;
        this.parameters = parameters;
    }
    
    /**
     * 解析Update语句.
     *
     * @return 解析结果
     */
    public UpdateSQLContext parse() {
        UpdateSQLContext result = new UpdateSQLContext(getLexer().getInput());
        getLexer().nextToken();
        skipBetweenUpdateAndTable();
        TableContext table = parseSingleTable(result);
        parseSetItems(result);
        getLexer().skipUntil(Token.WHERE);
        Optional<ConditionContext> conditionContext = new ParserUtil(exprParser, shardingRule, parameters, table, result, parametersIndex).parseWhere();
        if (conditionContext.isPresent()) {
            result.getConditionContexts().add(conditionContext.get());
        }
        return result;
    }
    
    protected abstract void skipBetweenUpdateAndTable();
    
    private void parseSetItems(final UpdateSQLContext sqlContext) {
        getLexer().accept(Token.SET);
        do {
            parseSetItem(sqlContext);
        } while (getLexer().skipIfEqual(Token.COMMA));
    }
    
    private void parseSetItem(final UpdateSQLContext sqlContext) {
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().skipParentheses();
        } else {
            int beginPosition = getLexer().getCurrentPosition();
            String literals = getLexer().getLiterals();
            getLexer().nextToken();
            String tableName = sqlContext.getTables().get(0).getName();
            if (getLexer().skipIfEqual(Token.DOT) && tableName.equalsIgnoreCase(SQLUtil.getExactlyValue(literals))) {
                sqlContext.getSqlTokens().add(new TableToken(beginPosition - literals.length(), literals, tableName));
                getLexer().nextToken();
            }
        }
        getLexer().skipIfEqual(Token.EQ, Token.COLON_EQ);
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
}
