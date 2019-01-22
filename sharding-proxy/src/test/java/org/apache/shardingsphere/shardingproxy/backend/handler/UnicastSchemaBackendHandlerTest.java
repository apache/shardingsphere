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

package org.apache.shardingsphere.shardingproxy.backend.handler;

import lombok.SneakyThrows;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.shardingproxy.backend.MockGlobalRegistryUtil;
import org.apache.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.OKPacket;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class UnicastSchemaBackendHandlerTest {
    
    private BackendConnection backendConnection = new BackendConnection(TransactionType.LOCAL);
    
    @Mock
    private BackendHandlerFactory backendHandlerFactory;
    
    @Before
    public void setUp() {
        MockGlobalRegistryUtil.setLogicSchemas("schema", 10);
        setUnderlyingHandler(new CommandResponsePackets(new OKPacket(1)));
    }
    
    @Test
    public void assertExecuteWhileSchemaIsNull() {
        UnicastSchemaBackendHandler backendHandler = new UnicastSchemaBackendHandler(1, "show variable like %s", backendConnection);
        setBackendHandlerFactory(backendHandler);
        CommandResponsePackets actual = backendHandler.execute();
        assertThat(actual.getHeadPacket(), instanceOf(OKPacket.class));
        backendHandler.execute();
    }
    
    @Test
    public void assertExecuteWhileSchemaNotNull() {
        backendConnection.setCurrentSchema("schema_0");
        UnicastSchemaBackendHandler backendHandler = new UnicastSchemaBackendHandler(1, "show variable like %s", backendConnection);
        setBackendHandlerFactory(backendHandler);
        CommandResponsePackets actual = backendHandler.execute();
        assertThat(actual.getHeadPacket(), instanceOf(OKPacket.class));
        backendHandler.execute();
    }
    
    private void setUnderlyingHandler(final CommandResponsePackets commandResponsePackets) {
        BackendHandler backendHandler = mock(BackendHandler.class);
        when(backendHandler.execute()).thenReturn(commandResponsePackets);
        when(backendHandlerFactory.newTextProtocolInstance(anyInt(), anyString(), (BackendConnection) any(), (DatabaseType) any())).thenReturn(backendHandler);
    }
    
    @SneakyThrows
    private void setBackendHandlerFactory(final UnicastSchemaBackendHandler unicastSchemaBackendHandler) {
        Field field = unicastSchemaBackendHandler.getClass().getDeclaredField("backendHandlerFactory");
        field.setAccessible(true);
        field.set(unicastSchemaBackendHandler, backendHandlerFactory);
    }
}
