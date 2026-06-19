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

package org.apache.shardingsphere.mcp.feature.sharding.completion;

import org.apache.shardingsphere.mcp.feature.sharding.ShardingFeatureDefinition;
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

class ShardingAlgorithmCompletionProviderTest {
    
    @Test
    void assertGetContextType() {
        assertThat(new ShardingAlgorithmCompletionProvider().getContextType(), is(MCPDatabaseHandlerContext.class));
    }
    
    @Test
    void assertSupportsShardingAlgorithm() {
        assertTrue(new ShardingAlgorithmCompletionProvider().supports(createRequestContext(ShardingFeatureDefinition.ALGORITHM_PLUGINS_RESOURCE_URI, "algorithm_type")));
    }
    
    @Test
    void assertSupportsKeyGeneratorAlgorithm() {
        assertTrue(new ShardingAlgorithmCompletionProvider().supports(createRequestContext(ShardingFeatureDefinition.KEY_GENERATE_ALGORITHM_PLUGINS_RESOURCE_URI, "key_generator_type")));
    }
    
    @Test
    void assertSupportsWithForeignReference() {
        assertFalse(new ShardingAlgorithmCompletionProvider().supports(createRequestContext("plan_mask_rule", "algorithm_type")));
    }
    
    @Test
    void assertCompleteShardingAlgorithm() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryWithAnyDatabase("SHOW SHARDING ALGORITHM PLUGINS")).thenReturn(List.of(
                Map.of("type", "INLINE", "description", "Inline sharding algorithm", "secret", "hidden"),
                Map.of("type", "")));
        MCPCompletionProviderResult actual = new ShardingAlgorithmCompletionProvider().complete(createHandlerContext(queryFacade),
                createRequestContext(ShardingFeatureDefinition.ALGORITHM_PLUGINS_RESOURCE_URI, "algorithm_type"));
        List<MCPCompletionCandidate> actualCandidates = new ArrayList<>(actual.getCandidates());
        assertThat(actualCandidates.size(), is(1));
        assertThat(actualCandidates.getFirst().getValue(), is("INLINE"));
        assertThat(actualCandidates.getFirst().getLabel(), is("Inline sharding algorithm"));
        assertThat(actualCandidates.getFirst().getSource(), is("sharding-algorithm"));
    }
    
    @Test
    void assertCompleteKeyGeneratorAlgorithm() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryWithAnyDatabase("SHOW KEY GENERATE ALGORITHM PLUGINS")).thenReturn(List.of(
                Map.of("type", "SNOWFLAKE", "description", "Snowflake key generator")));
        MCPCompletionProviderResult actual = new ShardingAlgorithmCompletionProvider().complete(createHandlerContext(queryFacade),
                createRequestContext(ShardingFeatureDefinition.KEY_GENERATE_ALGORITHM_PLUGINS_RESOURCE_URI, "key_generator_type"));
        List<MCPCompletionCandidate> actualCandidates = new ArrayList<>(actual.getCandidates());
        assertThat(actualCandidates.size(), is(1));
        assertThat(actualCandidates.getFirst().getValue(), is("SNOWFLAKE"));
        assertThat(actualCandidates.getFirst().getSource(), is("sharding-key-generate-algorithm"));
    }
    
    @Test
    void assertSPIRegistration() {
        assertTrue(ServiceLoader.load(MCPCompletionProvider.class).stream().anyMatch(each -> ShardingAlgorithmCompletionProvider.class.equals(each.type())));
    }
    
    private MCPDatabaseHandlerContext createHandlerContext(final MCPFeatureQueryFacade queryFacade) {
        MCPDatabaseHandlerContext result = mock(MCPDatabaseHandlerContext.class);
        when(result.getQueryFacade()).thenReturn(queryFacade);
        return result;
    }
    
    private MCPCompletionRequestContext createRequestContext(final String reference, final String argumentName) {
        return new MCPCompletionRequestContext("session-1",
                new MCPCompletionTargetDescriptor("resource", reference, List.of(argumentName), 50, Map.of()), argumentName, Map.of("database", "logic_db"));
    }
}
