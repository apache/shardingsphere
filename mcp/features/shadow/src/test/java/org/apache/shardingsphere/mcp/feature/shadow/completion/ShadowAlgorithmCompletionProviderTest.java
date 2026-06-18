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
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionCandidate;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionProvider;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionProviderResult;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionRequestContext;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.descriptor.MCPCompletionTargetDescriptor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShadowAlgorithmCompletionProviderTest {
    
    @Test
    void assertGetContextType() {
        assertThat(new ShadowAlgorithmCompletionProvider().getContextType(), is(MCPDatabaseHandlerContext.class));
    }
    
    @Test
    void assertSupports() {
        assertTrue(new ShadowAlgorithmCompletionProvider().supports(createRequestContext(ShadowFeatureDefinition.PLAN_RULE_PROMPT_NAME)));
    }
    
    @Test
    void assertSupportsWithDefaultAlgorithmPrompt() {
        assertTrue(new ShadowAlgorithmCompletionProvider().supports(createRequestContext(ShadowFeatureDefinition.PLAN_DEFAULT_ALGORITHM_PROMPT_NAME)));
    }
    
    @Test
    void assertSupportsWithAlgorithmResource() {
        assertTrue(new ShadowAlgorithmCompletionProvider().supports(createRequestContext(ShadowFeatureDefinition.ALGORITHM_PLUGINS_RESOURCE_URI)));
    }
    
    @Test
    void assertSupportsWithForeignReference() {
        assertFalse(new ShadowAlgorithmCompletionProvider().supports(createRequestContext("plan_mask_rule")));
    }
    
    @Test
    void assertComplete() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryWithAnyDatabase("SHOW SHADOW ALGORITHM PLUGINS")).thenReturn(List.of(
                Map.of("type", "VALUE_MATCH", "description", "Value match shadow algorithm", "password", "hidden"),
                Map.of("type", "")));
        MCPDatabaseHandlerContext handlerContext = mock(MCPDatabaseHandlerContext.class);
        when(handlerContext.getQueryFacade()).thenReturn(queryFacade);
        MCPCompletionProviderResult actual = new ShadowAlgorithmCompletionProvider().complete(handlerContext,
                createRequestContext(ShadowFeatureDefinition.PLAN_RULE_PROMPT_NAME));
        List<MCPCompletionCandidate> actualCandidates = new ArrayList<>(actual.getCandidates());
        assertThat(actualCandidates.size(), is(1));
        assertThat(actualCandidates.getFirst().getValue(), is("VALUE_MATCH"));
        assertThat(actualCandidates.getFirst().getLabel(), is("Value match shadow algorithm"));
        assertThat(actualCandidates.getFirst().getSource(), is("shadow-algorithm"));
    }
    
    @Test
    void assertSPIRegistration() {
        assertTrue(ServiceLoader.load(MCPCompletionProvider.class).stream().anyMatch(each -> ShadowAlgorithmCompletionProvider.class.equals(each.type())));
    }
    
    private MCPCompletionRequestContext createRequestContext(final String reference) {
        String referenceType = reference.startsWith("shardingsphere://") ? "resource" : "prompt";
        return new MCPCompletionRequestContext("session-1",
                new MCPCompletionTargetDescriptor(referenceType, reference, List.of(ShadowFeatureDefinition.ALGORITHM_TYPE_FIELD), 50, Map.of()),
                ShadowFeatureDefinition.ALGORITHM_TYPE_FIELD, Map.of("database", "logic_db"));
    }
}
