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

package org.apache.shardingsphere.mcp.support.workflow.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.support.workflow.model.SecretReferenceValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * Workflow-scoped planning arguments.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class WorkflowPlanningArguments {
    
    private final Map<String, Object> arguments;
    
    /**
     * Get string argument.
     *
     * @param name argument name
     * @return argument value
     */
    public String getStringArgument(final String name) {
        return Objects.toString(arguments.get(name), "").trim();
    }
    
    /**
     * Get boolean argument.
     *
     * @param name argument name
     * @param defaultValue default value
     * @return argument value
     */
    public boolean getBooleanArgument(final String name, final boolean defaultValue) {
        Object result = arguments.get(name);
        if (null == result) {
            return defaultValue;
        }
        if (result instanceof Boolean) {
            return (Boolean) result;
        }
        String actualValue = result.toString().trim();
        return actualValue.isEmpty() ? defaultValue : Boolean.parseBoolean(actualValue);
    }
    
    /**
     * Get string map argument.
     *
     * @param name argument name
     * @return string map
     */
    public Map<String, String> getMapArgument(final String name) {
        Object rawValue = arguments.get(name);
        if (rawValue instanceof Map) {
            return createMapArgument((Map<?, ?>) rawValue);
        }
        if (rawValue instanceof Collection) {
            return createMapArgument((Collection<?>) rawValue);
        }
        return Collections.emptyMap();
    }
    
    /**
     * Get algorithm property map argument.
     *
     * @param name argument name
     * @param algorithmRole algorithm role
     * @return algorithm property map
     */
    public Map<String, String> getAlgorithmPropertyMapArgument(final String name, final String algorithmRole) {
        return WorkflowSecretReferenceUtils.createAlgorithmProperties(arguments.get(name), algorithmRole);
    }
    
    /**
     * Get secret reference map argument.
     *
     * @param name argument name
     * @return secret reference map
     */
    public Map<String, SecretReferenceValue> getSecretReferenceMapArgument(final String name) {
        return WorkflowSecretReferenceUtils.createSecretReferences(arguments.get(name));
    }
    
    private Map<String, String> createMapArgument(final Map<?, ?> rawValue) {
        Map<String, String> result = new LinkedHashMap<>(rawValue.size(), 1F);
        for (Entry<?, ?> entry : rawValue.entrySet()) {
            String actualKey = Objects.toString(entry.getKey(), "").trim();
            if (!actualKey.isEmpty()) {
                result.put(actualKey, Objects.toString(entry.getValue(), "").trim());
            }
        }
        return result;
    }
    
    private Map<String, String> createMapArgument(final Collection<?> rawValue) {
        Map<String, String> result = new LinkedHashMap<>(rawValue.size(), 1F);
        for (Object each : rawValue) {
            String actualEntry = Objects.toString(each, "").trim();
            int separatorIndex = actualEntry.indexOf('=');
            if (-1 == separatorIndex) {
                continue;
            }
            String actualKey = actualEntry.substring(0, separatorIndex).trim();
            String actualValue = actualEntry.substring(separatorIndex + 1).trim();
            if (!actualKey.isEmpty()) {
                result.put(actualKey, actualValue);
            }
        }
        return result;
    }
}
