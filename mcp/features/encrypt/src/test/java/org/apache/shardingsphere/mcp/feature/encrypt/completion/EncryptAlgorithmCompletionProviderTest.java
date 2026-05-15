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
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionCandidate;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionProviderResult;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionRequestContext;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.descriptor.MCPCompletionTargetDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptAlgorithmCompletionProviderTest {
    
    @Test
    void assertGetContextType() {
        assertThat(new EncryptAlgorithmCompletionProvider().getContextType(), is(MCPDatabaseHandlerContext.class));
    }
    
    @Test
    void assertSupports() {
        assertTrue(new EncryptAlgorithmCompletionProvider().supports(createRequestContext(EncryptFeatureDefinition.PLAN_PROMPT_NAME, "algorithm_type")));
    }
    
    @Test
    void assertSupportsWithAlgorithmResource() {
        assertTrue(new EncryptAlgorithmCompletionProvider().supports(createRequestContext(EncryptFeatureDefinition.ALGORITHMS_RESOURCE_URI, "like_query_algorithm_type")));
    }
    
    @Test
    void assertSupportsWithForeignReference() {
        assertFalse(new EncryptAlgorithmCompletionProvider().supports(createRequestContext("plan_mask_rule", "algorithm_type")));
    }
    
    @Test
    void assertComplete() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryWithAnyDatabase("SHOW ENCRYPT ALGORITHM PLUGINS")).thenReturn(List.of(
                Map.of("type", "AES", "description", "AES encryptor"),
                Map.of("type", "")));
        MCPDatabaseHandlerContext handlerContext = mock(MCPDatabaseHandlerContext.class);
        when(handlerContext.getQueryFacade()).thenReturn(queryFacade);
        MCPCompletionProviderResult actual = new EncryptAlgorithmCompletionProvider().complete(handlerContext,
                createRequestContext(EncryptFeatureDefinition.PLAN_PROMPT_NAME, "algorithm_type"));
        List<MCPCompletionCandidate> actualCandidates = List.copyOf(actual.getCandidates());
        assertThat(actualCandidates.size(), is(1));
        assertThat(actualCandidates.get(0).getValue(), is("AES"));
        assertThat(actualCandidates.get(0).getLabel(), is("AES encryptor"));
    }
    
    private MCPCompletionRequestContext createRequestContext(final String reference, final String argumentName) {
        return new MCPCompletionRequestContext("session-1", new MCPCompletionTargetDescriptor("prompt", reference, List.of(argumentName), 50, Map.of()), argumentName, Map.of());
    }
}
