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

package org.apache.shardingsphere.proxy.backend.postgresql.handler.admin;

import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.SessionVariableHandler;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.SetCharsetExecutor;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLResetParameterStatement;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

class PostgreSQLResetVariableAdminExecutorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    @Test
    void assertExecute() {
        PostgreSQLResetVariableAdminExecutor executor = new PostgreSQLResetVariableAdminExecutor(new PostgreSQLResetParameterStatement("key"));
        try (MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            SetCharsetExecutor setCharsetExecutor = mock(SetCharsetExecutor.class);
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(SetCharsetExecutor.class, databaseType)).thenReturn(Optional.of(setCharsetExecutor));
            SessionVariableHandler sessionVariableHandler = mock(SessionVariableHandler.class);
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(SessionVariableHandler.class, databaseType)).thenReturn(Optional.of(sessionVariableHandler));
            executor.execute(null);
            verify(setCharsetExecutor).handle(null, "key", "DEFAULT");
            verify(sessionVariableHandler).handle(null, "key", "DEFAULT");
        }
    }
}
