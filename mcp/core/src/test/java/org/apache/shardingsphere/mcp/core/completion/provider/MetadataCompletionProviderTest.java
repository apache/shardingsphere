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

package org.apache.shardingsphere.mcp.core.completion.provider;

import org.apache.shardingsphere.mcp.support.completion.MCPCompletionCandidate;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionProviderResult;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionRequestContext;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.descriptor.MCPCompletionTargetDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MetadataCompletionProviderTest {
    
    @Test
    void assertGetContextType() {
        assertThat(new MetadataCompletionProvider().getContextType(), is(MCPDatabaseHandlerContext.class));
    }
    
    @Test
    void assertSupports() {
        assertTrue(new MetadataCompletionProvider().supports(createRequestContext("database", Map.of())));
    }
    
    @Test
    void assertCompleteWithEmptySchemaDefaulted() {
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryDatabase("logic_db")).thenReturn(Optional.of(createDatabaseMetadata()));
        when(metadataQueryFacade.queryTables("logic_db", "public")).thenReturn(List.of(createTableMetadata()));
        MCPDatabaseHandlerContext handlerContext = mock(MCPDatabaseHandlerContext.class);
        when(handlerContext.getMetadataQueryFacade()).thenReturn(metadataQueryFacade);
        MCPCompletionProviderResult actual = new MetadataCompletionProvider().complete(handlerContext, createRequestContext("table", Map.of("database", "logic_db", "schema", "")));
        List<MCPCompletionCandidate> actualCandidates = List.copyOf(actual.getCandidates());
        assertThat(actualCandidates.size(), is(1));
        assertThat(actualCandidates.get(0).getValue(), is("t_order"));
        assertThat(actual.getInferredContextArguments(), is(Map.of("schema", "public")));
    }
    
    private MCPCompletionRequestContext createRequestContext(final String argumentName, final Map<String, String> contextArguments) {
        return new MCPCompletionRequestContext("session-1", new MCPCompletionTargetDescriptor("prompt", "inspect_metadata", List.of(argumentName), 50, Map.of()), argumentName,
                contextArguments);
    }
    
    private MCPDatabaseMetadata createDatabaseMetadata() {
        return new MCPDatabaseMetadata("logic_db", "MySQL", "8.0", List.of(new MCPSchemaMetadata("logic_db", "public", List.of(createTableMetadata()), List.of())));
    }
    
    private MCPTableMetadata createTableMetadata() {
        return new MCPTableMetadata("logic_db", "public", "t_order", List.of(), List.of());
    }
}
