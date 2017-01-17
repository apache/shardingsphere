package com.alibaba.druid.sql.context;

import com.alibaba.druid.sql.lexer.Lexer;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLBuilder;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Update SQL上下文.
 *
 * @author zhangliang
 */
@Getter
public final class UpdateSQLContext implements SQLContext {
    
    @Setter
    private Table table;
    
    private final Collection<ConditionContext> conditionContexts = new LinkedList<>();
    
    private final SQLBuilder sqlBuilder = new SQLBuilder();
    
    public void appendBeforeTable(final Lexer lexer) {
        try {
            sqlBuilder.append(lexer.getInput().substring(0, lexer.getCurrentPosition() - lexer.getLiterals().length()));
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void appendTable(final Table table) {
        sqlBuilder.appendToken(table.getName());
        // TODO 应该使用计算offset而非output AS + alias的方式生成sql
        if (table.getAlias().isPresent()) {
            try {
                sqlBuilder.append(" AS ").append(table.getAlias().get());
            } catch (final IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
    public void appendAfterTable(final Lexer lexer) {
        try {
            sqlBuilder.append(" ").append(lexer.getInput().substring(lexer.getCurrentPosition() - lexer.getLiterals().length(), lexer.getInput().length()));
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
