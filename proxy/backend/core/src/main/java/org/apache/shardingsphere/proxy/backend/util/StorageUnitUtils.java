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

package org.apache.shardingsphere.proxy.backend.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Storage unit utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StorageUnitUtils {
    
    /**
     * Export configuration data to specified file.
     * 
     * @param ruleMetaData ShardingSphere rule meta data
     * @param initialCapacity the initial capacity for map
     * @return in used storage units
     */
    public static Map<String, Collection<String>> getInUsedStorageUnits(final ShardingSphereRuleMetaData ruleMetaData, final int initialCapacity) {
        Map<String, Collection<String>> result = new LinkedHashMap<>(initialCapacity, 1F);
        getFromDataSourceContainedRules(result, ruleMetaData.findRules(DataSourceContainedRule.class));
        getFromDataNodeContainedRules(result, ruleMetaData.findRules(DataNodeContainedRule.class));
        return result;
    }
    
    private static void getFromDataSourceContainedRules(final Map<String, Collection<String>> result, final Collection<DataSourceContainedRule> dataSourceContainedRules) {
        for (DataSourceContainedRule each : dataSourceContainedRules) {
            Collection<String> inUsedStorageUnits = getInUsedStorageUnitNames(each);
            if (inUsedStorageUnits.isEmpty()) {
                continue;
            }
            inUsedStorageUnits.forEach(storageUnit -> {
                Collection<String> rules = result.getOrDefault(storageUnit, new LinkedHashSet<>());
                rules.add(each.getType());
                result.put(storageUnit, rules);
            });
        }
    }
    
    private static void getFromDataNodeContainedRules(final Map<String, Collection<String>> result, final Collection<DataNodeContainedRule> dataNodeContainedRules) {
        for (DataNodeContainedRule each : dataNodeContainedRules) {
            Collection<String> inUsedStorageUnits = getInUsedStorageUnitNames(each);
            if (inUsedStorageUnits.isEmpty()) {
                continue;
            }
            inUsedStorageUnits.forEach(storageUnit -> {
                Collection<String> rules = result.getOrDefault(storageUnit, new LinkedHashSet<>());
                rules.add(each.getType());
                result.put(storageUnit, rules);
            });
        }
    }
    
    private static Collection<String> getInUsedStorageUnitNames(final DataSourceContainedRule rule) {
        Set<String> result = new HashSet<>();
        for (Collection<String> each : rule.getDataSourceMapper().values()) {
            result.addAll(each);
        }
        return result;
    }
    
    private static Collection<String> getInUsedStorageUnitNames(final DataNodeContainedRule rule) {
        Set<String> result = new HashSet<>();
        for (Collection<DataNode> each : rule.getAllDataNodes().values()) {
            result.addAll(each.stream().map(DataNode::getDataSourceName).collect(Collectors.toSet()));
        }
        return result;
    }
}
