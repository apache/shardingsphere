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

package io.shardingsphere.api;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.shardingsphere.core.hint.HintManagerHolder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The manager that use hint to inject sharding key directly through {@code ThreadLocal}.
 *
 * @author gaohongtao
 * @author zhangliang
 * @author panjun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HintManager implements AutoCloseable {
    
    @Getter
    private final Multimap<String, Comparable<?>> databaseShardingValues = HashMultimap.create();

    @Getter
    private final Multimap<String, Comparable<?>> tableShardingValues = HashMultimap.create();
    
    @Getter
    private boolean databaseShardingOnly;

    @Getter
    private boolean masterRouteOnly;

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
        databaseShardingValues.clear();
        addDatabaseShardingValue(HintManagerHolder.DB_TABLE_NAME, value);
        databaseShardingOnly = true;
    }
    
    /**
     * Set CRUD operation force route to master database only.
     */
    public void setMasterRouteOnly() {
        masterRouteOnly = true;
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
        tableShardingValues.put(logicTable, value);
        databaseShardingOnly = false;
    }
    
    @Override
    public void close() {
        HintManagerHolder.clear();
    }
}
