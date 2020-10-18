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

package org.apache.shardingsphere.proxy.frontend.mysql;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConnectionPhase;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.handshake.MySQLHandshakePacket;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.ProxyUser;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.schema.SchemaContexts;
import org.apache.shardingsphere.infra.context.schema.impl.StandardSchemaContexts;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorKernel;
import org.apache.shardingsphere.infra.schema.ShardingSphereSchema;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.auth.AuthenticationResult;
import org.apache.shardingsphere.proxy.frontend.connection.ConnectionIdGenerator;
import org.apache.shardingsphere.proxy.frontend.mysql.auth.MySQLAuthenticationEngine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLFrontendEngineTest {
    
    private static final String SCHEMA = "schema_%s";
    
    private MySQLFrontendEngine mysqlFrontendEngine;
    
    @Mock
    private ChannelHandlerContext context;
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private Channel channel;
    
    @Before
    public void setUp() {
        resetConnectionIdGenerator();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void resetConnectionIdGenerator() {
        Field field = ConnectionIdGenerator.class.getDeclaredField("currentId");
        field.setAccessible(true);
        field.set(ConnectionIdGenerator.getInstance(), 0);
        mysqlFrontendEngine = new MySQLFrontendEngine();
    }
    
    @Test
    public void assertHandshake() {
        assertTrue(mysqlFrontendEngine.getAuthEngine().handshake(context) > 0);
        verify(context).writeAndFlush(isA(MySQLHandshakePacket.class));
    }
    
    @Test
    public void assertAuthWhenLoginSuccess() {
        setConnectionPhase(MySQLConnectionPhase.AUTH_PHASE_FAST_PATH);
        ProxyUser proxyUser = new ProxyUser("", Collections.singleton("db1"));
        setAuthentication(proxyUser);
        when(payload.readStringNul()).thenReturn("root");
        AuthenticationResult actual = mysqlFrontendEngine.getAuthEngine().auth(context, payload);
        assertThat(actual.getUsername(), is("root"));
        assertNull(actual.getDatabase());
        assertTrue(actual.isFinished());
        verify(context).writeAndFlush(isA(MySQLOKPacket.class));
    }
    
    @Test
    public void assertAuthWhenLoginFailure() {
        setConnectionPhase(MySQLConnectionPhase.AUTH_PHASE_FAST_PATH);
        ProxyUser proxyUser = new ProxyUser("error", Collections.singleton("db1"));
        setAuthentication(proxyUser);
        when(payload.readStringNul()).thenReturn("root");
        when(payload.readStringNulByBytes()).thenReturn("root".getBytes());
        when(channel.remoteAddress()).thenReturn(new InetSocketAddress("localhost", 3307));
        when(context.channel()).thenReturn(channel);
        AuthenticationResult actual = mysqlFrontendEngine.getAuthEngine().auth(context, payload);
        assertThat(actual.getUsername(), is("root"));
        assertNull(actual.getDatabase());
        assertTrue(actual.isFinished());
        verify(context).writeAndFlush(isA(MySQLErrPacket.class));
    }

    @Test
    public void assertErrorMsgWhenLoginFailure() throws UnknownHostException {
        setConnectionPhase(MySQLConnectionPhase.AUTH_PHASE_FAST_PATH);
        ProxyUser proxyUser = new ProxyUser("error", Collections.singleton("db1"));
        setAuthentication(proxyUser);
        when(payload.readStringNul()).thenReturn("root");
        when(payload.readStringNulByBytes()).thenReturn("root".getBytes());
        when(context.channel()).thenReturn(channel);
        when(channel.remoteAddress()).thenReturn(new InetSocketAddress(InetAddress.getByAddress(new byte[] {(byte) 192, (byte) 168, (byte) 0, (byte) 102}), 3307));
        AuthenticationResult actual = mysqlFrontendEngine.getAuthEngine().auth(context, payload);
        assertThat(actual.getUsername(), is("root"));
        assertNull(actual.getDatabase());
        assertTrue(actual.isFinished());
        verify(context).writeAndFlush(argThat((ArgumentMatcher<MySQLErrPacket>) argument -> "Access denied for user 'root'@'192.168.0.102' (using password: YES)".equals(argument.getErrorMessage())));
    }
    
    private void setAuthentication(final ProxyUser proxyUser) {
        Authentication authentication = new Authentication();
        authentication.getUsers().put("root", proxyUser);
        initProxyContext(authentication);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setConnectionPhase(final MySQLConnectionPhase connectionPhase) {
        Field field = MySQLAuthenticationEngine.class.getDeclaredField("connectionPhase");
        field.setAccessible(true);
        field.set(mysqlFrontendEngine.getAuthEngine(), connectionPhase);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void initProxyContext(final Authentication authentication) {
        Field field = ProxyContext.getInstance().getClass().getDeclaredField("schemaContexts");
        field.setAccessible(true);
        field.set(ProxyContext.getInstance(), getSchemaContexts(authentication));
    }
    
    private SchemaContexts getSchemaContexts(final Authentication authentication) {
        return new StandardSchemaContexts(getSchemas(), mock(ExecutorKernel.class), authentication, new ConfigurationProperties(new Properties()), new MySQLDatabaseType());
    }
    
    private Map<String, ShardingSphereSchema> getSchemas() {
        Map<String, ShardingSphereSchema> result = new HashMap<>(10, 1);
        for (int i = 0; i < 10; i++) {
            String name = String.format(SCHEMA, i);
            ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
            result.put(name, schema);
        }
        return result;
    }
}
