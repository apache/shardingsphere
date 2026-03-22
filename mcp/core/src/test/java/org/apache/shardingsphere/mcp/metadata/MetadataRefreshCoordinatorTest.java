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

package org.apache.shardingsphere.mcp.metadata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetadataRefreshCoordinatorTest {
    
    @Test
    void assertMarkStructureChangeCommitted() {
        MetadataRefreshCoordinator coordinator = new MetadataRefreshCoordinator();
        
        coordinator.markStructureChangeCommitted("session-1", "logic_db");
        
        assertTrue(coordinator.isVisibleToSession("session-1", "logic_db"));
    }
    
    @Test
    void assertMarkDclChangeCommitted() {
        MetadataRefreshCoordinator coordinator = new MetadataRefreshCoordinator();
        
        coordinator.markDclChangeCommitted("session-1", "logic_db");
        
        assertTrue(coordinator.isVisibleToSession("session-1", "logic_db"));
    }
    
    @Test
    void assertIsVisibleToSession() {
        MetadataRefreshCoordinator coordinator = new MetadataRefreshCoordinator();
        coordinator.markStructureChangeCommitted("session-1", "logic_db");
        
        boolean actual = coordinator.isVisibleToSession("session-1", "logic_db");
        
        assertTrue(actual);
    }
    
    @Test
    void assertIsGlobalVisibilityWithinSla() {
        MetadataRefreshCoordinator coordinator = new MetadataRefreshCoordinator();
        coordinator.markStructureChangeCommitted("session-1", "logic_db");
        long nowMillis = coordinator.getLastGlobalRefreshTimeMillis("logic_db") + 1000L;
        
        boolean actual = coordinator.isGlobalVisibilityWithinSla("logic_db", nowMillis);
        
        assertTrue(actual);
    }
    
    @Test
    void assertGetLastGlobalRefreshTimeMillis() {
        MetadataRefreshCoordinator coordinator = new MetadataRefreshCoordinator();
        coordinator.markStructureChangeCommitted("session-1", "logic_db");
        long actual = coordinator.getLastGlobalRefreshTimeMillis("logic_db");
        assertFalse(0L == actual);
    }
    
    @Test
    void assertClearSession() {
        MetadataRefreshCoordinator coordinator = new MetadataRefreshCoordinator();
        coordinator.markStructureChangeCommitted("session-1", "logic_db");
        coordinator.clearSession("session-1");
        assertFalse(coordinator.isVisibleToSession("session-1", "logic_db"));
    }
}
