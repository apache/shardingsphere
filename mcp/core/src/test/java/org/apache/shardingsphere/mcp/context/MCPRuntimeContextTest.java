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

package org.apache.shardingsphere.mcp.context;

import org.apache.shardingsphere.mcp.capability.ServiceCapability;
import org.apache.shardingsphere.mcp.execute.MCPJdbcExecutionAdapter;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshots;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class MCPRuntimeContextTest {
    
    @Test
    void assertCreate() {
        MCPRuntimeContext actual = createRuntimeContext();
        assertNotNull(actual.getCapabilityBuilder());
        assertNotNull(actual.getTransactionCommandExecutor());
        assertNotNull(actual.getExecuteQueryFacade());
    }
    
    @Test
    void assertAssembleServiceCapability() {
        MCPRuntimeContext runtimeContext = createRuntimeContext();
        ServiceCapability actual = runtimeContext.getCapabilityBuilder().assembleServiceCapability();
        assertTrue(actual.getSupportedResources().contains("shardingsphere://capabilities"));
        assertTrue(actual.getSupportedTools().contains("execute_query"));
    }
    
    private MCPRuntimeContext createRuntimeContext() {
        MCPJdbcExecutionAdapter jdbcExecutionAdapter = mock(MCPJdbcExecutionAdapter.class);
        return new MCPRuntimeContextTestFactory().create(new DatabaseMetadataSnapshots(Collections.emptyMap()), jdbcExecutionAdapter);
    }
}
