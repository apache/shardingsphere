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

package org.apache.shardingsphere.proxy.backend.handler.admin.postgresql;

import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLSetStatement;
import org.junit.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

public final class PostgreSQLSetVariableAdminExecutorTest {
    
    @Test
    public void assertExecute() {
        VariableAssignSegment variableAssignSegment = new VariableAssignSegment();
        VariableSegment variable = new VariableSegment();
        variable.setVariable("key");
        variableAssignSegment.setVariable(variable);
        variableAssignSegment.setAssignValue("value");
        PostgreSQLSetStatement setStatement = new PostgreSQLSetStatement();
        setStatement.getVariableAssigns().add(variableAssignSegment);
        PostgreSQLSetVariableAdminExecutor executor = new PostgreSQLSetVariableAdminExecutor(setStatement);
        try (MockedStatic<PostgreSQLSessionVariableHandlerFactory> mockStatic = mockStatic(PostgreSQLSessionVariableHandlerFactory.class)) {
            PostgreSQLSessionVariableHandler mockHandler = mock(PostgreSQLSessionVariableHandler.class);
            mockStatic.when(() -> PostgreSQLSessionVariableHandlerFactory.getHandler("key")).thenReturn(mockHandler);
            executor.execute(null);
            verify(mockHandler).handle(null, "key", "value");
        }
    }
}
