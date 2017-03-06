package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.context.DeleteSQLContext;
import com.alibaba.druid.sql.lexer.Token;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.google.common.base.Optional;
import lombok.Getter;

import java.util.List;

/**
 * Delete语句解析器.
 *
 * @author zhangliang
 */
public abstract class AbstractDeleteParser {
    
    @Getter
    private final SQLExprParser exprParser;
    
    private final ShardingRule shardingRule;
    
    private final List<Object> parameters;
    
    public AbstractDeleteParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        this.exprParser = exprParser;
        this.shardingRule = shardingRule;
        this.parameters = parameters;
    }
    
    /**
     * 解析Delete语句.
     *
     * @return 解析结果
     */
    public DeleteSQLContext parse() {
        DeleteSQLContext result = new DeleteSQLContext(exprParser.getLexer().getInput());
        exprParser.getLexer().nextToken();
        skipBetweenDeleteAndTable();
        exprParser.parseSingleTable(result);
        exprParser.getLexer().skipUntil(Token.WHERE);
        Optional<ConditionContext> conditionContext = new ParserUtil(exprParser, shardingRule, parameters, result, 0).parseWhere();
        if (conditionContext.isPresent()) {
            result.getConditionContexts().add(conditionContext.get());
        }
        return result;
    }
    
    protected abstract void skipBetweenDeleteAndTable();
}
