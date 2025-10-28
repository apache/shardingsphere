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

package org.apache.shardingsphere.proxy.frontend.mysql.ssl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.internal.StringUtil;
import org.apache.shardingsphere.proxy.frontend.ssl.ProxySSLContext;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxySSLContext.class)
class MySQLSSLRequestHandlerTest {
    
    private static final byte[] MYSQL_SSL_REQUEST = StringUtil.decodeHexDump("2000000185aeff1900000001080000000000000000000000000000000000000000000000");
    
    private static final byte[] FAKE_TLS_HANDSHAKE = StringUtil.decodeHexDump("1603010000");
    
    private static final byte[] MYSQL_NON_SSL_REQUEST = StringUtil.decodeHexDump("2000000185a6ff1900000001080000000000000000000000000000000000000000000000");
    
    @Test
    void assertReceiveSSLRequest() throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addFirst(MySQLSSLRequestHandler.class.getSimpleName(), new MySQLSSLRequestHandler());
        try (MockedConstruction<SslHandler> mockedConstruction = mockConstruction(SslHandler.class)) {
            channel.writeInbound(Unpooled.wrappedBuffer(MYSQL_SSL_REQUEST), Unpooled.wrappedBuffer(FAKE_TLS_HANDSHAKE));
            verify(mockedConstruction.constructed().get(0)).channelRead(any(ChannelHandlerContext.class), argThat(this::assertTLSHandshakeByteBuf));
        }
        assertNull(channel.pipeline().get(MySQLSSLRequestHandler.class));
    }
    
    private boolean assertTLSHandshakeByteBuf(final Object actual) {
        assertThat(ByteBufUtil.getBytes((ByteBuf) actual), is(FAKE_TLS_HANDSHAKE));
        return true;
    }
    
    @Test
    void assertReceiveHandshakeResponse() {
        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addFirst(MySQLSSLRequestHandler.class.getSimpleName(), new MySQLSSLRequestHandler());
        channel.writeInbound(Unpooled.wrappedBuffer(MYSQL_NON_SSL_REQUEST));
        ByteBuf actual = channel.readInbound();
        assertThat(ByteBufUtil.getBytes(actual), is(MYSQL_NON_SSL_REQUEST));
        assertNull(channel.pipeline().get(MySQLSSLRequestHandler.class));
    }
}
