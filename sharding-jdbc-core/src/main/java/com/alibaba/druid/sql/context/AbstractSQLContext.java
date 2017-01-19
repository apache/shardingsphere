package com.alibaba.druid.sql.context;

import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLBuilder;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Table;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL上下文抽象类.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractSQLContext implements SQLContext {
    
    private final String originalSQL;
    
    @Setter
    private Table table;
    
    private final Collection<ConditionContext> conditionContexts = new LinkedList<>();
    
    private final List<SQLToken> sqlTokens = new LinkedList<>();
    
    private void append(final SQLBuilder sqlBuilder, final String literals) {
        try {
            sqlBuilder.append(literals);
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public SQLBuilder toSqlBuilder() {
        SQLBuilder result = new SQLBuilder();
        if (sqlTokens.isEmpty()) {
            append(result, originalSQL);
            return result;
        }
        int count = 0;
        SQLToken previousToken = null;
        for (SQLToken each : sqlTokens) {
            if (0 == count) {
                append(result, originalSQL.substring(0, each.getBeginPosition()));
            }
            if (null != previousToken) {
                append(result, originalSQL.substring(previousToken.getBeginPosition() + previousToken.getOriginalLiterals().length(), each.getBeginPosition()));
            }
            result.appendToken(SQLUtil.getExactlyValue(each.getOriginalLiterals()));
            if (sqlTokens.size() - 1 == count) {
                append(result, originalSQL.substring(each.getBeginPosition() + each.getOriginalLiterals().length(), originalSQL.length()));
            }
            count++;
            previousToken = each;
        }
        return result;
    }
}
