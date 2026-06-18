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

package org.apache.shardingsphere.mcp.support.workflow.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Secret reference value.
 */
@RequiredArgsConstructor
@Getter
public final class SecretReferenceValue {
    
    private final boolean malformed;
    
    /**
     * Create secret reference value.
     *
     * @return secret reference value
     */
    public static SecretReferenceValue create() {
        return new SecretReferenceValue(false);
    }
    
    /**
     * Create malformed secret reference value.
     *
     * @return malformed secret reference value
     */
    public static SecretReferenceValue malformed() {
        return new SecretReferenceValue(true);
    }
    
    /**
     * Create SQL placeholder.
     *
     * @param algorithmRole algorithm role
     * @param propertyKey property key
     * @return SQL placeholder
     */
    public static String createPlaceholder(final String algorithmRole, final String propertyKey) {
        return String.format("secret_reference:%s.%s", normalize(algorithmRole), normalize(propertyKey));
    }
    
    /**
     * Create manual placeholder.
     *
     * @param algorithmRole algorithm role
     * @param propertyKey property key
     * @return manual placeholder
     */
    public static String createManualPlaceholder(final String algorithmRole, final String propertyKey) {
        return String.format("<SECRET_VALUE_%s_%s>", normalizeForManualPlaceholder(algorithmRole), normalizeForManualPlaceholder(propertyKey));
    }
    
    /**
     * Convert to safe summary.
     *
     * @param algorithmRole algorithm role
     * @param propertyKey property key
     * @return safe summary
     */
    public Map<String, Object> toSafeSummary(final String algorithmRole, final String propertyKey) {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        result.put("algorithm_role", algorithmRole);
        result.put("property_key", propertyKey);
        result.put("label", String.format("secret_placeholder:%s.%s", normalize(algorithmRole), normalize(propertyKey)));
        result.put("manual_placeholder", createManualPlaceholder(algorithmRole, propertyKey));
        result.put("replacement_required", true);
        result.put("malformed", malformed);
        return result;
    }
    
    private static String normalize(final String value) {
        return null == value ? "" : value.trim();
    }
    
    private static String normalizeForManualPlaceholder(final String value) {
        String result = normalize(value).toUpperCase().replaceAll("[^A-Z0-9]+", "_");
        return result.isEmpty() ? "VALUE" : result;
    }
}
