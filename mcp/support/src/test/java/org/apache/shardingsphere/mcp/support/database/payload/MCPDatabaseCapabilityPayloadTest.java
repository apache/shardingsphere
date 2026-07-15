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

package org.apache.shardingsphere.mcp.support.database.payload;

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaSemantics;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.support.database.capability.SchemaExecutionSemantics;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MCPDatabaseCapabilityPayloadTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertToPayloadArguments")
    void assertToPayload(final String name, final boolean supportsExplain) {
        MCPDatabaseCapability actualCapability = mock(MCPDatabaseCapability.class);
        when(actualCapability.getDatabaseName()).thenReturn("logic_db");
        when(actualCapability.getDatabaseType()).thenReturn("FixtureDB");
        when(actualCapability.getSupportedMetadataObjectTypes()).thenReturn(EnumSet.of(SupportedMCPMetadataObjectType.SCHEMA, SupportedMCPMetadataObjectType.TABLE,
                SupportedMCPMetadataObjectType.VIEW, SupportedMCPMetadataObjectType.COLUMN, SupportedMCPMetadataObjectType.INDEX));
        Set<SupportedMCPStatement> supportedStatementClasses = EnumSet.of(SupportedMCPStatement.QUERY, SupportedMCPStatement.DML, SupportedMCPStatement.DDL,
                SupportedMCPStatement.DCL, SupportedMCPStatement.TRANSACTION_CONTROL, SupportedMCPStatement.SAVEPOINT);
        if (supportsExplain) {
            supportedStatementClasses.add(SupportedMCPStatement.EXPLAIN);
        }
        when(actualCapability.getSupportedStatementClasses()).thenReturn(supportedStatementClasses);
        when(actualCapability.supportsTransactionControl()).thenReturn(true);
        when(actualCapability.supportsSavepoint()).thenReturn(true);
        when(actualCapability.getDefaultSchemaSemantics()).thenReturn(DialectSchemaSemantics.DATABASE_AS_SCHEMA);
        when(actualCapability.getSchemaExecutionSemantics()).thenReturn(SchemaExecutionSemantics.FIXED_TO_DATABASE);
        when(actualCapability.supportsExplain()).thenReturn(supportsExplain);
        Map<String, Object> actual = new MCPDatabaseCapabilityPayload(actualCapability).toPayload();
        assertThat(actual, is(Map.ofEntries(
                Map.entry("response_mode", "detail"),
                Map.entry("database", "logic_db"),
                Map.entry("databaseType", "FixtureDB"),
                Map.entry("supportedObjectTypes", EnumSet.of(SupportedMCPMetadataObjectType.SCHEMA, SupportedMCPMetadataObjectType.TABLE,
                        SupportedMCPMetadataObjectType.VIEW, SupportedMCPMetadataObjectType.COLUMN, SupportedMCPMetadataObjectType.INDEX)),
                Map.entry("supportedStatementClasses", supportedStatementClasses),
                Map.entry("supportsTransactionControl", true),
                Map.entry("supportsSavepoint", true),
                Map.entry("defaultSchemaSemantics", DialectSchemaSemantics.DATABASE_AS_SCHEMA),
                Map.entry("schemaExecutionSemantics", SchemaExecutionSemantics.FIXED_TO_DATABASE),
                Map.entry("supportsCrossSchemaSql", false),
                Map.entry("supportsExplain", supportsExplain))));
    }
    
    private static Stream<Arguments> assertToPayloadArguments() {
        return Stream.of(
                Arguments.of("supported", true),
                Arguments.of("unsupported", false));
    }
}
