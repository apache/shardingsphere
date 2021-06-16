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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl;

import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.DBDropExistsException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DropDatabaseBackendHandlerTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private DropDatabaseStatement sqlStatement;
    
    @Mock
    private TransactionContexts transactionContexts;
    
    @Mock
    private MetaDataContexts metaDataContexts;
    
    private DropDatabaseBackendHandler handler;
    
    @Before
    public void setUp() {
        ProxyContext.getInstance().init(metaDataContexts, transactionContexts);
        handler = new DropDatabaseBackendHandler(sqlStatement, backendConnection);
        when(metaDataContexts.getAllSchemaNames()).thenReturn(Arrays.asList("test_db", "other_db"));
    }
    
    @Test(expected = DBDropExistsException.class)
    public void assertExecuteDropNotExistDatabase() {
        when(sqlStatement.getDatabaseName()).thenReturn("test_not_exist_db");
        handler.execute();
    }

    @Test
    public void assertExecuteDropWithoutCurrentDatabase() {
        when(sqlStatement.getDatabaseName()).thenReturn("test_db");
        ResponseHeader responseHeader = handler.execute();
        verify(backendConnection, times(0)).setCurrentSchema(null);
        assertNotNull(responseHeader);
        assertTrue(responseHeader instanceof UpdateResponseHeader);
    }

    @Test
    public void assertExecuteDropCurrentDatabase() {
        when(backendConnection.getSchemaName()).thenReturn("test_db");
        when(sqlStatement.getDatabaseName()).thenReturn("test_db");
        ResponseHeader responseHeader = handler.execute();
        verify(backendConnection).setCurrentSchema(null);
        assertNotNull(responseHeader);
        assertTrue(responseHeader instanceof UpdateResponseHeader);
    }

    @Test
    public void assertExecuteDropOtherDatabase() {
        when(backendConnection.getSchemaName()).thenReturn("test_db");
        when(sqlStatement.getDatabaseName()).thenReturn("other_db");
        ResponseHeader responseHeader = handler.execute();
        verify(backendConnection, times(0)).setCurrentSchema(null);
        assertNotNull(responseHeader);
        assertTrue(responseHeader instanceof UpdateResponseHeader);
    }
}
