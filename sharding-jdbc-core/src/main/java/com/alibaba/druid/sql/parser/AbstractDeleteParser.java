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
    
    @Getter
    private DeleteSQLContext deleteSQLContext;
    
    private final ShardingRule shardingRule;
    
    private final List<Object> parameters;
    
    public AbstractDeleteParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(exprParser.getLexer(), exprParser.getDbType());
        this.exprParser = exprParser;
        this.deleteSQLContext = new DeleteSQLContext();
        this.shardingRule = shardingRule;
        this.parameters = parameters;
    }
    
    /**
     * 解析Delete语句.
     *
     * @return 解析结果
     */
    public SQLDeleteStatement parse() {
        getLexer().nextToken();
        SQLDeleteStatement result = new SQLDeleteStatement(getDbType());
        parseBetweenDeleteAndTable();
        deleteSQLContext.append(getLexer().getInput().substring(0, getLexer().getCurrentPosition() - getLexer().getLiterals().length()));
        
        
        SQLTableSource tableSource = createSQLSelectParser().parseTableSource();
        if (tableSource instanceof SQLJoinTableSource) {
            throw new UnsupportedOperationException("Cannot support delete Multiple-Table.");
        }
        
        result.setTableSource(tableSource);
        Table table = new Table(SQLUtil.getExactlyValue(tableSource.toString()), Optional.fromNullable(SQLUtil.getExactlyValue(tableSource.getAlias())));
        deleteSQLContext.setTable(table);
        deleteSQLContext.appendToken(table.getName());
        // TODO 应该使用计算offset而非output AS + alias的方式生成sql
        if (table.getAlias().isPresent()) {
            deleteSQLContext.append(" AS " + table.getAlias().get());
        }
        if (!getLexer().equalToken(Token.EOF)) {
            deleteSQLContext.append(" " + getLexer().getInput().substring(getLexer().getCurrentPosition() - getLexer().getLiterals().length(), getLexer().getInput().length()));
            parseBetweenTableAndWhere();
            Optional<ConditionContext> conditionContext = new ParserUtil(shardingRule, parameters, exprParser, 0).parseWhere(table);
            if (conditionContext.isPresent()) {
                deleteSQLContext.getConditionContexts().add(conditionContext.get());
            }
        }
        result.setSqlContext(deleteSQLContext);
        return result;
    }
    
    protected SQLSelectParser createSQLSelectParser() {
        return new SQLSelectParser(exprParser);
    }
    
    protected abstract void parseBetweenDeleteAndTable();
    
    protected abstract void parseBetweenTableAndWhere();
}
