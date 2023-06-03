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

package org.apache.shardingsphere.infra.state;

import org.apache.shardingsphere.infra.state.instance.InstanceStateContext;
import org.apache.shardingsphere.infra.state.instance.InstanceState;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class InstanceStateContextTest {
    
    private final InstanceStateContext instanceStateContext = new InstanceStateContext();
    
    @Test
    void assertSwitchStateWithCircuitBreakOn() {
        instanceStateContext.switchToValidState(InstanceState.CIRCUIT_BREAK);
        assertThat(instanceStateContext.getCurrentState(), is(InstanceState.CIRCUIT_BREAK));
        instanceStateContext.switchToInvalidState(InstanceState.CIRCUIT_BREAK);
    }
    
    @Test
    void assertSwitchStateWithCircuitBreakOff() {
        instanceStateContext.switchToInvalidState(InstanceState.CIRCUIT_BREAK);
        assertThat(instanceStateContext.getCurrentState(), is(InstanceState.OK));
    }
    
    @Test
    void assertSwitchStateWithMultiState() {
        instanceStateContext.switchToValidState(InstanceState.CIRCUIT_BREAK);
        instanceStateContext.switchToValidState(InstanceState.LOCK);
        assertThat(instanceStateContext.getCurrentState(), is(InstanceState.LOCK));
        instanceStateContext.switchToInvalidState(InstanceState.LOCK);
        assertThat(instanceStateContext.getCurrentState(), is(InstanceState.CIRCUIT_BREAK));
    }
}
