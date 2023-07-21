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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor;

import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.ReplayedSessionVariablesProvider;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.SessionVariableReplayExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.RequiredSessionVariableRecorder;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MySQLSessionVariableReplayExecutorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final SessionVariableReplayExecutor sessionVariableReplayExecutor = DatabaseTypedSPILoader.getService(SessionVariableReplayExecutor.class, databaseType);
    
    @Test
    void assertHandleDiscard() {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        sessionVariableReplayExecutor.handle(connectionSession, "", "");
        verifyNoInteractions(connectionSession);
    }
    
    @Test
    void assertHandleRecord() {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getRequiredSessionVariableRecorder()).thenReturn(mock(RequiredSessionVariableRecorder.class));
        try (MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            ReplayedSessionVariablesProvider variablesProvider = mock(ReplayedSessionVariablesProvider.class);
            when(variablesProvider.getVariables()).thenReturn(Collections.singleton("sql_mode"));
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(ReplayedSessionVariablesProvider.class, databaseType)).thenReturn(Optional.of(variablesProvider));
            sessionVariableReplayExecutor.handle(connectionSession, "sql_mode", "''");
            verify(connectionSession.getRequiredSessionVariableRecorder()).setVariable("sql_mode", "''");
            sessionVariableReplayExecutor.handle(connectionSession, "@variable_name", "'variable_value'");
            verify(connectionSession.getRequiredSessionVariableRecorder()).setVariable("@variable_name", "'variable_value'");
        }
    }
}
