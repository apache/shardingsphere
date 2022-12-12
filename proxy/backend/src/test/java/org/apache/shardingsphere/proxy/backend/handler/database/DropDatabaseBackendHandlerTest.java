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

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.dialect.exception.syntax.database.DatabaseDropNotExistsException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DropDatabaseBackendHandlerTest extends ProxyContextRestorer {
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private DropDatabaseStatement sqlStatement;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataContexts metaDataContexts;
    
    private DropDatabaseBackendHandler handler;
    
    @Before
    public void setUp() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
        handler = new DropDatabaseBackendHandler(sqlStatement, connectionSession);
        Map<String, ShardingSphereDatabase> databases = new HashMap<>(2, 1);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        databases.put("test_db", database);
        databases.put("other_db", database);
        when(metaDataContexts.getMetaData().getDatabases()).thenReturn(databases);
        when(metaDataContexts.getMetaData().getDatabase("test_db")).thenReturn(database);
        when(metaDataContexts.getMetaData().getDatabase("other_db")).thenReturn(database);
        when(metaDataContexts.getMetaData().getDatabase("test_not_exist_db")).thenReturn(database);
        when(metaDataContexts.getMetaData().getDatabase("test_not_exist_db")).thenReturn(database);
        when(metaDataContexts.getMetaData().getGlobalRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        when(ProxyContext.getInstance().databaseExists("test_db")).thenReturn(true);
        when(ProxyContext.getInstance().databaseExists("other_db")).thenReturn(true);
    }
    
    @Test(expected = DatabaseDropNotExistsException.class)
    public void assertExecuteDropNotExistDatabase() {
        when(sqlStatement.getDatabaseName()).thenReturn("test_not_exist_db");
        handler.execute();
    }
    
    @Test
    public void assertExecuteDropNotExistDatabaseWithIfExists() {
        when(sqlStatement.getDatabaseName()).thenReturn("test_not_exist_db");
        when(sqlStatement.isIfExists()).thenReturn(true);
        handler.execute();
    }
    
    @Test
    public void assertExecuteDropWithoutCurrentDatabase() {
        when(sqlStatement.getDatabaseName()).thenReturn("test_db");
        ResponseHeader responseHeader = handler.execute();
        verify(connectionSession, times(0)).setCurrentDatabase(null);
        assertThat(responseHeader, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteDropCurrentDatabase() {
        when(connectionSession.getDatabaseName()).thenReturn("test_db");
        when(sqlStatement.getDatabaseName()).thenReturn("test_db");
        ResponseHeader responseHeader = handler.execute();
        verify(connectionSession).setCurrentDatabase(null);
        assertThat(responseHeader, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteDropOtherDatabase() {
        when(connectionSession.getDatabaseName()).thenReturn("test_db");
        when(sqlStatement.getDatabaseName()).thenReturn("other_db");
        ResponseHeader responseHeader = handler.execute();
        verify(connectionSession, times(0)).setCurrentDatabase(null);
        assertThat(responseHeader, instanceOf(UpdateResponseHeader.class));
    }
}
