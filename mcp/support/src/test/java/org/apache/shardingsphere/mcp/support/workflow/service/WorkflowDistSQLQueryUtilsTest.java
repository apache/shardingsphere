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

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPQueryFailedException;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowDistSQLQueryUtilsTest {
    
    @Test
    void assertIsUnsupportedDistSQLQueryFailureWithSyntaxCause() {
        MCPQueryFailedException actualException = new MCPQueryFailedException("You have an error in your SQL syntax near 'MASK RULE orders FROM logic_db'",
                new SQLSyntaxErrorException("You have an error in your SQL syntax"));
        assertTrue(WorkflowDistSQLQueryUtils.isUnsupportedDistSQLQueryFailure(actualException));
    }
    
    @Test
    void assertIsUnsupportedDistSQLQueryFailureWithDistSQLMessage() {
        MCPQueryFailedException actualException = new MCPQueryFailedException("DistSQL syntax is unsupported by this runtime backend.");
        assertTrue(WorkflowDistSQLQueryUtils.isUnsupportedDistSQLQueryFailure(actualException));
    }
    
    @Test
    void assertIsUnsupportedDistSQLQueryFailureWithConnectionFailure() {
        MCPQueryFailedException actualException = new MCPQueryFailedException("Connection refused.", new SQLException("Connection refused."));
        assertFalse(WorkflowDistSQLQueryUtils.isUnsupportedDistSQLQueryFailure(actualException));
    }
}
