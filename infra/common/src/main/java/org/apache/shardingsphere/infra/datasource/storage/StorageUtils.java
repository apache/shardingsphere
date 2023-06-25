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

package org.apache.shardingsphere.infra.datasource.storage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Storage utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StorageUtils {
    
    /**
     * Get storage units from provided data sources.
     *
     * @param dataSources data sources
     * @return storage units
     */
    public static Map<String, StorageUnit> getStorageUnits(final Map<String, DataSource> dataSources) {
        Map<String, StorageUnit> result = new LinkedHashMap<>(dataSources.size(), 1F);
        for (String each : dataSources.keySet()) {
            result.put(each, new StorageUnit(each, each));
        }
        return result;
    }
    
    /**
     * Get aggregated storage units.
     *
     * @param storageUnits storage units
     * @param builtRules built rules
     * @return aggregated storage units
     */
    public static Map<String, StorageUnit> getAggregatedStorageUnits(final Map<String, StorageUnit> storageUnits, final Collection<ShardingSphereRule> builtRules) {
        Map<String, StorageUnit> result = new LinkedHashMap<>(storageUnits);
        for (ShardingSphereRule each : builtRules) {
            if (each instanceof DataSourceContainedRule) {
                result = getAggregatedDataSourceMap(result, (DataSourceContainedRule) each);
            }
        }
        return result;
    }
    
    private static Map<String, StorageUnit> getAggregatedDataSourceMap(final Map<String, StorageUnit> storageUnits, final DataSourceContainedRule builtRule) {
        Map<String, StorageUnit> result = new LinkedHashMap<>();
        for (Entry<String, Collection<String>> entry : builtRule.getDataSourceMapper().entrySet()) {
            for (String each : entry.getValue()) {
                if (storageUnits.containsKey(each)) {
                    storageUnits.remove(each);
                    result.putIfAbsent(entry.getKey(), new StorageUnit(entry.getKey(), entry.getKey()));
                }
            }
        }
        result.putAll(storageUnits);
        return result;
    }
    
    /**
     * Get catalog string from storage unit.
     *
     * @param storageUnit storage unit
     * @return catalog
     */
    public static String getCatalog(final StorageUnit storageUnit) {
        if (null == storageUnit) {
            return null;
        }
        return storageUnit.getCatalog().isPresent() ? storageUnit.getCatalog().get() : null;
    }
}
