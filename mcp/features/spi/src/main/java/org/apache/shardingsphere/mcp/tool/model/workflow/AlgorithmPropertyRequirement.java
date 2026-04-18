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

package org.apache.shardingsphere.mcp.tool.model.workflow;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Algorithm property requirement.
 */
@RequiredArgsConstructor
@Getter
public final class AlgorithmPropertyRequirement {
    
    private final String algorithmRole;
    
    private final String propertyKey;
    
    private final boolean required;
    
    private final boolean secret;
    
    private final String description;
    
    private final String defaultValue;
    
    /**
     * Convert to map.
     *
     * @return map representation
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("algorithm_role", algorithmRole);
        result.put("property_key", propertyKey);
        result.put("required", required);
        result.put("secret", secret);
        result.put("description", description);
        result.put("default_value", defaultValue);
        return result;
    }
}
