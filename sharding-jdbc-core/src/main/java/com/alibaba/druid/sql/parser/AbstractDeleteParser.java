package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.context.DeleteSQLContext;
import com.alibaba.druid.sql.lexer.Token;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Delete语句解析器.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public abstract class AbstractDeleteParser {
    
    @Getter(AccessLevel.PROTECTED)
    private final SQLExprParser exprParser;
    
    private final DeleteSQLContext sqlContext;
    
    public AbstractDeleteParser(final SQLExprParser exprParser) {
        this.exprParser = exprParser;
        sqlContext = new DeleteSQLContext(exprParser.getLexer().getInput());
    }
    
    /**
     * 解析Delete语句.
     *
     * @return 解析结果
     */
    public DeleteSQLContext parse() {
        exprParser.getLexer().nextToken();
        skipBetweenDeleteAndTable();
        exprParser.parseSingleTable(sqlContext);
        exprParser.getLexer().skipUntil(Token.WHERE);
        Optional<ConditionContext> conditionContext = exprParser.parseWhere(sqlContext);
        if (conditionContext.isPresent()) {
            sqlContext.getConditionContexts().add(conditionContext.get());
        }
        return sqlContext;
    }
    
    protected abstract void skipBetweenDeleteAndTable();
}
