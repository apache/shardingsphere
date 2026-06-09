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
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionProvider;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionRequestContext;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.descriptor.MCPCompletionTargetDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptAlgorithmCompletionProviderTest {
    
    @Test
    void assertGetContextType() {
        assertThat(new EncryptAlgorithmCompletionProvider().getContextType(), is(MCPDatabaseHandlerContext.class));
    }
    
    @Test
    void assertLoadByServiceLoader() {
        assertTrue(ServiceLoader.load(MCPCompletionProvider.class).stream().map(ServiceLoader.Provider::type).anyMatch(EncryptAlgorithmCompletionProvider.class::equals));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("supportedReferences")
    void assertSupports(final String name, final String referenceType, final String reference, final String argumentName) {
        assertTrue(new EncryptAlgorithmCompletionProvider().supports(createRequestContext(referenceType, reference, argumentName)));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("unsupportedReferences")
    void assertSupportsWithForeignReference(final String name, final String referenceType, final String reference, final String argumentName) {
        assertFalse(new EncryptAlgorithmCompletionProvider().supports(createRequestContext(referenceType, reference, argumentName)));
    }
    
    @Test
    void assertComplete() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryWithAnyDatabase("SHOW ENCRYPT ALGORITHM PLUGINS")).thenReturn(List.of(
                Map.of("type", "AES", "description", "AES encryptor", "aes-key-value", "secret-value"),
                Map.of("type", "")));
        MCPDatabaseHandlerContext handlerContext = mock(MCPDatabaseHandlerContext.class);
        when(handlerContext.getQueryFacade()).thenReturn(queryFacade);
        List<MCPCompletionCandidate> actualCandidates = new ArrayList<>(new EncryptAlgorithmCompletionProvider().complete(handlerContext,
                createRequestContext("prompt", EncryptFeatureDefinition.PLAN_PROMPT_NAME, "algorithm_type")).getCandidates());
        assertThat(actualCandidates.size(), is(1));
        assertThat(actualCandidates.getFirst().getValue(), is("AES"));
        assertThat(actualCandidates.getFirst().getLabel(), is("AES encryptor"));
        assertThat(actualCandidates.getFirst().getSource(), is("encrypt-algorithm"));
        assertNull(actualCandidates.getFirst().getUpdateTime());
        assertThat(actualCandidates.getFirst().getRankingReason(), is(""));
    }
    
    private static Collection<Arguments> supportedReferences() {
        return List.of(
                Arguments.of("prompt algorithm_type", "prompt", EncryptFeatureDefinition.PLAN_PROMPT_NAME, "algorithm_type"),
                Arguments.of("prompt assisted_query_algorithm_type", "prompt", EncryptFeatureDefinition.PLAN_PROMPT_NAME, "assisted_query_algorithm_type"),
                Arguments.of("prompt like_query_algorithm_type", "prompt", EncryptFeatureDefinition.PLAN_PROMPT_NAME, "like_query_algorithm_type"),
                Arguments.of("resource algorithm_type", "resource", EncryptFeatureDefinition.ALGORITHMS_RESOURCE_URI, "algorithm_type"),
                Arguments.of("resource assisted_query_algorithm_type", "resource", EncryptFeatureDefinition.ALGORITHMS_RESOURCE_URI, "assisted_query_algorithm_type"),
                Arguments.of("resource like_query_algorithm_type", "resource", EncryptFeatureDefinition.ALGORITHMS_RESOURCE_URI, "like_query_algorithm_type"));
    }
    
    private static Collection<Arguments> unsupportedReferences() {
        return List.of(
                Arguments.of("foreign prompt", "prompt", "plan_mask_rule", "algorithm_type"),
                Arguments.of("foreign resource", "resource", "shardingsphere://features/mask/algorithms", "algorithm_type"));
    }
    
    private MCPCompletionRequestContext createRequestContext(final String referenceType, final String reference, final String argumentName) {
        return new MCPCompletionRequestContext("session-1", new MCPCompletionTargetDescriptor(referenceType, reference, List.of(argumentName), 50, Map.of()), argumentName, Map.of());
    }
}
