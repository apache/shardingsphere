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

package org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.executor;

import io.netty.util.Attribute;
import io.netty.util.AttributeMap;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.variable.charset.CharsetVariableProvider;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.variable.session.ReplayedSessionVariableProvider;
import org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.executor.variable.charset.PostgreSQLCharsetVariableProvider;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.RequiredSessionVariableRecorder;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dal.PostgreSQLResetParameterStatement;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PostgreSQLResetVariableAdminExecutorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    @Test
    void assertExecute() {
        PostgreSQLResetVariableAdminExecutor executor = new PostgreSQLResetVariableAdminExecutor(new PostgreSQLResetParameterStatement(databaseType, "key"));
        ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        RequiredSessionVariableRecorder requiredSessionVariableRecorder = mock(RequiredSessionVariableRecorder.class);
        when(connectionSession.getRequiredSessionVariableRecorder()).thenReturn(requiredSessionVariableRecorder);
        try (MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(CharsetVariableProvider.class, databaseType)).thenReturn(Optional.empty());
            ReplayedSessionVariableProvider replayedSessionVariableProvider = mock(ReplayedSessionVariableProvider.class);
            when(replayedSessionVariableProvider.isNeedToReplay("key")).thenReturn(true);
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(ReplayedSessionVariableProvider.class, databaseType)).thenReturn(Optional.of(replayedSessionVariableProvider));
            executor.execute(connectionSession, mock());
            verify(requiredSessionVariableRecorder).setVariable("key", "DEFAULT");
        }
    }
    
    @Test
    void assertExecuteWithClientEncoding() {
        PostgreSQLResetVariableAdminExecutor executor = new PostgreSQLResetVariableAdminExecutor(new PostgreSQLResetParameterStatement(databaseType, "client_encoding"));
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        RequiredSessionVariableRecorder requiredSessionVariableRecorder = mock(RequiredSessionVariableRecorder.class);
        AttributeMap attributeMap = mock(AttributeMap.class);
        @SuppressWarnings("unchecked")
        Attribute<Charset> attribute = mock(Attribute.class);
        when(connectionSession.getRequiredSessionVariableRecorder()).thenReturn(requiredSessionVariableRecorder);
        when(connectionSession.getAttributeMap()).thenReturn(attributeMap);
        when(attributeMap.attr(CommonConstants.CHARSET_ATTRIBUTE_KEY)).thenReturn(attribute);
        try (MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(CharsetVariableProvider.class, databaseType)).thenReturn(Optional.of(new PostgreSQLCharsetVariableProvider()));
            ReplayedSessionVariableProvider replayedSessionVariableProvider = mock(ReplayedSessionVariableProvider.class);
            when(replayedSessionVariableProvider.isNeedToReplay("client_encoding")).thenReturn(true);
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(ReplayedSessionVariableProvider.class, databaseType)).thenReturn(Optional.of(replayedSessionVariableProvider));
            executor.execute(connectionSession, mock());
            verify(attribute).set(StandardCharsets.UTF_8);
            verify(requiredSessionVariableRecorder).setVariable("client_encoding", "DEFAULT");
        }
    }
}
