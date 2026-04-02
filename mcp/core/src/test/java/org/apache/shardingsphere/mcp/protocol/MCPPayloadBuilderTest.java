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

package org.apache.shardingsphere.mcp.protocol;

import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.capability.ResultBehavior;
import org.apache.shardingsphere.mcp.capability.SchemaSemantics;
import org.apache.shardingsphere.mcp.capability.ServiceCapability;
import org.apache.shardingsphere.mcp.capability.StatementClass;
import org.apache.shardingsphere.mcp.capability.TransactionBoundaryBehavior;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObject;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPPayloadBuilderTest {
    
    private final MCPPayloadBuilder payloadBuilder = new MCPPayloadBuilder();
    
    @Test
    void assertCreateServiceCapabilityPayload() {
        ServiceCapability capability = new ServiceCapability(List.of("shardingsphere://capabilities"), List.of("get_capabilities"), Set.of(StatementClass.QUERY));
        
        Map<String, Object> actual = payloadBuilder.createServiceCapabilityPayload(capability);
        
        assertThat(actual.get("supportedResources"), is(List.of("shardingsphere://capabilities")));
        assertThat(actual.get("supportedTools"), is(List.of("get_capabilities")));
        assertThat(actual.get("supportedStatementClasses"), is(Set.of(StatementClass.QUERY)));
    }
    
    @Test
    void assertCreateDatabaseCapabilityPayload() {
        DatabaseCapability capability = new DatabaseCapability("logic_db", "H2", "2.2",
                Set.of(MetadataObjectType.TABLE), Set.of(StatementClass.QUERY), true, true, Set.of("BEGIN"),
                true, 100, 3000, SchemaSemantics.NATIVE_SCHEMA, true, false,
                TransactionBoundaryBehavior.UNIFORM, TransactionBoundaryBehavior.NATIVE,
                ResultBehavior.RESULT_SET, TransactionBoundaryBehavior.UNSUPPORTED);
        
        Map<String, Object> actual = payloadBuilder.createDatabaseCapabilityPayload(capability);
        
        assertThat(actual.get("database"), is("logic_db"));
        assertThat(actual.get("databaseType"), is("H2"));
        assertThat(actual.get("maxRowsDefault"), is(100));
        assertThat(actual.get("defaultSchemaSemantics"), is(SchemaSemantics.NATIVE_SCHEMA));
        assertTrue((Boolean) actual.get("supportsTransactionControl"));
    }
    
    @Test
    void assertCreateMetadataItemsPayload() {
        Map<String, Object> actual = payloadBuilder.createMetadataItemsPayload(
                List.of(new MetadataObject("logic_db", "public", MetadataObjectType.TABLE, "orders", "", "")), "10");
        
        assertThat(((List<?>) actual.get("items")).size(), is(1));
        assertThat(actual.get("next_page_token"), is("10"));
    }
    
    @Test
    void assertCreateExecuteQueryPayload() {
        ExecuteQueryResponse response = ExecuteQueryResponse.resultSet(
                List.of(new ExecuteQueryColumnDefinition("order_id", "INTEGER", "INTEGER", false)),
                List.of(List.of(1L)), false);
        
        Map<String, Object> actual = payloadBuilder.createExecuteQueryPayload(response);
        
        assertThat(actual.get("result_kind"), is("result_set"));
        assertThat(actual.get("statement_type"), is("QUERY"));
        assertThat(((List<?>) actual.get("columns")).size(), is(1));
        assertThat(((List<?>) actual.get("rows")).size(), is(1));
        assertFalse((Boolean) actual.get("truncated"));
    }
    
    @Test
    void assertCreateExecuteQueryPayloadWithError() {
        Map<String, Object> actual = payloadBuilder.createExecuteQueryPayload(ExecuteQueryResponse.error(MCPErrorCode.NOT_FOUND, "missing table"));
        
        assertThat(actual.get("result_kind"), is("statement_ack"));
        assertTrue(actual.containsKey("error"));
        assertThat(((Map<?, ?>) actual.get("error")).get("error_code"), is("not_found"));
    }
    
    @Test
    void assertCreateErrorPayload() {
        Map<String, Object> actual = payloadBuilder.createErrorPayload("invalid_request", "bad request");
        
        assertThat(actual.get("error_code"), is("invalid_request"));
        assertThat(actual.get("message"), is("bad request"));
    }
    
    @Test
    void assertToDomainErrorCode() {
        assertThat(payloadBuilder.toDomainErrorCode(MCPErrorCode.NOT_FOUND), is("not_found"));
    }
}
