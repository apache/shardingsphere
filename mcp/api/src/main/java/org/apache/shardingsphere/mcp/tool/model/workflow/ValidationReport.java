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
        result.put("ddl_validation", null == ddlValidation ? null : ddlValidation.toMap());
        result.put("rule_validation", null == ruleValidation ? null : ruleValidation.toMap());
        result.put("logical_metadata_validation", null == logicalMetadataValidation ? null : logicalMetadataValidation.toMap());
        result.put("sql_executability_validation", null == sqlExecutabilityValidation ? null : sqlExecutabilityValidation.toMap());
        result.put("overall_status", overallStatus);
        result.put("mismatches", mismatches);
        return result;
    }
}
