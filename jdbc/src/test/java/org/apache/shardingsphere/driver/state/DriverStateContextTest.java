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

package org.apache.shardingsphere.driver.state;

import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.state.circuit.connection.CircuitBreakerConnection;
import org.apache.shardingsphere.infra.state.instance.InstanceState;
import org.apache.shardingsphere.infra.state.instance.InstanceStateContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DriverStateContextTest {
    
    @Test
    void assertGetConnectionWithOkState() {
        ContextManager contextManager = mockContextManager(InstanceState.OK);
        assertThat(DriverStateContext.getConnection("foo_db", contextManager), isA(ShardingSphereConnection.class));
    }
    
    @Test
    void assertGetConnectionWithCircuitBreakState() {
        ContextManager contextManager = mockContextManager(InstanceState.CIRCUIT_BREAK);
        assertThat(DriverStateContext.getConnection("foo_db", contextManager), isA(CircuitBreakerConnection.class));
    }
    
    private ContextManager mockContextManager(final InstanceState instanceState) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        InstanceStateContext instanceStateContext = new InstanceStateContext();
        instanceStateContext.switchState(instanceState);
        when(result.getComputeNodeInstanceContext().getInstance().getState()).thenReturn(instanceStateContext);
        return result;
    }
}
