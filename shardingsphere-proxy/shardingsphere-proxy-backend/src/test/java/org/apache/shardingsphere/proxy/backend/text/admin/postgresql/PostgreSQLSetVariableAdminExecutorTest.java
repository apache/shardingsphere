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

package org.apache.shardingsphere.proxy.backend.text.admin.postgresql;

import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLSetStatement;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.sql.SQLException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

public final class PostgreSQLSetVariableAdminExecutorTest {
    
    @Test
    public void assertExecute() throws SQLException {
        PostgreSQLSetStatement setStatement = new PostgreSQLSetStatement();
        PostgreSQLSetVariableAdminExecutor executor = new PostgreSQLSetVariableAdminExecutor(setStatement);
        try (MockedStatic<PostgreSQLSessionVariableHandlerFactory> mockStatic = mockStatic(PostgreSQLSessionVariableHandlerFactory.class)) {
            PostgreSQLSessionVariableHandler mockHandler = mock(PostgreSQLSessionVariableHandler.class);
            mockStatic.when(() -> PostgreSQLSessionVariableHandlerFactory.getHandler("")).thenReturn(mockHandler);
            executor.execute(null);
            verify(mockHandler).handle(null, setStatement);
        }
    }
}
