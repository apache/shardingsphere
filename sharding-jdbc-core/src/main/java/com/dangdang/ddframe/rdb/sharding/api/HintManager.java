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

package com.dangdang.ddframe.rdb.sharding.api;

import com.dangdang.ddframe.rdb.sharding.hint.HintManagerHolder;
import com.dangdang.ddframe.rdb.sharding.hint.ShardingKey;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import com.google.common.base.Preconditions;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 通过线索传递分片值的管理器.
 *
 * @author gaohongtao
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HintManager implements AutoCloseable {
    
    private final Map<ShardingKey, ShardingValue<?>> databaseShardingValues = new HashMap<>();
    
    private final Map<ShardingKey, ShardingValue<?>> tableShardingValues = new HashMap<>();
    
    @Getter
    private boolean shardingHint;
    
    @Getter
    private boolean masterRouteOnly;
    
    /**
     * 获取线索分片管理器实例.
     * 
     * @return 线索分片管理器实例
     */
    public static HintManager getInstance() {
        HintManager result = new HintManager();
        HintManagerHolder.setHintManager(result);
        return result;
    }
    
    /**
     * 添加分库分片值.
     * 
     * <p>分片操作符为等号.</p>
     *
     * @param logicTable 逻辑表名称
     * @param shardingColumn 分片键
     * @param value 分片值
     */
    public void addDatabaseShardingValue(final String logicTable, final String shardingColumn, final Comparable<?> value) {
        addDatabaseShardingValue(logicTable, shardingColumn, Condition.BinaryOperator.EQUAL, value);
    }
    
    /**
     * 添加分库分片值.
     *
     * @param logicTable 逻辑表名称
     * @param shardingColumn 分片键
     * @param binaryOperator 分片操作符
     * @param values 分片值
     */
    public void addDatabaseShardingValue(final String logicTable, final String shardingColumn, final Condition.BinaryOperator binaryOperator, final Comparable<?>... values) {
        shardingHint = true;
        databaseShardingValues.put(new ShardingKey(logicTable, shardingColumn), getShardingValue(logicTable, shardingColumn, binaryOperator, values));
    }
    
    /**
     * 添加分表分片值.
     * 
     * <p>分片操作符为等号.</p>
     *
     * @param logicTable 逻辑表名称
     * @param shardingColumn 分片键
     * @param value 分片值
     */
    public void addTableShardingValue(final String logicTable, final String shardingColumn, final Comparable<?> value) {
        addTableShardingValue(logicTable, shardingColumn, Condition.BinaryOperator.EQUAL, value);
    }
    
    /**
     * 添加分表分片值.
     *
     * @param logicTable 逻辑表名称
     * @param shardingColumn 分片键
     * @param binaryOperator 分片操作符
     * @param values 分片值
     */
    public void addTableShardingValue(final String logicTable, final String shardingColumn, final Condition.BinaryOperator binaryOperator, final Comparable<?>... values) {
        shardingHint = true;
        tableShardingValues.put(new ShardingKey(logicTable, shardingColumn), getShardingValue(logicTable, shardingColumn, binaryOperator, values));
    }
    
    @SuppressWarnings("unchecked")
    private ShardingValue getShardingValue(final String logicTable, final String shardingColumn, final Condition.BinaryOperator binaryOperator, final Comparable<?>[] values) {
        Preconditions.checkArgument(null != values && values.length > 0);
        switch (binaryOperator) {
            case EQUAL:
                return new ShardingValue<Comparable<?>>(logicTable, shardingColumn, values[0]);
            case IN:
                return new ShardingValue(logicTable, shardingColumn, Arrays.asList(values));
            case BETWEEN:
                return new ShardingValue(logicTable, shardingColumn, Range.range(values[0], BoundType.CLOSED, values[1], BoundType.CLOSED));
            default:
                throw new UnsupportedOperationException(binaryOperator.getExpression());
        }
    }
    
    /**
     * 获取分库分片键值.
     * 
     * @param shardingKey 分片键
     * @return 分库分片键值
     */
    public ShardingValue<?> getDatabaseShardingValue(final ShardingKey shardingKey) {
        return databaseShardingValues.get(shardingKey);
    }
    
    /**
     * 获取分表分片键值.
     * 
     * @param shardingKey 分片键
     * @return 分表分片键值
     */
    public ShardingValue<?> getTableShardingValue(final ShardingKey shardingKey) {
        return tableShardingValues.get(shardingKey);
    }
    
    /**
     * 设置数据库操作只路由至主库.
     */
    public void setMasterRouteOnly() {
        masterRouteOnly = true;
    }
    
    @Override
    public void close() {
        HintManagerHolder.clear();
    }
}
