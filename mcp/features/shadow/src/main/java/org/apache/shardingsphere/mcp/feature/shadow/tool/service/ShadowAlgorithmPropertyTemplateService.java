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

package org.apache.shardingsphere.mcp.feature.shadow.tool.service;

import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Shadow algorithm property template service.
 */
public final class ShadowAlgorithmPropertyTemplateService {
    
    private static final Map<String, List<AlgorithmPropertyRequirement>> TEMPLATES = createTemplates();
    
    /**
     * Find property requirements.
     *
     * @param algorithmType shadow algorithm type
     * @return property requirements
     */
    public List<AlgorithmPropertyRequirement> findRequirements(final String algorithmType) {
        return TEMPLATES.getOrDefault(Objects.toString(algorithmType, "").trim().toUpperCase(Locale.ENGLISH), List.of());
    }
    
    private static Map<String, List<AlgorithmPropertyRequirement>> createTemplates() {
        Map<String, List<AlgorithmPropertyRequirement>> result = new LinkedHashMap<>(3, 1F);
        result.put("SQL_HINT", List.of());
        result.put("VALUE_MATCH", List.of(
                new AlgorithmPropertyRequirement("primary", "operation", true, false, "Match operation.", ""),
                new AlgorithmPropertyRequirement("primary", "column", true, false, "Column used by the shadow match.", ""),
                new AlgorithmPropertyRequirement("primary", "value", true, false, "Literal value used by the shadow match.", "")));
        result.put("REGEX_MATCH", List.of(
                new AlgorithmPropertyRequirement("primary", "operation", true, false, "Match operation.", ""),
                new AlgorithmPropertyRequirement("primary", "column", true, false, "Column used by the shadow match.", ""),
                new AlgorithmPropertyRequirement("primary", "regex", true, false, "Regular expression used by the shadow match.", "")));
        return result;
    }
}
