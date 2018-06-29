/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.api;

import com.google.common.base.Preconditions;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import io.shardingsphere.core.api.algorithm.sharding.ListShardingValue;
import io.shardingsphere.core.api.algorithm.sharding.RangeShardingValue;
import io.shardingsphere.core.api.algorithm.sharding.ShardingValue;
import io.shardingsphere.core.constant.ShardingOperator;
import io.shardingsphere.core.hint.HintManagerHolder;
import io.shardingsphere.core.hint.ShardingKey;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The manager that use hint to inject sharding key directly through {@code ThreadLocal}.
 *
 * @author gaohongtao
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HintManager implements AutoCloseable {
    
    private final Map<ShardingKey, ShardingValue> databaseShardingValues = new HashMap<>();
    
    private final Map<ShardingKey, ShardingValue> tableShardingValues = new HashMap<>();
    
    @Getter
    private boolean shardingHint;
    
    @Getter
    private boolean masterRouteOnly;
    
    @Getter
    private boolean databaseShardingOnly;
    
    /**
     * Get a new instance for {@code HintManager}.
     *
     * @return  {@code HintManager} instance
     */
    public static HintManager getInstance() {
        HintManager result = new HintManager();
        HintManagerHolder.setHintManager(result);
        return result;
    }
    
    /**
     * Set sharding value for database sharding only.
     *
     * <p>The sharding operator is {@code =}</p>
     *
     * @param value sharding value
     */
    public void setDatabaseShardingValue(final Comparable<?> value) {
        databaseShardingOnly = true;
        addDatabaseShardingValue(HintManagerHolder.DB_TABLE_NAME, HintManagerHolder.DB_COLUMN_NAME, value);
    }
    
    /**
     * Add sharding value for database.
     *
     * <p>The sharding operator is {@code =}</p>
     *
     * @param logicTable logic table name
     * @param shardingColumn sharding column name
     * @param value sharding value
     */
    public void addDatabaseShardingValue(final String logicTable, final String shardingColumn, final Comparable<?> value) {
        addDatabaseShardingValue(logicTable, shardingColumn, ShardingOperator.EQUAL, value);
    }
    
    /**
     * Add sharding value for database.
     *
     * @param logicTable logic table name
     * @param shardingColumn sharding column name
     * @param operator sharding operator
     * @param values sharding value
     */
    public void addDatabaseShardingValue(final String logicTable, final String shardingColumn, final ShardingOperator operator, final Comparable<?>... values) {
        shardingHint = true;
        databaseShardingValues.put(new ShardingKey(logicTable, shardingColumn), getShardingValue(logicTable, shardingColumn, operator, values));
    }
    
    /**
     * Add sharding value for table.
     *
     * <p>The sharding operator is {@code =}</p>
     *
     * @param logicTable logic table name
     * @param shardingColumn sharding column name
     * @param value sharding value
     */
    public void addTableShardingValue(final String logicTable, final String shardingColumn, final Comparable<?> value) {
        addTableShardingValue(logicTable, shardingColumn, ShardingOperator.EQUAL, value);
    }
    
    /**
     * Add sharding value for table.
     *
     * @param logicTable logic table name
     * @param shardingColumn sharding column name
     * @param operator sharding operator
     * @param values sharding value
     */
    public void addTableShardingValue(final String logicTable, final String shardingColumn, final ShardingOperator operator, final Comparable<?>... values) {
        shardingHint = true;
        tableShardingValues.put(new ShardingKey(logicTable, shardingColumn), getShardingValue(logicTable, shardingColumn, operator, values));
    }
    
    @SuppressWarnings("unchecked")
    private ShardingValue getShardingValue(final String logicTable, final String shardingColumn, final ShardingOperator operator, final Comparable<?>[] values) {
        Preconditions.checkArgument(null != values && values.length > 0);
        switch (operator) {
            case EQUAL:
            case IN:
                return new ListShardingValue(logicTable, shardingColumn, Arrays.asList(values));
            case BETWEEN:
                return new RangeShardingValue(logicTable, shardingColumn, Range.range(values[0], BoundType.CLOSED, values[1], BoundType.CLOSED));
            default:
                throw new UnsupportedOperationException(operator.getExpression());
        }
    }
    
    /**
     * Get sharding value for database.
     *
     * @param shardingKey sharding key
     * @return sharding value for database
     */
    public ShardingValue getDatabaseShardingValue(final ShardingKey shardingKey) {
        return databaseShardingValues.get(shardingKey);
    }
    
    /**
     * Get sharding value for table.
     *
     * @param shardingKey sharding key
     * @return sharding value for table
     */
    public ShardingValue getTableShardingValue(final ShardingKey shardingKey) {
        return tableShardingValues.get(shardingKey);
    }
    
    /**
     * Set CRUD operation force route to master database only.
     */
    public void setMasterRouteOnly() {
        masterRouteOnly = true;
    }
    
    @Override
    public void close() {
        HintManagerHolder.clear();
    }
}
