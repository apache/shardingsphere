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

package com.dangdang.ddframe.rdb.sharding.parser.result.router;

import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition.BinaryOperator;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition.Column;
import com.google.common.base.Optional;
import lombok.ToString;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 条件对象上下文.
 * 
 * @author zhangliang
 */
@ToString
public final class ConditionContext {
    
    private final Map<Column, Condition> conditions = new LinkedHashMap<>();
    
    /**
     * 添加条件对象.
     * 
     * @param condition 条件对象
     */
    public void add(final Condition condition) {
        // TODO 自关联有问题，表名可考虑使用别名对应
        conditions.put(condition.getColumn(), condition);
    }
    
    /**
     * 查找条件对象.
     * 
     * @param table 表名称
     * @param column 列名称
     * @return 条件对象
     */
    public Optional<Condition> find(final String table, final String column) {
        return Optional.fromNullable(conditions.get(new Column(column, table)));
    }
    
    /**
     * 查找条件对象.
     * 
     * @param table 表名称
     * @param column 列名称
     * @param operator 操作符
     * @return 条件对象
     */
    public Optional<Condition> find(final String table, final String column, final BinaryOperator operator) {
        Optional<Condition> result = find(table, column);
        if (!result.isPresent()) {
            return result;
        }
        return result.get().getOperator() == operator ? result : Optional.<Condition>absent();
    }
    
    public boolean isEmpty() {
        return conditions.isEmpty();
    }
    
    public void clear() {
        conditions.clear();
    }
    
    public Collection<Condition> getAllConditions() {
        return conditions.values();
    }
    
    /**
     * 解析参数中间的新数据.
     * 
     * @param parameters 参数列表
     */
    public void setNewConditionValue(final List<Object> parameters) {
        for (Condition each : conditions.values()) {
            if (each.getValueIndices().isEmpty()) {
                continue;
            }
            for (int i = 0; i < each.getValueIndices().size(); i++) {
                Object value = parameters.get(each.getValueIndices().get(i));
                if (value instanceof Comparable<?>) {
                    each.getValues().set(i, (Comparable<?>) value);
                } else {
                    each.getValues().set(i, "");
                }
            }
        }
    }
}
