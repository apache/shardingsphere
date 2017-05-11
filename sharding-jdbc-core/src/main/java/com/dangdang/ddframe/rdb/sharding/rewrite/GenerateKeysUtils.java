package com.dangdang.ddframe.rdb.sharding.rewrite;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.InsertSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ShardingColumnContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLNumberExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLPlaceholderExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.ItemsToken;

import java.util.Collection;
import java.util.List;

/**
 * .
 *
 * @author zhangliang
 */
public class GenerateKeysUtils {
    
    /**
     * 追加自增主键.
     *
     * @param shardingRule 分片规则
     * @param parameters 参数
     * @param insertSQLContext 解析结果
     */
    public static void appendGenerateKeys(final ShardingRule shardingRule, final List<Object> parameters, final InsertSQLContext insertSQLContext) {
        String tableName = insertSQLContext.getTables().get(0).getName();
        ItemsToken columnsToken = new ItemsToken(insertSQLContext.getColumnsListLastPosition());
        Collection<String> autoIncrementColumns = shardingRule.getAutoIncrementColumns(tableName);
        for (String each : autoIncrementColumns) {
            if (!isIncluded(insertSQLContext, each)) {
                columnsToken.getItems().add(each);
            }
        }
        if (!columnsToken.getItems().isEmpty()) {
            insertSQLContext.getSqlTokens().add(columnsToken);
        }
        ItemsToken valuesToken = new ItemsToken(insertSQLContext.getValuesListLastPosition());
        int offset = parameters.size() - 1;
        for (String each : autoIncrementColumns) {
            if (isIncluded(insertSQLContext, each)) {
                continue;
            }
            Number generatedId = shardingRule.findTableRule(tableName).generateId(each);
            ShardingColumnContext shardingColumnContext = new ShardingColumnContext(each, tableName, true);
            if (parameters.isEmpty()) {
                valuesToken.getItems().add(generatedId.toString());
                if (shardingRule.isShardingColumn(shardingColumnContext)) {
                    insertSQLContext.getConditionContext().add(new ConditionContext.Condition(shardingColumnContext, new SQLNumberExpr(generatedId)));
                }
            } else {
                valuesToken.getItems().add("?");
                parameters.add(generatedId);
                offset++;
                if (shardingRule.isShardingColumn(shardingColumnContext)) {
                    insertSQLContext.getConditionContext().add(new ConditionContext.Condition(shardingColumnContext, new SQLPlaceholderExpr(offset)));
                }
            }
            insertSQLContext.getGeneratedKeyContext().getColumns().add(each);
            insertSQLContext.getGeneratedKeyContext().putValue(each, generatedId);
        }
        if (!valuesToken.getItems().isEmpty()) {
            insertSQLContext.getSqlTokens().add(valuesToken);
        }
    }
    
    private static boolean isIncluded(final InsertSQLContext insertSQLContext, final String autoIncrementColumn) {
        for (ShardingColumnContext shardingColumnContext : insertSQLContext.getShardingColumnContexts()) {
            if (shardingColumnContext.getColumnName().equalsIgnoreCase(autoIncrementColumn)) {
                return true;
            }
        }
        return false;
    }
}
