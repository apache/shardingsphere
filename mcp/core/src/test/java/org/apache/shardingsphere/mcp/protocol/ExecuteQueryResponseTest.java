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

import org.apache.shardingsphere.mcp.protocol.MCPError.MCPErrorCode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecuteQueryResponseTest {
    
    @Test
    void assertResultSet() {
        ExecuteQueryResponse actual = ExecuteQueryResponse.resultSet(List.of(new ExecuteQueryColumnDefinition("order_id", "INT", "INT", false)), List.of(List.of(1)), true);
        
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.RESULT_SET));
        assertThat(actual.getColumns().size(), is(1));
        assertThat(actual.getRows().size(), is(1));
        assertTrue(actual.isTruncated());
    }
    
    @Test
    void assertUpdateCount() {
        ExecuteQueryResponse actual = ExecuteQueryResponse.updateCount("UPDATE", 2);
        
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.UPDATE_COUNT));
        assertThat(actual.getStatementType(), is("UPDATE"));
        assertThat(actual.getAffectedRows(), is(2));
        assertThat(actual.getStatus(), is("OK"));
    }
    
    @Test
    void assertStatementAck() {
        ExecuteQueryResponse actual = ExecuteQueryResponse.statementAck("COMMIT", "Transaction committed.");
        
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.STATEMENT_ACK));
        assertThat(actual.getStatementType(), is("COMMIT"));
        assertThat(actual.getMessage(), is("Transaction committed."));
    }
    
    @Test
    void assertError() {
        ExecuteQueryResponse actual = ExecuteQueryResponse.error(MCPErrorCode.UNSUPPORTED, "Feature is not supported.");
        
        assertThat(actual.getStatementType(), is("ERROR"));
        assertThat(actual.getStatus(), is("ERROR"));
        assertTrue(actual.getError().isPresent());
        assertThat(actual.getError().get().getCode(), is(MCPErrorCode.UNSUPPORTED));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertToPayloadWithError() {
        Map<String, Object> actual = ExecuteQueryResponse.error(MCPErrorCode.UNSUPPORTED, "Feature is not supported.").toPayload();
        
        assertThat(String.valueOf(((Map<String, Object>) actual.get("error")).get("error_code")), is("unsupported"));
        assertThat(String.valueOf(actual.get("message")), is("Feature is not supported."));
    }
    
    @Test
    void assertIsSuccessful() {
        ExecuteQueryResponse actual = ExecuteQueryResponse.resultSet(List.of(), List.of(), false);
        
        assertTrue(actual.isSuccessful());
    }
    
    @Test
    void assertIsSuccessfulWithError() {
        ExecuteQueryResponse actual = ExecuteQueryResponse.error(MCPErrorCode.QUERY_FAILED, "Failure.");
        
        assertFalse(actual.isSuccessful());
    }
    
    @Test
    void assertResultSetWithNullRows() {
        ExecuteQueryResponse actual = ExecuteQueryResponse.resultSet(List.of(), null, false);
        
        assertNull(actual.getRows());
    }
}
