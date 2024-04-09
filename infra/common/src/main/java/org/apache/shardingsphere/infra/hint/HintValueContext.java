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

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;

/**
 * Hint value context.
 */
@Getter
@Setter
public final class HintValueContext {
    
    private final Multimap<String, Comparable<?>> shardingDatabaseValues = ArrayListMultimap.create();
    
    private final Multimap<String, Comparable<?>> shardingTableValues = ArrayListMultimap.create();
    
    private final Collection<String> disableAuditNames = new LinkedHashSet<>();
    
    private String dataSourceName = "";
    
    private boolean databaseShardingOnly;
    
    private boolean writeRouteOnly;
    
    private boolean useTraffic;
    
    private boolean skipSQLRewrite;
    
    private boolean shadow;
    
    /**
     * Find hint data source name.
     *
     * @return data source name
     */
    public Optional<String> findHintDataSourceName() {
        return dataSourceName.isEmpty() ? Optional.empty() : Optional.of(dataSourceName);
    }
    
    /**
     * Judge contains hint sharding databases value or not.
     *
     * @param tableName table name
     * @return contains hint sharding databases value or not
     */
    public boolean containsHintShardingDatabaseValue(final String tableName) {
        String key = Joiner.on(".").join(tableName.toUpperCase(), SQLHintPropertiesKey.SHARDING_DATABASE_VALUE_KEY.getKey());
        return shardingDatabaseValues.containsKey(key) || shardingDatabaseValues.containsKey(SQLHintPropertiesKey.SHARDING_DATABASE_VALUE_KEY.getKey());
    }
    
    /**
     * Judge contains hint sharding table value or not.
     *
     * @param tableName table name
     * @return Contains hint sharding table value or not
     */
    public boolean containsHintShardingTableValue(final String tableName) {
        String key = Joiner.on(".").join(tableName.toUpperCase(), SQLHintPropertiesKey.SHARDING_TABLE_VALUE_KEY.getKey());
        return shardingTableValues.containsKey(key) || shardingTableValues.containsKey(SQLHintPropertiesKey.SHARDING_TABLE_VALUE_KEY.getKey());
    }
    
    /**
     * Judge contains hint sharding value or not.
     *
     * @param tableName table name
     * @return Contains hint sharding value or not
     */
    public boolean containsHintShardingValue(final String tableName) {
        return containsHintShardingDatabaseValue(tableName) || containsHintShardingTableValue(tableName);
    }
    
    /**
     * Get hint sharding table value.
     *
     * @param tableName table name
     * @return sharding table value
     */
    public Collection<Comparable<?>> getHintShardingTableValue(final String tableName) {
        String key = String.join(".", tableName.toUpperCase(), SQLHintPropertiesKey.SHARDING_TABLE_VALUE_KEY.getKey());
        return shardingTableValues.containsKey(key)
                ? shardingTableValues.get(key)
                : shardingTableValues.get(SQLHintPropertiesKey.SHARDING_TABLE_VALUE_KEY.getKey());
    }
    
    /**
     * Get hint sharding database value.
     *
     * @param tableName table name
     * @return sharding database value
     */
    public Collection<Comparable<?>> getHintShardingDatabaseValue(final String tableName) {
        String key = String.join(".", tableName.toUpperCase(), SQLHintPropertiesKey.SHARDING_DATABASE_VALUE_KEY.getKey());
        return shardingDatabaseValues.containsKey(key)
                ? shardingDatabaseValues.get(key)
                : shardingDatabaseValues.get(SQLHintPropertiesKey.SHARDING_DATABASE_VALUE_KEY.getKey());
    }
}
