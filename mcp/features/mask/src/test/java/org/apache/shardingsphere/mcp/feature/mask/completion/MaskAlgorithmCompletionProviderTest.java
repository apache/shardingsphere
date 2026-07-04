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

package org.apache.shardingsphere.mcp.feature.mask.completion;

import org.apache.shardingsphere.mcp.feature.mask.MaskFeatureDefinition;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionCandidate;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionProviderResult;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionRequestContext;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.descriptor.MCPCompletionTargetDescriptor;
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

class MaskAlgorithmCompletionProviderTest {
    
    @Test
    void assertGetContextType() {
        assertThat(new MaskAlgorithmCompletionProvider().getContextType(), is(MCPDatabaseHandlerContext.class));
    }
    
    @Test
    void assertSupports() {
        assertTrue(new MaskAlgorithmCompletionProvider().supports(createRequestContext(MaskFeatureDefinition.PLAN_PROMPT_NAME)));
    }
    
    @Test
    void assertSupportsWithAlgorithmResource() {
        assertTrue(new MaskAlgorithmCompletionProvider().supports(createRequestContext(MaskFeatureDefinition.ALGORITHMS_RESOURCE_URI)));
    }
    
    @Test
    void assertSupportsWithForeignReference() {
        assertFalse(new MaskAlgorithmCompletionProvider().supports(createRequestContext("plan_encrypt_rule")));
    }
    
    @Test
    void assertComplete() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryWithAnyDatabase("SHOW MASK ALGORITHM PLUGINS")).thenReturn(List.of(
                Map.of("type", "MASK_FROM_X_TO_Y", "description", "Range mask"),
                Map.of("type", "")));
        MCPDatabaseHandlerContext handlerContext = mock(MCPDatabaseHandlerContext.class);
        when(handlerContext.getQueryFacade()).thenReturn(queryFacade);
        MCPCompletionProviderResult actual = new MaskAlgorithmCompletionProvider().complete(handlerContext,
                createRequestContext(MaskFeatureDefinition.PLAN_PROMPT_NAME));
        Collection<MCPCompletionCandidate> actualCandidates = actual.getCandidates();
        assertThat(actualCandidates.size(), is(1));
        MCPCompletionCandidate actualCandidate = actualCandidates.iterator().next();
        assertThat(actualCandidate.getValue(), is("MASK_FROM_X_TO_Y"));
        assertThat(actualCandidate.getLabel(), is("Range mask"));
    }
    
    private MCPCompletionRequestContext createRequestContext(final String reference) {
        return new MCPCompletionRequestContext("session-1", new MCPCompletionTargetDescriptor("prompt", reference, List.of("algorithm_type"), 50, Map.of()), "algorithm_type", Map.of());
    }
}
