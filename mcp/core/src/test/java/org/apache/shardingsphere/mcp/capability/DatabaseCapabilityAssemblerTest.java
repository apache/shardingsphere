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

package org.apache.shardingsphere.mcp.capability;

import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshot;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.resource.MetadataObjectType;
import org.apache.shardingsphere.mcp.resource.ResourceUriResolver;
import org.apache.shardingsphere.mcp.tool.MCPToolCatalog;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseCapabilityAssemblerTest {
    
    @Test
    void assertAssembleServiceCapability() {
        DatabaseCapabilityAssembler assembler = createAssembler();
        
        ServiceCapability actualServiceCapability = assembler.assembleServiceCapability();
        
        assertThat(actualServiceCapability.getSupportedResources(), is(new ResourceUriResolver().getSupportedResources()));
        assertThat(actualServiceCapability.getSupportedResources().size(), is(16));
        assertThat(actualServiceCapability.getSupportedResources().get(0), is("shardingsphere://capabilities"));
        assertThat(actualServiceCapability.getSupportedTools(), is(new MCPToolCatalog().getSupportedTools()));
        assertThat(actualServiceCapability.getSupportedTools().size(), is(11));
        assertThat(actualServiceCapability.getSupportedTools().get(5), is("list_indexes"));
        assertThat(actualServiceCapability.getSupportedStatementClasses().size(), is(7));
        assertTrue(actualServiceCapability.getSupportedStatementClasses().contains(StatementClass.EXPLAIN_ANALYZE));
    }
    
    @Test
    void assertAssembleDatabaseCapability() {
        DatabaseCapabilityAssembler assembler = createAssembler();
        
        Optional<DatabaseCapability> actualCapability = assembler.assembleDatabaseCapability("logic_db");
        
        assertTrue(actualCapability.isPresent());
        assertThat(actualCapability.get().getDatabase(), is("logic_db"));
        assertThat(actualCapability.get().getDatabaseType(), is("MySQL"));
        assertThat(actualCapability.get().getMinSupportedVersion(), is("BASELINE"));
        assertThat(actualCapability.get().getSupportedMetadataObjectTypes(),
                is(EnumSet.of(MetadataObjectType.SCHEMA, MetadataObjectType.TABLE, MetadataObjectType.VIEW, MetadataObjectType.COLUMN, MetadataObjectType.INDEX)));
        assertTrue(actualCapability.get().isSupportsTransactionControl());
        assertTrue(actualCapability.get().isSupportsSavepoint());
        assertThat(actualCapability.get().getSupportedTransactionStatements().size(), is(7));
        assertThat(actualCapability.get().getDefaultSchemaSemantics(), is(SchemaSemantics.DATABASE_AS_SCHEMA));
        assertFalse(actualCapability.get().isSupportsCrossSchemaSql());
        assertFalse(actualCapability.get().isSupportsExplainAnalyze());
        assertThat(actualCapability.get().getExplainAnalyzeResultBehavior(), is(ResultBehavior.UNSUPPORTED));
        assertThat(actualCapability.get().getExplainAnalyzeTransactionBehavior(), is(TransactionBoundaryBehavior.UNSUPPORTED));
    }
    
    @Test
    void assertAssembleDatabaseCapabilityWithoutIndex() {
        DatabaseCapabilityAssembler assembler = createAssembler();
        
        Optional<DatabaseCapability> actualCapability = assembler.assembleDatabaseCapability("warehouse");
        
        assertTrue(actualCapability.isPresent());
        assertFalse(actualCapability.get().getSupportedMetadataObjectTypes().contains(MetadataObjectType.INDEX));
        assertFalse(actualCapability.get().isSupportsTransactionControl());
        assertFalse(actualCapability.get().isSupportsSavepoint());
        assertThat(actualCapability.get().getSupportedTransactionStatements().size(), is(0));
        assertThat(actualCapability.get().getDefaultSchemaSemantics(), is(SchemaSemantics.DATABASE_AS_SCHEMA));
    }
    
    @Test
    void assertAssembleDatabaseCapabilityWithRuntimeOverlay() {
        DatabaseMetadataSnapshots snapshots = new DatabaseMetadataSnapshots(Map.of("logic_db", new DatabaseMetadataSnapshot("MySQL", "8.0.32", Collections.emptyList())));
        DatabaseCapabilityAssembler assembler = new DatabaseCapabilityAssembler(snapshots);
        Optional<DatabaseCapability> actualCapability = assembler.assembleDatabaseCapability("logic_db");
        assertTrue(actualCapability.isPresent());
        assertTrue(actualCapability.get().getSupportedMetadataObjectTypes().contains(MetadataObjectType.INDEX));
        assertFalse(actualCapability.get().isSupportsCrossSchemaSql());
        assertTrue(actualCapability.get().isSupportsExplainAnalyze());
    }
    
    @Test
    void assertAssembleDatabaseCapabilityWithUnknownDatabaseType() {
        DatabaseCapabilityAssembler assembler = createAssembler();
        
        Optional<DatabaseCapability> actualCapability = assembler.assembleDatabaseCapability("logic_db", "unknown");
        
        assertFalse(actualCapability.isPresent());
    }
    
    @Test
    void assertConstructWithNullMetadataCatalog() {
        DatabaseCapabilityAssembler actual = assertDoesNotThrow(() -> new DatabaseCapabilityAssembler(null));
        
        assertNotNull(actual);
    }
    
    private DatabaseCapabilityAssembler createAssembler() {
        return new DatabaseCapabilityAssembler(new DatabaseMetadataSnapshots(Map.of(
                "logic_db", new DatabaseMetadataSnapshot("MySQL", Collections.emptyList()),
                "warehouse", new DatabaseMetadataSnapshot("Hive", Collections.emptyList()))));
    }
}
