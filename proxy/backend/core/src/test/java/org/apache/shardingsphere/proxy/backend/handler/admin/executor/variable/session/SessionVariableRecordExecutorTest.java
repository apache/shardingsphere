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

package org.apache.shardingsphere.proxy.backend.handler.admin.executor.variable.session;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.RequiredSessionVariableRecorder;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DatabaseTypedSPILoader.class)
class SessionVariableRecordExecutorTest {
    
    private final DatabaseType databaseType = mock(DatabaseType.class);
    
    @Test
    void assertRecordSingleVariableWhenNeedReplay() {
        ReplayedSessionVariableProvider provider = mock(ReplayedSessionVariableProvider.class);
        when(provider.isNeedToReplay("autocommit")).thenReturn(true);
        when(DatabaseTypedSPILoader.findService(ReplayedSessionVariableProvider.class, databaseType)).thenReturn(Optional.of(provider));
        RequiredSessionVariableRecorder recorder = mock(RequiredSessionVariableRecorder.class);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getRequiredSessionVariableRecorder()).thenReturn(recorder);
        new SessionVariableRecordExecutor(databaseType, connectionSession).recordVariable("autocommit", "1");
        verify(recorder).setVariable("autocommit", "1");
    }
    
    @Test
    void assertSkipSingleVariableWhenNotNeedReplay() {
        ReplayedSessionVariableProvider provider = mock(ReplayedSessionVariableProvider.class);
        when(DatabaseTypedSPILoader.findService(ReplayedSessionVariableProvider.class, databaseType)).thenReturn(Optional.of(provider));
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        assertDoesNotThrow(() -> new SessionVariableRecordExecutor(databaseType, connectionSession).recordVariable("sql_mode", "STRICT_ALL_TABLES"));
    }
    
    @Test
    void assertRecordVariableWithAllWhenProviderAbsent() {
        when(DatabaseTypedSPILoader.findService(ReplayedSessionVariableProvider.class, databaseType)).thenReturn(Optional.empty());
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        assertDoesNotThrow(() -> new SessionVariableRecordExecutor(databaseType, connectionSession).recordVariable(Collections.singletonMap("var_a", "1")));
    }
    
    @Test
    void assertRecordOnlyNeededVariables() {
        ReplayedSessionVariableProvider provider = mock(ReplayedSessionVariableProvider.class);
        when(provider.isNeedToReplay("var_a")).thenReturn(true);
        when(provider.isNeedToReplay("var_b")).thenReturn(false);
        when(DatabaseTypedSPILoader.findService(ReplayedSessionVariableProvider.class, databaseType)).thenReturn(Optional.of(provider));
        RequiredSessionVariableRecorder recorder = mock(RequiredSessionVariableRecorder.class);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getRequiredSessionVariableRecorder()).thenReturn(recorder);
        Map<String, String> variables = new HashMap<>(2, 1F);
        variables.put("var_a", "1");
        variables.put("var_b", "2");
        new SessionVariableRecordExecutor(databaseType, connectionSession).recordVariable(variables);
        verify(recorder).setVariable("var_a", "1");
        verify(recorder, never()).setVariable("var_b", "2");
    }
}
