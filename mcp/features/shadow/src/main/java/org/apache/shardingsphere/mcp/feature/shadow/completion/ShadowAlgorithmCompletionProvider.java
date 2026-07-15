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

package org.apache.shardingsphere.mcp.feature.shadow.completion;

import org.apache.shardingsphere.mcp.feature.shadow.ShadowFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.shadow.tool.service.ShadowInspectionService;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionCandidate;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionProvider;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionProviderResult;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionRequest;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseRequestContext;

import java.util.Map;
import java.util.Objects;

/**
 * Shadow algorithm completion provider.
 */
public final class ShadowAlgorithmCompletionProvider implements MCPCompletionProvider<MCPDatabaseRequestContext> {
    
    private final ShadowInspectionService inspectionService = new ShadowInspectionService();
    
    @Override
    public Class<MCPDatabaseRequestContext> getContextType() {
        return MCPDatabaseRequestContext.class;
    }
    
    @Override
    public boolean supports(final MCPCompletionRequest request) {
        return ShadowFeatureDefinition.ALGORITHM_TYPE_FIELD.equals(request.getArgumentName()) && isShadowReference(request);
    }
    
    private boolean isShadowReference(final MCPCompletionRequest request) {
        String reference = request.getDescriptor().getReference();
        return ShadowFeatureDefinition.PLAN_RULE_PROMPT_NAME.equals(reference)
                || ShadowFeatureDefinition.PLAN_DEFAULT_ALGORITHM_PROMPT_NAME.equals(reference)
                || ShadowFeatureDefinition.ALGORITHM_PLUGINS_RESOURCE_URI.equals(reference);
    }
    
    @Override
    public MCPCompletionProviderResult complete(final MCPDatabaseRequestContext handlerContext, final MCPCompletionRequest request) {
        return new MCPCompletionProviderResult(inspectionService.queryAlgorithmPlugins(handlerContext.getQueryFacade()).stream()
                .map(this::createAlgorithmCandidate).filter(each -> !each.getValue().isEmpty()).toList());
    }
    
    private MCPCompletionCandidate createAlgorithmCandidate(final Map<String, Object> row) {
        String value = Objects.toString(row.getOrDefault("type", row.getOrDefault("name", "")), "").trim();
        String label = Objects.toString(row.getOrDefault("description", "shadow algorithm"), "shadow algorithm");
        return new MCPCompletionCandidate(value, label, "shadow-algorithm");
    }
}
