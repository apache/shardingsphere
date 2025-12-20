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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.client.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.util.concurrent.Promise;
import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.client.MySQLServerVersion;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.client.PasswordEncryption;
import org.apache.shardingsphere.database.exception.mysql.vendor.MySQLVendorError;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLAuthenticationMethod;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLAuthenticationPlugin;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLCapabilityFlag;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLAuthMoreDataPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLAuthSwitchRequestPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLAuthSwitchResponsePacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLAuthenticationPluginData;
import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLHandshakePacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLHandshakeResponse41Packet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MySQLNegotiateHandlerTest {
    
    private static final String USER_NAME = "username";
    
    private static final String PASSWORD = "password";
    
    @Mock
    private ChannelHandlerContext channelHandlerContext;
    
    @Mock
    private Channel channel;
    
    @Mock
    private ChannelPipeline pipeline;
    
    @Mock
    private Promise<Object> authResultCallback;
    
    private MySQLNegotiateHandler mysqlNegotiateHandler;
    
    @BeforeEach
    void setUp() {
        lenient().when(channelHandlerContext.channel()).thenReturn(channel);
        lenient().when(channel.pipeline()).thenReturn(pipeline);
        mysqlNegotiateHandler = new MySQLNegotiateHandler(USER_NAME, PASSWORD, authResultCallback);
    }
    
    @ParameterizedTest
    @MethodSource("handshakeParams")
    void assertChannelReadHandshakeInitPacket(final String password, final boolean expectEmptyAuth) throws ReflectiveOperationException {
        MySQLNegotiateHandler handler = new MySQLNegotiateHandler(USER_NAME, password, authResultCallback);
        MySQLHandshakePacket handshakePacket = new MySQLHandshakePacket(0, false, new MySQLAuthenticationPluginData(new byte[8], new byte[12]));
        handshakePacket.setAuthPluginName(MySQLAuthenticationMethod.NATIVE);
        handler.channelRead(channelHandlerContext, handshakePacket);
        MySQLServerVersion serverVersion = (MySQLServerVersion) Plugins.getMemberAccessor().get(MySQLNegotiateHandler.class.getDeclaredField("serverVersion"), handler);
        assertThat(Plugins.getMemberAccessor().get(MySQLServerVersion.class.getDeclaredField("major"), serverVersion), is(5));
        assertThat(Plugins.getMemberAccessor().get(MySQLServerVersion.class.getDeclaredField("minor"), serverVersion), is(7));
        assertThat(Plugins.getMemberAccessor().get(MySQLServerVersion.class.getDeclaredField("series"), serverVersion), is(22));
        ArgumentCaptor<MySQLHandshakeResponse41Packet> responseCaptor = ArgumentCaptor.forClass(MySQLHandshakeResponse41Packet.class);
        verify(channel).writeAndFlush(responseCaptor.capture());
        MySQLHandshakeResponse41Packet actualResponse = responseCaptor.getValue();
        assertThat(actualResponse.getAuthResponse().length, expectEmptyAuth ? is(0) : not(0));
        assertThat(actualResponse.getCapabilityFlags(), is(calculateExpectedCapabilities()));
    }
    
    private int calculateExpectedCapabilities() {
        return MySQLCapabilityFlag.calculateCapabilityFlags(
                MySQLCapabilityFlag.CLIENT_LONG_PASSWORD,
                MySQLCapabilityFlag.CLIENT_LONG_FLAG,
                MySQLCapabilityFlag.CLIENT_PROTOCOL_41,
                MySQLCapabilityFlag.CLIENT_INTERACTIVE,
                MySQLCapabilityFlag.CLIENT_TRANSACTIONS,
                MySQLCapabilityFlag.CLIENT_SECURE_CONNECTION,
                MySQLCapabilityFlag.CLIENT_MULTI_STATEMENTS,
                MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH);
    }
    
    @Test
    void assertChannelReadOkPacket() throws ReflectiveOperationException {
        MySQLOKPacket okPacket = new MySQLOKPacket(0);
        MySQLServerVersion serverVersion = new MySQLServerVersion("5.5.0-log");
        Plugins.getMemberAccessor().set(MySQLNegotiateHandler.class.getDeclaredField("serverVersion"), mysqlNegotiateHandler, serverVersion);
        mysqlNegotiateHandler.channelRead(channelHandlerContext, okPacket);
        verify(pipeline).remove(mysqlNegotiateHandler);
        verify(authResultCallback).setSuccess(serverVersion);
    }
    
    @Test
    void assertChannelReadErrorPacket() {
        MySQLErrPacket errorPacket = new MySQLErrPacket(
                new SQLException(MySQLVendorError.ER_NO_DB_ERROR.getReason(), MySQLVendorError.ER_NO_DB_ERROR.getSqlState().getValue(), MySQLVendorError.ER_NO_DB_ERROR.getVendorCode()));
        assertThrows(PipelineInternalException.class, () -> mysqlNegotiateHandler.channelRead(channelHandlerContext, errorPacket));
        verify(channel).close();
    }
    
    @Test
    void assertHandleCachingSha2RequestPublicKey() throws ReflectiveOperationException {
        mysqlNegotiateHandler.channelRead(channelHandlerContext, new MySQLAuthMoreDataPacket(new byte[]{4}));
        assertThat(captureAuthSwitchResponse().getAuthPluginResponse()[0], is((byte) 2));
        assertTrue((boolean) Plugins.getMemberAccessor().get(MySQLNegotiateHandler.class.getDeclaredField("publicKeyRequested"), mysqlNegotiateHandler));
    }
    
    @Test
    void assertHandleCachingSha2SkipPublicKeyRequest() throws ReflectiveOperationException {
        mysqlNegotiateHandler.channelRead(channelHandlerContext, new MySQLAuthMoreDataPacket(new byte[]{1}));
        verify(channel, never()).writeAndFlush(ArgumentMatchers.any());
        assertFalse((Boolean) Plugins.getMemberAccessor().get(MySQLNegotiateHandler.class.getDeclaredField("publicKeyRequested"), mysqlNegotiateHandler));
    }
    
    @ParameterizedTest
    @MethodSource("authSwitchParams")
    void assertChannelReadAuthSwitchRequestForPlugin(final String pluginName, final byte[] expectedResponse) {
        mysqlNegotiateHandler.channelRead(channelHandlerContext, new MySQLAuthSwitchRequestPacket(pluginName, new MySQLAuthenticationPluginData(seedBytesPart1(), seedBytesPart2())));
        assertThat(captureAuthSwitchResponse().getAuthPluginResponse(), is(expectedResponse));
    }
    
    @ParameterizedTest
    @MethodSource("publicKeyEncryptParams")
    void assertHandleCachingSha2EncryptWithPublicKey(final String serverVersion) throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(MySQLNegotiateHandler.class.getDeclaredField("publicKeyRequested"), mysqlNegotiateHandler, true);
        Plugins.getMemberAccessor().set(MySQLNegotiateHandler.class.getDeclaredField("serverVersion"), mysqlNegotiateHandler, new MySQLServerVersion(serverVersion));
        Plugins.getMemberAccessor().set(MySQLNegotiateHandler.class.getDeclaredField("seed"), mysqlNegotiateHandler, authenticationPluginSeed());
        mysqlNegotiateHandler.channelRead(channelHandlerContext, new MySQLAuthMoreDataPacket(publicKey().getBytes()));
        MySQLAuthSwitchResponsePacket response = captureAuthSwitchResponse();
        assertTrue(response.getAuthPluginResponse().length > 0);
    }
    
    private String publicKey() {
        return "-----BEGIN PUBLIC KEY-----\n"
                + "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAL8eq+i+GtqR4344d18PT9bjK5YfX/8r\n"
                + "O8uRAZ3kKQEiC5EvhczHxVn9Yx8RJWb1x1oGf4bm/FYnGV8eK3opg+cCAwEAAQ==\n"
                + "-----END PUBLIC KEY-----";
    }
    
    private MySQLAuthSwitchResponsePacket captureAuthSwitchResponse() {
        ArgumentCaptor<MySQLAuthSwitchResponsePacket> responseCaptor = ArgumentCaptor.forClass(MySQLAuthSwitchResponsePacket.class);
        verify(channel).writeAndFlush(responseCaptor.capture());
        return responseCaptor.getValue();
    }
    
    private static Stream<Arguments> handshakeParams() {
        return Stream.of(Arguments.of(PASSWORD, false), Arguments.of("", true));
    }
    
    @SneakyThrows(NoSuchAlgorithmException.class)
    private static Stream<Arguments> authSwitchParams() {
        return Stream.of(
                Arguments.of(MySQLAuthenticationPlugin.NATIVE.getPluginName(), PasswordEncryption.encryptWithMySQL41(PASSWORD.getBytes(), authenticationPluginSeed())),
                Arguments.of(MySQLAuthenticationPlugin.CACHING_SHA2.getPluginName(), PasswordEncryption.encryptWithSha2(PASSWORD.getBytes(), authenticationPluginSeed())),
                Arguments.of(MySQLAuthenticationPlugin.SHA256.getPluginName(), PASSWORD.getBytes())
        );
    }
    
    private static byte[] authenticationPluginSeed() {
        byte[] result = new byte[seedBytesPart1().length + seedBytesPart2().length];
        System.arraycopy(seedBytesPart1(), 0, result, 0, seedBytesPart1().length);
        System.arraycopy(seedBytesPart2(), 0, result, seedBytesPart1().length, seedBytesPart2().length);
        return result;
    }
    
    private static byte[] seedBytesPart1() {
        return new byte[]{0, 1, 2, 3, 4, 5, 6, 7};
    }
    
    private static byte[] seedBytesPart2() {
        return new byte[]{8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19};
    }
    
    private static Stream<Arguments> publicKeyEncryptParams() {
        return Stream.of(Arguments.of("8.0.5"), Arguments.of("5.7.22"));
    }
}
