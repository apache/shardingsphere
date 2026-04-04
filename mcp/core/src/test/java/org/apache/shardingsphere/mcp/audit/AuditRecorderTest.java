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

package org.apache.shardingsphere.mcp.audit;

import org.apache.shardingsphere.mcp.protocol.MCPError.MCPErrorCode;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditRecorderTest {
    
    @Test
    void assertRecordQueryExecution() {
        AuditRecorder auditRecorder = new AuditRecorder();
        
        AuditRecord actual = auditRecorder.recordQueryExecution("session-1", "logic_db", "SELECT * FROM orders", true, "QUERY");
        
        assertThat(actual.getOperationClass(), is(OperationClass.QUERY_EXECUTION));
        assertTrue(actual.isSuccess());
        assertThat(actual.getDatabase(), is("logic_db"));
        assertThat(actual.getOperationDigest().length(), is(64));
    }
    
    @Test
    void assertRecordQueryExecutionWithErrorCode() {
        AuditRecorder auditRecorder = new AuditRecorder();
        
        AuditRecord actual = auditRecorder.recordQueryExecution("session-1", "logic_db", "SELECT * FROM orders", false, MCPErrorCode.INVALID_REQUEST, "QUERY");
        
        assertThat(actual.getOperationClass(), is(OperationClass.QUERY_EXECUTION));
        assertTrue(actual.getErrorCode().isPresent());
        assertThat(actual.getErrorCode().get(), is(MCPErrorCode.INVALID_REQUEST));
    }
}
