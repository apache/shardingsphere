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
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.client.PasswordEncryption;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.client.MySQLServerVersion;
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
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MySQLNegotiateHandlerTest {
    
    private static final String USER_NAME = "username";
    
    private static final String PASSWORD = "password";
    
    @Mock
    private Promise<Object> authResultCallback;
    
    @Mock
    private ChannelHandlerContext channelHandlerContext;
    
    @Mock
    private Channel channel;
    
    @Mock
    private ChannelPipeline pipeline;
    
    private MySQLNegotiateHandler mysqlNegotiateHandler;
    
    @BeforeEach
    void setUp() {
        when(channelHandlerContext.channel()).thenReturn(channel);
        when(channel.pipeline()).thenReturn(pipeline);
        mysqlNegotiateHandler = new MySQLNegotiateHandler(USER_NAME, PASSWORD, authResultCallback);
    }
    
    @Test
    void assertChannelReadHandshakeInitPacket() throws ReflectiveOperationException {
        MySQLHandshakePacket handshakePacket = new MySQLHandshakePacket(0, false, new MySQLAuthenticationPluginData(new byte[8], new byte[12]));
        handshakePacket.setAuthPluginName(MySQLAuthenticationMethod.NATIVE);
        mysqlNegotiateHandler.channelRead(channelHandlerContext, handshakePacket);
        MySQLServerVersion serverVersion = (MySQLServerVersion) Plugins.getMemberAccessor().get(MySQLNegotiateHandler.class.getDeclaredField("serverVersion"), mysqlNegotiateHandler);
        assertThat(Plugins.getMemberAccessor().get(MySQLServerVersion.class.getDeclaredField("major"), serverVersion), is(5));
        assertThat(Plugins.getMemberAccessor().get(MySQLServerVersion.class.getDeclaredField("minor"), serverVersion), is(7));
        assertThat(Plugins.getMemberAccessor().get(MySQLServerVersion.class.getDeclaredField("series"), serverVersion), is(22));
        ArgumentCaptor<MySQLHandshakeResponse41Packet> responseCaptor = ArgumentCaptor.forClass(MySQLHandshakeResponse41Packet.class);
        verify(channel).writeAndFlush(responseCaptor.capture());
        MySQLHandshakeResponse41Packet actualResponse = responseCaptor.getValue();
        assertThat(actualResponse.getAuthResponse().length, not(0));
        assertThat(actualResponse.getCapabilityFlags(), is(calculateExpectedCapabilities()));
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
    void assertChannelReadHandshakeWithEmptyPassword() {
        MySQLNegotiateHandler handler = new MySQLNegotiateHandler(USER_NAME, "", authResultCallback);
        when(channelHandlerContext.channel()).thenReturn(channel);
        MySQLHandshakePacket handshakePacket = new MySQLHandshakePacket(0, false, new MySQLAuthenticationPluginData(new byte[8], new byte[12]));
        handler.channelRead(channelHandlerContext, handshakePacket);
        ArgumentCaptor<MySQLHandshakeResponse41Packet> responseCaptor = ArgumentCaptor.forClass(MySQLHandshakeResponse41Packet.class);
        verify(channel).writeAndFlush(responseCaptor.capture());
        assertThat(responseCaptor.getValue().getAuthResponse().length, is(0));
    }
    
    @Test
    void assertChannelReadAuthSwitchRequestForNativePlugin() throws NoSuchAlgorithmException {
        MySQLAuthSwitchRequestPacket request = new MySQLAuthSwitchRequestPacket(MySQLAuthenticationPlugin.NATIVE.getPluginName(), authPluginData(seedBytesPart1(), seedBytesPart2()));
        mysqlNegotiateHandler.channelRead(channelHandlerContext, request);
        MySQLAuthSwitchResponsePacket response = captureAuthSwitchResponse();
        assertThat(response.getAuthPluginResponse(), is(PasswordEncryption.encryptWithMySQL41(PASSWORD.getBytes(), authenticationPluginSeed())));
    }
    
    @Test
    void assertChannelReadAuthSwitchRequestForCachingSha2Plugin() throws NoSuchAlgorithmException {
        MySQLAuthSwitchRequestPacket request = new MySQLAuthSwitchRequestPacket(MySQLAuthenticationPlugin.CACHING_SHA2.getPluginName(), authPluginData(seedBytesPart1(), seedBytesPart2()));
        mysqlNegotiateHandler.channelRead(channelHandlerContext, request);
        MySQLAuthSwitchResponsePacket response = captureAuthSwitchResponse();
        assertThat(response.getAuthPluginResponse(),
                is(PasswordEncryption.encryptWithSha2(PASSWORD.getBytes(), authenticationPluginSeed())));
    }
    
    @Test
    void assertChannelReadAuthSwitchRequestForDefaultPlugin() {
        MySQLAuthSwitchRequestPacket request = new MySQLAuthSwitchRequestPacket(MySQLAuthenticationPlugin.SHA256.getPluginName(), authPluginData(seedBytesPart1(), seedBytesPart2()));
        mysqlNegotiateHandler.channelRead(channelHandlerContext, request);
        MySQLAuthSwitchResponsePacket response = captureAuthSwitchResponse();
        assertThat(response.getAuthPluginResponse(), is(PASSWORD.getBytes()));
    }
    
    @Test
    void assertHandleCachingSha2RequestPublicKey() throws ReflectiveOperationException {
        MySQLAuthMoreDataPacket authMoreDataPacket = new MySQLAuthMoreDataPacket(new byte[]{4});
        mysqlNegotiateHandler.channelRead(channelHandlerContext, authMoreDataPacket);
        MySQLAuthSwitchResponsePacket response = captureAuthSwitchResponse();
        assertThat(response.getAuthPluginResponse()[0], is((byte) 2));
        assertThat(Plugins.getMemberAccessor().get(MySQLNegotiateHandler.class.getDeclaredField("publicKeyRequested"), mysqlNegotiateHandler), is(true));
    }
    
    @Test
    void assertHandleCachingSha2SkipPublicKeyRequest() throws ReflectiveOperationException {
        mysqlNegotiateHandler.channelRead(channelHandlerContext, new MySQLAuthMoreDataPacket(new byte[]{1}));
        verify(channel, never()).writeAndFlush(ArgumentMatchers.any());
        assertThat((Boolean) Plugins.getMemberAccessor().get(MySQLNegotiateHandler.class.getDeclaredField("publicKeyRequested"), mysqlNegotiateHandler), is(false));
    }
    
    @Test
    void assertHandleCachingSha2EncryptWithOaep() throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(MySQLNegotiateHandler.class.getDeclaredField("publicKeyRequested"), mysqlNegotiateHandler, true);
        Plugins.getMemberAccessor().set(MySQLNegotiateHandler.class.getDeclaredField("serverVersion"), mysqlNegotiateHandler, new MySQLServerVersion("8.0.5"));
        Plugins.getMemberAccessor().set(MySQLNegotiateHandler.class.getDeclaredField("seed"), mysqlNegotiateHandler, authenticationPluginSeed());
        mysqlNegotiateHandler.channelRead(channelHandlerContext, new MySQLAuthMoreDataPacket(publicKey().getBytes()));
        MySQLAuthSwitchResponsePacket response = captureAuthSwitchResponse();
        assertThat(response.getAuthPluginResponse().length > 0, is(true));
    }
    
    @Test
    void assertHandleCachingSha2EncryptWithPkcs1() throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(MySQLNegotiateHandler.class.getDeclaredField("publicKeyRequested"), mysqlNegotiateHandler, true);
        Plugins.getMemberAccessor().set(MySQLNegotiateHandler.class.getDeclaredField("serverVersion"), mysqlNegotiateHandler, new MySQLServerVersion("5.7.22"));
        Plugins.getMemberAccessor().set(MySQLNegotiateHandler.class.getDeclaredField("seed"), mysqlNegotiateHandler, authenticationPluginSeed());
        mysqlNegotiateHandler.channelRead(channelHandlerContext, new MySQLAuthMoreDataPacket(publicKey().getBytes()));
        MySQLAuthSwitchResponsePacket response = captureAuthSwitchResponse();
        assertThat(response.getAuthPluginResponse().length > 0, is(true));
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
    
    private MySQLAuthSwitchResponsePacket captureAuthSwitchResponse() {
        ArgumentCaptor<MySQLAuthSwitchResponsePacket> responseCaptor = ArgumentCaptor.forClass(MySQLAuthSwitchResponsePacket.class);
        verify(channel).writeAndFlush(responseCaptor.capture());
        return responseCaptor.getValue();
    }
    
    private MySQLAuthenticationPluginData authPluginData(final byte[] seedPart1, final byte[] seedPart2) {
        return new MySQLAuthenticationPluginData(seedPart1, seedPart2);
    }
    
    private byte[] seedBytesPart1() {
        return new byte[]{0, 1, 2, 3, 4, 5, 6, 7};
    }
    
    private byte[] seedBytesPart2() {
        return new byte[]{8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19};
    }
    
    private byte[] authenticationPluginSeed() {
        byte[] result = new byte[seedBytesPart1().length + seedBytesPart2().length];
        System.arraycopy(seedBytesPart1(), 0, result, 0, seedBytesPart1().length);
        System.arraycopy(seedBytesPart2(), 0, result, seedBytesPart1().length, seedBytesPart2().length);
        return result;
    }
    
    private String publicKey() {
        return "-----BEGIN PUBLIC KEY-----\n"
                + "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAL8eq+i+GtqR4344d18PT9bjK5YfX/8r\n"
                + "O8uRAZ3kKQEiC5EvhczHxVn9Yx8RJWb1x1oGf4bm/FYnGV8eK3opg+cCAwEAAQ==\n"
                + "-----END PUBLIC KEY-----";
    }
}
