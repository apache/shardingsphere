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

package org.apache.shardingsphere.mcp.capability.provider;

import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.capability.ResultBehavior;
import org.apache.shardingsphere.mcp.capability.SchemaSemantics;
import org.apache.shardingsphere.mcp.capability.TransactionBoundaryBehavior;
import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshot;
import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPDatabaseCapabilityProviderTest {
    
    @Test
    void assertProvide() {
        Optional<DatabaseCapability> actual = createDatabaseCapabilityBuilder().provide("logic_db");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDatabase(), is("logic_db"));
        assertThat(actual.get().getDatabaseType(), is("MySQL"));
        assertThat(actual.get().getMinSupportedVersion(), is("BASELINE"));
        assertThat(actual.get().getSupportedMetadataObjectTypes(),
                is(EnumSet.of(MetadataObjectType.SCHEMA, MetadataObjectType.TABLE, MetadataObjectType.VIEW, MetadataObjectType.COLUMN, MetadataObjectType.INDEX)));
        assertTrue(actual.get().isSupportsTransactionControl());
        assertTrue(actual.get().isSupportsSavepoint());
        assertThat(actual.get().getSupportedTransactionStatements().size(), is(7));
        assertThat(actual.get().getDefaultSchemaSemantics(), is(SchemaSemantics.DATABASE_AS_SCHEMA));
        assertFalse(actual.get().isSupportsCrossSchemaSql());
        assertFalse(actual.get().isSupportsExplainAnalyze());
        assertThat(actual.get().getExplainAnalyzeResultBehavior(), is(ResultBehavior.UNSUPPORTED));
        assertThat(actual.get().getExplainAnalyzeTransactionBehavior(), is(TransactionBoundaryBehavior.UNSUPPORTED));
    }
    
    @Test
    void assertProvideWithoutIndex() {
        Optional<DatabaseCapability> actual = createDatabaseCapabilityBuilder().provide("warehouse");
        assertTrue(actual.isPresent());
        assertFalse(actual.get().getSupportedMetadataObjectTypes().contains(MetadataObjectType.INDEX));
        assertFalse(actual.get().isSupportsTransactionControl());
        assertFalse(actual.get().isSupportsSavepoint());
        assertThat(actual.get().getSupportedTransactionStatements().size(), is(0));
        assertThat(actual.get().getDefaultSchemaSemantics(), is(SchemaSemantics.DATABASE_AS_SCHEMA));
    }
    
    @Test
    void assertProvideWithRuntimeOverlay() {
        DatabaseMetadataSnapshots snapshots = new DatabaseMetadataSnapshots(Map.of("logic_db", new DatabaseMetadataSnapshot("MySQL", "8.0.32", Collections.emptyList())));
        Optional<DatabaseCapability> actual = new MCPDatabaseCapabilityProvider(snapshots).provide("logic_db");
        assertTrue(actual.isPresent());
        assertTrue(actual.get().getSupportedMetadataObjectTypes().contains(MetadataObjectType.INDEX));
        assertFalse(actual.get().isSupportsCrossSchemaSql());
        assertTrue(actual.get().isSupportsExplainAnalyze());
    }
    
    private MCPDatabaseCapabilityProvider createDatabaseCapabilityBuilder() {
        return new MCPDatabaseCapabilityProvider(new DatabaseMetadataSnapshots(Map.of(
                "logic_db", new DatabaseMetadataSnapshot("MySQL", "", Collections.emptyList()),
                "warehouse", new DatabaseMetadataSnapshot("Hive", "", Collections.emptyList()))));
    }
}
