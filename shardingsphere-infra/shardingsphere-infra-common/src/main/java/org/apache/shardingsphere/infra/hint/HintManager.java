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

package org.apache.shardingsphere.infra.hint;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Collections;

/**
 * The manager that use hint to inject sharding key directly through {@code ThreadLocal}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HintManager implements AutoCloseable {
    
    private static final ThreadLocal<HintManager> HINT_MANAGER_HOLDER = new ThreadLocal<>();
    
    private final Multimap<String, Comparable<?>> databaseShardingValues = HashMultimap.create();
    
    private final Multimap<String, Comparable<?>> tableShardingValues = HashMultimap.create();
    
    private boolean databaseShardingOnly;
    
    private boolean masterRouteOnly;
    
    /**
     * Get a new instance for {@code HintManager}.
     *
     * @return  {@code HintManager} instance
     */
    public static HintManager getInstance() {
        Preconditions.checkState(null == HINT_MANAGER_HOLDER.get(), "Hint has previous value, please clear first.");
        HintManager result = new HintManager();
        HINT_MANAGER_HOLDER.set(result);
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
        databaseShardingValues.clear();
        tableShardingValues.clear();
        databaseShardingValues.put("", value);
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
    public void addDatabaseShardingValue(final String logicTable, final Comparable<?> value) {
        if (databaseShardingOnly) {
            databaseShardingValues.removeAll("");
        }
        databaseShardingValues.put(logicTable, value);
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
    public void addTableShardingValue(final String logicTable, final Comparable<?> value) {
        if (databaseShardingOnly) {
            databaseShardingValues.removeAll("");
        }
        tableShardingValues.put(logicTable, value);
        databaseShardingOnly = false;
    }
    
    /**
     * Get database sharding values.
     *
     * @return database sharding values
     */
    public static Collection<Comparable<?>> getDatabaseShardingValues() {
        return getDatabaseShardingValues("");
    }
    
    /**
     * Get database sharding values.
     *
     * @param logicTable logic table
     * @return database sharding values
     */
    public static Collection<Comparable<?>> getDatabaseShardingValues(final String logicTable) {
        return null == HINT_MANAGER_HOLDER.get() ? Collections.emptyList() : HINT_MANAGER_HOLDER.get().databaseShardingValues.get(logicTable);
    }
    
    /**
     * Get table sharding values.
     *
     * @param logicTable logic table name
     * @return table sharding values
     */
    public static Collection<Comparable<?>> getTableShardingValues(final String logicTable) {
        return null == HINT_MANAGER_HOLDER.get() ? Collections.emptyList() : HINT_MANAGER_HOLDER.get().tableShardingValues.get(logicTable);
    }
    
    /**
     * Judge whether database sharding only.
     *
     * @return database sharding or not
     */
    public static boolean isDatabaseShardingOnly() {
        return null != HINT_MANAGER_HOLDER.get() && HINT_MANAGER_HOLDER.get().databaseShardingOnly;
    }
    
    /**
     * Set database operation force route to master database only.
     */
    public void setMasterRouteOnly() {
        masterRouteOnly = true;
    }
    
    /**
     * Judge whether route to master database only or not.
     *
     * @return route to master database only or not
     */
    public static boolean isMasterRouteOnly() {
        return null != HINT_MANAGER_HOLDER.get() && HINT_MANAGER_HOLDER.get().masterRouteOnly;
    }
    
    /**
     * Clear threadlocal for hint manager.
     */
    public static void clear() {
        HINT_MANAGER_HOLDER.remove();
    }
    
    @Override
    public void close() {
        clear();
    }
}
