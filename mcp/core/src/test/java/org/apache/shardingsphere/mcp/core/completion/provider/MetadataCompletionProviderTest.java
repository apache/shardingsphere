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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.TableType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSequence;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionCandidate;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionProviderResult;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionRequestContext;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureCapabilityFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.descriptor.MCPCompletionTargetDescriptor;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    void assertSupportsStorageUnit() {
        assertTrue(new MetadataCompletionProvider().supports(createRequestContext("storageUnit", Map.of())));
    }
    
    @Test
    void assertSupportsStorageUnitAliases() {
        MetadataCompletionProvider provider = new MetadataCompletionProvider();
        assertTrue(provider.supports(createRequestContext("storage_unit", Map.of())));
        assertTrue(provider.supports(createRequestContext("write_storage_unit", Map.of())));
        assertTrue(provider.supports(createRequestContext("source_storage_unit", Map.of())));
        assertTrue(provider.supports(createRequestContext("shadow_storage_unit", Map.of())));
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
        assertThat(actual.getNearestResourceUri(), is("shardingsphere://databases"));
        assertThat(actual.getMissingContextArguments(), is(List.of()));
    }
    
    @Test
    void assertCompleteSchema() {
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.querySchemas("logic_db")).thenReturn(List.of(createSchemaMetadata()));
        MCPCompletionProviderResult actual = new MetadataCompletionProvider().complete(createHandlerContext(metadataQueryFacade), createRequestContext("schema", Map.of("database", "logic_db")));
        assertCandidate(actual, "public");
        assertThat(actual.getNearestResourceUri(), is("shardingsphere://databases/logic_db/schemas"));
    }
    
    @Test
    void assertCompleteWithEmptySchemaDefaulted() {
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.querySchemas("logic_db")).thenReturn(List.of(createSchemaMetadata()));
        when(metadataQueryFacade.queryTables("logic_db", "public")).thenReturn(List.of(createTableMetadata()));
        MCPCompletionProviderResult actual = new MetadataCompletionProvider().complete(createHandlerContext(metadataQueryFacade),
                createRequestContext("table", Map.of("database", "logic_db", "schema", "")));
        assertCandidate(actual, "t_order");
        assertThat(actual.getInferredContextArguments(), is(Map.of("schema", "public")));
        assertThat(actual.getMissingContextArguments(), is(List.of()));
    }
    
    @Test
    void assertCompleteWithSingleDatabaseDefaulted() {
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.querySchemas("logic_db")).thenReturn(List.of(createSchemaMetadata()));
        when(metadataQueryFacade.queryTables("logic_db", "public")).thenReturn(List.of(createTableMetadata()));
        MCPCompletionProviderResult actual = new MetadataCompletionProvider().complete(createHandlerContext(metadataQueryFacade, List.of(createDatabaseProfile("logic_db"))),
                createRequestContext("table", Map.of()));
        assertCandidate(actual, "t_order");
        assertThat(actual.getInferredContextArguments(), is(Map.of("database", "logic_db", "schema", "public")));
        assertThat(actual.getMissingContextArguments(), is(List.of()));
        verify(metadataQueryFacade, never()).queryDatabases();
    }
    
    @Test
    void assertCompleteWithMultipleDatabasesKeepsMissingContext() {
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        MCPCompletionProviderResult actual = new MetadataCompletionProvider().complete(createHandlerContext(metadataQueryFacade,
                List.of(createDatabaseProfile("logic_db"), createDatabaseProfile("warehouse"))), createRequestContext("table", Map.of()));
        assertThat(actual.getCandidates(), is(List.of()));
        assertThat(actual.getInferredContextArguments(), is(Map.of()));
        assertThat(actual.getMissingContextArguments(), is(List.of("database", "schema")));
        verify(metadataQueryFacade, never()).queryDatabase(anyString());
        verify(metadataQueryFacade, never()).queryDatabases();
    }
    
    @Test
    void assertCompletePreservesProvidedDatabase() {
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.querySchemas("provided_db")).thenReturn(List.of(createSchemaMetadata()));
        MCPCompletionProviderResult actual = new MetadataCompletionProvider().complete(createHandlerContext(metadataQueryFacade, List.of(createDatabaseProfile("logic_db"))),
                createRequestContext("schema", Map.of("database", "provided_db")));
        assertCandidate(actual, "public");
        assertThat(actual.getInferredContextArguments(), is(Map.of()));
    }
    
    @Test
    void assertCompleteTableWithMissingContext() {
        MCPCompletionProviderResult actual = new MetadataCompletionProvider().complete(createHandlerContext(mock(MCPMetadataQueryFacade.class)), createRequestContext("table", Map.of()));
        assertThat(actual.getCandidates(), is(List.of()));
        assertThat(actual.getMissingContextArguments(), is(List.of("database", "schema")));
        assertThat(actual.getNearestResourceUri(), is("shardingsphere://databases"));
    }
    
    @Test
    void assertCompleteColumn() {
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryTableColumns("logic_db", "public", "t_order"))
                .thenReturn(List.of(new ShardingSphereColumn("order_id", java.sql.Types.OTHER, false, false, false, true, false, true)));
        MCPCompletionProviderResult actual = new MetadataCompletionProvider().complete(createHandlerContext(metadataQueryFacade),
                createRequestContext("column", Map.of("database", "logic_db", "schema", "public", "table", "t_order")));
        assertCandidate(actual, "order_id");
        assertThat(actual.getNearestResourceUri(), is("shardingsphere://databases/logic_db/schemas/public/tables/t_order/columns"));
    }
    
    @Test
    void assertCompleteIndex() {
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryIndexes("logic_db", "public", "t_order")).thenReturn(List.of(new ShardingSphereIndex("idx_order_id", List.of(), false)));
        MCPCompletionProviderResult actual = new MetadataCompletionProvider().complete(createHandlerContext(metadataQueryFacade),
                createRequestContext("index", Map.of("database", "logic_db", "schema", "public", "table", "t_order")));
        assertCandidate(actual, "idx_order_id");
        assertThat(actual.getNearestResourceUri(), is("shardingsphere://databases/logic_db/schemas/public/tables/t_order/indexes"));
    }
    
    @Test
    void assertCompleteSequence() {
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.querySequences("logic_db", "public")).thenReturn(List.of(new ShardingSphereSequence("order_seq")));
        MCPCompletionProviderResult actual = new MetadataCompletionProvider().complete(createHandlerContext(metadataQueryFacade),
                createRequestContext("sequence", Map.of("database", "logic_db", "schema", "public")));
        assertCandidate(actual, "order_seq");
        assertThat(actual.getNearestResourceUri(), is("shardingsphere://databases/logic_db/schemas/public/sequences"));
    }
    
    @Test
    void assertCompleteStorageUnit() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("logic_db", "", "SHOW STORAGE UNITS FROM logic_db")).thenReturn(List.of(Map.of("name", "write_ds")));
        MCPCompletionProviderResult actual = new MetadataCompletionProvider().complete(createHandlerContext(mock(MCPMetadataQueryFacade.class), queryFacade),
                createRequestContext("storageUnit", Map.of("database", "logic_db")));
        assertCandidate(actual, "write_ds");
        assertThat(actual.getMissingContextArguments(), is(List.of()));
        assertThat(actual.getNearestResourceUri(), is("shardingsphere://databases/logic_db/storage-units"));
    }
    
    @Test
    void assertCompleteStorageUnitAlias() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("logic_db", "", "SHOW STORAGE UNITS FROM logic_db")).thenReturn(List.of(Map.of("name", "write_ds")));
        MCPCompletionProviderResult actual = new MetadataCompletionProvider().complete(createHandlerContext(mock(MCPMetadataQueryFacade.class), queryFacade),
                createRequestContext("write_storage_unit", Map.of("database", "logic_db")));
        assertCandidate(actual, "write_ds");
        assertThat(actual.getMissingContextArguments(), is(List.of()));
        assertThat(actual.getNearestResourceUri(), is("shardingsphere://databases/logic_db/storage-units"));
    }
    
    @Test
    void assertCompleteStorageUnitWithSingleDatabaseDefaulted() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("logic_db", "", "SHOW STORAGE UNITS FROM logic_db")).thenReturn(List.of(Map.of("name", "write_ds")));
        MCPCompletionProviderResult actual = new MetadataCompletionProvider().complete(
                createHandlerContext(mock(MCPMetadataQueryFacade.class), queryFacade, List.of(createDatabaseProfile("logic_db"))), createRequestContext("storageUnit", Map.of()));
        assertCandidate(actual, "write_ds");
        assertThat(actual.getInferredContextArguments(), is(Map.of("database", "logic_db")));
        assertThat(actual.getMissingContextArguments(), is(List.of()));
        assertThat(actual.getNearestResourceUri(), is("shardingsphere://databases/logic_db/storage-units"));
    }
    
    @Test
    void assertCompleteStorageUnitWithMissingContext() {
        MCPCompletionProviderResult actual = new MetadataCompletionProvider().complete(createHandlerContext(mock(MCPMetadataQueryFacade.class),
                List.of(createDatabaseProfile("logic_db"), createDatabaseProfile("warehouse"))), createRequestContext("storageUnit", Map.of()));
        assertThat(actual.getCandidates(), is(List.of()));
        assertThat(actual.getMissingContextArguments(), is(List.of("database")));
        assertThat(actual.getNearestResourceUri(), is("shardingsphere://databases"));
    }
    
    private MCPCompletionRequestContext createRequestContext(final String argumentName, final Map<String, String> contextArguments) {
        return new MCPCompletionRequestContext("session-1", new MCPCompletionTargetDescriptor("prompt", "inspect_metadata", List.of(argumentName), 50, Map.of()), argumentName,
                contextArguments);
    }
    
    private MCPDatabaseHandlerContext createHandlerContext(final MCPMetadataQueryFacade metadataQueryFacade) {
        return createHandlerContext(metadataQueryFacade, List.of());
    }
    
    private MCPDatabaseHandlerContext createHandlerContext(final MCPMetadataQueryFacade metadataQueryFacade, final List<RuntimeDatabaseProfile> databaseProfiles) {
        MCPDatabaseHandlerContext result = mock(MCPDatabaseHandlerContext.class);
        MCPFeatureCapabilityFacade capabilityFacade = mock(MCPFeatureCapabilityFacade.class);
        when(capabilityFacade.getDatabaseProfiles()).thenReturn(databaseProfiles);
        when(result.getMetadataQueryFacade()).thenReturn(metadataQueryFacade);
        when(result.getCapabilityFacade()).thenReturn(capabilityFacade);
        return result;
    }
    
    private MCPDatabaseHandlerContext createHandlerContext(final MCPMetadataQueryFacade metadataQueryFacade, final MCPFeatureQueryFacade queryFacade) {
        MCPDatabaseHandlerContext result = createHandlerContext(metadataQueryFacade, List.of());
        when(result.getQueryFacade()).thenReturn(queryFacade);
        return result;
    }
    
    private MCPDatabaseHandlerContext createHandlerContext(final MCPMetadataQueryFacade metadataQueryFacade, final MCPFeatureQueryFacade queryFacade,
                                                           final List<RuntimeDatabaseProfile> databaseProfiles) {
        MCPDatabaseHandlerContext result = createHandlerContext(metadataQueryFacade, databaseProfiles);
        when(result.getQueryFacade()).thenReturn(queryFacade);
        return result;
    }
    
    private RuntimeDatabaseProfile createDatabaseProfile(final String database) {
        return new RuntimeDatabaseProfile(database, "FixtureDB", "1.0", true, true);
    }
    
    private RuntimeDatabaseProfile createDatabaseMetadata() {
        return new RuntimeDatabaseProfile("logic_db", "FixtureDB", "1.0", true, true);
    }
    
    private ShardingSphereSchema createSchemaMetadata() {
        return new ShardingSphereSchema("public", mock(DatabaseType.class), List.of(createTableMetadata()), List.of());
    }
    
    private ShardingSphereTable createTableMetadata() {
        return new ShardingSphereTable("t_order", List.of(), List.of(), List.of(), TableType.TABLE);
    }
    
    private void assertCandidate(final MCPCompletionProviderResult actual, final String expectedValue) {
        Collection<MCPCompletionCandidate> actualCandidates = actual.getCandidates();
        assertThat(actualCandidates.size(), is(1));
        assertThat(actualCandidates.iterator().next().getValue(), is(expectedValue));
    }
}
