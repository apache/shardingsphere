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

package org.apache.shardingsphere.proxy.backend.handler.database;

import org.apache.shardingsphere.dialect.exception.syntax.database.DatabaseCreateExistsException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CreateDatabaseBackendHandlerTest extends ProxyContextRestorer {
    
    @Mock
    private CreateDatabaseStatement statement;
    
    private CreateDatabaseBackendHandler handler;
    
    @Before
    public void setUp() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
        handler = new CreateDatabaseBackendHandler(statement);
        when(metaDataContexts.getMetaData().getDatabases()).thenReturn(Collections.singletonMap("test_db", mock(ShardingSphereDatabase.class)));
    }
    
    @Test
    public void assertExecuteCreateNewDatabase() throws SQLException {
        when(statement.getDatabaseName()).thenReturn("other_db");
        assertThat(handler.execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test(expected = DatabaseCreateExistsException.class)
    public void assertExecuteCreateExistDatabase() throws SQLException {
        when(ProxyContext.getInstance().databaseExists("test_db")).thenReturn(true);
        when(statement.getDatabaseName()).thenReturn("test_db");
        assertThat(handler.execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteCreateExistDatabaseWithIfNotExists() throws SQLException {
        when(statement.getDatabaseName()).thenReturn("test_db");
        when(statement.isIfNotExists()).thenReturn(true);
        assertThat(handler.execute(), instanceOf(UpdateResponseHeader.class));
    }
}
