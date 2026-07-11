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

package org.apache.shardingsphere.mcp.support.workflow.service;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.TableType;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.IdentifierPatternType;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.support.database.exception.DatabaseCapabilityNotFoundException;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class WorkflowPlanningContextValidatorTest {
    
    private final WorkflowPlanningContextValidator validator = new WorkflowPlanningContextValidator();
    
    @Test
    void assertEnsurePlanningContextRejectsMissingDatabase() {
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        boolean actual = validator.ensurePlanningContext(mock(MCPMetadataQueryFacade.class), new WorkflowRequest(), clarifiedIntent, snapshot);
        assertFalse(actual);
        assertThat(snapshot.getStatus(), is("clarifying"));
        assertThat(clarifiedIntent.getClarificationMessages(), is(List.of("Please provide logical database first.")));
        assertThat(snapshot.getIssues().getFirst().getCode(), is(WorkflowIssueCode.DATABASE_REQUIRED));
    }
    
    @Test
    void assertEnsurePlanningContextRejectsUnknownDatabase() {
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        request.setTable("orders");
        request.setColumn("phone");
        assertThrows(DatabaseCapabilityNotFoundException.class,
                () -> validator.ensurePlanningContext(mock(MCPMetadataQueryFacade.class), request, new ClarifiedIntent(), new WorkflowContextSnapshot()));
    }
    
    @Test
    void assertEnsurePlanningContextResolvesSchema() {
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        request.setTable("orders");
        request.setColumn("phone");
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryDatabase("logic_db")).thenReturn(Optional.of(createDatabaseMetadata()));
        when(metadataQueryFacade.querySchemas("logic_db")).thenReturn(List.of(createSchemaMetadata()));
        when(metadataQueryFacade.queryTable("logic_db", "public", "orders")).thenReturn(Optional.of(createTableMetadata()));
        when(metadataQueryFacade.queryTableColumn("logic_db", "public", "orders", "phone")).thenReturn(Optional.of(createColumnMetadata()));
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class, CALLS_REAL_METHODS);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            DatabaseType databaseType = mock(DatabaseType.class);
            typedSPILoader.when(() -> TypedSPILoader.findService(DatabaseType.class, "Fixture")).thenReturn(Optional.of(databaseType));
            DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
            when(dialectDatabaseMetaData.getIdentifierPatternType()).thenReturn(IdentifierPatternType.KEEP_ORIGIN);
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectDatabaseMetaData.class, databaseType)).thenReturn(Optional.of(dialectDatabaseMetaData));
            boolean actual = validator.ensurePlanningContext(metadataQueryFacade, request, clarifiedIntent, snapshot);
            assertTrue(actual);
            assertThat(request.getSchema(), is("public"));
            assertThat(clarifiedIntent.getInferredValues().get("schema"), is("public"));
            assertTrue(snapshot.getIssues().isEmpty());
        }
    }
    
    @Test
    void assertEnsureSupportedIdentifiersRejectsUnsupportedIdentifier() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        boolean actual = validator.ensureSupportedIdentifiers("", List.of("orders\ndrop"), snapshot, "intaking");
        assertFalse(actual);
        assertThat(snapshot.getIssues().getFirst().getMessage(), is("Identifier `orders\ndrop` contains unsupported characters."));
        assertThat(snapshot.getIssues().getFirst().getUserAction(), is("Use reviewable logical identifiers without NUL or line terminators."));
        assertThat(snapshot.getIssues().getFirst().getDetails(), is(Map.of("identifier", "orders\ndrop")));
    }
    
    @Test
    void assertEnsureOptionalSupportedIdentifiersAllowsEmptyIdentifier() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        boolean actual = validator.ensureOptionalSupportedIdentifiers("rule", List.of(""), snapshot, "intaking");
        assertTrue(actual);
        assertTrue(snapshot.getIssues().isEmpty());
    }
    
    private RuntimeDatabaseProfile createDatabaseMetadata() {
        return new RuntimeDatabaseProfile("logic_db", "Fixture", "1.0", true, true);
    }
    
    private ShardingSphereSchema createSchemaMetadata() {
        return new ShardingSphereSchema("public", mock(DatabaseType.class), List.of(createTableMetadata()), List.of());
    }
    
    private ShardingSphereTable createTableMetadata() {
        return new ShardingSphereTable("orders", List.of(createColumnMetadata()), List.of(), List.of(), TableType.TABLE);
    }
    
    private ShardingSphereColumn createColumnMetadata() {
        return new ShardingSphereColumn("phone", java.sql.Types.OTHER, false, false, false, true, false, true);
    }
}
