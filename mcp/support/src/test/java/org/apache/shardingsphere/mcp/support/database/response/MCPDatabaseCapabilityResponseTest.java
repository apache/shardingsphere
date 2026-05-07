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

package org.apache.shardingsphere.mcp.support.database.response;

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityOption;
import org.apache.shardingsphere.mcp.support.database.capability.SchemaExecutionSemantics;
import org.apache.shardingsphere.mcp.support.database.capability.SchemaSemantics;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPDatabaseCapabilityResponseTest {

    @Test
    void assertToPayload() {
        MCPDatabaseCapability actualCapability = new MCPDatabaseCapability("logic_db", "8.0.32", TypedSPILoader.getService(MCPDatabaseCapabilityOption.class, "MySQL"));
        Map<String, Object> actual = new MCPDatabaseCapabilityResponse(actualCapability).toPayload();
        assertThat(actual, is(Map.ofEntries(
                Map.entry("database", "logic_db"),
                Map.entry("databaseType", "MySQL"),
                Map.entry("supportedObjectTypes", EnumSet.of(SupportedMCPMetadataObjectType.SCHEMA, SupportedMCPMetadataObjectType.TABLE,
                        SupportedMCPMetadataObjectType.VIEW, SupportedMCPMetadataObjectType.COLUMN, SupportedMCPMetadataObjectType.INDEX)),
                Map.entry("supportedStatementClasses", EnumSet.of(SupportedMCPStatement.QUERY, SupportedMCPStatement.DML, SupportedMCPStatement.DDL,
                        SupportedMCPStatement.DCL, SupportedMCPStatement.TRANSACTION_CONTROL, SupportedMCPStatement.SAVEPOINT, SupportedMCPStatement.EXPLAIN_ANALYZE)),
                Map.entry("supportsTransactionControl", true),
                Map.entry("supportsSavepoint", true),
                Map.entry("defaultSchemaSemantics", SchemaSemantics.DATABASE_AS_SCHEMA),
                Map.entry("schemaExecutionSemantics", SchemaExecutionSemantics.FIXED_TO_DATABASE),
                Map.entry("supportsCrossSchemaSql", false),
                Map.entry("supportsExplainAnalyze", true),
                Map.entry("explainAnalyzeExecutionRisk", "EXPLAIN ANALYZE may execute the underlying SELECT on this engine; use it only when the user accepts runtime execution cost."))));
    }
}
