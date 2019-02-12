/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.hint;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.api.hint.HintManager;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.routing.value.ListRouteValue;
import org.apache.shardingsphere.core.routing.value.RouteValue;

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
     * Judge whether only database is sharding.
     *
     * @return database sharding or not
     */
    public static boolean isDatabaseShardingOnly() {
        return null != HINT_MANAGER_HOLDER.get() && HINT_MANAGER_HOLDER.get().isDatabaseShardingOnly();
    }
    
    /**
     * Judge whether it is routed to master database or not.
     *
     * @return is force route to master database only or not
     */
    public static boolean isMasterRouteOnly() {
        return null != HINT_MANAGER_HOLDER.get() && HINT_MANAGER_HOLDER.get().isMasterRouteOnly();
    }
    
    /**
     * Get database sharding value.
     * 
     * @param logicTable logic table
     * @return database sharding value
     */
    public static Optional<RouteValue> getDatabaseShardingValue(final String logicTable) {
        if (null == HINT_MANAGER_HOLDER.get() || !HINT_MANAGER_HOLDER.get().getDatabaseShardingValues().containsKey(logicTable)) {
            return Optional.absent();
        }
        return Optional.of(getShardingValue(logicTable, HINT_MANAGER_HOLDER.get().getDatabaseShardingValues().get(logicTable)));
    }
    
    /**
     * Get table sharding value.
     *
     * @param logicTable logic table name
     * @return table sharding value
     */
    public static Optional<RouteValue> getTableShardingValue(final String logicTable) {
        if (null == HINT_MANAGER_HOLDER.get() || !HINT_MANAGER_HOLDER.get().getTableShardingValues().containsKey(logicTable)) {
            return Optional.absent();
        }
        return Optional.of(getShardingValue(logicTable, HINT_MANAGER_HOLDER.get().getTableShardingValues().get(logicTable)));
    }
    
    private static RouteValue getShardingValue(final String logicTable, final Collection<Comparable<?>> values) {
        Preconditions.checkArgument(null != values && !values.isEmpty());
        return new ListRouteValue<>(new Column(DB_COLUMN_NAME, logicTable), values);
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
    }
}
