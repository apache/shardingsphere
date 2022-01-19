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

package org.apache.shardingsphere.infra.rule.identifier.type;

import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * ShardingSphere rule which supports exporting data.
 */
public interface ExportableRule extends ShardingSphereRule {
    
    /**
     * The methods that the rule can supply.
     *
     * @return export method supplier
     */
    Map<String, Supplier<Object>> getExportedMethods();
    
    /**
     * Export data by specified key.
     *
     * @param keys specified keys
     * @return data map
     */
    default Map<String, Object> export(final Collection<String> keys) {
        Map<String, Supplier<Object>> exportMethods = getExportedMethods();
        Map<String, Object> result = new HashMap<>(keys.size(), 1);
        keys.forEach(each -> {
            if (exportMethods.containsKey(each)) {
                result.put(each, exportMethods.get(each).get());
            }
        });
        return result;
    }
    
    /**
     * Export data by specified key.
     *
     * @param key specified key
     * @return data
     */
    default Optional<Object> export(final String key) {
        Map<String, Supplier<Object>> exportMethods = getExportedMethods();
        if (exportMethods.containsKey(key)) {
            return Optional.ofNullable(exportMethods.get(key).get());
        }
        return Optional.empty();
    }
    
    /**
     * Whether the current rule contains the specified key.
     *
     * @param keys specified keys
     * @return contain or not
     */
    default boolean containExportableKey(final Collection<String> keys) {
        return keys.stream().anyMatch(each -> getExportedMethods().containsKey(each));
    }
}
