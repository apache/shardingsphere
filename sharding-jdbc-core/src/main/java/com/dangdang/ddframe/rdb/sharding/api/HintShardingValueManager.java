/**
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

package com.dangdang.ddframe.rdb.sharding.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import com.dangdang.ddframe.rdb.sharding.router.single.SingleRouterUtil;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * 通过线索传递分片值的管理器.
 *
 * @author gaohongtao
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class HintShardingValueManager {
    
    private static final ThreadLocal<ShardingValueContainer> SHARING_VALUE_CONTAINER = new ThreadLocal<>();
    
    /**
     * 初始化容器.
     */
    public static void init() {
        if (null != SHARING_VALUE_CONTAINER.get()) {
            throw new ShardingJdbcException("CAN NOT init repeatedly");
        }
        SHARING_VALUE_CONTAINER.set(new ShardingValueContainer());
    }
    
    /**
     * 注册分库分片值.
     * 
     * @param logicTable 逻辑表明
     * @param shardingColumn 分库键
     * @param values 分库值
     */
    public static void registerShardingValueOfDatabase(final String logicTable, final String shardingColumn, final Comparable<?>... values) {
        registerShardingValueOfDatabase(logicTable, shardingColumn, Condition.BinaryOperator.EQUAL, values);
    }
    
    /**
     * 注册分库分片值.
     * 
     * @param logicTable 逻辑表明
     * @param shardingColumn 分库键
     * @param binaryOperator 分库操作符
     * @param values 分库值
     */
    public static void registerShardingValueOfDatabase(final String logicTable, final String shardingColumn, final Condition.BinaryOperator binaryOperator, final Comparable<?>... values) {
        if (null == SHARING_VALUE_CONTAINER.get()) {
            throw new ShardingJdbcException("Please first invoke HintShardingValueManager.init()");
        }
        registerShardingValue(SHARING_VALUE_CONTAINER.get().databaseShardingValues, logicTable, shardingColumn, binaryOperator, values);
    }
    
    /**
     * 获取分库键值.
     * 
     * @param logicTable 逻辑表名
     * @return 分库键值
     */
    public static Optional<List<ShardingValue<?>>> getShardingValueOfDatabase(final String logicTable) {
        if (null == SHARING_VALUE_CONTAINER.get()) {
            return Optional.absent();
        }
        return Optional.fromNullable(SHARING_VALUE_CONTAINER.get().databaseShardingValues.get(logicTable));
    }
    
    /**
     * 注册分表分片值.
     * 
     * @param logicTable 逻辑表明
     * @param shardingColumn 分库键
     * @param values 分库值
     */
    public static void registerShardingValueOfTable(final String logicTable, final String shardingColumn, final Comparable<?>... values) {
        registerShardingValueOfTable(logicTable, shardingColumn, Condition.BinaryOperator.EQUAL, values);
    }
    
    /**
     * 注册分表分片值.
     * 
     * @param logicTable 逻辑表明
     * @param shardingColumn 分库键
     * @param binaryOperator 分库操作符
     * @param values 分库值
     */
    public static void registerShardingValueOfTable(final String logicTable, final String shardingColumn, final Condition.BinaryOperator binaryOperator, final Comparable<?>... values) {
        if (null == SHARING_VALUE_CONTAINER.get()) {
            throw new ShardingJdbcException("Please first invoke HintShardingValueManager.init()");
        }
        registerShardingValue(SHARING_VALUE_CONTAINER.get().tableShardingValues, logicTable, shardingColumn, binaryOperator, values);
    }
    
    /**
     * 获取分表键值.
     * 
     * @param logicTable 逻辑表名
     * @return 分库键值
     */
    public static Optional<List<ShardingValue<?>>> getShardingValueOfTable(final String logicTable) {
        if (null == SHARING_VALUE_CONTAINER.get()) {
            return Optional.absent();
        }
        return Optional.fromNullable(SHARING_VALUE_CONTAINER.get().tableShardingValues.get(logicTable));
    }
    
    private static void registerShardingValue(final Map<String, List<ShardingValue<?>>> container, final String logicTable, 
                                              final String shardingColumn, final Condition.BinaryOperator binaryOperator, final Comparable<?>... values) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(logicTable));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(shardingColumn));
        Preconditions.checkArgument(null != values && values.length > 0);
        
        List<ShardingValue<?>> shardingValues;
        if (container.containsKey(logicTable)) {
            shardingValues = container.get(logicTable);
        } else {
            shardingValues = new ArrayList<>();
            container.put(logicTable, shardingValues);
        }
        Condition condition = new Condition(new Condition.Column(shardingColumn, logicTable), binaryOperator);
        condition.getValues().addAll(Arrays.asList(values));
        shardingValues.add(SingleRouterUtil.convertConditionToShardingValue(condition));
    }
    
    /**
     * 清理容器.
     * 
     */
    public static void clear() {
        SHARING_VALUE_CONTAINER.remove();
    }
    
    private static class ShardingValueContainer {
        
        private final Map<String, List<ShardingValue<?>>> databaseShardingValues = new HashMap<>();
        
        private final Map<String, List<ShardingValue<?>>> tableShardingValues = new HashMap<>();
    }
    
}
