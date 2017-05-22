/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.rewrite;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.GeneratedKeyContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.InsertSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ShardingColumnContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLNumberExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLPlaceholderExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.ItemsToken;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 自增主键工具类.
 *
 * @author zhangliang
 */
public final class GenerateKeysUtils {
    
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
    
    /**
     * 获取自增主键.
     * 
     * @param shardingRule 分片规则
     * @param insertSQLContext 解析结果
     * @return 自增主键集合
     */
    public static List<Number> generateKeys(final ShardingRule shardingRule, final InsertSQLContext insertSQLContext) {
        Optional<TableRule> tableRuleOptional = shardingRule.tryFindTableRule(insertSQLContext.getTables().iterator().next().getName());
        if (!tableRuleOptional.isPresent()) {
            return Collections.emptyList();
        }
        TableRule tableRule = tableRuleOptional.get();
        GeneratedKeyContext generatedKeyContext = insertSQLContext.getGeneratedKeyContext();
        List<Number> result = new ArrayList<>(generatedKeyContext.getColumns().size());
        for (String each : generatedKeyContext.getColumns()) {
            Number generatedId = tableRule.generateId(each);
            result.add(generatedId);
            generatedKeyContext.putValue(each, generatedId);
        }
        return result;
    }
}
