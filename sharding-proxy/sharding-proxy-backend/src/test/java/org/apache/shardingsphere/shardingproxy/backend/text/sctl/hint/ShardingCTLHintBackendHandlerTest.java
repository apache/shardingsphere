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

package org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint;

import org.apache.shardingsphere.api.hint.HintManager;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.exception.InvalidShardingCTLFormatException;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.exception.UnsupportedShardingCTLTypeException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingCTLHintBackendHandlerTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Before
    public void setUp() {
        when(backendConnection.isSupportHint()).thenReturn(true);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertNotSupportHint() {
        when(backendConnection.isSupportHint()).thenReturn(false);
        new ShardingCTLHintBackendHandler("", backendConnection).execute();
    }
    
    @Test
    public void assertInvalidShardingCTLFormat() {
        String sql = "sctl:hint1 xx=yy";
        ShardingCTLHintBackendHandler shardingCTLHintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        assertThat(((ErrorResponse) shardingCTLHintBackendHandler.execute()).getCause(), instanceOf(InvalidShardingCTLFormatException.class));
    }
    
    @Test
    public void assertSetMasterOnly() {
        String sql = "sctl:hint set master_only=true ";
        ShardingCTLHintBackendHandler shardingCTLHintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        assertThat(shardingCTLHintBackendHandler.execute(), instanceOf(UpdateResponse.class));
        assertTrue(HintManager.isMasterRouteOnly());
    }
    
    @Test
    public void assertSetDatabaseShardingValueTable() {
        String sql = "sctl:hint set databaseShardingValue=100";
        ShardingCTLHintBackendHandler shardingCTLHintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        assertThat(shardingCTLHintBackendHandler.execute(), instanceOf(UpdateResponse.class));
        assertEquals(HintManager.getDatabaseShardingValues().iterator().next(), "100");
    }
    
    @Test
    public void assertAddDatabaseShardingValue() {
        String sql = "sctl:hint addDatabaseShardingValue user=100 ";
        ShardingCTLHintBackendHandler shardingCTLHintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        assertThat(shardingCTLHintBackendHandler.execute(), instanceOf(UpdateResponse.class));
        assertEquals(HintManager.getDatabaseShardingValues("user").iterator().next(), "100");
    }
    
    @Test
    public void assertAddTableShardingValue() {
        String sql = "sctl:hint addTableShardingValue  user=100 ";
        ShardingCTLHintBackendHandler shardingCTLHintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        assertThat(shardingCTLHintBackendHandler.execute(), instanceOf(UpdateResponse.class));
        assertEquals(HintManager.getTableShardingValues("user").iterator().next(), "100");
    }
    
    @Test
    public void assertClear() {
        String sql = "sctl:hint clear ";
        ShardingCTLHintBackendHandler shardingCTLHintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        assertThat(shardingCTLHintBackendHandler.execute(), instanceOf(UpdateResponse.class));
        assertThat(HintManager.getInstance(), instanceOf(HintManager.class));
        HintManager.clear();
    }
    
    @Test
    public void assertUnsupportedShardingCTLType() {
        String sql = "sctl:hint xx=yy";
        ShardingCTLHintBackendHandler shardingCTLHintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        assertThat(((ErrorResponse) shardingCTLHintBackendHandler.execute()).getCause(), instanceOf(UnsupportedShardingCTLTypeException.class));
    }
}
