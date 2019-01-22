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

package org.apache.shardingsphere.shardingproxy.backend;

import lombok.SneakyThrows;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.ErrPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.OKPacket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SchemaBroadcastBackendHandlerTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private BackendHandlerFactory backendHandlerFactory;
    
    @Test
    public void assertExecuteSchemaBroadcast() {
        MockGlobalRegistryUtil.setLogicSchemas("schema", 10);
        setUnderlyingHandler(new CommandResponsePackets(new OKPacket(1)));
        String sql = "grant select on test_db.* to root@'%'";
        SchemaBroadcastBackendHandler schemaBroadcastBackendHandler = new SchemaBroadcastBackendHandler(1, sql, backendConnection, DatabaseType.MySQL);
        setBackendHandlerFactory(schemaBroadcastBackendHandler);
        CommandResponsePackets actual = schemaBroadcastBackendHandler.execute();
        assertThat(actual.getHeadPacket(), instanceOf(OKPacket.class));
        verify(backendConnection).setCurrentSchema(null);
        verify(backendConnection, times(10)).setCurrentSchema(anyString());
    }
    
    @Test
    public void assertExecuteSchemaBroadcastFailed() {
        MockGlobalRegistryUtil.setLogicSchemas("schema", 5);
        setUnderlyingHandler(new CommandResponsePackets(new ErrPacket(1, new SQLException("no reason", "X999", -1))));
        String sql = "grant select on test_db.* to root@'%'";
        SchemaBroadcastBackendHandler schemaBroadcastBackendHandler = new SchemaBroadcastBackendHandler(1, sql, backendConnection, DatabaseType.MySQL);
        setBackendHandlerFactory(schemaBroadcastBackendHandler);
        CommandResponsePackets actual = schemaBroadcastBackendHandler.execute();
        assertThat(actual.getHeadPacket(), instanceOf(ErrPacket.class));
        verify(backendConnection).setCurrentSchema(null);
        verify(backendConnection, times(5)).setCurrentSchema(anyString());
    }
    
    private void setUnderlyingHandler(final CommandResponsePackets commandResponsePackets) {
        BackendHandler backendHandler = mock(BackendHandler.class);
        when(backendHandler.execute()).thenReturn(commandResponsePackets);
        when(backendHandlerFactory.newTextProtocolInstance(anyInt(), anyString(), (BackendConnection) any(), (DatabaseType) any())).thenReturn(backendHandler);
    }
    
    @SneakyThrows
    private void setBackendHandlerFactory(final SchemaBroadcastBackendHandler schemaBroadcastBackendHandler) {
        Field field = schemaBroadcastBackendHandler.getClass().getDeclaredField("backendHandlerFactory");
        field.setAccessible(true);
        field.set(schemaBroadcastBackendHandler, backendHandlerFactory);
    }
}


