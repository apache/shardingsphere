package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.context.DeleteSQLContext;
import com.alibaba.druid.sql.lexer.Token;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Table;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.Getter;

import java.util.List;

/**
 * Delete语句解析器.
 *
 * @author zhangliang
 */
public abstract class AbstractDeleteParser extends SQLParser {
    
    @Getter
    private final SQLExprParser exprParser;
    
    private final ShardingRule shardingRule;
    
    private final List<Object> parameters;
    
    public AbstractDeleteParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(exprParser.getLexer(), exprParser.getDbType());
        this.exprParser = exprParser;
        this.shardingRule = shardingRule;
        this.parameters = parameters;
    }
    
    /**
     * 解析Delete语句.
     *
     * @return 解析结果
     */
    public SQLDeleteStatement parse() {
        DeleteSQLContext result = new DeleteSQLContext();
        getLexer().nextToken();
        parseBetweenDeleteAndTable();
        result.appendBeforeTable(getLexer());
        Table table = parseTable(result);
        if (!getLexer().equalToken(Token.EOF)) {
            result.appendAfterTable(getLexer());
            parseBetweenTableAndWhere();
            Optional<ConditionContext> conditionContext = new ParserUtil(shardingRule, parameters, exprParser, 0).parseWhere(table);
            if (conditionContext.isPresent()) {
                result.getConditionContexts().add(conditionContext.get());
            }
        }
        return new SQLDeleteStatement(result);
    }
    
    private Table parseTable(final DeleteSQLContext deleteSQLContext) {
        SQLTableSource tableSource = new SQLSelectParser(exprParser).parseTableSource();
        if (tableSource instanceof SQLJoinTableSource) {
            throw new UnsupportedOperationException("Cannot support delete Multiple-Table.");
        }
        Table result = new Table(SQLUtil.getExactlyValue(tableSource.toString()), Optional.fromNullable(SQLUtil.getExactlyValue(tableSource.getAlias())));
        deleteSQLContext.setTable(result);
        deleteSQLContext.appendTable(result);
        return result;
    }
    
    protected abstract void parseBetweenDeleteAndTable();
    
    protected abstract void parseBetweenTableAndWhere();
}
