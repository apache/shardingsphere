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

package org.apache.shardingsphere.mode.node.path.state;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class StatesNodePathTest {
    
    @Test
    void assertGetClusterStatePath() {
        assertThat(StatesNodePath.getClusterStatePath(), is("/states/cluster_state"));
    }
    
    @Test
    void assertGetDatabaseListenerCoordinatorNodeRootPath() {
        assertThat(StatesNodePath.getDatabaseListenerCoordinatorNodeRootPath(), is("/states/database_listener_coordinator"));
    }
    
    @Test
    void assertGetDatabaseListenerCoordinatorNodePath() {
        assertThat(StatesNodePath.getDatabaseListenerCoordinatorNodePath("foo_db"), is("/states/database_listener_coordinator/foo_db"));
    }
    
    @Test
    void assertFindDatabaseName() {
        assertThat(StatesNodePath.findDatabaseName("/states/database_listener_coordinator/foo_db"), is(Optional.of("foo_db")));
        assertFalse(StatesNodePath.findDatabaseName("/states/database_listener_coordinator").isPresent());
    }
}
