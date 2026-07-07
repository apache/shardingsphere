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

package org.apache.shardingsphere.mcp.feature.encrypt.tool.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFeatureData;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Encrypt workflow state.
 */
@Getter
@NoArgsConstructor
public final class EncryptWorkflowState implements WorkflowFeatureData {
    
    private final List<Map<String, Object>> beforeRules = new LinkedList<>();
    
    private final List<Map<String, Object>> expectedRules = new LinkedList<>();
    
    public EncryptWorkflowState(final List<Map<String, Object>> beforeRules, final List<Map<String, Object>> expectedRules) {
        this.beforeRules.addAll(copyRules(beforeRules));
        this.expectedRules.addAll(copyRules(expectedRules));
    }
    
    @Override
    public Map<String, String> getAlgorithmProperties(final String algorithmRole) {
        return Map.of();
    }
    
    @Override
    public EncryptWorkflowState copy() {
        return new EncryptWorkflowState(beforeRules, expectedRules);
    }
    
    private List<Map<String, Object>> copyRules(final List<Map<String, Object>> rules) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (Map<String, Object> each : rules) {
            result.add(copyMap(each));
        }
        return result;
    }
    
    private Map<String, Object> copyMap(final Map<String, Object> original) {
        Map<String, Object> result = new LinkedHashMap<>(original.size(), 1F);
        original.forEach((key, value) -> result.put(key, copyValue(value)));
        return result;
    }
    
    private Object copyValue(final Object original) {
        if (original instanceof final Map<?, ?> originalMap) {
            Map<String, Object> result = new LinkedHashMap<>(originalMap.size(), 1F);
            originalMap.forEach((key, value) -> result.put(String.valueOf(key), copyValue(value)));
            return result;
        }
        if (original instanceof List) {
            return ((List<?>) original).stream().map(this::copyValue).toList();
        }
        return original;
    }
}
