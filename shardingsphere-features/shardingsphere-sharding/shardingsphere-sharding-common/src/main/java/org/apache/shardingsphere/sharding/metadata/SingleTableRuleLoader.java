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

package org.apache.shardingsphere.sharding.metadata;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.SchemaMetaDataLoader;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.SingleTableRule;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Single table rule loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SingleTableRuleLoader {
    
    /**
     * Load single table rules.
     * 
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @param shardingRule sharding rule
     * @return single table rule map
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("CollectionWithoutInitialCapacity")
    public static Map<String, SingleTableRule> load(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule) throws SQLException {
        Collection<String> excludedTables = getExcludedTables(shardingRule);
        Map<String, SingleTableRule> result = new HashMap<>();
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            result.putAll(load(databaseType, entry.getKey(), entry.getValue(), excludedTables));
        }
        return result;
    }
    
    private static Map<String, SingleTableRule> load(final DatabaseType databaseType,
                                                     final String dataSourceName, final DataSource dataSource, final Collection<String> excludedTables) throws SQLException {
        Collection<String> tables = SchemaMetaDataLoader.loadAllTableNames(dataSource, databaseType);
        Map<String, SingleTableRule> result = new HashMap<>(tables.size(), 1);
        for (String each : tables) {
            if (!excludedTables.contains(each)) {
                result.put(each, new SingleTableRule(each, dataSourceName));
            }
        }
        return result;
    }
    
    private static Collection<String> getExcludedTables(final ShardingRule shardingRule) {
        Collection<String> result = new HashSet<>(shardingRule.getTables());
        result.addAll(shardingRule.getAllActualTables());
        return result;
    }
}
