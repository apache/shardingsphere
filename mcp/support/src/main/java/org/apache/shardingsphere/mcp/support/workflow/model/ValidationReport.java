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
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Validation report.
 */
@Getter
@Setter
public final class ValidationReport {
    
    private ValidationSection ddlValidation;
    
    private ValidationSection ruleValidation;
    
    private ValidationSection logicalMetadataValidation;
    
    private ValidationSection sqlExecutabilityValidation;
    
    private String overallStatus;
    
    private final List<Map<String, Object>> mismatches = new LinkedList<>();
    
    /**
     * Convert to map.
     *
     * @return map representation
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        putValidationSection(result, "ddl_validation", ddlValidation);
        putValidationSection(result, "rule_validation", ruleValidation);
        putValidationSection(result, "logical_metadata_validation", logicalMetadataValidation);
        putValidationSection(result, "sql_executability_validation", sqlExecutabilityValidation);
        result.put("overall_status", overallStatus);
        result.put("mismatches", mismatches);
        return result;
    }
    
    private void putValidationSection(final Map<String, Object> target, final String key, final ValidationSection section) {
        if (null != section) {
            target.put(key, section.toMap());
        }
    }
}
