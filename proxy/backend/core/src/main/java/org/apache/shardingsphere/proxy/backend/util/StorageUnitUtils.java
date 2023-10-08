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
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Storage unit utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StorageUnitUtils {
    
    /**
     * Export configuration data to specified file.
     * 
     * @param ruleMetaData rule meta data
     * @return in used storage units
     */
    public static Map<String, Collection<String>> getInUsedStorageUnits(final RuleMetaData ruleMetaData) {
        Map<String, Collection<String>> result = new LinkedHashMap<>();
        for (DataSourceContainedRule each : ruleMetaData.findRules(DataSourceContainedRule.class)) {
            result.putAll(getInUsedStorageUnits(each, getInUsedStorageUnitNames(each)));
        }
        for (DataNodeContainedRule each : ruleMetaData.findRules(DataNodeContainedRule.class)) {
            result.putAll(getInUsedStorageUnits(each, getInUsedStorageUnitNames(each)));
        }
        return result;
    }
    
    private static Map<String, Collection<String>> getInUsedStorageUnits(final ShardingSphereRule rule, final Collection<String> inUsedStorageUnitNames) {
        Map<String, Collection<String>> result = new LinkedHashMap<>();
        for (String each : inUsedStorageUnitNames) {
            if (!result.containsKey(each)) {
                result.put(each, new LinkedHashSet<>());
            }
            result.get(each).add(rule.getClass().getSimpleName());
        }
        return result;
    }
    
    private static Collection<String> getInUsedStorageUnitNames(final DataSourceContainedRule rule) {
        return rule.getDataSourceMapper().values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }
    
    private static Collection<String> getInUsedStorageUnitNames(final DataNodeContainedRule rule) {
        return rule.getAllDataNodes().values().stream().flatMap(each -> each.stream().map(DataNode::getDataSourceName).collect(Collectors.toSet()).stream()).collect(Collectors.toSet());
    }
}
