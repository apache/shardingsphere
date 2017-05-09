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

package com.dangdang.ddframe.rdb.sharding.router.single;

import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ConditionContext;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 路由工具类.
 * 
 * @author gaohongtao
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SingleRouterUtil {
    
    /**
     * 将条件对象转换为分片值对象.
     * 
     * @param condition 条件对象
     * @param parameters 参数列表
     * @return 分片值对象
     */
    public static ShardingValue<?> convertConditionToShardingValue(final ConditionContext.Condition condition, final List<Object> parameters) {
        List<Comparable<?>> conditionValues = condition.getValues(parameters);
        switch (condition.getOperator()) {
            case EQUAL:
            case IN:
                if (1 == conditionValues.size()) {
                    return new ShardingValue<Comparable<?>>(condition.getShardingColumnContext().getTableName(), condition.getShardingColumnContext().getColumnName(), conditionValues.get(0));
                }
                return new ShardingValue<>(condition.getShardingColumnContext().getTableName(), condition.getShardingColumnContext().getColumnName(), conditionValues);
            case BETWEEN:
                return new ShardingValue<>(condition.getShardingColumnContext().getTableName(), condition.getShardingColumnContext().getColumnName(), 
                        Range.range(conditionValues.get(0), BoundType.CLOSED, conditionValues.get(1), BoundType.CLOSED));
            default:
                throw new UnsupportedOperationException(condition.getOperator().getExpression());
        }
    }
}
