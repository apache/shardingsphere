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

package org.apache.shardingsphere.shardingproxy.frontend.mysql;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.context.GlobalContext;
import org.apache.shardingsphere.shardingproxy.frontend.ConnectionIdGenerator;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.handshake.MySQLHandshakePacket;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLFrontendEngineTest {
    
    private MySQLFrontendEngine mysqlFrontendEngine;
    
    @Mock
    private ChannelHandlerContext context;
    
    @Before
    @SneakyThrows
    public void resetConnectionIdGenerator() {
        Field field = ConnectionIdGenerator.class.getDeclaredField("currentId");
        field.setAccessible(true);
        field.set(ConnectionIdGenerator.getInstance(), 0);
        mysqlFrontendEngine = new MySQLFrontendEngine();
    }
    
    @Test
    public void assertHandshake() {
        mysqlFrontendEngine.handshake(context, mock(BackendConnection.class));
        verify(context).writeAndFlush(isA(MySQLHandshakePacket.class));
    }
    
    @Test
    public void assertAuthWhenLoginSuccess() throws ReflectiveOperationException {
        Authentication authentication = new Authentication("", "");
        setAuthentication(authentication);
        assertTrue(mysqlFrontendEngine.auth(context, mock(ByteBuf.class), mock(BackendConnection.class)));
        verify(context).writeAndFlush(isA(MySQLOKPacket.class));
    }
    
    @Test
    public void assertAuthWhenLoginFailure() throws ReflectiveOperationException {
        Authentication authentication = new Authentication("root", "root");
        setAuthentication(authentication);
        assertTrue(mysqlFrontendEngine.auth(context, mock(ByteBuf.class), mock(BackendConnection.class)));
        verify(context).writeAndFlush(isA(MySQLErrPacket.class));
    }
    
    private void setAuthentication(final Object value) throws ReflectiveOperationException {
        Field field = GlobalContext.class.getDeclaredField("authentication");
        field.setAccessible(true);
        field.set(GlobalContext.getInstance(), value);
    }
}
