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

package org.apache.shardingsphere.mcp.capability.database;

import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadataCatalog;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPDatabaseCapabilityProviderTest {
    
    @Test
    void assertProvide() {
        Optional<MCPDatabaseCapability> actual = createDatabaseCapabilityBuilder().provide("logic_db");
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
        Optional<MCPDatabaseCapability> actual = createDatabaseCapabilityBuilder().provide("warehouse");
        assertTrue(actual.isPresent());
        assertFalse(actual.get().getSupportedMetadataObjectTypes().contains(MetadataObjectType.INDEX));
        assertFalse(actual.get().getSupportedMetadataObjectTypes().contains(MetadataObjectType.SEQUENCE));
        assertFalse(actual.get().isSupportsTransactionControl());
        assertFalse(actual.get().isSupportsSavepoint());
        assertThat(actual.get().getSupportedTransactionStatements().size(), is(0));
        assertThat(actual.get().getDefaultSchemaSemantics(), is(SchemaSemantics.DATABASE_AS_SCHEMA));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideSequenceMatrixArguments")
    void assertProvideWithSequenceMatrix(final String name, final String databaseType, final boolean expectedSequenceSupport) {
        Optional<MCPDatabaseCapability> actual = new MCPDatabaseCapabilityProvider(
                new MCPDatabaseMetadataCatalog(Map.of("logic_db", new MCPDatabaseMetadata("logic_db", databaseType, "", Collections.emptyList())))).provide("logic_db");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getSupportedMetadataObjectTypes().contains(MetadataObjectType.SEQUENCE), is(expectedSequenceSupport));
    }
    
    @Test
    void assertProvideWithRuntimeOverlay() {
        MCPDatabaseMetadataCatalog metadataCatalog = new MCPDatabaseMetadataCatalog(Map.of("logic_db", new MCPDatabaseMetadata("logic_db", "MySQL", "8.0.32", Collections.emptyList())));
        Optional<MCPDatabaseCapability> actual = new MCPDatabaseCapabilityProvider(metadataCatalog).provide("logic_db");
        assertTrue(actual.isPresent());
        assertTrue(actual.get().getSupportedMetadataObjectTypes().contains(MetadataObjectType.INDEX));
        assertFalse(actual.get().isSupportsCrossSchemaSql());
        assertTrue(actual.get().isSupportsExplainAnalyze());
    }
    
    private MCPDatabaseCapabilityProvider createDatabaseCapabilityBuilder() {
        return new MCPDatabaseCapabilityProvider(new MCPDatabaseMetadataCatalog(Map.of(
                "logic_db", new MCPDatabaseMetadata("logic_db", "MySQL", "", Collections.emptyList()),
                "warehouse", new MCPDatabaseMetadata("warehouse", "Hive", "", Collections.emptyList()))));
    }
    
    private static Stream<Arguments> provideSequenceMatrixArguments() {
        return Stream.of(
                Arguments.of("mysql", "MySQL", false),
                Arguments.of("postgresql", "PostgreSQL", true),
                Arguments.of("open gauss", "openGauss", true),
                Arguments.of("sql server", "SQLServer", true),
                Arguments.of("mariadb", "MariaDB", true),
                Arguments.of("oracle", "Oracle", true),
                Arguments.of("clickhouse", "ClickHouse", false),
                Arguments.of("doris", "Doris", false),
                Arguments.of("hive", "Hive", false),
                Arguments.of("presto", "Presto", false),
                Arguments.of("firebird", "Firebird", true),
                Arguments.of("h2", "H2", true));
    }
}
