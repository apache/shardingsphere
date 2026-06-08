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

package org.apache.shardingsphere.mcp.feature.readwritesplitting.completion;

import org.apache.shardingsphere.mcp.feature.readwritesplitting.ReadwriteSplittingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.service.ReadwriteSplittingInspectionService;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionCandidate;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionProvider;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionProviderResult;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionRequestContext;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;

import java.util.Map;
import java.util.Objects;

/**
 * Readwrite-splitting load-balance algorithm completion provider.
 */
public final class ReadwriteSplittingLoadBalanceAlgorithmCompletionProvider implements MCPCompletionProvider<MCPDatabaseHandlerContext> {
    
    private final ReadwriteSplittingInspectionService inspectionService = new ReadwriteSplittingInspectionService();
    
    @Override
    public Class<MCPDatabaseHandlerContext> getContextType() {
        return MCPDatabaseHandlerContext.class;
    }
    
    @Override
    public boolean supports(final MCPCompletionRequestContext requestContext) {
        return ReadwriteSplittingFeatureDefinition.LOAD_BALANCER_TYPE_FIELD.equals(requestContext.getArgumentName()) && isReadwriteSplittingReference(requestContext);
    }
    
    private boolean isReadwriteSplittingReference(final MCPCompletionRequestContext requestContext) {
        String reference = requestContext.getDescriptor().getReference();
        return ReadwriteSplittingFeatureDefinition.PLAN_RULE_PROMPT_NAME.equals(reference)
                || ReadwriteSplittingFeatureDefinition.LOAD_BALANCE_ALGORITHM_PLUGINS_RESOURCE_URI.equals(reference);
    }
    
    @Override
    public MCPCompletionProviderResult complete(final MCPDatabaseHandlerContext handlerContext, final MCPCompletionRequestContext requestContext) {
        return new MCPCompletionProviderResult(inspectionService.queryLoadBalanceAlgorithmPlugins(handlerContext.getQueryFacade()).stream()
                .map(this::createAlgorithmCandidate).filter(each -> !each.getValue().isEmpty()).toList());
    }
    
    private MCPCompletionCandidate createAlgorithmCandidate(final Map<String, Object> row) {
        String value = Objects.toString(row.getOrDefault("type", row.getOrDefault("name", "")), "").trim();
        String label = Objects.toString(row.getOrDefault("description", "load-balance algorithm"), "load-balance algorithm");
        return new MCPCompletionCandidate(value, label, "readwrite-splitting-load-balance-algorithm");
    }
}
