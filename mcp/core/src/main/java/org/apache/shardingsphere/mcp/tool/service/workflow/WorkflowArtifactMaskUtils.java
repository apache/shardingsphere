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

package org.apache.shardingsphere.mcp.tool.service.workflow;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.tool.model.workflow.RuleArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Workflow artifact masking utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowArtifactMaskUtils {
    
    /**
     * Create masked rule artifact map.
     *
     * @param ruleArtifact rule artifact
     * @param request workflow request
     * @param propertyRequirements property requirements
     * @return masked rule artifact map
     */
    public static Map<String, Object> createMaskedRuleArtifactMap(final RuleArtifact ruleArtifact, final WorkflowRequest request,
                                                                  final List<AlgorithmPropertyRequirement> propertyRequirements) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("operation_type", ruleArtifact.getOperationType());
        result.put("sql", maskSensitiveSql(ruleArtifact.getSql(), request, propertyRequirements));
        return result;
    }
    
    /**
     * Mask sensitive values inside SQL text.
     *
     * @param sql SQL text
     * @param request workflow request
     * @param propertyRequirements property requirements
     * @return masked SQL text
     */
    public static String maskSensitiveSql(final String sql, final WorkflowRequest request, final List<AlgorithmPropertyRequirement> propertyRequirements) {
        String result = sql;
        for (String each : collectSecretValues(request, propertyRequirements)) {
            if (each.isEmpty()) {
                continue;
            }
            result = result.replace(each, "******");
            result = result.replace(WorkflowSqlUtils.escapeLiteral(each), "******");
        }
        return result;
    }
    
    private static Set<String> collectSecretValues(final WorkflowRequest request, final List<AlgorithmPropertyRequirement> propertyRequirements) {
        if (null == request) {
            return Set.of();
        }
        Set<String> result = new LinkedHashSet<>();
        for (AlgorithmPropertyRequirement each : propertyRequirements) {
            if (!each.isSecret()) {
                continue;
            }
            String actualValue = WorkflowSqlUtils.trimToEmpty(getPropertyValue(request, each));
            if (!actualValue.isEmpty()) {
                result.add(actualValue);
            }
        }
        return result;
    }
    
    private static String getPropertyValue(final WorkflowRequest request, final AlgorithmPropertyRequirement propertyRequirement) {
        if ("assisted_query".equals(propertyRequirement.getAlgorithmRole())) {
            return request.getAssistedQueryAlgorithmProperties().get(propertyRequirement.getPropertyKey());
        }
        if ("like_query".equals(propertyRequirement.getAlgorithmRole())) {
            return request.getLikeQueryAlgorithmProperties().get(propertyRequirement.getPropertyKey());
        }
        return request.getPrimaryAlgorithmProperties().get(propertyRequirement.getPropertyKey());
    }
}
