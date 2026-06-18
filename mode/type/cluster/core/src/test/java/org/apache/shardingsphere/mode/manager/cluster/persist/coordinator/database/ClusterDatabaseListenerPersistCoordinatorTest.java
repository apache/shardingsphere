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

package org.apache.shardingsphere.mode.manager.cluster.persist.coordinator.database;

import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClusterDatabaseListenerPersistCoordinatorTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ClusterPersistRepository repository;
    
    @Test
    void assertPersist() {
        new ClusterDatabaseListenerPersistCoordinator(repository).persist("foo_db", ClusterDatabaseListenerCoordinatorType.CREATE);
        verify(repository).persistEphemeral("/states/database_listener_coordinator/foo_db", ClusterDatabaseListenerCoordinatorType.CREATE.name());
    }
    
    @Test
    void assertDelete() {
        new ClusterDatabaseListenerPersistCoordinator(repository).delete("foo_db");
        verify(repository).delete("/states/database_listener_coordinator/foo_db");
    }
}
