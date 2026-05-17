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
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSequenceMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.descriptor.MCPCompletionTargetDescriptor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void assertSupportsWithUnknownArgument() {
        assertFalse(new MetadataCompletionProvider().supports(createRequestContext("foo_value", Map.of())));
    }
    
    @Test
    void assertCompleteDatabase() {
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryDatabases()).thenReturn(List.of(createDatabaseMetadata()));
        MCPCompletionProviderResult actual = new MetadataCompletionProvider().complete(createHandlerContext(metadataQueryFacade), createRequestContext("database", Map.of()));
        assertCandidate(actual, "logic_db");
        assertThat(actual.getGuidanceResourceUri(), is("shardingsphere://databases"));
        assertThat(actual.getMissingContextArguments(), is(List.of()));
    }
    
    @Test
    void assertCompleteSchema() {
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.querySchemas("logic_db")).thenReturn(List.of(createSchemaMetadata()));
        MCPCompletionProviderResult actual = new MetadataCompletionProvider().complete(createHandlerContext(metadataQueryFacade), createRequestContext("schema", Map.of("database", "logic_db")));
        assertCandidate(actual, "public");
        assertThat(actual.getGuidanceResourceUri(), is("shardingsphere://databases/logic_db/schemas"));
    }
    
    @Test
    void assertCompleteWithEmptySchemaDefaulted() {
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryDatabase("logic_db")).thenReturn(Optional.of(createDatabaseMetadata()));
        when(metadataQueryFacade.queryTables("logic_db", "public")).thenReturn(List.of(createTableMetadata()));
        MCPCompletionProviderResult actual = new MetadataCompletionProvider().complete(createHandlerContext(metadataQueryFacade),
                createRequestContext("table", Map.of("database", "logic_db", "schema", "")));
        assertCandidate(actual, "t_order");
        assertThat(actual.getInferredContextArguments(), is(Map.of("schema", "public")));
        assertThat(actual.getMissingContextArguments(), is(List.of()));
    }
    
    @Test
    void assertCompleteTableWithMissingContext() {
        MCPCompletionProviderResult actual = new MetadataCompletionProvider().complete(createHandlerContext(mock(MCPMetadataQueryFacade.class)), createRequestContext("table", Map.of()));
        assertThat(actual.getCandidates(), is(List.of()));
        assertThat(actual.getMissingContextArguments(), is(List.of("database", "schema")));
        assertThat(actual.getGuidanceResourceUri(), is("shardingsphere://databases"));
    }
    
    @Test
    void assertCompleteColumn() {
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryTableColumns("logic_db", "public", "t_order")).thenReturn(List.of(new MCPColumnMetadata("logic_db", "public", "t_order", "", "order_id")));
        MCPCompletionProviderResult actual = new MetadataCompletionProvider().complete(createHandlerContext(metadataQueryFacade),
                createRequestContext("column", Map.of("database", "logic_db", "schema", "public", "table", "t_order")));
        assertCandidate(actual, "order_id");
        assertThat(actual.getGuidanceResourceUri(), is("shardingsphere://databases/logic_db/schemas/public/tables/t_order/columns"));
    }
    
    @Test
    void assertCompleteIndex() {
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryIndexes("logic_db", "public", "t_order")).thenReturn(List.of(new MCPIndexMetadata("logic_db", "public", "t_order", "idx_order_id")));
        MCPCompletionProviderResult actual = new MetadataCompletionProvider().complete(createHandlerContext(metadataQueryFacade),
                createRequestContext("index", Map.of("database", "logic_db", "schema", "public", "table", "t_order")));
        assertCandidate(actual, "idx_order_id");
        assertThat(actual.getGuidanceResourceUri(), is("shardingsphere://databases/logic_db/schemas/public/tables/t_order/indexes"));
    }
    
    @Test
    void assertCompleteSequence() {
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.querySequences("logic_db", "public")).thenReturn(List.of(new MCPSequenceMetadata("logic_db", "public", "order_seq")));
        MCPCompletionProviderResult actual = new MetadataCompletionProvider().complete(createHandlerContext(metadataQueryFacade),
                createRequestContext("sequence", Map.of("database", "logic_db", "schema", "public")));
        assertCandidate(actual, "order_seq");
        assertThat(actual.getGuidanceResourceUri(), is("shardingsphere://databases/logic_db/schemas/public/sequences"));
    }
    
    private MCPCompletionRequestContext createRequestContext(final String argumentName, final Map<String, String> contextArguments) {
        return new MCPCompletionRequestContext("session-1", new MCPCompletionTargetDescriptor("prompt", "inspect_metadata", List.of(argumentName), 50, Map.of()), argumentName,
                contextArguments);
    }
    
    private MCPDatabaseHandlerContext createHandlerContext(final MCPMetadataQueryFacade metadataQueryFacade) {
        MCPDatabaseHandlerContext result = mock(MCPDatabaseHandlerContext.class);
        when(result.getMetadataQueryFacade()).thenReturn(metadataQueryFacade);
        return result;
    }
    
    private MCPDatabaseMetadata createDatabaseMetadata() {
        return new MCPDatabaseMetadata("logic_db", "MySQL", "8.0", List.of(createSchemaMetadata()));
    }
    
    private MCPSchemaMetadata createSchemaMetadata() {
        return new MCPSchemaMetadata("logic_db", "public", List.of(createTableMetadata()), List.of(), List.of());
    }
    
    private MCPTableMetadata createTableMetadata() {
        return new MCPTableMetadata("logic_db", "public", "t_order", List.of(), List.of());
    }
    
    private void assertCandidate(final MCPCompletionProviderResult actual, final String expectedValue) {
        List<MCPCompletionCandidate> actualCandidates = new ArrayList<>(actual.getCandidates());
        assertThat(actualCandidates.size(), is(1));
        assertThat(actualCandidates.get(0).getValue(), is(expectedValue));
    }
}
