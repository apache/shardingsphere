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

package org.apache.shardingsphere.mode.manager.standalone.service;

import org.apache.shardingsphere.infra.executor.sql.process.Process;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessRegistry;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProcessRegistry.class)
class StandaloneProcessPersistServiceTest {
    
    private StandaloneProcessPersistService processPersistService;
    
    @BeforeEach
    void setUp() {
        processPersistService = new StandaloneProcessPersistService();
    }
    
    @Test
    void getProcessList() {
        ProcessRegistry processRegistry = mock(ProcessRegistry.class);
        when(ProcessRegistry.getInstance()).thenReturn(processRegistry);
        processPersistService.getProcessList();
        verify(processRegistry).listAll();
    }
    
    @Test
    void killProcess() throws SQLException {
        ProcessRegistry processRegistry = mock(ProcessRegistry.class);
        when(ProcessRegistry.getInstance()).thenReturn(processRegistry);
        Process process = mock(Process.class);
        Statement statement = mock(Statement.class);
        Map<Integer, Statement> processStatements = new ConcurrentHashMap<>();
        processStatements.put(1, statement);
        when(process.getProcessStatements()).thenReturn(processStatements);
        when(processRegistry.get(any())).thenReturn(process);
        processPersistService.killProcess("foo_process_id");
        verify(statement).cancel();
    }
}
