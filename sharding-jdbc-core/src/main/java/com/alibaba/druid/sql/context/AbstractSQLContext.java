package com.alibaba.druid.sql.context;

import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLBuilder;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Table;
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
        for (SQLToken each : sqlTokens) {
            if (0 == count) {
                append(result, originalSQL.substring(0, each.getBeginPosition()));
            }
            if (each instanceof TableToken) {
                result.appendToken(((TableToken) each).getTableName());
                int beginPosition = each.getBeginPosition() + ((TableToken) each).getOriginalLiterals().length();
                int endPosition = sqlTokens.size() - 1 == count ? originalSQL.length() : sqlTokens.get(count + 1).getBeginPosition();
                append(result, originalSQL.substring(beginPosition, endPosition));
            } else if (each instanceof ItemsToken) {
                for (String item : ((ItemsToken) each).getItems()) {
                    append(result, ", ");
                    append(result, item);
                }
                int beginPosition = each.getBeginPosition();
                int endPosition = sqlTokens.size() - 1 == count ? originalSQL.length() : sqlTokens.get(count + 1).getBeginPosition();
                append(result, originalSQL.substring(beginPosition, endPosition));
            }
            count++;
        }
        return result;
    }
}
