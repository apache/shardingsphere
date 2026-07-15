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
import org.apache.shardingsphere.mcp.support.database.exception.MCPDatabaseQueryFailedException;
import org.apache.shardingsphere.mcp.support.database.exception.MCPJDBCErrorCategory;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkflowDistSQLQueryUtilsTest {
    
    @Test
    void assertIsUnsupportedDistSQLQueryFailureWithSyntaxCause() {
        MCPQueryFailedException actualException = new MCPQueryFailedException("You have an error in your SQL syntax near 'MASK RULE orders FROM logic_db'",
                new SQLSyntaxErrorException("You have an error in your SQL syntax"));
        assertTrue(WorkflowDistSQLQueryUtils.isUnsupportedDistSQLQueryFailure(actualException));
    }
    
    @Test
    void assertIsUnsupportedDistSQLQueryFailureWithoutJDBCEvidence() {
        MCPQueryFailedException actualException = new MCPQueryFailedException("DistSQL syntax is unsupported by this runtime backend.");
        assertFalse(WorkflowDistSQLQueryUtils.isUnsupportedDistSQLQueryFailure(actualException));
    }
    
    @Test
    void assertIsUnsupportedDistSQLQueryFailureWithConnectionFailure() {
        MCPQueryFailedException actualException = new MCPQueryFailedException("Connection refused.", new SQLException("Connection refused."));
        assertFalse(WorkflowDistSQLQueryUtils.isUnsupportedDistSQLQueryFailure(actualException));
    }
    
    @Test
    void assertQueryRuleRows() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        List<Map<String, Object>> expected = List.of(Map.of("name", "orders"));
        when(queryFacade.query("logic_db", "", "SHOW MASK RULES")).thenReturn(expected);
        assertThat(WorkflowDistSQLQueryUtils.queryRuleRows(queryFacade, "logic_db", "SHOW MASK RULES"), is(expected));
    }
    
    @Test
    void assertQueryRuleRowsWithUnsupportedDistSQL() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("logic_db", "", "SHOW MASK RULES")).thenThrow(
                new MCPDatabaseQueryFailedException(MCPJDBCErrorCategory.SYNTAX, new SQLException("syntax error", "42601")));
        assertTrue(WorkflowDistSQLQueryUtils.queryRuleRows(queryFacade, "logic_db", "SHOW MASK RULES").isEmpty());
    }
    
    @Test
    void assertQueryRuleRowsWithConnectionFailure() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        MCPQueryFailedException expected = new MCPQueryFailedException("Connection refused.", new SQLException("Connection refused."));
        when(queryFacade.query("logic_db", "", "SHOW MASK RULES")).thenThrow(expected);
        MCPQueryFailedException actual = assertThrows(MCPQueryFailedException.class, () -> WorkflowDistSQLQueryUtils.queryRuleRows(queryFacade, "logic_db", "SHOW MASK RULES"));
        assertThat(actual, sameInstance(expected));
    }
}
