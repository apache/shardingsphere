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
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionCandidate;
import org.apache.shardingsphere.mcp.spi.MCPCompletionProvider;
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionProviderResult;
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionRequest;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Shadow algorithm completion provider.
 */
public final class ShadowAlgorithmCompletionProvider implements MCPCompletionProvider<MCPFeatureRequestContext> {
    
    private final ShadowInspectionService inspectionService = new ShadowInspectionService();
    
    @Override
    public Class<MCPFeatureRequestContext> getContextType() {
        return MCPFeatureRequestContext.class;
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
    public MCPCompletionProviderResult complete(final MCPFeatureRequestContext handlerContext, final MCPCompletionRequest request) {
        Stream<MCPCompletionCandidate> candidates = inspectionService.queryAlgorithmPlugins(handlerContext.getQueryFacade()).stream()
                .map(this::createAlgorithmCandidate).filter(each -> !each.getValue().isEmpty());
        if (ShadowFeatureDefinition.PLAN_DEFAULT_ALGORITHM_PROMPT_NAME.equals(request.getDescriptor().getReference())) {
            candidates = candidates.filter(each -> ShadowFeatureDefinition.DEFAULT_ALGORITHM_TYPE.equalsIgnoreCase(each.getValue()));
        }
        return new MCPCompletionProviderResult(candidates.toList());
    }
    
    private MCPCompletionCandidate createAlgorithmCandidate(final Map<String, Object> row) {
        String value = Objects.toString(row.getOrDefault("type", row.getOrDefault("name", "")), "").trim();
        String label = Objects.toString(row.getOrDefault("description", "shadow algorithm"), "shadow algorithm");
        return new MCPCompletionCandidate(value, label, "shadow-algorithm");
    }
}
