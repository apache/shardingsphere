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

package org.apache.shardingsphere.mode.node.path.type.global.state;

import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.global.state.coordinator.database.DatabaseListenerCoordinatorNodePath;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DatabaseListenerCoordinatorNodePathTest {
    
    @Test
    void assertToPath() {
        assertThat(NodePathGenerator.toPath(new DatabaseListenerCoordinatorNodePath(null)), is("/states/database_listener_coordinator"));
        assertThat(NodePathGenerator.toPath(new DatabaseListenerCoordinatorNodePath("foo_db")), is("/states/database_listener_coordinator/foo_db"));
    }
    
    @Test
    void assertCreateDatabaseSearchCriteria() {
        assertThat(NodePathSearcher.get("/states/database_listener_coordinator/foo_db", DatabaseListenerCoordinatorNodePath.createDatabaseSearchCriteria()), is("foo_db"));
        assertFalse(NodePathSearcher.find("/states/database_listener_coordinator", DatabaseListenerCoordinatorNodePath.createDatabaseSearchCriteria()).isPresent());
    }
}
