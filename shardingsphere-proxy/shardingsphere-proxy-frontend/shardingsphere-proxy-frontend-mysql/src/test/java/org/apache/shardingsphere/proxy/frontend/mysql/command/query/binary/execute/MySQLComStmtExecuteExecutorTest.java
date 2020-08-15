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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.execute;

import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.MySQLComStmtExecutePacket;
import org.apache.shardingsphere.infra.executor.sql.raw.execute.result.query.QueryHeader;
import org.apache.shardingsphere.kernel.context.SchemaContext;
import org.apache.shardingsphere.kernel.context.runtime.RuntimeContext;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.proxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.rdl.parser.engine.ShardingSphereSQLParserEngine;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collections;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLComStmtExecuteExecutorTest {
    
    @Mock
    private SQLException sqlException;
    
    @Mock
    private DatabaseCommunicationEngine databaseCommunicationEngine;
    
    @Test
    @SneakyThrows
    public void assertIsErrorResponse() {
        BackendConnection backendConnection = mock(BackendConnection.class);
        SchemaContext schema = mock(SchemaContext.class);
        RuntimeContext runtimeContext = mock(RuntimeContext.class);
        ShardingSphereSQLParserEngine sqlParserEngine = mock(ShardingSphereSQLParserEngine.class);
        when(runtimeContext.getSqlParserEngine()).thenReturn(sqlParserEngine);
        when(schema.getRuntimeContext()).thenReturn(runtimeContext);
        when(backendConnection.getSchema()).thenReturn(schema);
        MySQLComStmtExecuteExecutor mysqlComStmtExecuteExecutor = new MySQLComStmtExecuteExecutor(mock(MySQLComStmtExecutePacket.class), backendConnection);
        FieldSetter.setField(mysqlComStmtExecuteExecutor, MySQLComStmtExecuteExecutor.class.getDeclaredField("databaseCommunicationEngine"), databaseCommunicationEngine);
        when(databaseCommunicationEngine.execute()).thenReturn(new ErrorResponse(sqlException));
        mysqlComStmtExecuteExecutor.execute();
        assertThat(mysqlComStmtExecuteExecutor.isErrorResponse(), Matchers.is(true));
    }
    
    @Test
    @SneakyThrows
    public void assertIsUpdateResponse() {
        BackendConnection backendConnection = mock(BackendConnection.class);
        SchemaContext schema = mock(SchemaContext.class);
        RuntimeContext runtimeContext = mock(RuntimeContext.class);
        ShardingSphereSQLParserEngine sqlParserEngine = mock(ShardingSphereSQLParserEngine.class);
        when(runtimeContext.getSqlParserEngine()).thenReturn(sqlParserEngine);
        when(schema.getRuntimeContext()).thenReturn(runtimeContext);
        when(backendConnection.getSchema()).thenReturn(schema);
        MySQLComStmtExecuteExecutor mysqlComStmtExecuteExecutor = new MySQLComStmtExecuteExecutor(mock(MySQLComStmtExecutePacket.class), backendConnection);
        FieldSetter.setField(mysqlComStmtExecuteExecutor, MySQLComStmtExecuteExecutor.class.getDeclaredField("databaseCommunicationEngine"), databaseCommunicationEngine);
        when(databaseCommunicationEngine.execute()).thenReturn(new UpdateResponse());
        mysqlComStmtExecuteExecutor.execute();
        assertThat(mysqlComStmtExecuteExecutor.isUpdateResponse(), Matchers.is(true));
    }
    
    @Test
    @SneakyThrows
    public void assertIsQuery() {
        BackendConnection backendConnection = mock(BackendConnection.class);
        SchemaContext schema = mock(SchemaContext.class);
        RuntimeContext runtimeContext = mock(RuntimeContext.class);
        ShardingSphereSQLParserEngine sqlParserEngine = mock(ShardingSphereSQLParserEngine.class);
        when(runtimeContext.getSqlParserEngine()).thenReturn(sqlParserEngine);
        when(schema.getRuntimeContext()).thenReturn(runtimeContext);
        when(backendConnection.getSchema()).thenReturn(schema);
        MySQLComStmtExecuteExecutor mysqlComStmtExecuteExecutor = new MySQLComStmtExecuteExecutor(mock(MySQLComStmtExecutePacket.class), backendConnection);
        FieldSetter.setField(mysqlComStmtExecuteExecutor, MySQLComStmtExecuteExecutor.class.getDeclaredField("databaseCommunicationEngine"), databaseCommunicationEngine);
        when(databaseCommunicationEngine.execute()).thenReturn(new QueryResponse(Collections.singletonList(mock(QueryHeader.class))));
        mysqlComStmtExecuteExecutor.execute();
        assertThat(mysqlComStmtExecuteExecutor.isQuery(), Matchers.is(true));
    }
}
