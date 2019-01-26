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

package org.apache.shardingsphere.shardingproxy.backend.text.admin;

import lombok.SneakyThrows;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.shardingproxy.backend.MockGlobalRegistryUtil;
import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.runtime.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.ErrPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.OKPacket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class BroadcastBackendHandlerTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private DatabaseCommunicationEngineFactory databaseCommunicationEngineFactory;
    
    @Mock
    private DatabaseCommunicationEngine databaseCommunicationEngine;
    
    @Test
    public void assertExecuteSuccess() {
        MockGlobalRegistryUtil.setLogicSchemas("schema", 10);
        mockDatabaseCommunicationEngine(new CommandResponsePackets(new OKPacket(1)));
        BroadcastBackendHandler broadcastBackendHandler = new BroadcastBackendHandler(1, "SET timeout = 1000", backendConnection, DatabaseType.MySQL);
        setBackendHandlerFactory(broadcastBackendHandler);
        OKPacket actual = (OKPacket) broadcastBackendHandler.execute().getHeadPacket();
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getAffectedRows(), is(0L));
        assertThat(actual.getLastInsertId(), is(0L));
        verify(databaseCommunicationEngine, times(10)).execute();
    }
    
    @Test
    public void assertExecuteFailure() {
        MockGlobalRegistryUtil.setLogicSchemas("schema", 10);
        ErrPacket errPacket = new ErrPacket(1, new SQLException("no reason", "X999", -1));
        mockDatabaseCommunicationEngine(new CommandResponsePackets(errPacket));
        BroadcastBackendHandler broadcastBackendHandler = new BroadcastBackendHandler(1, "SET timeout = 1000", backendConnection, DatabaseType.MySQL);
        setBackendHandlerFactory(broadcastBackendHandler);
        ErrPacket actual = (ErrPacket) broadcastBackendHandler.execute().getHeadPacket();
        assertThat(actual, is(errPacket));
        verify(databaseCommunicationEngine, times(10)).execute();
    }
    
    private void mockDatabaseCommunicationEngine(final CommandResponsePackets commandResponsePackets) {
        when(databaseCommunicationEngine.execute()).thenReturn(commandResponsePackets);
        when(databaseCommunicationEngineFactory.newTextProtocolInstance(
                (LogicSchema) any(), anyInt(), anyString(), (BackendConnection) any(), (DatabaseType) any())).thenReturn(databaseCommunicationEngine);
    }
    
    @SneakyThrows
    private void setBackendHandlerFactory(final BroadcastBackendHandler schemaBroadcastBackendHandler) {
        Field field = schemaBroadcastBackendHandler.getClass().getDeclaredField("databaseCommunicationEngineFactory");
        field.setAccessible(true);
        field.set(schemaBroadcastBackendHandler, databaseCommunicationEngineFactory);
    }
}
