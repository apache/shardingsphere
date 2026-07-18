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

package org.apache.shardingsphere.mcp.feature.encrypt.completion;

import org.apache.shardingsphere.mcp.feature.encrypt.EncryptFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptRuleInspectionService;
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionCandidate;
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionHandler;
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionHandlerResult;
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionRequest;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Encrypt algorithm completion handler.
 */
public final class EncryptAlgorithmCompletionHandler implements MCPCompletionHandler<MCPFeatureRequestContext> {
    
    private static final Set<String> SUPPORTED_ARGUMENTS = Set.of("algorithm_type", "assisted_query_algorithm_type", "like_query_algorithm_type");
    
    private final EncryptRuleInspectionService ruleInspectionService = new EncryptRuleInspectionService();
    
    @Override
    public Class<MCPFeatureRequestContext> getContextType() {
        return MCPFeatureRequestContext.class;
    }
    
    @Override
    public boolean supports(final MCPCompletionRequest request) {
        return SUPPORTED_ARGUMENTS.contains(request.getArgumentName()) && isEncryptReference(request);
    }
    
    private boolean isEncryptReference(final MCPCompletionRequest request) {
        String reference = request.getDescriptor().getReference();
        return EncryptFeatureDefinition.PLAN_PROMPT_NAME.equals(reference) || EncryptFeatureDefinition.ALGORITHMS_RESOURCE_URI.equals(reference);
    }
    
    @Override
    public MCPCompletionHandlerResult complete(final MCPFeatureRequestContext handlerContext, final MCPCompletionRequest request) {
        return new MCPCompletionHandlerResult(ruleInspectionService.queryEncryptAlgorithms(handlerContext.getQueryFacade()).stream()
                .map(this::createAlgorithmCandidate).filter(each -> !each.getValue().isEmpty()).toList());
    }
    
    private MCPCompletionCandidate createAlgorithmCandidate(final Map<String, Object> row) {
        String value = Objects.toString(row.get("type"), "").trim();
        String label = Objects.toString(row.getOrDefault("description", "algorithm"), "algorithm");
        return new MCPCompletionCandidate(value, label, "encrypt-algorithm");
    }
}
