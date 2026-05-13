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
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionCandidate;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionProvider;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionProviderResult;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionRequestContext;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Encrypt algorithm completion provider.
 */
public final class EncryptAlgorithmCompletionProvider implements MCPCompletionProvider<MCPDatabaseHandlerContext> {
    
    private static final Set<String> SUPPORTED_ARGUMENTS = Set.of("algorithm_type", "assisted_query_algorithm_type", "like_query_algorithm_type");
    
    private final EncryptRuleInspectionService ruleInspectionService = new EncryptRuleInspectionService();
    
    @Override
    public Class<MCPDatabaseHandlerContext> getContextType() {
        return MCPDatabaseHandlerContext.class;
    }
    
    @Override
    public boolean supports(final MCPCompletionRequestContext requestContext) {
        return SUPPORTED_ARGUMENTS.contains(requestContext.getArgumentName()) && isEncryptReference(requestContext);
    }
    
    private boolean isEncryptReference(final MCPCompletionRequestContext requestContext) {
        String reference = requestContext.getDescriptor().getReference();
        return EncryptFeatureDefinition.PLAN_TOOL_NAME.equals(reference) || EncryptFeatureDefinition.ALGORITHMS_RESOURCE_URI.equals(reference);
    }
    
    @Override
    public MCPCompletionProviderResult complete(final MCPDatabaseHandlerContext handlerContext, final MCPCompletionRequestContext requestContext) {
        return new MCPCompletionProviderResult(ruleInspectionService.queryEncryptAlgorithms(handlerContext.getQueryFacade()).stream()
                .map(this::createAlgorithmCandidate).filter(each -> !each.getValue().isEmpty()).toList());
    }
    
    private MCPCompletionCandidate createAlgorithmCandidate(final Map<String, Object> row) {
        String value = Objects.toString(row.get("type"), "").trim();
        String label = Objects.toString(row.containsKey("description") ? row.get("description") : "algorithm", "algorithm");
        return new MCPCompletionCandidate(value, label, "encrypt-algorithm");
    }
}
