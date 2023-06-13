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

package org.apache.shardingsphere.single.util;

import com.google.common.base.Splitter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

/**
 * Single table load utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SingleTableLoadUtils {
    
    private static final String DELIMITER = ",";
    
    /**
     * Get aggregated data source map.
     * 
     * @param dataSourceMap data source map
     * @param builtRules built rules
     * @return aggregated data source map
     */
    public static Map<String, DataSource> getAggregatedDataSourceMap(final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> builtRules) {
        Map<String, DataSource> result = new LinkedHashMap<>(dataSourceMap);
        for (ShardingSphereRule each : builtRules) {
            if (each instanceof DataSourceContainedRule) {
                result = getAggregatedDataSourceMap(result, (DataSourceContainedRule) each);
            }
        }
        return result;
    }
    
    private static Map<String, DataSource> getAggregatedDataSourceMap(final Map<String, DataSource> dataSourceMap, final DataSourceContainedRule builtRule) {
        Map<String, DataSource> result = new LinkedHashMap<>();
        for (Entry<String, Collection<String>> entry : builtRule.getDataSourceMapper().entrySet()) {
            for (String each : entry.getValue()) {
                if (dataSourceMap.containsKey(each)) {
                    result.putIfAbsent(entry.getKey(), dataSourceMap.remove(each));
                }
            }
        }
        result.putAll(dataSourceMap);
        return result;
    }
    
    /**
     * Get loaded tables.
     *
     * @param builtRules built rules
     * @return loaded tables
     */
    public static Collection<String> getLoadedTables(final Collection<ShardingSphereRule> builtRules) {
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (ShardingSphereRule each : builtRules) {
            if (!(each instanceof TableContainedRule)) {
                continue;
            }
            result.addAll(((TableContainedRule) each).getDistributedTableMapper().getTableNames());
            result.addAll(((TableContainedRule) each).getActualTableMapper().getTableNames());
        }
        return result;
    }
    
    /**
     * Get feature required single tables.
     *
     * @param builtRules built rules
     * @param excludedTables excluded tables
     * @return feature required single tables
     */
    public static Collection<String> getFeatureRequiredSingleTables(final Collection<ShardingSphereRule> builtRules, final Collection<String> excludedTables) {
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (ShardingSphereRule each : builtRules) {
            if (!(each instanceof TableContainedRule)) {
                continue;
            }
            TableContainedRule tableContainedRule = (TableContainedRule) each;
            if (tableContainedRule.getEnhancedTableMapper().getTableNames().isEmpty() || !tableContainedRule.getDistributedTableMapper().getTableNames().isEmpty()) {
                continue;
            }
            result.addAll(getRequiredTables(tableContainedRule.getEnhancedTableMapper().getTableNames(), excludedTables));
        }
        return result;
    }
    
    private static Collection<String> getRequiredTables(final Collection<String> requiredTables, final Collection<String> excludedTables) {
        Collection<String> result = new LinkedList<>();
        for (final String each : requiredTables) {
            if (!excludedTables.contains(each)) {
                result.add(each);
            }
        }
        return result;
    }
    
    /**
     * Split table lines.
     *
     * @param tables tables in configuration
     * @return split tables
     */
    public static Collection<String> splitTableLines(final Collection<String> tables) {
        Collection<String> result = new LinkedHashSet<>();
        for (String each : tables) {
            if (each.contains(DELIMITER)) {
                result.addAll(Splitter.on(DELIMITER).omitEmptyStrings().splitToList(each));
            } else {
                result.add(each);
            }
        }
        return result;
    }
    
    /**
     * Convert tables to data nodes.
     *
     * @param databaseName database name
     * @param databaseType database type
     * @param tables tables in configuration
     * @return data nodes
     */
    public static Collection<DataNode> convertToDataNodes(final String databaseName, final DatabaseType databaseType, final Collection<String> tables) {
        Collection<DataNode> result = new LinkedHashSet<>();
        for (String each : tables) {
            result.add(new DataNode(databaseName, databaseType, each));
        }
        return result;
    }
}
