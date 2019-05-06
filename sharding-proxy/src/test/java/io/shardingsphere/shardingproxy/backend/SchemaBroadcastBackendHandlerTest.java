/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.backend;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.OKPacket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
public class SchemaBroadcastBackendHandlerTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private BackendHandlerFactory backendHandlerFactory;
    
    @Test
    public void assertExecuteSchemaBroadcast() {
        MockGlobalRegistryUtil.setLogicSchemas("schema", 10);
        setUnderlyingHandler(new CommandResponsePackets(new OKPacket(1)));
        String sql = "grant select on testdb.* to root@'%'";
        SchemaBroadcastBackendHandler schemaBroadcastBackendHandler = new SchemaBroadcastBackendHandler(1, sql, backendConnection, DatabaseType.MySQL, backendHandlerFactory);
        CommandResponsePackets actual = schemaBroadcastBackendHandler.execute();
        assertThat(actual.getHeadPacket(), instanceOf(OKPacket.class));
        verify(backendConnection).getSchemaName();
        verify(backendConnection, times(10)).setCurrentSchema(anyString());
    }
    
    @Test
    public void assertExecuteSchemaBroadcastFailed() {
        MockGlobalRegistryUtil.setLogicSchemas("schema", 5);
        setUnderlyingHandler(new CommandResponsePackets(new ErrPacket(1, new SQLException("no reason", "X999", -1))));
        String sql = "grant select on testdb.* to root@'%'";
        SchemaBroadcastBackendHandler schemaBroadcastBackendHandler = new SchemaBroadcastBackendHandler(1, sql, backendConnection, DatabaseType.MySQL, backendHandlerFactory);
        CommandResponsePackets actual = schemaBroadcastBackendHandler.execute();
        assertThat(actual.getHeadPacket(), instanceOf(ErrPacket.class));
        verify(backendConnection).getSchemaName();
        verify(backendConnection, times(5)).setCurrentSchema(anyString());
    }
    
    private void setUnderlyingHandler(final CommandResponsePackets commandResponsePackets) {
        BackendHandler backendHandler = mock(BackendHandler.class);
        when(backendHandler.execute()).thenReturn(commandResponsePackets);
        when(backendHandlerFactory.newTextProtocolInstance(anyInt(), anyString(), (BackendConnection) any(), (DatabaseType) any())).thenReturn(backendHandler);
    }
}


