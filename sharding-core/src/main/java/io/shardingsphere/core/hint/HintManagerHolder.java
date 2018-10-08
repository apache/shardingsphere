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

package io.shardingsphere.core.hint;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.shardingsphere.api.HintManager;
import io.shardingsphere.api.algorithm.sharding.ListShardingValue;
import io.shardingsphere.api.algorithm.sharding.ShardingValue;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

/**
 * Hint manager holder.
 * 
 * <p>Use thread-local to manage hint.</p>
 *
 * @author zhangliang
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HintManagerHolder {
    
    public static final String DB_TABLE_NAME = "DB_TABLE_NAME";
    
    public static final String DB_COLUMN_NAME = "DB_COLUMN_NAME";
    
    private static final ThreadLocal<HintManager> HINT_MANAGER_HOLDER = new ThreadLocal<>();
    
    private static final Multimap<String, Comparable<?>> DATABASE_SHARDING_VALUES = HashMultimap.create();
    
    private static final Multimap<String, Comparable<?>> TABLE_SHARDING_VALUES = HashMultimap.create();
    
    @Setter
    private static boolean databaseShardingOnly;
    
    @Setter
    private static boolean isMasterRouteOnly;
    
    /**
     * Set hint manager.
     *
     * @param hintManager hint manager instance
     */
    public static void setHintManager(final HintManager hintManager) {
        Preconditions.checkState(null == HINT_MANAGER_HOLDER.get(), "HintManagerHolder has previous value, please clear first.");
        HINT_MANAGER_HOLDER.set(hintManager);
    }
    
    /**
     * Add sharding value for one certain sharding database.
     *
     * <p>The sharding operator is {@code =}</p>
     * When you need to assign the values to one certain sharding database, use this method to add sharding value for this database.
     *
     * @param value sharding value
     */
    public static void setDatabaseShardingValue(final Comparable<?> value) {
        DATABASE_SHARDING_VALUES.clear();
        addDatabaseShardingValue(DB_TABLE_NAME, value);
        databaseShardingOnly = true;
    }
    
    /**
     * Add sharding value for database.
     *
     * <p>The sharding operator is {@code =}</p>
     *
     * @param logicTable logic table name
     * @param value sharding value
     */
    public static void addDatabaseShardingValue(final String logicTable, final Comparable<?> value) {
        DATABASE_SHARDING_VALUES.put(logicTable, value);
        databaseShardingOnly = false;
    }
    
    /**
     * Add sharding value for table.
     *
     * <p>The sharding operator is {@code =}</p>
     *
     * @param logicTable logic table name
     * @param value sharding value
     */
    public static void addTableShardingValue(final String logicTable, final Comparable<?> value) {
        TABLE_SHARDING_VALUES.put(logicTable, value);
        databaseShardingOnly = false;
    }
    
    /**
     * Judge whether only database is sharding.
     *
     * @return database sharding or not
     */
    public static boolean isDatabaseShardingOnly() {
        return null != HINT_MANAGER_HOLDER.get() && databaseShardingOnly;
    }
    
    /**
     * Judge whether it is routed to master database or not.
     *
     * @return is force route to master database only or not
     */
    public static boolean isMasterRouteOnly() {
        return null != HINT_MANAGER_HOLDER.get() && isMasterRouteOnly;
    }
    
    /**
     * Get database sharding value.
     * 
     * @param logicTable logic table
     * @return database sharding value
     */
    public static Optional<ShardingValue> getDatabaseShardingValue(final String logicTable) {
        if (null == HINT_MANAGER_HOLDER.get() || !DATABASE_SHARDING_VALUES.containsKey(logicTable)) {
            return Optional.absent();
        }
        return Optional.of(getShardingValue(logicTable, DATABASE_SHARDING_VALUES.get(logicTable)));
    }
    
    /**
     * Get table sharding value.
     *
     * @param logicTable logic table name
     * @return table sharding value
     */
    public static Optional<ShardingValue> getTableShardingValue(final String logicTable) {
        if (null == HINT_MANAGER_HOLDER.get() || !TABLE_SHARDING_VALUES.containsKey(logicTable)) {
            return Optional.absent();
        }
        return Optional.of(getShardingValue(logicTable, TABLE_SHARDING_VALUES.get(logicTable)));
    }
    
    @SuppressWarnings("unchecked")
    private static ShardingValue getShardingValue(final String logicTable, final Collection<Comparable<?>> values) {
        Preconditions.checkArgument(null != values && !values.isEmpty());
        return new ListShardingValue(logicTable, DB_COLUMN_NAME, values);
    }
    
    /**
     * Get hint manager in current thread.
     *
     * @return hint manager in current thread
     */
    public static HintManager get() {
        return HINT_MANAGER_HOLDER.get();
    }
    
    /**
     * Clear hint manager for current thread-local.
     */
    public static void clear() {
        HINT_MANAGER_HOLDER.remove();
        DATABASE_SHARDING_VALUES.clear();
        TABLE_SHARDING_VALUES.clear();
        databaseShardingOnly = false;
        isMasterRouteOnly = false;
    }
}
