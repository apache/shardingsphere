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

import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.MySQLComStmtExecutePacket;
import org.apache.shardingsphere.infra.auth.memory.MemoryAuthentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLComStmtExecuteExecutorTest {
    
    @Mock
    private DatabaseCommunicationEngine databaseCommunicationEngine;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        Field metaDataContexts = ProxyContext.getInstance().getClass().getDeclaredField("metaDataContexts");
        metaDataContexts.setAccessible(true);
        Map<String, ShardingSphereMetaData> metaDataMap = Collections.singletonMap("logic_db", mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS));
        metaDataContexts.set(ProxyContext.getInstance(), 
                new StandardMetaDataContexts(metaDataMap, mock(ExecutorEngine.class), new MemoryAuthentication(), new ConfigurationProperties(new Properties()), new MySQLDatabaseType()));
    }
    
    @Test
    public void assertIsQueryResponse() throws NoSuchFieldException, SQLException {
        BackendConnection backendConnection = mock(BackendConnection.class);
        when(backendConnection.getSchemaName()).thenReturn("logic_db");
        MySQLComStmtExecutePacket packet = mock(MySQLComStmtExecutePacket.class);
        when(packet.getSql()).thenReturn("SELECT 1");
        MySQLComStmtExecuteExecutor mysqlComStmtExecuteExecutor = new MySQLComStmtExecuteExecutor(packet, backendConnection);
        FieldSetter.setField(mysqlComStmtExecuteExecutor, MySQLComStmtExecuteExecutor.class.getDeclaredField("databaseCommunicationEngine"), databaseCommunicationEngine);
        when(databaseCommunicationEngine.execute()).thenReturn(new QueryResponseHeader(Collections.singletonList(mock(QueryHeader.class))));
        mysqlComStmtExecuteExecutor.execute();
        assertThat(mysqlComStmtExecuteExecutor.getResponseType(), is(ResponseType.QUERY));
    }
    
    @Test
    public void assertIsUpdateResponse() throws NoSuchFieldException, SQLException {
        BackendConnection backendConnection = mock(BackendConnection.class);
        when(backendConnection.getSchemaName()).thenReturn("logic_db");
        MySQLComStmtExecutePacket packet = mock(MySQLComStmtExecutePacket.class);
        when(packet.getSql()).thenReturn("SELECT 1");
        MySQLComStmtExecuteExecutor mysqlComStmtExecuteExecutor = new MySQLComStmtExecuteExecutor(packet, backendConnection);
        FieldSetter.setField(mysqlComStmtExecuteExecutor, MySQLComStmtExecuteExecutor.class.getDeclaredField("databaseCommunicationEngine"), databaseCommunicationEngine);
        when(databaseCommunicationEngine.execute()).thenReturn(new UpdateResponseHeader(mock(SQLStatement.class)));
        mysqlComStmtExecuteExecutor.execute();
        assertThat(mysqlComStmtExecuteExecutor.getResponseType(), is(ResponseType.UPDATE));
    }
}
