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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import io.shardingsphere.core.api.algorithm.sharding.ListShardingValue;
import io.shardingsphere.core.api.algorithm.sharding.RangeShardingValue;
import io.shardingsphere.core.api.algorithm.sharding.ShardingValue;
import io.shardingsphere.core.constant.ShardingOperator;
import io.shardingsphere.core.hint.HintManagerHolder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The manager that use hint to inject sharding key directly through {@code ThreadLocal}.
 *
 * @author gaohongtao
 * @author zhangliang
 * @author panjun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HintManager implements AutoCloseable {
    
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
     * Add sharding value for database sharding only.
     *
     * <p>The sharding operator is {@code =}</p>
     * When you only need to sharding database, use this method to add database sharding value.
     *
     * @param value sharding value
     */
    public void setDatabaseShardingValue(final Comparable<?> value) {
        HintManagerHolder.setDatabaseShardingValue(value);
    }
    
    /**
     * Add sharding value for database.
     *
     * <p>The sharding operator is {@code =}</p>
     *
     * @param logicTable logic table name
     * @param value sharding value
     */
    public void addDatabaseShardingValue(final String logicTable, final Comparable<?> value) {
        HintManagerHolder.addDatabaseShardingValue(logicTable, value);
    }
    
    /**
     * Add sharding value for table.
     *
     * <p>The sharding operator is {@code =}</p>
     *
     * @param logicTable logic table name
     * @param value sharding value
     */
    public void addTableShardingValue(final String logicTable, final Comparable<?> value) {
        tableShardingValues.put(logicTable, value);
        HintManagerHolder.setDatabaseShardingOnly(false);
    }
    
    @SuppressWarnings("unchecked")
    private ShardingValue getShardingValue(final String logicTable, final String shardingColumn, final Comparable<?>[] values) {
        Preconditions.checkArgument(null != values && values.length > 0);
        return new ListShardingValue(logicTable, shardingColumn, Arrays.asList(values));
    }
    
    /**
     * Get sharding value for database.
     *
     * @param logicTable logic table name
     * @return sharding value for database
     */
    public ShardingValue getDatabaseShardingValue(final String logicTable) {
        return databaseShardingValues.get(logicTable);
    }
    
    /**
     * Get sharding value for table.
     *
     * @param logicTable logic table name
     * @return sharding value for table
     */
    public ShardingValue getTableShardingValue(final String logicTable) {
        return tableShardingValues.get(logicTable);
    }
    
    /**
     * Set CRUD operation force route to master database only.
     */
    public void setMasterRouteOnly() {
        HintManagerHolder.setMasterRouteOnly(true);
    }
    
    @Override
    public void close() {
        HintManagerHolder.clear();
    }
}
