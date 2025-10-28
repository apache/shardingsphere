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

package org.apache.shardingsphere.mode.manager.standalone.persist.service;

import org.apache.shardingsphere.infra.executor.sql.process.ProcessRegistry;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.SQLException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProcessRegistry.class)
class StandaloneProcessPersistServiceTest {
    
    private final StandaloneProcessPersistService processPersistService = new StandaloneProcessPersistService();
    
    @Test
    void assertGetProcessList() {
        ProcessRegistry processRegistry = mock(ProcessRegistry.class);
        when(ProcessRegistry.getInstance()).thenReturn(processRegistry);
        processPersistService.getProcessList();
        verify(processRegistry).listAll();
    }
    
    @Test
    void assertKillProcess() throws SQLException {
        ProcessRegistry processRegistry = mock(ProcessRegistry.class);
        when(ProcessRegistry.getInstance()).thenReturn(processRegistry);
        processPersistService.killProcess("foo_id");
        verify(ProcessRegistry.getInstance()).kill("foo_id");
    }
}
