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

package org.apache.shardingsphere.proxy.backend.handler.database.type;

import org.apache.shardingsphere.database.exception.core.exception.syntax.database.DatabaseCreateExistsException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.CreateDatabaseStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateDatabaseProxyBackendHandlerTest {
    
    @Mock
    private CreateDatabaseStatement statement;
    
    @Test
    void assertExecuteCreateNewDatabase() {
        when(statement.getDatabaseName()).thenReturn("bar_db");
        assertThat(new CreateDatabaseProxyBackendHandler(statement, mock(ContextManager.class, RETURNS_DEEP_STUBS)).execute(), isA(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteCreateExistDatabase() {
        when(statement.getDatabaseName()).thenReturn("foo_db");
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.containsDatabase("foo_db")).thenReturn(true);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData()).thenReturn(metaData);
        assertThrows(DatabaseCreateExistsException.class, () -> new CreateDatabaseProxyBackendHandler(statement, contextManager).execute());
    }
    
    @Test
    void assertExecuteCreateExistDatabaseWithIfNotExists() {
        when(statement.getDatabaseName()).thenReturn("foo_db");
        when(statement.isIfNotExists()).thenReturn(true);
        assertThat(new CreateDatabaseProxyBackendHandler(statement, mock(ContextManager.class, RETURNS_DEEP_STUBS)).execute(), isA(UpdateResponseHeader.class));
    }
}
