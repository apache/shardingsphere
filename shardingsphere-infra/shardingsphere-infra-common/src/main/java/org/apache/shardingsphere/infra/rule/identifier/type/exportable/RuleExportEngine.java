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

package org.apache.shardingsphere.infra.rule.identifier.type.exportable;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Rule export engine.
 */
@RequiredArgsConstructor
public final class RuleExportEngine {
    
    private final ExportableRule rule;
    
    /**
     * Export data by specified key.
     *
     * @param keys specified keys
     * @return data map
     */
    public Map<String, Object> export(final Collection<String> keys) {
        Map<String, Object> exportMethods = rule.getExportData();
        Map<String, Object> result = new HashMap<>(keys.size(), 1);
        keys.forEach(each -> {
            if (exportMethods.containsKey(each)) {
                result.put(each, exportMethods.get(each));
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
    public Optional<Object> export(final String key) {
        return Optional.ofNullable(rule.getExportData().get(key));
    }
    
    /**
     * Whether the current rule contains the specified key.
     *
     * @param keys specified keys
     * @return contain or not
     */
    public boolean containExportableKey(final Collection<String> keys) {
        return keys.stream().anyMatch(each -> rule.getExportData().containsKey(each));
    }
}
