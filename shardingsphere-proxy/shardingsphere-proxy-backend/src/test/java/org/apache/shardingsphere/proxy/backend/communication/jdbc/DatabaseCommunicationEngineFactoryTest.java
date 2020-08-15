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

package org.apache.shardingsphere.proxy.backend.communication.jdbc;

import org.apache.shardingsphere.kernel.context.SchemaContext;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DatabaseCommunicationEngineFactoryTest {
    
    private SchemaContext schemaContext;
    
    private BackendConnection backendConnection;
    
    @Before
    public void setUp() {
        schemaContext = mock(SchemaContext.class, RETURNS_DEEP_STUBS);
        when(schemaContext.getSchema().getRules()).thenReturn(Collections.emptyList());
        when(schemaContext.getRuntimeContext().getSqlParserEngine().parse(anyString(), anyBoolean())).thenReturn(mock(SQLStatement.class));
    
        backendConnection = mock(BackendConnection.class, RETURNS_DEEP_STUBS);
        when(backendConnection.isSerialExecute()).thenReturn(true);
    }
    
    @Test
    public void assertNewTextProtocolInstance() {
        BackendConnection backendConnection = mock(BackendConnection.class);
        when(backendConnection.getSchema()).thenReturn(schemaContext);
        DatabaseCommunicationEngine engine =
                DatabaseCommunicationEngineFactory.getInstance().newTextProtocolInstance(mock(SQLStatement.class), "schemaName", backendConnection);
        assertNotNull(engine);
        assertThat(engine, instanceOf(JDBCDatabaseCommunicationEngine.class));
    }
    
    @Test
    public void assertNewBinaryProtocolInstance() {
        BackendConnection backendConnection = mock(BackendConnection.class);
        when(backendConnection.getSchema()).thenReturn(schemaContext);
        DatabaseCommunicationEngine engine =
                DatabaseCommunicationEngineFactory.getInstance().newBinaryProtocolInstance(mock(SQLStatement.class), "schemaName", Collections.emptyList(), backendConnection);
        assertNotNull(engine);
        assertThat(engine, instanceOf(JDBCDatabaseCommunicationEngine.class));
    }
}
