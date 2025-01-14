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

package org.apache.shardingsphere.mode.persist.service.unified;

import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ListenerAssistedPersistServiceTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PersistRepository repository;
    
    @Test
    void assertPersistDatabaseNameListenerAssisted() {
        new ListenerAssistedPersistService(repository).persistDatabaseNameListenerAssisted("foo_db", ListenerAssistedType.CREATE_DATABASE);
        verify(repository).persistEphemeral("/states/listener_assisted/foo_db", ListenerAssistedType.CREATE_DATABASE.name());
    }
    
    @Test
    void assertDeleteDatabaseNameListenerAssisted() {
        new ListenerAssistedPersistService(repository).deleteDatabaseNameListenerAssisted("foo_db");
        verify(repository).delete("/states/listener_assisted/foo_db");
    }
}
