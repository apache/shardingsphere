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
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionCandidate;
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionHandlerResult;
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionRequest;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionTargetDescriptor;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReadwriteSplittingLoadBalanceAlgorithmCompletionHandlerTest {
    
    @Test
    void assertGetContextType() {
        assertThat(new ReadwriteSplittingLoadBalanceAlgorithmCompletionHandler().getContextType(), is(MCPFeatureRequestContext.class));
    }
    
    @Test
    void assertSupports() {
        assertTrue(new ReadwriteSplittingLoadBalanceAlgorithmCompletionHandler().supports(createRequestContext(ReadwriteSplittingFeatureDefinition.PLAN_RULE_PROMPT_NAME)));
    }
    
    @Test
    void assertSupportsWithAlgorithmResource() {
        assertTrue(new ReadwriteSplittingLoadBalanceAlgorithmCompletionHandler().supports(createRequestContext(
                ReadwriteSplittingFeatureDefinition.LOAD_BALANCE_ALGORITHM_PLUGINS_RESOURCE_URI)));
    }
    
    @Test
    void assertSupportsWithForeignReference() {
        assertFalse(new ReadwriteSplittingLoadBalanceAlgorithmCompletionHandler().supports(createRequestContext("plan_shadow_rule")));
    }
    
    @Test
    void assertComplete() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryWithAnyDatabase("SHOW LOAD BALANCE ALGORITHM PLUGINS")).thenReturn(List.of(
                Map.of("type", "ROUND_ROBIN", "description", "Round robin load balance", "secret", "hidden"),
                Map.of("type", "")));
        MCPFeatureRequestContext handlerContext = mock(MCPFeatureRequestContext.class);
        when(handlerContext.getQueryFacade()).thenReturn(queryFacade);
        MCPCompletionHandlerResult actual = new ReadwriteSplittingLoadBalanceAlgorithmCompletionHandler().complete(handlerContext,
                createRequestContext(ReadwriteSplittingFeatureDefinition.PLAN_RULE_PROMPT_NAME));
        Collection<MCPCompletionCandidate> actualCandidates = actual.getCandidates();
        assertThat(actualCandidates.size(), is(1));
        MCPCompletionCandidate actualCandidate = actualCandidates.iterator().next();
        assertThat(actualCandidate.getValue(), is("ROUND_ROBIN"));
        assertThat(actualCandidate.getLabel(), is("Round robin load balance"));
        assertThat(actualCandidate.getSource(), is("readwrite-splitting-load-balance-algorithm"));
    }
    
    private MCPCompletionRequest createRequestContext(final String reference) {
        String referenceType = reference.startsWith("shardingsphere://") ? "resource" : "prompt";
        return new MCPCompletionRequest(
                new MCPCompletionTargetDescriptor(referenceType, reference, List.of(ReadwriteSplittingFeatureDefinition.LOAD_BALANCER_TYPE_FIELD), 50, Map.of()),
                ReadwriteSplittingFeatureDefinition.LOAD_BALANCER_TYPE_FIELD, Map.of("database", "logic_db"));
    }
}
