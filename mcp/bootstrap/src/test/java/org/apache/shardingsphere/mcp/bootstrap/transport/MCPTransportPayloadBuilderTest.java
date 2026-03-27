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

package org.apache.shardingsphere.mcp.bootstrap.transport;

import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityView;
import org.apache.shardingsphere.mcp.capability.ResultBehavior;
import org.apache.shardingsphere.mcp.capability.SchemaSemantics;
import org.apache.shardingsphere.mcp.capability.StatementClass;
import org.apache.shardingsphere.mcp.capability.TransactionBoundaryBehavior;
import org.apache.shardingsphere.mcp.protocol.ErrorCode;
import org.apache.shardingsphere.mcp.resource.MetadataObjectType;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPTransportPayloadBuilderTest {
    
    private final MCPTransportPayloadBuilder payloadBuilder = new MCPTransportPayloadBuilder();
    
    @Test
    void assertCreateDatabaseCapabilityPayload() {
        DatabaseCapabilityView capability = new DatabaseCapabilityView("logic_db", "H2", "2.2",
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
    void assertCreateErrorPayload() {
        Map<String, Object> actual = payloadBuilder.createErrorPayload("invalid_request", "bad request");
        
        assertThat(actual.get("error_code"), is("invalid_request"));
        assertThat(actual.get("message"), is("bad request"));
    }
    
    @Test
    void assertToDomainErrorCode() {
        assertThat(payloadBuilder.toDomainErrorCode(ErrorCode.NOT_FOUND), is("not_found"));
    }
}
