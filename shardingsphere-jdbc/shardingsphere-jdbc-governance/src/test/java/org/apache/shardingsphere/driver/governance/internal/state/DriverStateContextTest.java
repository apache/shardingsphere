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

package org.apache.shardingsphere.driver.governance.internal.state;

import org.apache.shardingsphere.driver.governance.internal.circuit.connection.CircuitBreakerConnection;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.state.StateContext;
import org.apache.shardingsphere.infra.state.StateEvent;
import org.apache.shardingsphere.infra.state.StateType;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public final class DriverStateContextTest {
    
    @Before
    @After
    public void reset() {
        StateContext.switchState(new StateEvent(StateType.OK, true));
    }
    
    @Test
    public void assertGetConnectionWithOkState() {
        Connection actual = DriverStateContext.getConnection(
                Collections.singletonMap("ds", mock(DataSource.class, RETURNS_DEEP_STUBS)), mock(MetaDataContexts.class), mock(TransactionContexts.class, RETURNS_DEEP_STUBS), TransactionType.LOCAL);
        assertThat(actual, instanceOf(ShardingSphereConnection.class));
    }
    
    @Test
    public void assertGetConnectionWithCircuitBreakState() {
        StateContext.switchState(new StateEvent(StateType.CIRCUIT_BREAK, true));
        Connection actual = DriverStateContext.getConnection(
                Collections.singletonMap("ds", mock(DataSource.class, RETURNS_DEEP_STUBS)), mock(MetaDataContexts.class), mock(TransactionContexts.class, RETURNS_DEEP_STUBS), TransactionType.LOCAL);
        assertThat(actual, instanceOf(CircuitBreakerConnection.class));
    }
}
